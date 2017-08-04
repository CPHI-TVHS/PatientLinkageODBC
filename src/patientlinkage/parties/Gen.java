/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in
 * the editor.
 */
package patientlinkage.parties;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import cv.CVCompEnv;
import flexsc.CompEnv;
import flexsc.CompPool;
import flexsc.Mode;
import flexsc.Party;
import gc.GCGen;
import patientlinkage.DataType.PatientLinkage;
import patientlinkage.DataType.PatientLinkage4GadgetInputs;
import patientlinkage.DataType.PatientLinkageResultMask;
import patientlinkage.GarbledCircuit.PatientLinkageGadget;
import patientlinkage.Util.Util;
import pm.PMCompEnv;

/**
 * @author cf
 * @param <T>
 */
public class Gen<T> extends network.Server {

	boolean[][][] bin_a;
	int len_b;
	Mode mode;
	int num_of_matched = 0;
	int numOfTasks;
	ArrayList<String> PartyA_IDs;
	ArrayList<String> PartyB_IDs;
	int port;
	ArrayList<PatientLinkage> res = null;
	boolean step2_usingmask = false;
	boolean verbose = false;
	boolean[][] z;

	public Gen(int port, Mode mode, int numOfTasks, boolean[][][] bin_a, boolean step2_usingmask, ArrayList<String> PartyA_IDs) {

		this.port = port;
		this.mode = mode;
		this.bin_a = bin_a;
		this.numOfTasks = numOfTasks;
		if (!step2_usingmask) {
			res = new ArrayList<>();
		}
		this.step2_usingmask = step2_usingmask;
		this.PartyA_IDs = PartyA_IDs;
	}

	public boolean[][] getMask() {

		return z;
	}

	public int getNumOfMatched() {

		return num_of_matched;
	}

	public ArrayList<String> getPartyB_IDs() {

		return PartyB_IDs;
	}

	public ArrayList<PatientLinkage> getRes() {

		return res;
	}

	public void implement() {

		final int[][] Range0 = Util.linspace(0, this.bin_a.length, numOfTasks);
		try {
			listen(port);
			CompEnv<T> gen = null;
			if (null != mode) {
				switch (mode) {
					case REAL:
						gen = (CompEnv<T>) new GCGen(is, os);
						break;
					case VERIFY:
						gen = (CompEnv<T>) new CVCompEnv(is, os, Party.Alice);
						break;
					case COUNT:
						gen = (CompEnv<T>) new PMCompEnv(is, os, Party.Alice);
						break;
					default:
						break;
				}
			}
			writeByte(os, ByteBuffer.allocate(4).putInt(bin_a.length).array());
			os.flush();
			final byte[] lenBytes = readBytes(is);
			this.len_b = ByteBuffer.wrap(lenBytes).getInt();
			this.z = new boolean[bin_a.length][this.len_b];
			// input
			System.out.println("initializing filter circuit...");
			final Object[] inputs = new Object[this.numOfTasks];
			final boolean[][][] bin_b = Util.generateDummyArray(bin_a, len_b);
			PatientLinkage4GadgetInputs.resetBar();
			PatientLinkage4GadgetInputs.all_progresses = bin_b.length * this.numOfTasks + bin_a.length;
			for (int i = 0; i < this.numOfTasks; i++) {
				final PatientLinkage4GadgetInputs<T> tmp0 = new PatientLinkage4GadgetInputs<>(Arrays.copyOfRange(bin_a, Range0[i][0], Range0[i][1]), gen, "Alice", i);
				final PatientLinkage4GadgetInputs<T> tmp1 = new PatientLinkage4GadgetInputs<>(bin_b, gen, "Bob", i);
				inputs[i] = new Object[] { tmp0, tmp1 };
			}
			System.out.println(String.format("[%s]%d%%    \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// compute
			System.out.println("computing filter circuit...");
			final CompPool<T> pool = new CompPool(gen, "localhost", this.port + 1);
			PatientLinkageGadget.resetBar();
			PatientLinkageGadget.all_progresses = Util.getPtLnkCnts(Range0, bin_b.length);
			final Object[] result = pool.runGadget(new PatientLinkageGadget(), inputs);
			final T[][] d = Util.<T>unifyArray(result, gen, this.bin_a.length);
			System.out.println(String.format("[%s]%d%%     \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// end
			// Output
			for (int i = 0; i < z.length; i++) {
				for (int j = 0; j < this.z[i].length; j++) {
					this.z[i][j] = gen.outputToAlice(d[i][j]);
					if (z[i][j]) {
						num_of_matched++;
						if (!step2_usingmask) {
							res.add(new PatientLinkage(i, j));
						}
					}
				}
			}
			os.flush();
			// end
			final ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(new Integer(num_of_matched));
			oos.writeObject(new PatientLinkageResultMask(z));
			if (!step2_usingmask) {
				oos.writeObject(res);
			}
			oos.flush();
			oos.writeObject(this.PartyA_IDs);
			oos.flush();
			final ObjectInputStream ois = new ObjectInputStream(is);
			this.PartyB_IDs = (ArrayList<String>) ois.readObject();
			pool.finalize();
			disconnect();
			int match_num = 0;
			if (this.verbose) {
				for (int i = 0; i < d.length; i++) {
					for (int j = 0; j < d[i].length; j++) {
						if (z[i][j]) {
							match_num++;
							System.out.println(i + " -> " + j);
						}
					}
				}
				System.out.println("the num of matches records: " + match_num);
			}
		} catch (final Exception ex) {
			Logger.getLogger(Env.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}

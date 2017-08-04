/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in
 * the editor.
 */
package patientlinkage.parties;

import static network.Server.readBytes;
import static network.Server.writeByte;

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
import gc.GCEva;
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
public class Env<T> extends network.Client {

	String addr;
	boolean[][][] bin_b;
	int len_a;
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

	/**
	 * @param addr
	 * @param port
	 * @param mode
	 * @param numOfTasks
	 * @param bin_b
	 * @param step2_usingmask
	 * @param PartyB_IDs
	 */
	public Env(String addr, int port, Mode mode, int numOfTasks, boolean[][][] bin_b, boolean step2_usingmask, ArrayList<String> PartyB_IDs) {

		this.addr = addr;
		this.port = port;
		this.mode = mode;
		this.bin_b = bin_b;
		if (!step2_usingmask) {
			res = new ArrayList<>();
		}
		this.numOfTasks = numOfTasks;
		this.step2_usingmask = step2_usingmask;
		this.PartyB_IDs = PartyB_IDs;
	}

	public boolean[][] getMask() {

		return z;
	}

	public int getNumOfMatched() {

		return num_of_matched;
	}

	public ArrayList<String> getPartyA_IDs() {

		return PartyA_IDs;
	}

	public ArrayList<PatientLinkage> getRes() {

		return res;
	}

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	public void implement() {

		try {
			connect(addr, port);
			CompEnv<T> eva = null;
			if (null != mode) {
				switch (mode) {
					case REAL:
						eva = (CompEnv<T>) new GCEva(is, os);
						break;
					case VERIFY:
						eva = (CompEnv<T>) new CVCompEnv(is, os, Party.Bob);
						break;
					case COUNT:
						eva = (CompEnv<T>) new PMCompEnv(is, os, Party.Bob);
						break;
					default:
						break;
				}
			}
			final byte[] lenBytes = readBytes(is);
			this.len_a = ByteBuffer.wrap(lenBytes).getInt();
			writeByte(os, ByteBuffer.allocate(4).putInt(bin_b.length).array());
			os.flush();
			this.z = new boolean[len_a][bin_b.length];
			// input
			final Object[] inputs = new Object[this.numOfTasks];
			final boolean[][][] bin_a = Util.generateDummyArray(bin_b, len_a);
			final int[][] Range0 = Util.linspace(0, this.len_a, numOfTasks);
			System.out.println("initializing filter circuit...");
			PatientLinkage4GadgetInputs.resetBar();
			PatientLinkage4GadgetInputs.all_progresses = len_a + this.bin_b.length * this.numOfTasks;
			for (int i = 0; i < this.numOfTasks; i++) {
				final PatientLinkage4GadgetInputs<T> tmp0 = new PatientLinkage4GadgetInputs<>(Arrays.copyOfRange(bin_a, Range0[i][0], Range0[i][1]), eva, "Alice", i);
				final PatientLinkage4GadgetInputs<T> tmp1 = new PatientLinkage4GadgetInputs<>(this.bin_b, eva, "Bob", i);
				inputs[i] = new Object[] { tmp0, tmp1 };
			}
			System.out.println(String.format("[%s]%d%%    \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// end
			// compute
			System.out.println("computing filter circuit...");
			final CompPool<T> pool = new CompPool(eva, this.addr, this.port + 1);
			PatientLinkageGadget.resetBar();
			PatientLinkageGadget.all_progresses = Util.getPtLnkCnts(Range0, this.bin_b.length);
			final Object[] result = pool.runGadget(new PatientLinkageGadget(), inputs);
			final T[][] d = Util.<T>unifyArray(result, eva, len_a);
			System.out.println(String.format("[%s]%d%%    \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// end
			// Output
			for (final T[] d1 : d) {
				for (final T d11 : d1) {
					eva.outputToAlice(d11);
				}
			}
			os.flush();
			// end
			final ObjectInputStream ois = new ObjectInputStream(is);
			num_of_matched = (Integer) ois.readObject();
			z = ((PatientLinkageResultMask) ois.readObject()).getMask();
			if (!step2_usingmask) {
				res = (ArrayList<PatientLinkage>) ois.readObject();
			}
			final ObjectOutputStream oos = new ObjectOutputStream(os);
			this.PartyA_IDs = (ArrayList<String>) ois.readObject();
			oos.writeObject(this.PartyB_IDs);
			oos.flush();
			pool.finalize();
			disconnect();
			System.out.println("pass here");
			if (this.verbose) {
				for (int n = 0; n < res.size(); n++) {
					final int[] link0 = res.get(n).getLinkage();
					System.out.println(link0[0] + " -> " + link0[1]);
				}
				System.out.println("the num of matches records: " + res.size());
			}
		} catch (final Exception ex) {
			Logger.getLogger(Env.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}

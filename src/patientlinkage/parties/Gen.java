/*
 *
 */
package patientlinkage.parties;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import flexsc.CompEnv;
import flexsc.CompPool;
import flexsc.Mode;
import flexsc.Party;
import network.Server;
import patientlinkage.DataType.PatientLinkage;
import patientlinkage.DataType.PatientLinkage4GadgetInputs;
import patientlinkage.DataType.PatientLinkageResultMask;
import patientlinkage.GarbledCircuit.PatientLinkageGadget;
import patientlinkage.Util.Util;

/**
 * @author cf
 * @param <T>
 */
public class Gen<T> extends PartyBase<T> {

	network.Server _server;

	/**
	 * @param port
	 * @param mode
	 * @param bin
	 * @param step2_using_mask
	 * @param num_of_tasks
	 * @param localIds
	 */
	public Gen(int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, LocalIds localIds) {

		super(port, mode, bin, step2_using_mask, num_of_tasks, localIds);
		_server = new Server();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Party getPartyName() {

		return Party.Alice;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void implement() {

		final int[][] Range0 = Util.linspace(0, getBin().length, getNumOfTasks());
		try {
			_server.listen(getPort());
			Party partyName = getPartyName();
			InputStream is = _server.is;
			OutputStream os = _server.os;
			CompEnv<T> gen = getCompEnv(partyName, is, os);
			Server.writeByte(os, ByteBuffer.allocate(4).putInt(getBin().length).array());
			os.flush();
			final byte[] lenBytes = Server.readBytes(is);
			setLength(ByteBuffer.wrap(lenBytes).getInt());
			setMask(new boolean[getBin().length][getLength()]);
			// input
			System.out.println("initializing filter circuit...");
			final Object[] inputs = new Object[getNumOfTasks()];
			final boolean[][][] bin_b = Util.generateDummyArray(getBin(), getLength());
			PatientLinkage4GadgetInputs.resetBar();
			PatientLinkage4GadgetInputs.all_progresses = bin_b.length * getNumOfTasks() + getBin().length;
			for (int i = 0; i < getNumOfTasks(); i++) {
				final PatientLinkage4GadgetInputs<T> tmp0 = new PatientLinkage4GadgetInputs<>(Arrays.copyOfRange(getBin(), Range0[i][0], Range0[i][1]), gen, "Alice", i);
				final PatientLinkage4GadgetInputs<T> tmp1 = new PatientLinkage4GadgetInputs<>(bin_b, gen, "Bob", i);
				inputs[i] = new Object[] { tmp0, tmp1 };
			}
			System.out.println(String.format("[%s]%d%%    \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// compute
			System.out.println("computing filter circuit...");
			final CompPool<T> pool = new CompPool(gen, "localhost", getPort() + 1);
			PatientLinkageGadget.resetBar();
			PatientLinkageGadget.all_progresses = Util.getPtLnkCnts(Range0, bin_b.length);
			final Object[] result = pool.runGadget(new PatientLinkageGadget(), inputs);
			final T[][] d = Util.<T>unifyArray(result, gen, getBin().length);
			System.out.println(String.format("[%s]%d%%     \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// end
			// Output
			for (int i = 0; i < getMask().length; i++) {
				for (int j = 0; j < getMask()[i].length; j++) {
					getMask()[i][j] = gen.outputToAlice(d[i][j]);
					if (getMask()[i][j]) {
						incrementNumberOfMatches();
						if (!doesStepTwoUseMask()) {
							getPatientLinkages().add(new PatientLinkage(i, j));
						}
					}
				}
			}
			os.flush();
			// end
			final ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(new Integer(getNumberOfMatched()));
			oos.writeObject(new PatientLinkageResultMask(getMask()));
			if (!doesStepTwoUseMask()) {
				oos.writeObject(getPatientLinkages());
			}
			oos.flush();
			oos.writeObject(getLocalIds());
			oos.flush();
			final ObjectInputStream ois = new ObjectInputStream(is);
			setRemoteIds((ArrayList<String>) ois.readObject());
			pool.finalize();
			_server.disconnect();
			int match_num = 0;
			if (isVerbose()) {
				for (int i = 0; i < d.length; i++) {
					for (int j = 0; j < d[i].length; j++) {
						if (getMask()[i][j]) {
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

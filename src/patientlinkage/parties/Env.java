/*
 *
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

import common.PartyName;
import flexsc.CompEnv;
import flexsc.CompPool;
import flexsc.Mode;
import flexsc.Party;
import network.Client;
import patientlinkage.DataType.PatientLinkage;
import patientlinkage.DataType.PatientLinkage4GadgetInputs;
import patientlinkage.DataType.PatientLinkageResultMask;
import patientlinkage.GarbledCircuit.PatientLinkageGadget;
import patientlinkage.Util.Util;

/**
 * @author cf
 * @param <T>
 */
public class Env<T> extends PartyBase<T> {

	network.Client _client;

	/**
	 * @param addr
	 * @param port
	 * @param mode
	 * @param bin
	 * @param step2_using_mask
	 * @param num_of_tasks
	 * @param remoteIds
	 */
	public Env(String addr, int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, RemoteIds remoteIds) {

		super(addr, port, mode, bin, step2_using_mask, num_of_tasks, remoteIds);
		_client = new Client();
	}

	/**
	 * @return
	 */
	@Override
	protected Party getPartyName() {

		return Party.Bob;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void implement() {

		try {
			_client.connect(getAddress(), getPort());
			Party partyName = getPartyName();
			CompEnv<T> eva = getCompEnv(partyName, _client.is, _client.os);
			final byte[] lenBytes = readBytes(_client.is);
			setLength(ByteBuffer.wrap(lenBytes).getInt());
			writeByte(_client.os, ByteBuffer.allocate(4).putInt(getBin().length).array());
			_client.os.flush();
			setMask(new boolean[getLength()][getBin().length]);
			// input
			final Object[] inputs = new Object[getNumOfTasks()];
			final boolean[][][] dummyBin = Util.generateDummyArray(getBin(), getLength());
			final int[][] Range0 = Util.linspace(0, getLength(), getNumOfTasks());
			log("initializing filter circuit..."); //$NON-NLS-1$
			PatientLinkage4GadgetInputs.resetBar();
			PatientLinkage4GadgetInputs.all_progresses = getLength() + getBin().length * getNumOfTasks();
			for (int index = 0; index < getNumOfTasks(); index++) {
				boolean[][][] dummyBinSubSet = Arrays.copyOfRange(dummyBin, Range0[index][0], Range0[index][1]);
				final PatientLinkage4GadgetInputs<T> tmp0 = new PatientLinkage4GadgetInputs<>(dummyBinSubSet, eva, PartyName.Alice, index);
				final PatientLinkage4GadgetInputs<T> tmp1 = new PatientLinkage4GadgetInputs<>(getBin(), eva, PartyName.Bob, index);
				inputs[index] = new Object[] { tmp0, tmp1 };
			}
			/**
			 *
			 */
			log(String.format("[%s]%d%%    \r", PatientLinkage4GadgetInputs.progress(100), 100)); //$NON-NLS-1$
			/**
			 *
			 */
			_client.os.flush();
			// end
			// compute
			/**
			 *
			 */
			log("computing filter circuit..."); //$NON-NLS-1$
			/**
			 *
			 */
			final CompPool<T> pool = new CompPool<>(eva, getAddress(), getPort() + 1);
			PatientLinkageGadget.resetBar();
			PatientLinkageGadget.all_progresses = Util.getPtLnkCnts(Range0, getBin().length);
			final Object[] result = pool.runGadget(new PatientLinkageGadget(), inputs);
			final T[][] d = Util.<T>unifyArray(result, eva, getLength());
			/**
			 *
			 */
			log(String.format("[%s]%d%%    \r", PatientLinkage4GadgetInputs.progress(100), 100)); //$NON-NLS-1$
			/**
			 *
			 */
			_client.os.flush();
			// end
			// Output
			for (final T[] d1 : d) {
				for (final T d11 : d1) {
					eva.outputToAlice(d11);
				}
			}
			_client.os.flush();
			// end
			final ObjectInputStream ois = new ObjectInputStream(_client.is);
			setNumberOfMatched((Integer) ois.readObject());
			setMask(((PatientLinkageResultMask) ois.readObject()).getMask());
			if (!doesStepTwoUseMask()) {
				setPatientLinkages((ArrayList<PatientLinkage>) ois.readObject());
			}
			final ObjectOutputStream oos = new ObjectOutputStream(_client.os);
			setRemoteIds((ArrayList<String>) ois.readObject());
			oos.writeObject(getLocalIds());
			oos.flush();
			pool.finalize();
			_client.disconnect();
			log("pass here"); //$NON-NLS-1$
			if (isVerbose()) {
				for (int n = 0; n < getPatientLinkages().size(); n++) {
					final int[] link0 = getPatientLinkages().get(n).getLinkage();
					log(link0[0] + " -> " + link0[1]); //$NON-NLS-1$
				}
				log("the num of matches records: " + getPatientLinkages().size()); //$NON-NLS-1$
			}
		} catch (final Exception ex) {
			Logger.getLogger(Env.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}

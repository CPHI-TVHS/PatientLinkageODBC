/*
 *
 */
package patientlinkage.parties;

import static network.Server.readBytes;
import static network.Server.writeByte;

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
	 * @param party_ids
	 */
	public Env(String addr, int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, ArrayList<String> party_ids) {

		super(addr, port, mode, bin, step2_using_mask, num_of_tasks, party_ids);
		_client = new Client();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void implement() {

		try {
			_client.connect(_state._address, _state._port);
			Party partyName = Party.Bob;
			InputStream is = _client.is;
			OutputStream os = _client.os;
			CompEnv<T> eva = getCompEnv(partyName, is, os);
			final byte[] lenBytes = readBytes(is);
			_state._length = ByteBuffer.wrap(lenBytes).getInt();
			writeByte(os, ByteBuffer.allocate(4).putInt(_state._bin.length).array());
			os.flush();
			_state._z = new boolean[_state._length][_state._bin.length];
			// input
			final Object[] inputs = new Object[_state._numOfTasks];
			final boolean[][][] bin_a = Util.generateDummyArray(_state._bin, _state._length);
			final int[][] Range0 = Util.linspace(0, _state._length, _state._numOfTasks);
			System.out.println("initializing filter circuit...");
			PatientLinkage4GadgetInputs.resetBar();
			PatientLinkage4GadgetInputs.all_progresses = _state._length + _state._bin.length * _state._numOfTasks;
			for (int i = 0; i < _state._numOfTasks; i++) {
				final PatientLinkage4GadgetInputs<T> tmp0 = new PatientLinkage4GadgetInputs<>(Arrays.copyOfRange(bin_a, Range0[i][0], Range0[i][1]), eva, "Alice", i);
				final PatientLinkage4GadgetInputs<T> tmp1 = new PatientLinkage4GadgetInputs<>(_state._bin, eva, "Bob", i);
				inputs[i] = new Object[] { tmp0, tmp1 };
			}
			System.out.println(String.format("[%s]%d%%    \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// end
			// compute
			System.out.println("computing filter circuit...");
			final CompPool<T> pool = new CompPool<>(eva, _state._address, _state._port + 1);
			PatientLinkageGadget.resetBar();
			PatientLinkageGadget.all_progresses = Util.getPtLnkCnts(Range0, _state._bin.length);
			final Object[] result = pool.runGadget(new PatientLinkageGadget(), inputs);
			final T[][] d = Util.<T>unifyArray(result, eva, _state._length);
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
			_state._num_of_matched = (Integer) ois.readObject();
			_state._z = ((PatientLinkageResultMask) ois.readObject()).getMask();
			if (!_state._step2_usingmask) {
				_state._patientLinkages = (ArrayList<PatientLinkage>) ois.readObject();
			}
			final ObjectOutputStream oos = new ObjectOutputStream(os);
			_state._partyA_IDs = (ArrayList<String>) ois.readObject();
			oos.writeObject(_state._partyB_IDs);
			oos.flush();
			pool.finalize();
			_client.disconnect();
			System.out.println("pass here");
			if (_state._verbose) {
				for (int n = 0; n < _state._patientLinkages.size(); n++) {
					final int[] link0 = _state._patientLinkages.get(n).getLinkage();
					System.out.println(link0[0] + " -> " + link0[1]);
				}
				System.out.println("the num of matches records: " + _state._patientLinkages.size());
			}
		} catch (final Exception ex) {
			Logger.getLogger(Env.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}

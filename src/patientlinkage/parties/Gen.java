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
	 * @param party_ids
	 */
	public Gen(int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, ArrayList<String> party_ids) {

		super(port, mode, bin, step2_using_mask, num_of_tasks, party_ids);
		_server = new Server();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void implement() {

		final int[][] Range0 = Util.linspace(0, _state._bin.length, _state._numOfTasks);
		try {
			_server.listen(_state._port);
			Party partyName = Party.Alice;
			InputStream is = _server.is;
			OutputStream os = _server.os;
			CompEnv<T> gen = getCompEnv(partyName, is, os);
			Server.writeByte(os, ByteBuffer.allocate(4).putInt(_state._bin.length).array());
			os.flush();
			final byte[] lenBytes = Server.readBytes(is);
			_state._length = ByteBuffer.wrap(lenBytes).getInt();
			_state._z = new boolean[_state._bin.length][_state._length];
			// input
			System.out.println("initializing filter circuit...");
			final Object[] inputs = new Object[_state._numOfTasks];
			final boolean[][][] bin_b = Util.generateDummyArray(_state._bin, _state._length);
			PatientLinkage4GadgetInputs.resetBar();
			PatientLinkage4GadgetInputs.all_progresses = bin_b.length * _state._numOfTasks + _state._bin.length;
			for (int i = 0; i < _state._numOfTasks; i++) {
				final PatientLinkage4GadgetInputs<T> tmp0 = new PatientLinkage4GadgetInputs<>(Arrays.copyOfRange(_state._bin, Range0[i][0], Range0[i][1]), gen, "Alice", i);
				final PatientLinkage4GadgetInputs<T> tmp1 = new PatientLinkage4GadgetInputs<>(bin_b, gen, "Bob", i);
				inputs[i] = new Object[] { tmp0, tmp1 };
			}
			System.out.println(String.format("[%s]%d%%    \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// compute
			System.out.println("computing filter circuit...");
			final CompPool<T> pool = new CompPool(gen, "localhost", _state._port + 1);
			PatientLinkageGadget.resetBar();
			PatientLinkageGadget.all_progresses = Util.getPtLnkCnts(Range0, bin_b.length);
			final Object[] result = pool.runGadget(new PatientLinkageGadget(), inputs);
			final T[][] d = Util.<T>unifyArray(result, gen, _state._bin.length);
			System.out.println(String.format("[%s]%d%%     \r", PatientLinkage4GadgetInputs.progress(100), 100));
			os.flush();
			// end
			// Output
			for (int i = 0; i < _state._z.length; i++) {
				for (int j = 0; j < _state._z[i].length; j++) {
					_state._z[i][j] = gen.outputToAlice(d[i][j]);
					if (_state._z[i][j]) {
						_state._num_of_matched++;
						if (!_state._step2_usingmask) {
							_state._patientLinkages.add(new PatientLinkage(i, j));
						}
					}
				}
			}
			os.flush();
			// end
			final ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(new Integer(_state._num_of_matched));
			oos.writeObject(new PatientLinkageResultMask(_state._z));
			if (!_state._step2_usingmask) {
				oos.writeObject(_state._patientLinkages);
			}
			oos.flush();
			oos.writeObject(_state._partyA_IDs);
			oos.flush();
			final ObjectInputStream ois = new ObjectInputStream(is);
			_state._partyB_IDs = (ArrayList<String>) ois.readObject();
			pool.finalize();
			_server.disconnect();
			int match_num = 0;
			if (_state._verbose) {
				for (int i = 0; i < d.length; i++) {
					for (int j = 0; j < d[i].length; j++) {
						if (_state._z[i][j]) {
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

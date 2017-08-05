/**
 *
 */
package patientlinkage.parties;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import cv.CVCompEnv;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;
import gc.GCEva;
import patientlinkage.DataType.PatientLinkage;
import pm.PMCompEnv;

/**
 * The Interface IParty.
 *
 * @author Dax Westerman
 * @param <T> the generic type
 */
public abstract class PartyBase<T> {

	/** The state. */
	CommunicationState _state;

	/**
	 * @param port
	 * @param mode
	 * @param bin
	 * @param step2_using_mask
	 * @param num_of_tasks
	 * @param party_ids
	 */
	public PartyBase(int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, ArrayList<String> party_ids) {

		_state = new CommunicationState(port, mode, bin, step2_using_mask, num_of_tasks, party_ids);
	}

	/**
	 * @param addr
	 * @param port
	 * @param mode
	 * @param bin
	 * @param step2_using_mask
	 * @param num_of_tasks
	 * @param party_ids
	 */
	public PartyBase(String addr, int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, ArrayList<String> party_ids) {

		_state = new CommunicationState(addr, port, mode, bin, step2_using_mask, num_of_tasks, party_ids);
	}

	@SuppressWarnings("unchecked")
	protected CompEnv<T> getCompEnv(Party partyName, InputStream is, OutputStream os) throws Exception {

		CompEnv<T> eva = null;
		if (null != _state.getMode()) {
			switch (_state.getMode()) {
				case REAL: {
					eva = (CompEnv<T>) new GCEva(is, os);
					break;
				}
				case VERIFY: {
					eva = (CompEnv<T>) new CVCompEnv(is, os, partyName);
					break;
				}
				case COUNT: {
					eva = (CompEnv<T>) new PMCompEnv(is, os, partyName);
					break;
				}
				default: {
					break;
				}
			}
		}
		return eva;
	}

	/**
	 * @return
	 */
	public boolean[][] getMask() {

		return _state.getZ();
	}

	/**
	 * @return
	 */
	public int getNumOfMatched() {

		return _state.getNum_of_matched();
	}

	/**
	 * @return
	 */
	public ArrayList<String> getPartyIds() {

		return _state.getPartyA_IDs();
	}

	/**
	 * @return
	 */
	public ArrayList<PatientLinkage> getPatientLinkages() {

		return _state.getPatientLinkages();
	}

	/**
	 * Implement.
	 */
	abstract void implement();
}
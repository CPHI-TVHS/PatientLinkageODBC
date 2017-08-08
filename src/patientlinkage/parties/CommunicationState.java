/**
 *
 */
package patientlinkage.parties;

import java.util.ArrayList;

import flexsc.Mode;
import patientlinkage.DataType.PatientLinkage;

/**
 * @author Dax Westerman
 */
public abstract class CommunicationState implements IState {

	private String _address;
	private final boolean[][][] _bin;
	private int _length;
	private boolean[][] _mask;
	private final Mode _mode;
	private int _number_of_matched = 0;
	private final int _number_of_tasks;
	private ArrayList<PatientLinkage> _patient_linkages = null;
	private final int _port;
	private boolean _step_2_is_using_mask = false;
	private final boolean _verbose = false;
	private final PartyIds _localIds;
	private RemoteIds _remoteIds;

	/**
	 * @param port
	 * @param mode
	 * @param bin
	 * @param step2_is_using_mask
	 * @param number_of_tasks
	 * @param localIds
	 */
	public CommunicationState(int port, Mode mode, boolean[][][] bin, boolean step2_is_using_mask, int number_of_tasks, PartyIds localIds) {

		_address = null;
		_port = port;
		_mode = mode;
		_bin = bin;
		if (!step2_is_using_mask) {
			_patient_linkages = new ArrayList<>();
		}
		_number_of_tasks = number_of_tasks;
		_step_2_is_using_mask = step2_is_using_mask;
		_localIds = localIds;
	}

	/**
	 * @param address
	 * @param port
	 * @param mode
	 * @param bin
	 * @param step2_using_mask
	 * @param num_of_tasks
	 * @param localIds
	 */
	public CommunicationState(String address, int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, PartyIds localIds) {

		this(port, mode, bin, step2_using_mask, num_of_tasks, localIds);
		_address = address;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean doesStepTwoUseMask() {

		return _step_2_is_using_mask;
	}

	/**
	 * @return the address
	 */
	@Override
	public String getAddress() {

		return _address;
	}

	/**
	 * @return the bin
	 */
	@Override
	public boolean[][][] getBin() {

		return _bin;
	}

	/**
	 * @return the length
	 */
	@Override
	public int getLength() {

		return _length;
	}

	/**
	 * @return the party_A_Ids
	 */
	public LocalIds getLocalIds() {

		return (LocalIds) _localIds;
	}

	/**
	 * @return the z
	 */
	public boolean[][] getMask() {

		return _mask;
	}

	/**
	 * @return the mode
	 */
	public Mode getMode() {

		return _mode;
	}

	/**
	 * @return the num_of_matched
	 */
	public int getNumberOfMatched() {

		return _number_of_matched;
	}

	/**
	 * @return the numOfTasks
	 */
	@Override
	public int getNumOfTasks() {

		return _number_of_tasks;
	}

	/**
	 * @return the patientLinkages
	 */
	public ArrayList<PatientLinkage> getPatientLinkages() {

		return _patient_linkages;
	}

	/**
	 * @return the port
	 */
	@Override
	public int getPort() {

		return _port;
	}

	/**
	 * @return the party_B_Ids
	 */
	public RemoteIds getRemoteIds() {

		return _remoteIds;
	}

	/**
	 * @return the verbose
	 */
	@Override
	public boolean isVerbose() {

		return _verbose;
	}

	/**
	 * @param length
	 */
	@Override
	public void setLength(int length) {

		_length = length;
	}

	/**
	 * @param mask
	 */
	@Override
	public void setMask(boolean[][] mask) {

		_mask = mask;
	}

	/**
	 * @param number_of_matched
	 */
	@Override
	public void setNumberOfMatched(int number_of_matched) {

		_number_of_matched = number_of_matched;
	}

	/**
	 * @param patient_linkages
	 */
	@Override
	public void setPatientLinkages(ArrayList<PatientLinkage> patient_linkages) {

		_patient_linkages = patient_linkages;
	}

	/**
	 * @param readObject
	 */
	protected void setRemoteIds(ArrayList<String> readObject) {

		_remoteIds.set(readObject);
	}
}

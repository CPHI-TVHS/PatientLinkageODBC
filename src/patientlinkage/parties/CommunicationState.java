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
public class CommunicationState {

	private final String _address;
	private final boolean[][][] _bin;
	private int _length;
	private final Mode _mode;
	private final int _num_of_matched = 0;
	private final int _numOfTasks;
	private ArrayList<String> _partyA_IDs;
	private final ArrayList<String> _partyB_IDs;
	private ArrayList<PatientLinkage> _patientLinkages = null;
	private final int _port;
	private boolean _step2_usingmask = false;
	private final boolean _verbose = false;
	private boolean[][] _z;

	/**
	 * @param port
	 * @param mode
	 * @param bin_a
	 * @param step2_usingmask
	 * @param numOfTasks
	 * @param partyA_IDs
	 */
	public CommunicationState(int port, Mode mode, boolean[][][] bin_a, boolean step2_usingmask, int numOfTasks, ArrayList<String> partyA_IDs) {

		this(null, port, mode, bin_a, step2_usingmask, numOfTasks, partyA_IDs);
	}

	/**
	 * @param addr
	 * @param port
	 * @param mode
	 * @param bin_b
	 * @param step2_usingmask
	 * @param numOfTasks
	 * @param partyB_IDs
	 */
	public CommunicationState(String addr, int port, Mode mode, boolean[][][] bin_b, boolean step2_usingmask, int numOfTasks, ArrayList<String> partyB_IDs) {

		this._address = addr;
		this._port = port;
		this._mode = mode;
		this._bin = bin_b;
		if (!step2_usingmask) {
			this._patientLinkages = new ArrayList<>();
		}
		this._numOfTasks = numOfTasks;
		this._step2_usingmask = step2_usingmask;
		this._partyB_IDs = partyB_IDs;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {

		return _address;
	}

	/**
	 * @return the bin
	 */
	public boolean[][][] getBin() {

		return _bin;
	}

	/**
	 * @return the length
	 */
	public int getLength() {

		return _length;
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
	public int getNum_of_matched() {

		return _num_of_matched;
	}

	/**
	 * @return the numOfTasks
	 */
	public int getNumOfTasks() {

		return _numOfTasks;
	}

	/**
	 * @return the partyA_IDs
	 */
	public ArrayList<String> getPartyA_IDs() {

		return _partyA_IDs;
	}

	/**
	 * @return the partyB_IDs
	 */
	public ArrayList<String> getPartyB_IDs() {

		return _partyB_IDs;
	}

	/**
	 * @return the patientLinkages
	 */
	public ArrayList<PatientLinkage> getPatientLinkages() {

		return _patientLinkages;
	}

	/**
	 * @return the port
	 */
	public int getPort() {

		return _port;
	}

	/**
	 * @return the z
	 */
	public boolean[][] getZ() {

		return _z;
	}

	/**
	 * @return the step2_usingmask
	 */
	public boolean isStep2_usingmask() {

		return _step2_usingmask;
	}

	/**
	 * @return the verbose
	 */
	public boolean isVerbose() {

		return _verbose;
	}
}

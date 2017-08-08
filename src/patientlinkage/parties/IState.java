/**
 *
 */
package patientlinkage.parties;

import java.util.ArrayList;

import patientlinkage.DataType.PatientLinkage;

/**
 * @author Dax Westerman
 */
public interface IState {

	/**
	 * @return
	 */
	boolean doesStepTwoUseMask();

	/**
	 * @return
	 */
	String getAddress();

	/**
	 * @return
	 */
	boolean[][][] getBin();

	/**
	 * @return
	 */
	int getLength();

	/**
	 * @return
	 */
	int getNumOfTasks();

	/**
	 * @return
	 */
	int getPort();

	/**
	 *
	 */
	void incrementNumberOfMatches();

	/**
	 * @return
	 */
	boolean isVerbose();

	/**
	 * @param int1
	 */
	void setLength(int int1);

	/**
	 * @param bs
	 */
	void setMask(boolean[][] bs);

	/**
	 * @param readObject
	 */
	void setNumberOfMatched(int readObject);

	/**
	 * @param readObject
	 */
	void setPatientLinkages(ArrayList<PatientLinkage> readObject);
}
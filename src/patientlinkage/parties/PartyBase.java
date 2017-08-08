/**
 *
 */
package patientlinkage.parties;

import java.io.InputStream;
import java.io.OutputStream;

import cv.CVCompEnv;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;
import gc.GCEva;
import pm.PMCompEnv;

// TODO: Auto-generated Javadoc
/**
 * The Interface IParty.
 *
 * @author Dax Westerman
 * @param <T> the generic type
 */
public abstract class PartyBase<T> extends CommunicationState implements IState {

	/**
	 * Log.
	 *
	 * @param msg the msg
	 */
	protected static void log(String msg) {

		System.out.println(msg);
	}

	/**
	 * @param port
	 * @param mode
	 * @param bin
	 * @param step2_using_mask
	 * @param num_of_tasks
	 * @param localIds
	 */
	public PartyBase(int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, PartyIds localIds) {

		super(port, mode, bin, step2_using_mask, num_of_tasks, localIds);
	}

	/**
	 * @param addr
	 * @param port
	 * @param mode
	 * @param bin
	 * @param step2_using_mask
	 * @param num_of_tasks
	 * @param localIds
	 */
	public PartyBase(String addr, int port, Mode mode, boolean[][][] bin, boolean step2_using_mask, int num_of_tasks, PartyIds localIds) {

		super(addr, port, mode, bin, step2_using_mask, num_of_tasks, localIds);
	}

	/**
	 * Gets the comp env.
	 *
	 * @param partyName the party name
	 * @param is the is
	 * @param os the os
	 * @return the comp env
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	protected CompEnv<T> getCompEnv(Party partyName, InputStream is, OutputStream os) throws Exception {

		CompEnv<T> eva = null;
		if (null != getMode()) {
			switch (getMode()) {
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
	 * Gets the party name.
	 *
	 * @return the party name
	 */
	protected abstract Party getPartyName();

	/**
	 * Implement.
	 */
	public abstract void implement();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void incrementNumberOfMatches() {

		int numOfMatched = getNumberOfMatched();
		setNumberOfMatched(numOfMatched++);
	}
}
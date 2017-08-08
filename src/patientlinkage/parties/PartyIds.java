/**
 *
 */
package patientlinkage.parties;

import java.util.ArrayList;

/**
 * @author Dax Westerman
 */
public class PartyIds {

	ArrayList<String> _ids;

	/**
	 * @param ids
	 */
	public PartyIds(ArrayList<String> ids) {

		set(ids);
	}

	/**
	 * @param readObject
	 */
	public void set(ArrayList<String> readObject) {

		_ids = readObject;
	}
}

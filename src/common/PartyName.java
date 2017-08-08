/**
 *
 */
package common;

// TODO: Auto-generated Javadoc
/**
 * The Enum PartyName.
 *
 * @author Dax Westerman
 */
public enum PartyName {
	/** The Alice. */
	Alice("Alice"), //$NON-NLS-1$
	/** The Bob. */
	Bob("Bob"); //$NON-NLS-1$

	/** The name. */
	private String _name;

	/**
	 * Instantiates a new party name.
	 *
	 * @param name the name
	 */
	PartyName(String name) {

		_name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		return _name;
	}
}

/*
 *
 */
package patientlinkage.DataType;

import java.util.logging.Level;
import java.util.logging.Logger;

import common.PartyName;
import flexsc.CompEnv;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientLinkage4GadgetInputs.
 *
 * @author cf
 * @param <T> the generic type
 */
public class PatientLinkage4GadgetInputs<T> {

	/** The all progresses. */
	public static int all_progresses = 0;
	/** The progress. */
	private static int progress = 0;
	/** The res. */
	private static StringBuilder RES = new StringBuilder();

	/**
	 * Progress.
	 *
	 * @param pct the pct
	 * @return the string
	 */
	public static String progress(int pct) {

		PatientLinkage4GadgetInputs.RES.delete(0, PatientLinkage4GadgetInputs.RES.length());
		final int numPounds = pct / 10;
		for (int i = 0; i <= numPounds; i++) {
			PatientLinkage4GadgetInputs.RES.append('#');
		}
		while (PatientLinkage4GadgetInputs.RES.length() <= 10) {
			PatientLinkage4GadgetInputs.RES.append(' ');
		}
		return PatientLinkage4GadgetInputs.RES.toString();
	}

	/**
	 * Reset bar.
	 */
	public static void resetBar() {

		all_progresses = 0;
		progress = 0;
		RES = new StringBuilder();
	}

	/** The Inputs. */
	private final T[][][] Inputs;
	/** The th ID. */
	int th_ID;

	/**
	 * Instantiates a new patient linkage 4 gadget inputs.
	 *
	 * @param Inputs the inputs
	 * @param gen the gen
	 * @param role the role
	 * @param th_ID the th ID
	 */
	public PatientLinkage4GadgetInputs(boolean[][][] Inputs, CompEnv<T> gen, PartyName role, int th_ID) {

		this.th_ID = th_ID;
		this.Inputs = gen.newTArray(Inputs.length, Inputs[0].length, 0);
		try {
			switch (role) {
				case Alice:
					for (int i = 0; i < Inputs.length; i++) {
						if (progress % 100 == 0) {
							final double tmp = progress * 100.0 / all_progresses;
							// System.out.println("tmp:" + tmp + "; progress:" + progress + "; all " + all_progresses);
							System.out.print(String.format("[%s]%.2f%%\r", progress((int) tmp), tmp));
						}
						progress++;
						for (int j = 0; j < Inputs[i].length; j++) {
							this.Inputs[i][j] = gen.inputOfAlice(Inputs[i][j]);
						}
					}
					break;
				case Bob:
					for (int i = 0; i < Inputs.length; i++) {
						if (progress % 100 == 0) {
							final double tmp = (float) progress * 100 / all_progresses;
							// System.out.println("tmp:" + tmp + "; progress:" + progress + "; all " + all_progresses);
							System.out.print(String.format("[%s]%.2f%%\r", progress((int) tmp), tmp));
						}
						progress++;
						for (int j = 0; j < Inputs[i].length; j++) {
							this.Inputs[i][j] = gen.inputOfBob(Inputs[i][j]);
						}
					}
					break;
				default:
					System.exit(1);
			}
		} catch (final Exception ex) {
			Logger.getLogger(PatientLinkage4GadgetInputs.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Instantiates a new patient linkage 4 gadget inputs.
	 *
	 * @param Inputs the inputs
	 */
	public PatientLinkage4GadgetInputs(T[][][] Inputs) {

		this.Inputs = Inputs;
	}

	/**
	 * Gets the inputs.
	 *
	 * @return the inputs
	 */
	public T[][][] getInputs() {

		return Inputs;
	}
}

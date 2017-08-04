/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in
 * the editor.
 */
package patientlinkage.DataType;

/**
 * @author cf
 */
public class PatientLinkageResultMask implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3547650484742623050L;
	boolean[][] mask;

	public PatientLinkageResultMask(boolean[][] mask) {

		this.mask = mask;
	}

	public boolean[][] getMask() {

		return mask;
	}

	public void setMask(boolean[][] mask) {

		this.mask = mask;
	}
}

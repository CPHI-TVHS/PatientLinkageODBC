/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in
 * the editor.
 */
package patientlinkage.DataType;

/**
 * @author cf
 */
public class DBProperty {

	int len;
	String prop;

	public DBProperty(String prop, int len) {

		this.prop = prop;
		this.len = len;
	}

	public int getLen() {

		return len;
	}

	public String getProp() {

		return prop;
	}
}

/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in
 * the editor.
 */
package patientlinkage.Util;

import static java.util.Arrays.copyOf;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang.StringUtils;

import flexsc.CompEnv;
import patientlinkage.DataType.DBConfig;
import patientlinkage.DataType.Helper;
import patientlinkage.DataType.PatientLinkage;

/**
 * @author cf
 */
@SuppressWarnings("unchecked")
public class Util {

	public static final int BYTE_BITS = 8;
	public static HashMap<Character, boolean[]> codebook;

	public static boolean[] bytes2boolean(byte[] vals) {

		final boolean[] ret = new boolean[BYTE_BITS * vals.length];
		for (int i = 0; i < vals.length; i++) {
			System.arraycopy(fromByte(vals[i]), 0, ret, i * BYTE_BITS, BYTE_BITS);
		}
		return ret;
	}

	public static boolean[][][] encodeCobinationAsJAMIA4Criteria(String[][] data1, int[][] properties_bytes) {

		// 12, 11, 9, 8
		assert data1[0].length == properties_bytes[0].length;
		final boolean[][][] ret = new boolean[data1.length][properties_bytes.length][];
		for (int i = 0; i < data1.length; i++) {
			for (int j = 0; j < properties_bytes.length; j++) {
				String temp = "";
				for (int k = 0; k < properties_bytes[j].length; k++) {
					temp += resizeString(data1[i][k], properties_bytes[j][k]);
				}
				ret[i][j] = bytes2boolean(temp.getBytes(StandardCharsets.US_ASCII));
			}
		}
		return ret;
	}

	public static boolean[] encodeStrByCodebook(String str, HashMap<Character, boolean[]> codebook_param, int len) {

		final char[] arr_ch = str.toCharArray();
		final boolean[] ret_barr = new boolean[arr_ch.length * len];
		int index = 0;
		for (final char ch : arr_ch) {
			System.arraycopy(codebook_param.get(ch), 0, ret_barr, index * len, len);
			index++;
		}
		return ret_barr;
	}

	public static boolean[][][] extractArray(boolean[][][] arr1, ArrayList<PatientLinkage> ptl_arr, String role) {

		final boolean[][][] res = new boolean[ptl_arr.size()][][];
		switch (role) {
			case "generator":
				int ind;
				for (int n = 0; n < ptl_arr.size(); n++) {
					ind = ptl_arr.get(n).getI();
					res[n] = arr1[ind];
				}
				break;
			case "evaluator":
				for (int n = 0; n < ptl_arr.size(); n++) {
					ind = ptl_arr.get(n).getJ();
					res[n] = arr1[ind];
				}
				break;
			default: {
				break;
			}
		}
		return res;
	}

	public static boolean[] fromByte(byte value) {

		final boolean[] res = new boolean[BYTE_BITS];
		for (int i = 0; i < BYTE_BITS; i++) {
			res[i] = (value >> i & 1) != 0;
		}
		return res;
	}

	public static boolean[] fromInt(int value, int width) {

		final boolean[] res = new boolean[width];
		for (int i = 0; i < width; i++) {
			res[i] = (value >> i & 1) != 0;
		}
		return res;
	}

	public static boolean[][][] generateDummyArray(boolean[][][] src) {

		final boolean[][][] retArr = new boolean[src.length][][];
		for (int i = 0; i < src.length; i++) {
			retArr[i] = new boolean[src[i].length][];
			for (int j = 0; j < src[i].length; j++) {
				retArr[i][j] = new boolean[src[i][j].length];
			}
		}
		return retArr;
	}

	public static boolean[][][] generateDummyArray(boolean[][][] src, int len) {

		final boolean[][][] retArr = new boolean[len][][];
		final int width = src[0].length;
		for (int i = 0; i < retArr.length; i++) {
			retArr[i] = new boolean[width][];
			for (int j = 0; j < width; j++) {
				retArr[i][j] = new boolean[src[0][j].length];
			}
		}
		return retArr;
	}

	public static int getPtLnkCnts(int[][] ranges1, int opp_num) {

		int ptLnkCnts = 0;
		for (final int[] ranges11 : ranges1) {
			ptLnkCnts += (ranges11[1] - ranges11[0]) * opp_num;
		}
		return ptLnkCnts;
	}

	public static void initialzingCodebook1() {

		codebook = new HashMap<>(40);
		for (char ch = 'a'; ch <= 'z'; ch++) {
			codebook.put(ch, fromInt(ch - 'a', 6));
		}
		for (char ch = '0'; ch <= '9'; ch++) {
			codebook.put(ch, fromInt(ch - '0' + 'z' - 'a' + 1, 6));
		}
		codebook.put(' ', fromInt('9' - '0' + 'z' - 'a' + 2, 6));
	}

	public static int[][] linspace(int pt0, int pt1, int num_of_intervals) {

		assert num_of_intervals > 0 : "math1.linspace: num of intervals > 0";
		final int[] ret = new int[num_of_intervals + 1];
		final int[][] ret1 = new int[num_of_intervals][2];
		ret[0] = pt0;
		ret[num_of_intervals] = pt1;
		ret1[0][0] = pt0;
		ret1[num_of_intervals - 1][1] = pt1;
		final int int_len = (pt1 - pt0) / num_of_intervals;
		for (int i = 1; i < num_of_intervals; i++) {
			ret[i] = ret[i - 1] + int_len;
			ret1[i - 1][1] = ret[i];
			ret1[i][0] = ret[i];
		}
		return ret1;
	}

	public static Helper readAndEncodeByHash(DBConfig db_config) {

		final Helper ret = new Helper();
		final ArrayList<boolean[][]> retArrList = new ArrayList<>();
		final Soundex sdx = new Soundex();
		final String connectionUrl = db_config.getG_url();
		Connection con = null;
		final String[] all_props = db_config.getAll_props();
		final int[][] prop_lens = db_config.getProps_len();
		ret.pros = all_props;
		ret.updatingrules(prop_lens);
		try {
			con = DriverManager.getConnection(connectionUrl);
			final Statement stmn = con.createStatement();
			String sql = "SELECT id";
			for (final String all_prop : all_props) {
				sql += ", ";
				sql += all_prop;
			}
			sql += " FROM ";
			sql += db_config.getDB_table();
			final ResultSet rs = stmn.executeQuery(sql);
			while (rs.next()) {
				ret.IDs.add(rs.getString("id"));
				final String[] coms_strs = new String[prop_lens.length];
				Arrays.fill(coms_strs, "");
				for (int i = 0; i < all_props.length; i++) {
					final String temp = rs.getString(i + 1).replace("-", "").toLowerCase();
					for (int j = 0; j < coms_strs.length; j++) {
						if (prop_lens[j][i] > Integer.MAX_VALUE / 2) {
							coms_strs[j] += sdx.soundex(temp).toLowerCase() + resizeString(temp, Integer.MAX_VALUE - prop_lens[j][i]);
						} else {
							coms_strs[j] += resizeString(temp, prop_lens[j][i]);
						}
					}
				}
				final boolean[][] bool_arr = new boolean[coms_strs.length][];
				for (int j = 0; j < coms_strs.length; j++) {
					bool_arr[j] = bytes2boolean(coms_strs[j].getBytes(StandardCharsets.US_ASCII));
				}
				retArrList.add(bool_arr);
			}
		} catch (final SQLException ex) {
			Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
		}
		ret.data_bin = new boolean[retArrList.size()][][];
		for (int i = 0; i < ret.data_bin.length; i++) {
			ret.data_bin[i] = retArrList.get(i);
		}
		return ret;
	}

	public static Helper readAndEncodeByHash(DBConfig db_config, int hash_len) {

		final Helper ret = new Helper();
		final ArrayList<boolean[][]> retArrList = new ArrayList<>();
		final Soundex sdx = new Soundex();
		final String[] all_props = db_config.getAll_props();
		final int[][] prop_lens = db_config.getProps_len();
		ret.pros = all_props;
		ret.updatingrules(prop_lens);
		try {
			int pos1;
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			final Connection con = DriverManager.getConnection(db_config.getODBC(), db_config.getDB_user(), db_config.getDB_password());
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final Statement stmn = con.createStatement();
			final String id = db_config.getId();
			String sql = "SELECT ";
			sql += id + " ";
			if (!"".equals(id)) {
				sql += ", ";
				pos1 = 2;
			} else {
				pos1 = 1;
			}
			for (final String all_prop : all_props) {
				sql += all_prop;
				sql += ", ";
			}
			sql = sql.substring(0, sql.length() - 2);
			sql += " FROM ";
			sql += db_config.getDB_table();
			// ResultSet rs = con.getTableQuery(sql);
			final ResultSet rs = stmn.executeQuery(sql);
			int index1 = 0;
			while (rs.next()) {
				if ("".equals(id)) {
					ret.IDs.add(new Integer(index1++).toString());
				} else {
					ret.IDs.add(rs.getString(id));
				}
				final String[] coms_strs = new String[prop_lens.length];
				Arrays.fill(coms_strs, "");
				for (int i = 0; i < all_props.length; i++) {
					String temp = rs.getString(i + pos1);
					if (temp == null) {
						temp = "";
					}
					temp = temp.replace("-", "").toLowerCase();
					// String temp = rs.getString(i + pos1).replace("-", "").toLowerCase();
					for (int j = 0; j < coms_strs.length; j++) {
						if (prop_lens[j][i] > Integer.MAX_VALUE / 2) {
							coms_strs[j] += sdx.soundex(temp).toLowerCase() + resizeString(temp, Integer.MAX_VALUE - prop_lens[j][i]);
						} else {
							coms_strs[j] += resizeString(temp, prop_lens[j][i]);
						}
					}
				}
				final boolean[][] bool_arr = new boolean[coms_strs.length][];
				for (int j = 0; j < coms_strs.length; j++) {
					if (hash_len > 0) {
						bool_arr[j] = copyOf(bytes2boolean(digest.digest(coms_strs[j].getBytes(StandardCharsets.UTF_8))), hash_len);
					} else {
						bool_arr[j] = bytes2boolean(coms_strs[j].getBytes(StandardCharsets.US_ASCII));
					}
				}
				retArrList.add(bool_arr);
			}
		} catch (SQLException | NoSuchAlgorithmException | ClassNotFoundException ex) {
			Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
		}
		ret.data_bin = new boolean[retArrList.size()][][];
		for (int i = 0; i < ret.data_bin.length; i++) {
			ret.data_bin[i] = retArrList.get(i);
		}
		return ret;
	}

	public static String resizeString(String str, int len) {

		if (str.length() < len) {
			return StringUtils.rightPad(str, len);
		} else if (str.length() > len) {
			return StringUtils.substring(str, 0, len);
		} else {
			return str;
		}
	}

	public static int toInt(boolean[] value) {

		int res = 0;
		for (int i = 0; i < value.length; i++) {
			res = value[i] ? res | 1 << i : res;
		}
		return res;
	}

	public static <T> T[][] unifyArray(Object[] input, CompEnv<T> eva, int len) {

		final T[][] ret = eva.newTArray(len, 0);
		int index = 0;
		for (final Object input1 : input) {
			final T[][] tmp = (T[][]) input1;
			for (final T[] tmp1 : tmp) {
				ret[index++] = tmp1;
			}
		}
		return ret;
	}

	public static <T> T[][][] unifyArray1(Object[] input, CompEnv<T> eva, int len) {

		final T[][][] ret = eva.newTArray(len, 0, 0);
		int index = 0;
		for (final Object input1 : input) {
			final T[][][] tmp = (T[][][]) input1;
			for (final T[][] tmp1 : tmp) {
				ret[index++] = tmp1;
			}
		}
		return ret;
	}

	public static <T> T[] unifyArrayWithF(Object[] input, CompEnv<T> eva, int len) {

		final T[] ret = eva.newTArray(len);
		int index = 0;
		for (final Object input1 : input) {
			final T[] tmp = (T[]) input1;
			for (final T tmp1 : tmp) {
				ret[index++] = tmp1;
			}
		}
		return ret;
	}

	public static void usagemain() {

		final String help_str = "" + String.format("     -config     <path>      : input configure file path\n") + String.format("     -data       <path>      : input data file path\n") + String.format("     -help                   : show help");
		System.out.println(help_str);
	}
}

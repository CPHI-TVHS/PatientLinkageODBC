/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in
 * the editor.
 */
package patientlinkage.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author cf
 */
/**
 * DatabaseConnection.java Purpose: This class connects to a MySQL database for reading/writing
 *
 * @author Vijay Thurimella
 * @author Brandon Abbott
 * @version 3.0 08/01/11
 */
public class DatabaseConnection {
	// Connection to the DB

	private Connection conn = null;
	private String database;
	private String passwd;
	private String schema;
	// Statement is used to interface queries with the DB
	private Statement stmt;
	private String URL;
	private String user;

	public DatabaseConnection(String URL_param, String driver, String database_param, String user_param, String passwd_param) {

		// TODO: remove driver from function parameters, just put it in main.java
		try {
			// Keep track of the essentials for ToSTring()
			this.database = database_param;
			this.URL = URL_param;
			this.user = user_param;
			this.passwd = passwd_param;
			// Modify dateTime behavior to avoid exception during read.
			URL_param += database_param;// + "?zeroDateTimeBehavior=convertToNull";
			// Establish Connection
			conn = DriverManager.getConnection(URL_param, user_param, passwd_param);
			// Ready an SQL statement
			stmt = conn.createStatement();
		} catch (final SQLException e) {
			System.err.println("Error Connecting to Database");
			e.printStackTrace();
			System.err.println("Program terminated with exit status 1");
		}
	}

	/**
	 * Establish a connection to a database so that reading or writing may take place
	 *
	 * @param URL_param - The location of the database. Ex: jdbc:mysql://localhost/
	 * @param driver_param - The driver to use in order to connect to the database
	 * @param database_param
	 * @param schema_param
	 * @param user_param - The username to use to connect to the db
	 * @param passwd_param - The password to use to connect to the db.
	 */
	public DatabaseConnection(String URL_param, String driver_param, String database_param, String schema_param, String user_param, String passwd_param) {

		// TODO: remove driver from function parameters, just put it in main.java
		try {
			// Keep track of the essentials for ToSTring()
			this.database = database_param;
			this.schema = schema_param;
			this.URL = URL_param;
			this.user = user_param;
			this.passwd = passwd_param;
			// Modify dateTime behavior to avoid exception during read.
			if (this.URL.contains("postgres") || this.URL.contains("mysql")) {
				URL_param += database_param; // + "?zeroDateTimeBehavior=convertToNull";
				conn = DriverManager.getConnection(URL_param, user_param, passwd_param);
			} else if (this.URL.contains("sqlserver")) {
				if (this.user.toLowerCase().equals("integratedsecurity")) {
					URL_param += ";databaseName=curl;integratedSecurity=true;";
				} else {
					URL_param += ";databaseName=curl;user=" + this.user + ";password=" + this.passwd;
				}
				conn = DriverManager.getConnection(URL_param);
			}
			// Ready an SQL statement
			stmt = conn.createStatement();
		} catch (final SQLException e) {
			System.err.println("Error Connecting to Database");
			System.err.println("Program terminated with exit status 1");
		}
	}

	/**
	 * This function queries PostgreSQL to determine whether a table 'table' exists in database 'database'
	 *
	 * @param table The name of the table to test existence against
	 * @param schema_param
	 * @return True if table exists, False if it does not exist
	 */
	public boolean checkPostgreTableExists(String table, String schema_param) {

		if (conn == null) {
			return false;
		}
		final String query = "SELECT COUNT(*) AS \"COUNT\" " + "FROM information_schema.tables " + "WHERE table_schema = '" + schema_param + "' " + "AND table_name = '" + table.toLowerCase() + "';";
		ResultSet rs = null;
		try {
			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
				rs.next();
				final String exists = rs.getString(1);
				// System.out.println("Row data:" + exists);
				if (exists.equals("0")) {
					return false;
				}
			}
		} catch (final SQLException e) {
			System.err.println("Error checking if table exists");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * This function queries PostgreSQL to determine whether a view 'table' exists in database 'database'
	 *
	 * @param table The name of the view to test existence against
	 * @param schema_param
	 * @return True if view exists, False if it does not exist
	 */
	public boolean checkPostgreViewExists(String table, String schema_param) {

		if (conn == null) {
			return false;
		}
		final String query = "SELECT COUNT(*) AS \"COUNT\" " + "FROM information_schema.views " + "WHERE table_schema = '" + schema_param + "' " + "AND table_name = '" + table.toLowerCase() + "';";
		ResultSet rs = null;
		try {
			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
				rs.next();
				final String exists = rs.getString(1);
				// System.out.println("Row data:" + exists);
				if (exists.equals("0")) {
					return false;
				}
			}
		} catch (final SQLException e) {
			System.err.println("Error checking if table exists");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * This function queries MySQL to determine whether a table 'table' exists in database 'database'
	 *
	 * @param table The name of the table to test existence against
	 * @return True if table exists, False if it does not exist
	 */
	public boolean checkTableExists(String table) {

		if (conn == null) {
			return false;
		}
		final String query = "SELECT COUNT(*) AS \"COUNT\" " + "FROM information_schema.tables " + "WHERE table_schema = '" + this.database + "' " + "AND table_name = '" + table + "';";
		ResultSet rs = null;
		try {
			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
				rs.next();
				final String exists = rs.getString(1);
				// System.out.println("Row data:" + exists);
				if (exists.equals("0")) {
					return false;
				}
			}
		} catch (final SQLException e) {
			System.err.println("Error checking if table exists");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Closes the connection to the MySQL database
	 */
	public void close() {

		// No need to close connection if there isn't one
		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (final SQLException e) {
				System.err.println("Could not close connection");
				e.printStackTrace();
			}
		}
		// Also destroy reference to the SQL statement
		if (stmt != null) {
			stmt = null;
		}
	}

	/**
	 * @param strSQL the query need to be executed.
	 * @return true if the query is successfully executed, false otherwise
	 */
	public Boolean executeActionQuery(String strSQL) {

		if (conn == null) {
			return false;
		}
		final String query = strSQL;
		final ResultSet rs = null;
		try {
			if (stmt.execute(query)) {
				return true;
			}
		} catch (final SQLException e) {
			System.err.println("Error checking if table exists");
			e.printStackTrace();
			return false;
		}
		return false;
	}

	// TODO: DBC1: Consider writing a function "connect()" in case a user wants to
	// TODO: DBC2: re-use a DBC after closing a connection.
	/**
	 * Execute a query on a MySQL database. Can be used to execute any valid MySQL query.
	 *
	 * @param query - The MySQL que Try to execute.
	 */
	public void executeQuery(String query) {

		// Return failure if there is no connection established
		if (conn == null) {
			return;
		}
		try {
			// attempt to execute the query
			stmt.execute(query);
		} catch (final SQLException e) {
			System.err.println("Could not execute query: \n" + query);
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Overloads getColumnCount(ResultSetMetaData)
	 *
	 * @see "getColumnCount(ResultSetMetaData)"
	 * @param sqlResults ResultSet containing table data
	 * @return number of columns for table, -1 for error
	 */
	public int getColumnCount(ResultSet sqlResults) {

		// TODO: try clause here might not be necesary.
		int columnCount = -1;
		if (sqlResults == null) {
			return columnCount;
		}
		try {
			columnCount = getColumnCount(sqlResults.getMetaData());
		} catch (final SQLException e) {
			System.err.println("Could not fetch columnCount from sqlResults MetaData");
			e.printStackTrace();
		}
		return columnCount;
	}

	/**
	 * Given the ResultMetaData for some ResultSet containing SQL table data, get the number of columns for the table.
	 *
	 * @param sqlRSMD MetaData for ResultSet
	 * @return number of columns in table, -1 for error
	 */
	public int getColumnCount(ResultSetMetaData sqlRSMD) {

		int columnCount = -1;
		if (sqlRSMD == null) {
			return columnCount;
		}
		try {
			columnCount = sqlRSMD.getColumnCount();
		} catch (final SQLException e) {
			System.err.println("Could not fetch columnCount from sqlResults MetaData");
			e.printStackTrace();
		}
		return columnCount;
	}

	/**
	 * Overloads getColumnNames(ResultSetMetaData)
	 *
	 * @param sqlResults - The result set of some database query
	 * @return - The column names of the query
	 */
	public String[] getColumnNames(ResultSet sqlResults) {

		return getColumnNames(getTableMetaData(sqlResults));
	}

	/**
	 * Given the ResultSetMetaData for some ResultSet containing SQL table data, get the names of each column for the table.
	 *
	 * @param sqlRSMD MetaData for ResultSet with table data
	 * @return an array of strings containing the names of each attribute of the table, in order
	 */
	public String[] getColumnNames(ResultSetMetaData sqlRSMD) {

		int columnCount;
		String[] columnNames = null;
		if (sqlRSMD == null) {
			return null;
		}
		if ((columnCount = getColumnCount(sqlRSMD)) == -1) {
			return null;
		}
		try {
			columnNames = new String[columnCount];
			for (int i = 0; i < columnCount; i++) {
				columnNames[i] = sqlRSMD.getColumnName(i + 1);
			}
		} catch (final SQLException e) {
			System.err.println("Could not fetch column names. " + "\nHave you set SQL ResultSetMetaData properly?");
			e.printStackTrace();
		}
		return columnNames;
	}

	/**
	 * Overloads getColumnNames(ResultSetMetaData)
	 *
	 * @param table - The name of the table to get the columnNames for
	 * @return - An array of Strings containing the names of the columns
	 */
	public String[] getColumnNames(String table) {

		return getColumnNames(getTableQuery("SELECT * FROM " + table + " WHERE 1=0"));
	}

	/**
	 * Given some sqlResults from a SQL query, get the next row in the result set as a String array. Moves the ResultSet cursor forward one position.
	 *
	 * @param sqlResults - The result set of some database query
	 * @return - One row from the SQL Results as an Array of Strings, null if there is no data
	 */
	public String[] getNextResult(ResultSet sqlResults) {

		String[] s = null;
		try {
			// Return null if there are no rows to parse
			if (sqlResults.next()) {
				// return null if you can't get the column count
				final int n = getColumnCount(sqlResults);
				if (n < 0) {
					return s;
				}
				// Initialize the string with DB results, getting things by columnName
				s = new String[n];
				final String[] colNames = getColumnNames(sqlResults);
				// Iterate through each record's value
				for (int i = 0; i < n; i++) {
					// Add values to the array if they exist
					if (sqlResults.getString(colNames[i]) != null) {
						s[i] = sqlResults.getString(colNames[i]).toUpperCase();
					} else {
						// Otherwise, just put a blank
						s[i] = "";
					}
				}
			}
		} catch (final SQLException e) {
			System.err.println("Could not fetch result into string array");
			e.printStackTrace();
			System.err.println("Program terminated with exit status 1");
			System.exit(1);
		}
		return s;
	}

	/**
	 * Given some sqlResults from a SQL query, get the next row in the result set as a String array with the column name in the first row and the value in the
	 * second row. Moves the ResultSet cursor forward one position.
	 *
	 * @param sqlResults - The result set of some database query
	 * @return - One row from the SQL Results as an Array of Strings, null if there is no data
	 */
	public String[][] getNextResultWithColName(ResultSet sqlResults) {

		String[][] s = null;
		try {
			// Return null if there are no rows to parse
			if (sqlResults.next()) {
				// return null if you can't get the column count
				final int n = getColumnCount(sqlResults);
				if (n < 0) {
					return s;
				}
				// Initialize the string with DB results, getting things by columnName
				s = new String[2][n];
				final String[] colNames = getColumnNames(sqlResults);
				// Iterate through each record's value
				for (int i = 0; i < n; i++) {
					// Add values to the array if they exist
					if (sqlResults.getString(colNames[i]) != null) {
						s[0][i] = colNames[i];
						s[1][i] = sqlResults.getString(colNames[i]).toUpperCase();
					} else {
						// Otherwise, just put a blank
						s[1][i] = "";
					}
				}
			}
		} catch (final SQLException e) {
			System.err.println("Could not fetch result into string array");
			e.printStackTrace();
			System.err.println("Program terminated with exit status 1");
			System.exit(1);
		}
		return s;
	}

	/**
	 * Given a ResultSet with a table from a selection statement get the number of rows in the table
	 *
	 * @param sqlResults ResultSet containing table data
	 * @return number of rows for table, -1 for an error
	 */
	public int getRowCount(ResultSet sqlResults) {

		int rowCount = -1;
		if (sqlResults == null) {
			return rowCount;
		}
		try {
			// Move the cursor to the last element in the sqlResults
			sqlResults.last();
			rowCount = sqlResults.getRow();
			sqlResults.first();
		} catch (final SQLException e) {
			System.err.println("Could not fetch number of Rows from sqlResults");
			e.printStackTrace();
		}
		return rowCount;
	}

	/**
	 * Given some table name, do a SELECT COUNT(*)
	 *
	 * @param table - The table name as a String
	 * @return - the number of rows in the table.
	 */
	public int getRowCount(String table) {

		final ResultSet sqlResults = getTableQuery("SELECT COUNT(*) AS COUNT FROM " + table + " ;");
		final String[] dataRow = getNextResult(sqlResults);
		return new Integer(dataRow[0]);
	}

	public String getSchema() {

		return this.schema;
	}

	/**
	 * @param strSQL user-defined query
	 * @return The value of the first row of the first column
	 */
	public Object getSingleValueSQL(String strSQL) {

		Object result = null;
		if (conn == null) {
			return result;
		}
		final String query = strSQL;
		ResultSet rs = null;
		try {
			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
				rs.next();
				result = rs.getString(1);
			}
		} catch (final SQLException e) {
			System.err.println("Error checking if table exists");
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * This function queries the currently connected database for an entire table of data & returns it as an SQL ResultSet.
	 *
	 * @param table The name of the table to return.
	 * @return An entire table of data as and SQL ResultSet
	 */
	public ResultSet getTable(String table) {

		return getTableQuery("SELECT * from " + table);
	}

	/**
	 * Given the ResultSet of some SQL query, get the MetaData
	 *
	 * @param sqlResults - The ResultSet of some SQL SELECT query
	 * @return - The MetaData of the ResultSet
	 */
	public ResultSetMetaData getTableMetaData(ResultSet sqlResults) {

		if (conn == null) {
			return null;
		}
		ResultSetMetaData sqlRSMD = null;
		try {
			sqlRSMD = sqlResults.getMetaData();
		} catch (final SQLException e1) {
			System.err.println("Error in fetching MetaData");
			e1.printStackTrace();
		}
		return sqlRSMD;
	}

	/**
	 * Given some valid MySQL SELECT query, return it as a MySQL ResultSet. This just wraps a Try-Catch and some error checking stuff.
	 *
	 * @param query - A valid MySQL SELECT query.
	 * @return - A SQL ResultSet containing the results of the query.
	 */
	public ResultSet getTableQuery(String query) {

		// Don't even try to getTable if there is no connection established
		if (conn == null) {
			return null;
		}
		ResultSet rs = null;
		try {
			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
			} else {
				System.err.println("Selection failed. Unknown Error");
			}
		} catch (final SQLException e) {
			System.err.println("Selection failed. Check query?");
			System.err.println("Query:" + query);
			e.printStackTrace();
			System.out.println("Program Terminated. Exit Status 1");
			System.exit(1);
		}
		return rs;
	}

	@Override
	public String toString() {

		return this.URL + "" + this.database + " usr:" + user + " pass:" + this.passwd;
	}
}

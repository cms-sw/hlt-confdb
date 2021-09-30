package confdb.db;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import oracle.jdbc.pool.*;

import java.util.ArrayList;

/**
 * OracleDatabaseConnector
 * -----------------------
 * @author Philipp Schieferdecker
 *
 * Oracle specific implementation of DatabaseConnector.
 */
public class OracleDatabaseConnector extends DatabaseConnector {
	//
	// member data
	//

	/** name of the database driver */
	private static final String driver = "oracle.jdbc.driver.OracleDriver";

	//
	// construction
	//

	/** standard constructor */
	public OracleDatabaseConnector(String url, String user, String password) throws DatabaseException {
		super(url, user, password);
	}

	/** constructor via Connection object */
	public OracleDatabaseConnector(Connection connection) throws DatabaseException {
		super(connection);
	}

	//
	// member functions
	//

	/** open database connection */
	public void openConnection() throws DatabaseException {
		if (connection != null)
			return;

		try {
			Class.forName(driver).newInstance();
		} catch (ClassNotFoundException e) {
			String msg = "Cannot load jdbc driver: " + e.getMessage();
			throw new DatabaseException(msg);
		} catch (InstantiationException e) {
			String msg = "Cannot instantiate jdbc driver: " + e.getMessage();
			throw new DatabaseException(msg);
		} catch (IllegalAccessException e) {
			String msg = "Cannot access db via jdbc driver: " + e.getMessage();
			throw new DatabaseException(msg);
		}

		try {
			connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
			// DB and Driver Info:
			try {
				DatabaseMetaData dbmd;
				dbmd = connection.getMetaData();
				System.err.println("=====  Database info =====");
				System.err.println("DatabaseProductName: " + dbmd.getDatabaseProductName());
				System.err.println("DatabaseProductVersion: " + dbmd.getDatabaseProductVersion());
				System.err.println("DatabaseMajorVersion: " + dbmd.getDatabaseMajorVersion());
				System.err.println("DatabaseMinorVersion: " + dbmd.getDatabaseMinorVersion());
				System.err.println("=====  Driver info =====");
				System.err.println("DriverName: " + dbmd.getDriverName());
				System.err.println("DriverVersion: " + dbmd.getDriverVersion());
				System.err.println("DriverMajorVersion: " + dbmd.getDriverMajorVersion());
				System.err.println("DriverMinorVersion: " + dbmd.getDriverMinorVersion());
				System.err.println("=====  JDBC/DB attributes =====");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SQLException e) {
			String msg = "Failed to open connection to " + dbURL + " as user " + dbUser+ "\n\n Exception: "+e.getMessage();
			System.err.println("Exception: " + e.getMessage());

			throw new DatabaseException(msg);
		}
	}

}

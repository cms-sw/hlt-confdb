package confdb.db;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;

/**
 * MySQLDatabaseConnector
 * ----------------------
 * @author Philipp Schieferdecker
 *
 * MySQL specific implementation of DatabaseConnector.
 */
public class MySQLDatabaseConnector extends DatabaseConnector
{
    //
    // member data
    //
    
    /** name of the database driver */
    private static final String driver = "com.mysql.jdbc.Driver";
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public MySQLDatabaseConnector(String url, String user, String password)
	throws DatabaseException
    {
	super(url, user, password);
    }
    
    /** constructor via Connection object*/
    public MySQLDatabaseConnector(Connection connection) throws DatabaseException
    {
	super(connection);
    }
    
    //
    // member functions
    //
    
    /** open database connection */
    public void openConnection() throws DatabaseException
    {
	if (connection!=null) return;
	
	try {
	    Class.forName(driver).newInstance();
	}
	catch (ClassNotFoundException e) {
	    String msg = "Cannot load jdbc driver: " + e.getMessage();
	    throw new DatabaseException(msg);
	}
	catch (InstantiationException e) {
	    String msg = "Cannot instantiate jdbc driver: " + e.getMessage();
	    throw new DatabaseException(msg);
	}
	catch (IllegalAccessException e) {
	    String msg = "Cannot access db via jdbc driver: " + e.getMessage();
	    throw new DatabaseException(msg);
	}

	try {
	    connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
	}
	catch (SQLException e) {
	    String msg = "Failed to open connection" +
		", url=" + dbURL + ", user=" +dbUser + ": " + e.getMessage();
	    throw new DatabaseException(msg);
	}
    }
    
}

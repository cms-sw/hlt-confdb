package confdb.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 * IDatabaseConnector
 * ------------------
 * @author Philipp Schieferdecker
 * 
 * Interface for all classes accessing a database. Different DBMS
 * reqire dedicated implementations (MySQL, Oracle, etc.).
 */
public interface IDatabaseConnector
{
    /** open connection to databse */
    public void openConnection() throws DatabaseException;

    /** close the connection to the database */
    public void closeConnection() throws DatabaseException;

    /** access to the connection to the database */
    public Connection getConnection();

    /** access to the URL of the database */
    public String dbURL();

    /** access the current username accessing the database */
    public String dbUser();

    /** access the password of the current user accessing the database */
    public String dbPassword();

    /** initialize the database access parameters */
    public void setParameters(String dbUrl,String dbUser,String dbPassword);

    /** release the resources associated with a result set */
    public ResultSet release(ResultSet rs);
    
}

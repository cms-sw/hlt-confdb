package confdb.db;

/**
 * DatabaseException
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Exception signature of the confdb.db. package.
 */
public class DatabaseException extends Exception
{
    //
    // construction
    //
    
    /** default constructor */
    public DatabaseException()
    {

    }

    /** constructor with error message */
    public DatabaseException(String errMsg)
    {
	super(errMsg);
    }
    
    /** constructor with error message and nested exception */
    public DatabaseException(String errMsg,Throwable e)
    {
	super(errMsg,e);
    }
    
}


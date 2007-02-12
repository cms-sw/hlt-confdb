package confdb.data;

/**
 * DataException
 * -------------
 * @author Philipp Schieferdecker
 *
 * Exception signature of the confdb.data package.
 */


public class DataException extends Exception
{
    //
    // construction
    //
    
    /** default constructor */
    public DataException()
    {

    }

    /** constructor with error message */
    public DataException(String errMsg)
    {
	super(errMsg);
    }
    
    /** constructor with error message and nested exception */
    public DataException(String errMsg,Throwable e)
    {
	super(errMsg,e);
    }
    
}

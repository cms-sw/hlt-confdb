package confdb.diff;

/**
 * DiffException
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Exception signature of the confdb.diff package.
 */
public class DiffException extends Exception
{
    //
    // construction
    //
    
    /** default constructor */
    public DiffException() {}

    /** constructor with error message */
    public DiffException(String errMsg)
    {
	super(errMsg);
    }
    
    /** constructor with error message and nested exception */
    public DiffException(String errMsg,Throwable e)
    {
	super(errMsg,e);
    }
    
}


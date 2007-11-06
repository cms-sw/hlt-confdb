package confdb.converter;

/**
 * ConverterException
 * ------------------
 * @author Philipp Schieferdecker
 *
 * Exception signature of the confdb.db. package.
 */
public class ConverterException extends Exception
{
    //
    // construction
    //
    
    /** default constructor */
    public ConverterException()
    {

    }

    /** constructor with error message */
    public ConverterException(String errMsg)
    {
	super(errMsg);
    }
    
    /** constructor with error message and nested exception */
    public ConverterException(String errMsg,Throwable e)
    {
	super(errMsg,e);
    }
    
}


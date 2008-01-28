package confdb.converter;

/**
 * ConversionException
 * ------------------
 */

public class ConversionException extends Exception
{
	private static final long serialVersionUID = 1L;

	//
    // construction
    //
    
    /** default constructor */
    public ConversionException()
    {

    }

    /** constructor with error message */
    public ConversionException(String errMsg)
    {
	super(errMsg);
    }
    
    /** constructor with error message and nested exception */
    public ConversionException(String errMsg,Throwable e)
    {
	super(errMsg,e);
    }
    
}


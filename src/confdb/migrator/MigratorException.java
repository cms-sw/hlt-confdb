package confdb.migrator;

/**
 * MigratorException
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Exception signature of the confdb.migrator package.
 */
public class MigratorException extends Exception
{
    //
    // construction
    //
    
    /** default constructor */
    public MigratorException()
    {

    }

    /** constructor with error message */
    public MigratorException(String errMsg)
    {
	super(errMsg);
    }
    
    /** constructor with error message and nested exception */
    public MigratorException(String errMsg,Throwable e)
    {
	super(errMsg,e);
    }
    
}


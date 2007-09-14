package confdb.parser;


/**
 * ParserException
 * ---------------
 * @author Philipp Schieferdecker
 *
 * Exception signature for confdb.parser.
 */
public class ParserException extends Exception
{
    /** default constructor */
    public ParserException() {}

    /** constructor with error message */
    public ParserException(String errMsg) { super(errMsg); }

    /** constructor with line number and error message */
    public ParserException(int lineCount,String errMsg)
    {
	super(new String("line " + lineCount + ": " + errMsg));
    }

    /** constructor with error message and nested exception */
    public ParserException(String errMsg,Throwable e) { super (errMsg,e); }

}

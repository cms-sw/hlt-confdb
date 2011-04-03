package confdb.parser;


/**
 * JParserException
 * ---------------
 * @author Philipp Schieferdecker
 *
 * Exception signature for confdb.parser.
 */
public class JParserException extends Exception
{
    /** default constructor */
    public JParserException() {}

    /** constructor with error message */
    public JParserException(String errMsg) { super(errMsg); }

    /** constructor with line number and error message */
    public JParserException(int lineCount,String errMsg)
    {
	super(new String("line " + lineCount + ": " + errMsg));
    }

    /** constructor with error message and nested exception */
    public JParserException(String errMsg,Throwable e) { super (errMsg,e); }

}

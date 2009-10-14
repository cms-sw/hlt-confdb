package confdb.diff;

import confdb.data.Stream;
import confdb.data.ReferenceContainer;
import confdb.data.Path;
import confdb.data.Sequence;


/**
 * StreamComparison
 * ----------------
 * @author Philipp Schieferdecker
 *
 */
public class StreamComparison extends Comparison
{
    //
    // member data
    //
    
    /** old stream */
    private Stream oldStream = null;

    /** new stream */
    private Stream newStream = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public StreamComparison(Stream oldStream,Stream newStream)
    {
	this.oldStream = oldStream;
	this.newStream = newStream;
    }
    
    
    //
    // member functions
    //
    
    /** determine the result of the comparison */
    public int result()
    {
	if      (oldStream==null&&newStream!=null) return RESULT_ADDED;
	else if (oldStream!=null&&newStream==null) return RESULT_REMOVED;
	else if (comparisonCount()==0&&
		 oldStream.name().equals(newStream.name()))
	    return RESULT_IDENTICAL;
	else return RESULT_CHANGED;
    }
    
    /** plain-text representation of the comparison */
    public String toString()
    {
	return (newStream==null) ?
	    "Stream "+oldStream.name()+" "+resultAsString():
	    "Stream "+newStream.name()+" "+resultAsString();
    }
    
    /** html representation of the comparison */
    public String toHtml()
    {
	return (newStream==null) ?
	    "<html>Stream <b>"+oldStream.name()+"</b></html>" :
	    "<html>Stream <b>"+newStream.name()+"</b></html>";
    }
    
}

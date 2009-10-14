package confdb.diff;

import confdb.data.EventContent;
import confdb.data.ReferenceContainer;
import confdb.data.Path;
import confdb.data.Sequence;


/**
 * EventContentComparison
 * ----------------
 * @author Philipp Schieferdecker
 *
 */
public class EventContentComparison extends Comparison
{
    //
    // member data
    //
    
    /** old stream */
    private EventContent oldEventContent = null;

    /** new stream */
    private EventContent newEventContent = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public EventContentComparison(EventContent oldEventContent,
				  EventContent newEventContent)
    {
	this.oldEventContent = oldEventContent;
	this.newEventContent = newEventContent;
    }
    
    
    //
    // member functions
    //
    
    /** determine the result of the comparison */
    public int result()
    {
	if (oldEventContent==null&&newEventContent!=null)
	    return RESULT_ADDED;
	else if (oldEventContent!=null&&newEventContent==null)
	    return RESULT_REMOVED;
	else if (comparisonCount()==0&&
		 oldEventContent.name()
		 .equals(newEventContent.name()))
	    return RESULT_IDENTICAL;
	else return RESULT_CHANGED;
    }
    
    /** plain-text representation of the comparison */
    public String toString()
    {
	return (newEventContent==null) ?
	    "EventContent "+oldEventContent.name()+" "+resultAsString():
	    "EventContent "+newEventContent.name()+" "+resultAsString();
    }
    
    /** html representation of the comparison */
    public String toHtml()
    {
	return (newEventContent==null) ?
	    "<html>EventContent <b>"+oldEventContent.name()+"</b></html>" :
	    "<html>EventContent <b>"+newEventContent.name()+"</b></html>";
    }
    
}

package confdb.diff;


import confdb.data.OutputModule;



/**
 * OutputModuleComparison
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class OutputModuleComparison extends Comparison
{
    //
    // member data
    //
    
    /** old output module */
    private OutputModule oldOutputModule = null;

    /** new output module */
    private OutputModule newOutputModule = null;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public OutputModuleComparison(OutputModule oldOutputModule,
				  OutputModule newOutputModule)
    {
	this.oldOutputModule = oldOutputModule;
	this.newOutputModule = newOutputModule;
    }

    
    //
    // member functions
    //
    
    /** determine the result of the comparison */
    public int result()
    {
	if      (oldOutputModule==null&&newOutputModule!=null)
	    return RESULT_ADDED;
	else if (oldOutputModule!=null&&newOutputModule==null)
	    return RESULT_REMOVED;
	else if (comparisonCount()==0&&
		 oldOutputModule.name()
		 .equals(newOutputModule.name())&&
		 oldOutputModule.className()
		 .equals(newOutputModule.className())&&
		 oldOutputModule.parentStream().label()
		 .equals(newOutputModule.parentStream().label()))
	    return RESULT_IDENTICAL;
	else return RESULT_CHANGED;
    }
    
    /** plain-text representation of the comparison */
    public String toString()
    {
	return (newOutputModule==null) ?
	    oldOutputModule.name()+" ["+oldOutputModule.className()+"] "+
	    resultAsString() :
	    newOutputModule.name()+" ["+newOutputModule.className()+"] "+
	    resultAsString();
    }

    /** html representation of the comparison */
    public String toHtml()
    {
	return (newOutputModule==null) ?
	    "<html>"+oldOutputModule.className()+
	    ".<b>"+oldOutputModule.name()+"<b></html>" :
	    "<html>"+newOutputModule.className()+
	    ".<b>"+newOutputModule.name()+"<b></html>";
    }
    
}

package confdb.diff;


import confdb.data.Instance;



/**
 * InstanceComparison
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class InstanceComparison extends Comparison
{
    //
    // member data
    //
    
    /** old instance */
    private Instance oldInstance = null;

    /** new instance */
    private Instance newInstance = null;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public InstanceComparison(Instance oldInstance,Instance newInstance)
    {
	this.oldInstance = oldInstance;
	this.newInstance = newInstance;
    }

    
    //
    // member functions
    //
    
    /** determine the result of the comparison */
    public int result()
    {
	if      (oldInstance==null&&newInstance!=null) return RESULT_ADDED;
	else if (oldInstance!=null&&newInstance==null) return RESULT_REMOVED;
	else { 
		if (oldInstance.template() != null && newInstance.template() != null) { 
			if (comparisonCount()==0&&
			 oldInstance.name().equals(newInstance.name())&&
			 oldInstance.template().name().equals(newInstance.template().name()))
		    return RESULT_IDENTICAL;
		} else {  //EDAlias branch
			if (comparisonCount()==0&& oldInstance.name().equals(newInstance.name()))
			 return RESULT_IDENTICAL;
		}
	}
	return RESULT_CHANGED;
    }
    
    /** plain-text representation of the comparison */
    public String toString()
    {
    	if (oldInstance.template() != null && newInstance.template() != null) {
			return (newInstance==null) ?
			    oldInstance.name()+" ["+oldInstance.template().name()+"] "+
			    resultAsString() :
			    newInstance.name()+" ["+newInstance.template().name()+"] "+
			    resultAsString();
    	} else { //EDAlias branch
    		return (newInstance==null) ?
    			    oldInstance.name() + resultAsString() :
    			    newInstance.name() + resultAsString();
    	}
    }

    /** html representation of the comparison */
    public String toHtml()
    {
    	if (oldInstance.template() != null && newInstance.template() != null) {
			return (newInstance==null) ?
			    "<html>"+oldInstance.template().name()+
			    ".<b>"+oldInstance.name()+"<b></html>" :
			    "<html>"+newInstance.template().name()+
			    ".<b>"+newInstance.name()+"<b></html>";
    	} else { //EDAlias branch
    		return (newInstance==null) ?
    			"<html>"+".<b>"+oldInstance.name()+"<b></html>" :
    			"<html>"+".<b>"+newInstance.name()+"<b></html>";
    	}
    }
    
    public boolean isInstanceOf(Class<?> cls) {
    	return (oldInstance.getClass().equals(cls)) ? true : false;
    }
    
}

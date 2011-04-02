package confdb.diff;


import confdb.data.Reference;



/**
 * ReferenceComparison
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class ReferenceComparison extends Comparison
{
    //
    // member data
    //
    
    /** old instance */
    private Reference oldReference = null;

    /** new instance */
    private Reference newReference = null;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ReferenceComparison(Reference oldReference,Reference newReference)
    {
	this.oldReference = oldReference;
	this.newReference = newReference;
    }

    
    //
    // member functions
    //
    
    /** determine the result of the comparison */
    public int result()
    {
	if      (oldReference==null&&newReference!=null) return RESULT_ADDED;
	else if (oldReference!=null&&newReference==null) return RESULT_REMOVED;
	else if (comparisonCount()==0&&
		 oldReference.name().equals(newReference.name())&&
		 oldReference.parent().name().equals(newReference.parent().name())&&
		 oldReference.getOperatorAndName().equals(newReference.getOperatorAndName()) ) 
	    return RESULT_IDENTICAL;
	else return RESULT_CHANGED;
    }
    
    /** plain-text representation of the comparison */
    public String toString()
    {
	if (oldReference==null) {
	    return newReference.getOperatorAndName()+" "+resultAsString();
	} else if (newReference==null) {
	    return oldReference.getOperatorAndName()+" "+resultAsString();
	} else {
	    return newReference.getOperatorAndName()+" ["+oldReference.getOperatorAndName()+"] "+resultAsString();
	}

    }

    /** html representation of the comparison */
    public String toHtml()
    {
	if (oldReference==null) {
	    return "<html><b>"+newReference.getOperatorAndName()+"</b></html>";
	} else if (newReference==null) {
	    return "<html><b>"+oldReference.getOperatorAndName()+"</b></html>";
	} else {
	    return "<html><b>"+newReference.getOperatorAndName()+" ["+oldReference.getOperatorAndName()+"]<b><html>";
	}
    }
    
}

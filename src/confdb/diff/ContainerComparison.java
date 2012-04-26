package confdb.diff;

import confdb.data.ReferenceContainer;
import confdb.data.Path;
import confdb.data.Sequence;


/**
 * ContainerComparison
 * -------------------
 * @author Philipp Schieferdecker
 *
 */
public class ContainerComparison extends Comparison
{
    //
    // member data
    //
    
    /** old container */
    private ReferenceContainer oldContainer = null;

    /** new container */
    private ReferenceContainer newContainer = null;
    
    /** container type */
    private String containerType;
    
    /** path qualifier (path/endpath */
    private String pathQualifier = "";

    //
    // construction
    //
    
    /** standard constructor */
    public ContainerComparison(ReferenceContainer oldContainer,
			       ReferenceContainer newContainer)
    {
	this.oldContainer = oldContainer;
	this.newContainer = newContainer;
	ReferenceContainer rc =
	    (newContainer==null) ? oldContainer : newContainer;

	if (rc instanceof Sequence) {
	    containerType = "Sequence";
	}
	else if (rc instanceof Path) {
	    containerType = "Path";
	    if (oldContainer!=null&&newContainer!=null) {
		Path oldPath = (Path)oldContainer;
		Path newPath = (Path)newContainer;
		pathQualifier = "";
		if (oldPath.isEndPath()!=newPath.isEndPath()) {
		    pathQualifier += (newPath.isEndPath()) ? "[ENDPATH]" : "[PATH]";
		}
		if (!oldPath.getDescription().equals(newPath.getDescription())){
		    pathQualifier += "[Description]";
		}
		if (!oldPath.getContacts().equals(newPath.getContacts())){
		    pathQualifier += "[Contacts]";
		}
	    }
	}
    }
    
    
    //
    // member functions
    //
    
    /** access to containers */
    public ReferenceContainer oldContainer() {return oldContainer;}
    public ReferenceContainer newContainer() {return newContainer;}

    /** determine the result of the comparison */
    public int result()
    {
	if      (oldContainer==null&&newContainer!=null) return RESULT_ADDED;
	else if (oldContainer!=null&&newContainer==null) return RESULT_REMOVED;
	else if (comparisonCount()==0&&
		 oldContainer.name().equals(newContainer.name())&&
		 pathQualifier.length()==0)
	    return RESULT_IDENTICAL;
	else return RESULT_CHANGED;
    }
    
    /** plain-text representation of the comparison */
    public String toString()
    {
	return (newContainer==null) ?
	    oldContainer.name()+" "+resultAsString()+" "+pathQualifier:
	    newContainer.name()+" "+resultAsString()+" "+pathQualifier;
    }
    
    /** html representation of the comparison */
    public String toHtml()
    {
	return (newContainer==null) ?
	    "<html><b>"+oldContainer.name()+"</b> "+" "+pathQualifier+"</html>":
	    "<html><b>"+newContainer.name()+"</b> "+" "+pathQualifier+"</html>";
    }
    
}

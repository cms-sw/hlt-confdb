package confdb.data;


/**
 * EventContentStatement
 * ---------------------
 * @author Philipp Schieferdecker
 *
 * manage a single drop / keep statement, representing an entry in the
 * vstring outputCommands parameter of an OutputModule.
 */
public class EventContentStatement
{
    //
    // member data
    //
    
    /** indicate if this is a 'drop' statement */
    private boolean isDrop = false;
    
    /** parent path, if it isn't a global statement */
    private Path parentPath = null;
    
    /** reference, if it isn't a global statement */
    private Reference parentReference = null;

    /** friendly class name*/
    private String className = "*";
    
    /** module label */
    private String moduleLabel = "*";

    /** extra label */
    private String extraLabel = "*";

    /** process name */
    private String processName = "*";
    

    //
    // construction
    //

    /** standard constructor */
    public EventContentStatement()
    {

    }

    /** constructor from path & reference */
    public EventContentStatement(Path parentPath,Reference parentReference)
    {
	this.parentPath      = parentPath;
	this.parentReference = parentReference;
    }
    
    
    //
    // member functions
    //
    
    /** indicate if this statement is global */
    public boolean isGlobal() { return (parentPath==null); }
    
    /** indicate if this is a 'drop' statement */
    public boolean isDrop() { return isDrop; }

    /** retrieve parent path of this statement */
    public Path parentPath() { return parentPath; }

    /** retrieve parent reference of this statement */
    public Reference parentReference() { return parentReference; }

    /** retrieve class name part of the statement */
    public String className() { return className; }

    /** retrieve module label part of the statement */
    public String moduleLabel()
    {
	return (parentReference==null) ? moduleLabel : parentReference.name();
    }

    /** retrieve extra label part of the statement */
    public String extraLabel() { return extraLabel; }

    /** retrieve process name part of the statement */
    public String processName() { return processName; }
    
    /** provide string represenation of the statement */
    public String toString()
    {
	StringBuffer result = new StringBuffer();
	String dropOrKeep = (isDrop()) ? "drop " : "keep ";
	result
	    .append(dropOrKeep)
	    .append(className()).append("_")
	    .append(moduleLabel()).append("_")
	    .append(extraLabel()).append("_")
	    .append(processName());
	return result.toString();
    }
    
    /** make this a 'drop' statement */
    public void setDrop() { isDrop = true; }
    
    /** make this a 'keep' statement */
    public void setKeep() { isDrop = false; }

    /** set the class name */
    public void setClassName(String className) { this.className = className; }

    /** set the module label */
    public boolean setModuleLabel(String moduleLabel)
    {
	if (parentReference != null) {
	    System.err.println("EventContentStatement.setModuleLabel() ERROR:"+
			       " statement has a parent reference!");
	    return false;
	}
	this.moduleLabel = moduleLabel;
	return true;
    }

    /** set the extra label */
    public void setExtraLabel(String extraLabel)
    {
	this.extraLabel = extraLabel;
    }

    /** set the process name */
    public void setProcessName(String processName)
    {
	this.processName=processName;
    }
    
}


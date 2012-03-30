package confdb.data;

import java.io.Serializable;
import java.util.Iterator;


/**
 * OutputCommand
 * -------------
 * @author Philipp Schieferdecker
 *
 * manage a single drop / keep statement, representing an entry in the
 * vstring outputCommands parameter of an OutputModule.
 */
public class OutputCommand implements Comparable<OutputCommand>, Serializable
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
    
    /** module name */
    private String moduleName = "*";

    /** extra name */
    private String extraName = "*";

    /** process name */
    private String processName = "*";
    

    //
    // construction
    //

    /** standard constructor */
    public OutputCommand()
    {

    }
    
    /** constructor from path & reference */
    public OutputCommand(Path parentPath,Reference parentReference)
    {
	this.parentPath      = parentPath;
	this.parentReference = parentReference;
	moduleName = parentReference.name();
    }
    
    

    //
    // member functions
    //

    /** Comparable: compareTo() */
    public int compareTo(OutputCommand oc) {return toString().compareTo(oc.toString());}

    /** Object: equals() */
    public boolean equals(Object o)
    {
	if (o==null) return false;
	if (!(o instanceof OutputCommand)) return false;
	OutputCommand command = (OutputCommand)o;
	if (parentPath!=command.parentPath()) return false;
	return (toString().equals(command.toString()));
    }

    /** Object: hashCode() */
    public int hashCode()
    {
	int result = 12;
	int pathHashCode = (parentPath==null) ? 0:parentPath.name().hashCode();
	result = 31*result+pathHashCode;
	result = 31*result+toString().hashCode();
	System.out.println("toString():"+toString()+", hashCode = "+result);
	return result;
    }
    
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

    /** retrieve module name part of the statement */
    public String moduleName()
    {
	return (parentReference==null) ? moduleName : parentReference.name();
    }

    /** retrieve extra name part of the statement */
    public String extraName() { return extraName; }

    /** retrieve process name part of the statement */
    public String processName() { return processName; }
    
    /** provide string represenation of the statement */
    public String toString()
    {
	String dropOrKeep = (isDrop()) ? "drop " : "keep ";

	if (className().equals("*")&&moduleName().equals("*")&&
	    extraName().equals("*")&&processName().equals("*"))
	    return (dropOrKeep+"*");
	
	StringBuffer result = new StringBuffer();
	result
	    .append(dropOrKeep)
	    .append(className()).append("_")
	    .append(moduleName()).append("_")
	    .append(extraName()).append("_")
	    .append(processName());
	return result.toString();
    }
    
    /** set all values according to a given output command (essentialy copy) */
    public void set(OutputCommand command)
    {
	this.isDrop = command.isDrop();
	this.parentPath = command.parentPath();
	this.parentReference = command.parentReference();
	this.className = command.className();
	this.moduleName = command.moduleName();
	this.extraName = command.extraName();
	this.processName = command.processName();
    }

    /** make this a 'drop' statement */
    public void setDrop() { isDrop = true; }
    
    /** make this a 'keep' statement */
    public void setKeep() { isDrop = false; }

    /** set the class name */
    public void setClassName(String className)
    {
	this.className = className.split(" ")[0];
	this.className = className.split("_")[0];
	if (className().length()==0) this.className = "*";
    }

    /** set module reference */
    public boolean setModuleReference(Reference reference)
    {
	if (parentPath()==null) return false;
	Iterator<Reference> itR = parentPath().recursiveReferenceIterator();
	boolean found = false;
	while (itR.hasNext()&&!found) if (itR.next()==reference) found=true;
	if (!found) return false;
	this.parentReference = reference;
	return true;
    }

    /** set the module name */
    public boolean setModuleName(String moduleName)
    {
		// allow to set this property. - bug #88643.
    	//if (parentReference != null) {
		    //System.err.println("OutputCommand.setModuleName() ERROR: output command has a parent reference!");
		    //return false;
		//}
    	
		this.moduleName = moduleName.split(" ")[0];
		this.moduleName = moduleName.split("_")[0];
		if (moduleName().length()==0) this.moduleName = "*";
		return true;
    }
    
    /** set the extra name */
    public void setExtraName(String extraName)
    {
	this.extraName = extraName.split(" ")[0];
	this.extraName = extraName.split("_")[0];
	if (extraName().length()==0) this.extraName = "*";
    }
    
    /** set the process name */
    public void setProcessName(String processName)
    {
	this.processName=processName.split(" ")[0];
	this.processName=processName.split("_")[0];
	if (processName().length()==0) this.processName = "*";
    }
    
    /** initialize from string */
    public boolean initializeFromString(String contentAsString)
    {
	String s[] = contentAsString.split(" ");
	if (s.length!=2) return false;
	String type = s[0];
	String names = s[1];
	
	if      (type.equals("drop")) isDrop = true;
	else if (type.equals("keep")) isDrop = false;
	else return false;

	if (names.equals("*")) {
	    this.parentPath = null;
	    this.parentReference = null;
	    setClassName("*");
	    setModuleName("*");
	    setExtraName("*");
	    setProcessName("*");
	    return true;
	}
	
	String s2[] = names.split("_");
	if (s2.length!=4) return false;

	this.parentPath = null;
	this.parentReference = null;
	setClassName(s2[0]);
	setModuleName(s2[1]);
	setExtraName(s2[2]);
	setProcessName(s2[3]);
	return true;
    }
    
}


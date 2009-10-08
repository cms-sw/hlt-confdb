package confdb.data;

import java.util.Iterator;
import java.util.ArrayList;


/**
 * OutputModule
 * ------------
 * @author Philipp Schieferdecker
 *
 * Explicit model for OutputModules, which are not -- like
 * ModuleInstances -- bound to a ModuleTemplate and don't have an
 * arbitrary list of parameters. OutputModules are linked to a stream
 * instead, and the values of their two parameters SelectEvents and
 * outputCommands are directly derived from teh associated Stream and
 * its parent EventContent.
 */
public class OutputModule extends ParameterContainer implements Referencable
{
    //
    // data members
    // 
    
    /** name of the class */
    private String className = "FUShmStreamConsumer";

    /** label of this OutputModule */
    private String name = "";

    /** reference to the parent stream */
    private Stream parentStream = null;
    
    /** vstring SelectEvents parameter, which contains the paths */
    private VStringParameter vstringSelectEvents = null;

    /** vstring outputCommands parameter, defining the data format */
    private VStringParameter vstringOutputCommands = null;

    /** list of references to output module (within reference containers) */
    private ArrayList<OutputModuleReference> references =
	new ArrayList<OutputModuleReference>();
    
    /** parent configuration of OutputModule */
    private IConfiguration parentConfig = null;


    //
    // construction
    //

    /** standard constructor */
    public OutputModule(String name, Stream parentStream)
    {
	this.name = name;
	this.parentStream = parentStream;

	PSetParameter psetSelectEvents =
	    new PSetParameter("SelectEvents","",false);
	
	vstringSelectEvents =
	    new VStringParameter("SelectEvents","",true);
	psetSelectEvents.addParameter(vstringSelectEvents);
	
	vstringOutputCommands =
	    new VStringParameter("outputCommands","",false);

	addParameter(psetSelectEvents);
	addParameter(vstringOutputCommands);

	updateSelectEvents();
	updateOutputCommands();
    }
    
    
    //
    // member functions
    //

    /** ParameterContainer: indicate wether parameter is at its default */
    public boolean isParameterAtItsDefault(Parameter p)  { return false; }
    
    /** ParameterContainer: indicate wether a parameter can be removed */
    public boolean isParameterRemovable(Parameter p)
    {
	int index = indexOfParameter(p);
	if (index<2) return false; // protect SelectEvents & outputCommands!
	return true;
    }
    
    /** retrieve the class name of the output module */
    public String className() { return className; }

    /** retrieve the parent stream of the output module */
    public Stream parentStream() { return parentStream; }
    
    /** Referencable: name() */
    public String name() { return name; }
    
    /** Referenable: setName() */
    public void setName(String name) throws DataException
    { 
	this.name = name;
    }
    
    /** Referencable: create a reference of this output module */
    public Reference createReference(ReferenceContainer container, int i)
    {
	OutputModuleReference reference = new OutputModuleReference(container,
								    this);
	references.add(reference);
	container.insertEntry(i,reference);
	return reference;
    }
    
    /** Referencable: number of references */
    public int referenceCount() { return references.size(); }

    /** Referencable: retrieve the i-th reference */
    public Reference reference(int i) { return references.get(i); }

    /** Referencable: test if specific reference refers to this o-module */
    public boolean isReferencedBy(Reference reference)
    {
	return references.contains(reference);
    }
    
    /** Referencable: remove a reference to this output module */
    public void removeReference(Reference reference)
    {
	int index = references.indexOf(reference);
	references.remove(index);
	//if (referenceCount()==0) remove();
    }
    
    /** Referencable: get list of parent paths */
    public Path[] parentPaths()
    {
	ArrayList<Path> list = new ArrayList<Path>();
	for (int i=0;i<referenceCount();i++) {
	    Path[] paths = reference(i).parentPaths();
	    for (Path p : paths) list.add(p);
	}
	return list.toArray(new Path[list.size()]);
    }

    public void setConfiguration(IConfiguration parentConfig)
    {
	this.parentConfig = parentConfig;
    }
    
    
    //
    // private member functions
    //

    /** update value of 'SelectEvents' parameter */
    private void updateSelectEvents()
    {
	
    }
   
    /** update value of 'outputCommands' parameter */
    private void updateOutputCommands()
    {
	
    }
    
}


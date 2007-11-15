package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * ModuleInstance
 * --------------
 * @author Philipp Schieferdecker
 *
 * CMSSW framework module instance.
 */
public class ModuleInstance extends Instance implements Referencable
{
    //
    // data members
    //
    
    /** list of references */
    private ArrayList<ModuleReference> references = new ArrayList<ModuleReference>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ModuleInstance(String name,ModuleTemplate template)
	throws DataException
    {
	super(name,template);
    }
    
    
    //
    // member functions
    //
    
    /** create a reference of this instance */
    public Reference createReference(ReferenceContainer container,int i)
    {
	ModuleReference reference = new ModuleReference(container,this);
	references.add(reference);
	container.insertEntry(i,reference);
	return reference;
    }
    
    /** number of references */
    public int referenceCount() { return references.size(); }
    
    /** retrieve the i-th reference */
    public Reference reference(int i) { return references.get(i); }

    /** test if a specifc reference refers to this entity */
    public boolean isReferencedBy(Reference reference) 
    {
	return references.contains(reference);
    }
    
    /** remove a reference of this instance */
    public void removeReference(Reference reference)
    {
	int index = references.indexOf(reference);
	references.remove(index);
	if (referenceCount()==0) remove();
    }
    
    /** get list of parent paths */
    public Path[] parentPaths()
    {
	ArrayList<Path> list = new ArrayList<Path>();
	for (int i=0;i<referenceCount();i++) {
	    Path[] paths = reference(i).parentPaths();
	    for (Path p : paths) list.add(p);
	}
	return list.toArray(new Path[list.size()]);
    }
    
    /** set the name and propagate it to all relevant downstreams InputTags */
    public void setNameAndPropagate(String name) throws DataException
    {
	String oldName = name();
	super.setName(name); // Instance.setName()
	Path[] paths = parentPaths();
	for (Path path : paths) {
	    boolean isDownstream = false;
	    Iterator<ModuleInstance> itM = path.moduleIterator();
	    while (itM.hasNext()) {
		ModuleInstance module = itM.next();
		if (module==this) {
		    isDownstream = true;
		    continue;
		}
		if (!isDownstream) continue;
		Iterator<Parameter> itP = module.parameterIterator();
		while (itP.hasNext()) {
		    Parameter p = itP.next();
		    if (p instanceof InputTagParameter) {
			InputTagParameter inputTag = (InputTagParameter)p;
			if (inputTag.label().equals(oldName)) {
			    inputTag.setLabel(name());
			    module.setHasChanged();
			}
		    }
		    else if (p instanceof VInputTagParameter) {
			VInputTagParameter vInputTag = (VInputTagParameter)p;
			for (int i=0;i<vInputTag.vectorSize();i++) {
			    InputTagParameter inputTag =
				new InputTagParameter("",vInputTag.value(i).toString(),false,false);
			    if (inputTag.label().equals(oldName)) {
				inputTag.setLabel(name());
				vInputTag.setValue(i,inputTag.valueAsString());
				module.setHasChanged();
			    }
			}
		    }
		}
	    }
	}
    }

}

package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Path
 * ----
 * @author Philipp Schieferdecker
 *
 * CMSSW framework path.
 */
public class Path extends ReferenceContainer
{
    //
    // construction
    //
    
    /** standard constructor */
    public Path(String name)
    {
	super(name);
    }
    
    
    //
    // member functions
    //

    /** chek if this path contains an output module */
    public boolean isEndPath()
    {
	for (Reference r : entries) {
	    Referencable parent = r.parent();
	    if (parent instanceof ModuleInstance) {
		ModuleInstance module = (ModuleInstance)parent;
		if (module.template().type().equals("OutputModule")) return true;
	    }
	    else if (parent instanceof Sequence) {
		Sequence sequence  = (Sequence)parent;
		if (sequence.hasOutputModule()) return true;
	    }
	    else if (parent instanceof Path) {
		Path path = (Path)parent;
		if (path.isEndPath()) return true;
	    }
	}
	return false;
    }

    /** insert a path entry */
    public void insertEntry(int i,Reference reference)
    {
	if (!entries.contains(reference))
	    entries.add(i,reference);
	else System.out.println("Path.insertEntry FAILED.");
	
    }
    
    /** check if path contains a specific entry */
    public boolean containsEntry(Reference reference)
    {
	Referencable parent = reference.parent();
	Iterator it = entries.iterator();
	while (it.hasNext()) {
	    Reference r = (Reference)it.next();
	    if (parent.isReferencedBy(r)) return true;
	    if (r.parent() instanceof ReferenceContainer) {
		ReferenceContainer container = (ReferenceContainer)r.parent();
		if (container.containsEntry(reference)) return true;
	    }
	}
	return false;
    }

    /** create a reference of this in a reference container (path/sequence) */
    public Reference createReference(ReferenceContainer container,int i)
    {
	PathReference reference = new PathReference(container,this);
	references.add(reference);
	container.insertEntry(i,reference);
	return reference;
    }
    
}

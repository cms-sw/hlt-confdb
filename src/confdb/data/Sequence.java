package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Sequence
 * --------
 *  @author Philipp Schieferdecker
 *  
 *  A 'Sequence' can host any number of ModuleReferences and
 *  SequenceReferences, but no references to other Paths.
*/
public class Sequence extends ReferenceContainer
{
    //
    // construction
    //
    
    /** standard constructor */
    public Sequence(String name)
    {
	super(name);
    }

    
    //
    // member functions
    //
    
    /** check if the sequence contains an outputmodule */
    public boolean hasOutputModule()
    {
	for (Reference r : entries) {
	    Referencable parent = r.parent();
	    if (parent instanceof ModuleInstance) {
		ModuleInstance module = (ModuleInstance)parent;
		if (module.template().type().equals("OutputModule")) return true;
	    }
	    else if (parent instanceof Sequence) {
		Sequence sequence = (Sequence)parent;
		if (sequence.hasOutputModule()) return true;
	    }
	}
	return false;
    }

    /** insert a module into the sequence */
    public void insertEntry(int i,Reference reference)
    {
	if (reference instanceof ModuleReference ||
	    reference instanceof SequenceReference) {
	    if (!entries.contains(reference)) {
		entries.add(i,reference);
		return;
	    }
	}
	System.out.println("Sequence.insertEntry FAILED.");
    }
    
    /** check if sequence contains a specific module */
    public boolean containsEntry(Reference reference)
    {
	Referencable parent = reference.parent();
	Iterator it = entries.iterator();
	while (it.hasNext()) {
	    Reference r = (Reference)it.next();
	    if (parent.isReferencedBy(r)) return true;
	}
	return false;
    }

    /** create a reference of this in a reference container (path/sequence) */
    public Reference createReference(ReferenceContainer container,int i)
    {
	SequenceReference reference = new SequenceReference(container,this);
	references.add(reference);
	container.insertEntry(i,reference);
	return reference;
    }
    
}

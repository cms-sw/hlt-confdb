package confdb.data;

import java.util.ArrayList;


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
    
    
}

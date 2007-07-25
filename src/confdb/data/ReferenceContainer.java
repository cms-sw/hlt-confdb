package confdb.data;

import java.util.ArrayList;


/**
   ReferenceContainer
   ------------------
   @author Philipp Schieferdecker
   
   Common base class of Path and Sequence.
*/
abstract public class ReferenceContainer extends DatabaseEntry implements Referencable
{
    //
    // member data
    //

    /** name of the container */
    private String name = null;

    /** list of contained references */
    protected ArrayList<Reference> entries = new ArrayList<Reference>();
    
    /** references of this path within other paths */
    protected ArrayList<Reference> references = new ArrayList<Reference>();
     

    //
    // construction
    //

    /** standard constructor */
    public ReferenceContainer(String name)
    {
	this.name = name;
    }

    //
    // abstract member functions
    //
    
    /** insert an entry into container */
    abstract public void insertEntry(int i,Reference reference);
    
    /** check if container contains the specified reference */
    abstract public boolean containsEntry(Reference reference);

    
    //
    // member functions
    //
    
    /** overload toString() */
    public String toString() { return name(); }

    /** get the name of the container */
    public String name() { return name; }
    
    /** override DatabaseEntry.hasChanged, check on entries! */
    public boolean hasChanged()
    {
	if (databaseId()==0) return true;
	for (Reference r : entries) {
	    DatabaseEntry dbentry = (DatabaseEntry)r.parent();
	    if (dbentry!=null&&dbentry.hasChanged()) return true;
	}
	return false;
    }
    
    /** set the name of the container */
    public void setName(String name) { this.name = name; setHasChanged(); }
    
    /** number of entries */
    public int entryCount() { return entries.size(); }

    /** retrieve i-th entry*/
    public Reference entry(int i) { return entries.get(i); }
    
    /** index of a certain entry */
    public int indexOfEntry(Reference reference)
    {
	return entries.indexOf(reference);
    }
    
    /** remove a reference from the container */
    public void removeEntry(Reference reference)
    {
	int index = entries.indexOf(reference);
	if (index>=0) {
	    entries.remove(index);
	    setHasChanged();
	}
	else {
	    System.out.println("ReferenceContainer.removeEntry FAILED.");
	}
    }
    
    /** create a reference of this in a reference container (path/sequence) */
    abstract public Reference createReference(ReferenceContainer container,int i);
    
    /** number of references */
    public int referenceCount() { return references.size(); }
    
    /** retrieve the i-th reference */
    public Reference reference(int i) { return references.get(i); }

    /** test if a specifc reference refers to this entity */
    public boolean isReferencedBy(Reference reference)
    {
	return references.contains(reference);
    }

    /** remove a reference of this */
    public void removeReference(Reference reference)
    {
	int index = references.indexOf(reference);
	references.remove(index);
    }

    /** number of unset tracked paremters */
    public int unsetTrackedParameterCount()
    {
	int result = 0;
	for (Reference r : entries) {
	    if (r instanceof ModuleReference) {
		ModuleReference module = (ModuleReference)r;
		result += module.unsetTrackedParameterCount();
	    }
	}
	return result;
    }

}

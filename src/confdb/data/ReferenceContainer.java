package confdb.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


/**
   ReferenceContainer
   ------------------
   @author Philipp Schieferdecker
   
   Common base class of Path and Sequence.
*/
abstract public class ReferenceContainer extends    DatabaseEntry
                                         implements Comparable<ReferenceContainer>,
						    Referencable
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
    
    /* Comparable: compareTo() */
    public int compareTo(ReferenceContainer rc)
    {
	return toString().compareTo(rc.toString());
    }

    /** get the name of the container */
    public String name() { return name; }
    
    /** override DatabaseEntry.hasChanged, check on entries! */
    public boolean hasChanged()
    {
	if (databaseId()==0) return true;
	for (Reference r : entries) {
	    DatabaseEntry dbentry = (DatabaseEntry)r.parent();
	    if (dbentry!=null&&dbentry.hasChanged()) {
		setHasChanged();
		return true;
	    }
	}
	return false;
    }
    
    /** calculate the number of unresolved InputTags */
    public int unresolvedInputTagCount()
    {
	ArrayList<String> unresolved = new ArrayList<String>();
	HashSet<String> labels = new HashSet<String>();
	for (Reference r : entries) {
	    getUnresolvedInputTags(r,labels,unresolved);
	}
	
	// DEBUG
	if (unresolved.size()>0) {
	    System.out.println("Unresolved InputTags for path "+name()+":");
	    for (String s : unresolved) System.out.println(s);
	    System.out.println();
	}

	return unresolved.size();
    }
    
    /** does this container contain an OutputModule? */
    public boolean hasOutputModule() { return hasModuleOfType("OutputModule"); }

    /** does this container contain an EDProducer? */
    public boolean hasEDProducer()   { return hasModuleOfType("EDProducer"); }

    /** does this container contain an EDFilter? */
    public boolean hasEDFilter() { return hasModuleOfType("EDFilter"); }

    /** does this container contain an HLTFilter? */
    public boolean hasHLTFilter() { return hasModuleOfType("HLTFilter"); }
    
    /** set the name of the container */
    public void setName(String name) { this.name = name; setHasChanged(); }
    
    /** get entry iterator */
    public Iterator entryIterator() { return entries.iterator(); }

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
    
    /** move an entry to a new position within the container */
    public boolean moveEntry(Reference reference,int targetIndex)
    {
	int currentIndex = entries.indexOf(reference);
	if (currentIndex<0) return false;
	if (currentIndex==targetIndex) return true;
	if (targetIndex>entries.size()) return false;
	if (currentIndex<targetIndex) targetIndex--;
	entries.remove(currentIndex);
	entries.add(targetIndex,reference);
	setHasChanged();
	return true;
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

    
    //
    // private member functions
    //

    /** does this container contain a module of type 'type' */
    private boolean hasModuleOfType(String type)
    {
	for (Reference r : entries) {
	    Referencable parent = r.parent();
	    if (parent instanceof ModuleInstance) {
		ModuleInstance module = (ModuleInstance)parent;
		if (module.template().type().equals(type)) return true;
	    }
	    else if (parent instanceof ReferenceContainer) {
		ReferenceContainer container = (ReferenceContainer)parent;
		if (container.hasModuleOfType(type)) return true;
	    }
	}
	return false;
    }

    /** get unresolved InputTags from a reference, given the labels to this point */
    private void getUnresolvedInputTags(Reference r,
					HashSet<String> labels,
					ArrayList<String> unresolved)
    {
	if (r instanceof ModuleReference) {
	    ModuleReference modref = (ModuleReference)r;
	    ModuleInstance  module = (ModuleInstance)modref.parent();
	    labels.add(module.name());
	    Iterator it = module.parameterIterator();
	    while (it.hasNext()) {
		Parameter p = (Parameter)it.next();
		getUnresolvedInputTags(p,labels,unresolved);
	    }
	}
	else {
	    ReferenceContainer container = (ReferenceContainer)r.parent();
	    Iterator it = container.entryIterator();
	    while (it.hasNext()) {
		Reference entry = (Reference)it.next();
		getUnresolvedInputTags(entry,labels,unresolved);
	    }
	}
    }

    /** get unresolved InputTags from a parameter, given the labels to this point */
    private void getUnresolvedInputTags(Parameter p,
					HashSet<String> labels,
					ArrayList<String> unresolved)
    {
	if (p instanceof InputTagParameter) {
	    InputTagParameter itp = (InputTagParameter)p;
	    if (!labels.contains(itp.label())) {
		Object parent = itp;
		String s = ":"+itp.name()+"@"+itp.label();
		do {
		    parent = ((Parameter)parent).parent();
		    s = "/"+parent+s;
		}
		while (parent instanceof Parameter);
		unresolved.add(s);
	    }
	}
	else if (p instanceof VInputTagParameter) {
	    VInputTagParameter vitp = (VInputTagParameter)p;
	    for (int i=0;i<vitp.vectorSize();i++) {
		InputTagParameter itp =
		    new InputTagParameter((new Integer(i)).toString(),
					  vitp.value(i).toString(),false,false);
		itp.setParent(vitp);
		getUnresolvedInputTags(itp,labels,unresolved);
	    }
	}
	else if (p instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)p;
	    for (int i=0;i<pset.parameterCount();i++)
		getUnresolvedInputTags(pset.parameter(i),labels,unresolved);
	}
	else if (p instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)p;
	    for (int i=0;i<vpset.parameterSetCount();i++)
		getUnresolvedInputTags(vpset.parameterSet(i),labels,unresolved);
	}
    }
    
}


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
    // member data
    //

    /** streams this path is associated with*/
    private ArrayList<Stream> streams = new ArrayList<Stream>();
    

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
	if (!entries.contains(reference)) {
	    entries.add(i,reference);
	    setHasChanged();
	}
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
	container.setHasChanged();
	return reference;
    }

    /** number of streams this path is associated with */
    public int streamCount() { return streams.size(); }

    /** retrieve the i=th stream this path is associated with */
    public Stream stream(int i) { return streams.get(i); }

    /** retrieve iterator over streams this path is associated with */
    public Iterator streamIterator() { return streams.iterator(); }

    /** add this path to a stream */
    public boolean addToStream(Stream stream)
    {
	if (streams.indexOf(stream)>=0) return false;
	streams.add(stream);
	return true;
    }
    
    /** remove this path from a stream */
    public boolean removeFromStream(Stream stream)
    {
	int index = streams.indexOf(stream);
	if (index<0) return false;
	streams.remove(index);
	return true;
    }
    
}

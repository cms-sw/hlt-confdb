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
    
    /** flag indicating that the path was set to be an endpath */
    private boolean isSetAsEndPath = false;
    

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
	if (isSetAsEndPath) return true;
	return hasOutputModule();
    }

    /** is this path *set* to be an endpath? *Not* the same as above! */
    public boolean isSetAsEndPath() { return isSetAsEndPath; }
    
    /** set this path to be an endpath */
    public boolean setAsEndPath(boolean isSetAsEndPath)
    {
	if (this.isSetAsEndPath==isSetAsEndPath) return true;
	
	/*
	  if (hasEDProducer()) {
	  System.err.println("Can't declare path '"+name()+"' as endpath: "+
	  "it contains one or more EDProducer(s).");
	  return false;
	  }
	  if (hasEDFilter()) {
	  System.err.println("Can't declare path '"+name()+"' as endpath: "+
	  "it contains one or more EDFilter(s).");
	  return false;
	  }
	  if (hasHLTFilter()) {
	  System.err.println("Can't declare path '"+name()+"' as endpath: "+
	  "it contains one or more HLTFilter(s).");
	  return false;
	  }
	*/

	this.isSetAsEndPath = isSetAsEndPath;
	setHasChanged();
	return true;
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

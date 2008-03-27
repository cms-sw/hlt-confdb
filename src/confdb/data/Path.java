package confdb.data;


import java.util.HashSet;
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

    /** primary datasets this path is associated with */
    private ArrayList<PrimaryDataset> datasets = new ArrayList<PrimaryDataset>();
    
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
	Iterator<Reference> it = entries.iterator();
	while (it.hasNext()) {
	    Reference r = it.next();
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

    /** number of primary datasets this path is associated with */
    public int datasetCount() { return datasets.size(); }

    /** retrieve the i-th primary dataset this path is associated with */
    public PrimaryDataset dataset(int i) { return datasets.get(i); }

    /** retrieve iterator over primary datasets this path is associated with */
    public Iterator<PrimaryDataset> datasetIterator()
    {
	return datasets.iterator();
    }

    /** add this path to a primary dataset */
    public boolean addToDataset(PrimaryDataset dataset)
    {
	if (datasets.indexOf(dataset)>=0) return false;
	datasets.add(dataset);
	setHasChanged();
	return true;
    }
    
    /** remove this path from a primary dataset */
    public boolean removeFromDataset(PrimaryDataset dataset)
    {
	int index = datasets.indexOf(dataset);
	if (index<0) return false;
	datasets.remove(index);
	setHasChanged();
	return true;
    }
    
    /** retrieve list of streams this path is assigned to */
    public Stream[] listOfStreams()
    {
	HashSet<Stream> setOfStreams = new HashSet<Stream>();
	for (PrimaryDataset pd : datasets) {
	    Stream s = pd.parentStream();
	    if (s!=null) setOfStreams.add(s);
	}
	return setOfStreams.toArray(new Stream[setOfStreams.size()]);
    }
    
    /** number of streams this path is assigned to */
    public int streamCount() { return listOfStreams().length; }
    
}

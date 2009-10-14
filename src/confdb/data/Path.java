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

    /** collection of event contents this path is associated with */
    private ArrayList<EventContent> contents=new ArrayList<EventContent>();
    
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

    /** set the name of the path; check 'SelectEvents' of OutputModules */
    /*
      public void setName(String name) throws DataException
      {
      String oldName = name();
      super.setName(name);
      if (config!=null) {
      Iterator<ModuleInstance> itM = config.moduleIterator();
      while (itM.hasNext()) {
      ModuleInstance module = itM.next();
      if (!module.template().type().equals("OutputModule")) continue;
      Parameter[] tmp=module.findParameters("SelectEvents::SelectEvents");
      if (tmp.length!=1) continue;
      VStringParameter selEvts = (VStringParameter)tmp[0];
      for (int i=0;i<selEvts.vectorSize();i++) {
      String iAsString = (String)selEvts.value(i);
      if (iAsString.equals(oldName)) {
      selEvts.setValue(i,name);
      module.setHasChanged();
      }
      }
      }
      }
      }
    */

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


    /** number of event contents this path is associated with */
    public int contentCount() { return contents.size(); }
    
    /** retrieve i-th event content */
    public EventContent content(int i) { return contents.get(i); }

    /** retrieve event content iterator */
    public Iterator<EventContent> contentIterator()
    {
	return contents.iterator();
    }
    
    /** add this path to an event content */
    public boolean addToContent(EventContent content)
    {
	if (contents.indexOf(content)>=0) return false;
	contents.add(content);
	// DON'T CALL setHasChanged()!?!
	return true;
    }
    
    /** remove this path from an event content */
    public boolean removeFromContent(EventContent content)
    {
	int index = contents.indexOf(content);
	if (index<0) return false;
	contents.remove(index);
	// DON'T CALL setHasChanged()!?!
	return true;
    }

    
    /** number of streams this path is assiged to */
    public int streamCount() { return streams().size(); }
    
    /** retrieve stream iterator */
    public Iterator<Stream> streamIterator()
    {
	return streams().iterator();
    }

    /** number of datasets this path is assigned to */
    public int datasetCount() { return datasets().size(); }
    
    /** retrieve dataset iterator */
    public Iterator<PrimaryDataset> datasetIterator()
    {
	return datasets().iterator();
    }
    
    //
    // private member functions
    //

    /** retrieve a list of streams this path is associated with */
    private ArrayList<Stream> streams()
    {
	ArrayList<Stream> result = new ArrayList<Stream>();
	Iterator<EventContent> itC = contentIterator();
	while (itC.hasNext()) {
	    Iterator<Stream> itS = itC.next().streamIterator();
	    while (itS.hasNext()) {
		Stream stream = itS.next();
		if (stream.indexOfPath(this)>=0) result.add(stream);
	    }
	}
	return result;
    }

    
    /** retrieve list of primary datasets this path is associated with */
    public ArrayList<PrimaryDataset> datasets()
    {
	ArrayList<PrimaryDataset> result = new ArrayList<PrimaryDataset>();
	Iterator<Stream> itS = streams().iterator();
	while (itS.hasNext()) {
	    Iterator<PrimaryDataset> itPD = itS.next().datasetIterator();
	    while (itPD.hasNext()) {
		PrimaryDataset dataset = itPD.next();
		if (dataset.indexOfPath(this)>=0) result.add(dataset);
	    }
	}
	return result;
    }
    

}

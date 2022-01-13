 package confdb.data;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Stream
 * ------
 * @author Philipp Schieferdecker
 *
 * streams contain primary datasets, and thereby implicitely a list of
 * paths.
 */
public class Stream extends DatabaseEntry implements Comparable<Stream>
{
    //
    // member data
    //

    /** name of the stream */
    private String name;

    /** fraction of events to be writte to local disk by SM */
    private double fractionToDisk = 1.0;
    
    /** reference to parent event content */
    private EventContent parentContent = null;
    
    /** associated output module */
    private OutputModule outputModule = null;

    /** collection of assigned paths */
    private ArrayList<Path> paths = new ArrayList<Path>();

    /** collection of assigned paths */
    private ArrayList<PrimaryDataset> datasets=new ArrayList<PrimaryDataset>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public Stream(String name,EventContent parentContent)
    {
	this.name = name.replaceAll("\\W", "");
	this.parentContent = parentContent;
	this.outputModule = new OutputModule("hltOutput"+name,this);
    }
    
    
    //
    // member functions
    //

    /** name of this stream */
    public String name() { return name; }
    
    /** retrieve fraction of events to be written to local disc by SM */
    public double fractionToDisk() { return fractionToDisk; }
    
    /** set name of this stream */
    public void setName(String name) { 
	this.name = name.replaceAll("\\W", "");
        setHasChanged();
	try {
	    this.outputModule.setName("hltOutput"+name);
	    this.outputModule.setHasChanged();
	}
	catch (DataException e) {
	    System.err.println(e.getMessage());
	}
	for (Path p : paths) {
	    p.setHasChanged();
	}
    }
    
    /** DatabaseEntry: databaseId() */
    public int databaseId() { return super.databaseId(); }
    
    /** DatabaseEntry: hasChanged() 
     * so we have some nasty logic here in the implimentation
     * both Stream and PrimaryDataset can be set to hasChanged and thus
     * have their databaseId reset if any of their paths have changed
     * the reason for their paths changing triggering a change in them is in the 
     * PrimaryDatset header's comments
     * the problem is the order we write to the DB which is
     *    PrimaryDatasets, EventContent, Stream, Path
     * once an object has been writen, we can not call its setHasChanged again 
     * and thus we cant call its hasChanged as the Paths will still be "hasChanged" as they 
     * havent been writen yet
     * EventContent calls the hasChanged of Streams which calls the hasChanged of PrimaryDatasets
     * however if the paths have changed the datasets hasChanged will not be called and all is 
     * well. And if the paths havent changed, well its safe to call the PrimaryDataset hasChanged
     * when adding in the dataset path which is not part of the "paths" field, this 
     * cause all this logic to break hence the protections to avoid calling the PrimaryDataset 
     * hasChanged if any of its paths or dataset path has changed
    */
    public boolean hasChanged()
    {
	for (Path p : paths) {
	    if(p.hasChanged()) {
		setHasChanged();
		return super.hasChanged();
	    }
	}
    for (PrimaryDataset pd : datasets) {
	    if((pd.datasetPath()!=null && pd.datasetPath().hasChanged()) || pd.hasChanged()) {
		setHasChanged();
	       	return super.hasChanged();
	    }
	}
	
	return super.hasChanged();
    }
    
    /** set the fraction of events to be writte to local disk by SM */
    public void setFractionToDisk(double fractionToDisk)
    {
	if (fractionToDisk>=0.0&&fractionToDisk<=1.0){
	    this.fractionToDisk = fractionToDisk;
	    setHasChanged();
	}
	else System.err.println("Stream.setFractionToDisk() ERROR: "+
				"fraction = " + fractionToDisk +
				" not in [0,1]!");
    }
    
    /** overload 'toString()' */
    public String toString() { return name(); }

    /** get parent event content */
    public EventContent parentContent() { return parentContent; }

    /** get associated output module */
    public OutputModule outputModule() { return outputModule; }
   
    /** set associated output module */
    public void setOutputModule(OutputModule om) { outputModule=om; return; }
   
    /** get the parent configuration */
    public IConfiguration config() { return parentContent.config(); }

    /** Comparable: compareTo() */
    public int compareTo(Stream s) {return toString().compareTo(s.toString());}
    
    /** number of paths */
    public int pathCount() { return paths.size(); }
    
    /** retrieve i-th path */
    public Path path(int i)
    {
	Collections.sort(paths);
	return paths.get(i);
    }
    
    /** retrieve path by name */
    public Path path(String pathName)
    {
	for (Path p : paths) if (p.name().equals(pathName)) return p;
	return null;
    }
    
    /** retrieve index of a given path */
    public int indexOfPath(Path path)
    {
	Collections.sort(paths);
	return paths.indexOf(path);
    }
    
    /** retrieve iterator over paths */
    public Iterator<Path> pathIterator()
    {
	Collections.sort(paths);
	return paths.iterator();
    }
    
    /** retrieve path iterator (alphabetical order) */
    public Iterator<Path> orderedPathIterator()
    {
	ArrayList<Path> orderedPaths = new ArrayList<Path>(paths);
	Collections.sort(orderedPaths);
	return orderedPaths.iterator();
    }
    
    /** associate another path with this stream */
    public boolean insertPath(Path path)
    {
	if (paths.indexOf(path)>=0) return false;

	path.addToContent(parentContent);
	paths.add(path);
	Collections.sort(paths);
	setHasChanged();
	
	return true;
    }
    
    /** remove a path from this stream */
    public boolean removePath(Path path)
    {
	int index = paths.indexOf(path);
	if (index<0) return false;
	paths.remove(index);
	setHasChanged();
	parentContent.removePath(path);
	Iterator<PrimaryDataset> itPD = datasetIterator();
	while (itPD.hasNext()) {
	    PrimaryDataset dataset = itPD.next();
	    if (dataset.indexOfPath(path)>=0) dataset.removePath(path);
	}
	return true;
    }
    
    /** number of paths assigned to a dataset */
    public int assignedPathCount() { return listOfAssignedPaths().size(); }
    
    /** retrieve collection of paths assigned to datasets */
    public ArrayList<Path> listOfAssignedPaths()
    {
	ArrayList<Path> result = new ArrayList<Path>();
	Iterator<PrimaryDataset> itPD = datasetIterator();
	while (itPD.hasNext()) {
	    PrimaryDataset PD = itPD.next();
	    Iterator<Path> itP = PD.pathIterator();
	    while (itP.hasNext()) {
	    	Path path = itP.next();
	    	if(!result.contains(path)) result.add(path);
	    }
	}
	return result;
    }


    /** number of paths NOT assigned to any dataset */
    public int unassignedPathCount() { return listOfUnassignedPaths().size(); }
    
    /** retrieve collection of paths NOT assigned to any dataset */
    public ArrayList<Path> listOfUnassignedPaths()
    {
	ArrayList<Path> result = new ArrayList<Path>();
	ArrayList<Path> assigned = listOfAssignedPaths();
	Iterator<Path> itP = pathIterator();
	while (itP.hasNext()) {
	    Path path = itP.next();
	    if (assigned.indexOf(path)<0) result.add(path);
	}
	return result;
    }

    /** remove all paths NOT assigned to any dataset */
    public boolean removeUnassignedPaths()
    {
	int count = 0;
	ArrayList<Path> unassigned = listOfUnassignedPaths();
	Iterator<Path> itP = unassigned.iterator();
	while (itP.hasNext()) {
	    Path path = itP.next();
	    if (removePath(path)) count ++;
	}
	return count>0;
    }


    /** number of primary datasets */
    public int datasetCount() { return datasets.size(); }
    
    /** retrieve i-th primary dataset */
    public PrimaryDataset dataset(int i)
    {
	Collections.sort(datasets);
	return datasets.get(i);
    }

    /** retrieve primary dataset by name */
    public PrimaryDataset dataset(String datasetName)
    {
	for (PrimaryDataset pd : datasets)
	    if (pd.name().equals(datasetName)) return pd;
	return null;
    }
    
    public boolean hasDatasetPath() {
        for(PrimaryDataset dataset: datasets){
            if(dataset.datasetPath()!=null){
                return true;
            }
        }
        return false;
    }

    /** retrieve primary dataset which contains specified path */
    /*
    public PrimaryDataset dataset(Path path)
    {
	for (PrimaryDataset pd : datasets)
	    if (pd.indexOfPath(path)>=0) return pd;
	return null;
    }
    */
    
    /** retrieve primary datasets which contains specified path 
     * NOTE: This method will be available after the intervention.
     * There will be more than one dataset sharing the same path.
     * But the Stream is still the same. */
    public ArrayList<PrimaryDataset> datasets(Path path)
    {
    	ArrayList<PrimaryDataset> datasetList = new ArrayList<PrimaryDataset>();
		for (PrimaryDataset pd : datasets)
		    if (pd.indexOfPath(path)>=0) datasetList.add(pd);//return pd;
		//return null;
		return datasetList;
    }
    
    /** retrieve primary dataset iterator */
    public Iterator<PrimaryDataset> datasetIterator()
    {
	Collections.sort(datasets);
	return datasets.iterator();
    }
    
    /** index of a given primary dataset */
    public int indexOfDataset(PrimaryDataset ds)
    {
	Collections.sort(datasets);
	return datasets.indexOf(ds);
    }
    
    /** insert and associate a primary dataset with this stream */
    public PrimaryDataset insertDataset(String datasetName)
    {
        for (PrimaryDataset pd : datasets)
            if (pd.name().equals(datasetName)) {
                return null;
            }
        PrimaryDataset result = new PrimaryDataset(datasetName, this);
        datasets.add(result);
	Collections.sort(datasets);
        setHasChanged();
        return result;
    }
    
    /** remove a dataset from this stream */
    public boolean removeDataset(PrimaryDataset dataset)
    {
	int index = datasets.indexOf(dataset);
	if (index<0) return false;
	datasets.remove(index);
	setHasChanged();
	return true;

    }

    /** remove reference to this stream's outputmodule */
    public void removeOutputModuleReferences()
    {
	for (int i=outputModule.referenceCount()-1;i>=0;i--) {
	    outputModule.reference(i).remove();
	}
    }
    
}

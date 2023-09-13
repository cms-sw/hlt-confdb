 package confdb.data;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

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

    /** collection of assigned paths 
     * this is now generated from the datasets and is not directly set
    */
    private ArrayList<Path> pathList = new ArrayList<Path>();

    /** collection of assigned paths */
    private ArrayList<PrimaryDataset> datasets=new ArrayList<PrimaryDataset>();
    
    /** whether datasets have been added/rmed and the path list needs to be regenerated */
    private boolean hasDatasetListChanged = true;

    
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

    /** handles getting the list of paths and updating it if necessary
    */
    private ArrayList<Path> paths(){
        if(hasDatasetContentChanged()){            
            setPathList();
        }
        return pathList;
    }

    /** sets the dataset from  */
    private void setPathList(){        
        ArrayList<Path> allPaths = new ArrayList<Path>();
        for(PrimaryDataset pd : datasets){
            Iterator<Path> pathIt = pd.pathIterator();
            while(pathIt.hasNext()){
                allPaths.add(pathIt.next());
            }
        }
        pathList = allPaths.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(pathList);
        hasDatasetListChanged = false;

    }

    /** tells us if paths have been added/rmed from any of the datasets or any dataset
     * has been added or removed and thus if we need to remake the list of paths
     * note we cant call hasChanged for datasets as that may actually reset the dataset
     * hence we call a different method
     */
    private boolean hasDatasetContentChanged(){    
        if(hasDatasetListChanged){
            return true;
        }
        for(PrimaryDataset pd : datasets){
            if(pd.hasPathListChanged()){                
                return true;
            }
        }
        return false;
    }

    /** name of this stream */
    public String name() { return name; }
    
    /** retrieve fraction of events to be written to local disc by SM */
    public double fractionToDisk() { return fractionToDisk; }
    
    /** set name of this stream */
    public void setName(String name) { 
    String oldOutputPathName = outputPathName();
	this.name = name.replaceAll("\\W", "");
        setHasChanged();
	try {
	    this.outputModule.setName("hltOutput"+name);
	    this.outputModule.setHasChanged();
        Path streamOutPath = parentContent().config().path(oldOutputPathName);
        if(streamOutPath!=null){
            streamOutPath.setNameAndPropagate(outputPathName());
        }

	}
	catch (DataException e) {
	    System.err.println(e.getMessage());
	}
	for (Path p : paths()) {
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
	for (Path p : paths()) {
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

    public String outputPathName(){return name()+"Output";}
   
    /** set associated output module */
    public void setOutputModule(OutputModule om) { outputModule=om; return; }
   
    /** get the parent configuration */
    public IConfiguration config() { return parentContent.config(); }

    /** Comparable: compareTo() */
    public int compareTo(Stream s) {return toString().compareTo(s.toString());}
    
    /** number of paths */
    public int pathCount() { return paths().size(); }
    
    /** retrieve i-th path */
    public Path path(int i)
    {	
	return paths().get(i);
    }
    
    /** retrieve path by name */
    public Path path(String pathName)
    {
	for (Path p : paths()) if (p.name().equals(pathName)) return p;
	return null;
    }
    
    /** retrieve index of a given path */
    public int indexOfPath(Path path)
    {	
	return paths().indexOf(path);
    }
    
    /** retrieve iterator over paths */
    public Iterator<Path> pathIterator()
    {	
	return paths().iterator();
    }
    
    /** retrieve path iterator (alphabetical order) */
    public Iterator<Path> orderedPathIterator()
    {
	ArrayList<Path> orderedPaths = new ArrayList<Path>(paths());	
	return orderedPaths.iterator();
    }
    
    /** remove a path from this stream */
    public boolean removePath(Path path)
    {
	int index = paths().indexOf(path);
	if (index<0) return false;	
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
    
    /** retrieve collection of paths assigned to datasets 
     * this is now the same as paths as all paths are assigned
    */
    public ArrayList<Path> listOfAssignedPaths()
    {
        return paths();
    }


    /** number of paths NOT assigned to any dataset */
    public int unassignedPathCount() { return listOfUnassignedPaths().size(); }
    
    /** retrieve collection of paths NOT assigned to any dataset 
     * now no paths can be unassigned and thus returns empty result
    */
    public ArrayList<Path> listOfUnassignedPaths()
    {
	ArrayList<Path> result = new ArrayList<Path>();
	return result;
    }

    /** remove all paths NOT assigned to any dataset 
     * now this does nothing as there is no such thing as an unassigned path
    */
    public boolean removeUnassignedPaths()
    {
        return false;
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
        hasDatasetListChanged = true;
        return result;
    }

    /** insert and associate an existing primary dataset with this stream 
     *  it can be only added if it has no parent stream already
    */
    public boolean insertDataset(PrimaryDataset dataset)
    {
        if(dataset.parentStream()==null){
            dataset.setParentStream(this);
            datasets.add(dataset);            
            setHasChanged();
            return true;
        }else {
            System.err.println("error adding dataset "+dataset.name()+" to stream "+name()+" but PD alread has parent "+dataset.parentStream().name());
            return false;
        }
    }
    
    /** remove a dataset from this stream */
    public boolean removeDataset(PrimaryDataset dataset)
    {
	int index = datasets.indexOf(dataset);
	if (index<0) return false;  
	datasets.remove(index).setParentStream(null);
	setHasChanged();
	hasDatasetListChanged = true;
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

package confdb.data;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

/**
 * PrimaryDataset
 * --------------
 * @author Philipp Schieferdecker
 *
 * PrimaryDatasets are a set (!) of paths assigned with the parent
 * stream. Each path can only be assigned to one of the PDs assigned
 * with a single stream.
 */
public class PrimaryDataset extends DatabaseEntry
                            implements Comparable<PrimaryDataset>
{
    //
    // member data
    //
    
    /** name of the stream */ 
    private String name;
    
    /** collection of assigned paths */
    private ArrayList<Path> paths = new ArrayList<Path>();
    
    /** parent stream */
    private Stream parentStream = null;
    
    /** path representing the dataset **/
    private Path datasetPath = null;

    /** filter module selecting dataset's paths **/
    private ModuleInstance datasetModule = null;

    //
    // construction
    //
    
    /** standard constructor */
    public PrimaryDataset(String name,Stream parentStream)
    {
	this.name         = name.replaceAll("\\W", "");
	this.parentStream = parentStream;
    createDatasetPath();
    }
    
    
    //
    // member functions
    //
    
    /** name of this stream */
    public String name() {
   
	return name; 
    }

   public boolean hasChanged(){
	for (Path p : paths){
	    if(p.hasChanged()){
		setHasChanged();
		break;
	    }
	}
	return super.hasChanged();
    }
    
    /** get the parent stream */
    public Stream parentStream() { return parentStream; }

    public Path datasetPath() { return datasetPath; }

    public ModuleInstance datasetModule() { return datasetModule; }

    /** set name of this stream */
    public void setName(String name) {
	this.name = name.replaceAll("\\W", "");
	setHasChanged();
	for (Path p : paths) {
	    p.setHasChanged();
	}

    }
    
    /** overload 'toString()' */
    public String toString() { return name(); }
    
    /** Comparable: compareTo() */
    public int compareTo(PrimaryDataset s)
    {
	return toString().compareTo(s.toString());
    }

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
    
    /** index of a certain path */
    public int indexOfPath(Path path)
    {
	Collections.sort(paths);
	return paths.indexOf(path);
    }

    /** insert and associate a path with this stream */
    public boolean insertPath(Path path)
    {
	if (paths.indexOf(path)>=0) {
	    System.err.println("PrimaryDataset.insertPath() ERROR: "+
			       "path already associated!");
	    return false;
	}
	/*
	if (parentStream.listOfAssignedPaths().indexOf(path)>=0) {
	    System.err.println("PrimaryDataset.insertPath() ERROR: "+
			       "path already associated with another dataset "+
			       "in the parent stream!");
	    return false;
	}
	*/
	if (parentStream.indexOfPath(path)<0) parentStream.insertPath(path);
	//else parentStream.setHasChanged();
	paths.add(path);
	Collections.sort(paths);
	setHasChanged();
	
	return true;
    }
    
    /** remove a path from this dataset */
    public boolean removePath(Path path)
    {
	int index = paths.indexOf(path);
	if (index<0) return false;
	paths.remove(index);
	setHasChanged();
        //parentStream.setHasChanged();
	return true;
    }
    
    /** remove all paths */
    public void clear()
    {
	paths.clear();
	setHasChanged();
        parentStream.setHasChanged();
    }

    public String datasetPathName()
    {
        return "Dataset_"+name();
    }
    public String datasetModuleName()
    {
        return "hltDataset"+name();
    }

    /** create the path representing the path **/ 
    public void createDatasetPath()
    {        
        Configuration cfg = (Configuration) parentStream.parentContent().config();
        this.datasetModule = cfg.insertModule("TriggerResultsFilter",datasetModuleName());
        this.datasetPath = cfg.insertPath(cfg.pathCount(),datasetPathName());
        cfg.insertModuleReference(this.datasetPath,0,"HLTPrescaler",Path.hltPrescalerLabel(this.datasetPath.name()));
        cfg.insertModuleReference(this.datasetPath,1,this.datasetModule);
    }

}

package confdb.data;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Stream
 * ------
 * @author Philipp Schieferdecker
 *
 * streams contain primary datasets, and thereby implicitely a list of paths.
 */
public class Stream extends DatabaseEntry implements Comparable<Stream>
{
    //
    // member data
    //
    
    /** label of the stream */
    private String label;

    /** collection of assigned paths */
    private ArrayList<PrimaryDataset> datasets = new ArrayList<PrimaryDataset>();
    

    //
    // construction
    //
    
    /** standard constructor */
    public Stream(String label)
    {
	this.label = label;
    }
    
    
    //
    // member functions
    //
    
    /** label of this stream */
    public String label() { return label; }
    
    /** set label of this stream */
    public void setLabel(String label) { this.label = label; }
    
    /** overload 'toString()' */
    public String toString() { return label(); }

    /** Comparable: compareTo() */
    public int compareTo(Stream s) { return toString().compareTo(s.toString()); }

    /** number of primary datasets */
    public int datasetCount() { return datasets.size(); }
    
    /** retrieve i-th primary dataset */
    public PrimaryDataset dataset(int i) { return datasets.get(i); }

    /** retrieve primary dataset iterator */
    public Iterator<PrimaryDataset> datasetIterator() {return datasets.iterator();}

    /** index of a given primary dataset */
    public int indexOfDataset(PrimaryDataset ds) { return datasets.indexOf(ds); }
    
    /** insert and associate a primary dataset with this stream */
    public boolean insertDataset(PrimaryDataset dataset)
    {
	if (datasets.indexOf(dataset)>=0) {
	    System.out.println("Stream.insertDataset() WARNING: dataset '"+
			       dataset.label()+"' already associated with stream '"+
			       label+"'");
	    return false;
	}
	if (!dataset.addToStream(this)) return false;
	datasets.add(dataset);
	setHasChanged();
	return true;
    }
    
    /** remove a dataset from this stream */
    public boolean removeDataset(PrimaryDataset dataset)
    {
	int index = datasets.indexOf(dataset);
	if (index<0) {
	    System.out.println("Stream.removeDataset() WARNING: dataset '"+
			       dataset.label()+"' not associated with stream '"+
			       label+"'");
	    return false;
	}
	datasets.remove(index);
	dataset.removeFromStream(this);
	setHasChanged();
	return true;
    }
    
    
    /** retrieve array of assigned paths */
    public Path[] listOfPaths()
    {
	HashSet<Path> setOfPaths = new HashSet<Path>();
	for (PrimaryDataset ds : datasets) {
	    Iterator<Path> itP = ds.pathIterator();
	    while (itP.hasNext()) setOfPaths.add(itP.next());
	}
	return setOfPaths.toArray(new Path[setOfPaths.size()]);
    }

    /** number of paths */
    public int pathCount() { return listOfPaths().length; }

    /** retrieve i-th path */
    public Path path(int i) { return listOfPaths()[i]; }
    
    /** retrieve iterator over paths */
    public Iterator<Path> pathIterator()
    {
	HashSet<Path> setOfPaths = new HashSet<Path>();
	for (PrimaryDataset ds : datasets) {
	    Iterator<Path> itP = ds.pathIterator();
	    while (itP.hasNext()) setOfPaths.add(itP.next());
	}
	return setOfPaths.iterator();
    }
    
}

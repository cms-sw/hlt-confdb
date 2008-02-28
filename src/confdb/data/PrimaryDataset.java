package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * PrimaryDataset
 * --------------
 * @author Philipp Schieferdecker
 *
 * For HLT configurations; paths can be assigned to primary datasets,
 * in order to organize events for anlyzers.
 */
public class PrimaryDataset extends DatabaseEntry implements Comparable<PrimaryDataset>
{
    //
    // member data
    //
    
    /** label of the stream */
    private String label;

    /** collection of assigned paths */
    private ArrayList<Path> paths = new ArrayList<Path>();
    

    //
    // construction
    //
    
    /** standard constructor */
    public PrimaryDataset(String label)
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
    public int compareTo(PrimaryDataset s) { return toString().compareTo(s.toString()); }

    /** number of paths */
    public int pathCount() { return paths.size(); }

    /** retrieve i-th path */
    public Path path(int i) { return paths.get(i); }
    
    /** retrieve iterator over paths */
    public Iterator<Path> pathIterator() { return paths.iterator(); }
    
    /** index of a certain path */
    public int indexOfPath(Path path) { return paths.indexOf(path); }

    /** insert and associate a path with this stream */
    public boolean insertPath(Path path)
    {
	if (paths.indexOf(path)>=0) {
	    System.out.println("PrimaryDataset.insertPath() WARNING: path '"+
			       path.name()+"' already associated with stream '"+
			       label+"'");
	    return false;
	}
	if (!path.addToDataset(this)) return false;
	paths.add(path);
	return true;
    }
    
    /** remove a path from this stream */
    public boolean removePath(Path path)
    {
	int index = paths.indexOf(path);
	if (index<0) {
	    System.out.println("PrimaryDataset.removePath() WARNING: path '"+
			       path.name()+"' not associated with stream '"+
			       label+"'");
	    return false;
	}
	paths.remove(index);
	path.removeFromDataset(this);
	return true;
    }
    
    
}

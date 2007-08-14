package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Stream
 * ------
 * @author Philipp Schieferdecker
 *
 * For HLT configurations; paths can be assigned streams, where
 * streams have differnt priorities. The corresponding configuration
 * must be passed to the StorageManager (and doesn't make any sense in
 * the offline world!)
 */
public class Stream
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

    /** number of paths */
    public int pathCount() { return paths.size(); }

    /** retrieve i-th path */
    public Path path(int i) { return paths.get(i); }
    
    /** retrieve iterator over paths */
    public Iterator pathIterator() { return paths.iterator(); }
    
    /** index of a certain path */
    public int indexOfPath(Path path) { return paths.indexOf(path); }

    /** insert and associate a path with this stream */
    public boolean insertPath(Path path)
    {
	if (paths.indexOf(path)>=0) {
	    System.out.println("Stream.insertPath() WARNING: path '"+path.name()+
			       "' already associated with stream '"+label+"'");
	    return false;
	}
	if (!path.addToStream(this)) return false;
	paths.add(path);
	return true;
    }
    
    /** remove a path from this stream */
    public boolean removePath(Path path)
    {
	int index = paths.indexOf(path);
	if (index<0) {
	    System.out.println("Stream.removePath() WARNING: path '"+path.name()+
			       "' not associated with stream '"+label+"'");
	    return false;
	}
	if (!path.removeFromStream(this)) return false;
	paths.remove(index);
	return true;
    }
    
    
}

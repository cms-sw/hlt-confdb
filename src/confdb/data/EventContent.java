package confdb.data;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * EventContent
 * ------------
 * @author Philipp Schieferdecker
 *
 * Manage different CMSSW file formats, which Streams & OutputModules
 * are based on.
 */
public class EventContent extends DatabaseEntry
{
    //
    // member data
    //

    /** label of this event content */
    private String label;
    
    /** collection of assigned paths */
    private ArrayList<Path> paths = new ArrayList<Path>();
    
    /** collection of event content statements */
    private ArrayList<EventContentStatement> statements
	= new ArrayList<EventContentStatement>();
    
    /** collection of assigned streams */
    private ArrayList<Stream> streams = new ArrayList<Stream>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public EventContent(String label)
    {
	this.label = label;
    }
    
    
    //
    // member functions
    //
    
    /** label of the event content */
    public String label() { return label; }
    
    /** set the label of this event content */
    public void setLabel(String label) { this.label = label; }
    
    /** retrieve string representation of this event content */
    public String toString() { return label(); }
    

    /** number of paths */
    public int pathCount() { return paths.size(); }

    /** retrieve i-th path */
    public Path path(int i) { return paths.get(i); }

    /** retrieve path by name */
    public Path path(String pathName)
    {
	for (Path p : paths) if (p.name().equals(pathName)) return p;
	return null;
    }
    
    /** retrieve path iterator */
    public Iterator<Path> pathIterator() { return paths.iterator(); }

    /** retrieve index of a given path */
    public int indexOfPath(Path path) { return paths.indexOf(path); }

    /** insert a path into this event content */
    public boolean insertPath(Path path)
    {
	if (indexOfPath(path)>=0) return false;
	paths.add(path);
	path.addToContent(this);
	setHasChanged();
	return true;
    }
    
    /** remove a path from this event content */
    public boolean removePath(Path path)
    {
	int index = paths.indexOf(path);
	if (index < 0) return false;
	paths.remove(index);
	Iterator<EventContentStatement> itEC = statementIterator();
	while (itEC.hasNext()) if(itEC.next().parentPath()==path) itEC.remove();
	Iterator<Stream> itS = streamIterator();
	while (itS.hasNext()) itS.next().removePath(path);
	path.removeFromContent(this);
	setHasChanged();
	return true;
    }
    
    
    /** number of statements */
    public int statementCount() { return statements.size(); }

    /** retrieve i-th statement */
    public EventContentStatement statement(int i) { return statements.get(i); }
    
    /** retrieve statement iterator */
    public Iterator<EventContentStatement> statementIterator()
    {
	return statements.iterator();
    }
    
    /** retrieve index of a given statement */
    public int indexOfStatement(EventContentStatement ecs)
    {
	return statements.indexOf(ecs);
    }

    /** insert a statement into event content */
    public boolean insertStatement(EventContentStatement ecs)
    {
	if (statements.indexOf(ecs)>=0) return false;
	if (ecs.parentPath()!=null&&
	    paths.indexOf(ecs.parentPath())<0) return false;
	statements.add(ecs);
	setHasChanged();
	return true;
    }

    /** remove a statement from this event content */
    public boolean removeStatement(EventContentStatement ecs)
    {
	int index = statements.indexOf(ecs);
	if (index<0) return false;
	statements.remove(ecs);
	return true;
    }

    /** number of streams associated with this event content */
    public int streamCount() { return streams.size(); }

    /** retrieve i-th stream associated with event content */
    public Stream stream(int i) { return streams.get(i); }

    /** retrieve stream with specific label from event content */
    public Stream stream(String streamLabel) {
	for (Stream s : streams) if (s.label().equals(streamLabel)) return s;
	return null;
    }
    
    /** retrieve stream iterator */
    public Iterator<Stream> streamIterator() { return streams.iterator(); }

    /** retrieve index of a given stream */
    public int indexOfStream(Stream stream) { return streams.indexOf(stream); }

    /** associate an existing stream with this event content */
    public boolean insertStream(Stream stream)
    {
	if (streams.indexOf(stream)>=0) return false;
	streams.add(stream);
	setHasChanged();
	return true;
    }
    
    /** remove a stream from the event content */
    public boolean removeStream(Stream stream)
    {
	int index = streams.indexOf(stream);
	if (index<0) return false;
	streams.remove(index);
	setHasChanged();
	return true;
    }

}

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

    /** name of this event content */
    private String name;
    
    /** collection of event content statements */
    private ArrayList<EventContentStatement> statements
	= new ArrayList<EventContentStatement>();
    
    /** collection of assigned streams */
    private ArrayList<Stream> streams = new ArrayList<Stream>();

    /** parent configuration */
    private IConfiguration config = null;
    
    //
    // construction
    //
    
    /** standard constructor */
    public EventContent(String name)
    {
	this.name = name;
    }
    
    
    //
    // member functions
    //
    
    /** name of the event content */
    public String name() { return name; }
    
    /** set the name of this event content */
    public void setName(String name) { this.name = name; }
    
    /** retrieve string representation of this event content */
    public String toString() { return name(); }
    
    /** get the parent configuration */
    public IConfiguration config() { return config; }

    /** set the parent configuration */
    public void setConfig(IConfiguration config) { this.config = config; }


    /** number of paths */
    public int pathCount() { return paths().size(); }

    /** retrieve i-th path */
    public Path path(int i) { return paths().get(i); }

    /** retrieve path by name */
    public Path path(String pathName)
    {
	Iterator<Path> itP = pathIterator();
	while (itP.hasNext()) {
	    Path path = itP.next();
	    if (path.name().equals(pathName)) return path;
	}
	return null;
    }
    
    /** retrieve path iterator */
    public Iterator<Path> pathIterator() { return paths().iterator(); }

    /** retrieve index of a given path */
    public int indexOfPath(Path path) { return paths().indexOf(path); }

    public void removePath(Path path)
    {
	if (paths().indexOf(path)>=0) return;
	Iterator<EventContentStatement> itEC = statementIterator();
	while (itEC.hasNext()) if(itEC.next().parentPath()==path) itEC.remove();
	path.removeFromContent(this);
	setHasChanged();
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
	    indexOfPath(ecs.parentPath())<0) return false;
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

    /** retrieve stream with specific name from event content */
    public Stream stream(String streamName) {
	for (Stream s : streams) if (s.name().equals(streamName)) return s;
	return null;
    }
    
    /** retrieve stream iterator */
    public Iterator<Stream> streamIterator() { return streams.iterator(); }

    /** retrieve index of a given stream */
    public int indexOfStream(Stream stream) { return streams.indexOf(stream); }

    /** associate an existing stream with this event content */
    public Stream insertStream(String streamName)
    {
	Iterator<Stream> itS = streamIterator();
	while (itS.hasNext())
	    if (itS.next().name().equals(streamName)) return null;
	Stream stream = new Stream(streamName,this);
	streams.add(stream);
	setHasChanged();
	return stream;
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

    /** retrieve number of associated primary dataset */
    public int datasetCount() { return datasets().size(); }
    
    /** retrieve i-th primary dataset */
    public PrimaryDataset dataset(int i) { return datasets().get(i); }

    /** retireve primary dataset by name */
    public PrimaryDataset dataset(String datasetName)
    {
	Iterator<PrimaryDataset> itPD = datasetIterator();
	while (itPD.hasNext()) {
	    PrimaryDataset dataset = itPD.next();
	    if (dataset.name().equals(datasetName)) return dataset;
	}
	return null;
    }

    /** retrieve dataset iterator */
    public Iterator<PrimaryDataset> datasetIterator()
    {
	return datasets().iterator();
    }
    
    /** retrieve index of a given dataset */
    public int indexOfDataset(PrimaryDataset dataset)
    {
	return datasets().indexOf(dataset);
    }

    //
    // private memeber functions
    //

    /** retrieve list of paths from associated streams */
    private ArrayList<Path> paths()
    {
	ArrayList<Path> result = new ArrayList<Path>();
	Iterator<Stream> itS = streamIterator();
	while (itS.hasNext()) {
	    Iterator<Path> itP = itS.next().pathIterator();
	    while (itP.hasNext()) {
		Path path = itP.next();
		if (result.indexOf(path)<0) result.add(path);
	    }
	}
	return result;
    }


    /** retrieve list of datasets from associated streams */
    private ArrayList<PrimaryDataset> datasets()
    {
	ArrayList<PrimaryDataset> result = new ArrayList<PrimaryDataset>();
	Iterator<Stream> itS = streamIterator();
	while (itS.hasNext()) {
	    Iterator<PrimaryDataset> itPD = itS.next().datasetIterator();
	    while (itPD.hasNext()) result.add(itPD.next());
	}
	return result;
    }
}

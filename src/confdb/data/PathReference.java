package confdb.data;

/**
 * PathReference
 * -------------
 * @author Philipp Schieferdecker
 *  
 * Reference to a Path.
 */
public class PathReference extends Reference
{
    //
    // construction
    //
    
    /** standard constructor */
    public PathReference(ReferenceContainer container,Path path)
    {
	super(container,path);
    }
    
}


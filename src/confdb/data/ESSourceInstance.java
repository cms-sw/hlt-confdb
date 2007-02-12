package confdb.data;


/**
 * ESSourceInstance
 * ----------------
 * @author Philipp Schieferdecker
 *
 * CMSSW event setup source instance.
 */
public class ESSourceInstance extends Instance
{
    //
    // construction
    //
    
    /** standard constructor */
    public ESSourceInstance(String name,ESSourceTemplate template)
    {
	super(name,template);
    }
    
}

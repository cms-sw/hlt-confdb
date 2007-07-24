package confdb.data;


/**
 * ESModuleInstance
 * ----------------
 * @author Philipp Schieferdecker
 *
 * CMSSW event setup module instance.
 */
public class ESModuleInstance extends Instance
{
    //
    // construction
    //
    
    /** standard constructor */
    public ESModuleInstance(String name,ESModuleTemplate template)
    {
	super(name,template);
    }
    
}

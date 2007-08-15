package confdb.data;


/**
 * ServiceInstance
 * ---------------
 * @author Philipp Schieferdecker
 *
 * CMSSW framework service instance.
 */


public class ServiceInstance extends Instance
{
    //
    // construction
    //
    
    /** standard constructor */
    public ServiceInstance(String name,ServiceTemplate template)
    {
	super(name,template);
    }

}

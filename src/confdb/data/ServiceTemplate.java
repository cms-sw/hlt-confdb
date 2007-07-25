package confdb.data;

import java.util.ArrayList;

/**
 * ServiceTemplate
 * ---------------
 * @author Philipp Schieferdecker
 *
 * Template of a CMSSW framework service.
 */
public class ServiceTemplate extends Template
{
    //
    // member data
    //
    
    /** template type */
    private String type = "Service";
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ServiceTemplate(String name,String cvsTag,int dbId,
			   ArrayList<Parameter> parameters)
    {
	super(name,cvsTag,dbId,parameters);
    }

    //
    // abstract member function implementations
    //
    
    /** type of the template */
    public String type() { return type; }
    
    /** create or retrieve an instance of this template (name=template-name)*/
    public Instance instance() throws DataException
    {
	if (instances.size()>0) return instances.get(0);
	ServiceInstance instance = new ServiceInstance(name,this);
	instances.add(instance);
	return instance;
    }

    /** create or retrieve an instance of this template */
    public Instance instance(String instanceName) throws DataException
    {
	if (instanceName.equals(name)) return instance();
	String msg = "Can't instantiate Service Instance of type '"+name+
	    "' with name '"+instanceName+"'.";
	throw new DataException(msg);
    }
    
    /** remove an instance of this template (name=template-name) */
    public void removeInstance() throws DataException
    {
	if (instances.size()==0) {
	    String msg = "Failed to remove Service instance '"+name+"'.";
	    throw new DataException(msg);
	}
	instances.clear();
    }

    /** remove an instance of this template */
    public void removeInstance(String instanceName) throws DataException
    {
	if (instanceName.equals(name)) {
	    removeInstance();
	    return;
	}
	String msg = "Can't remove Service Instance of type'"+name+
	    "' with name '"+instanceName+"'.";
	throw new DataException(msg);
    }

}

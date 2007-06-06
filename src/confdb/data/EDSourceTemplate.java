package confdb.data;

import java.util.ArrayList;

/**
 * EDSourceTemplate
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Template of a CMSSW event data source.
 */
public class EDSourceTemplate extends Template
{
    //
    // member data
    //
    
    /** template type */
    private String type = "EDSource";
    
    /** keyword */
    private String keyword = "source";

    
    //
    // construction
    //
    
    /** standard constructor */
    public EDSourceTemplate(String name,String cvsTag,int dbId,
			    ArrayList<Parameter> parameters)
    {
	super(name,cvsTag,dbId,parameters);
    }

    
    //
    // member functions
    //

    /** type of the template */
    public String type()
    {
	return type;
    }
    
    /** keyword (configuration language) */
    public String keyword()
    {
	return keyword;
    }
    
    /** create or retrieve an instance of this template (name=template-name)*/
    public Instance instance() throws DataException
    {
	if (instances.size()>0) return instances.get(0);
	EDSourceInstance instance = new EDSourceInstance(name,this);
	instances.add(instance);
	return instance;
    }
    
    /** create or retrieve an instance of this template */
    public Instance instance(String instanceName) throws DataException
    {
	if (instanceName.equals(name)) return instance();
	String msg = "Can't instantiate EDSource Instance of type '"+name+
	    "' with name '"+instanceName+"'.";
	throw new DataException(msg);
    }
    
    /** remove an instance of this template (name=template-name) */
    public void removeInstance() throws DataException
    {
	if (instances.size()==0) {
	    String msg = "Failed to remove EDSource Instance '"+name+"'.";
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
	String msg = "Can't remove EDSource Instance of type '"+name+
	    "' with name '"+instanceName+"'.";
	throw new DataException(msg);
    }

}

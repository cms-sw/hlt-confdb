package confdb.data;

import java.util.ArrayList;

/**
 * Module
 * ------
 * @author Philipp Schieferdecker
 *
 * CMSSW framework module.
 */
public class ModuleTemplate extends Template
{
    //
    // member data
    //
    
    /** type of the module */
    private String type = null;
    
    /** keyword */
    private String keyword = "module";
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ModuleTemplate(String name,String cvsTag,int dbId,
			  ArrayList<Parameter> parameters,
			  String type)
    {
	super(name,cvsTag,dbId,parameters);
	this.type = type;
    }

    //
    // member functions
    //

    /** type of the template */
    public String type() { return type; }
    
    /** keyword (configuration language) */
    public String keyword() { return keyword; }
    
    /** create or retrieve an instance of this template (name=template-name)*/
    public Instance instance() throws DataException
    {
	for (Instance i : instances)
	    if (i.name().equals(name)) return i;
	ModuleInstance instance = new ModuleInstance(name,this);
	instances.add(instance);
	return instance;
    }

    /** create or retrieve an instance of this template */
    public Instance instance(String instanceName) throws DataException
    {
	for (Instance i : instances)
	    if (i.name().equals(instanceName)) return i;
	ModuleInstance instance = new ModuleInstance(instanceName,this);
	instances.add(instance);
	return instance;
    }
    
    /** remove an instance of this template (name=template-name) */
    public void removeInstance() throws DataException
    {
	for (Instance i : instances) {
	    if (i.name().equals(name)) {
		instances.remove(instances.indexOf(i));
		return;
	    }
	}
	String msg = "Failed to remove Module Instance '"+name+"'.";
	throw new DataException(msg);
    }

    /** remove an instance of this template */
    public void removeInstance(String instanceName) throws DataException
    {
	for (Instance i : instances) {
	    if (i.name().equals(instanceName)) {
		instances.remove(instances.indexOf(i));
		return;
	    }
	}
	String msg = "Failed to remove Module Instance '"+instanceName+"'.";
	throw new DataException(msg);
    }
    
}

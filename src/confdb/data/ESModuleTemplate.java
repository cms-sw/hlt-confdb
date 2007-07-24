package confdb.data;

import java.util.ArrayList;

/**
 * ESModuleTemplate
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Template of a CMSSW event setup module.
 */
public class ESModuleTemplate extends Template
{
    //
    // member data
    //
    
    /** template type */
    private String type = "ESModule";
    
    /** keyword */
    private String keyword = "es_module";

    
    //
    // construction
    //
    
    /** standard constructor */
    public ESModuleTemplate(String name,String cvsTag,int dbId,
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
	for (Instance i : instances)
	    if (i.name().equals(name)) return i;
	ESModuleInstance instance = new ESModuleInstance(name,this);
	instances.add(instance);
	return instance;
    }

    /** create or retrieve an instance of this template */
    public Instance instance(String instanceName) throws DataException
    {
	for (Instance i : instances)
	    if (i.name().equals(instanceName)) return i;
	ESModuleInstance instance = new ESModuleInstance(instanceName,this);
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
	String msg = "Failed to remove ESModule Instance '"+name+"'.";
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
	String msg = "Failed to remove ESModule Instance '"+instanceName+"'.";
	throw new DataException(msg);
    }
    

}

package confdb.data;

import java.util.ArrayList;

/**
 * ESSourceTemplate
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Template of a CMSSW event setup source.
 */
public class ESSourceTemplate extends Template
{
    //
    // member data
    //
    
    /** template type */
    private String type = "ESSource";
    
    /** keyword */
    private String keyword = "es_source";

    
    //
    // construction
    //
    
    /** standard constructor */
    public ESSourceTemplate(String name,String cvsTag,int dbSuperId,
			    ArrayList<Parameter> parameters)
    {
	super(name,cvsTag,dbSuperId,parameters);
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
	ESSourceInstance instance = new ESSourceInstance(name,this);
	instances.add(instance);
	return instance;
    }

    /** create or retrieve an instance of this template */
    public Instance instance(String instanceName) throws DataException
    {
	for (Instance i : instances)
	    if (i.name().equals(instanceName)) return i;
	ESSourceInstance instance = new ESSourceInstance(instanceName,this);
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
	String msg = "Failed to remove ESSource Instance '"+name+"'.";
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
	String msg = "Failed to remove ESSource Instance '"+instanceName+"'.";
	throw new DataException(msg);
    }
    

}

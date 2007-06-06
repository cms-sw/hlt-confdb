package confdb.data;

import java.util.ArrayList;

/**
 * Template
 * --------
 * @author Philipp Schieferdecker
 *
 * abstract base-class for Service, EDSource, ESSource, and Module Templates.
 */
abstract public class Template
{
    //
    // member data
    //
    
    /** name of the template */
    protected String name = null;
    
    /** cvs tag of the template */
    protected String cvsTag = null;

    /** the database super id */
    private int dbId = 0;

    /** parameters of this template */
    private ArrayList<Parameter> parameters = null;
    
    /** list of instances of this template */
    protected ArrayList<Instance> instances = new ArrayList<Instance>();
    

    //
    // construction
    //
    
    /** standard constructor */
    public Template(String name,String cvsTag,int dbId,
		    ArrayList<Parameter> parameters)
    {
	this.name       = name;
	this.cvsTag     = cvsTag;
	this.dbId       = dbId;
	this.parameters = parameters;
	for (Parameter p : this.parameters) p.setParent(this);
    }


    //
    // abstract member functions
    //

    /** type of the template */
    abstract public String type();
    
    /** keyword (configuration language) */
    abstract public String keyword();
    
    /** create or retrieve an instance of this template (name=template-name)*/
    abstract public Instance instance() throws DataException;

    /** create or retrieve an instance of this template */
    abstract public Instance instance(String instanceName) throws DataException;
    
    /** remove an instance of this template (name=template-name) */
    abstract public void removeInstance() throws DataException;

    /** remove an instance of this template */
    abstract public void removeInstance(String instanceName) throws DataException;
    

    //
    // non-abstract member functions
    //
    
    /** name of the template */
    public String name() { return name; }

    /** cvs tag of the template */
    public String cvsTag() { return cvsTag; }

    /** database id */
    public int dbId() { return dbId; }

    /** number of parameters */
    public int parameterCount() { return parameters.size(); }
    
    /** get the i-th parameter */
    public Parameter parameter(int i) { return parameters.get(i); }
     
    /** get parameter by name */
    public Parameter parameter(String name)
    {
	for (Parameter p : parameters)
	    if (name.equals(p.name())) return p;
	System.out.println("ERROR: template '"+name()+
			   "' has no parameter '"+name+"'.");
	return null;
    }
     
    /** get the index of a parameter */
    public int parameterIndex(Parameter p) { return parameters.indexOf(p); }
    
    /** number of instance */
    public int instanceCount() { return instances.size(); }

    /** retrieve the i-th instance */
    public Instance instance(int i) { return instances.get(i); }
    
    /** check if instance with name 'instanceName' exists */
    public boolean hasInstance(String instanceName)
    {
	for (Instance i : instances) if (i.name().equals(instanceName)) return true;
	return false;
    }
    
    /** remove all instances of this template */
    public int removeAllInstances()
    {
	int result = instances.size();
	instances.clear();
	return result;
    }

    /** check if there is an instance with the specified parameters */
    public boolean isUniqueInstance(Instance instance)
    {
	if (instances.indexOf(instance)==-1) {
	    System.out.println("WARNING: isInstanceUnique called for "+
			       "unknown instance!");
	    return true;
	}
	
	for (int i=0;i<instanceCount();i++) {
	    Instance other = instance(i);
	    if (other.equals(instance)) continue;
	    boolean sameParams = true;
	    for (int j=0;j<parameterCount();j++) {
		if (!other.parameter(j).valueAsString()
		    .equals(instance.parameter(j).valueAsString()))
		    sameParams = false;
		if (!sameParams) break;
	    }
	    if (sameParams) return false;
	}
	return true;
    }

    /** check if there is an instance with the specified parameters */
    public boolean isUniqueParameterSet(ArrayList<Parameter> parameterSet)
    {
	if (parameterSet.size()!=parameterCount()) return true;
	for (int i=0;i<parameterCount();i++)
	    if (!parameter(i).name().equals(parameterSet.get(i).name()))
		return true;
	
	for (int i=0;i<instanceCount();i++) {
	    Instance instance = instance(i);
	    boolean  sameParams = true;
	    for (int j=0;j<parameterCount();j++) {
		if (!parameterSet.get(j).valueAsString()
		    .equals(instance.parameter(j).valueAsString()))
		    sameParams = false;
		if (!sameParams) break;
	    }
	    if (sameParams) return false;
	}
	return true;
    }
    
    /** check if all instances have unique parameters */
    public boolean areAllInstancesUnique()
    {
	for (int i=0;i<instanceCount()-1;i++) {
	    Instance instance1 = instance(i);
	    for (int j=i+1;j<instanceCount();j++) {
		Instance instance2 = instance(j);
		boolean  notUnique = true;
		for (int k=0;k<instance1.parameterCount();k++) {
		    String valueAsString1 = instance1.parameter(k).valueAsString();
		    String valueAsString2 = instance2.parameter(k).valueAsString();
		    if (!valueAsString1.equals(valueAsString2)) {
			notUnique = false;
			break;
		    }
		}
		if (notUnique) return false;
	    }
	}
	return true;
    }
    
    /** set the db id */
    public void setDbId(int dbId) { this.dbId = dbId; }

}

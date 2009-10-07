package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
 * Template
 * --------
 * @author Philipp Schieferdecker
 *
 * abstract base-class for Service, EDSource, ESSource, and Module Templates.
 */
abstract public class Template extends    ParameterContainer
                               implements Comparable<Template>
{
    //
    // member data
    //
    
    /** name of the template */
    protected String name = null;
    
    /** cvs tag of the template */
    protected String cvsTag = null;

    /** parent software package */
    private SoftwarePackage parentPackage = null;

    /** list of instances of this template */
    protected ArrayList<Instance> instances = new ArrayList<Instance>();
    

    //
    // construction
    //
    
    /** standard constructor */
    public Template(String name,String cvsTag,ArrayList<Parameter> parameters)
    {
	this.name   = name;
	this.cvsTag = cvsTag;
	if (parameters!=null) for (Parameter p : parameters) addParameter(p);
    }


    //
    // abstract member functions
    //

    /** type of the template */
    abstract public String type();
    
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
    
    /** toString conversion */
    public String toString() { return name; }

    /** ParameterContainer: get the default value as a string */
    public String parameterDefaultValueAsString(Parameter p)
    {
	if (containsParameter(p)) return p.valueAsString();
	System.err.println("Template ERROR: unknown parameter: "+p);
	return new String();
    }
    
    /** ParameterContainer: indicate wether a parameter is removable */
    public boolean isRemovable(Parameter p) { return false; }

    /** Comparable: compareTo */
    public int compareTo(Template t) { return name().compareTo(t.name()); }
    
    /** name of the template */
    public String name() { return name; }

    /** cvs tag of the template */
    public String cvsTag() { return cvsTag; }

    /** parent software package of the template */
    public SoftwarePackage parentPackage() { return parentPackage; }


    /** set parameters */
    public void setParameters(ArrayList<Parameter> params)
    {
	clear();
	for (Parameter p : params) addParameter(p);
    }
    

    /** number of instances */
    public int instanceCount() { return instances.size(); }

    /** retrieve the i-th instance */
    public Instance instance(int i) { return instances.get(i); }
    
    /** get instance iterator */
    public Iterator<Instance> instanceIterator()
    {
	Collections.sort(instances);
	return instances.iterator();
    }
    
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

    /** set the parent software package */
    public void setParentPackage(SoftwarePackage p) { parentPackage = p; }

}

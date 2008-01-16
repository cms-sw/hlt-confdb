package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Instance
 * --------
 * @author Philipp Schieferdecker
 *
 * abstract base class for Service, EDSource, ESSource, and Module Instances.
 */
abstract public class Instance extends DatabaseEntry implements Comparable<Instance>
{
    //
    // member data
    //
    
    /** name of the instance*/
    protected String name = null;

    /** reference to the template of this instance */
    private Template template = null;

    /** list of parameters */
    private ArrayList<Parameter> parameters = new ArrayList<Parameter>();

    /** parent configuration of instance */
    private IConfiguration config = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public Instance(String name,Template template) throws DataException
    {
	this.template = template;
	setName(name);
	for (int i=0;i<template.parameterCount();i++)
	    parameters.add(template.parameter(i).clone(this));
    }
    
    
    //
    // non-abstract member functions
    //
    
    /** overload toString */
    public String toString() { return name(); }
    
    /** Comparable: compareTo() */
    public int compareTo(Instance i) { return toString().compareTo(i.toString()); }

    /** name of the instance */
    public String name() { return name; }
    
    /** get the template */
    public Template template() { return template; }
    
    /** get the configuration */
    protected IConfiguration config() { return config; }
    
    /** number of parameters */
    public int parameterCount() { return parameters.size(); }
    
    /** get i-th parameter */
    public Parameter parameter(int i) { return parameters.get(i); }
    
    /** get parameter by name and type */
    public Parameter parameter(String name)
    {
	for (Parameter p : parameters)
	    if (name.equals(p.name())) return p;
	return null;
    }
    
    /** get parameter by name and type */
    public Parameter parameter(String name,String type)
    {
	for (Parameter p : parameters)
	    if (name.equals(p.name())&&type.equals(p.type())) return p;
	return null;
    }
    
    /** parameter iterator */
    public Iterator<Parameter> parameterIterator() { return parameters.iterator(); }

    /** recursively retrieve parameters to all levels */
    public Iterator<Parameter> recursiveParameterIterator()
    {
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	getParameters(parameterIterator(),params);
	return params.iterator();
    }

    /** get all parameters (recursively) with specified name */
    public Parameter[] findParameters(String name)
    {
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	Iterator<Parameter> itP = recursiveParameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
	    String fullParamName = p.fullName();
	    if (fullParamName.equals(name)||
		(!fullParamName.equals(name)&&
		 fullParamName.endsWith("::"+name))) params.add(p);
	}
	return params.toArray(new Parameter[params.size()]);
    }
    
    /** get all parameters (recursively) with specified name *and* type */
    public Parameter[] findParameters(String name,String type)
    {
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	Iterator<Parameter> itP = recursiveParameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
	    String fullParamName = p.fullName();
	    if ((fullParamName.equals(name)||
		 (!fullParamName.equals(name)&&
		  fullParamName.endsWith("::"+name)))&&
		p.type().equals(type)) params.add(p);
	}
	return params.toArray(new Parameter[params.size()]);
    }
    
    /** get the index of a parameter */
    public int indexOfParameter(Parameter p) { return parameters.indexOf(p); }
    
    /** set the name of this instance */
    public void setName(String name) throws DataException
    {
	if (template().hasInstance(name)||
	    (config!=null&&!config.isUniqueQualifier(name)))
	    throw new DataException("Instance.setName() ERROR: " +
				    "name '"+name+"' is not unique!");
	this.name = name;
	setHasChanged();
    }
    
    /** set the parent configuration of the instance */
    public void setConfiguration(IConfiguration config) { this.config = config; }

    /** update a parameter when the value is changed */
    public void updateParameter(int index,String valueAsString)
    {
	String  oldValueAsString = parameter(index).valueAsString();
	if (valueAsString.equals(oldValueAsString)) return;
	
	String  defaultAsString  = template.parameter(index).valueAsString();
	parameter(index).setValue(valueAsString,defaultAsString);
	setHasChanged();
    }
    
    /** update a parameter when the value is changed */
    public boolean updateParameter(String name,String type,String valueAsString)
    {
	Parameter[] params = findParameters(name,type);
	
	if (params.length>0) {

	    Parameter param = params[0];

	    if (params.length>1) {
		for (Parameter p : params) {
		    String a[] = p.fullName().split("::");
		    String b[] = param.fullName().split("::");
		    if (a.length<b.length) param = p;
		}
	    }
	    
	    int index = indexOfParameter(param);
	    if (index>=0) {
		updateParameter(index,valueAsString);
		return true;
	    }
	    String a[] = param.fullName().split("::");
	    if (a.length>1) {
		String b[] = a[0].split("\\[");
		Parameter parentParam = parameter(b[0]);
		int parentIndex = indexOfParameter(parentParam);
		if (parentIndex>=0) {
		    param.setValue(valueAsString,"");
		    String defaultAsString =
			template.parameter(parentIndex).valueAsString();
		    parentParam.setValue(parentParam.valueAsString(),
					 defaultAsString);
		    setHasChanged();
		    return true;
		}
	    }
	}
	System.err.println("Instance.updateParameter ERROR: "+
			   "no parameter '"+name+"' of type '"+type+"' "+
			   "in "+template.name()+"."+name());
	return false;
    }
    
    /** set parameters */
    public boolean setParameters(ArrayList<Parameter> newParameters)
    {
	for (int i=0;i<newParameters.size();i++) {
	    Parameter parameter     = newParameters.get(i);
	    String    parameterName = parameter.name();
	    String    parameterType = parameter.type();
	    String    valueAsString = parameter.valueAsString();
	    if (!updateParameter(parameterName,parameterType,valueAsString))
		return false;
	}
	return true;
    }

    /** remove this instance */
    public void remove()
    {
	try {
	    template.removeInstance(name);
	}
	catch (DataException e) {
	    System.err.println("Instance.remove ERROR: "+e.getMessage());
	}
    }

    /** number of unset tracked parameters */
    public int unsetTrackedParameterCount()
    {
	int result = 0;
	for (Parameter p : parameters) {
	    if (p instanceof VPSetParameter) {
		VPSetParameter vpset = (VPSetParameter)p;
		//if (vpset.parameterSetCount()>0)
		result += vpset.unsetTrackedParameterCount();
		//else if (vpset.isTracked())
		//result++;
	    }
	    else if (p instanceof PSetParameter) {
		PSetParameter pset = (PSetParameter)p;
		//if (pset.parameterCount()>0)
		result += pset.unsetTrackedParameterCount();
		//else if (pset.isTracked())
		//result++;
	    }
	    else {
		if (p.isTracked()&&!p.isValueSet()) result++;
	    }
	}
	return result;
    }
    
    //
    // private member functions
    //
    
    /** needed to retrieve parameters to all levels recursively */
    private void getParameters(Iterator<Parameter> itParam,
			       ArrayList<Parameter> params)
    {
	while (itParam.hasNext()) {
	    Parameter param = itParam.next();
	    params.add(param);
	    if (param instanceof PSetParameter) {
		PSetParameter pset = (PSetParameter)param;
		getParameters(pset.parameterIterator(),params);
	    }
	    else if (param instanceof VPSetParameter) {
		VPSetParameter vpset = (VPSetParameter)param;
		getParameters(vpset.parameterIterator(),params);
	    }
	}
    }
    


}

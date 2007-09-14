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

    
    //
    // construction
    //
    
    /** standard constructor */
    public Instance(String name,Template template)
    {
	this.name     = name;
	this.template = template;
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
    
    /** parameter iterator */
    public Iterator parameterIterator() { return parameters.iterator(); }

    /** number of parameters */
    public int parameterCount() { return parameters.size(); }
    
    /** get i-th parameter */
    public Parameter parameter(int i) { return parameters.get(i); }

    /** get parameter by name & type */
    public Parameter parameter(String name,String type)
    {
	for (Parameter p : parameters)
	    if (name.equals(p.name())&&type.equals(p.type())) return p;
	return null;
    }
    
    /** get the index of a parameter */
    public int indexOfParameter(Parameter p) { return parameters.indexOf(p); }
    
    /** set the name of this instance */
    public void setName(String name) { this.name = name; setHasChanged(); }
    
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
	for (int i=0;i<parameterCount();i++) {
	    if (name.equals(parameter(i).name())&&
		type.equals(parameter(i).type())) {
		updateParameter(i,valueAsString);
		return true;
	    }
	}
	System.err.println("Instance.updateParameter ERROR: "+
			   "no parameter '"+name+"' of type "+type);
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
    
}

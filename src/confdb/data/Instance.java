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
public class Instance extends ParameterContainer implements Comparable<Instance>
{
    //
    // member data
    //

    /** name of the instance*/
    protected String name = null;

    /** reference to the template of this instance */
    private Template template = null;

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
	    addParameter(template.parameter(i).clone(this));
    }
    
    
    //
    // member functions
    //
    
    /** overload toString */
    public String toString() { return name(); }
  
    /** Comparable: compareTo() */
    public int compareTo(Instance i)
    {
	return toString().compareTo(i.toString());
    }
    
    /** name of the instance */
    public String name() { return name; }
    
    /** get the template */
    public Template template() { return template; }
    
    /** get the configuration */
    public IConfiguration config() { return config; }
    
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
    public void setConfiguration(IConfiguration config) { this.config=config; }

    /** ParameterContainer: retrieve default value of a given parameter */
    public String parameterDefaultValueAsString(Parameter p)
    {
	if (isRemovable(p)) return new String();
	return template.parameter(indexOfParameter(p)).valueAsString();
    }
    
    /** ParameterContainer: indicate wether a parameter can be removed */
    public boolean isRemovable(Parameter p)
    {
	int index = indexOfParameter(p);
	if (index<template.parameterCount()) return false;
	return true;
    }
    
    /** set parameters */
    /*
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
    */
    

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

}

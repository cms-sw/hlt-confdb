package confdb.data;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Parameter
 * ---------
 * @author Philipp Schieferdecker
 *
 * Common parameter base class for scalar and vector type parameters.
 */
abstract public class Parameter implements Comparable<Parameter>, Serializable
{
    //
    // member data
    //
    
    /** parent object */
    private  Object   parent = null;

    /** name of the parameter */
    protected String  name = null;
    
    /** flag indicating if the parameter is tracked/untracked */
    protected boolean isTracked = true;
    
    /** flag indicating if the value of this parameter is set */
    protected boolean isValueSet = false;
    
    
    //
    // construction
    //
    
    /** default constructor */
    public Parameter(String name,boolean isTracked)
    {
	this.name      = name;
	this.isTracked = isTracked;
    }
    
    //
    // abstract interface
    //
    
    /** make a clone of the parameter */
    abstract public Parameter clone(Object parent);
    
    /** type of the parameter as a string */
    abstract public String type();
    
    /** retrieve value(s) as a string */
    abstract public String valueAsString();

    /** set the value the parameter from string */
    abstract public boolean setValue(String valueAsString);

    
    //
    // member functions
    //

    /** overload toString() */
    public String toString() { return name(); }

    /** Comparable: compareTo() */
    public int compareTo(Parameter p) { return toString().compareTo(p.toString()); }

    /** retrieve the parent of the parameter */
    public Object parent() { return parent; }
    
    /** retrieve the name of the parameter */
    public String name() { return name; }

    /** retrieve the flag indication ig the parameter is tracked/untracked */
    public boolean isTracked() { return isTracked; }

    /** retrieve the flag indication ig the parameter value is set */
    public boolean isValueSet() { return isValueSet; }

    /** retrieve status indicating if the parameter is at its default */
    public boolean isDefault()
    {
	ParameterContainer container = getParentContainer();
	if (container==null) return false;
	return container.isParameterAtItsDefault(this);
    }
    
    /** set the parent object */
    public void setParent(Object parent) { this.parent = parent; }

    /** set isTracked */
    public void setTracked(boolean isTracked) { this.isTracked = isTracked; }

    /** get the parent container, if any (otherwise: global pset, likley) */
    public ParameterContainer getParentContainer()
    {
	Object p = parent();
	while (p!=null) {
	    if (p instanceof ParameterContainer) return (ParameterContainer)p;
	    else if (p instanceof Parameter) {
		Parameter param = (Parameter)p;
		p = param.parent();
	    }
	    else p=null;
	}
	return null;
    }
    

    /** get the parent instance, if any (otherwise: global pset, most likley) */
    /*
      public Instance getParentInstance()
      {
      Object p = parent();
      while (p!=null) {
      if (p instanceof Instance) return (Instance)p;
      else if (p instanceof Parameter) {
      Parameter param = (Parameter)p;
      p = param.parent();
      }
      else p=null;
      }
      return null;
      }
    */
    
    /** get the full name of the parameter, including parent parameters */
    public String fullName()
    {
	StringBuffer result = new StringBuffer(name());
	Parameter param = this;
	Object    p     = parent();
	while (p != null) {
	    if (p instanceof VPSetParameter) {
		VPSetParameter vpset = (VPSetParameter)p;
		PSetParameter  pset  = (PSetParameter)param;
		int            index = vpset.indexOfParameterSet(pset);
		result.insert(0,vpset.name()+"["+index+"]");
		param = vpset;
		p     = vpset.parent();
	    }
	    else if (p instanceof Parameter) {
		param = (Parameter)p;
		result.insert(0,param.name()+"::");
		p = param.parent();
	    }
	    else p=null;
	}
	return result.toString();
    }

    
    //
    // static member functions
    //
    
    /** static function to retrieve parameters recursively, given an iterator */
    public static void getParameters(Iterator<Parameter> itParam,
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

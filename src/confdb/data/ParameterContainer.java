package confdb.data;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * ParameterContainer
 * ------------------
 * @author Philipp Schieferdecker
 *
 * Base-class for all components which contain a list of parameters,
 * like instances, output modules, etc.
 */
abstract public class ParameterContainer extends DatabaseEntry
{
    //
    // member data
    //

    /** collection of parameters */
    private ArrayList<Parameter> parameters = new ArrayList<Parameter>();


    //
    // abstract member functions
    //
    
    /** retrieve default value of a given parameter */
    abstract public String parameterDefaultValueAsString(Parameter p);
    
    /** indicate wether a parameter can be removed or not */
    abstract public boolean isRemovable(Parameter p);

    //
    // non-abstract member functions
    //

    /** add a parameter */
    public void addParameter(Parameter parameter)
    {
	parameters.add(parameter);
	parameter.setParent(this);
    }

    /** remove a given parameter */
    public void removeParameter(Parameter parameter)
    {
	parameters.remove(parameter);
	parameter.setParent(null);
    }

    /** remove all parameters */
    public void clear() { parameters.clear(); }
    
    /** number of parameters */
    public int parameterCount() { return parameters.size(); }

    /** retrieve i-th parameter */
    public Parameter parameter(int i) { return parameters.get(i); }

    /** retrieve index of a given parameter */
    public int indexOfParameter(Parameter p) { return parameters.indexOf(p); }

    /** retrieve parameter iterator */
    public Iterator<Parameter> parameterIterator()
    {
	return parameters.iterator();
    }
    
    /**check wether this container contains a given parameter */
    public boolean containsParameter(Parameter p)
    {
	return parameters.contains(p);
    }

    /** remove untracked parameter */
    public boolean removeUntrackedParameter(Parameter p)
    {
	if (isRemovable(p)) {
	    parameters.remove(p);
	    setHasChanged();
	    return true;
	}
	return false;
    }
    
    /** get parameter by name */
    public Parameter parameter(String name)
    {
	Iterator<Parameter> itP = parameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
	    if (p.name().equals(name)) return p;
	}
	System.err.println("ERROR: ParameterContainer nas no parameter '"+
			   name+"'!");
	return null;
    }
    
    /** get parameter by name AND type */
    public Parameter parameter(String name,String type)
    {
	Iterator<Parameter> itP = parameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
	    if (p.name().equals(name)&&p.type().equals(type)) return p;
	}
	return null;
    }

    /** recursively retrieve parameters to all levels */
    public Iterator<Parameter> recursiveParameterIterator()
    {
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	Parameter.getParameters(parameterIterator(),params);
	return params.iterator();
    }
    
    /** find  parameter (recursively) with specified name */
    public Parameter findParameter(String name)
    {
	Iterator<Parameter> itP = recursiveParameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
	    String fullParamName = p.fullName();
	    if (fullParamName.equals(name)) return p;
	}
	return null;
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
    

    /** get all parameter (recursively) with specified strict name *and* type */
    public Parameter findParameter(String name,String type)
    {
	if (type==null) return findParameter(name);

	Iterator<Parameter> itP = recursiveParameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
	    String fullParamName = p.fullName();
	    if ((fullParamName.equals(name))&&
		p.type().equals(type)) return p;
	}
	return null;
    }
    
    /** get all parameters (recursively) with specified name *and* type */
    public Parameter[] findParameters(String name,String type)
    {
	if (type==null) return findParameters(name);
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
    
    /** get all parameters (recursively) with specified name *and* type */
    public Parameter[] findParameters(String name,String type,String value)
    {
	if (type==null&&value==null) return findParameters(name);
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	Iterator<Parameter> itP = recursiveParameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
		String paramType = p.type();
	    String fullParamName = p.fullName();
	    String paramValue = p.valueAsString();

		boolean typeMatch=(type==null) ? true : paramType.equals(type);
		boolean nameMatch=(name==null) ? true :
			((fullParamName.equals(name))||
			 (!fullParamName.equals(name)&&
			  fullParamName.endsWith("::"+name)));
		boolean valueMatch =
		    (value==null) ? true : paramValue.equals(value);
		if (typeMatch&&nameMatch&&valueMatch) params.add(p);
	}
	return params.toArray(new Parameter[params.size()]);
    }

        /** update a parameter when the value is changed */
    public boolean updateParameter(int index,String valueAsString)
    {
	Parameter p = parameter(index);
	String  oldValueAsString = p.valueAsString();
	if (valueAsString.equals(oldValueAsString)) return true;
	
	String  defaultAsString = parameterDefaultValueAsString(p);
	if (p.setValue(valueAsString,defaultAsString)) {
	    setHasChanged();
	    return true;
	}
	return false;
    }
    
    /** update a parameter when the value is changed */
    public boolean updateParameter(String name,String type,String valueAsString)
    {
	Parameter param = findParameter(name,type);
	
	// change the value of an existing parameter (top-level or not)
	if (param!=null) {
	    
	    // handle top-level parameter
	    int index = indexOfParameter(param);
	    if (index>=0) return updateParameter(index,valueAsString);
	    
	    // handle parameter within a top-level [V]PSet
	    String a[] = param.fullName().split("::");
	    if (a.length>1) {
		String b[] = a[0].split("\\[");
		Parameter parentParam = parameter(b[0]);
		if (parentParam==null) return false;
		if (isRemovable(parentParam)) {
		    param.setValue(valueAsString,"");
		    setHasChanged();
		    return true;
		}
		else {
		    param.setValue(valueAsString,"");
		    String defaultAsString =
			parameterDefaultValueAsString(parentParam);
		    if (parentParam.setValue(parentParam.valueAsString(),
					     defaultAsString)) {
			setHasChanged();
			return true;
		    }
		    return false;
		}
	    }
	}
	// add an untracked parameter to the top-level
	else {
	    Parameter parameterNew =
		ParameterFactory.create(type,name,valueAsString,false,false);
	    System.out.println("Adding an untracked parameter without "+
			       "any default value: " + parameterNew);
	    parameters.add(parameterNew);
	    parameterNew.setParent(this);
	    setHasChanged();
	    return true;
	}
	System.err.println("Instance.updateParameter ERROR! "+
			   "No parameter '"+name+"' of type '"+type+"' found.");
	return false;
    }
    

    /** number of unset tracked parameters */
    public int unsetTrackedParameterCount()
    {
	int result = 0;
	Iterator<Parameter> itP = parameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
	    if (p instanceof VPSetParameter) {
		VPSetParameter vpset = (VPSetParameter)p;
		result += vpset.unsetTrackedParameterCount();
	    }
	    else if (p instanceof PSetParameter) {
		PSetParameter pset = (PSetParameter)p;
		result += pset.unsetTrackedParameterCount();
	    }
	    else {
		if (p.isTracked()&&!p.isValueSet()) result++;
	    }
	}
	return result;
    }

}
package confdb.data;

import java.util.ArrayList;


/**
 * PSetParameter
 * -------------
 * @author Philipp Schieferdecker
 *
 * Model CMSSW's ParameterSet.
 */
public class PSetParameter extends Parameter
{
    //
    // data members
    //
    
    /** parameter type string */
    private static final String type = "PSet";

    /** parameters */
    private ArrayList<Parameter> parameters = new ArrayList<Parameter>();


    //
    // construction
    //
    
    /** standard constructor */
    public PSetParameter(String name,ArrayList<Parameter> parameters,
			 boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	for (Parameter p : parameters) this.parameters.add(p.clone(this));
	isValueSet = (parameters.size()>0);
    }
    
    /** constructor from a string */
    public PSetParameter(String name,String valueAsString,
			 boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	setValue(valueAsString);
    }
    

    //
    // member functions
    //

    /** make a clone of the parameter */
    public Parameter clone(Object parent)
    {
	PSetParameter result = new PSetParameter(name,parameters,isTracked,isDefault);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the values of the parameter set as a string */
    public String valueAsString()
    {
	String result = new String();
	if (isValueSet) {
	    for (Parameter p : parameters) {
		String paramIsDef = Boolean.toString(p.isDefault());
		String paramIsTrk = Boolean.toString(p.isTracked());
		String parameterString =
		    p.name()           + ":" +
		    p.type()           + ":" +
		    p.valueAsString()  + ":" +
		    paramIsDef         + ":" +
		    paramIsTrk;
		result += parameterString + "#";
	    }
	    result = result.substring(0,result.length()-1);
	}
	return result;
    }
    
    /** set parameter values from string */
    public boolean setValue(String valueAsString)
    {
	parameters.clear();
	if (valueAsString.length()==0) {
	    isValueSet = false;
	}
	else {
	    String[] strValues = valueAsString.split("#");
	    for (int i=0;i<strValues.length;i++) {
		String[] strParam = strValues[i].split(":");
		if (strParam.length!=5) return false;
		String    paramName   = strParam[0];
		String    paramType   = strParam[1];
		String    paramValue  = strParam[2];
		Boolean   paramIsDef  = new Boolean(strParam[3]);
		Boolean   paramIsTrkd = new Boolean(strParam[4]);
		Parameter param       = ParameterFactory.create(paramType,
								paramName,
								paramValue,
								paramIsTrkd,
								paramIsDef);
		parameters.add(param);
	    }
	    isValueSet = true;
	}
	return true;
    }
    
    /** a parameter set is default if all of its children are */
    public boolean isDefault()
    {
	for (int i=0;i<parameterCount();i++) {
	    Parameter p = (Parameter)parameter(i);
	    if (!p.isDefault()) return false;
	}
	return true;
    }

    /** number of parameters in parameter-set */
    public int parameterCount() { return parameters.size(); }

    /** retrieve the i-th parameter in the set */
    public Parameter parameter(int i) { return parameters.get(i); }

    /** index of a certain parameter */
    public int indexOfParameter(Parameter p) { return parameters.indexOf(p); }

    /** set i-th parameter of the pset */
    public boolean setParameterValue(int i,String valueAsString)
    {
	Parameter p = parameter(i);
	if (!p.setValue(valueAsString)) return false;
	this.isDefault = isDefault;
	return true;
    }
    
    /** add a parameter */
    public boolean addParameter(Parameter p)
    {
	if (p instanceof PSetParameter||p instanceof VPSetParameter) return false;
	parameters.add(p);
	isValueSet = true;
	return true;
    }

}

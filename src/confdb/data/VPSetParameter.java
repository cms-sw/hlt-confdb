package confdb.data;

import java.util.ArrayList;


/**
 * VPSetParameter
 * --------------
 * 
 * Model CMSSW's vector<ParameterSet>.
 */
public class VPSetParameter extends Parameter
{
    //
    // data members
    //
    
    /** parameter type string */
    private static final String type = "VPSet";

    /** parameter sets */
    private ArrayList<PSetParameter> parameterSets = new ArrayList<PSetParameter>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public VPSetParameter(String name,ArrayList<PSetParameter> parameterSets,
			  boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	for (PSetParameter p : parameterSets)
	    this.parameterSets.add((PSetParameter)p.clone(this));
	isValueSet = (parameterSets.size()>0);
    }
    
    /** constructor from a string */
    public VPSetParameter(String name,String valueAsString,
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
	VPSetParameter result = new VPSetParameter(name,parameterSets,isTracked,isDefault);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the values of the vpset as a string */
    public String valueAsString()
    {
	String result = new String();
	if (isValueSet) {
	    for (PSetParameter p : parameterSets) {
		String paramIsDef = Boolean.toString(p.isDefault());
		String paramIsTrk = Boolean.toString(p.isTracked());
		String parameterString =
		    p.name()           + ";" +
		    p.type()           + ";" +
		    p.valueAsString()  + ";" +
		    paramIsDef         + ";" +
		    paramIsTrk;
		result += parameterString + "|";
	    }
	    result = result.substring(0,result.length()-1);
	}
	return result;
    }
    
    /** set parameter values from string */
    public boolean setValue(String valueAsString)
    {
	parameterSets.clear();
	if (valueAsString.length()==0) {
	    isValueSet = false;
	}
	else {
	    String[] strValues = valueAsString.split("|");
	    for (int i=0;i<strValues.length;i++) {
		String[] strParam = strValues[i].split(";");
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
		parameterSets.add((PSetParameter)param);
	    }
	    isValueSet = true;
	}
	return true;
    }

    /** a vecotr<pset> is default if all of its children are */
    public boolean isDefault()
    {
	for (int i=0;i<parameterSetCount();i++) {
	    PSetParameter p = parameterSet(i);
	    if (!p.isDefault()) return false;
	}
	return true;
    }
    
    /** number of parameter set entries */
    public int parameterSetCount() { return parameterSets.size(); }

    /** retrieve the i-th parameter set */
    public PSetParameter parameterSet(int i) { return parameterSets.get(i); }

    /** index of a certain parameter set */
    public int indexOfParameterSet(PSetParameter pset) { return parameterSets.indexOf(pset); }

    /** set i-th parameter set value  */
    public boolean setParameterSetValue(int i,String valueAsString)
    {
	PSetParameter p = parameterSet(i);
	if (!p.setValue(valueAsString)) return false;
	this.isDefault = isDefault;
	return true;
    }
    
    /** add a parameter-set */
    public void addParameterSet(PSetParameter pset)
    {
	parameterSets.add(pset);
	isValueSet = true;
    }
    
}

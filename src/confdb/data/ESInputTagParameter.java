package confdb.data;

/**
 * ESInputTagParameter
 * ---------------
 * @author 
 *
 * parameter base class for scalar parameters of type ESInputTag.
 */
public class ESInputTagParameter extends ScalarParameter
{
    //
    // member data
    //
    
    /** parameter type string */
    private static final String type = "ESInputTag";
    
    /** first value: data */
    private String data = null;
    
    /** second value: module */
    private String module = null;
    
    
    
    //
    // construction
    //

    /** standard constructor */
    public ESInputTagParameter(String name,
			     String data,String module,
			     boolean isTracked)
    {
	super(name,isTracked);
	isValueSet = (data!=null&&module!=null);
	if (isValueSet) {
	    this.data    = new String(data);
	    this.module = new String(module);
	}
    }
    
    /** constructor from string */
    public ESInputTagParameter(String name,String valueAsString,
			     boolean isTracked)
    {
	super(name,isTracked);
	setValue(valueAsString);
    }
    
    //
    // member functions
    //
    
    /** make a clone of the parameter */
    public Parameter clone(Object parent)
    {
	ESInputTagParameter result =
	    new ESInputTagParameter(name,data,module,isTracked);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the value of the parameter */
    public Object value()
    {
	return (isValueSet) ? valueAsString() : null;
    }
    
    /** get data */
    public String data() { return data; }

    /** get module */
    public String module() { return module; }

    
    /** retrieve the value of the parameter as a string */
    public String valueAsString()
    {
	if (isValueSet) {
	    String result = data;
	    if (module.length()>0)
		result += ":" + module;
	    if (result.equals(new String())) result = "\"\"";
	    return result;
	}
	return new String();
    }

    /** set the value  the parameter */
    public boolean setValue(String valueAsString)
    {
	if (valueAsString==null||valueAsString.length()==0) {
	    isValueSet = false;
	    data      = null;
	    module   = null;
	}
	else {
	    if ((valueAsString.startsWith("'") &&valueAsString.endsWith("'"))||
		(valueAsString.startsWith("\"")&&valueAsString.endsWith("\"")))
		valueAsString=valueAsString.substring(1,valueAsString.length()-1);
	    
	    String[] strValues = valueAsString.split(":");
	    if (strValues.length==0||strValues.length>3) return false;
	    data = strValues[0];
	    if (strValues.length>1) module = strValues[1];
	    else module = "";
	    isValueSet = true;
	}
	return true;
    }

    /** set data */
    public void setData(String data) { this.data = data; }
    
    /** set data */
    public void setModule(String module) { this.module = module; }


}

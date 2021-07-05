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
    
    /** first value: module label */
    private String module = null;
    
    /** second value: data label */
    private String data = null;
    
    
    
    //
    // construction
    //

    /** standard constructor */
    public ESInputTagParameter(String name,
			       String module, String data,
			       boolean isTracked)
    {
	super(name,isTracked);
	isValueSet = (module!=null&&data!=null);
	if (isValueSet) {
	    this.module = new String(module);
	    this.data   = new String(data);
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
	    new ESInputTagParameter(name,module,data,isTracked);
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
    
    /** get module */
    public String module() { return module; }

    /** get data */
    public String data() { return data; }
    
    /** retrieve the value of the parameter as a string */
    /** note: an ESInputTag always needs a : if it is not empty 
     *  therefore it returns module:, :data, module:data or "" */
    public String valueAsString()
    {
	if (isValueSet) {
	    String result = module + ":"+data;
	    if (result.equals(":")) result = "\"\"";
	    return result;
	}
	return new String();
    }

    /** set the value  the parameter */
    /*  note the input from parsing an empty ESInputTag is :
     *  therefore we check for this via a split length of 0 
     *  and a non zero string length */
    public boolean setValue(String valueAsString)
    {
	if (valueAsString==null||valueAsString.length()==0) {
	    isValueSet = false;
	    module     = null;
	    data       = null;
	}
	else {
	    if ((valueAsString.startsWith("'") &&valueAsString.endsWith("'"))||
		(valueAsString.startsWith("\"")&&valueAsString.endsWith("\"")))
		valueAsString=valueAsString.substring(1,valueAsString.length()-1);
     
	    String[] strValues = valueAsString.split(":");
	    if(strValues.length>2) return false;
	    module = strValues.length==0 ? "" : strValues[0];
	    if (strValues.length>1) data = strValues[1];
	    else data = "";
	    isValueSet = true;
	}
	return true;
    }

    /** set data */
    public void setModule(String module) { this.module = module; }

    /** set data */
    public void setData(String data) { this.data = data; }

    /** unresolved ESInputTags */
    public int unresolvedESInputTagCount(IConfiguration config)
    {
	if ((config.essource(module)==null)&&(config.esmodule(module)==null)) {
	    return 1;
	} else {
	    return 0;
	}
    }
}

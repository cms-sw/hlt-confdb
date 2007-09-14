package confdb.data;

/**
 * FileInPathParameter
 * -------------------
 * @author Philipp Schieferdecker
 *
 * parameter base class for scalar parameters of type FileInPath.
 */
public class FileInPathParameter extends ScalarParameter
{
    //
    // member data
    //

    /** parameter type FileInPath */
    private static final String type = "FileInPath";
    
    /** parameter values */
    private String value = null;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public FileInPathParameter(String name,String value,
			       boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	if (value!=null&&value.length()==0) value="''";
	setValue(value);
    }
    
    
    //
    // member functions
    //
    
    /** make a clone of the parameter */
    public Parameter clone(Object parent)
    {
	FileInPathParameter result =
	    new FileInPathParameter(name,value,isTracked,isDefault);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the value of the parameter */
    public Object value() { return value; }
    
    /** retrieve the value of the parameter as a string */
    public String valueAsString()
    {
	return (isValueSet) ? "\""+value.toString()+"\"" : new String();
    }
    
    /** set the value  the parameter, indicate if default */
    public boolean setValue(String valueAsString)
    {
	if (valueAsString==null||valueAsString.length()==0) {
	    isValueSet = false;
	    value      = null;
	}
	else {
	    if ((valueAsString.startsWith("'") &&valueAsString.endsWith("'"))||
		(valueAsString.startsWith("\"")&&valueAsString.endsWith("\"")))
		value = valueAsString.substring(1,valueAsString.length()-1);
	    else 
		value = new String(valueAsString);
	    isValueSet = true;
	}
	return true;
    }
    
}

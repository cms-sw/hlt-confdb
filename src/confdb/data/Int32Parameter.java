package confdb.data;

/**
 * Int32Parameter
 * --------------
 * @author Philipp Schieferdecker
 *
 * parameter base class for scalar parameters of type int32.
 */
public class Int32Parameter extends ScalarParameter
{
    //
    // member data
    //

    /** parameter type string */
    private static final String type = "int32";
    
    /** parameter values */
    private Integer value = null;
         
    
    //
    // construction
    //

    /** standard constructor */
    public Int32Parameter(String name,Integer value,
			  boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	isValueSet = (value!=null);
	if (isValueSet) this.value = new Integer(value.intValue());
    }
    
    /** constructor from string */
    public Int32Parameter(String name,String valueAsString,
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
	Int32Parameter result = new Int32Parameter(name,value,isTracked,isDefault);
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
	return (isValueSet) ? value.toString() : new String();
    }
    
    /** set the value  the parameter, indicate if default */
    public boolean setValue(String valueAsString)
    {
	if (valueAsString==null||valueAsString.length()==0) {
	    isValueSet = false;
	    value      = null;
	}
	else {
	    try {
		this.value = new Integer(valueAsString);
		isValueSet = true;
	    }
	    catch (NumberFormatException e) {
		System.err.println("Int32Parameter.setValue "+
				   "NumberFormatException: "+
				   e.getMessage());
		return false;
	    }
	}
	return true;
    }
    
}

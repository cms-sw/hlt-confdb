package confdb.data;

/**
 * BoolParameter
 * -------------
 * @author Philipp Schieferdecker
 *
 * parameter base class for scalar parameters of type bool.
 */
public class BoolParameter extends ScalarParameter
{
    //
    // member data
    //

    /** parameter type string */
    private static final String type = "bool";
    
    /** parameter values */
    private Boolean value = null;
         
    
    //
    // construction
    //

    /** standard constructor */
    public BoolParameter(String name,Boolean value,
			 boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	isValueSet = (value!=null);
	if (isValueSet) this.value = new Boolean(value.booleanValue());
    }
    
    /** constructor from string */
    public BoolParameter(String name,String valueAsString,
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
	BoolParameter result = new BoolParameter(name,value,isTracked,isDefault);
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
	//if (valueAsString.length()==0) {
	if (valueAsString==null||valueAsString.length()==0) {
	    isValueSet = false;
	    value      = null;
	}
	else {
	    try {
		this.value = new Boolean(valueAsString);
		isValueSet = true;
	    }
	    catch (NumberFormatException e) {
		System.out.println("BoolParameter.setValue NumberFormatException: "+
				   e.getMessage());
		return false;
	    }
	}
	return true;
    }
    
}

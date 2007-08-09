package confdb.data;

/**
 * DoubleParameter
 * ---------------
 * @author Philipp Schieferdecker
 *
 * parameter base class for scalar parameters of type double.
 */
public class DoubleParameter extends ScalarParameter
{
    //
    // member data
    //
    
    /** parameter type string */
    private static final String type = "double";
    
    /** parameter values */
    private Double value = null;
         
    
    //
    // construction
    //

    /** standard constructor */
    public DoubleParameter(String name,Double value,
			   boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	isValueSet = (value!=null);
	if (isValueSet) this.value = new Double(value.doubleValue());
    }
    
    /** constructor from string */
    public DoubleParameter(String name,String valueAsString,
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
	DoubleParameter result = new DoubleParameter(name,value,isTracked,isDefault);
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
		this.value = new Double(valueAsString);
		isValueSet = true;
	    }
	    catch (NumberFormatException e) {
		System.out.println("DoubleParameter.setValue " +
				   "NumberFormatException: "+
				   e.getMessage());
		return false;
	    }
	}
	return true;
    }
    
}

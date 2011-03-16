package confdb.data;

import java.math.BigInteger;

/**
 * UInt64Parameter
 * ---------------
 * @author Philipp Schieferdecker
 *
 * parameter base class for scalar parameters of type uint64.
 *
 * java's long is signed, so need to use BigInteger
 *
 */
public class UInt64Parameter extends ScalarParameter
{
    //
    // member data
    //

    /** parameter type string */
    private static final String type = "uint64";
    
    /** parameter values */
    private BigInteger value = null;
         
    /** flag to indicate that this integer is given in hex format */
    private boolean isHex = false;
    
    
    //
    // construction
    //

    /** standard constructor */
    public UInt64Parameter(String name,Long value,boolean isTracked)
    {
	super(name,isTracked);
	isValueSet = (value!=null);
	if (isValueSet)	this.value = new BigInteger(Long.toHexString(value),16);
    }
    public UInt64Parameter(String name,BigInteger value,boolean isTracked)
    {
	super(name,isTracked);
	isValueSet = (value!=null);
	if (isValueSet)	this.value = value.abs().mod((BigInteger.ONE.add(BigInteger.ONE)).pow(64));;
    }
    
    /** constructor from string */
    public UInt64Parameter(String name,String valueAsString,boolean isTracked)
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
	UInt64Parameter result = new UInt64Parameter(name,value,isTracked);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the value of the parameter */
    public Object value() { return value; }

    /** hex format? */
    public boolean isHex() { return isHex; }
    
    /** retrieve the value of the parameter as a string */
    public String valueAsString()
    {
	if (!isValueSet) return new String();
	return (isHex) ? "0x"+value.toString(16) : value.toString();
    }

    /** set the value  the parameter, indicate if default */
    public boolean setValue(String valueAsString)
    {
	isValueSet = false;
	value      = null;
	
	if (valueAsString==null) return true;
	valueAsString=valueAsString.replace(" ","");
	if (valueAsString.length()==0) return true;

	if (valueAsString.startsWith("+"))
	    valueAsString = valueAsString.substring(1);
	if (valueAsString.startsWith("-"))
	    valueAsString = valueAsString.substring(1);
	    
	isHex = false;
	if (valueAsString.startsWith("0x")) {
	    isHex = true;
	    valueAsString = valueAsString.substring(2);
	}

	try {
	    this.value = (isHex) ?
		new BigInteger(valueAsString,16) :
		new BigInteger(valueAsString);
	    isValueSet = true;
	}
	catch (NumberFormatException e) {
	    System.err.println("UInt64Parameter.setValue " +
			       "NumberFormatException: "+
			       e.getMessage());
	    return false;
	}

	value=value.abs().mod((BigInteger.ONE.add(BigInteger.ONE)).pow(64));

	return true;
    }

}

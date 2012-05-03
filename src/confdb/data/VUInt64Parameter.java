package confdb.data;

import java.math.BigInteger;
import java.util.ArrayList;


/**
 * VUInt64Parameter
 * ----------------
 * @author Philipp Schieferdecker
 *
 * for parameters of type vector<uint64>.
 *
 * java's long is signed, so need to use BigInteger
 *
 */
public class VUInt64Parameter extends VectorParameter
{
    //
    // data members
    //

    /** parameter type string */
    private static final String type = "vuint64";

    /** parameter values */
    private ArrayList<BigInteger> values = new ArrayList<BigInteger>();
    
    /** inidcate if values are in hex format */
    private ArrayList<Boolean> isHex  = new ArrayList<Boolean>();
    
    
    //
    // construction
    //
    
    /** standard constructor */

    public VUInt64Parameter(String name,ArrayList<Long> values,
			    boolean isTracked)
    {
	super(name,isTracked);
	for (Long i : values) {
	    this.values.add(new BigInteger(Long.toHexString(i),16));
	    this.isHex.add(new Boolean(false));
	}
    }

    /* need to avoid "have the same erasure"
    public VUInt64Parameter(String name,ArrayList<BigInteger> values,
			    boolean isTracked)
    {
	super(name,isTracked);
	for (BigInteger i : values) {
	    this.values.add(i.abs().mod((BigInteger.ONE.add(BigInteger.ONE)).pow(64)));
	    this.isHex.add(new Boolean(false));
	}
    }
    */

    /** constructor from a string */
    public VUInt64Parameter(String name,String valuesAsString,boolean isTracked)
    {
	super(name,isTracked);
	setValue(valuesAsString);
    }

    
    //
    // member functions
    //
    
    /** make a clone of the parameter */
    public Parameter clone(Object parent)
    {
	ArrayList<Long> temp = new ArrayList<Long>();
	for (int i=0;i<values.size();i++) {
	    temp.add(values.get(i).longValue());
	}

	VUInt64Parameter result = new VUInt64Parameter(name,temp,isTracked);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** hex format? */
    public boolean isHex(int i) { return isHex.get(i); }
    
    /** retrieve the values of the parameter as a string */
    public String valueAsString()
    {
	String result = new String();
	for (int i=0;i<values.size();i++) {
	    result += (isHex.get(i)) ?
		"0x"+values.get(i).toString(16) :
		values.get(i).toString();
	    result += ", ";
	}
	if (values.size()>0) result = result.substring(0,result.length()-2);
	return result;
    }

    /** set the parameter values from string */
    public boolean setValue(String valueAsString)
    {
	values.clear();
	isHex.clear();

	if (valueAsString==null) return true;
	valueAsString=valueAsString.replace(" ","");
	if (valueAsString.length()==0) return true;

	boolean ishex;
	String[] strValues = valueAsString.split(",");
	for (int i=0;i<strValues.length;i++) {
	    String value = strValues[i].replace(" ","");
	    if (value.startsWith("+")) value = value.substring(1,value.length());
	    if (value.startsWith("-")) value = value.substring(1,value.length());
	    ishex = false;
	    if (value.startsWith("0x")) {
		ishex = true;
		value = value.substring(2);
	    }

	    BigInteger big;
	    try {
		big = (ishex) ?
		    new BigInteger(value,16) :
		    new BigInteger(value);
	    }
	    catch (NumberFormatException e) {
		System.err.println("VUInt64Parameter.setValue " +
				   "NumberFormatException: "+
				   e.getMessage());
		return false;
	    }

	    big = big.abs().mod((BigInteger.ONE.add(BigInteger.ONE)).pow(64));
	    this.values.add(big);
	    this.isHex.add(ishex);
	}
    	return true;
    }

    /** number of values, *if* a vector type (throw exception otherwise!) */
    public int vectorSize() { return values.size(); }

    /** i-th value of a vector type parameter */
    public Object value(int i) { return values.get(i); }

    /** set i-th value of a vector-type parameter */
    public boolean setValue(int i,String valueAsString)
    {
	if (valueAsString==null) return true;
	valueAsString=valueAsString.replace(" ","");
	if (valueAsString.length()==0) return true;

	if (valueAsString.startsWith("+"))
	    valueAsString = valueAsString.substring(1);
	if (valueAsString.startsWith("-"))
	    valueAsString = valueAsString.substring(1);

	boolean ishex=false;
	if (valueAsString.startsWith("0x")) {
	    ishex = true;
	    valueAsString = valueAsString.substring(2);
	}

	BigInteger big;
	try {
	    big = (ishex) ?
		new BigInteger(valueAsString,16) :
		new BigInteger(valueAsString);
	}
	catch (NumberFormatException e) {
	    System.err.println("VUInt64Parameter.setValue " +
			       "NumberFormatException: "+
			       e.getMessage());
	    return false;
	}

	big = big.abs().mod((BigInteger.ONE.add(BigInteger.ONE)).pow(64));
	values.set(i,big);
	isHex.set(i,ishex);

	return true;
    }

    /** remove i-th value from vector type parameter */
    public Object removeValue(int i)
    {
	Object result = values.remove(i);
	return result;
    }
 
}

package confdb.data;

import java.util.ArrayList;


/**
 * VInt32Parameter
 * ---------------
 * @author Philipp Schieferdecker
 *
 * for parameters of type vector<int32>.
 */
public class VInt32Parameter extends VectorParameter
{
    //
    // data members
    //

    /** parameter type string */
    private static final String type = "vint32";
    
    /** parameter values */
    private ArrayList<Integer> values = new ArrayList<Integer>();

    
    //
    // construction
    //
    
    /** standard constructor */
    public VInt32Parameter(String name,ArrayList<Integer> values,
			   boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	for (Integer i : values) this.values.add(new Integer(i));
	isValueSet = (values.size()>0);
    }
    
    /** constructor from a string */
    public VInt32Parameter(String name,String valuesAsString,
			   boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	setValue(valuesAsString);
    }

    //
    // member functions
    //
    
    /** make a clone of the parameter */
    public Parameter clone(Object parent)
    {
	VInt32Parameter result = new VInt32Parameter(name,values,
						     isTracked,isDefault);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the values of the parameter as a string */
    public String valueAsString()
    {
	String result = new String();
	if (isValueSet) {
	    for (Integer v : values) result += v.toString() + ", ";
	    result = result.substring(0,result.length()-2);
	}
	return result;
    }

    /** set the parameter values from string */
    public boolean setValue(String valueAsString)
    {
	values.clear();
	if (valueAsString==null||valueAsString.length()==0) {
	    isValueSet = false;
	}
	else {
	    try {
		String[] strValues = valueAsString.split(",");
		for (int i=0;i<strValues.length;i++) {
		    String s = strValues[i];
		    while (s.startsWith(" ")) s = s.substring(1,s.length());
		    while (s.endsWith(" ")) s = s.substring(0,s.length()-1);
		    this.values.add(new Integer(s));
		}
		isValueSet = true;
	    }
	    catch (NumberFormatException e) {
		System.err.println("VInt32Parameter.setValue " +
				   "NumberFormatException: "+
				   e.getMessage());
		return false;
	    }
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
	try {
	    values.set(i,new Integer(valueAsString));
	}
	catch (NumberFormatException e) {
	    System.err.println(e.getMessage());
	    return false;
	}
	return true;
    }
    
}

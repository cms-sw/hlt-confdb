package confdb.data;

import java.util.ArrayList;


/**
 * VUInt32Parameter
 * ----------------
 * @author Philipp Schieferdecker
 *
 * for parameters of type vector<uint32>.
 */
public class VUInt32Parameter extends VectorParameter
{
    //
    // data members
    //

    /** parameter type string */
    private static final String type = "vuint32";

    /** parameter values */
    private ArrayList<Integer> values = new ArrayList<Integer>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public VUInt32Parameter(String name,ArrayList<Integer> values,
			    boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	for (Integer i : values) this.values.add(new Integer(i));
	isValueSet = (values.size()>0);
    }
    
    /** constructor from a string */
    public VUInt32Parameter(String name,String valuesAsString,
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
	VUInt32Parameter result = new VUInt32Parameter(name,values,
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
		    values.add(new Integer(s));
		}
		isValueSet = true;
	    }
	    catch (NumberFormatException e) {
		System.out.println(e.getMessage());
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
	    System.out.println(e.getMessage());
	    return false;
	}
	return true;
    }
 
}

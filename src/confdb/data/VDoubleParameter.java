package confdb.data;

import java.util.ArrayList;


/**
 * VDoubleParameter
 * ----------------
 * @author Philipp Schieferdecker
 *
 * for parameters of type vector<double>.
 */
public class VDoubleParameter extends VectorParameter
{
    //
    // data members
    //
    
    /** parameter type string */
    private static final String type = "vdouble";
    
    /** parameter values */
    private ArrayList<Double> values = new ArrayList<Double>();

    
    //
    // construction
    //
    
    /** standard constructor */
    public VDoubleParameter(String name,ArrayList<Double> values,
			    boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	for (Double d : values) this.values.add(new Double(d));
	isValueSet = (values.size()>0);
    }
    
    /** constructor from a string */
    public VDoubleParameter(String name,String valuesAsString,
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
	VDoubleParameter result = new VDoubleParameter(name,values,
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
	    for (Double v : values) result += v.toString() + ", ";
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
		    values.add(new Double(s));
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
    
    /** number of vector entries  */
    public int vectorSize() { return values.size(); }

    /** i-th value of a vector type parameter */
    public Object value(int i) { return values.get(i); }

    /** set i-th value of a vector-type parameter */
    public boolean setValue(int i,String valueAsString)
    {
	try {
	    values.set(i,new Double(valueAsString));
	}
	catch (NumberFormatException e) {
	    System.out.println(e.getMessage());
	    return false;
	}
	return true;
    }
    
}

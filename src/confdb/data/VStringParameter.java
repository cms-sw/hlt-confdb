package confdb.data;

import java.util.ArrayList;


/**
 * VStringParameter
 * ----------------
 * @author Philipp Schieferdecker
 *
 * for parameters of type vector<string>.
 */
public class VStringParameter extends VectorParameter
{
    //
    // data members
    //

    /** parameter type string */
    private static final String type = "vstring";

    /** parameter values */
    private ArrayList<String> values = new ArrayList<String>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public VStringParameter(String name,ArrayList<String> values,
			    boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	for (String s : values) {
	    if (s!=null) this.values.add(new String(s));
	    else         this.values.add(new String());
	}
	isValueSet = (values.size()>0);
    }
    
    /** constructor from a string */
    public VStringParameter(String name,String valuesAsString,
			    boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	if (valuesAsString!=null&&valuesAsString.length()==0) valuesAsString="''";
	setValue(valuesAsString);
    }
    
    
    //
    // member functions
    //
    
    /** make a clone of the parameter */
    public Parameter clone(Object parent)
    {
	VStringParameter result = new VStringParameter(name,values,
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
	    for (String v : values) result += "\"" + v + "\"" + ", ";
	    result = result.substring(0,result.length()-2);
	}
	return result;
    }
    
    /** set parameter values from string */
    public boolean setValue(String valueAsString)
    {
	values.clear();
	if (valueAsString==null||valueAsString.length()==0) {
	    isValueSet = false;
	}
	else {
	    String[] strValues = valueAsString.split(",");
	    for (int i=0;i<strValues.length;i++) {
		String s = strValues[i];
		while (s.startsWith(" ")) s = s.substring(1,s.length());
		while (s.endsWith(" "))   s = s.substring(0,s.length()-1);
		if ((s.startsWith("'")&&s.endsWith("'"))||
		    (s.startsWith("\"")&&s.endsWith("\"")))
		    s = s.substring(1,s.length()-1);
		values.add(s);
	    }
	    isValueSet = true;
	}
	return true;
    }
    
    /** number of vector entries */
    public int vectorSize() { return values.size(); }

    /** i-th value of a vector type parameter */
    public Object value(int i) { return values.get(i); }

    /** set i-th value of a vector-type parameter */
    public boolean setValue(int i,String valueAsString)
    {
	if ((valueAsString.startsWith("'")&&valueAsString.endsWith("'"))||
	    (valueAsString.startsWith("\"")&&valueAsString.endsWith("\"")))
	    values.set(i,valueAsString.substring(1,valueAsString.length()-1));
	else
	    values.set(i,new String(valueAsString));
	return true;
    }
    
}

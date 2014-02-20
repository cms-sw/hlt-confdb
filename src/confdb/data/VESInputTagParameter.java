package confdb.data;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * VInputTagParameter
 * -----------------
 * @author Philipp Schieferdecker
 *
 * for parameters of type vector<ESInputTag>.
 */
public class VESInputTagParameter extends VectorParameter
{
    //
    // data members
    //

    /** parameter type string */
    private static final String type = "VESInputTag";
    
    /** parameter values */
    private ArrayList<ESInputTag> values = new ArrayList<ESInputTag>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public VESInputTagParameter(String name,ArrayList<String> values,
			      boolean isTracked)
    {
	super(name,isTracked);
	for (String v : values) {
	    try {
		this.values.add(new ESInputTag(v));
	    }
	    catch (DataException e) {
		System.err.println("VESInputTagParameter ctor ERROR: " +
				   e.getMessage());
	    }
	}
    }
    
    /** constructor from a string */
    public VESInputTagParameter(String name,String valuesAsString,
			      boolean isTracked)
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
	ArrayList<String> strValues = new ArrayList<String>();
	for (ESInputTag tag : values) strValues.add(tag.toString());
	VESInputTagParameter result = new VESInputTagParameter(name,strValues,
							   isTracked);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the values of the parameter as a string */
    public String valueAsString()
    {
	String result = new String();
	for (ESInputTag tag : values) result += tag.toString() + ", ";
	if (values.size()>0) result = result.substring(0,result.length()-2);
	return result;
    }

    /** set the parameter values from string */
    public boolean setValue(String valueAsString)
    {
	values.clear();
	if (valueAsString==null||valueAsString.length()==0) {
	    return true;
	}
	else {
	    try {
		String[] strValues = valueAsString.split(",");
		for (int i=0;i<strValues.length;i++) {
		    String s = strValues[i];
		    while (s.startsWith(" ")) s = s.substring(1,s.length());
		    while (s.endsWith(" ")) s = s.substring(0,s.length()-1);
		    values.add(new ESInputTag(s));
		}
	    }
	    catch (DataException e) {
		System.err.println("VESInputTagParameter.setValue ERROR: " +
				   e.getMessage());
		return false;
	    }
	}
    	return true;
    }
    
    /** number of vector entries */
    public int vectorSize() { return values.size(); }

    /** i-th value of a vector type parameter */
    public Object value(int i) { return values.get(i).toString(); }

    public String module(int i) { return values.get(i).module(); }
    public String data(int i) { return values.get(i).data(); }

    /** set i-th value of a vector-type parameter */
    public boolean setValue(int i,String valueAsString)
    {
	try {
	    ESInputTag tag = new ESInputTag(valueAsString);
	    values.set(i,tag);
	}
	catch (DataException e) {
	    System.err.println("VESInputTagParameter.setValue ERROR: " +
			       e.getMessage());
	    return false;
	}
	return true;
    }

    /** remove i-th value from vector type parameter */
    public Object removeValue(int i)
    {
    	Object result = values.remove(i);
	return result;
    }

}


/**
 * ESInputTag
 * -------
 * @author Philipp Schieferdecker
 */
class ESInputTag implements Serializable
{
    //
    // member data
    //
    
    /** module label */
    private String module = null;

    /** data label */
    private String data    = null;



    //
    // construction
    //

    /** standard constructor */
    public ESInputTag(String module,String data)
    {
	this.module = module;
	this.data   = data;
    }
    
    /** constructor from string */
    public ESInputTag(String valueAsString) throws DataException
    {
	if ((valueAsString.startsWith("'") &&valueAsString.endsWith("'"))||
	    (valueAsString.startsWith("\"")&&valueAsString.endsWith("\"")))
	    valueAsString = valueAsString.substring(1,valueAsString.length()-1);
	data    = "";
	module  = "";
	String[] strValues = valueAsString.split(":");
	if (strValues.length>0&&strValues.length<3) {
	    module = strValues[0];
	    if (strValues.length>1) data = strValues[1];
	}
	else throw new DataException("ESInputTag format is " +
				     "<module>[:<data>]");
    }
    

    //
    // member functions
    //

    public String module() {return module;}
    public String data() {return data;}

    /** overload toString() */
    public String toString()
    {
	String result = module;
	if (data.length()>0) {
	    result += ":" + data;
	}
	return result;
    }
    
}

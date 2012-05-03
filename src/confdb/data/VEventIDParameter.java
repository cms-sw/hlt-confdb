package confdb.data;

import java.util.ArrayList;


/**
 * VEventIDParameter
 * -----------------
 * @author Philipp Schieferdecker
 *
 * for parameters of type vector<EventID>.
 */
public class VEventIDParameter extends VectorParameter
{
    //
    // data members
    //

    /** parameter type string */
    private static final String type = "VEventID";
    
    /** parameter values */
    private ArrayList<EventID> values = new ArrayList<EventID>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public VEventIDParameter(String name,ArrayList<String> values,
			     boolean isTracked)
    {
	super(name,isTracked);
	for (String v : values) {
	    try {
		this.values.add(new EventID(v));
	    }
	    catch (DataException e) {
		System.err.println("VEventIDParameter ctor ERROR: " +
				   e.getMessage());
	    }
	}
    }
    
    /** constructor from a string */
    public VEventIDParameter(String name,String valuesAsString,
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
	for (EventID id : values) strValues.add(id.toString());
	VEventIDParameter result = new VEventIDParameter(name,strValues,
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
	for (EventID id : values) result += id.toString() + ", ";
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
		    values.add(new EventID(s));
		}
	    }
	    catch (DataException e) {
		System.err.println("VEventIDParameter.setValue ERROR: "+
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

    /** set i-th value of a vector-type parameter */
    public boolean setValue(int i,String valueAsString)
    {
	try {
	    EventID id = new EventID(valueAsString);
	    values.set(i,id);
	}
	catch (DataException e) {
	    System.err.println("VEventIDParameter.setValue ERROR: "
			       + e.getMessage());
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
   EventID
   -------
   @author Philipp Schieferdecker
*/
class EventID
{
    //
    // member data
    //
    
    /** run number */
    private Integer runNumber = null;

    /** event number */
    private Integer evtNumber = null;

    //
    // construction
    //

    /** standard constructor */
    public EventID(Integer runNumber, Integer evtNumber)
    {
	this.runNumber = runNumber;
	this.evtNumber = evtNumber;
    }
    
    /** constructor from string */
    public EventID(String valueAsString) throws DataException
    {
	String[] strValues = valueAsString.split(":");
	if (strValues.length==2) {
	    try {
		this.runNumber = new Integer(strValues[0]);
		this.evtNumber = new Integer(strValues[1]);
	    }
	    catch (NumberFormatException e) {
		throw new DataException("EventID ctor failed: " + e.getMessage());
	    }
	}
	else throw new DataException("EventID format is <run>:<evt>!");
    }
    

    //
    // member functions
    //

    /** overload toString() */
    public String toString()
    {
	return new String(runNumber.toString() + " : " + evtNumber.toString());
    }
    
}

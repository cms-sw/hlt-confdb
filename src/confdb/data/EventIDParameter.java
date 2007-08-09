package confdb.data;

/**
 * EventIDParameter
 * ---------------
 * @author Philipp Schieferdecker
 *
 * parameter base class for scalar parameters of type EventID.
 */
public class EventIDParameter extends ScalarParameter
{
    //
    // member data
    //
    
    /** parameter type string */
    private static final String type = "EventID";
    
    /** first value: run number */
    private Integer runNumber = null;

    /** second value: event number */
    private Integer evtNumber = null;
    
    
    //
    // construction
    //

    /** standard constructor */
    public EventIDParameter(String name,Integer runNumber,Integer evtNumber,
			    boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	isValueSet = (runNumber!=null&&evtNumber!=null);
	if (isValueSet) {
	    this.runNumber = new Integer(runNumber.intValue());
	    this.evtNumber = new Integer(evtNumber.intValue());
	}
    }
    
    /** constructor from string */
    public EventIDParameter(String name,String valueAsString,
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
	EventIDParameter result =
	    new EventIDParameter(name,runNumber,evtNumber,isTracked,isDefault);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the value of the parameter */
    public Object value()
    {
	return (isValueSet) ? valueAsString() : null;
    }
    
    /** get run number */
    public Integer runNumber() { return runNumber; }

    /** get event number */
    public Integer evtNumber() { return evtNumber; }

    /** retrieve the value of the parameter as a string */
    public String valueAsString()
    {
	if (isValueSet) {
	    String result = runNumber.toString() + ":" + evtNumber.toString();
	    return result;
	}
	return new String();
    }

    /** set the value  the parameter */
    public boolean setValue(String valueAsString)
    {
	if (valueAsString==null||valueAsString.length()==0) {
	    isValueSet = false;
	    runNumber  = null;
	    evtNumber  = null;
	}
	else {
	    String[] strValues = valueAsString.split(":");
	    if (strValues.length!=2) return false;
	    try {
		this.runNumber = new Integer(strValues[0]);
		this.evtNumber = new Integer(strValues[1]);
		isValueSet = true;
	    }
	    catch (NumberFormatException e) {
		System.out.println("EventIDParameter.setValue " +
				   "NumberFormatException: "+
				   e.getMessage());
		return false;
	    }
	}
	return true;
    }

}

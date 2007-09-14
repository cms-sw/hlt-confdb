package confdb.data;

import java.util.ArrayList;


/**
 * VPSetParameter
 * --------------
 * 
 * Model CMSSW's vector<ParameterSet>.
 */
public class VPSetParameter extends Parameter
{
    //
    // data members
    //
    
    /** parameter type string */
    private static final String type = "VPSet";

    /** parameter sets */
    private ArrayList<PSetParameter> parameterSets = new ArrayList<PSetParameter>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public VPSetParameter(String name,ArrayList<PSetParameter> parameterSets,
			  boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
	for (PSetParameter p : parameterSets)
	    this.parameterSets.add((PSetParameter)p.clone(this));
    }
    
    /** constructor from a string */
    public VPSetParameter(String name,String valueAsString,
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
	VPSetParameter result =
	    new VPSetParameter(name,parameterSets,isTracked,isDefault);
	result.setParent(parent);
	return result;
    }
    
    /** type of the parameter as a string */
    public String type() { return type; }
    
    /** retrieve the values of the vpset as a string */
    public String valueAsString()
    {
	String result =
	    "<" + type() +
	    " name="     + name() +
	    " default="  + Boolean.toString(isDefault) +
	    " tracked="  + Boolean.toString(isTracked()) +
	    ">";
	for (PSetParameter pset : parameterSets) result += pset.valueAsString();
	result += "</" + type() + ">";
	return result;
    }
    
    /** set parameter values from string */
    public boolean setValue(String valueAsString)
    {
	parameterSets.clear();
	if (valueAsString.length()>0) {

	    if (!valueAsString.startsWith("<VPSet name="+name()))
		valueAsString=
		    "<VPSet" +
		    " name=" + name() +
		    " default=" + Boolean.toString(isDefault()) +
		    " tracked=" + Boolean.toString(isTracked()) +
		    ">" + valueAsString + "</VPSet>";
	    
	    VParameterSetParser parser = new VParameterSetParser(valueAsString);
	    if (!parser.parseVParameterSet()||
		!parser.vpsetName().equals(name())||
		!parser.vpsetIsTracked()==isTracked()) return false;
	    while (parser.parseNextParameterSet()) {
		PSetParameter pset = new PSetParameter(parser.psetString());
		pset.setParent(this);
		parameterSets.add(pset);
	    }
	}
	return true;
    }
    
    /** a vpset is default if its string-rep matches the template */
    public boolean isDefault()
    {
	if (parent() instanceof Instance) {
	    isDefault = true;
	    Instance instance = (Instance)parent();
	    Template template = instance.template();
	    String defaultAsString = template.parameter(name()).valueAsString();
	    isDefault = defaultAsString.equals(valueAsString());
	}
	else {
	    isDefault = false;
	}
	return isDefault;
    }
    
     /** a vpset is set if all of its children are, or if it is empty (?!) */
    public boolean isValueSet()
    {
	for (PSetParameter pset : parameterSets)
	    if (!pset.isValueSet()) return false;
	//return (parameterSets.size()>0);
	return true;
    }

    /** number of unset tracked parameters in vpset */
    public int unsetTrackedParameterCount()
    {
	int result = 0;
	for (PSetParameter pset : parameterSets) {
	    //if (pset.parameterCount()>0)
	    result += pset.unsetTrackedParameterCount();
	    //else if (pset.isTracked())
	    //result++;
	}
	return result;
    }

    /** number of parameter set entries */
    public int parameterSetCount() { return parameterSets.size(); }

    /** retrieve the i-th pset */
    public PSetParameter parameterSet(int i) { return parameterSets.get(i); }

    /** retrieve pset by name */
    public PSetParameter parameterSet(String name)
    {
	for (PSetParameter pset : parameterSets)
	    if (name.equals(pset.name())) return pset;
	return null;
    }

    /** index of a certain parameter set */
    public int indexOfParameterSet(PSetParameter pset)
    {
	return parameterSets.indexOf(pset);
    }

    /** set i-th parameter set value  */
    public boolean setParameterSetValue(int i,String valueAsString)
    {
	PSetParameter p = parameterSet(i);
	if (!p.setValue(valueAsString)) return false;
	this.isDefault = isDefault;
	return true;
    }
    
    /** add a parameter-set */
    public void addParameterSet(PSetParameter pset)
    {
	if (pset.name().length()>0) {
	    System.err.println("VPSetParameter.addParameterSet ERROR: "+
			       "can't add named PSet to VPSet "+name());
	    return;
	}
	
	pset.setParent(this);
	pset.setTracked(isTracked());
	parameterSets.add(pset);
    }

    /** remove a parameter-set */
    public int removeParameterSet(PSetParameter pset)
    {
	int index = parameterSets.indexOf(pset);
	if (index>=0) parameterSets.remove(index);
	return index;
    }
}



/**
 * VParameterSetParser
 * ------------------
 * @author Philipp Schieferdecker
 *
 * The string representation of a VPSetParameter value needs to be
 * somewhat complex, sort of a xml format. this class helps decode it.
 */
class VParameterSetParser
{
    //
    // member data
    //
    private String  parseString = null;
    private String  psetString  = null;
    private String  name        = null;
    private boolean isDefault   = false;
    private boolean isTracked   = false;

    
    //
    // construction
    //
    
    /** standard constructor */
    public VParameterSetParser(String parseString)
    {
	this.parseString = parseString;
    }
    
    //
    // member functions
    //
    
    /** parse the VPSet properties*/
    public boolean parseVParameterSet()
    {
	String s = parseString;
	if (!s.startsWith("<VPSet")||!s.endsWith("</VPSet>")) return false;
	s = s.substring(7,s.length()-8);

	int    pos    = s.indexOf(">");
	String attStr = s.substring(0,pos);
	
	s = s.substring(pos+1);
	
	String[] atts = attStr.split(" ");
	for (int i=0;i<atts.length;i++) {
	    pos = atts[i].indexOf("=");
	    String attName = atts[i].substring(0,pos);
	    String attVal  = atts[i].substring(pos+1);
	    if      (attName.equals("name"))    name=attVal;
	    else if (attName.equals("default"))	isDefault=Boolean.valueOf(attVal);
	    else if (attName.equals("tracked")) isTracked=Boolean.valueOf(attVal);
	    else return false;
	}
	
	parseString = s;
	
	return true;
    }
    
    /** parse the next parameter set */
    public boolean parseNextParameterSet()
    {
	String s = parseString;
	
	if (!s.startsWith("<PSet")) return false;
	
	String otag = "<PSet";
	String ctag = "</PSet>";
	int    opos = s.indexOf(otag,1);
	int    cpos = s.indexOf(ctag);
	int    skipCount = 0;
	while (opos>=0&&opos<cpos) {
	    opos = s.indexOf(otag,opos+1);
	    //cpos = s.indexOf(ctag,opos+1);
	    cpos = s.indexOf(ctag,cpos+1);
	    skipCount++;
	}
	//for (int i=0;i<skipCount;i++) cpos = s.indexOf(ctag,cpos+1);
	
	psetString  = s.substring(0,cpos+ctag.length());
	parseString = s.substring(cpos+ctag.length());
	
	return true;
    }
    
    
    /** get parsed vectro<parameterset> name */
    public String vpsetName() { return name; }

    /** get last parsed paramter default flag */
    public boolean vpsetIsDefault() { return isDefault; }
    
    /** get last parsed parameter tracked flag */
    public boolean vpsetIsTracked() { return isTracked; }
    
    /** get last parsed pset string value */
    public String psetString() { return psetString; }
    
    
}

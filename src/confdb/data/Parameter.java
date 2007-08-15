package confdb.data;


/**
 * Parameter
 * ---------
 * @author Philipp Schieferdecker
 *
 * a common parameter base class for scalar *and* vector type parameters.
 */
abstract public class Parameter implements Comparable<Parameter>
{
    //
    // member data
    //
    
    /** parent object */
    private  Object   parent = null;

    /** name of the parameter */
    protected String  name = null;
    
    /** flag indicating if this is a default parameter or not */
    protected boolean isDefault = true;
    
    /** flag indicating if the parameter is tracked/untracked */
    protected boolean isTracked = true;
    
    /** flag indicating if the value of this parameter is set */
    protected boolean isValueSet = false;
    
    
    //
    // construction
    //
    
    /** default constructor */
    public Parameter(String name,boolean isTracked,boolean isDefault)
    {
	this.name      = name;
	this.isTracked = isTracked;
	this.isDefault = isDefault;
    }
    
    //
    // abstract interface
    //
    
    /** make a clone of the parameter */
    abstract public Parameter clone(Object parent);
    
    /** type of the parameter as a string */
    abstract public String type();
    
    /** retrieve value(s) as a string */
    abstract public String valueAsString();

    /** set the value the parameter from string */
    abstract public boolean setValue(String valueAsString);

    
    //
    // member functions
    //

    /** overload toString() */
    public String toString() { return name(); }

    /** Comparable: compareTo() */
    public int compareTo(Parameter p) { return toString().compareTo(p.toString()); }

    /** retrieve the parent of the parameter */
    public Object parent() { return parent; }
    
    /** retrieve the name of the parameter */
    public String name() { return name; }

    /** retrieve the flag indicating if the parameter is a default parameter */
    public boolean isDefault() { return isDefault; }
    
    /** retrieve the flag indication ig the parameter is tracked/untracked */
    public boolean isTracked() { return isTracked; }

    /** retrieve the flag indication ig the parameter value is set */
    public boolean isValueSet() { return isValueSet; }

    /** set the parent object */
    public void setParent(Object parent) { this.parent = parent; }

    /** set the value  the parameter from string, provide default as string */
    public boolean setValue(String valueAsString,String defaultAsString)
    {
	if (!setValue(valueAsString)) return false;
	isDefault = false;
	if (this.valueAsString().equals(defaultAsString)) isDefault = true;
	return true;
    }
    
}

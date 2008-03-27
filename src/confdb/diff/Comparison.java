package confdb.diff;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.data.ModuleInstance;
import confdb.data.Path;


/**
 * Comparison
 * ----------
 * @author Philipp Schieferdecker
 *
 * Store information about the differences between two (arbitrary!)
 * components.
 */
public class Comparison
{
    //
    // member data
    //

    /** constants defining the possible results of the comparison */
    public static final int RESULT_IDENTICAL =  0;
    public static final int RESULT_CHANGED   =  1;
    public static final int RESULT_REMOVED   =  2;
    public static final int RESULT_ADDED     =  3;
    
    public static final String[] RESULTS = { "IDENTICAL",
					     "CHANGED",
					     "REMOVED",
					     "ADDED" };
    
    /** reference to source object */
    private Object source = null;
    
    /** parent object */
    private Object parent = null;
    
    /** type of the components being compared */
    private String type;

    /** names of the two components being compared */
    private String name1 = null;
    private String name2 = null;
    
    /** if the comparison regards a parameter which has changed, set old value */
    private String oldValue = null;

    /** if the comparison regards a type change, set old type */
    private String oldType = null;

    /** comparisons of daughter components */
    private ArrayList<Comparison> comparisons = new ArrayList<Comparison>();
    

    //
    // construction
    //

    /** standard constructor */
    public Comparison(Object source,String type,String name1,String name2)
    {
	this.source = source;
	this.type   = type;
	this.name1  = name1;
	this.name2  = name2;
    }
    
    
    //
    // member functions
    //

    /** Object::toString() */
    public String toString()
    {
	String name = (name2==null) ? name1 : name2;
	String result = name;
	if (source instanceof ModuleInstance) result += " ["+type+"]";
	result += " " +resultAsString();
	if (source instanceof Path&&oldType!=null) result += " ["+oldType+"]";
	return result;
    }

    /** get the source object */
    public Object source() { return this.source; }

    /** get the parent object */
    public Object parent() { return this.parent; }
    
    /** type of the components being compared */
    public String type() { return this.type; }

    /** name of the first component */
    public String name1() { return this.name1; }

    /** name of the second component */
    public String name2() { return this.name2; }
    
    /** old value of changed parameter */
    public String oldValue() { return this.oldValue; }
    
    /** old type of changed instance */
    public String oldType() { return this.oldType; }
    
    /** result of the comparison */
    public int result()
    {
	if      (name1==null&&name2!=null) return RESULT_ADDED;
	else if (name1!=null&&name2==null) return RESULT_REMOVED;
	else if (comparisons.size()==0&&oldValue==null&&oldType==null)
	    return RESULT_IDENTICAL;
	else return RESULT_CHANGED;
    }
    
    /** result of comparison as a string */
    public String resultAsString() { return RESULTS[result()]; }

    /** are the two components identical? */
    public boolean isIdentical() { return result()==RESULT_IDENTICAL; }
    
    /** are the two components both present but changed? */
    public boolean isChanged() { return result()==RESULT_CHANGED; }

    /** was the second component added? */
    public boolean isAdded() { return result()==RESULT_ADDED; }

    /** was the first component removed? */
    public boolean isRemoved() { return result()==RESULT_REMOVED; }

    /** number of child comparisons */
    public int comparisonCount() { return comparisons.size(); }

    /** get i-th child comparison */
    public Comparison comparison(int i) { return comparisons.get(i); }

    /** get index of specified comparsion daughter */
    public int indexOfComparison(Comparison c) { return comparisons.indexOf(c); }

    /** retrieve iterator over  child comparisons */
    public Iterator<Comparison> comparisonIterator()
    {
	return comparisons.iterator();
    }

    /** retrieve iterator over comparisons to all levels */
    public Iterator<Comparison> recursiveComparisonIterator()
    {
	ArrayList<Comparison> comps = new ArrayList<Comparison>();
	getComparisons(comparisonIterator(),comps);
	return comps.iterator();
    }

    /** set old value (for parameters which are changed!) */
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    /** set old type (for instances with identical name but new type!) */
    public void setOldType(String oldType) { this.oldType = oldType; }

    /** add a child comparison */
    public void addComparison(Comparison c)
    {
	c.setParent(this); comparisons.add(c);
    }
    
    /** set the parent object of this comparison */
    public void setParent(Object parent) { this.parent = parent; }
    

    //
    // private member functions
    //

    /** retrieve comparisons recursively, given an iterator */
    private void getComparisons(Iterator<Comparison> itComp,
				ArrayList<Comparison> comps)
    {
	while (itComp.hasNext()) {
	    Comparison c = itComp.next();
	    comps.add(c);
	    getComparisons(c.comparisonIterator(),comps);
	}
    }
    
}

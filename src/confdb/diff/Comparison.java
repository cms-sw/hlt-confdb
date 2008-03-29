package confdb.diff;


/**
 * Comparison
 * ----------
 * @author Philipp Schieferdecker
 *
 * abstract base class for comparisons between parameters, instances,
 * paths, sequences, etc.
 */
abstract public class Comparison
{
    /** constants defining the possible results of the comparison */
    public static final int RESULT_IDENTICAL =  0;
    public static final int RESULT_CHANGED   =  1;
    public static final int RESULT_REMOVED   =  2;
    public static final int RESULT_ADDED     =  3;
    
    public static final String[] RESULTS = { "IDENTICAL",
					     "CHANGED",
					     "REMOVED",
					     "ADDED" };
    
    /** parent object of this comparison (for visualization in tree) */
    private Object parent = null;

    /** non-identical comparisons of daughter components */
    private ArrayList<Comparison> comparisons = new ArrayList<Comparison>();
    
    

    /** determine the result of the comparison */
    abstract public int result();
    
    /** plain-text representation of the comparison */
    abstract public String toString();

    /** html representation of the comparison */
    abstract public String toHtml();
    
    

    /** get the parent object */
    public Object parent() { return this.parent; }
    
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

    /** add a child comparison */
    public void addComparison(Comparison c)
    {
	c.setParent(this);
	comparisons.add(c);
    }
    
    /** set the parent object of this comparison */
    public void setParent(Object parent) { this.parent = parent; }


    
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

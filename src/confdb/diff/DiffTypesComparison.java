package confdb.diff;

import confdb.data.Referencable;

/**
 * DiffTypesComparison
 * ------------------
 * @author Sam Harper
 * 
 * compares different types (calling function should ensure they are different)
 * result will always be RESULT_CHANGED 
 *
 */
public class DiffTypesComparison extends Comparison {
	//
	// member data
	//

	/** old instance */
	private Referencable oldReferencable = null;

	/** new instance */
	private Referencable newReferencable = null;

	//
	// construction
	//

	/** standard constructor */
	public DiffTypesComparison(Referencable oldReferencable, Referencable newReferencable) {
		this.oldReferencable = oldReferencable;
		this.newReferencable = newReferencable;
	}

	//
	// member functions
	//

	/** determine the result of the comparison */
	public int result() {
		return RESULT_CHANGED;
	}

	/** plain-text representation of the comparison */
	public String toString() {
        
	    //return oldReferencable.name()+"changed type from "+oldReferencable.getClass()+" to "+newReferencable.getClass();
        return oldReferencable.name() + resultAsString();
        
	}

	/** html representation of the comparison */
	public String toHtml() {
		return "<html>"+oldReferencable.name()+"<b></html>";
	}

}

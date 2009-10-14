package confdb.diff;

import confdb.data.PrimaryDataset;
import confdb.data.ReferenceContainer;
import confdb.data.Path;
import confdb.data.Sequence;


/**
 * DatasetComparison
 * ----------------
 * @author Philipp Schieferdecker
 *
 */
public class DatasetComparison extends Comparison
{
    //
    // member data
    //
    
    /** old dataset */
    private PrimaryDataset oldDataset = null;

    /** new dataset */
    private PrimaryDataset newDataset = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public DatasetComparison(PrimaryDataset oldDataset,
			     PrimaryDataset newDataset)
    {
	this.oldDataset = oldDataset;
	this.newDataset = newDataset;
    }
    
    
    //
    // member functions
    //
    
    /** determine the result of the comparison */
    public int result()
    {
	if      (oldDataset==null&&newDataset!=null) return RESULT_ADDED;
	else if (oldDataset!=null&&newDataset==null) return RESULT_REMOVED;
	else if (comparisonCount()==0&&
		 oldDataset.name().equals(newDataset.name()))
	    return RESULT_IDENTICAL;
	else return RESULT_CHANGED;
    }
    
    /** plain-text representation of the comparison */
    public String toString()
    {
	return (newDataset==null) ?
	    "Dataset "+oldDataset.name()+" "+resultAsString():
	    "Dataset "+newDataset.name()+" "+resultAsString();
    }
    
    /** html representation of the comparison */
    public String toHtml()
    {
	return (newDataset==null) ?
	    "<html>Dataset <b>"+oldDataset.name()+"</b></html>" :
	    "<html>Dataset <b>"+newDataset.name()+"</b></html>";
    }
    
}

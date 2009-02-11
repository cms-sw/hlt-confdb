package confdb.converter.summary.data;

import java.util.Iterator;
import java.util.ArrayList;

import confdb.data.*;

/**
 * PrescaleTableRow
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Organize the prescale data associated to one trigger path.
 */
public class PrescaleTableRow implements ITableRow
{
    //
    // data members
    //

    /** trigger path name */
    private String triggerName;
    
    /** prescale values for each scenario */
    private ArrayList<Long> prescales;

    
    //
    // construction
    //
    
    /** standard constructor */
    public PrescaleTableRow(String triggerName, ArrayList<Long> prescales)
    {
	this.triggerName = triggerName;
	this.prescales   = prescales;
    }
    
    
    //
    // member functions
    //

    /** number of columns for _this_ trigger */
    public int columnCount() { return prescales.size()+1; }

    /** number of lines needed to display this trigger */
    public int lineCount() { return 1; }
    
    /** width needed to display a particular column for this trigger */
    public int columnWidth(int iColumn)
    {
	if (iColumn>=columnCount()) return 0;
	if (iColumn==0) return triggerName.length()+1;
	return prescales.get(iColumn-1).toString().length()+1;
    }

    /** get the data entry to be displayed in particluar column/line */
    public String dataEntry(int iColumn,int iLine)
    {
	if (iColumn==0) return triggerName;
	return prescales.get(iColumn-1).toString();
    }
    
}

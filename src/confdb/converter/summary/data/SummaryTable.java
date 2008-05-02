package confdb.converter.summary.data;

import java.util.Iterator;
import java.util.ArrayList;

import confdb.data.*;

/**
 * SummaryTable
 * ------------
 * @author Philipp Schieferdecker
 *
 * Summary data to be displayed in tables of various formats (ascii,
 * html, latex, etc.)
 */
public class SummaryTable implements ITable
{
    //
    // member data
    //
    
    /** the collection of summary table rows */
    private ArrayList<ITableRow> rows = new ArrayList<ITableRow>();
    
    /** column titles */
    private ArrayList<String> columnTitle = new ArrayList<String>();

    /** column widths */
    private ArrayList<Integer> columnWidth = new ArrayList<Integer>();
    

    //
    // construction
    //

    /** standard constructor */
    public SummaryTable(IConfiguration config)
    {
	columnTitle.add("TRIGGER NAME");
	columnTitle.add("L1 CONDITION");
	columnWidth.add(columnTitle.get(0).length()+1);
	columnWidth.add(columnTitle.get(1).length()+1);
	
	Iterator<Path> itP = config.pathIterator();
	while (itP.hasNext()) {
	    SummaryTableRow row = new SummaryTableRow(itP.next());
	    if (row.isNoTrigger()) continue;
	    for (int iColumn=0;iColumn<row.columnCount();iColumn++) {
		if (iColumn==columnTitle.size()) {
		    columnTitle.add("FILTER"+(iColumn-1));
		    columnWidth.add(columnTitle.get(iColumn).length()+1);
		}
		int width = row.columnWidth(iColumn);
		if (width>columnWidth.get(iColumn)) columnWidth.set(iColumn,width);
	    }
	    rows.add(row);
	}
    }

    
    //
    // member functions
    //
    
    /** number of columns */
    public int columnCount() { return columnTitle.size(); }
    
    /** get the i-th column title */
    public String columnTitle(int iColumn) { return columnTitle.get(iColumn); }

    /** get the width of i-th column */
    public int columnWidth(int iColumn) { return columnWidth.get(iColumn); }
    
    /** get the total table width */
    public int totalWidth()
    {
	int result = columnCount()*2+1;
	Iterator<Integer> itI = columnWidth.iterator();
	while (itI.hasNext()) result += itI.next();
	return result;
    }

    /** number of rows (triggers) in the table */
    public int rowCount() { return rows.size(); }

    /** get an iterator for rows in the table */
    public Iterator<ITableRow> rowIterator() { return rows.iterator(); }
    
}
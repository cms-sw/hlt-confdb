package confdb.converter.summary.data;

import java.util.Iterator;


/**
 * ITable
 * ------
 * @author Philipp Schieferdecker
 *
 */
public interface ITable
{
    /** number of columns */
    public int columnCount();
    
    /** title of i-th column */
    public String columnTitle(int iColumn);

    /** width of i-th column */
    public int columnWidth(int iColumn);

    /** total width of the table */
    public int totalWidth();

    /** number of rows (triggers) */
    public int rowCount();
    
    /** get iterator over table rows */
    public Iterator<ITableRow> rowIterator();
}
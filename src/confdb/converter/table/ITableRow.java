package confdb.converter.table;

/**
 * ITableRow
 * ---------
 * @author Philipp Schieferdecker
 *
 */
public interface ITableRow
{
    /** number of columns of this particular row */
    public int columnCount();

    /** number of lines required to display this row */
    public int lineCount();

    /** data to be displayed for this row, for a particular column/line */
    public String dataEntry(int iColumn,int iLine);
}
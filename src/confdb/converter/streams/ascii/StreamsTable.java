package confdb.converter.streams.ascii;

import java.util.Iterator;
import java.util.ArrayList;

import confdb.converter.table.ITable;
import confdb.converter.table.ITableRow;
import confdb.data.*;

/**
 * StreamsTable
 * ------------
 * @author Ulf Behrens
 * @author Philipp Schieferdecker
 *
 * streams data to be displayed in tables of various formats (ascii,
 * html, latex, etc.)
 */
public class StreamsTable implements ITable
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
    public StreamsTable( IConfiguration config )
    {
    	columnTitle.add("Stream");
    	columnTitle.add("Primary Dataset");
    	columnTitle.add("HLT Path");
    	columnTitle.add("L1 seed");
    	columnWidth.add(columnTitle.get(0).length()+1);
    	columnWidth.add(columnTitle.get(1).length()+1);
    	columnWidth.add(columnTitle.get(2).length()+1);
    	columnWidth.add(columnTitle.get(3).length()+1);
	
    	Iterator<Stream> it = config.streamIterator();
    	while ( it.hasNext() )
    	{
    		Stream stream = it.next();
    		String streamName = stream.label();
        	Iterator<PrimaryDataset> datasets = stream.datasetIterator();
        	while ( datasets.hasNext() )
        	{
        		PrimaryDataset dataset = datasets.next();
        		String datasetName = dataset.label();
        		Iterator<Path> paths = dataset.pathIterator();
        		while ( paths.hasNext() )
        		{
        			Path path = paths.next();
        			StreamsTableRow row = new StreamsTableRow( streamName, datasetName, path );
        			streamName = "";
        			datasetName = "";
            		for ( int iColumn=0; iColumn < row.columnCount(); iColumn++ ) 
            		{
            			int width = row.columnWidth(iColumn);
            			if ( width > columnWidth.get(iColumn) ) 
            				columnWidth.set( iColumn, width );
            		}
            		rows.add(row);
        		}
    		}
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
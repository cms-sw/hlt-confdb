package confdb.converter.summary.html;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.summary.ISummaryWriter;
import confdb.converter.summary.data.ITable;
import confdb.converter.summary.data.ITableRow;
import confdb.converter.summary.data.SummaryTable;
import confdb.data.IConfiguration;

/**
 * HtmlConfigurationWriter
 * ------------------------
 * @author Ulf Behrens
 * @author Philipp Schieferdecker
 * 
 * Display the summary information about each path (trigger) in an
 * html table.
 *
 * FORMAT:
 * ----------------------------------------------------
 * | trigger | L1 condition   | filter1   | filterN   |
 * ----------------------------------------------------
 * | name    | cond1 [OR/AND] | name      | name      |
 * |         | [cond2]        | p1/v1     | p1/v1     |
 * |         |                | [p2/v2]   | [p2/v2]   |
 * ----------------------------------------------------
 */
public class HtmlConfigurationWriter implements ISummaryWriter
{
    //
    // member data
    //
    protected ConverterEngine converterEngine = null;
    
    
    //
    // member functions
    //

    /** generate html summary table representation of the configuration */
    public String toString(IConfiguration conf, WriteProcess writeProcess) throws ConverterException
    {
    	StringBuffer result = new StringBuffer( 2000 );

    	SummaryTable summaryTable = new SummaryTable(conf);
    	//PrescaleTable prescaleTable = new PrescaleTable(conf);
    	result.append( createHtmlTable(summaryTable) );

    	return result.toString();
    }
    
    public void setConverterEngine(ConverterEngine converterEngine) 
    {
    	this.converterEngine = converterEngine;
    }
    
    //
    // private member functions
    //
    
    /** create html table as string */
    private String createHtmlTable(ITable table)
    {
    	if ( table.rowCount()==0 ) 
    		return new String();
	
    	StringBuffer result = new StringBuffer(10000);
	
    	result.append( "<table>" );

    	/*
    	for (int i = 0; i < table.columnCount(); i++ ) 
    	{
    		String title = table.columnTitle(iColumn);
    		int    width = table.columnWidth(iColumn);
    		result.append(colSeparator).append(title).append(fillColumn(title,width));
    	}
    	*/
	
    	Iterator<ITableRow> itR = table.rowIterator();
    	while (itR.hasNext()) 
    	{
			result.append( "<tr>" );
    		ITableRow row = itR.next();
    		for ( int i=0; i < table.columnCount(); i++) 
    		{
    			result.append( "<td>" );
    			for ( int iLine = 0; iLine < row.lineCount(); iLine++ )
    				result.append( row.dataEntry( i, iLine ) ).append( "<br>" );
    			result.append( "</td>" );
    		}
			result.append( "</tr>\n" );
		}
	
    	result.append( "</table>" );
	    return result.toString();
    }
    
}

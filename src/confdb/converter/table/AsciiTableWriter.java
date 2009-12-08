package confdb.converter.table;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.data.IConfiguration;

import confdb.converter.ITableWriter;


/**
 * abstract base class for ascii table output
 * ------------------------
 * @author Ulf Behrens
 * @author Philipp Schieferdecker
 * 
 */
abstract public class AsciiTableWriter implements ITableWriter 
{
    //
    // member data
    //
    private static final String newline = "\n";
    private static final String colSeparator = "| ";

    
    //
    // member functions
    //

    /** generate ascii summary table representation of the configuration */
    abstract public String toString(IConfiguration conf, WriteProcess writeProcess) throws ConverterException;

    public void setConverterEngine( ConverterEngine converterEngine) 
    {
    }
    

    //
    // protected member functions
    //
    
    /** create ascii table as string */
    protected String createAsciiTable(ITable table)
    {
    	if ( table.rowCount()==0 ) 
    		return new String();
	
    	StringBuffer result = new StringBuffer(10000);
	
    	int          totalWidth      = table.totalWidth();
    	StringBuffer rowSeparator    = new StringBuffer(totalWidth);
    	StringBuffer headerSeparator = new StringBuffer(totalWidth);
    	for (int i=0;i<totalWidth;i++) {
    		rowSeparator.append("-");
    		headerSeparator.append("=");
    	}
	
    	result.append(newline).append(headerSeparator).append(newline);
    	for (int iColumn=0;iColumn<table.columnCount();iColumn++) {
    		String title = table.columnTitle(iColumn);
    		int    width = table.columnWidth(iColumn);
    		result.append(colSeparator).append(title).append(fillColumn(title,width));
    	}
    	result
	    	.append(colSeparator).append(newline)
	    	.append(headerSeparator).append(newline);
	
    	Iterator<ITableRow> itR = table.rowIterator();
    	while (itR.hasNext()) {
    		ITableRow row = itR.next();
    		for (int iLine=0;iLine<row.lineCount();iLine++) {
    			for (int iCol=0;iCol<table.columnCount();iCol++) {
    				String entry = row.dataEntry(iCol,iLine);
    				int    width = table.columnWidth(iCol);
    				result
    					.append(colSeparator).append(entry)
    					.append(fillColumn(entry,width));
    			}
    			result.append(colSeparator).append(newline);
    		}
    		result.append(rowSeparator).append(newline);
    	}
    	result.append(newline).append(newline);

    	return result.toString();
    }
    
    /** generate empty string to fill column */
    private String fillColumn(String data, int colWidth)
    {
    	int width = colWidth-data.length(); if (width<0) return new String();
    	StringBuffer result = new StringBuffer(width);
    	for (int i=0;i<width;i++) result.append(" ");
    		return result.toString();
    }
    
}

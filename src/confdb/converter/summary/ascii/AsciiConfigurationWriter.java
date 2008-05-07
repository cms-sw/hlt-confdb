package confdb.converter.summary.ascii;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.data.IConfiguration;

import confdb.converter.summary.ISummaryWriter;
import confdb.converter.summary.data.*;

/**
 * AsciiConfigurationWriter
 * ------------------------
 * @author Ulf Behrens
 * @author Philipp Schieferdecker
 * 
 * Display the summary information about each path (trigger) in an
 * ascii table.
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
public class AsciiConfigurationWriter implements ISummaryWriter 
{
    //
    // member data
    //
    private static final String newline = "\n";
    private static final String colSeparator = "| ";

    protected ConverterEngine converterEngine = null;
    
    
    //
    // member functions
    //

    /** generate ascii summary table representation of the configuration */
    public String toString(IConfiguration conf, WriteProcess writeProcess)
	throws ConverterException
    {
	StringBuffer result = new StringBuffer();

	SummaryTable summaryTable = new SummaryTable(conf);
	PrescaleTable prescaleTable = new PrescaleTable(conf);
	result.append(createAsciiTable(summaryTable));
	result.append(newline).append(newline);
	result.append(createAsciiTable(prescaleTable));
	result.append(newline).append(newline);

	return result.toString();
    }
    
    public void setConverterEngine(ConverterEngine converterEngine) 
    {
	this.converterEngine = converterEngine;
    }
    
    //
    // private member functions
    //
    
    /** create ascii table as string */
    private String createAsciiTable(ITable table)
    {
	if (table.rowCount()==0) return new String();
	
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

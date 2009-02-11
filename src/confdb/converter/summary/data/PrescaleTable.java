package confdb.converter.summary.data;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

import confdb.data.*;

/**
 * PrescaleTable
 * ------------
 * @author Philipp Schieferdecker
 *
 * Prescales to be displayed as tables of various formats (ascii,
 * html, latex, etc.)
 */
public class PrescaleTable implements ITable
{
    //
    // member data
    //
    
    /** the collection of prescale table rows */
    private ArrayList<ITableRow> rows = new ArrayList<ITableRow>();
    
    /** column titles */
    private ArrayList<String> columnTitle = new ArrayList<String>();

    /** column widths */
    private ArrayList<Integer> columnWidth = new ArrayList<Integer>();
    

    //
    // construction
    //

    /** standard constructor */
    public PrescaleTable(IConfiguration config)
    {
	initialize(config);
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
 
    //
    // private member functions
    //
    
    /** initialize the table */
    private void initialize(IConfiguration config)
    {
	ServiceInstance prescaleSvc = config.service("PrescaleService");
	if (prescaleSvc==null) return;

	VStringParameter vColumnNames =
	    (VStringParameter)prescaleSvc.parameter("lvl1Labels","vstring");
	if (vColumnNames==null) return;
	
	VPSetParameter vpsetPrescaleTable =
	    (VPSetParameter)prescaleSvc.parameter("prescaleTable","VPSet");
	if (vpsetPrescaleTable==null) return;

	
	columnTitle.add("TRIGGER NAME");
	columnWidth.add(columnTitle.get(0).length()+1);

	for (int i=0;i<vColumnNames.vectorSize();i++) {
	    columnTitle.add((String)vColumnNames.value(i));
	    columnWidth.add(columnTitle.get(i+1).length()+1);
	}
	
	HashMap<String,ArrayList<Long>> pathToPrescales =
	    new HashMap<String,ArrayList<Long>>();
	
	for (int i=0;i<vpsetPrescaleTable.parameterSetCount();i++) {
	    PSetParameter    pset      =vpsetPrescaleTable.parameterSet(i);
	    StringParameter  sPathName =(StringParameter)pset.parameter("pathName");
	    VUInt32Parameter vPrescales=(VUInt32Parameter)pset.parameter("prescales");
	    String           pathName  =(String)sPathName.value();
	    ArrayList<Long>  prescales = new ArrayList<Long>();
	    for (int ii=0;ii<vPrescales.vectorSize();ii++)
		prescales.add((Long)vPrescales.value(ii));
	    pathToPrescales.put(pathName,prescales);
	}
	
	Iterator<Path> itP = config.pathIterator();
	while (itP.hasNext()) {
	    Path path = itP.next();
	    ArrayList<Long> prescales = pathToPrescales.remove(path.name());
	    if (prescales==null) {
		prescales = new ArrayList<Long>();
		for (int i=0;i<columnCount()-1;i++) prescales.add(new Long(1));
	    }
	    PrescaleTableRow row = new PrescaleTableRow(path.name(),prescales);
	    //if (row.isNoTrigger()) continue; TODO
	    for (int iColumn=0;iColumn<row.columnCount();iColumn++) {
		int width = row.columnWidth(iColumn);
		if (width>columnWidth.get(iColumn)) columnWidth.set(iColumn,width);
	    }
	    rows.add(row);
	}
    }
    
}

package confdb.data;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * PrescaleTable
 * -------------
 * @author Philipp Schieferdecker
 *
 */
public class PrescaleTable
{
    //
    // member data
    //
    
    /** column names */
    protected String defaultName = new String();
    protected ArrayList<String> columnNames = new ArrayList<String>();

    /** prescale table rows */
    protected ArrayList<PrescaleTableRow> rows=new ArrayList<PrescaleTableRow>();
    

    //
    // construction
    //
    
    /** standard constructor */
    public PrescaleTable(IConfiguration config)
    {
	initialize(config);
    }
    
    /** NULL CONSTRUCTOR. To allow different construction in subclass. */
    protected PrescaleTable() {} 
    
    //
    // member functions
    //
    
    public String defaultName()
    {
	return defaultName;
    }
    public void setDefaultName(String name)
    {
	defaultName = name;
    }
    

    /** number of prescale columns */
    public int prescaleCount() { return columnNames.size()-1; }
    
    /** get the i-th prescale column name */
    public String prescaleColumnName(int i)
    {
	return columnNames.get(i+1);
    }

    /** number of prescaled paths */
    public int pathCount() { return rows.size(); }

    /** get i-th path name */
    public String pathName(int i)
    {
	return rows.get(i).pathName;
    }
    
    /** get the j-th prescale for i-th path */
    public long prescale(int i,int j)
    {
	return rows.get(i).prescales.get(j);
    }
    
    /** get the prescales for i-th path as comma-separated list */
    public String prescalesAsString(int i)
    {
	StringBuffer result = new StringBuffer();
	Iterator<Long> itI = rows.get(i).prescales.iterator();
	while (itI.hasNext()) {
	    if (result.length()>0) result.append(",");
	    result.append(itI.next());
	}
	return result.toString();
    }
    
    /** check if the i-th path is prescaled at all */
    public boolean isPrescaled(int i)
    {
	Iterator<Long> itI = rows.get(i).prescales.iterator();
	while (itI.hasNext()) if (itI.next()!=1) return true;
	return false;
    }

    /** get prescales for a given path name */
    public ArrayList<Long> prescales(String pathName)
    {
	ArrayList<Long> result = new ArrayList<Long>();
	Iterator<PrescaleTableRow> itR = rows.iterator();
	while (itR.hasNext()) {
	    PrescaleTableRow row = itR.next();
	    if (row.pathName.equals(pathName)) {
		for (Long l : row.prescales) result.add(l);
		return result;
	    }
	}
	return result;
    }
    
    /** set a prescale */
    public void setPrescale(int i,int j,long prescale)
    {
	rows.get(i).prescales.set(j,prescale);
    }

    /** add a new column at i-th position */
    public void addPrescaleColumn(int i,String columnName,long prescale)
    {
	if (i>=columnNames.size()) return;
	columnNames.add(i+1,columnName);
	Iterator<PrescaleTableRow> itR = rows.iterator();
	while (itR.hasNext()) itR.next().prescales.add(i,prescale);

    }
    public void duplicatePrescaleColumn(int i,String columnName)
    {
	if (i>=columnNames.size()) return;
	int index = columnNames.indexOf(columnName);
	if (index==-1) return;
	columnNames.add(i+1,"Copy_of_"+columnName);
	Iterator<PrescaleTableRow> itR = rows.iterator();
	while (itR.hasNext()) {
	    PrescaleTableRow r = itR.next();
	    Long p = r.prescales.get(index-1);
	    r.prescales.add(i,p);
	}
    }
    public void renamePrescaleColumn(int i,String columnName)
    {
	if (i>=columnNames.size()) return;
	columnNames.set(i,columnName);
    }
    public void reorderPrescaleColumns(ArrayList<String> newColumns)
    {
	if (newColumns.size()!=prescaleCount()) return;
	ArrayList<Integer> newIndices = new ArrayList<Integer>();
	for (int i=0; i<newColumns.size(); i++) {
	    String column = newColumns.get(i);
	    if (newColumns.lastIndexOf(column)!=i) return; // duplicate entry on newColumns
	    int    ind = columnNames.indexOf(column)-1;
	    if (ind<0) return; // label not found (or "Path" which is invalid)
	    newIndices.add(ind);
	}
	for (int i=0; i<prescaleCount(); i++) {
	    renamePrescaleColumn(i+1,newColumns.get(i));
	}
	ArrayList<Long> temp = new ArrayList<Long>();
	Iterator<PrescaleTableRow> itR = rows.iterator();
	while (itR.hasNext()) {
	    PrescaleTableRow r = itR.next();
	    temp.clear();
	    for (int i=0; i<prescaleCount(); i++) {
		temp.add(r.prescales.get(i));
	    }
	    for (int i=0; i<prescaleCount(); i++) {
		int  j = newIndices.get(i).intValue();
		r.prescales.set(i,temp.get(j));
	    }
	}
    }
    
    /** remove a column at the i-th position */
    public void removePrescaleColumn(int i)
    {
	if (i>columnNames.size()||i<=0) return;
	columnNames.remove(i);
	Iterator<PrescaleTableRow> itR = rows.iterator();
	while (itR.hasNext()) itR.next().prescales.remove(i-1);
    }


    //
    // private member functions
    //
    
    /** initialize the prescale table from a given configuration */
    private void initialize(IConfiguration config)
    {
	defaultName="";
	columnNames.clear();
	rows.clear();
	
	columnNames.add("Path");
 
	ServiceInstance prescaleSvc = config.service("PrescaleService");
	if (prescaleSvc==null) {
	    System.err.println("No PrescaleService found.");
	    return;
	}
	
	StringParameter vDefaultName =
	    (StringParameter)prescaleSvc.parameter("lvl1DefaultLabel","string");
	if (vDefaultName==null) {
	    System.err.println("No string lvl1DefaultLabel found.");
	    // return;
	}
 
	VStringParameter vColumnNames =
	    (VStringParameter)prescaleSvc.parameter("lvl1Labels","vstring");
	if (vColumnNames==null) {
	    System.err.println("No vstring lvl1Labels found.");
	    return;
	}
	
	VPSetParameter vpsetPrescaleTable =
	    (VPSetParameter)prescaleSvc.parameter("prescaleTable","VPSet");
	if (vpsetPrescaleTable==null) {
	    System.err.println("No VPSet prescaleTable found.");
	    return;
	}

	if (vDefaultName==null || vDefaultName.value()==null) {
	    defaultName = "";
	} else {
	    defaultName = (String)vDefaultName.value();
	}

	for (int i=0;i<vColumnNames.vectorSize();i++)
	    columnNames.add((String)vColumnNames.value(i));
	
	HashMap<String,PrescaleTableRow> pathToRow =
	    new HashMap<String,PrescaleTableRow>();
	
	for (int i=0;i<vpsetPrescaleTable.parameterSetCount();i++) {
	    PSetParameter    pset      =vpsetPrescaleTable.parameterSet(i);
	    StringParameter  sPathName =
		(StringParameter)pset.parameter("pathName");
	    VUInt32Parameter vPrescales=
		(VUInt32Parameter)pset.parameter("prescales");
	    String           pathName  =(String)sPathName.value();
	    
	    if (config.path(pathName)==null) {
		System.out.println("invalid pathName '"+pathName+"'.");
		continue;
	    }
	    if (vPrescales.vectorSize()!=columnNames.size()-1) {
		System.out.println("invalid size of vuint prescales.");
		continue;
	    }
	    
	    ArrayList<Long> prescales = new ArrayList<Long>();
	    for (int ii=0;ii<vPrescales.vectorSize();ii++)
		prescales.add((Long)vPrescales.value(ii));
	    pathToRow.put(pathName,new PrescaleTableRow(pathName,prescales));
	}
	
	Iterator<Path> itP = config.pathIterator();
	while (itP.hasNext()) {
	    Path path = itP.next();
	    PrescaleTableRow row = pathToRow.remove(path.name());
	    if (row==null)
		rows.add(new PrescaleTableRow(path.name(),prescaleCount()));
	    else
		rows.add(row);
	}
    }
    
}


//
// class to hold the data for one prescale table row
//
class PrescaleTableRow
{
    public String pathName;
    public ArrayList<Long> prescales;
    public PrescaleTableRow(String pathName,ArrayList<Long> prescales)
    {
	this.pathName = pathName;
	this.prescales = prescales;
    }
    public PrescaleTableRow(String pathName, int prescaleCount)
    {
	this.pathName = pathName;
	prescales = new ArrayList<Long>();
	for (int i=0;i<prescaleCount;i++) prescales.add(new Long(1));
    }
}

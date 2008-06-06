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
    private ArrayList<String> columnNames = new ArrayList<String>();

    /** prescale table rows */
    private ArrayList<PrescaleTableRow> rows = new ArrayList<PrescaleTableRow>();
    

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
    public int prescale(int i,int j)
    {
	return rows.get(i).prescales.get(j);
    }
    
    /** get the prescales for i-th path as comma-separated list */
    public String prescalesAsString(int i)
    {
	StringBuffer result = new StringBuffer();
	Iterator<Integer> itI = rows.get(i).prescales.iterator();
	while (itI.hasNext()) {
	    if (result.length()>0) result.append(",");
	    result.append(itI.next());
	}
	return result.toString();
    }
    
    /** check if the i-th path is prescaled at all */
    public boolean isPrescaled(int i)
    {
	Iterator<Integer> itI = rows.get(i).prescales.iterator();
	while (itI.hasNext()) if (itI.next()!=1) return true;
	return false;
    }

    /** set a prescale */
    public void setPrescale(int i,int j,int prescale)
    {
	rows.get(i).prescales.set(j,prescale);
    }

    /** add a new column at i-th position */
    public void addPrescaleColumn(int i,String columnName)
    {
	if (i>=columnNames.size()) return;
	columnNames.add(i+1,columnName);
	Iterator<PrescaleTableRow> itR = rows.iterator();
	while (itR.hasNext()) itR.next().prescales.add(i,1);
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
	columnNames.clear();
	rows.clear();
	
	columnNames.add("Path");
 
	ServiceInstance prescaleSvc = config.service("PrescaleService");
	if (prescaleSvc==null) {
	    System.err.println("No PrescaleService found.");
	    return;
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

	for (int i=0;i<vColumnNames.vectorSize();i++)
	    columnNames.add((String)vColumnNames.value(i));
	
	HashMap<String,PrescaleTableRow> pathToRow =
	    new HashMap<String,PrescaleTableRow>();
	
	for (int i=0;i<vpsetPrescaleTable.parameterSetCount();i++) {
	    PSetParameter    pset      =vpsetPrescaleTable.parameterSet(i);
	    StringParameter  sPathName =(StringParameter)pset.parameter("pathName");
	    VUInt32Parameter vPrescales=(VUInt32Parameter)pset.parameter("prescales");
	    String           pathName  =(String)sPathName.value();
	    
	    if (config.path(pathName)==null) {
		System.out.println("invalid pathName '"+pathName+"'.");
		continue;
	    }
	    if (vPrescales.vectorSize()!=columnNames.size()-1) {
		System.out.println("invalid size of vuint prescales.");
		continue;
	    }
	    
	    ArrayList<Integer> prescales = new ArrayList<Integer>();
	    for (int ii=0;ii<vPrescales.vectorSize();ii++)
		prescales.add((Integer)vPrescales.value(ii));
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
    public ArrayList<Integer> prescales;
    public PrescaleTableRow(String pathName,ArrayList<Integer> prescales)
    {
	this.pathName = pathName;
	this.prescales = prescales;
    }
    public PrescaleTableRow(String pathName, int prescaleCount)
    {
	this.pathName = pathName;
	prescales = new ArrayList<Integer>();
	for (int i=0;i<prescaleCount;i++) prescales.add(1);
    }
}

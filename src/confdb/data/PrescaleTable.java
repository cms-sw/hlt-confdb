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
    
	//paths of dataset paths which are split need to have the same prescale value
	//this allows us to determine which other rows need to be edited when this one is
	protected ArrayList<ArrayList<Integer> > rowSiblings = new ArrayList<ArrayList<Integer>>();

	protected boolean requirePathsToHavePrescaler = true;

	//if an external tool has provided the prescales, the name of the table is stored here
	//this should be empty otherewise and its cleared on  update of the prescale service
	protected String externalTableName = new String();
	protected String externalTableUUID = new String();
	protected String externalTableDBName = new String();

	// name of the pset we write the external tool table name
	public static final String PSTBLINFO_PSET_NAME = "PrescaleTableInfo";

	// menu location we write the prescales to
	public static final String PSTBL_CONFDB_LOCATION = "/prescales/v1";

    //
    // construction
    //
    
    /** standard constructor */
    public PrescaleTable(IConfiguration config)
    {
	initialize(config);
    }

	/** constructor with option to require paths to have prescaler 
	 * neeed by smart prescaler
	 */ 
	public PrescaleTable(IConfiguration config, boolean requirePathsToHavePrescaler)
	{
		this.requirePathsToHavePrescaler = requirePathsToHavePrescaler;
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
    
	public String externalTableName(){
		return externalTableName;
	}
	public String externalTableUUID(){
		return externalTableUUID;
	}
	public String externalTableDBName(){
		return externalTableDBName;
	}
     
	public void setExternalTableName(String name){
		externalTableName = name;
	}
	public void setExternalTableUUID(String uuid){
		externalTableUUID = uuid;
	}
	public void setExternalTableDBName(String name){
		externalTableDBName = name;
	}
		
	public boolean hasExternalTableInfo(){
		return externalTableName.length()>0;
	}
	public void clearExternalTableInfo(){
		externalTableName = "";
		externalTableUUID = "";
		externalTableDBName = "";
	}
	
	/** get the column names */
	public ArrayList<String> columnNames() { return columnNames; }

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

	
	public int rowNr(String pathName){
		for(int rowNr=0;rowNr<rows.size();rowNr++){
			if(rows.get(rowNr).pathName.equals(pathName)){
				return rowNr;
			}
		}
		return -1;
	}

	protected PrescaleTableRow row(String pathName){
		int rowNr = rowNr(pathName);
		return rowNr!=-1 ? rows.get(rowNr) : null;							
	}


    /** get prescales for a given path name */
    public ArrayList<Long> prescales(String pathName)
    {
		ArrayList<Long> result = new ArrayList<Long>();
		PrescaleTableRow row = row(pathName);
		if(row!=null){			
			for (Long l : row.prescales) result.add(l);
	    }
		return result;
    }

	/** get prescales for a given path name */
    public ArrayList<Long> prescales(int rowNr)
    {
		ArrayList<Long> result = new ArrayList<Long>();
		PrescaleTableRow row = rows.get(rowNr);
		if(row!=null){			
			for (Long l : row.prescales) result.add(l);
	    }
		return result;
    }
    
    /** set a prescale for a rhow and all its siblings */
    public void setPrescale(int rowNr,int columnNr,long prescale)
    {
		for(int siblingNr : rowSiblings.get(rowNr)){
			rows.get(siblingNr).prescales.set(columnNr,prescale);
		}
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
	rowSiblings.clear();
	
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
		System.err.println("invalid pathName '"+pathName+"'.");
		continue;
	    }
	    if (vPrescales.vectorSize()!=columnNames.size()-1) {
		System.err.println("invalid size of vuint prescales.");
		continue;
	    }
	    
	    ArrayList<Long> prescales = new ArrayList<Long>();
	    for (int ii=0;ii<vPrescales.vectorSize();ii++)
		prescales.add((Long)vPrescales.value(ii));
	    pathToRow.put(pathName,new PrescaleTableRow(pathName,prescales));
	}
	
	HashMap<String,Integer> datasetPathToRowIndex = new HashMap<String,Integer>();
	Iterator<Path> itP = config.pathIterator();
	while (itP.hasNext()) {
	    Path path = itP.next();

            // If the Path does not contain any HLTPrescaler modules,
            // do not show it in the PrescaleTable
			// okay but turns out this is used by the smart prescale service
			// which CAN have paths without prescale modules in it
			// so this needs to be optional
            if(this.requirePathsToHavePrescaler && !path.hasHLTPrescalerModule()) continue;

	    PrescaleTableRow row = pathToRow.remove(path.name());
	    if (row==null)
		rows.add(new PrescaleTableRow(path.name(),prescaleCount()));
	    else
		rows.add(row);

	    if(path.isDatasetPath()){
		datasetPathToRowIndex.put(path.name(),rows.size()-1);
	    }
	}

	//so this all rather depends on the dataset paths being correctly named
	//if they are not they will not update the other split instances
	//and configuration checks will catch it later
	for(int rowNr=0;rowNr<rows.size();rowNr++){
		PrescaleTableRow row = rows.get(rowNr);
		ArrayList<Integer> rowSiblingsEntry = new ArrayList<Integer>();
		if(row.pathName.startsWith(PrimaryDataset.datasetPathNamePrefix()) && 
			config.path(row.pathName).isDatasetPath()){ 
			String datasetName = row.pathName.replaceFirst(PrimaryDataset.datasetPathNamePrefix(),"");
			PrimaryDataset pd = config.dataset(datasetName);
			ArrayList<PrimaryDataset> pdSplitSiblings = pd.getSplitSiblings();
			for(PrimaryDataset splitSib : pdSplitSiblings){				
				rowSiblingsEntry.add(datasetPathToRowIndex.get(splitSib.datasetPathName()));
			}
		}else{
			rowSiblingsEntry.add(rowNr);
		}
		rowSiblings.add(rowSiblingsEntry);
	}

    }

	/* returns the column labels as string suitable to for a VString pset representation */
	public  String getColumnsAsVStringStr(){
		StringBuffer labelsAsString = new StringBuffer();
		for (int i = 0; i <this.prescaleCount(); i++) {
			if (labelsAsString.length() > 0)
				labelsAsString.append(",");
			labelsAsString.append(this.prescaleColumnName(i));
		}
		return labelsAsString.toString();
	}
    
}

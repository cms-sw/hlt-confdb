package confdb.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Class SinglePathPrescaleTable
 * @author raul.jimenez.estupinan@cern.ch
 * @see PrescaleTable, PrescaleTableService
 * Implements PrescaleTable to contain prescales of one single Path each time.
 * This class has been designed to manipulate prescales of a single path embedded
 * in a panel.
 * This class allows to edit the prescales.
 * IMPORTANT: DO NOT USE the parent method "initialize" since it will fill all prescales.
 * */
public class SinglePathPrescaleTable extends PrescaleTable {
	// Class members:
	Path path;
	

	// CONSTRUCTOR
	/** PrescaleTable constructor to load one single path 
     *  Only fill the table with prescales for the given path.*/
    public SinglePathPrescaleTable(IConfiguration config, Path path) {
    	this.path = path;
    	initializeForGivenPath(config, path);
    }
    
    // METHODS
    
    /** Initialise prescale table with prescales for the given path.  */
    private void initializeForGivenPath(IConfiguration config, Path path)
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
		
		StringParameter vDefaultName = (StringParameter)prescaleSvc.parameter("lvl1DefaultLabel","string");
		if (vDefaultName==null) System.err.println("No string lvl1DefaultLabel found.");
	 
		VStringParameter vColumnNames = (VStringParameter)prescaleSvc.parameter("lvl1Labels","vstring");
		if (vColumnNames==null) {
		    System.err.println("No vstring lvl1Labels found.");
		    return;
		}
		
		VPSetParameter vpsetPrescaleTable = (VPSetParameter)prescaleSvc.parameter("prescaleTable","VPSet");
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
		
		HashMap<String,PrescaleTableRow> pathToRow = new HashMap<String,PrescaleTableRow>();
		
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
		    // Select only the given path.
			if(itP.next().equals(path)) {
			    PrescaleTableRow row = pathToRow.remove(path.name());
			    if (row==null) 	rows.add(new PrescaleTableRow(path.name(),prescaleCount()));
			    else 			rows.add(row);				
			}

		}
    }
}

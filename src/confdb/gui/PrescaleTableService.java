package confdb.gui;

import java.util.ArrayList;

import javax.swing.JTable;
import confdb.data.Configuration;
import confdb.data.IConfiguration;
import confdb.data.PSetParameter;
import confdb.data.Parameter;
import confdb.data.Path;
import confdb.data.PrescaleTable;
import confdb.data.ServiceInstance;
import confdb.data.SinglePathPrescaleTable;
import confdb.data.StringParameter;
import confdb.data.VPSetParameter;
import confdb.data.VUInt32Parameter;

/**
 * PrescaleTableService
 * This is to get a prescale JTable for one particular path. 
 * NOTE: It will be used in rightUpperPanel. */
class PrescaleTableService {
    /** model for the prescale table */
    private SinglePathPrescaleTableModel 	tableModel	;
	private Configuration 		config		;
    private JTable     jTable           = new javax.swing.JTable();
    private boolean	   hasChanged		= false;
    

    public PrescaleTableService(){};	// NULL CONSTRUCTOR
    public PrescaleTableService(Configuration config) {
    	this.config = config;
    }

    /** Initialise a customised Prescale table using the given path. 
     * NOTE: The table will be used to ONLY display prescales and
     * not to modify them. */
    public JTable initialise(Path path) {
    	tableModel = new SinglePathPrescaleTableModel();
    	tableModel.initialize(config, path);
    	jTable.setModel(tableModel);
    	jTable.setDefaultRenderer(Integer.class,new PrescaleTableCellRenderer());
    	jTable.setEnabled(false);	// THIS MAKES THE TABLE READ/ONLY.
    	adjustTableColumnWidths();
    	return jTable;
    }
    
    /** adjust the width of each table column */
    private void adjustTableColumnWidths()
    {
		int tableWidth = jTable.getPreferredSize().width;
		int columnCount = jTable.getColumnModel().getColumnCount();
	        int headerWidth = (int) (tableWidth * 0.4);
		jTable.getColumnModel().getColumn(0).setPreferredWidth(headerWidth);
		for (int i = 1; i < columnCount; i++) {
		    int columnWidth = (tableWidth - headerWidth) / (columnCount-1);
		    jTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth);
		}
    }
    
    /** Get the Row corresponding to the given Path */
    public JTable getPrescaleTable(Path path) {
    	return initialise(path);
    }
    
    /** Get a JPanel with the corresponding prescale row.
     * Allow to edit the prescale for that path and includes
     * Component to save the values ready to be embebed in a Panel. 
     * @param path
     * @return JPanel
     */
    public JTable getPrescaleTableEditable(Path path) {
    	JTable table = initialise(path);
    	table.setEnabled(true);    	
    	return table;
    }
    
    public void savePrescales() {
    	//tableModel.updatePrescaleService(config); DO NOT USE. This will scratch ALL prescales.
    	tableModel.updatePrescaleServiceSinglePath(config);
    	
    }
    
    public void setHasChanged() { hasChanged = true; }
    
    public boolean hasChanged() { return hasChanged; }
}



/**
* PrescaleTable Model for one single prescale row/path
* NOTE: DO NOT USE the parent methods since that will scratch ALL prescales.
* In this Model will only be loaded ONE path prescale.
*/

class SinglePathPrescaleTableModel extends PrescaleTableModel {
	
	/* Initialize the table with only one row - the given path */
    public void initialize(IConfiguration config, Path path) {
    	prescaleTable = new SinglePathPrescaleTable(config, path);
    }
	
    /*Update prescale service with the only row contained in the table. 
     * NOTE: This function copies all the values in the table from the Original PrescaleTable.
     * This follows the workflow of the original class.
     * */
    public void updatePrescaleServiceSinglePath(IConfiguration config) {
    	
    	PrescaleTable fullTable = new PrescaleTable(config); // FULL TABLE:
    	
    	
    	ServiceInstance prescaleSvc = config.service("PrescaleService");
    	if (prescaleSvc==null) {
    	    System.err.println("No PrescaleService found.");
    	    return;
    	}
    	prescaleSvc.updateParameter("lvl1DefaultLabel","string",prescaleTable.defaultName());
    	StringBuffer labelsAsString = new StringBuffer();
    	for (int i=0;i<prescaleTable.prescaleCount();i++) {
    	    if (labelsAsString.length()>0) labelsAsString.append(",");
    	    labelsAsString.append(prescaleTable.prescaleColumnName(i));
    	}
    	prescaleSvc.updateParameter("lvl1Labels","vstring",labelsAsString.toString());
    	
    	
    	VPSetParameter vpsetPrescaleTable = (VPSetParameter)prescaleSvc.parameter("prescaleTable","VPSet");
    	if (vpsetPrescaleTable==null) {
    	    System.err.println("No VPSet prescaleTable found.");
    	    return;
    	}
    	vpsetPrescaleTable.setValue("");
    	
    	// Get the values of the modified path prescale.
    	String pathNameToUpdate = prescaleTable.pathName(0);			// Zero because there is only one row.
    	String PrescAsString 	= prescaleTable.prescalesAsString(0);	// Zero because there is only one row.
    	
    	for (int iPath=0;iPath<fullTable.pathCount();iPath++) {
    	    String pathName = fullTable.pathName(iPath);
    	    String prescalesAsString = fullTable.prescalesAsString(iPath);
    	    
    	    if(pathName.compareTo(pathNameToUpdate) == 0)  {
    	    	prescalesAsString = PrescAsString; // Overwrite values with the update.	
    	    } else {
    	    	if (!fullTable.isPrescaled(iPath)) continue;
    	    }
    	    
    	    ArrayList<Parameter> params = new ArrayList<Parameter>();
    	    StringParameter  sPathName = new StringParameter("pathName",pathName,true);
    	    VUInt32Parameter vPrescales = new VUInt32Parameter("prescales",prescalesAsString,true);
    	    params.add(sPathName);
    	    params.add(vPrescales);
    	    vpsetPrescaleTable.addParameterSet(new PSetParameter("",params,true));
    	    
    	}
    	prescaleSvc.setHasChanged();
    }
	
}
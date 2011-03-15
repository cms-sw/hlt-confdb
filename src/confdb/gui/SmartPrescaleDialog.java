package confdb.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import confdb.data.*;

/**
 * SmartPrescaleDialog
 * --------------
 * @author Philipp Schieferdecker
 *
 * Edit the prescale table, which is encoded in the configuration of
 * the SmartPrescaleService.
 */
public class SmartPrescaleDialog extends JDialog
{
    //
    // member data
    //
    
    /** reference to the configuration */
    private Configuration config;
    private ModuleInstance module;

    
    /** GUI components */
    private JTextField jTextFieldHLT    = new javax.swing.JTextField();
    private JComboBox  jComboBoxModule  = new javax.swing.JComboBox();
    private JButton    jButtonOK        = new javax.swing.JButton();
    private JButton    jButtonApply     = new javax.swing.JButton();
    private JButton    jButtonCancel    = new javax.swing.JButton();
    private JTable     jTable           = new javax.swing.JTable();
    private DefaultComboBoxModel cmbModule;
    
    /** model for the smart prescale table */
    private SmartPrescaleTableModel smartTableModel;

    private ArrayList<SmartPrescaleTable> smartPrescaleTable;

    /** index of the selected column */
    private int iRow = 0;
    
    
    //
    // construction
    //

    /** standard constructor */
    public SmartPrescaleDialog(JFrame jFrame,Configuration config)
    {


	super(jFrame,true);
	this.config = config;

	cmbModule=(DefaultComboBoxModel)jComboBoxModule.getModel();
	cmbModule.removeAllElements();
      
	
	smartPrescaleTable= new ArrayList<SmartPrescaleTable>();
	Iterator<ModuleInstance> itM = config.moduleIterator();
	while (itM.hasNext()) {
	    ModuleInstance moduleT = itM.next();
	    if(moduleT.template().name().equals("TriggerResultsFilter")){
		cmbModule.addElement(moduleT.name());
		smartPrescaleTable.add(new SmartPrescaleTable(config,moduleT));
	    }
	}

	jComboBoxModule.setSelectedIndex(0);
	module = config.module((String)jComboBoxModule.getSelectedItem());

	jComboBoxModule.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxModelActionPerformed(e);
		}
	    });
					   
	
	smartTableModel = new SmartPrescaleTableModel();
	smartTableModel.initialize(config,module,smartPrescaleTable.get(0));
	jTable.setModel(smartTableModel);
	jTable.setDefaultRenderer(String.class, new SmartPrescaleTableCellRenderer());
	jTable.setDefaultRenderer(Integer.class,new SmartPrescaleTableCellRenderer());
	jTextFieldHLT.setText(config.toString());
	
	jButtonCancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });
	jButtonApply.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    applySmartPrescale();
		}
	    });
	jButtonOK.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    applySmartPrescale();
		    setVisible(false);
		}
	    });
	jTable.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    jTableShowPopup(e);
		}
		public void mouseReleased(MouseEvent e) {
		    jTableShowPopup(e);
		}
	    });
				
	setTitle("SmartPrescale Editor");
	setContentPane(initComponents());
	adjustTableColumnWidths();
    }
    
    //
    // private member functions
    //
    
    /** update the configurations SmartPrescaleService according to table data */
    /*   private void updateSmartPrescaleService()
    {
	tableModel.updateSmartPrescaleWindow(module);
	}*/
    
    /** adjust the width of each table column */
    private void adjustTableColumnWidths()
    {
	int tableWidth = jTable.getPreferredSize().width;
	int columnCount = jTable.getColumnModel().getColumnCount();
	for (int i=0;i<columnCount;i++) {
	    int columnWidth = (i==0) ? tableWidth/2 : tableWidth/2/(columnCount-1);
	    jTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth);
	}
    }
    
    // listener callbacks
    private void jTableShowPopup(MouseEvent e)
    {
       
       	if (!e.isPopupTrigger()) return;

	iRow = jTable.rowAtPoint(e.getPoint());
	JPopupMenu popup = new JPopupMenu();

	JMenuItem menuItemAdd = new JMenuItem("Add Row");	
	menuItemAdd.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae)
		{
		    smartTableModel.addRow(iRow+1, //+1 for insert after
					   JOptionPane
					   .showInputDialog("Enter the condition: "));
		}
	    });
	popup.add(menuItemAdd);

	JMenuItem menuItemRemove = new JMenuItem("Remove Row");	
	menuItemRemove.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae)
		{
		    smartTableModel.removeRow(iRow);
		}
	    });
	popup.add(menuItemRemove);

	popup.show(e.getComponent(),e.getX(),e.getY());
    }
    
    private void jComboBoxModelActionPerformed(ActionEvent e)
    {
	updateMainPanel();
    }


    private void updateMainPanel(){
	module=config.module((String)jComboBoxModule.getSelectedItem());
	int i =jComboBoxModule.getSelectedIndex();
	System.out.println(module.name()+ " "+smartPrescaleTable.get(i).module.name()+" "+i);
	smartTableModel.updateSmartPrescaleWindow(module,smartPrescaleTable.get(i));
    }


    
    /** update the SmartPrescaleService in configuration according to table data Apply changes*/
    public void applySmartPrescale()
    {
	for(int i=0;i<smartPrescaleTable.size();i++){
	    VStringParameter parameterTriggerConditions =  (VStringParameter)smartPrescaleTable.get(i).module.parameter("triggerConditions");
	    parameterTriggerConditions.setValue("");
	    for(int j=0;j<smartPrescaleTable.get(i).prescaleConditionCount();j++){
		String condition = smartPrescaleTable.get(i).prescaleCondition(j);
		if(!condition.equals("")) {
		    if ( (!smartPrescaleTable.get(i).simple(j))
			 || (smartPrescaleTable.get(i).prescale(j) != 0)
			 || (condition.substring(0,2).equals("L1")) ) {
			parameterTriggerConditions.addValue(smartPrescaleTable.get(i).prescaleCondition(j));
		    }
		}
	    }
	}
    }


    /** initialize GUI components */
    private JPanel initComponents()
    {
	JPanel jPanel = new JPanel();
	
        JLabel      jLabel1     = new javax.swing.JLabel();
        JLabel      jLabel2     = new javax.swing.JLabel();
        JScrollPane jScrollPane = new javax.swing.JScrollPane();
	
        jLabel1.setText("HLT:");
        jLabel2.setText("Instance:");
	
        jTextFieldHLT.setEditable(false);
        jTextFieldHLT.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jComboBoxModule.setEditable(false);
        jComboBoxModule.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
       

        jScrollPane.setViewportView(jTable);

        jButtonOK.setText("OK");
        jButtonApply.setText("Apply");
        jButtonCancel.setText("Cancel");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				  .add(layout.createSequentialGroup()
				       .addContainerGap()
				       .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					    .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 708, Short.MAX_VALUE)
					    .add(layout.createSequentialGroup()
						 .add(jLabel1)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						 .add(jTextFieldHLT, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
						 .add(18, 18, 18)
						 .add(jLabel2)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						 .add(jComboBoxModule, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
					    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
						 .add(jButtonCancel)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
						 .add(jButtonApply)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
						 .add(jButtonOK)))
				       .addContainerGap())
				  );
	
        layout.linkSize(new java.awt.Component[] {jButtonApply, jButtonCancel, jButtonOK}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
	
        layout.setVerticalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
				     .add(22, 22, 22)
				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
					  .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					  .add(jTextFieldHLT, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
					  .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					  .add(jComboBoxModule, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
				     .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
					  .add(jButtonOK)
					  .add(jButtonApply)
					  .add(jButtonCancel))
				     .addContainerGap())
				);
	return jPanel;
    }

}


//
//SmartPrescaleTableModel
//
class SmartPrescaleTableModel extends AbstractTableModel
{
    /** the presacale table data structure */
    private PrescaleTable prescaleTable;
    private SmartPrescaleTable smartPrescaleTable;
    private IConfiguration config;
    private ModuleInstance module;

    /** update the table model according to configuration's SmartPrescaleService */
    public void initialize(IConfiguration config,ModuleInstance module,SmartPrescaleTable smartPrescaleTable)
    {
	this.config = config;
	this.module = module;
	prescaleTable = new PrescaleTable(config);
	this.smartPrescaleTable = smartPrescaleTable;
	fireTableStructureChanged();
	fireTableDataChanged();
    }

    /** update the SmartPrescale Window */
    public void updateSmartPrescaleWindow(ModuleInstance module,SmartPrescaleTable smartPrescaleTable)
    {
	this.module = module;
	this.smartPrescaleTable = smartPrescaleTable;
	prescaleTable = new PrescaleTable(config);
	fireTableStructureChanged();
	fireTableDataChanged();
    }
    

    
    /** number of rows */
    public int getRowCount() { return smartPrescaleTable.prescaleConditionCount(); }
    
    public int getColumnCount() { return prescaleTable.prescaleCount()+2; }
    
    /** get column name for colimn 'col' */
    public String getColumnName(int col) {
	if (col==0) {
	    ArrayList<Stream> streams = smartPrescaleTable.associatedStreams();
	    String work;
	    if (streams.size()==0) {
		work="No stream";
	    } else if (streams.size()==1) {
		work="Stream: "+streams.get(0).name();
	    } else {
		work="Streams: "+streams.get(0).name();
		for (int i=1; i<streams.size(); ++i) work += ","+streams.get(i).name();
	    }
	    return work;
	} else if (col==1) {
	    return "SMART";
	} else {
	    return prescaleTable.prescaleColumnName(col-2);
	}
    }
   
    /** is a cell editable or not? */
    public boolean isCellEditable(int row, int col) { return col==0; }
    
    /** get the class of the column 'c' */
    public Class getColumnClass(int c)
    {
	if (c==0) {
	    return String.class;
	} else {
	    return Integer.class;
	}
    }

    /** set the value of a table cell */
    public void setValueAt(Object value,int row, int col)
    {
	String strCondition = SmartPrescaleTable.regularise((String)value);
	if(strCondition.equals("")) return;

	StringTokenizer pathTokens = new StringTokenizer(strCondition,"/ ");

	while ( pathTokens.hasMoreTokens()) {
	    String strPath = pathTokens.nextToken().trim();
	    if (strPath.length()<5) continue;
	    int g = -10000;
	    try { 
		g = Integer.parseInt(strPath); 
	    }catch (NumberFormatException e) { 
		g = -10000;
	    }
	    if ( (g<0)
		 && (!strPath.equals("FALSE"))
		 && (!strPath.substring(0,2).equals("L1"))
		 && (smartPrescaleTable.checkPathExists(strPath)==null) ) {
		return;
	    }
	}

	// replace conditions containing only FALSE by empty conditions
	strCondition = SmartPrescaleTable.simplify(strCondition);

	if (!strCondition.equals("")) {
	    smartPrescaleTable.modRow(row,strCondition);
	}
    }
    
    
    /** check if a certain path is already in the list of rows */
    private boolean rowsContainPath(String pathName)
    {
	for (int iPath=0;iPath<smartPrescaleTable.prescaleConditionCount();iPath++)
	    if (smartPrescaleTable.prescaleCondition(iPath).indexOf(pathName)>=0) return true;
	return false;
    }

    /** get the value for row,col */
    public Object getValueAt(int row, int col)
    {
	if (col==0) {
	    return smartPrescaleTable.prescaleCondition(row);
	} else if (col==1) {
	    if (smartPrescaleTable.simple(row)) {
		return smartPrescaleTable.prescale(row);
	    } else {
		return new Long(-1);
	    }
	} else {
	    if (smartPrescaleTable.simple(row)) {
		String pathName=smartPrescaleTable.pathName(row);
		if (smartPrescaleTable.checkPathExists(pathName)!=null) {
		    return smartPrescaleTable.prescale(row)
			*prescaleTable.prescales(pathName).get(col-2);
		} else {
		    return new Long(-1);
		}
	    } else {
		return new Long(-1);
	    }
	}
    }

    /** add an additional row  */
    public void addRow(int i, String value)
    {
	String strCondition = SmartPrescaleTable.regularise((String)value);
	if(strCondition.equals("")) return;
	
	StringTokenizer pathTokens = new StringTokenizer(strCondition,"/ ");

	while ( pathTokens.hasMoreTokens()) {
	    String strPath = pathTokens.nextToken().trim();
	    if (strPath.length()<5) continue;
	    int g = -10000;
	    try { 
		g = Integer.parseInt(strPath); 
	    }catch (NumberFormatException e) { 
		g = -10000;
	    }
	    if ( (g<0)
		 && (!strPath.equals("FALSE"))
		 && (!strPath.substring(0,2).equals("L1"))
		 && (smartPrescaleTable.checkPathExists(strPath)==null) ) {
		return;
	    }
	}

	// replace conditions containing only FALSE by empty conditions
	strCondition = SmartPrescaleTable.simplify(strCondition);

	if (!strCondition.equals("")) {
	    smartPrescaleTable.addRow(i,strCondition);
	    fireTableStructureChanged();
	}
    }

   /** remove row  */
    public void removeRow(int i)
    {
	smartPrescaleTable.removeRow(i);
	fireTableStructureChanged();
    }
    

}


//
// SmartPrescaleTableCellRenderer
//
class SmartPrescaleTableCellRenderer extends DefaultTableCellRenderer
{
    public Component getTableCellRendererComponent(JTable table,
						   Object value,
						   boolean isSelected,
						   boolean hasFocus,
						   int row,int column)
    {
	setText(value.toString());
	if (value instanceof Long) {
	    setHorizontalAlignment(SwingConstants.CENTER);
	    long valueAsLong = (Long)value;
	    if (valueAsLong==0) setBackground(Color.RED);
	    else if (valueAsLong==1) setBackground(Color.GREEN);
	    else if (valueAsLong >1) setBackground(Color.ORANGE);
	    else setBackground(Color.BLUE);
	} else if (value instanceof String) {
	    String valueAsString=(String)value;
	    if (valueAsString.indexOf("/")==-1) {
		setBackground(Color.GREEN);
	    } else if (valueAsString.indexOf("/ 0")>=0) {
		setBackground(Color.RED);
	    } else {
		setBackground(Color.ORANGE);
	    }
	}
	return this;
    }
}

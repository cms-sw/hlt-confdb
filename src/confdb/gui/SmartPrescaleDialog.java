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
    
    /** model for the prescale table */
    private SmartPrescaleTableModel tableModel;

    private ArrayList<SmartPrescaleTable> prescaleTable;

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
      
	
	prescaleTable= new ArrayList<SmartPrescaleTable>();
	Iterator<ModuleInstance> itM = config.moduleIterator();
	while (itM.hasNext()) {
	    ModuleInstance moduleT = itM.next();
	    if(moduleT.template().name().equals("TriggerResultsFilter")){
		cmbModule.addElement(moduleT.name());
		prescaleTable.add(new SmartPrescaleTable(config,moduleT));
	    }
	}

	jComboBoxModule.setSelectedIndex(0);
	module = config.module((String)jComboBoxModule.getSelectedItem());

	jComboBoxModule.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxModelActionPerformed(e);
		}
	    });
					   
	
	tableModel = new SmartPrescaleTableModel();
	tableModel.initialize(config,module,prescaleTable.get(0));
	jTable.setModel(tableModel);
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
	JMenuItem menuItem = new JMenuItem("Add Row");	
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae)
		{
		    tableModel.addRow(iRow,
					 JOptionPane
					 .showInputDialog("Enter the condition: "));
		}
	    });
	popup.add(menuItem);

    
	JMenuItem menuItemRemove = new JMenuItem("Remove Row");	
	menuItemRemove.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae)
		{
		    tableModel.removeRow(iRow);
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
	System.out.println(module.name()+ " "+prescaleTable.get(i).module.name()+" "+i);
	tableModel.updateSmartPrescaleWindow(module,prescaleTable.get(i));
    }


    
    /** update the SmartPrescaleService in configuration according to table data Apply changes*/
    public void applySmartPrescale()
    {
	for(int i=0;i<prescaleTable.size();i++){
	    VStringParameter parameterTriggerConditions =  (VStringParameter)prescaleTable.get(i).module.parameter("triggerConditions");
	    parameterTriggerConditions.setValue("");
	    for(int j=0;j<prescaleTable.get(i).prescaleConditionCount();j++){
		if(!prescaleTable.get(i).prescaleCondition(j).equals(""))
		    parameterTriggerConditions.addValue(prescaleTable.get(i).prescaleCondition(j));
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
        jLabel2.setText("Level1:");
	
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
    private SmartPrescaleTable prescaleTable;
    private IConfiguration config;
    private ModuleInstance module;

    /** update the table model according to configuration's SmartPrescaleService */
    public void initialize(IConfiguration config,ModuleInstance module,SmartPrescaleTable prescaleTable)
    {
	this.config = config;
	this.module = module;
	this.prescaleTable = prescaleTable;
	fireTableDataChanged();
    }

    /** update the SmartPrescale Window */
    public void updateSmartPrescaleWindow(ModuleInstance module,SmartPrescaleTable prescaleTable)
    {
	this.module = module;
	this.prescaleTable = prescaleTable;
	fireTableDataChanged();
    }
    

    
    /** number of rows */
    public int getRowCount() { return prescaleTable.prescaleConditionCount(); }
    
    public int getColumnCount() { return 1; }
    
   
   
    /** is a cell editable or not? */
    public boolean isCellEditable(int row, int col) { return col>0; }
    
    /** set the value of a table cell */
    public void setValueAt(Object value,int row, int col)
    {
    }
    
    
    /** check if a certain path is already in the list of rows */
    private boolean rowsContainPath(String pathName)
    {
	for (int iPath=0;iPath<prescaleTable.prescaleConditionCount();iPath++)
	    if (pathName.equals(prescaleTable.prescaleCondition(iPath))) return true;
	return false;
    }

    /** get the value for row,col */
    public Object getValueAt(int row, int col)
    {
	return  prescaleTable.prescaleCondition(row);
    }

    /** add an additional row  */
    public void addRow(int i,String strCondition)
    {
	if(strCondition.equals(""))
	    return;
	
	StringTokenizer pathTokens = new StringTokenizer(strCondition, "+-/ &*");

	while ( pathTokens.hasMoreTokens()) {
	    String strPath = pathTokens.nextToken();
	    int g = -10000;
	    try { 
		g = Integer.parseInt(strPath); 
	    }catch (NumberFormatException e) { 
		g = -10000;
	    }
	    if(g>0)
		continue;
	    Path path = prescaleTable.checkPathExists(strPath);
	    if(path==null)
		return;
	};

	prescaleTable.addRow(i,strCondition);
	fireTableStructureChanged();
    }

   /** remove row  */
    public void removeRow(int i)
    {
	prescaleTable.removeRow(i);
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
	    else setBackground(Color.ORANGE);
	}
	return this;
    }
}

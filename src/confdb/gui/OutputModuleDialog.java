package confdb.gui;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import confdb.gui.treetable.CheckBoxTableCellRenderer;
import confdb.data.*;


/**
 * OutputModuleDialog
 * ------------------
 * @author Philipp Schieferdecker
 *
 * Editor to help set the SelectEvents and outputCommand parameters of
 * OutputModules.
 */
public class OutputModuleDialog extends JDialog
{
    //
    // member data
    //

    /** reference to the configuration */
    private Configuration config;

    /** current output module being edited */
    private ModuleInstance outputModule = null;

    /** products */
    private HashMap<String,Product> products = new HashMap<String,Product>();
    
    /** GUI components */
    private JComboBox   jComboBoxOutputModule = new javax.swing.JComboBox();
    private JComboBox   jComboBoxAddPaths     = new javax.swing.JComboBox();
    private JList       jListPaths            = new javax.swing.JList();
    private JCheckBox   jCheckBoxKeepRaw      = new javax.swing.JCheckBox();
    private JTextField  jTextFieldSearch      = new javax.swing.JTextField();
    private JTable      jTableProducts        = new javax.swing.JTable();
    private JButton     jButtonCancel         = new javax.swing.JButton();
    private JButton     jButtonApply          = new javax.swing.JButton();
    private JButton     jButtonOK             = new javax.swing.JButton();

    /** GUI models */
    private DefaultComboBoxModel      cbmOutputModules;
    private DefaultListModel          lmPaths;
    private DefaultListSelectionModel lsmPaths;
    private DefaultComboBoxModel      cbmPaths;
    private ProductTableModel         tmProducts;
    

    //
    // construction
    //
    
    /** standard constructor */
    public OutputModuleDialog(JFrame jFrame, Configuration config)
    {
	super(jFrame,true);
	this.config = config;
	
	jTableProducts.setDefaultRenderer(Boolean.class,
					  new CheckBoxTableCellRenderer());

	jListPaths.setModel(new DefaultListModel());
	jTableProducts.setModel(new ProductTableModel());

	jTableProducts.getColumnModel().getColumn(0).setPreferredWidth(50);
	jTableProducts.getColumnModel().getColumn(1).setPreferredWidth(400);
	jTableProducts.getColumnModel().getColumn(2).setPreferredWidth(150);
	jTableProducts.getColumnModel().getColumn(3).setPreferredWidth(150);

	cbmOutputModules=(DefaultComboBoxModel)jComboBoxOutputModule.getModel();
	lmPaths         =(DefaultListModel)jListPaths.getModel();
	lsmPaths        =(DefaultListSelectionModel)jListPaths.getSelectionModel();
	cbmPaths        =(DefaultComboBoxModel)jComboBoxAddPaths.getModel();
	tmProducts      =(ProductTableModel)jTableProducts.getModel();
	
	cbmOutputModules.removeAllElements();
	cbmOutputModules.addElement("");
	Iterator<ModuleInstance> itM = config.moduleIterator();
	while (itM.hasNext()) {
	    ModuleInstance module = itM.next();
	    if (module.template().type().equals("OutputModule"))
		cbmOutputModules.addElement(module.name());
	}
	
	// register action listeners
	jComboBoxOutputModule.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxOutputModuleActionPerformed(e);
		}
	    });
	jComboBoxAddPaths.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxAddPathsActionPerformed(e);
		}
	    });
	jListPaths.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    jListPathsShowPopup(e);
		}
		public void mouseReleased(MouseEvent e) {
		    jListPathsShowPopup(e);
		}
	    });
	jListPaths.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    jListPathsValueChanged(e);
		}
	    });
	jButtonCancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonCancelActionPerformed(e);
		}
	    });
	jButtonApply.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonApplyActionPerformed(e);
		}
	    });
	jButtonOK.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonOKActionPerformed(e);
		}
	    });
	
	setTitle("OutputModule Editor");
	setContentPane(initComponents());
    }
    

    //
    // member functions
    //

    /** set the output module to be edited */
    public void setOutputModule(String label)
    {
	outputModule = config.module(label);
	lmPaths.removeAllElements();
	cbmPaths.removeAllElements();
	cbmPaths.addElement("");
	
	if (outputModule==null) {
	    jButtonApply.setEnabled(false);
	    jButtonOK.setEnabled(false);
	    return;
	}
	
	PSetParameter psetSelectEvents =
	    (PSetParameter)outputModule.parameter("SelectEvents","PSet");
	VStringParameter vsSelectEvents =
	    (VStringParameter)psetSelectEvents.parameter("SelectEvents");
	
	if (vsSelectEvents==null||vsSelectEvents.vectorSize()==0) {
	    Iterator<Path> itP=config.pathIterator();
	    while (itP.hasNext()) {
		Path p = itP.next();
		if (!p.isEndPath()) lmPaths.addElement(p.name());
	    }
	}
	else {
	    for (int i=0;i<vsSelectEvents.vectorSize();i++) {
		String pathName = (String)vsSelectEvents.value(i);
		Path   path     = config.path(pathName);
		if (path!=null&&!path.isEndPath()) lmPaths.addElement(pathName);
		else System.err.println("invalid path '"+pathName+"'");
	    }
	    Iterator<Path> itP = config.pathIterator();
	    while (itP.hasNext()) {
		Path path = itP.next();
		if (path.isEndPath()||lmPaths.contains(path.name())) continue;
		cbmPaths.addElement(path.name());
	    }
	}
	sortPathComboBox();
	sortPathList();
	    
	products.clear();
	updateProducts();
	parseOutputCommands();
	updateProducts();
	
	jButtonApply.setEnabled(true);
	jButtonOK.setEnabled(true);
    }
    

	
    
    //
    // private member functions
    //
    
    // listener callbacks
    private void jComboBoxOutputModuleActionPerformed(ActionEvent e)
    {
	JComboBox jComboBox  = (JComboBox)e.getSource();
	String    name       = (String)jComboBox.getSelectedItem();
	setOutputModule(name);
    }
    private void jComboBoxAddPathsActionPerformed(ActionEvent e)
    {
	String pathName = (String)cbmPaths.getSelectedItem();
	if (pathName==null||pathName.equals("")) return;
	
	if (cbmPaths.getIndexOf(pathName)>0) {
	    addPath(pathName);
	}
	else {
	    boolean beginsWith = true;
	    if (pathName.startsWith("*")) {
		beginsWith = false; pathName = pathName.substring(1);
	    }

	    ArrayList<String> toBeAdded = new ArrayList<String>();
	    Iterator<Path> itP = config.pathIterator();
	    while (itP.hasNext()) {
		Path path = itP.next();
		if (lmPaths.contains(path.name())) continue;
		int index = path.name().indexOf(pathName);
		if ((beginsWith&&index==0)||(!beginsWith&&index>=0))
		    toBeAdded.add(path.name());
	    }
	    if (toBeAdded.size()!=0) {
		StringBuffer msg = new StringBuffer();
		msg.append("Add the following Paths?").append("\n");
		for (int i=0;i<toBeAdded.size();i++) {
		    msg.append(" ").append(toBeAdded.get(i));
		    if ((i+1)%3==0) msg.append("\n");
		    
		}
		if (JOptionPane.YES_OPTION==JOptionPane
		    .showConfirmDialog(null,msg,"Add Path(s)",
				       JOptionPane.YES_NO_OPTION)) {
		    for (int i=0;i<toBeAdded.size();i++) addPath(toBeAdded.get(i));
		}
	    }
	}
	cbmPaths.setSelectedItem("");
    }
    private void jListPathsShowPopup(MouseEvent e)
    {
	if (!e.isPopupTrigger()) return;
	int index = jListPaths.locationToIndex(e.getPoint());
	jListPaths.addSelectionInterval(index,index);
	
	JPopupMenu popup = new JPopupMenu();
	JMenuItem menuItem = new JMenuItem("Remove");
	StringBuffer actionCommand = new StringBuffer();
	for (int i=lsmPaths.getMinSelectionIndex();
	     i<=lsmPaths.getMaxSelectionIndex();i++) {
	    if (lsmPaths.isSelectedIndex(i)) {
		actionCommand.append((String)lmPaths.elementAt(i)).append(" ");
	    }
	}
	menuItem.setActionCommand(actionCommand.toString());
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JMenuItem source = (JMenuItem)e.getSource();
		    String    action = source.getActionCommand();
		    String    paths[]=action.split(" "); 
		    for (int i=0;i<paths.length;i++) removePath(paths[i]);
		}
	    });
	popup.add(menuItem);
	popup.show(e.getComponent(),e.getX(),e.getY());
    }
    private void jListPathsValueChanged(ListSelectionEvent e)
    {
	if (!e.getValueIsAdjusting()) updateProducts();
    }
    private void jButtonCancelActionPerformed(ActionEvent e)
    {
	setVisible(false);
    }
    private void jButtonApplyActionPerformed(ActionEvent e)
    {
	setParameters();
    }
    private void jButtonOKActionPerformed(ActionEvent e)
    {
	setParameters();
	setVisible(false);
    }
    
    /** sort path list */
    private void sortPathList()
    {
	ArrayList<String> sortedPaths = new ArrayList<String>();
	for (int i=0;i<lmPaths.getSize();i++)
	    sortedPaths.add((String)lmPaths.getElementAt(i));
	Collections.sort(sortedPaths);
	lmPaths.removeAllElements();
	for (int i=0;i<sortedPaths.size();i++)
	    lmPaths.addElement(sortedPaths.get(i));
    }
    
    /** sort add path combo box */
    private void sortPathComboBox()
    {
	ArrayList<String> sortedPaths = new ArrayList<String>();
	for (int i=1;i<cbmPaths.getSize();i++)
	    sortedPaths.add((String)cbmPaths.getElementAt(i));
	Collections.sort(sortedPaths);
	cbmPaths.removeAllElements();
	cbmPaths.addElement("");
	for (int i=0;i<sortedPaths.size();i++)
	    cbmPaths.addElement(sortedPaths.get(i));
    }
    
    /** add a path to the list, remove it from the combo box */
    private void addPath(String pathName)
    {
	lmPaths.addElement(pathName);
	cbmPaths.removeElement(pathName);
	sortPathList();
	updateProducts();
    }
    
    /** remove a path from the list, add it to the combo box! */
    private void removePath(String pathName)
    {
	lmPaths.removeElement(pathName);
	cbmPaths.addElement(pathName);
	sortPathComboBox();
	updateProducts();
    }
    
    /** update the products when paths are added/removed */
    private void updateProducts()
    {
	HashMap<String,Product> oldproducts =
	    new HashMap<String,Product>(products);
	products.clear();
	HashMap<String,Product> selectedProducts = new HashMap<String,Product>();
	for (int iPath=0;iPath<lmPaths.getSize();iPath++) {
	    Path path = config.path((String)lmPaths.getElementAt(iPath));
	    boolean isSelected =
		lsmPaths.isSelectionEmpty()||
		lsmPaths.isSelectedIndex(lmPaths.indexOf(path.name()));
	    Iterator<ModuleInstance> itM = path.moduleIterator();
	    while (itM.hasNext()) {
		ModuleInstance module = itM.next();
		String         moduleName = module.name();
		String         moduleType = module.template().type();
		if (!moduleType.equals("EDProducer")&&
		    !moduleType.equals("HLTFilter"))  continue;
		if (products.containsKey(moduleName)) continue;
		if (oldproducts.containsKey(moduleName)) {
		    Product prod = oldproducts.get(moduleName);
		    products.put(moduleName,prod);
		    if (isSelected) selectedProducts.put(moduleName,prod);
		}
		else {
		    Product prod = new Product(moduleName);
		    products.put(moduleName,prod);
		    if (isSelected) selectedProducts.put(moduleName,prod);
		}
	    }
	}
	tmProducts.update(selectedProducts);
    }
    
    /** parse 'outputCommands' parameter and set 'keep'/'instances' values */
    private void parseOutputCommands()
    {
	jCheckBoxKeepRaw.setSelected(false);

	VStringParameter vOutputCommands =
	    (VStringParameter)outputModule.parameter("outputCommands","vstring");
	for (int i=0;i<vOutputCommands.vectorSize();i++) {
	    String a[] = ((String)vOutputCommands.value(i)).split(" ");
	    if (!a[0].equals("keep")) continue;
	    String b[] = a[1].split("_");
	    if (b.length!=4) continue;

	    String className    = b[0];
	    String moduleName   = b[1];
	    String instanceName = b[2];
	    //String processName  = b[3];
	    
	    if (className.equals("FEDRawDataCollection")) {
		jCheckBoxKeepRaw.setSelected(true);
		continue;
	    }
	    
	    Product prod = products.get(moduleName);
	    if (prod==null) {
		System.err.println("unknown product '"+moduleName+"'");
	    }
	    else {
		prod.keep = true;
		if (!className.equals("*")) {
		    if (prod.classes.equals("")) prod.classes = className;
		    else if (prod.classes.indexOf(className)<0)
			prod.classes += ","+className;
		}
		if (!instanceName.equals("*")) {
		    if (prod.instances.equals("")) prod.instances = instanceName;
		    else if (prod.instances.indexOf(instanceName)<0)
			prod.instances += ","+instanceName;
		}
	    }
	}
    }
    
    /** set the output module parameters according to the list/table */
    private void setParameters()
    {
	StringBuffer outputCommandsAsString = new StringBuffer();
	outputCommandsAsString.append("drop *");

	if (jCheckBoxKeepRaw.isSelected())
	    outputCommandsAsString
		.append(",").append("keep FEDRawDataCollection_*_*_*");

	Iterator<Product> itP = products.values().iterator();
	while (itP.hasNext()) {
	    Product p = itP.next();
	    if (!p.keep) continue;
	    if (p.classes.length()>0) {
		String classNames[] = p.classes.split(",");
		for (int i=0;i<classNames.length;i++)
		    outputCommandsAsString
			.append(",keep ").append(classNames[i]).append("_")
			.append(p.label).append("_*_*");
	    }
	    if (p.instances.length()>0) {
		String instanceNames[] = p.instances.split(",");
		for (int i=0;i<instanceNames.length;i++)
		    outputCommandsAsString
			.append(",keep ").append("*_").append(p.label).append("_")
			.append(instanceNames[i]).append("_*");
	    }
	    if (p.classes.length()==0&&p.instances.length()==0)
		outputCommandsAsString
		    .append(",keep ").append("*_").append(p.label).append("_*_*");
	}
	outputModule.updateParameter("outputCommands","vstring",
				     outputCommandsAsString.toString());
	
	StringBuffer selectEventsAsString = new StringBuffer();
	for (int i=0;i<lmPaths.getSize();i++) {
	    if (i>0) selectEventsAsString.append(",");
	    selectEventsAsString.append(lmPaths.getElementAt(i));
	}
	outputModule.updateParameter("SelectEvents::SelectEvents","vstring",
				     selectEventsAsString.toString());
    }
    

    
    /** init GUI components */
    private JPanel initComponents()
    {
	JPanel jPanel = new JPanel();
	
        JLabel      jLabel1 = new javax.swing.JLabel();
        JButton     jLabel3 = new javax.swing.JButton();
        JLabel      jLabel4 = new javax.swing.JLabel();
        JLabel      jLabel5 = new javax.swing.JLabel();
        JLabel      jLabel6 = new javax.swing.JLabel();

	JSplitPane  jSplitPane = new javax.swing.JSplitPane();
        JPanel      jPanelPaths = new javax.swing.JPanel();
        JPanel      jPanelModules = new javax.swing.JPanel();
        JScrollPane jScrollPanePaths = new javax.swing.JScrollPane();
        JScrollPane jScrollPaneModules = new javax.swing.JScrollPane();

        jSplitPane.setDividerLocation(280);
        jSplitPane.setResizeWeight(0.5);

        jComboBoxAddPaths.setEditable(true);
        jScrollPanePaths.setViewportView(jListPaths);

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 16));
        jLabel5.setText("Selected Paths:");

        jCheckBoxKeepRaw.setFont(new java.awt.Font("Dialog", 1, 12));
        jCheckBoxKeepRaw.setText("keep FEDRawDataCollection");
	

        org.jdesktop.layout.GroupLayout jPanelPathsLayout = new org.jdesktop.layout.GroupLayout(jPanelPaths);
        jPanelPaths.setLayout(jPanelPathsLayout);
        jPanelPathsLayout.setHorizontalGroup(
					     jPanelPathsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					     .add(jPanelPathsLayout.createSequentialGroup()
						  .add(jLabel5)
						  .addContainerGap())
					     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelPathsLayout.createSequentialGroup()
						  .addContainerGap()
						  .add(jComboBoxAddPaths, 0, 255, Short.MAX_VALUE)
						  .addContainerGap())
					     .add(jScrollPanePaths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
					     );
        jPanelPathsLayout.setVerticalGroup(
					   jPanelPathsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					   .add(jPanelPathsLayout.createSequentialGroup()
						.add(12, 12, 12)
						.add(jLabel5)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(jComboBoxAddPaths, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(jScrollPanePaths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE))
					   );

        jSplitPane.setLeftComponent(jPanelPaths);

	jLabel3.setIcon(new ImageIcon(getClass().getResource("/CancelSearchIcon.png")));
        jLabel4.setFont(new java.awt.Font("Dialog", 1, 12));
        jLabel4.setText("Search:");

        jScrollPaneModules.setBackground(new java.awt.Color(255, 255, 255));
	jScrollPaneModules.setViewportView(jTableProducts);

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 16));
        jLabel6.setText("Products:");

        org.jdesktop.layout.GroupLayout jPanelModulesLayout = new org.jdesktop.layout.GroupLayout(jPanelModules);
        jPanelModules.setLayout(jPanelModulesLayout);
        jPanelModulesLayout.setHorizontalGroup(
					       jPanelModulesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					       .add(jLabel6)
					       .add(jPanelModulesLayout.createSequentialGroup()
						    .add(6, 6, 6)
						    .add(jLabel4)
						    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
						    .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
						    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						    .add(40, 40, 40)
						    .add(jCheckBoxKeepRaw))
					       .add(jScrollPaneModules, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
					       );
        jPanelModulesLayout.setVerticalGroup(
					     jPanelModulesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					     .add(jPanelModulesLayout.createSequentialGroup()
						  .addContainerGap()
						  .add(jLabel6)
						  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						  .add(jPanelModulesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
						       .add(jLabel4)
						       .add(jCheckBoxKeepRaw)
						       .add(jLabel3)
						       .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
						  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						  .add(jScrollPaneModules, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE))
					     );
	
	jSplitPane.setRightComponent(jPanelModules);
	
	jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
	jLabel1.setText("OutputModule:");
    
	jComboBoxOutputModule.setBackground(new java.awt.Color(255, 255, 255));
    
	jButtonCancel.setText("Cancel");
	jButtonOK.setText("OK");
	jButtonApply.setText("Apply");
    
	org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel);
	jPanel.setLayout(layout);
	layout.setHorizontalGroup(
				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				  .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
				       .addContainerGap()
				       .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
					    .add(org.jdesktop.layout.GroupLayout.LEADING, jSplitPane,
						 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
					    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
						 .add(jLabel1)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						 .add(jComboBoxOutputModule, 0, 656, Short.MAX_VALUE))
					    .add(layout.createSequentialGroup()
						 .add(jButtonCancel)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						 .add(jButtonApply)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						 .add(jButtonOK)))
				       .addContainerGap())
				  );
    
	layout.linkSize(new java.awt.Component[] {jButtonApply, jButtonCancel, jButtonOK}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

	layout.setVerticalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
				     .addContainerGap()
				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
					  .add(jLabel1)
					  .add(jComboBoxOutputModule, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
					       org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					       org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
				     .add(jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
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
// table model
//
class ProductTableModel extends AbstractTableModel
{
    /** all products in the configuration */
    private ArrayList<Product> products = new ArrayList<Product>();
    
    /** column names */
    private static String[] columnNames = {"keep","label","classes","instances"};
    
    
    
    /** update the table */
    public void update(HashMap<String,Product> map)
    {
	products.clear();
	Iterator<Product> it = map.values().iterator();
	while (it.hasNext()) products.add(it.next());
	fireTableDataChanged();
    }

    
    /** number of columns */
    public int getColumnCount() { return columnNames.length; }
    
    /** number of rows */
    public int getRowCount() { return products.size(); }

    /** get column name for colimn 'col' */
    public String getColumnName(int col) { return columnNames[col]; }
    
    /** get the value for row,col */
    public Object getValueAt(int row, int col)
    {
	Product product = products.get(row);
	switch (col) {
	case 0 : return product.keep;
	case 1 : return product.label;
	case 2 : return product.classes;
	case 3 : return product.instances;
	}
	return null;
    }
    
    /** get the class of the column 'c' */
    public Class getColumnClass(int c)
    {
	return getValueAt(0,c).getClass();
    }
    
    /** is a cell editable or not? */
    public boolean isCellEditable(int row, int col) { return (col!=1); }

    /** set the value of a table cell */
    public void setValueAt(Object value,int row, int col)
    {
	if (col==1) return;
	Product product = products.get(row);
	
	if      (col==0) product.keep      = (Boolean)value;
	else if (col==2) product.classes   = (String)value;
	else if (col==3) product.instances = (String)value;
    }
}


//
// product data class
//
class Product
{
    public Boolean keep = false;
    public String  label;
    public String  classes="";
    public String  instances="";
    public Product(String label) { this.label = label; }
}

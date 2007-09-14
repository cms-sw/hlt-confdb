package confdb.gui;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.gui.treetable.*;

import confdb.data.ParameterFactory;
import confdb.data.Parameter;
import confdb.data.ScalarParameter;
import confdb.data.VectorParameter;
import confdb.data.PSetParameter;
import confdb.data.VPSetParameter;
import confdb.data.TemplateFactory;
import confdb.data.Template;

import confdb.db.ConfDB;


/**
 * CreateTemplateDialog
 * --------------------
 * @author Philipp Schieferdecker
 *
 * Dialog to temporarily allow the creation of Service, EDSource,
 * ESSource, and Module Templates in the database.
 */
public class CreateTemplateDialog extends JDialog implements ActionListener
{
    //
    // member data
    //
    
    /** text field for the user to specify the name of the template */
    private JTextField textFieldTemplateName = null;
    
    /** text field for the cvs tag */
    private JTextField textFieldTemplateCvsTag = null;

    /** combo box to choose the release tag to associate this template with */
    private JComboBox comboBoxReleaseTag = null;
    private String[]  releaseTags = null;
    
    /** radio button to specify the type (service/module) */
    private ButtonGroup buttonGroupTemplateType = null;
    private JRadioButton edsourceButton = null;
    private JRadioButton essourceButton = null;
    private JRadioButton esmoduleButton = null;
    private JRadioButton serviceButton  = null;
    private JRadioButton moduleButton   = null;  
    private JRadioButton noneButton     = null;
    private static final String templateTypeService  = "Service";
    private static final String templateTypeEDSource = "EDSource";
    private static final String templateTypeESSource = "ESSource";
    private static final String templateTypeESModule = "ESModule";
    private static final String templateTypeModule   = "Module";
    private static final String templateTypeNone     = "None";

    /** combo box to pick module type */
    private JComboBox comboBoxModuleType = null;
    private static final String[] moduleTypeOptions =
    {
	"","EDProducer","EDFilter","EDAnalyzer","HLTProducer","HLTFilter"
    };
    
    /** parameter list */
    private ArrayList<Parameter> parameterList = null;
    
    /** parameter table */
    private TreeTableTableModel tableModel     = null;
    private ParameterTreeModel  treeModel      = null;
    private TreeTable           parameterTable = null;
    
    /** text field to specify the name of a new parameter */
    private JTextField textFieldParameterName = null;
    
    /** combo box to pick a parameter type */
    private JComboBox comboBoxParameterType = null;
    private static final String[] parameterTypes = 
    {
	"",
	"int32", "uint32", "double", "string","bool","EventID","InputTag",
	"FileInPath","PSet",
	"vint32","vuint32","vdouble","vstring","VEventID","VInputTag","VPSet"
    };
    
    /** text field to enter value for new parameter */
    private JTextField textFieldParameterValue = null;
    
    /** button to ADD/CLEAR new parameter to the current template */
    private JButton buttonAddParameter = null;
    
    /** buttons to add template, clear current values, and finish dialog */
    private JButton buttonAddTemplate = null;
    private JButton buttonClear = null;
    private JButton buttonDone = null;
    
    /** button action commands */
    private static final String actionAddParameter = "AddParameter";
    private static final String actionAddTemplate = "AddTemplate";
    private static final String actionClear = "Clear";
    private static final String actionDone = "Done";
    
    /** text area for logging information */
    private JTextArea textAreaLog = null;
    
    /** reference to the database */
    private ConfDB database = null;
    
    
    //
    // construction
    //

    /** standard constructor */
    public CreateTemplateDialog(JFrame frame,ConfDB database)
    {
	super(frame,true);
	this.database = database;
	setTitle("Create Service/Module Templates");

	releaseTags = database.getReleaseTags();
	setContentPane(createContentPane());

    }

    //
    // member functions
    //
    
    /** create content pane */
    private JPanel createContentPane()
    {
	JPanel panel = new JPanel(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.fill=GridBagConstraints.BOTH;
	c.weightx = 1.0;
	
	//
	// Name&Type panel
	//
	JPanel nameAndTypePanel = new JPanel(new GridBagLayout());
	JPanel namePanel = new JPanel(new FlowLayout());
	JPanel typePanel = new JPanel(new FlowLayout());
	TitledBorder nameAndTypeBorder = 
	    BorderFactory.createTitledBorder("Template Name&Type");
	nameAndTypePanel.setBorder(nameAndTypeBorder);
	c.gridx=0; c.gridy=0; c.gridwidth=1;
	nameAndTypePanel.add(namePanel,c);
	c.gridx=0; c.gridy=1; c.gridwidth=1;
	nameAndTypePanel.add(typePanel,c);
	
	// template name text field
	textFieldTemplateName = new JTextField(10);
	textFieldTemplateCvsTag = new JTextField(6);
	comboBoxReleaseTag = new JComboBox(releaseTags);
	comboBoxReleaseTag.setBackground(Color.WHITE);
	namePanel.add(new JLabel("<html><b>Name:</b></html>"));
	namePanel.add(textFieldTemplateName);
	namePanel.add(new JLabel(" "));
	namePanel.add(new JLabel("<html><b>Tag:</b></html>"));
	namePanel.add(textFieldTemplateCvsTag);
	namePanel.add(new JLabel(" "));
	namePanel.add(new JLabel("<html><b>Release:</b></html>"));
	namePanel.add(comboBoxReleaseTag);
	
	// template type radio buttons
	serviceButton = new JRadioButton("Service");
	serviceButton.setMnemonic(KeyEvent.VK_S);
	serviceButton.addActionListener(this);
	serviceButton.setActionCommand(templateTypeService);
	serviceButton.setSelected(false);
	edsourceButton = new JRadioButton("EDSource");
	edsourceButton.setMnemonic(KeyEvent.VK_D);
	edsourceButton.addActionListener(this);
	edsourceButton.setActionCommand(templateTypeEDSource);
	edsourceButton.setSelected(false);
	essourceButton = new JRadioButton("ESSource");
	essourceButton.setMnemonic(KeyEvent.VK_E);
	essourceButton.addActionListener(this);
	essourceButton.setActionCommand(templateTypeESSource);
	essourceButton.setSelected(false);
	esmoduleButton = new JRadioButton("ESModule");
	esmoduleButton.setMnemonic(KeyEvent.VK_E);
	esmoduleButton.addActionListener(this);
	esmoduleButton.setActionCommand(templateTypeESModule);
	esmoduleButton.setSelected(false);
	moduleButton = new JRadioButton("Module");
	moduleButton.setMnemonic(KeyEvent.VK_M);
	moduleButton.addActionListener(this);
	moduleButton.setActionCommand(templateTypeModule);
	moduleButton.setSelected(false);
	noneButton = new JRadioButton("None");
	noneButton.addActionListener(this);
	noneButton.setActionCommand(templateTypeNone);
	noneButton.setSelected(true);
	buttonGroupTemplateType = new ButtonGroup();
	buttonGroupTemplateType.add(edsourceButton);
	buttonGroupTemplateType.add(essourceButton);
	buttonGroupTemplateType.add(esmoduleButton);
	buttonGroupTemplateType.add(serviceButton);
	buttonGroupTemplateType.add(moduleButton);
	buttonGroupTemplateType.add(noneButton);
	typePanel.add(new JLabel("<html><b>Type:</b></html>"));
	typePanel.add(serviceButton);
	typePanel.add(edsourceButton);
	typePanel.add(essourceButton);
	typePanel.add(moduleButton);
	
	// module type combo box
	comboBoxModuleType = new JComboBox(moduleTypeOptions);
	comboBoxModuleType.setBackground(Color.WHITE);
	comboBoxModuleType.setEnabled(false);
	typePanel.add(comboBoxModuleType);

	c.gridx=0; c.gridy=0; c.gridwidth=1;
	panel.add(nameAndTypePanel,c);

	//
	// parameter panel
	//
	JPanel parameterPanel = new JPanel(new GridBagLayout());
	TitledBorder parameterBorder = 
	    BorderFactory.createTitledBorder("Parameters");
	parameterPanel.setBorder(parameterBorder);
	
	// parameter list
	parameterList = new ArrayList<Parameter>();
	
	// parameter table
	treeModel      = new ParameterTreeModel();
	parameterTable = new TreeTable(treeModel);
	tableModel     = (TreeTableTableModel)parameterTable.getModel();
	parameterTable.setTreeCellRenderer(new ParameterTreeCellRenderer());
	JScrollPane spParameterTable=new JScrollPane(parameterTable);
	spParameterTable.setPreferredSize(new Dimension(300,100));
	c.gridx=0; c.gridy=0; c.gridwidth=1;
	parameterPanel.add(spParameterTable,c);
	
	// "add-parameter" panel
	JPanel addParPanel = new JPanel(new GridLayout(2,4));
	TitledBorder addParBorder = BorderFactory.createTitledBorder("Add Parameter");
	addParPanel.setBorder(addParBorder);
	addParPanel.add(new JLabel("Name"));
	addParPanel.add(new JLabel("Type"));
	addParPanel.add(new JLabel("Value"));
	addParPanel.add(new JLabel());
	
	// add parameter: parameter name
	textFieldParameterName = new JTextField(8);
	addParPanel.add(textFieldParameterName);
	
	// add parameter: parameter type
	comboBoxParameterType = new JComboBox(parameterTypes);
	comboBoxParameterType.setBackground(Color.WHITE);
	addParPanel.add(comboBoxParameterType);
	
	// add parameter: parameter value
	textFieldParameterValue = new JTextField(8);
	addParPanel.add(textFieldParameterValue);
	
	// add parameter: ADD button
	buttonAddParameter = new JButton("ADD");
	buttonAddParameter.addActionListener(this);
	buttonAddParameter.setActionCommand(actionAddParameter);
	buttonAddParameter.setMnemonic(KeyEvent.VK_A);
	addParPanel.add(buttonAddParameter);
	
	c.gridx=0; c.gridy=1; c.gridwidth=1;
	parameterPanel.add(addParPanel,c);

	c.gridx=0; c.gridy=1; c.gridwidth=1;
	panel.add(parameterPanel,c);
	
	
	//
	// add template panel
	//
	JPanel buttonPanel = new JPanel(new FlowLayout());
	
	// add-template button
	buttonAddTemplate = new JButton("Create Template");
	buttonAddTemplate.addActionListener(this);
	buttonAddTemplate.setActionCommand(actionAddTemplate);
	buttonAddTemplate.setMnemonic(KeyEvent.VK_A);
	buttonPanel.add(buttonAddTemplate);
	
	// clear button
	buttonClear = new JButton("Clear");
	buttonClear.addActionListener(this);
	buttonClear.setActionCommand(actionClear);
	buttonClear.setMnemonic(KeyEvent.VK_C);
	buttonPanel.add(buttonClear);

	// done button
	buttonDone = new JButton("Done");
	buttonDone.addActionListener(this);
	buttonDone.setActionCommand(actionDone);
	buttonDone.setMnemonic(KeyEvent.VK_D);
	buttonPanel.add(buttonDone);
	
	c.gridx=0; c.gridy=2; c.gridwidth=1;
	panel.add(buttonPanel,c);
	
	//
	// add text area for logging
	//
	textAreaLog = new JTextArea(10,20);
	textAreaLog.setEditable(false);
	textAreaLog.setLineWrap(true);
	textAreaLog.setWrapStyleWord(true);
	JScrollPane scrollPaneLog = new JScrollPane(textAreaLog);
	scrollPaneLog.setBorder(BorderFactory.createTitledBorder("Messages"));
	c.gridx=0; c.gridy=3; c.gridwidth=1;
	panel.add(scrollPaneLog,c);
	
	return panel;
    }

    /** ActionListener inteface implementation */
    public void actionPerformed(ActionEvent e)
    {
	// handle template type buttons
	if (templateTypeEDSource.equals(e.getActionCommand())||
	    templateTypeESSource.equals(e.getActionCommand())||
	    templateTypeESModule.equals(e.getActionCommand())||
	    templateTypeService.equals(e.getActionCommand())||
	    templateTypeNone.equals(e.getActionCommand())) {
	    comboBoxModuleType.setSelectedIndex(0);
	    comboBoxModuleType.setEnabled(false);
	    return;
	}
	if (templateTypeModule.equals(e.getActionCommand())) {
	    comboBoxModuleType.setEnabled(true);
	    return;
	}
	
	// 'add template' button pressed
	if(actionAddTemplate.equals(e.getActionCommand())) {
	    String name = textFieldTemplateName.getText();
	    if (name.length()==0) {
		textAreaLog.append("ERROR:You must specify the template name.\n");
		return;
	    }
	    
	    String cvsTag = textFieldTemplateCvsTag.getText();
	    if (!isValidCvsTag(cvsTag)) {
		textAreaLog.append("ERROR: specify template cvs tag in format " +
				   "VXX-YY-ZZ.\n");
		return;
	    }
	    
	    String releaseTag = (String)comboBoxReleaseTag.getSelectedItem();
	    if (releaseTag.equals("")) {
		textAreaLog.append("ERROR: You must choose a release.");
		return;
	    }
	    
	    String type = buttonGroupTemplateType.getSelection().getActionCommand();
	    if (type.equals(templateTypeNone)) {
		textAreaLog.append("ERROR: You must specify the template type.\n");
		return;
	    }
	    if (type.equals(templateTypeModule)) {
		type = (String)comboBoxModuleType.getSelectedItem();
	    }
	    
	    Template template = TemplateFactory
		.create(type,name,cvsTag,-1,parameterList);
	    
	    if (database.insertTemplate(template, releaseTag)) {
		textAreaLog.append("--> CREATED "+type+" template '"+name+"'" +
				   " cvstag = '" + cvsTag + "'" +
				   " release = '" + releaseTag + "'\n");
		for (Parameter p : parameterList) {
		    textAreaLog.append("   -> parameter '"+p.name()+"' type='"+
				       p.type()+"' ");
		    if (p instanceof VectorParameter) {
			VectorParameter vp = (VectorParameter)p;
			textAreaLog.append("values= ");
			for (int i=0;i<vp.vectorSize();i++) {
			    textAreaLog.append("'"+vp.value(i).toString()+"'");
			    if (i<vp.vectorSize()-1) textAreaLog.append(", ");
			}
			textAreaLog.append("\n");
		    }
		    else if (p instanceof ScalarParameter) {
			ScalarParameter sp = (ScalarParameter)p;
			textAreaLog.append("value='"+sp.value()+"'\n");
		    }
		}
		textAreaLog.append("\n");
	    }
	    else {
		textAreaLog.append("FAILED to create "+type+" template '"+
				   name+"'\n");
	    }
	    parameterList.clear();
	    treeModel.setParameters(parameterList);
	    textFieldTemplateName.setText("");
	    textFieldTemplateCvsTag.setText("");
	    comboBoxReleaseTag.setSelectedIndex(0);
	    noneButton.setSelected(true);
	    textFieldTemplateName.requestFocusInWindow();
	    return;
	}
	
	// 'clear' button pressed
	if (actionClear.equals(e.getActionCommand())) {
	    parameterList.clear();
	    treeModel.setParameters(parameterList);
	    textFieldTemplateName.setText("");
	    textFieldTemplateCvsTag.setText("");
	    comboBoxReleaseTag.setSelectedIndex(0);
	    noneButton.setSelected(true);
	    
	    // parameter fields;
	    textFieldParameterName.setText("");
	    comboBoxParameterType.setSelectedIndex(0);
	    textFieldParameterValue.setText("");
	    return;
	}

	// 'done' button pressed
	if (actionDone.equals(e.getActionCommand())) {
	    setVisible(false);
	    return;
	}

	// 'add parameter' button pressed
	if (actionAddParameter.equals(e.getActionCommand())) {
	    
	    String name  = textFieldParameterName.getText();
	    String type  = (String)comboBoxParameterType.getSelectedItem();
	    String value = textFieldParameterValue.getText();
	    
	    textFieldParameterName.setText("");
	    comboBoxParameterType.setSelectedIndex(0);
	    textFieldParameterValue.setText("");
	    if (name.length()==0||type.length()==0) {
		textAreaLog.append("FAILED to add parameter '"+name+"'\n");
		return;
	    }
	    
	    boolean parameterExists=false;
	    Iterator it = parameterList.iterator();
	    while (it.hasNext()) {
		Parameter p=(Parameter)it.next();
		if (name.equals(p.name())) parameterExists=true;
	    }
	    
	    if (!parameterExists) {
		TreePath treePath = parameterTable.getTree().getSelectionPath();
		if (treePath!=null) {
		    Object o = treePath.getLastPathComponent();
		    if (o instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter)o;
			pset.addParameter(ParameterFactory
					  .create(type,name,value,true,true));
		    }
		    else if (o instanceof VPSetParameter && type.equals("PSet")) {
			VPSetParameter vpset = (VPSetParameter)o;
			PSetParameter  pset  = (PSetParameter)ParameterFactory
			    .create(type,name,value,true,true);
			    vpset.addParameterSet(pset);
		    }
		    else {
			parameterList.add(ParameterFactory
					  .create(type,name,value,true,true));
		    }
		}
		else {
		    parameterList.add(ParameterFactory
				      .create(type,name,value,true,true));
		}
		treeModel.setParameters(parameterList);
		parameterTable.expandTree();
	    }
	    textFieldParameterName.requestFocusInWindow();
	}
    }
    
    /** validate cvs tag entry */
    private boolean isValidCvsTag(String cvsTag)
    {
	if (cvsTag.length()!=9) return false;
	if (!cvsTag.startsWith("V")) return false;
	String tmp = cvsTag.substring(1,cvsTag.length());
	String [] tmp2 = tmp.split("-");
	if (tmp2.length!=3) return false;
	for (int i=0;i<3;i++) {
	    try { new Integer(tmp2[i]); }
	    catch (NumberFormatException e) { return false; }
	}
	return true;
    }
    
}

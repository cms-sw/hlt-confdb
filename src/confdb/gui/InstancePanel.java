package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;

import org.jdesktop.layout.*;

import confdb.data.*;

import confdb.converter.ConverterFactory;
import confdb.converter.Converter;

import confdb.gui.treetable.*;


/**
 * InstancePanel
 * -------------
 * @author Philipp Schieferdecker
 *
 * Display the information about the selected instance in the tree.
 * This is the only place where the user can edit the parameters of
 * this instance.
 */
public class InstancePanel extends JPanel implements TreeSelectionListener,
						     TableModelListener
{
    //
    // member data
    //

    /** the tree model of the current configuration */
    private ConfigurationTreeModel configurationTreeModel = null;

    /** parent frame */
    private JFrame frame = null;
    
    /** list of parameters, for the table */
    private ArrayList<Parameter> parameterList = new ArrayList<Parameter>();
    
    /** the table and tree model to display parameter list */
    private ParameterTreeModel  treeModel  = null;
    private TreeTableTableModel tableModel = null;
    
    /** GUI components */
    private JPanel      jPanelTop          = new JPanel(new CardLayout());
    private JLabel      jLabelType         = new JLabel();
    private JTextField  jTextFieldType     = new JTextField();
    private JTextField  jTextFieldLabel    = new JTextField();
    private JTextField  jTextFieldCvsTag   = new JTextField();
    private TreeTable   jTreeTableParameters   = null;
    private JEditorPane jEditorPaneSnippet = new JEditorPane();


    /** the current instance, to redisplay upon change */
    private Object currentObject = null;
    
    /** converter, to display instance configuration snippets */
    private Converter converter = null;

    
    //
    // construction
    //
    
    /** standard constructor */
    public InstancePanel(JFrame frame)
    {
	this.frame = frame;
	
	treeModel            = new ParameterTreeModel();
	jTreeTableParameters = new TreeTable(treeModel);
	tableModel           = (TreeTableTableModel)jTreeTableParameters.getModel();
	tableModel.addTableModelListener(this);
	jTreeTableParameters.setTreeCellRenderer(new ParameterTreeCellRenderer());
	jTreeTableParameters.getColumnModel().getColumn(0).setPreferredWidth(120);
	jTreeTableParameters.getColumnModel().getColumn(1).setPreferredWidth(90);
	jTreeTableParameters.getColumnModel().getColumn(2).setPreferredWidth(180);
	jTreeTableParameters.getColumnModel().getColumn(3).setPreferredWidth(30);
	jTreeTableParameters.getColumnModel().getColumn(4).setPreferredWidth(30);

	jTreeTableParameters
	    .addMouseListener(new ParameterTableMouseListener(frame,
							      jTreeTableParameters,
							      tableModel,
							      treeModel));
	
	initComponents();

	ConverterFactory factory = ConverterFactory.getFactory("default");
	try { converter = factory.getConverter("ASCII"); }
	catch (Exception e){ e.printStackTrace(); }
	
	clear();
    }
    
    
    //
    // member functions
    //

    /** add a TableModelListener */
    public void addTableModelListener(TableModelListener l)
    {
	tableModel.addTableModelListener(l);
    }

    /** set the ConfigurationTreeModel */
    public void setConfigurationTreeModel(ConfigurationTreeModel model)
    {
	this.configurationTreeModel = model;
    }
    
    /** set instance to be displayed */
    public void displayInstance(Instance instance)
    {
	jTextFieldType.setText(instance.template().name());
	jLabelType.setText(instance.template().type()+":");
	jTextFieldLabel.setText(instance.name());
	jTextFieldCvsTag.setText(instance.template().cvsTag());
	parameterList.clear();
	for (int i=0;i<instance.parameterCount();i++)
	    parameterList.add(instance.parameter(i));
	currentObject = instance;
	treeModel.setParameters(parameterList);
	jTreeTableParameters.expandTree();
    }
    
    /** set instance to be displayed */
    public void displayGlobalPSets()
    {
	clear();
	parameterList.clear();
	Configuration config = (Configuration)configurationTreeModel.getRoot();
	if (null==config) return;
	
	for (int i=0;i<config.psetCount();i++) parameterList.add(config.pset(i));
	currentObject = config;
	treeModel.setParameters(parameterList);
	jTreeTableParameters.expandTree();
    }
    
    /** clear all fields */
    public void clear()
    {
	jLabelType.setText("Type:");
	jTextFieldLabel.setText("");
	jTextFieldType.setText("");
	jTextFieldCvsTag.setText("");
	parameterList.clear();
	treeModel.setParameters(parameterList);
	currentObject = null;
    }
    
    /** TreeSelectionListener: valueChanged() */
    public void valueChanged(TreeSelectionEvent e)
    {
	TreePath treePath=e.getNewLeadSelectionPath();if(treePath==null)return;
	Object   node=treePath.getLastPathComponent();if(node==null){clear();return;}

	CardLayout cardLayout = (CardLayout)(jPanelTop.getLayout());
	cardLayout.show(jPanelTop,"InstancePanel");
	
	while (node instanceof Parameter) {
	    Parameter p = (Parameter)node;
	    node = p.parent();
	}
	
	if (node instanceof Instance) {
	    Instance instance = (Instance)node;
	    displayInstance(instance);
	}
	else if (node instanceof ModuleReference) {
	    ModuleReference reference = (ModuleReference)node;
	    ModuleInstance  instance = (ModuleInstance)reference.parent();
	    displayInstance(instance);
	}
	else if (node == null ||
		 ((node instanceof StringBuffer)&&
		  node.toString().startsWith("<html>PSets"))
		 ) {
	    displayGlobalPSets();
	    cardLayout.show(jPanelTop,"PSetPanel");
	}
	else {
	    clear();
	}
    }
    
    /** TableModelListener: tableChanged() */
    public void tableChanged(TableModelEvent e)
    {
	if (currentObject==null) {
	    jEditorPaneSnippet.setText("");
	    return;
	}

	String configAsString = null;
	
	if (currentObject instanceof Instance) {
	    if (currentObject instanceof EDSourceInstance) {
		EDSourceInstance edsource = (EDSourceInstance)currentObject;
		configAsString =converter.getEDSourceWriter().toString(edsource,
								       converter);
	    }
	    if (currentObject instanceof ESSourceInstance) {
		ESSourceInstance essource = (ESSourceInstance)currentObject;
		configAsString = converter.getESSourceWriter().toString(essource,
									converter);
	    }
	    if (currentObject instanceof ESModuleInstance) {
		ESModuleInstance esmodule = (ESModuleInstance)currentObject;
		configAsString = converter.getESModuleWriter().toString(esmodule,
									converter);
	    }
	    if (currentObject instanceof ServiceInstance) {
		ServiceInstance service = (ServiceInstance)currentObject;
		configAsString =converter.getServiceWriter().toString(service,
								     converter);
	    }
	    if (currentObject instanceof ModuleInstance) {
		ModuleInstance module = (ModuleInstance)currentObject;
		configAsString =converter.getModuleWriter().toString(module);
	    }
	}
	else if (currentObject instanceof Configuration) {
	    configAsString = new String();
	    Configuration config = (Configuration)currentObject;
	    for (int i=0;i<config.psetCount();i++) {
		PSetParameter pset = config.pset(i);
		configAsString +=
		    converter.getParameterWriter().toString(pset,converter,"")+"\n";
	    }
	}
	jEditorPaneSnippet.setText(configAsString);	
    }

    
    //
    // private member functions
    //
    
    /** init GUI components [generated by NetBeans] */
    private void initComponents() {
        JPanel      jPanelInstance        = new JPanel();
        JPanel      jPanelGlobalPSets     = new JPanel();
        JLabel      jLabel2               = new JLabel();
        JLabel      jLabel3               = new JLabel();
        JScrollPane jScrollPaneParameters = new JScrollPane();
        JScrollPane jScrollPaneSnippet    = new JScrollPane();

	jPanelTop.add(jPanelInstance,"InstancePanel");
	jPanelTop.add(jPanelGlobalPSets,"PSetPanel");

        jPanelInstance
	    .setBorder(BorderFactory
		       .createTitledBorder(null,
					   "Selected Instance",
					   TitledBorder.DEFAULT_JUSTIFICATION,
					   TitledBorder.DEFAULT_POSITION,
					   new Font("Dialog", 1, 12)));
        jPanelGlobalPSets
	    .setBorder(BorderFactory
		       .createTitledBorder(null,
					   "Global PSets",
					   TitledBorder.DEFAULT_JUSTIFICATION,
					   TitledBorder.DEFAULT_POSITION,
					   new Font("Dialog", 1, 12)));
        jPanelInstance.setFont(new Font("Dialog", 1, 12));

        jTextFieldType.setBackground(new java.awt.Color(255, 255, 255));
        jTextFieldType.setEditable(false);
        jTextFieldType.setBorder(BorderFactory.createBevelBorder(BevelBorder
								 .LOWERED));
        jTextFieldLabel.setBackground(new java.awt.Color(255, 255, 255));
        jTextFieldLabel.setEditable(false);
        jTextFieldLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder
								  .LOWERED));
        jTextFieldCvsTag.setBackground(new java.awt.Color(255, 255, 255));
        jTextFieldCvsTag.setEditable(false);
        jTextFieldCvsTag.setBorder(BorderFactory.createBevelBorder(BevelBorder
								   .LOWERED));
        jLabelType.setText("Type:");
        jLabel2.setText("Label:");
        jLabel3.setText("CVS Tag:");

        GroupLayout jPanelInstanceLayout = new GroupLayout(jPanelInstance);
        jPanelInstance.setLayout(jPanelInstanceLayout);
        jPanelInstanceLayout.setHorizontalGroup(
            jPanelInstanceLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelInstanceLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelInstanceLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(jTextFieldType,
			 GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                    .add(jLabelType))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelInstanceLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(jTextFieldLabel,
			 GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .add(jLabel2))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelInstanceLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jTextFieldCvsTag,
			 GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelInstanceLayout.setVerticalGroup(
            jPanelInstanceLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelInstanceLayout.createSequentialGroup()
                .add(jPanelInstanceLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jLabel3)
                    .add(jLabelType))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelInstanceLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jTextFieldType,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldLabel,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldCvsTag,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jScrollPaneParameters
	    .setBorder(BorderFactory
		       .createTitledBorder(null, "Parameters",
					   TitledBorder.DEFAULT_JUSTIFICATION,
					   TitledBorder.DEFAULT_POSITION,
					   new Font("Dialog", 1, 12)));
        jScrollPaneParameters.setViewportView(jTreeTableParameters);

        jScrollPaneSnippet
	    .setBorder(BorderFactory
		       .createTitledBorder(null, "Configuration Snippet",
					   TitledBorder.DEFAULT_JUSTIFICATION,
					   TitledBorder.DEFAULT_POSITION,
					   new java.awt.Font("Dialog", 1, 12)));
        jEditorPaneSnippet.setEditable(false);
        jScrollPaneSnippet.setViewportView(jEditorPaneSnippet);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(jScrollPaneSnippet,
		 GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
            .add(jScrollPaneParameters,
		 GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
	    .add(jPanelTop,
		 GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
		 .add(jPanelTop,
		      GroupLayout.PREFERRED_SIZE,
		      GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		 .addPreferredGap(LayoutStyle.RELATED)
		 .add(jScrollPaneParameters,
		      GroupLayout.PREFERRED_SIZE, 237, GroupLayout.PREFERRED_SIZE)
		 .addPreferredGap(LayoutStyle.RELATED)
		 .add(jScrollPaneSnippet,
		      GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
        );
    }
    

}

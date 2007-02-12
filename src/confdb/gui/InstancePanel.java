package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;

import confdb.data.*;

import confdb.gui.treetable.*;


/**
 * InstancePanel
 * -------------
 * @author Philipp Schieferdecker
 *
 * Display the information about the selected instance in the tree.
 * This is the only place where the user can edit the information about
 * this instance.
 */
public class InstancePanel extends JPanel implements TreeSelectionListener,
						     TableModelListener
{
    //
    // member data
    //
    
    /** text field shoing the instance name */
    private JLabel labelName = null;
    private JLabel valueName = null;
    
    /** text field shoing the template name */
    private JLabel labelType = null;
    private JLabel valueType = null;

    /** text field shoing the template cvs tag */
    private JLabel labelCvsTag = null;
    private JLabel valueCvsTag = null;
    
    /** list of parameters, for the table */
    private ArrayList<Parameter> parameterList = null;
    
    /** the table and tree model to display parameter list */
    private ParameterTreeModel  treeModel  = null;
    private TreeTableTableModel tableModel = null;
    
    /** the table to display the parameter, their types, and values */
    private TreeTable parameterTable = null;

    /** text area for configuration snippet */
    private JEditorPane editorPaneSnippet = null;

    /** the current instance, to redisplay upon change */
    private Instance currentInstance = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public InstancePanel(Dimension size)
    {
	super(new GridBagLayout());
	setPreferredSize(size);
	
	Dimension dimTop = new Dimension(size.width,(int)(size.height*0.1));
	Dimension dimPar = new Dimension(size.width,(int)(size.height*0.45));
	Dimension dimSnp = new Dimension(size.width,(int)(size.height*0.45));

	parameterList  = new ArrayList<Parameter>();
	treeModel      = new ParameterTreeModel();
	parameterTable = new TreeTable(treeModel);
	tableModel     = (TreeTableTableModel)parameterTable.getModel();
	tableModel.addTableModelListener(this);
	parameterTable.setTreeCellRenderer(new ParameterTreeCellRenderer());
	
	parameterTable.getColumnModel().getColumn(0).setPreferredWidth(100);
	parameterTable.getColumnModel().getColumn(1).setPreferredWidth(100);
	parameterTable.getColumnModel().getColumn(2).setPreferredWidth(150);
	parameterTable.getColumnModel().getColumn(3).setPreferredWidth(50);
	parameterTable.getColumnModel().getColumn(4).setPreferredWidth(50);

	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 0.5;
	c.weighty = 0.01;
	
	// top panel
	JPanel topPanel = new JPanel(new GridBagLayout());
	topPanel.setPreferredSize(dimTop);
	topPanel.setBorder(BorderFactory.createTitledBorder("Selected Instance"));
	labelType = new JLabel("<html><u>Type:</u></html>");
	valueType = new JLabel(" - ");
	labelName = new JLabel("<html><u>Name:</u></html>");
	valueName = new JLabel(" - ");
	labelCvsTag = new JLabel("<html><u>CVS Tag:</u></html>");
	valueCvsTag = new JLabel(" - ");
	
	c.gridx=0;c.gridy=0;c.gridwidth=1;
	topPanel.add(labelType,c);
	c.gridx=0;c.gridy=1;c.gridwidth=1;
	topPanel.add(valueType,c);
	c.gridx=1;c.gridy=0;c.gridwidth=1;
	topPanel.add(labelName,c);
	c.gridx=1;c.gridy=1;c.gridwidth=1;
	topPanel.add(valueName,c);
	c.gridx=2;c.gridy=0;c.gridwidth=1;
	topPanel.add(labelCvsTag,c);
	c.gridx=2;c.gridy=1;c.gridwidth=1;
	topPanel.add(valueCvsTag,c);

	c.gridx=0;c.gridy=0;c.gridwidth=1;
	add(topPanel,c);
	
	// parameter panel
	JPanel parameterPanel = new JPanel(new BorderLayout());
	parameterPanel.setPreferredSize(dimPar);
	parameterPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
	JScrollPane scrollPane = new JScrollPane(parameterTable);
	scrollPane.setPreferredSize(dimPar);
	parameterPanel.add(scrollPane);
	
	c.gridx=0; c.gridy=1; c.gridwidth=1; c.weighty=0.45;
	add(parameterPanel,c);

	// snippet panel
	JPanel snippetPanel = new JPanel(new BorderLayout());
	snippetPanel.setPreferredSize(dimSnp);
	snippetPanel.setBorder(BorderFactory
			       .createTitledBorder("Configuration Snippet"));
	editorPaneSnippet = new JEditorPane("text/html","");
	editorPaneSnippet.setEditable(false);
	editorPaneSnippet.setPreferredSize(dimSnp);
	snippetPanel.add(new JScrollPane(editorPaneSnippet));
	
	c.gridx=0; c.gridy=2; c.gridwidth=1; c.weighty=0.45;
	add(snippetPanel,c);
	
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
    
    /** set instance to be displayed */
    public void displayInstance(Instance instance)
    {
	valueType.setText(instance.template().name());
	labelType.setText("<html><u>" + instance.template().type()+":</u></html>");
	valueName.setText(instance.name());
	valueCvsTag.setText(instance.template().cvsTag());
	parameterList.clear();
	for (int i=0;i<instance.parameterCount();i++)
	    parameterList.add(instance.parameter(i));
	currentInstance = instance;
	treeModel.setParameters(instance.name(),parameterList);
	parameterTable.expandTree();
    }
    
    /** clear all fields */
    public void clear()
    {
	labelType.setText("<html><u>Type:</u></html>");
	valueName.setText(" - ");
	valueType.setText(" - ");
	valueCvsTag.setText(" - ");
	parameterList.clear();
	treeModel.setParameters("ParametersRoot",parameterList);
	currentInstance = null;
    }
    
    /** pass the tree model to the table model, to initiate updates */
    public void setConfigurationTree(JTree configurationTree)
    {
	treeModel.setConfigurationTree(configurationTree);
    }
    
    /** TreeSelectionListener: valueChanged() */
    public void valueChanged(TreeSelectionEvent e)
    {
	TreePath treePath = e.getNewLeadSelectionPath();
	if (treePath==null) return;
	Object   node     = treePath.getLastPathComponent();
	
	if (node==null) { clear(); return; }
	
	if (node instanceof Parameter) {
	    Parameter p = (Parameter)node;
	    node = p.parent();
	}
	
	if (node instanceof Instance) {
	    Instance instance = (Instance)node;
	    displayInstance(instance);
	    return;
	}
	
	if (node instanceof ModuleReference) {
	    ModuleReference reference = (ModuleReference)node;
	    ModuleInstance  instance = (ModuleInstance)reference.parent();
	    displayInstance(instance);
	    return;
	}

	clear();
    }
    
    /** TableModelListener: tableChanged() */
    public void tableChanged(TableModelEvent e)
    {
	if (currentInstance!=null)
	    editorPaneSnippet.setText(currentInstance.getSnippet());
	else
	    editorPaneSnippet.setText("");
    }
}

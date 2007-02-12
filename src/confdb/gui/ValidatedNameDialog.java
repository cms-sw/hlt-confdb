package confdb.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;

import confdb.data.Template;
import confdb.data.Instance;
import confdb.data.Parameter;

import confdb.gui.treetable.*;


/**
 * ValidatedNameDialog
 * -------------------
 * @author Philipp Schieferdecker
 *
 * Enter and validate the name of either a Module or an ESSource.
 */
public class ValidatedNameDialog extends JDialog implements PropertyChangeListener
{
    //
    // member data
    //
    
    /** the component template */
    private Template template = null;

    /** the dialog's option pane */
    private JOptionPane optionPane = null;
    
    /** the text field where the module instance name is entered */
    private JTextField textFieldInstanceName = null;

    /** parameter table */
    private ParameterTreeModel treeModel      = null;
    private TreeTable          parameterTable = null;
    
    /** flag indicating that a valid choice was made */
    private boolean validChoice = false;

    /** the entered module instance name */
    private String instanceName = null;
    
    /** the choosen set of parameters for the new instance */
    private ArrayList<Parameter> instanceParameters = null;

    /** label of the 'OK' button */
    private static final String ok = new String("OK");

    /** label of the 'Cancel' button */
    private static final String cancel = new String("cancel");
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ValidatedNameDialog(JFrame frame,Template template)
    {
	super(frame,true);
	this.template = template;
	
	instanceParameters = new ArrayList<Parameter>();
	for (int i=0;i<template.parameterCount();i++)
	    instanceParameters.add(template.parameter(i).clone(null));
	
	setTitle(template.type() + " Instance Name");
	
	// text field label
	String labelInstanceName =
	    "Enter the Name of the " + template.name() + " instance: ";
	
	// text field
	textFieldInstanceName = new JTextField(20);
	initTextField();

	// parameter table label
	String labelInstanceParameters = "Enter a unique set of parameter values: ";
	
	// parameter table
	treeModel      = new ParameterTreeModel();
	parameterTable = new TreeTable(treeModel);
	parameterTable.setTreeCellRenderer(new ParameterTreeCellRenderer());
	parameterTable.getColumnModel().getColumn(0).setPreferredWidth(100);
	parameterTable.getColumnModel().getColumn(1).setPreferredWidth(100);
	parameterTable.getColumnModel().getColumn(2).setPreferredWidth(150);
	parameterTable.getColumnModel().getColumn(3).setPreferredWidth(50);
	parameterTable.getColumnModel().getColumn(4).setPreferredWidth(50);
	
	JScrollPane splitPaneTable = new JScrollPane(parameterTable);
	splitPaneTable.setPreferredSize(new Dimension(400,100));
	
	treeModel.setParameters(template.name(),instanceParameters);
	parameterTable.expandTree();
	treeModel.setDefaultTemplate(template);
	
	// option pane
	Object[] components = { labelInstanceName,       textFieldInstanceName,
				labelInstanceParameters, splitPaneTable };
	Object[] options    = { ok, cancel };
	optionPane = new JOptionPane(components,
				     JOptionPane.QUESTION_MESSAGE,
				     JOptionPane.YES_NO_OPTION,
				     null,options,options[0]);
	setContentPane(optionPane);
	
	// handle window closing correctly
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
		}
	    });
	
	// ensure that the text field gets focus
	addComponentListener(new ComponentAdapter()
	    {
		public void componentShown(ComponentEvent e)
		{
		    textFieldInstanceName.requestFocusInWindow();
		}
	    });
	
	// add action listener to text field, in case <RETURN> is pressed
	textFieldInstanceName.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    optionPane.setValue(ok);
		}
	    });
	
	// add property change listener to the option pane, for validatation
	optionPane.addPropertyChangeListener(this);
    }

    
    //
    // member functions
    //

    /** was a valid instance module name entered? */
    public boolean success() { return validChoice; }
    
    /** return the entered module instance name */
    public String instanceName() { return instanceName; }

    /** return the choosen set of instance parameters */
    public ArrayList<Parameter> instanceParameters() { return instanceParameters; }
    
    /** handle option pane property changes, to validate entered name */
    public void propertyChange(PropertyChangeEvent e) {
	String propertyName = e.getPropertyName();
	
	if (isVisible()&&
	    e.getSource()==optionPane&&
	    (JOptionPane.VALUE_PROPERTY.equals(propertyName)||
	     JOptionPane.INPUT_VALUE_PROPERTY.equals(propertyName))) {
	    Object value = optionPane.getValue();
	    
	    if (value==JOptionPane.UNINITIALIZED_VALUE) return;
	    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
	    
	    if (ok.equals(value)) {
		if (isValidInstanceName(textFieldInstanceName.getText())&&
		    template.isUniqueParameterSet(instanceParameters)) {
		    instanceName = textFieldInstanceName.getText();
		    validChoice = true;
		    textFieldInstanceName.setText(null);
		    setVisible(false);
		}
		else {
		    initTextField();
		    textFieldInstanceName.requestFocusInWindow();
		}
	    }
	    else {
		validChoice = false;
		textFieldInstanceName.setText(null);
		setVisible(false);
	    }
	}   
    }
    
    /** validate the entered instance name */
    private boolean isValidInstanceName(String instanceName)
    {
	if (!instanceName.startsWith(template.name())) return false;
	if (instanceName.length()>template.name().length()) {
	    if (!instanceName.startsWith(template.name()+"_")) return false;
	    if (instanceName.length()==template.name().length()+1) return false;
	}
	if (template.hasInstance(instanceName)) return false;
	return true;
    } 
    
    /** initialize the text field with a hint to what's valid */
    private void initTextField()
    {
	textFieldInstanceName.setText(template.name()+"_");
	textFieldInstanceName.setCaretPosition(template.name().length()+1);
    }
    
}

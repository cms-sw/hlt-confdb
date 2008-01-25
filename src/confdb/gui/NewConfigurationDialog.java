package confdb.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import confdb.db.ConfDB;
import confdb.db.DatabaseException;


/**
 * NewConfigurationDialog
 * ----------------------
 * @author Philipp Schieferdecker
 *
 * Enter the name of a configuration and associate it with a release tag.
 */
public class NewConfigurationDialog extends JDialog implements ActionListener,
							       PropertyChangeListener
{
    //
    // member data
    //
    
    /** reference to the main frame */
    private JFrame frame = null;
    
    /** reference to the database */
    private ConfDB database = null;
    
    /** the name of the new configuration */
    private String name = null;
    
    /** the process of the new configuration */
    private String process = null;
    
    /** the release tag to be associated with the new configuration */
    private String releaseTag = null;
    
    /** the dialog's option pane */
    private JOptionPane optionPane = null;
    
    /** the text field where the configuration name is entered */
    private JTextField textFieldName = null;

    /** the text field where the configuration process-name is entered */
    private JTextField textFieldProcess = null;

    /** the combo box to pick the release tag from */
    private JComboBox comboBoxReleaseTag = null;

    /** was a valid choice made? */
    private boolean validChoice = false;
    
    /** label of the 'OK' button */
    private static final String ok = new String("OK");

    /** label of the 'Cancel' button */
    private static final String cancel = new String("cancel");
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public NewConfigurationDialog(JFrame frame,ConfDB database)
    {
	super(frame,true);
	this.frame = frame;
	this.database = database;
	
	setTitle("Enter Configuration Name");
	
	textFieldName = new JTextField(20);
	textFieldProcess = new JTextField(20);

	try {
	    comboBoxReleaseTag = new JComboBox(database.getReleaseTags());
	    comboBoxReleaseTag.setBackground(Color.WHITE);
	}
	catch (DatabaseException e) {
	    System.err.println(e.getMessage());
	    comboBoxReleaseTag = new JComboBox();
	}
	
	Object[] components = { new JLabel("Name:"),textFieldName,
				new JLabel("Process:"),textFieldProcess,
				new JLabel("Release:"), comboBoxReleaseTag
	                      };
	Object[] options    = { ok, cancel };
	optionPane = new JOptionPane(components,
				     JOptionPane.QUESTION_MESSAGE,
				     JOptionPane.YES_NO_OPTION,
				     null,options,options[0]);
	setContentPane(optionPane);
	
	// handle window closing correctly
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
	        optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
	    }
	});
	
	// ensure that the text field gets focus
	addComponentListener(new ComponentAdapter() {
	    public void componentShown(ComponentEvent e) {
		textFieldName.requestFocusInWindow();
	    }
	});
	
	// add action listener to text field, in case <RETURN> is pressed
	textFieldName.addActionListener(this);
	textFieldProcess.addActionListener(this);

	// add property change listener to the option pane, for validatation
	optionPane.addPropertyChangeListener(this);
    }

    
    //
    // member functions
    //

    /** was a valid choice made? */
    public boolean validChoice() { return validChoice; }
    
    /** return the entered configuration name */
    public String name() { return name; }
    
    /** return the entered configuration process */
    public String process() { return process; }
    
    /** return the choosen release tag */
    public String releaseTag() { return releaseTag; }
    
    /** set the release tag, by making it the selected item in the combo box */
    public boolean setReleaseTag(String releaseTag)
    {
	for (int i=0;i<comboBoxReleaseTag.getItemCount();i++) {
	    String itemString = (String)comboBoxReleaseTag.getItemAt(i);
	    if (itemString.equals(releaseTag)) {
		comboBoxReleaseTag.setSelectedIndex(i);
		return true;
	    }
	}
	return false;
    }
    
    /** <RETURN> is like <OK PRESSED> */
    public void actionPerformed(ActionEvent e) {
	optionPane.setValue(ok);
    }
    
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
		if (textFieldName.getText().length()>0&&
		    textFieldProcess.getText().length()>0&&
		    comboBoxReleaseTag.getSelectedIndex()>0) {
		    name = textFieldName.getText();
		    process = textFieldProcess.getText();
		    releaseTag = (String)comboBoxReleaseTag.getSelectedItem();
		    validChoice = true;
		}
		else {
		    System.out.println("You must specify name&release for your " +
				       "new configuration!");
		    return;
		}
	    }
	    else {
		name        = null;
		process     = null;
		releaseTag  = null;
		validChoice = false;
	    }
	    setVisible(false);
	}   
    }
    
}

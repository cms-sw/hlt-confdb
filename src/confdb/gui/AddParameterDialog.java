package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import confdb.data.ParameterFactory;
import confdb.data.Parameter;

/**
 * AddParameterDialog
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class AddParameterDialog extends JDialog implements ActionListener
{
    //
    // member data
    //
    
    /** indicate if a valid choice was made */
    private boolean validChoice = false;

    /** inidcate if only PSet is allowed as type (for adding to VPSet) */
    private boolean psetMode = false;
    
    /** parameter name text field */
    private JTextField textFieldName = null;

    /** parameter type combo box */
    private JComboBox  comboBoxType = null;

    /** parameter value text field */
    private JTextField textFieldValue = null;
    
    /** array of valid parameter types */
    private static final String[] types =
    {
	"",
	"int32","uint32","double","string","bool","EventID","InputTag","PSet",
	"vint32","vuint32","vdouble","vstring","VEventID","VInputTag","VPSet"
    };
    
    /** buttons */
    private static final String OK           = "Add";
    private static final String CANCEL       = "Cancel";
    private JButton             okButton     = new JButton(OK);
    private JButton             cancelButton = new JButton(CANCEL);
    
    
    //
    // construction
    //

    /** standard constructor */
    public AddParameterDialog(JFrame frame)
    {
	super(frame,true);
	setContentPane(createContentPane());
	setTitle("Add Parameter");
    }
    

    //
    // member functions
    //

    /** layout the dialog box*/
    private JPanel createContentPane()
    {
	textFieldName  = new JTextField(10);
	comboBoxType   = new JComboBox(types);
	textFieldValue = new JTextField(10);
	
	okButton.addActionListener(this);
	cancelButton.addActionListener(this);
	textFieldName.addActionListener(this);
	comboBoxType.addActionListener(this);
	
	textFieldValue.setEditable(false);
	comboBoxType.setEditable(false);
	okButton.setEnabled(false);
	
	comboBoxType.setBackground(Color.WHITE);
	
	JPanel panel = new JPanel(new FlowLayout());

	panel.add(new JLabel("Name: "));
	panel.add(textFieldName);
	panel.add(new JLabel(" Type: "));
	panel.add(comboBoxType);
	panel.add(new JLabel("Value: "));
	panel.add(textFieldValue);
	panel.add(okButton);
	panel.add(cancelButton);
	
	return panel;
    }
    
    /** only allow the addition of a pset! */
    public void addParameterSet()
    {
	psetMode = true;
	comboBoxType.setSelectedIndex(8);
	comboBoxType.setEnabled(false);
    }

    /** ActionListener: actionPerformed() */
    public void actionPerformed(ActionEvent e)
    {
	Object src = e.getSource();

	if (src instanceof JComboBox) {
	    JComboBox cb = (JComboBox)src;
	    String type = (String)cb.getSelectedItem();
	    if (type.equals("PSet")||type.equals("VPSet")) {
		textFieldValue.setEditable(false);
		okButton.setEnabled(true);
	    }
	    else if (!type.equals("")) {
		textFieldValue.setEditable(true);
		textFieldValue.requestFocusInWindow();
		okButton.setEnabled(true);
	    }
	}
	else if (src instanceof JTextField) {
	    JTextField tf   = (JTextField)src;
	    String     name = tf.getText();
	    if (!name.equals("")) {
		if (psetMode) {
		    okButton.setEnabled(true);
		}
		else {
		    comboBoxType.setEditable(true);
		}
	    }
	}
	else if (src instanceof JButton) {
	    JButton b = (JButton)src;
	    String cmd = b.getText();
	    if (cmd.equals(OK)) {
		validChoice = true;
	    }
	    setVisible(false);
	}
    }

    /** valid choide? */
    public boolean validChoice() { return validChoice; }
    
    /** parameter name */
    public String name() { return textFieldName.getText(); }

    /** parameter type */
    public String type() { return (String)comboBoxType.getSelectedItem(); }

    /** parameter value as string */
    public String valueAsString() { return textFieldValue.getText(); }
    
}

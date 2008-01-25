package confdb.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import confdb.db.ConfDB;
import confdb.db.DatabaseException;


/**
 * MigrateConfigurationDialog
 * --------------------------
 * @author Philipp Schieferdecker
 *
 * Enter the name of a configuration and associate it with a release tag.
 */
public class MigrateConfigurationDialog extends JDialog implements ActionListener
{
    //
    // member data
    //
    
    /** reference to the main frame */
    private JFrame frame = null;
    
    /** reference to the database */
    private ConfDB database = null;
    
    /** the target release tag */
    private String releaseTag = null;
    
    /** the combo box to pick the release tag from */
    private JComboBox comboBoxReleaseTag = null;
    
    /** buttons */
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public MigrateConfigurationDialog(JFrame frame,ConfDB database)
    {
	super(frame,true);
	this.frame = frame;
	this.database = database;
	
	setTitle("Enter Target Release");

	GridBagConstraints c = new GridBagConstraints();
	//c.fill = GridBagConstraints.HORIZONTAL;
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0.5;
	c.insets = new Insets(3,3,3,3);
	
	JPanel contentPane = new JPanel(new GridBagLayout());

	try {
	    comboBoxReleaseTag = new JComboBox(database.getReleaseTags());
	    comboBoxReleaseTag.setBackground(Color.WHITE);
	    comboBoxReleaseTag.setActionCommand("RELEASETAG");
	    comboBoxReleaseTag.addActionListener(this);
	}
	catch (DatabaseException e) {
	    System.err.println(e.getMessage());
	    comboBoxReleaseTag = new JComboBox();
	}
	comboBoxReleaseTag.setPreferredSize(new Dimension(300,20));
	
	okButton = new JButton("OK");
	okButton.setActionCommand("OK");
	okButton.addActionListener(this);
	okButton.setEnabled(false);
	
	cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("CANCEL");
	cancelButton.addActionListener(this);
	
	okButton.setPreferredSize(cancelButton.getPreferredSize());
	
	c.gridx=0; c.gridy=0; c.gridwidth=4;
	contentPane.add(comboBoxReleaseTag,c);

	c.gridx=1; c.gridy=1; c.gridwidth=1;
	contentPane.add(okButton,c);

	c.gridx=2; c.gridy=1; c.gridwidth=1;
	contentPane.add(cancelButton,c);

	setContentPane(contentPane);
    }

    
    //
    // member functions
    //

    /** return the choosen release tag */
    public String releaseTag()
    {
	return (String)(comboBoxReleaseTag.getSelectedItem());
    }
    
    /** ActionListener: actionPerformed() */
    public void actionPerformed(ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("RELEASETAG")) {
	    if (comboBoxReleaseTag.getSelectedIndex()>0) {
		okButton.setEnabled(true);
	    }
	    else {
		okButton.setEnabled(false);
	    }
	    return;
	}
	
	if (cmd.equals("CANCEL")) {
	    comboBoxReleaseTag.setSelectedIndex(0);
	}
	
	setVisible(false);
    }

    
}

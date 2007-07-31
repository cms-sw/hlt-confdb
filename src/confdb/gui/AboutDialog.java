package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

    
/**
 * AboutDialog
 * -----------
 * @author Philipp Schieferdecker
 *
 * Display information about the application.
 */
public class AboutDialog extends JDialog implements ActionListener
{
    //
    // member data
    //

    /** the text field where the module instance name is entered */
    private JEditorPane editorPane = null;
    
    /** 'OK' button */
    private JButton okButton = null;

    /** label of the 'OK' button */
    private static final String ok = new String("OK");
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public AboutDialog(JFrame frame)
    {
	super(frame,true);
	
	setTitle("About ConfDbGUI");
	
	JPanel contentPane = new JPanel(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	
	editorPane = new JEditorPane("text/html","");
	editorPane.setEditable(false);
	editorPane.setPreferredSize(new Dimension(620,250));
	c.weighty=0.95;
	c.gridx=0;c.gridy=0;c.gridwidth=5;
	contentPane.add(new JScrollPane(editorPane),c);
	
	okButton = new JButton("OK");
	okButton.addActionListener(this);
	okButton.setActionCommand(ok);
	c.gridx=2;c.gridy=1;c.gridwidth=1;
	c.weightx=0.5;
	contentPane.add(okButton,c);
	
	setContentPane(contentPane);

	String txt =
	    "<font size=+1>" +
	    "<p>Thanks for using <b>ConfDbGUI</b>, " +
	    "a CMS tool to create and manage" +
	    "CMSSW job configurations based on a " +
	    "relational database backend.</p>" +
	    "<p>For feedback please contact me at " +
	    "<b>philipp.schieferdecker@cern.ch</b>.</p>" +
	    "<p>Find documented on the web under " +
	    "<b>https://twiki.cern.ch/twiki/bin/view/CMS/EvfConfDBDesign</b>.</p>"+
	    "</font></n>";
	editorPane.setText(txt);
    }
    
    
    //
    // member functions
    //

    /** close the dialog window if 'OK' was pressed */
    public void actionPerformed(ActionEvent e)
    {
	if (ok.equals(e.getActionCommand())) setVisible(false);
    }
    
}

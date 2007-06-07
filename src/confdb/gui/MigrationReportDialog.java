package confdb.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Iterator;

import confdb.migrator.ReleaseMigrator;


/**
 * MigrationReportDialog
 * ---------------------
 * @author Philipp Schieferdecker
 *
 */

public class MigrationReportDialog extends JDialog implements ActionListener
{
    //
    // construction
    //

    /** standard constructor */
    public MigrationReportDialog(JFrame frame,ReleaseMigrator migrator)
    {
	super(frame);
	
	JPanel contentPane = new JPanel(new GridBagLayout());
	contentPane.setPreferredSize(new Dimension(350,350));

	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.NONE;

	
	JEditorPane editorPane = new JEditorPane("text/plain","");
	editorPane.setEditable(false);
	//editorPane.setPreferredSize(new Dimension(350,350));
	JScrollPane editorScrollPane = new JScrollPane(editorPane);
	editorScrollPane.setPreferredSize(new Dimension(300,300));
	editorScrollPane.setMinimumSize(new Dimension(150,150));

	JButton okButton = new JButton("OK");
	okButton.setActionCommand("OK");
	okButton.addActionListener(this);
	
	StringBuffer report = new StringBuffer();
	report.append(new String("missingTemplateCount: " +
				 migrator.missingTemplateCount() + "\n"));
	report.append(new String("missingParameterCount: " +
				 migrator.missingParameterCount() + "\n"));
	report.append(new String("mismatchParameterTypeCount: " +
				 migrator.mismatchParameterTypeCount() + "\n\n"));
	Iterator it = migrator.messageIterator();
	while (it.hasNext()) report.append(new String((String)(it.next()) + "\n"));
	editorPane.setText(report.toString());
	
	c.weightx = 0.1;
	c.gridx=0; c.gridy=0; c.gridwidth=3;// c.ipady=350;
	contentPane.add(editorScrollPane,c);
	c.weightx = 0.9;
	c.gridx=1; c.gridy=1; c.gridwidth=1;
	contentPane.add(okButton,c);
	
	setContentPane(contentPane);
    }
    

    //
    // member functions
    //

    /** ActionListener: actionPerformed() */
    public void actionPerformed(ActionEvent e)
    {
	setVisible(false);
    }
}

package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import confdb.parser.JPythonParser;


/**
 * ParserProblemsDialog
 * --------------------
 * @author Philipp Schieferdecker
 *
 */
public class JParserProblemsDialog extends JDialog
{
    //
    // member data
    //
    
    /** GUI components */
    private JButton   jButtonClose = new JButton();
    private JTextArea jTextArea    = new JTextArea();
    
    
    //
    // construction
    //

    /** standard constructor */
    public JParserProblemsDialog(JFrame jFrame, JPythonParser parser)
    {
	super(jFrame, true);
	
	setContentPane(initComponents());
	jTextArea.setText(parser.problemsAsString());
	
	jButtonClose.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonCloseActionPerformed(e);
		}
	    });
    }

    
    //
    // private member functions
    //
    
    //listener callbacks
    public void jButtonCloseActionPerformed(ActionEvent e)
    {
	setVisible(false);
    }
    

    /** init GUI components */
    private JPanel initComponents()
    {
	JPanel      jPanel = new JPanel();
	JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
	JLabel      jLabel1 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14));
        /*
        jLabel1.setText("Problems were encountered parsing this configuration; "+
			"a file problems.txt was created with the following "+
			"content:");
			*/
        jLabel1.setText("Problems report for the imported configuration.");
        jButtonClose.setText("Close");

        jTextArea.setColumns(20);
        jTextArea.setEditable(false);
        jTextArea.setRows(5);
        jScrollPane1.setViewportView(jTextArea);
	
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout
	    .setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
				     .addContainerGap()
				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					  .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 826, Short.MAX_VALUE)
					  .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					  .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonClose))
				     .addContainerGap())
				);
        layout.setVerticalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
				     .addContainerGap()
				     .add(jLabel1)
				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
				     .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
				     .add(jButtonClose)
				     .addContainerGap())
				);
	return jPanel;
    }
}

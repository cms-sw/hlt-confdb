package confdb.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Iterator;

import org.jdesktop.layout.*;

import confdb.migrator.ReleaseMigrator;


/**
 * MigrationReportDialog
 * ---------------------
 * @author Philipp Schieferdecker
 *
 */

public class MigrationReportDialog extends JDialog // implements ActionListener
{
    //
    // member data
    //
    
    /** editor pane for report */
    private JEditorPane jEditorPaneReport = null;
    
    //
    // construction
    //

    /** standard constructor */
    public MigrationReportDialog(JFrame frame,ReleaseMigrator migrator)
    {
	super(frame);
	
	setContentPane(initContentPane());

	StringBuffer report = new StringBuffer();
	report.append(new String("missingTemplateCount: " +
				 migrator.missingTemplateCount() + "\n"));
	report.append(new String("missingParameterCount: " +
				 migrator.missingParameterCount() + "\n"));
	report.append(new String("mismatchParameterTypeCount: " +
				 migrator.mismatchParameterTypeCount() + "\n\n"));

	Iterator it = migrator.messageIterator();
	while (it.hasNext()) report.append(new String((String)(it.next()) + "\n"));

	jEditorPaneReport.setText(report.toString());
	
	/*
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
	*/
    }
    

    //
    // member functions
    //
    
    /** getContentPane() [generated with NetBeans] */
    private JPanel initContentPane()
    {
	JPanel contentPane = new JPanel();

        jEditorPaneReport = new JEditorPane();
	
        JScrollPane jScrollPane = new JScrollPane();
	JButton     okButton    = new JButton();
	
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants
						 .HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setViewportView(jEditorPaneReport);
	
        okButton.setText("OK");
	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    okButtonActionPerformed(e);
		}
	    });
	
        GroupLayout layout =  new GroupLayout(contentPane);
        contentPane.setLayout(layout);
	layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.LEADING)
				  .add(GroupLayout.TRAILING,
				       layout.createSequentialGroup()
				       .addContainerGap()
				       .add(layout.createParallelGroup(GroupLayout
								       .TRAILING)
					    .add(GroupLayout.LEADING,
						 okButton,
						 GroupLayout.DEFAULT_SIZE,
						 341,
						 Short.MAX_VALUE)
					    .add(GroupLayout.LEADING,
						 jScrollPane,
						 GroupLayout.DEFAULT_SIZE,
						 341,
						 Short.MAX_VALUE))
				       .addContainerGap())
				  );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
				     .addContainerGap()
				     .add(jScrollPane,
					  GroupLayout.DEFAULT_SIZE,
					  398,
					  Short.MAX_VALUE)
				     .addPreferredGap(LayoutStyle.RELATED)
				     .add(okButton)
				     .addContainerGap())
				);
	
	return contentPane;
    }
    
    /** react to okButton being pressed */
    public void okButtonActionPerformed(ActionEvent e)
    {
	setVisible(false);
    }
}

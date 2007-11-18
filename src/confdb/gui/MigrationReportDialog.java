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

	Iterator<String> it = migrator.messageIterator();
	while (it.hasNext()) report.append(new String(it.next() + "\n"));

	jEditorPaneReport.setText(report.toString());
    }
    
    
    //
    // member functions
    //
    
    /** react to okButton being pressed */
    public void okButtonActionPerformed(ActionEvent e)
    {
	setVisible(false);
    }

    //
    // private member functions
    //
    
    /** init contentPane [generated with NetBeans] */
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
}

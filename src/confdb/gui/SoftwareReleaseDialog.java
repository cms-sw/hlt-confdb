package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import confdb.data.SoftwareRelease;


/**
 * SoftwareReleaseDialog
 * ---------------------
 * @author Philipp Schieferdecker
 *
 * display the current release: subsystems, packages, templates, and
 * instance counts for the current configuration.
 */
public class SoftwareReleaseDialog extends JDialog
{
    //
    // member data
    //
    
    /** reference to the main frame */
    private JFrame frame = null;

    /** the software release to be displayed */
    private SoftwareReleaseTreeModel treeModel = null;

    /** GUI components */
    JLabel      jLabelReleaseTag           = new javax.swing.JLabel();
    JTextField  jTextFieldReleaseTag       = new javax.swing.JTextField();
    JScrollPane jScrollPaneSoftwareRelease = new javax.swing.JScrollPane();
    JButton     jButtonDone                = new javax.swing.JButton();
    JTree       jTreeSoftwareRelease = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public SoftwareReleaseDialog(JFrame frame,SoftwareRelease release)
    {
	super(frame,true);
	this.frame = frame;
	setTitle(release.releaseTag());
	jTextFieldReleaseTag.setText(release.releaseTag());
	jTreeSoftwareRelease = new JTree(new SoftwareReleaseTreeModel(release));
	jTreeSoftwareRelease.setRootVisible(false);
	jTreeSoftwareRelease.setEditable(false);
	jTreeSoftwareRelease.setCellRenderer(new SoftwareReleaseTreeRenderer());

	setContentPane(initComponents());
    }
    

    //
    // member functions
    //
    
    /** Done button callback */
    public void jButtonDoneActionPerformed(ActionEvent e)
    {
	setVisible(false);
    }

    
    //
    // private member functions
    //

    /** initialize GUI */
    private JPanel initComponents()
    {
	JPanel jPanel = new JPanel();
	
        jLabelReleaseTag.setText("ReleaseTag:");
	
        jTextFieldReleaseTag.setBackground(new java.awt.Color(255, 255, 255));
        jTextFieldReleaseTag.setEditable(false);
	
        jScrollPaneSoftwareRelease.setViewportView(jTreeSoftwareRelease);
	
        jButtonDone.setText("Done");
	
	// ADDED BY HAND
	jButtonDone.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    jButtonDoneActionPerformed(evt);
		}
	    });
	// END ADDED BY HAND
	    
	    org.jdesktop.layout.GroupLayout layout =
	    new org.jdesktop.layout.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout
	    .setHorizontalGroup(layout
				.createParallelGroup(org.jdesktop
						     .layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
				     .addContainerGap()
				     .add(layout
					  .createParallelGroup(org.jdesktop
							       .layout
							       .GroupLayout.LEADING)
					  .add(layout.createSequentialGroup()
					       .add(jScrollPaneSoftwareRelease,
						    org.jdesktop
						    .layout.GroupLayout
						    .DEFAULT_SIZE,
						    395, Short.MAX_VALUE)
					       .addContainerGap())
					  .add(layout.createSequentialGroup()
					       .add(jLabelReleaseTag)
					       .addPreferredGap(org.jdesktop
								.layout.LayoutStyle
								.RELATED)
					       .add(jTextFieldReleaseTag,
						    org.jdesktop
						    .layout.GroupLayout
						    .DEFAULT_SIZE, 310,
						    Short.MAX_VALUE)
					       .addContainerGap())
					  .add(layout.createSequentialGroup()
					       .add(155, 155, 155)
					       .add(jButtonDone,
						    org.jdesktop
						    .layout.GroupLayout
						    .DEFAULT_SIZE, 88,
						    Short.MAX_VALUE)
					       .add(164, 164, 164))))
				);
        layout
	    .setVerticalGroup(layout
			      .createParallelGroup(org.jdesktop
						   .layout.GroupLayout.LEADING)
			      .add(layout.createSequentialGroup()
				   .addContainerGap()
				   .add(layout
					.createParallelGroup(org.jdesktop
							     .layout.GroupLayout
							     .BASELINE)
					.add(jLabelReleaseTag)
					.add(jTextFieldReleaseTag,
					     org.jdesktop
					     .layout.GroupLayout
					     .PREFERRED_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.DEFAULT_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.PREFERRED_SIZE))
				   .addPreferredGap(org.jdesktop
						    .layout.LayoutStyle.RELATED)
				   .add(jScrollPaneSoftwareRelease,
					org.jdesktop
					.layout.GroupLayout.DEFAULT_SIZE, 391,
					Short.MAX_VALUE)
				   .addPreferredGap(org.jdesktop
						    .layout.LayoutStyle.RELATED)
				   .add(jButtonDone)
				   .addContainerGap())
			      );
	return jPanel;
    }
    
}

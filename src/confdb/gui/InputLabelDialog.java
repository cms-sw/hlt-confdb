package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


/**
 * InputLabelDialog
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Input the name of a new stream or primary dataset to be added to
 * the ConfDB.
 */
public class InputLabelDialog extends JDialog
{
    //
    // member data
    //

    /** indicate if a valid choice was made */
    public boolean validChoice = false;

    /** GUI components */
    private JLabel     jLabel          = new JLabel();
    private JTextField jTextFieldLabel = new JTextField();
    private JButton    jButtonCancel   = new JButton();
    private JButton    jButtonOk       = new JButton();
	

    //
    // construction
    //

    /** standard constructor */
    public InputLabelDialog(JFrame jFrame,String title,String label)
    {
	super(jFrame,true);
	setTitle(title);
	jLabel.setText(label);
	setContentPane(initComponents());

	jTextFieldLabel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jTextFieldLabelActionPerformed(e);
		}
	    });
	jTextFieldLabel
	    .getDocument().addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jTextFieldLabelInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jTextFieldLabelRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});
	jButtonCancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonCancelActionPerformed(e);
		}
	    });
	jButtonOk.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonOkActionPerformed(e);
		}
	    });
	addComponentListener(new ComponentAdapter() {
		public void componentShown(ComponentEvent e) {
		    jTextFieldLabel.requestFocusInWindow();
		}
	    });
    }
    
    
    //
    // member functions
    //

    /** indicate if a valid label was entered */
    public boolean validChoice() { return validChoice; }

    /** get the entered label */
    public String label() { return jTextFieldLabel.getText(); }


    //
    // private member functions
    //
    
    /** listener callbacks */
    private void jTextFieldLabelActionPerformed(ActionEvent e)
    {
	if (jButtonOk.isEnabled()) jButtonOkActionPerformed(e);
    }
    private void jTextFieldLabelInsertUpdate(DocumentEvent e)
    {
	jButtonOk.setEnabled(true);
    }
    private void jTextFieldLabelRemoveUpdate(DocumentEvent e)
    {
	if (label().length()==0) jButtonOk.setEnabled(false);
	else jButtonOk.setEnabled(true);
    }
    private void jButtonCancelActionPerformed(ActionEvent e)
    {
	setVisible(false);
    }
    private void jButtonOkActionPerformed(ActionEvent e)
    {
	validChoice = true;
	setVisible(false);
    }
    

    /** init GUI components */
    private JPanel initComponents()
    {
	JPanel jPanel = new JPanel();

        jButtonCancel.setText("Cancel");
        jButtonOk.setText("OK");
	jButtonOk.setEnabled(false);
	
        org.jdesktop.layout.GroupLayout layout =
	    new org.jdesktop.layout.GroupLayout(jPanel);

        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				  .add(layout.createSequentialGroup()
				       .addContainerGap()
				       .add(jLabel)
				       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
				       .add(jTextFieldLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
				       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
				       .add(jButtonCancel)
				       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
				       .add(jButtonOk)
				       .addContainerGap())
				  );
	
        layout.linkSize(new java.awt.Component[] {jButtonCancel, jButtonOk}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
	
        layout.setVerticalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
				     .addContainerGap()
				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
					  .add(jLabel)
					  .add(jTextFieldLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
					  .add(jButtonCancel)
					  .add(jButtonOk))
				     .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
	
        layout.linkSize(new java.awt.Component[] {jButtonCancel, jTextFieldLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);
	
	return jPanel;
    }

}

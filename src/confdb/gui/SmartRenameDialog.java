package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.jdesktop.layout.*;

import confdb.data.Configuration;

/**
 * SmartRenamingDialog
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class SmartRenameDialog extends JDialog
{
    //
    // member data
    //

    private Configuration config = null;

    /** indicate if a valid choice was made */
    private boolean validChoice = false;
    
    /** GUI components */
    private JComboBox  jComboBoxType    = new JComboBox();
    private JTextField jTextOldPattern  = new JTextField();
    private JTextField jTextNewPattern  = new JTextField();
    private JButton    jButtonOk        = new JButton();
    private JButton    jButtonCancel    = new JButton();
    
    /** old/new */
    private String oldPattern = null;
    private String newPattern = null;

    private String applyTo    = null;

    //
    // construction
    //

    /** standard constructor */
    public SmartRenameDialog(JFrame frame,Configuration config)
    {
	super(frame,true);
	this.config=config;

	applyTo = "All";
	setContentPane(createContentPane());
	jComboBoxType.setEnabled(true);
	jTextOldPattern.setEditable(true);
	jTextNewPattern.setEditable(true);
	jButtonOk.setEnabled(true);
	jButtonCancel.setEnabled(true);
	setTitle("Change substrings within module/sequence/path names");
    }
    

    //
    // member functions
    //
    
    private void jComboBoxTypeActionPerformed(ActionEvent e)
    {
	JComboBox jComboBox   = (JComboBox)e.getSource();
	applyTo     = (String)jComboBox.getSelectedItem();
    }
  
    /** parameter name entered */
    public void jTextOldPatternActionPerformed(ActionEvent e)
    {
	oldPattern = jTextOldPattern.getText();
    }
    
    public void jTextNewPatternActionPerformed(ActionEvent e)
    {
	newPattern = jTextNewPattern.getText();
    }
    
    /** 'Do' button pressed */
    public void jButtonOkActionPerformed(ActionEvent e)
    {
	oldPattern = jTextOldPattern.getText();
	newPattern = jTextNewPattern.getText();
	validChoice = ((oldPattern!=null) && (newPattern!=null) && (!oldPattern.equals("")) && (!oldPattern.equals(newPattern)));
	setVisible(false);
    }

    /** 'Cancel' button pressed */
    public void jButtonCancelActionPerformed(ActionEvent e)
    {
	oldPattern=null;
	newPattern=null;
	validChoice = false;
	setVisible(false);
    }

    /** valid choide? */
    public boolean validChoice() { return validChoice; }
    
    /** parameter old name */
    public String oldPattern() { return oldPattern; }

    /** parameter new name */
    public String newPattern() { return newPattern; }

    /** applyTo */
    public String applyTo() { return applyTo; }

    //
    // private member functions
    //

    /** init GUI components [generated with NetBeans] */
    private JPanel createContentPane()
    {
	JPanel contentPane = new JPanel();
	
        JLabel jLabelComboBoxType= new JLabel();
        JLabel jLabelOldPattern  = new JLabel();
        JLabel jLabelNewPattern  = new JLabel();
	
	jLabelComboBoxType.setText("Apply to:");
        jLabelOldPattern.setText("Old Pattern:");
        jLabelNewPattern.setText("New Pattern:");

	jComboBoxType.setBackground(Color.white);
	DefaultComboBoxModel m=(DefaultComboBoxModel)jComboBoxType.getModel();
	m.addElement("All");
	m.addElement("Paths");
	m.addElement("Sequences");
	m.addElement("Modules");

	jComboBoxType.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxTypeActionPerformed(e);
		}
	    });

	jTextOldPattern.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jTextOldPatternActionPerformed(evt);
            }
        });
	
	jTextNewPattern.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jTextNewPatternActionPerformed(evt);
            }
        });
	
        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
	org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(contentPane);
	contentPane.setLayout(layout);

	layout.setAutocreateGaps(true);
	layout.setAutocreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
				  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				       .add(jLabelComboBoxType,
					    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,100,Short.MAX_VALUE)
				       .add(jLabelOldPattern,
					    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,100,Short.MAX_VALUE)
				       .add(jLabelNewPattern,
					    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,100,Short.MAX_VALUE)
				       .add(jButtonCancel,
					    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,100,Short.MAX_VALUE)
				       )
				  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				       .add(jComboBoxType,
					    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,400,Short.MAX_VALUE)
				       .add(jTextOldPattern,
					    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,400,Short.MAX_VALUE)
				       .add(jTextNewPattern,
					    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,400,Short.MAX_VALUE)
				       .add(jButtonOk,
					    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,100,Short.MAX_VALUE)
				       )
				  );
	layout.setVerticalGroup(layout.createSequentialGroup()
				.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
				     .add(jLabelComboBoxType,
					  org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					  org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				     .add(jComboBoxType,
					  org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					  org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				     )
				.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
				     .add(jLabelOldPattern,
					  org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					  org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				     .add(jTextOldPattern,
					  org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					  org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				     )
				.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
				     .add(jLabelNewPattern,
					  org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					  org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				     .add(jTextNewPattern,
					  org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					  org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				     )
				.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
				     .add(jButtonCancel,
					  org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					  org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				     .add(jButtonOk,
					  org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
					  org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				     )
				);
	
	return contentPane;
    }

}

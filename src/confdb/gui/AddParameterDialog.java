package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.jdesktop.layout.*;

import confdb.data.ParameterFactory;
import confdb.data.Parameter;

/**
 * AddParameterDialog
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class AddParameterDialog extends JDialog
{
    //
    // member data
    //
    
    /** indicate if a valid choice was made */
    private boolean validChoice = false;

    /** inidcate if only PSet is allowed as type (for adding to VPSet) */
    private boolean psetMode = false;
    
    /** array of valid parameter types */
    private static final String[] types =
    {
	"",
	"int32","uint32","double","string","bool","EventID","InputTag","PSet",
	"vint32","vuint32","vdouble","vstring","VEventID","VInputTag","VPSet"
    };
    
    /** GUI components */
    private JTextField jTextFieldName   = new JTextField();
    private JComboBox  jComboBoxType    = new JComboBox(types);
    private JTextField jTextFieldValue  = new JTextField();
    private JCheckBox  jCheckBoxTracked = new JCheckBox();
    private JButton    addButton        = new JButton();
    private JButton    cancelButton     = new JButton();
    
    
    //
    // construction
    //

    /** standard constructor */
    public AddParameterDialog(JFrame frame,boolean isTrackedDefault)
    {
	super(frame,true);
	setContentPane(createContentPane(isTrackedDefault));
	jTextFieldValue.setEditable(false);
	jComboBoxType.setEditable(false);
	addButton.setEnabled(false);
	setTitle("Add Parameter");
    }
    

    //
    // member functions
    //
    
    /** only allow the addition of a pset! */
    public void addParameterSet()
    {
	psetMode = true;
	jComboBoxType.setSelectedIndex(8);
	jComboBoxType.setEnabled(false);
    }

    /** parameter name entered */
    public void jTextFieldNameActionPerformed(ActionEvent e)
    {
	String name = jTextFieldName.getText();
	if (!name.equals("")) {
	    if (psetMode) {
		addButton.setEnabled(true);
	    }
	    else {
		jComboBoxType.setEditable(true);
	    }
	}
    }
    
    /** type choosen from the combo box */
    public void jComboBoxTypeActionPerformed(ActionEvent e)
    {
	String type = (String)jComboBoxType.getSelectedItem();
	if (type.equals("PSet")||type.equals("VPSet")) {
	    jTextFieldValue.setEditable(false);
	    addButton.setEnabled(true);
	}
	else if (!type.equals("")) {
	    jTextFieldValue.setEditable(true);
	    jTextFieldValue.requestFocusInWindow();
	    addButton.setEnabled(true);
	}
    }

    /** 'Add' button pressed */
    public void addButtonActionPerformed(ActionEvent e)
    {
	validChoice = true;
	setVisible(false);
    }

    /** 'Cancel' button pressed */
    public void cancelButtonActionPerformed(ActionEvent e)
    {
	validChoice = false;
	setVisible(false);
    }

    /** valid choide? */
    public boolean validChoice() { return validChoice; }
    
    /** parameter name */
    public String name() { return jTextFieldName.getText(); }

    /** parameter type */
    public String type() { return (String)jComboBoxType.getSelectedItem(); }

    /** parameter isTracked */
    public boolean isTracked() { return jCheckBoxTracked.isSelected(); }
    
    /** parameter value as string */
    public String valueAsString() { return jTextFieldValue.getText(); }

    
    //
    // private member functions
    //

    /** init GUI components [generated with NetBeans] */
    private JPanel createContentPane(boolean isTrackedDefault)
    {
	JPanel contentPane = new JPanel();
	
        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        JLabel jLabel3 = new JLabel();
	
        jLabel1.setText("Name:");
        jLabel2.setText("Type:");
        jLabel3.setText("Value:");
        
	jTextFieldName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jTextFieldNameActionPerformed(evt);
            }
        });
	
        
	jComboBoxType.setBackground(new Color(255, 255, 255));
        jComboBoxType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jComboBoxTypeActionPerformed(evt);
            }
        });
	
        jCheckBoxTracked.setText("tracked");
        jCheckBoxTracked.setSelected(isTrackedDefault);
        jCheckBoxTracked.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxTracked.setMargin(new java.awt.Insets(0, 0, 0, 0));
	//if (!isTrackedDefault) jCheckBoxTracked.setEnabled(false);
	
        addButton.setText("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

	GroupLayout layout = new GroupLayout(contentPane);
	contentPane.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel3,
				     GroupLayout.DEFAULT_SIZE,
				     GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(223, 223, 223))
                            .add(layout.createSequentialGroup()
                                .add(jTextFieldValue,
				     GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)))
                        .add(jCheckBoxTracked))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jTextFieldName,
				 GroupLayout.PREFERRED_SIZE,
				 149, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.TRAILING)
                            .add(jLabel2,
				 GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING,
				 jComboBoxType, 0, 162, Short.MAX_VALUE)))
                    .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(addButton,
			     GroupLayout.PREFERRED_SIZE,
			     84, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(cancelButton,
			     GroupLayout.PREFERRED_SIZE,
			     85, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jComboBoxType,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldName,
			 GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jCheckBoxTracked)
                    .add(jTextFieldValue,
			 GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED, 35, Short.MAX_VALUE)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(addButton)
                    .add(cancelButton))
                .addContainerGap())
        );

	return contentPane;
    }

}

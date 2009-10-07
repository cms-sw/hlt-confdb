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
    
    /** inidcate if tracked is an option */
    private boolean isTrackedOption = true;

    /** array of valid parameter types */
    private static final String[] types =
    {
	"",
	"int32","uint32","int64","uint64","double","string","bool",
	"EventID","InputTag","FileInPath","PSet",
	"vint32","vuint32","vint64","vuint64","vdouble","vstring",
	"VEventID","VInputTag","VPSet"
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
    

    /** Overloaded constructor*/
    public AddParameterDialog(JFrame frame)
    {
	this(frame,false);
	isTrackedOption = false;
	if(!isTrackedOption)
	    jCheckBoxTracked.setEnabled(false); 
    }

    //
    // member functions
    //
    
    /** only allow the addition of a pset! */
    public void addParameterSet()
    {
	psetMode = true;
	jComboBoxType.setSelectedIndex(11);
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

	org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(contentPane);
	contentPane.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel3,
				     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(223, 223, 223))
                            .add(layout.createSequentialGroup()
                                .add(jTextFieldValue,
				     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(jCheckBoxTracked))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jTextFieldName,
				 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				 149, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel2,
				 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING,
				 jComboBoxType, 0, 162, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(addButton,
			     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			     84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton,
			     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			     85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxType,
			 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldName,
			 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxTracked)
                    .add(jTextFieldValue,
			 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 35, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(cancelButton))
                .addContainerGap())
        );

	return contentPane;
    }

}

package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

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

	/** indicate if only PSet is allowed as type (for adding to VPSet) */
	private boolean psetMode = false;

	/** indicate if tracked is an option */
    private boolean isTrackedOption = true;

    /** array of valid parameter types */
    private static final String[] types =
    {
        "",
        "int32","uint32","int64","uint64","double","string","bool",
        "EventID","InputTag","FileInPath","PSet",
        "vint32","vuint32","vint64","vuint64","vdouble","vstring",
        "VEventID","VInputTag","VPSet","ESInputTag","VESInputTag"
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

	/** only allow the addition of a vpset! */
	public void addVParameterSet() {
		// psetMode = true;
		jComboBoxType.setSelectedIndex(20);
		jComboBoxType.setEnabled(false);
	}

	/** only allow the addition of a string! */
	public void addString() {
		// psetMode = true;
		jComboBoxType.setSelectedIndex(6);
		jComboBoxType.setEnabled(false);
	}

	/** only allow the addition of a tracked parameter! */
	public void disableTrackedCheckbox() {
		// psetMode = true;
		jCheckBoxTracked.setEnabled(false);
	}

	/** set the name */
	public void setName(String name) {
		jTextFieldName.setText(name);
	}

	/** focus the value */
	public void setValueFocus() {
		jTextFieldValue.requestFocusInWindow();		 
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
    
    /** type chosen from the combo box */
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

    /** valid choice? */
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(contentPane);
        contentPane.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3,
                                     javax.swing.GroupLayout.DEFAULT_SIZE,
                                     javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(223))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextFieldValue,
                                     javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addComponent(jCheckBoxTracked))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jTextFieldName,
                                 javax.swing.GroupLayout.PREFERRED_SIZE,
                                 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2,
                                 javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                            .addComponent(jComboBoxType, javax.swing.GroupLayout.Alignment.LEADING,
                                 0, 162, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(addButton,
                             javax.swing.GroupLayout.PREFERRED_SIZE,
                             84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton,
                             javax.swing.GroupLayout.PREFERRED_SIZE,
                             85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxType,
                         javax.swing.GroupLayout.PREFERRED_SIZE,
                         javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldName,
                         javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxTracked)
                    .addComponent(jTextFieldValue,
                         javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

		return contentPane;
	}

}

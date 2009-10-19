package confdb.gui;

import java.util.Iterator;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import org.jdesktop.layout.*;

import confdb.data.EventContent;
import confdb.data.Path;
import confdb.data.ModuleInstance;
import confdb.data.OutputCommand;


/**
 * OutputCommandEditorDialog
 * -------------------------
 * @author Philipp Schieferdecker
 *
 * Edit output commands (drop/keep statements) of format
 * <classname>_<modulename>_<extraname>_<processname>.
 */
public class OutputCommandEditorDialog extends JDialog
{
    //
    // member data
    //

    /** reference to the event content */
    private EventContent content = null;

    /** output command to be created / edited */
    private OutputCommand command = null;
    
    /** GUI components */
    private ButtonGroup  buttonGroup            = new ButtonGroup();
    private JRadioButton jRadioButtonKeep       = new JRadioButton();
    private JRadioButton jRadioButtonDrop       = new JRadioButton();
    private JTextField   jTextFieldEventContent = new JTextField();
    private JTextField   jTextFieldPath         = new JTextField();
    private JTextField   jTextFieldClassName    = new JTextField();
    private JComboBox    jComboBoxModuleName    = new JComboBox();
    private JTextField   jTextFieldExtraName    = new JTextField();
    private JTextField   jTextFieldProcess      = new JTextField();
    private JTextPane    jTextPanePreview       = new JTextPane();
    private JButton      jButtonOK              = new JButton();
    private JButton      jButtonCancel          = new JButton();
    
    
    //
    // construction
    //
    
    /** constructor from event content */
    public OutputCommandEditorDialog(JFrame jFrame,EventContent content)
    {
	super(jFrame,true);
	this.content = content;
	this.command = new OutputCommand();
	setContentPane(createContentPane());
	setTitle("Edit Output Command");
	initialize();
    }
    
    
    /** constructor from event content *and* path */
    public OutputCommandEditorDialog(JFrame jFrame,
				     EventContent content,OutputCommand command)
    {
	super(jFrame,true);
	this.content = content;
	this.command = command;
	setContentPane(createContentPane());
	setTitle("Edit Output Command");
	initialize();
    }
    
    
    //
    // member functions
    //

    /** get the edited command */
    public OutputCommand command() { return command; }


    //
    // private member functions
    //

    /** update all fields according to content and command fields */
    private void initialize()
    {
	jTextFieldEventContent.setText(content.name());
	String pathName = new String();
	if (!command.isGlobal()) pathName = command.parentPath().name();
	jTextFieldPath.setText(pathName);
	if (command.isDrop()) jRadioButtonDrop.setSelected(true);
	else                  jRadioButtonKeep.setSelected(true);
	
	jTextFieldClassName.setText(command.className());
	jTextFieldExtraName.setText(command.extraName());
	jTextFieldProcess.setText(command.processName());
	
	DefaultComboBoxModel cbm =
	    (DefaultComboBoxModel)jComboBoxModuleName.getModel();
	cbm.removeAllElements();
	if (!command.isGlobal()) {
	    Iterator<ModuleInstance> itM =
		command.parentPath().moduleIterator();
	    while (itM.hasNext()) cbm.addElement(itM.next().name());
	}
	cbm.setSelectedItem(cbm.getElementAt(0));
	
	jTextPanePreview.setText(command.toString());
    }
    
    /** update preview after manipulation of fields */
    private void updatePreview()
    {
	jTextPanePreview.setText(command.toString());
    }
    
    
    private void jRadioButtonDropActionPerformed(ActionEvent evt)
    {
	if (jRadioButtonDrop.isSelected()) command.setDrop();
	else command.setKeep();
	updatePreview();
    }
    private void jRadioButtonKeepActionPerformed(ActionEvent evt)
    {
	if (jRadioButtonDrop.isSelected()) command.setDrop();
	else command.setKeep();
	updatePreview();
    }
    private void jButtonCancelActionPerformed(ActionEvent evt)
    {
	command = null;
        setVisible(false);
    }
    private void jComboBoxModuleNameActionPerformed(ActionEvent evt)
    {
	command.setModuleName(jComboBoxModuleName.getSelectedItem().toString());
	updatePreview();
    }
    private void jButtonOKActionPerformed(ActionEvent evt)
    {
	setVisible(false);
    }
    
    private void jTextFieldClassNameInsertUpdate(DocumentEvent e)
    {
	command.setClassName(jTextFieldClassName.getText());
	updatePreview();
    }
    private void jTextFieldClassNameRemoveUpdate(DocumentEvent e)
    {
	command.setClassName(jTextFieldClassName.getText());
	updatePreview();
    }
    private void jComboBoxModuleNameInsertUpdate(DocumentEvent e)
    {
	command.setModuleName(jComboBoxModuleName
			      .getEditor().getItem().toString());
	updatePreview();
    }
    private void jComboBoxModuleNameRemoveUpdate(DocumentEvent e)
    {
	command.setModuleName(jComboBoxModuleName
			      .getEditor().getItem().toString());
	updatePreview();
    }
    private void jTextFieldExtraNameInsertUpdate(DocumentEvent e)
    {
	command.setExtraName(jTextFieldExtraName.getText());
	updatePreview();
    }
    private void jTextFieldExtraNameRemoveUpdate(DocumentEvent e)
    {
	command.setExtraName(jTextFieldExtraName.getText());
	updatePreview();
    }
    private void jTextFieldProcessInsertUpdate(DocumentEvent e)
    {
	command.setProcessName(jTextFieldProcess.getText());
	updatePreview();
    }
    private void jTextFieldProcessRemoveUpdate(DocumentEvent e)
    {
	command.setProcessName(jTextFieldProcess.getText());
	updatePreview();
    }

    
    /** create the graphical components */
    private JPanel createContentPane()
    {
	JPanel jPanel = new JPanel();
	
	JLabel       jLabelEventContent = new JLabel();
        JLabel       jLabelPath = new JLabel();
        JLabel       jLabelClassName = new JLabel();
        JLabel       jLabelModuleName = new JLabel();
        JLabel       jLabelExtraName = new JLabel();
        JLabel       jLabelProcess = new JLabel();
        JScrollPane  jScrollPanePreview = new JScrollPane();

	buttonGroup.add(jRadioButtonKeep);
	buttonGroup.add(jRadioButtonDrop);

        jLabelEventContent.setText("EventContent:");

        jTextFieldEventContent.setEditable(false);

        jLabelPath.setText("Path:");

        jRadioButtonKeep.setText("keep");
        jRadioButtonKeep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jRadioButtonKeepActionPerformed(evt);
            }
        });

        jRadioButtonDrop.setText("drop");
        jRadioButtonDrop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jRadioButtonDropActionPerformed(evt);
            }
        });

        jLabelClassName.setText("Class Name:");

        jLabelModuleName.setText("Module Name:");

        jLabelExtraName.setText("Extra Name:");

        jLabelProcess.setText("Process:");

        jTextFieldClassName.setText("*");

        jComboBoxModuleName.setEditable(true);
        jComboBoxModuleName.setModel(new DefaultComboBoxModel(new String[] {}));
        jComboBoxModuleName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jComboBoxModuleNameActionPerformed(evt);
            }
        });

        jTextFieldExtraName.setText("*");

        jTextFieldProcess.setText("*");

        jTextFieldPath.setEditable(false);
        jTextFieldPath.setText("jTextField2");

	jTextFieldClassName.getDocument()
	    .addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jTextFieldClassNameInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jTextFieldClassNameRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});
	JTextComponent tc=
	    (JTextComponent)jComboBoxModuleName
	    .getEditor().getEditorComponent();
	tc.getDocument()
	    .addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jComboBoxModuleNameInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jComboBoxModuleNameRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});
	jTextFieldExtraName.getDocument()
	    .addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jTextFieldExtraNameInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jTextFieldExtraNameRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});
	jTextFieldProcess.getDocument()
	    .addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jTextFieldProcessInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jTextFieldProcessRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});
	
	
        jScrollPanePreview.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));

        jTextPanePreview.setEditable(false);
        jScrollPanePreview.setViewportView(jTextPanePreview);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPanePreview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jRadioButtonDrop)
                                    .add(jRadioButtonKeep))
                                .add(34, 34, 34)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jTextFieldClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabelClassName)
                                        .add(48, 48, 48)))
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabelModuleName)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jComboBoxModuleName, 0, 120, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabelExtraName)
                                    .add(jTextFieldExtraName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabelProcess)
                                        .add(68, 68, 68))
                                    .add(jTextFieldProcess, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(jLabelEventContent)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextFieldEventContent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                                .add(30, 30, 30)
                                .add(jLabelPath)
                                .add(3, 3, 3)
                                .add(jTextFieldPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jButtonCancel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonOK))))
        );

        layout.linkSize(new java.awt.Component[] {jButtonCancel, jButtonOK}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelEventContent)
                    .add(jTextFieldEventContent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelPath))
                .add(37, 37, 37)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabelExtraName)
                        .add(jLabelProcess)
                        .add(jLabelModuleName))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabelClassName)
                        .add(jRadioButtonKeep)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldClassName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jRadioButtonDrop)
                    .add(jComboBoxModuleName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldExtraName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldProcess, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(37, 37, 37)
                .add(jScrollPanePreview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonOK)
                    .add(jButtonCancel)))
        );

	return jPanel;
    }
    
}
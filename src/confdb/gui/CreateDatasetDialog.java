package confdb.gui;

import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

import confdb.data.Configuration;
import confdb.data.Stream;
import confdb.data.PrimaryDataset;


/**
 * CreateDatasetDialog
 * -------------------
 * @author Philipp Schieferdecker
 *
 * Create a new primary dataset off an existing stream.
 */
public class CreateDatasetDialog extends JDialog
{
    //
    // member data
    //

    /** reference to the configuration */
    private Configuration config = null;

    /** created dataset */
    private PrimaryDataset dataset = null;

    /** GUI components */
    private JButton jButtonCancel;
    private JButton jButtonOK;
    private JComboBox jComboBoxStream;
    private JLabel jLabelDatasetLabel;
    private JLabel jLabelStream;
    private JTextField jTextFieldDatasetLabel;


    //
    // construction
    //

    /** Creates new form CreateDatasetDialog */
    public CreateDatasetDialog(JFrame frame,Configuration config)
    {
	super(frame,true);
        this.config = config;
        setContentPane(initComponents());
        updateStreamList();
	setTitle("Create New Primary Dataset");
    }
    

    //
    // public member functions
    //

    /** indicate wether dataset was successfully created */
    public boolean isSuccess() { return dataset!=null; }

    /** retrieve the created primary dataset */
    public PrimaryDataset dataset() { return dataset; }


    //
    // private member functions
    //

    /** update combo box with list of streams */
    private void updateStreamList()
    {
        DefaultComboBoxModel cbm =
	    (DefaultComboBoxModel)jComboBoxStream.getModel();
        cbm.removeAllElements();
        Iterator<Stream> itS = config.streamIterator();
        while (itS.hasNext()) {
            Stream stream = itS.next();
            cbm.addElement(stream.label());
        }
    }

    //
    // ACTIONLISTENER CALLBACKS
    //
    private void jButtonOKActionPerformed(ActionEvent evt)
    {
        String datasetLabel = jTextFieldDatasetLabel.getText();
        String streamLabel  = (String)jComboBoxStream.getSelectedItem();
        Stream stream = config.stream(streamLabel);
        dataset = stream.insertDataset(datasetLabel);
	setVisible(false);
    }

    private void jButtonCancelActionPerformed(ActionEvent evt) 
    {
	setVisible(false);
    }


    //
    // DOCUMENTLISTENER CALLBACKS
    //
    private void jTextFieldDatasetLabelInsertUpdate(DocumentEvent e)
    {
	String datasetLabel = jTextFieldDatasetLabel.getText();
	if (config.dataset(datasetLabel)==null) jButtonOK.setEnabled(true);
	else jButtonOK.setEnabled(false);
    }
    public void jTextFieldDatasetLabelRemoveUpdate(DocumentEvent e)
    {
	String datasetLabel = jTextFieldDatasetLabel.getText();
	if (config.dataset(datasetLabel)==null) jButtonOK.setEnabled(true);
	else jButtonOK.setEnabled(false);
    }
    
    /** init graphical components */
    private JPanel initComponents()
    {
	JPanel jPanel = new JPanel();
	
        jLabelDatasetLabel = new javax.swing.JLabel();
        jTextFieldDatasetLabel = new javax.swing.JTextField();
        jLabelStream = new javax.swing.JLabel();
        jComboBoxStream = new javax.swing.JComboBox();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        jLabelDatasetLabel.setText("Primary Dataset Name:");

        jLabelStream.setText("Stream:");

        jTextFieldDatasetLabel.getDocument()
	    .addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jTextFieldDatasetLabelInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jTextFieldDatasetLabelRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});

        jComboBoxStream.setModel(new javax.swing.DefaultComboBoxModel(new String[] {  }));

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
                    .add(layout.createSequentialGroup()
                        .add(jTextFieldDatasetLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 327, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxStream, 0, 316, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabelDatasetLabel)
                        .add(199, 199, 199)
                        .add(jLabelStream))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jButtonCancel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonOK)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jButtonCancel, jButtonOK}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelDatasetLabel)
                    .add(jLabelStream))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldDatasetLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBoxStream, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonOK)
                    .add(jButtonCancel))
                .addContainerGap())
        );

	return jPanel;
    }


}

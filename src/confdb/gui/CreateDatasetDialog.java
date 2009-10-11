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
    private JComboBox jComboBoxStreamLabel;
    private JLabel jLabel1;
    private JLabel jLabel2;
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
	    (DefaultComboBoxModel)jComboBoxStreamLabel.getModel();
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
        String streamLabel  = (String)jComboBoxStreamLabel.getSelectedItem();
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
	
        jLabel1 = new JLabel();
        jTextFieldDatasetLabel = new JTextField();
        jComboBoxStreamLabel = new JComboBox();
        jLabel2 = new JLabel();
        jButtonOK = new JButton();
        jButtonCancel = new JButton();

        jLabel1.setText("Primary Dataset Name:");

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

        jComboBoxStreamLabel
	    .setModel(new DefaultComboBoxModel(new String[] {} ));

        jLabel2.setText("Stream:");

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

        GroupLayout layout = new GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
				  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				  .addGroup(layout.createSequentialGroup()
					    .addContainerGap()
					    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						      .addGroup(layout.createSequentialGroup()
								.addComponent(jTextFieldDatasetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jComboBoxStreamLabel, 0, 350, Short.MAX_VALUE))
						      .addGroup(layout.createSequentialGroup()
								.addComponent(jLabel1)
								.addGap(258, 258, 258)
								.addComponent(jLabel2))
						      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(jButtonCancel)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jButtonOK)))
					    .addContainerGap())
				  );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonCancel, jButtonOK});

        layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					  .addContainerGap()
					  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						    .addComponent(jLabel1)
						    .addComponent(jLabel2))
					  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						    .addComponent(jTextFieldDatasetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						    .addComponent(jComboBoxStreamLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
					  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						    .addComponent(jButtonOK)
						    .addComponent(jButtonCancel))
					  .addContainerGap())
				);
	return jPanel;
    }

}

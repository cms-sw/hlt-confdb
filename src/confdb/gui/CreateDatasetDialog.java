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
public class CreateDatasetDialog extends JDialog {
	//
	// member data
	//

	/** reference to the configuration */
	private Configuration config = null;

	/** created dataset */
	private PrimaryDataset dataset = null;

	/** streamName (fixed if set!) */
	private String streamName = null;

	/** GUI components */
	private JButton jButtonCancel;
	private JButton jButtonOK;
	private JComboBox jComboBoxStream;
	private JLabel jLabelDatasetName;
	private JLabel jLabelStream;
	private JTextField jTextFieldDatasetName;
	
	private PrimaryDataset datasetToClone;
	//
	// construction
	//

	/** Creates new form CreateDatasetDialog */
	public CreateDatasetDialog(JFrame frame, Configuration config, PrimaryDataset datasetToClone) {
		super(frame, true);
		this.config = config;
		this.datasetToClone = datasetToClone;
		setContentPane(initComponents());
		updateStreamList();
		if(datasetToClone==null){
			setTitle("Create New Primary Dataset");
		} else{
			setTitle("Cloning Dataset "+datasetToClone);
		}
	}
	public CreateDatasetDialog(JFrame frame, Configuration config) {
		this(frame,config,null);
		
	}
	//
	// public member functions
	//

	/** indicate wether dataset was successfully created */
	public boolean isSuccess() {
		return dataset != null;
	}

	/** fix the name of the stream */
	public void fixStreamName(String streamName) {
		this.streamName = streamName;
		updateStreamList();
		jComboBoxStream.setEnabled(false);
	}

	/** retrieve the created primary dataset */
	public PrimaryDataset dataset() {
		return dataset;
	}

	//
	// private member functions
	//

	/** update combo box with list of streams */
	private void updateStreamList() {
		DefaultComboBoxModel cbm = (DefaultComboBoxModel) jComboBoxStream.getModel();
		cbm.removeAllElements();
		Iterator<Stream> itS = config.streamIterator();
		while (itS.hasNext()) {
			Stream stream = itS.next();
			// cbm.addElement(stream.name());
			if(datasetToClone==null || (stream.parentContent()!=datasetToClone.parentStream().parentContent())) {
				cbm.addElement(stream);
				if (stream.name().equals(streamName))
					cbm.setSelectedItem(stream);
			}
		}
	}

	//
	// ACTIONLISTENER CALLBACKS
	//
	private void jButtonOKActionPerformed(ActionEvent evt) {
		String datasetName = jTextFieldDatasetName.getText();
		Stream stream = (Stream) jComboBoxStream.getSelectedItem();
		dataset = stream.insertDataset(datasetName);
		dataset.createDatasetPath(this.datasetToClone!=null ? this.datasetToClone.pathFilter() : null);
		setVisible(false);
	}

	private void jButtonCancelActionPerformed(ActionEvent evt) {
		setVisible(false);
	}

	//
	// DOCUMENTLISTENER CALLBACKS
	//
	private void jTextFieldDatasetNameInsertUpdate(DocumentEvent e) {
		String datasetName = jTextFieldDatasetName.getText().replaceAll("\\W","");
		if (config.dataset(datasetName) == null &&
			config.isUniqueQualifier(PrimaryDataset.datasetPathName(datasetName)) &&
			config.isUniqueQualifier(PrimaryDataset.pathFilterDefaultName(datasetName))
		)
			jButtonOK.setEnabled(true);
		else
			jButtonOK.setEnabled(false);
	}

	public void jTextFieldDatasetNameRemoveUpdate(DocumentEvent e) {
		String datasetName = jTextFieldDatasetName.getText().replaceAll("\\W","");
		if (config.dataset(datasetName) == null &&
			config.isUniqueQualifier(PrimaryDataset.datasetPathName(datasetName)) &&
			config.isUniqueQualifier(PrimaryDataset.pathFilterDefaultName(datasetName))
		)
			jButtonOK.setEnabled(true);
		else
			jButtonOK.setEnabled(false);
	}

	/** init graphical components */
	private JPanel initComponents() {
		JPanel jPanel = new JPanel();

		jLabelDatasetName = new javax.swing.JLabel();
		jTextFieldDatasetName = new javax.swing.JTextField();
		jLabelStream = new javax.swing.JLabel();
		jComboBoxStream = new javax.swing.JComboBox();
		jButtonOK = new javax.swing.JButton();
		jButtonCancel = new javax.swing.JButton();

		jLabelDatasetName.setText("Primary Dataset Name:");
		if(datasetToClone==null){
			jLabelStream.setText("Stream:");
		}else{
			jLabelStream.setText("Stream (only those with diff. event contents):");
		}
		jTextFieldDatasetName.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				jTextFieldDatasetNameInsertUpdate(e);
			}

			public void removeUpdate(DocumentEvent e) {
				jTextFieldDatasetNameRemoveUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
			}
		});

		jComboBoxStream.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));

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

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addComponent(jTextFieldDatasetName, javax.swing.GroupLayout.PREFERRED_SIZE,
												327, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jComboBoxStream, 0, 316, Short.MAX_VALUE))
								.addGroup(layout.createSequentialGroup().addComponent(jLabelDatasetName).addGap(199)
										.addComponent(jLabelStream))
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup().addComponent(jButtonCancel)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jButtonOK)))
						.addContainerGap()));

		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { jButtonCancel, jButtonOK });

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelDatasetName).addComponent(jLabelStream))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jTextFieldDatasetName, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jComboBoxStream, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonOK).addComponent(jButtonCancel))
						.addContainerGap()));

		return jPanel;
	}

}

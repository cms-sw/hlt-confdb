package confdb.gui;

import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;


/**
 * SplitDatasetDialog
 * -------------------
 * @author Sam Harper based of CreateDatasetDialog by Philipp Schieferdecker
 *
 * Gets the number of datasets to split the PD by
 */
public class SplitDatasetDialog extends JDialog {
	//
	// member data
	//

	
	/** GUI components */
	private JButton jButtonCancel;
	private JButton jButtonOK;
	private JLabel jLabelNrInstanceText;	
	private JTextField jTextFieldNumberToSplit;
    private int nrInstances = -1;
		
	//
	// construction
	//

	/** Creates new form SplitDatasetDialog */
	public SplitDatasetDialog(JFrame frame,String datasetName) {
		super(frame, true);
		setContentPane(initComponents());		
		setTitle("Splitting Dataset: "+datasetName);
		
	}
	
	//
	// public member functions
	//

	/** indicate wether dataset was successfully created */
	public boolean isSuccess() {
		return nrInstances >0;
	}
    
    public int nrInstances() {
        return nrInstances;
    }


    private int getNrToSplit(){
        try {
            return Integer.parseInt(jTextFieldNumberToSplit.getText());
        } catch (NumberFormatException e){
            return -1;
        }
    }

	//
	// ACTIONLISTENER CALLBACKS
	//
	private void jButtonOKActionPerformed(ActionEvent evt) {
        nrInstances = getNrToSplit();        
		setVisible(false);
	}

	private void jButtonCancelActionPerformed(ActionEvent evt) {
		setVisible(false);
	}

	//
	// DOCUMENTLISTENER CALLBACKS
	//
	private void jTextFieldTextFileNumberToSplitUpdate(DocumentEvent e) {
        if(getNrToSplit()>0){
			jButtonOK.setEnabled(true);
        }else{
			jButtonOK.setEnabled(false);
        }
	}

	/** init graphical components */
	private JPanel initComponents() {
		JPanel jPanel = new JPanel();

		jLabelNrInstanceText = new javax.swing.JLabel();
		jTextFieldNumberToSplit = new javax.swing.JTextField();		
		jButtonOK = new javax.swing.JButton();
		jButtonCancel = new javax.swing.JButton();

		jLabelNrInstanceText.setText("Number of Dataset Instances (must be postive)");
		
		jTextFieldNumberToSplit.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				jTextFieldTextFileNumberToSplitUpdate(e);
			}

			public void removeUpdate(DocumentEvent e) {
				jTextFieldTextFileNumberToSplitUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
                jTextFieldTextFileNumberToSplitUpdate(e);
			}
		});
		
        jButtonOK.setEnabled(false);
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
										.addComponent(jTextFieldNumberToSplit, javax.swing.GroupLayout.PREFERRED_SIZE,
												327, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                )
								.addGroup(layout.createSequentialGroup().addComponent(jLabelNrInstanceText).addGap(199)
										)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup().addComponent(jButtonCancel)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jButtonOK)))
						.addContainerGap()));

		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { jButtonCancel, jButtonOK });

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelNrInstanceText))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jTextFieldNumberToSplit, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonOK).addComponent(jButtonCancel))
						.addContainerGap()));
        return jPanel;
	}

}

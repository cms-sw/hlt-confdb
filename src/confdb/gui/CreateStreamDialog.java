package confdb.gui;

import java.util.Iterator;

import javax.swing.*;
import java.awt.event.*;

import confdb.data.EventContent;
import confdb.data.Stream;
import confdb.data.Configuration;

/**
 * CreateStreamDialog
 * ------------------
 * @author Philipp Schieferdecker
 *
 * Let the user create a new stream and either link it an existing
 * content or create a new one.
 */
public class CreateStreamDialog extends JDialog
{
    //
    // member data
    //
    
    /** reference to the configuration */
    private Configuration config = null;

    /** created stream */
    private Stream stream = null;

    /** GUI components */
    private JButton    jButtonCancel;
    private JButton    jButtonOK;
    private JComboBox  jComboBoxEventContent;
    private JLabel     jLabelEventContent;
    private JLabel     jLabelStreamLabel;
    private JTextField jTextFieldStreamLabel;

    //
    // construction
    //
    
    /** Creates new form CreateStreamDialog */
    public CreateStreamDialog(JFrame frame,Configuration config)
    {
	super(frame,true);
        this.config = config;
        setContentPane(initComponents());
        updateEventContentList();
	setTitle("Create New Stream");
    }

    //
    // public member functions
    //

    /** indicate if a stream was successfully created */
    public boolean isSuccess() { return stream!=null; }

    /** retrieve the created stream */
    public Stream stream() { return stream; }


    //
    // private member functions
    //

    /** update the list of available event context, put into combo box */
    private void updateEventContentList()
    {
        DefaultComboBoxModel cbm =
           (DefaultComboBoxModel)jComboBoxEventContent.getModel();
        cbm.removeAllElements();
        cbm.addElement(new String("<NEW>"));
        Iterator<EventContent> itC = config.contentIterator();
        while (itC.hasNext()) {
            EventContent content = itC.next();
            cbm.addElement(content.label());
        }
    }

    /** generate the graphical components */
    private JPanel initComponents()
    {
	
	JPanel jPanel = new JPanel();
	
        jTextFieldStreamLabel = new JTextField();
        jLabelStreamLabel = new JLabel();
        jLabelEventContent = new JLabel();
        jComboBoxEventContent = new JComboBox();
        jButtonOK = new JButton();
        jButtonCancel = new JButton();
	
        jTextFieldStreamLabel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    jTextFieldStreamLabelActionPerformed(evt);
		}
	    });
	
        jLabelStreamLabel.setText("Stream Name:");
	
        jLabelEventContent.setText("Event Content:");

        jComboBoxEventContent.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldStreamLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabelStreamLabel)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxEventContent, 0, 377, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabelEventContent))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(589, Short.MAX_VALUE)
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
                    .addComponent(jLabelStreamLabel)
                    .addComponent(jLabelEventContent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldStreamLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxEventContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOK)
                    .addComponent(jButtonCancel))
                .addContainerGap())
        );

	return jPanel;
    }

    private void jButtonOKActionPerformed(ActionEvent evt)
    {
        String streamLabel = jTextFieldStreamLabel.getText();
        String contentLabel = (String)jComboBoxEventContent.getSelectedItem();
        EventContent content = config.content(contentLabel);
        if (content==null) {
            content = config.insertContent(config.contentCount(),
                                           "hltEventContent" + streamLabel);
        }
        stream = content.insertStream(streamLabel);
        setVisible(false);
    }
    private void jButtonCancelActionPerformed(ActionEvent evt)
    {
        setVisible(false);
    }
    private void jTextFieldStreamLabelActionPerformed(ActionEvent evt)
    {
        String streamLabel = jTextFieldStreamLabel.getText();
        if (config.stream(streamLabel)==null) jButtonOK.setEnabled(false);
        else jButtonOK.setEnabled(true);
    }




}

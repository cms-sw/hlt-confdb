package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import org.jdesktop.layout.*;

import confdb.data.Directory;
import confdb.data.Configuration;
import confdb.data.ConfigInfo;
    
import confdb.db.ConfDB;


/**
 * SaveConfigurationDialog
 * -----------------------
 * @author Philipp Schieferdecker
 *
 */
public class SaveConfigurationDialog extends ConfigurationDialog
{
    //
    // member data
    //
    
    /** configuration to be saved */
    private Configuration config = null;
    
    /** currently selected directory */
    private Directory selectedDir = null;
    
    /** GUI components */
    private JTree      jTreeDirectories     = null;
    private JTextField jTextFieldConfigName = new JTextField();
    private JButton    okButton             = new JButton();
    private JButton    cancelButton         = new JButton();
    
    /** action commands */
    private static final String OK            = new String("OK");
    private static final String CANCEL        = new String("Cancel");
    private static final String ADD_DIRECTORY = new String("Add Directory");
    private static final String RMV_DIRECTORY = new String("Remove Directory");
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public SaveConfigurationDialog(JFrame        frame,
				   ConfDB   database,
				   Configuration config)
    {
	super(frame,database);
	this.config = config;
	
	setTitle("Save Configuration");
	
	createTreeView(new Dimension(200,200));
	jTreeDirectories = this.dirTree;
	setContentPane(createContentPane());
	
	if (config.version()==0) {
	    jTextFieldConfigName.setText(config.name());
	    jTextFieldConfigName.selectAll();
	}
	
	jTextFieldConfigName.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    if (jTextFieldConfigName.getText().equals(new String()))
			okButton.setEnabled(false);
		    else if (selectedDir!=null)
			okButton.setEnabled(true);
		}
	    });


	addMouseListener(new DirectoryTreeMouseListener(this.dirTree,database));
	addTreeSelectionListener(new TreeSelectionListener()
	    {
		public void valueChanged(TreeSelectionEvent ev)
		{
		    JTree  dirTree = (JTree)ev.getSource();
		    Object o       = dirTree.getLastSelectedPathComponent();
		    if (o instanceof Directory) {
			selectedDir = (Directory)o;
			if (jTextFieldConfigName.getText().length()>0&&
			    selectedDir.dbId()>0)
			    okButton.setEnabled(true);
			else
			    okButton.setEnabled(false);
		    }
		    else if (o instanceof ConfigInfo) {
			selectedDir = null;
			dirTree.getSelectionModel().clearSelection();
			okButton.setEnabled(false);
		    }
		}
	    });
    }
    
    
    //
    // member functions
    //
    
    /** 'OK' button pressed */
    public void okButtonActionPerformed(ActionEvent e)
    {
	String    configName  = jTextFieldConfigName.getText();
	Directory parentDir   = selectedDir;
	String    releaseTag  = config.releaseTag();
	
	if (configName.length()>0&&parentDir!=null) {
	    ConfigInfo configInfo = new ConfigInfo(configName,parentDir,releaseTag);
	    config.setConfigInfo(configInfo);
	    validChoice = true;
	    setVisible(false);
	}
    }
    
    /** 'Cancel' button pressed */
    public void cancelButtonActionPerformed(ActionEvent e)
    {
	validChoice = false;
	setVisible(false);
    }
    
    
    //
    // private member functions
    //
    
    /** init GUI components [generated with NetBeans] */
    private JPanel createContentPane()
    {
	JPanel      contentPane  = new JPanel();
        JScrollPane jScrollPane1 = new JScrollPane();
        JLabel      jLabel1      = new JLabel();

        jScrollPane1.setViewportView(jTreeDirectories);

        jLabel1.setText("Configuration Name:");

        okButton.setText("OK");
	okButton.setEnabled(false);
	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    okButtonActionPerformed(e);
		}
	    });
	
        cancelButton.setText("Cancel");
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    cancelButtonActionPerformed(e);
		}
	    });
	
        GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(jScrollPane1,
				 GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(jTextFieldConfigName,
				     GroupLayout.DEFAULT_SIZE,
				     217, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .add(91, 91, 91)
                        .add(okButton, GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(cancelButton,
			     GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                        .add(99, 99, 99)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1,GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextFieldConfigName,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE,
			 GroupLayout.PREFERRED_SIZE))
                .add(19, 19, 19)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap())
        );

	return contentPane;
    }

}


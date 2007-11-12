package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;

import org.jdesktop.layout.*;

import confdb.data.Directory;
import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;

import confdb.db.ConfDB;
import confdb.db.ConfDBSetups;
import confdb.db.DatabaseException;

    
/**
 * ExportConfigurationDialog
 * -------------------------
 * @author Philipp Schieferdecker
 *
 */
public class ExportConfigurationDialog extends ConfigurationDialog
{
    //
    // member data
    //

    /** target database */
    private ConfDB targetDB = null;

    /** currently selected directory in target DB */
    private Directory targetDir = null;
    
    /** predefined database setup */
    private ConfDBSetups dbSetups = new ConfDBSetups();

    /** GUI components */
    private JPanel         jPanelLeft           = new JPanel();
    private JPanel         jPanelRight          = new JPanel();
    private JComboBox      jComboBoxSetup       = new JComboBox(dbSetups
								.labelsAsArray());
    private ButtonGroup    buttonGroup          = new ButtonGroup();
    private JRadioButton   jRadioButtonMySQL    = new JRadioButton();
    private JRadioButton   jRadioButtonOracle   = new JRadioButton();
    private JRadioButton   jRadioButtonNone     = new JRadioButton();
    private JTextField     jTextFieldHost       = new JTextField();
    private JTextField     jTextFieldPort       = new JTextField();
    private JTextField     jTextFieldName       = new JTextField();
    private JTextField     jTextFieldUser       = new JTextField();
    private JPasswordField jTextFieldPwrd       = new JPasswordField();
    private JTextField     jTextFieldConfigName = new JTextField();
    private JScrollPane    jScrollPaneTree      = new JScrollPane();
    private JTree          jTreeDirectories     = null;
    private JButton        connectButton        = new JButton();
    private JButton        exportButton         = new JButton();
    private JButton        cancelButton         = new JButton();
    

    //
    // construction
    //
    
    /** standard constructor */
    public ExportConfigurationDialog(JFrame frame,String targetName)
    {
	super(frame);
	this.targetDB = new ConfDB();
	
	setContentPane(createContentPane());

	jTextFieldConfigName.setText(targetName);
	
	setTitle("Export Configuration");
    
	addComponentListener(new ComponentAdapter() {
		public void componentShown(ComponentEvent ce) {
		    jTextFieldHost.requestFocusInWindow();
		    jTextFieldHost.selectAll();
		}
	    });
    }
    
    
    //
    // member functions
    //
    
    /** retrieve target database */
    public ConfDB targetDB() { return this.targetDB; }
    
    /** retrieve configuration name in target DB */
    public String targetName() { return jTextFieldConfigName.getText(); }
    
    /** retrieve configuration directory in target DB */
    public Directory targetDir() { return this.targetDir; }

    /** type choosen from the combo box */
    public void jComboBoxSetupActionPerformed(ActionEvent e)
    {
	int selectedIndex = jComboBoxSetup.getSelectedIndex();
	jTextFieldHost.setText(dbSetups.host(selectedIndex));
	jTextFieldPort.setText(dbSetups.port(selectedIndex));
	jTextFieldName.setText(dbSetups.name(selectedIndex));
	jTextFieldUser.setText(dbSetups.user(selectedIndex));
	String dbType = dbSetups.type(selectedIndex);
	if      (dbType.equals("mysql"))  jRadioButtonMySQL.setSelected(true);
	else if (dbType.equals("oracle")) jRadioButtonOracle.setSelected(true);
	else                              jRadioButtonNone.setSelected(true);
	
	jTextFieldUser.requestFocusInWindow();
	jTextFieldUser.selectAll();
    }
    
    /** 'Connect' button pressed */
    public void connectButtonActionPerformed(ActionEvent e)
    {
	try {
	    String dbType = buttonGroup.getSelection().getActionCommand();
	    String dbHost = jTextFieldHost.getText();
	    String dbPort = jTextFieldPort.getText();
	    String dbName = jTextFieldName.getText();
	    String dbUrl  = url(dbType,dbHost,dbPort,dbName);
	    String dbUser = jTextFieldUser.getText();
	    String dbPwrd = new String(jTextFieldPwrd.getPassword());
	    
	    targetDB.connect(dbType,dbUrl,dbUser,dbPwrd);
	    
	    this.setDatabase(targetDB);
	    createTreeView(new Dimension(200,200));
	    jTreeDirectories = this.dirTree;
	    jScrollPaneTree.setViewportView(jTreeDirectories);

	    jTreeDirectories
		.addMouseListener(new DirectoryTreeMouseListener(jTreeDirectories,
								 targetDB));
	    jTreeDirectories.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
			jTreeDirectoriesValueChanged(e);
		    }
		});
	    
	    connectButton.setEnabled(false);
	}
	catch (DatabaseException ex) {
	    System.out.println("Failed to connect to DB: "+ex.getMessage());
	}
    }
    
    /** 'Export' button pressed */
    public void exportButtonActionPerformed(ActionEvent e)
    {
	if (targetName().length()>0&&targetDir!=null) validChoice=true;
	else {
	    try { targetDB.disconnect(); } catch (DatabaseException ex) {}
	}
	setVisible(false);
    }

    /** 'Cancel' button pressed */
    public void cancelButtonActionPerformed(ActionEvent e)
    {
	validChoice = false;
	setVisible(false);
    }
    
    /** Target configuration name entered */
    public void jTextFieldConfigNameActionPerformed(ActionEvent e)
    {
	if (targetName().length()>0&&targetDir!=null)
	    exportButton.setEnabled(true);
	else
	    exportButton.setEnabled(false);
    }
    
    /** the selection in the directory tree has changed */
    public void jTreeDirectoriesValueChanged(TreeSelectionEvent e)
    {
	JTree  dirTree = (JTree)e.getSource();
	Object o       = dirTree.getLastSelectedPathComponent();
	if (o instanceof Directory) {
	    Directory d = (Directory)o;
	    targetDir = d;
	    if (jTextFieldConfigName.getText().length()>0&&
		targetDir.dbId()>0)
		exportButton.setEnabled(true);
	    else
		exportButton.setEnabled(false);
	}
	else if (o instanceof ConfigInfo) {
	    exportButton.setEnabled(false);
	    targetDir = null;
	}
    }
    


    //
    // private member functions
    //

    /** compute database url */
    private String url(String dbType,String dbHost,String dbPort,String dbName)
    {
	String result = null;
	if (dbHost=="" || dbPort=="" || dbName=="") return result;
	if (dbType.equals("mysql"))       result = "jdbc:mysql://";
	else if (dbType.equals("oracle")) result = "jdbc:oracle:thin:@//";
	else return result;
	result += dbHost + ":" + dbPort + "/" + dbName;
	return result;
    }

    /** init GUI components [generated with NetBeans] */
    private JPanel createContentPane()
    {
	JPanel contentPane = new JPanel();
        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        JLabel jLabel3 = new JLabel();
        JLabel jLabel4 = new JLabel();
        JLabel jLabel5 = new JLabel();
        JLabel jLabel6 = new JLabel();
        JLabel jLabel7 = new JLabel();
        JLabel jLabel8 = new JLabel();
	JLabel jLabel9 = new JLabel();

	
        jPanelLeft
	    .setBorder(BorderFactory
		       .createTitledBorder(null,
					   "Target Database",
					   TitledBorder.DEFAULT_JUSTIFICATION,
					   TitledBorder.DEFAULT_POSITION,
					   new Font("Dialog", 1, 12)));
        jLabel9.setText("Setup:");
        jLabel3.setText("Host:");
        jLabel4.setText("Port:");
        jLabel5.setText("Name:");
        jLabel6.setText("User:");
        jLabel7.setText("Password:");
        jLabel8.setText("Type:");

	jComboBoxSetup.setBackground(new java.awt.Color(255, 255, 255));
	jComboBoxSetup.setSelectedIndex(0);
	jComboBoxSetup.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		jComboBoxSetupActionPerformed(evt);
	    }
	});
	
	buttonGroup.add(jRadioButtonMySQL);
	buttonGroup.add(jRadioButtonOracle);
	buttonGroup.add(jRadioButtonNone);
	jRadioButtonNone.setSelected(true);
	
        jRadioButtonMySQL.setText("MySQL");
        jRadioButtonMySQL.setActionCommand("mysql");
        jRadioButtonMySQL.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonMySQL.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jRadioButtonOracle.setText("Oracle");
        jRadioButtonOracle.setActionCommand("oracle");
        jRadioButtonOracle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonOracle.setMargin(new java.awt.Insets(0, 0, 0, 0));

        connectButton.setText("Connect");
	connectButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    connectButtonActionPerformed(e);
		}
	    });

	org.jdesktop.layout.GroupLayout jPanelLeftLayout =
	    new org.jdesktop.layout.GroupLayout(jPanelLeft);
        jPanelLeft.setLayout(jPanelLeftLayout);
        jPanelLeftLayout
	    .setHorizontalGroup(jPanelLeftLayout
				.createParallelGroup(org.jdesktop
						     .layout
						     .GroupLayout
						     .LEADING)
				.add(jPanelLeftLayout
				     .createSequentialGroup()
				     .addContainerGap()
				     .add(jPanelLeftLayout
					  .createParallelGroup(org
							       .jdesktop
							       .layout
							       .GroupLayout
							       .LEADING)
					  .add(org.jdesktop
					       .layout
					       .GroupLayout
					       .TRAILING,
					       jPanelLeftLayout
					       .createSequentialGroup()
					       .add(jLabel9)
					       .add(37, 37, 37)
					       .add(jComboBoxSetup, 0, 150,
						    Short.MAX_VALUE))
					  .add(jPanelLeftLayout
					       .createSequentialGroup()
					       .add(jLabel8)
					       .add(42, 42, 42)
					       .add(jRadioButtonMySQL)
					       .addPreferredGap(org.jdesktop
								.layout
								.LayoutStyle
								.RELATED, 35,
								Short.MAX_VALUE)
					       .add(jRadioButtonOracle))
					  .add(org.jdesktop
					       .layout
					       .GroupLayout
					       .TRAILING,
					       jPanelLeftLayout
					       .createSequentialGroup()
					       .add(jLabel3)
					       .add(43, 43, 43)
					       .add(jTextFieldHost,
						    org.jdesktop
						    .layout
						    .GroupLayout
						    .DEFAULT_SIZE, 150,
						    Short.MAX_VALUE))
					  .add(jPanelLeftLayout
					       .createSequentialGroup()
					       .add(jLabel4)
					       .add(47, 47, 47)
					       .add(jTextFieldPort,
						    org.jdesktop
						    .layout
						    .GroupLayout
						    .DEFAULT_SIZE, 150,
						    Short.MAX_VALUE))
					  .add(org.jdesktop
					       .layout
					       .GroupLayout
					       .TRAILING,
					       jPanelLeftLayout
					       .createSequentialGroup()
					       .add(jLabel5)
					       .add(38, 38, 38)
					       .add(jTextFieldName,
						    org.jdesktop
						    .layout
						    .GroupLayout
						    .DEFAULT_SIZE, 149,
						    Short.MAX_VALUE))
					  .add(jPanelLeftLayout
					       .createSequentialGroup()
					       .add(jLabel6)
					       .add(44, 44, 44)
					       .add(jTextFieldUser,
						    org.jdesktop
						    .layout
						    .GroupLayout
						    .DEFAULT_SIZE, 150,
						    Short.MAX_VALUE))
					  .add(jPanelLeftLayout
					       .createSequentialGroup()
					       .add(jLabel7)
					       .addPreferredGap(org.jdesktop
								.layout
								.LayoutStyle
								.RELATED)
					       .add(jTextFieldPwrd,
						    org.jdesktop
						    .layout
						    .GroupLayout
						    .DEFAULT_SIZE, 150,
						    Short.MAX_VALUE))
					  .add(connectButton,
					       org.jdesktop
					       .layout
					       .GroupLayout
					       .DEFAULT_SIZE, 225,
					       Short.MAX_VALUE))
				     .addContainerGap())
				);

        jPanelLeftLayout
	    .setVerticalGroup(jPanelLeftLayout
			      .createParallelGroup(org.jdesktop
						   .layout.GroupLayout.LEADING)
			      .add(jPanelLeftLayout.createSequentialGroup()
				   .addContainerGap()
				   .add(jPanelLeftLayout
					.createParallelGroup(org.jdesktop
							     .layout
							     .GroupLayout.BASELINE)
					.add(jComboBoxSetup,
					     org.jdesktop
					     .layout
					     .GroupLayout
					     .PREFERRED_SIZE,
					     org.jdesktop
					     .layout
					     .GroupLayout
					     .DEFAULT_SIZE,
					     org.jdesktop
					     .layout
					     .GroupLayout
					     .PREFERRED_SIZE)
					.add(jLabel9))
				   .add(30, 30, 30)
				   .add(jPanelLeftLayout
					.createParallelGroup(org.jdesktop
							     .layout
							     .GroupLayout.BASELINE)
					.add(jLabel8)
					.add(jRadioButtonMySQL)
					.add(jRadioButtonOracle))
				   .addPreferredGap(org.jdesktop
						    .layout
						    .LayoutStyle
						    .RELATED)
				   .add(jPanelLeftLayout
					.createParallelGroup(org.jdesktop
							     .layout
							     .GroupLayout.BASELINE)
					.add(jTextFieldHost,
					     org.jdesktop
					     .layout
					     .GroupLayout
					     .PREFERRED_SIZE,
					     org.jdesktop
					     .layout
					     .GroupLayout.DEFAULT_SIZE,
					     org.jdesktop
					     .layout
					     .GroupLayout.PREFERRED_SIZE)
					.add(jLabel3))
				   .addPreferredGap(org.jdesktop
						    .layout
						    .LayoutStyle
						    .RELATED)
				   .add(jPanelLeftLayout
					.createParallelGroup(org.jdesktop
							     .layout
							     .GroupLayout.BASELINE)
					.add(jLabel4)
					.add(jTextFieldPort,
					     org.jdesktop
					     .layout
					     .GroupLayout
					     .PREFERRED_SIZE,
					     org.jdesktop
					     .layout
					     .GroupLayout
					     .DEFAULT_SIZE,
					     org.jdesktop
					     .layout
					     .GroupLayout.PREFERRED_SIZE))
				   .addPreferredGap(org.jdesktop
						    .layout
						    .LayoutStyle.RELATED)
				   .add(jPanelLeftLayout
					.createParallelGroup(org.jdesktop
							     .layout
							     .GroupLayout.BASELINE)
					.add(jTextFieldName,
					     org.jdesktop
					     .layout
					     .GroupLayout.PREFERRED_SIZE,
					     org.jdesktop
					     .layout.GroupLayout
					     .DEFAULT_SIZE,
					     org.jdesktop
					     .layout.GroupLayout
					     .PREFERRED_SIZE)
					.add(jLabel5))
				   .addPreferredGap(org.jdesktop
						    .layout.LayoutStyle.RELATED)
				   .add(jPanelLeftLayout
					.createParallelGroup(org.jdesktop
							     .layout.GroupLayout
							     .BASELINE)
					.add(jLabel6)
					.add(jTextFieldUser,
					     org.jdesktop
					     .layout.GroupLayout.PREFERRED_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.DEFAULT_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.PREFERRED_SIZE))
				   .addPreferredGap(org.jdesktop
						    .layout.LayoutStyle.RELATED)
				   .add(jPanelLeftLayout
					.createParallelGroup(org.jdesktop
							     .layout.GroupLayout
							     .BASELINE)
					.add(jLabel7)
					.add(jTextFieldPwrd,
					     org.jdesktop
					     .layout.GroupLayout.PREFERRED_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.DEFAULT_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.PREFERRED_SIZE))
				   .addPreferredGap(org.jdesktop
						    .layout.LayoutStyle.RELATED, 29,
						    Short.MAX_VALUE)
				   .add(connectButton)
				   .addContainerGap())
			      );

        jPanelRight.setBorder(javax.swing.BorderFactory
			      .createTitledBorder("Target Name / Directory"));
        jLabel1.setText("Name:");
        jLabel2.setText("Directory:");

        jScrollPaneTree.setViewportView(jTreeDirectories);

        org.jdesktop.layout.GroupLayout jPanelRightLayout =
	    new org.jdesktop.layout.GroupLayout(jPanelRight);
        jPanelRight.setLayout(jPanelRightLayout);
        jPanelRightLayout
	    .setHorizontalGroup(jPanelRightLayout
				.createParallelGroup(org.jdesktop
						     .layout.GroupLayout.LEADING)
				.add(jPanelRightLayout.createSequentialGroup()
				     .addContainerGap()
				     .add(jPanelRightLayout
					  .createParallelGroup(org.jdesktop
							       .layout.GroupLayout
							       .LEADING)
					  .add(jScrollPaneTree,
					       org.jdesktop
					       .layout.GroupLayout.DEFAULT_SIZE,
					       231, Short.MAX_VALUE)
					  .add(jPanelRightLayout
					       .createSequentialGroup()
					       .add(jLabel1)
					       .addPreferredGap(org.jdesktop
								.layout.LayoutStyle
								.RELATED)
					       .add(jTextFieldConfigName,
						    org.jdesktop
						    .layout.GroupLayout.DEFAULT_SIZE,
						    181, Short.MAX_VALUE))
					  .add(jLabel2))
				     .addContainerGap())
				);

        jPanelRightLayout
	    .setVerticalGroup(jPanelRightLayout
			      .createParallelGroup(org.jdesktop
						   .layout.GroupLayout.LEADING)
			      .add(jPanelRightLayout
				   .createSequentialGroup()
				   .addContainerGap()
				   .add(jPanelRightLayout
					.createParallelGroup(org.jdesktop
							     .layout.GroupLayout
							     .BASELINE)
					.add(jLabel1)
					.add(jTextFieldConfigName,
					     org.jdesktop
					     .layout.GroupLayout.PREFERRED_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.DEFAULT_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.PREFERRED_SIZE))
				   .addPreferredGap(org.jdesktop
						    .layout.LayoutStyle.RELATED)
				   .add(jLabel2)
				   .addPreferredGap(org.jdesktop
						    .layout.LayoutStyle.RELATED)
				   .add(jScrollPaneTree,
					org.jdesktop
					.layout.GroupLayout.DEFAULT_SIZE, 202,
					Short.MAX_VALUE)
				   .addContainerGap())
			      );
	
        org.jdesktop.layout.GroupLayout layout =
	    new org.jdesktop.layout.GroupLayout(contentPane);
        contentPane.setLayout(layout);
        layout
	    .setHorizontalGroup(layout
				.createParallelGroup(org.jdesktop
						     .layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
				     .add(0, 0, 0)
				     .add(layout
					  .createParallelGroup(org.jdesktop
							       .layout.GroupLayout
							       .TRAILING)
					  .add(layout.createSequentialGroup()
					       .add(184, 184, 184)
					       .add(cancelButton,
						    org.jdesktop
						    .layout.GroupLayout.DEFAULT_SIZE,
						    81, Short.MAX_VALUE))
					  .add(layout.createSequentialGroup()
					       .add(6, 6, 6)
					       .add(jPanelLeft,
						    org.jdesktop
						    .layout.GroupLayout.DEFAULT_SIZE,
						    org.jdesktop
						    .layout.GroupLayout.DEFAULT_SIZE,
						    Short.MAX_VALUE)))
				     .addPreferredGap(org.jdesktop
						      .layout.LayoutStyle.RELATED)
				     .add(layout
					  .createParallelGroup(org.jdesktop
							       .layout.GroupLayout
							       .LEADING)
					  .add(jPanelRight,
					       org.jdesktop
					       .layout.GroupLayout.DEFAULT_SIZE,
					       org.jdesktop
					       .layout.GroupLayout.DEFAULT_SIZE,
					       Short.MAX_VALUE)
					  .add(layout.createSequentialGroup()
					       .add(exportButton,
						    org.jdesktop
						    .layout.GroupLayout.DEFAULT_SIZE,
						    org.jdesktop
						    .layout.GroupLayout.DEFAULT_SIZE,
						    Short.MAX_VALUE)
					       .add(191, 191, 191)))
				     .addContainerGap())
				);

        layout
	    .setVerticalGroup(layout
			      .createParallelGroup(org.jdesktop
						   .layout.GroupLayout.LEADING)
			      .add(layout.createSequentialGroup()
				   .addContainerGap()
				   .add(layout
					.createParallelGroup(org.jdesktop
							     .layout.GroupLayout
							     .LEADING)
					.add(jPanelRight,
					     org.jdesktop
					     .layout.GroupLayout.DEFAULT_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.DEFAULT_SIZE,
					     Short.MAX_VALUE)
					.add(jPanelLeft,
					     org.jdesktop
					     .layout.GroupLayout.DEFAULT_SIZE,
					     org.jdesktop
					     .layout.GroupLayout.DEFAULT_SIZE,
					     Short.MAX_VALUE))
				   .addPreferredGap(org.jdesktop
						    .layout.LayoutStyle.RELATED)
				   .add(layout.createParallelGroup(org.jdesktop
								   .layout
								   .GroupLayout
								   .BASELINE)
					.add(cancelButton)
					.add(exportButton))
				   .add(13, 13, 13))
			      );

        cancelButton.setText("Cancel");
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    cancelButtonActionPerformed(e);
		}
	    });
	
        exportButton.setText("Export");
        exportButton.setEnabled(false);
	exportButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    exportButtonActionPerformed(e);
		}
	    });
	
	return contentPane;
    }
}

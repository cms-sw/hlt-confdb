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

import confdb.db.CfgDatabase;
import confdb.db.DatabaseException;

    
/**
 * ConfigurationExportDialog
 * -------------------------
 * @author Philipp Schieferdecker
 *
 */
public class ConfigurationExportDialog extends ConfigurationDialog
{
    //
    // member data
    //

    /** target database */
    private CfgDatabase targetDB = null;

    /** currently selected directory in target DB */
    private Directory targetDir = null;
    
    /** setup choices */
    private static final String[] setupChoices =
    { "","CMS Online","HLT Development","MySQL - local","Oracle XE - local"};

    /** GUI components */
    private JPanel         jPanelLeft           = new JPanel();
    private JPanel         jPanelRight          = new JPanel();
    private JComboBox      jComboBoxSetup       = new JComboBox(setupChoices);
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
    public ConfigurationExportDialog(JFrame frame,String targetName)
    {
	super(frame);
	this.targetDB = new CfgDatabase();
	
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
    public CfgDatabase targetDB() { return this.targetDB; }
    
    /** retrieve configuration name in target DB */
    public String targetName() { return jTextFieldConfigName.getText(); }
    
    /** retrieve configuration directory in target DB */
    public Directory targetDir() { return this.targetDir; }

    /** type choosen from the combo box */
    public void jComboBoxSetupActionPerformed(ActionEvent e)
    {
	String setup = (String)jComboBoxSetup.getSelectedItem();
	if (setup.equals(new String())) {
	    jRadioButtonNone.setSelected(true);
	    jTextFieldHost.setText("");
	    jTextFieldPort.setText("");
	    jTextFieldName.setText("");
	    jTextFieldUser.setText("");
	    jTextFieldPwrd.setText("");
	    jTextFieldHost.requestFocusInWindow();
	    jTextFieldHost.selectAll();
	}
	else if (setup.equals("CMS Online")) {
	    jRadioButtonOracle.setSelected(true);
	    jTextFieldHost.setText("oracms.cern.ch");
	    jTextFieldPort.setText("10121");
	    jTextFieldName.setText("OMDS");
	    jTextFieldUser.setText("cms_hlt_writer");
	    jTextFieldPwrd.setText("");
	    jTextFieldPwrd.requestFocusInWindow();
	    jTextFieldPwrd.selectAll();
	}
	else if (setup.equals("HLT Development")) {
	    jRadioButtonOracle.setSelected(true);
	    jTextFieldHost.setText("int2r1-v.cern.ch");
	    jTextFieldPort.setText("10121");
	    jTextFieldName.setText("int2r_lb.cern.ch");
	    jTextFieldUser.setText("cms_hlt_writer");
	    jTextFieldPwrd.setText("");
	    jTextFieldPwrd.requestFocusInWindow();
	    jTextFieldPwrd.selectAll();
	}
	else if (setup.equals("MySQL - local")) {
	    jRadioButtonMySQL.setSelected(true);
	    jTextFieldHost.setText("localhost");
	    jTextFieldPort.setText("3306");
	    jTextFieldName.setText("hltdb");
	    jTextFieldUser.setText("username");
	    jTextFieldPwrd.setText("");
	    jTextFieldUser.requestFocusInWindow();
	    jTextFieldUser.selectAll();
	}
	else if (setup.equals("Oracle XE - local")) {
	    jRadioButtonOracle.setSelected(true);
	    jTextFieldHost.setText("localhost");
	    jTextFieldPort.setText("1521");
	    jTextFieldName.setText("XE");
	    jTextFieldUser.setText("username");
	    jTextFieldPwrd.setText("");
	    jTextFieldUser.requestFocusInWindow();
	    jTextFieldUser.selectAll();
	}
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
	    if (jTextFieldConfigName.getText().length()>0)
		exportButton.setEnabled(true);
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
	
        GroupLayout jPanelLeftLayout = new GroupLayout(jPanelLeft);
        jPanelLeft.setLayout(jPanelLeftLayout);
        jPanelLeftLayout.setHorizontalGroup(
            jPanelLeftLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelLeftLayout.createSequentialGroup()
		 .addContainerGap()
		 .add(jPanelLeftLayout.createParallelGroup(GroupLayout.LEADING)
		      .add(GroupLayout.TRAILING,
			   jPanelLeftLayout.createSequentialGroup()
			   .add(jLabel9)
			   .add(37, 37, 37)
			   .add(jComboBoxSetup, 0, 150, Short.MAX_VALUE))
		      .add(jPanelLeftLayout.createSequentialGroup()
			   .add(jLabel8)
			   .add(42, 42, 42)
			   .add(jRadioButtonMySQL)
			   .addPreferredGap(LayoutStyle.RELATED, 35, Short.MAX_VALUE)
			   .add(jRadioButtonOracle))
		      .add(GroupLayout.TRAILING,
			   jPanelLeftLayout.createSequentialGroup()
			   .add(jLabel3)
			   .add(43, 43, 43)
			   .add(jTextFieldHost,
				GroupLayout.DEFAULT_SIZE,150,Short.MAX_VALUE))
		      .add(jPanelLeftLayout.createSequentialGroup()
			   .add(jLabel4)
			   .add(47, 47, 47)
			   .add(jTextFieldPort,
				GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
		      .add(GroupLayout.TRAILING,
			   jPanelLeftLayout.createSequentialGroup()
			   .add(jLabel5)
			   .addPreferredGap(LayoutStyle.RELATED, 38, Short.MAX_VALUE)
			   .add(jTextFieldName,
				GroupLayout.PREFERRED_SIZE,149,
				GroupLayout.PREFERRED_SIZE))
		      .add(jPanelLeftLayout.createSequentialGroup()
			   .add(jLabel6)
			   .add(44, 44, 44)
			   .add(jTextFieldUser,
				GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
		      .add(jPanelLeftLayout.createSequentialGroup()
			   .add(jLabel7)
			   .addPreferredGap(LayoutStyle.RELATED)
			   .add(jTextFieldPwrd,
				GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
		      .add(connectButton,
			   GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
		 .addContainerGap())
	    );

        jPanelLeftLayout.setVerticalGroup(
            jPanelLeftLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelLeftLayout.createSequentialGroup()
		 .addContainerGap()
		 .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
		      .add(jComboBoxSetup,
			   GroupLayout.PREFERRED_SIZE,
			   GroupLayout.DEFAULT_SIZE,
			   GroupLayout.PREFERRED_SIZE)
		      .add(jLabel9))
		 .add(30, 30, 30)
		 .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
		      .add(jLabel8)
		      .add(jRadioButtonMySQL)
		      .add(jRadioButtonOracle))
		 .addPreferredGap(LayoutStyle.RELATED)
		 .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
		      .add(jTextFieldHost,
			   GroupLayout.PREFERRED_SIZE,
			   GroupLayout.DEFAULT_SIZE,
			   GroupLayout.PREFERRED_SIZE)
		      .add(jLabel3))
		 .addPreferredGap(LayoutStyle.RELATED)
		 .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
		      .add(jLabel4)
		      .add(jTextFieldPort,
			   GroupLayout.PREFERRED_SIZE,
			   GroupLayout.DEFAULT_SIZE,
			   GroupLayout.PREFERRED_SIZE))
		 .addPreferredGap(LayoutStyle.RELATED)
		 .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
		      .add(jTextFieldName,
			   GroupLayout.PREFERRED_SIZE,
			   GroupLayout.DEFAULT_SIZE,
			   GroupLayout.PREFERRED_SIZE)
		      .add(jLabel5))
		 .addPreferredGap(LayoutStyle.RELATED)
		 .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
		      .add(jLabel6)
		      .add(jTextFieldUser,
			   GroupLayout.PREFERRED_SIZE,
			   GroupLayout.DEFAULT_SIZE,
			   GroupLayout.PREFERRED_SIZE))
		 .addPreferredGap(LayoutStyle.RELATED)
		 .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
		      .add(jLabel7)
		      .add(jTextFieldPwrd,
			   GroupLayout.PREFERRED_SIZE,
			   GroupLayout.DEFAULT_SIZE,
			   GroupLayout.PREFERRED_SIZE))
		 .add(29, 29, 29)
		 .add(connectButton)
		 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	    );
	
	jPanelRight
	    .setBorder(BorderFactory
		       .createTitledBorder(null,
					   "Target Name / Directory",
					   TitledBorder.DEFAULT_JUSTIFICATION,
					   TitledBorder.DEFAULT_POSITION,
					   new Font("Dialog", 1, 12)));
	
        jLabel1.setText("Name:");
        jLabel2.setText("Directory:");
	
        //jScrollPaneTree.setViewportView(jTreeDirectories);
	
        GroupLayout jPanelRightLayout = new GroupLayout(jPanelRight);
        jPanelRight.setLayout(jPanelRightLayout);
        jPanelRightLayout.setHorizontalGroup(
            jPanelRightLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelRightLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelRightLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(jScrollPaneTree,
			 GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                    .add(jPanelRightLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(jTextFieldConfigName,
			     GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
                    .add(jLabel2))
                .addContainerGap())
        );
        jPanelRightLayout.setVerticalGroup(
            jPanelRightLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelRightLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelRightLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextFieldConfigName,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jScrollPaneTree, GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addContainerGap())
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
	
        GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(jPanelLeft,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE,
			 GroupLayout.PREFERRED_SIZE)
                    .add(cancelButton))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(jPanelRight,
			 GroupLayout.DEFAULT_SIZE,
			 GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(exportButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(jPanelRight,
			 GroupLayout.DEFAULT_SIZE,
			 GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanelLeft,
			 GroupLayout.DEFAULT_SIZE,
			 GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(exportButton))
                .add(13, 13, 13))
        );
	
	return contentPane;
    }
}

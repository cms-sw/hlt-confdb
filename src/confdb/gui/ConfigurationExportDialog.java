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
    
    /** GUI components */
    private JPanel         jPanelLeft           = new JPanel();
    private JPanel         jPanelRight          = new JPanel();
    private ButtonGroup    buttonGroup          = new ButtonGroup();
    private JRadioButton   jRadioButtonMySQL    = new JRadioButton();
    private JRadioButton   jRadioButtonOracle   = new JRadioButton();
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

	
        jPanelLeft
	    .setBorder(BorderFactory
		       .createTitledBorder(null,
					   "Target Database",
					   TitledBorder.DEFAULT_JUSTIFICATION,
					   TitledBorder.DEFAULT_POSITION,
					   new Font("Dialog", 1, 12)));
        jLabel3.setText("Host:");
        jLabel4.setText("Port:");
        jLabel5.setText("Name:");
        jLabel6.setText("User:");
        jLabel7.setText("Password:");
        jLabel8.setText("Type:");

	buttonGroup.add(jRadioButtonMySQL);
	buttonGroup.add(jRadioButtonOracle);
	jRadioButtonMySQL.setSelected(true);
	
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
                    .add(connectButton,
			 GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                    .add(jPanelLeftLayout.createSequentialGroup()
                        .add(jPanelLeftLayout.createParallelGroup(GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jLabel3)
                            .add(jLabel5)
                            .add(jLabel8)
                            .add(jLabel6)
                            .add(jLabel7))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(jPanelLeftLayout.createParallelGroup(GroupLayout
								  .TRAILING)
                            .add(jPanelLeftLayout.createSequentialGroup()
                                .add(jRadioButtonMySQL)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(jRadioButtonOracle))
                            .add(jTextFieldName,
				 GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .add(jTextFieldPort,
				 GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .add(jTextFieldHost,
				 GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .add(jTextFieldUser,
				 GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .add(jTextFieldPwrd,
				 GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanelLeftLayout.setVerticalGroup(
            jPanelLeftLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelLeftLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(jRadioButtonOracle)
                    .add(jRadioButtonMySQL))
                .add(23, 23, 23)
                .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jTextFieldHost,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(22, 22, 22)
                .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jTextFieldPort,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(23, 23, 23)
                .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jTextFieldName,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .add(27, 27, 27)
                .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jTextFieldUser,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(26, 26, 26)
                .add(jPanelLeftLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jTextFieldPwrd,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(24, 24, 24)
                .add(connectButton)
                .addContainerGap(13, Short.MAX_VALUE))
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
                .add(jScrollPaneTree, GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
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

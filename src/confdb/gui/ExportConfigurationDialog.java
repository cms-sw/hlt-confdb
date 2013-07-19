package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;

import javax.swing.*;

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
public class ExportConfigurationDialog extends JDialog
{
    //
    // member data
    //

    /** reference to application frame */
    private JFrame jFrame = null;
    
    /** release tag of the configuration to be exported */
    private String releaseTag = "";

    /** target database */
    private ConfDB targetDB = null;

    /** currently selected directory in target DB */
    private Directory targetDir = null;
    
    /** predefined database setup */
    private ConfDBSetups dbSetups = new ConfDBSetups();

    /** indiace if a valid choice was made */
    private boolean validChoice = false;

    /** directory tree model */
    private DirectoryTreeModel treeModel = null;

    /** GUI components */
    private JPanel         jPanelLeft           = new JPanel();
    private JPanel         jPanelRight          = new JPanel();
    private JComboBox      jComboBoxSetup       = new JComboBox(dbSetups.labelsAsArray());
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
    private JTree          jTreeDirectories;
    private JButton        jButtonConnect       = new JButton();
    private JButton        jButtonExport        = new JButton();
    private JButton        jButtonCancel        = new JButton();
    

    //
    // construction
    //
    
    /** standard constructor */
    public ExportConfigurationDialog(JFrame jFrame,
                                     String releaseTag,String targetName)
    {
        super(jFrame,true);
        this.jFrame = jFrame;
        this.releaseTag = releaseTag;
        targetDB = new ConfDB();
        
        setContentPane(createContentPane());
        
        jTextFieldConfigName.setText(targetName);
        
        setTitle("Export Configuration");
    
        // register listener callbacks
        addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent ce) {
                    jTextFieldHost.requestFocusInWindow();
                    jTextFieldHost.selectAll();
                }
            });
        jComboBoxSetup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jComboBoxSetupActionPerformed(e);
                }
            });
        jTextFieldPwrd.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jTextFieldPwrdActionPerformed(e);
                }
            });
        jButtonConnect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jButtonConnectActionPerformed(e);
                }
            });
        jButtonCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jButtonCancelActionPerformed(e);
                }
            });
        jButtonExport.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jButtonExportActionPerformed(e);
                }
            });
        jTextFieldConfigName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jTextFieldConfigNameActionPerformed(e);
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

    /** indicate if a valid choice was made */
    public boolean validChoice() { return this.validChoice; }


    //
    // private member functions
    //
    
    private void jComboBoxSetupActionPerformed(ActionEvent e)
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
    private void jTextFieldPwrdActionPerformed(ActionEvent e)
    {
        jButtonConnectActionPerformed(e);
    }
    private void jButtonConnectActionPerformed(ActionEvent e)
    {
        try {
            String dbType = buttonGroup.getSelection().getActionCommand();
            String dbHost = jTextFieldHost.getText();
            String dbPort = jTextFieldPort.getText();
            String dbName = jTextFieldName.getText();
            //String dbUrl  = url(dbType,dbHost,dbPort,dbName);
            String dbUser = jTextFieldUser.getText();
            String dbPwrd = new String(jTextFieldPwrd.getPassword());
            String dbUrl =targetDB.setDbParameters(dbPwrd, dbName, dbHost, dbPort);
            targetDB.connect(dbType,dbUrl,dbUser,dbPwrd);
         //   targetDB.getReleaseId(releaseTag); // check
            
            Directory rootDir = targetDB.loadConfigurationTree();
            treeModel = new DirectoryTreeModel(rootDir);
            jTreeDirectories = new JTree(treeModel);
            jTreeDirectories.setEditable(true);
            jTreeDirectories.addMouseListener(new DirectoryTreeMouseListener(jTreeDirectories, targetDB));
            jTreeDirectories.setCellRenderer(new DirectoryTreeCellRenderer());
            jTreeDirectories.setCellEditor(new DirectoryTreeCellEditor(jTreeDirectories, new DirectoryTreeCellRenderer()));
            
            // register additional listener callbacks
            jTreeDirectories.addTreeSelectionListener(new TreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent e) {
                        jTreeDirectoriesValueChanged(e);
                    }
                });
            jTextFieldConfigName.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        jTextFieldConfigNameActionPerformed(e);
                    }
                });
            jTextFieldConfigName
                .getDocument().addDocumentListener(new DocumentListener() {
                        public void insertUpdate(DocumentEvent e) {
                            jTextFieldConfigNameInsertUpdate(e);
                        }
                        public void removeUpdate(DocumentEvent e) {
                            jTextFieldConfigNameRemoveUpdate(e);
                        }
                        public void changedUpdate(DocumentEvent e) {}
                    });
            jScrollPaneTree.setViewportView(jTreeDirectories);      
            jButtonConnect.setEnabled(false);
        }
        catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(jFrame,ex.getMessage(),"",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
    private void jButtonExportActionPerformed(ActionEvent e)
    {
        validChoice=true;
        setVisible(false);
    }
    private void jButtonCancelActionPerformed(ActionEvent e)
    {
        validChoice = false;
        try { targetDB.disconnect(); } catch (DatabaseException ex) {}
        setVisible(false);
    }
    private void jTextFieldConfigNameActionPerformed(ActionEvent e)
    {
        if (jButtonExport.isEnabled()) jButtonExportActionPerformed(e);
    }
    private void jTextFieldConfigNameInsertUpdate(DocumentEvent e)
    {
        updateExportButton();
    }
    private void jTextFieldConfigNameRemoveUpdate(DocumentEvent e)
    {
        updateExportButton();
    }
    private void jTreeDirectoriesValueChanged(TreeSelectionEvent e)
    {
        Object o = jTreeDirectories.getLastSelectedPathComponent();
        if (o instanceof Directory) {
            targetDir = (Directory)o;
            updateExportButton();
        }
        else if (o==null||(o instanceof ConfigInfo)) {
            jButtonExport.setEnabled(false);
            targetDir = null;
        }
    }
    
    private void updateExportButton()
    {
        if (targetDir==null) {
            jButtonExport.setEnabled(false);
            return;
        }
        String configName = jTextFieldConfigName.getText();
        if (configName.length()==0) {
            jButtonExport.setEnabled(false);
            return;
        }
        for (int i=0;i<targetDir.configInfoCount();i++) {
            if (targetDir.configInfo(i).name().equals(configName)) {
                jButtonExport.setEnabled(false);
                return;
            }
        }
        jButtonExport.setEnabled(true);
    }

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

        jButtonConnect.setText("Connect");

        javax.swing.GroupLayout jPanelLeftLayout =
            new javax.swing.GroupLayout(jPanelLeft);
        jPanelLeft.setLayout(jPanelLeftLayout);
        jPanelLeftLayout.setHorizontalGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanelLeftLayout.createSequentialGroup()
                                     .addContainerGap()
                                     .addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, 
                                               jPanelLeftLayout.createSequentialGroup()
                                               .addComponent(jLabel9)
                                               .addGap(37)
                                               .addComponent(jComboBoxSetup, 0, 150, Short.MAX_VALUE))
                                          .addGroup(jPanelLeftLayout.createSequentialGroup()
                                               .addComponent(jLabel8)
                                               .addGap(42)
                                               .addComponent(jRadioButtonMySQL)
                                               .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35,
                                                                Short.MAX_VALUE)
                                               .addComponent(jRadioButtonOracle))
                                          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                               jPanelLeftLayout.createSequentialGroup()
                                               .addComponent(jLabel3)
                                               .addGap(43)
                                               .addComponent(jTextFieldHost,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                    Short.MAX_VALUE))
                                          .addGroup(jPanelLeftLayout.createSequentialGroup()
                                               .addComponent(jLabel4)
                                               .addGap(47)
                                               .addComponent(jTextFieldPort,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                    Short.MAX_VALUE))
                                          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                               jPanelLeftLayout.createSequentialGroup()
                                               .addComponent(jLabel5)
                                               .addGap(38)
                                               .addComponent(jTextFieldName,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE, 149,
                                                    Short.MAX_VALUE))
                                          .addGroup(jPanelLeftLayout.createSequentialGroup()
                                               .addComponent(jLabel6)
                                               .addGap(44)
                                               .addComponent(jTextFieldUser,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                    Short.MAX_VALUE))
                                          .addGroup(jPanelLeftLayout.createSequentialGroup()
                                               .addComponent(jLabel7)
                                               .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                               .addComponent(jTextFieldPwrd,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                    Short.MAX_VALUE))
                                          .addComponent(jButtonConnect,
                                               javax.swing.GroupLayout.DEFAULT_SIZE, 225,
                                               Short.MAX_VALUE))
                                     .addContainerGap())
                                );

        jPanelLeftLayout.setVerticalGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addGroup(jPanelLeftLayout.createSequentialGroup()
                                   .addContainerGap()
                                   .addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jComboBoxSetup,
                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel9))
                                   .addGap(30)
                                   .addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel8)
                                        .addComponent(jRadioButtonMySQL)
                                        .addComponent(jRadioButtonOracle))
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                   .addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextFieldHost,
                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3))
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                   .addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jTextFieldPort,
                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.PREFERRED_SIZE))
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                   .addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextFieldName,
                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5))
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                   .addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(jTextFieldUser,
                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.PREFERRED_SIZE))
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                   .addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(jTextFieldPwrd,
                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.PREFERRED_SIZE))
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29,
                                                    Short.MAX_VALUE)
                                   .addComponent(jButtonConnect)
                                   .addContainerGap())
                              );

        jPanelRight.setBorder(javax.swing.BorderFactory.createTitledBorder("Target Name / Directory"));
        jLabel1.setText("Name:");
        jLabel2.setText("Directory:");

        jScrollPaneTree.setViewportView(jTreeDirectories);

        javax.swing.GroupLayout jPanelRightLayout =
            new javax.swing.GroupLayout(jPanelRight);
        jPanelRight.setLayout(jPanelRightLayout);
        jPanelRightLayout.setHorizontalGroup(jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanelRightLayout.createSequentialGroup()
                                     .addContainerGap()
                                     .addGroup(jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                          .addComponent(jScrollPaneTree,
                                               javax.swing.GroupLayout.DEFAULT_SIZE,
                                               231, Short.MAX_VALUE)
                                          .addGroup(jPanelRightLayout.createSequentialGroup()
                                               .addComponent(jLabel1)
                                               .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                               .addComponent(jTextFieldConfigName,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE,
                                                    181, Short.MAX_VALUE))
                                          .addComponent(jLabel2))
                                     .addContainerGap())
                                );

        jPanelRightLayout.setVerticalGroup(jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addGroup(jPanelRightLayout.createSequentialGroup()
                                   .addContainerGap()
                                   .addGroup(jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jTextFieldConfigName,
                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.PREFERRED_SIZE))
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                   .addComponent(jLabel2)
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                   .addComponent(jScrollPaneTree,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, 202,
                                        Short.MAX_VALUE)
                                   .addContainerGap())
                              );
        
        javax.swing.GroupLayout layout =
            new javax.swing.GroupLayout(contentPane);
        contentPane.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                     .addGap(0)
                                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                          .addGroup(layout.createSequentialGroup()
                                               .addGap(184)
                                               .addComponent(jButtonCancel,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE,
                                                    81, Short.MAX_VALUE))
                                          .addGroup(layout.createSequentialGroup()
                                               .addGap(6)
                                               .addComponent(jPanelLeft,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)))
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                          .addComponent(jPanelRight,
                                               javax.swing.GroupLayout.DEFAULT_SIZE,
                                               javax.swing.GroupLayout.DEFAULT_SIZE,
                                               Short.MAX_VALUE)
                                          .addGroup(layout.createSequentialGroup()
                                               .addComponent(jButtonExport,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE,
                                                    javax.swing.GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                               .addGap(191)))
                                     .addContainerGap())
                                );

        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addGroup(layout.createSequentialGroup()
                                   .addContainerGap()
                                   .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanelRight,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             Short.MAX_VALUE)
                                        .addComponent(jPanelLeft,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             javax.swing.GroupLayout.DEFAULT_SIZE,
                                             Short.MAX_VALUE))
                                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                   .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButtonCancel)
                                        .addComponent(jButtonExport))
                                   .addGap(13))
                              );

        jButtonCancel.setText("Cancel");
        jButtonExport.setText("Export");
        jButtonExport.setEnabled(false);

        return contentPane;
    }
}

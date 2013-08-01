package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.util.concurrent.ExecutionException;

import confdb.data.*;
import confdb.diff.*;
import confdb.db.*;


/**
 * DiffDialog
 * ----------
 * @author Philipp Schieferdecker
 *
 * Show the difference between two configurations in the current
 * ConfDB instance.
 */
public class DiffDialog extends JDialog
{
    //
    // member data
    //
    
    /** the application frame */
    private JFrame jFrame;

    /** database instance */
    private ConfDB database;

    /** GUI components */
    private JTextField   jTextFieldNewConfig  = new javax.swing.JTextField();
    private JComboBox    jComboBoxOldConfig   = new javax.swing.JComboBox();
    private JProgressBar jProgressBarDiff     = new javax.swing.JProgressBar();
    private JEditorPane  jEditorPaneDiff      = new javax.swing.JEditorPane();
    private JButton      jButtonLoadNewConfig = new javax.swing.JButton();
    private JButton      jButtonLoadOldConfig = new javax.swing.JButton();
    private JButton      jButtonApply         = new javax.swing.JButton();    
    private JButton      jButtonClose         = new javax.swing.JButton();    
    
    /** diff tree components */
    private JTree         jTreeDiff;
    private DiffTreeModel treeModel;
    
    /** Diff object, which actually carries out the comparison */
    private IConfiguration newConfig = null;
    private IConfiguration oldConfig = null;
    private Diff           diff      = null;

    private boolean apply = false;

    //
    // construction
    //

    /** standard constructor */
    public DiffDialog(JFrame jFrame,ConfDB database)
    {
	super(jFrame,true);
	this.jFrame = jFrame;
	this.database = database;
	setTitle("Compare Configurations");
	
	// initialize tree
	treeModel = new DiffTreeModel();
	jTreeDiff = new JTree(treeModel);
	jTreeDiff.setRootVisible(false);
	jTreeDiff.setEditable(false);
	jTreeDiff.getSelectionModel()
	    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	jTreeDiff.setCellRenderer(new DiffTreeRenderer());
	
	setContentPane(initComponents());

	// register listeners
	jButtonLoadNewConfig.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonLoadNewConfigActionPerformed(e);
		}
	    });
	jButtonLoadOldConfig.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonLoadOldConfigActionPerformed(e);
		}
	    });
	jComboBoxOldConfig.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxOldConfigActionPerformed(e);
		}
	    });
	jButtonApply.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonApplyActionPerformed(e);
		}
	    });
	jButtonClose.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonCloseActionPerformed(e);
		}
	    });
    }
    
    /** constructor with diff object */
    public DiffDialog(Diff diff)
    {
	//super(true);
	this.diff = diff;
	setTitle("Compare Configurations");
	
	// initialize tree
	treeModel = new DiffTreeModel();
	jTreeDiff = new JTree(treeModel);
	jTreeDiff.setRootVisible(false);
	jTreeDiff.setEditable(false);
	jTreeDiff.getSelectionModel()
	    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	jTreeDiff.setCellRenderer(new DiffTreeRenderer());
	
	setContentPane(initComponents());
	
	// register listeners
	jButtonLoadNewConfig.setEnabled(false);
	jButtonLoadOldConfig.setEnabled(false);
	jComboBoxOldConfig.setEnabled(false);
	jButtonApply.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonApplyActionPerformed(e);
		}
	    });
	jButtonClose.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonCloseActionPerformed(e);
		}
	    });
	
	treeModel.setDiff(diff);
	for (int i=jTreeDiff.getRowCount()-1;i>=0;i--) jTreeDiff.expandRow(i);
	jEditorPaneDiff.setText(diff.printAll());
	
	jTextFieldNewConfig.setText(diff.configName2());
	DefaultComboBoxModel m=(DefaultComboBoxModel)jComboBoxOldConfig.getModel();
	m.removeAllElements();
	m.addElement(diff.configName1());
    }
    

    //
    // member functions
    //

    /** get the diff object for later scrutiny */
    public Diff getDiff() {return diff;}
    /** should the diff be applied to another config? */
    public boolean getApply() {return apply;}

    /** set the new configuration */
    public void setNewConfig(IConfiguration c)
    {
	newConfig = c;
	jTextFieldNewConfig.setText(c.toString());
    }

    /** set the old configuration */
    public void setOldConfigs(ConfigInfo configInfo)
    {
	jComboBoxOldConfigUpdate(configInfo,0,-1);
    }
    
    /** set both configurations and compare */
    public void setConfigurations(IConfiguration oldCfg,IConfiguration newCfg)
    {
	newConfig = newCfg;
	jTextFieldNewConfig.setText(newCfg.toString());
	oldConfig = oldCfg;
	DefaultComboBoxModel m=(DefaultComboBoxModel)jComboBoxOldConfig.getModel();
	m.removeAllElements();
	m.addElement(oldConfig.toString());
	m.setSelectedItem(m.getElementAt(0));
    }

    //
    // private member functions
    //

    // listeners
    public void jButtonLoadNewConfigActionPerformed(ActionEvent e)
    {
	PickConfigurationDialog dialog = 
	    new PickConfigurationDialog(jFrame,"Pick New Configuration",database);
	dialog.pack();
	dialog.setLocationRelativeTo(jFrame);
	dialog.setVisible(true);
	if (dialog.validChoice()) {
	    ConfigInfo configInfo = dialog.configInfo();
	    jTextFieldNewConfig.setText(configInfo.parentDir().name()+"/"+
					configInfo.name()+"/V"+
					configInfo.version());
	    jComboBoxOldConfigUpdate(configInfo,-1,configInfo.version());
	    newConfig = null;
	}
    }
    public void jButtonLoadOldConfigActionPerformed(ActionEvent e)
    {
	PickConfigurationDialog dialog = 
	    new PickConfigurationDialog(jFrame,"Pick Old Configuration",database);
	dialog.pack();
	dialog.setLocationRelativeTo(jFrame);
	dialog.setVisible(true);
	if (dialog.validChoice()) {
	    ConfigInfo configInfo = dialog.configInfo();
	    jComboBoxOldConfigUpdate(configInfo,configInfo.version(),-1);
	}
    }
    public void jComboBoxOldConfigActionPerformed(ActionEvent e)
    {
	String snew = jTextFieldNewConfig.getText();
	String sold = (String)jComboBoxOldConfig.getSelectedItem();
	
	if (snew.length()>0&&sold!=null&&sold.length()>0) {
	    oldConfig = null;
	    compareConfigurations();
	}
    }
    public void jButtonApplyActionPerformed(ActionEvent e)
    {
	apply = true;
	setVisible(false);
    }
    public void jButtonCloseActionPerformed(ActionEvent e)
    {
	apply = false;
	setVisible(false);
    }
    public void jComboBoxOldConfigUpdate(ConfigInfo info,
					 int selectedVersion,int maskedVersion)
    {
	DefaultComboBoxModel m=
	    (DefaultComboBoxModel)jComboBoxOldConfig.getModel();
	m.removeAllElements();
	m.addElement("");
	for (int i=info.versionCount();i>0;i--) {
	    if (i==maskedVersion) continue;
	    String element = info.parentDir().name()+"/"+info.name()+"/V"+i;
	    m.addElement(element);
	    if (i==selectedVersion) m.setSelectedItem(element);
	}
    }
    public void compareConfigurations()
    {
	jButtonApply.setEnabled(false);
	jButtonClose.setEnabled(false);
	DiffThread worker = new DiffThread();
	worker.start();
	jProgressBarDiff.setIndeterminate(true);
	jProgressBarDiff.setString("Compare Configurations ... ");
    }

    
    /** initialize GUI components */
    private JPanel initComponents()
    {
	JPanel jPanel = new JPanel();

	JLabel      jLabel1         = new javax.swing.JLabel();
        JLabel      jLabel2         = new javax.swing.JLabel();
        JTabbedPane jTabbedPane1    = new javax.swing.JTabbedPane();
	JScrollPane jScrollPaneTree = new javax.swing.JScrollPane();
	JScrollPane jScrollPaneText = new javax.swing.JScrollPane();

        jLabel1.setFont(new java.awt.Font("Dialog",1,11));
        jLabel1.setText("New Configuration:");
        jLabel2.setFont(new java.awt.Font("Dialog",1,11));
        jLabel2.setText("Old Configuration:");
	
        jTextFieldNewConfig.setBackground(new java.awt.Color(255, 255, 255));
        jTextFieldNewConfig.setEditable(false);
        jTextFieldNewConfig.setForeground(new java.awt.Color(0, 153, 0));
	
	jProgressBarDiff.setStringPainted(true);
	jProgressBarDiff.setString("");

        jButtonLoadNewConfig.setText("...");
        jButtonLoadOldConfig.setText("...");
        jButtonApply.setText("Apply");
        jButtonClose.setText("Close");
        
	jScrollPaneTree.setViewportView(jTreeDiff);
        jTabbedPane1.addTab("Tree", jScrollPaneTree);
        jScrollPaneText.setViewportView(jEditorPaneDiff);
        jTabbedPane1.addTab("Text", jScrollPaneText);

        jComboBoxOldConfig.setModel(new javax.swing.DefaultComboBoxModel());
        jComboBoxOldConfig.setBackground(new java.awt.Color(255, 255, 255));
        jComboBoxOldConfig.setForeground(Color.red);


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jButtonApply, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                            .addComponent(jButtonClose, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                            .addComponent(jTabbedPane1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldNewConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(15)
                                .addComponent(jComboBoxOldConfig, 0, 638, Short.MAX_VALUE)))
                        .addGap(18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonLoadOldConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonLoadNewConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jProgressBarDiff, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonLoadNewConfig, jButtonLoadOldConfig});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldNewConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLoadNewConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBoxOldConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLoadOldConfig))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBarDiff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonApply)
                .addComponent(jButtonClose)
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {jButtonLoadNewConfig, jTextFieldNewConfig});
        layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {jComboBoxOldConfig, jProgressBarDiff});

	return jPanel;
    }


    //
    // THREADS
    //

    /** compare the two selected configurations */
    private class DiffThread extends SwingWorker<String>
    {
	// member data
	private long startTime;

	// construction
	public DiffThread() {}
	
	// member functions
	
	/** SwingWorker::construct() */
	protected String construct() throws DatabaseException, DiffException
	{
	    startTime = System.currentTimeMillis();
	    if (newConfig==null) {
		int newId=database.getConfigId(jTextFieldNewConfig.getText());
		newConfig=database.loadConfiguration(newId);
	    }
	    if (oldConfig==null) {
		String oldConfigName=(String)jComboBoxOldConfig.getSelectedItem();
		int oldId=database.getConfigId(oldConfigName);
		oldConfig=database.loadConfiguration(oldId);
	    }

	    diff = new Diff(oldConfig,newConfig);
	    diff.compare();
	    treeModel.setDiff(diff);
	    for (int i=jTreeDiff.getRowCount()-1;i>=0;i--)
		jTreeDiff.expandRow(i);
	    jEditorPaneDiff.setText(diff.printAll());
	    return new String("Done!");
	}

	/** SwingWorker::finished() */
	protected void finished()
	{
	    try {
		long elapsedTime = System.currentTimeMillis()-startTime;
		jProgressBarDiff.setString(jProgressBarDiff.getString()+get()+
					   " ("+elapsedTime+")");
	    }
	    catch (ExecutionException e) {
		String errMsg =
		    "Comparison FAILED:\n"+e.getCause().getMessage();
		JOptionPane.showMessageDialog(jFrame,errMsg,"Comparison Failed",
					      JOptionPane.ERROR_MESSAGE,null);
		jProgressBarDiff.setString(jProgressBarDiff.getString()+"FAILED!");
	    }
	    catch (Exception e) {
		e.printStackTrace();
		jProgressBarDiff.setString(jProgressBarDiff.getString()+"FAILED!!");
	    }
	    jProgressBarDiff.setIndeterminate(false);
	    jButtonApply.setEnabled(true);
	    jButtonClose.setEnabled(true);
	}
    }
    
}

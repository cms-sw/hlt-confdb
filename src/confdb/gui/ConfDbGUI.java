package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;

import java.util.concurrent.ExecutionException;

import confdb.data.SoftwareRelease;
import confdb.data.Configuration;
import confdb.data.Directory;
import confdb.data.ConfigInfo;
import confdb.data.Template;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;

import confdb.db.CfgDatabase;
import confdb.db.DatabaseException;

import confdb.migrator.DatabaseMigrator;
import confdb.migrator.ReleaseMigrator;

import confdb.gui.treetable.TreeTableTableModel;


/**
 * ConfDbGUI
 * ---------
 * @author Philipp Schieferdecker
 *
 * Graphical User Interface to create and manipulate cmssw
 * configurations stored in the Configuration Database, ConfDB.
 */
public class ConfDbGUI implements TableModelListener
{
    //
    // member data
    //
    
    /** main frame of the application */
    private JFrame frame = null; 
    
    /** the current software release (collection of all templates) */
    private SoftwareRelease currentRelease = null;

    /** the current configuration */
    private Configuration currentConfig = null;
    
    /** the import configuration */
    private Configuration importConfig = null;
    
    /** handle access to the database */
    private CfgDatabase database = null;

    /** ConverterService */
    private ConverterService converterService = null;
    
    /** tree models for both the current and the import tree */
    private ConfigurationTreeModel currentTreeModel = null;
    private ConfigurationTreeModel importTreeModel = null;
    

    /** GUI components */
    private DatabaseInfoPanel  dbInfoPanel        = new DatabaseInfoPanel();
    private ConfigurationPanel configurationPanel = null;
    private InstancePanel      instancePanel      = null;
    private JProgressBar       progressBar        = null;
    private JTree              currentTree        = null;
    private JTree              importTree         = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public ConfDbGUI(JFrame frame)
    {
	this.frame            = frame;
	this.currentRelease   = new SoftwareRelease();
	this.currentConfig    = new Configuration();
	this.importConfig     = new Configuration();
	this.database         = new CfgDatabase();
	this.converterService = new ConverterService(database);
	
	// current configuration tree
	currentTreeModel = new ConfigurationTreeModel(currentConfig);
	currentTree      = new JTree(currentTreeModel);
	currentTree.setRootVisible(false);
	currentTree.setEditable(true);
	currentTree.getSelectionModel().setSelectionMode(TreeSelectionModel
							.SINGLE_TREE_SELECTION);
	ConfigurationTreeRenderer renderer = new ConfigurationTreeRenderer();
	currentTree.setCellRenderer(renderer);
	currentTree.setCellEditor(new ConfigurationTreeEditor(currentTree,renderer));
	
	ConfigurationTreeMouseListener mouseListener =
	    new ConfigurationTreeMouseListener(currentTree,frame,currentRelease);
	currentTree.addMouseListener(mouseListener);
	currentTreeModel.addTreeModelListener(mouseListener);
	
	// import tree
	importTreeModel = new ConfigurationTreeModel(importConfig);
	importTree      = new JTree(importTreeModel);
	importTree.setRootVisible(false);
	importTree.setEditable(false);
	importTree.getSelectionModel().setSelectionMode(TreeSelectionModel
							.SINGLE_TREE_SELECTION);
	importTree.setCellRenderer(new ConfigurationTreeRenderer());
	
    }
    
    
    //
    // member functions
    //

    /** TableModelListener: tableChanged() */
    public void tableChanged(TableModelEvent e)
    {
	Object source = e.getSource();
	if (source instanceof TreeTableTableModel) {
	    TreeTableTableModel tableModel = (TreeTableTableModel)source;
	    Parameter node = (Parameter)tableModel.changedNode();

	    if (node!=null) {
		Object parent = node.parent();
		while (parent instanceof Parameter) {
		    Parameter p = (Parameter)parent;
		    parent = p.parent();
		}

		currentTreeModel.nodeChanged(node);
		if (parent instanceof ModuleInstance) currentTree.updateUI();
		currentTreeModel.updateLevel1Nodes();
		currentConfig.setHasChanged(true);
	    }
	}
    }
    
    /** connect to the database */
    public void connectToDatabase()
    {
	// close currently open configuration
	if (!closeConfiguration()) return;
	
	// query the database info from the user in a dialog
	DatabaseConnectionDialog dbDialog = new DatabaseConnectionDialog(frame);
	dbDialog.pack();
	dbDialog.setLocationRelativeTo(frame);
	dbDialog.setVisible(true);
	
	// retrieve the database parameters from the dialog
	if (!dbDialog.validChoice()) return;
	String dbType = dbDialog.getDbType();
	String dbHost = dbDialog.getDbHost();
	String dbPort = dbDialog.getDbPort();
	String dbName = dbDialog.getDbName();
	String dbUrl  = dbDialog.getDbUrl();
	String dbUser = dbDialog.getDbUser();
	String dbPwrd = dbDialog.getDbPassword();
	
	try {
	    database.connect(dbType,dbUrl,dbUser,dbPwrd);
	    dbInfoPanel.connectedToDatabase(dbType,dbHost,dbPort,dbName,dbUser);
	}
	catch (DatabaseException e) {
	    String msg = "Failed to connect to DB: " + e.getMessage();
	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
	}
    }
    
    /** connect to the database */
    public void disconnectFromDatabase()
    {
	if (!closeConfiguration()) return;
	
	try {
	    database.disconnect();
	    dbInfoPanel.disconnectedFromDatabase();
	    currentRelease.clear("");
	}
	catch (DatabaseException e) {
	    String msg = "Failed to disconnect from DB: " + e.getMessage();
	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
	}
    }
    
    /** migrate configuration to another database */
    public void exportConfiguration()
    {
	if (!checkConfiguration()) return;
	
	ConfigurationExportDialog dialog =
	    new ConfigurationExportDialog(frame,currentConfig.name());

	dialog.pack();
	dialog.setLocationRelativeTo(frame);
	dialog.setVisible(true);

	if (dialog.validChoice()) {
	    CfgDatabase targetDB   = dialog.targetDB();
	    String      targetName = dialog.targetName();
	    Directory   targetDir  = dialog.targetDir();
	    
	    ConfigurationExportThread worker =
		new ConfigurationExportThread(targetDB,targetName,targetDir);
	    worker.start();
	    progressBar.setIndeterminate(true);
	    progressBar.setVisible(true);
	    progressBar.setString("Migrate Configuration to " +
				  targetDB.dbUrl() + " ... ");
	}
    }
    

    /** new configuration */
    public void newConfiguration()
    {
	if (!closeConfiguration()) return;
	
	ConfigurationNameDialog dialog = new ConfigurationNameDialog(frame,database);
	dialog.pack();
	dialog.setLocationRelativeTo(frame);
	dialog.setVisible(true);
	
	if (dialog.validChoice()) {
	    String cfgName    = dialog.name();
	    String releaseTag = dialog.releaseTag();
	    
	    NewConfigurationThread worker =
		new NewConfigurationThread(cfgName,releaseTag);
	    worker.start();
	    progressBar.setIndeterminate(true);
	    progressBar.setVisible(true);
	    progressBar.setString("Loading Templates for Release " +
				  dialog.releaseTag() + " ... ");
	}
    }
    
    /** open configuration */
    public void openConfiguration()
    {
	if (!closeConfiguration()) return;
	
	OpenConfigurationDialog dialog =
	    new OpenConfigurationDialog(frame,database);
	dialog.pack();
	dialog.setLocationRelativeTo(frame);
	dialog.setVisible(true);
	
	if (dialog.validChoice()) {
	    OpenConfigurationThread worker =
		new OpenConfigurationThread(dialog.configInfo());
	    worker.start();
	    progressBar.setIndeterminate(true);
	    progressBar.setVisible(true);
	    progressBar.setString("Loading Configuration ...");
	}
    }

    /** close configuration */
    public boolean closeConfiguration()
    {
	if (currentConfig.isEmpty()) return true;
	
	if (currentConfig.hasChanged()) {
	    Object[] options = { "OK", "CANCEL" };
	    int answer = 
		JOptionPane.showOptionDialog(null,
					     "The current configuration contains "+
					     "unsaved changes, really close?",
					     "Warning",
					     JOptionPane.DEFAULT_OPTION,
					     JOptionPane.WARNING_MESSAGE,
					     null, options, options[1]);
	    if (answer==1) return false;
	}
	
	currentRelease.clearInstances();
	currentConfig.reset();
	currentTreeModel.setConfiguration(currentConfig);
	configurationPanel.setCurrentConfig(currentConfig);
	instancePanel.clear();
	return true;
    }
    
    /** save configuration */
    public void saveConfiguration()
    {
	if (currentConfig.isEmpty()) return;
	if (!currentConfig.hasChanged()) return;
	if (!checkConfiguration()) return;	

	if (currentConfig.version()==0) {
	    saveAsConfiguration();
	    return;
	}
	
	SaveConfigurationThread worker =
	    new SaveConfigurationThread();
	worker.start();
	progressBar.setIndeterminate(true);
	progressBar.setString("Save Configuration ...");
	progressBar.setVisible(true);
    }
    
    /** saveAs configuration */
    public void saveAsConfiguration()
    {
	if (!checkConfiguration()) return;

	SaveConfigurationDialog dialog =
	    new SaveConfigurationDialog(frame,database,currentConfig);
	dialog.pack();
	dialog.setLocationRelativeTo(frame);
	dialog.setVisible(true);
	
	if (dialog.validChoice()) {
	    SaveConfigurationThread worker =
		new SaveConfigurationThread();
	    worker.start();
	    progressBar.setIndeterminate(true);
	    progressBar.setString("Save Configuration ...");
	    progressBar.setVisible(true);
	}
    }
    
    /** migrate configuration (to another release) */
    public void migrateConfiguration()
    {
	if (!checkConfiguration()) return;
	
	ConfigurationMigrationDialog dialog =
	    new ConfigurationMigrationDialog(frame,database);
	dialog.pack();
	dialog.setLocationRelativeTo(frame);
	dialog.setVisible(true);
	
	String releaseTag = dialog.releaseTag();	
	
	if (releaseTag.length()>0) {
	    ConfigurationMigrationThread worker =
		new ConfigurationMigrationThread(releaseTag);
	    worker.start();
	    progressBar.setIndeterminate(true);
	    progressBar.setVisible(true);
	    progressBar.setString("Migration configuration to release '" +
				  releaseTag + "' ... ");
	}
    }
    

    /** check if configuration is in a storable state */
    public boolean checkConfiguration()
    {
	if (currentConfig.isEmpty()) return false;
	int unsetParamCount = currentConfig.unsetTrackedParameterCount();
	if (unsetParamCount>0) {
	    String msg =
		"The configuration contains " + unsetParamCount +
		" unset tracked parameters. They must be set before saving!";
	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
	    return false;
	}
	return true;
    }
    
    /** show 'create templates' dialog */
    public void createTemplates()
    {
	CreateTemplateDialog dialog = new CreateTemplateDialog(frame,database);
	dialog.pack();
	dialog.setLocationRelativeTo(frame);
	dialog.setVisible(true);
	String releaseTag = currentConfig.releaseTag();
	
	UpdateTemplatesThread worker =
	    new UpdateTemplatesThread(releaseTag);
	worker.start();
	progressBar.setIndeterminate(true);
	progressBar.setVisible(true);
	progressBar.setString("Updating Templates for Release "+releaseTag+" ... ");
    }
    

    /** create the content pane */
    private JPanel createContentPane()
    {
	JPanel contentPane = new JPanel(new GridBagLayout());
	contentPane.setOpaque(true);
	
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 0.5;
	
	c.gridx=0;c.gridy=0; c.weighty=0.01;
	contentPane.add(dbInfoPanel,c);

	instancePanel = new InstancePanel(frame);
	instancePanel.setConfigurationTreeModel(currentTreeModel);
	instancePanel.addTableModelListener(this);
	currentTree.addTreeSelectionListener(instancePanel);
	
	configurationPanel = new ConfigurationPanel(currentTree,importTree,
						    converterService);
	
	JSplitPane  splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					       configurationPanel,
					       instancePanel);
	splitPane.setOneTouchExpandable(true);
	splitPane.setResizeWeight(0.4);
	splitPane.setDividerLocation(0.4);
	
	c.gridx=0;c.gridy=1; c.weighty=0.98;
	contentPane.add(splitPane,c);
	
	JPanel statusPanel = new JPanel(new GridLayout());
	progressBar = new JProgressBar(0);
	progressBar.setIndeterminate(true);
	progressBar.setStringPainted(true);
	progressBar.setVisible(false);
	statusPanel.add(progressBar);

	c.gridx=0;c.gridy=2; c.weighty=0.01;
	contentPane.add(statusPanel,c);
	
	return contentPane;
    }

    /** create the menu bar */
    private void createMenuBar() { new CfgMenuBar(frame,this); }
    
    
    //
    // static member functions
    //
    
    /** create and show the ConfDbGUI */
    private static void createAndShowGUI()
    {
	// create the application's main frame
	JFrame frame = new JFrame("ConfDbGUI");
	
	// create the ConfDbGUI app and set it as the main frame's content pane
	ConfDbGUI app = new ConfDbGUI(frame);
	
	// add the application's content pane to the main frame
	frame.setContentPane(app.createContentPane());
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	// add menu bar to the main frame
	app.createMenuBar();
	
	// display the main frame
	int frameWidth  = (int)(0.75*frame.getToolkit().getScreenSize().getWidth());
	int frameHeight = (int)(0.75*frame.getToolkit().getScreenSize().getHeight());
	int frameX      = (int)(0.125*frame.getToolkit().getScreenSize().getWidth());
	int frameY      = (int)(0.10*frame.getToolkit().getScreenSize().getHeight());
	frame.pack();
	frame.setSize(frameWidth,frameHeight);
	frame.setLocation(frameX,frameY);
	frame.setVisible(true);
		
	// try to etablish a database connection
	app.connectToDatabase();
    }
    

    /**
     * MAIN create and show GUI, thread-safe
     */
    public static void main(String[] args)
    {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {  createAndShowGUI(); }
	    });
    }
    

    //
    // threads *not* to be executed on the EDT
    //

    /**
     * migrate current configuration to another database
     */
    private class ConfigurationExportThread extends SwingWorker<String>
    {
	/** target database */
	private CfgDatabase targetDB = null;
	
	/** name of the configuration in the target DB */
	private String targetName = null;
	
	/** target directory */
	private Directory targetDir = null;
	
	/** database migrator */
	DatabaseMigrator migrator = null;

	/** start time */
	private long startTime;
	
	/** standard constructor */
	public ConfigurationExportThread(CfgDatabase targetDB,
					 String targetName,Directory targetDir)
	{
	    this.targetDB   = targetDB;
	    this.targetName = targetName;
	    this.targetDir  = targetDir;
	}
	
	/** SwingWorker: construct() */
	protected String construct() throws InterruptedException
	{
	    startTime = System.currentTimeMillis();
	    migrator = new DatabaseMigrator(currentConfig,database,targetDB);
	    migrator.migrate(targetName,targetDir);
	    return new String("Done!");
	}
	
	/** SwingWorker: finished */
	protected void finished()
	{
	    try {
		targetDB.disconnect();
		long elapsedTime = System.currentTimeMillis() - startTime;
		progressBar.setString(progressBar.getString() +
				      get() + " (" + elapsedTime + " ms)");
		MigrationReportDialog dialog =
		    new MigrationReportDialog(frame,migrator.releaseMigrator());
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	    }
	    catch (Exception e) {
		System.out.println("EXCEPTION: "+ e.getMessage());
		e.printStackTrace();
		progressBar.setString(progressBar.getString() + "FAILED!");	
	    }
	    progressBar.setIndeterminate(false);
	}
    }
    

    /**
     * load release templates from the database
     */
    private class NewConfigurationThread extends SwingWorker<String>
    {
	/** name of the new configuration */
	private String cfgName = null;
	
	/** release to be loaded */
	private String releaseTag = null;
	
	/** start time */
	private long startTime;
	
	/** standard constructor */
	public NewConfigurationThread(String cfgName,String releaseTag)
	{
	    this.cfgName    = cfgName;
	    this.releaseTag = releaseTag;
	}
	
	/** SwingWorker: construct() */
	protected String construct() throws InterruptedException
	{
	    startTime = System.currentTimeMillis();
	    if (!releaseTag.equals(currentRelease.releaseTag()))
		database.loadSoftwareRelease(releaseTag,currentRelease);
	    return new String("Done!");
	}
	
	/** SwingWorker: finished */
	protected void finished()
	{
	    try {
		currentConfig = new Configuration();
		currentConfig.initialize(new ConfigInfo(cfgName,null,releaseTag),
					 currentRelease);
		currentTreeModel.setConfiguration(currentConfig);
		configurationPanel.setCurrentConfig(currentConfig);
		long elapsedTime = System.currentTimeMillis() - startTime;
		progressBar.setString(progressBar.getString() +
				      get() + " (" + elapsedTime + " ms)");
	    }
	    catch (Exception e) {
		System.out.println("EXCEPTION: "+ e.getMessage());
		e.printStackTrace();
		progressBar.setString(progressBar.getString() + "FAILED!");	
	    }
	    progressBar.setIndeterminate(false);
	}
    }
    

    /**
     * load a configuration from the database
     */
    private class OpenConfigurationThread extends SwingWorker<String>
    {
	/** configuration info */
	private ConfigInfo configInfo = null;
	
	/** start time */
	private long startTime;
	
	/** standard constructor */
	public OpenConfigurationThread(ConfigInfo configInfo)
	{
	    this.configInfo = configInfo;
	}
	
	/** SwingWorker: construct() */
	protected String construct() throws InterruptedException
	{
	    startTime = System.currentTimeMillis();
	    currentConfig = database.loadConfiguration(configInfo,currentRelease);
	    return new String("Done!");
	}
	
	/** SwingWorker: finished */
	protected void finished()
	{
	    try {
		currentTreeModel.setConfiguration(currentConfig);
		configurationPanel.setCurrentConfig(currentConfig);
		long elapsedTime = System.currentTimeMillis() - startTime;
		progressBar.setString(progressBar.getString() +
				      get() + " (" + elapsedTime + " ms)");
	    }
	    catch (ExecutionException e) {
		System.out.println("EXECUTION-EXCEPTION: "+ e.getCause());
		e.printStackTrace();
	    }
	    catch (Exception e) {
		System.out.println("EXCEPTION: "+ e.getMessage());
		e.printStackTrace();
		progressBar.setString(progressBar.getString() + "FAILED!");
	    }
	    progressBar.setIndeterminate(false);
	}
    }

    /**
     * save a configuration in the database
     */
    private class SaveConfigurationThread extends SwingWorker<String>
    {
	/** start time */
	private long startTime;
	
	/** standard constructor */
	public SaveConfigurationThread() {}
	
	/** SwingWorker: construct() */
	protected String construct() throws InterruptedException
	{
	    startTime = System.currentTimeMillis();
	    database.insertConfiguration(currentConfig);
	    return new String("Done!");
	}
	
	/** SwingWorker: finished */
	protected void finished()
	{
	    try {
		currentConfig.setHasChanged(false);
		configurationPanel.setCurrentConfig(currentConfig);
		long elapsedTime = System.currentTimeMillis() - startTime;
		progressBar.setString(progressBar.getString() +
				      get() + " (" + elapsedTime + " ms)");
	    }
	    catch (Exception e) {
		System.out.println("EXCEPTION: "+ e.getMessage());
		e.printStackTrace();
		progressBar.setString(progressBar.getString() + "FAILED!");
	    }
	    progressBar.setIndeterminate(false);
	}
    }

    /**
     * save a configuration in the database
     */
    private class ConfigurationMigrationThread extends SwingWorker<String>
    {
	/** the target release */
	private String targetReleaseTag = null;
	
	/** release migrator */
	private ReleaseMigrator migrator = null;
	
	/** start time */
	private long startTime;
	
	/** standard constructor */
	public ConfigurationMigrationThread(String targetReleaseTag)
	{
	    this.targetReleaseTag = targetReleaseTag;
	}
	
	/** SwingWorker: construct() */
	protected String construct() throws InterruptedException
	{
	    startTime = System.currentTimeMillis();
	    
	    SoftwareRelease targetRelease = new SoftwareRelease();
	    database.loadSoftwareRelease(targetReleaseTag,targetRelease);

	    ConfigInfo targetConfigInfo =
		new ConfigInfo(currentConfig.name(),currentConfig.parentDir(),
			       -1,currentConfig.version(),"",targetReleaseTag);
	    Configuration targetConfig = new Configuration(targetConfigInfo,
							   targetRelease);
	    
	    migrator = new ReleaseMigrator(currentConfig,targetConfig);
	    migrator.migrate();
	    
	    database.insertConfiguration(targetConfig);
	    currentRelease.clearInstances();
	    currentConfig =
		database.loadConfiguration(targetConfigInfo,currentRelease);
	    return new String("Done!");
	}
	
	/** SwingWorker: finished */
	protected void finished()
	{
	    try {
		currentConfig.setHasChanged(false);
		configurationPanel.setCurrentConfig(currentConfig);
		long elapsedTime = System.currentTimeMillis() - startTime;
		progressBar.setString(progressBar.getString() +
				      get() + " (" + elapsedTime + " ms)");
		MigrationReportDialog dialog =
		    new MigrationReportDialog(frame,migrator);
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	    }
	    catch (Exception e) {
		System.out.println("EXCEPTION: "+ e.getMessage());
		e.printStackTrace();
		progressBar.setString(progressBar.getString() + "FAILED!");
	    }
	    progressBar.setIndeterminate(false);
	}
    }

    /**
     * load release templates from the database
     */
    private class UpdateTemplatesThread extends SwingWorker<String>
    {
	/** release to be loaded */
	private String releaseTag = null;
	
	/** start time */
	private long startTime;
	
	/** standard constructor */
	public UpdateTemplatesThread(String releaseTag)
	{
	    this.releaseTag = releaseTag;
	}
	
	/** SwingWorker: construct() */
	protected String construct() throws InterruptedException
	{
	    startTime = System.currentTimeMillis();
	    if (!releaseTag.equals(currentRelease.releaseTag()))
		database.loadSoftwareRelease(releaseTag,currentRelease);
	    return new String("Done!");
	}
	
	/** SwingWorker: finished */
	protected void finished()
	{
	    try {
		long elapsedTime = System.currentTimeMillis() - startTime;
		progressBar.setString(progressBar.getString() +
				      get() + " (" + elapsedTime + " ms)");
	    }
	    catch (Exception e) {
		System.out.println("EXCEPTION: "+ e.getMessage());
		e.printStackTrace();
		progressBar.setString(progressBar.getString() + "FAILED!");	
	    }
	    progressBar.setIndeterminate(false);
	}
    }
    
}

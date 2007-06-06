package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;

import confdb.data.Directory;
import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;

import confdb.db.CfgDatabase;
import confdb.db.DatabaseException;

    
/**
 * ConfigurationExportDialog
 * -----------------------
 * @author Philipp Schieferdecker
 *
 */
public class ConfigurationExportDialog
    extends ConfigurationDialog implements ActionListener
{
    //
    // member data
    //

    /** target database */
    private CfgDatabase targetDB = null;

    /** currently selected directory in target DB */
    private Directory targetDir = null;
    
    /** text field to query the name of the cfg in the target db */
    private JTextField textFieldTargetName = null;

    /** panel to query the user for target-database connection information */
    private DatabaseConnectionPanel dbConnectionPanel = null;

    /** rigth panel, to show directory view */
    private JPanel rightPanel = null;
    
    /** Connect / Cancel / OK buttons */
    private JButton connectButton = null;
    private JButton okButton      = null;
    private JButton cancelButton  = null;

    /** action commands */
    private static final String CONNECT = new String("Connect");
    private static final String OK      = new String("OK");
    private static final String CANCEL  = new String("Cancel");
    

    //
    // construction
    //
    
    /** standard constructor */
    public ConfigurationExportDialog(JFrame frame,String targetName)
    {
	super(frame);
	textFieldTargetName = new JTextField(10);
	textFieldTargetName.setText(targetName);
	setTitle("Export Configuration");
	setContentPane(createContentPane());
	targetDB = new CfgDatabase();
    }
    
    
    //
    // member functions
    //
    
    /** retrieve target database */
    public CfgDatabase targetDB() { return this.targetDB; }
    
    /** retrieve configuration name in target DB */
    public String targetName() { return textFieldTargetName.getText(); }
    
    /** retrieve configuration directory in target DB */
    public Directory targetDir() { return this.targetDir; }
    
    /** create the content pane */
    public JPanel createContentPane()
    {
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0.5;
	
	JPanel contentPane = new JPanel(new GridBagLayout());
	JPanel topPanel = new JPanel(new GridBagLayout());
	JPanel leftPanel = new JPanel(new GridBagLayout());
	
	dbConnectionPanel = new DatabaseConnectionPanel();
	dbConnectionPanel.setDefaults(DatabaseConnectionPanel.DBTYPE_MYSQL,
				      "localhost","3306","hltdb","","");
	connectButton = new JButton("Connect");
	connectButton.setActionCommand(CONNECT);
	connectButton.addActionListener(this);
	
	c.gridx=0; c.gridy=0; c.gridwidth=3;
	leftPanel.add(dbConnectionPanel,c);
	c.gridx=1; c.gridy=1; c.gridwidth=1;
	leftPanel.add(connectButton,c);
	leftPanel.setBorder(BorderFactory.createTitledBorder("Target Database:"));
	
	c.gridx=0; c.gridy=0;
	topPanel.add(leftPanel,c);

	rightPanel = new JPanel(new CardLayout());
	rightPanel.setPreferredSize(leftPanel.getPreferredSize());
	
	c.gridx=1;c.gridy=0;
	topPanel.add(rightPanel,c);
	
	contentPane.add(topPanel);
	
	c.gridx=0;c.gridy=1;c.gridwidth=2;
	contentPane.add(createButtonPanel(),c);

	// ensure the database user field always get the first focus
	addComponentListener(new ComponentAdapter()
	    {
		public void componentShown(ComponentEvent ce)
		{
		    dbConnectionPanel.requestFocusOnUser();
		}
	    });
	
	
	return contentPane;
    }

    /** create button panel */
    public JPanel createButtonPanel()
    {
	JPanel result = new JPanel(new FlowLayout());
	okButton = new JButton(OK);
	okButton.addActionListener(this);
	okButton.setActionCommand(OK);
	okButton.setEnabled(false);
	cancelButton = new JButton(CANCEL);
	cancelButton.addActionListener(this);
	cancelButton.setActionCommand(CANCEL);
	okButton.setPreferredSize(cancelButton.getPreferredSize());
	result.add(cancelButton);
	result.add(okButton);
	return result;
    }

    /** ActionListener: actionPerformed() */
    public void actionPerformed(ActionEvent e)
    {
	validChoice = false;

	// NAME
	if (e.getActionCommand().equals("NAME")) {
	    if (targetName().length()>0&&targetDir!=null)
		okButton.setEnabled(true);
	    else
		okButton.setEnabled(false);
	    return;
	}
	// CONNECT
	else if (e.getActionCommand().equals(CONNECT)) {
	    try {
		boolean success = targetDB.connect(dbConnectionPanel.dbType(),
						   dbConnectionPanel.dbUrl(),
						   dbConnectionPanel.dbUser(),
						   dbConnectionPanel.dbPassword());
		System.out.println("success = " + success);
		this.setDatabase(targetDB);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.insets = new Insets(3,3,3,3);
		
		JPanel nameAndTreePanel = new JPanel(new GridBagLayout());
		
		c.gridx=0; c.gridy=0;
		nameAndTreePanel.add(new JLabel("Name:"),c);
		
		textFieldTargetName.setActionCommand("NAME");
		textFieldTargetName.addActionListener(this);
		
		c.gridx=0; c.gridy=1;
		nameAndTreePanel.add(textFieldTargetName,c);
		
		c.gridx=0; c.gridy=2;
		nameAndTreePanel.add(new JLabel("Directory:"),c);
		
		JScrollPane treePane=createTreeView(new Dimension(200,180));
		c.gridx=0; c.gridy=3; c.ipady=130;
		nameAndTreePanel.add(treePane,c);
		
		nameAndTreePanel.setBorder(BorderFactory
					   .createTitledBorder("Target Name "+
							       "/ Directory:"));
	
		// dummy ?!?!
		JPanel testPanel = new JPanel(new FlowLayout());
		testPanel.add(new JLabel("TEST"));
		rightPanel.add(testPanel,"TestPanel");
		
		rightPanel.add(nameAndTreePanel,"TreePanel");
		//rightPanel.add(treePane,"TreePanel");
		CardLayout cl = (CardLayout)(rightPanel.getLayout());
		cl.show(rightPanel,"TreePanel");
		//cl.show(rightPanel,"TestPanel");

		addTreeSelectionListener(new DBExportTreeSelListener());
		connectButton.setEnabled(false);
	    }
	    catch (DatabaseException ex) {
		System.out.println("Failed to connect to DB: "+ex.getMessage());
	    }
	    return;
	}
	// OK
	else if (e.getActionCommand().equals(OK)) {
	    if (targetName().length()>0&&targetDir!=null) validChoice=true;
	    else {
		try { targetDB.disconnect(); } catch (DatabaseException ex) {}
	    }
	}
	else {
	    try { targetDB.disconnect(); } catch (DatabaseException ex) {}
	}
	setVisible(false);
    }
    
    
    //
    // classes
    //
    

    /**
     * DBExportTreeSelListener
     * -----------------------
     * @author Philipp Schieferdecker
     */
    public class DBExportTreeSelListener implements TreeSelectionListener
    {
	/** TreeSelectionListener: valueChanged() */
	public void valueChanged(TreeSelectionEvent ev)
	{
	    JTree  dirTree = (JTree)ev.getSource();
	    Object o       = dirTree.getLastSelectedPathComponent();
	    if (o instanceof Directory) {
		Directory d = (Directory)o;
		targetDir = d;
		if (textFieldTargetName.getText().length()>0)
		    okButton.setEnabled(true);
	    }
	    else if (o instanceof ConfigInfo) {
		okButton.setEnabled(false);
		targetDir = null;
	    }
	}

    }
    
}

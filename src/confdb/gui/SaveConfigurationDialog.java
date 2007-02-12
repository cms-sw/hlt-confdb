package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;

import confdb.data.Directory;
import confdb.data.Configuration;
import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;
    
import confdb.db.CfgDatabase;


/**
 * SaveConfigurationDialog
 * -----------------------
 * @author Philipp Schieferdecker
 *
 */
public class SaveConfigurationDialog
    extends ConfigurationDialog implements ActionListener
{
    //
    // member data
    //

    /** configuration to be saved */
    private Configuration config = null;

    /** currently selected directory */
    private Directory selectedDir = null;

    /** text field for the name of the configurations */
    private JTextField textFieldConfigName = null;
    
    /** Cancel / OK buttons */
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    /** action commands */
    private static final String OK            = new String("OK");
    private static final String CANCEL        = new String("Cancel");
    private static final String ADD_DIRECTORY = new String("Add Directory");
    
    //
    // construction
    //
    
    /** standard constructor */
    public SaveConfigurationDialog(JFrame frame,CfgDatabase database,
				   Configuration config)
    {
	super(frame,database);
	this.config = config;
	
	setTitle("Save Configuration");
	
	setContentPane(createContentPane());
	if (config.version()==0) {
	    textFieldConfigName.setText(config.name());
	    textFieldConfigName.selectAll();
	}
	
	addMouseListener(new SaveConfigMouseListener());
	addTreeSelectionListener(new SaveConfigTreeSelListener(dirTree));
    }
    
    
    //
    // member functions
    //

    /** create the content pane */
    public JPanel createContentPane()
    {
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.VERTICAL;
	c.weightx = 0.5;
	
	JPanel contentPane = new JPanel(new GridBagLayout());
	
	Dimension dimTreeView = new Dimension(300,300);
	c.gridx=0; c.gridy=0;
	contentPane.add(createTreeView(dimTreeView),c);
	
	c.gridx=0;c.gridy=1;
	contentPane.add(createTextFieldPanel(),c);

	c.gridx=0;c.gridy=2;
	contentPane.add(createButtonPanel(),c);

	return contentPane;
    }

    /** create the text field panel */
    public JPanel createTextFieldPanel()
    {
	JPanel result = new JPanel(new FlowLayout());
	result.add(new JLabel("Configuration Name: "));
	textFieldConfigName = new JTextField(12);
	result.add(textFieldConfigName);
	return result;
    }

    /** create the bottom panel: text field for config name and buttons */
    public JPanel createButtonPanel()
    {
	JPanel result = new JPanel(new FlowLayout());

	okButton = new JButton(OK);
	okButton.addActionListener(this);
	okButton.setActionCommand(OK);
	okButton.setEnabled(false);
	result.add(okButton);
	
	cancelButton = new JButton(CANCEL);
	cancelButton.addActionListener(this);
	cancelButton.setActionCommand(CANCEL);
	result.add(cancelButton);

	return result;	
    }
    
    /** ActionListener: actionPerformed() */
    public void actionPerformed(ActionEvent e)
    {
	validChoice = false;
	if (e.getActionCommand().equals(OK)) {
	    String    configName  = textFieldConfigName.getText();
	    Directory parentDir   = selectedDir;
	    String    releaseTag  = config.releaseTag();
	    if (configName.length()>0&&parentDir!=null) {
		ConfigInfo configInfo = new ConfigInfo(configName,
						       parentDir,
						       releaseTag);
		config.setConfigInfo(configInfo);
		validChoice = true;
	    }
	}
	else {
	    System.out.println(CANCEL);
	}
	setVisible(false);
    }
    

    //
    // classes
    //
    
    /**
     * SaveConfigMouseListener
     * -----------------------
     * @author Philipp Schieferdecker
     */
    public class SaveConfigMouseListener extends MouseAdapter
    {
	public void mousePressed(MouseEvent e) { maybeShowPopup(e); }

	public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }

	private void maybeShowPopup(MouseEvent e)
	{
	    if (!e.isPopupTrigger()) return;
	    JTree    tree     = (JTree)e.getComponent();
	    TreePath treePath = tree.getPathForLocation(e.getX(),e.getY());
	    if (treePath==null) return;
	    tree.setSelectionPath(treePath);
	    Object o = treePath.getLastPathComponent();
	    if (o instanceof Directory) {
		JPopupMenu popup    = new JPopupMenu();
		JMenuItem  menuItem = new JMenuItem(ADD_DIRECTORY);
		menuItem.addActionListener(new SaveConfigActionListener(tree));
		menuItem.setActionCommand(ADD_DIRECTORY);
		popup.add(menuItem);
		popup.show(e.getComponent(),e.getX(),e.getY());
	    }
	}
	
    }
    
    
    /** 
     * SaveConfigActionListener
     * ------------------------
     * @author Philipp Schieferdecker
     */
    public class SaveConfigActionListener implements ActionListener
    {
	/** directory tree */
	private JTree dirTree = null;

	/** standard constructor */
	public SaveConfigActionListener(JTree dirTree)
	{
	    this.dirTree = dirTree;
	}
	
	/** ActionListener: actionPerformed() */
	public void actionPerformed(ActionEvent e)
	{
	    String actionCmd = e.getActionCommand();
	    if (actionCmd.equals(ADD_DIRECTORY)) {
		TreePath  treePath  = dirTree.getSelectionPath();
		Directory parentDir = (Directory)treePath.getLastPathComponent();
		Directory newDir    = new Directory(-1,"<ENTER DIR NAME>","",
						    parentDir);
		parentDir.addChildDir(newDir);
		
		dirTree.updateUI();
		dirTree.expandPath(treePath);
		int      parentRow   = dirTree.getRowForPath(treePath);
		int      newRow      = parentRow + parentDir.childDirCount();
		TreePath newTreePath = dirTree.getPathForRow(newRow);
		
		dirTree.expandPath(newTreePath);
		dirTree.scrollPathToVisible(newTreePath);
		dirTree.setSelectionPath(newTreePath);
		dirTree.startEditingAtPath(newTreePath);
	    }
	}
    }
  

    /**
     * SaveConfigTreeSelListener
     * -------------------------
     * @author Philipp Schieferdecker
     */
    public class SaveConfigTreeSelListener implements TreeSelectionListener
    {
	/** directory tree */
	private JTree dirTree = null;

	/** standard constructor */
	public SaveConfigTreeSelListener(JTree dirTree)
	{
	    this.dirTree = dirTree;
	}
	
	/** TreeSelectionListener: valueChanged() */
	public void valueChanged(TreeSelectionEvent ev)
	{
	    Object o = dirTree.getLastSelectedPathComponent();
	    if (o instanceof Directory) {
		selectedDir = (Directory)o;
		if (textFieldConfigName.getText().length()>0)
		    okButton.setEnabled(true);
	    }
	    else if (o instanceof ConfigInfo) {
		selectedDir = null;
		dirTree.getSelectionModel().clearSelection();
		okButton.setEnabled(false);
	    }
	}
    }



}


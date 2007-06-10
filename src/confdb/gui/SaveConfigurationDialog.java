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
    
import confdb.db.CfgDatabase;


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
    
    //
    // construction
    //
    
    /** standard constructor */
    public SaveConfigurationDialog(JFrame        frame,
				   CfgDatabase   database,
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
	
	addMouseListener(new SaveConfigMouseListener());
	addTreeSelectionListener(new SaveConfigTreeSelListener());
	addTreeModelListener(new SaveConfigTreeModelListener(database));
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

	/** directory tree model */
	private DirectoryTreeModel treeModel = null;
	
	/** standard constructor */
	public SaveConfigActionListener(JTree dirTree)
	{
	    this.dirTree = dirTree;
	    this.treeModel = (DirectoryTreeModel)dirTree.getModel();
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
		treeModel.nodeInserted(parentDir,parentDir.childDirCount()-1);
		
		dirTree.expandPath(treePath);
		TreePath newTreePath = treePath.pathByAddingChild(newDir);
		
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
	/** TreeSelectionListener: valueChanged() */
	public void valueChanged(TreeSelectionEvent ev)
	{
	    JTree  dirTree = (JTree)ev.getSource();
	    Object o       = dirTree.getLastSelectedPathComponent();
	    if (o instanceof Directory) {
		selectedDir = (Directory)o;
		if (jTextFieldConfigName.getText().length()>0)
		    okButton.setEnabled(true);
	    }
	    else if (o instanceof ConfigInfo) {
		selectedDir = null;
		dirTree.getSelectionModel().clearSelection();
		okButton.setEnabled(false);
	    }
	}
    }


    /**
     * SaveConfigTreeModelListener
     * ---------------------------
     * @author Philipp Schieferdecker
     */
    public class SaveConfigTreeModelListener implements TreeModelListener
    {
	/** reference to the db interface */
	private CfgDatabase database = null;

	/** standard constructor */
	public SaveConfigTreeModelListener(CfgDatabase database)
	{
	    this.database = database;
	}
	
	/** TreeModelListener: treeNodesChanged() */
	public void treeNodesChanged(TreeModelEvent e)
	{
	    TreePath  treePath  = e.getTreePath(); if (treePath==null) return;
	    int       index     = e.getChildIndices()[0];
	    Directory parentDir = (Directory)treePath.getLastPathComponent();
	    Directory childDir  = parentDir.childDir(index);
	    
	    if (!database.insertDirectory(childDir)) {
		parentDir.removeChildDir(childDir);
		DirectoryTreeModel treeModel = (DirectoryTreeModel)e.getSource();
		treeModel.nodeRemoved(parentDir,parentDir.childDirCount(),childDir);
	    }
	}
	
	/** TreeModelListener: treeNodesInserted() */
	public void treeNodesInserted(TreeModelEvent e) {}
	
	/** TreeModelListener: treeNodesRemoved() */
	public void treeNodesRemoved(TreeModelEvent e) {}
	
	/** TreeModelListener: treeStructureChanged() */
	public void treeStructureChanged(TreeModelEvent e) {}
	
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
        cancelButton.setText("Cancel");

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


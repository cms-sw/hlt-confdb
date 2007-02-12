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


/**
 * ConfigurationDialog
 * -------------------
 * @author Philipp Schieferdecker
 *
 * Open / Save a configuration.
 */
public class ConfigurationDialog extends JDialog
{
    //
    // member data
    //

    /** reference to the main frame */
    private JFrame frame = null;
    
    /** reference to the database */
    protected CfgDatabase database = null;
    
    /** was a valid choice made? */
    protected boolean validChoice = false;
    
    /** root directory */
    private Directory rootDir = null;

    /** dirctory tree */
    protected JTree dirTree = null;
    
    /** directory tree-model */
    private DirectoryTreeModel dirTreeModel = null;
    
    //
    // construction
    //
    
    /** standard constructor */
    public ConfigurationDialog(JFrame frame,CfgDatabase database)
    {
	super(frame,true);
	this.frame    = frame;
	this.database = database;
    }
    
    
    //
    // member functions
    //

    /** was a valid choice made? */
    public boolean validChoice() { return validChoice; }
    
    /** create the tree view */
    protected JScrollPane createTreeView(Dimension dim)
    {
	rootDir = database.loadConfigurationTree();
	dirTreeModel = new DirectoryTreeModel(rootDir);
	dirTree = new JTree(dirTreeModel);
	dirTree.setEditable(true);
	dirTree.setCellEditor(new DirTreeCellEditor(dirTree,
						    new DefaultTreeCellRenderer()));
	JScrollPane result = new JScrollPane(dirTree);
	result.setPreferredSize(dim);
	return result;
    }

    /** add a mouse listener */
    public void addMouseListener(MouseAdapter l)
    {
	dirTree.addMouseListener(l);
    }

    /** add a tree selection listener */
    public void addTreeSelectionListener(TreeSelectionListener l)
    {
	dirTree.addTreeSelectionListener(l);
    }

    /** add a tree model listener */
    public void addTreeModelListener(TreeModelListener l)
    {
	dirTreeModel.addTreeModelListener(l);
    }

    //
    // classes
    //
    
    /**
     * DirTreeCellEditor
     * -----------------
     * @author Philipp Schieferdecker
     */
    public class DirTreeCellEditor extends DefaultTreeCellEditor
    {
	//
	// data member
	//
	
	/** Directory to be edited */
	private Directory dir = null;
	
	//
	//
	//
	
	/** standard constructor */
	public DirTreeCellEditor(JTree tree,DefaultTreeCellRenderer renderer)
	{
	    super(tree,renderer);
	}
	
	//
	// member functions
	//
	
	/** is cell editable? don't respond to double clicks */
	public boolean isCellEditable(EventObject e)
	{
	    if (e instanceof MouseEvent) return false;
	    return true;
	}
	
	/**  DefaultTreeCellEditor's 'getCellEditorValue' */
	public Object getCellEditorValue()
	{
	    Object value = super.getCellEditorValue();
	    if (dir == null) return null;
	    Directory parentDir = dir.parentDir();
	    String newDirName = parentDir.name();
	    if (!newDirName.equals("/")) newDirName+="/";
	    newDirName+=value.toString();
	    dir.setName(newDirName);
	    return dir;
	}
	
	/** TreeCellEditor's 'getTreeCellEditorComponent' */
	public Component getTreeCellEditorComponent(JTree   tree,
						    Object  value,
						    boolean isSelected,
						    boolean expanded,
						    boolean leaf,
						    int     row)
	{
	    if (value instanceof Directory) {
		dir = (Directory)value;
	    }
	    return super.getTreeCellEditorComponent(tree,
						    value,
						    isSelected,
						    expanded,
						    leaf,
						    row);
	}
	
    }
    
}

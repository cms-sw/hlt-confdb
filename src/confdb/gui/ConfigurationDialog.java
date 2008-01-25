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
    
import confdb.db.ConfDB;
import confdb.db.DatabaseException;


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
    protected ConfDB database = null;
    
    /** was a valid choice made? */
    protected boolean validChoice = false;
    
    /** root directory */
    protected Directory rootDir = null;

    /** dirctory tree */
    protected JTree dirTree = null;
    
    /** directory tree-model */
    private DirectoryTreeModel dirTreeModel = null;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ConfigurationDialog(JFrame frame,ConfDB database)
    {
	super(frame,true);
	this.frame    = frame;
	this.database = database;
    }
    
    
    /** constructor without database */
    public ConfigurationDialog(JFrame frame)
    {
	super(frame,true);
	this.frame    = frame;
	this.database = null;
    }
    
    
    //
    // member functions
    //

    /** was a valid choice made? */
    public boolean validChoice() { return validChoice; }
    
    /** set the database */
    public void setDatabase(ConfDB database) { this.database = database; }
    
    /** create the tree view */
    protected JScrollPane createTreeView(Dimension dim)
    {
	try {
	    rootDir = database.loadConfigurationTree();
	    dirTreeModel = new DirectoryTreeModel(rootDir);
	}
	catch (DatabaseException e) {
	    return new JScrollPane(new JLabel(e.getMessage()));
	}
	
	dirTree = new JTree(dirTreeModel) {
		public String getToolTipText(MouseEvent evt) {
		    String text = "";
		    if (getRowForLocation(evt.getX(),evt.getY()) == -1) return text;
		    TreePath tp = getPathForLocation(evt.getX(), evt.getY());
		    Object selectedNode = tp.getLastPathComponent();
		    if (selectedNode instanceof ConfigInfo) {
			ConfigInfo info = (ConfigInfo)selectedNode;
			if (info.isLocked()) {
			    text = "locked by user '" + info.lockedByUser() + "'";
			}
		    }
		    return text;
		}
	    };
	dirTree.setToolTipText("");
	
	dirTree.setEditable(true);

	DefaultTreeCellRenderer renderer = new DirTreeCellRenderer();
	renderer.setLeafIcon(new ImageIcon(getClass()
					   .getResource("/ConfigIcon.png")));
	renderer.setOpenIcon(new ImageIcon(getClass()
					   .getResource("/DirIcon.png")));
	renderer.setClosedIcon(new ImageIcon(getClass()
					     .getResource("/DirIcon.png")));
	dirTree.setCellRenderer((DefaultTreeCellRenderer)renderer);
	dirTree.setCellEditor(new DirTreeCellEditor(dirTree,renderer));
	
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
	/** Directory to be edited */
	private Directory dir = null;
	
	/** standard constructor */
	public DirTreeCellEditor(JTree tree,DefaultTreeCellRenderer renderer)
	{
	    super(tree,renderer);
	}
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
    
    /**
     * DirTreeCellRenderer
     * -------------------
     * @author Philipp Schieferdecker
     */
    public class DirTreeCellRenderer extends DefaultTreeCellRenderer
    {
	/** Directory to be edited */
	private Directory dir = null;
	
	/** icon for locked configurations */
	private ImageIcon lockedConfigIcon =
	    new ImageIcon(getClass().getResource("/LockedConfigIcon.png"));

	/** TreeCellRenderer interface, overwrite Default implementation */
	public Component getTreeCellRendererComponent(JTree   tree,
						      Object  value,
						      boolean sel,
						      boolean expanded,
						      boolean leaf,
						      int     row,
						      boolean hasFocus)
	{
	    super.getTreeCellRendererComponent(tree,value,sel,
					       expanded,leaf,row,
					       hasFocus);
	    if (value instanceof ConfigInfo) {
		ConfigInfo configInfo = (ConfigInfo)value;
		if (configInfo.isLocked()) {
		    setIcon(lockedConfigIcon);
		    setText("<html>"+getText() +
			    " <font color=#ff0000>LOCKED</font></html>");
		}
	    }
	    
	    return this;
	}
    }
    
}

package confdb.gui.treetable;

import javax.swing.tree.*;
import javax.swing.event.*;

import confdb.gui.tree.AbstractTreeModel;


/**
 * AbstractTreeTableTreeModel
 * --------------------------
 * @author Philipp Schieferdecker
 * 
 * Abstract implementation of the TreeTableTreeModel interface.
 */
public abstract class AbstractTreeTableTreeModel extends    AbstractTreeModel
                                                 implements TreeTableTreeModel
{
    //
    // member data
    //
    
    /** the root of the tree */
    protected Object root = null;


    //
    // construction
    //
    
    /** standard constructor */
    public AbstractTreeTableTreeModel(Object root)
    {
	this.root = root;
    }
    
    
    //
    // member functions
    //
    
    /** get the root of the tree */
    public Object getRoot() { return root; }
    
    /** indicate if a node is a leaf or not */
    public boolean isLeaf(Object node) { return getChildCount(node) == 0; }
    
    /** get the index of child node */
    public int getIndexOfChild(Object parent, Object child)
    {
	for (int i=0;i<getChildCount(parent);i++) {
	    if (getChild(parent,i).equals(child)) return i;
	}
	return -1;
    }
    
    /** TreeTableTreeModel: getColumnClass */
    public Class getColumnClass(int column) { return Object.class; }
    
}

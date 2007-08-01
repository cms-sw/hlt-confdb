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
    
    /** inidicate that the next TreeModel event comes from Listener */
    private boolean nextFromListener = false;


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
    
    /** does the next tree model event come from a listener? */
    public boolean nextFromListener()
    {
	boolean result = nextFromListener;
	nextFromListener = false;
	return result;
    }
    
    /** indicate that the next tree model event comes from a listener */
    public void setNextFromListener() { nextFromListener = true; }

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

package confdb.gui.treetable;

import javax.swing.tree.*;
import javax.swing.event.*;

/**
 * AbstractTreeTableTreeModel
 * --------------------------
 * @author Philipp Schieferdecker
 * 
 * Abstract implementation of the TreeTableTreeModel interface.
 */
public abstract class AbstractTreeTableTreeModel implements TreeTableTreeModel
{
    //
    // member data
    //
    
    /** the root of the tree */
    protected Object root = null;

    /** list of event listeners */
    protected EventListenerList listenerList = new EventListenerList();

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
    public boolean isLeaf(Object node) { return getChildCount(node) ==0; }
    
    /** no idea what this id for:) */
    public void valueForPathChanged(TreePath path, Object newValue) {}

    /** get the index of child node */
    public int getIndexOfChild(Object parent, Object child)
    {
	for (int i=0;i<getChildCount(parent);i++) {
	    if (getChild(parent,i).equals(child)) return i;
	}
	return -1;
    }

    /** add tree model listener to listener list */
    public void addTreeModelListener(TreeModelListener l)
    {
	listenerList.add(TreeModelListener.class, l);
    }
    
    /** remove tree model listener from listener list */
    public void removeTreeModelListener(TreeModelListener l)
    {
	listenerList.remove(TreeModelListener.class,l);
    }

    /** notify all listeners for TreeModelEvent that a node has changed */
    protected void fireTreeNodesChanged(Object[] source,
					Object[] path,
					int[]    childIndices,
					Object[] children)
    {
	Object[] listeners = listenerList.getListenerList();
	TreeModelEvent e = null;
	for (int i=listeners.length-2;i>=0;i-=2) {
	    if (listeners[i]==TreeModelListener.class) {
		if (e==null) e=new TreeModelEvent(source,path,childIndices,children);
		((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
	    }
	}
    }
    
    /** notify all listeners for TreeModelEvent that a node was inserted */
    protected void fireTreeNodesInserted(Object[] source,
					 Object[] path,
					 int[]    childIndices,
					 Object[] children)
    {
	Object[] listeners = listenerList.getListenerList();
	TreeModelEvent e = null;
	for (int i=listeners.length-2;i>=0;i-=2) {
	    if (listeners[i]==TreeModelListener.class) {
		if (e==null) e=new TreeModelEvent(source,path,childIndices,children);
		((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
	    }
	}
    }
    
    /** notify all listeners for TreeModelEvent that a node was removed */
    protected void fireTreeNodesRemoved(Object[] source,
					Object[] path,
					int[]    childIndices,
					Object[] children)
    {
	Object[] listeners = listenerList.getListenerList();
	TreeModelEvent e = null;
	for (int i=listeners.length-2;i>=0;i-=2) {
	    if (listeners[i]==TreeModelListener.class) {
		if (e==null) e=new TreeModelEvent(source,path,childIndices,children);
		((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
	    }
	}
    }
    
    /** notify all listeners for TreeModelEvent that the tree structure changed */
    protected void fireTreeStructureChanged(Object[] source,
					    Object[] path,
					    int[]    childIndices,
					    Object[] children)
    {
	Object[] listeners = listenerList.getListenerList();
	TreeModelEvent e = null;
	for (int i=listeners.length-2;i>=0;i-=2) {
	    if (listeners[i]==TreeModelListener.class) {
		if (e==null) e=new TreeModelEvent(source,path,childIndices,children);
		((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
	    }
	}
    }
    
    // TreeTableTreeModel interface implementations
    
    public Class getColumnClass(int column) { return Object.class; }
    
}

package confdb.gui.tree;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;


/**
 * AbstractTreeModel
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Abstract base class for any custom tree model.
 */
public abstract class AbstractTreeModel implements TreeModel
{
    //
    // member data
    //
    
    /** list of event listeners */
    protected EventListenerList listenerList = new EventListenerList();

    
    //
    // abstract member functions
    // 
    
    /** get the parent of a node */
    abstract public Object getParent(Object node);
    
    
    //
    // member functions
    //
    
    /** TreeModel: addTreeModelListener() */
    public void addTreeModelListener(TreeModelListener l)
    {
	listenerList.add(TreeModelListener.class,l);
    }

    /** TreeModel: removeTreeModelListener() */
    public void removeTreeModelListener(TreeModelListener l)
    {
	listenerList.remove(TreeModelListener.class,l);
    }
    
    /** fire node-changed event if the value of a node has changed */
    public void valueForPathChanged(TreePath treePath,Object newValue)
    {
	if (treePath!=null) {
	    Object node = treePath.getLastPathComponent();
	    nodeChanged(node);
	}
    }
    
    /** invoke if represantation of node has changed in the tree */
    public void nodeChanged(Object node)
    {
	if (listenerList!=null && node!=null) {
	    Object parent = getParent(node);
	    if (parent!=null) {
		int index = getIndexOfChild(parent,node);
		if (index!=-1) {
		    int[] indices = new int[1];
		    indices[0] = index;
		    childNodesChanged(parent,indices);
		}
	    }
	    else if (node==getRoot()) {
		childNodesChanged(node,null);
	    }
	}
    }
    
    /** invoke if representation of children of node was changed in the tree */
    public void childNodesChanged(Object parent,int[] childIndices)
    {
	if (parent != null) {
	    if (childIndices != null) {
		int childCount = childIndices.length;
		if (childCount>0) {
		    Object[] children = new Object[childCount];
		    for (int i=0;i<childCount;i++)
			children[i] = getChild(parent,childIndices[i]);
		    fireTreeNodesChanged(this,getPathToRoot(parent),childIndices,children);
		}
	    }
	    else if (parent == getRoot()) {
		fireTreeNodesChanged(this,getPathToRoot(parent), null, null);
	    }
	}
    }

    /** invoke if a child was inserted at i-th position in parent */
    public void nodeInserted(Object parent,int i)
    {
	int[] childIndices = { i };
	nodesInserted(parent,childIndices);
    }
    
    /** invoke if nodes were inserted as children of parent */
    public void nodesInserted(Object parent,int[] childIndices)
    {
	if (listenerList!=null && parent != null &&
	    childIndices != null && childIndices.length > 0) {
	    int      childCount = childIndices.length;
	    Object[] children   = new Object[childCount];
	    for (int i=0;i<childCount;i++)
		children[i] = getChild(parent,childIndices[i]);
	    fireTreeNodesInserted(this,getPathToRoot(parent),childIndices,children);
	}
    }
    
    /** invoke if child was removed at i-th position of parent */
    public void nodeRemoved(Object parent,int i,Object child)
    {
	int[]    childIndices = { i };
	Object[] children     = { child };
	nodesRemoved(parent,childIndices,children);
    }
    
    /** invoke if nodes were removed from parent */
    public void nodesRemoved(Object parent,int[] childIndices,Object[] children)
    {
	if (parent!=null && childIndices!=null) {
	    fireTreeNodesRemoved(this,getPathToRoot(parent),childIndices,children);
	}
    }
    
    /** invoke if the tree structure below node */
    public void nodeStructureChanged(Object node)
    {
	if (node!=null)
	    fireTreeStructureChanged(this,getPathToRoot(node),null,null);
    }

    /** get the path to the root */
    protected Object[] getPathToRoot(Object node)
    {
	return getPathToRoot(node,0);
    }

    /** get path to root, called recursively by getPathToRoot(Object node) */
    protected Object[] getPathToRoot(Object node,int depth)
    {
	Object[] result;
	if(node == null) {
            if(depth == 0) return null;
            else result = new Object[depth];
        }
        else {
            depth++;
            if(node == getRoot()) result = new Object[depth];
            else result = getPathToRoot(getParent(node),depth);
            result[result.length - depth] = node;
        }
        return result;
    }
    
    /** notify all listeners for TreeModelEvent that a node has changed */
    protected void fireTreeNodesChanged(Object   source,
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
    protected void fireTreeNodesInserted(Object   source,
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
    protected void fireTreeNodesRemoved(Object   source,
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
    protected void fireTreeStructureChanged(Object   source,
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
    
}

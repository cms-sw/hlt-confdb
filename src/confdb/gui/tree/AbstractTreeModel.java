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
    private EventListenerList listenerList = new EventListenerList();

    
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
    
    /** TreeModel: valueForPathChanged() */
    public void valueForPathChanged(TreePath treePath,Object newValue)
    {
	nodeChanged(treePath);
    }


    /** to be called if a node has changed */
    public void nodeChanged(TreePath treePath)
    {
	int      depth    = treePath.getPathCount();
	Object   child    = treePath.getPathComponent(depth-1);
	Object   parent   = treePath.getPathComponent(depth-2);
	Object[] source   = { this };
	Object[] path     = treePath.getParentPath().getPath();
	int[]    indices  = { getIndexOfChild(parent,child) };
	Object[] children = { child };
	fireTreeNodesChanged(source,path,indices,children);
    }
    
    /** to be called if a node has changed */
    public void nodeInserted(TreePath parentTreePath,int index)
    {
	Object   parent   = parentTreePath.getLastPathComponent();	
	Object   child    = getChild(parent,index);
	Object[] source   = { this };
	Object[] path     = parentTreePath.getPath();
	int[]    indices  = { index };
	Object[] children = { child };
	fireTreeNodesInserted(source,path,indices,children);
    }
    
    /** to be called if a node has changed */
    public void nodeRemoved(TreePath parentTreePath,int index,Object node)
    {
	Object[] source   = { this };
	Object[] path     = parentTreePath.getPath();
	int[]    indices  = { index };
	Object[] children = { node };
	fireTreeNodesRemoved(source,path,indices,children);
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
    
}

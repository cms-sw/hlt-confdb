package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.data.*;


/**
 * StreamTreeActions
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Repository of actions which change the stream configuration and at
 * the same time need to be visually represented in the (stream-)
 * JTree.
 */
public class StreamTreeActions
{
    /** insert a new Stream */
    public static void insertStream(JTree tree)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	if (treePath==null) treePath = new TreePath(model.getRoot());
	
	int index = (treePath.getPathCount()==1) ?
	    0 : model.getIndexOfChild(treePath.getParentPath().
				      getLastPathComponent(),
				      treePath.getLastPathComponent())+1;
	
	Stream stream = config.insertStream(index,"<ENTER STREAM LABEL>");
	
	model.nodeInserted(model.getRoot(),index);
	
	TreePath parentPath = (index==0) ? treePath : treePath.getParentPath();
	TreePath newTreePath = parentPath.pathByAddingChild(stream);	
	
	tree.setSelectionPath(newTreePath);
	editNodeName(tree);
    }
    
    /** edit a stream label */
    public static void editNodeName(JTree tree)
    {
	TreePath treePath = tree.getSelectionPath();
	tree.expandPath(treePath);
	tree.scrollPathToVisible(treePath);
	tree.startEditingAtPath(treePath);
    }

    public static boolean removeStream(JTree tree)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	
	Stream stream = (Stream)treePath.getLastPathComponent();
	int    index  = config.indexOfStream(stream);
	
	config.removeStream(stream);
	model.nodeRemoved(model.getRoot(),index,stream);
	
	treePath = (config.streamCount()>0&&index>0) ?
	    new TreePath(model.getPathToRoot(config.stream(index-1))) :
	    new TreePath(model.getRoot());
	tree.setSelectionPath(treePath);
	
	return true;
    }
    
    
    public static boolean removePath(JTree tree)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	
	Path   path   = (Path)treePath.getLastPathComponent();
	Stream stream = (Stream)treePath.getParentPath().getLastPathComponent();
	int    index  = stream.indexOfPath(path);
	
	stream.removePath(path);
	model.nodeRemoved(stream,index,path);
	
	return true;
    }
    
    public static boolean addPath(JTree tree,String cmd)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	
	Stream stream = (Stream)treePath.getLastPathComponent();
	Path   path   = config.path(cmd);
	stream.insertPath(path);
	model.nodeInserted(stream,stream.pathCount()-1);
	
	return true;
    }
    
    public static boolean addAllPaths(JTree tree)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	
	Stream stream = (Stream)treePath.getLastPathComponent();
	Iterator<Path> it = config.pathIterator();
	while (it.hasNext()) {
	    Path path = it.next();
	    if (stream.indexOfPath(path)<0) {
		stream.insertPath(path);
		model.nodeInserted(stream,stream.pathCount()-1);
	    }
	}
	
	return true;
    }
    
}

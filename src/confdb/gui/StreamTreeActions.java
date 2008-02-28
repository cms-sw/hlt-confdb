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
	    if (path.isEndPath()) continue;
	    if (stream.indexOfPath(path)<0) {
		stream.insertPath(path);
		model.nodeInserted(stream,stream.pathCount()-1);
	    }
	}
	model.nodeChanged(stream);

	return true;
    }
    
}

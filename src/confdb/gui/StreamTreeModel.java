package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import java.util.ArrayList;

import confdb.gui.tree.AbstractTreeModel;

import confdb.data.*;


/**
 * StreamTreeModel
 * ---------------
 * @author Philipp Schieferdecker
 *
 * Display the streams defined for a configuration, and the associated
 * paths as leaf nodes.
 */
public class StreamTreeModel extends AbstractTreeModel
{
    //
    // member data
    //

    /** the parent configuration */
    private Configuration config = null;
    
    /** the root node: a simple StringBuffer */
    private String rootNode = new String("Streams");
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public StreamTreeModel(Configuration config)
    {
	setConfiguration(config);
    }


    //
    // member functions
    //

    /** get the configuration */
    public Configuration getConfiguration() { return config; }
    
    /** set the configuration to be displayed */
    public void setConfiguration(Configuration config)
    {
	this.config = config;
	nodeStructureChanged(rootNode);
    }
    
    /** get root directory */
    public Object getRoot() { return rootNode; }

    /** indicate if a node is a leaf node */
    public boolean isLeaf(Object node)
    {
	if (node instanceof Path) return true;
	return false;
    }
    
    /** number of child nodes */
    public int getChildCount(Object node)
    {
	if (node.equals(rootNode)) {
	    return config.streamCount();
	}
	else if (node instanceof Stream) {
	    Stream stream = (Stream)node;
	    return stream.pathCount();
	}
	
	return 0;
    }
    
    /** get the i-th child node */
    public Object getChild(Object parent,int i)
    {
	if (parent.equals(rootNode)) {
	    return config.stream(i);
	}
	else if (parent instanceof Stream) {
	    Stream stream = (Stream)parent;
	    return stream.path(i);
	}
	
	return null;
    }
    
    /** get index of a certain child w.r.t. its parent dir */
    public int getIndexOfChild(Object parent,Object child)
    {
	if (parent.equals(rootNode)) {
	    Stream stream = (Stream)child;
	    return config.indexOfStream(stream);
	}
	else if (parent instanceof Stream) {
	    Stream stream = (Stream)parent;
	    Path path = (Path)child;
	    return stream.indexOfPath(path);
	}
	
	return -1;
    }
    
    /** get parent of a node */
    public Object getParent(Object node)
    {
	if (node instanceof Path) {
	    Path   path   = (Path)node;
	    Stream stream = (Stream)path.stream(0);
	    return stream;
	}
	else if (node instanceof Stream) {
	    return rootNode;
	}

	return null;
    }
    
}

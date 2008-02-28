package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import java.util.ArrayList;

import confdb.gui.tree.AbstractTreeModel;

import confdb.data.*;


/**
 * PrimaryDatasetTreeModel
 * -----------------------
 * @author Philipp Schieferdecker
 *
 * Display the primary datasets defined for a configuration, and the
 * associated paths as leaf nodes.
 */
public class PrimaryDatasetTreeModel extends AbstractTreeModel
{
    //
    // member data
    //
    
    /** the parent configuration */
    private Configuration config = null;
    
    /** the root node: a simple StringBuffer */
    private String rootNode = new String("Primary Datasets");
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public PrimaryDatasetTreeModel(Configuration config)
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
	    return config.datasetCount();
	}
	else if (node instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)node;
	    return dataset.pathCount();
	}
	return 0;
    }
    
    /** get the i-th child node */
    public Object getChild(Object parent,int i)
    {
	if (parent.equals(rootNode)) {
	    return config.dataset(i);
	}
	else if (parent instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)parent;
	    return dataset.path(i);
	}
	return null;
    }
    
    /** get index of a certain child w.r.t. its parent dir */
    public int getIndexOfChild(Object parent,Object child)
    {
	if (parent.equals(rootNode)) {
	    PrimaryDataset dataset = (PrimaryDataset)child;
	    return config.indexOfDataset(dataset);
	}
	else if (parent instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)parent;
	    Path path = (Path)child;
	    return dataset.indexOfPath(path);
	}
	return -1;
    }
    
    /** get parent of a node */
    public Object getParent(Object node)
    {
	if (node instanceof Path) {
	    Path           path    = (Path)node;
	    PrimaryDataset dataset = (PrimaryDataset)path.dataset(0);
	    return dataset;
	}
	else if (node instanceof PrimaryDataset) {
	    return rootNode;
	}
	
	return null;
    }
    
}

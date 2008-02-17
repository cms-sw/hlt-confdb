package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import confdb.gui.tree.AbstractTreeModel;

import confdb.data.Directory;
import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;


/**
 * DirectoryTreeModel
 * ------------------
 * @author Philipp Schieferdecker
 *
 * Display the directory tree of the configuration database.
 */
public class DirectoryTreeModel extends AbstractTreeModel
{
    //
    // member data
    //

    /** root of the tree = root directory */
    private Directory rootDir = null;

    
    //
    // construction
    //

    /** standard constructor */
    public DirectoryTreeModel(Directory rootDir)
    {
	this.rootDir = rootDir;
    }


    //
    // member functions
    //

    /** set root directory */
    public void setRootDir(Directory rootDir)
    {
	this.rootDir = rootDir;
	nodeStructureChanged(rootDir);
    }
    
    /** get root directory */
    public Object getRoot() { return rootDir; }

    /** indicate if a node is a leaf node */
    public boolean isLeaf(Object node)
    {
	return (node instanceof ConfigInfo) ? true: false;
    }
    
    /** number of child nodes */
    public int getChildCount(Object node)
    {
	if (node instanceof Directory) {
	    Directory d = (Directory)node;
	    return d.childDirCount() + d.configInfoCount();
	}
	return 0;
    }
    
    /** get the i-th child node */
    public Object getChild(Object parent,int i)
    {
	if (parent instanceof Directory) {
	    Directory d = (Directory)parent;
	    if (i<d.childDirCount()) return d.childDir(i);
	    return d.configInfo(i-d.childDirCount());
	}
	return null;
    }
    
    /** get index of a certain child w.r.t. its parent dir */
    public int getIndexOfChild(Object parent,Object child)
    {
	if (parent instanceof Directory) {
	    Directory parentDir = (Directory)parent;
	    for (int i=0;i<parentDir.childDirCount();i++)
		if (parentDir.childDir(i).equals(child)) return i;
	    for (int i=0;i<parentDir.configInfoCount();i++)
		if (parentDir.configInfo(i).equals(child))
		    return i+parentDir.childDirCount();
	}
	return -1;
    }
    
    /** get parent of a node */
    public Object getParent(Object node)
    {
	if (node instanceof Directory) {
	    Directory d = (Directory)node;
	    return d.parentDir();
	}
	if (node instanceof ConfigInfo) {
	    ConfigInfo ci = (ConfigInfo)node;
	    return ci.parentDir();
	}
	return null;
    }
    
}

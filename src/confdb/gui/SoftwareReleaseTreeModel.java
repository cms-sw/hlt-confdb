package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import java.util.ArrayList;

import confdb.gui.tree.AbstractTreeModel;

import confdb.data.*;

/**
 * SoftwareReleaseTreeModel
 * ------------------------
 * @author Philipp Schieferdecker
 *
 * Display a software release in a JTree structure.
 */
public class SoftwareReleaseTreeModel extends AbstractTreeModel {
	//
	// member data
	//

	/** root of the tree = software release */
	private SoftwareRelease release = null;

	//
	// construction
	//

	/** standard constructor */
	public SoftwareReleaseTreeModel(SoftwareRelease release) {
		setRelease(release);
	}

	//
	// member functions
	//

	/** set the release to be displayed */
	public void setRelease(SoftwareRelease release) {
		this.release = release;
		nodeStructureChanged(release);
	}

	/** get the root of the tree */
	public Object getRoot() {
		return release;
	}

	/** indicate if a node is a leaf node */
	public boolean isLeaf(Object node) {
		return (node instanceof Template);
	}

	/** get number of children of node */
	public int getChildCount(Object node) {
		if (node instanceof SoftwareRelease) {
			SoftwareRelease r = (SoftwareRelease) node;
			return r.subsystemCount();
		} else if (node instanceof SoftwareSubsystem) {
			SoftwareSubsystem s = (SoftwareSubsystem) node;
			return s.packageCount();
		} else if (node instanceof SoftwarePackage) {
			SoftwarePackage p = (SoftwarePackage) node;
			return p.templateCount();
		}
		return 0;
	}

	/** get the i-th child node of parent */
	public Object getChild(Object parent, int i) {
		if (parent instanceof SoftwareRelease) {
			SoftwareRelease r = (SoftwareRelease) parent;
			return r.subsystem(i);
		} else if (parent instanceof SoftwareSubsystem) {
			SoftwareSubsystem s = (SoftwareSubsystem) parent;
			return s.getPackage(i);
		} else if (parent instanceof SoftwarePackage) {
			SoftwarePackage p = (SoftwarePackage) parent;
			return p.template(i);
		}
		return null;
	}

	/** get index of child node w.r.t. parent node */
	public int getIndexOfChild(Object parent, Object node) {
		if (parent instanceof SoftwareRelease) {
			SoftwareRelease r = (SoftwareRelease) parent;
			SoftwareSubsystem s = (SoftwareSubsystem) node;
			return r.indexOfSubsystem(s);
		} else if (parent instanceof SoftwareSubsystem) {
			SoftwareSubsystem s = (SoftwareSubsystem) parent;
			SoftwarePackage p = (SoftwarePackage) node;
			return s.indexOfPackage(p);
		} else if (parent instanceof SoftwarePackage) {
			SoftwarePackage p = (SoftwarePackage) parent;
			Template t = (Template) node;
			return p.indexOfTemplate(t);
		}
		return -1;
	}

	/** get the parent of the node */
	public Object getParent(Object node) {
		if (node instanceof Template) {
			Template t = (Template) node;
			return t.parentPackage();
		} else if (node instanceof SoftwarePackage) {
			SoftwarePackage p = (SoftwarePackage) node;
			return p.subsystem();
		} else if (node instanceof SoftwareSubsystem) {
			return getRoot();
		}
		return null;
	}

}

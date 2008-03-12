package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import java.util.Iterator;
import java.util.ArrayList;

import confdb.gui.tree.AbstractTreeModel;

import confdb.diff.*;

/**
 * DiffTreeModel
 * -------------
 * @author Philipp Schieferdecker
 *
 * Visualize the result of the comparions of two configurations.
 */
public class DiffTreeModel extends AbstractTreeModel
{
    //
    // member data
    //
    
    /** diff */
    private Diff diff = null;

    /** top-level nodes */
    private StringBuffer psetsNode     = new StringBuffer();
    private StringBuffer edsourcesNode = new StringBuffer();
    private StringBuffer essourcesNode = new StringBuffer();
    private StringBuffer esmodulesNode = new StringBuffer();
    private StringBuffer servicesNode  = new StringBuffer();
    private StringBuffer pathsNode     = new StringBuffer();
    private StringBuffer sequencesNode = new StringBuffer();
    private StringBuffer modulesNode   = new StringBuffer();
    
    private ArrayList<StringBuffer> topNodes = new ArrayList<StringBuffer>();
    
    
    //
    // construction
    //

    /** standard constructor */
    public DiffTreeModel() {}


    //
    // member functions
    //
    
    /** set diff */
    public void setDiff(Diff diff)
    {
	this.diff = diff;
	updateTopNodes();
	nodeStructureChanged(topNodes);
    }

    /** update the top nodes */
    public void updateTopNodes()
    {
	topNodes.clear();
	if (this.diff==null) return;
	
	// PSets node
	int psetCount = diff.psetCount();
	if (psetCount>0) {
	    psetsNode.delete(0,psetsNode.length());
	    psetsNode.append("<html><b>PSets</b> (").append(psetCount)
		.append(")</html>");
	    topNodes.add(psetsNode);
	    Iterator<Comparison> it = diff.psetIterator();
	    while (it.hasNext()) it.next().setParent(psetsNode);
	}
	
	// EDSources node
	int edsourceCount = diff.edsourceCount();
	if (edsourceCount>0) {
	    edsourcesNode.delete(0,edsourcesNode.length());
	    edsourcesNode.append("<html><b>EDSource</b> (").append(edsourceCount)
		.append(")</html>");
	    topNodes.add(edsourcesNode);
	    Iterator<Comparison> it = diff.edsourceIterator();
	    while (it.hasNext()) it.next().setParent(edsourcesNode);
	}
	
	// ESSource node
	int essourceCount = diff.essourceCount();
	if (essourceCount>0) {
	    essourcesNode.delete(0,essourcesNode.length());
	    essourcesNode.append("<html><b>ESSources</b> (").append(essourceCount)
		.append(")</html>");
	    topNodes.add(essourcesNode);
	    Iterator<Comparison> it = diff.essourceIterator();
	    while (it.hasNext()) it.next().setParent(essourcesNode);
	}
	
	// ESModule node
	int esmoduleCount = diff.esmoduleCount();
	if (esmoduleCount>0) {
	    esmodulesNode.delete(0,esmodulesNode.length());
	    esmodulesNode.append("<html><b>ESModules</b> (").append(esmoduleCount)
		.append(")</html>");
	    topNodes.add(esmodulesNode);
	    Iterator<Comparison> it = diff.esmoduleIterator();
	    while (it.hasNext()) it.next().setParent(esmodulesNode);
	}
	
	// Service node
	int serviceCount = diff.serviceCount();
	if (serviceCount>0) {
	    servicesNode.delete(0,servicesNode.length());
	    servicesNode.append("<html><b>Services</b> (").append(serviceCount)
		.append(")</html>");
	    topNodes.add(servicesNode);
	    Iterator<Comparison> it = diff.serviceIterator();
	    while (it.hasNext()) it.next().setParent(servicesNode);
	}
	
	// Paths node
	int pathCount = diff.pathCount();
	if (pathCount>0) {
	    pathsNode.delete(0,pathsNode.length());
	    pathsNode.append("<html><b>Paths</b> (").append(pathCount)
		.append(")</html>");
	    topNodes.add(pathsNode);
	    Iterator<Comparison> it = diff.pathIterator();
	    while (it.hasNext()) it.next().setParent(pathsNode);
	}
	
	// Sequences node
	int sequenceCount = diff.sequenceCount();
	if (sequenceCount>0) {
	    sequencesNode.delete(0,sequencesNode.length());
	    sequencesNode.append("<html><b>Sequences</b> (").append(sequenceCount)
		.append(")</html>");
	    topNodes.add(sequencesNode);
	    Iterator<Comparison> it = diff.sequenceIterator();
	    while (it.hasNext()) it.next().setParent(sequencesNode);
	}
	
	// Module node
	int moduleCount = diff.moduleCount();
	if (moduleCount>0) {
	    modulesNode.delete(0,modulesNode.length());
	    modulesNode.append("<html><b>Modules</b> (").append(moduleCount)
		.append(")</html>");
	    topNodes.add(modulesNode);
	    Iterator<Comparison> it = diff.moduleIterator();
	    while (it.hasNext()) it.next().setParent(modulesNode);
	}
    }

    /** TreeModel::getRoot() */
    public Object getRoot() { return topNodes; }

    /** AbstractTreeModel::isLeaf() */
    public boolean isLeaf(Object node)
    {
	if      (node==getRoot()) return (topNodes.size()==0);
	else if (node instanceof StringBuffer) return false;
	else if (node instanceof Comparison) {
	    Comparison c = (Comparison)node;
	    return (c.comparisonCount()==0);
	}
	System.err.println("DiffTreeModel.isLeaf() ERROR.");
	return true;
    }

    /** AbstractTreeModel::getChildCount() */
    public int getChildCount(Object node)
    {
	if (node.equals(getRoot())) return topNodes.size();
	else if (node instanceof StringBuffer) {
	    if (node.equals(psetsNode))     return diff.psetCount();
	    if (node.equals(edsourcesNode)) return diff.edsourceCount();
	    if (node.equals(essourcesNode)) return diff.essourceCount();
	    if (node.equals(esmodulesNode)) return diff.esmoduleCount();
	    if (node.equals(servicesNode))  return diff.serviceCount();
	    if (node.equals(pathsNode))     return diff.pathCount();
	    if (node.equals(sequencesNode)) return diff.sequenceCount();
	    if (node.equals(modulesNode))   return diff.moduleCount();
	}
	else if (node instanceof Comparison)
	    return ((Comparison)node).comparisonCount();
	System.err.println("DiffTreeModel.getChildCount() ERROR.");
	return 0;
    }

    /** AbstractTreeModel::getChild() */
    public Object getChild(Object parent,int i)
    {
	if (parent.equals(getRoot())) return topNodes.get(i);
	else if (parent instanceof StringBuffer) {
	    if (parent.equals(psetsNode))     return diff.pset(i);
	    if (parent.equals(edsourcesNode)) return diff.edsource(i);
	    if (parent.equals(essourcesNode)) return diff.essource(i);
	    if (parent.equals(esmodulesNode)) return diff.esmodule(i);
	    if (parent.equals(servicesNode))  return diff.service(i);
	    if (parent.equals(pathsNode))     return diff.path(i);
	    if (parent.equals(modulesNode))   return diff.module(i);
	    if (parent.equals(sequencesNode)) return diff.sequence(i);
	}
	else if (parent instanceof Comparison)
	    return ((Comparison)parent).comparison(i);
	System.err.println("DiffTreeModel.getChild() ERROR.");
	return null;
    }

    /** AbstractTreeModel.getIndexOfChild() */
    public int getIndexOfChild(Object parent,Object child)
    {
	if (parent.equals(getRoot())) return topNodes.indexOf(child);
	else if (parent instanceof StringBuffer) {
	    if (parent.equals(psetsNode))
		return diff.indexOfPSet((Comparison)child);
	    if (parent.equals(edsourcesNode))
		return diff.indexOfEDSource((Comparison)child);
	    if (parent.equals(essourcesNode))
		return diff.indexOfESSource((Comparison)child);
	    if (parent.equals(esmodulesNode))
		return diff.indexOfESModule((Comparison)child);
	    if (parent.equals(servicesNode))
		return diff.indexOfService((Comparison)child);
	    if (parent.equals(pathsNode))
		return diff.indexOfPath((Comparison)child);
	    if (parent.equals(sequencesNode))
		return diff.indexOfSequence((Comparison)child);
	    if (parent.equals(modulesNode))
		return diff.indexOfModule((Comparison)child);
	}
	else if (parent instanceof Comparison)
	    return ((Comparison)parent).indexOfComparison((Comparison)child);
	System.err.println("DiffTreeModel.getIndexOfChild() ERROR.");
	return -1;
    }
    
    /** AbstractTreeModel::getParent() */
    public Object getParent(Object node)
    {
	if (node instanceof Comparison) return ((Comparison)node).parent();
	else if (node instanceof StringBuffer) return getRoot();
	System.err.println("DiffTreeModel.getParent() ERROR.");
	return null;
    }
    
}

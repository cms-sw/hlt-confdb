package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import java.util.ArrayList;

import confdb.gui.tree.AbstractTreeModel;

import confdb.data.*;


/**
 * ConfigurationTreeModel
 * ----------------------
 * @author Philipp Schieferdecker
 *
 * Display a configuration in a JTree structure.
 */
public class ConfigurationTreeModel extends AbstractTreeModel
{
    //
    // member data
    //

    /** root of the tree = configuration */
    private Configuration config = null;
    
    /** first level of nodes */
    private StringBuffer psetsNode     = new StringBuffer();
    private StringBuffer edsourcesNode = new StringBuffer();
    private StringBuffer essourcesNode = new StringBuffer();
    private StringBuffer esmodulesNode = new StringBuffer();
    private StringBuffer servicesNode  = new StringBuffer();
    private StringBuffer pathsNode     = new StringBuffer();
    private StringBuffer sequencesNode = new StringBuffer();
    private StringBuffer modulesNode   = new StringBuffer();

    private ArrayList<StringBuffer> level1Nodes = new ArrayList<StringBuffer>();
    
    
    //
    // construction
    //

    /** standard constructor */
    public ConfigurationTreeModel(Configuration config)
    {
	setConfiguration(config);
    }


    //
    // member functions
    //

    /** get the PSets root node */
    public StringBuffer psetsNode() { return psetsNode; }
    
    /** get the EDSources root node */
    public StringBuffer edsourcesNode() { return edsourcesNode; }
    
    /** get the ESSources root node */
    public StringBuffer essourcesNode() { return essourcesNode; }
    
    /** get the ESModules root node */
    public StringBuffer esmodulesNode() { return esmodulesNode; }
    
    /** get the EDSource root node */
    public StringBuffer servicesNode() { return servicesNode; }
    
    /** get the EDSource root node */
    public StringBuffer pathsNode() { return pathsNode; }
    
    /** get the EDSource root node */
    public StringBuffer sequencesNode() { return sequencesNode; }
    
    /** get the EDSource root node */
    public StringBuffer modulesNode() { return modulesNode; }
    
    /** set the configuration to be displayed */
    public void setConfiguration(Configuration config)
    {
	this.config = config;
	if (config.isEmpty()) {
	    level1Nodes.clear();
	}
	else {
	    if (level1Nodes.isEmpty()) {
		level1Nodes.add(psetsNode);
		level1Nodes.add(edsourcesNode);
		level1Nodes.add(essourcesNode);
		level1Nodes.add(esmodulesNode);
		level1Nodes.add(servicesNode);
		level1Nodes.add(pathsNode);
		level1Nodes.add(sequencesNode);
		level1Nodes.add(modulesNode);
	    }
	    updateLevel1Nodes();
	}
	nodeStructureChanged(config);
    }
    
    /** update information of level1 nodes */
    public void updateLevel1Nodes()
    {
	if (config==null) return;
	
	// PSets node
	int psetCount = config.psetCount();
	int unsetPSetCount = config.unsetTrackedPSetParameterCount();
	psetsNode.delete(0,edsourcesNode.length());
	psetsNode.append("<html>PSets (");
	psetsNode.append(psetCount);
	psetsNode.append(")");
	if (unsetPSetCount>0) {
	    psetsNode.append(" <font color=#ff0000>[");
	    psetsNode.append(unsetPSetCount);
	    psetsNode.append("]</font>");
	}
	psetsNode.append("</html>");
	nodeChanged(psetsNode);
	
	// EDSources node
	int edsourceCount = config.edsourceCount();
	int unsetEDSourceCount = config.unsetTrackedEDSourceParameterCount();
	edsourcesNode.delete(0,edsourcesNode.length());
	edsourcesNode.append("<html>EDSource (");
	edsourcesNode.append(edsourceCount);
	edsourcesNode.append(")");
	if (unsetEDSourceCount>0) {
	    edsourcesNode.append(" <font color=#ff0000>[");
	    edsourcesNode.append(unsetEDSourceCount);
	    edsourcesNode.append("]</font>");
	}
	edsourcesNode.append("</html>");
	nodeChanged(edsourcesNode);
	
	// ESSource node
	int essourceCount = config.essourceCount();
	int unsetESSourceCount = config.unsetTrackedESSourceParameterCount();
	essourcesNode.delete(0,essourcesNode.length());
	essourcesNode.append("<html>ESSources (");
	essourcesNode.append(essourceCount);
	essourcesNode.append(")");
	if (unsetESSourceCount>0) {
	    essourcesNode.append(" <font color=#ff0000>[");
	    essourcesNode.append(unsetESSourceCount);
	    essourcesNode.append("]</font>");
	}
	essourcesNode.append("</html>");
	nodeChanged(essourcesNode);
	
	// ESModule node
	int esmoduleCount = config.esmoduleCount();
	int unsetESModuleCount = config.unsetTrackedESModuleParameterCount();
	esmodulesNode.delete(0,esmodulesNode.length());
	esmodulesNode.append("<html>ESModules (");
	esmodulesNode.append(esmoduleCount);
	esmodulesNode.append(")");
	if (unsetESModuleCount>0) {
	    esmodulesNode.append(" <font color=#ff0000>[");
	    esmodulesNode.append(unsetESModuleCount);
	    esmodulesNode.append("]</font>");
	}
	esmodulesNode.append("</html>");
	nodeChanged(esmodulesNode);
	
	// Service node
	int serviceCount = config.serviceCount();
	int unsetServiceCount = config.unsetTrackedServiceParameterCount();
	servicesNode.delete(0,servicesNode.length());
	servicesNode.append("<html>Services (");
	servicesNode.append(serviceCount);
	servicesNode.append(")");
	if (unsetServiceCount>0) {
	    servicesNode.append(" <font color=#ff0000>[");
	    servicesNode.append(unsetServiceCount);
	    servicesNode.append("]</font>");
	}
	servicesNode.append("</html>");
	nodeChanged(servicesNode);
	
	// Module node
	int moduleCount = config.moduleCount();
	int unsetModuleCount = config.unsetTrackedModuleParameterCount();
	modulesNode.delete(0,modulesNode.length());
	modulesNode.append("<html>Modules (");
	modulesNode.append(moduleCount);
	modulesNode.append(")");
	if (unsetModuleCount>0) {
	    modulesNode.append(" <font color=#ff0000>[");
	    modulesNode.append(unsetModuleCount);
	    modulesNode.append("]</font>");
	}
	modulesNode.append("</html>");
	nodeChanged(modulesNode);
	
	// Paths node
	int pathCount = config.pathCount();
	pathsNode.delete(0,pathsNode.length());
	pathsNode.append("<html>Paths (");
	pathsNode.append(pathCount);
	pathsNode.append(")</html>");
	nodeChanged(pathsNode);
	
	// Sequences node
	int sequenceCount = config.sequenceCount();
	sequencesNode.delete(0,sequencesNode.length());
	sequencesNode.append("<html>Sequences (");
	sequencesNode.append(sequenceCount);
	sequencesNode.append(")</html>");
	nodeChanged(sequencesNode);
    }
    
    /** get root directory */
    public Object getRoot() { return config; }

    /** indicate if a node is a leaf node */
    public boolean isLeaf(Object node)
    {
	boolean result;
	if (node instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)node;
	    result = (pset.parameterCount()>0) ? false : true;
	}
	else if (node instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)node;
	    result = (vpset.parameterSetCount()>0) ? false : true;
	}
	else {
	    result = (node instanceof Parameter) ? true : false;
	}

	return result;
    }
    
    /** number of child nodes */
    public int getChildCount(Object node)
    {
	if (node.equals(config)) {
	    return (config.isEmpty()) ? 0 : level1Nodes.size();
	}
	else if (node instanceof StringBuffer) {
	    if (node.equals(psetsNode))     return config.psetCount();
	    if (node.equals(edsourcesNode)) return config.edsourceCount();
	    if (node.equals(essourcesNode)) return config.essourceCount();
	    if (node.equals(esmodulesNode)) return config.esmoduleCount();
	    if (node.equals(servicesNode))  return config.serviceCount();
	    if (node.equals(pathsNode))     return config.pathCount();
	    if (node.equals(modulesNode))   return config.moduleCount();
	    if (node.equals(sequencesNode)) return config.sequenceCount();
	}
	else if (node instanceof Instance) {
	    Instance instance = (Instance)node;
	    return instance.parameterCount();
	}
	else if (node instanceof ReferenceContainer) {
	    ReferenceContainer refContainer = (ReferenceContainer)node;
	    return refContainer.entryCount();
	}
	else if (node instanceof ModuleReference) {
	    ModuleReference reference = (ModuleReference)node;
	    return reference.parameterCount();
	}
	else if (node instanceof PathReference) {
	    PathReference reference = (PathReference)node;
	    Path path = (Path)reference.parent();
	    return path.entryCount();
	}
	else if (node instanceof SequenceReference) {
	    SequenceReference reference = (SequenceReference)node;
	    Sequence sequence = (Sequence)reference.parent();
	    return sequence.entryCount();
	}
	else if (node instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)node;
	    return pset.parameterCount();
	}
	else if (node instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)node;
	    return vpset.parameterSetCount();
	}
	
	return 0;
    }
    
    /** get the i-th child node */
    public Object getChild(Object parent,int i)
    {
	if (parent.equals(config)) {
	    return level1Nodes.get(i);
	}
	else if (parent instanceof StringBuffer) {
	    if (parent.equals(psetsNode))     return config.pset(i);
	    if (parent.equals(edsourcesNode)) return config.edsource(i);
	    if (parent.equals(essourcesNode)) return config.essource(i);
	    if (parent.equals(esmodulesNode)) return config.esmodule(i);
	    if (parent.equals(servicesNode))  return config.service(i);
	    if (parent.equals(pathsNode))     return config.path(i);
	    if (parent.equals(modulesNode))   return config.module(i);
	    if (parent.equals(sequencesNode)) return config.sequence(i);
	}
	else if (parent instanceof Instance) {
	    Instance instance = (Instance)parent;
	    return instance.parameter(i);
	}
	else if (parent instanceof ReferenceContainer) {
	    ReferenceContainer refContainer = (ReferenceContainer)parent;
	    return refContainer.entry(i);
	}
	else if (parent instanceof ModuleReference) {
	    ModuleReference reference = (ModuleReference)parent;
	    return reference.parameter(i);
	}
	else if (parent instanceof PathReference) {
	    PathReference reference = (PathReference)parent;
	    Path path = (Path)reference.parent();
	    return path.entry(i);
	}
	else if (parent instanceof SequenceReference) {
	    SequenceReference reference = (SequenceReference)parent;
	    Sequence sequence = (Sequence)reference.parent();
	    return sequence.entry(i);
	}
	else if (parent instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)parent;
	    return pset.parameter(i);
	}
	else if (parent instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)parent;
	    return vpset.parameterSet(i);
	}
	
	return null;
    }
    
    /** get index of a certain child w.r.t. its parent dir */
    public int getIndexOfChild(Object parent,Object child)
    {
	if (parent.equals(config)) {
	    return level1Nodes.indexOf(child);
	}
	else if (parent instanceof StringBuffer) {
	    if (parent.equals(psetsNode)) {
		PSetParameter pset = (PSetParameter)child;
		return config.indexOfPSet(pset);
	    }
	    if (parent.equals(edsourcesNode)) {
		EDSourceInstance edsource = (EDSourceInstance)child;
		return config.indexOfEDSource(edsource);
	    }
	    if (parent.equals(essourcesNode)) {
		ESSourceInstance essource = (ESSourceInstance)child;
		return config.indexOfESSource(essource);
	    }
	    if (parent.equals(esmodulesNode)) {
		ESModuleInstance esmodule = (ESModuleInstance)child;
		return config.indexOfESModule(esmodule);
	    }
	    if (parent.equals(servicesNode)) {
		ServiceInstance service = (ServiceInstance)child;
		return config.indexOfService(service);
	    }
	    if (parent.equals(pathsNode)) {
		Path path = (Path)child;
		return config.indexOfPath(path);
	    }
	    if (parent.equals(modulesNode)) {
		ModuleInstance module = (ModuleInstance)child;
		return config.indexOfModule(module);
	    }
	    if (parent.equals(sequencesNode)) {
		Sequence sequence = (Sequence)child;
		return config.indexOfSequence(sequence);
	    }
	}
	else if (parent instanceof Instance) {
	    Instance instance = (Instance)parent;
	    Parameter parameter = (Parameter)child;
	    return instance.indexOfParameter(parameter);
	}
	else if (parent instanceof ReferenceContainer) {
	    ReferenceContainer refContainer = (ReferenceContainer)parent;
	    Reference reference = (Reference)child;
	    return refContainer.indexOfEntry(reference);
	}
	else if (parent instanceof ModuleReference) {
	    ModuleReference reference = (ModuleReference)parent;
	    Parameter parameter = (Parameter)child;
	    return reference.indexOfParameter(parameter);
	}
	else if (parent instanceof PathReference) {
	    PathReference pathreference = (PathReference)parent;
	    Path path = (Path)pathreference.parent();
	    Reference reference = (Reference)child;
	    return path.indexOfEntry(reference);
	}
	else if (parent instanceof SequenceReference) {
	    SequenceReference seqreference = (SequenceReference)parent;
	    Sequence sequence = (Sequence)seqreference.parent();
	    Reference reference = (Reference)child;
	    return sequence.indexOfEntry(reference);
	}
	else if (parent instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)parent;
	    Parameter parameter = (Parameter)child;
	    return pset.indexOfParameter(parameter);
	}
	else if (parent instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)parent;
	    PSetParameter pset = (PSetParameter)child;
	    return vpset.indexOfParameterSet(pset);
	}
	
	return -1;
    }
    
    /** get parent of a node */
    public Object getParent(Object node)
    {
	if (node instanceof Parameter) {
	    Parameter p      = (Parameter)node;
	    Object    parent = p.parent();
	    return (null==parent) ? psetsNode : parent;
	}
	else if (node instanceof Reference) {
	    Reference r = (Reference)node;
	    return r.container();
	}
	else if (node instanceof EDSourceInstance) {
	    return edsourcesNode;
	}
	else if (node instanceof ESSourceInstance) {
	    return essourcesNode;
	}
	else if (node instanceof ESModuleInstance) {
	    return esmodulesNode;
	}
	else if (node instanceof ServiceInstance) {
	    return servicesNode;
	}
	else if (node instanceof ModuleInstance) {
	    return modulesNode;
	}
	else if (node instanceof Path) {
	    return pathsNode;
	}
	else if (node instanceof Sequence) {
	    return sequencesNode;
	}
	else if (node instanceof StringBuffer) {
	    return getRoot();
	}
	return null;
    }
    
}

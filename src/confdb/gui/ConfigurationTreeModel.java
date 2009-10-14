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
    private IConfiguration config = null;

    /** mode indicating which type of children are shown for contents */
    private String contentMode = "streams";

    /** mode indicating which type of childen are shown for streams */
    private String streamMode = "datasets";

    /** flag indicating if parameters are displayed or not */
    private boolean displayParameters = true;
    
    /** first level of nodes */
    private StringBuffer psetsNode     = new StringBuffer();
    private StringBuffer edsourcesNode = new StringBuffer();
    private StringBuffer essourcesNode = new StringBuffer();
    private StringBuffer esmodulesNode = new StringBuffer();
    private StringBuffer servicesNode  = new StringBuffer();
    private StringBuffer pathsNode     = new StringBuffer();
    private StringBuffer sequencesNode = new StringBuffer();
    private StringBuffer modulesNode   = new StringBuffer();
    private StringBuffer outputsNode   = new StringBuffer();
    private StringBuffer contentsNode  = new StringBuffer();
    private StringBuffer streamsNode   = new StringBuffer();
    private StringBuffer datasetsNode  = new StringBuffer();

    private ArrayList<StringBuffer> level1Nodes=new ArrayList<StringBuffer>();
    
    
    //
    // construction
    //

    /** standard constructor */
    public ConfigurationTreeModel(IConfiguration config)
    {
	setConfiguration(config);
    }

    /** constructor which allows to set displayParameter flag */
    public ConfigurationTreeModel(IConfiguration config,
				  boolean displayParameters)
    {
	this.displayParameters = displayParameters;
	setConfiguration(config);
    }

    //
    // member functions
    //

    /** retrieve content mode */
    public String contentMode() { return contentMode; }
    
    /** retrieve stream mode */
    public String streamMode() { return streamMode; }

    /** get the PSets root node */
    public StringBuffer psetsNode() { return psetsNode; }
    
    /** get the EDSources root node */
    public StringBuffer edsourcesNode() { return edsourcesNode; }
    
    /** get the ESSources root node */
    public StringBuffer essourcesNode() { return essourcesNode; }
    
    /** get the ESModules root node */
    public StringBuffer esmodulesNode() { return esmodulesNode; }
    
    /** get the services root node */
    public StringBuffer servicesNode() { return servicesNode; }
    
    /** get the paths root node */
    public StringBuffer pathsNode() { return pathsNode; }
    
    /** get the sequences root node */
    public StringBuffer sequencesNode() { return sequencesNode; }
    
    /** get the modules root node */
    public StringBuffer modulesNode() { return modulesNode; }
    
    /** get the output modules root node */
    public StringBuffer outputsNode() { return outputsNode; }
    
    /** get the contents root node */
    public StringBuffer contentsNode() { return contentsNode; }
    
    /** get the streams root node */
    public StringBuffer streamsNode() { return streamsNode; }
    
    /** get the datasets root node */
    public StringBuffer datasetsNode() { return datasetsNode; }
    
    /** set the configuration to be displayed */
    public void setConfiguration(IConfiguration config)
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
		level1Nodes.add(outputsNode);
		level1Nodes.add(contentsNode);
		level1Nodes.add(streamsNode);
		level1Nodes.add(datasetsNode);
	    }
	    updateLevel1Nodes();
	}
	nodeStructureChanged(config);
    }

    /** set the content mode, indicating what children are displayed */
    public boolean setContentMode(String contentMode)
    {
	if (contentMode.equals("streams")||
	    contentMode.equals("paths")||
	    contentMode.equals("datasets")) {
	    this.contentMode = contentMode;
	    return true;
	}
	System.err.println("ConfigurationTreeModel.setContentMode() ERROR: "+
			   "invalid mode '" + contentMode +"'");
	return false;
    }
    
    /** set the stream mode, indicating what children are displayed */
    public boolean setStreamMode(String streamMode)
    {
	if (streamMode.equals("paths")||streamMode.equals("datasets")) {
	    this.streamMode = streamMode;
	    return true;
	}
	System.err.println("ConfigurationTreeModel.setStreamMode() ERROR: "+
			   "invalid mode '" + streamMode +"'");
	return false;
    }

    /** update information of level1 nodes */
    public void updateLevel1Nodes()
    {
	if (config==null) return;
	
	// PSets node
	int psetCount = config.psetCount();
	int unsetPSetCount = config.unsetTrackedPSetParameterCount();
	psetsNode.delete(0,psetsNode.length());
	psetsNode.append("<html><b>PSets</b> (");
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
	edsourcesNode.append("<html><b>EDSource</b> (");
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
	essourcesNode.append("<html><b>ESSources</b> (");
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
	esmodulesNode.append("<html><b>ESModules</b> (");
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
	servicesNode.append("<html><b>Services</b> (");
	servicesNode.append(serviceCount);
	servicesNode.append(")");
	if (unsetServiceCount>0) {
	    servicesNode.append(" <font color=#ff0000>[");
	    servicesNode.append(unsetServiceCount);
	    servicesNode.append("]</font>");
	}
	servicesNode.append("</html>");
	nodeChanged(servicesNode);

	// Paths node
	int pathCount = config.pathCount();
	pathsNode.delete(0,pathsNode.length());
	pathsNode.append("<html><b>Paths</b> (");
	pathsNode.append(pathCount);
	pathsNode.append(")</html>");
	nodeChanged(pathsNode);
	
	// Sequences node
	int sequenceCount = config.sequenceCount();
	sequencesNode.delete(0,sequencesNode.length());
	sequencesNode.append("<html><b>Sequences</b> (");
	sequencesNode.append(sequenceCount);
	sequencesNode.append(")</html>");
	nodeChanged(sequencesNode);
	
	// Module node
	int moduleCount = config.moduleCount();
	int unsetModuleCount = config.unsetTrackedModuleParameterCount();
	modulesNode.delete(0,modulesNode.length());
	modulesNode.append("<html><b>Modules</b> (");
	modulesNode.append(moduleCount);
	modulesNode.append(")");
	if (unsetModuleCount>0) {
	    modulesNode.append(" <font color=#ff0000>[");
	    modulesNode.append(unsetModuleCount);
	    modulesNode.append("]</font>");
	}
	modulesNode.append("</html>");
	nodeChanged(modulesNode);

	// OutputModule node
	int outputCount = config.outputCount();
	outputsNode.delete(0,outputsNode.length());
	outputsNode.append("<html><b>OutputModules</b> (");
	outputsNode.append(outputCount);
	outputsNode.append(")</html>");
	nodeChanged(outputsNode);

	// Content node
	int contentCount = config.contentCount();
	contentsNode.delete(0,contentsNode.length());
	contentsNode.append("<html><b>EventContents</b> (");
	contentsNode.append(contentCount);
	contentsNode.append(")");
	contentsNode.append("</html>");
	nodeChanged(contentsNode);

	// Stream node
	int streamCount = config.streamCount();
	streamsNode.delete(0,streamsNode.length());
	streamsNode.append("<html><b>Streams</b> (");
	streamsNode.append(streamCount);
	streamsNode.append(")");
	streamsNode.append("</html>");
	nodeChanged(streamsNode);

	// Dataset node
	int datasetCount = config.datasetCount();
	datasetsNode.delete(0,datasetsNode.length());
	datasetsNode.append("<html><b>PrimaryDatasets</b> (");
	datasetsNode.append(datasetCount);
	datasetsNode.append(")");
	datasetsNode.append("</html>");
	nodeChanged(datasetsNode);
	
    }
    
    /** get root directory */
    public Object getRoot() { return config; }

    /** indicate if a node is a leaf node */
    public boolean isLeaf(Object node)
    {
	return (getChildCount(node)==0);
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
	    if (node.equals(sequencesNode)) return config.sequenceCount();
	    if (node.equals(modulesNode))   return config.moduleCount();
	    if (node.equals(outputsNode))   return config.outputCount();
	    if (node.equals(contentsNode))  return config.contentCount();
	    if (node.equals(streamsNode))   return config.streamCount();
	    if (node.equals(datasetsNode))  return config.datasetCount();
	}
	else if (!displayParameters) {
	    return 0;
	}
	else if (node instanceof ParameterContainer) {
	    ParameterContainer container = (ParameterContainer)node;
	    return container.parameterCount();
	}
	else if (node instanceof EventContent) {
	    EventContent content = (EventContent)node;
	    if (contentMode.equals("streams"))  return content.streamCount();
	    if (contentMode.equals("paths"))    return content.pathCount();
	    if (contentMode.equals("datasets")) return content.datasetCount();
	}
	else if (node instanceof Stream) {
	    Stream stream = (Stream)node;
	    if (streamMode.equals("datasets")) return stream.datasetCount();
	    if (streamMode.equals("paths"))    return stream.pathCount();
	}
	else if (node instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)node;
	    return dataset.pathCount();
	}
	else if (node instanceof ReferenceContainer) {
	    ReferenceContainer refContainer = (ReferenceContainer)node;
	    return refContainer.entryCount();
	}
	else if (node instanceof ModuleReference) {
	    ModuleReference reference = (ModuleReference)node;
	    return reference.parameterCount();
	}
	else if (node instanceof OutputModuleReference) {
	    OutputModuleReference reference = (OutputModuleReference)node;
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
	else if (node instanceof ConfigurationTreeNode) {
	    return 0;
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
	    if (parent.equals(sequencesNode)) return config.sequence(i);
	    if (parent.equals(modulesNode))   return config.module(i);
	    if (parent.equals(outputsNode))   return config.output(i);
	    if (parent.equals(contentsNode))  return config.content(i);
	    if (parent.equals(streamsNode))   return config.stream(i);
	    if (parent.equals(datasetsNode))  return config.dataset(i);
	}
	else if (parent instanceof ParameterContainer) {
	    ParameterContainer container = (ParameterContainer)parent;
	    return container.parameter(i);
	}
	else if (parent instanceof EventContent) {
	    EventContent content = (EventContent)parent;
	    if (contentMode.equals("streams"))
		return new ConfigurationTreeNode(content,content.stream(i));
	    if (contentMode.equals("paths"))
		return new ConfigurationTreeNode(content,content.path(i));
	    if (contentMode.equals("datasets"))
		return new ConfigurationTreeNode(content,content.dataset(i));
	}
	else if (parent instanceof Stream) {
	    Stream stream = (Stream)parent;
	    if (streamMode.equals("datasets"))
		return new ConfigurationTreeNode(stream,stream.dataset(i));
	    if (streamMode.equals("paths"))
		return new ConfigurationTreeNode(stream,stream.path(i));
	}
	else if (parent instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)parent;
	    return new ConfigurationTreeNode(dataset,dataset.path(i));
	}
	else if (parent instanceof ReferenceContainer) {
	    ReferenceContainer refContainer = (ReferenceContainer)parent;
	    return refContainer.entry(i);
	}
	else if (parent instanceof ModuleReference) {
	    ModuleReference reference = (ModuleReference)parent;
	    return reference.parameter(i);
	}
	else if (parent instanceof OutputModuleReference) {
	    OutputModuleReference reference = (OutputModuleReference)parent;
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
	    if (parent.equals(sequencesNode)) {
		Sequence sequence = (Sequence)child;
		return config.indexOfSequence(sequence);
	    }
	    if (parent.equals(modulesNode)) {
		ModuleInstance module = (ModuleInstance)child;
		return config.indexOfModule(module);
	    }
	    if (parent.equals(outputsNode)) {
		OutputModule output = (OutputModule)child;
		return config.indexOfOutput(output);
	    }
	    if (parent.equals(contentsNode)) {
		EventContent content = (EventContent)child;
		return config.indexOfContent(content);
	    }
	    if (parent.equals(streamsNode)) {
		Stream stream = (Stream)child;
		return config.indexOfStream(stream);
	    }
	    if (parent.equals(datasetsNode)) {
		PrimaryDataset dataset = (PrimaryDataset)child;
		return config.indexOfDataset(dataset);
	    }
	}
	else if (parent instanceof ParameterContainer) {
	    ParameterContainer container = (ParameterContainer)parent;
	    Parameter parameter = (Parameter)child;
	    return container.indexOfParameter(parameter);
	}
	else if (parent instanceof EventContent) {
	    EventContent content = (EventContent)parent;
	    Stream stream = (Stream)child;
	    return content.indexOfStream(stream);
	}
	else if (parent instanceof Stream) {
	    Stream stream = (Stream)parent;
	    PrimaryDataset dataset = (PrimaryDataset)child;
	    return stream.indexOfDataset(dataset);
	}
	else if (parent instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)parent;
	    ConfigurationTreeNode treeNode = (ConfigurationTreeNode)child;
	    Path path = (Path)treeNode.object();
	    return dataset.indexOfPath(path);
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
	else if (parent instanceof OutputModuleReference) {
	    OutputModuleReference reference = (OutputModuleReference)parent;
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
	else if (node instanceof StringBuffer)     return getRoot();
	else if (node instanceof EDSourceInstance) return edsourcesNode;
	else if (node instanceof ESSourceInstance) return essourcesNode;
	else if (node instanceof ESModuleInstance) return esmodulesNode;
	else if (node instanceof ServiceInstance)  return servicesNode;
	else if (node instanceof ModuleInstance)   return modulesNode;
	else if (node instanceof OutputModule)     return outputsNode;
	else if (node instanceof Path) 	           return pathsNode;
	else if (node instanceof Sequence)         return sequencesNode;
	else if (node instanceof EventContent)     return contentsNode;
	else if (node instanceof Stream)           return streamsNode;
	else if (node instanceof PrimaryDataset)   return datasetsNode;
	else if (node instanceof ConfigurationTreeNode) {
	    ConfigurationTreeNode treeNode = (ConfigurationTreeNode)node;
	    return treeNode.parent();
	}
	
	return null;
    }
    
}

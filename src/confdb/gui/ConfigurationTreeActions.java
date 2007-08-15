package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import java.util.ArrayList;

import confdb.data.*;

/**
 * ConfigurationTreeActions
 * ------------------------
 * @author Philipp Schieferdecker
 *
 * Repository of actions which change the configuration data model and
 * at the same time need to be visually represented in the JTree.
 */
public class ConfigurationTreeActions
{
    //
    // Parameters
    //
    
    /** copy a parameter into another (v)pset */
    public static boolean insertParameter(JTree              tree,
					  Parameter          parameter,
					  ParameterTreeModel parameterTreeModel)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	Object target         = treePath.getLastPathComponent();
	Object parentOfTarget = treePath.getParentPath().getLastPathComponent();
	
	Object child  = null;
	Object parent = null;
	int    index  = -1;
	Object parentOfPSet = null;
	
	if (target instanceof VPSetParameter && parameter instanceof PSetParameter) {
	    PSetParameter  pset  = (PSetParameter)parameter.clone(null);
	    VPSetParameter vpset = (VPSetParameter)target;
	    vpset.addParameterSet(pset);
	    child        = pset;
	    parent       = vpset;
	    index        = vpset.parameterSetCount()-1;
	    parentOfPSet = vpset.parent();
	}
	else if (target instanceof PSetParameter) {
	    Parameter     p    = (Parameter)parameter.clone(null);
	    PSetParameter pset = (PSetParameter)target;
	    pset.addParameter(p);
	    child        = p;
	    parent       = pset;
	    index        = pset.parameterCount()-1;
	    parentOfPSet = pset.parent();
	}
	else if (parentOfTarget instanceof PSetParameter) {
	    Parameter     p       = (Parameter)parameter.clone(null);
	    Parameter     ptarget = (Parameter)target;
	    PSetParameter pset    = (PSetParameter)parentOfTarget;
	    child        = p;
	    parent       = pset;
	    index        = pset.indexOfParameter(ptarget)+1;
	    parentOfPSet = pset.parent();
	    pset.addParameter(index,p);
	}
	else {
	    return false;
	}
	
	config.setHasChanged(true);

	model.nodeInserted(parent,index);
	model.updateLevel1Nodes();
	
	// notify the tree of the children of the child which were inserted
	if (child instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)child;
	    for (int i=0;i<pset.parameterCount();i++) {
		model.nodeInserted(child,i);
	    }
	}

	if (child instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)child;
	    for (int i=0;i<vpset.parameterSetCount();i++) {
		model.nodeInserted(child,i);
	    }
	}
	
	// notify the parameter tree model (parameter tree table)
	if (parameterTreeModel!=null) {
	    parameterTreeModel.setNextFromListener();
	    parameterTreeModel.nodeInserted(parent,index);
	}
	
	// notify the parent component that is has changed
	while (parentOfPSet != null) {
	    if (parentOfPSet instanceof DatabaseEntry) {
		DatabaseEntry dbEntry = (DatabaseEntry)parentOfPSet;
		dbEntry.setHasChanged();
		parentOfPSet = null;
	    }
	    else if (parentOfPSet instanceof Parameter) {
		Parameter p = (Parameter)parentOfPSet;
		parentOfPSet = p.parent();
	    }
	    else {
		parentOfPSet = null;
	    }
	}
	
	return true;
    }
    

    //
    // Global PSets
    //

    /** insert global pset */
    public static boolean insertPSet(JTree tree,PSetParameter pset)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	config.insertPSet(pset);

	model.nodeInserted(model.psetsNode(),config.psetCount()-1);
	model.updateLevel1Nodes();
	
	TreePath parentPath = (treePath.getPathCount()==2) ?
	    treePath : treePath.getParentPath();
	tree.setSelectionPath(parentPath.pathByAddingChild(pset));
	
	return true;
    }
    
    /** remove global pset */
    public static boolean removePSet(JTree tree,PSetParameter pset)
    {
	return removeNode(tree,pset);
    }
     
    /** sort global psets */
    public static void sortPSets(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	config.sortPSets();
	model.nodeStructureChanged(model.psetsNode());
    }
    

    //
    // EDSources
    //
    
    /** insert EDSource */
    public static boolean insertEDSource(JTree tree,String templateName)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	Template template = config.release().edsourceTemplate(templateName);
	return insertInstance(tree,template);
    }
    
    /** import EDSource */
    public static boolean importEDSource(JTree tree,EDSourceInstance external)
    {
	return importInstance(tree,external);
    }

    /** remove EDSource */
    public static boolean removeEDSource(JTree tree,EDSourceInstance edsource)
    {
	return removeNode(tree,edsource);
    }
    
    /** sort EDSources */
    public static void sortEDSources(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	config.sortEDSources();
	model.nodeStructureChanged(model.edsourcesNode());
    }
    

    //
    // ESSources
    //
    
    /** insert ESSource */
    public static boolean insertESSource(JTree tree,String templateName)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	Template template = config.release().essourceTemplate(templateName);
	return insertInstance(tree,template);
    }
    
    /** import ESSource */
    public static boolean importESSource(JTree tree,ESSourceInstance external)
    {
	return importInstance(tree,external);
    }

    /** remove ESSource */
    public static boolean removeESSource(JTree tree,ESSourceInstance essource)
    {
	return removeNode(tree,essource);
    }
    
    /** sort ESSources */
    public static void sortESSources(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	config.sortESSources();
	model.nodeStructureChanged(model.essourcesNode());
    }
    

    //
    // ESModules
    //
    
    /** insert ESModule */
    public static boolean insertESModule(JTree tree,String templateName)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	Template  template = config.release().esmoduleTemplate(templateName);
	return insertInstance(tree,template);
    }
    
    /** import ESModule */
    public static boolean importESModule(JTree tree,ESModuleInstance external)
    {
	return importInstance(tree,external);
    }

    /** remove ESModule */
    public static boolean removeESModule(JTree tree,ESModuleInstance esmodule)
    {
	return removeNode(tree,esmodule);
    }

    /** sort ESModules */
    public static void sortESModules(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	config.sortESModules();
	model.nodeStructureChanged(model.esmodulesNode());
    }
    

    //
    // Services
    //
    
    /** insert Service */
    public static boolean insertService(JTree tree,String templateName)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	Template  template = config.release().serviceTemplate(templateName);
	return insertInstance(tree,template);
    }

    /** import Service */
    public static boolean importService(JTree tree,ServiceInstance external)
    {
	return importInstance(tree,external);
    }

    /** remove Service */
    public static boolean removeService(JTree tree,ServiceInstance service)
    {
	return removeNode(tree,service);
    }
    
    /** sort services */
    public static void sortServices(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	config.sortServices();
	model.nodeStructureChanged(model.servicesNode());
    }
    
    //
    // Paths & Sequences
    //

    /** insert a new path */
    public static boolean insertPath(JTree tree)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	int index = (treePath.getPathCount()==2) ?
	    0 :model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				     treePath.getLastPathComponent())+1;
	
	Path path = config.insertPath(index,"<ENTER PATH NAME>");
	
	model.nodeInserted(model.pathsNode(),index);
	model.updateLevel1Nodes();
	
	TreePath parentPath = (index==0) ? treePath : treePath.getParentPath();
	TreePath newTreePath = parentPath.pathByAddingChild(path);

	tree.setSelectionPath(newTreePath);
	editNodeName(tree);
	
	return true;
    }
    
    /** insert a new sequence */
    public static boolean insertSequence(JTree tree)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	int index = (treePath.getPathCount()==2) ?
	    0 :model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				     treePath.getLastPathComponent())+1;
	
	Sequence sequence = config.insertSequence(index,"<ENTER SEQUENCE NAME>");
	
	model.nodeInserted(model.sequencesNode(),index);
	model.updateLevel1Nodes();
	
	TreePath parentPath = (index==0) ? treePath : treePath.getParentPath();
	TreePath newTreePath = parentPath.pathByAddingChild(sequence);

	tree.setSelectionPath(newTreePath);	
	editNodeName(tree);

	return true;
    }
    
    /** import Path / Sequence */
    public static boolean importReferenceContainer(JTree tree,
						   ReferenceContainer external)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	if (!config.hasUniqueQualifier(external)) return false;
	if (!config.hasUniqueEntries(external)) return false;

	int index = (treePath.getPathCount()==2) ?
	    0:model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				    treePath.getLastPathComponent())+1;
	
	ReferenceContainer container = null;
	Object             parent    = null;
	if (external instanceof Path){
	    container = config.insertPath(index,external.name());
	    parent    = model.pathsNode();
	}
	else if (external instanceof Sequence) {
	    container = config.insertSequence(index,external.name());
	    parent    = model.sequencesNode();
	}
	if (container==null) return false;
	
	model.nodeInserted(parent,index);
	importContainerEntries(config,model,external,container);
	container.setDatabaseId(external.databaseId());
	model.updateLevel1Nodes();
	
	return true;
    }

    /** insert entries of an external reference container into the local copy */
    private static void importContainerEntries(Configuration        config,
					       ConfigurationTreeModel treeModel,
					       ReferenceContainer   sourceContainer,
					       ReferenceContainer   targetContainer)
    {
	for (int i=0;i<sourceContainer.entryCount();i++) {
	    Reference entry = sourceContainer.entry(i);
	    
	    if (entry instanceof ModuleReference) {
		ModuleReference sourceRef = (ModuleReference)entry;
		ModuleInstance  source    = (ModuleInstance)sourceRef.parent();
		ModuleReference targetRef =
		    config.insertModuleReference(targetContainer,i,
						 source.template().name(),
						 source.name());
		ModuleInstance  target = (ModuleInstance)targetRef.parent();
		for (int j=0;j<target.parameterCount();j++)
		    target.updateParameter(j,source.parameter(j).valueAsString());
		treeModel.nodeInserted(targetContainer,i);
		treeModel.nodeInserted(treeModel.modulesNode(),
				       config.moduleCount()-1);
		target.setDatabaseId(source.databaseId());
	    }
	    else if (entry instanceof PathReference) {
		PathReference sourceRef = (PathReference)entry;
		Path          source    = (Path)sourceRef.parent();
		Path          target    = config.insertPath(config.pathCount(),
							    sourceRef.name());
		PathReference targetRef = config.insertPathReference(targetContainer,
								     i,target);
		treeModel.nodeInserted(targetContainer,i);
		treeModel.nodeInserted(treeModel.pathsNode(),
				       config.pathCount()-1);
		importContainerEntries(config,treeModel,source,target);
		target.setDatabaseId(source.databaseId());
	    }
	    else if (entry instanceof SequenceReference) {
		SequenceReference sourceRef = (SequenceReference)entry;
		Sequence          source    = (Sequence)sourceRef.parent();
		Sequence          target    = config.insertSequence(config
								    .sequenceCount(),
								    sourceRef
								    .name());
		SequenceReference targetRef =
		    config.insertSequenceReference(targetContainer,i,target);
		treeModel.nodeInserted(targetContainer,i);
		treeModel.nodeInserted(treeModel.sequencesNode(),
				       config.sequenceCount()-1);
		importContainerEntries(config,treeModel,source,target);
		target.setDatabaseId(source.databaseId());
	    }
	}
    }
        
    /** insert reference into currently selected reference container */
    public static boolean insertReference(JTree tree,String type,String name)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	int                    depth    = treePath.getPathCount();
	
	TreePath parentTreePath =
	    (depth==3) ? treePath : treePath.getParentPath();
	ReferenceContainer parent =
	    (ReferenceContainer)parentTreePath.getLastPathComponent();
	int index = (depth==3) ?
	    0 : parent.indexOfEntry((Reference)treePath.getLastPathComponent())+1;
	
	Reference      reference    = null;
	ModuleInstance module       = null;
	
	if (type.compareToIgnoreCase("Path")==0) {
	    Path referencedPath = config.path(name);
	    if (referencedPath==null) return false;
	    reference=config.insertPathReference(parent,index,referencedPath);
	}
	else if (type.compareToIgnoreCase("Sequence")==0) {
	    Sequence referencedSequence = config.sequence(name);
	    if (referencedSequence==null) return false;
	    reference=config.insertSequenceReference(parent,index,referencedSequence);
	}
	else if (type.compareToIgnoreCase("Module")==0) {
	    String templateName = name;
	    String instanceName = "";
	    int    pos          = templateName.indexOf(":");
	    if (pos>0) {
		instanceName = templateName.substring(pos+1);
		templateName = templateName.substring(0,pos);
	    }
	    
	    ModuleTemplate template = config.release().moduleTemplate(templateName);
	    if (template.hasInstance(instanceName)) {
		try {
		    module = (ModuleInstance)template.instance(instanceName);
		}
		catch (DataException e) {
		    System.out.println(e.getMessage());
		    return false;
		}
		reference = config.insertModuleReference(parent,index,module);
	    }
	    else  {
		instanceName = templateName; int count=2;
		while (template.hasInstance(instanceName)) {
		    instanceName = templateName + count; ++count;
		}
		reference = config.insertModuleReference(parent,index,
							 templateName,instanceName);
		module = (ModuleInstance)reference.parent();
	    }
	}
	
	model.nodeInserted(parent,index);
	model.updateLevel1Nodes();

	TreePath newTreePath = parentTreePath.pathByAddingChild(reference);
	tree.expandPath(newTreePath.getParentPath());
	tree.setSelectionPath(newTreePath);
	
	if (module!=null&&module.referenceCount()==1) {
	    TreePath moduleTreePath =
		new TreePath(model.getPathToRoot((Object)module));
	    model.nodeInserted(model.modulesNode(),config.moduleCount()-1);
	    editNodeName(tree);
	}
	
	return true;
    }
    
    /** remove a reference container */
    public static boolean removeReferenceContainer(JTree tree)
    {
	ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
	Configuration          config    = (Configuration)model.getRoot();
	TreePath               treePath  = tree.getSelectionPath();
	ReferenceContainer     container =
	    (ReferenceContainer)treePath.getLastPathComponent();
	
	ArrayList<Integer> unreferencedIndices = new ArrayList<Integer>();
	for (int i=0;i<container.entryCount();i++) {
	    Reference entry = container.entry(i);
	    if (entry instanceof ModuleReference) {
		ModuleReference reference = (ModuleReference)entry;
		ModuleInstance  instance  = (ModuleInstance)reference.parent();
		if (instance.referenceCount()==1)
		    unreferencedIndices.add(i);
	    }
	}
	
	int    childIndices[] = null;
	Object children[]     = null;
	if (unreferencedIndices.size()>0) {
	    childIndices = new int[unreferencedIndices.size()];
	    children     = new Object[unreferencedIndices.size()];
	    for (Integer i : unreferencedIndices) {
		childIndices[i.intValue()] = i.intValue();
		children[i.intValue()] = container.entry(i.intValue());
	    }
	}
	
	int    index  = -1;
	Object parent = null;
	if (container instanceof Path) {
	    index  = config.indexOfPath((Path)container);
	    parent = model.pathsNode();
	    config.removePath((Path)container);
	}
	else if (container instanceof Sequence) {
	    index = config.indexOfSequence((Sequence)container);
	    parent = model.sequencesNode();
	    config.removeSequence((Sequence)container);
	}
	
	model.nodeRemoved(parent,index,container);
	if (childIndices!=null)
	    model.nodesRemoved(model.modulesNode(),childIndices,children);
	model.updateLevel1Nodes();
	
	TreePath parentTreePath = treePath.getParentPath();
	if (index==0)
	    tree.setSelectionPath(parentTreePath);
	else
	    tree.setSelectionPath(parentTreePath
				  .pathByAddingChild(model.getChild(parent,index-1)));
	
	return true;
    }
    
    /** remove reference from currently selected reference container */
    public static boolean removeReference(JTree tree)
    {
	ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
	Configuration          config    = (Configuration)model.getRoot();
	TreePath               treePath  = tree.getSelectionPath();
	Reference              reference = (Reference)treePath.getLastPathComponent();
	ReferenceContainer     container = reference.container();
	int                    index     = container.indexOfEntry(reference);
	ModuleInstance         module    = null;
	int                    indexOfModule= -1;
	
	if (reference instanceof ModuleReference) {
	    module = (ModuleInstance)reference.parent();
	    indexOfModule = config.indexOfModule(module);
	    config.removeModuleReference((ModuleReference)reference);
	}
	else {
	    container.removeEntry(reference);
	    config.setHasChanged(true);
	}
	
	model.nodeRemoved(container,index,reference);
	if (module!=null&&module.referenceCount()==0)
	    model.nodeRemoved(model.modulesNode(),indexOfModule,module);
	model.updateLevel1Nodes();
	
	TreePath parentTreePath = treePath.getParentPath();
	Object   parent         = parentTreePath.getLastPathComponent();
	if (index==0)
	    tree.setSelectionPath(parentTreePath);
	else
	    tree.setSelectionPath(parentTreePath
				  .pathByAddingChild(model.getChild(parent,index-1)));

	return true;
    }

    /** import a single module into path or sequence */
    public static boolean importModule(JTree tree,ModuleInstance external)
    {
	ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
	Configuration          config    = (Configuration)model.getRoot();
	TreePath               treePath  = tree.getSelectionPath();
	Object                 targetNode= treePath.getLastPathComponent();
	
	if (!config.isUniqueQualifier(external.name())) return false;
	
	ReferenceContainer parent        = null;
	ModuleInstance     target        = null;
	int                insertAtIndex = 0;
	
	if (targetNode instanceof ReferenceContainer) {
	    parent = (ReferenceContainer)targetNode;
	    ModuleReference reference =
		config.insertModuleReference(parent,0,
					     external.template().name(),
					     external.name());
	    target = (ModuleInstance)reference.parent();
	}
	else if (targetNode instanceof Reference) {
	    Reference selectedRef = (Reference)targetNode;
	    parent = selectedRef.container();
	    insertAtIndex = parent.indexOfEntry(selectedRef) + 1;
	    ModuleReference reference =
		config.insertModuleReference(parent,insertAtIndex,
					     external.template().name(),
					     external.name());
	    target = (ModuleInstance)reference.parent();
	}
	
	if (target==null) return false;

	for (int i=0;i<target.parameterCount();i++)
	    target.updateParameter(i,external.parameter(i).valueAsString());
	target.setDatabaseId(external.databaseId());
	model.nodeInserted(parent,insertAtIndex);
	model.nodeInserted(model.modulesNode(),config.moduleCount()-1);
	model.updateLevel1Nodes();

	return true;
    }
    
    /** sort Paths */
    public static void sortPaths(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	config.sortPaths();
	model.nodeStructureChanged(model.pathsNode());
    }

    /** sort Sequences */
    public static void sortSequences(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	config.sortSequences();
	model.nodeStructureChanged(model.sequencesNode());
    }

    /** sort Modules */
    public static void sortModules(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	config.sortModules();
	model.nodeStructureChanged(model.modulesNode());
    }
    

    //
    // generic functions
    //

    /**
     * insert a node into the tree and add the respective component to
     * the configuration
     */
    private static boolean insertInstance(JTree tree,Template template)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	String templateName = template.name();
	String instanceName = templateName;
	int    count        = 2;
	while (template.hasInstance(instanceName)) {
	    instanceName = templateName + count;
	    count++;
	}

	int    index = (treePath.getPathCount()==2) ?
	    0:model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				    treePath.getLastPathComponent())+1;

	Instance instance = null;
	Object   parent   = null;
	
	if (template instanceof EDSourceTemplate) {
	    instance = config.insertEDSource(templateName);
	    parent   = model.edsourcesNode();
	}
	else if (template instanceof ESSourceTemplate) {
	    instance = config.insertESSource(index,templateName,instanceName);
	    parent   = model.essourcesNode();
	}
	else if (template instanceof ESModuleTemplate) {
	    instance = config.insertESModule(index,templateName,instanceName);
	    parent   = model.esmodulesNode();
	}
	else if (template instanceof ServiceTemplate) {
	    instance = config.insertService(index,templateName);
	    parent   = model.servicesNode();
	}
	else return false;
	
	model.nodeInserted(parent,index);
	model.updateLevel1Nodes();
	
	TreePath newTreePath =
	    (index==0) ? treePath.pathByAddingChild(instance) :
	    treePath.getParentPath().pathByAddingChild(instance);
	tree.expandPath(newTreePath.getParentPath());
	tree.setSelectionPath(newTreePath);
	
	if (instance instanceof ESSourceInstance ||
	    instance instanceof ESModuleInstance) {
	    editNodeName(tree);
	}
	
	return true;
    }

    
    /**
     * import a node into the tree and add the respective component
     * to the configuration
     */
    private static boolean importInstance(JTree tree,Instance external)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	if (!config.isUniqueQualifier(external.name())) return false;

	int index = (treePath.getPathCount()==2) ?
	    0:model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				    treePath.getLastPathComponent())+1;
	
	String   templateName = external.template().name();
	String   instanceName = external.name();
	Instance instance     = null;
	Object   parent       = null;
	
	if (external instanceof EDSourceInstance) {
	    instance = config.insertEDSource(templateName);
	    parent = model.edsourcesNode();
	}
	else if (external instanceof ESSourceInstance) {
	    instance = config.insertESSource(index,templateName,instanceName);
	    parent   = model.essourcesNode();
	}
	else if (external instanceof ESModuleInstance) {
	    instance = config.insertESModule(index,templateName,instanceName);
	    parent   = model.esmodulesNode();
	}
	else if (external instanceof ServiceInstance) {
	    instance = config.insertService(index,templateName);
	    parent   = model.servicesNode();
	}
	else return false;
	
	for (int i=0;i<instance.parameterCount();i++)
	    instance.updateParameter(i,external.parameter(i).valueAsString());
	instance.setDatabaseId(external.databaseId());
	
	model.nodeInserted(parent,index);
	model.updateLevel1Nodes();
	
	return true;
    }

    /**
     * remove a node from the tree and the respective component from
     * the configuration
     */
    private static boolean removeNode(JTree tree,Object node)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath().getParentPath();
	
	int    index  =   -1;
	Object parent = null;

	if (node instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)node;
	    index = config.indexOfPSet(pset); if (index<0) return false;
	    config.removePSet(pset);
	    parent = model.psetsNode();
	}
	else if (node instanceof EDSourceInstance) {
	    EDSourceInstance edsource = (EDSourceInstance)node;
	    index = config.indexOfEDSource(edsource); if (index<0) return false;
	    config.removeEDSource(edsource);
	    parent = model.edsourcesNode();
	}
	else if (node instanceof ESSourceInstance) {
	    ESSourceInstance essource = (ESSourceInstance)node;
	    index = config.indexOfESSource(essource); if (index<0) return false;
	    config.removeESSource(essource);
	    parent = model.essourcesNode();
	}
	else if (node instanceof ESModuleInstance) {
	    ESModuleInstance esmodule = (ESModuleInstance)node;
	    index = config.indexOfESModule(esmodule); if (index<0) return false;
	    config.removeESModule(esmodule);
	    parent = model.esmodulesNode();
	}
	else if (node instanceof ServiceInstance) {
	    ServiceInstance service = (ServiceInstance)node;
	    index = config.indexOfService(service); if (index<0) return false;
	    config.removeService(service);
	    parent = model.servicesNode();
	}
	else return false;
	
	model.nodeRemoved(parent,index,node);
	model.updateLevel1Nodes();
	
	if (index==0)
	    tree.setSelectionPath(treePath);
	else
	    tree.setSelectionPath(treePath
				  .pathByAddingChild(model.getChild(parent,index-1)));
	
	return true;
    }
    
    /*
     * edit the name of the node
     */
    public static void editNodeName(JTree tree)
    {
	TreePath treePath = tree.getSelectionPath();
	tree.expandPath(treePath.getParentPath());
	tree.scrollPathToVisible(treePath);
	tree.startEditingAtPath(treePath);
    }

}


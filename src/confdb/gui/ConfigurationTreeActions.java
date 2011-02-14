package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.diff.Diff;
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
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	IConfiguration         config   = (IConfiguration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	Object target         = treePath.getLastPathComponent();
	Object parentOfTarget = treePath.getParentPath().getLastPathComponent();
	
	Object child  = null;
	Object parent = null;
	int    index  = -1;
	Object parentOfPSet = null;
	
	if (target instanceof VPSetParameter&&
	    parameter instanceof PSetParameter) {
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
	if (parameterTreeModel!=null)
	    parameterTreeModel.nodeInserted(parent,index);
	
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
    /*
      public static void sortPSets(JTree tree)
      {
      ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
      Configuration          config = (Configuration)model.getRoot();
      config.sortPSets();
      model.nodeStructureChanged(model.psetsNode());
      }
    */
    

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

	if (templateName.indexOf(':')>=0) {
	    String[] s = templateName.split(":");
	    Template template = config.release().essourceTemplate(s[1]);
	    Instance original = null;
	    try {
		original = template.instance(s[2]);
	    }
	    catch (DataException e) {
		System.err.println(e.getMessage());
		return false;
	    }
	    return insertCopy(tree,original);
	}
	else {
	    Template template = config.release().essourceTemplate(templateName);
	    return insertInstance(tree,template);
	}
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
	
	if (templateName.indexOf(':')>0) {
	    String[] s = templateName.split(":");
	    Template template = config.release().esmoduleTemplate(s[1]);
	    Instance original = null;
	    try {
		original = template.instance(s[2]);
	    }
	    catch (DataException e) {
		System.err.println(e.getMessage());
		return false;
	    }
	    return insertCopy(tree,original);
	}
	else {
	    Template  template = config.release().esmoduleTemplate(templateName);
	    return insertInstance(tree,template);
	}
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
    // ESSources *and* ESModules (ESPreferables)
    //
    
    /** set Preferable attribute */
    public static void setPreferred(JTree tree,boolean isPreferred)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	IConfiguration         config   = (IConfiguration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	ESPreferable esp = (ESPreferable)treePath.getLastPathComponent();
	esp.setPreferred(isPreferred);
	//config.setHasChanged(true);
	model.nodeChanged(esp);
	model.updateLevel1Nodes();
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

    /** remove all unreferenced sequences */
    public static void removeUnreferencedSequences(JTree tree)
    {
	ConfigurationTreeModel model =(ConfigurationTreeModel)tree.getModel();
	Configuration          config=(Configuration)model.getRoot();
	Object                 parent=model.sequencesNode();
	
	ArrayList<Sequence> toBeRemoved = new ArrayList<Sequence>();

	Iterator<Sequence> itSeq = config.sequenceIterator();
	while (itSeq.hasNext()) {
	    Sequence sequence = itSeq.next();
	    if (sequence.parentPaths().length==0) toBeRemoved.add(sequence);
	}
	
	Iterator<Sequence> itRmv = toBeRemoved.iterator();
	while (itRmv.hasNext()) {
	    Sequence sequence = itRmv.next();
	    int      index = config.indexOfSequence(sequence);
	    config.removeSequence(sequence);
	    model.nodeRemoved(parent,index,sequence);
	}
	model.nodeStructureChanged(model.modulesNode());
	model.updateLevel1Nodes();
    }

    /** resolve unnecessary sequences (those referenced only once) */
    public static void resolveUnnecessarySequences(JTree tree)
    {
	ConfigurationTreeModel model =(ConfigurationTreeModel)tree.getModel();
	Configuration          config=(Configuration)model.getRoot();
	
	ArrayList<Sequence> sequences = new ArrayList<Sequence>();
	Iterator<Sequence> itS = config.sequenceIterator();
	while (itS.hasNext()) {
	    Sequence sequence = itS.next();
	    if (sequence.referenceCount()==1) sequences.add(sequence);
	}
	
	itS = sequences.iterator();
	while (itS.hasNext()) {
	    Sequence           sequence  = itS.next();
	    Reference          reference = sequence.reference(0);
	    ReferenceContainer container = reference.container();
	    int                index     = container.indexOfEntry(reference);
	    
	    Referencable[] instances = new Referencable[sequence.entryCount()];
	    for (int i=0;i<sequence.entryCount();i++)
		instances[i] = sequence.entry(i).parent();
	    
	    config.removeSequence(sequence);
	    
	    for (int i=0;i<instances.length;i++) {
		if (instances[i] instanceof ModuleInstance) {
		    ModuleInstance module = (ModuleInstance)instances[i];
		    config.insertModule(config.moduleCount(),module);
		    config.insertModuleReference(container,index+i,module);
		}
		else if (instances[i] instanceof Sequence) {
		    Sequence seq = (Sequence)instances[i];
		    config.insertSequenceReference(container,index+i,seq);
		}
		else if (instances[i] instanceof Path) {
		    Path path = (Path)instances[i];
		    config.insertPathReference(container,index+i,path);
		}
	    }
	}
	
	model.nodeStructureChanged(model.getRoot());
	model.updateLevel1Nodes();
    }

    /** set a path as endpath */
    public static void setPathAsEndpath(JTree tree,boolean isEndPath)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	IConfiguration         config   = (IConfiguration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();

	Path path = (Path)treePath.getLastPathComponent();
	path.setAsEndPath(isEndPath);
	//config.setHasChanged(true);
	model.nodeChanged(path);
    }

    /** insert a new path */
    public static boolean insertPath(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
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
    
    /** move an existing path within the list of paths */
    public static boolean movePath(JTree tree,Path sourcePath)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	int sourceIndex = config.indexOfPath(sourcePath);
	int targetIndex = (treePath.getPathCount()==2) ?
	    0:model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				    treePath.getLastPathComponent())+1;
	
	config.movePath(sourcePath,targetIndex);
	model.nodeRemoved(model.pathsNode(),sourceIndex,sourcePath);
	if (sourceIndex<targetIndex) targetIndex--;
	model.nodeInserted(model.pathsNode(),targetIndex);
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
    
    /** move an existing sequence within the list of sequences */
    public static boolean moveSequence(JTree tree,Sequence sourceSequence)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	int sourceIndex = config.indexOfSequence(sourceSequence);
	int targetIndex = (treePath.getPathCount()==2) ?
	    0:model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				    treePath.getLastPathComponent())+1;
	
	config.moveSequence(sourceSequence,targetIndex);
	model.nodeRemoved(model.sequencesNode(),sourceIndex,sourceSequence);
	if (sourceIndex<targetIndex) targetIndex--;
	model.nodeInserted(model.sequencesNode(),targetIndex);
	return true;
    }

    /** import Path / Sequence */
    public static boolean importReferenceContainer(JTree tree,
						   ReferenceContainer external)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	int count =
	    (external instanceof Path) ? config.pathCount():config.sequenceCount();

	int index = (treePath==null) ? count :
	    (treePath.getPathCount()==2) ?
	    0:model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				    treePath.getLastPathComponent())+1;
	
	ReferenceContainer   container = null;
	Object               parent    = null;
	String               type      = null;

	if (external instanceof Path) {
	    container = config.path(external.name());
	    parent    = model.pathsNode();
	    type      = "path";
	}
	else if (external instanceof Sequence) {
	    container = config.sequence(external.name());
	    parent    = model.sequencesNode();
	    type      = "sequence";
	}
	
	boolean update = false;
	if (container!=null) {
	
	    index = (type.equals("path")) ? config.indexOfPath((Path)container) 
		                          : config.indexOfSequence((Sequence)container);
    
	    int choice =
		JOptionPane.showConfirmDialog(null,"The "+type+" '"+
					      container.name()+"' exists, "+
					      "do you want to overwrite it?",
					      "Overwrite "+type,
					      JOptionPane.OK_CANCEL_OPTION);
	    if (choice==JOptionPane.CANCEL_OPTION) return false;
	    
	    update = true;
	    
	    while (container.entryCount()>0) {
		Reference entry = (Reference)container.entry(0);
		tree.setSelectionPath(new TreePath(model.getPathToRoot(entry)));
		removeReference(tree);
	    }
	}
	else {
	    if (!config.hasUniqueQualifier(external)) return false;
	    container = (type.equals("path")) ?
		config.insertPath(index,external.name()) :
		config.insertSequence(index,external.name());
	}
	
	if (importContainerEntries(config,model,external,container))
	    container.setDatabaseId(external.databaseId());
	
	if (update) model.nodeChanged(container);
	else	    model.nodeInserted(parent,index);
	model.updateLevel1Nodes();
	
	Diff diff = new Diff(external.config(),config);
	String search = type+":"+container.name();
	diff.compare(search);
	if (!diff.isIdentical()) {
	    DiffDialog dlg = new DiffDialog(diff);
	    dlg.pack();
	    dlg.setVisible(true);
	}
	
	// PS 31/01/2011: fixes bug reported by Andrea B.
	for (int i=0;i<container.referenceCount();i++) {
	    Reference reference = container.reference(i);
	    ReferenceContainer parentContainer = reference.container();
	    parentContainer.setHasChanged();
	}
	
	return true;
    }

    /** insert entries of an external reference container into the local copy */
    private static
	boolean importContainerEntries(Configuration          config,
				       ConfigurationTreeModel treeModel,
				       ReferenceContainer     sourceContainer,
				       ReferenceContainer     targetContainer)
    {
	// result=true: import all daugthers unchangend
	boolean result = true;
	for (int i=0;i<sourceContainer.entryCount();i++) {
	    Reference entry = sourceContainer.entry(i);
	    
	    if (entry instanceof ModuleReference) {
		ModuleReference sourceRef = (ModuleReference)entry;
		ModuleInstance  source    = (ModuleInstance)sourceRef.parent();
		ModuleInstance  target    = config.module(source.name());
		if (target!=null) {
		    config.insertModuleReference(targetContainer,i,target);
		    result = false;
		}
		else {
		    ModuleReference targetRef =
			config.insertModuleReference(targetContainer,i,
						     source.template().name(),
						     source.name());
		    target = (ModuleInstance)targetRef.parent();
		    for (int j=0;j<target.parameterCount();j++)
			target.updateParameter(j,source.parameter(j)
					       .valueAsString());
		    target.setDatabaseId(source.databaseId());
		}

		treeModel.nodeInserted(targetContainer,i);
		if (target.referenceCount()==1)
		    treeModel.nodeInserted(treeModel.modulesNode(),
					   config.moduleCount()-1);
	    }
	    else if (entry instanceof PathReference) {
		PathReference sourceRef=(PathReference)entry;
		Path          source   =(Path)sourceRef.parent();
		Path          target   =config.path(source.name());
		if (target!=null) {
		    config.insertPathReference(targetContainer,i,target);
		    result = false;
		}
		else {
		    target = config.insertPath(config.pathCount(),sourceRef.name());
		    treeModel.nodeInserted(treeModel.pathsNode(),
					   config.pathCount()-1);
		    config.insertPathReference(targetContainer,i,target);
		    boolean tmp =
			importContainerEntries(config,treeModel,source,target);
		    if (tmp) target.setDatabaseId(source.databaseId());
		    if (result) result = tmp;
		}
		
		treeModel.nodeInserted(targetContainer,i);
	    }
	    else if (entry instanceof SequenceReference) {
		SequenceReference sourceRef=(SequenceReference)entry;
		Sequence          source=(Sequence)sourceRef.parent();
		Sequence          target=config.sequence(sourceRef.name());
		if (target!=null) {
		    config.insertSequenceReference(targetContainer,i,target);
		    result = false;
		}
		else {
		    target = config.insertSequence(config.sequenceCount(),
						   sourceRef.name());
		    treeModel.nodeInserted(treeModel.sequencesNode(),
					   config.sequenceCount()-1);
		    config.insertSequenceReference(targetContainer,i,target);
		    boolean tmp =
			importContainerEntries(config,treeModel,source,target);
		    if (tmp) target.setDatabaseId(source.databaseId());
		    if (result) result = tmp;
		}
		
		treeModel.nodeInserted(targetContainer,i);
	    }
	}
	return result;
    }
        
    /** insert reference into currently selected reference container */
    public static boolean insertReference(JTree tree,String type,String name)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
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
	
	if (type.equalsIgnoreCase("Path")) {
	    Path referencedPath = config.path(name);
	    if (referencedPath==null) return false;
	    reference =
		config.insertPathReference(parent,index,referencedPath);
	}
	else if (type.equalsIgnoreCase("Sequence")) {
	    Sequence referencedSequence = config.sequence(name);
	    if (referencedSequence==null) return false;
	    reference =
		config.insertSequenceReference(parent,index,referencedSequence);
	}
	else if (type.equalsIgnoreCase("OutputModule")) {
	    OutputModule referencedOutput = config.output(name);
	    if (referencedOutput==null) return false;
	    reference =
		config.insertOutputModuleReference(parent,index,
						   referencedOutput);
	}
	else if (type.equalsIgnoreCase("Module")) {
	    String[] s = name.split(":");
	    String   templateName="";
	    String   instanceName="";
	    boolean  copy = false;
	    
	    if (s.length==1) {
		templateName = s[0];
	    }
	    else if (s.length==2) {
		templateName = s[0];
		instanceName = s[1];
	    }
	    else {
		copy = true;
		templateName = s[1];
		instanceName = s[2];
	    }
	    
	    ModuleTemplate template
		= config.release().moduleTemplate(templateName);

	    if (!copy) {
		if (template.hasInstance(instanceName)) {
		    try {
			module =
			    (ModuleInstance)template.instance(instanceName);
		    }
		    catch (DataException e) {
			System.err.println(e.getMessage());
			return false;
		    }
		    reference =
			config.insertModuleReference(parent,index,module);
		}
		else  {
		    instanceName = templateName; int count=2;
		    while (template.hasInstance(instanceName)) {
			instanceName = templateName + count; ++count;
		    }
		    reference = config.insertModuleReference(parent,index,
							     templateName,
							     instanceName);
		    module = (ModuleInstance)reference.parent();
		}
	    }
	    else {
		ModuleInstance original = null;
		try {
		    original = (ModuleInstance)template.instance(instanceName);
		}
		catch (DataException e) {
		    System.err.println(e.getMessage());
		    return false;
		}
		instanceName = "copy_of_" + instanceName;
		reference = config.insertModuleReference(parent,index,
							 templateName,
							 instanceName);
		module = (ModuleInstance)reference.parent();
		Iterator<Parameter> itP = original.parameterIterator();
		while (itP.hasNext()) {
		    Parameter p = itP.next();
		    module.updateParameter(p.name(),p.type(),p.valueAsString());
		}
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
    
    /** move a reference within its container */
    public static boolean moveReference(JTree tree,Reference sourceReference)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	Object target = treePath.getLastPathComponent();
	
	ReferenceContainer container = (target instanceof ReferenceContainer) ?
	    (ReferenceContainer)target : ((Reference)target).container();
	
	if (sourceReference.container()!=container) return false;
	
	int sourceIndex = container.indexOfEntry(sourceReference);
	int targetIndex = (treePath.getPathCount()==3) ?
	    0:model.getIndexOfChild(treePath.getParentPath()
				    .getLastPathComponent(),
				    treePath.getLastPathComponent())+1;
	
	container.moveEntry(sourceReference,targetIndex);
	model.nodeRemoved(container,sourceIndex,sourceReference);
	if (sourceIndex<targetIndex) targetIndex--;
	model.nodeInserted(container,targetIndex);
	//config.setHasChanged(true);
	return true;
    }
    
    /** remove a reference container */
    public static boolean removeReferenceContainer(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	ReferenceContainer     container=
	    (ReferenceContainer)treePath.getLastPathComponent();
	
	ArrayList<Integer> unreferencedIndices = new ArrayList<Integer>();
	for (int i=0;i<container.entryCount();i++) {
	    Reference entry = container.entry(i);
	    if (entry instanceof ModuleReference) {
		ModuleReference reference = (ModuleReference)entry;
		ModuleInstance  instance  = (ModuleInstance)reference.parent();
		if (instance.referenceCount()==1)
		    unreferencedIndices.add(config.indexOfModule(instance));
	    }
	}
	
	int    childIndices[] = null;
	Object children[]     = null;
	if (unreferencedIndices.size()>0) {
	    childIndices = new int[unreferencedIndices.size()];
	    children     = new Object[unreferencedIndices.size()];
	    for (int i=0;i<unreferencedIndices.size();i++) {
		int moduleIndex = unreferencedIndices.get(i).intValue();
		childIndices[i] = moduleIndex;
		children[i]     = config.module(moduleIndex);
	    }
	}
	
	int    index  = -1;
	Object parent = null;
	if (container instanceof Path) {
	    Path path = (Path)container;
	    index  = config.indexOfPath(path);
	    parent = model.pathsNode();

	    if (model.contentMode().equals("paths")) {
		Iterator<EventContent> itC = path.contentIterator();
		while (itC.hasNext()) {
		    EventContent content = itC.next();
		    model.nodeRemoved(content,content.indexOfPath(path),path);
		}
	    }
	    
	    if (model.streamMode().equals("paths")) {
		Iterator<Stream> itS = path.streamIterator();
		while (itS.hasNext()) {
		    Stream stream = itS.next();
		    model.nodeRemoved(stream,stream.indexOfPath(path),path);
		}
	    }

	    Iterator<PrimaryDataset> itD = path.datasetIterator();
	    while (itD.hasNext()) {
		PrimaryDataset dataset = itD.next();
		model.nodeRemoved(dataset,dataset.indexOfPath(path),path);
	    }
	    config.removePath(path);
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
	model.nodeStructureChanged(model.outputsNode());
	
	TreePath parentTreePath = treePath.getParentPath();
	if (index==0)
	    tree.setSelectionPath(parentTreePath);
	else
	    tree.setSelectionPath(parentTreePath
				  .pathByAddingChild(model
						     .getChild(parent,
							       index-1)));
	
	return true;
    }
    
    /** remove reference from currently selected reference container */
    public static boolean removeReference(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config    = (Configuration)model.getRoot();
	TreePath               treePath  = tree.getSelectionPath();
	Reference       reference = (Reference)treePath.getLastPathComponent();
	ReferenceContainer     container = reference.container();
	int                    index     = container.indexOfEntry(reference);
	ModuleInstance         module    = null;
	int                    indexOfModule= -1;
	
	if (reference instanceof ModuleReference) {
	    module = (ModuleInstance)reference.parent();
	    indexOfModule = config.indexOfModule(module);
	    config.removeModuleReference((ModuleReference)reference);
	}
	else if (reference instanceof OutputModuleReference) {
	    OutputModuleReference omr = (OutputModuleReference)reference;
	    config.removeOutputModuleReference(omr);
	}
	else {
	    container.removeEntry(reference);
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
				  .pathByAddingChild(model.getChild(parent,
								    index-1)));

	return true;
    }

    /** set operator  */
    public static boolean setOperator( JTree tree, String newOperator )
    {
    	ConfigurationTreeModel 	model  = (ConfigurationTreeModel)tree.getModel();
    	Configuration          	config    = (Configuration)model.getRoot();
    	TreePath               	treePath  = tree.getSelectionPath();
    	Reference       		reference = (Reference)treePath.getLastPathComponent();
    	Operator op = Operator.valueOf( newOperator );
    	reference.setOperator( op );
    	model.nodeChanged( reference );
    	return true;
    }

	
	/** scroll to the instance of the currently selected reference */
    public static void scrollToInstance(JTree tree)
    {
	ConfigurationTreeModel model   =(ConfigurationTreeModel)tree.getModel();
	Configuration          config  =(Configuration)model.getRoot();
	TreePath               treePath=tree.getSelectionPath();

	Reference    reference = (Reference)treePath.getLastPathComponent();
	Referencable instance  = reference.parent();
	
	TreePath instanceTreePath = new TreePath(model.getPathToRoot(instance));
	tree.setSelectionPath(instanceTreePath);
	tree.expandPath(instanceTreePath);
	tree.scrollPathToVisible(instanceTreePath);

    }

    /** import a single module into path or sequence */
    public static boolean importModule(JTree tree,ModuleInstance external)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	ReferenceContainer parent        = null;
	ModuleInstance     target        = config.module(external.name());
	int                insertAtIndex = 0;
	
	if (target!=null) {
	    int choice =
		JOptionPane.showConfirmDialog(null,"The module '"+
					      target.name()+"' exists, "+
					      "do you want to overwrite it?",
					      "Overwrite module",
					      JOptionPane.OK_CANCEL_OPTION);
	    if (choice==JOptionPane.CANCEL_OPTION) return false;
	    else return replaceModule(tree,external);
	}
	else if (treePath==null) return false;
	
	Object targetNode=treePath.getLastPathComponent();
	
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
    // EventContents
    //

    /** insert a new event content */
    public static boolean insertContent(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	EventContent content = config.insertContent("<ENTER EVENTCONTENT LABEL>");
	
	int index = config.indexOfContent(content);
	model.nodeInserted(model.contentsNode(),index);
	model.updateLevel1Nodes();
	
	TreePath parentPath = (index==0) ? treePath : treePath.getParentPath();
	TreePath newTreePath = parentPath.pathByAddingChild(content);

	tree.setSelectionPath(newTreePath);
	editNodeName(tree);

	return true;
    }
    
    /** import event content */
    public static boolean importContent(JTree tree, EventContent external)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	EventContent content = config.content(external.name());
	if (content==null)  content = config.insertContent(external.name());
	model.nodeInserted(model.contentsNode(),config.indexOfContent(content));
	
	Iterator<Stream> itS = external.streamIterator();
	while (itS.hasNext()) {
	    Stream stream = itS.next();
	    importStream(tree,content.name(),stream);
	}
	
	Iterator<OutputCommand> itOC = external.commandIterator();
	while (itOC.hasNext()) {
	    OutputCommand command = itOC.next();
	    content.insertCommand(command);
	}

	itS = content.streamIterator();
	while (itS.hasNext()) {
	    OutputModule output = itS.next().outputModule();
	    PSetParameter psetSelectEvents =
		(PSetParameter)output.parameter(0);
	    model.nodeChanged(psetSelectEvents.parameter(0));
	    if (output.referenceCount()>0)
		model.nodeStructureChanged(output.reference(0));
	}
	
	model.updateLevel1Nodes();
	
	return true;
    }
					
    
    /** remove an existing event content */
    public static boolean removeContent(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	EventContent content = (EventContent)treePath.getLastPathComponent();
	int          index   = config.indexOfContent(content);
	
	int streamCount = 0;
	int datasetCount = 0;
	Iterator<Stream> itS = content.streamIterator();
	while (itS.hasNext()) {
	    Stream stream = itS.next();
	    Iterator<PrimaryDataset> itPD = stream.datasetIterator();
	    while (itPD.hasNext()) {
		PrimaryDataset dataset = itPD.next();
		model.nodeRemoved(model.datasetsNode(),
				  config.indexOfDataset(dataset)-datasetCount,
				  dataset);
		datasetCount++;
	    }
	    stream.removeOutputModuleReferences();
	    OutputModule output = stream.outputModule();
	    model.nodeRemoved(model.streamsNode(),
			      config.indexOfStream(stream)-streamCount,
			      stream);
	    model.nodeRemoved(model.outputsNode(),
			      config.indexOfOutput(output)-streamCount,
			      output);
	    streamCount++;
	}
	
	config.removeContent(content);
	model.nodeRemoved(model.contentsNode(),index,content);
	model.updateLevel1Nodes();
	
	return true;
    }


    //
    // Streams
    //
    
    /** insert a new stream */
    public static boolean insertStream(JTree tree,Stream stream)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	int index = config.indexOfStream(stream);
	model.nodeInserted(model.streamsNode(),index);
	model.nodeInserted(stream.parentContent(),
			   stream.parentContent().indexOfStream(stream));
	model.nodeInserted(model.outputsNode(),
			   config.indexOfOutput(stream.outputModule()));
	model.updateLevel1Nodes();
	
	
	TreePath newTreePath = treePath.pathByAddingChild(stream);
	tree.setSelectionPath(newTreePath);
	
	return true;
    }
    
    /** import stream */
    public static boolean importStream(JTree tree,String contentName,Stream external)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();

	EventContent content = null;
	
	if (contentName.equals("")) {
	   Object targetNode=treePath.getLastPathComponent();
	   if (targetNode instanceof EventContent)
	       content = (EventContent)targetNode;
	   else return false;
	}
	else {
	    content = config.content(contentName);
	}
	
	if (content==null) {
	    System.err.println("stream must be added to existing event content!");
	    return false;
	}
	
	Stream stream = content.stream(external.name());
	if (stream==null) {
	    stream = content.insertStream(external.name());
	    stream.setFractionToDisk(external.fractionToDisk());
	}
	
	Iterator<Path> itP = external.pathIterator();
	while (itP.hasNext()) {
	    String pathName = itP.next().name();
	    Path path = config.path(pathName);
	    if (path==null) {
		System.out.println("importStream: skip path "+pathName);
		continue;
	    }
	    stream.insertPath(path);
	}
	
	Iterator<PrimaryDataset> itPD = external.datasetIterator();
	while (itPD.hasNext()) {
	    PrimaryDataset dataset = itPD.next();
	    importPrimaryDataset(tree,stream.name(),dataset);
	}
	
	model.nodeInserted(model.streamsNode(),config.indexOfStream(stream));
	model.updateLevel1Nodes();
	
	return true;
    }
    
    /** remove an existing stream */
    public static boolean removeStream(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();

	Stream       stream       = (Stream)treePath.getLastPathComponent();
	OutputModule output       = stream.outputModule();
	EventContent content      = stream.parentContent();
	int          index        = config.indexOfStream(stream);
	int          indexOutput  = config.indexOfOutput(output);
	int          indexContent = content.indexOfStream(stream);


	// remove dataset nodes
	int datasetCount = 0;
	Iterator<PrimaryDataset> itPD = stream.datasetIterator();
	while (itPD.hasNext()) {
	    PrimaryDataset dataset = itPD.next();
	    model.nodeRemoved(model.datasetsNode(),
			      config.indexOfDataset(dataset)-datasetCount,
			      dataset);
	    datasetCount++;
	}
	
	content.removeStream(stream);
	model.nodeRemoved(model.streamsNode(),index,stream);
	model.nodeRemoved(model.outputsNode(),index,output);
	if (model.contentMode().equals("streams"))
	    model.nodeRemoved(content,indexContent,stream);
	model.nodeStructureChanged(model.pathsNode());
	model.nodeStructureChanged(model.sequencesNode());
	model.updateLevel1Nodes();
	
	return true;
    }
    
    /** add a path to an existing stream */
    public static boolean addPathToStream(JTree tree,String pathName)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();

	Stream       stream  = (Stream)treePath.getLastPathComponent();
	EventContent content = stream.parentContent();
	Path         path    = config.path(pathName);
	
	stream.insertPath(path);


	if (model.contentMode().equals("paths"))
	    model.nodeInserted(content,content.indexOfPath(path));

	if (model.streamMode().equals("paths"))
	    model.nodeInserted(stream,stream.indexOfPath(path));

	Iterator<Stream> itS = content.streamIterator();
	while (itS.hasNext()) {
	    OutputModule output = itS.next().outputModule();
	    PSetParameter psetSelectEvents =
		(PSetParameter)output.parameter(0);
	    model.nodeChanged(psetSelectEvents.parameter(0));
	    if (output.referenceCount()>0)
		model.nodeStructureChanged(output.reference(0));
	}
	
	model.updateLevel1Nodes();
	
	return true;
    }
    
    /** remove a path from a stream */
    public static boolean removePathFromStream(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	ConfigurationTreeNode treeNode =
	    (ConfigurationTreeNode)treePath.getLastPathComponent();
	Stream stream = (Stream)treeNode.parent();
	Path   path   = (Path)treeNode.object();
	int    index  = stream.indexOfPath(path);

	EventContent content = stream.parentContent();
	int contentIndex = content.indexOfPath(path);
	
	PrimaryDataset dataset = stream.dataset(path);
	if (dataset!=null)
	    model.nodeRemoved(dataset,dataset.indexOfPath(path),path);
	
	stream.removePath(path);

	if (model.contentMode().equals("paths")&&content.indexOfPath(path)<0) {
	    model.nodeRemoved(content,content.indexOfPath(path),path);
	}
	
	Iterator<Stream> itS = content.streamIterator();
	while (itS.hasNext()) {
	    OutputModule output = itS.next().outputModule();
	    PSetParameter psetSelectEvents =
		(PSetParameter)output.parameter(0);
	    model.nodeChanged(psetSelectEvents.parameter(0));
	    if (output.referenceCount()>0)
		model.nodeStructureChanged(output.reference(0));
	}
	
	model.nodeRemoved(stream,index,treeNode);
	model.updateLevel1Nodes();
	
	return true;
    }


    //
    // PrimaryDatasets
    //
    /** insert a newly created primary dataset */
    public static boolean insertPrimaryDataset(JTree tree,
					       PrimaryDataset dataset)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	Object                 node     = treePath.getLastPathComponent();
	
	int index = config.indexOfDataset(dataset);
	model.nodeInserted(model.datasetsNode(),index);
	if (model.streamMode().equals("datasets"))
	    model.nodeInserted(dataset.parentStream(),
			       dataset.parentStream().indexOfDataset(dataset));
	model.nodeStructureChanged(model.contentsNode());
	model.updateLevel1Nodes();
	
	if (node == model.datasetsNode()) {
	    TreePath newTreePath = treePath.pathByAddingChild(dataset);
	    tree.setSelectionPath(newTreePath);
	}
	
	return true;
    }
    
    /** import primary dataset */
    public static boolean importPrimaryDataset(JTree tree,
					       String streamName,
					       PrimaryDataset external)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	Stream stream = null;
	
	if (streamName.equals("")) {
	    Object targetNode=treePath.getLastPathComponent();
	    if (targetNode instanceof Stream)
		stream = (Stream)targetNode;
	    else return false;
	}
	else {
	    stream = config.stream(streamName);
	}
	
	if (stream==null) {
	    System.err.println("dataset must be added to existing stream!");
	    return false;
	}
	
	PrimaryDataset dataset = stream.insertDataset(external.name());
	if (dataset==null) {
	    if (config.dataset(external.name())!=null) return false; // TODO?
	    dataset = stream.dataset(external.name());
	}
	
	Iterator<Path> itP = external.pathIterator();
	while (itP.hasNext()) {
	    String pathName = itP.next().name();
	    Path path = config.path(pathName);
	    if (path==null) {
		System.out.println("importPrimaryDataset: skip path " + pathName);
		continue;
	    }
	    dataset.insertPath(path);
	}
	
	model.nodeInserted(model.datasetsNode(),config.indexOfDataset(dataset));
	
	return true;
    }

    /** remove an existing primary dataset */
    public static boolean removePrimaryDataset(JTree tree)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	Object                 node     = treePath.getLastPathComponent();
	PrimaryDataset dataset = null;
	Stream         stream  = null;

	if (node instanceof PrimaryDataset) {
	    dataset = (PrimaryDataset)node;
	    stream  = dataset.parentStream();
	}
	else if (node instanceof ConfigurationTreeNode) {
	    ConfigurationTreeNode treeNode = (ConfigurationTreeNode)node;
	    dataset = (PrimaryDataset)treeNode.object();
	    stream  = (Stream)treeNode.parent();
	    tree.setSelectionPath(treePath.getParentPath());
	}

	int index       = config.indexOfDataset(dataset);
	int indexStream = stream.indexOfDataset(dataset);

	stream.removeDataset(dataset);
	model.nodeRemoved(model.datasetsNode(),index,dataset);
	model.nodeRemoved(stream,indexStream,dataset);
	model.nodeStructureChanged(model.contentsNode());
	Iterator<Path> itP = dataset.pathIterator();
	while (itP.hasNext())
	    model.nodeInserted(model.getChild(stream,stream.datasetCount()),
			       stream.listOfUnassignedPaths().indexOf(itP.next()));
	model.updateLevel1Nodes();
	
	return true;
    }
    
    /** add a path to a primary dataset */
    public static boolean addPathToDataset(JTree tree,String name)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	Object                 node     = treePath.getLastPathComponent();
	
	PrimaryDataset dataset = null;
	Stream         stream  = null;
	Path           path    = null;
	
	if (node instanceof PrimaryDataset) {
	    dataset = (PrimaryDataset)node;
	    stream  = dataset.parentStream();
	    path    = config.path(name);
	}
	else if (node instanceof ConfigurationTreeNode) {
	    ConfigurationTreeNode treeNode = (ConfigurationTreeNode)node;
	    ConfigurationTreeNode parentNode = (ConfigurationTreeNode)treeNode.parent();
	    path    = (Path)treeNode.object();
	    stream  = (Stream)parentNode.parent();
	    dataset = stream.dataset(name);
	    tree.setSelectionPath(treePath.getParentPath());
	}
	
	int index = -1;
	if (stream.indexOfPath(path)<0) stream.insertPath(path);
	else index = stream.listOfUnassignedPaths().indexOf(path);
	dataset.insertPath(path);
	
	model.nodeInserted(dataset,dataset.indexOfPath(path));
	if (model.streamMode().equals("datasets")) {
	    model.nodeInserted(model.getChild(stream,stream.indexOfDataset(dataset)),
			       dataset.indexOfPath(path));
	    model.nodeRemoved(model.getChild(stream,stream.datasetCount()),index,path);
	}
	model.nodeChanged(path);
	model.updateLevel1Nodes();
	
	return true;
    }
    
    /** remove a path from its parent dataset */
    public static boolean removePathFromDataset(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	ConfigurationTreeNode treeNode =
	    (ConfigurationTreeNode)treePath.getLastPathComponent();
	PrimaryDataset dataset = (PrimaryDataset)treeNode.parent();
	Stream         stream  = dataset.parentStream();
	Path           path    = (Path)treeNode.object();
	int            index   = dataset.indexOfPath(path);
	
	dataset.removePath(path);
	
	model.nodeRemoved(dataset,index,treeNode);
	if (model.streamMode().equals("datasets"))
	    model.nodeRemoved(model.getChild(stream,stream.indexOfDataset(dataset)),
			      index,treeNode);
	model.nodeInserted(model.getChild(stream,stream.datasetCount()),
			   stream.listOfUnassignedPaths().indexOf(path));
	model.updateLevel1Nodes();
	
	return true;
    }

    /** move a path from one dataset to another within the same stream */
    public static boolean movePathToDataset(JTree tree,
					    String targetDatasetName)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	ConfigurationTreeNode treeNode =
	    (ConfigurationTreeNode)treePath.getLastPathComponent();
	PrimaryDataset sourceDataset = (PrimaryDataset)treeNode.parent();
	Stream         parentStream  = sourceDataset.parentStream();
	PrimaryDataset targetDataset = parentStream.dataset(targetDatasetName);
	if (targetDataset==null) {
	    System.err.println("ConfigurationTreeActions.movePathToDataset ERROR: "+
			       targetDatasetName+" dataset not found in stream "+
			       parentStream);
	}

	Path path        = (Path)treeNode.object();
	int  sourceIndex = sourceDataset.indexOfPath(path);
	sourceDataset.removePath(path);
	targetDataset.insertPath(path);
	int targetIndex = targetDataset.indexOfPath(path);
	
	model.nodeRemoved(sourceDataset,sourceIndex,treeNode);
	model.nodeInserted(targetDataset,targetIndex);
	model.updateLevel1Nodes();

	return true;
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

	int index = (treePath.getPathCount()==2) ?
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

    /** insert copy of an existing instance to configuration and tree */
    private static boolean insertCopy(JTree tree,Instance original)
    {
	if (!(original instanceof ESSourceInstance)&&
	    !(original instanceof ESModuleInstance)) return false;
	
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	Template template     = original.template();
	String   templateName = template.name();
	String   instanceName = "copy_of_" + original.name();
	int      count        = 2;
	while (template.hasInstance(instanceName)) {
	    instanceName = templateName + count;
	    count++;
	}

	int index = (treePath.getPathCount()==2) ?
	    0:model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
				    treePath.getLastPathComponent())+1;
	
	Instance instance = null;
	Object   parent   = null;
	
	if (template instanceof ESSourceTemplate) {
	    instance = config.insertESSource(index,templateName,instanceName);
	    parent   = model.essourcesNode();
	}
	else if (template instanceof ESModuleTemplate) {
	    instance = config.insertESModule(index,templateName,instanceName);
	    parent   = model.esmodulesNode();
	}
	else return false;
	
	Iterator<Parameter> itP = original.parameterIterator();
	while (itP.hasNext()) {
	    Parameter p = itP.next();
	    instance.updateParameter(p.name(),p.type(),p.valueAsString());
	}

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
     * replace a module with the external one
     */
    private static boolean replaceModule(JTree tree,ModuleInstance external)
    {
	ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
	Configuration          config    = (Configuration)model.getRoot();
	ModuleInstance         oldModule = config.module(external.name());
	if (oldModule==null) return false;

	int index    = config.indexOfModule(oldModule);
	int refCount = oldModule.referenceCount();
	ReferenceContainer[] parents = new ReferenceContainer[refCount];
	int[]                indices = new int[refCount];
	int iRefCount=0;
	while (oldModule.referenceCount()>0) {
	    Reference reference = oldModule.reference(0);
	    parents[iRefCount] = reference.container();
	    indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
	    config.removeModuleReference((ModuleReference)reference);
	    model.nodeRemoved(parents[iRefCount],indices[iRefCount],reference);
	    iRefCount++;
	}
	
	model.nodeRemoved(model.modulesNode(),index,oldModule);
	
	try {
	    ModuleTemplate template = (ModuleTemplate)
		config.release().moduleTemplate(external.template().name());
	    ModuleInstance newModule = (ModuleInstance)
		template.instance(external.name());
	    for (int i=0;i<newModule.parameterCount();i++)
		newModule.updateParameter(i,external.parameter(i).valueAsString());
	    newModule.setDatabaseId(external.databaseId());
	    config.insertModule(index,newModule);
	    model.nodeInserted(model.modulesNode(),index);
	    
	    for (int i=0;i<refCount;i++) {
		config.insertModuleReference(parents[i],indices[i],newModule);
		model.nodeInserted(parents[i],indices[i]);
	    }
	    model.updateLevel1Nodes();
	    tree.expandPath(new TreePath(model.getPathToRoot(newModule)));
	}
	catch (DataException e) {
	    System.err.println("replaceModule() FAILED: " + e.getMessage());
	    return false;
	}
	return true;
    }


    /**
     * import a node into the tree and add the respective component
     * to the configuration
     */
    public static boolean importInstance(JTree tree,Instance external)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	if ((external instanceof EDSourceInstance)&&
	    config.edsource(external.name())!=null||
	    (external instanceof ESSourceInstance)&&
	    config.essource(external.name())!=null||
	    (external instanceof ESModuleInstance)&&
	    config.esmodule(external.name())!=null||
	    (external instanceof ServiceInstance)&&
	    config.service(external.name())!=null) {
	    return replaceInstance(tree,external);
	}
	
	if (!config.isUniqueQualifier(external.name())) return false;

	int index = (treePath==null) ? 0 : (treePath.getPathCount()==2) ?
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

    /*
     * replace an existing instance with the external one
     */
    private static boolean replaceInstance(JTree tree,Instance external)
    {
	ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
	Configuration          config    = (Configuration)model.getRoot();
	Object                 parent    = null;
	Instance               oldInst   = null;
	Instance               newInst   = null;
	int                    index     = -1;

	if (external instanceof EDSourceInstance) {
	    parent  = model.edsourcesNode();
	    oldInst = config.edsource(external.name());
	    index   = 0;
	    config.removeEDSource((EDSourceInstance)oldInst);
	    model.nodeRemoved(parent,index,oldInst);
	    newInst = config.insertEDSource(external.template().name());
	}
	else if (external instanceof ESSourceInstance) {
	    parent  = model.essourcesNode();
	    oldInst = config.essource(external.name());
	    index   = config.indexOfESSource((ESSourceInstance)oldInst);
	    config.removeESSource((ESSourceInstance)oldInst);
	    model.nodeRemoved(parent,index,oldInst);
	    newInst = config.insertESSource(index,
					    external.template().name(),
					    external.name());
	}
	else if (external instanceof ESModuleInstance) {
	    parent  = model.esmodulesNode();
	    oldInst = config.esmodule(external.name());
	    index   = config.indexOfESModule((ESModuleInstance)oldInst);
	    config.removeESModule((ESModuleInstance)oldInst);
	    model.nodeRemoved(parent,index,oldInst);
	    newInst = config.insertESModule(index,
					    external.template().name(),
					    external.name());
	}
	else if (external instanceof ServiceInstance) {
	    parent  = model.servicesNode();
	    oldInst = config.service(external.name());
	    index   = config.indexOfService((ServiceInstance)oldInst);
	    config.removeService((ServiceInstance)oldInst);
	    model.nodeRemoved(parent,index,oldInst);
	    newInst = config.insertService(index,external.template().name());
	}
	
	for (int i=0;i<newInst.parameterCount();i++)
	    newInst.updateParameter(i,external.parameter(i).valueAsString());
	newInst.setDatabaseId(external.databaseId()); // dangerous?

	model.nodeInserted(parent,index);
	model.updateLevel1Nodes();
	tree.expandPath(new TreePath(model.getPathToRoot(newInst)));
	
	return true;
    }

    /**
     * remove a node from the tree and the respective component from
     * the configuration
     */
    private static boolean removeNode(JTree tree,Object node)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration        config   = (Configuration)model.getRoot();
	TreePath             treePath = tree.getSelectionPath().getParentPath();
	
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
				  .pathByAddingChild(model.getChild(parent,
								    index-1)));
	
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


package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.SwingWorker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import confdb.diff.Comparison;
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
    
    /** Import new global pset 
     * To do that first it will look for the parameter.
     * Following the previous schema: If the parameter exist and its index 
     * is not less than zero then it will be replaced.
     * New Pset parameters are inserted.
     * */
    public static boolean importPSet(JTree tree,Object external, PSetParameter pset, boolean update)
    {
		ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
		Configuration          config   = (Configuration)model.getRoot();
		
		if (config.pset(pset.name())!=null) {
			if(update) {
				int    index  =   -1;
				Object parent = null;

			    index = config.indexOfPSet(pset);
			    if (index<0) return false;
			    config.removePSet(pset);
			    parent = model.psetsNode();
				model.nodeRemoved(parent,index,external);
			} else return false;
		}
	
		config.insertPSet(pset);	
		model.nodeInserted(model.psetsNode(),config.psetCount()-1);
		
		return true;
    }
    
    
    /** 
     * Import All Parameter Sets
     * */
        public static boolean ImportAllPSets(JTree tree, JTree sourceTree, Object external)  {
    			ConfigurationTreeModel sm  		= (ConfigurationTreeModel) sourceTree.getModel();
    			ConfigurationTreeModel tm    	= (ConfigurationTreeModel)tree.getModel();
    			Configuration          config   = (Configuration)tm.getRoot();
    			Configuration	importConfig	= (Configuration)sm.getRoot();			
    			
    	    	if(sm.getChildCount(external) == 0) {
    	    		String error = "[confdb.gui.ConfigurationTreeActions.ImportAllPSets] ERROR: Child count == 0";
    	    		System.err.println(error);
    	    		return false;
    	    	}

    	    	// Checks if any item already exist.
    	    	boolean existance = false;
    			for(int i = 0; i < sm.getChildCount(external); i++) {
    				PSetParameter PSet = (PSetParameter) sm.getChild(external, i);
    				if (config.pset(PSet.name())!=null) {
    					existance = true;
    					break;
    				} 
    			}
    	    	
    			boolean updateAll = false;
    			if(existance) {
    		    	int choice = JOptionPane.showConfirmDialog(null			,
    		    			" Some PSets may already exist. "				+
    						"Do you want to overwrite them All?"			,
    								      "Overwrite all"					,
    								      JOptionPane.YES_NO_CANCEL_OPTION	);
    		    	
    		    	if(choice == JOptionPane.CANCEL_OPTION) return false;
    		    	updateAll = (choice == JOptionPane.YES_OPTION);
    			}
    	    	
    			ArrayList<String> items = new ArrayList<String>();
    			
    			for(int i = 0; i < sm.getChildCount(external); i++) {
    				PSetParameter PSet = (PSetParameter) sm.getChild(external, i);
    				importPSet(tree, external, PSet, updateAll);
    				items.add(PSet.name()); // register imported PSet for diff.
    			}
    			tm.updateLevel1Nodes();
    			
    			// Shows differences between configurations.
    	        Diff diff = new Diff(importConfig, config);
    	        diff.compare("PSet", items);
    	    	if (!diff.isIdentical()) {
    	    	    DiffDialog dlg = new DiffDialog(diff);
    	    	    dlg.pack();
    	    	    dlg.setVisible(true);
    	    	}
    	        
    		    return true;
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
	    
	    Operator[]     operators = new Operator[sequence.entryCount()];
	    Referencable[] instances = new Referencable[sequence.entryCount()];
	    for (int i=0;i<sequence.entryCount();i++) {
		operators[i] = sequence.entry(i).getOperator();
		instances[i] = sequence.entry(i).parent();
	    }
	    config.removeSequence(sequence);
	    
	    for (int i=0;i<instances.length;i++) {
		if (instances[i] instanceof ModuleInstance) {
		    ModuleInstance module = (ModuleInstance)instances[i];
		    config.insertModule(config.moduleCount(),module);
		    config.insertModuleReference(container,index+i,module).setOperator(operators[i]);
		}
		else if (instances[i] instanceof Sequence) {
		    Sequence seq = (Sequence)instances[i];
		    config.insertSequenceReference(container,index+i,seq).setOperator(operators[i]);
		}
		else if (instances[i] instanceof Path) {
		    Path path = (Path)instances[i];
		    config.insertPathReference(container,index+i,path).setOperator(operators[i]);
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
    
    /** 
     * insertSequenceNamed
     * -----------------------------------------------------------------
     * Insert a new sequence using the given name passed by parameter 
     * It checks the sequence name existence and tries different 
     * suffixes using underscore + a number from 0 to 9.
     * */
    private static String insertSequenceNamed(JTree tree, String name)
    {
		ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
		Configuration          config   = (Configuration)model.getRoot();
		TreePath               treePath = tree.getSelectionPath();
		
		int index = (treePath.getPathCount()==2) ?
		    0 :model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
					     treePath.getLastPathComponent())+1;
		// To make sure that sequence name doesn't exist:
		String newName = name;
		if(config.sequence(name)!= null) {
			for(int j = 0; j < 10; j++) {
				newName = name + "_" + j;
				if(config.sequence(newName) == null) {
					j = 10;
				}
			}					
		}
		
		Sequence sequence = config.insertSequence(index, newName);
		
		model.nodeInserted(model.sequencesNode(),index);
		model.updateLevel1Nodes();
		TreePath parentPath = (index==0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(sequence);
		tree.setSelectionPath(newTreePath);
		return newName;
    }
    
    /** 
     * insertPathNamed
     * -----------------------------------------------------------------
     * Insert a new Path using the given name passed by parameter 
     * It checks the path name existence and tries different 
     * suffixes using underscore + a number from 0 to 9.
     * */
    private static String insertPathNamed(JTree tree, String name)
    {
		ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
		Configuration          config   = (Configuration)model.getRoot();
		TreePath               treePath = tree.getSelectionPath();
		
		int index = (treePath.getPathCount()==2) ?
		    0 :model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
					     treePath.getLastPathComponent())+1;
		// To make sure that sequence name doesn't exist:
		String newName = name;
		if(config.path(name)!= null) {
			for(int j = 0; j < 10; j++) {
				newName = name + "_" + j;
				if(config.path(newName) == null) {
					j = 10;
				}
			}					
		}
		
		Path path = config.insertPath(index, newName);
		
		model.nodeInserted(model.pathsNode(),index);
		model.updateLevel1Nodes();
		TreePath parentPath = (index==0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(path);
		tree.setSelectionPath(newTreePath);
		return newName;
    }
    
    /**
     * DeepCloneSequence
     * -----------------------------------------------------------------
     * Clone a sequence from source reference container to target reference container.
     * If the target reference container is null, it creates the root sequence using the
     * source sequence name + suffix.
     * This method use recursion to clone the source tree based in the selection path.
     * NOTE: It automatically go across the entries setting the selection path in different
     * levels. Selection Path is restored to current level when recursion ends.
     * */
    public static boolean DeepCloneSequence(JTree tree, ReferenceContainer sourceContainer, ReferenceContainer targetContainer) {
    	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
    	Configuration          config   = (Configuration)model.getRoot();
    	TreePath               treePath = tree.getSelectionPath();
    	String targetName = sourceContainer.name() + "_clone";
    	
    	if(targetContainer == null) {
    		targetName = ConfigurationTreeActions.insertSequenceNamed(tree, targetName); // It has created the targetContainer
    		targetContainer = config.sequence(targetName);
        	if(targetContainer == null) {
        		System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneSequence] ERROR: targetSequence == NULL");
        		return false;
        	} 
    	}
    	else targetName = targetContainer.name();
    	
    	
    	treePath = tree.getSelectionPath(); // need to get selection path again. (after insertSequenceNamed).
    	
    	if(targetContainer.entryCount() != 0) {
    		System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneSequence] ERROR: targetSequence.entryCount != 0 " + targetContainer.name());
    	}
    	
    	String newName; 
    	for(int i = 0; i < sourceContainer.entryCount(); i++) {
    		Reference entry = sourceContainer.entry(i);
    		newName	= entry.name() + "_clone";
    		if(entry instanceof SequenceReference) {
		    SequenceReference 	sourceRef = (SequenceReference)entry;
		    Sequence source    = (Sequence) sourceRef.parent();
		    newName = ConfigurationTreeActions.insertSequenceNamed(tree, newName); // It has created the targetContainer
		    Object lc = tree.getSelectionPath().getLastPathComponent(); // get from the new selectionPath set in insertSequenceNamed.
		    ConfigurationTreeActions.DeepCloneSequence(tree, source, (ReferenceContainer) lc);
		    Sequence targetSequence = config.sequence(newName);
		    config.insertSequenceReference(targetContainer,i, targetSequence).setOperator(sourceRef.getOperator());
		    model.nodeInserted(targetContainer,i);
    		} else if(entry instanceof ModuleReference) {
    			ModuleReference 	sourceRef = (ModuleReference)entry;
    			ConfigurationTreeActions.CloneModule(tree, sourceRef, newName);
    		} else {
        		System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneSequence] ERROR: reference instanceof " + entry.getClass());
        		return false;
    		}
    		tree.setSelectionPath(treePath); // set the selection path again to this level.
    	}
    	
    	return true;
    }
    
    /**
     * DeepCloneContainer
     * -----------------------------------------------------------------
     * DeepCloneContainer clones a Path container.
     * Generates a full copy of the selected path also creating
     * clones of modules and nested sequences.
     * This method uses recursion to also take into account the sub-paths.
     * */
    public static boolean DeepCloneContainer(JTree tree, ReferenceContainer sourceContainer, ReferenceContainer targetContainer) {
    	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
    	Configuration          config   = (Configuration)model.getRoot();
    	TreePath               treePath = tree.getSelectionPath();
    	String targetName = sourceContainer.name() + "_clone";
    	
    	if(targetContainer == null) {
    		if (sourceContainer instanceof Path) {
        		targetName = ConfigurationTreeActions.insertPathNamed(tree, targetName); // It has created the targetContainer
        		targetContainer = config.path(targetName);
        		
        		Path sourcePath = config.path(sourceContainer.name());
        		((Path)targetContainer).setAsEndPath(sourcePath.isSetAsEndPath());	// Setting as End path if needed.
    		} else System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneContainer] ERROR: sourceContainer NOT instanceof Path");
    		
        	if(targetContainer == null) {
        		System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneContainer] ERROR: targetSequence == NULL");
        		return false;
        	}
    	} else targetName = targetContainer.name();
    	
    	
    	treePath = tree.getSelectionPath(); // need to get selection path again. (after insertSequenceNamed).
    	
    	if(targetContainer.entryCount() != 0) {
    		System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneContainer] ERROR: targetContainer.entryCount != 0 " + targetContainer.name());
    	}
    	

    	for(int i = 0; i < sourceContainer.entryCount(); i++) {
    		Reference entry = sourceContainer.entry(i);
    		
    		if(entry instanceof SequenceReference) {
		        // Sets selection path to sequenceNode:
		        tree.setSelectionPath(new TreePath(model.getPathToRoot(model.sequencesNode())));
			
			SequenceReference 	sourceRef = (SequenceReference)entry;
			Sequence source    = (Sequence) sourceRef.parent();
			ConfigurationTreeActions.DeepCloneSequence(tree, source, null);
			
			// Getting the cloned sequence using the selection path:
			Sequence clonedSequence = (Sequence) tree.getLastSelectedPathComponent();
			
			config.insertSequenceReference(targetContainer,i, clonedSequence).setOperator(sourceRef.getOperator());
			model.nodeInserted(targetContainer,i);
			
			// sets the selection path back to pathNode:
			tree.setSelectionPath(treePath);    	    	

    		} else if (entry instanceof ModuleReference) {
    			String newName	= entry.name() + "_clone";
    			ModuleReference 	sourceRef = (ModuleReference)entry;
    			ConfigurationTreeActions.CloneModule(tree, sourceRef, newName);

    		} else if (entry instanceof PathReference) {
    			String newName	= entry.name() + "_clone";
        		newName = ConfigurationTreeActions.insertPathNamed(tree, newName); // It has created the targetContainer
        		Path targetPath = config.path(newName);
        		Path sourcePath = config.path(entry.name());
        		targetPath.setAsEndPath(sourcePath.isSetAsEndPath());	// Setting as End path if needed.
        		
        		// Clone subpath:
        		ConfigurationTreeActions.DeepCloneContainer(tree, sourcePath, targetPath);
        		
        		// and now insert the reference
			config.insertPathReference(targetContainer, i, targetPath).setOperator(entry.getOperator());
			model.nodeInserted(targetContainer,i);
        		
    		} else {
    			System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneContainer] Error instanceof ?");
    		}
    		
    		tree.setSelectionPath(treePath); // set the selection path again to this level.
    	}

    	return true;
    }
    
    
    /** CloneReferenceContainer
     * -----------------------------------------------------------------
     * It will perform clones of sequences or paths.
     * This is also called "Simple Clone" or "shallow clone". It will only create a new 
     * top level sequence/path containing references to original modules 
     * and sequences of the source one.
     * */
    public static boolean CloneReferenceContainer(JTree tree, ReferenceContainer sourceContainer) {
    	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
    	Configuration          config   = (Configuration)model.getRoot();
    	TreePath               treePath = tree.getSelectionPath();
    	
    	String targetName = "Copy_of_" + sourceContainer.name();
    	ReferenceContainer targetContainer = null;
    	
		if (sourceContainer instanceof Path) {
    		targetName = ConfigurationTreeActions.insertPathNamed(tree, targetName); // It has created the targetContainer
    		targetContainer = config.path(targetName);
    		
    		Path sourcePath = config.path(sourceContainer.name());
    		((Path)targetContainer).setAsEndPath(sourcePath.isSetAsEndPath());	// Setting as End path if needed.
		} else if(sourceContainer instanceof Sequence) {
			targetName = ConfigurationTreeActions.insertSequenceNamed(tree, targetName); // It has created the targetContainer
			targetContainer = config.sequence(targetName); 
		} else {
			System.err.println("[confdb.gui.ConfigurationTreeActions.CloneReferenceContainer] ERROR: sourceContainer NOT instanceof Path");
			return false;
		}
		
    	if(targetContainer == null) {
    		System.err.println("[confdb.gui.ConfigurationTreeActions.CloneReferenceContainer] ERROR: targetSequence == NULL");
    		return false;
    	}
    	
    	//treePath = tree.getSelectionPath(); // need to get selection path again. (after insertSequenceNamed).
    	
    	if(targetContainer.entryCount() != 0) {
    		System.err.println("[confdb.gui.ConfigurationTreeActions.CloneReferenceContainer] ERROR: targetContainer.entryCount != 0 " + targetContainer.name());
    	}
    	

    	for(int i = 0; i < sourceContainer.entryCount(); i++) {
    		Reference entry = sourceContainer.entry(i);
    		
    		if(entry instanceof SequenceReference) {
    			SequenceReference 	sourceRef = (SequenceReference)entry;
			Sequence source    = (Sequence) sourceRef.parent();
			config.insertSequenceReference(targetContainer,i, source).setOperator(sourceRef.getOperator());
			model.nodeInserted(targetContainer,i);
    		} else if (entry instanceof ModuleReference) {
    			ModuleInstance module = config.module(entry.name());
    			config.insertModuleReference(targetContainer, i, module).setOperator(entry.getOperator());
    			model.nodeInserted(targetContainer, i);
    		} else if (entry instanceof PathReference) {
        		Path sourcePath = config.path(entry.name());
			config.insertPathReference(targetContainer, i, sourcePath).setOperator(entry.getOperator());
			model.nodeInserted(targetContainer,i);
    		} else {
    			System.err.println("[confdb.gui.ConfigurationTreeActions.CloneReferenceContainer] Error: instanceof ?");
    		}
    		
    		//tree.setSelectionPath(treePath); // set the selection path again to this level.
    	}

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

    
    /** Import all references from a container (paths or sequences).
     * The import operation is performed by a worker and showing a progress bar.
	 * */
    public static boolean importAllReferenceContainers(JTree tree, JTree sourceTree, Object external)  {
    	ConfigurationTreeModel sm  		= (ConfigurationTreeModel)sourceTree.getModel();
		ConfigurationTreeModel tm    	= (ConfigurationTreeModel)tree.getModel();
		Configuration          config   = (Configuration)tm.getRoot();
		
    	if(sm.getChildCount(external) == 0) return false;

    	// Check existing items:
    	boolean existance = false;
		for(int i = 0; i < sm.getChildCount(external); i++) {
			ReferenceContainer container = (ReferenceContainer) sm.getChild(external, i);
			ReferenceContainer   targetContainer = null;
			if 		(container instanceof Path) 	{
				targetContainer = config.path(container.name())		;
			}
			else if (container instanceof Sequence)	{
				targetContainer = config.sequence(container.name())	;
			}
			
			if (targetContainer!=null) {
				existance = true;
				break;
			} 
		}

		boolean updateAll = false;
    	if(existance) {
        	int choice = JOptionPane.showConfirmDialog(null			,
        			" Some Items may already exist. "				+
    				"Do you want to overwrite them All?"			,
    						      "Overwrite all"					,
    						      JOptionPane.YES_NO_CANCEL_OPTION	);        	
        	if(choice == JOptionPane.CANCEL_OPTION) return false;
        	updateAll = (choice == JOptionPane.YES_OPTION);
    	} 
    	
        ImportAllReferencesThread worker = new ImportAllReferencesThread(tree, sourceTree, external, updateAll);
        WorkerProgressBar progressBar = new WorkerProgressBar("Importing all references", worker);
        progressBar.createAndShowGUI();	// Run the worker and show the progress bar.

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
	    if (type.equals("path")) {
		container = config.insertPath(index,external.name());
		((Path)container).setAsEndPath(((Path)external).isSetAsEndPath());
	    } else {
		container = config.insertSequence(index,external.name());
	    }

	}
	
	// Force update the tree to avoid ArrayIndexOutOfBoundsException bug76145
	if (update) model.nodeChanged(container);
	else	    model.nodeInserted(parent,index);

	if (importContainerEntries(config,model,external,container))
	    container.setDatabaseId(external.databaseId());
	
	model.nodeChanged(container);
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
    
    
    /** import Path / Sequence
     * DeepImportReferenceContainer Import Paths and Sequences container
     * giving as result identical containers in source and target configuration.
     * NOTE: The implementation of this method has change to support filtering.
     * It uses the source configuration instead of the source JTree. 
     * */
    public static boolean DeepImportReferenceContainer(JTree tree, Configuration sourceConfig, ReferenceContainer external)
    {
		ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();			// Target Model.
		Configuration          config   = (Configuration)model.getRoot();
		TreePath               treePath = tree.getSelectionPath();
		
		int count =
		    (external instanceof Path) ? config.pathCount():config.sequenceCount();
		int index = (treePath==null) ? count :
		    (treePath.getPathCount()==2) ?
		    0:model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
					    treePath.getLastPathComponent())+1;
		
		ReferenceContainer  container	= null;
		Object              parent		= null;
		String              type		= null;
		Diff				diff		= null;
		

		// prepare to make a diff.
		Configuration importTestConfig = getConfigurationCopy(config);
		if (external instanceof Path) {
		    container = importTestConfig.path(external.name());
		    type      = "path";
		} else if (external instanceof Sequence) {
		    container = importTestConfig.sequence(external.name());
		    type      = "sequence";
		}
		if (container==null)  { // if root container doesn't exist:
		    if (!importTestConfig.hasUniqueQualifier(external)) System.out.println("[DeepImportReferenceContainer] !importTestConfig.hasUniqueQualifier(external)!");
		    if (type.equals("path")) {
				container = importTestConfig.insertPath(index,external.name());
				((Path)container).setAsEndPath(((Path)external).isSetAsEndPath());
		    } else {
		    	container = importTestConfig.insertSequence(index,external.name());
		    }
		}
		
		DeepImportContainerEntries(importTestConfig, sourceConfig, null, external, container);
		diff = new Diff(config, importTestConfig);

		// Instead of comparing the configuration as usually, we make use of two new comparing
		// methods which doesn't take Streams into account. This is because the temporary copy
		// created by "getConfigurationCopy" does not create a full functional copy of the target
		// configuration.
		diff.compareModules();
		diff.comparePathsIgnoreStreams();		// Ignore Streams.
		diff.compareSequencesIgnoreStreams();	// Ignore Streams.
		
	    String message 	= "You are about to add, delete or order multiple items! \n"; 
		message+= "These operations could adversely affect many parts of the configuration.\n";
		message+= "Please check the differences and make sure you want to do this.\n";
		
		boolean accept = false;
	
    	if (!diff.isIdentical()) {
    		DeepImportDiffDialog dlg = new DeepImportDiffDialog(diff, message);
    	    dlg.pack();
    	    dlg.setVisible(true);
    	    accept = dlg.getResults();
    	}
    	
	    // In case user cancel the operation:
    	if(!accept) return false;
    	// IF ACCEPT THEN DO IT
    	
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
		
		if (container==null)  { // if root container doesn't exist:
		    if (!config.hasUniqueQualifier(external)) return false;
		    if (type.equals("path")) {
				container = config.insertPath(index,external.name());
				((Path)container).setAsEndPath(((Path)external).isSetAsEndPath());
		    } else {
		    	container = config.insertSequence(index,external.name());
		    }
		    
			model.nodeInserted(parent,index); // Force update bug76145
		}
    	
		
		// This does the rest of the work:
		if (DeepImportContainerEntries(config, sourceConfig, tree, external, container))
		    container.setDatabaseId(external.databaseId());
		
		model.nodeChanged(container);
		model.updateLevel1Nodes();
		
		diff = new Diff(external.config(),config);
		String search = type+":"+container.name();
		diff.compare(search);
		if (!diff.isIdentical()) {
		    DiffDialog dlg = new DiffDialog(diff);
		    dlg.pack();
		    dlg.setVisible(true);
		}
		
		for (int i=0;i<container.referenceCount();i++) {
		    Reference reference = container.reference(i);
		    ReferenceContainer parentContainer = reference.container();
		    parentContainer.setHasChanged();
		}
		
		
		return true;
    }
    
    
    /**
     * getConfigurationCopy
     * ------------------------------------------------------------------------
     * This method return a partial copy of the given configuration.
     * The only purpose is to dispose of a copy to simulate changes in a configuration
     * allowing to check the differences between the copy and the original config.
     * It was originally designed to be used by DeepImportContainerEntries
     * and DeepImportContainerEntriesSimulation.
     * ------------------------------------------------------------------------
     * NOTE: DO NOT USE this method to create functional copies of a configuration.
     * */
    private static Configuration getConfigurationCopy(Configuration sourceConf) {
    	Configuration configurationCopy = new Configuration();
    	
    	// Configuration needs to be initialised with a new software release.
    	// A new software release allows to insert modules from scratch. 
    	configurationCopy.initialize(	new ConfigInfo("",null,sourceConf.releaseTag()) ,
				  						new SoftwareRelease(sourceConf.release()));
    	int index = 0;
    	
    	// COPY ESMODULES:
    	Iterator<ESModuleInstance> ESMit = sourceConf.esmoduleIterator();
    	index = 0;
	    while (ESMit.hasNext()) {
			ESModuleInstance esmodule = ESMit.next();
			ESModuleInstance NewModule = configurationCopy.insertESModule(index, esmodule.template().name(), esmodule.name()); 
			index++;
			Iterator<Parameter> itP = esmodule.parameterIterator();
			while (itP.hasNext()) {
			    Parameter p = itP.next();
			    NewModule.updateParameter(p.name(),p.type(),p.valueAsString());
			}
	    }

	    // COPY EDSOURCES:
    	Iterator<EDSourceInstance> EDSit = sourceConf.edsourceIterator();
    	index = 0;
	    while (EDSit.hasNext()) {
	    	EDSourceInstance edsource = EDSit.next();
			EDSourceInstance Newedsource = configurationCopy.insertEDSource(edsource.template().name());
			index++;
			Iterator<Parameter> itP = edsource.parameterIterator();
			while (itP.hasNext()) {
			    Parameter p = itP.next();
			    Newedsource.updateParameter(p.name(),p.type(),p.valueAsString());
			}
	    }

	    // COPY ESSource:
    	Iterator<ESSourceInstance> ESSit = sourceConf.essourceIterator();
    	index = 0;
	    while (ESSit.hasNext()) {
	    	ESSourceInstance essource = ESSit.next();
			ESSourceInstance Newessource = configurationCopy.insertESSource(index, essource.template().name(), essource.name());
			index++;
			Iterator<Parameter> itP = essource.parameterIterator();
			while (itP.hasNext()) {
			    Parameter p = itP.next();
			    Newessource.updateParameter(p.name(),p.type(),p.valueAsString());
			}
	    }
	    

	    // COPY SERVICES:
    	Iterator<ServiceInstance> SERit = sourceConf.serviceIterator();
    	index = 0;
	    while (SERit.hasNext()) {
	    	ServiceInstance service = SERit.next();
			ServiceInstance NewService = configurationCopy.insertService(index, service.template().name());
			index++;
			Iterator<Parameter> itP = service.parameterIterator();
			while (itP.hasNext()) {
			    Parameter p = itP.next();
			    NewService.updateParameter(p.name(),p.type(),p.valueAsString());
			}
	    }

	    
	    // COPY DATASETS:
    	Iterator<PSetParameter> dataIt = sourceConf.psetIterator();
    	index = 0;
	    while (dataIt.hasNext()) {
	    	PSetParameter data = dataIt.next();
	    	PSetParameter datacheck = configurationCopy.pset(data.name());
	    	if(datacheck == null) {
	    		configurationCopy.insertPSet(data);	
	    	}
			index++;
	    }
	    
	    // COPY Event Content:
    	Iterator<EventContent> E = sourceConf.contentIterator();
    	index = 0;
	    while (E.hasNext()) {
	    	EventContent EvC = E.next();
	    	EventContent EvCCheck = configurationCopy.insertContent(EvC.name());
	    	// COPY STREAMS:
	    	
	    	Iterator<Stream> StrIterator = EvC.streamIterator();
	    	while(StrIterator.hasNext()) {
	    		Stream Str = StrIterator.next();
	    		EvCCheck.insertStream(Str.name());
	    	}
			index++;
			
	    }   	    

	    // COPY Sequence:
    	Iterator<Sequence> Seqit = sourceConf.sequenceIterator();
    	index = 0;
	    while (Seqit.hasNext()) {
	    	Sequence sequ = Seqit.next();
			Sequence newSequ = configurationCopy.sequence(sequ.name());
			if (newSequ==null) {
				newSequ = configurationCopy.insertSequence(index, sequ.name());
				importContainerEntries(configurationCopy,null,sequ,newSequ);
			}
			index++;
	    }
	
	    // COPY PATHS:
    	Iterator<Path> pathit = sourceConf.pathIterator();
    	index = 0;
	    while (pathit.hasNext()) {
	    	Path path = pathit.next();
	    	Path pathCheck = configurationCopy.path(path.name());
	    	if(pathCheck == null) {
				Path newPath = configurationCopy.insertPath(index, path.name());
				newPath.setAsEndPath(path.isEndPath());
				importContainerEntries(configurationCopy,null,path,newPath);	
	    	}
			index++;
	    }
	    
 
	    // It doesn't really COPY:
	    //		- EVENTCONTENT, OUTPUTMODULES, STREAMS AND DATASETS.
	    
	    

	    
    	return configurationCopy;
    }
    
    
    
    
    /** import Path / Sequence
     * Perform updates and insertions of new references into a target configuration.
     * NOTE: Nodes are not updated in the Tree model.
     * */
    public static boolean importReferenceContainersNoModel(JTree tree, ReferenceContainer external, boolean update)
    {
	ConfigurationTreeModel model    = (ConfigurationTreeModel)tree.getModel();
	Configuration          config   = (Configuration)model.getRoot();
	
	int index = (external instanceof Path) ? config.pathCount():config.sequenceCount();
	
	ReferenceContainer   container = null;
	String               type      = null;

	if (external instanceof Path) {
	    container = config.path(external.name());
	    type      = "path";
	}
	else if (external instanceof Sequence) {
	    container = config.sequence(external.name());
	    type      = "sequence";
	}
	
	if (container!=null) {
	    index = (type.equals("path")) ? config.indexOfPath((Path)container) 
		                          : config.indexOfSequence((Sequence)container);
	    if(update) {
		    while (container.entryCount()>0) {
			Reference entry = (Reference)container.entry(0);
			removeReference(config, null, entry);
		    }
	    } else return false;
	} else {
	    if (!config.hasUniqueQualifier(external)) return false;
	    if (type.equals("path")) {
	    	container = config.insertPath(index,external.name());
	    	((Path)container).setAsEndPath(((Path)external).isSetAsEndPath());
	    } else {
			container = config.insertSequence(index,external.name());
	    }
	}
	
	
	if (importContainerEntries(config,null,external,container))
	    container.setDatabaseId(external.databaseId());
	
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
	boolean updateModel = (treeModel !=null);
	// result=true: import all daugthers unchangend
	boolean result = true;

	for (int i=0;i<sourceContainer.entryCount();i++) {
	    Reference entry = sourceContainer.entry(i);
	    
	    if (entry instanceof ModuleReference) {
			ModuleReference sourceRef = (ModuleReference)entry;
			ModuleInstance  source    = (ModuleInstance)sourceRef.parent();
			ModuleInstance  target    = config.module(source.name());
			ModuleReference targetRef = null;
			if (target!=null) {
			    targetRef = config.insertModuleReference(targetContainer,i,target);
			    result = false;
			}
			else {
			    targetRef = config.insertModuleReference(targetContainer,i,
							     source.template().name(),
							     source.name());
			    target = (ModuleInstance)targetRef.parent();
			    for (int j=0;j<target.parameterCount();j++)
				target.updateParameter(j,source.parameter(j)
						       .valueAsString());
			    target.setDatabaseId(source.databaseId());
			}
			targetRef.setOperator(sourceRef.getOperator());

			if (updateModel) {
			    treeModel.nodeInserted(targetContainer,i);
			    if (target.referenceCount()==1)
				treeModel.nodeInserted(treeModel.modulesNode(),
						       config.moduleCount()-1);
			}

	    } else if (entry instanceof OutputModuleReference) {
			OutputModuleReference sourceRef = (OutputModuleReference)entry;
			OutputModule          source    = (OutputModule)sourceRef.parent();
			OutputModule          target    = config.output(source.name());
			OutputModuleReference targetRef = null;
			if (target!=null) {
			    targetRef = config.insertOutputModuleReference(targetContainer,i,target);
			    result = false;
			}
			else {
			    System.out.println("OutputModules must already exist as they are imported via stream import!");
			    return result;
			}
			targetRef.setOperator(sourceRef.getOperator());
	
			if (updateModel) {
			    treeModel.nodeInserted(targetContainer,i);
			    if (target.referenceCount()==1)
				treeModel.nodeInserted(treeModel.outputsNode(),
						       config.outputCount()-1);
			}

	    } else if (entry instanceof PathReference) {
			PathReference sourceRef=(PathReference)entry;
			Path          source   =(Path)sourceRef.parent();
			Path          target   =config.path(source.name());
			if (target!=null) {
			    config.insertPathReference(targetContainer,i,target).setOperator(sourceRef.getOperator());
			    result = false;
			}
			else {
			    target = config.insertPath(config.pathCount(),sourceRef.name());
			    if (updateModel) treeModel.nodeInserted(treeModel.pathsNode(),
								 config.pathCount()-1);
			    config.insertPathReference(targetContainer,i,target).setOperator(sourceRef.getOperator());
			    boolean tmp =
				importContainerEntries(config,treeModel,source,target);
			    if (tmp) target.setDatabaseId(source.databaseId());
			    if (result) result = tmp;
			}
			
			if (updateModel) treeModel.nodeInserted(targetContainer,i);

	    } else if (entry instanceof SequenceReference) {
			SequenceReference sourceRef=(SequenceReference)entry;
			Sequence          source=(Sequence)sourceRef.parent();
			Sequence          target=config.sequence(sourceRef.name());
			if (target!=null) {
			    config.insertSequenceReference(targetContainer,i,target).setOperator(sourceRef.getOperator());
			    result = false;
			}
			else {
			    target = config.insertSequence(config.sequenceCount(), sourceRef.name());
			    if (updateModel) treeModel.nodeInserted(treeModel.sequencesNode(), config.sequenceCount()-1);
			    config.insertSequenceReference(targetContainer,i,target).setOperator(sourceRef.getOperator());
			    boolean tmp = importContainerEntries(config,treeModel,source,target);
			    if (tmp) target.setDatabaseId(source.databaseId());
			    if (result) result = tmp;
			}
			
			if (updateModel) treeModel.nodeInserted(targetContainer,i);
	    }
	}
	return result;
    }
    
    
    /** 
     * DeepImportContainerEntries
     * ----------------------------
     * Insert entries of an external reference container into the local copy 
     * In this case, deep check is made to ensure that Containers are identical.
     * Inserting, Replacing, Deleting and Ordering modules.
     * NOTE: DeepImport feature make use of preanalysis before performing its
     * operations. If for any reason the DeepImportContainerEntries structure is changed
     * then DeepImportContainerEntriesSimulation must also be changed to ensure the
     * diff results matches the DeepImport results.
     * @author jimeneze
     * */
    private static
	boolean DeepImportContainerEntries(Configuration config,
					   Configuration sourceConfig,
					   JTree targetTree,
					   ReferenceContainer sourceContainer,
					   ReferenceContainer targetContainer) {

	boolean updateModel = (targetTree != null);
    	ConfigurationTreeModel treeModel = null;
	if (updateModel) treeModel = (ConfigurationTreeModel)targetTree.getModel();
	
    	boolean result = true;

	for (int i=0;i<sourceContainer.entryCount();i++) {
	    Reference entry = sourceContainer.entry(i);
	    
	    if (entry instanceof ModuleReference) {					// MODULE REFERENCES
		ModuleReference sourceRef = (ModuleReference)entry;
		ModuleInstance  source    = (ModuleInstance)sourceRef.parent();
		ModuleInstance  target    = config.module(source.name());
		ModuleReference targetRef = null;
		
		if (target!=null) { // if module already exist then just insert the reference.
		    
		    Diff diff = new Diff(sourceConfig,config);
		    Comparison c = diff.compareInstances(source, target);
		    
		    // If module exist but it's not identical:
		    if (!c.isIdentical()) { // replace module.
			if (updateModel) {
			    ConfigurationTreeActions.replaceModule(targetTree, source);
			    treeModel.nodeStructureChanged(treeModel.modulesNode()); // forcing refresh
			} else {
			    ConfigurationTreeActions.replaceModuleNoModel(config, source);
			}
		    } // else If module exist and it's identical then do nothing.
		    
		    // After replacing the module (or not) we proceed checking the reference:
		    boolean existance = false;
		    for (int j=0;j<targetContainer.entryCount();j++) {
			Reference subentry = (Reference)targetContainer.entry(j);
			if(	(subentry 	instanceof ModuleReference)&&
				(entry 		instanceof ModuleReference)&&
				(subentry.name().equals(entry.name()))) 	{
			    
			    // Check if modules are in the same order:
			    if(i != j) { // then remove reference, and insert it again.
				if (updateModel) {
				    removeReference(null, targetTree, subentry); // this might delete the Module (index of -1 when searching).
				    treeModel.nodeStructureChanged(targetContainer);
				} else {
				    removeReference(config, null, subentry);     // this might delete the Module (index of -1 when searching).
				}
				existance = false;	// this force the reference to be inserted again (in order).
			    } else {
				subentry.setOperator(sourceRef.getOperator());
				existance = true;
			    }
			}
		    }
		    if(!existance) { 
			int indexOfModule = config.indexOfModule(source);
			if(indexOfModule == -1) {
			    // the instance was also removed so it needs to be inserted again:
			    targetRef = config.insertModuleReference(	
								     targetContainer, i			, 
								     source.template().name()	,
								     source.name())				;
			    target = (ModuleInstance)targetRef.parent();
			    // Update parameters:
			    for (int j=0;j<target.parameterCount();j++)
				target.updateParameter(j,source.parameter(j).valueAsString());
			    target.setDatabaseId(source.databaseId());
			    
			    // it was inserted. common operations:
			    targetRef.setOperator(sourceRef.getOperator()); 
			    if (updateModel) {
				treeModel.nodeInserted(targetContainer,i);
				if (target.referenceCount()==1) treeModel.nodeInserted(treeModel.modulesNode(), config.moduleCount()-1);
			    }
			} else { // ONLY insert the reference. (the module already exists).
			    targetRef = config.insertModuleReference(targetContainer, i, target);
			    targetRef.setOperator(sourceRef.getOperator()); 
			    if (updateModel) treeModel.nodeInserted(targetContainer,i);	    					
			}	    				
		    }
		} else { // Inserts the module and the reference:
		    
		    // NOTE: next call also inserts the module before inserting references	
		    targetRef = config.insertModuleReference(	
							     targetContainer, i			,
							     source.template().name()	,
							     source.name())				;
		    
		    target = (ModuleInstance)targetRef.parent();
		    // Update parameters:
		    for (int j=0;j<target.parameterCount();j++)
			target.updateParameter(j,source.parameter(j).valueAsString());
		    target.setDatabaseId(source.databaseId());
		    
		    // it was inserted. common operations:
		    targetRef.setOperator(sourceRef.getOperator()); 
		    if (updateModel) {
			treeModel.nodeInserted(targetContainer,i);
			if (target.referenceCount()==1) treeModel.nodeInserted(treeModel.modulesNode(), config.moduleCount()-1);
		    }
		}
		//-----------------------------------------------------------------------------//
	    } else if (entry instanceof OutputModuleReference) {	// OUTPUTMODULE REFERENCES
		OutputModuleReference sourceRef = (OutputModuleReference)entry;
		OutputModule          source    = (OutputModule)sourceRef.parent();
		OutputModule          target    = config.output(source.name());
		OutputModuleReference targetRef = null;
		if (target!=null) { // if OutputModule exist then insert ONLY the Reference.
		    Diff diff = new Diff(sourceConfig,config);
		    Comparison c = diff.compareOutputModules(source, target);
		    
		    // If output module exists but it's not identical:
		    if (!c.isIdentical()) { // replace module:
					    // Update parameters: 
			for (int j=0;j<target.parameterCount();j++) {
			    target.updateParameter(j,source.parameter(j).valueAsString());
			}
			target.setDatabaseId(source.databaseId());
			
			if (updateModel) treeModel.nodeStructureChanged(treeModel.modulesNode()); // forcing refresh
		    } else {	// If module exist and it's identical then do nothing:
			result = false;
		    }
		    
		    targetRef = config.insertOutputModuleReference(targetContainer,i,target);
		    
		    // common operations: 
		    targetRef.setOperator(sourceRef.getOperator());
		    if (updateModel) {
			treeModel.nodeInserted(targetContainer,i);
			if (target.referenceCount()==1)
			    treeModel.nodeInserted(treeModel.outputsNode(), config.outputCount()-1);
		    }
		    result = false;
		} else { // If it does not exist won't import anything.
		    System.out.println("OutputModules must already exist as they are imported via stream import!");
		}
		//-----------------------------------------------------------------------------//
	    } else if (entry instanceof PathReference) {	// PATH REFERENCES
		PathReference sourceRef=(PathReference)entry;
		Path          source   =(Path)sourceRef.parent();
		Path          target   =config.path(source.name());

		if (target!=null) {	// if the path already exist then it just insert the reference.
		    
		    Diff diff = new Diff(sourceConfig,config);
		    Comparison c = diff.compareContainers(source, target);

		    if (!c.isIdentical()) {
			//System.out.println("existing Path differences found!");
			DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
			if (updateModel) treeModel.nodeStructureChanged(treeModel.pathsNode());		    	
		    } // else if IDENTICAL: nothing to do.
		    
		    // Do not insert Paths references.
		    // Nested paths are not allowed in theory.
		    // config.insertPathReference(targetContainer,i,target);
		    // if (updateModel) treeModel.nodeInserted(targetContainer,i); // refresh the tree view
		    
		    // Now references must be checked:
		    for (int j=0;j<targetContainer.entryCount();j++) {
			Reference subentry = (Reference)targetContainer.entry(j);
			if(	(subentry 	instanceof PathReference)&&
				(entry 		instanceof PathReference)&&
				(subentry.name().equals(entry.name()))) 	{
			    
			    // Check if SequenceReference are in the same order:
			    if(i != j) {
				// So remove reference, and insert it later.
				if (updateModel) {
				    removeReference(null, targetTree, subentry); // this might delete the ITEM (index of -1 when searching). 
				    treeModel.nodeStructureChanged(targetContainer);
				} else {
				    removeReference(config, null, subentry);     // this might delete the ITEM (index of -1 when searching). 
				}
			    } else {
				subentry.setOperator(sourceRef.getOperator());
			    }
			}
		    }
		    
		    result = false;
		} else { // insert the path and the reference.
		    
		    target = config.insertPath(config.pathCount(),sourceRef.name());
		    config.insertPathReference(targetContainer,i,target).setOperator(sourceRef.getOperator());	// insert the reference.

		    if (updateModel) {
			treeModel.nodeInserted(treeModel.pathsNode(), config.pathCount()-1);
			treeModel.nodeInserted(targetContainer,i); // refresh the tree view
		    }

		    // recursively entries insertion!
		    boolean tmp = DeepImportContainerEntries(config, sourceConfig, targetTree,source,target);
		    if (tmp) target.setDatabaseId(source.databaseId());
		    if (result) result = tmp;
		    if (updateModel) treeModel.nodeStructureChanged(treeModel.pathsNode());
		}
		
		
		// INSERT REFERENCES: for new sequences, and out of order references.
		boolean existance = false;
    	    	for (int j=0;j<targetContainer.entryCount();j++) {
		    Reference subentry = (Reference)targetContainer.entry(j);
		    if(	(subentry 	instanceof PathReference)&&
			(entry 		instanceof PathReference)&&
			(subentry.name().equals(entry.name()))) 	{
			subentry.setOperator(sourceRef.getOperator());
			existance = true;
		    }
    	    	}
    	    	if(!existance) {
		    config.insertPathReference(targetContainer,i,target).setOperator(sourceRef.getOperator());
		    if (updateModel) treeModel.nodeInserted(targetContainer,i); // refresh the tree view
    	    	}
		
		//treeModel.nodeInserted(targetContainer,i); // refresh the tree view
		//-----------------------------------------------------------------------------//
	    } else if (entry instanceof SequenceReference) {		// SEQUENCE REFERENCES
		
		SequenceReference sourceRef=(SequenceReference)entry;
		Sequence          source=(Sequence)sourceRef.parent();
		Sequence          target=config.sequence(sourceRef.name());
		
		if (target!=null) {	// if sequence already exist then just insert the reference.

		    Diff diff = new Diff(sourceConfig,config);
		    Comparison c = diff.compareContainers(source, target);

		    if (!c.isIdentical()) {
			DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
			if (updateModel) {
			    treeModel.nodeStructureChanged(treeModel.sequencesNode());
			    treeModel.nodeStructureChanged(treeModel.pathsNode());
			}
		    } // if identical, just check the order.
		    
		    // Now references must be checked:
		    for (int j=0;j<targetContainer.entryCount();j++) {
			Reference subentry = (Reference)targetContainer.entry(j);
			if(	(subentry 	instanceof SequenceReference)&&
				(entry 		instanceof SequenceReference)&&
				(subentry.name().equals(entry.name()))) 	{
			    
			    // Check if SequenceReference are in the same order:
			    if(i != j) {
				// So remove reference, and insert it later.
				if (updateModel) {
				    removeReference(null, targetTree, subentry); // this might delete the ITEM (index of -1 when searching). 
				    treeModel.nodeStructureChanged(targetContainer);
				} else {
				    removeReference(config, null, subentry);     // this might delete the ITEM (index of -1 when searching). 
				}
			    } else {
				subentry.setOperator(sourceRef.getOperator());
			    }
			}
		    }
		    result = false;
		} else { // Insert the sequence and the reference.					
		    
		    target = config.insertSequence(config.sequenceCount(), sourceRef.name());
		    config.insertSequenceReference(targetContainer,i,target).setOperator(sourceRef.getOperator());

		    if (updateModel) treeModel.nodeInserted(treeModel.sequencesNode(), config.sequenceCount()-1);

		    //config.insertSequenceReference(targetContainer,targetContainer.entryCount(),target);		    

		    // recursively entries insertion!
		    boolean tmp = DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
		    if (tmp) target.setDatabaseId(source.databaseId());
		    if (result) result = tmp;
		    if (updateModel) treeModel.nodeInserted(targetContainer,targetContainer.entryCount() -1);
		}
		
		// INSERT REFERENCES: for new sequences, and out of order references.
		boolean existance = false;
    	    	for (int j=0;j<targetContainer.entryCount();j++) {
		    Reference subentry = (Reference)targetContainer.entry(j);
		    if(	(subentry 	instanceof SequenceReference)&&
			(entry 		instanceof SequenceReference)&&
			(subentry.name().equals(entry.name()))) 	{
			subentry.setOperator(sourceRef.getOperator());
			existance = true;
		    }
    	    	}
    	    	if(!existance) {
		    config.insertSequenceReference(targetContainer,i,target).setOperator(sourceRef.getOperator());
		    if (updateModel) treeModel.nodeInserted(targetContainer,i);
    	    	}
	    }
	}
	//-----------------------------------------------------------------------------//
	
	// REMOVE ALL REMAINING ITEMS. Containers must be identical.
	for (int i=0;i<targetContainer.entryCount();i++) {
			
	    Reference targetSubEntry = (Reference) targetContainer.entry(i);
	    boolean found = false;
	    for (int j=0;j<sourceContainer.entryCount();j++) {
		Reference sourceSubEntry = (Reference) sourceContainer.entry(j);
		
		if(	((((targetSubEntry instanceof SequenceReference)&&
			   (sourceSubEntry instanceof SequenceReference))
			  ||
			  ((targetSubEntry instanceof ModuleReference)&&
			   (sourceSubEntry  instanceof ModuleReference))
			  ||
			  ((targetSubEntry instanceof PathReference)&&
			   (sourceSubEntry  instanceof PathReference)))	
			 &&
			 (targetSubEntry.name().equals(sourceSubEntry.name())))) {
		    if(i == j) 
			found = true;
		} 
	    }
	    if(!found) { // DELETE
		if (updateModel) {
		    removeReference(null, targetTree, targetSubEntry);
		    treeModel.nodeStructureChanged(targetContainer);
		} else {
		    removeReference(config, null, targetSubEntry);
		}
		i--;	// going back after modifying the size.
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
			} else  {
			    instanceName = templateName; int count=2;
			    while (template.hasInstance(instanceName)) {
				instanceName = templateName + count; ++count;
			    }
			    reference = config.insertModuleReference(parent,index,
								     templateName,
								     instanceName);
			    module = (ModuleInstance)reference.parent();
			}
	    } else {
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
    
    
    /** removeReference
     * ------------------
     * remove reference passed by parameter instead using selectionPath.
     * NOTE: This will be used by deep import function.
     * */
    public static boolean removeReference(Configuration config, JTree tree, Reference reference)
    {
	if ((config==null) && (tree==null)) return false;
	if ((config!=null) && (tree!=null)) return false;

	ConfigurationTreeModel model  = null;

	boolean updateModel = (tree!=null);
	if (updateModel) {
	    model  = (ConfigurationTreeModel)tree.getModel();
	    config = (Configuration)model.getRoot();
	}

	ReferenceContainer     container = reference.container();
	int                    index     = container.indexOfEntry(reference);

	ModuleInstance         module    = null;
	int                    indexOfModule= -1;
		
	if (reference instanceof ModuleReference) {
	    module = (ModuleInstance)reference.parent();
	    indexOfModule = config.indexOfModule(module);
	    config.removeModuleReference((ModuleReference)reference);
	} else if (reference instanceof OutputModuleReference) {
	    config.removeOutputModuleReference((OutputModuleReference)reference);
	} else {
	    container.removeEntry(reference);
	}
	
	if (updateModel) {
	    model.nodeRemoved(container,index,reference);
	    if (module!=null&&module.referenceCount()==0) model.nodeRemoved(model.modulesNode(),indexOfModule,module);
	    model.updateLevel1Nodes();
	}
	
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
    
    
    /**
     * scroll to the Path given by the path name and expand the tree.
     * */
    public static void scrollToPathByName(String pathName, JTree tree) {
    	ConfigurationTreeModel model   =(ConfigurationTreeModel)tree.getModel();
    	Configuration          config  =(Configuration)model.getRoot();
    	
    	TreePath Path = new TreePath(model.getPathToRoot(config.path(pathName)));
    	tree.setSelectionPath(Path);
    	tree.expandPath(Path);
    	tree.scrollPathToVisible(Path);
    }

    /**
     * scroll to the Path given by the sequence name and expand the tree.
     * */
    public static void scrollToSequenceByName(String sequenceName, JTree tree) {
    	ConfigurationTreeModel model   =(ConfigurationTreeModel)tree.getModel();
    	Configuration          config  =(Configuration)model.getRoot();
    	
    	TreePath Path = new TreePath(model.getPathToRoot(config.sequence(sequenceName)));
    	tree.setSelectionPath(Path);
    	tree.expandPath(Path);
    	tree.scrollPathToVisible(Path);
    }
    
    /**
     * scroll to the module given by the module name and expand the tree.
     * */
    public static void scrollToModuleByName(String moduleName, JTree tree) {
    	ConfigurationTreeModel model   =(ConfigurationTreeModel)tree.getModel();
    	Configuration          config  =(Configuration)model.getRoot();
    	
    	TreePath Path = new TreePath(model.getPathToRoot(config.module(moduleName)));
    	tree.setSelectionPath(Path);
    	tree.expandPath(Path);
    	tree.scrollPathToVisible(Path);
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
    
    /**
     * replace a module with the internal one
     */
    public static boolean replaceModuleInternally(JTree tree,ModuleInstance oldModule,String newObject)
    {
	/* newObject = class or class:name or copy:class:name */
	System.out.println("XXX "+oldModule.name()+" "+newObject);

	if (tree==null||oldModule==null||newObject==null) return false;

	String oldModuleName = oldModule.name();
	String newModuleName = null;
	String[] s = newObject.split(":");

	ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
	Configuration          config    = (Configuration)model.getRoot();
	if (config==null) return false;

	if (config.module(oldModule.name())==null) return false;

	ModuleInstance newModule = null;
	String newTemplateName   = null;
	if (s.length==1) {
	    // old module replaced by new instance of the template, keeping oldModuleName
	    newTemplateName = s[0];
	    // temporary unique name
	    newModuleName   = "Instance_of_"+newTemplateName;
	    int i=0;
	    while (!config.isUniqueQualifier(newModuleName)) {
		newModuleName = "Instance_of_"+newTemplateName+"_"+i;
		++i;
	    }
	    newModule = config.insertModule(newTemplateName,newModuleName);
	    Iterator<Parameter> itP = null;
	    itP=oldModule.parameterIterator();
	    while (itP.hasNext()) {
		Parameter p = itP.next();
		Iterator<Parameter> itQ = newModule.parameterIterator();
		while (itQ.hasNext()) {
		    Parameter q = itQ.next();
		    if (p.type().equals(q.type())) newModule.updateParameter(q.name(),q.type(),p.valueAsString());
		}
	    }			
	    itP = oldModule.parameterIterator();
	    while (itP.hasNext()) {
		Parameter p = itP.next();
		Parameter n = newModule.parameter(p.name(),p.type());
		if (n!=null) newModule.updateParameter(p.name(),p.type(),p.valueAsString());
	    }
	} else if (s.length==2) {
	    // old module replaced by existing module, keeping newModuleName
	    newTemplateName = s[0];
	    newModuleName   = s[1];
	    if (newModuleName.equals(oldModuleName)) return false;
	    newModule = config.module(newModuleName);
	} else if (s.length==3) {
	    // old module replaced by new copy of an existing module, keeping oldModuleName
	    newTemplateName = s[1];
	    // temporary unique name
	    newModuleName   = "Copy_of_"+s[2];
	    int i=0;
	    while (!config.isUniqueQualifier(newModuleName)) {
		newModuleName = "Copy_of_"+s[2]+"_"+i;
		++i;
	    }
	    newModule = config.insertModule(newTemplateName,newModuleName);
	    Iterator<Parameter> itP = null;
	    itP=oldModule.parameterIterator();
	    while (itP.hasNext()) {
		Parameter p = itP.next();
		Iterator<Parameter> itQ = newModule.parameterIterator();
		while (itQ.hasNext()) {
		    Parameter q = itQ.next();
		    if (p.type().equals(q.type())) newModule.updateParameter(q.name(),q.type(),p.valueAsString());
		}
	    }			
	    itP = oldModule.parameterIterator();
	    while (itP.hasNext()) {
		Parameter p = itP.next();
		Parameter n = newModule.parameter(p.name(),p.type());
		if (n!=null) newModule.updateParameter(p.name(),p.type(),p.valueAsString());
	    }	    
	} else {
	    return false;
	}

	int index    = config.indexOfModule(oldModule);
	int refCount = oldModule.referenceCount();
	ReferenceContainer[] parents = new ReferenceContainer[refCount];
	int[]                indices = new int[refCount];
	Operator[]           operators = new Operator[refCount];
	int iRefCount=0;
	while (oldModule.referenceCount()>0) {
	    Reference reference = oldModule.reference(0);
	    parents[iRefCount] = reference.container();
	    indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
	    operators[iRefCount] = reference.getOperator();
	    config.removeModuleReference((ModuleReference)reference);
	    model.nodeRemoved(parents[iRefCount],indices[iRefCount],reference);
	    iRefCount++;
	}
	model.nodeRemoved(model.modulesNode(),index,oldModule);	

	// oldModule's refCount is now 0 and hence oldModule is removed
	// from the config; thus we can rename newModule to oldModule's
	// name which is needed for later combined setNameAndPropagate
	try {
	    newModule.setNameAndPropagate(oldModuleName);
	}
	catch (DataException e) {
	    System.err.println(e.getMessage());
	}
	
	// update refs pointing to oldModule to point to newModule
	for (int i=0;i<refCount;i++) {
	    config.insertModuleReference(parents[i],indices[i],newModule).setOperator(operators[i]);
	    model.nodeInserted(parents[i],indices[i]);
	}
	
	if (s.length==2) {
	    // now rename newModule back to its original name, and update all
	    // (V)InputTags/keeps etc. originally referring to both oldModule
	    // and the also newModule under oldModule's name to use  newModule's
	    // original and final name.
	    try {
		newModule.setNameAndPropagate(newModuleName);
	    }
	    catch (DataException e) {
		System.err.println(e.getMessage());
	    }
	}

	model.updateLevel1Nodes();

	model.nodeStructureChanged(model.modulesNode());
	if (s.length==2) {
	    scrollToModuleByName(newModuleName, tree);
	} else {
	    scrollToModuleByName(oldModuleName, tree);
	}

	return true;
    }


    /**
     * Clone module
     * --------------------
     * Clone an existing module with a different name.
     * USAGE: if newName is null, it allows the user to edit the name,
     * otherwise it assign the prefix "copy_of_" to the sourceModule. 
     * @NOTE: if the user doesn't change the default name 'copy_of_xxx'
     * it will throw an exception:
     * Instance.setName() ERROR: name 'copy_of_hltDisplacedHT250L25Associator' is not unique!
     * That message is not a problem. It also happens adding a new module.
     * */
    public static boolean CloneModule(JTree tree,ModuleReference oldModule, String newName) {
    	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
		Configuration          config   = (Configuration)model.getRoot();
		TreePath               treePath = tree.getSelectionPath();
		int                    depth    = treePath.getPathCount();
		TreePath parentTreePath = (depth==3) ? treePath : treePath.getParentPath();
		ReferenceContainer parent = (ReferenceContainer)parentTreePath.getLastPathComponent();

		// INDEX: if you are cloning the module by hand use next position in selection path.
		// if module is being cloned by "cloneSequence" use the count to insert it in order.
		
		int index = (newName != null) ? parent.entryCount() 
				: parent.indexOfEntry((Reference)treePath.getLastPathComponent())+1;
		
		Reference      reference    = null;
		ModuleInstance module       = config.module(oldModule.name());
		
		// retrieving the template:
	    String   templateName=  module.template().name();
	    String   instanceName = oldModule.name();
	    ModuleTemplate template	= config.release().moduleTemplate(templateName);
    
    	ModuleInstance original = null;
		try {
		    original = (ModuleInstance)template.instance(instanceName);
		}
		catch (DataException e) {
		    System.err.println(e.getMessage());
		    return false;
		}
		
		// Temporary name "copy_of_xxx"
		if(newName != null) instanceName = newName;
		else 				instanceName = "copy_of_" + instanceName;
		
		// To make sure module name doesn't exist.
		String temp;
		if(config.module(instanceName)!= null)
			for(int j = 0; j < 10; j++) {
				temp = instanceName + "_" + j;
				if(config.module(temp) == null) {
					j = 10;
					instanceName = temp;
				}
			}

		reference = config.insertModuleReference(parent,index,
							 templateName,
							 instanceName);
		reference.setOperator(oldModule.getOperator());

		module = (ModuleInstance)reference.parent();
		
		// Copy values
		Iterator<Parameter> itP = original.parameterIterator();
		while (itP.hasNext()) {
		    Parameter p = itP.next();
		    module.updateParameter(p.name(),p.type(),p.valueAsString());
		}
    	
		// Inserting in the model and refreshing tree view:
    	model.nodeInserted(parent,index);
    	model.updateLevel1Nodes();

    	TreePath newTreePath = parentTreePath.pathByAddingChild(reference);
    	tree.expandPath(newTreePath.getParentPath());
    	tree.setSelectionPath(newTreePath);
    	
    	// Allow the user to modify the name of the reference
    	if (module!=null&&module.referenceCount()==1) {
    	    TreePath moduleTreePath = new TreePath(model.getPathToRoot((Object)module));
    	    model.nodeInserted(model.modulesNode(),config.moduleCount()-1);
    	    if(newName == null) editNodeName(tree);
    	}
    	
    	return true;
    }
    

    /**
     * replace a container (path or sequence) with the internal one
     */
    public static boolean replaceContainerInternally(JTree tree,String type,ReferenceContainer oldContainer,String newObject)
    {
	if (tree==null||type==null||oldContainer==null||newObject==null) return false;
	if (newObject.equals(oldContainer.name())) return false;

	ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
	Configuration          config    = (Configuration)model.getRoot();
	if (config==null) return false;

	if (type.equals("Sequence")) {

	    Sequence oldSequence = (Sequence)oldContainer;
	    if (oldSequence==null) return false;
	    if (config.sequence(oldSequence.name())==null) return false;
	    Sequence newSequence = config.sequence(newObject);
	    if (newSequence==null) return false;

	    int index    = config.indexOfSequence(oldSequence);
	    int refCount = oldSequence.referenceCount();
	    ReferenceContainer[] parents = new ReferenceContainer[refCount];
	    int[]                indices = new int[refCount];
	    Operator[]           operators = new Operator[refCount];
	    int iRefCount=0;
	    while (oldSequence.referenceCount()>0) {
		Reference reference = oldSequence.reference(0);
		parents[iRefCount] = reference.container();
		indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
		operators[iRefCount] = reference.getOperator();
		reference.remove();
		model.nodeRemoved(parents[iRefCount],indices[iRefCount],reference);
		iRefCount++;
	    }
	    model.nodeRemoved(model.sequencesNode(),index,oldSequence);	
	    for (int i=0;i<refCount;i++) {
		Reference check = parents[i].entry(newSequence.name());
		int iref=parents[i].indexOfEntry(check);
		if (iref<0) {
		    config.insertSequenceReference(parents[i],indices[i],newSequence).setOperator(operators[i]);
		    model.nodeInserted(parents[i],indices[i]);
		} else if (iref>indices[i]) {
		    config.insertSequenceReference(parents[i],indices[i],newSequence).setOperator(operators[i]);
		    model.nodeInserted(parents[i],indices[i]);
		    check.remove();
		    model.nodeRemoved(parents[i],iref,check);
		}
	    }
	    model.updateLevel1Nodes();
	    tree.expandPath(new TreePath(model.getPathToRoot(newSequence)));
	    config.removeSequence(oldSequence);

	} else if (type.equals("Path")) {

	    Path oldPath = (Path)oldContainer;
	    if (oldPath==null) return false;
	    if (config.path(oldPath.name())==null) return false;
	    Path newPath = config.path(newObject);
	    if (newPath==null) return false;

	    int index    = config.indexOfPath(oldPath);
	    int refCount = oldPath.referenceCount();
	    ReferenceContainer[] parents = new ReferenceContainer[refCount];
	    int[]                indices = new int[refCount];
	    Operator[]           operators = new Operator[refCount];
	    int iRefCount=0;
	    while (oldPath.referenceCount()>0) {
		Reference reference = oldPath.reference(0);
		parents[iRefCount] = reference.container();
		indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
		operators[iRefCount] = reference.getOperator();
		reference.remove();
		model.nodeRemoved(parents[iRefCount],indices[iRefCount],reference);
		iRefCount++;
	    }
	    model.nodeRemoved(model.pathsNode(),index,oldPath);	
	    for (int i=0;i<refCount;i++) {
		Reference check = parents[i].entry(newPath.name());
		int iref=parents[i].indexOfEntry(check);
		if (iref<0) {
		    config.insertPathReference(parents[i],indices[i],newPath).setOperator(operators[i]);
		    model.nodeInserted(parents[i],indices[i]);
		} else if (iref>indices[i]) {
		    config.insertPathReference(parents[i],indices[i],newPath).setOperator(operators[i]);
		    model.nodeInserted(parents[i],indices[i]);
		    check.remove();
		    model.nodeRemoved(parents[i],iref,check);
		}
	    }	    
	    model.updateLevel1Nodes();
	    tree.expandPath(new TreePath(model.getPathToRoot(newPath)));
	    //
	    // newPath is added to oldPath's datasets/streams/contents
	    Iterator<PrimaryDataset> itPD = oldPath.datasetIterator();
	    while (itPD.hasNext())   itPD.next().insertPath(newPath);
	    Iterator<Stream>         itST = oldPath.streamIterator();
	    while (itST.hasNext())   itST.next().insertPath(newPath);
	    Iterator<EventContent>   itEC = oldPath.contentIterator();
	    while (itEC.hasNext())   newPath.addToContent(itEC.next());
	    // tricky: newPath must get oldPath's [Smart]Prescales
	    String newPathName = newPath.name();
	    String tmpPathName = newPathName+"_X";
	    while (!config.isUniqueQualifier(tmpPathName)) tmpPathName +="X";
	    try {
		newPath.setNameAndPropagate(tmpPathName);
		newPath.setName(newPathName);
		oldPath.setName(tmpPathName);
	    }
	    catch (DataException e) {
		System.err.println(e.getMessage());
	    }
	    //
	    config.removePath(oldPath);

	} else {
	    return false;
	}

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
	    if (treePath!=null) {
		Object targetNode=treePath.getLastPathComponent();
		if (targetNode instanceof EventContent)
		    content = (EventContent)targetNode;
	    }
	}
	else {
	    content = config.content(contentName);
	}
	
	if (content==null) {
	    System.err.println("stream must be added to existing and selected event content!");
	    return false;
	}
	
	Stream stream = content.stream(external.name());
	if (stream==null) stream = content.insertStream(external.name());

	stream.setFractionToDisk(external.fractionToDisk());
	OutputModule om = new OutputModule(external.outputModule().name(),stream);
	stream.setOutputModule(om);
	
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
    
    /** add a path to an existing stream 
     *  This function also updates the prescaler modules */
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
	
	
	// Fixes the unassignedPathsList mess.
	// I need to update the unassigned paths.
	// NOTE: nothing but the last item of this container is a stringBuffer.
	// the rest of the nodes are primaryDatasets (So we need to loop).
	int index = model.getChildCount(stream);
	for(int i = 0; i < index; i++) {
		ConfigurationTreeNode unassignedPathsNode = (ConfigurationTreeNode)model.getChild(stream, i);
		if(unassignedPathsNode.object() instanceof StringBuffer) model.nodeStructureChanged(unassignedPathsNode);
	}
	
	model.updateLevel1Nodes();
	
	// Feature/Bug 86605
	ArrayList<Path> newPaths = new ArrayList<Path>(); // Needed for method definition.
	newPaths.add(path);	
	updateFilter(config, stream, newPaths);	// To copy update prescaler.
	
	return true;
    }
    
    
    /** remove a path from a stream */
    //TODO deprecated method, sharing one path in more than one dataset.
    /*
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
    */
    
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
		
		//PrimaryDataset dataset = stream.dataset(path);
		ArrayList<PrimaryDataset> primaryDatasets = stream.datasets(path);
		
		for(int ds = 0; ds < primaryDatasets.size(); ds++) {
			PrimaryDataset dataset = primaryDatasets.get(ds);
			
			if (dataset!=null)
			    model.nodeRemoved(dataset,dataset.indexOfPath(path),path);
		}

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
    

    /** remove unassigned paths from an existing stream */
    public static boolean removeUnassignedPathsFromStream(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	Object                 node   = treePath.getLastPathComponent();
	Stream stream = null;

	if (node instanceof Stream) {
	    stream = (Stream)node;
	} else if (node instanceof ConfigurationTreeNode) {
	    ConfigurationTreeNode treeNode = (ConfigurationTreeNode)node;
	    stream = (Stream)treeNode.parent();
	    tree.setSelectionPath(treePath.getParentPath());
	}

	EventContent content = stream.parentContent();

	ArrayList<Path> unassigned = stream.listOfUnassignedPaths();
	Iterator<Path> itP = unassigned.iterator();
	while (itP.hasNext()) {
	    Path path = itP.next();
	    int index = stream.indexOfPath(path);

	    int contentIndex = content.indexOfPath(path);
	    
	    //PrimaryDataset dataset = stream.dataset(path);
	    ArrayList<PrimaryDataset> datasets = stream.datasets(path);
	    
	    
	    //if (dataset!=null) model.nodeRemoved(dataset,dataset.indexOfPath(path),path);
	    // TODO update to count on many datasets sharing the same path.
	    for(int i = 0; i < datasets.size(); i++) {
	    	PrimaryDataset ds = datasets.get(i);
	    	model.nodeRemoved(ds,ds.indexOfPath(path),path);
	    }
	    
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
	
	    model.updateLevel1Nodes();
	    
	}
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
	    model.nodeInserted(dataset.parentStream(), dataset.parentStream().indexOfDataset(dataset));
	
	model.nodeStructureChanged(model.contentsNode());
	model.updateLevel1Nodes();
	
	if (node == model.datasetsNode()) {
	    TreePath newTreePath = treePath.pathByAddingChild(dataset);
	    tree.setSelectionPath(newTreePath);
	    
	}
	
	return true;
    }
    
    
    /**
     * This method inserts an existing PrimaryDatasets into a Stream including
     * all it's paths. 
     * This is to implement the drag and drop functionality for PrimaryDatasets
     * between Streams.
     * bug/feature 88066
     * */
    public static boolean insertPrimaryDatasetPathsIncluded(JTree tree, PrimaryDataset dataset) {
    	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
    	Configuration          config = (Configuration)model.getRoot();
    	TreePath               treePath = tree.getSelectionPath();
    	Object                 node     = treePath.getLastPathComponent();
    	boolean inserted = true;
    	
    	if(node instanceof Stream) {
    		Stream targetStream = (Stream) node;
    		PrimaryDataset newDataSet = targetStream.insertDataset(dataset.name());
    		insertPrimaryDataset(tree, newDataSet);
    		
    		TreePath TargetPath = new TreePath(model.getPathToRoot(newDataSet));
    		tree.setSelectionPath(TargetPath);
    		
    		for(int i = 0; i < dataset.pathCount(); i++)
    			inserted = addPathToDataset_noUpdateTreeNodes(tree, dataset.path(i).name());
    		
    		if(!inserted) {
    			// if not all the paths were properly inserted then remove the dataset.
    			// this will allow to restore the dataset to the original stream.
    			removePrimaryDataset(tree);
    		}
    		
    		// Due too the large amount of paths that can be associated to a primaryDataset
    		// the Tree structure is only updated at the end of the process.
    		model.nodeChanged(newDataSet);
    		model.updateLevel1Nodes();
    	}
    	
    	return inserted;
    }
    
    /**
     * movePrimaryDataset
     * Uses sourceTree and targetTree to retrieve the transferred components
     * NOTE: 
     *  - Launch an error message to the user and revert the operation
     *    in case of failure.
     *  - When a Primary Dataset is transferred/moved from one stream to 
     *    another, paths are not removed from the parent source stream.
     * bug/feature 88066
     * */
    public static boolean movePrimaryDataset(JTree sourceTree, JTree targetTree, PrimaryDataset dataset) {
    	
    	TreePath               targetPath  = targetTree.getSelectionPath();
    	Object                 targetNode  = targetPath.getLastPathComponent();
		ConfigurationTreeModel sourceModel = (ConfigurationTreeModel)sourceTree.getModel();
		Configuration          config = (Configuration)sourceModel.getRoot();
		
		// Remove
    	TreePath SourcePath = new TreePath(sourceModel.getPathToRoot(dataset));
    	sourceTree.setSelectionPath(SourcePath);			    	
    	ConfigurationTreeActions.removePrimaryDataset(sourceTree);
    	
    	// Insert
    	TreePath TargetPath = new TreePath(sourceModel.getPathToRoot(targetNode));
    	targetTree.setSelectionPath(TargetPath);
		boolean ok = ConfigurationTreeActions.insertPrimaryDatasetPathsIncluded(targetTree, dataset);
		
		if(!ok) {
			// REVERT THE OPERATION

			// Restore the dataset to the previous Stream:
			SourcePath = new TreePath(sourceModel.getPathToRoot(dataset.parentStream())); // Point to the parent Stream.
	    	sourceTree.setSelectionPath(SourcePath);			    	
			ConfigurationTreeActions.insertPrimaryDatasetPathsIncluded(sourceTree, dataset);
			
			// ERROR: 
			// Add the configuration details:
			AboutDialog ad = new AboutDialog(null); // Only to get version and contact info. //DONT SHOW DIALOG!
			String StackTrace = "ConfDb Version: " 	+ ad.getConfDbVersion() 	+ "\n";
			StackTrace+= "Release Tag: " 			+ config.releaseTag()   		+ "\n";
			StackTrace+= "Configuration: " 			+ config.name()				+ "\n";
			StackTrace+= "-----------------------------------------------------------------\n";
			StackTrace+= "ERROR: ConfigurationTreeActions.insertPrimaryDatasetPathsIncluded(targetTree, pset); \n";
			StackTrace+= "ERROR: PrimaryDataset.insertPath() \n";
			
	    	String errMsg = "Drag and Drop operation FAILED!\n"	+
			"path already associated with dataset in the parent stream!\n"; 
			
	    	errorNotificationPanel cd = new errorNotificationPanel("ERROR", errMsg, StackTrace);
			cd.createAndShowGUI();
		}
		
		return ok;
    }
    
    /**
     * movePathsBetweenDatasets
     * Moves a path from one primary data set to another.
     * If datasets are not contained in the same stream it will remove
     * the path from the source P. Dataset leaving the path in the source stream.
     * 
     * Uses targetTree to retrieve the targetComponent 
     * @see ConfigurationTreeDropTarget.java, ConfigurationTreeTransferHandler.java
     * bug #82526: add/remove path to/from a primary datase
     * */
    public static boolean movePathsBetweenDatasets(JTree sourceTree, JTree targetTree, ConfigurationTreeNode pathNode) {
    	TreePath               targetPath  = targetTree.getSelectionPath();
    	Object                 targetNode  = targetPath.getLastPathComponent();
		ConfigurationTreeModel sourceModel = (ConfigurationTreeModel)sourceTree.getModel();
		Configuration          config = (Configuration)sourceModel.getRoot();
    	
    	Path path = (Path) pathNode.object();
    	
		// Remove
    	TreePath SourcePath = new TreePath(sourceModel.getPathToRoot(pathNode));
    	sourceTree.setSelectionPath(SourcePath);
    	ConfigurationTreeActions.removePathFromDataset(sourceTree);
    	
    	// Insert
    	TreePath TargetPath = new TreePath(sourceModel.getPathToRoot(targetNode));
    	targetTree.setSelectionPath(TargetPath);
    	ConfigurationTreeActions.addPathToDataset(targetTree, path.name());
    	
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
		if(treePath != null) {
		    Object targetNode=treePath.getLastPathComponent();
		    if (targetNode instanceof Stream)
			stream = (Stream)targetNode;
		    else return false;
		}
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
    
    /** Add a path to a primary dataset 
     * This method is used to add a single path to a Primary dataset.
     * This is not used to insert paths from the EditDatasetDialog panel.
     * */
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
	}else if (node instanceof ConfigurationTreeNode) {
	    ConfigurationTreeNode treeNode = (ConfigurationTreeNode)node;
	    if(treeNode.parent() instanceof ConfigurationTreeNode) {
	    	// REMOVING PATH FROM DATASET - FROM DATASET LIST.
		    ConfigurationTreeNode parentNode = (ConfigurationTreeNode)treeNode.parent();
		    path    = (Path)treeNode.object();
		    stream  = (Stream)parentNode.parent();
		    dataset = stream.dataset(name);
		    tree.setSelectionPath(treePath.getParentPath());
	    } else if(treeNode.parent() instanceof PrimaryDataset) {
			// REMOVING PATH FROM DATASET - FROM STREAM LIST.
	    	String pathName = "";
	    	if(treeNode.object() instanceof Path) {
	    		pathName = ((Path)treeNode.object()).name();
	    	} else System.err.println("[ConfigurationTreeActions.java][addPathToDataset] ERROR: TreeNode is not instance of Path");
		    dataset = config.dataset(name);
		    stream  = dataset.parentStream();
		    path    = config.path(pathName);
	    }

	} else if (node instanceof Path) {
		// REMOVING PATH FROM DATASET - FROM PATH LIST.
    	path = (Path) node;
	    dataset = config.dataset(name);
	    stream  = dataset.parentStream();
	}
	
	
	int index = -1;
	if (stream.indexOfPath(path)<0) stream.insertPath(path);
	else index = stream.listOfUnassignedPaths().indexOf(path);
	
	// bug/feature #93322 	Remove GUI and database restriction to share a path in more than one PrimaryDataset in a Stream.
	if(dataset.path(path.name()) == null) {
		dataset.insertPath(path);
		model.nodeInserted(dataset,dataset.indexOfPath(path));
		if (model.streamMode().equals("datasets")) {
		    model.nodeInserted(model.getChild(stream,stream.indexOfDataset(dataset)),dataset.indexOfPath(path));
		    if(index != -1) {
		    	model.nodeRemoved(model.getChild(stream,stream.datasetCount()),index,path);
		    }
		}
	}
	

	model.nodeChanged(path);
	model.updateLevel1Nodes();
	
	// Feature/Bug 86605
	ArrayList<Path> newPaths = new ArrayList<Path>(); // Needed for method definition.
	newPaths.add(path);	
	updateFilter(config, stream, newPaths);	// To copy update prescaler.
	
	return true;
    }
    
    /** Add a path to a primary dataset 
     * So far, this method is used to add a single path to a Primary dataset.
     * NOTE: This method will not update the TreeNodes since this is to be used
     * in a loop for multiple path insertions.
     * bug/feature 88066.
     * */
    private static boolean addPathToDataset_noUpdateTreeNodes(JTree tree,String name)
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
	
	boolean inserted = dataset.insertPath(path);
	if(inserted) {
		model.nodeInserted(dataset,dataset.indexOfPath(path));
		if (model.streamMode().equals("datasets")) {
		    model.nodeInserted(model.getChild(stream,stream.indexOfDataset(dataset)),
				       dataset.indexOfPath(path));
		    model.nodeRemoved(model.getChild(stream,stream.datasetCount()),index,path);
		}
		//model.nodeChanged(path);
		//model.updateLevel1Nodes();
		
		// Feature/Bug 86605
		ArrayList<Path> newPaths = new ArrayList<Path>(); // Needed for method definition.
		newPaths.add(path);	
		updateFilter(config, stream, newPaths);	// To copy update prescaler.
	} else {
		// This will invoke an errorNotificationPanel.
		return false;
	}
	
	return true;
    }
    
    
    /** remove a path from its parent dataset */
    public static boolean removePathFromDataset(JTree tree)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	ConfigurationTreeNode treeNode = (ConfigurationTreeNode)treePath.getLastPathComponent();
	PrimaryDataset dataset = (PrimaryDataset)treeNode.parent();
	Stream         stream  = dataset.parentStream();
	Path           path    = (Path)treeNode.object();
	int            index   = dataset.indexOfPath(path);
	
	dataset.removePath(path);
	
	model.nodeRemoved(dataset,index,treeNode);
	if (model.streamMode().equals("datasets")) {
	    model.nodeRemoved(model.getChild(stream,stream.indexOfDataset(dataset)), index,treeNode);
	}
	
	if(stream.datasets(path).size() == 0) {
		// Only if the path goes to unassignedPaths.
		model.nodeInserted(model.getChild(stream,stream.datasetCount()), stream.listOfUnassignedPaths().indexOf(path));
	}
		
	
	model.updateLevel1Nodes();
	
	return true;
    }
    
    /** remove a path from the given dataset. 
     * bug #82526: add/remove path to/from a primary dataset
     * */
    public static boolean removePathFromDataset(JTree tree, String datasetName)
    {
	ConfigurationTreeModel model  = (ConfigurationTreeModel)tree.getModel();
	Configuration          config = (Configuration)model.getRoot();
	TreePath               treePath = tree.getSelectionPath();
	
	Path           path    = (Path) treePath.getLastPathComponent();
	PrimaryDataset dataset = config.dataset(datasetName);
	Stream         stream  = dataset.parentStream();
	int            index   = dataset.indexOfPath(path);
	
	dataset.removePath(path);
	
	model.nodeRemoved(dataset,index,path);
	if (model.streamMode().equals("datasets"))
	    model.nodeRemoved(model.getChild(stream,stream.indexOfDataset(dataset)), index,path);
	
	// Only if the path goes to unassignedPaths.
	if(stream.datasets(path).size() == 0)
		model.nodeInserted(model.getChild(stream,stream.datasetCount()), stream.listOfUnassignedPaths().indexOf(path));
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
	Operator[]           operators = new Operator[refCount];
	ReferenceContainer[] parents = new ReferenceContainer[refCount];
	int[]                indices = new int[refCount];
	int iRefCount=0;
	while (oldModule.referenceCount()>0) {
	    Reference reference = oldModule.reference(0);
	    operators[iRefCount] = reference.getOperator();
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
		config.insertModuleReference(parents[i],indices[i],newModule).setOperator(operators[i]);
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
     * replaceModuleNoModel
     * --------------------------
     * Replace a Module but do not update the JTree representation.
     * Used by Import all functionality.
     * */
    public static boolean replaceModuleNoModel(Configuration config, ModuleInstance external)
    {
	//ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
	//Configuration          config    = (Configuration)model.getRoot();
	ModuleInstance         oldModule = config.module(external.name());
	if (oldModule==null) return false;

	int index    = config.indexOfModule(oldModule);
	int refCount = oldModule.referenceCount();
	Operator[]           operators = new Operator[refCount];
	ReferenceContainer[] parents = new ReferenceContainer[refCount];
	int[]                indices = new int[refCount];
	int iRefCount=0;
	while (oldModule.referenceCount()>0) {
	    Reference reference = oldModule.reference(0);
	    operators[iRefCount] = reference.getOperator();
	    parents[iRefCount] = reference.container();
	    indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
	    config.removeModuleReference((ModuleReference)reference);
	    iRefCount++;
	}
	
	try {
	    ModuleTemplate template = (ModuleTemplate)
		config.release().moduleTemplate(external.template().name());
	    ModuleInstance newModule = (ModuleInstance)
		template.instance(external.name());
	    for (int i=0;i<newModule.parameterCount();i++)
	    	newModule.updateParameter(i,external.parameter(i).valueAsString());
	    newModule.setDatabaseId(external.databaseId());
	    config.insertModule(index,newModule);
	    
	    for (int i=0;i<refCount;i++) {
	    	config.insertModuleReference(parents[i],indices[i],newModule).setOperator(operators[i]);
	    }
	} catch (DataException e) {
	    System.err.println("replaceModuleNoModel() FAILED: " + e.getMessage());
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
    
    
/** 
 * Import All instances.
 * */
    public static boolean ImportAllInstances(JTree tree, JTree sourceTree, Object external)  {
			ConfigurationTreeModel sm  		= (ConfigurationTreeModel) sourceTree.getModel();
			ConfigurationTreeModel tm    	= (ConfigurationTreeModel)tree.getModel();
			Configuration          config   = (Configuration)tm.getRoot();
			
	    	if(sm.getChildCount(external) == 0) {
	    		String error = "[confdb.gui.ConfigurationTreeActions.ImportAllInstances] ERROR: Child count == 0";
	    		System.err.println(error);
	    		return false;
	    	}

	    	// Checks if any item already exist.
	    	boolean existance = false;

			for(int i = 0; i < sm.getChildCount(external); i++) {
				Instance instance = (Instance) sm.getChild(external, i);
				if (	(instance instanceof EDSourceInstance)&&
					    config.edsource(instance.name())!=null||
					    (instance instanceof ESSourceInstance)&&
					    config.essource(instance.name())!=null||
					    (instance instanceof ESModuleInstance)&&
					    config.esmodule(instance.name())!=null||
					    (instance instanceof ServiceInstance)&&
					    config.service(instance.name())!=null) {

						existance = true;
						break;
					}
			}

			
			boolean updateAll = false;
			if(existance) {
		    	int choice = JOptionPane.showConfirmDialog(null			,
		    			" Some Items may already exist. "				+
						"Do you want to overwrite them All?"			,
								      "Overwrite all"					,
								      JOptionPane.YES_NO_CANCEL_OPTION	);
		    	
		    	if(choice == JOptionPane.CANCEL_OPTION) return false;
		    	updateAll = (choice == JOptionPane.YES_OPTION);
			}

			
			ImportAllInstancesThread worker = new ImportAllInstancesThread(tree, sm, external, updateAll);
			WorkerProgressBar wpb = new WorkerProgressBar("Importing Instances...", worker);
			wpb.createAndShowGUI();
			
		    return true;
    }

    /** 
     * Update All ESModules.
     * NOTE: ESModules are instances of Instance.
     * This function won't be used anymore.
     * */
    public static boolean UpdateAllModules(JTree tree, JTree sourceTree, Object external)  {
			ConfigurationTreeModel sm  		= (ConfigurationTreeModel) sourceTree.getModel();	
			
	    	if(sm.getChildCount(external) == 0) {
	    		String error = "[confdb.gui.ConfigurationTreeActions.UpdateAllESModules] ERROR: Child count == 0";
	    		System.err.println(error);
	    		return false;
	    	}

			if(sm.getChildCount(external) > 0) {
				Instance instance = (Instance) sm.getChild(external, 0);
				if(!(instance instanceof ModuleInstance)) {
					System.err.println("[confdb.gui.ConfigurationTreeActions.UpdateAllModules] ERROR: type mismatch!");
					return false;
				}
			} else return false;
			
	    	int choice = JOptionPane.showConfirmDialog(null						,
	    			" Items shared by both configurations will be overwritten.\n"+
					"Do you want to update them?"								,
							      "update all"									,
							      JOptionPane.YES_NO_OPTION						);
	    	
	    	if(choice == JOptionPane.NO_OPTION) return false;

	    	UpdateAllModulesThread worker = new UpdateAllModulesThread(tree, sm, external);
	    	WorkerProgressBar	wpb	 = new WorkerProgressBar("Updating all Modules", worker);
	    	wpb.createAndShowGUI();
	    	
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
    // NOTE: Requests to change names are caught 
    // by getCellEditorValue method at ConfigurationTreeEditor.java 
    public static void editNodeName(JTree tree)
    {
	TreePath treePath = tree.getSelectionPath();
	tree.expandPath(treePath.getParentPath());
	tree.scrollPathToVisible(treePath);
	tree.startEditingAtPath(treePath);
    }
    
    
    /** Replace instances in a configuration but don't update the model */
    public static boolean replaceInstancesNoModel(JTree tree,Instance external) {
    	ConfigurationTreeModel model     = (ConfigurationTreeModel)tree.getModel();
    	Configuration          config    = (Configuration)model.getRoot();
    	Instance               oldInst   = null;
    	Instance               newInst   = null;
    	int                    index     = -1;

    	if (external instanceof EDSourceInstance) {
    	    oldInst = config.edsource(external.name());
    	    index   = 0;
    	    config.removeEDSource((EDSourceInstance)oldInst);
    	    newInst = config.insertEDSource(external.template().name());
    	}
    	else if (external instanceof ESSourceInstance) {
    	    oldInst = config.essource(external.name());
    	    index   = config.indexOfESSource((ESSourceInstance)oldInst);
    	    config.removeESSource((ESSourceInstance)oldInst);
    	    newInst = config.insertESSource(index,
    					    external.template().name(),
    					    external.name());
    	}
    	else if (external instanceof ESModuleInstance) {
    	    oldInst = config.esmodule(external.name());
    	    index   = config.indexOfESModule((ESModuleInstance)oldInst);
    	    config.removeESModule((ESModuleInstance)oldInst);
    	    newInst = config.insertESModule(index,
    					    external.template().name(),
    					    external.name());
    	}
    	else if (external instanceof ServiceInstance) {
    	    oldInst = config.service(external.name());
    	    index   = config.indexOfService((ServiceInstance)oldInst);
    	    config.removeService((ServiceInstance)oldInst);
    	    newInst = config.insertService(index,external.template().name());
    	}
    	
    	for (int i=0;i<newInst.parameterCount();i++)
    	    newInst.updateParameter(i,external.parameter(i).valueAsString());
    	newInst.setDatabaseId(external.databaseId()); // dangerous?
    	
    	return true;
    }
    
    /** 
     * When a dataset has changed it must trigger the OutputModule Update as well as
     * the Prescaler values update.
     * Only the module instances created with the template TriggerResultsFilter
     * must be synch with the OutputModule path list.
     * NOTE: technically it is possible to have more than one instance of TriggerResultsFilter
     * but it should not happen in an EndPath. In a normal path it could happen, but those 
     * you don't need to autoupdate in any way, they are only edited by hand
     * 
     * Module template : TriggerResultsFilter
     * module --> HLTFilter --> T --> TriggerResultsFilter
     * */
    public static boolean updateFilter(Configuration config, Stream stream, ArrayList<Path> newPaths) {
    	OutputModule OutputM = stream.outputModule();
    	OutputM.forceUpdate(); // force update the OutputModules paths before update the filter.

    	Path[] paths = OutputM.parentPaths(); // END PATHS
    	
    	for(int i = 0; i < paths.length; i++) {
    		Path currentPath = paths[i];
    		Iterator<ModuleInstance> modules = currentPath.moduleIterator();
    		while(modules.hasNext()) {
    			ModuleInstance currentModule = modules.next();
    			Template template = currentModule.template();
    			ArrayList<Path> toAdd = new ArrayList<Path>();
    			
    			// Only module template : TriggerResultsFilter
    			if(	(template.name().compareTo("TriggerResultsFilter") == 0) &&
    				(template.type().compareTo("HLTFilter") == 0)){
    				
        			Parameter para = null;
        			para = currentModule.findParameter("triggerConditions");
        			if(para != null) {
        				VStringParameter parameterTriggerConditions =  (VStringParameter) para;
        				
        				if (parameterTriggerConditions==null) {
        				    System.err.println("[ERROR@ConfigurationTreeActions::synchOutputModuleAndStream] parameterTriggerConditions == null! ");
        				    return false;
        				}
        				
        				for(int w=0; w < newPaths.size(); w++) {
        					String pathname = newPaths.get(w).name();
        					boolean found = false;
	        				for(int j=0;j<parameterTriggerConditions.vectorSize();j++){
	        				    String trgCondition = (String)parameterTriggerConditions.value(j);
	        				    String strCondition = SmartPrescaleTable.regularise(trgCondition);
	        				    StringTokenizer pathTokens = new StringTokenizer(strCondition,"/ ");
	                		    while ( pathTokens.hasMoreTokens()) {
	                				String strPath = pathTokens.nextToken().trim();
	                				if(strPath.compareTo(pathname) == 0) {
	                					found = true;
	                					continue;
	                				}
	                			} // end while tokens.
	        				} // end for parameters
	        				
	        				if(!found) {	// then add the path
	        					toAdd.add(newPaths.get(w));
	        				}
        				} // end for newPaths
        				
        				// Add new paths to triggerConditions:
    					for(int w=0; w<toAdd.size();w++) {
    						if(para.valueAsString().length() > 2)	para.setValue(para.valueAsString() + ",\"" + toAdd.get(w) + "\"");
    						else 									para.setValue(para.valueAsString() + "\""  + toAdd.get(w) + "\""); 
    					}
        			}
    			}
    		} // end while modules.
    	} // end for paths.
    	return true;
    }
    
    // @TODO! change hardcode lines to enum values.
    public enum specialModules {
    	prescalerTemplateName {
    	    public String toString() { return "TriggerResultsFilter"; }
    	},

    	prescalerTemplateType {
    		public String toString() { return "HLTFilter"; }
    	}
    }

}


//////////////////////////////////////////////
/// threads
//////////////////////////////////////////////

/** Import a instance container using a different thread.  */
class ImportAllInstancesThread extends SwingWorker<String, String>
{
	  /** member data */
	  private long       				startTime	;

	  private 	JTree 					tree		;
	  private	ConfigurationTreeModel	sourceModel	;
	  private	ConfigurationTreeModel	targetModel	;
	  private	Configuration			targetConfig;
	  private	Configuration			sourceConfig;
	  private	Object					ext			;
	  private	boolean					updateAll	;
	  
	  private	String 					type		;	// type of imported items
	  private	ArrayList<String>		items		;	// name of imported items
	  private	ConfigurationModifier	mconf		;	// Needed when filtering. Only available resulting items.
	  
	  /** standard constructor */
	  public ImportAllInstancesThread(JTree Tree, ConfigurationTreeModel sourceModel, Object external, boolean UpdateAll)
	  {
		  this.tree 		= Tree;
		  this.sourceModel 	= sourceModel;
		  this.targetModel	= (ConfigurationTreeModel)tree.getModel();
		  this.ext 			= external;
		  this.updateAll	= UpdateAll;
		  this.items		= new ArrayList<String>();
		  this.type			= "";
		  this.targetConfig	= (Configuration)targetModel.getRoot();
		  this.sourceConfig	= null;
		  this.mconf		= null;
		  
		  
		  // Allows to import from a ConfigurationModifier Object. (after filtering).
		  Object sourceConf	= sourceModel.getRoot();
		  if(sourceConf instanceof Configuration) 
			  this.sourceConfig	= (Configuration)sourceModel.getRoot();
		  else if(sourceConf instanceof ConfigurationModifier)
			  mconf = (ConfigurationModifier) sourceModel.getRoot();

	  }
	
	  
	  /** Return an Array list with containers name to perform a diff operation. */
	  public ArrayList<String> getImportedItems(){
		  return items;
	  }
	  

	@Override
	protected String doInBackground() throws Exception {
		startTime = System.currentTimeMillis();
			if(sourceModel.getChildCount(ext) > 0) {
				Instance instance = (Instance) sourceModel.getChild(ext, 0);
						if(instance instanceof EDSourceInstance) type = "EDSource";
				else 	if(instance instanceof ESSourceInstance) type = "ESSource";
				else 	if(instance instanceof ESModuleInstance) type = "ESModule";
				else 	if(instance instanceof ServiceInstance)  type = "Service" ;
			}
			int count = sourceModel.getChildCount(ext);	
			
			for(int i = 0; i < sourceModel.getChildCount(ext); i++) {
				tree.setSelectionPath(null);
				Instance instance = (Instance) sourceModel.getChild(ext, i);
				
				int progress = (i*100)/count;	// range 0-100.
				setProgress(progress);
				items.add(instance.name());	// register item for diff

				if ((instance instanceof EDSourceInstance)&&
						targetConfig.edsource(instance.name())!=null||
				    (instance instanceof ESSourceInstance)&&
				    targetConfig.essource(instance.name())!=null||
				    (instance instanceof ESModuleInstance)&&
				    targetConfig.esmodule(instance.name())!=null||
				    (instance instanceof ServiceInstance)&&
				    targetConfig.service(instance.name())!=null) {
					
					if(updateAll) {
						ConfigurationTreeActions.replaceInstancesNoModel(tree,instance);
						firePropertyChange("current", null, instance.name());
					}
					continue;
				}
				
				if (targetConfig.isUniqueQualifier(instance.name())) {
					int index = 0;
					
					String   templateName = instance.template().name();
					String   instanceName = instance.name();
					Instance InsertedIinstance  = null;
					Object   parent       		= null;
					
					if (instance instanceof EDSourceInstance) {
						InsertedIinstance = targetConfig.insertEDSource(templateName);
					    parent = targetModel.edsourcesNode();
					}
					else if (instance instanceof ESSourceInstance) {
						InsertedIinstance = targetConfig.insertESSource(index,templateName,instanceName);
					    parent   = targetModel.essourcesNode();
					}
					else if (instance instanceof ESModuleInstance) {
						InsertedIinstance = targetConfig.insertESModule(index,templateName,instanceName);
					    parent   = targetModel.esmodulesNode();
					}
					else if (instance instanceof ServiceInstance) {
						InsertedIinstance = targetConfig.insertService(index,templateName);
					    parent   = targetModel.servicesNode();
					} else continue;
					
					for (int j=0;j<InsertedIinstance.parameterCount();j++)
						InsertedIinstance.updateParameter(j,instance.parameter(j).valueAsString());
					
					InsertedIinstance.setDatabaseId(instance.databaseId());
					targetModel.nodeInserted(parent,index);
					
					firePropertyChange("current", null, instance.name());
				}
			}
			
			setProgress(100);
			
	      return new String("Done!");
	}
	
	/*
    * Executed in event dispatching thread
    */
   @Override
   public void done() {
		long elapsedTime = System.currentTimeMillis() - startTime;
		
		Instance child = (Instance) sourceModel.getChild(ext, 0);
		if 	  (child instanceof ServiceInstance)	targetModel.nodeStructureChanged(targetModel.servicesNode()) ; 		
		else if (child instanceof ESModuleInstance)	targetModel.nodeStructureChanged(targetModel.esmodulesNode());
		else if (child instanceof ESSourceInstance)	targetModel.nodeStructureChanged(targetModel.essourcesNode());
		else if (child instanceof EDSourceInstance)	targetModel.nodeStructureChanged(targetModel.edsourcesNode());
		targetModel.updateLevel1Nodes();

		
		Diff diff;
		if(sourceConfig != null) 	diff= new Diff(sourceConfig,targetConfig);
		else						diff = new Diff(mconf, targetConfig);
		
    	diff.compare(type, items);
    	if (!diff.isIdentical()) {
    	    DiffDialog dlg = new DiffDialog(diff);
    	    dlg.pack();
    	    dlg.setVisible(true);
    	}
    	String time = 
        	String.format("%d min, %d sec", 
        		    TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
        		    TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - 
        		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime))
        		);
    	firePropertyChange("current", null, items.size() + " items imported! " + time);
    	System.out.println(items.size() + " items imported! enlapsedTime: " + time);
  

   }
}



/** Update Modules using threads.  */
final class UpdateAllModulesThread extends SwingWorker<String, String>
{
	  /** member data */
	  private long       				startTime	;
	  private 	JTree 					tree		;
	  private	ConfigurationTreeModel	sourceModel	;
	  private	ConfigurationTreeModel	targetModel	;
	  private	Object					ext			;
	  private	ArrayList<String>		items		;	// names of imported items
	  private	Configuration			targetConfig;
	  private	Configuration			sourceConfig;
	  private	ConfigurationModifier	sourceConfM	;
	  private	String					type		;
	  
	  /** standard constructor */
	  public UpdateAllModulesThread(JTree Tree, ConfigurationTreeModel sourceModel, Object external)
	  {
		  this.tree 		= Tree;
		  this.sourceModel 	= sourceModel;
		  this.targetModel	= (ConfigurationTreeModel)tree.getModel();
		  this.ext 			= external;
		  this.items		= new ArrayList<String>();
		  this.targetConfig	= (Configuration)targetModel.getRoot();
		  this.type			= "";
		  this.sourceConfig = null;
		  this.sourceConfM	= null; // Necessary to allow import from a filtered configuration.
		  
		  
		  Object conf = sourceModel.getRoot();
		  if(conf instanceof Configuration)
			  this.sourceConfig	= (Configuration)sourceModel.getRoot();
		  else if(conf instanceof ConfigurationModifier)
			  this.sourceConfM = (ConfigurationModifier) sourceModel.getRoot();
		  
	  }
	
	  /** Return an Array list with containers name to perform a diff operation. */
	  public ArrayList<String> getUpdatedItems(){
		  return items;
	  }

	@Override
	protected String doInBackground() throws Exception {
		startTime = System.currentTimeMillis();
        int count = sourceModel.getChildCount(ext);	
        ModuleInstance instance = null;
		for(int i = 0; i < sourceModel.getChildCount(ext); i++) {
			tree.setSelectionPath(null);
			instance = (ModuleInstance) sourceModel.getChild(ext, i);
			
			int progress = (i*100)/count;	// range 0-100
			setProgress(progress);
			
			if ((instance instanceof ModuleInstance)&&
			    targetConfig.module(instance.name())!=null) {
				ConfigurationTreeActions.replaceModuleNoModel(targetConfig,instance);
				items.add(instance.name());	// register item for diff
				firePropertyChange("current", null, instance.name());
			}
		}
		setProgress(100);
		
		if(instance != null)
		if(instance instanceof ModuleInstance) type = "Module";
		
		return new String("Done!");
	}
	  
	/*
    * Executed in event dispatching thread
    */
   @Override
   public void done() {
		long elapsedTime = System.currentTimeMillis() - startTime;

    	Diff diff;
    	if(sourceConfig != null)
    		diff= new Diff(sourceConfig,targetConfig);
    	else diff = new Diff(sourceConfM,targetConfig);
    	
    	diff.compare(type, items);
    	if (!diff.isIdentical()) {
    	    DiffDialog dlg = new DiffDialog(diff);
    	    dlg.pack();
    	    dlg.setVisible(true);
    	}
    	String time = 
    	String.format("%d min, %d sec", 
    		    TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
    		    TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - 
    		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime))
    		);

    	firePropertyChange("current", null, items.size() + " items updated! " + time);
    	System.out.println(items.size() + " items updated! enlapsedTime: " + time);
    	targetModel.updateLevel1Nodes();
    	targetModel.nodeStructureChanged(targetModel.modulesNode());
   }

}


/** Import a reference container using threads 
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4275976
 * */
class ImportAllReferencesThread extends SwingWorker<String, String> {
	  /** member data */
	  private 	long       				startTime	;
	  private 	JTree 					tree		;
	  private	ConfigurationTreeModel	sourceModel	;
	  private	ConfigurationTreeModel	targetModel	;
	  private	Object					ext			;
	  private	boolean					updateAll	;
	  private	ArrayList<String>		items		;
	  private	String					type		;
	  private	Configuration			targetConfig;
	  private	Configuration			sourceConfig;
	  private	ConfigurationModifier	sourceConfM	;	// Necessary to allow import from filtered configurations.
	  
	  
	  
	  /** standard constructor */
	  public ImportAllReferencesThread(JTree tree, JTree sourceTree, Object external, boolean UpdateAll)
	  {
		  this.tree 		= tree;
		  this.sourceModel 	= (ConfigurationTreeModel)sourceTree.getModel();;
		  this.targetModel	= (ConfigurationTreeModel)tree.getModel();
		  this.ext 			= external;
		  this.updateAll	= UpdateAll;
		  this.items		= new ArrayList<String>();
		  this.type			= "";
		  this.targetConfig	= (Configuration)targetModel.getRoot();
		  this.sourceConfig	= null;
		  this.sourceConfM	= null;
		  
		  Object sourceConf = sourceModel.getRoot();
		  if(sourceConf instanceof Configuration)
			  this.sourceConfig	= (Configuration)sourceModel.getRoot();
		  else if(sourceConf instanceof ConfigurationModifier)
			  this.sourceConfM	= (ConfigurationModifier) sourceModel.getRoot();
		  
	  }
	  
	  /** Return an Array list with container names to perform a diff operation. */
	  public ArrayList<String> getImportedItems() {
		  return items;
	  }


	@Override
	protected String doInBackground() throws Exception {
		startTime = System.currentTimeMillis();
		
        tree.setSelectionPath(null);
        int count = sourceModel.getChildCount(ext);
        ReferenceContainer container = null;
        String oldValue = "";
		for(int i = 0; i < count; i++) {
			container = (ReferenceContainer) sourceModel.getChild(ext, i);
			ConfigurationTreeActions.importReferenceContainersNoModel(tree, container, updateAll);
			items.add(container.name());	// registering container name for diff.
			int progress = (i*100)/count;	// range 0-100.
			
			setProgress(progress);
			firePropertyChange("current", oldValue, container.name());
			oldValue = container.name();
		}
		setProgress(100);
        
		if(container != null)
		if(container instanceof Path) type = "path";
		else if (container instanceof Sequence) type = "sequence";

    	return new String("Done!");
	}
	
	/**
    * Executed in event dispatching thread
    */
   @Override
   public void done() {
		long elapsedTime = System.currentTimeMillis() - startTime;

    	Diff diff;
    	if(sourceConfig != null)	diff = new Diff(sourceConfig,targetConfig);
    	else 						diff = new Diff(sourceConfM, targetConfig);
    	
    	diff.compare(type, items);
    	if (!diff.isIdentical()) {
    	    DiffDialog dlg = new DiffDialog(diff);
    	    dlg.pack();
    	    dlg.setVisible(true);
    	}
    	
    	String time = 
        	String.format("%d min, %d sec", 
        		    TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
        		    TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - 
        		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime))
        		);
    	firePropertyChange("current", null, items.size() + " items imported! " +  time);
    	System.out.println(items.size() + " items imported! enlapsedTime: " + time);
    	
    	// Since the model is not updated during the process, do it now.
    	if(this.type == "path") targetModel.nodeStructureChanged(targetModel.pathsNode());
    	else targetModel.nodeStructureChanged(targetModel.sequencesNode());
    	targetModel.updateLevel1Nodes();
   }
 }







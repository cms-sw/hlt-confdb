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
public class ConfigurationTreeActions {

	//
	// Parameters
	//
	private static boolean globalEDAliasAvailability = false;

	/** copy a parameter into another (v)pset */
	public static boolean insertParameter(JTree tree, Parameter parameter, ParameterTreeModel parameterTreeModel) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		IConfiguration config = (IConfiguration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		Object target = treePath.getLastPathComponent();
		Object parentOfTarget = treePath.getParentPath().getLastPathComponent();

		Object child = null;
		Object parent = null;
		int index = -1;
		Object parentOfPSet = null;

		if (target instanceof VPSetParameter && parameter instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter) parameter.clone(null);
			VPSetParameter vpset = (VPSetParameter) target;
			vpset.addParameterSet(pset);
			child = pset;
			parent = vpset;
			index = vpset.parameterSetCount() - 1;
			parentOfPSet = vpset.parent();
		} else if (target instanceof PSetParameter) {
			Parameter p = (Parameter) parameter.clone(null);
			PSetParameter pset = (PSetParameter) target;
			pset.addParameter(p);
			child = p;
			parent = pset;
			index = pset.parameterCount() - 1;
			parentOfPSet = pset.parent();
		} else if (parentOfTarget instanceof PSetParameter) {
			Parameter p = (Parameter) parameter.clone(null);
			Parameter ptarget = (Parameter) target;
			PSetParameter pset = (PSetParameter) parentOfTarget;
			child = p;
			parent = pset;
			index = pset.indexOfParameter(ptarget) + 1;
			parentOfPSet = pset.parent();
			pset.addParameter(index, p);
		} else {
			return false;
		}

		config.setHasChanged(true);

		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();

		// notify the tree of the children of the child which were inserted
		if (child instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter) child;
			for (int i = 0; i < pset.parameterCount(); i++) {
				model.nodeInserted(child, i);
			}
		}

		if (child instanceof VPSetParameter) {
			VPSetParameter vpset = (VPSetParameter) child;
			for (int i = 0; i < vpset.parameterSetCount(); i++) {
				model.nodeInserted(child, i);
			}
		}

		// notify the parameter tree model (parameter tree table)
		if (parameterTreeModel != null)
			parameterTreeModel.nodeInserted(parent, index);

		// notify the parent component that is has changed
		while (parentOfPSet != null) {
			if (parentOfPSet instanceof DatabaseEntry) {
				DatabaseEntry dbEntry = (DatabaseEntry) parentOfPSet;
				dbEntry.setHasChanged();
				parentOfPSet = null;
			} else if (parentOfPSet instanceof Parameter) {
				Parameter p = (Parameter) parentOfPSet;
				parentOfPSet = p.parent();
			} else {
				parentOfPSet = null;
			}
		}

		return true;
	}

	//
	// Global PSets
	//

	/** insert global pset */
	public static boolean insertPSet(JTree tree, PSetParameter pset) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		if(config.isUniqueQualifier(pset.name())){
			config.insertPSet(pset);

			model.nodeInserted(model.psetsNode(), config.psetCount() - 1);
			model.updateLevel1Nodes();

			TreePath parentPath = (treePath.getPathCount() == 2) ? treePath : treePath.getParentPath();
			tree.setSelectionPath(parentPath.pathByAddingChild(pset));

			return true;
		}else{
			System.err.println("Global PSet name "+pset.name()+" is not unique, pset not added");
			return false;
		}
	}

	/** remove global pset */
	public static boolean removePSet(JTree tree, PSetParameter pset) {
		return removeNode(tree, pset);
	}

	/** sort global psets */
	/*
	 * public static void sortPSets(JTree tree) { ConfigurationTreeModel model =
	 * (ConfigurationTreeModel)tree.getModel(); Configuration config =
	 * (Configuration)model.getRoot(); config.sortPSets();
	 * model.nodeStructureChanged(model.psetsNode()); }
	 */

	/**
	 * Import new global pset To do that first it will look for the parameter.
	 * Following the previous schema: If the parameter exist and its index is not
	 * less than zero then it will be replaced. New Pset parameters are inserted.
	 */
	public static boolean importPSet(JTree tree, Object external, PSetParameter pset, boolean update) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		PSetParameter oldPSet = config.pset(pset.name());

		if (oldPSet != null) {
			if (update) {
				int index = -1;
				Object parent = null;

				index = config.indexOfPSet(oldPSet);
				if (index < 0)
					return false;
				config.removePSet(oldPSet);
				parent = model.psetsNode();
				model.nodeRemoved(parent, index, external);
			} else
				return false;
		}

		config.insertPSet(pset);
		model.nodeInserted(model.psetsNode(), config.psetCount() - 1);

		return true;
	}

	/**
	 * Import All Parameter Sets
	 */
	public static boolean ImportAllPSets(JTree tree, JTree sourceTree, Object external) {
		ConfigurationTreeModel sm = (ConfigurationTreeModel) sourceTree.getModel();
		ConfigurationTreeModel tm = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) tm.getRoot();
		Configuration importConfig = (Configuration) sm.getRoot();

		if (sm.getChildCount(external) == 0) {
			String error = "[confdb.gui.ConfigurationTreeActions.ImportAllPSets] ERROR: Child count == 0";
			System.err.println(error);
			return false;
		}

		// Checks if any item already exist.
		boolean existance = false;
		for (int i = 0; i < sm.getChildCount(external); i++) {
			PSetParameter PSet = (PSetParameter) sm.getChild(external, i);
			if (config.pset(PSet.name()) != null) {
				existance = true;
				break;
			}
		}

		boolean updateAll = false;
		if (existance) {
			int choice = JOptionPane.showConfirmDialog(null,
					" Some PSets may already exist. " + "Do you want to overwrite them All?", "Overwrite all",
					JOptionPane.YES_NO_CANCEL_OPTION);

			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			updateAll = (choice == JOptionPane.YES_OPTION);
		}

		ArrayList<String> items = new ArrayList<String>();

		for (int i = 0; i < sm.getChildCount(external); i++) {
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
	public static boolean insertEDSource(JTree tree, String templateName) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		Template template = config.release().edsourceTemplate(templateName);
		return insertInstance(tree, template);
	}

	/** import EDSource */
	public static boolean importEDSource(JTree tree, EDSourceInstance external) {
		return importInstance(tree, external);
	}

	/** remove EDSource */
	public static boolean removeEDSource(JTree tree, EDSourceInstance edsource) {
		return removeNode(tree, edsource);
	}

	/** sort EDSources */
	public static void sortEDSources(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortEDSources();
		model.nodeStructureChanged(model.edsourcesNode());
	}

	//
	// ESSources
	//

	/** insert ESSource */
	public static boolean insertESSource(JTree tree, String templateName) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		if (templateName.indexOf(":") >= 0) {
			String[] s = templateName.split(":");
			Template template = config.release().essourceTemplate(s[1]);
			Instance original = null;
			try {
				original = template.instance(s[2]);
			} catch (DataException e) {
				System.err.println(e.getMessage());
				return false;
			}
			return insertCopy(tree, original);
		} else {
			Template template = config.release().essourceTemplate(templateName);
			return insertInstance(tree, template);
		}
	}

	/** import ESSource */
	public static boolean importESSource(JTree tree, ESSourceInstance external) {
		return importInstance(tree, external);
	}

	/** remove ESSource */
	public static boolean removeESSource(JTree tree, ESSourceInstance essource) {
		return removeNode(tree, essource);
	}

	/** sort ESSources */
	public static void sortESSources(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortESSources();
		model.nodeStructureChanged(model.essourcesNode());
	}

	//
	// ESModules
	//

	/** insert ESModule */
	public static boolean insertESModule(JTree tree, String templateName) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		if (templateName.indexOf(":") > 0) {
			String[] s = templateName.split(":");
			Template template = config.release().esmoduleTemplate(s[1]);
			Instance original = null;
			try {
				original = template.instance(s[2]);
			} catch (DataException e) {
				System.err.println(e.getMessage());
				return false;
			}
			return insertCopy(tree, original);
		} else {
			Template template = config.release().esmoduleTemplate(templateName);
			return insertInstance(tree, template);
		}
	}

	/** import ESModule */
	public static boolean importESModule(JTree tree, ESModuleInstance external) {
		return importInstance(tree, external);
	}

	/** remove ESModule */
	public static boolean removeESModule(JTree tree, ESModuleInstance esmodule) {
		return removeNode(tree, esmodule);
	}

	/** sort ESModules */
	public static void sortESModules(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortESModules();
		model.nodeStructureChanged(model.esmodulesNode());
	}

	//
	// ESSources *and* ESModules (ESPreferables)
	//

	/** set Preferable attribute */
	public static void setPreferred(JTree tree, boolean isPreferred) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		IConfiguration config = (IConfiguration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		ESPreferable esp = (ESPreferable) treePath.getLastPathComponent();
		esp.setPreferred(isPreferred);
		// config.setHasChanged(true);
		model.nodeChanged(esp);
		model.updateLevel1Nodes();
	}

	//
	// Services
	//

	/** insert Service */
	public static boolean insertService(JTree tree, String templateName) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		Template template = config.release().serviceTemplate(templateName);
		return insertInstance(tree, template);
	}

	/** import Service */
	public static boolean importService(JTree tree, ServiceInstance external) {
		return importInstance(tree, external);
	}

	/** remove Service */
	public static boolean removeService(JTree tree, ServiceInstance service) {
		return removeNode(tree, service);
	}

	/** sort services */
	public static void sortServices(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortServices();
		model.nodeStructureChanged(model.servicesNode());
	}

	//
	// Paths, Sequences, Tasks
	//

	/** remove all unreferenced sequences */
	public static void removeUnreferencedSequences(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		Object parent = model.sequencesNode();

		ArrayList<Sequence> toBeRemoved = new ArrayList<Sequence>();

		Iterator<Sequence> itSeq = config.sequenceIterator();
		while (itSeq.hasNext()) {
			Sequence sequence = itSeq.next();
			if (sequence.parentPaths().length == 0)
				toBeRemoved.add(sequence);
		}

		Iterator<Sequence> itRmv = toBeRemoved.iterator();
		while (itRmv.hasNext()) {
			Sequence sequence = itRmv.next();
			int index = config.indexOfSequence(sequence);
			config.removeSequence(sequence);
			model.nodeRemoved(parent, index, sequence);
		}
		model.nodeStructureChanged(model.modulesNode());
		model.updateLevel1Nodes();
	}

	/** resolve unnecessary sequences (those referenced only once) */
	public static void resolveUnnecessarySequences(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		ArrayList<Sequence> sequences = new ArrayList<Sequence>();
		Iterator<Sequence> itS = config.sequenceIterator();
		while (itS.hasNext()) {
			Sequence sequence = itS.next();
			if (sequence.referenceCount() == 1)
				sequences.add(sequence);
		}

		itS = sequences.iterator();
		while (itS.hasNext()) {
			Sequence sequence = itS.next();
			Reference reference = sequence.reference(0);
			ReferenceContainer container = reference.container();
			int index = container.indexOfEntry(reference);

			Operator[] operators = new Operator[sequence.entryCount()];
			Referencable[] instances = new Referencable[sequence.entryCount()];
			for (int i = 0; i < sequence.entryCount(); i++) {
				operators[i] = sequence.entry(i).getOperator();
				instances[i] = sequence.entry(i).parent();
			}
			config.removeSequence(sequence);

			for (int i = 0; i < instances.length; i++) {
				if (instances[i] instanceof ModuleInstance) {
					ModuleInstance module = (ModuleInstance) instances[i];
					config.insertModule(config.moduleCount(), module);
					config.insertModuleReference(container, index + i, module).setOperator(operators[i]);
				} else if (instances[i] instanceof Sequence) {
					Sequence seq = (Sequence) instances[i];
					config.insertSequenceReference(container, index + i, seq).setOperator(operators[i]);
				} else if (instances[i] instanceof Task) {
					Task tas = (Task) instances[i];
					config.insertTaskReference(container, index + i, tas).setOperator(operators[i]);
				} else if (instances[i] instanceof SwitchProducer) {
					SwitchProducer sp = (SwitchProducer) instances[i];
					config.insertSwitchProducerReference(container, index + i, sp).setOperator(operators[i]);
				} else if (instances[i] instanceof Path) {
					Path path = (Path) instances[i];
					config.insertPathReference(container, index + i, path).setOperator(operators[i]);
				}
			}
		}

		model.nodeStructureChanged(model.getRoot());
		model.updateLevel1Nodes();
	}

	/** remove all unreferenced tasks */
	public static void removeUnreferencedTasks(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		Object parent = model.tasksNode();

		ArrayList<Task> toBeRemoved = new ArrayList<Task>();

		Iterator<Task> itTas = config.taskIterator();
		while (itTas.hasNext()) {
			Task task = itTas.next();
			if (task.parentPaths().length == 0)
				toBeRemoved.add(task);
		}

		Iterator<Task> itRmv = toBeRemoved.iterator();
		while (itRmv.hasNext()) {
			Task task = itRmv.next();
			int index = config.indexOfTask(task);
			config.removeTask(task);
			model.nodeRemoved(parent, index, task);
		}
		model.nodeStructureChanged(model.modulesNode());
		model.updateLevel1Nodes();
	}

	/** resolve unnecessary tasks (those referenced only once) */
	public static void resolveUnnecessaryTasks(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		ArrayList<Task> tasks = new ArrayList<Task>();
		Iterator<Task> itT = config.taskIterator();
		while (itT.hasNext()) {
			Task task = itT.next();
			if (task.referenceCount() == 1)
				tasks.add(task);
		}

		itT = tasks.iterator();
		while (itT.hasNext()) {
			Task task = itT.next();
			Reference reference = task.reference(0);
			ReferenceContainer container = reference.container();
			int index = container.indexOfEntry(reference);

			Operator[] operators = new Operator[task.entryCount()];
			Referencable[] instances = new Referencable[task.entryCount()];
			for (int i = 0; i < task.entryCount(); i++) {
				operators[i] = task.entry(i).getOperator();
				instances[i] = task.entry(i).parent();
			}
			config.removeTask(task);
			// this is reconnection of task parent container (container), with task child
			// nodes (instances)
			for (int i = 0; i < instances.length; i++) {
				if (instances[i] instanceof ModuleInstance) {
					ModuleInstance module = (ModuleInstance) instances[i];
					config.insertModule(config.moduleCount(), module);
					config.insertModuleReference(container, index + i, module).setOperator(operators[i]);
				} else if (instances[i] instanceof Task) {
					Task tas = (Task) instances[i];
					config.insertTaskReference(container, index + i, tas).setOperator(operators[i]);
				} else if (instances[i] instanceof SwitchProducer) {
					SwitchProducer sp = (SwitchProducer) instances[i];
					config.insertSwitchProducerReference(container, index + i, sp).setOperator(operators[i]);
				} else if (instances[i] instanceof Path) {
					Path path = (Path) instances[i];
					config.insertPathReference(container, index + i, path).setOperator(operators[i]);
				}
			}
		}

		model.nodeStructureChanged(model.getRoot());
		model.updateLevel1Nodes();
	}

	/** remove all unreferenced switch producers */
	public static void removeUnreferencedSwitchProducers(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		Object parent = model.switchProducersNode();

		ArrayList<SwitchProducer> toBeRemoved = new ArrayList<SwitchProducer>();

		Iterator<SwitchProducer> itSP = config.switchProducerIterator();
		while (itSP.hasNext()) {
			SwitchProducer switchProducer = itSP.next();
			if (switchProducer.parentPaths().length == 0)
				toBeRemoved.add(switchProducer);
		}

		Iterator<SwitchProducer> itRmv = toBeRemoved.iterator();
		while (itRmv.hasNext()) {
			SwitchProducer switchProducer = itRmv.next();
			int index = config.indexOfSwitchProducer(switchProducer);
			config.removeSwitchProducer(switchProducer);
			model.nodeRemoved(parent, index, switchProducer);
		}
		model.nodeStructureChanged(model.modulesNode());
		model.updateLevel1Nodes();
	}

	/** resolve unnecessary switch producers (those referenced only once) */
	public static void resolveUnnecessarySwitchProducers(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		ArrayList<SwitchProducer> switchProducers = new ArrayList<SwitchProducer>();
		Iterator<SwitchProducer> itSP = config.switchProducerIterator();
		while (itSP.hasNext()) {
			SwitchProducer switchProducer = itSP.next();
			if (switchProducer.referenceCount() == 1)
				switchProducers.add(switchProducer);
		}

		itSP = switchProducers.iterator();
		while (itSP.hasNext()) {
			SwitchProducer switchProducer = itSP.next();
			Reference reference = switchProducer.reference(0);
			ReferenceContainer container = reference.container();
			int index = container.indexOfEntry(reference);

			Operator[] operators = new Operator[switchProducer.entryCount()];
			Referencable[] instances = new Referencable[switchProducer.entryCount()];
			for (int i = 0; i < switchProducer.entryCount(); i++) {
				operators[i] = switchProducer.entry(i).getOperator();
				instances[i] = switchProducer.entry(i).parent();
			}
			config.removeSwitchProducer(switchProducer);
			// this is reconnection of switch producer parent container (container), with
			// switch producer child
			// nodes (instances)
			for (int i = 0; i < instances.length; i++) {
				if (instances[i] instanceof ModuleInstance) {
					ModuleInstance module = (ModuleInstance) instances[i];
					config.insertModule(config.moduleCount(), module);
					config.insertModuleReference(container, index + i, module).setOperator(operators[i]);
				} else if (instances[i] instanceof EDAliasInstance) {
					EDAliasInstance edAlias = (EDAliasInstance) instances[i];
					config.insertEDAlias(config.edAliasCount(), edAlias);
					// this down should not be possible since only Switch Producer can
					// reference EDAlias
					// config.insertEDAliasReference(container, index + i,
					// edAlias).setOperator(operators[i]);
				}
			}
		}

		model.nodeStructureChanged(model.getRoot());
		model.updateLevel1Nodes();
	}

	public static void setPathType(JTree tree,Path.Type pathType) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		TreePath treePath = tree.getSelectionPath();
		Path path = (Path) treePath.getLastPathComponent();
		path.setType(pathType);		
		model.nodeChanged(path);
	}

	public static void setPathAsStdPath(JTree tree) {
		setPathType(tree,Path.Type.STD);
	}

	public static void setPathAsEndPath(JTree tree) {
		setPathType(tree,Path.Type.END);
	}

	public static void setPathAsFinalPath(JTree tree) {
		setPathType(tree,Path.Type.FINAL);
	}

	public static void setPathAsDatasetPath(JTree tree) {
		setPathType(tree,Path.Type.DATASET);
	}
	

	/** insert a new path */
	public static boolean insertPath(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		Path path = config.insertPath(index, "<ENTER PATH NAME>");

		model.nodeInserted(model.pathsNode(), index);
		model.updateLevel1Nodes();

		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(path);

		tree.setSelectionPath(newTreePath);
		editNodeName(tree);

		return true;
	}

	/** move an existing path within the list of paths */
	public static boolean movePath(JTree tree, Path sourcePath) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int sourceIndex = config.indexOfPath(sourcePath);
		int targetIndex = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		config.movePath(sourcePath, targetIndex);
		model.nodeRemoved(model.pathsNode(), sourceIndex, sourcePath);
		if (sourceIndex < targetIndex)
			targetIndex--;
		model.nodeInserted(model.pathsNode(), targetIndex);
		return true;
	}

	/** insert a new sequence */
	public static boolean insertSequence(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		Sequence sequence = config.insertSequence(index, "<ENTER SEQUENCE NAME>");

		model.nodeInserted(model.sequencesNode(), index);
		model.updateLevel1Nodes();

		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(sequence);

		tree.setSelectionPath(newTreePath);
		editNodeName(tree);

		return true;
	}

	/**
	 * insertSequenceNamed
	 * ----------------------------------------------------------------- Insert a
	 * new sequence using the given name passed by parameter It checks the sequence
	 * name existence and tries different suffixes using underscore + a number from
	 * 0 to 9.
	 */
	private static String insertSequenceNamed(JTree tree, String name) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;
		// To make sure that sequence name doesn't exist:
		String newName = name;
		if (config.sequence(name) != null) {
			for (int j = 0; j < 10; j++) {
				newName = name + "_" + j;
				if (config.sequence(newName) == null) {
					j = 10;
				}
			}
		}

		Sequence sequence = config.insertSequence(index, newName);

		model.nodeInserted(model.sequencesNode(), index);
		model.updateLevel1Nodes();
		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(sequence);
		tree.setSelectionPath(newTreePath);
		return newName;
	}

	/** insert a new task */
	public static boolean insertTask(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		Task task = config.insertTask(index, "<ENTER TASK NAME>");

		model.nodeInserted(model.tasksNode(), index);
		model.updateLevel1Nodes();

		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(task);

		tree.setSelectionPath(newTreePath);
		editNodeName(tree);

		return true;
	}

	/**
	 * insertTaskNamed
	 * ----------------------------------------------------------------- Insert a
	 * new task using the given name passed by parameter It checks the task name
	 * existence and tries different suffixes using underscore + a number from 0 to
	 * 9.
	 */
	private static String insertTaskNamed(JTree tree, String name) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		// okay the original sequence code assumed that decendents of sequences were allways sequences or modules
		// which to be fair was true
		// now with tasks and sps, we need to code the following logic
		// first nomenclature:
		//    getLastPathCompontent() : parent of our new module
		//    getParentPath(): grand parent of our new module, this will 
		//                     be the top level node (eg sequences, tasks)
		// The goal of the index is to find out where to insert the new 
		// reference container. This is index+1 of the parent path in the 
		// top level node when the types are the same
		// this means a deep cloned sequence will right after its original
		// and all deep cloned child sequences will be directly after that 
		// sequence
		// 
		// however when the parent is different to the inserted object, we
		// will put it at last index of the of the new node
	
		int index = 0;
		if( treePath.getPathCount() == 2){
			index = 0;
		}else if(treePath.getParentPath().equals(model.tasksNode())) {
			index = (treePath.getPathCount() == 2) ? 0
					: model.getIndexOfChild(treePath.getParentPath().	getLastPathComponent(),
					treePath.getLastPathComponent()) + 1;	
		}else{
			index = config.taskCount();
		}

		// To make sure that task name doesn't exist:
		String newName = name;
		if (config.task(name) != null) {
			for (int j = 0; j < 10; j++) {
				newName = name + "_" + j;
				if (config.task(newName) == null) {
					j = 10;
				}
			}
		}

		Task task = config.insertTask(index, newName);

		model.nodeInserted(model.tasksNode(), index);
		model.updateLevel1Nodes();
		
		//bug fix: we need to ensure we are using the tasks node
		//TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath parentPath = (index == 0) ? treePath : new TreePath(model.getPathToRoot(model.tasksNode()));
		
		TreePath newTreePath = parentPath.pathByAddingChild(task);
		tree.setSelectionPath(newTreePath);
		return newName;
	}

	/** insert a new switch producer */
	public static boolean insertSwitchProducer(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		SwitchProducer switchProducer = config.insertSwitchProducer(index, "<ENTER SWITCH PRODUCER NAME>");

		model.nodeInserted(model.switchProducersNode(), index);
		model.updateLevel1Nodes();

		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(switchProducer);

		tree.setSelectionPath(newTreePath);
		editNodeName(tree);

		return true;
	}

	/**
	 * insertSwitchProducerNamed
	 * ----------------------------------------------------------------- Insert a
	 * new switch producer using the given name passed by parameter It checks the
	 * switch producer name existence and tries different suffixes using underscore
	 * + a number from 0 to 9.
	 */
	private static String insertSwitchProducerNamed(JTree tree, String name) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		
		// okay the original sequence code assumed that decendents of sequences were allways sequences or modules
		// which to be fair was true
		// now with tasks and sps, we need to code the following logic
		// first nomenclature:
		//    getLastPathCompontent() : parent of our new module
		//    getParentPath(): grand parent of our new module, this will 
		//                     be the top level node (eg sequences, tasks)
		// The goal of the index is to find out where to insert the new 
		// reference container. This is index+1 of the parent path in the 
		// top level node when the types are the same
		// this means a deep cloned sequence will right after its original
		// and all deep cloned child sequences will be directly after that 
		// sequence
		// 
		// however when the parent is different to the inserted object, we
		// will put it at last index of the of the new node
	
		int index = 0;
		if( treePath.getPathCount() == 2){
			index = 0;
		}else if(treePath.getParentPath().equals(model.switchProducersNode())) {
			index = (treePath.getPathCount() == 2) ? 0
					: model.getIndexOfChild(treePath.getParentPath().	getLastPathComponent(),
					treePath.getLastPathComponent()) + 1;
		}else{
			index = config.switchProducerCount();
		}

		// To make sure that switch producer name doesn't exist:
		String newName = name;
		if (config.switchProducer(name) != null) {
			for (int j = 0; j < 10; j++) {
				newName = name + "_" + j;
				if (config.switchProducer(newName) == null) {
					j = 10;
				}
			}
		}

		SwitchProducer switchProducer = config.insertSwitchProducer(index, newName);

		model.nodeInserted(model.switchProducersNode(), index);
		model.updateLevel1Nodes();
		//bug fix: we need to ensure we are using the tasks node
		//TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath parentPath = (index == 0) ? treePath : new TreePath(model.getPathToRoot(model.switchProducersNode()));
		TreePath newTreePath = parentPath.pathByAddingChild(switchProducer);
		tree.setSelectionPath(newTreePath);
		return newName;
	}

	/** insert a new EDAlias */
	public static boolean insertEDAlias(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		TreePath parentTreePath = (depth == 3) ? treePath : treePath.getParentPath();
		ReferenceContainer parent = (ReferenceContainer) parentTreePath.getLastPathComponent();

		int index = parent.entryCount();

		EDAliasInstance edAlias = config.insertEDAlias("<ENTER EDALIAS NAME>");
		Reference reference = null;

		reference = config.insertEDAliasReference(parent, index, edAlias);

		// Inserting in the model and refreshing tree view:
		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();

		TreePath newTreePath = parentTreePath.pathByAddingChild(reference);
		tree.expandPath(newTreePath.getParentPath());
		tree.setSelectionPath(newTreePath);

		// Allow the user to modify the name of the reference
		if (edAlias != null && edAlias.referenceCount() == 1) {
			TreePath edAliasTreePath = new TreePath(model.getPathToRoot((Object) edAlias));
			editNodeName(tree);
		}

		return true;
	}
	
	public static boolean removeGlobalEDAlias(JTree tree, EDAliasInstance globalEDAlias) {
		return removeNode(tree, globalEDAlias);
	}
	
	/** insert a new global EDAlias producer */
	public static boolean insertGlobalEDAlias(JTree tree) {
		if (globalEDAliasAvailability) {
			errorNotificationPanel dialog = new errorNotificationPanel("Error: InsertGlobalEDAlias", "Error GlobalEDAliases are not supported by this database",
			"This database does not have support for GlobalEDAliases (but supports EDAliases in SwitchProducers) at this time");
			dialog.createAndShowGUI();
			return true;
		}
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		int index = config.globalEDAliasCount();

		EDAliasInstance globalEDAlias = config.insertGlobalEDAlias("<ENTER GLOBAL EDALIAS NAME>");

		// Inserting in the model and refreshing tree view:
		model.nodeInserted(model.globalEDAliasesNode(), index);
		model.updateLevel1Nodes();
				
		TreePath parentPath = (treePath.getPathCount() == 2) ? treePath : treePath.getParentPath();
		tree.setSelectionPath(parentPath.pathByAddingChild(globalEDAlias));

		// Allow the user to modify the name of the reference
		if (globalEDAlias != null) {
			editNodeName(tree);
		}

		return true;
	}

	/**
	 * insertEDAliasNamed
	 * ----------------------------------------------------------------- Insert a
	 * new EDAlias using the given name passed by parameter It checks the
	 * EDAlias name existence and tries different suffixes using underscore
	 * + a number from 0 to 9.
	 * @throws DataException 
	 */
	private static String insertEDAliasNamed(JTree tree, String name) throws DataException {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		TreePath parentTreePath = (depth == 3) ? treePath : treePath.getParentPath();

		ReferenceContainer parent = (ReferenceContainer) parentTreePath.getLastPathComponent();

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;
		// To make sure that EDALias name doesn't exist:
		String newName = name;
		if (config.edAlias(name) != null) {
			for (int j = 0; j < 10; j++) {
				newName = name + "_" + j;
				if (config.edAlias(newName) == null) {
					j = 10;
				}
			}
		}

		EDAliasInstance edAlias = config.insertEDAlias(index, newName);

		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();
		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(edAlias);
		tree.setSelectionPath(newTreePath);
		return newName;
	}
	
	/**
	 * insertGlobalEDAliasNamed
	 * ----------------------------------------------------------------- Insert a
	 * new global EDAlias using the given name passed by parameter It checks the
	 * EDAlias name existence and tries different suffixes using underscore
	 * + a number from 0 to 9.
	 * @throws DataException 
	 */
	private static String insertGlobalEDAliasNamed(JTree tree, String name) throws DataException {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		TreePath parentTreePath = (depth == 3) ? treePath : treePath.getParentPath();

		ReferenceContainer parent = (ReferenceContainer) parentTreePath.getLastPathComponent();

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;
		// To make sure that global EDALias name doesn't exist:
		String newName = name;
		if (config.globalEDAlias(name) != null) {
			for (int j = 0; j < 10; j++) {
				newName = name + "_" + j;
				if (config.globalEDAlias(newName) == null) {
					j = 10;
				}
			}
		}

		EDAliasInstance globalEDAlias = config.insertGlobalEDAlias(index, newName);

		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();
		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(globalEDAlias);
		tree.setSelectionPath(newTreePath);
		return newName;
	}

	/**
	 * insertPathNamed
	 * ----------------------------------------------------------------- Insert a
	 * new Path using the given name passed by parameter It checks the path name
	 * existence and tries different suffixes using underscore + a number from 0 to
	 * 9.
	 */
	private static String insertPathNamed(JTree tree, String name) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;
		// To make sure that path name doesn't exist:
		String newName = name;
		if (config.path(name) != null) {
			for (int j = 0; j < 10; j++) {
				newName = name + "_" + j;
				if (config.path(newName) == null) {
					j = 10;
				}
			}
		}

		Path path = config.insertPath(index, newName);

		model.nodeInserted(model.pathsNode(), index);
		model.updateLevel1Nodes();
		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(path);
		tree.setSelectionPath(newTreePath);
		return newName;
	}

	/**
	 * DeepCloneSequence
	 * ----------------------------------------------------------------- Clone a
	 * sequence from source reference container to target reference container. If
	 * the target reference container is null, it creates the root sequence using
	 * the source sequence name + suffix. This method use recursion to clone the
	 * source tree based in the selection path. NOTE: It automatically go across the
	 * entries setting the selection path in different levels. Selection Path is
	 * restored to current level when recursion ends.
	 */
	public static boolean DeepCloneSequence(JTree tree, ReferenceContainer sourceContainer,
			ReferenceContainer targetContainer) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		String targetName = sourceContainer.name() + "_clone";

		if (targetContainer == null) {
			targetName = ConfigurationTreeActions.insertSequenceNamed(tree, targetName); // It has created the
																							// targetContainer
			targetContainer = config.sequence(targetName);
			if (targetContainer == null) {
				System.err.println(
						"[confdb.gui.ConfigurationTreeActions.DeepCloneSequence] ERROR: targetSequence == NULL");
				return false;
			}
		} else
			targetName = targetContainer.name();

		treePath = tree.getSelectionPath(); // need to get selection path again. (after insertSequenceNamed).

		if (targetContainer.entryCount() != 0) {
			System.err.println(
					"[confdb.gui.ConfigurationTreeActions.DeepCloneSequence] ERROR: targetSequence.entryCount != 0 "
							+ targetContainer.name());
		}

		String newName;
		for (int i = 0; i < sourceContainer.entryCount(); i++) {
			Reference entry = sourceContainer.entry(i);
			newName = entry.name() + "_clone";
			if (entry instanceof SequenceReference) {
				SequenceReference sourceRef = (SequenceReference) entry;
				Sequence source = (Sequence) sourceRef.parent();
				newName = ConfigurationTreeActions.insertSequenceNamed(tree, newName); // It has created the
																						// targetContainer
				Object lc = tree.getSelectionPath().getLastPathComponent(); // get from the new selectionPath set in
																			// insertSequenceNamed.
				ConfigurationTreeActions.DeepCloneSequence(tree, source, (ReferenceContainer) lc); // DEEP RECURSION
				Sequence targetSequence = config.sequence(newName);
				config.insertSequenceReference(targetContainer, i, targetSequence).setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof TaskReference) {
				TaskReference sourceRef = (TaskReference) entry;
				Task source = (Task) sourceRef.parent();
				newName = ConfigurationTreeActions.insertTaskNamed(tree, newName); // It has created the
																					// targetContainer
				Object lc = tree.getSelectionPath().getLastPathComponent(); // get from the new selectionPath set in
																			// insertTaskNamed.
				ConfigurationTreeActions.DeepCloneTask(tree, source, (ReferenceContainer) lc); // DEEP RECURSION
				Task targetTask = config.task(newName);
				config.insertTaskReference(targetContainer, i, targetTask).setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof SwitchProducerReference) {
				SwitchProducerReference sourceRef = (SwitchProducerReference) entry;
				SwitchProducer source = (SwitchProducer) sourceRef.parent();
				newName = ConfigurationTreeActions.insertSwitchProducerNamed(tree, newName); // It has created the
				// targetContainer
				Object lc = tree.getSelectionPath().getLastPathComponent(); // get from the new selectionPath set in
																			// insertSwitchProducerNamed.
				ConfigurationTreeActions.DeepCloneSwitchProducer(tree, source, (ReferenceContainer) lc); // DEEP
																											// RECURSION
				SwitchProducer targetSwitchProducer = config.switchProducer(newName);
				config.insertSwitchProducerReference(targetContainer, i, targetSwitchProducer)
						.setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof ModuleReference) {
				ModuleReference sourceRef = (ModuleReference) entry;
				ConfigurationTreeActions.CloneModule(tree, sourceRef, newName);
			} else {
				System.err
						.println("[confdb.gui.ConfigurationTreeActions.DeepCloneSequence] ERROR: reference instanceof "
								+ entry.getClass());
				return false;
			}
			tree.setSelectionPath(treePath); // set the selection path again to this level.
		}

		return true;
	}

	/**
	 * DeepCloneTask
	 * ----------------------------------------------------------------- Clone a
	 * task from source reference container to target reference container. If the
	 * target reference container is null, it creates the root task using the source
	 * task name + suffix. This method use recursion to clone the source tree based
	 * in the selection path. NOTE: It automatically go across the entries setting
	 * the selection path in different levels. Selection Path is restored to current
	 * level when recursion ends.
	 */
	public static boolean DeepCloneTask(JTree tree, ReferenceContainer sourceContainer,
			ReferenceContainer targetContainer) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		String targetName = sourceContainer.name() + "_clone";

		if (targetContainer == null) {
			targetName = ConfigurationTreeActions.insertTaskNamed(tree, targetName); // It has created the
																						// targetContainer
			targetContainer = config.task(targetName);
			if (targetContainer == null) {
				System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneTask] ERROR: targetTask == NULL");
				return false;
			}
		} else
			targetName = targetContainer.name();

		treePath = tree.getSelectionPath(); // need to get selection path again. (after insertTaskNamed).

		if (targetContainer.entryCount() != 0) {
			System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneTask] ERROR: targetTask.entryCount != 0 "
					+ targetContainer.name());
		}

		String newName;
		for (int i = 0; i < sourceContainer.entryCount(); i++) {
			Reference entry = sourceContainer.entry(i);
			newName = entry.name() + "_clone";
			if (entry instanceof TaskReference) {
				TaskReference sourceRef = (TaskReference) entry;
				Task source = (Task) sourceRef.parent();
				newName = ConfigurationTreeActions.insertTaskNamed(tree, newName); // It has created the
																					// targetContainer
				Object lc = tree.getSelectionPath().getLastPathComponent(); // get from the new selectionPath set in
																			// insertTaskNamed.
				ConfigurationTreeActions.DeepCloneTask(tree, source, (ReferenceContainer) lc); // DEEP RECURSION
				Task targetTask = config.task(newName);
				config.insertTaskReference(targetContainer, i, targetTask).setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof SwitchProducerReference) {
				SwitchProducerReference sourceRef = (SwitchProducerReference) entry;
				SwitchProducer source = (SwitchProducer) sourceRef.parent();
				newName = ConfigurationTreeActions.insertSwitchProducerNamed(tree, newName); // It has created the
				// targetContainer
				Object lc = tree.getSelectionPath().getLastPathComponent(); // get from the new selectionPath set in
																			// insertSwitchProducerNamed.
				ConfigurationTreeActions.DeepCloneSwitchProducer(tree, source, (ReferenceContainer) lc); // DEEP
																											// RECURSION
				SwitchProducer targetSwitchProducer = config.switchProducer(newName);
				config.insertSwitchProducerReference(targetContainer, i, targetSwitchProducer)
						.setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof ModuleReference) {
				ModuleReference sourceRef = (ModuleReference) entry;
				ConfigurationTreeActions.CloneModule(tree, sourceRef, newName);
			} else {
				System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneTask] ERROR: reference instanceof "
						+ entry.getClass());
				return false;
			}
			tree.setSelectionPath(treePath); // set the selection path again to this level.
		}

		return true;
	}

	/**
	 * DeepCloneSwitchProducer
	 * ----------------------------------------------------------------- Clone a
	 * switch producer from source reference container to target reference
	 * container. If the target reference container is null, it creates the root
	 * switch produce using the source switch producer name + suffix. This method
	 * use recursion to clone the source tree based in the selection path. NOTE: It
	 * automatically go across the entries setting the selection path in different
	 * levels. Selection Path is restored to current level when recursion ends.
	 */
	public static boolean DeepCloneSwitchProducer(JTree tree, ReferenceContainer sourceContainer,
			ReferenceContainer targetContainer) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		String targetName = sourceContainer.name() + "_clone";

		if (targetContainer == null) {
			targetName = ConfigurationTreeActions.insertSwitchProducerNamed(tree, targetName); // It has created the
			// targetContainer
			targetContainer = config.switchProducer(targetName);
			if (targetContainer == null) {
				System.err.println(
						"[confdb.gui.ConfigurationTreeActions.DeepCloneSwitchProducer] ERROR: targetSwitchProducer == NULL");
				return false;
			}
		} else
			targetName = targetContainer.name();

		treePath = tree.getSelectionPath(); // need to get selection path again. (after insertSwitchProducerNamed).

		if (targetContainer.entryCount() != 0) {
			System.err.println(
					"[confdb.gui.ConfigurationTreeActions.DeepCloneSwitchProducer] ERROR: targetSwitchProducer.entryCount != 0 "
							+ targetContainer.name());
		}

		String newName;
		for (int i = 0; i < sourceContainer.entryCount(); i++) { // technically only 2 entries are possible here
			Reference entry = sourceContainer.entry(i);
			
			newName = entry.name().replace(((SwitchProducer)sourceContainer).modulePrefix(),"");
			newName = targetName+SwitchProducer.nameSeperator()+newName;
			if (entry instanceof ModuleReference) {
				ModuleReference sourceRef = (ModuleReference) entry;
				ConfigurationTreeActions.CloneModule(tree, sourceRef, newName);
			} else if (entry instanceof EDAliasReference) {
				EDAliasReference sourceRef = (EDAliasReference) entry;
				ConfigurationTreeActions.CloneEDAlias(tree, sourceRef, newName);
			} else {
				System.err.println(
						"[confdb.gui.ConfigurationTreeActions.DeepCloneSwitchProducer] ERROR: reference instanceof "
								+ entry.getClass());
				return false;
			}
			tree.setSelectionPath(treePath); // set the selection path again to this level.
		}

		return true;
	}

	/**
	 * DeepCloneContainer
	 * -----------------------------------------------------------------
	 * DeepCloneContainer clones a Path container. Generates a full copy of the
	 * selected path also creating clones of modules, nested sequences, tasks and
	 * switch producers. This method uses recursion to also take into account the
	 * sub-paths.
	 * 
	 */
	public static boolean DeepCloneContainer(JTree tree, ReferenceContainer sourceContainer,
			ReferenceContainer targetContainer) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		String targetName = sourceContainer.name() + "_clone";

		if (targetContainer == null) {
			if (sourceContainer instanceof Path) {
				targetName = ConfigurationTreeActions.insertPathNamed(tree, targetName); // It has created the
																							// targetContainer
				targetContainer = config.path(targetName);

				Path sourcePath = config.path(sourceContainer.name());
				((Path) targetContainer).setFields(sourcePath);
			} else
				System.err.println(
						"[confdb.gui.ConfigurationTreeActions.DeepCloneContainer] ERROR: sourceContainer NOT instanceof Path");

			if (targetContainer == null) {
				System.err.println(
						"[confdb.gui.ConfigurationTreeActions.DeepCloneContainer] ERROR: targetSequence == NULL");
				return false;
			}
		} else
			targetName = targetContainer.name();

		treePath = tree.getSelectionPath(); // need to get selection path again. (after insertSequenceNamed).

		if (targetContainer.entryCount() != 0) {
			System.err.println(
					"[confdb.gui.ConfigurationTreeActions.DeepCloneContainer] ERROR: targetContainer.entryCount != 0 "
							+ targetContainer.name());
		}

		for (int i = 0; i < sourceContainer.entryCount(); i++) {
			Reference entry = sourceContainer.entry(i);

			if (entry instanceof SequenceReference) {
				// Sets selection path to sequenceNode:
				tree.setSelectionPath(new TreePath(model.getPathToRoot(model.sequencesNode())));

				SequenceReference sourceRef = (SequenceReference) entry;
				Sequence source = (Sequence) sourceRef.parent();
				ConfigurationTreeActions.DeepCloneSequence(tree, source, null);

				// Getting the cloned sequence using the selection path:
				Sequence clonedSequence = (Sequence) tree.getLastSelectedPathComponent();

				config.insertSequenceReference(targetContainer, i, clonedSequence).setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);

				// sets the selection path back to pathNode:
				tree.setSelectionPath(treePath);

			} else if (entry instanceof TaskReference) {
				// Sets selection path to taskNode:
				tree.setSelectionPath(new TreePath(model.getPathToRoot(model.tasksNode())));

				TaskReference sourceRef = (TaskReference) entry;
				Task source = (Task) sourceRef.parent();
				ConfigurationTreeActions.DeepCloneTask(tree, source, null);

				// Getting the cloned task using the selection path:
				Task clonedTask = (Task) tree.getLastSelectedPathComponent();

				config.insertTaskReference(targetContainer, i, clonedTask).setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);

				// sets the selection path back to pathNode:
				tree.setSelectionPath(treePath);

			} else if (entry instanceof SwitchProducerReference) {
				// Sets selection path to switchProducerNode:
				tree.setSelectionPath(new TreePath(model.getPathToRoot(model.switchProducersNode())));

				SwitchProducerReference sourceRef = (SwitchProducerReference) entry;
				SwitchProducer source = (SwitchProducer) sourceRef.parent();
				ConfigurationTreeActions.DeepCloneSwitchProducer(tree, source, null);

				// Getting the cloned switch producer using the selection path:
				SwitchProducer clonedSwitchProducer = (SwitchProducer) tree.getLastSelectedPathComponent();

				config.insertSwitchProducerReference(targetContainer, i, clonedSwitchProducer)
						.setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);

				// sets the selection path back to pathNode:
				tree.setSelectionPath(treePath);

			} else if (entry instanceof ModuleReference) {
				String newName = entry.name() + "_clone";
				ModuleReference sourceRef = (ModuleReference) entry;
				ConfigurationTreeActions.CloneModule(tree, sourceRef, newName);

			} else if (entry instanceof EDAliasReference) {
				String newName = entry.name() + "_clone";
				EDAliasReference sourceRef = (EDAliasReference) entry;
				ConfigurationTreeActions.CloneEDAlias(tree, sourceRef, newName);

			} else if (entry instanceof PathReference) {
				String newName = entry.name() + "_clone";
				newName = ConfigurationTreeActions.insertPathNamed(tree, newName); // It has created the targetContainer
				Path targetPath = config.path(newName);
				Path sourcePath = config.path(entry.name());
				targetPath.setFields(sourcePath);

				// Clone subpath:
				ConfigurationTreeActions.DeepCloneContainer(tree, sourcePath, targetPath);

				// and now insert the reference
				config.insertPathReference(targetContainer, i, targetPath).setOperator(entry.getOperator());
				model.nodeInserted(targetContainer, i);

			} else {
				System.err.println("[confdb.gui.ConfigurationTreeActions.DeepCloneContainer] Error instanceof ?");
			}

			tree.setSelectionPath(treePath); // set the selection path again to this level.
		}

		return true;
	}

	/**
	 * CloneReferenceContainer
	 * ----------------------------------------------------------------- It will
	 * perform clones of sequences or paths. This is also called "Simple Clone" or
	 * "shallow clone". It will only create a new top level sequence/path containing
	 * references to original modules and sequences of the source one.
	 */
	public static boolean CloneReferenceContainer(JTree tree, ReferenceContainer sourceContainer) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		String targetName = "Copy_of_" + sourceContainer.name();
		ReferenceContainer targetContainer = null;

		if (sourceContainer instanceof Path) {
			targetName = ConfigurationTreeActions.insertPathNamed(tree, targetName); // It has created the
																						// targetContainer
			targetContainer = config.path(targetName);

			Path sourcePath = config.path(sourceContainer.name());
			((Path) targetContainer).setFields(sourcePath);
		} else if (sourceContainer instanceof Sequence) {
			targetName = ConfigurationTreeActions.insertSequenceNamed(tree, targetName); // It has created the
																							// targetContainer
			targetContainer = config.sequence(targetName);
		} else if (sourceContainer instanceof Task) {
			targetName = ConfigurationTreeActions.insertTaskNamed(tree, targetName); // It has created the
																						// targetContainer
			targetContainer = config.task(targetName);
		} else if (sourceContainer instanceof SwitchProducer) {
			targetName = ConfigurationTreeActions.insertSwitchProducerNamed(tree, targetName); // It has created the
			// targetContainer
			targetContainer = config.switchProducer(targetName);
		}

		else {
			System.err.println(
					"[confdb.gui.ConfigurationTreeActions.CloneReferenceContainer] ERROR: sourceContainer NOT instanceof Path");
			return false;
		}

		if (targetContainer == null) {
			System.err.println(
					"[confdb.gui.ConfigurationTreeActions.CloneReferenceContainer] ERROR: targetSequence == NULL");
			return false;
		}

		// treePath = tree.getSelectionPath(); // need to get selection path again.
		// (after insertSequenceNamed).

		if (targetContainer.entryCount() != 0) {
			System.err.println(
					"[confdb.gui.ConfigurationTreeActions.CloneReferenceContainer] ERROR: targetContainer.entryCount != 0 "
							+ targetContainer.name());
		}

		for (int i = 0; i < sourceContainer.entryCount(); i++) {
			Reference entry = sourceContainer.entry(i);

			if (entry instanceof SequenceReference) {
				SequenceReference sourceRef = (SequenceReference) entry;
				Sequence source = (Sequence) sourceRef.parent();
				config.insertSequenceReference(targetContainer, i, source).setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof TaskReference) {
				TaskReference sourceRef = (TaskReference) entry;
				Task source = (Task) sourceRef.parent();
				config.insertTaskReference(targetContainer, i, source).setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof SwitchProducerReference) {
				SwitchProducerReference sourceRef = (SwitchProducerReference) entry;
				SwitchProducer source = (SwitchProducer) sourceRef.parent();
																				
				config.insertSwitchProducerReference(targetContainer, i, source).setOperator(sourceRef.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof ModuleReference) {
				ModuleInstance module = config.module(entry.name());
				//prescale modules are unique to a path and thus must be cloned
				if(module.template().name().equals("HLTPrescaler")){
					String newName = Path.hltPrescalerLabel(targetName);
					ConfigurationTreeActions.CloneModule(tree, (ModuleReference) entry, newName);	
				}else{
					config.insertModuleReference(targetContainer, i, module).setOperator(entry.getOperator());
					model.nodeInserted(targetContainer, i);
				}
			} else if (entry instanceof EDAliasReference) {
				EDAliasInstance edAlias = config.edAlias(entry.name());
				config.insertEDAliasReference(targetContainer, i, edAlias).setOperator(entry.getOperator());
				model.nodeInserted(targetContainer, i);
			} else if (entry instanceof PathReference) {
				Path sourcePath = config.path(entry.name());
				config.insertPathReference(targetContainer, i, sourcePath).setOperator(entry.getOperator());
				model.nodeInserted(targetContainer, i);
			} else {
				System.err.println("[confdb.gui.ConfigurationTreeActions.CloneReferenceContainer] Error: instanceof ?");
			}

			// tree.setSelectionPath(treePath); // set the selection path again to this
			// level.
		}

		return true;
	}

	/** move an existing sequence within the list of sequences */
	public static boolean moveSequence(JTree tree, Sequence sourceSequence) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int sourceIndex = config.indexOfSequence(sourceSequence);
		int targetIndex = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		config.moveSequence(sourceSequence, targetIndex);
		model.nodeRemoved(model.sequencesNode(), sourceIndex, sourceSequence);
		if (sourceIndex < targetIndex)
			targetIndex--;
		model.nodeInserted(model.sequencesNode(), targetIndex);
		return true;
	}

	/** move an existing task within the list of tasks */
	public static boolean moveTask(JTree tree, Task sourceTask) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int sourceIndex = config.indexOfTask(sourceTask);
		int targetIndex = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		config.moveTask(sourceTask, targetIndex);
		model.nodeRemoved(model.tasksNode(), sourceIndex, sourceTask);
		if (sourceIndex < targetIndex)
			targetIndex--;
		model.nodeInserted(model.tasksNode(), targetIndex);
		return true;
	}

	/**
	 * move an existing switch producer within the list of switch producers 
	 */
	public static boolean moveSwitchProducer(JTree tree, SwitchProducer sourceSwitchProducer) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int sourceIndex = config.indexOfSwitchProducer(sourceSwitchProducer);
		int targetIndex = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		config.moveSwitchProducer(sourceSwitchProducer, targetIndex);
		model.nodeRemoved(model.switchProducersNode(), sourceIndex, sourceSwitchProducer);
		if (sourceIndex < targetIndex)
			targetIndex--;
		model.nodeInserted(model.switchProducersNode(), targetIndex);
		return true;
	}

	/**
	 * Import all references from a container (paths, sequences or tasks). The
	 * import operation is performed by a worker and showing a progress bar.
	 * 
	 */
	public static boolean importAllReferenceContainers(JTree tree, JTree sourceTree, Object external) {
		ConfigurationTreeModel sm = (ConfigurationTreeModel) sourceTree.getModel();
		ConfigurationTreeModel tm = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) tm.getRoot();

		if (sm.getChildCount(external) == 0)
			return false;

		// Check existing items:
		boolean existance = false;
		for (int i = 0; i < sm.getChildCount(external); i++) {
			ReferenceContainer container = (ReferenceContainer) sm.getChild(external, i);
			ReferenceContainer targetContainer = null;
			if (container instanceof Path) {
				targetContainer = config.path(container.name());
			} else if (container instanceof Sequence) {
				targetContainer = config.sequence(container.name());
			} else if (container instanceof Task) {
				targetContainer = config.task(container.name());
			} else if (container instanceof SwitchProducer) {
				targetContainer = config.switchProducer(container.name());
			}
			if (targetContainer != null) {
				existance = true;
				break;
			}
		}

		boolean updateAll = false;
		if (existance) {
			int choice = JOptionPane.showConfirmDialog(null,
					" Some Items may already exist. " + "Do you want to overwrite them All?", "Overwrite all",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			updateAll = (choice == JOptionPane.YES_OPTION);
		}

		ImportAllReferencesThread worker = new ImportAllReferencesThread(tree, sourceTree, external, updateAll);
		WorkerProgressBar progressBar = new WorkerProgressBar("Importing all references", worker);
		progressBar.createAndShowGUI(); // Run the worker and show the progress bar.

		return true;
	}

	/** import Path / Sequence / Task */
	public static boolean importReferenceContainer(JTree tree, ReferenceContainer external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int count = 0;
		if (external instanceof Path)
			count = config.pathCount();
		else if (external instanceof Sequence)
			count = config.sequenceCount();
		else if (external instanceof Task)
			count = config.taskCount();
		else if (external instanceof SwitchProducer)
			count = config.switchProducerCount();

		int index = (treePath == null) ? count
				: (treePath.getPathCount() == 2) ? 0
						: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
								treePath.getLastPathComponent()) + 1;

		ReferenceContainer container = null;
		Object parent = null;
		String type = null;

		if (external instanceof Path) {
			container = config.path(external.name());
			parent = model.pathsNode();
			type = "path";
		} else if (external instanceof Sequence) {
			container = config.sequence(external.name());
			parent = model.sequencesNode();
			type = "sequence";
		} else if (external instanceof Task) {
			container = config.task(external.name());
			parent = model.tasksNode();
			type = "task";
		} else if (external instanceof SwitchProducer) {
			container = config.switchProducer(external.name());
			parent = model.switchProducersNode();
			type = "switchproducer";
		}

		boolean update = false;
		if (container != null) {

			if (type.equals("path"))
				index = config.indexOfPath((Path) container);
			else if (type.equals("sequence"))
				index = config.indexOfSequence((Sequence) container);
			else if (type.equals("task"))
				index = config.indexOfTask((Task) container);
			else if (type.equals("switchproducer"))
				index = config.indexOfSwitchProducer((SwitchProducer) container);

			int choice = JOptionPane.showConfirmDialog(null,
					"The " + type + " '" + container.name() + "' exists, " + "do you want to overwrite it?",
					"Overwrite " + type, JOptionPane.OK_CANCEL_OPTION);
			if (choice == JOptionPane.CANCEL_OPTION)
				return false;

			update = true;

			while (container.entryCount() > 0) {
				Reference entry = (Reference) container.entry(0);
				tree.setSelectionPath(new TreePath(model.getPathToRoot(entry)));
				removeReference(tree);
			}

			if (type.equals("path"))
				((Path) container).setFields((Path) external);
		} else {
			if (!config.hasUniqueQualifier(external))
				return false;
			if (type.equals("path")) {
				container = config.insertPath(index, external.name());
				((Path) container).setFields((Path) external);
			} else if (type.equals("sequence")) {
				container = config.insertSequence(index, external.name());
			} else if (type.equals("task")) {
				container = config.insertTask(index, external.name());
			} else if (type.equals("switchproducer")) {
				container = config.insertSwitchProducer(index, external.name());
			}

		}

		// Force update the tree to avoid ArrayIndexOutOfBoundsException bug76145
		if (update)
			model.nodeChanged(container);
		else
			model.nodeInserted(parent, index);

		if (importContainerEntries(config, model, external, container))
			container.setDatabaseId(external.databaseId());

		model.nodeChanged(container);
		model.updateLevel1Nodes();

		Diff diff = new Diff(external.config(), config);
		String search = type + ":" + container.name();
		diff.compare(search);
		if (!diff.isIdentical()) {
			DiffDialog dlg = new DiffDialog(diff);
			dlg.pack();
			dlg.setVisible(true);
		}

		// PS 31/01/2011: fixes bug reported by Andrea B.
		for (int i = 0; i < container.referenceCount(); i++) {
			Reference reference = container.reference(i);
			ReferenceContainer parentContainer = reference.container();
			parentContainer.setHasChanged();
		}

		return true;
	}

	/**
	 * import Path / Sequence / Task / Switch producer DeepImportReferenceContainer
	 * Import Paths, Sequences, Tasks and Switch producer containers giving as
	 * result identical containers in source and target configuration. NOTE: The
	 * implementation of this method has change to support filtering. It uses the
	 * source configuration instead of the source JTree.
	 * 
	 */
	public static boolean DeepImportReferenceContainer(JTree tree, Configuration sourceConfig,
			ReferenceContainer external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel(); // Target Model.
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int count = 0;
		if (external instanceof Path)
			count = config.pathCount();
		else if (external instanceof Sequence)
			count = config.sequenceCount();
		else if (external instanceof Task)
			count = config.taskCount();
		else if (external instanceof SwitchProducer)
			count = config.switchProducerCount();

		int index = (treePath == null) ? count
				: (treePath.getPathCount() == 2) ? 0
						: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
								treePath.getLastPathComponent()) + 1;

		ReferenceContainer container = null;
		Object parent = null;
		String type = null;
		Diff diff = null;

		// prepare to make a diff.
		Configuration importTestConfig = getConfigurationCopy(config);
		if (external instanceof Path) {
			container = importTestConfig.path(external.name());
			type = "path";
		} else if (external instanceof Sequence) {
			container = importTestConfig.sequence(external.name());
			type = "sequence";
		} else if (external instanceof Task) {
			container = importTestConfig.task(external.name());
			type = "task";
		} else if (external instanceof SwitchProducer) {
			container = importTestConfig.switchProducer(external.name());
			type = "switchproducer";
		}
		if (container == null) { // if root container doesn't exist:
			if (!importTestConfig.hasUniqueQualifier(external))
				System.out.println("[DeepImportReferenceContainer] !importTestConfig.hasUniqueQualifier(external)!");
			if (type.equals("path")) {
				container = importTestConfig.insertPath(index, external.name());
				((Path) container).setFields((Path) external);
			} else if (type.equals("sequence")) {
				container = importTestConfig.insertSequence(index, external.name());
			} else if (type.equals("task")) {
				container = importTestConfig.insertTask(index, external.name());
			} else if (type.equals("switchproducer")) {
				container = importTestConfig.insertSwitchProducer(index, external.name());
			}
		}

		DeepImportContainerEntries(importTestConfig, sourceConfig, null, external, container);
		diff = new Diff(config, importTestConfig);

		// Instead of comparing the configuration as usually, we make use of two new
		// comparing
		// methods which doesn't take Streams into account. This is because the
		// temporary copy
		// created by "getConfigurationCopy" does not create a full functional copy of
		// the target
		// configuration.
		diff.compareModules();
		diff.compareEDAliases();
		diff.comparePathsIgnoreStreams(); // Ignore Streams.
		diff.compareTasksIgnoreStreams(); // Ignore Streams.
		diff.compareSequencesIgnoreStreams(); // Ignore Streams.
		diff.compareSwitchProducersIgnoreStreams(); // Ignore Streams.

		String message = "You are about to add, delete or order multiple items! \n";
		message += "These operations could adversely affect many parts of the configuration.\n";
		message += "Please check the differences and make sure you want to do this.\n";

		boolean accept = false;

		if (!diff.isIdentical()) {
			DeepImportDiffDialog dlg = new DeepImportDiffDialog(diff, message);
			dlg.pack();
			dlg.setVisible(true);
			accept = dlg.getResults();
		}

		// In case user cancel the operation:
		if (!accept)
			return false;
		// IF ACCEPT THEN DO IT

		if (external instanceof Path) {
			container = config.path(external.name());
			parent = model.pathsNode();
			type = "path";
		} else if (external instanceof Sequence) {
			container = config.sequence(external.name());
			parent = model.sequencesNode();
			type = "sequence";
		} else if (external instanceof Task) {
			container = config.task(external.name());
			parent = model.tasksNode();
			type = "task";
		} else if (external instanceof SwitchProducer) {
			container = config.switchProducer(external.name());
			parent = model.switchProducersNode();
			type = "switchproducer";
		}

		if (container == null) { // if root container doesn't exist:								
			if (!config.hasUniqueQualifier(external))
				return false;
			if (type.equals("path")) {
				container = config.insertPath(index, external.name());
				((Path) container).setFields((Path) external);
			} else if (type.equals("sequence")) {
				container = config.insertSequence(index, external.name());
			} else if (type.equals("task")) {
				container = config.insertTask(index, external.name());
			} else if (type.equals("switchproducer")) {
				container = config.insertSwitchProducer(index, external.name());
			}

			model.nodeInserted(parent, index); // Force update bug76145
		}

		// This does the rest of the work:
		if (DeepImportContainerEntries(config, sourceConfig, tree, external, container))
			container.setDatabaseId(external.databaseId());

		model.nodeChanged(container);
		model.updateLevel1Nodes();

		diff = new Diff(external.config(), config);
		String search = type + ":" + container.name();
		diff.compare(search);
		if (!diff.isIdentical()) {
			DiffDialog dlg = new DiffDialog(diff);
			dlg.pack();
			dlg.setVisible(true);
		}

		for (int i = 0; i < container.referenceCount(); i++) {
			Reference reference = container.reference(i);
			ReferenceContainer parentContainer = reference.container();
			parentContainer.setHasChanged();
		}

		return true;
	}

	/**
	 * getConfigurationCopy
	 * ------------------------------------------------------------------------ This
	 * method return a partial copy of the given configuration. The only purpose is
	 * to dispose of a copy to simulate changes in a configuration allowing to check
	 * the differences between the copy and the original config. It was originally
	 * designed to be used by DeepImportContainerEntries and
	 * DeepImportContainerEntriesSimulation.
	 * ------------------------------------------------------------------------
	 * NOTE: DO NOT USE this method to create functional copies of a configuration.
	 */
	private static Configuration getConfigurationCopy(Configuration sourceConf) {
		Configuration configurationCopy = new Configuration();

		// Configuration needs to be initialized with a new software release.
		// A new software release allows to insert modules from scratch.
		configurationCopy.initialize(new ConfigInfo("", null, sourceConf.releaseTag()),
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
				NewModule.updateParameter(p.name(), p.type(), p.valueAsString());
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
				Newedsource.updateParameter(p.name(), p.type(), p.valueAsString());
			}
		}

		// COPY ESSource:
		Iterator<ESSourceInstance> ESSit = sourceConf.essourceIterator();
		index = 0;
		while (ESSit.hasNext()) {
			ESSourceInstance essource = ESSit.next();
			ESSourceInstance Newessource = configurationCopy.insertESSource(index, essource.template().name(),
					essource.name());
			index++;
			Iterator<Parameter> itP = essource.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Newessource.updateParameter(p.name(), p.type(), p.valueAsString());
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
				NewService.updateParameter(p.name(), p.type(), p.valueAsString());
			}
		}

		// COPY DATASETS:
		Iterator<PSetParameter> dataIt = sourceConf.psetIterator();
		index = 0;
		while (dataIt.hasNext()) {
			PSetParameter data = dataIt.next();
			PSetParameter datacheck = configurationCopy.pset(data.name());
			if (datacheck == null) {
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
			while (StrIterator.hasNext()) {
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
			if (newSequ == null) {
				newSequ = configurationCopy.insertSequence(index, sequ.name());
				importContainerEntries(configurationCopy, null, sequ, newSequ);
			}
			index++;
		}

		// COPY Task:
		Iterator<Task> Tasit = sourceConf.taskIterator();
		index = 0;
		while (Tasit.hasNext()) {
			Task task = Tasit.next();
			Task newTask = configurationCopy.task(task.name());
			if (newTask == null) {
				newTask = configurationCopy.insertTask(index, task.name());
				importContainerEntries(configurationCopy, null, task, newTask);
			}
			index++;
		}

		// COPY Switch producers:
		Iterator<SwitchProducer> SPit = sourceConf.switchProducerIterator();
		index = 0;
		while (SPit.hasNext()) {
			SwitchProducer switchProducer = SPit.next();
			SwitchProducer newSwitchProducer = configurationCopy.switchProducer(switchProducer.name());
			if (newSwitchProducer == null) {
				newSwitchProducer = configurationCopy.insertSwitchProducer(index, switchProducer.name());
				importContainerEntries(configurationCopy, null, switchProducer, newSwitchProducer);
			}
			index++;
		}

		// COPY PATHS:
		Iterator<Path> pathit = sourceConf.pathIterator();
		index = 0;
		while (pathit.hasNext()) {
			Path path = pathit.next();
			Path pathCheck = configurationCopy.path(path.name());
			if (pathCheck == null) {
				Path newPath = configurationCopy.insertPath(index, path.name());
				newPath.setFields(path);
				importContainerEntries(configurationCopy, null, path, newPath);
			}
			index++;
		}

		// It doesn't really COPY:
		// - EVENTCONTENT, OUTPUTMODULES, STREAMS AND DATASETS.

		return configurationCopy;
	}

	/**
	 * import Path / Sequence / Task / Switch producer. Perform updates and
	 * insertions of new references into a target configuration. NOTE: Nodes are not
	 * updated in the Tree model.
	 */
	public static boolean importReferenceContainersNoModel(JTree tree, ReferenceContainer external, boolean update) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		int index = 0;
		if (external instanceof Path)
			index = config.pathCount();
		else if (external instanceof Sequence)
			index = config.sequenceCount();
		else if (external instanceof Task)
			index = config.taskCount();
		else if (external instanceof SwitchProducer)
			index = config.switchProducerCount();

		ReferenceContainer container = null;
		String type = null;

		if (external instanceof Path) {
			container = config.path(external.name());
			type = "path";
		} else if (external instanceof Sequence) {
			container = config.sequence(external.name());
			type = "sequence";
		} else if (external instanceof Task) {
			container = config.task(external.name());
			type = "task";
		} else if (external instanceof SwitchProducer) {
			container = config.switchProducer(external.name());
			type = "switchproducer";
		}

		if (container != null) {
			if (type.equals("path"))
				index = config.indexOfPath((Path) container);
			else if (type.equals("sequence"))
				index = config.indexOfSequence((Sequence) container);
			else if (type.equals("task"))
				index = config.indexOfTask((Task) container);
			else if (type.equals("switchproducer"))
				index = config.indexOfSwitchProducer((SwitchProducer) container);

			if (update) {
				while (container.entryCount() > 0) {
					Reference entry = (Reference) container.entry(0);
					removeReference(config, null, entry);
				}
				if (type.equals("path"))
					((Path) container).setFields((Path) external);
			} else
				return false;
		} else {
			if (!config.hasUniqueQualifier(external))
				return false;
			if (type.equals("path")) {
				container = config.insertPath(index, external.name());
				((Path) container).setFields((Path) external);
			} else if (type.equals("sequence")) {
				container = config.insertSequence(index, external.name());
			} else if (type.equals("task")) {
				container = config.insertTask(index, external.name());
			} else if (type.equals("switchproducer")) {
				container = config.insertSwitchProducer(index, external.name());
			}
		}

		if (importContainerEntries(config, null, external, container))
			container.setDatabaseId(external.databaseId());

		// PS 31/01/2011: fixes bug reported by Andrea B.
		for (int i = 0; i < container.referenceCount(); i++) {
			Reference reference = container.reference(i);
			ReferenceContainer parentContainer = reference.container();
			parentContainer.setHasChanged();
		}

		return true;
	}

	/**
	 * insert entries of an external reference container into the local copy
	 */
	private static boolean importContainerEntries(Configuration config, ConfigurationTreeModel treeModel,
			ReferenceContainer sourceContainer, ReferenceContainer targetContainer) {
		boolean updateModel = (treeModel != null);
		// result=true: import all daughters unchanged
		boolean result = true;

		for (int i = 0; i < sourceContainer.entryCount(); i++) {
			Reference entry = sourceContainer.entry(i);

			if (entry instanceof ModuleReference) {
				ModuleReference sourceRef = (ModuleReference) entry;
				ModuleInstance source = (ModuleInstance) sourceRef.parent();
				ModuleInstance target = config.module(source.name());
				ModuleReference targetRef = null;
				if (target != null) {
					targetRef = config.insertModuleReference(targetContainer, i, target);
					result = false;
				} else {
					targetRef = config.insertModuleReference(targetContainer, i, source.template().name(),
							source.name());
					target = (ModuleInstance) targetRef.parent();
					for (int j = 0; j < target.parameterCount(); j++)
						target.updateParameter(j, source.parameter(j).valueAsString());
					target.setDatabaseId(source.databaseId());
				}
				targetRef.setOperator(sourceRef.getOperator());

				if (updateModel) {
					treeModel.nodeInserted(targetContainer, i);
					if (target.referenceCount() == 1)
						treeModel.nodeInserted(treeModel.modulesNode(), config.moduleCount() - 1);
				}

			} else if (entry instanceof EDAliasReference) {
				EDAliasReference sourceRef = (EDAliasReference) entry;
				EDAliasInstance source = (EDAliasInstance) sourceRef.parent();
				EDAliasInstance target = config.edAlias(source.name());
				EDAliasReference targetRef = null;
				if (target != null) {
					targetRef = config.insertEDAliasReference(targetContainer, i, target);
					result = false;
				} else {
					targetRef = config.insertEDAliasReference(targetContainer, i, source.name());
					target = (EDAliasInstance) targetRef.parent();
					for (int j = 0; j < source.parameterCount(); j++)
						target.updateParameter(source.parameter(j));
					target.setDatabaseId(source.databaseId());
				}
				targetRef.setOperator(sourceRef.getOperator());

				if (updateModel) {
					treeModel.nodeInserted(targetContainer, i);
					/*
					 * if (target.referenceCount() == 1)
					 * treeModel.nodeInserted(treeModel.modulesNode(), config.moduleCount() - 1);
					 */
				}

			} else if (entry instanceof OutputModuleReference) {
				OutputModuleReference sourceRef = (OutputModuleReference) entry;
				OutputModule source = (OutputModule) sourceRef.parent();
				OutputModule target = config.output(source.name());
				OutputModuleReference targetRef = null;
				if (target != null) {
					targetRef = config.insertOutputModuleReference(targetContainer, i, target);
					result = false;
				} else {
					System.out.println("OutputModules must already exist as they are imported via stream import!");
					return result;
				}
				targetRef.setOperator(sourceRef.getOperator());

				if (updateModel) {
					treeModel.nodeInserted(targetContainer, i);
					if (target.referenceCount() == 1)
						treeModel.nodeInserted(treeModel.outputsNode(), config.outputCount() - 1);
				}

			} else if (entry instanceof PathReference) {
				PathReference sourceRef = (PathReference) entry;
				Path source = (Path) sourceRef.parent();
				Path target = config.path(source.name());
				if (target != null) {
					config.insertPathReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					result = false;
				} else {
					target = config.insertPath(config.pathCount(), sourceRef.name());
					if (updateModel)
						treeModel.nodeInserted(treeModel.pathsNode(), config.pathCount() - 1);
					config.insertPathReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					boolean tmp = importContainerEntries(config, treeModel, source, target);
					if (tmp)
						target.setDatabaseId(source.databaseId());
					if (result)
						result = tmp;
				}

				if (updateModel)
					treeModel.nodeInserted(targetContainer, i);

			} else if (entry instanceof SequenceReference) {
				SequenceReference sourceRef = (SequenceReference) entry;
				Sequence source = (Sequence) sourceRef.parent();
				Sequence target = config.sequence(sourceRef.name());
				if (target != null) {
					config.insertSequenceReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					result = false;
				} else {
					target = config.insertSequence(config.sequenceCount(), sourceRef.name());
					if (updateModel)
						treeModel.nodeInserted(treeModel.sequencesNode(), config.sequenceCount() - 1);
					config.insertSequenceReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					boolean tmp = importContainerEntries(config, treeModel, source, target);
					if (tmp)
						target.setDatabaseId(source.databaseId());
					if (result)
						result = tmp;
				}

				if (updateModel)
					treeModel.nodeInserted(targetContainer, i);
			} else if (entry instanceof TaskReference) {
				TaskReference sourceRef = (TaskReference) entry;
				Task source = (Task) sourceRef.parent();
				Task target = config.task(sourceRef.name());
				if (target != null) {
					config.insertTaskReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					result = false;
				} else {
					target = config.insertTask(config.taskCount(), sourceRef.name());
					if (updateModel)
						treeModel.nodeInserted(treeModel.tasksNode(), config.taskCount() - 1);
					config.insertTaskReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					boolean tmp = importContainerEntries(config, treeModel, source, target);
																								
					if (tmp)
						target.setDatabaseId(source.databaseId());
					if (result)
						result = tmp;
				}

				if (updateModel)
					treeModel.nodeInserted(targetContainer, i);
			} else if (entry instanceof SwitchProducerReference) {
				SwitchProducerReference sourceRef = (SwitchProducerReference) entry;
				SwitchProducer source = (SwitchProducer) sourceRef.parent();
				SwitchProducer target = config.switchProducer(sourceRef.name());
				if (target != null) {								
					config.insertSwitchProducerReference(targetContainer, i, target)
							.setOperator(sourceRef.getOperator());
					result = false;
				} else {
					target = config.insertSwitchProducer(config.switchProducerCount(), sourceRef.name());
					if (updateModel)
						treeModel.nodeInserted(treeModel.switchProducersNode(), config.switchProducerCount() - 1);
					config.insertSwitchProducerReference(targetContainer, i, target)
							.setOperator(sourceRef.getOperator());
					boolean tmp = importContainerEntries(config, treeModel, source, target);
																								
					if (tmp)
						target.setDatabaseId(source.databaseId());
					if (result)
						result = tmp;
				}

				if (updateModel)
					treeModel.nodeInserted(targetContainer, i);
			}
		}
		return result;
	}

	/**
	 * DeepImportContainerEntries ---------------------------- Insert entries of an
	 * external reference container into the local copy In this case, deep check is
	 * made to ensure that Containers are identical. Inserting, Replacing, Deleting
	 * and Ordering modules. NOTE: DeepImport feature make use of preanalysis before
	 * performing its operations. If for any reason the DeepImportContainerEntries
	 * structure is changed then DeepImportContainerEntriesSimulation must also be
	 * changed to ensure the diff results matches the DeepImport results.
	 * 
	 * @author jimeneze
	 */
	private static boolean DeepImportContainerEntries(Configuration config, Configuration sourceConfig,
			JTree targetTree, ReferenceContainer sourceContainer, ReferenceContainer targetContainer) {

		boolean updateModel = (targetTree != null);
		ConfigurationTreeModel treeModel = null;
		if (updateModel)
			treeModel = (ConfigurationTreeModel) targetTree.getModel();

		boolean result = true;

		for (int i = 0; i < sourceContainer.entryCount(); i++) {
			Reference entry = sourceContainer.entry(i);

			if (entry instanceof ModuleReference) { // MODULE REFERENCES
				ModuleReference sourceRef = (ModuleReference) entry;
				ModuleInstance source = (ModuleInstance) sourceRef.parent();
				ModuleInstance target = config.module(source.name());
				ModuleReference targetRef = null;

				if (target != null) { // if module already exist then just insert the reference.

					Diff diff = new Diff(sourceConfig, config);
					Comparison c = diff.compareInstances(source, target);

					// If module exist but it's not identical:
					if (!c.isIdentical()) { // replace module.
						if (updateModel) {
							ConfigurationTreeActions.replaceModule(null, targetTree, source);
							treeModel.nodeStructureChanged(treeModel.modulesNode()); // forcing refresh
						} else {
							ConfigurationTreeActions.replaceModule(config, null, source);
						}
					} // else If module exist and it's identical then do nothing.

					// After replacing the module (or not) we proceed checking the reference:
					boolean existance = false;
					for (int j = 0; j < targetContainer.entryCount(); j++) {
						Reference subentry = (Reference) targetContainer.entry(j);
						if ((subentry instanceof ModuleReference) && (entry instanceof ModuleReference)
								&& (subentry.name().equals(entry.name()))) {

							// Check if modules are in the same order:
							if (i != j) { // then remove reference, and insert it again.
								if (updateModel) {
									removeReference(null, targetTree, subentry); // this might delete the Module (index
																					// of -1 when searching).
									treeModel.nodeStructureChanged(targetContainer);
								} else {
									removeReference(config, null, subentry); // this might delete the Module (index of
																				// -1 when searching).
								}
								existance = false; // this force the reference to be inserted again (in order).
							} else {
								subentry.setOperator(sourceRef.getOperator());
								existance = true;
							}
						}
					}
					if (!existance) {
						int indexOfModule = config.indexOfModule(source);
						if (indexOfModule == -1) {
							// the instance was also removed so it needs to be inserted again:
							targetRef = config.insertModuleReference(targetContainer, i, source.template().name(),
									source.name());
							target = (ModuleInstance) targetRef.parent();
							// Update parameters:
							for (int j = 0; j < target.parameterCount(); j++)
								target.updateParameter(j, source.parameter(j).valueAsString());
							target.setDatabaseId(source.databaseId());

							// it was inserted. common operations:
							targetRef.setOperator(sourceRef.getOperator());
							if (updateModel) {
								treeModel.nodeInserted(targetContainer, i);
								if (target.referenceCount() == 1)
									treeModel.nodeInserted(treeModel.modulesNode(), config.moduleCount() - 1);
							}
						} else { // ONLY insert the reference. (the module already exists).
							targetRef = config.insertModuleReference(targetContainer, i, target);
							targetRef.setOperator(sourceRef.getOperator());
							if (updateModel)
								treeModel.nodeInserted(targetContainer, i);
						}
					}
				} else { // Inserts the module and the reference:

					// NOTE: next call also inserts the module before inserting references
					targetRef = config.insertModuleReference(targetContainer, i, source.template().name(),
							source.name());

					target = (ModuleInstance) targetRef.parent();
					// Update parameters:
					for (int j = 0; j < target.parameterCount(); j++)
						target.updateParameter(j, source.parameter(j).valueAsString());
					target.setDatabaseId(source.databaseId());

					// it was inserted. common operations:
					targetRef.setOperator(sourceRef.getOperator());
					if (updateModel) {
						treeModel.nodeInserted(targetContainer, i);
						if (target.referenceCount() == 1)
							treeModel.nodeInserted(treeModel.modulesNode(), config.moduleCount() - 1);
					}
				}
				// -----------------------------------------------------------------------------//
			} else if (entry instanceof EDAliasReference) { // EDALIAS REFERENCES
				EDAliasReference sourceRef = (EDAliasReference) entry;
				EDAliasInstance source = (EDAliasInstance) sourceRef.parent();
				EDAliasInstance target = config.edAlias(source.name());
				EDAliasReference targetRef = null;

				if (target != null) { // if edAlias already exist then just insert the reference.

					Diff diff = new Diff(sourceConfig, config);
					Comparison c = diff.compareInstances(source, target);

					// If edAlias exist but it's not identical:
					if (!c.isIdentical()) { // replace edAlias.
						if (updateModel) {
							ConfigurationTreeActions.replaceEDAlias(null, targetTree, source);
							// refresh SP node since EDA are there
							treeModel.nodeStructureChanged(treeModel.switchProducersNode()); // forcing refresh
						} else {
							ConfigurationTreeActions.replaceEDAlias(config, null, source);
						}
					} // else If edAlias exist and it's identical then do nothing.

					// After replacing the edAlias (or not) we proceed checking the reference:
					boolean existance = false;
					for (int j = 0; j < targetContainer.entryCount(); j++) {
						Reference subentry = (Reference) targetContainer.entry(j);
						if ((subentry instanceof EDAliasReference) && (entry instanceof EDAliasReference)
								&& (subentry.name().equals(entry.name()))) {

							// Check if edAliases are in the same order:
							if (i != j) { // then remove reference, and insert it again.
								if (updateModel) {
									removeReference(null, targetTree, subentry); // this might delete the edAlias (index
																					// of -1 when searching).
									treeModel.nodeStructureChanged(targetContainer);
								} else {
									removeReference(config, null, subentry); // this might delete the edAlias (index of
																				// -1 when searching).
								}
								existance = false; // this force the reference to be inserted again (in order).
							} else {
								subentry.setOperator(sourceRef.getOperator());
								existance = true;
							}
						}
					}
					if (!existance) {
						int indexOfEDAlias = config.indexOfEDAlias(source);
						if (indexOfEDAlias == -1) {
							// the instance was also removed so it needs to be inserted again:
							targetRef = config.insertEDAliasReference(targetContainer, i, source.name());
							target = (EDAliasInstance) targetRef.parent();
							// Update parameters:
							for (int j = 0; j < source.parameterCount(); j++){
								Parameter param = source.parameter(j);
								target.updateParameter(param);
							}
								
							target.setDatabaseId(source.databaseId());

							// it was inserted. common operations:
							targetRef.setOperator(sourceRef.getOperator());
							if (updateModel) {
								treeModel.nodeInserted(targetContainer, i);
								/*
								 * if (target.referenceCount() == 1)
								 * treeModel.nodeInserted(treeModel.modulesNode(), config.moduleCount() - 1);
								 */
							}
						} else { // ONLY insert the reference. (the module already exists).
							targetRef = config.insertEDAliasReference(targetContainer, i, target);
							targetRef.setOperator(sourceRef.getOperator());
							if (updateModel)
								treeModel.nodeInserted(targetContainer, i);
						}
					}
				} else { // Inserts the edAlias and the reference:

					// NOTE: next call also inserts the module before inserting references
					targetRef = config.insertEDAliasReference(targetContainer, i, source.name());

					target = (EDAliasInstance) targetRef.parent();
					// Update parameters:
					for (int j = 0; j < source.parameterCount(); j++){
						Parameter param = source.parameter(j);
						target.updateParameter(param);
					}
						
					target.setDatabaseId(source.databaseId());

					// it was inserted. common operations:
					targetRef.setOperator(sourceRef.getOperator());
					if (updateModel) {
						treeModel.nodeInserted(targetContainer, i);
						/*
						 * if (target.referenceCount() == 1)
						 * treeModel.nodeInserted(treeModel.modulesNode(), config.moduleCount() - 1);
						 */
					}
				}
				// -----------------------------------------------------------------------------//
			} else if (entry instanceof OutputModuleReference) { // OUTPUTMODULE REFERENCES
				OutputModuleReference sourceRef = (OutputModuleReference) entry;
				OutputModule source = (OutputModule) sourceRef.parent();
				OutputModule target = config.output(source.name());
				OutputModuleReference targetRef = null;
				if (target != null) { // if OutputModule exist then insert ONLY the Reference.
					Diff diff = new Diff(sourceConfig, config);
					Comparison c = diff.compareOutputModules(source, target);

					// If output module exists but it's not identical:
					if (!c.isIdentical()) { // replace module:
						// Update parameters:
						for (int j = 0; j < target.parameterCount(); j++) {
							target.updateParameter(j, source.parameter(j).valueAsString());
						}
						target.setDatabaseId(source.databaseId());

						if (updateModel)
							treeModel.nodeStructureChanged(treeModel.modulesNode()); // forcing refresh
					} else { // If module exist and it's identical then do nothing:
						result = false;
					}

					targetRef = config.insertOutputModuleReference(targetContainer, i, target);

					// common operations:
					targetRef.setOperator(sourceRef.getOperator());
					if (updateModel) {
						treeModel.nodeInserted(targetContainer, i);
						if (target.referenceCount() == 1)
							treeModel.nodeInserted(treeModel.outputsNode(), config.outputCount() - 1);
					}
					result = false;
				} else { // If it does not exist won't import anything.
					System.out.println("OutputModules must already exist as they are imported via stream import!");
				}
				// -----------------------------------------------------------------------------//
			} else if (entry instanceof PathReference) { // PATH REFERENCES
				PathReference sourceRef = (PathReference) entry;
				Path source = (Path) sourceRef.parent();
				Path target = config.path(source.name());

				if (target != null) { // if the path already exist then it just insert the reference.

					Diff diff = new Diff(sourceConfig, config);
					Comparison c = diff.compareContainers(source, target);

					if (!c.isIdentical()) {
						// System.out.println("existing Path differences found!");
						DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
						if (updateModel)
							treeModel.nodeStructureChanged(treeModel.pathsNode());
					} // else if IDENTICAL: nothing to do.

					// Do not insert Paths references.
					// Nested paths are not allowed in theory.
					// config.insertPathReference(targetContainer,i,target);
					// if (updateModel) treeModel.nodeInserted(targetContainer,i); // refresh the
					// tree view

					// Now references must be checked:
					for (int j = 0; j < targetContainer.entryCount(); j++) {
						Reference subentry = (Reference) targetContainer.entry(j);
						if ((subentry instanceof PathReference) && (entry instanceof PathReference)
								&& (subentry.name().equals(entry.name()))) {

							// Check if SequenceReference are in the same order:
							if (i != j) {
								// So remove reference, and insert it later.
								if (updateModel) {
									removeReference(null, targetTree, subentry); // this might delete the ITEM (index of
																					// -1 when searching).
									treeModel.nodeStructureChanged(targetContainer);
								} else {
									removeReference(config, null, subentry); // this might delete the ITEM (index of -1
																				// when searching).
								}
							} else {
								subentry.setOperator(sourceRef.getOperator());
							}
						}
					}

					result = false;
				} else { // insert the path and the reference.

					target = config.insertPath(config.pathCount(), sourceRef.name());
					config.insertPathReference(targetContainer, i, target).setOperator(sourceRef.getOperator()); // insert
																													// the
																													// reference.

					if (updateModel) {
						treeModel.nodeInserted(treeModel.pathsNode(), config.pathCount() - 1);
						treeModel.nodeInserted(targetContainer, i); // refresh the tree view
					}

					// recursively entries insertion!
					boolean tmp = DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
					if (tmp)
						target.setDatabaseId(source.databaseId());
					if (result)
						result = tmp;
					if (updateModel)
						treeModel.nodeStructureChanged(treeModel.pathsNode());
				}

				// INSERT REFERENCES: for new sequences, and out of order references.
				boolean existance = false;
				for (int j = 0; j < targetContainer.entryCount(); j++) {
					Reference subentry = (Reference) targetContainer.entry(j);
					if ((subentry instanceof PathReference) && (entry instanceof PathReference)
							&& (subentry.name().equals(entry.name()))) {
						subentry.setOperator(sourceRef.getOperator());
						existance = true;
					}
				}
				if (!existance) {
					config.insertPathReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					if (updateModel)
						treeModel.nodeInserted(targetContainer, i); // refresh the tree view
				}

				// treeModel.nodeInserted(targetContainer,i); // refresh the tree view
				// -----------------------------------------------------------------------------//
			} else if (entry instanceof SequenceReference) { // SEQUENCE REFERENCES

				SequenceReference sourceRef = (SequenceReference) entry;
				Sequence source = (Sequence) sourceRef.parent();
				Sequence target = config.sequence(sourceRef.name());															

				if (target != null) { // if sequence already exist then just insert the reference.

					Diff diff = new Diff(sourceConfig, config);
					Comparison c = diff.compareContainers(source, target);

					if (!c.isIdentical()) {
						DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
						if (updateModel) {
							treeModel.nodeStructureChanged(treeModel.sequencesNode());
							treeModel.nodeStructureChanged(treeModel.pathsNode());
						}
					} // if identical, just check the order.

					// Now references must be checked:
					for (int j = 0; j < targetContainer.entryCount(); j++) {
						Reference subentry = (Reference) targetContainer.entry(j);
						if ((subentry instanceof SequenceReference) && (entry instanceof SequenceReference)
								&& (subentry.name().equals(entry.name()))) {

							// Check if SequenceReference are in the same order:
							if (i != j) {
								// So remove reference, and insert it later.
								if (updateModel) {
									removeReference(null, targetTree, subentry); // this might delete the ITEM (index of
																					// -1 when searching).
									treeModel.nodeStructureChanged(targetContainer);
								} else {
									removeReference(config, null, subentry); // this might delete the ITEM (index of -1
																				// when searching).
								}
							} else {
								subentry.setOperator(sourceRef.getOperator());
							}
						}
					}
					result = false;
				} else { // Insert the sequence and the reference.

					target = config.insertSequence(config.sequenceCount(), sourceRef.name());
					config.insertSequenceReference(targetContainer, i, target).setOperator(sourceRef.getOperator());

					if (updateModel)
						treeModel.nodeInserted(treeModel.sequencesNode(), config.sequenceCount() - 1);

					// config.insertSequenceReference(targetContainer,targetContainer.entryCount(),target);

					// recursively entries insertion!
					boolean tmp = DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
					if (tmp)
						target.setDatabaseId(source.databaseId());
					if (result)
						result = tmp;
					if (updateModel)
						treeModel.nodeInserted(targetContainer, targetContainer.entryCount() - 1);
				}

				// INSERT REFERENCES: for new sequences, and out of order references.
				boolean existance = false;
				for (int j = 0; j < targetContainer.entryCount(); j++) {
					Reference subentry = (Reference) targetContainer.entry(j);
					if ((subentry instanceof SequenceReference) && (entry instanceof SequenceReference)
							&& (subentry.name().equals(entry.name()))) {
						subentry.setOperator(sourceRef.getOperator());
						existance = true;
					}
				}
				if (!existance) {
					config.insertSequenceReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					if (updateModel)
						treeModel.nodeInserted(targetContainer, i);
				}
			} else if (entry instanceof TaskReference) { // TASK REFERENCES

				TaskReference sourceRef = (TaskReference) entry;
				Task source = (Task) sourceRef.parent();
				Task target = config.task(sourceRef.name());

				if (target != null) { // if task already exist then just insert the reference.

					Diff diff = new Diff(sourceConfig, config);
					Comparison c = diff.compareContainers(source, target);

					if (!c.isIdentical()) {
						DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
						if (updateModel) {
							treeModel.nodeStructureChanged(treeModel.sequencesNode());
							treeModel.nodeStructureChanged(treeModel.tasksNode());
							treeModel.nodeStructureChanged(treeModel.pathsNode());
						}
					} // if identical, just check the order.

					// Now references must be checked:
					for (int j = 0; j < targetContainer.entryCount(); j++) {
						Reference subentry = (Reference) targetContainer.entry(j);
						if ((subentry instanceof TaskReference) && (entry instanceof TaskReference)
								&& (subentry.name().equals(entry.name()))) {

							if (i != j) {
								// So remove reference, and insert it later.
								if (updateModel) {
									removeReference(null, targetTree, subentry); // this might delete the ITEM (index of
																					// -1 when searching).
									treeModel.nodeStructureChanged(targetContainer);
								} else {
									removeReference(config, null, subentry); // this might delete the ITEM (index of -1
																				// when searching).
								}
							} else {
								subentry.setOperator(sourceRef.getOperator());
							}
						}
					}
					result = false;
				} else { // Insert the task and the reference.

					target = config.insertTask(config.taskCount(), sourceRef.name());
					config.insertTaskReference(targetContainer, i, target).setOperator(sourceRef.getOperator());

					if (updateModel)
						treeModel.nodeInserted(treeModel.tasksNode(), config.taskCount() - 1);

					// config.insertTaskReference(targetContainer,targetContainer.entryCount(),target);

					// recursively entries insertion!
					boolean tmp = DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
					if (tmp)
						target.setDatabaseId(source.databaseId());
					if (result)
						result = tmp;
					if (updateModel)
						treeModel.nodeInserted(targetContainer, targetContainer.entryCount() - 1);
				}

				// INSERT REFERENCES: for new tasks, and out of order tasks
				boolean existance = false;
				for (int j = 0; j < targetContainer.entryCount(); j++) {
					Reference subentry = (Reference) targetContainer.entry(j);
					if ((subentry instanceof TaskReference) && (entry instanceof TaskReference)
							&& (subentry.name().equals(entry.name()))) {
						subentry.setOperator(sourceRef.getOperator());
						existance = true;
					}
				}
				if (!existance) {
					config.insertTaskReference(targetContainer, i, target).setOperator(sourceRef.getOperator());
					if (updateModel)
						treeModel.nodeInserted(targetContainer, i);
				}
			} else if (entry instanceof SwitchProducerReference) { // SWITCH PRODUCER REFERENCES

				SwitchProducerReference sourceRef = (SwitchProducerReference) entry;
				SwitchProducer source = (SwitchProducer) sourceRef.parent();
				SwitchProducer target = config.switchProducer(sourceRef.name());																	

				if (target != null) { // if switch producer already exist then just insert the reference.

					Diff diff = new Diff(sourceConfig, config);
					Comparison c = diff.compareContainers(source, target);

					if (!c.isIdentical()) {
						DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
						if (updateModel) {
							treeModel.nodeStructureChanged(treeModel.switchProducersNode());
							treeModel.nodeStructureChanged(treeModel.sequencesNode());
							treeModel.nodeStructureChanged(treeModel.tasksNode());
							treeModel.nodeStructureChanged(treeModel.pathsNode());
						}
					} // if identical, just check the order.

					// Now references must be checked:
					for (int j = 0; j < targetContainer.entryCount(); j++) {
						Reference subentry = (Reference) targetContainer.entry(j);
						if ((subentry instanceof SwitchProducerReference) && (entry instanceof SwitchProducerReference)
								&& (subentry.name().equals(entry.name()))) {

							// Check if SwitchProducerReference are in the same order
							if (i != j) {
								// So remove reference, and insert it later.
								if (updateModel) {				
									removeReference(null, targetTree, subentry); 												
									treeModel.nodeStructureChanged(targetContainer);
								} else {
									removeReference(config, null, subentry);
								}
							} else {
								subentry.setOperator(sourceRef.getOperator());
							}
						}
					}
					result = false;
				} else { // Insert the switch producer and the reference.

					target = config.insertSwitchProducer(config.switchProducerCount(), sourceRef.name());
					config.insertSwitchProducerReference(targetContainer, i, target)
							.setOperator(sourceRef.getOperator());

					if (updateModel)
						treeModel.nodeInserted(treeModel.switchProducersNode(), config.switchProducerCount() - 1);

					// config.insertSwitchProducerReference(targetContainer,targetContainer.entryCount(),target);

					// recursively entries insertion!
					boolean tmp = DeepImportContainerEntries(config, sourceConfig, targetTree, source, target);
					if (tmp)
						target.setDatabaseId(source.databaseId());
					if (result)
						result = tmp;
					if (updateModel)
						treeModel.nodeInserted(targetContainer, targetContainer.entryCount() - 1);
				}

				// INSERT REFERENCES: for new switch producers, and out of order switch
				// producers 
				boolean existance = false;
				for (int j = 0; j < targetContainer.entryCount(); j++) {
					Reference subentry = (Reference) targetContainer.entry(j);
					if ((subentry instanceof SwitchProducerReference) && (entry instanceof SwitchProducerReference)
							&& (subentry.name().equals(entry.name()))) {
						subentry.setOperator(sourceRef.getOperator());
						existance = true;
					}
				}
				if (!existance) {
					config.insertSwitchProducerReference(targetContainer, i, target)
							.setOperator(sourceRef.getOperator());
					if (updateModel)
						treeModel.nodeInserted(targetContainer, i);
				}
			}
		}
		// -----------------------------------------------------------------------------//

		// REMOVE ALL REMAINING ITEMS. Containers must be identical.
		for (int i = 0; i < targetContainer.entryCount(); i++) {

			Reference targetSubEntry = (Reference) targetContainer.entry(i);
			boolean found = false;
			for (int j = 0; j < sourceContainer.entryCount(); j++) {
				Reference sourceSubEntry = (Reference) sourceContainer.entry(j);

				if (((((targetSubEntry instanceof SequenceReference) && (sourceSubEntry instanceof SequenceReference))
						|| ((targetSubEntry instanceof TaskReference) && (sourceSubEntry instanceof TaskReference))
						|| ((targetSubEntry instanceof SwitchProducerReference)
								&& (sourceSubEntry instanceof SwitchProducerReference))
						|| ((targetSubEntry instanceof ModuleReference) && (sourceSubEntry instanceof ModuleReference))
						|| ((targetSubEntry instanceof EDAliasReference)
								&& (sourceSubEntry instanceof EDAliasReference))
						|| ((targetSubEntry instanceof PathReference) && (sourceSubEntry instanceof PathReference)))
						&& (targetSubEntry.name().equals(sourceSubEntry.name())))) {
					if (i == j)
						found = true;
				}
			}
			if (!found) { // DELETE
				if (updateModel) {
					removeReference(null, targetTree, targetSubEntry);
					treeModel.nodeStructureChanged(targetContainer);
				} else {
					removeReference(config, null, targetSubEntry);
				}
				i--; // going back after modifying the size.
			}
		}

		return result;
	}

	/**
	 * insert reference into currently selected reference container
	 */
	public static boolean insertReference(JTree tree, String type, String name) {

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();

		TreePath parentTreePath = (depth == 3) ? treePath : treePath.getParentPath();
		ReferenceContainer parent = (ReferenceContainer) parentTreePath.getLastPathComponent();
		int index = (depth == 3) ? 0 : parent.indexOfEntry((Reference) treePath.getLastPathComponent()) + 1;

		Reference reference = null;
		ModuleInstance module = null;
		EDAliasInstance edAlias = null;

		if (type.equalsIgnoreCase("Path")) {
			Path referencedPath = config.path(name);
			if (referencedPath == null)
				return false;
			reference = config.insertPathReference(parent, index, referencedPath);
		} else if (type.equalsIgnoreCase("Sequence")) {
			Sequence referencedSequence = config.sequence(name);
			if (referencedSequence == null)
				return false;
			reference = config.insertSequenceReference(parent, index, referencedSequence);
		} else if (type.equalsIgnoreCase("Task")) {
			Task referencedTask = config.task(name);
			if (referencedTask == null)
				return false;
			reference = config.insertTaskReference(parent, index, referencedTask);
		} else if (type.equalsIgnoreCase("SwitchProducer")) {
			SwitchProducer referencedSwitchProducer = config.switchProducer(name);
			if (referencedSwitchProducer == null)
				return false;
			reference = config.insertSwitchProducerReference(parent, index, referencedSwitchProducer);
		} else if (type.equalsIgnoreCase("OutputModule")) {
			OutputModule referencedOutput = config.output(name);
			if (referencedOutput == null)
				return false;
			reference = config.insertOutputModuleReference(parent, index, referencedOutput);
		} else if (type.equalsIgnoreCase("Module")) {
			// The "unlikely string" hack.
			//  Here, the variable "name" can presumably take values of the form
			//  "pluginType", "pluginType:moduleLabel", or "copy:pluginType:moduleLabel".
			//  Unfortunately, this convention does not take into account the fact that
			//  pluginType itself can contain the substring "::" (e.g. plugin types with namespace specification,
			//  like Alpaka plugins with explicit backend selection).
			//  The hack below consists in replacing "::" in "name" with "##", before "name" is split by ":".
			//  After that, the replacement of "::" with this unlikely string is undone in "templateName".
			// NOTE.
			//  This hack assumes that plugin types in CMSSW will never
			//  have "##" in their name, since that would be invalid in C++.
			String unlikelyStr = "##";
			String[] s = name.replaceAll("::", unlikelyStr).split(":");
			String templateName = "";
			String instanceName = "";
			boolean copy = false;

			if (s.length == 1) {
				templateName = s[0];
			} else if (s.length == 2) {
				templateName = s[0];
				instanceName = s[1];
			} else {
				copy = true;
				templateName = s[1];
				instanceName = s[2];
			}

			templateName = templateName.replaceAll(unlikelyStr, "::");

			ModuleTemplate template = config.release().moduleTemplate(templateName);

			if (!copy) {
				if (template.hasInstance(instanceName)) {
					//switch producers can not add existing modules
					
					if (!(parent instanceof SwitchProducer) ) {
						try {
							module = (ModuleInstance) template.instance(instanceName);
						} catch (DataException e) {
							System.err.println(e.getMessage());
							return false;
						}
						//modules in switch producers can not be added to other containers
						if( module.moduleType() != 1 ){
							reference = config.insertModuleReference(parent, index, module);
						}
					}
				} else {
					instanceName = templateName;
					int count = 2;
					while (template.hasInstance(instanceName)) {
						instanceName = templateName + count;
						++count;
					}
					reference = config.insertModuleReference(parent, index, templateName, instanceName);
					module = (ModuleInstance) reference.parent();
				}
			} else {
				ModuleInstance original = null;
				try {
					original = (ModuleInstance) template.instance(instanceName);
				} catch (DataException e) {
					System.err.println(e.getMessage());
					return false;
				}
				instanceName = "copy_of_" + instanceName;
				reference = config.insertModuleReference(parent, index, templateName, instanceName);
				module = (ModuleInstance) reference.parent();
				Iterator<Parameter> itP = original.parameterIterator();
				while (itP.hasNext()) {
					Parameter p = itP.next();
					module.updateParameter(p.name(), p.type(), p.valueAsString());
				}
				
			}
		} else if (type.equalsIgnoreCase("EDAlias")) {
			String[] s = name.split(":");
			String instanceName = "";
			boolean copy = false;

			if (s.length == 1) {
				instanceName = s[0];
			} else if (s.length == 2) {
				copy = true;
				instanceName = s[1];
			}

			// ModuleTemplate template = config.release().moduleTemplate(templateName);

			if (!copy) {
				reference = config.insertEDAliasReference(parent, index, instanceName);
			} else {
				EDAliasInstance original = null;
				original = config.edAlias(instanceName);
				instanceName = "copy_of_" + instanceName;
				reference = config.insertEDAliasReference(parent, index, original);
				edAlias = (EDAliasInstance) reference.parent();
				Iterator<Parameter> itP = original.parameterIterator();
				while (itP.hasNext()) {
					Parameter p = itP.next();
					edAlias.updateParameter(p.name(), p.type(), p.valueAsString());
				}
			}
		}

		if (reference != null) {
			model.nodeInserted(parent, index);
			model.updateLevel1Nodes();

			TreePath newTreePath = parentTreePath.pathByAddingChild(reference);
			tree.expandPath(newTreePath.getParentPath());
			tree.setSelectionPath(newTreePath);

			if (module != null && module.referenceCount() == 1) {
				TreePath moduleTreePath = new TreePath(model.getPathToRoot((Object) module));
				model.nodeInserted(model.modulesNode(), config.moduleCount() - 1);
				editNodeName(tree);
			}
			return true;
		} else {
			return true;
		}
	}

	/** move a reference within its container */
	public static boolean moveReference(JTree tree, Reference sourceReference) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		Object target = treePath.getLastPathComponent();

		ReferenceContainer container = (target instanceof ReferenceContainer) ? (ReferenceContainer) target
				: ((Reference) target).container();

		if (sourceReference.container() != container)
			return false;

		int sourceIndex = container.indexOfEntry(sourceReference);
		int targetIndex = (treePath.getPathCount() == 3) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		container.moveEntry(sourceReference, targetIndex);
		model.nodeRemoved(container, sourceIndex, sourceReference);
		if (sourceIndex < targetIndex)
			targetIndex--;
		model.nodeInserted(container, targetIndex);
		// config.setHasChanged(true);
		return true;
	}

	/** remove all end paths with an output module */
	public static boolean rmEndPathsWithOutputMods(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		Iterator<Path> pathIt = config.pathIterator();
		ArrayList<Path> pathsToRm = new ArrayList<Path>();
		while(pathIt.hasNext()){
			Path path = pathIt.next();
			if(path.isEndPath() && path.hasOutputModule()){
				pathsToRm.add(path);				
			}
		}
		if( !pathsToRm.isEmpty()) {
			for(Path path : pathsToRm){
				config.removePath(path);
			}
			model.nodeStructureChanged(model.getRoot());
			model.updateLevel1Nodes();
		}
		return true;
	}
	/** remove a reference container */
	public static boolean removeReferenceContainer(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		ReferenceContainer container = (ReferenceContainer) treePath.getLastPathComponent();

		ArrayList<Integer> unreferencedIndices = new ArrayList<Integer>();
		for (int i = 0; i < container.entryCount(); i++) {
			Reference entry = container.entry(i);
			if (entry instanceof ModuleReference) {
				ModuleReference reference = (ModuleReference) entry;
				ModuleInstance instance = (ModuleInstance) reference.parent();
				if (instance.referenceCount() == 1)
					unreferencedIndices.add(config.indexOfModule(instance));
			} 
		}

		int childIndices[] = null;
		Object children[] = null;
		if (unreferencedIndices.size() > 0) {
			childIndices = new int[unreferencedIndices.size()];
			children = new Object[unreferencedIndices.size()];
			for (int i = 0; i < unreferencedIndices.size(); i++) {
				int moduleIndex = unreferencedIndices.get(i).intValue();
				childIndices[i] = moduleIndex;
				children[i] = config.module(moduleIndex);
			}
		}

		int index = -1;
		Object parent = null;
		if (container instanceof Path) {
			Path path = (Path) container;
			index = config.indexOfPath(path);
			parent = model.pathsNode();

			if (model.contentMode().equals("paths")) {
				Iterator<EventContent> itC = path.contentIterator();
				while (itC.hasNext()) {
					EventContent content = itC.next();
					model.nodeRemoved(content, content.indexOfPath(path), path);
				}
			}

			if (model.streamMode().equals("paths")) {
				Iterator<Stream> itS = path.streamIterator();
				while (itS.hasNext()) {
					Stream stream = itS.next();
					model.nodeRemoved(stream, stream.indexOfPath(path), path);
				}
			}

			Iterator<PrimaryDataset> itD = path.datasetIterator();
			while (itD.hasNext()) {
				PrimaryDataset dataset = itD.next();
				model.nodeRemoved(dataset, dataset.indexOfPath(path), path);
			}
			config.removePath(path);
		} else if (container instanceof Sequence) {
			index = config.indexOfSequence((Sequence) container);
			parent = model.sequencesNode();
			config.removeSequence((Sequence) container);
		} else if (container instanceof Task) {
			index = config.indexOfTask((Task) container);
			parent = model.tasksNode();
			config.removeTask((Task) container);
		} else if (container instanceof SwitchProducer) {
			index = config.indexOfSwitchProducer((SwitchProducer) container);
			parent = model.switchProducersNode();
			config.removeSwitchProducer((SwitchProducer) container);
		}

		model.nodeRemoved(parent, index, container);
		if (childIndices != null)
			model.nodesRemoved(model.modulesNode(), childIndices, children);
		model.updateLevel1Nodes();
		model.nodeStructureChanged(model.outputsNode());

		TreePath parentTreePath = treePath.getParentPath();
		if (index == 0)
			tree.setSelectionPath(parentTreePath);
		else
			tree.setSelectionPath(parentTreePath.pathByAddingChild(model.getChild(parent, index - 1)));

		return true;
	}

	/** remove reference from currently selected reference container */
	public static boolean removeReference(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		Reference reference = (Reference) treePath.getLastPathComponent();
		ReferenceContainer container = reference.container();
		int index = container.indexOfEntry(reference);
		ModuleInstance module = null;
		EDAliasInstance edAlias = null;
		int indexOfModule = -1;
		// int indexOfEDAlias = -1;

		if (reference instanceof ModuleReference) {
			module = (ModuleInstance) reference.parent();
			indexOfModule = config.indexOfModule(module);
			config.removeModuleReference((ModuleReference) reference);
		} else if (reference instanceof EDAliasReference) {
			edAlias = (EDAliasInstance) reference.parent();
			// indexOfEDAlias = config.indexOfEDAlias(edAlias);
			config.removeEDAliasReference((EDAliasReference) reference);
		} else if (reference instanceof OutputModuleReference) {
			OutputModuleReference omr = (OutputModuleReference) reference;
			config.removeOutputModuleReference(omr);
		} else {
			container.removeEntry(reference);
		}

		model.nodeRemoved(container, index, reference);
		if (module != null && module.referenceCount() == 0)
			model.nodeRemoved(model.modulesNode(), indexOfModule, module);
		model.updateLevel1Nodes();

		TreePath parentTreePath = treePath.getParentPath();
		Object parent = parentTreePath.getLastPathComponent();
		if (index == 0)
			tree.setSelectionPath(parentTreePath);
		else
			tree.setSelectionPath(parentTreePath.pathByAddingChild(model.getChild(parent, index - 1)));

		return true;
	}

	/**
	 * removeReference ------------------ remove reference passed by parameter
	 * instead using selectionPath. NOTE: This will be used by deep import function.
	 */
	public static boolean removeReference(Configuration config, JTree tree, Reference reference) {
		if ((config == null) && (tree == null))
			return false;
		if ((config != null) && (tree != null))
			return false;

		ConfigurationTreeModel model = null;

		boolean updateModel = (tree != null);
		if (updateModel) {
			model = (ConfigurationTreeModel) tree.getModel();
			config = (Configuration) model.getRoot();
		}

		ReferenceContainer container = reference.container();
		int index = container.indexOfEntry(reference);

		ModuleInstance module = null;
		int indexOfModule = -1;

		if (reference instanceof ModuleReference) {
			module = (ModuleInstance) reference.parent();
			indexOfModule = config.indexOfModule(module);
			config.removeModuleReference((ModuleReference) reference);
		} else if (reference instanceof EDAliasReference) {
			config.removeEDAliasReference((EDAliasReference) reference);
		} else if (reference instanceof OutputModuleReference) {
			config.removeOutputModuleReference((OutputModuleReference) reference);
		} else {
			container.removeEntry(reference);
		}

		if (updateModel) {
			model.nodeRemoved(container, index, reference);
			if (module != null && module.referenceCount() == 0)
				model.nodeRemoved(model.modulesNode(), indexOfModule, module);																				
			model.updateLevel1Nodes();
		}

		return true;
	}

	/** set operator */
	public static boolean setOperator(JTree tree, String newOperator) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		Reference reference = (Reference) treePath.getLastPathComponent();
		Operator op = Operator.valueOf(newOperator);

		reference.setOperator(op);

		model.nodeChanged(reference);
		return true;
	}

	/** scroll to the instance of the currently selected reference */
	public static void scrollToInstance(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		Reference reference = (Reference) treePath.getLastPathComponent();
		Referencable instance = reference.parent();

		TreePath instanceTreePath = new TreePath(model.getPathToRoot(instance));
		tree.setSelectionPath(instanceTreePath);
		tree.expandPath(instanceTreePath);
		tree.scrollPathToVisible(instanceTreePath);

	}

	/**
	 * scroll to the Path given by the path name and expand the tree.
	 */
	public static void scrollToPathByName(String pathName, JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		TreePath Path = new TreePath(model.getPathToRoot(config.path(pathName)));
		tree.setSelectionPath(Path);
		tree.expandPath(Path);
		tree.scrollPathToVisible(Path);
	}

	/**
	 * scroll to the Path given by the sequence name and expand the tree.
	 */
	public static void scrollToSequenceByName(String sequenceName, JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		TreePath Path = new TreePath(model.getPathToRoot(config.sequence(sequenceName)));
		tree.setSelectionPath(Path);
		tree.expandPath(Path);
		tree.scrollPathToVisible(Path);
	}

	/**
	 * scroll to the Path given by the task name and expand the tree.
	 */
	public static void scrollToTaskByName(String taskName, JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		TreePath Path = new TreePath(model.getPathToRoot(config.task(taskName)));
		tree.setSelectionPath(Path);
		tree.expandPath(Path);
		tree.scrollPathToVisible(Path);
	}

	/**
	 * scroll to the Path given by the switch prouducer name and expand the
	 * tree.
	 */
	public static void scrollToSwitchProducerByName(String switchProducerName, JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		TreePath Path = new TreePath(model.getPathToRoot(config.switchProducer(switchProducerName)));
		tree.setSelectionPath(Path);
		tree.expandPath(Path);
		tree.scrollPathToVisible(Path);
	}

	/**
	 * scroll to the module given by the module name and expand the tree.
	 */
	public static void scrollToModuleByName(String moduleName, JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		TreePath Path = new TreePath(model.getPathToRoot(config.module(moduleName)));
		tree.setSelectionPath(Path);
		tree.expandPath(Path);
		tree.scrollPathToVisible(Path);
	}

	/**
	 * scroll to the edAlias given by the edAlias name and expand the tree.
	 */
	public static void scrollToEDAliasByName(String edAliasName, JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		TreePath Path = new TreePath(model.getPathToRoot(config.edAlias(edAliasName)));
		tree.setSelectionPath(Path);
		tree.expandPath(Path);
		tree.scrollPathToVisible(Path);
	}
	
	/**
	 * scroll to the global edAlias given by the edAlias name and expand the tree.
	 */
	public static void scrollToGlobalEDAliasByName(String globalEDAliasName, JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		TreePath Path = new TreePath(model.getPathToRoot(config.globalEDAlias(globalEDAliasName)));
		tree.setSelectionPath(Path);
		tree.expandPath(Path);
		tree.scrollPathToVisible(Path);
	}

	/** import a single module into path, sequence, task or a switch producer */
	public static boolean importModule(JTree tree, ModuleInstance external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		ReferenceContainer parent = null;
		ModuleInstance target = config.module(external.name());
		int insertAtIndex = 0;

		if (target != null) {
			int choice = JOptionPane.showConfirmDialog(null,
					"The module '" + target.name() + "' exists, " + "do you want to overwrite it?", "Overwrite module",
					JOptionPane.OK_CANCEL_OPTION);
			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			else
				return replaceModule(null, tree, external);
		} else if (treePath == null)
			return false;

		Object targetNode = treePath.getLastPathComponent();

		if (targetNode instanceof ReferenceContainer) {
			parent = (ReferenceContainer) targetNode;
			ModuleReference reference = config.insertModuleReference(parent, 0, external.template().name(),
					external.name());
			target = (ModuleInstance) reference.parent();
		} else if (targetNode instanceof Reference) {
			Reference selectedRef = (Reference) targetNode;
			parent = selectedRef.container();
			insertAtIndex = parent.indexOfEntry(selectedRef) + 1;
			ModuleReference reference = config.insertModuleReference(parent, insertAtIndex, external.template().name(),
					external.name());
			target = (ModuleInstance) reference.parent();
		}

		if (target == null)
			return false;

		for (int i = 0; i < target.parameterCount(); i++)
			target.updateParameter(i, external.parameter(i).valueAsString());
		target.setDatabaseId(external.databaseId());
		model.nodeInserted(parent, insertAtIndex);
		model.nodeInserted(model.modulesNode(), config.moduleCount() - 1);
		model.updateLevel1Nodes();

		return true;
	}

	/** import a single EDAlias into a switch producer */
	public static boolean importEDAlias(JTree tree, EDAliasInstance external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		ReferenceContainer parent = null;
		EDAliasInstance target = config.edAlias(external.name());
		int insertAtIndex = 0;

		if (target != null) {
			int choice = JOptionPane.showConfirmDialog(null,
					"The EDALias '" + target.name() + "' exists, " + "do you want to overwrite it?",
					"Overwrite EDAlias", JOptionPane.OK_CANCEL_OPTION);
			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			else
				return replaceEDAlias(null, tree, external);
		} else if (treePath == null)
			return false;

		Object targetNode = treePath.getLastPathComponent();

		if (targetNode instanceof ReferenceContainer) {
			parent = (ReferenceContainer) targetNode;
			EDAliasReference reference = config.insertEDAliasReference(parent, 0, external.name());
			target = (EDAliasInstance) reference.parent();
		} else if (targetNode instanceof Reference) {
			Reference selectedRef = (Reference) targetNode;
			parent = selectedRef.container();
			insertAtIndex = parent.indexOfEntry(selectedRef) + 1;
			EDAliasReference reference = config.insertEDAliasReference(parent, insertAtIndex, external.name());
			target = (EDAliasInstance) reference.parent();
		}

		if (target == null)
			return false;

		for (int i = 0; i < external.parameterCount(); i++) {
			target.updateParameter(external.parameter(i));
		}
		target.setDatabaseId(external.databaseId());
		model.nodeInserted(parent, insertAtIndex);
		// model.nodeInserted(model.modulesNode(), config.moduleCount() - 1);
		model.updateLevel1Nodes();

		return true;
	}
	
	/** import a single global EDAlias into a switch producer */
	public static boolean importGlobalEDAlias(JTree tree, EDAliasInstance external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		EDAliasInstance target = config.globalEDAlias(external.name());

		if (target != null) {
			int choice = JOptionPane.showConfirmDialog(null,
					"The Global EDALias '" + target.name() + "' exists, " + "do you want to overwrite it?",
					"Overwrite Global EDAlias", JOptionPane.OK_CANCEL_OPTION);
			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			else
				return replaceGlobalEDAlias(null, tree, external);
		} else if (treePath == null)
			return false;

		//Target reference part is probably completely unnecessary since global EDAliases cannot be referenced
		
		return true;
	}

	/**
	 * replace a module with the internal one
	 */
	public static boolean replaceModuleInternally(JTree tree, ModuleInstance oldModule, String newObject) {
		/* newObject = class or class:name or copy:class:name */
		System.out.println("XXX " + oldModule.name() + " " + newObject);

		if (tree == null || oldModule == null || newObject == null)
			return false;

		String oldModuleName = oldModule.name();
		String newModuleName = null;
		String[] s = newObject.split(":");

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		if (config == null)
			return false;

		if (config.module(oldModule.name()) == null)
			return false;

		ModuleInstance newModule = null;
		String newTemplateName = null;
		if (s.length == 1) {
			// old module replaced by new instance of the template, keeping oldModuleName
			newTemplateName = s[0];
			// temporary unique name
			newModuleName = "Instance_of_" + newTemplateName;
			int i = 0;
			while (!config.isUniqueQualifier(newModuleName)) {
				newModuleName = "Instance_of_" + newTemplateName + "_" + i;
				++i;
			}
			newModule = config.insertModule(newTemplateName, newModuleName);
			Iterator<Parameter> itP = null;
			itP = oldModule.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Iterator<Parameter> itQ = newModule.parameterIterator();
				while (itQ.hasNext()) {
					Parameter q = itQ.next();
					if (p.type().equals(q.type()))
						newModule.updateParameter(q.name(), q.type(), p.valueAsString());
				}
			}
			itP = oldModule.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Parameter n = newModule.parameter(p.name(), p.type());
				if (n != null)
					newModule.updateParameter(p.name(), p.type(), p.valueAsString());
			}
		} else if (s.length == 2) {
			// old module replaced by existing module, keeping newModuleName
			newTemplateName = s[0];
			newModuleName = s[1];
			if (newModuleName.equals(oldModuleName))
				return false;
			newModule = config.module(newModuleName);
		} else if (s.length == 3) {
			// old module replaced by new copy of an existing module, keeping oldModuleName
			newTemplateName = s[1];
			// temporary unique name
			newModuleName = "Copy_of_" + s[2];
			int i = 0;
			while (!config.isUniqueQualifier(newModuleName)) {
				newModuleName = "Copy_of_" + s[2] + "_" + i;
				++i;
			}
			newModule = config.insertModule(newTemplateName, newModuleName);
			Iterator<Parameter> itP = null;
			itP = oldModule.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Iterator<Parameter> itQ = newModule.parameterIterator();
				while (itQ.hasNext()) {
					Parameter q = itQ.next();
					if (p.type().equals(q.type()))
						newModule.updateParameter(q.name(), q.type(), p.valueAsString());
				}
			}
			itP = oldModule.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Parameter n = newModule.parameter(p.name(), p.type());
				if (n != null)
					newModule.updateParameter(p.name(), p.type(), p.valueAsString());
			}
		} else {
			return false;
		}

		int index = config.indexOfModule(oldModule);
		int refCount = oldModule.referenceCount();
		ReferenceContainer[] parents = new ReferenceContainer[refCount];
		int[] indices = new int[refCount];
		Operator[] operators = new Operator[refCount];
		int iRefCount = 0;
		while (oldModule.referenceCount() > 0) {
			Reference reference = oldModule.reference(0);
			parents[iRefCount] = reference.container();
			indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
			operators[iRefCount] = reference.getOperator();
			config.removeModuleReference((ModuleReference) reference);
			model.nodeRemoved(parents[iRefCount], indices[iRefCount], reference);
			iRefCount++;
		}
		model.nodeRemoved(model.modulesNode(), index, oldModule);

		// oldModule's refCount is now 0 and hence oldModule is removed
		// from the config; thus we can rename newModule to oldModule's
		// name which is needed for later combined setNameAndPropagate
		try {
			newModule.setNameAndPropagate(oldModuleName);
		} catch (DataException e) {
			System.err.println(e.getMessage());
		}

		// update refs pointing to oldModule to point to newModule
		for (int i = 0; i < refCount; i++) {
			config.insertModuleReference(parents[i], indices[i], newModule).setOperator(operators[i]);
			model.nodeInserted(parents[i], indices[i]);
		}

		if (s.length == 2) {
			// now rename newModule back to its original name, and update all
			// (V)InputTags/keeps etc. originally referring to both oldModule
			// and the also newModule under oldModule's name to use newModule's
			// original and final name.
			try {
				newModule.setNameAndPropagate(newModuleName);
			} catch (DataException e) {
				System.err.println(e.getMessage());
			}
		}

		model.updateLevel1Nodes();

		model.nodeStructureChanged(model.modulesNode());
		if (s.length == 2) {
			scrollToModuleByName(newModuleName, tree);
		} else {
			scrollToModuleByName(oldModuleName, tree);
		}

		return true;
	}

	/**
	 * replace a EDAlias with the internal one
	 */
	public static boolean replaceEDAliasInternally(JTree tree, EDAliasInstance oldEDAlias, String newObject) {
		/* newObject = class or class:name or copy:class:name */
		if (tree == null || oldEDAlias == null || newObject == null)
			return false;

		String oldEDAliasName = oldEDAlias.name();
		String newEDAliasName = null;
		String[] s = newObject.split(":");

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		if (config == null)
			return false;

		if (config.edAlias(oldEDAlias.name()) == null)
			return false;

		EDAliasInstance newEDAlias = null;
		if (s.length == 1) {
			// temporary unique name
			newEDAliasName = "Instance_of_" + s[0];
			int i = 0;
			while (!config.isUniqueQualifier(newEDAliasName)) {
				newEDAliasName = "Instance_of_" + s[0] + "_" + i;
				++i;
			}
			newEDAlias = config.insertEDAlias(newEDAliasName);
			Iterator<Parameter> itP = null;
			itP = oldEDAlias.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Iterator<Parameter> itQ = newEDAlias.parameterIterator();
				while (itQ.hasNext()) {
					Parameter q = itQ.next();
					if (p.type().equals(q.type()))
						newEDAlias.updateParameter(q.name(), q.type(), p.valueAsString());
				}
			}
			itP = oldEDAlias.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Parameter n = newEDAlias.parameter(p.name(), p.type());
				if (n != null)
					newEDAlias.updateParameter(p.name(), p.type(), p.valueAsString());
			}
		} else if (s.length == 2) {
			// old edAlias replaced by existing edAlias, keeping newEDAliasName
			// newTemplateName = s[0];
			newEDAliasName = s[1];
			if (newEDAliasName.equals(oldEDAliasName))
				return false;
			newEDAlias = config.edAlias(newEDAliasName);
		} else if (s.length == 3) {
			// old edAlias replaced by new copy of an existing edAlias, keeping
			// oldEDAliasName
			// newTemplateName = s[1];
			// temporary unique name
			newEDAliasName = "Copy_of_" + s[2];
			int i = 0;
			while (!config.isUniqueQualifier(newEDAliasName)) {
				newEDAliasName = "Copy_of_" + s[2] + "_" + i;
				++i;
			}
			newEDAlias = config.insertEDAlias(newEDAliasName);
			Iterator<Parameter> itP = null;
			itP = oldEDAlias.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Iterator<Parameter> itQ = newEDAlias.parameterIterator();
				while (itQ.hasNext()) {
					Parameter q = itQ.next();
					if (p.type().equals(q.type()))
						newEDAlias.updateParameter(q.name(), q.type(), p.valueAsString());
				}
			}
			itP = oldEDAlias.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Parameter n = newEDAlias.parameter(p.name(), p.type());
				if (n != null)
					newEDAlias.updateParameter(p.name(), p.type(), p.valueAsString());
			}
		} else {
			return false;
		}

		//int index = config.indexOfEDAlias(oldEDAlias);
		int refCount = oldEDAlias.referenceCount();
		ReferenceContainer[] parents = new ReferenceContainer[refCount];
		int[] indices = new int[refCount];
		Operator[] operators = new Operator[refCount];
		int iRefCount = 0;
		while (oldEDAlias.referenceCount() > 0) {
			Reference reference = oldEDAlias.reference(0);
			parents[iRefCount] = reference.container();
			indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
			operators[iRefCount] = reference.getOperator();
			config.removeEDAliasReference((EDAliasReference) reference);
			model.nodeRemoved(parents[iRefCount], indices[iRefCount], reference);
			iRefCount++;
		}
		// model.nodeRemoved(model.modulesNode(), index, oldEDAlias);

		// oldEDAliase's refCount is now 0 and hence oldEDAlias is removed
		// from the config; thus we can rename newEDAlias to oldEDAliase's
		// name which is needed for later combined setNameAndPropagate
		try {
			newEDAlias.setNameAndPropagate(oldEDAliasName);
		} catch (DataException e) {
			System.err.println(e.getMessage());
		}

		// update refs pointing to oldEDAlias to point to newEDAlias
		for (int i = 0; i < refCount; i++) {
			config.insertEDAliasReference(parents[i], indices[i], newEDAlias).setOperator(operators[i]);
			model.nodeInserted(parents[i], indices[i]);
		}

		if (s.length == 2) {
			// now rename newEDAlias back to its original name, and update all
			// (V)InputTags/keeps etc. originally referring to both oldEDAlias
			// and the also newEDAlias under oldModule's name to use newEDAlias's
			// original and final name.
			try {
				newEDAlias.setNameAndPropagate(newEDAliasName);
			} catch (DataException e) {
				System.err.println(e.getMessage());
			}
		}

		model.updateLevel1Nodes();

		// model.nodeStructureChanged(model.modulesNode());
		if (s.length == 2) {
			scrollToEDAliasByName(newEDAliasName, tree);
		} else {
			scrollToEDAliasByName(oldEDAliasName, tree);
		}

		return true;
	}
	
	/**
	 * replace a global EDAlias with the internal one
	 */
	public static boolean replaceGlobalEDAliasInternally(JTree tree, EDAliasInstance oldGlobalEDAlias, String newObject) {
		/* newObject = class or class:name or copy:class:name */
		if (tree == null || oldGlobalEDAlias == null || newObject == null)
			return false;

		String oldGlobalEDAliasName = oldGlobalEDAlias.name();
		String newGlobalEDAliasName = null;
		String[] s = newObject.split(":");

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		if (config == null)
			return false;

		if (config.globalEDAlias(oldGlobalEDAlias.name()) == null)
			return false;

		EDAliasInstance newGlobalEDAlias = null;
		String newTemplateName = null;
		if (s.length == 1) {
			// temporary unique name
			newGlobalEDAliasName = "Instance_of_" + s[0];
			int i = 0;
			while (!config.isUniqueQualifier(newGlobalEDAliasName)) {
				newGlobalEDAliasName = "Instance_of_" + s[0] + "_" + i;
				++i;
			}
			newGlobalEDAlias = config.insertGlobalEDAlias(newGlobalEDAliasName);
			Iterator<Parameter> itP = null;
			itP = oldGlobalEDAlias.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Iterator<Parameter> itQ = newGlobalEDAlias.parameterIterator();
				while (itQ.hasNext()) {
					Parameter q = itQ.next();
					if (p.type().equals(q.type()))
						newGlobalEDAlias.updateParameter(q.name(), q.type(), p.valueAsString());
				}
			}
			itP = oldGlobalEDAlias.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Parameter n = newGlobalEDAlias.parameter(p.name(), p.type());
				if (n != null)
					newGlobalEDAlias.updateParameter(p.name(), p.type(), p.valueAsString());
			}
		} else if (s.length == 2) {
			// old edAlias replaced by existing edAlias, keeping newGlobalEDAliasName
			// newTemplateName = s[0]; //here there will have to be dummy, or
			// empty space
			newGlobalEDAliasName = s[1];
			if (newGlobalEDAliasName.equals(oldGlobalEDAliasName))
				return false;
			newGlobalEDAlias = config.globalEDAlias(newGlobalEDAliasName);
		} else if (s.length == 3) {
			// old edAlias replaced by new copy of an existing edAlias, keeping
			// oldGlobalEDAliasName
			// newTemplateName = s[1];
			// temporary unique name
			newGlobalEDAliasName = "Copy_of_" + s[2];
			int i = 0;
			while (!config.isUniqueQualifier(newGlobalEDAliasName)) {
				newGlobalEDAliasName = "Copy_of_" + s[2] + "_" + i;
				++i;
			}
			newGlobalEDAlias = config.insertGlobalEDAlias(newGlobalEDAliasName);
			Iterator<Parameter> itP = null;
			itP = oldGlobalEDAlias.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Iterator<Parameter> itQ = newGlobalEDAlias.parameterIterator();
				while (itQ.hasNext()) {
					Parameter q = itQ.next();
					if (p.type().equals(q.type()))
						newGlobalEDAlias.updateParameter(q.name(), q.type(), p.valueAsString());
				}
			}
			itP = oldGlobalEDAlias.parameterIterator();
			while (itP.hasNext()) {
				Parameter p = itP.next();
				Parameter n = newGlobalEDAlias.parameter(p.name(), p.type());
				if (n != null)
					newGlobalEDAlias.updateParameter(p.name(), p.type(), p.valueAsString());
			}
		} else {
			return false;
		}

		int index = config.indexOfGlobalEDAlias(oldGlobalEDAlias);

		model.nodeRemoved(model.globalEDAliasesNode(), index, oldGlobalEDAlias);

		try {
			newGlobalEDAlias.setNameAndPropagate(oldGlobalEDAliasName);
		} catch (DataException e) {
			System.err.println(e.getMessage());
		}

		if (s.length == 2) {
			// now rename newGlobalEDAlias back to its original name, and update all
			// (V)InputTags/keeps etc. originally referring to both oldGlobalEDAlias
			// and the also newGlobalEDAlias under oldModule's name to use newGlobalEDAlias's
			// original and final name.
			try {
				newGlobalEDAlias.setNameAndPropagate(newGlobalEDAliasName);
			} catch (DataException e) {
				System.err.println(e.getMessage());
			}
		}

		model.updateLevel1Nodes();

		model.nodeStructureChanged(model.globalEDAliasesNode());
		if (s.length == 2) {
			scrollToGlobalEDAliasByName(newGlobalEDAliasName, tree);
		} else {
			scrollToGlobalEDAliasByName(oldGlobalEDAliasName, tree);
		}

		return true;
	}

	/**
	 * Clone module -------------------- Clone an existing module with a different
	 * name. USAGE: if newName is null, it allows the user to edit the name,
	 * otherwise it assign the prefix "copy_of_" to the sourceModule.
	 * 
	 * @NOTE: if the user doesn't change the default name 'copy_of_xxx' it will
	 *        throw an exception: Instance.setName() ERROR: name
	 *        'copy_of_hltDisplacedHT250L25Associator' is not unique! That message
	 *        is not a problem. It also happens adding a new module.
	 */
	public static boolean CloneModule(JTree tree, ModuleReference oldModule, String newName) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		TreePath parentTreePath = (depth == 3) ? treePath : treePath.getParentPath();
		ReferenceContainer parent = (ReferenceContainer) parentTreePath.getLastPathComponent();

		// INDEX: if you are cloning the module by hand use next position in selection
		// path.
		// if module is being cloned by "cloneSequence" use the count to insert it in
		// order.

		int index = (newName != null) ? parent.entryCount()
				: parent.indexOfEntry((Reference) treePath.getLastPathComponent()) + 1;

		Reference reference = null;
		ModuleInstance module = config.module(oldModule.name());

		// retrieving the template:
		String templateName = module.template().name();
		String instanceName = oldModule.name();
		ModuleTemplate template = config.release().moduleTemplate(templateName);

		ModuleInstance original = null;
		try {
			original = (ModuleInstance) template.instance(instanceName);
		} catch (DataException e) {
			System.err.println(e.getMessage());
			return false;
		}

		// Temporary name "copy_of_xxx"
		if (newName != null)
			instanceName = newName;
		else
			instanceName = "copy_of_" + instanceName;

		// To make sure module name doesn't exist.
		String temp;
		if (config.module(instanceName) != null)
			for (int j = 0; j < 10; j++) {
				temp = instanceName + "_" + j;
				if (config.module(temp) == null) {
					j = 10;
					instanceName = temp;
				}
			}

		reference = config.insertModuleReference(parent, index, templateName, instanceName);
		reference.setOperator(oldModule.getOperator());

		module = (ModuleInstance) reference.parent();

		// Copy values
		Iterator<Parameter> itP = original.parameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			module.updateParameter(p.name(), p.type(), p.valueAsString());
		}

		// Inserting in the model and refreshing tree view:
		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();

		TreePath newTreePath = parentTreePath.pathByAddingChild(reference);
		tree.expandPath(newTreePath.getParentPath());
		tree.setSelectionPath(newTreePath);

		// Allow the user to modify the name of the reference
		if (module != null && module.referenceCount() == 1) {
			TreePath moduleTreePath = new TreePath(model.getPathToRoot((Object) module));
			model.nodeInserted(model.modulesNode(), config.moduleCount() - 1);
			if (newName == null)
				editNodeName(tree);
		}

		return true;
	}

	/**
	 * Clone EDAlias -------------------- Clone an existing EDAlias with a different
	 * name. USAGE: if newName is null, it allows the user to edit the name,
	 * otherwise it assign the prefix "copy_of_" to the sourceEDAlias.
	 * 
	 */
	public static boolean CloneEDAlias(JTree tree, EDAliasReference oldEDAlias, String newName) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		TreePath parentTreePath = (depth == 3) ? treePath : treePath.getParentPath();
		ReferenceContainer parent = (ReferenceContainer) parentTreePath.getLastPathComponent();

		// INDEX: if you are cloning the EDAlias by hand use next position in selection
		// path.
		// if EDAlias is being cloned by "cloneSequence" use the count to insert it in
		// order.

		int index = (newName != null) ? parent.entryCount()
				: parent.indexOfEntry((Reference) treePath.getLastPathComponent()) + 1;

		Reference reference = null;
		EDAliasInstance original = config.edAlias(oldEDAlias.name());

		String instanceName = oldEDAlias.name();

		// Temporary name "copy_of_xxx"
		if (newName != null)
			instanceName = newName;
		else
			instanceName = "copy_of_" + instanceName;

		// To make sure edAlias name doesn't exist.
		String temp;
		if (config.edAlias(instanceName) != null)
			for (int j = 0; j < 10; j++) {
				temp = instanceName + "_" + j;
				if (config.edAlias(temp) == null) {
					j = 10;
					instanceName = temp;
				}
			}

		reference = config.insertEDAliasReference(parent, index, instanceName); // this creates new EDAlias internally
		reference.setOperator(oldEDAlias.getOperator());

		EDAliasInstance newEDAlias = (EDAliasInstance) reference.parent();

		// Copy values
		Iterator<Parameter> itP = original.parameterIterator(); // this should copy EDAlias module names
		while (itP.hasNext()) {
			Parameter p = itP.next();
			newEDAlias.updateTrackedParameter(p.name(), p.type(), p.valueAsString());
		}

		// Inserting in the model and refreshing tree view:
		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();

		TreePath newTreePath = parentTreePath.pathByAddingChild(reference);
		tree.expandPath(newTreePath.getParentPath());
		tree.setSelectionPath(newTreePath);

		// Allow the user to modify the name of the reference
		if (newEDAlias != null && newEDAlias.referenceCount() == 1) {
			TreePath edAliasTreePath = new TreePath(model.getPathToRoot((Object) newEDAlias));
			// model.nodeInserted(model.modulesNode(), config.moduleCount() - 1);
			if (newName == null)
				editNodeName(tree);
		}

		return true;
	}
	
	/**
	 * Clone EDAlias -------------------- Clone an existing global EDAlias with a different
	 * name. USAGE: if newName is null, it allows the user to edit the name,
	 * otherwise it assign the prefix "copy_of_" to the sourceGlobalEDAlias.
	 * @throws DataException 
	 * 
	 */
	public static boolean CloneGlobalEDAlias(JTree tree, EDAliasInstance oldGlobalEDAlias, String newName) throws DataException {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		
		int index = config.globalEDAliasCount();

		//EDAliasInstance globalEDAlias = config.insertGlobalEDAlias("<ENTER GLOBAL EDALIAS NAME>");

		EDAliasInstance original = config.globalEDAlias(oldGlobalEDAlias.name());

		String instanceName = oldGlobalEDAlias.name();

		// Temporary name "copy_of_xxx"
		if (newName != null)
			instanceName = newName;
		else
			instanceName = "copy_of_" + instanceName;

		// To make sure global edAlias name doesn't exist.
		String temp;
		if (config.globalEDAlias(instanceName) != null)
			for (int j = 0; j < 10; j++) {
				temp = instanceName + "_" + j;
				if (config.globalEDAlias(temp) == null) {
					j = 10;
					instanceName = temp;
				}
			}
				
		EDAliasInstance newEDAlias = config.insertGlobalEDAlias(index, instanceName);

		// Copy values
		Iterator<Parameter> itP = original.parameterIterator(); // this should copy EDAlias module names
		while (itP.hasNext()) {
			Parameter p = itP.next();
			newEDAlias.updateTrackedParameter(p.name(), p.type(), p.valueAsString());
		}

		// Inserting in the model and refreshing tree view:
		model.nodeInserted(model.globalEDAliasesNode(), index);
		model.updateLevel1Nodes();

		TreePath parentPath = (treePath.getPathCount() == 2) ? treePath : treePath.getParentPath();
		tree.setSelectionPath(parentPath.pathByAddingChild(newEDAlias));

		// Allow the user to modify the name of the reference
		if (newEDAlias != null && newName == null) {
			editNodeName(tree);
		}

		return true;
	}

	/**
	 * replace a container (path, sequence, task, switch producer) with the internal
	 * one
	 */
	public static boolean replaceContainerInternally(JTree tree, String type, ReferenceContainer oldContainer,
			String newObject) {
		if (tree == null || type == null || oldContainer == null || newObject == null)
			return false;
		if (newObject.equals(oldContainer.name()))
			return false;

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		if (config == null)
			return false;

		if (type.equals("Sequence")) {

			Sequence oldSequence = (Sequence) oldContainer;
			if (oldSequence == null)
				return false;
			if (config.sequence(oldSequence.name()) == null)
				return false;
			Sequence newSequence = config.sequence(newObject);
			if (newSequence == null)
				return false;

			int index = config.indexOfSequence(oldSequence);
			int refCount = oldSequence.referenceCount();
			ReferenceContainer[] parents = new ReferenceContainer[refCount];
			int[] indices = new int[refCount];
			Operator[] operators = new Operator[refCount];
			int iRefCount = 0;
			while (oldSequence.referenceCount() > 0) {
				Reference reference = oldSequence.reference(0);
				parents[iRefCount] = reference.container();
				indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
				operators[iRefCount] = reference.getOperator();
				reference.remove();
				model.nodeRemoved(parents[iRefCount], indices[iRefCount], reference);
				iRefCount++;
			}
			model.nodeRemoved(model.sequencesNode(), index, oldSequence);
			for (int i = 0; i < refCount; i++) {
				Reference check = parents[i].entry(newSequence.name());
				int iref = parents[i].indexOfEntry(check);
				if (iref < 0) {
					config.insertSequenceReference(parents[i], indices[i], newSequence).setOperator(operators[i]);
					model.nodeInserted(parents[i], indices[i]);
				} else if (iref > indices[i]) {
					config.insertSequenceReference(parents[i], indices[i], newSequence).setOperator(operators[i]);
					model.nodeInserted(parents[i], indices[i]);
					check.remove();
					model.nodeRemoved(parents[i], iref, check);
				}
			}
			model.updateLevel1Nodes();
			tree.expandPath(new TreePath(model.getPathToRoot(newSequence)));
			config.removeSequence(oldSequence);

		} else if (type.equals("Task")) {

			Task oldTask = (Task) oldContainer;
			if (oldTask == null)
				return false;
			if (config.task(oldTask.name()) == null)
				return false;
			Task newTask = config.task(newObject);
													
			if (newTask == null)
				return false;

			int index = config.indexOfTask(oldTask);
			int refCount = oldTask.referenceCount();
			ReferenceContainer[] parents = new ReferenceContainer[refCount];
			int[] indices = new int[refCount];
			Operator[] operators = new Operator[refCount];
			int iRefCount = 0;
			while (oldTask.referenceCount() > 0) {
				Reference reference = oldTask.reference(0); // fill old task parents (references)
				parents[iRefCount] = reference.container();
				indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
				operators[iRefCount] = reference.getOperator();
				reference.remove();
				model.nodeRemoved(parents[iRefCount], indices[iRefCount], reference);
				iRefCount++;
			}
			model.nodeRemoved(model.tasksNode(), index, oldTask);
			for (int i = 0; i < refCount; i++) {
				Reference check = parents[i].entry(newTask.name());
				int iref = parents[i].indexOfEntry(check);
				if (iref < 0) {
					config.insertTaskReference(parents[i], indices[i], newTask).setOperator(operators[i]);
					model.nodeInserted(parents[i], indices[i]);
				} else if (iref > indices[i]) {
					config.insertTaskReference(parents[i], indices[i], newTask).setOperator(operators[i]);
					model.nodeInserted(parents[i], indices[i]);
					check.remove();
					model.nodeRemoved(parents[i], iref, check);
				}
			}
			model.updateLevel1Nodes();
			tree.expandPath(new TreePath(model.getPathToRoot(newTask)));
			config.removeTask(oldTask);

		} else if (type.equals("SwitchProducer")) {

			SwitchProducer oldSwitchProducer = (SwitchProducer) oldContainer;
			if (oldSwitchProducer == null)
				return false;
			if (config.switchProducer(oldSwitchProducer.name()) == null)
				return false;
			SwitchProducer newSwitchProducer = config.switchProducer(newObject);
			if (newSwitchProducer == null)
				return false;

			int index = config.indexOfSwitchProducer(oldSwitchProducer);
			int refCount = oldSwitchProducer.referenceCount();
			ReferenceContainer[] parents = new ReferenceContainer[refCount];
			int[] indices = new int[refCount];
			Operator[] operators = new Operator[refCount];
			int iRefCount = 0;
			while (oldSwitchProducer.referenceCount() > 0) {
				Reference reference = oldSwitchProducer.reference(0);
				parents[iRefCount] = reference.container();
				indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
				operators[iRefCount] = reference.getOperator();
				reference.remove();
				model.nodeRemoved(parents[iRefCount], indices[iRefCount], reference);
				iRefCount++;
			}
			model.nodeRemoved(model.switchProducersNode(), index, oldSwitchProducer);
			for (int i = 0; i < refCount; i++) {
				Reference check = parents[i].entry(newSwitchProducer.name());
				int iref = parents[i].indexOfEntry(check);
				if (iref < 0) {
					config.insertSwitchProducerReference(parents[i], indices[i], newSwitchProducer)
							.setOperator(operators[i]);
					model.nodeInserted(parents[i], indices[i]);
				} else if (iref > indices[i]) {
					config.insertSwitchProducerReference(parents[i], indices[i], newSwitchProducer)
							.setOperator(operators[i]);
					model.nodeInserted(parents[i], indices[i]);
					check.remove();
					model.nodeRemoved(parents[i], iref, check);
				}
			}
			model.updateLevel1Nodes();
			tree.expandPath(new TreePath(model.getPathToRoot(newSwitchProducer)));
			config.removeSwitchProducer(oldSwitchProducer);

		} else if (type.equals("Path")) {

			Path oldPath = (Path) oldContainer;
			if (oldPath == null)
				return false;
			if (config.path(oldPath.name()) == null)
				return false;
			Path newPath = config.path(newObject);
			if (newPath == null)
				return false;

			int index = config.indexOfPath(oldPath);
			int refCount = oldPath.referenceCount();
			ReferenceContainer[] parents = new ReferenceContainer[refCount];
			int[] indices = new int[refCount];
			Operator[] operators = new Operator[refCount];
			int iRefCount = 0;
			while (oldPath.referenceCount() > 0) {
				Reference reference = oldPath.reference(0);
				parents[iRefCount] = reference.container();
				indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
				operators[iRefCount] = reference.getOperator();
				reference.remove();
				model.nodeRemoved(parents[iRefCount], indices[iRefCount], reference);
				iRefCount++;
			}
			model.nodeRemoved(model.pathsNode(), index, oldPath);
			for (int i = 0; i < refCount; i++) {
				Reference check = parents[i].entry(newPath.name());
				int iref = parents[i].indexOfEntry(check);
				if (iref < 0) {
					config.insertPathReference(parents[i], indices[i], newPath).setOperator(operators[i]);
					model.nodeInserted(parents[i], indices[i]);
				} else if (iref > indices[i]) {
					config.insertPathReference(parents[i], indices[i], newPath).setOperator(operators[i]);
					model.nodeInserted(parents[i], indices[i]);
					check.remove();
					model.nodeRemoved(parents[i], iref, check);
				}
			}
			model.updateLevel1Nodes();
			tree.expandPath(new TreePath(model.getPathToRoot(newPath)));
			//
			// newPath is added to oldPath's datasets/streams/contents
			Iterator<PrimaryDataset> itPD = oldPath.datasetIterator();
			while (itPD.hasNext())
				itPD.next().insertPath(newPath);
			Iterator<EventContent> itEC = oldPath.contentIterator();
			while (itEC.hasNext())
				newPath.addToContent(itEC.next());
			// tricky: newPath must get oldPath's [Smart]Prescales
			String newPathName = newPath.name();
			String tmpPathName = newPathName + "_X";
			while (!config.isUniqueQualifier(tmpPathName))
				tmpPathName += "X";
			try {
				newPath.setNameAndPropagate(tmpPathName);
				newPath.setName(newPathName);
				oldPath.setName(tmpPathName);
			} catch (DataException e) {
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
	public static void sortPaths(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortPaths();
		model.nodeStructureChanged(model.pathsNode());
	}

	/** sort Sequences */
	public static void sortSequences(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortSequences();
		model.nodeStructureChanged(model.sequencesNode());
	}

	/** sort Tasks */
	public static void sortTasks(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortTasks();
		model.nodeStructureChanged(model.tasksNode());
	}

	/** sort SwitchProducers */
	public static void sortSwitchProducers(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortSwitchProducers();
		model.nodeStructureChanged(model.switchProducersNode());
	}

	/** sort Modules */
	public static void sortModules(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortModules();
		model.nodeStructureChanged(model.modulesNode());
	}

	/** sort EDAliases */
	public static void sortEDAliases(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortEDAliases();
		// model.nodeStructureChanged(model.modulesNode());
	}
	
	/** sort global EDAliases */
	public static void sortGlobalEDAliases(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		config.sortGlobalEDAliases();
		model.nodeStructureChanged(model.globalEDAliasesNode());
	}

	//
	// EventContents
	//

	/** insert a new event content */
	public static boolean insertContent(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		EventContent content = config.insertContent("<ENTER EVENTCONTENT LABEL>");

		int index = config.indexOfContent(content);
		model.nodeInserted(model.contentsNode(), index);
		model.updateLevel1Nodes();

		TreePath parentPath = (index == 0) ? treePath : treePath.getParentPath();
		TreePath newTreePath = parentPath.pathByAddingChild(content);

		tree.setSelectionPath(newTreePath);
		editNodeName(tree);

		return true;
	}

	/** import event content */
	public static boolean importContent(JTree tree, EventContent external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		EventContent content = config.content(external.name());
		if (content == null)
			content = config.insertContent(external.name());
		model.nodeInserted(model.contentsNode(), config.indexOfContent(content));

		Iterator<Stream> itS = external.streamIterator();
		while (itS.hasNext()) {
			Stream stream = itS.next();
			importStream(tree, content.name(), stream);
		}

		Iterator<OutputCommand> itOC = external.commandIterator();
		while (itOC.hasNext()) {
			OutputCommand command = itOC.next();
			content.insertCommand(command);
		}

		itS = content.streamIterator();
		while (itS.hasNext()) {
			OutputModule output = itS.next().outputModule();
			PSetParameter psetSelectEvents = (PSetParameter) output.parameter("SelectEvents");
			model.nodeChanged(psetSelectEvents.parameter(0));
			if (output.referenceCount() > 0) {
				model.nodeStructureChanged(output.reference(0));
			}
		}

		model.updateLevel1Nodes();

		return true;
	}

	/** remove an existing event content */
	public static boolean removeContent(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		EventContent content = (EventContent) treePath.getLastPathComponent();
		int index = config.indexOfContent(content);

		int streamCount = 0;
		int datasetCount = 0;
		Iterator<Stream> itS = content.streamIterator();
		while (itS.hasNext()) {
			Stream stream = itS.next();
			Iterator<PrimaryDataset> itPD = stream.datasetIterator();
			while (itPD.hasNext()) {
				PrimaryDataset dataset = itPD.next();
				model.nodeRemoved(model.datasetsNode(), config.indexOfDataset(dataset) - datasetCount, dataset);
				datasetCount++;
			}
			stream.removeOutputModuleReferences();
			OutputModule output = stream.outputModule();
			model.nodeRemoved(model.streamsNode(), config.indexOfStream(stream) - streamCount, stream);
			model.nodeRemoved(model.outputsNode(), config.indexOfOutput(output) - streamCount, output);
			streamCount++;
		}

		config.removeContent(content);
		model.nodeRemoved(model.contentsNode(), index, content);
		model.updateLevel1Nodes();

		return true;
	}

	//
	// Streams
	//

	/** insert a new stream */
	public static boolean insertStream(JTree tree, Stream stream) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		int index = config.indexOfStream(stream);
		model.nodeInserted(model.streamsNode(), index);
		model.nodeInserted(stream.parentContent(), stream.parentContent().indexOfStream(stream));
		model.nodeInserted(model.outputsNode(), config.indexOfOutput(stream.outputModule()));
		model.updateLevel1Nodes();

		TreePath newTreePath = treePath.pathByAddingChild(stream);
		tree.setSelectionPath(newTreePath);

		return true;
	}

	/** import stream */
	public static boolean importStream(JTree tree, String contentName, Stream external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		EventContent content = null;

		if (contentName.equals("")) {
			if (treePath != null) {
				Object targetNode = treePath.getLastPathComponent();
				if (targetNode instanceof EventContent)
					content = (EventContent) targetNode;
			}
		} else {
			content = config.content(contentName);
		}

		if (content == null) {
			System.err.println("stream must be added to existing and selected event content!");
			return false;
		}

		Stream stream = content.stream(external.name());
		if (stream == null)
			stream = content.insertStream(external.name());

		stream.setFractionToDisk(external.fractionToDisk());
		OutputModule om = new OutputModule(external.outputModule().name(), stream);
		stream.setOutputModule(om);

		Iterator<PrimaryDataset> itPD = external.datasetIterator();
		ArrayList<PrimaryDataset> externalPDs = new ArrayList<PrimaryDataset>();
		while (itPD.hasNext()) {
			externalPDs.add(itPD.next());
		}
		for(PrimaryDataset dataset : externalPDs){
			importPrimaryDataset(tree, stream.name(), dataset);
		}

		model.nodeInserted(model.streamsNode(), config.indexOfStream(stream));
		model.updateLevel1Nodes();

		return true;
	}

	/** remove an existing stream */
	public static boolean removeStream(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		Stream stream = (Stream) treePath.getLastPathComponent();
		OutputModule output = stream.outputModule();
		EventContent content = stream.parentContent();
		int index = config.indexOfStream(stream);
		int indexOutput = config.indexOfOutput(output);
		int indexContent = content.indexOfStream(stream);

		// remove dataset nodes
		int datasetCount = 0;
		Iterator<PrimaryDataset> itPD = stream.datasetIterator();
		while (itPD.hasNext()) {
			PrimaryDataset dataset = itPD.next();
			model.nodeRemoved(model.datasetsNode(), config.indexOfDataset(dataset) - datasetCount, dataset);
			datasetCount++;
		}

		content.removeStream(stream);
		model.nodeRemoved(model.streamsNode(), index, stream);
		model.nodeRemoved(model.outputsNode(), index, output);
		if (model.contentMode().equals("streams"))
			model.nodeRemoved(content, indexContent, stream);
		model.nodeStructureChanged(model.pathsNode());
		model.nodeStructureChanged(model.sequencesNode());
		model.updateLevel1Nodes();

		return true;
	}

	public static boolean generateStreamOutputPaths(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		
		config.generateOutputPaths();
		
		model.nodeStructureChanged(model.getRoot());
		model.updateLevel1Nodes();
		
		return true;
	}
	
	/** remove a path from a stream */
	// TODO deprecated method, sharing one path in more than one dataset.
	/*
	 * public static boolean removePathFromStream(JTree tree) {
	 * ConfigurationTreeModel model = (ConfigurationTreeModel)tree.getModel();
	 * Configuration config = (Configuration)model.getRoot(); TreePath treePath =
	 * tree.getSelectionPath();
	 * 
	 * ConfigurationTreeNode treeNode =
	 * (ConfigurationTreeNode)treePath.getLastPathComponent(); Stream stream =
	 * (Stream)treeNode.parent(); Path path = (Path)treeNode.object(); int index =
	 * stream.indexOfPath(path);
	 * 
	 * EventContent content = stream.parentContent(); int contentIndex =
	 * content.indexOfPath(path);
	 * 
	 * PrimaryDataset dataset = stream.dataset(path); if (dataset!=null)
	 * model.nodeRemoved(dataset,dataset.indexOfPath(path),path);
	 * 
	 * stream.removePath(path);
	 * 
	 * if (model.contentMode().equals("paths")&&content.indexOfPath(path)<0) {
	 * model.nodeRemoved(content,content.indexOfPath(path),path); }
	 * 
	 * Iterator<Stream> itS = content.streamIterator(); while (itS.hasNext()) {
	 * OutputModule output = itS.next().outputModule(); PSetParameter
	 * psetSelectEvents = (PSetParameter)output.parameter("SelectEvents");
	 * model.nodeChanged(psetSelectEvents.parameter(0)); if
	 * (output.referenceCount()>0) model.nodeStructureChanged(output.reference(0));
	 * }
	 * 
	 * model.nodeRemoved(stream,index,treeNode); model.updateLevel1Nodes();
	 * 
	 * return true; }
	 */

	/** remove a path from a stream */
	public static boolean removePathFromStream(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		ConfigurationTreeNode treeNode = (ConfigurationTreeNode) treePath.getLastPathComponent();
		Stream stream = (Stream) treeNode.parent();
		Path path = (Path) treeNode.object();
		int index = stream.indexOfPath(path);

		EventContent content = stream.parentContent();
		int contentIndex = content.indexOfPath(path);

		// PrimaryDataset dataset = stream.dataset(path);
		ArrayList<PrimaryDataset> primaryDatasets = stream.datasets(path);

		for (int ds = 0; ds < primaryDatasets.size(); ds++) {
			PrimaryDataset dataset = primaryDatasets.get(ds);

			if (dataset != null)
				model.nodeRemoved(dataset, dataset.indexOfPath(path), path);
		}

		stream.removePath(path);

		if (model.contentMode().equals("paths") && content.indexOfPath(path) < 0) {
			model.nodeRemoved(content, content.indexOfPath(path), path);
		}

		Iterator<Stream> itS = content.streamIterator();
		while (itS.hasNext()) {
			OutputModule output = itS.next().outputModule();
			PSetParameter psetSelectEvents = (PSetParameter) output.parameter("SelectEvents");
			model.nodeChanged(psetSelectEvents.parameter(0));
			if (output.referenceCount() > 0)
				model.nodeStructureChanged(output.reference(0));
		}

		model.nodeRemoved(stream, index, treeNode);
		model.updateLevel1Nodes();

		return true;
	}

	/** remove unassigned paths from an existing stream */
	public static boolean removeUnassignedPathsFromStream(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();
		Stream stream = null;

		if (node instanceof Stream) {
			stream = (Stream) node;
		} else if (node instanceof ConfigurationTreeNode) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			stream = (Stream) treeNode.parent();
			tree.setSelectionPath(treePath.getParentPath());
		}

		EventContent content = stream.parentContent();

		ArrayList<Path> unassigned = stream.listOfUnassignedPaths();
		Iterator<Path> itP = unassigned.iterator();
		while (itP.hasNext()) {
			Path path = itP.next();
			int index = stream.indexOfPath(path);

			int contentIndex = content.indexOfPath(path);

			// PrimaryDataset dataset = stream.dataset(path);
			ArrayList<PrimaryDataset> datasets = stream.datasets(path);

			// if (dataset!=null) model.nodeRemoved(dataset,dataset.indexOfPath(path),path);
			// TODO update to count on many datasets sharing the same path.
			for (int i = 0; i < datasets.size(); i++) {
				PrimaryDataset ds = datasets.get(i);
				model.nodeRemoved(ds, ds.indexOfPath(path), path);
			}

			stream.removePath(path);

			if (model.contentMode().equals("paths") && content.indexOfPath(path) < 0) {
				model.nodeRemoved(content, content.indexOfPath(path), path);
			}

			Iterator<Stream> itS = content.streamIterator();
			while (itS.hasNext()) {
				OutputModule output = itS.next().outputModule();
				PSetParameter psetSelectEvents = (PSetParameter) output.parameter("SelectEvents");
				model.nodeChanged(psetSelectEvents.parameter(0));
				if (output.referenceCount() > 0)
					model.nodeStructureChanged(output.reference(0));
			}

			model.updateLevel1Nodes();

		}
		return true;
	}

	//
	// PrimaryDatasets
	//
	/** insert a newly created primary dataset  in the config tree model
	 * it may create a stream output path
	 * note all newly created have a dataset path on creation
	*/
	public static boolean insertPrimaryDataset(JTree tree, PrimaryDataset dataset) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		int index = config.indexOfDataset(dataset);
		model.nodeInserted(model.datasetsNode(), index);
		index = config.indexOfPath(dataset.datasetPath());
		model.nodeInserted(model.pathsNode(), index);
		
		//we may need to generate a streams output path which is triggered if it has 
		//a dataset path
		//if insertOutputPath is true, we have always created a new path and need to
		//insert it into the module
		//however if the path existed before we also have to remove it first
		//note: we dont change if its not an output path so it wont belong to streams/datasets
		//      so we dont have to worry about removing from nodes other than path
		Path oldStreamOutPath = config.path(dataset.parentStream().outputPathName());
		int oldOutPathIndex = oldStreamOutPath!=null ? config.indexOfPath(oldStreamOutPath) : -1;
		if(config.insertOutputPath(dataset.parentStream())){
			
			if(oldOutPathIndex!=-1){
				model.nodeRemoved(model.pathsNode(),oldOutPathIndex,oldStreamOutPath);
			}
			Path streamOutPath = config.path(dataset.parentStream().outputPathName());
			int outPathIndex = config.indexOfPath(streamOutPath);
			model.nodeInserted(model.pathsNode(),outPathIndex);
		}

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
	 * This method inserts an existing PrimaryDatasets into a Stream including all
	 * it's paths. This is to implement the drag and drop functionality for
	 * PrimaryDatasets between Streams. bug/feature 88066
	 */
	public static boolean insertPrimaryDatasetPathsIncluded(JTree tree, PrimaryDataset dataset) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();
		boolean inserted = true;
		boolean success = true;

		if (node instanceof Stream) {
			Stream targetStream = (Stream) node;
			PrimaryDataset newDataSet = targetStream.insertDataset(dataset.name());
			insertPrimaryDataset(tree, newDataSet);

			TreePath TargetPath = new TreePath(model.getPathToRoot(newDataSet));
			tree.setSelectionPath(TargetPath);

			for (int i = 0; i < dataset.pathCount(); i++) {
				inserted = addPathToDataset_noUpdateTreeNodes(tree, dataset.path(i).name());
				success = success && inserted;
			}

			if (!success) {
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

	public static boolean rmSplitPrimaryDatasetInstance(JTree tree, PrimaryDataset dataset, Stream stream)
	{
		ArrayList<PrimaryDataset> splitInstances  = dataset.getSplitSiblings();		
		if(splitInstances.size()==1){
			return removePrimaryDataset(tree,dataset,stream);
		}

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		PrimaryDataset pdInstance0 = splitInstances.get(0);
		//incase we are removing the first instance, the second is now the new instance 0
		if(pdInstance0==dataset){ 
			pdInstance0 = splitInstances.get(1);
		}
		int instanceNrRemoved = dataset.splitInstanceNumber();		
		if(removePrimaryDataset(tree,dataset,stream)){					
			splitInstances  = pdInstance0.getSplitSiblings();			
			pdInstance0.updateSplitInstanceNrs();
			for(PrimaryDataset pdInstance : splitInstances){
				if(pdInstance.splitInstanceNumber()>=instanceNrRemoved){					
					model.nodeChanged(pdInstance);
				}
			}

			updateSplitDatasetPathPrescales(model, config, pdInstance0, splitInstances.size(), splitInstances.size()+1);			
			return true;
		}else{
			return false;
		}



	}

	/** splits the primary dataset, dataset must be path based for this to work
	 * 
	*/
	public static boolean splitPrimaryDataset(JTree tree,String datasetName,int nrInstances)
	{
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		PrimaryDataset dataset = config.dataset(datasetName);
		ArrayList<PrimaryDataset> splitInstances  = dataset.getSplitSiblings();
		PrimaryDataset pdInstance0 = splitInstances.get(0);
		int initialNrSplitInstances = splitInstances.size();		
		//we already have the correct number of instances
		if(splitInstances.size()==nrInstances){
			return false;
		}else if(nrInstances<splitInstances.size()){			
			//now we have to remove the extra instances
			for(PrimaryDataset datasetInstance : splitInstances){
				if(datasetInstance.splitInstanceNumber()>=nrInstances){					
					removePrimaryDataset(tree,datasetInstance,datasetInstance.parentStream());
				}
			}
			if(nrInstances==1){
				//dataset is now unsplit so remove instance number from the name
				pdInstance0.setName(pdInstance0.nameWithoutInstanceNr(false));
				//need to adjust the model now
				model.nodeChanged(pdInstance0);
			}			
		}else{			
			Stream stream = pdInstance0.parentStream();
			boolean firstTimeSplit = splitInstances.size()==1;
			if(pdInstance0.splitInstanceNumber()!=0){
				String errMsg = "Dataset Split Failure, Instance0 has instance nr "+pdInstance0.splitInstanceNumber()+"\nThis is taken from the DatasetPath prescale module\nThis should not be possible and represents a logic bug \nyou should report in the TSG ConfdbDev mattermost channel";
				JOptionPane.showMessageDialog(null, errMsg, "Split Failure", JOptionPane.ERROR_MESSAGE,null);
				return false;
			}
			
			for(int instanceNr=splitInstances.size();instanceNr<nrInstances;instanceNr++){
				PrimaryDataset splitDataset = stream.insertDataset(pdInstance0.nameWithoutInstanceNr()+instanceNr);
				splitDataset.createDatasetPath(pdInstance0.pathFilter());
				TreePath streamPath = new TreePath(model.getPathToRoot(stream));
				TreePath oldPath = tree.getSelectionPath();
				tree.setSelectionPath(streamPath);
				ConfigurationTreeActions.insertPrimaryDataset(tree,splitDataset);
				tree.setSelectionPath(oldPath);
			}
			if(firstTimeSplit){
				//we are splitting dataset for the first time so add instance number to the name
				//note we are doing this after making the other datasets so primary dataset is aware it has
				//split siblings when renaming itself (and thus can properly deal with the instance number)
				//it also helps when reutrning its name for the newly split datasets above
				pdInstance0.setName(pdInstance0.name()+pdInstance0.splitInstanceNumber());
				//need to adjust the model now
				model.nodeChanged(pdInstance0);
			}
			

		}
		updateSplitDatasetPathPrescales(model,config, pdInstance0, nrInstances, initialNrSplitInstances);
		return true;

	}

	/**
	 * adjusts the values of the datasetpaths of a split dataset where approprate
	 * eg, ensuring the prescale is always equal to or greater (unless 0) of the number of datasets
	 */
	public static void updateSplitDatasetPathPrescales(ConfigurationTreeModel model,IConfiguration config,PrimaryDataset dataset,int nrInstances,int oldNrInstances){
		//now lets adjust the prescales
		PrescaleTable psTbl = new PrescaleTable(config);
		PrescaleTableModel psTblModel = new PrescaleTableModel();
		psTblModel.initialize(psTbl);
	
		int rowNr = psTbl.rowNr(dataset.datasetPathName());
		ArrayList<Long> prescales = psTbl.prescales(rowNr);
		for(int colNr=0;colNr<prescales.size();colNr++){
			Long prescale = prescales.get(colNr);
			if( (prescale<nrInstances && prescale!=0) ||
				prescale.equals(Long.valueOf(oldNrInstances))){
				psTbl.setPrescale(rowNr,colNr,nrInstances);
			}else{
				//we set it agian to automatically propagate it to any new paths
				psTbl.setPrescale(rowNr,colNr,prescale);
			}
		}			
		psTblModel.updatePrescaleService(config);
	
		ArrayList<PrimaryDataset> splitSiblings = dataset.getSplitSiblings();
		for(PrimaryDataset sibling : splitSiblings){
			model.nodeChanged(sibling);
		}

	}


	/**
	 * converts all datasets of a config to the new path based ones
	 * does this by just generating the DatasetPath
	 * in general this is just to ease migration and will become redudant in time
	 */

	public static boolean convertPDsToPathBased(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		Iterator<PrimaryDataset> datasetIt = config.datasetIterator();
		while(datasetIt.hasNext()){
			PrimaryDataset dataset = datasetIt.next();
			dataset.createDatasetPath();
		}		
		//now we need to make the output path as its triggered by path based datasets
		config.generateOutputPaths();
		
		model.nodeStructureChanged(model.getRoot());
		model.updateLevel1Nodes();
		
		return true;
	}
	



	/**
	 * this function has been re-worked with the big dataset update
	 * 1) it now moves rather than deletes then adds (easier to deal with the dataset + dataset path)
	 * 2) it only works when the source and target tree is identical, so far its not thought
	 *    there is a case where they are different, it would need to be reworked for this
	 * 3) because of contraint 2, we no longer have to worry about moving paths over etc 
	 *    which simplifies the logic
	 * 4) finally this path assumes that a PrimaryDataset can belong to exactly 1 stream
	 */
	public static boolean movePrimaryDataset(JTree sourceTree, JTree targetTree, PrimaryDataset dataset) {

		if(sourceTree!=targetTree){
			String errMsg = "Move Primary Dataset Failed";
			String moreDetail = "We are currently constrained only to move between the same config and not across configs.\nPlease ping the TSG mattermost (ConfdbDev or HLT User Support) to report this issue as we thought cross config PD moving was not required";

			errorNotificationPanel cd = new errorNotificationPanel("ERROR", errMsg, moreDetail);
			cd.createAndShowGUI();
			return false;
		}
		
		//Datasets only exist as part of a stream in the config
		//1) remove the dataset from source stream, this will remove it from the config but remove
		//   the dataset path
		//2) add the dataset to the target stream, it will add it back to the config but also not 
		//   touch the dataset path
		//3) we now need to adjust the model, specifically we need to
		//   a) remove the dataset from source stream
		//   b) add the dataset in the target stream		
		//   c) no change is needed to the dataset as its alphabetical 
		//   d) however we do need to update the event content dataset /path view as the order of
		//      is stream based. Dataset is not so bad but paths have massive changes therefore
		//      we will reset it
	


		TreePath targetPath = targetTree.getSelectionPath();
		Object targetNode = targetPath.getLastPathComponent();		
		ConfigurationTreeModel model = (ConfigurationTreeModel) targetTree.getModel();
		Configuration config = (Configuration) model.getRoot();

		Stream targetStream = null;
		if (targetNode instanceof Stream) {
			targetStream = (Stream) targetNode;			
		} else if (targetNode instanceof ConfigurationTreeNode) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) targetNode;			
			targetStream = (Stream) treeNode.object();
		//	tree.setSelectionPath(treePath.getParentPath());
		}
		Stream sourceStream = dataset.parentStream();
		//we are disabling dataset moving accross event contents when they have 
		//siblings as it would change the status of split/clone
		//we can support this but in a rush so disabling for now till we can put the logic in
		//which properly handles the changing of a clone to split of an instance
		if(sourceStream.parentContent()!=targetStream.parentContent() && 
			dataset.getSiblings().size()!=1){
			String msg = "A dataset with siblings can not currently move between streams with different event content.\n Please remove the siblings or clone to a different stream.\n We can impliment this feature if needed, report it in the TSG ConfdbDev Mattermost channel";
			JOptionPane.showMessageDialog(null, msg, "", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		int sourceIndex = dataset.parentStream().indexOfDataset(dataset);
		dataset.parentStream().removeDataset(dataset);
		targetStream.insertDataset(dataset);
		int targetIndex = dataset.parentStream().indexOfDataset(dataset);
		model.nodeRemoved(sourceStream, sourceIndex, dataset);
		model.nodeInserted(targetStream, targetIndex);

		//we now need to check if we had to make or remake the streams output path
		Path oldStreamOutPath = config.path(dataset.parentStream().outputPathName());
		int oldOutPathIndex = oldStreamOutPath!=null ? config.indexOfPath(oldStreamOutPath) : -1;
		if(config.insertOutputPath(dataset.parentStream())){
			
			if(oldOutPathIndex!=-1){
				model.nodeRemoved(model.pathsNode(),oldOutPathIndex,oldStreamOutPath);
			}
			Path streamOutPath = config.path(dataset.parentStream().outputPathName());
			int outPathIndex = config.indexOfPath(streamOutPath);
			model.nodeInserted(model.pathsNode(),outPathIndex);
		}

		model.nodeStructureChanged(model.contentsNode());
		model.updateLevel1Nodes();
		return true;
	}

	/**
	 * movePathsBetweenDatasets Moves a path from one primary data set to another.
	 * If datasets are not contained in the same stream it will remove the path from
	 * the source P. Dataset leaving the path in the source stream.
	 * 
	 * Uses targetTree to retrieve the targetComponent
	 * 
	 * @see ConfigurationTreeDropTarget.java, ConfigurationTreeTransferHandler.java
	 *      bug #82526: add/remove path to/from a primary datase
	 */
	public static boolean movePathsBetweenDatasets(JTree sourceTree, JTree targetTree, ConfigurationTreeNode pathNode) {
		TreePath targetPath = targetTree.getSelectionPath();
		Object targetNode = targetPath.getLastPathComponent();
		ConfigurationTreeModel sourceModel = (ConfigurationTreeModel) sourceTree.getModel();
		Configuration config = (Configuration) sourceModel.getRoot();

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
	public static boolean importPrimaryDataset(JTree tree, String streamName, PrimaryDataset external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		Stream stream = null;

		if (streamName.equals("")) {
			if (treePath != null) {
				Object targetNode = treePath.getLastPathComponent();
				if (targetNode instanceof Stream)
					stream = (Stream) targetNode;
				else
					return false;
			}
		} else {
			stream = config.stream(streamName);
		}

		if (stream == null) {
			System.err.println("dataset must be added to existing stream!");
			return false;
		}

		PrimaryDataset dataset = null;
		boolean updatePaths = false;

		ModuleInstance externalPathFilter = external.pathFilterMod();
		if(externalPathFilter == null){
			//old style dataset
			if( config.isUniqueQualifier(external.pathFilterDefaultName()) && 
				config.isUniqueQualifier(external.datasetPathName()) &&
			   	config.dataset(external.name())==null)  
			   {				
				dataset = stream.insertDataset(external.name());
				dataset.createDatasetPath();
				updatePaths = true;
			}else{
				System.err.println("error the dataset "+external.name()+", its datasetpath or its datasetpathfilter already exists in the menu");
				return false;
			}
		}else{
			//new style dataset which may be split
			ModuleInstance datasetPathFilterMod = config.module(externalPathFilter.name());
			String name = null;
			PrimaryDataset instance0ToRename = null;

			if (datasetPathFilterMod != null) {
				ArrayList<PrimaryDataset> splitSiblings = config.getDatasetsWithFilter(datasetPathFilterMod);
				final EventContent content = stream.parentContent();
				splitSiblings.removeIf((PrimaryDataset d) -> d.parentStream().parentContent() != content);
				if (!splitSiblings.isEmpty() && external.getSplitSiblings().size()>1) {
					int instanceNr = splitSiblings.size();
					name = external.nameWithoutInstanceNr() + instanceNr;	
					if(instanceNr==1){
						//dataset is being split for the first time, we'll need to rename the 0th instance
						instance0ToRename = splitSiblings.get(0);
					}
				} else {
					name = external.nameWithoutInstanceNr();					
				}
			} else {
				name = external.nameWithoutInstanceNr();
				updatePaths = true;
			}
				
			if(config.dataset(name)==null && config.isUniqueQualifier(PrimaryDataset.datasetPathName(name))){
				dataset = stream.insertDataset(name);
				dataset.createDatasetPath(config.getDatasetPathFilter(datasetPathFilterMod));
				if(instance0ToRename!=null){
					instance0ToRename.setName(instance0ToRename.name() + instance0ToRename.splitInstanceNumber());
					model.nodeChanged(instance0ToRename);
				}
			}else{
				System.err.println("error dataset "+name+" from source pd "+external.name()+"already exists as a dataset or dataset path. Note can only add to dataset instances if source was also split");
				return false;
			}
		}
				
		TreePath streamPath = new TreePath(model.getPathToRoot(stream));
		TreePath oldPath = tree.getSelectionPath();
		tree.setSelectionPath(streamPath);
		ConfigurationTreeActions.insertPrimaryDataset(tree,dataset);
		tree.setSelectionPath(oldPath);	
				
		if(updatePaths) {
			Iterator<Path> itP = external.pathIterator();
			while (itP.hasNext()) {
				String pathName = itP.next().name();
				Path path = config.path(pathName);
				if (path == null) {
					System.out.println("importPrimaryDataset: skip path " + pathName);
					continue;
				}
				dataset.insertPath(path);
			}
		}		

		return true;
	}

	/** remove an existing primary dataset */
	public static boolean removePrimaryDataset(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();		
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();
		PrimaryDataset dataset = null;
		Stream stream = null;

		if (node instanceof PrimaryDataset) {
			dataset = (PrimaryDataset) node;
			stream = dataset.parentStream();
		} else if (node instanceof ConfigurationTreeNode) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			dataset = (PrimaryDataset) treeNode.object();
			stream = (Stream) treeNode.parent();
			tree.setSelectionPath(treePath.getParentPath());
		}
		//so here we have to treat removing a split instance of a dataset diffently
		//if we remove one instance, we should adjust the numbering of all others
		int nrSplitSiblings = dataset.getSplitSiblings().size();
		if(nrSplitSiblings>1){
			return rmSplitPrimaryDatasetInstance(tree, dataset, stream);
		}else{
			return removePrimaryDataset(tree,dataset,stream);
		}
	}

	/** removes the primary dataset when given a dataset and stream 
	 * note the stream seems redundant as should be dataset.parentStream
	 * but in the spirit of not changing too much from the original function
	 * we will pass in the stream and if null set it to the parentStream
	 * 
	*/
	public static boolean removePrimaryDataset(JTree tree,PrimaryDataset dataset,Stream stream) {
		
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		if(stream==null){
			stream = dataset.parentStream();
		}

		int index = config.indexOfDataset(dataset);
		int indexStream = stream.indexOfDataset(dataset);

		//we need the node index to set the correct tree path later
		TreePath treePath = tree.getSelectionPath();
		Object selected = treePath.getLastPathComponent();
		int selectedIndex = -1;
		if(selected instanceof PrimaryDataset){
			selectedIndex = config.indexOfDataset((PrimaryDataset) selected);
		}

		stream.removeDataset(dataset);
		model.nodeRemoved(model.datasetsNode(), index, dataset);
		model.nodeRemoved(stream, indexStream, dataset);
		if(dataset.datasetPath()!=null){			
			Path datasetPath = dataset.datasetPath();
			int indxDatasetPath = config.indexOfPath(datasetPath);
			config.removePath(datasetPath);			
			model.nodeRemoved(model.pathsNode(),indxDatasetPath,datasetPath);
			dataset.removeDatasetPath();
		}
		
		

		model.nodeStructureChanged(model.contentsNode());		
		model.updateLevel1Nodes();

		//so now we update the model
		//updating l1 nodes clears our selection so we need to get it back
		//we also need to figure out which we select too
		//basically if our dataset was the node being removed, its now points to the index below that
		//however if not, we need to figure out the new index of that node	
		treePath = new TreePath(model.getPathToRoot(model.datasetsNode()));	
		if(selectedIndex == index){		
			tree.setSelectionPath(treePath.pathByAddingChild(model.getChild(model.datasetsNode(), index-1)));
		}else if(selectedIndex!=-1 && selectedIndex < index){
			tree.setSelectionPath(treePath.pathByAddingChild(model.getChild(model.datasetsNode(), selectedIndex)));
		}else if(selectedIndex> index){
			tree.setSelectionPath(treePath.pathByAddingChild(model.getChild(model.datasetsNode(), selectedIndex-1)));
		}
		
		return true;
	}

	/**
	 * Add a path to a primary dataset This method is used to add a single path to a
	 * Primary dataset. This is not used to insert paths from the EditDatasetDialog
	 * panel.
	 */
	public static boolean addPathToDataset(JTree tree, String name) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		PrimaryDataset dataset = null;
		Stream stream = null;
		Path path = null;

		if (node instanceof PrimaryDataset) {
			dataset = (PrimaryDataset) node;
			stream = dataset.parentStream();
			path = config.path(name);
		} else if (node instanceof ConfigurationTreeNode) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			if (treeNode.parent() instanceof ConfigurationTreeNode) {
				// REMOVING PATH FROM DATASET - FROM DATASET LIST.
				ConfigurationTreeNode parentNode = (ConfigurationTreeNode) treeNode.parent();
				path = (Path) treeNode.object();
				stream = (Stream) parentNode.parent();
				dataset = stream.dataset(name);
				tree.setSelectionPath(treePath.getParentPath());
			} else if (treeNode.parent() instanceof PrimaryDataset) {
				// REMOVING PATH FROM DATASET - FROM STREAM LIST.
				String pathName = "";
				if (treeNode.object() instanceof Path) {
					pathName = ((Path) treeNode.object()).name();
				} else
					System.err.println(
							"[ConfigurationTreeActions.java][addPathToDataset] ERROR: TreeNode is not instance of Path");
				dataset = config.dataset(name);
				stream = dataset.parentStream();
				path = config.path(pathName);
			}
		} else if (node instanceof Path) {
			// REMOVING PATH FROM DATASET - FROM PATH LIST.
			path = (Path) node;
			dataset = config.dataset(name);
			stream = dataset.parentStream();
		}

		
		// datasets can be linked to each other, they will all update automatically
		// but we need to adjust their nodes individually
		if (dataset.path(path.name()) == null) {
			dataset.insertPath(path); //adds it to all paths
			int index = dataset.indexOfPath(path);
			ArrayList<PrimaryDataset> siblings = dataset.getSiblings();			
			for(PrimaryDataset sibling : siblings) {	
				model.nodeInserted(sibling, index);
				if (model.streamMode().equals("datasets")) {
					
					model.nodeInserted(model.getChild(sibling.parentStream(), sibling.parentStream().indexOfDataset(sibling)), index);					
				}
			}
		}		
		model.nodeChanged(path);
		model.updateLevel1Nodes();

		// Feature/Bug 86605
		ArrayList<Path> newPaths = new ArrayList<Path>(); // Needed for method definition.
		newPaths.add(path);
		updateFilter(config, stream, newPaths); // To copy update prescaler.

		return true;
	}

	/**
	 * Add a path to a primary dataset So far, this method is used to add a single
	 * path to a Primary dataset. NOTE: This method will not update the TreeNodes
	 * since this is to be used in a loop for multiple path insertions. bug/feature
	 * 88066.
	 */
	private static boolean addPathToDataset_noUpdateTreeNodes(JTree tree, String name) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		PrimaryDataset dataset = null;
		Stream stream = null;
		Path path = null;

		if (node instanceof PrimaryDataset) {
			dataset = (PrimaryDataset) node;
			stream = dataset.parentStream();
			path = config.path(name);
		} else if (node instanceof ConfigurationTreeNode) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			ConfigurationTreeNode parentNode = (ConfigurationTreeNode) treeNode.parent();
			path = (Path) treeNode.object();
			stream = (Stream) parentNode.parent();
			dataset = stream.dataset(name);
			tree.setSelectionPath(treePath.getParentPath());
		}

		boolean inserted = dataset.insertPath(path);		
		if (inserted) {
			int index = dataset.indexOfPath(path);
			ArrayList<PrimaryDataset> siblings = dataset.getSiblings();			
			
			for(PrimaryDataset sibling : siblings) {	
				model.nodeInserted(sibling, index );
				if (model.streamMode().equals("datasets")) {
					model.nodeInserted(model.getChild(sibling.parentStream(), sibling.parentStream().indexOfDataset(sibling)), index);
				}
				// model.nodeChanged(path);
				// model.updateLevel1Nodes();

				// Feature/Bug 86605
				if(dataset.datasetPath()==null){
					ArrayList<Path> newPaths = new ArrayList<Path>(); // Needed for method definition.
					newPaths.add(path);
					updateFilter(config, stream, newPaths); // To copy update prescaler.
				}
			}
		} else {
			// This will invoke an errorNotificationPanel.
			return false;
		}

		return true;
	}

	/** remove a path from its parent dataset 
	 * note while a path removes automatically from any sibling datasets
	 * we need to update the model
	 * likewise we need to adjust the event content and streams
	*/
	public static boolean removePathFromDataset(JTree tree) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		ConfigurationTreeNode treeNode = (ConfigurationTreeNode) treePath.getLastPathComponent();

		if (treeNode.parent() instanceof PrimaryDataset) {
			PrimaryDataset dataset = (PrimaryDataset) treeNode.parent();
			
			Path path = (Path) treeNode.object();
			int index = dataset.indexOfPath(path);

			//if the streams / content node is displaying paths, we need the path indices in the 
			//to be able to remove them
			ArrayList<PrimaryDataset.StreamIndexPair> streamPathIndices = model.streamMode().equals("paths") ? dataset.getSiblingsStreamsIndexOfPath(path) : null;
			ArrayList<PrimaryDataset.ContentIndexPair> contentPathIndices = model.contentMode().equals("paths") ? dataset.getSiblingsContentsIndexOfPath(path) : null;
						
			dataset.removePath(path);

			ArrayList<PrimaryDataset> siblings = dataset.getSiblings();
			for(PrimaryDataset sibling : siblings){
				model.nodeRemoved(sibling, index, treeNode);
				Stream stream = sibling.parentStream();
				EventContent content = stream.parentContent();
				if (model.streamMode().equals("datasets")) {
					model.nodeRemoved(model.getChild(stream, stream.indexOfDataset(sibling)), index, treeNode);
				}
				if (model.contentMode().equals("datasets")) {
					model.nodeRemoved(model.getChild(content, content.indexOfDataset(sibling)), index, treeNode);
				}
			}

			
			if (streamPathIndices!=null){
				for(PrimaryDataset.StreamIndexPair streamPathIndex : streamPathIndices){
					if(streamPathIndex.stream.indexOfPath(path)==-1){
						model.nodeRemoved(streamPathIndex.stream,streamPathIndex.index,treeNode);
					}
				}												
			}
			if (contentPathIndices!=null){
				for(PrimaryDataset.ContentIndexPair contentPathIndex : contentPathIndices){
					if(contentPathIndex.content.indexOfPath(path)==-1){
						model.nodeRemoved(contentPathIndex.content,contentPathIndex.index,treeNode);
					}
				}												
			}

		} else {
			// Unnasigned path.
			// this should no longer be possible to call

			Stream stream = (Stream) ((ConfigurationTreeNode) treeNode.parent()).parent();
			EventContent content = stream.parentContent();
			Path path = (Path) treeNode.object();
			int index = stream.listOfUnassignedPaths().indexOf(path);

			stream.removePath(path);

			if (model.contentMode().equals("paths") && content.indexOfPath(path) < 0) {
				model.nodeRemoved(content, content.indexOfPath(path), path);
			}

			Iterator<Stream> itS = content.streamIterator();
			while (itS.hasNext()) {
				OutputModule output = itS.next().outputModule();
				PSetParameter psetSelectEvents = (PSetParameter) output.parameter("SelectEvents");
				model.nodeChanged(psetSelectEvents.parameter(0));
				if (output.referenceCount() > 0)
					model.nodeStructureChanged(output.reference(0));
			}

			model.nodeRemoved(treeNode.parent(), index, treeNode);

		}
		model.updateLevel1Nodes();

		return true;
	}

	/**
	 * remove a path from the given dataset. bug #82526: add/remove path to/from a
	 * primary dataset
	 */
	public static boolean removePathFromDataset(JTree tree, String datasetName) {

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		Path path = (Path) treePath.getLastPathComponent();
		PrimaryDataset dataset = config.dataset(datasetName);
		Stream stream = dataset.parentStream();
		int index = dataset.indexOfPath(path);

		dataset.removePath(path);

		model.nodeRemoved(dataset, index, path);
		if (model.streamMode().equals("datasets"))
			model.nodeRemoved(model.getChild(stream, stream.indexOfDataset(dataset)), index, path);

		// Only if the path goes to unassignedPaths.
		if (stream.datasets(path).size() == 0)
			model.nodeInserted(model.getChild(stream, stream.datasetCount()),
					stream.listOfUnassignedPaths().indexOf(path));
		model.updateLevel1Nodes();

		return true;
	}

	/** move a path from one dataset to another within the same stream */
	public static boolean movePathToDataset(JTree tree, String targetDatasetName) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		ConfigurationTreeNode treeNode = (ConfigurationTreeNode) treePath.getLastPathComponent();
		PrimaryDataset sourceDataset = (PrimaryDataset) treeNode.parent();
		Stream parentStream = sourceDataset.parentStream();
		PrimaryDataset targetDataset = parentStream.dataset(targetDatasetName);
		if (targetDataset == null) {
			System.err.println("ConfigurationTreeActions.movePathToDataset ERROR: " + targetDatasetName
					+ " dataset not found in stream " + parentStream);
		}

		Path path = (Path) treeNode.object();
		int sourceIndex = sourceDataset.indexOfPath(path);
		sourceDataset.removePath(path);
		targetDataset.insertPath(path);
		int targetIndex = targetDataset.indexOfPath(path);

		model.nodeRemoved(sourceDataset, sourceIndex, treeNode);
		model.nodeInserted(targetDataset, targetIndex);
		model.updateLevel1Nodes();

		return true;
	}

	//
	// generic functions
	//

	/**
	 * insert a node into the tree and add the respective component to the
	 * configuration
	 */
	private static boolean insertInstance(JTree tree, Template template) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		String templateName = template.name();
		String instanceName = templateName;
		int count = 2;
		while (template.hasInstance(instanceName)) {
			instanceName = templateName + count;
			count++;
		}

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		Instance instance = null;
		Object parent = null;

		if (template instanceof EDSourceTemplate) {
			instance = config.insertEDSource(templateName);
			parent = model.edsourcesNode();
		} else if (template instanceof ESSourceTemplate) {
			instance = config.insertESSource(index, templateName, instanceName);
			parent = model.essourcesNode();
		} else if (template instanceof ESModuleTemplate) {
			instance = config.insertESModule(index, templateName, instanceName);
			parent = model.esmodulesNode();
		} else if (template instanceof ServiceTemplate) {
			instance = config.insertService(index, templateName);
			parent = model.servicesNode();
		} else
			return false;

		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();

		TreePath newTreePath = (index == 0) ? treePath.pathByAddingChild(instance)
				: treePath.getParentPath().pathByAddingChild(instance);
		tree.expandPath(newTreePath.getParentPath());
		tree.setSelectionPath(newTreePath);

		if (instance instanceof ESSourceInstance || instance instanceof ESModuleInstance) {
			editNodeName(tree);
		}

		return true;
	}

	/** insert copy of an existing instance to configuration and tree */
	private static boolean insertCopy(JTree tree, Instance original) {
		if (!(original instanceof ESSourceInstance) && !(original instanceof ESModuleInstance))
			return false;

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		Template template = original.template();
		String templateName = template.name();
		String instanceName = "copy_of_" + original.name();
		int count = 2;
		while (template.hasInstance(instanceName)) {
			instanceName = templateName + count;
			count++;
		}

		int index = (treePath.getPathCount() == 2) ? 0
				: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
						treePath.getLastPathComponent()) + 1;

		Instance instance = null;
		Object parent = null;

		if (template instanceof ESSourceTemplate) {
			instance = config.insertESSource(index, templateName, instanceName);
			parent = model.essourcesNode();
		} else if (template instanceof ESModuleTemplate) {
			instance = config.insertESModule(index, templateName, instanceName);
			parent = model.esmodulesNode();
		} else
			return false;

		Iterator<Parameter> itP = original.parameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			instance.updateParameter(p.name(), p.type(), p.valueAsString());
		}

		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();

		TreePath newTreePath = (index == 0) ? treePath.pathByAddingChild(instance)
				: treePath.getParentPath().pathByAddingChild(instance);
		tree.expandPath(newTreePath.getParentPath());
		tree.setSelectionPath(newTreePath);

		if (instance instanceof ESSourceInstance || instance instanceof ESModuleInstance) {
			editNodeName(tree);
		}

		return true;
	}

	/**
	 * replace a module with the external one
	 */
	public static boolean replaceModule(Configuration config, JTree tree, ModuleInstance external) {
		if ((config == null) && (tree == null))
			return false;
		if ((config != null) && (tree != null))
			return false;

		ConfigurationTreeModel model = null;

		boolean updateModel = (tree != null);

		if (updateModel) {
			model = (ConfigurationTreeModel) tree.getModel();
			config = (Configuration) model.getRoot();
		}

		ModuleInstance oldModule = config.module(external.name());
		if (oldModule == null)
			return false;

		int index = config.indexOfModule(oldModule);
		int refCount = oldModule.referenceCount();
		Operator[] operators = new Operator[refCount];
		ReferenceContainer[] parents = new ReferenceContainer[refCount];
		int[] indices = new int[refCount];
		int iRefCount = 0;
		while (oldModule.referenceCount() > 0) {
			Reference reference = oldModule.reference(0);
			operators[iRefCount] = reference.getOperator();
			parents[iRefCount] = reference.container();
			indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
			config.removeModuleReference((ModuleReference) reference);
			if (updateModel)
				model.nodeRemoved(parents[iRefCount], indices[iRefCount], reference);
			iRefCount++;
		}

		if (updateModel)
			model.nodeRemoved(model.modulesNode(), index, oldModule);

		try {
			ModuleTemplate template = (ModuleTemplate) config.release().moduleTemplate(external.template().name());
			ModuleInstance newModule = (ModuleInstance) template.instance(external.name());
			for (int i = 0; i < newModule.parameterCount(); i++)
				newModule.updateParameter(i, external.parameter(i).valueAsString());
			newModule.setDatabaseId(external.databaseId());
			config.insertModule(index, newModule);

			if (updateModel)
				model.nodeInserted(model.modulesNode(), index);

			for (int i = 0; i < refCount; i++) {
				config.insertModuleReference(parents[i], indices[i], newModule).setOperator(operators[i]);
				if (updateModel)
					model.nodeInserted(parents[i], indices[i]);
			}

			if (updateModel) {
				model.updateLevel1Nodes();
				tree.expandPath(new TreePath(model.getPathToRoot(newModule)));
			}

		} catch (DataException e) {
			System.err.println("replaceModule() FAILED: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * replace a EDAlias with the external one
	 */
	public static boolean replaceEDAlias(Configuration config, JTree tree, EDAliasInstance external) {
		if ((config == null) && (tree == null))
			return false;
		if ((config != null) && (tree != null))
			return false;

		ConfigurationTreeModel model = null;

		boolean updateModel = (tree != null);

		if (updateModel) {
			model = (ConfigurationTreeModel) tree.getModel();
			config = (Configuration) model.getRoot();
		}

		EDAliasInstance oldEDAlias = config.edAlias(external.name());
		if (oldEDAlias == null)
			return false;

		int index = config.indexOfEDAlias(oldEDAlias);
		int refCount = oldEDAlias.referenceCount();
		Operator[] operators = new Operator[refCount];
		ReferenceContainer[] parents = new ReferenceContainer[refCount];
		int[] indices = new int[refCount];
		int iRefCount = 0;
		while (oldEDAlias.referenceCount() > 0) {
			Reference reference = oldEDAlias.reference(0);
			operators[iRefCount] = reference.getOperator();
			parents[iRefCount] = reference.container();
			indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
			config.removeEDAliasReference((EDAliasReference) reference);
			if (updateModel)
				model.nodeRemoved(parents[iRefCount], indices[iRefCount], reference);
			iRefCount++;
		}

		try {
			EDAliasInstance newEDAlias = new EDAliasInstance(external.name());
			for (int i = 0; i < external.parameterCount(); i++){
				Parameter param = external.parameter(i);
				newEDAlias.updateParameter(param);
			}
			newEDAlias.setDatabaseId(external.databaseId());
			config.insertEDAlias(index, newEDAlias);

			for (int i = 0; i < refCount; i++) {
				config.insertEDAliasReference(parents[i], indices[i], newEDAlias).setOperator(operators[i]);
				if (updateModel)
					model.nodeInserted(parents[i], indices[i]);
			}

			if (updateModel) {
				model.updateLevel1Nodes();
				tree.expandPath(new TreePath(model.getPathToRoot(newEDAlias)));
			}

		} catch (DataException e) {
			System.err.println("replaceEDAlias() FAILED: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * replace a global EDAlias with the external one
	 */
	public static boolean replaceGlobalEDAlias(Configuration config, JTree tree, EDAliasInstance external) {
		if ((config == null) && (tree == null))
			return false;
		if ((config != null) && (tree != null))
			return false;

		ConfigurationTreeModel model = null;

		boolean updateModel = (tree != null);

		if (updateModel) {
			model = (ConfigurationTreeModel) tree.getModel();
			config = (Configuration) model.getRoot();
		}

		EDAliasInstance oldGlobalEDAlias = config.globalEDAlias(external.name());
		if (oldGlobalEDAlias == null)
			return false;

		int index = config.indexOfGlobalEDAlias(oldGlobalEDAlias);
		
		if (updateModel) model.nodeRemoved(model.globalEDAliasesNode(), index, oldGlobalEDAlias);
		
		try {
			EDAliasInstance newGlobalEDAlias = new EDAliasInstance(external.name());
			for (int i = 0; i < newGlobalEDAlias.parameterCount(); i++)
				newGlobalEDAlias.updateParameter(i, external.parameter(i).valueAsString());
			newGlobalEDAlias.setDatabaseId(external.databaseId());
			config.insertGlobalEDAlias(index, newGlobalEDAlias);
			 
			if (updateModel) {
				model.nodeInserted(model.globalEDAliasesNode(), index);
				model.updateLevel1Nodes();
				tree.expandPath(new TreePath(model.getPathToRoot(newGlobalEDAlias)));
			}

		} catch (DataException e) {
			System.err.println("replaceGlobalEDAlias() FAILED: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * import a node into the tree and add the respective component to the
	 * configuration
	 */
	public static boolean importInstance(JTree tree, Instance external) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath();

		if ((external instanceof EDSourceInstance) && config.edsource(external.name()) != null
				|| (external instanceof ESSourceInstance) && config.essource(external.name()) != null
				|| (external instanceof ESModuleInstance) && config.esmodule(external.name()) != null
				|| (external instanceof ServiceInstance) && config.service(external.name()) != null) {
			return replaceInstance(tree, external, true);
		}

		if (!config.isUniqueQualifier(external.name()))
			return false;

		int index = (treePath == null) ? 0
				: (treePath.getPathCount() == 2) ? 0
						: model.getIndexOfChild(treePath.getParentPath().getLastPathComponent(),
								treePath.getLastPathComponent()) + 1;

		String templateName = external.template().name();
		String instanceName = external.name();
		Instance instance = null;
		Object parent = null;

		if (external instanceof EDSourceInstance) {
			instance = config.insertEDSource(templateName);
			parent = model.edsourcesNode();
		} else if (external instanceof ESSourceInstance) {
			instance = config.insertESSource(index, templateName, instanceName);
			parent = model.essourcesNode();
		} else if (external instanceof ESModuleInstance) {
			instance = config.insertESModule(index, templateName, instanceName);
			parent = model.esmodulesNode();
		} else if (external instanceof ServiceInstance) {
			instance = config.insertService(index, templateName);
			parent = model.servicesNode();
		} else
			return false;

		for (int i = 0; i < instance.parameterCount(); i++)
			instance.updateParameter(i, external.parameter(i).valueAsString());
		instance.setDatabaseId(external.databaseId());

		model.nodeInserted(parent, index);
		model.updateLevel1Nodes();

		return true;
	}

	/**
	 * Import All instances.
	 */
	public static boolean ImportAllInstances(JTree tree, JTree sourceTree, Object external) {
		ConfigurationTreeModel sm = (ConfigurationTreeModel) sourceTree.getModel();
		ConfigurationTreeModel tm = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) tm.getRoot();

		if (sm.getChildCount(external) == 0) {
			String error = "[confdb.gui.ConfigurationTreeActions.ImportAllInstances] ERROR: Child count == 0";
			System.err.println(error);
			return false;
		}

		// Checks if any item already exist.
		boolean existance = false;

		for (int i = 0; i < sm.getChildCount(external); i++) {
			Instance instance = (Instance) sm.getChild(external, i);
			if ((instance instanceof EDSourceInstance) && config.edsource(instance.name()) != null
					|| (instance instanceof ESSourceInstance) && config.essource(instance.name()) != null
					|| (instance instanceof ESModuleInstance) && config.esmodule(instance.name()) != null
					|| (instance instanceof ServiceInstance) && config.service(instance.name()) != null) {

				existance = true;
				break;
			}
		}

		boolean updateAll = false;
		if (existance) {
			int choice = JOptionPane.showConfirmDialog(null,
					" Some Items may already exist. " + "Do you want to overwrite them All?", "Overwrite all",
					JOptionPane.YES_NO_CANCEL_OPTION);

			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			updateAll = (choice == JOptionPane.YES_OPTION);
		}

		ImportAllInstancesThread worker = new ImportAllInstancesThread(tree, sm, external, updateAll);
		WorkerProgressBar wpb = new WorkerProgressBar("Importing Instances...", worker);
		wpb.createAndShowGUI();

		return true;
	}

	/**
	 * Update All ESModules. NOTE: ESModules are instances of Instance. This
	 * function won't be used anymore.
	 */
	public static boolean UpdateAllModules(JTree tree, JTree sourceTree, Object external) {
		ConfigurationTreeModel sm = (ConfigurationTreeModel) sourceTree.getModel();

		if (sm.getChildCount(external) == 0) {
			String error = "[confdb.gui.ConfigurationTreeActions.UpdateAllESModules] ERROR: Child count == 0";
			System.err.println(error);
			return false;
		}

		if (sm.getChildCount(external) > 0) {
			Instance instance = (Instance) sm.getChild(external, 0);
			if (!(instance instanceof ModuleInstance)) {
				System.err.println("[confdb.gui.ConfigurationTreeActions.UpdateAllModules] ERROR: type mismatch!");
				return false;
			}
		} else
			return false;

		int choice = JOptionPane.showConfirmDialog(null,
				" Items shared by both configurations will be overwritten.\n" + "Do you want to update them?",
				"update all", JOptionPane.YES_NO_OPTION);

		if (choice == JOptionPane.NO_OPTION)
			return false;

		UpdateAllModulesThread worker = new UpdateAllModulesThread(tree, sm, external);
		WorkerProgressBar wpb = new WorkerProgressBar("Updating all Modules", worker);
		wpb.createAndShowGUI();

		return true;
	}
	
	
	/**
	 * Update All EDAliases.
	 */
	public static boolean UpdateAllEDAliases(JTree tree, JTree sourceTree, Object external) {
		ConfigurationTreeModel sm = (ConfigurationTreeModel) sourceTree.getModel();

		if (sm.getChildCount(external) == 0) {
			String error = "[confdb.gui.ConfigurationTreeActions.UpdateAllEDAliases] ERROR: Child count == 0";
			System.err.println(error);
			return false;
		}

		if (sm.getChildCount(external) > 0) {
			Instance instance = (Instance) sm.getChild(external, 0);
			if (!(instance instanceof EDAliasInstance)) {
				System.err.println("[confdb.gui.ConfigurationTreeActions.UpdateAllEDAliases] ERROR: type mismatch!");
				return false;
			}
		} else
			return false;

		int choice = JOptionPane.showConfirmDialog(null,
				" Items shared by both configurations will be overwritten.\n" + "Do you want to update them?",
				"update all", JOptionPane.YES_NO_OPTION);

		if (choice == JOptionPane.NO_OPTION)
			return false;

		UpdateAllEDAliasesThread worker = new UpdateAllEDAliasesThread(tree, sm, external);
		WorkerProgressBar wpb = new WorkerProgressBar("Updating all EDAliases", worker);
		wpb.createAndShowGUI();

		return true;
	}

	/*
	 * replace an existing instance with the external one
	 */
	public static boolean replaceInstance(JTree tree, Instance external, boolean updateModel) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		Object parent = null;
		Instance oldInst = null;
		Instance newInst = null;
		int index = -1;

		if (external instanceof EDSourceInstance) {
			if (updateModel)
				parent = model.edsourcesNode();
			oldInst = config.edsource(external.name());
			index = 0;
			config.removeEDSource((EDSourceInstance) oldInst);
			if (updateModel)
				model.nodeRemoved(parent, index, oldInst);
			newInst = config.insertEDSource(external.template().name());
		} else if (external instanceof ESSourceInstance) {
			if (updateModel)
				parent = model.essourcesNode();
			oldInst = config.essource(external.name());
			index = config.indexOfESSource((ESSourceInstance) oldInst);
			config.removeESSource((ESSourceInstance) oldInst);
			if (updateModel)
				model.nodeRemoved(parent, index, oldInst);
			newInst = config.insertESSource(index, external.template().name(), external.name());
		} else if (external instanceof ESModuleInstance) {
			if (updateModel)
				parent = model.esmodulesNode();
			oldInst = config.esmodule(external.name());
			index = config.indexOfESModule((ESModuleInstance) oldInst);
			config.removeESModule((ESModuleInstance) oldInst);
			if (updateModel)
				model.nodeRemoved(parent, index, oldInst);
			newInst = config.insertESModule(index, external.template().name(), external.name());
		} else if (external instanceof ServiceInstance) {
			if (updateModel)
				parent = model.servicesNode();
			oldInst = config.service(external.name());
			index = config.indexOfService((ServiceInstance) oldInst);
			config.removeService((ServiceInstance) oldInst);
			if (updateModel)
				model.nodeRemoved(parent, index, oldInst);
			newInst = config.insertService(index, external.template().name());
		}

		for (int i = 0; i < newInst.parameterCount(); i++)
			newInst.updateParameter(i, external.parameter(i).valueAsString());
		newInst.setDatabaseId(external.databaseId()); // dangerous?

		if (updateModel) {
			model.nodeInserted(parent, index);
			model.updateLevel1Nodes();
			tree.expandPath(new TreePath(model.getPathToRoot(newInst)));
		}

		return true;
	}

	/**
	 * remove a node from the tree and the respective component from the
	 * configuration
	 */
	private static boolean removeNode(JTree tree, Object node) {
		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		TreePath treePath = tree.getSelectionPath().getParentPath();

		int index = -1;
		Object parent = null;

		if (node instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter) node;
			index = config.indexOfPSet(pset);
			if (index < 0)
				return false;
			config.removePSet(pset);
			parent = model.psetsNode();
		} else if (node instanceof EDAliasInstance) {
			EDAliasInstance globalEDAlias = (EDAliasInstance) node;
			index = config.indexOfGlobalEDAlias(globalEDAlias);
			if (index < 0)
				return false;
			config.removeGlobalEDAlias(globalEDAlias);
			parent = model.globalEDAliasesNode();
		} else if (node instanceof EDSourceInstance) {
			EDSourceInstance edsource = (EDSourceInstance) node;
			index = config.indexOfEDSource(edsource);
			if (index < 0)
				return false;
			config.removeEDSource(edsource);
			parent = model.edsourcesNode();
		} else if (node instanceof ESSourceInstance) {
			ESSourceInstance essource = (ESSourceInstance) node;
			index = config.indexOfESSource(essource);
			if (index < 0)
				return false;
			config.removeESSource(essource);
			parent = model.essourcesNode();
		} else if (node instanceof ESModuleInstance) {
			ESModuleInstance esmodule = (ESModuleInstance) node;
			index = config.indexOfESModule(esmodule);
			if (index < 0)
				return false;
			config.removeESModule(esmodule);
			parent = model.esmodulesNode();
		} else if (node instanceof ServiceInstance) {
			ServiceInstance service = (ServiceInstance) node;
			index = config.indexOfService(service);
			if (index < 0)
				return false;
			config.removeService(service);
			parent = model.servicesNode();
		} else
			return false;

		model.nodeRemoved(parent, index, node);
		model.updateLevel1Nodes();

		if (index == 0)
			tree.setSelectionPath(treePath);
		else
			tree.setSelectionPath(treePath.pathByAddingChild(model.getChild(parent, index - 1)));

		return true;
	}

	/*
	 * edit the name of the node
	 */
	// NOTE: Requests to change names are caught
	// by getCellEditorValue method at ConfigurationTreeEditor.java
	public static void editNodeName(JTree tree) {
		TreePath treePath = tree.getSelectionPath();
		tree.expandPath(treePath.getParentPath());
		tree.scrollPathToVisible(treePath);
		tree.startEditingAtPath(treePath);
	}

	/**
	 * When a dataset has changed it must trigger the OutputModule Update as well as
	 * the Prescaler values update. Only the module instances created with the
	 * template TriggerResultsFilter must be synch with the OutputModule path list.
	 * NOTE: technically it is possible to have more than one instance of
	 * TriggerResultsFilter but it should not happen in an EndPath. In a normal path
	 * it could happen, but those you don't need to autoupdate in any way, they are
	 * only edited by hand
	 * 
	 * Module template : TriggerResultsFilter module --> HLTFilter --> T -->
	 * TriggerResultsFilter
	 * 
	 * note: this is legacy code before the Dataset update to DatasetPaths
	 * this will fail gracefully for new style Datasets as the TriggerResultsFilter
	 * is not on an output path but the dataset path
	 * this is referring to the old style smart prescales on the end path and not 
	 * the PathFilters of a DatasetPath
	 */
	public static boolean updateFilter(Configuration config, Stream stream, ArrayList<Path> newPaths) {
		OutputModule OutputM = stream.outputModule();
		OutputM.forceUpdate(); // force update the OutputModules paths before update the filter.

		Path[] paths = OutputM.parentPaths(); // END PATHS

		for (int i = 0; i < paths.length; i++) {
			Path currentPath = paths[i];
			Iterator<ModuleInstance> modules = currentPath.moduleIterator();
			while (modules.hasNext()) {
				ModuleInstance currentModule = modules.next();
				Template template = currentModule.template();
				ArrayList<Path> toAdd = new ArrayList<Path>();

				// Only module template : TriggerResultsFilter
				if ((template.name().compareTo("TriggerResultsFilter") == 0)
						&& (template.type().compareTo("HLTFilter") == 0)) {

					Parameter para = null;
					para = currentModule.findParameter("triggerConditions");
					if (para != null) {
						VStringParameter parameterTriggerConditions = (VStringParameter) para;

						if (parameterTriggerConditions == null) {
							System.err.println(
									"[ERROR@ConfigurationTreeActions::synchOutputModuleAndStream] parameterTriggerConditions == null! ");
							return false;
						}

						for (int w = 0; w < newPaths.size(); w++) {
							String pathname = newPaths.get(w).name();
							boolean found = false;
							for (int j = 0; j < parameterTriggerConditions.vectorSize(); j++) {
								String trgCondition = (String) parameterTriggerConditions.value(j);
								String strCondition = SmartPrescaleTable.regularise(trgCondition);
								StringTokenizer pathTokens = new StringTokenizer(strCondition, "/ ");
								while (pathTokens.hasMoreTokens()) {
									String strPath = pathTokens.nextToken().trim();
									if (strPath.compareTo(pathname) == 0) {
										found = true;
										continue;
									}
								} // end while tokens.
							} // end for parameters

							if (!found) { // then add the path
								toAdd.add(newPaths.get(w));
							}
						} // end for newPaths

						// Add new paths to triggerConditions:
						for (int w = 0; w < toAdd.size(); w++) {
							if (para.valueAsString().length() > 2)
								para.setValue(para.valueAsString() + ",\"" + toAdd.get(w) + "\"");
							else
								para.setValue(para.valueAsString() + "\"" + toAdd.get(w) + "\"");
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
			public String toString() {
				return "TriggerResultsFilter";
			}
		},

		prescalerTemplateType {
			public String toString() {
				return "HLTFilter";
			}
		}
	}

	public static void renameEDAliasVPSets(IConfiguration config, String oldModuleName, String newModuleName) {
		
		Iterator<EDAliasInstance> itEDA = config.edAliasIterator();
		while (itEDA.hasNext()) {
			EDAliasInstance edAliasInstance = itEDA.next();
			for (int j = 0; j < edAliasInstance.parameterCount(); j++) {
				edAliasInstance.updateName(oldModuleName, "VPSet", newModuleName);
			}

		}
	}
	
	public static void renameGlobalEDAliasVPSets(IConfiguration config, String oldModuleName, String newModuleName) {
		
		Iterator<EDAliasInstance> itGEDA = config.globalEDAliasIterator();
		while (itGEDA.hasNext()) {
			EDAliasInstance globalEDAliasInstance = itGEDA.next();
			for (int j = 0; j < globalEDAliasInstance.parameterCount(); j++) {
				globalEDAliasInstance.updateName(oldModuleName, "VPSet", newModuleName);
			}

		}
	}

}

//////////////////////////////////////////////
/// threads
//////////////////////////////////////////////

/** Import a instance container using a different thread. */
class ImportAllInstancesThread extends SwingWorker<String, String> {
	/** member data */
	private long startTime;

	private JTree tree;
	private ConfigurationTreeModel sourceModel;
	private ConfigurationTreeModel targetModel;
	private Configuration targetConfig;
	private Configuration sourceConfig;
	private Object ext;
	private boolean updateAll;

	private String type; // type of imported items
	private ArrayList<String> items; // name of imported items
	private ConfigurationModifier mconf; // Needed when filtering. Only available resulting items.

	/** standard constructor */
	public ImportAllInstancesThread(JTree Tree, ConfigurationTreeModel sourceModel, Object external,
			boolean UpdateAll) {
		this.tree = Tree;
		this.sourceModel = sourceModel;
		this.targetModel = (ConfigurationTreeModel) tree.getModel();
		this.ext = external;
		this.updateAll = UpdateAll;
		this.items = new ArrayList<String>();
		this.type = "";
		this.targetConfig = (Configuration) targetModel.getRoot();
		this.sourceConfig = null;
		this.mconf = null;

		// Allows to import from a ConfigurationModifier Object. (after filtering).
		Object sourceConf = sourceModel.getRoot();
		if (sourceConf instanceof Configuration)
			this.sourceConfig = (Configuration) sourceModel.getRoot();
		else if (sourceConf instanceof ConfigurationModifier)
			mconf = (ConfigurationModifier) sourceModel.getRoot();

	}

	/** Return an Array list with containers name to perform a diff operation. */
	public ArrayList<String> getImportedItems() {
		return items;
	}

	@Override
	protected String doInBackground() throws Exception {
		startTime = System.currentTimeMillis();
		if (sourceModel.getChildCount(ext) > 0) {
			Instance instance = (Instance) sourceModel.getChild(ext, 0);
			if (instance instanceof EDSourceInstance)
				type = "EDSource";
			else if (instance instanceof ESSourceInstance)
				type = "ESSource";
			else if (instance instanceof ESModuleInstance)
				type = "ESModule";
			else if (instance instanceof ServiceInstance)
				type = "Service";
		}
		int count = sourceModel.getChildCount(ext);

		for (int i = 0; i < sourceModel.getChildCount(ext); i++) {
			tree.setSelectionPath(null);
			Instance instance = (Instance) sourceModel.getChild(ext, i);

			int progress = (i * 100) / count; // range 0-100.
			setProgress(progress);
			items.add(instance.name()); // register item for diff

			if ((instance instanceof EDSourceInstance) && targetConfig.edsource(instance.name()) != null
					|| (instance instanceof ESSourceInstance) && targetConfig.essource(instance.name()) != null
					|| (instance instanceof ESModuleInstance) && targetConfig.esmodule(instance.name()) != null
					|| (instance instanceof ServiceInstance) && targetConfig.service(instance.name()) != null) {

				if (updateAll) {
					ConfigurationTreeActions.replaceInstance(tree, instance, false);
					firePropertyChange("current", null, instance.name());
				}
				continue;
			}

			if (targetConfig.isUniqueQualifier(instance.name())) {
				int index = 0;

				String templateName = instance.template().name();
				String instanceName = instance.name();
				Instance InsertedIinstance = null;
				Object parent = null;

				if (instance instanceof EDSourceInstance) {
					InsertedIinstance = targetConfig.insertEDSource(templateName);
					parent = targetModel.edsourcesNode();
				} else if (instance instanceof ESSourceInstance) {
					InsertedIinstance = targetConfig.insertESSource(index, templateName, instanceName);
					parent = targetModel.essourcesNode();
				} else if (instance instanceof ESModuleInstance) {
					InsertedIinstance = targetConfig.insertESModule(index, templateName, instanceName);
					parent = targetModel.esmodulesNode();
				} else if (instance instanceof ServiceInstance) {
					InsertedIinstance = targetConfig.insertService(index, templateName);
					parent = targetModel.servicesNode();
				} else
					continue;

				for (int j = 0; j < InsertedIinstance.parameterCount(); j++)
					InsertedIinstance.updateParameter(j, instance.parameter(j).valueAsString());

				InsertedIinstance.setDatabaseId(instance.databaseId());
				targetModel.nodeInserted(parent, index);

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
		if (child instanceof ServiceInstance)
			targetModel.nodeStructureChanged(targetModel.servicesNode());
		else if (child instanceof ESModuleInstance)
			targetModel.nodeStructureChanged(targetModel.esmodulesNode());
		else if (child instanceof ESSourceInstance)
			targetModel.nodeStructureChanged(targetModel.essourcesNode());
		else if (child instanceof EDSourceInstance)
			targetModel.nodeStructureChanged(targetModel.edsourcesNode());
		targetModel.updateLevel1Nodes();

		Diff diff;
		if (sourceConfig != null)
			diff = new Diff(sourceConfig, targetConfig);
		else
			diff = new Diff(mconf, targetConfig);

		diff.compare(type, items);
		if (!diff.isIdentical()) {
			DiffDialog dlg = new DiffDialog(diff);
			dlg.pack();
			dlg.setVisible(true);
		}
		String time = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
				TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));
		firePropertyChange("current", null, items.size() + " items imported! " + time);
		System.out.println(items.size() + " items imported! enlapsedTime: " + time);

	}
}

/** Update Modules using threads. */
final class UpdateAllModulesThread extends SwingWorker<String, String> {
	/** member data */
	private long startTime;
	private JTree tree;
	private ConfigurationTreeModel sourceModel;
	private ConfigurationTreeModel targetModel;
	private Object ext;
	private ArrayList<String> items; // names of imported items
	private Configuration targetConfig;
	private Configuration sourceConfig;
	private ConfigurationModifier sourceConfM;
	private String type;

	/** standard constructor */
	public UpdateAllModulesThread(JTree Tree, ConfigurationTreeModel sourceModel, Object external) {
		this.tree = Tree;
		this.sourceModel = sourceModel;
		this.targetModel = (ConfigurationTreeModel) tree.getModel();
		this.ext = external;
		this.items = new ArrayList<String>();
		this.targetConfig = (Configuration) targetModel.getRoot();
		this.type = "";
		this.sourceConfig = null;
		this.sourceConfM = null; // Necessary to allow import from a filtered configuration.

		Object conf = sourceModel.getRoot();
		if (conf instanceof Configuration)
			this.sourceConfig = (Configuration) sourceModel.getRoot();
		else if (conf instanceof ConfigurationModifier)
			this.sourceConfM = (ConfigurationModifier) sourceModel.getRoot();

	}

	/** Return an Array list with containers name to perform a diff operation. */
	public ArrayList<String> getUpdatedItems() {
		return items;
	}

	@Override
	protected String doInBackground() throws Exception {
		startTime = System.currentTimeMillis();
		int count = sourceModel.getChildCount(ext);
		ModuleInstance instance = null;
		for (int i = 0; i < sourceModel.getChildCount(ext); i++) {
			tree.setSelectionPath(null);
			instance = (ModuleInstance) sourceModel.getChild(ext, i);

			int progress = (i * 100) / count; // range 0-100
			setProgress(progress);

			if ((instance instanceof ModuleInstance) && targetConfig.module(instance.name()) != null) {
				ConfigurationTreeActions.replaceModule(targetConfig, null, instance);
				items.add(instance.name()); // register item for diff
				firePropertyChange("current", null, instance.name());
			}
		}
		setProgress(100);

		if (instance != null)
			if (instance instanceof ModuleInstance)
				type = "Module";

		return new String("Done!");
	}

	/*
	 * Executed in event dispatching thread
	 */
	@Override
	public void done() {
		long elapsedTime = System.currentTimeMillis() - startTime;

		Diff diff;
		if (sourceConfig != null)
			diff = new Diff(sourceConfig, targetConfig);
		else
			diff = new Diff(sourceConfM, targetConfig);

		diff.compare(type, items);
		if (!diff.isIdentical()) {
			DiffDialog dlg = new DiffDialog(diff);
			dlg.pack();
			dlg.setVisible(true);
		}
		String time = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
				TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));

		firePropertyChange("current", null, items.size() + " items updated! " + time);
		System.out.println(items.size() + " items updated! enlapsedTime: " + time);
		targetModel.updateLevel1Nodes();
		targetModel.nodeStructureChanged(targetModel.modulesNode());
	}

}

/** Update Modules using threads. */
final class UpdateAllEDAliasesThread extends SwingWorker<String, String> {
	/** member data */
	private long startTime;
	private JTree tree;
	private ConfigurationTreeModel sourceModel;
	private ConfigurationTreeModel targetModel;
	private Object ext;
	private ArrayList<String> items; // names of imported items
	private Configuration targetConfig;
	private Configuration sourceConfig;
	private ConfigurationModifier sourceConfM;
	private String type;

	/** standard constructor */
	public UpdateAllEDAliasesThread(JTree Tree, ConfigurationTreeModel sourceModel, Object external) {
		this.tree = Tree;
		this.sourceModel = sourceModel;
		this.targetModel = (ConfigurationTreeModel) tree.getModel();
		this.ext = external;
		this.items = new ArrayList<String>();
		this.targetConfig = (Configuration) targetModel.getRoot();
		this.type = "";
		this.sourceConfig = null;
		this.sourceConfM = null; // Necessary to allow import from a filtered configuration.

		Object conf = sourceModel.getRoot();
		if (conf instanceof Configuration)
			this.sourceConfig = (Configuration) sourceModel.getRoot();
		else if (conf instanceof ConfigurationModifier)
			this.sourceConfM = (ConfigurationModifier) sourceModel.getRoot();

	}

	/** Return an Array list with containers name to perform a diff operation. */
	public ArrayList<String> getUpdatedItems() {
		return items;
	}

	@Override
	protected String doInBackground() throws Exception {
		startTime = System.currentTimeMillis();
		int count = sourceModel.getChildCount(ext);
		EDAliasInstance instance = null;
		for (int i = 0; i < sourceModel.getChildCount(ext); i++) {
			tree.setSelectionPath(null);
			instance = (EDAliasInstance) sourceModel.getChild(ext, i);

			int progress = (i * 100) / count; // range 0-100
			setProgress(progress);

			if ((instance instanceof EDAliasInstance) && targetConfig.module(instance.name()) != null) {
				if (!ConfigurationTreeActions.replaceEDAlias(targetConfig, null, instance)) {
					ConfigurationTreeActions.replaceGlobalEDAlias(targetConfig, null, instance);
					type = "GEDAlias";
				} else
					type = "EDAlias";
				items.add(instance.name()); // register item for diff
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

		Diff diff;
		if (sourceConfig != null)
			diff = new Diff(sourceConfig, targetConfig);
		else
			diff = new Diff(sourceConfM, targetConfig);

		diff.compare(type, items);
		if (!diff.isIdentical()) {
			DiffDialog dlg = new DiffDialog(diff);
			dlg.pack();
			dlg.setVisible(true);
		}
		String time = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
				TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));

		firePropertyChange("current", null, items.size() + " items updated! " + time);
		System.out.println(items.size() + " items updated! enlapsedTime: " + time);
		targetModel.updateLevel1Nodes();
		targetModel.nodeStructureChanged(targetModel.globalEDAliasesNode());
		targetModel.nodeStructureChanged(targetModel.switchProducersNode());
	}

}

/**
 * Import a reference container using threads
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4275976
 */
class ImportAllReferencesThread extends SwingWorker<String, String> {
	/** member data */
	private long startTime;
	private JTree tree;
	private ConfigurationTreeModel sourceModel;
	private ConfigurationTreeModel targetModel;
	private Object ext;
	private boolean updateAll;
	private ArrayList<String> items;
	private String type;
	private Configuration targetConfig;
	private Configuration sourceConfig;
	private ConfigurationModifier sourceConfM; // Necessary to allow import from filtered configurations.

	/** standard constructor */
	public ImportAllReferencesThread(JTree tree, JTree sourceTree, Object external, boolean UpdateAll) {
		this.tree = tree;
		this.sourceModel = (ConfigurationTreeModel) sourceTree.getModel();
		;
		this.targetModel = (ConfigurationTreeModel) tree.getModel();
		this.ext = external;
		this.updateAll = UpdateAll;
		this.items = new ArrayList<String>();
		this.type = "";
		this.targetConfig = (Configuration) targetModel.getRoot();
		this.sourceConfig = null;
		this.sourceConfM = null;

		Object sourceConf = sourceModel.getRoot();
		if (sourceConf instanceof Configuration)
			this.sourceConfig = (Configuration) sourceModel.getRoot();
		else if (sourceConf instanceof ConfigurationModifier)
			this.sourceConfM = (ConfigurationModifier) sourceModel.getRoot();

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
		for (int i = 0; i < count; i++) {
			container = (ReferenceContainer) sourceModel.getChild(ext, i);
			if(container instanceof Path){
				Path path = (Path) container;
				if(path.isDatasetPath() || path.isFinalPath()){
					continue;
				}
			}
			ConfigurationTreeActions.importReferenceContainersNoModel(tree, container, updateAll);
			items.add(container.name()); // registering container name for diff.
			int progress = (i * 100) / count; // range 0-100.

			setProgress(progress);
			firePropertyChange("current", oldValue, container.name());
			oldValue = container.name();
		}
		setProgress(100);

		if (container != null)
			if (container instanceof Path)
				type = "path";
			else if (container instanceof Sequence)
				type = "sequence";
			else if (container instanceof Task)
				type = "task";
			else if (container instanceof SwitchProducer)
				type = "switchproducer";

		return new String("Done!");
	}

	/**
	 * Executed in event dispatching thread
	 */
	@Override
	public void done() {
		long elapsedTime = System.currentTimeMillis() - startTime;

		Diff diff;
		if (sourceConfig != null)
			diff = new Diff(sourceConfig, targetConfig);
		else
			diff = new Diff(sourceConfM, targetConfig);

		diff.compare(type, items);
		if (!diff.isIdentical()) {
			DiffDialog dlg = new DiffDialog(diff);
			dlg.pack();
			dlg.setVisible(true);
		}

		String time = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
				TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));
		firePropertyChange("current", null, items.size() + " items imported! " + time);
		System.out.println(items.size() + " items imported! enlapsedTime: " + time);

		// Since the model is not updated during the process, do it now.
		if (this.type == "path")
			targetModel.nodeStructureChanged(targetModel.pathsNode());
		else
			targetModel.nodeStructureChanged(targetModel.sequencesNode());
		targetModel.updateLevel1Nodes();
	}
}

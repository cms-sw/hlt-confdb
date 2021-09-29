package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;

import java.awt.Color;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import confdb.gui.menu.ScrollableMenu;
import confdb.gui.treetable.TreeTable;

import confdb.data.*;

import java.util.Collections;

/**
 * ConfigurationTreeMouseListener
 * ------------------------------
 * @author Philipp Schieferdecker
 *
 */
public class ConfigurationTreeMouseListener extends MouseAdapter {
	//
	// member data
	//

	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** reference to the configuration tree-model */
	private ConfigurationTreeModel treeModel = null;

	/** popup menu associated with global psets */
	private JPopupMenu popupPSets = null;
	
	/** popup menu associated with global EDAliases */
	private JPopupMenu popupGlobalEDAliases = null;

	/** popup menu showing all available service templates */
	private JPopupMenu popupServices = null;

	/** popup menu showing all available event data source templates */
	private JPopupMenu popupEDSources = null;

	/** popup menu showing all available event setup source templates */
	private JPopupMenu popupESSources = null;

	/** popup menu showing all available event setup module templates */
	private JPopupMenu popupESModules = null;

	/** popup menu associated with paths */
	private JPopupMenu popupPaths = null;

	/** popup menu associated with sequences */
	private JPopupMenu popupSequences = null;

	/** popup menu associated with tasks */
	private JPopupMenu popupTasks = null;

	/** popup menu associated with switch producers */
	private JPopupMenu popupSwitchProducers = null;

	/** popup menu associated with modules */
	private JPopupMenu popupModules = null;

	/** popup menu associated with event contents */
	private JPopupMenu popupContents = null;

	/** popup menu associated with streams */
	private JPopupMenu popupStreams = null;

	/** popup menu associated with datasets */
	private JPopupMenu popupDatasets = null;

	/** action listener for psets-menu actions */
	private PSetMenuListener psetListener = null;
	
	/** action listener for global EDAliases-menu actions */
	private GlobalEDAliasMenuListener globalEDAliasListener = null;

	/** action listener for services-menu actions */
	private ServiceMenuListener serviceListener = null;

	/** action listener for edsources-menu actions */
	private EDSourceMenuListener edsourceListener = null;

	/** action listener for essources-menu actions */
	private ESSourceMenuListener essourceListener = null;

	/** action listener for esmodules-menu actions */
	private ESModuleMenuListener esmoduleListener = null;

	/** action listener for paths-menu actions */
	private PathMenuListener pathListener = null;

	/** action listener for sequences-menu actions */
	private SequenceMenuListener sequenceListener = null;

	/** action listener for tasks-menu actions */
	private TaskMenuListener taskListener = null;

	/** action listener for switch producer-menu actions */
	private SwitchProducerMenuListener switchProducerListener = null;

	/** action listener for modules-menu actions */
	private ModuleMenuListener moduleListener = null;

	/** action listern for contents-menu actions */
	private ContentMenuListener contentListener = null;

	/** action listern for streams-menu actions */
	private StreamMenuListener streamListener = null;

	/** action listern for datasets-menu actions */
	private DatasetMenuListener datasetListener = null;

	/** enable the ability to sort components */
	private boolean enableSort = true;

	/** Enable path cloning context menu */
	private boolean enablePathCloning = false;

	/**
	 * Reference to jTreeTableParameters/ConfDbGUI.java Bug: 75952 FIX: Stop editing
	 * cell component when clicking the tree.
	 */
	private TreeTable TreeTableParameters;
	
	/** reference to the gui application */
    //private ConfDbGUI app = null;

	//
	// construction
	//

	/** standard constructor */
	public ConfigurationTreeMouseListener(JTree tree, JFrame frame, ConfDbGUI app) {

		this.tree = tree;
		this.treeModel = (ConfigurationTreeModel) tree.getModel();

		psetListener = new PSetMenuListener(tree, frame);
		globalEDAliasListener = new GlobalEDAliasMenuListener(tree, frame);
		edsourceListener = new EDSourceMenuListener(tree);
		essourceListener = new ESSourceMenuListener(tree);
		esmoduleListener = new ESModuleMenuListener(tree);
		serviceListener = new ServiceMenuListener(tree);
		pathListener = new PathMenuListener(tree);
		sequenceListener = new SequenceMenuListener(tree);
		taskListener = new TaskMenuListener(tree);
		switchProducerListener = new SwitchProducerMenuListener(tree, frame, app);
		moduleListener = new ModuleMenuListener(tree);
		contentListener = new ContentMenuListener(tree);
		streamListener = new StreamMenuListener(tree, frame);
		datasetListener = new DatasetMenuListener(tree, frame);
		
	}

	//
	// member functions
	//

	/** MouseAdapter: mousePressed() */
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	/** MouseAdapter: mouseReleased() */
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void setTreeTable(TreeTable tt) {
		TreeTableParameters = tt;
	}

	/** check if this event should really trigger the menu to be displayed */
	private void maybeShowPopup(MouseEvent e) {
		/*
		 * Bug: 75952 FIX: Stop editing cell component when clicking the tree.
		 */
		if (TreeTableParameters != null) {
			TreeTableParameters.stopEditing(); // Stop Edition in right upper panel
		}

		if (!e.isPopupTrigger())
			return;

		if (!tree.isEditable())
			return;

		if (treeModel.getRoot() instanceof ConfigurationModifier)
			return;

		IConfiguration config = (IConfiguration) treeModel.getRoot();
		if (config.name().length() == 0)
			return;

		tree = (JTree) e.getComponent();
		TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
		if (treePath == null)
			return;
		int depth = treePath.getPathCount();
		if (depth <= 1)
			return;

		tree.setSelectionPath(treePath);

		Object node = treePath.getLastPathComponent();

		// show the 'PSets' popup?
		if (isInTreePath(treePath, treeModel.psetsNode()) && depth <= 3) {
			updatePSetMenu();
			popupPSets.show(e.getComponent(), e.getX(), e.getY());
			return;
		}
		
		if (isInTreePath(treePath, treeModel.globalEDAliasesNode()) && depth <= 3) {
			updateGlobalEDAliasMenu();
			popupGlobalEDAliases.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'EDSources' popup?
		if (isInTreePath(treePath, treeModel.edsourcesNode()) && depth <= 3) {
			updateEDSourceMenu();
			popupEDSources.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'ESSources' popup?
		if (isInTreePath(treePath, treeModel.essourcesNode()) && depth <= 3) {
			updateESSourceMenu();
			popupESSources.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'ESModules' popup?
		if (isInTreePath(treePath, treeModel.esmodulesNode()) && depth <= 3) {
			updateESModuleMenu();
			popupESModules.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'Services' popup?
		if (isInTreePath(treePath, treeModel.servicesNode()) && depth <= 3) {
			updateServiceMenu();
			popupServices.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'Paths' popup?
		if (isInTreePath(treePath, treeModel.pathsNode())/* &&depth<=4 */) {
			updatePathMenu();
			popupPaths.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'Sequences' popup?
		if (isInTreePath(treePath, treeModel.sequencesNode())/* &&depth<=4 */) {
			updateSequenceMenu();
			popupSequences.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'Tasks' popup?
		if (isInTreePath(treePath, treeModel.tasksNode())/* &&depth<=4 */) {
			updateTaskMenu();
			popupTasks.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'SwitchProducers' popup?
		if (isInTreePath(treePath, treeModel.switchProducersNode())/* &&depth<=4 */) {
			updateSwitchProducersMenu();
			popupSwitchProducers.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show the 'VPSets' popup (EDAlias)?
		
		// show the 'Modules' popup?
		if (isInTreePath(treePath, treeModel.modulesNode()) && depth <= 3) {
			updateModuleMenu();
			popupModules.show(e.getComponent(), e.getX(), e.getY());
			return;
		}

		// show 'Contents' popup?
		if (isInTreePath(treePath, treeModel.contentsNode())) {
			updateContentMenu();
			popupContents.show(e.getComponent(), e.getX(), e.getY());
		}

		// show 'Streams' popup?
		if (isInTreePath(treePath, treeModel.streamsNode())) {
			updateStreamMenu();
			popupStreams.show(e.getComponent(), e.getX(), e.getY());
		}

		// show 'Datasets' popup?
		if (isInTreePath(treePath, treeModel.datasetsNode())) {
			updateDatasetMenu();
			popupDatasets.show(e.getComponent(), e.getX(), e.getY());
		}

	}

	/** check if a node is in a tree path */
	private boolean isInTreePath(TreePath treePath, Object node) {
		for (int i = 0; i < treePath.getPathCount(); i++)
			if (treePath.getPathComponent(i).equals(node))
				return true;
		return false;
	}

	/** Sets EnablePathCloning property */
	public boolean setEnablePathClonig(boolean EnableCloning) {
		this.enablePathCloning = EnableCloning;
		return this.enablePathCloning;
	}

	/** update 'PSets' menu */
	public void updatePSetMenu() {
		JMenuItem menuItem;
		popupPSets = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getLastPathComponent();

		menuItem = new JMenuItem("Add PSet");
		menuItem.addActionListener(psetListener);
		menuItem.setActionCommand("NEWPSET");
		popupPSets.add(menuItem);

		if (depth == 3) {
			menuItem = new JMenuItem("Remove PSet");
			menuItem.addActionListener(psetListener);
			popupPSets.add(menuItem);
		}

		// if (depth==2&&enableSort) {
		// popupPSets.addSeparator();
		// menuItem = new JMenuItem("Sort");
		// menuItem.addActionListener(psetListener);
		// popupPSets.addComponent(menuItem);
		// }
	}
	
	/** update 'Global EDAliases' menu */
	public void updateGlobalEDAliasMenu() {
		JMenuItem menuItem;
		popupGlobalEDAliases = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();

		menuItem = new JMenuItem("Add Global EDAlias");
		menuItem.addActionListener(globalEDAliasListener);
		popupGlobalEDAliases.add(menuItem);

		if (depth == 3) {
			menuItem = new JMenuItem("Remove Global EDAlias");
			menuItem.addActionListener(globalEDAliasListener);
			popupGlobalEDAliases.add(menuItem);
		}

		// if (depth==2&&enableSort) {
		// popupPSets.addSeparator();
		// menuItem = new JMenuItem("Sort");
		// menuItem.addActionListener(psetListener);
		// popupPSets.addComponent(menuItem);
		// }
	}

	/** update 'EDSource' menu */
	public void updateEDSourceMenu() {
		JMenuItem menuItem;
		popupEDSources = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getLastPathComponent();

		JMenu edsourceMenu = new ScrollableMenu("Add EDSource");
		if (depth == 3 || (depth == 2 && treeModel.getChildCount(node) > 0)) {
			edsourceMenu.setEnabled(false);
			popupEDSources.add(edsourceMenu);
		} else {
			TreeModel model = tree.getModel();
			Configuration config = (Configuration) model.getRoot();
			SoftwareRelease release = config.release();
			Iterator<EDSourceTemplate> it = release.edsourceTemplateIterator();
			while (it.hasNext()) {
				EDSourceTemplate t = it.next();
				menuItem = new JMenuItem(t.name());
				menuItem.addActionListener(edsourceListener);
				edsourceMenu.add(menuItem);
			}
			popupEDSources.add(edsourceMenu);
		}
		if (depth == 3) {
			menuItem = new JMenuItem("Remove EDSource");
			menuItem.addActionListener(edsourceListener);
			popupEDSources.add(menuItem);
		}

		if (depth == 2 && enableSort) {
			popupEDSources.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(edsourceListener);
			popupEDSources.add(menuItem);
		}
	}

	/** update 'ESSources' menu */
	public void updateESSourceMenu() {
		JMenuItem menuItem;
		popupESSources = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();

		// 'ESSources' or specific event setup source
		if (depth >= 2 && depth <= 3) {
			JMenu essourceMenu = new ScrollableMenu("Add ESSource");

			TreeModel model = tree.getModel();
			Configuration config = (Configuration) model.getRoot();
			SoftwareRelease release = config.release();
			Iterator<ESSourceTemplate> it = release.essourceTemplateIterator();

			while (it.hasNext()) {
				ESSourceTemplate t = it.next();
				if (t.instanceCount() > 0) {
					JMenu instanceMenu = new ScrollableMenu(t.name());
					menuItem = new JMenuItem("New Instance");
					menuItem.addActionListener(essourceListener);
					menuItem.setActionCommand(t.name());
					instanceMenu.add(menuItem);

					JMenu copyMenu = new ScrollableMenu("Copy");
					instanceMenu.add(copyMenu);

					for (int i = 0; i < t.instanceCount(); i++) {
						Instance instance = t.instance(i);
						menuItem = new JMenuItem(instance.name());
						menuItem.addActionListener(essourceListener);
						menuItem.setActionCommand("copy:" + t.name() + ":" + instance.name());
						copyMenu.add(menuItem);
					}
					essourceMenu.add(instanceMenu);
				} else {
					menuItem = new JMenuItem(t.name());
					menuItem.addActionListener(essourceListener);
					essourceMenu.add(menuItem);
				}
			}
			popupESSources.add(essourceMenu);
		}

		// a specific event setup source is selected
		if (depth == 3) {
			menuItem = new JMenuItem("Remove ESSource");
			menuItem.addActionListener(essourceListener);
			popupESSources.add(menuItem);
			menuItem = new JMenuItem("Rename ESSource");
			menuItem.addActionListener(essourceListener);
			popupESSources.add(menuItem);
			popupESSources.addSeparator();

			ESPreferable essource = (ESPreferable) treePath.getLastPathComponent();
			JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("preferred");
			cbMenuItem.setState(essource.isPreferred());
			cbMenuItem.addItemListener(new ESPreferableItemListener(tree));
			popupESSources.add(cbMenuItem);
		}

		if (depth == 2 && enableSort) {
			popupESSources.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(essourceListener);
			popupESSources.add(menuItem);
		}
	}

	/** update 'ESModules' menu */
	public void updateESModuleMenu() {
		JMenuItem menuItem;
		popupESModules = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();

		// 'ESModules' or specific event setup module
		if (depth >= 2 && depth <= 3) {
			JMenu esmoduleMenu = new ScrollableMenu("Add ESModule");

			TreeModel model = tree.getModel();
			Configuration config = (Configuration) model.getRoot();
			SoftwareRelease release = config.release();
			Iterator<ESModuleTemplate> it = release.esmoduleTemplateIterator();

			while (it.hasNext()) {
				ESModuleTemplate t = it.next();
				if (t.instanceCount() > 0) {
					JMenu instanceMenu = new ScrollableMenu(t.name());
					menuItem = new JMenuItem("New Instance");
					menuItem.addActionListener(esmoduleListener);
					menuItem.setActionCommand(t.name());
					instanceMenu.add(menuItem);

					JMenu copyMenu = new ScrollableMenu("Copy");
					instanceMenu.add(copyMenu);

					for (int i = 0; i < t.instanceCount(); i++) {
						Instance instance = t.instance(i);
						menuItem = new JMenuItem(instance.name());
						menuItem.addActionListener(esmoduleListener);
						menuItem.setActionCommand("copy:" + t.name() + ":" + instance.name());
						copyMenu.add(menuItem);
					}
					esmoduleMenu.add(instanceMenu);
				} else {
					menuItem = new JMenuItem(t.name());
					menuItem.addActionListener(esmoduleListener);
					esmoduleMenu.add(menuItem);
				}
			}
			popupESModules.add(esmoduleMenu);
		}

		// a specific event setup source is selected
		if (depth == 3) {
			menuItem = new JMenuItem("Remove ESModule");
			menuItem.addActionListener(esmoduleListener);
			popupESModules.add(menuItem);
			menuItem = new JMenuItem("Rename ESModule");
			menuItem.addActionListener(esmoduleListener);
			popupESModules.add(menuItem);

			ESPreferable esmodule = (ESPreferable) treePath.getLastPathComponent();
			JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("preferred");
			cbMenuItem.setState(esmodule.isPreferred());
			cbMenuItem.addItemListener(new ESPreferableItemListener(tree));
			popupESModules.add(cbMenuItem);
		}

		if (depth == 2 && enableSort) {
			popupESModules.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(esmoduleListener);
			popupESModules.add(menuItem);
		}
	}

	/** update 'Services' menu */
	public void updateServiceMenu() {
		JMenuItem menuItem;
		popupServices = new JPopupMenu();
		int depth = tree.getSelectionPath().getPathCount();

		JMenu serviceMenu = new ScrollableMenu("Add Service");

		TreeModel model = tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		SoftwareRelease release = config.release();
		Iterator<ServiceTemplate> it = release.serviceTemplateIterator();

		while (it.hasNext()) {
			ServiceTemplate t = it.next();
			menuItem = new JMenuItem(t.name());
			if (t.instanceCount() > 0)
				menuItem.setEnabled(false);
			else
				menuItem.addActionListener(serviceListener);
			serviceMenu.add(menuItem);
		}
		popupServices.add(serviceMenu);

		if (depth == 3) {
			menuItem = new JMenuItem("Remove Service");
			menuItem.addActionListener(serviceListener);
			popupServices.add(menuItem);
		}

		if (depth == 2 && enableSort) {
			popupServices.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(serviceListener);
			popupServices.add(menuItem);
		}
	}

	/** update 'Paths' menu */
	public void updatePathMenu() {
		JMenuItem menuItem;
		popupPaths = new JPopupMenu();

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		IConfiguration config = (IConfiguration) model.getRoot();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getPathComponent(depth - 1);
		Object parent = treePath.getPathComponent(depth - 2);

		// 'Paths' selectd
		if (depth == 2) {
			menuItem = new JMenuItem("Add Path");
			menuItem.addActionListener(pathListener);
			menuItem.setActionCommand("NEWPATH");
			popupPaths.add(menuItem);
		}

		// specific path is selected
		if (depth == 3) {
			Path path = (Path) node;

			JMenu addModuleMenu = createAddRepModuleMenu(path, null, pathListener, true, false);
			popupPaths.add(addModuleMenu);

			JMenu addPathMenu = createAddRepPathMenu(path, true);
			popupPaths.add(addPathMenu);

			JMenu addSequenceMenu = createAddRepSequenceMenu(path, pathListener, false, true);
			popupPaths.add(addSequenceMenu);

			JMenu addTaskMenu = createAddRepTaskMenu(path, pathListener, false, true);
			popupPaths.add(addTaskMenu);

			JMenu addSwitchProducerMenu = createAddRepSPMenu(path, pathListener, false, true);
			popupPaths.add(addSwitchProducerMenu);

			popupPaths.addSeparator();

			menuItem = new JMenuItem("Rename Path");
			menuItem.addActionListener(pathListener);
			popupPaths.add(menuItem);

			menuItem = new JMenuItem("Remove Path");
			menuItem.addActionListener(pathListener);
			popupPaths.add(menuItem);

			// Copy path request 75955
			menuItem = new JMenuItem("Clone Path");
			menuItem.addActionListener(pathListener);
			popupPaths.add(menuItem);

			// Clone path request 75955
			// NOTE: Deep Clone for path disable by default.
			if (enablePathCloning) {
				menuItem = new JMenuItem("Deep Clone Path");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);
			}

			// bug #82526: add/remove path to/from a primary dataset
			// ASSIGN TO DATASET/STREAM MENU
			popupPaths.addSeparator();
			JMenu assignPathMenu = new ScrollableMenu("Assign to P.Dataset");
			popupPaths.add(assignPathMenu);
			Iterator<Stream> itST = config.streamIterator();
			while (itST.hasNext()) {
				Stream stream = itST.next();
				/*
				 * PrimaryDataset pds = stream.dataset(path); if(pds == null) { menuItem = new
				 * ScrollableMenu(stream.name()); JMenuItem subMenuItem = new JMenuItem();
				 * 
				 * Iterator<PrimaryDataset> itPD = stream.datasetIterator();
				 * while(itPD.hasNext()) { PrimaryDataset dataset = itPD.next(); subMenuItem =
				 * new JMenuItem(dataset.name()); subMenuItem.addActionListener(streamListener);
				 * subMenuItem.setActionCommand("ADDPATHTO:"+dataset.name());
				 * menuItem.addComponent(subMenuItem); } } else { // GREYED OUT STREAM: menuItem
				 * = new JMenuItem(stream.name()); menuItem.setEnabled(false); }
				 */
				// ArrayList<PrimaryDataset> pds = stream.datasets(path);
				// for(int i = 0; i < pds.size(); i++) {

				menuItem = new ScrollableMenu(stream.name());

				if (stream.datasetCount() != 0) {
					JMenuItem subMenuItem = new JMenuItem();
					Iterator<PrimaryDataset> itPD = stream.datasetIterator();
					while (itPD.hasNext()) {
						PrimaryDataset dataset = itPD.next();
						subMenuItem = new JMenuItem(dataset.name());
						subMenuItem.addActionListener(streamListener);
						subMenuItem.setActionCommand("ADDPATHTO:" + dataset.name());

						if (dataset.path(path.name()) != null)
							subMenuItem.setEnabled(false);

						menuItem.add(subMenuItem);
					}
				} else
					menuItem.setEnabled(false);

				assignPathMenu.add(menuItem);
			}
			// bug #82526: add/remove path to/from a primary dataset
			// REMOVE FROM THIS DATASET/STREAM
			JMenu removePathMenu = new ScrollableMenu("Remove from P.Dataset");
			popupPaths.add(removePathMenu);
			itST = config.streamIterator();
			while (itST.hasNext()) {
				Stream stream = itST.next();

				/*
				 * PrimaryDataset pds = stream.dataset(path); if(pds != null) { menuItem = new
				 * ScrollableMenu(stream.name()); JMenuItem subMenuItem = new JMenuItem();
				 * 
				 * Iterator<PrimaryDataset> itPD = stream.datasetIterator();
				 * while(itPD.hasNext()) { PrimaryDataset dataset = itPD.next(); subMenuItem =
				 * new JMenuItem(dataset.name()); subMenuItem.setEnabled(false);
				 * if(pds.equals(dataset)) { subMenuItem.addActionListener(pathListener);
				 * subMenuItem.setActionCommand("REMOVEPATH:"+dataset.name());
				 * subMenuItem.setEnabled(true); } menuItem.addComponent(subMenuItem); } } else
				 * { // GREYED OUT STREAM: menuItem = new JMenuItem(stream.name());
				 * menuItem.setEnabled(false); }
				 */
				menuItem = new ScrollableMenu(stream.name());

				if (stream.datasetCount() != 0) {
					JMenuItem subMenuItem = new JMenuItem();
					Iterator<PrimaryDataset> itPD = stream.datasetIterator();
					while (itPD.hasNext()) {
						PrimaryDataset dataset = itPD.next();
						subMenuItem = new JMenuItem(dataset.name());
						subMenuItem.setEnabled(true);

						subMenuItem.addActionListener(pathListener);
						subMenuItem.setActionCommand("REMOVEPATH:" + dataset.name());

						if (dataset.path(path.name()) == null)
							subMenuItem.setEnabled(false);

						menuItem.add(subMenuItem);
					}
				} else
					menuItem.setEnabled(false);

				removePathMenu.add(menuItem);
			}
			popupPaths.add(removePathMenu);

			popupPaths.addSeparator();
			JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("endpath");
			cbMenuItem.setState(path.isEndPath());
			if (path.hasOutputModule())
				cbMenuItem.setEnabled(false);
			cbMenuItem.addItemListener(new PathItemListener(tree));
			popupPaths.add(cbMenuItem);

			JMenu repPathMenu = createAddRepPathMenu(path, false);
			popupPaths.add(repPathMenu);

			return;
		}

		// a specific module/path/sequence/task reference is selected
		if (depth == 4) {
			Path path = (Path) parent;
			JMenu addModuleMenu = createAddRepModuleMenu(path, null, pathListener, true, false);
			popupPaths.add(addModuleMenu);

			JMenu addPathMenu = createAddRepPathMenu(path, true);
			popupPaths.add(addPathMenu);

			JMenu addSequenceMenu = createAddRepSequenceMenu(path, pathListener, false, true);
			popupPaths.add(addSequenceMenu);

			JMenu addTaskMenu = createAddRepTaskMenu(path, pathListener, false, true);
			popupPaths.add(addTaskMenu);

			JMenu addSwitchProducerMenu = createAddRepSPMenu(path, pathListener, false, true);
			popupPaths.add(addSwitchProducerMenu);

			popupPaths.addSeparator();

			if (node instanceof ModuleReference) {
				menuItem = new JMenuItem("Remove Module");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);

				// CLONE OPTION:
				menuItem = new JMenuItem("Clone Module");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);

				popupPaths.addSeparator();
				popupPaths.add(createSetOperatorMenu((Reference) node, pathListener));
			}
			if (node instanceof OutputModuleReference) {
				menuItem = new JMenuItem("Remove OutputModule");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);
			}
			if (node instanceof PathReference) {
				menuItem = new JMenuItem("Remove Path");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);
			}
			if (node instanceof SequenceReference) {
				menuItem = new JMenuItem("Remove Sequence");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);
			}
			if (node instanceof TaskReference) {
				menuItem = new JMenuItem("Remove Task");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);
			}
			if (node instanceof SwitchProducerReference) {
				menuItem = new JMenuItem("Remove SwitchProducer");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);

				// CLONE OPTION:
				menuItem = new JMenuItem("Clone SwitchProducer");
				menuItem.addActionListener(pathListener);
				popupPaths.add(menuItem);

				popupPaths.addSeparator();
				popupPaths.add(createSetOperatorMenu((Reference) node, pathListener));
			}
		}

		if (depth == 2 && enableSort) {
			popupPaths.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(pathListener);
			popupPaths.add(menuItem);
		}

		if (node instanceof Reference) {
			if (depth == 4)
				popupPaths.addSeparator();
			Reference reference = (Reference) node;
			menuItem = new JMenuItem("GOTO " + reference.name());
			menuItem.setActionCommand("GOTO");
			menuItem.addActionListener(pathListener);
			popupPaths.add(menuItem);
		}

	}

	/** update 'Sequences' Menu */
	public void updateSequenceMenu() {
		JMenuItem menuItem;
		popupSequences = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getPathComponent(depth - 1);
		Object parent = treePath.getPathComponent(depth - 2);

		IConfiguration config = (IConfiguration) treeModel.getRoot();

		if (depth == 2) {
			menuItem = new JMenuItem("Add Sequence");
			menuItem.addActionListener(sequenceListener);
			popupSequences.add(menuItem);
			popupSequences.addSeparator();
			menuItem = new JMenuItem("Remove Unreferenced Sequences");
			menuItem.addActionListener(sequenceListener);
			menuItem.setActionCommand("RMUNREF");
			popupSequences.add(menuItem);
			menuItem = new JMenuItem("Resolve Unnecessary Sequences");
			menuItem.addActionListener(sequenceListener);
			menuItem.setActionCommand("RESOLVE");
			popupSequences.add(menuItem);
		} else if (depth == 3) {
			Sequence sequence = (Sequence) node;

			JMenu addModuleMenu = createAddRepModuleMenu(sequence, null, sequenceListener, true, false);
			popupSequences.add(addModuleMenu);

			JMenu addSequenceMenu = createAddRepSequenceMenu(sequence, sequenceListener, false, true);
			popupSequences.add(addSequenceMenu);

			JMenu addTaskMenu = createAddRepTaskMenu(sequence, sequenceListener, false, true);
			popupSequences.add(addTaskMenu);

			JMenu addSwitchProducerMenu = createAddRepSPMenu(sequence, sequenceListener, false, true);
			popupSequences.add(addSwitchProducerMenu);

			popupSequences.addSeparator();

			menuItem = new JMenuItem("Rename Sequence");
			menuItem.addActionListener(sequenceListener);
			popupSequences.add(menuItem);

			// request 75955
			menuItem = new JMenuItem("Clone Sequence");
			menuItem.addActionListener(sequenceListener);
			popupSequences.add(menuItem);

			// request 75955
			menuItem = new JMenuItem("Deep Clone Sequence");
			menuItem.addActionListener(sequenceListener);
			popupSequences.add(menuItem);

			menuItem = new JMenuItem("Remove Sequence");
			menuItem.addActionListener(sequenceListener);
			popupSequences.add(menuItem);

			JMenu repSequenceMenu = createAddRepSequenceMenu(sequence, sequenceListener, false, false);
			popupSequences.add(repSequenceMenu);

		} else if (depth == 4) {
			Sequence sequence = (Sequence) parent;

			JMenu addModuleMenu = createAddRepModuleMenu(sequence, null, sequenceListener, true, false);
			popupSequences.add(addModuleMenu);

			JMenu addSequenceMenu = createAddRepSequenceMenu(sequence, sequenceListener, true, true);
			popupSequences.add(addSequenceMenu);

			JMenu addTaskMenu = createAddRepTaskMenu(sequence, sequenceListener, true, true);
			popupSequences.add(addTaskMenu);

			JMenu addSwitchProducerMenu = createAddRepSPMenu(sequence, sequenceListener, true, true);
			popupSequences.add(addSwitchProducerMenu);

			if (node instanceof ModuleReference) {
				menuItem = new JMenuItem("Remove Module");
				menuItem.addActionListener(sequenceListener);
				popupSequences.add(menuItem);

				// CLONE OPTION:
				menuItem = new JMenuItem("Clone Module");
				menuItem.addActionListener(sequenceListener);
				popupSequences.add(menuItem);

				popupSequences.addSeparator();
				popupSequences.add(createSetOperatorMenu((Reference) node, sequenceListener));
			}
			if (node instanceof OutputModuleReference) {
				menuItem = new JMenuItem("Remove OutputModule");
				menuItem.addActionListener(sequenceListener);
				popupSequences.add(menuItem);
			}
			if (node instanceof PathReference) {
				menuItem = new JMenuItem("Remove Path");
				menuItem.addActionListener(sequenceListener);
				popupSequences.add(menuItem);
			}
			if (node instanceof SequenceReference) {
				menuItem = new JMenuItem("Remove Sequence");
				menuItem.addActionListener(sequenceListener);
				popupSequences.add(menuItem);
			}
			if (node instanceof TaskReference) {
				menuItem = new JMenuItem("Remove Task");
				menuItem.addActionListener(sequenceListener);
				popupSequences.add(menuItem);
			}
			if (node instanceof SwitchProducerReference) {
				menuItem = new JMenuItem("Remove SwitchProducer");
				menuItem.addActionListener(sequenceListener);
				popupSequences.add(menuItem);

				// CLONE OPTION:
				menuItem = new JMenuItem("Clone SwitchProducer");
				menuItem.addActionListener(sequenceListener);
				popupSequences.add(menuItem);

				popupSequences.addSeparator();
				popupSequences.add(createSetOperatorMenu((Reference) node, sequenceListener));
			}
		}

		if (depth == 2 && enableSort) {
			popupSequences.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(sequenceListener);
			popupSequences.add(menuItem);
		}

		if (node instanceof Reference) {
			if (depth == 4)
				popupSequences.addSeparator();
			Reference reference = (Reference) node;
			menuItem = new JMenuItem("GOTO " + reference.name());
			menuItem.setActionCommand("GOTO");
			menuItem.addActionListener(sequenceListener);

			popupSequences.add(menuItem);
		}

	}

	/** update 'Tasks' Menu */
	public void updateTaskMenu() {
		JMenuItem menuItem;
		popupTasks = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getPathComponent(depth - 1);
		Object parent = treePath.getPathComponent(depth - 2);

		IConfiguration config = (IConfiguration) treeModel.getRoot();

		if (depth == 2) {
			menuItem = new JMenuItem("Add Task");
			menuItem.addActionListener(taskListener);
			popupTasks.add(menuItem);
			popupTasks.addSeparator();
			menuItem = new JMenuItem("Remove Unreferenced Tasks");
			menuItem.addActionListener(taskListener);
			menuItem.setActionCommand("RMUNREFTAS");
			popupTasks.add(menuItem);
			menuItem = new JMenuItem("Resolve Unnecessary Tasks");
			menuItem.addActionListener(taskListener);
			menuItem.setActionCommand("RESOLVETAS");
			popupTasks.add(menuItem);
		} else if (depth == 3) {
			Task task = (Task) node;

			JMenu addModuleMenu = createAddRepModuleMenu(task, null, taskListener, true, false);
			popupTasks.add(addModuleMenu);

			JMenu addTaskMenu = createAddRepTaskMenu(task, taskListener, false, true);
			popupTasks.add(addTaskMenu);

			JMenu addSwitchProducerMenu = createAddRepSPMenu(task, taskListener, false, true);
			popupTasks.add(addSwitchProducerMenu);

			popupTasks.addSeparator();

			menuItem = new JMenuItem("Rename Task");
			menuItem.addActionListener(taskListener);
			popupTasks.add(menuItem);

			menuItem = new JMenuItem("Clone Task");
			menuItem.addActionListener(taskListener);
			popupTasks.add(menuItem);

			menuItem = new JMenuItem("Deep Clone Task");
			menuItem.addActionListener(taskListener);
			popupTasks.add(menuItem);

			menuItem = new JMenuItem("Remove Task");
			menuItem.addActionListener(taskListener);
			popupTasks.add(menuItem);

			JMenu repTaskMenu = createAddRepTaskMenu(task, taskListener, false, false);
			popupTasks.add(repTaskMenu);

		} else if (depth == 4) {
			Task task = (Task) parent;

			JMenu addModuleMenu = createAddRepModuleMenu(task, null, taskListener, true, false);
			popupTasks.add(addModuleMenu);

			JMenu addTaskMenu = createAddRepTaskMenu(task, taskListener, true, true);
			popupTasks.add(addTaskMenu);

			JMenu addSwitchProducerMenu = createAddRepSPMenu(task, taskListener, true, true);
			popupTasks.add(addSwitchProducerMenu);

			if (node instanceof ModuleReference) {
				ModuleReference mr = (ModuleReference) node;

				menuItem = new JMenuItem("Remove Module");
				menuItem.addActionListener(taskListener);
				popupTasks.add(menuItem);

				// CLONE OPTION:
				menuItem = new JMenuItem("Clone Module");
				menuItem.addActionListener(taskListener);
				popupTasks.add(menuItem);

				popupTasks.addSeparator();
				popupTasks.add(createSetOperatorMenu((Reference) node, taskListener));
			}
			if (node instanceof OutputModuleReference) {
				menuItem = new JMenuItem("Remove OutputModule");
				menuItem.addActionListener(taskListener);
				popupTasks.add(menuItem);
			}
			if (node instanceof PathReference) {
				menuItem = new JMenuItem("Remove Path");
				menuItem.addActionListener(taskListener);
				popupTasks.add(menuItem);
			}
			if (node instanceof TaskReference) {
				menuItem = new JMenuItem("Remove Task");
				menuItem.addActionListener(taskListener);
				popupTasks.add(menuItem);
			}
			if (node instanceof SwitchProducerReference) {
				menuItem = new JMenuItem("Remove SwitchProducer");
				menuItem.addActionListener(taskListener);
				popupTasks.add(menuItem);

				// CLONE OPTION:
				menuItem = new JMenuItem("Clone SwitchProducer");
				menuItem.addActionListener(taskListener);
				popupTasks.add(menuItem);

				popupTasks.addSeparator();
				popupTasks.add(createSetOperatorMenu((Reference) node, sequenceListener));
			}
		}

		if (depth == 2 && enableSort) {
			popupTasks.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(taskListener);
			popupTasks.add(menuItem);
		}

		if (node instanceof Reference) {
			if (depth == 4)
				popupTasks.addSeparator();
			Reference reference = (Reference) node;
			menuItem = new JMenuItem("GOTO " + reference.name());
			menuItem.setActionCommand("GOTO");
			menuItem.addActionListener(taskListener);

			popupTasks.add(menuItem);
		}

	}

	/** update 'SwitchProducers' Menu */
	public void updateSwitchProducersMenu() {
		JMenuItem menuItem;
		popupSwitchProducers = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getPathComponent(depth - 1);
		Object parent = treePath.getPathComponent(depth - 2);

		IConfiguration config = (IConfiguration) treeModel.getRoot();

		if (depth == 2) {
			menuItem = new JMenuItem("Add SwitchProducer");
			menuItem.addActionListener(switchProducerListener);
			popupSwitchProducers.add(menuItem);
			popupSwitchProducers.addSeparator();
			menuItem = new JMenuItem("Remove Unreferenced SwitchProducers");
			menuItem.addActionListener(switchProducerListener);
			menuItem.setActionCommand("RMUNREFSP");
			popupSwitchProducers.add(menuItem);
			menuItem = new JMenuItem("Resolve Unnecessary SwitchProducers");
			menuItem.addActionListener(switchProducerListener);
			menuItem.setActionCommand("RESOLVESP");
			popupSwitchProducers.add(menuItem);
		} else if (depth == 3) {
			SwitchProducer switchProducer = (SwitchProducer) node;

			if (switchProducer.entryCount() < 2) {
				JMenu addModuleMenu = createAddRepModuleMenu(switchProducer, null, switchProducerListener, true, true);
				popupSwitchProducers.add(addModuleMenu);

				JMenu addEDAliasMenu = createAddRepEDAliasMenu(switchProducer, null, switchProducerListener, true);
				popupSwitchProducers.add(addEDAliasMenu);

				popupSwitchProducers.addSeparator();
			}

			menuItem = new JMenuItem("Rename SwitchProducer");
			menuItem.addActionListener(switchProducerListener);
			popupSwitchProducers.add(menuItem);

			menuItem = new JMenuItem("Clone SwitchProducer");
			menuItem.addActionListener(switchProducerListener);
			popupSwitchProducers.add(menuItem);

			menuItem = new JMenuItem("Deep Clone SwitchProducer");
			menuItem.addActionListener(switchProducerListener);
			popupSwitchProducers.add(menuItem);

			menuItem = new JMenuItem("Remove SwitchProducer");
			menuItem.addActionListener(switchProducerListener);
			popupSwitchProducers.add(menuItem);

			JMenu repSwitchProducerMenu = createAddRepSPMenu(switchProducer, switchProducerListener, false, false);
			popupSwitchProducers.add(repSwitchProducerMenu);

		} else if (depth == 4) {
			SwitchProducer switchProducer = (SwitchProducer) parent;

			if (switchProducer.entryCount() < 2) {
				JMenu addModuleMenu = createAddRepModuleMenu(switchProducer, null, switchProducerListener, true, true);
				popupSwitchProducers.add(addModuleMenu);

				JMenu addEDAliasMenu = createAddRepEDAliasMenu(switchProducer, null, switchProducerListener, true);
				popupSwitchProducers.add(addEDAliasMenu);
			}

			if (node instanceof ModuleReference) {
				menuItem = new JMenuItem("Remove Module");
				menuItem.addActionListener(switchProducerListener);
				popupSwitchProducers.add(menuItem);

				// CLONE OPTION:
				if (switchProducer.entryCount() < 2) {
					menuItem = new JMenuItem("Clone Module");
					menuItem.addActionListener(switchProducerListener);
					popupSwitchProducers.add(menuItem);
				}

				popupSwitchProducers.addSeparator();
				popupSwitchProducers.add(createSetOperatorMenu((Reference) node, switchProducerListener));
			}
			if (node instanceof EDAliasReference) {
				menuItem = new JMenuItem("Rename EDAlias");
				menuItem.addActionListener(switchProducerListener);
				popupSwitchProducers.add(menuItem);

				menuItem = new JMenuItem("Remove EDAlias");
				menuItem.addActionListener(switchProducerListener);
				popupSwitchProducers.add(menuItem);

				// CLONE OPTION:
				if (switchProducer.entryCount() < 2) {
					menuItem = new JMenuItem("Clone EDAlias");
					menuItem.addActionListener(switchProducerListener);
					popupSwitchProducers.add(menuItem);
				}
				
				/*
				 * JMenuItem menuItem; popupPSets = new JPopupMenu();
				 * 
				 * TreePath treePath = tree.getSelectionPath(); int depth =
				 * treePath.getPathCount(); Object node = treePath.getLastPathComponent();
				 */

				menuItem = new JMenuItem("Add Module (VPSet)");
				menuItem.addActionListener(switchProducerListener);
				popupSwitchProducers.add(menuItem);

				if (depth == 3) {
					menuItem = new JMenuItem("Remove Module (VPSet)");
					menuItem.addActionListener(switchProducerListener);
					popupSwitchProducers.add(menuItem);
				}

				popupSwitchProducers.addSeparator();
				popupSwitchProducers.add(createSetOperatorMenu((Reference) node, switchProducerListener));
			}
			if (node instanceof PathReference) {
				menuItem = new JMenuItem("Remove Path");
				menuItem.addActionListener(switchProducerListener);
				popupSwitchProducers.add(menuItem);
			}
			if (node instanceof SwitchProducerReference) {
				menuItem = new JMenuItem("Remove SwitchProducer");
				menuItem.addActionListener(switchProducerListener);
				popupSwitchProducers.add(menuItem);
			}
		}

		if (depth == 2 && enableSort) {
			popupSwitchProducers.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(switchProducerListener);
			popupSwitchProducers.add(menuItem);
		}

		if (node instanceof Reference) {
			if (depth == 4)
				popupSwitchProducers.addSeparator();
			Reference reference = (Reference) node;
			menuItem = new JMenuItem("GOTO " + reference.name());
			menuItem.setActionCommand("GOTO");
			menuItem.addActionListener(switchProducerListener);

			popupSwitchProducers.add(menuItem);
		}
	}

	/** update 'Modules' Menu */
	public void updateModuleMenu() {

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getPathComponent(depth - 1);

		JMenuItem menuItem;
		popupModules = new JPopupMenu();

		if (depth == 3) {
			menuItem = new JMenuItem("Rename Module");
			menuItem.addActionListener(moduleListener);
			popupModules.add(menuItem);

			ModuleInstance module = (ModuleInstance) node;
			JMenu repModuleMenu = createAddRepModuleMenu(null, module, moduleListener, false, false);
			popupModules.add(repModuleMenu);
		}

		if (depth == 2 && enableSort) {
			popupModules.addSeparator();
			menuItem = new JMenuItem("Sort");
			menuItem.addActionListener(moduleListener);
			popupModules.add(menuItem);
		}
	}

	/** update 'EventContents' menu */
	public void updateContentMenu() {
		JMenuItem menuItem;
		popupContents = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getLastPathComponent();

		if (depth > 3)
			return;

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		IConfiguration config = (IConfiguration) model.getRoot();

		if (depth == 2) {
			menuItem = new JMenuItem("Add EventContent");
			menuItem.addActionListener(contentListener);
			menuItem.setActionCommand("ADD");
			popupContents.add(menuItem);

			popupContents.addSeparator();

			ButtonGroup bg = new ButtonGroup();
			JRadioButtonMenuItem jrbMenuItem;

			jrbMenuItem = new JRadioButtonMenuItem("show streams");
			jrbMenuItem.setActionCommand("SHOW:streams");
			jrbMenuItem.addActionListener(contentListener);
			popupContents.add(jrbMenuItem);
			if (model.contentMode().equals("streams"))
				jrbMenuItem.setSelected(true);
			bg.add(jrbMenuItem);

			jrbMenuItem = new JRadioButtonMenuItem("show paths");
			jrbMenuItem.setActionCommand("SHOW:paths");
			jrbMenuItem.addActionListener(contentListener);
			popupContents.add(jrbMenuItem);
			if (model.contentMode().equals("paths"))
				jrbMenuItem.setSelected(true);
			bg.add(jrbMenuItem);

			jrbMenuItem = new JRadioButtonMenuItem("show datasets");
			jrbMenuItem.setActionCommand("SHOW:datasets");
			jrbMenuItem.addActionListener(contentListener);
			popupContents.add(jrbMenuItem);
			if (model.contentMode().equals("datasets"))
				jrbMenuItem.setSelected(true);
			bg.add(jrbMenuItem);
		}

		if (depth == 3) {
			EventContent content = (EventContent) node;

			menuItem = new JMenuItem("<html>Rename <i>" + content.name() + "</i></html>");
			menuItem.addActionListener(contentListener);
			menuItem.setActionCommand("RENAME");
			popupContents.add(menuItem);

			menuItem = new JMenuItem("<html>Remove <i>" + content.name() + "</i></html>");
			menuItem.addActionListener(contentListener);
			menuItem.setActionCommand("REMOVE");
			popupContents.add(menuItem);

			popupContents.addSeparator();
		}
	}

	/** update 'Streams' menu */
	public void updateStreamMenu() {
		JMenuItem menuItem;
		popupStreams = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getLastPathComponent();

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		IConfiguration config = (IConfiguration) model.getRoot();

		if (depth == 2) {
			menuItem = new JMenuItem("Add Stream");
			menuItem.addActionListener(streamListener);
			menuItem.setActionCommand("ADD");
			popupStreams.add(menuItem);

			popupStreams.addSeparator();
			ButtonGroup bg = new ButtonGroup();
			JRadioButtonMenuItem jrbMenuItem;

			jrbMenuItem = new JRadioButtonMenuItem("show datasets");
			jrbMenuItem.setActionCommand("SHOW:datasets");
			jrbMenuItem.addActionListener(streamListener);
			popupStreams.add(jrbMenuItem);
			if (model.streamMode().equals("datasets"))
				jrbMenuItem.setSelected(true);
			bg.add(jrbMenuItem);

			jrbMenuItem = new JRadioButtonMenuItem("show paths");
			jrbMenuItem.setActionCommand("SHOW:paths");
			jrbMenuItem.addActionListener(streamListener);
			popupStreams.add(jrbMenuItem);
			if (model.streamMode().equals("paths"))
				jrbMenuItem.setSelected(true);
			bg.add(jrbMenuItem);

		} else if (depth == 3) {
			Stream stream = (Stream) node;

			JMenu addPathMenu = new ScrollableMenu("Add Path");
			popupStreams.add(addPathMenu);

			menuItem = new JMenuItem("Add Primary Dataset");
			menuItem.addActionListener(streamListener);
			menuItem.setActionCommand("ADDDATASETTO:" + stream.name());
			popupStreams.add(menuItem);

			popupStreams.addSeparator();

			menuItem = new JMenuItem("Set Fraction-to-Disk");
			menuItem.addActionListener(streamListener);
			menuItem.setActionCommand("FRACTION");
			popupStreams.add(menuItem);

			menuItem = new JMenuItem("<html>Rename <i>" + stream.name() + "</i></html>");
			menuItem.addActionListener(streamListener);
			menuItem.setActionCommand("RENAME");
			popupStreams.add(menuItem);

			menuItem = new JMenuItem("<html>Remove <i>" + stream.name() + "</i></html>");
			menuItem.addActionListener(streamListener);
			menuItem.setActionCommand("REMOVE");
			popupStreams.add(menuItem);

			menuItem = new JMenuItem("<html>Remove Unassigned Paths from <i>" + stream.name() + "</i></html>");
			menuItem.addActionListener(streamListener);
			menuItem.setActionCommand("REMOVEUNASSIGNED");
			popupStreams.add(menuItem);

			ArrayList<Path> paths = new ArrayList<Path>();
			Iterator<Path> itP = config.pathIterator();
			while (itP.hasNext()) {
				Path path = itP.next();
				if (stream.indexOfPath(path) < 0)
					paths.add(path);
			}
			Collections.sort(paths);
			itP = paths.iterator();
			while (itP.hasNext()) {
				menuItem = new JMenuItem(itP.next().name());
				menuItem.addActionListener(streamListener);
				menuItem.setActionCommand("ADDPATH");
				addPathMenu.add(menuItem);
			}
		} else if (depth == 4) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			if (model.streamMode().equals("paths")) {
				Path path = (Path) treeNode.object();
				menuItem = new JMenuItem("<html>Remove <i>" + path.name() + "</i></html>");
				menuItem.addActionListener(streamListener);
				menuItem.setActionCommand("REMOVEPATH");
				popupStreams.add(menuItem);
			} else if (model.streamMode().equals("datasets")) {
				if (treeNode.object() instanceof PrimaryDataset) {
					PrimaryDataset dataset = (PrimaryDataset) treeNode.object();
					menuItem = new JMenuItem("<html>Remove <i>" + dataset.name() + "</i></html>");
					menuItem.addActionListener(streamListener);
					menuItem.setActionCommand("REMOVEDATASET");
					popupStreams.add(menuItem);
				} else if (treeNode.object() instanceof StringBuffer) {
					StringBuffer unassigned = (StringBuffer) treeNode.object();
					menuItem = new JMenuItem("<html>Remove Unassigned Paths</i></html>");
					menuItem.addActionListener(streamListener);
					menuItem.setActionCommand("REMOVEUNASSIGNED");
					popupStreams.add(menuItem);
				}
			}
		} else if (depth == 5) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			if (treeNode.parent() instanceof ConfigurationTreeNode) {
				ConfigurationTreeNode parentNode = (ConfigurationTreeNode) treeNode.parent();
				if (parentNode.object() instanceof StringBuffer) {
					Stream stream = (Stream) parentNode.parent();
					Path path = (Path) treeNode.object();
					JMenu assignPathMenu = new ScrollableMenu("Assign " + path.name() + " to");
					popupStreams.add(assignPathMenu);

					Iterator<PrimaryDataset> itPD = stream.datasetIterator();
					while (itPD.hasNext()) {
						PrimaryDataset dataset = itPD.next();
						menuItem = new JMenuItem(dataset.name());
						menuItem.addActionListener(streamListener);
						menuItem.setActionCommand("ADDPATHTO:" + dataset.name());
						assignPathMenu.add(menuItem);
					}
				}
			}

			// The code above seems to be fake.
			// Find bellow code for new option menu.
			// bug #82526: add/remove path to/from a primary dataset
			if (treeNode.object() instanceof Path) {

				// ASSIGN MENU
				Path path = (Path) treeNode.object();
				JMenu assignPathMenu = new ScrollableMenu("Assign to");
				popupStreams.add(assignPathMenu);
				Iterator<Stream> itST = config.streamIterator();
				while (itST.hasNext()) {
					Stream stream = itST.next();

					/*
					 * PrimaryDataset pds = stream.dataset(path); if(pds == null) { menuItem = new
					 * ScrollableMenu(stream.name()); JMenuItem subMenuItem = new JMenuItem();
					 * Iterator<PrimaryDataset> itPD = stream.datasetIterator();
					 * while(itPD.hasNext()) { PrimaryDataset dataset = itPD.next(); subMenuItem =
					 * new JMenuItem(dataset.name()); subMenuItem.addActionListener(streamListener);
					 * subMenuItem.setActionCommand("ADDPATHTO:"+dataset.name());
					 * menuItem.addComponent(subMenuItem); } } else { // GREYED OUT STREAM: menuItem
					 * = new JMenuItem(stream.name()); menuItem.setEnabled(false); }
					 */

					menuItem = new ScrollableMenu(stream.name());
					if (stream.datasetCount() != 0) {
						JMenuItem subMenuItem = new JMenuItem();
						Iterator<PrimaryDataset> itPD = stream.datasetIterator();
						while (itPD.hasNext()) {
							PrimaryDataset dataset = itPD.next();
							subMenuItem = new JMenuItem(dataset.name());
							subMenuItem.addActionListener(streamListener);
							subMenuItem.setActionCommand("ADDPATHTO:" + dataset.name());

							if (dataset.path(path.name()) != null)
								subMenuItem.setEnabled(false);

							menuItem.add(subMenuItem);
						}
					} else
						menuItem.setEnabled(false);

					assignPathMenu.add(menuItem);
				}

				// REMOVE FROM THIS DATASET/STREAM
				JMenuItem removePathMenu = new JMenuItem("Remove from P.Dataset");
				removePathMenu.addActionListener(datasetListener);
				removePathMenu.setActionCommand("REMOVEPATH");
				popupStreams.add(removePathMenu);
			}

		}
	}

	/** update 'PrimaryDatasets' menu */
	public void updateDatasetMenu() {
		JMenuItem menuItem;
		popupDatasets = new JPopupMenu();

		TreePath treePath = tree.getSelectionPath();
		int depth = treePath.getPathCount();
		Object node = treePath.getLastPathComponent();

		TreeModel model = tree.getModel();
		IConfiguration config = (IConfiguration) model.getRoot();

		if (depth > 4)
			return;

		if (depth == 2) {
			menuItem = new JMenuItem("Add Primary Dataset");
			menuItem.addActionListener(datasetListener);
			menuItem.setActionCommand("ADD");
			if (config.streamCount() == 0)
				menuItem.setEnabled(false);
			popupDatasets.add(menuItem);
		} else if (depth == 3) {
			PrimaryDataset dataset = (PrimaryDataset) node;

			menuItem = new JMenuItem("<html>Edit <i>" + dataset.name() + "</i></html>");
			menuItem.addActionListener(datasetListener);
			menuItem.setActionCommand("EDIT " + dataset.name());
			popupDatasets.add(menuItem);

			JMenu addPathMenu = new ScrollableMenu("Add Path");
			popupDatasets.add(addPathMenu);

			popupDatasets.addSeparator();

			menuItem = new JMenuItem("<html>Rename <i>" + dataset.name() + "</i></html>");
			menuItem.addActionListener(datasetListener);
			menuItem.setActionCommand("RENAME");
			popupDatasets.add(menuItem);

			menuItem = new JMenuItem("<html>Remove <i>" + dataset.name() + "</i></html>");
			menuItem.addActionListener(datasetListener);
			menuItem.setActionCommand("REMOVE");
			popupDatasets.add(menuItem);

			ArrayList<Path> paths = new ArrayList<Path>();
			Iterator<Path> itP = config.pathIterator();

			while (itP.hasNext())
				paths.add(itP.next());

			Collections.sort(paths);
			itP = paths.iterator();

			while (itP.hasNext()) {
				Path path = itP.next();
				if (path.isEndPath())
					continue;
				Stream stream = dataset.parentStream();

				menuItem = null;
				if (stream.listOfUnassignedPaths().indexOf(path) >= 0) {
					menuItem = new JMenuItem("<html><font color='blue'>" + path.name() + "</font></html>");
				} else {
					// Red if this exist in any other dataset of the same stream.
					ArrayList<PrimaryDataset> pds = stream.datasets(path);
					for (int i = 0; i < pds.size(); i++) {
						PrimaryDataset ds = pds.get(i);
						if (!ds.equals(dataset)) {
							menuItem = new JMenuItem("<html><font color='red'>" + path.name() + "</font></html>");
							break;
						}
					}
					// default:
					if (menuItem == null)
						menuItem = new JMenuItem(path.name());
				}

				menuItem.addActionListener(datasetListener);
				menuItem.setActionCommand("ADDPATH");

				if (dataset.path(path.name()) == null)
					addPathMenu.add(menuItem);
			}
		} else if (depth == 4) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			Path path = (Path) treeNode.object();

			menuItem = new JMenuItem("<html>Remove <i>" + path.name() + "</i></html>");
			menuItem.addActionListener(datasetListener);
			menuItem.setActionCommand("REMOVEPATH");
			popupDatasets.add(menuItem);

			PrimaryDataset parentDataset = (PrimaryDataset) treeNode.parent();
			Stream parentStream = parentDataset.parentStream();
			if (parentStream.datasetCount() > 1) {
				JMenu movePathMenu = new ScrollableMenu("<html>Move <i>" + path.name() + "</i> to ...</html>");
				popupDatasets.add(movePathMenu);
				Iterator<PrimaryDataset> itPD = parentStream.datasetIterator();
				while (itPD.hasNext()) {
					PrimaryDataset dataset = itPD.next();
					if (dataset.name().equals(parentDataset.name()))
						continue;

					menuItem = new JMenuItem(dataset.name());
					menuItem.addActionListener(datasetListener);
					menuItem.setActionCommand("MOVEPATH:" + dataset.name());
					movePathMenu.add(menuItem);
					if (dataset.path(path.name()) != null)
						menuItem.setEnabled(false);
				}
			}
		}
	}

	//
	// private member functions
	//

	/** create the 'Add/Replace Module' submenu */
	private JMenu createAddRepModuleMenu(ReferenceContainer container, ModuleInstance module, ActionListener listener,
			boolean isAdd, boolean isSwitchProducer) {
		JMenu moduleMenu = null;
		if (isAdd) {
			moduleMenu = new JMenu("Add Module");
		} else {
			moduleMenu = new JMenu("Replace Module");
		}
		JMenuItem menuItemAll;
		JMenuItem menuItem;
		JMenuItem copyItemAll;
		JMenuItem copyItem;

		TreeModel model = tree.getModel();
		Configuration config = (Configuration) model.getRoot();
		SoftwareRelease release = config.release();

		// Explicitly add OutputModule menu
		if (!isSwitchProducer) {
			JMenu outputMenu = new ScrollableMenu("OutputModule");
			moduleMenu.add(outputMenu);
			Iterator<OutputModule> itOM = config.outputIterator();
			while (itOM.hasNext()) {
				OutputModule om = itOM.next();
				menuItem = new JMenuItem(om.name());
				menuItem.addActionListener(listener);
				menuItem.setActionCommand("OutputModule");
				if (om.referenceCount() > 0)
					menuItem.setEnabled(false);
				outputMenu.add(menuItem);
			}
		}

		// dynamically fill menus to add remaining module types
		HashMap<String, JMenu> menuHashMap = new HashMap<String, JMenu>();
		Iterator<ModuleTemplate> it = release.moduleTemplateIterator();
		while (it.hasNext()) {
			ModuleTemplate t = it.next();

			JMenu moduleTypeMenu;
			JMenu moduleTypeAllMenu;
			JMenu moduleTypeAndLetterMenu;

			String moduleType = t.type();
			if (moduleType.equals("OutputModule"))
				continue;

			String moduleTypeAll = moduleType + "All";
			if (!menuHashMap.containsKey(moduleType)) {
				moduleTypeMenu = new ScrollableMenu(moduleType);
				moduleTypeAllMenu = new ScrollableMenu("All");
				menuHashMap.put(moduleType, moduleTypeMenu);
				menuHashMap.put(moduleTypeAll, moduleTypeAllMenu);
				if (!isSwitchProducer) {
					moduleMenu.add(moduleTypeMenu);
					moduleTypeMenu.add(moduleTypeAllMenu);
				} else {
					if (t.type().equals("EDProducer")) {
						moduleMenu.add(moduleTypeMenu);
					}
				}
			} else {
				moduleTypeMenu = menuHashMap.get(moduleType);
				moduleTypeAllMenu = menuHashMap.get(moduleTypeAll);
			}

			String moduleLetter = t.name().substring(0, 1);
			String moduleTypeAndLetter = t.type() + moduleLetter;
			if (!menuHashMap.containsKey(moduleTypeAndLetter)) {
				moduleTypeAndLetterMenu = new ScrollableMenu(moduleLetter);
				menuHashMap.put(moduleTypeAndLetter, moduleTypeAndLetterMenu);
				if (!isSwitchProducer)
					moduleTypeMenu.add(moduleTypeAndLetterMenu);
				else {
					if (t.type().equals("EDProducer")) {
						moduleTypeMenu.add(moduleTypeAndLetterMenu);
					}
				}
			} else {
				moduleTypeAndLetterMenu = menuHashMap.get(moduleTypeAndLetter);
			}

			if (t.instanceCount() > 0) {
				if (isSwitchProducer && !t.type().equals("EDProducer"))
					continue;

				JMenu instanceMenuAll = new ScrollableMenu(t.name());
				JMenu instanceMenu = new ScrollableMenu(t.name());
				menuItemAll = new JMenuItem("New Instance");
				menuItem = new JMenuItem("New Instance");
				menuItemAll.addActionListener(listener);
				menuItem.addActionListener(listener);
				menuItemAll.setActionCommand(t.name());
				menuItem.setActionCommand(t.name());
				instanceMenuAll.add(menuItemAll);
				instanceMenu.add(menuItem);

				JMenu copyMenuAll = new ScrollableMenu("Copy");
				JMenu copyMenu = new ScrollableMenu("Copy");
				instanceMenuAll.add(copyMenuAll);
				instanceMenu.add(copyMenu);

				instanceMenuAll.addSeparator();
				instanceMenu.addSeparator();

				ArrayList<String> sortedInstanceNames = new ArrayList<String>();
				for (int i = 0; i < t.instanceCount(); i++) {
					ModuleInstance instance = (ModuleInstance) t.instance(i);
					sortedInstanceNames.add(instance.name());
				}
				Collections.sort(sortedInstanceNames);

				Iterator<String> itI = sortedInstanceNames.iterator();
				while (itI.hasNext()) {
					String instanceName = itI.next();
					ModuleInstance instance = null;
					try {
						instance = (ModuleInstance) t.instance(instanceName);
					} catch (DataException ex) {
					}

					menuItemAll = new JMenuItem(instance.name());
					menuItem = new JMenuItem(instance.name());
					copyItemAll = new JMenuItem(instance.name());
					copyItem = new JMenuItem(instance.name());
					menuItemAll.addActionListener(listener);
					menuItem.addActionListener(listener);
					copyItemAll.addActionListener(listener);
					copyItem.addActionListener(listener);
					menuItemAll.setActionCommand(t.name() + ":" + instance.name());
					menuItem.setActionCommand(t.name() + ":" + instance.name());
					copyItemAll.setActionCommand("copy:" + t.name() + ":" + instance.name());
					copyItem.setActionCommand("copy:" + t.name() + ":" + instance.name());

					if (container != null) {
						for (int j = 0; j < container.entryCount(); j++) {
							Reference reference = container.entry(j);
							if (instance.isReferencedBy(reference)) {
								menuItemAll.setEnabled(false);
								menuItem.setEnabled(false);
								copyItemAll.setEnabled(false);
								copyItem.setEnabled(false);
								break;
							}
						}
					}
					if (module != null) {
						if (module.name().equals(instance.name())) {
							menuItemAll.setEnabled(false);
							menuItem.setEnabled(false);
							copyItemAll.setEnabled(false);
							copyItem.setEnabled(false);
						}
					}

					instanceMenuAll.add(menuItemAll);
					instanceMenu.add(menuItem);
					copyMenuAll.add(copyItemAll);
					copyMenu.add(copyItem);
				}
				moduleTypeAllMenu.add(instanceMenuAll);
				moduleTypeAndLetterMenu.add(instanceMenu);
			} else {
				menuItemAll = new JMenuItem(t.name());
				menuItem = new JMenuItem(t.name());
				menuItemAll.setActionCommand(t.name());
				menuItem.setActionCommand(t.name());
				menuItemAll.addActionListener(listener);
				menuItem.addActionListener(listener);
				moduleTypeAllMenu.add(menuItemAll);
				moduleTypeAndLetterMenu.add(menuItem);
			}
		}
		return moduleMenu;
	}

	/** create the 'Add/Replace EDAlias' submenu */ // TODO: test and fix this
	private JMenu createAddRepEDAliasMenu(ReferenceContainer container, EDAliasInstance edAlias,
			ActionListener listener, boolean isAdd) {
		JMenu edAliasMenu = null;
		JMenuItem menuItem;
		if (isAdd) {
			edAliasMenu = new JMenu("Add EDAlias");
		} else {
			edAliasMenu = new JMenu("Replace EDAlias");
		}

		menuItem = new JMenuItem("New EDAlias");
		menuItem.addActionListener(listener);
		menuItem.setActionCommand("NEWEDA");
		edAliasMenu.add(menuItem);
		/*
		 * edAliasMenu.addSeparator();
		 * 
		 * 
		 * JMenuItem menuItemAll; JMenuItem copyItemAll; JMenuItem copyItem;
		 * 
		 * TreeModel model = tree.getModel(); Configuration config = (Configuration)
		 * model.getRoot();
		 * 
		 * // dynamically fill menus to add remaining edAlias types HashMap<String,
		 * JMenu> menuHashMap = new HashMap<String, JMenu>(); Iterator<EDAliasInstance>
		 * it2 = container.edAliasIterator(); int edAliasNumber = config.edAliasCount();
		 * for (int i = 0; i < edAliasNumber; i++) { EDAliasInstance ed =
		 * config.edAlias(i);
		 * 
		 * System.out.println("EDAlias: " + ed.name());
		 * 
		 * JMenu edAliasTypeAllMenu; JMenu edAliasLetterMenu;
		 * 
		 * String edAliasTypeAll = "All"; if (!menuHashMap.containsKey(edAliasTypeAll))
		 * { // edAliasTypeMenu = new ScrollableMenu("All EDAliases");
		 * edAliasTypeAllMenu = new ScrollableMenu("All"); //
		 * menuHashMap.put(moduleType, edAliasTypeMenu); menuHashMap.put(edAliasTypeAll,
		 * edAliasTypeAllMenu); // edAliasMenu.add(edAliasTypeMenu);
		 * edAliasMenu.add(edAliasTypeAllMenu); //
		 * edAliasTypeMenu.add(edAliasTypeAllMenu); } else { // edAliasTypeMenu =
		 * menuHashMap.get(moduleType); edAliasTypeAllMenu =
		 * menuHashMap.get(edAliasTypeAll); }
		 * 
		 * String edAliasLetter = ed.name().substring(0, 1); if
		 * (!menuHashMap.containsKey(edAliasLetter)) { edAliasLetterMenu = new
		 * ScrollableMenu(edAliasLetter); menuHashMap.put(edAliasLetter,
		 * edAliasLetterMenu); // edAliasTypeMenu.add(edAliasLetterMenu);
		 * edAliasTypeAllMenu.add(edAliasLetterMenu); } else { edAliasLetterMenu =
		 * menuHashMap.get(edAliasLetter); }
		 * 
		 * while (it2.hasNext()) { EDAliasInstance ed2 = it2.next();
		 * 
		 * // if 2 EDAliases are called the same (maybe object comparison instead?)
		 * forbid // some options if (ed.name().matches(ed2.name())) { JMenu
		 * instanceMenuAll = new ScrollableMenu(ed.name()); JMenu instanceMenu = new
		 * ScrollableMenu(ed.name()); menuItemAll = new JMenuItem("New Instance");
		 * menuItem = new JMenuItem("New Instance");
		 * menuItemAll.addActionListener(listener);
		 * menuItem.addActionListener(listener);
		 * menuItemAll.setActionCommand(ed.name());
		 * menuItem.setActionCommand(ed.name()); instanceMenuAll.add(menuItemAll);
		 * instanceMenu.add(menuItem);
		 * 
		 * JMenu copyMenuAll = new ScrollableMenu("Copy"); JMenu copyMenu = new
		 * ScrollableMenu("Copy"); instanceMenuAll.add(copyMenuAll);
		 * instanceMenu.add(copyMenu);
		 * 
		 * instanceMenuAll.addSeparator(); instanceMenu.addSeparator();
		 * 
		 * ArrayList<String> sortedInstanceNames = new ArrayList<String>(); for (int j =
		 * 0; j < ed.referenceCount(); j++) { // for (int i = 0; i < t.instanceCount();
		 * i++) { // ModuleInstance instance = (ModuleInstance) t.instance(i); //
		 * EDAliasInstance instance = (EDAliasInstance) //
		 * sortedInstanceNames.add(instance.name());
		 * sortedInstanceNames.add(ed.reference(j).name()); // BSATARIC: TODO: I'm not
		 * sure how to do this // like instances
		 * 
		 * } Collections.sort(sortedInstanceNames);
		 * 
		 * Iterator<String> itI = sortedInstanceNames.iterator(); while (itI.hasNext())
		 * { String instanceName = itI.next(); // ModuleInstance instance = null;
		 * EDAliasInstance instance1 = null; // instance = (ModuleInstance)
		 * t.instance(instanceName); instance1 = config.insertEDAlias(instanceName); //
		 * this probably cannot be like this as it will // be the same name
		 * 
		 * menuItemAll = new JMenuItem(instance1.name()); menuItem = new
		 * JMenuItem(instance1.name()); copyItemAll = new JMenuItem(instance1.name());
		 * copyItem = new JMenuItem(instance1.name());
		 * menuItemAll.addActionListener(listener);
		 * menuItem.addActionListener(listener);
		 * copyItemAll.addActionListener(listener);
		 * copyItem.addActionListener(listener); menuItemAll.setActionCommand(ed.name()
		 * + ":" + instance1.name()); menuItem.setActionCommand(ed.name() + ":" +
		 * instance1.name()); copyItemAll.setActionCommand("copy:" + ed.name() + ":" +
		 * instance1.name()); copyItem.setActionCommand("copy:" + ed.name() + ":" +
		 * instance1.name());
		 * 
		 * if (container != null) { for (int j = 0; j < container.entryCount(); j++) {
		 * Reference reference = container.entry(j); if
		 * (instance1.isReferencedBy(reference)) { menuItemAll.setEnabled(false);
		 * menuItem.setEnabled(false); copyItemAll.setEnabled(false);
		 * copyItem.setEnabled(false); break; } } } if (edAlias != null) { if
		 * (edAlias.name().equals(instance1.name())) { menuItemAll.setEnabled(false);
		 * menuItem.setEnabled(false); copyItemAll.setEnabled(false);
		 * copyItem.setEnabled(false); } }
		 * 
		 * instanceMenuAll.add(menuItemAll); instanceMenu.add(menuItem);
		 * copyMenuAll.add(copyItemAll); copyMenu.add(copyItem); }
		 * edAliasTypeAllMenu.add(instanceMenuAll); edAliasLetterMenu.add(instanceMenu);
		 * } else { System.out.println("OVDE"); menuItemAll = new JMenuItem(ed.name());
		 * menuItem = new JMenuItem(ed.name()); menuItemAll.setActionCommand(ed.name());
		 * menuItem.setActionCommand(ed.name());
		 * menuItemAll.addActionListener(listener);
		 * menuItem.addActionListener(listener); edAliasTypeAllMenu.add(menuItemAll);
		 * edAliasLetterMenu.add(menuItem); } } }
		 */
		// }
		return edAliasMenu;
	}

	/** create 'Add/Replace Path' submenu */
	private JMenu createAddRepPathMenu(Object node, boolean isAdd) {
		String actionCmd = null;
		JMenu pathMenu = null;
		if (isAdd) {
			actionCmd = "PATHREF";
			pathMenu = new ScrollableMenu("Add Path");
		} else {
			actionCmd = "PATHREP";
			pathMenu = new ScrollableMenu("Replace Path");
		}
		JMenuItem menuItem;
		ArrayList<Path> forbiddenPaths = new ArrayList<Path>();

		if (node instanceof Path) {
			Path path = (Path) node;
			forbiddenPaths.add(path);
			menuItem = new JMenuItem("New Path");
			menuItem.addActionListener(pathListener);
			menuItem.setActionCommand("NEWPATH");
			pathMenu.add(menuItem);
			pathMenu.addSeparator();
		} else if (node instanceof Reference) {
			Reference reference = (Reference) node;
			Path parentPath = (Path) reference.container();
			forbiddenPaths.add(parentPath);
			for (int i = 0; i < parentPath.entryCount(); i++) {
				Reference r = parentPath.entry(i);
				if (r instanceof PathReference) {
					PathReference pathreference = (PathReference) r;
					Path path = (Path) reference.parent();
					forbiddenPaths.add(path);
				}
			}
		}

		IConfiguration config = (IConfiguration) treeModel.getRoot();
		for (int i = 0; i < config.pathCount(); i++) {
			Path path = config.path(i);
			menuItem = new JMenuItem(path.name());
			menuItem.addActionListener(pathListener);
			menuItem.setActionCommand(actionCmd);
			if (forbiddenPaths.contains(path))
				menuItem.setEnabled(false);
			pathMenu.add(menuItem);
		}
		return pathMenu;
	}

	/** create 'Add/Replace Sequence' Menu */
	private JMenu createAddRepSequenceMenu(ReferenceContainer pathOrSequence, ActionListener listener, boolean isSeqRef,
			boolean isAdd) {
		String actionCmd = null;
		JMenu sequenceMenu = null;
		if (isAdd) {
			actionCmd = "SEQREF";
			sequenceMenu = new ScrollableMenu("Add Sequence");
		} else {
			actionCmd = "SEQREP";
			sequenceMenu = new ScrollableMenu("Replace Sequence");
		}
		JMenuItem menuItem;
		ArrayList<Sequence> forbiddenSequences = new ArrayList<Sequence>();

		if (pathOrSequence instanceof Sequence) {
			Sequence sequence = (Sequence) pathOrSequence;
			forbiddenSequences.add(sequence);
			if (!isSeqRef) {
				menuItem = new JMenuItem("New Sequence");
				menuItem.addActionListener(listener);
				menuItem.setActionCommand("NEWSEQ");
				sequenceMenu.add(menuItem);
				sequenceMenu.addSeparator();
			}
		}

		for (int i = 0; i < pathOrSequence.entryCount(); i++) {
			Reference reference = pathOrSequence.entry(i);
			if (reference instanceof SequenceReference) {
				SequenceReference seqreference = (SequenceReference) reference;
				Sequence sequence = (Sequence) seqreference.parent();
				forbiddenSequences.add(sequence);
			}
		}

		IConfiguration config = (IConfiguration) treeModel.getRoot();
		for (int i = 0; i < config.sequenceCount(); i++) {
			Sequence sequence = config.sequence(i);
			menuItem = new JMenuItem(sequence.name());
			menuItem.addActionListener(listener);
			menuItem.setActionCommand(actionCmd);
			if (forbiddenSequences.contains(sequence))
				menuItem.setEnabled(false);
			sequenceMenu.add(menuItem);
		}
		return sequenceMenu;
	}

	/** create 'Add/Replace Task' Menu */
	private JMenu createAddRepTaskMenu(ReferenceContainer pathOrTask, ActionListener listener, boolean isTasRef,
			boolean isAdd) {
		String actionCmd = null;
		JMenu taskMenu = null;
		if (isAdd) {
			actionCmd = "TASREF";
			taskMenu = new ScrollableMenu("Add Task");
		} else {
			actionCmd = "TASREP";
			taskMenu = new ScrollableMenu("Replace Task");
		}
		JMenuItem menuItem;
		ArrayList<Task> forbiddenTasks = new ArrayList<Task>();

		if (pathOrTask instanceof Task) {
			Task task = (Task) pathOrTask;
			forbiddenTasks.add(task);
			if (!isTasRef) {
				menuItem = new JMenuItem("New Task");
				menuItem.addActionListener(listener);
				menuItem.setActionCommand("NEWTAS");
				taskMenu.add(menuItem);
				taskMenu.addSeparator();
			}
		}

		for (int i = 0; i < pathOrTask.entryCount(); i++) {
			Reference reference = pathOrTask.entry(i);
			if (reference instanceof TaskReference) {
				TaskReference tasreference = (TaskReference) reference;
				Task task = (Task) tasreference.parent();
				forbiddenTasks.add(task);
			}
		}

		IConfiguration config = (IConfiguration) treeModel.getRoot();
		for (int i = 0; i < config.taskCount(); i++) {
			Task task = config.task(i);
			menuItem = new JMenuItem(task.name());
			menuItem.addActionListener(listener);
			menuItem.setActionCommand(actionCmd);
			if (forbiddenTasks.contains(task))
				menuItem.setEnabled(false);
			taskMenu.add(menuItem);
		}
		return taskMenu;
	}

	/** create 'Add/Replace SwitchProducer' Menu */
	private JMenu createAddRepSPMenu(ReferenceContainer pathOrSP, ActionListener listener, boolean isSPRef,
			boolean isAdd) {
		String actionCmd = null;
		JMenu switchProducerMenu = null;
		if (isAdd) {
			actionCmd = "SPREF";
			switchProducerMenu = new ScrollableMenu("Add SwitchProducer");
		} else {
			actionCmd = "SPREP";
			switchProducerMenu = new ScrollableMenu("Replace SwitchProducer");
		}
		JMenuItem menuItem;
		ArrayList<SwitchProducer> forbiddenSwitchProducers = new ArrayList<SwitchProducer>();

		if (pathOrSP instanceof SwitchProducer) {
			SwitchProducer switchProducer = (SwitchProducer) pathOrSP;
			forbiddenSwitchProducers.add(switchProducer);
			if (!isSPRef) {
				menuItem = new JMenuItem("New SwitchProducer");
				menuItem.addActionListener(listener);
				menuItem.setActionCommand("NEWSP");
				switchProducerMenu.add(menuItem);
				switchProducerMenu.addSeparator();
			}
		}

		for (int i = 0; i < pathOrSP.entryCount(); i++) {
			Reference reference = pathOrSP.entry(i);
			if (reference instanceof SwitchProducerReference) {
				SwitchProducerReference spreference = (SwitchProducerReference) reference;
				SwitchProducer switchProducer = (SwitchProducer) spreference.parent();
				forbiddenSwitchProducers.add(switchProducer);
			}
		}

		IConfiguration config = (IConfiguration) treeModel.getRoot();
		for (int i = 0; i < config.switchProducerCount(); i++) {
			SwitchProducer switchProducer = config.switchProducer(i);
			menuItem = new JMenuItem(switchProducer.name());
			menuItem.addActionListener(listener);
			menuItem.setActionCommand(actionCmd);
			if (forbiddenSwitchProducers.contains(switchProducer))
				menuItem.setEnabled(false);
			switchProducerMenu.add(menuItem);
		}
		return switchProducerMenu;
	}

	/** create 'Set Operator' Menu */
	private JMenu createSetOperatorMenu(Reference reference, ActionListener listener) {
		JMenu menu = new ScrollableMenu("Set Operator");
		for (Operator op : Operator.values()) {
			if (reference.getOperator() != op) {
				JMenuItem menuItem = new JMenuItem(op.toString());
				menuItem.addActionListener(listener);
				menuItem.setActionCommand("Set Operator");
				menu.add(menuItem);
			}
		}
		return menu;
	}

}

/**
 * listen to actions from the 'PSets' popup menu
 */
class PSetMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** reference to the parent frame */
	private JFrame frame = null;

	/** standard constructor */
	public PSetMenuListener(JTree tree, JFrame frame) {
		this.tree = tree;
		this.frame = frame;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();

		/*
		 * if (cmd.equals("Sort")) { ConfigurationTreeActions.sortPSets(tree); } else
		 */
		if (cmd.equals("Remove PSet")) {
			PSetParameter pset = (PSetParameter) tree.getSelectionPath().getLastPathComponent();
			ConfigurationTreeActions.removePSet(tree, pset);
		} else if (cmd.equals("Add PSet")) {
			AddParameterDialog dlg = new AddParameterDialog(frame, true);
			dlg.addParameterSet();
			dlg.pack();
			dlg.setLocationRelativeTo(frame);
			dlg.setVisible(true);
			if (dlg.validChoice()) {
				PSetParameter pset = (PSetParameter) ParameterFactory.create(dlg.type(), dlg.name(),
						dlg.valueAsString(), dlg.isTracked());
				ConfigurationTreeActions.insertPSet(tree, pset);
			}
		}
	}

}


/**
 * listen to actions from the 'Global EDAliases' popup menu
 */
class GlobalEDAliasMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** reference to the parent frame */
	private JFrame frame = null;

	/** standard constructor */
	public GlobalEDAliasMenuListener(JTree tree, JFrame frame) {
		this.tree = tree;
		this.frame = frame;
		
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();

		if (cmd.equals("Remove Global EDAlias")) {
			EDAliasInstance globalEDAlias = (EDAliasInstance) tree.getSelectionPath().getLastPathComponent();
			ConfigurationTreeActions.removeGlobalEDAlias(tree, globalEDAlias);
		} else if (cmd.equals("Add Global EDAlias")) {		
			ConfigurationTreeActions.insertGlobalEDAlias(tree);
		}
	}

}

/**
 * listen to actions from the 'EDSource' popup menu
 */
class EDSourceMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public EDSourceMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortEDSources(tree);
		} else if (cmd.equals("Remove EDSource")) {
			EDSourceInstance edsource = (EDSourceInstance) node;
			ConfigurationTreeActions.removeEDSource(tree, edsource);
		} else { // cmd = template name of new EDSource !
			ConfigurationTreeActions.insertEDSource(tree, cmd);
		}
	}

}

/**
 * listen to actions from the 'ESSources' popup menu
 */
class ESSourceMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public ESSourceMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortESSources(tree);
		} else if (cmd.equals("Remove ESSource")) {
			ESSourceInstance essource = (ESSourceInstance) node;
			ConfigurationTreeActions.removeESSource(tree, essource);
		} else if (cmd.equals("Rename ESSource")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else {
			String templateName = source.getActionCommand();
			ConfigurationTreeActions.insertESSource(tree, templateName);
		}
	}
}

/**
 * listen to actions from the 'ESModules' popup menu
 */
class ESModuleMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public ESModuleMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortESModules(tree);
		} else if (cmd.equals("Remove ESModule")) {
			ESModuleInstance esmodule = (ESModuleInstance) node;
			ConfigurationTreeActions.removeESModule(tree, esmodule);
		} else if (cmd.equals("Rename ESModule")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else {
			String templateName = source.getActionCommand();
			ConfigurationTreeActions.insertESModule(tree, templateName);
		}
	}
}

/**
 * listen to actions from the 'Services' popup menu
 */
class ServiceMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public ServiceMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortServices(tree);
		} else if (cmd.equals("Remove Service")) {
			ServiceInstance service = (ServiceInstance) node;
			ConfigurationTreeActions.removeService(tree, service);
		} else {
			String templateName = cmd;
			ConfigurationTreeActions.insertService(tree, templateName);
		}
	}

}

/**
 * listen to actions from the 'Paths' popup menu
 */
class PathMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public PathMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		String action = source.getActionCommand();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (action.equals("NEWPATH")) {
			ConfigurationTreeActions.insertPath(tree);
		} else if (action.equals("PATHREF")) {
			ConfigurationTreeActions.insertReference(tree, "Path", cmd);
		} else if (action.equals("PATHREP")) {
			ConfigurationTreeActions.replaceContainerInternally(tree, "Path", (Path) node, cmd);
		} else if (action.equals("SEQREF")) {
			ConfigurationTreeActions.insertReference(tree, "Sequence", cmd);
		} else if (action.equals("TASREF")) {
			ConfigurationTreeActions.insertReference(tree, "Task", cmd);
		} else if (action.equals("SPREF")) {
			ConfigurationTreeActions.insertReference(tree, "SwitchProducer", cmd);
		} else if (action.equals("GOTO")) {
			ConfigurationTreeActions.scrollToInstance(tree);
		} else if (action.startsWith("REMOVEPATH:")) {
			// bug #82526: add/remove path to/from a primary dataset
			String datasetName = action.split(":")[1];
			ConfigurationTreeActions.removePathFromDataset(tree, datasetName);
		} else if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortPaths(tree);
		} else if (cmd.equals("Rename Path")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else if (cmd.equals("Deep Clone Path")) {
			ConfigurationTreeActions.DeepCloneContainer(tree, (Path) node, null);
		} else if (cmd.equals("Clone Path")) {
			ConfigurationTreeActions.CloneReferenceContainer(tree, (Path) node);
		} else if (cmd.equals("Remove Path")) {
			if (node instanceof Path)
				ConfigurationTreeActions.removeReferenceContainer(tree);
			else if (node instanceof PathReference)
				ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove Module")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove OutputModule")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove Sequence")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove Task")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove SwitchProducer")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (action.equals("OutputModule")) {
			ConfigurationTreeActions.insertReference(tree, "OutputModule", cmd);
		} else if (action.equals("Set Operator")) {
			ConfigurationTreeActions.setOperator(tree, cmd);
		} else if (cmd.equals("Clone Module")) {
			ConfigurationTreeActions.CloneModule(tree, (ModuleReference) node, null);
		}
		// add a module(-reference) to the currently selected path
		else {
			ConfigurationTreeActions.insertReference(tree, "Module", action);
		}
	}

}

/**
 * listen to actions from the 'Sequences' popup menu x
 */
class SequenceMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public SequenceMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		String action = source.getActionCommand();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (action.equals("RMUNREF")) {
			ConfigurationTreeActions.removeUnreferencedSequences(tree);
		} else if (action.equals("RESOLVE")) {
			ConfigurationTreeActions.resolveUnnecessarySequences(tree);
		} else if (action.equals("NEWSEQ")) {
			ConfigurationTreeActions.insertSequence(tree);
		} else if (action.equals("SEQREF")) {
			ConfigurationTreeActions.insertReference(tree, "Sequence", cmd);
		} else if (action.equals("TASREF")) {
			ConfigurationTreeActions.insertReference(tree, "Task", cmd);
		} else if (action.equals("SPREF")) {
			ConfigurationTreeActions.insertReference(tree, "SwitchProducer", cmd);
		} else if (action.equals("SEQREP")) {
			ConfigurationTreeActions.replaceContainerInternally(tree, "Sequence", (Sequence) node, cmd);
		} else if (action.equals("GOTO")) {
			ConfigurationTreeActions.scrollToInstance(tree);
		} else if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortSequences(tree);
		} else if (cmd.equals("Add Sequence")) {
			ConfigurationTreeActions.insertSequence(tree);
		} else if (cmd.equals("Rename Sequence")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else if (cmd.equals("Deep Clone Sequence")) {
			ConfigurationTreeActions.DeepCloneSequence(tree, (Sequence) node, null);
		} else if (cmd.equals("Clone Sequence")) {
			ConfigurationTreeActions.CloneReferenceContainer(tree, (Sequence) node);
		} else if (cmd.equals("Remove Sequence")) {
			if (node instanceof Sequence) {
				Sequence sequence = (Sequence) node;
				if (sequence.referenceCount() > 0) {
					StringBuffer warning = new StringBuffer();
					warning.append("Do you really want to remove '").append(sequence.name())
							.append("', which is referenced ").append(sequence.referenceCount()).append(" times?");
					if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(null, warning.toString(), "",
							JOptionPane.OK_CANCEL_OPTION))
						return;
				}
				ConfigurationTreeActions.removeReferenceContainer(tree);
			} else if (node instanceof SequenceReference)
				ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove Task")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove SwitchProducer")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove Module")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove OutputModule")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (action.equals("OutputModule")) {
			ConfigurationTreeActions.insertReference(tree, "OutputModule", cmd);
		} else if (action.equals("Set Operator")) {
			ConfigurationTreeActions.setOperator(tree, cmd);
		} else if (cmd.equals("Clone Module")) {
			ConfigurationTreeActions.CloneModule(tree, (ModuleReference) node, null);
		}
		// add a module to the selected sequence
		else {
			ConfigurationTreeActions.insertReference(tree, "Module", action);
		}
	}

}

/**
 * listen to actions from the 'Tasks' popup menu x
 */
class TaskMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public TaskMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		String action = source.getActionCommand();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (action.equals("RMUNREFTAS")) {
			ConfigurationTreeActions.removeUnreferencedTasks(tree);
		} else if (action.equals("RESOLVETAS")) {
			ConfigurationTreeActions.resolveUnnecessaryTasks(tree);
		} else if (action.equals("NEWTAS")) {
			ConfigurationTreeActions.insertTask(tree);
		} else if (action.equals("TASREF")) {
			ConfigurationTreeActions.insertReference(tree, "Task", cmd);
		} else if (action.equals("SPREF")) {
			ConfigurationTreeActions.insertReference(tree, "SwitchProducer", cmd);
		} else if (action.equals("TASREP")) {
			ConfigurationTreeActions.replaceContainerInternally(tree, "Task", (Task) node, cmd);
		} else if (action.equals("GOTO")) {
			ConfigurationTreeActions.scrollToInstance(tree);
		} else if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortTasks(tree);
		} else if (cmd.equals("Add Task")) {
			ConfigurationTreeActions.insertTask(tree);
		} else if (cmd.equals("Rename Task")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else if (cmd.equals("Deep Clone Task")) {
			ConfigurationTreeActions.DeepCloneTask(tree, (Task) node, null);
		} else if (cmd.equals("Clone Task")) {
			ConfigurationTreeActions.CloneReferenceContainer(tree, (Task) node);
		} else if (cmd.equals("Remove Task")) {
			if (node instanceof Task) {
				Task task = (Task) node;
				if (task.referenceCount() > 0) {
					StringBuffer warning = new StringBuffer();
					warning.append("Do you really want to remove '").append(task.name())
							.append("', which is referenced ").append(task.referenceCount()).append(" times?");
					if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(null, warning.toString(), "",
							JOptionPane.OK_CANCEL_OPTION))
						return;
				}
				ConfigurationTreeActions.removeReferenceContainer(tree);
			} else if (node instanceof TaskReference)
				ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove Module")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove SwitchProducer")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove OutputModule")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (action.equals("OutputModule")) {
			ConfigurationTreeActions.insertReference(tree, "OutputModule", cmd);
		} else if (action.equals("Set Operator")) {
			ConfigurationTreeActions.setOperator(tree, cmd);
		} else if (cmd.equals("Clone Module")) {
			ConfigurationTreeActions.CloneModule(tree, (ModuleReference) node, null);
		}
		// add a module to the selected task
		else {
			ConfigurationTreeActions.insertReference(tree, "Module", action);
		}
	}
}

/**
 * listen to actions from the 'Switch producers' popup menu x
 */
class SwitchProducerMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;
	
	/** reference to the parent frame */
	private JFrame frame = null;
	
	/** reference to the parent GUI */
    private ConfDbGUI app = null;


	/** standard constructor */
	public SwitchProducerMenuListener(JTree tree, JFrame frame, ConfDbGUI app) {
		this.tree = tree;
		this.frame = frame;
		this.app = app;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		String action = source.getActionCommand();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (action.equals("RMUNREFSP")) {
			ConfigurationTreeActions.removeUnreferencedSwitchProducers(tree);
		} else if (action.equals("RESOLVESP")) {
			ConfigurationTreeActions.resolveUnnecessarySwitchProducers(tree);
		} else if (action.equals("NEWSP")) {
			ConfigurationTreeActions.insertSwitchProducer(tree);
		} else if (action.equals("NEWEDA")) {
			ConfigurationTreeActions.insertEDAlias(tree);
		} else if (action.equals("SPREF")) {
			ConfigurationTreeActions.insertReference(tree, "SwitchProducer", cmd);
		} else if (action.equals("SPREP")) {
			ConfigurationTreeActions.replaceContainerInternally(tree, "SwitchProducer", (SwitchProducer) node, cmd);
		} else if (action.equals("GOTO")) {
			ConfigurationTreeActions.scrollToInstance(tree);
		} else if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortSwitchProducers(tree);
		} else if (cmd.equals("Add SwitchProducer")) {
			ConfigurationTreeActions.insertSwitchProducer(tree);
		} else if (cmd.equals("Rename SwitchProducer")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else if (cmd.equals("Deep Clone SwitchProducer")) {
			ConfigurationTreeActions.DeepCloneSwitchProducer(tree, (SwitchProducer) node, null);
		} else if (cmd.equals("Clone SwitchProducer")) {
			ConfigurationTreeActions.CloneReferenceContainer(tree, (SwitchProducer) node);
		} else if (cmd.equals("Remove SwitchProducer")) {
			if (node instanceof SwitchProducer) {
				SwitchProducer switchProducer = (SwitchProducer) node;
				if (switchProducer.referenceCount() > 0) {
					StringBuffer warning = new StringBuffer();
					warning.append("Do you really want to remove '").append(switchProducer.name())
							.append("', which is referenced ").append(switchProducer.referenceCount())
							.append(" times?");
					if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(null, warning.toString(), "",
							JOptionPane.OK_CANCEL_OPTION))
						return;
				}
				ConfigurationTreeActions.removeReferenceContainer(tree);
			} else if (node instanceof SwitchProducerReference)
				ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove Module")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove EDAlias")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Rename EDAlias")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else if (cmd.equals("Add Module (VPSet)")) {
			app.addTrackedVPsetParameter();
		} else if (cmd.equals("Remove module (VPSet)")) {
			System.out.println("REMOVE MODULE (VPSET)");
			//ConfigurationTreeActions.removeReference(tree);
		} else if (cmd.equals("Remove OutputModule")) {
			ConfigurationTreeActions.removeReference(tree);
		} else if (action.equals("OutputModule")) {
			ConfigurationTreeActions.insertReference(tree, "OutputModule", cmd);
		} else if (action.equals("Set Operator")) {
			ConfigurationTreeActions.setOperator(tree, cmd);
		} else if (cmd.equals("Clone Module")) {
			ConfigurationTreeActions.CloneModule(tree, (ModuleReference) node, null);
		} else if (cmd.equals("Clone EDAlias")) {
			ConfigurationTreeActions.CloneEDAlias(tree, (EDAliasReference) node, null);
		}
		// add a module to the selected task
		else {
			ConfigurationTreeActions.insertReference(tree, "Module", action);
		} // else here is missing for EDALias see how to do it
	}
}

/**
 * listen to actions from the 'Modules' popup menu
 */
class ModuleMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public ModuleMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener interface */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		String action = source.getActionCommand();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		if (cmd.equals("Sort")) {
			ConfigurationTreeActions.sortModules(tree);
		} else if (cmd.equals("Rename Module")) {

			String oldModuleName = ((ModuleInstance)node).name();
			//System.out.println("OLD MODULE NAME: " + oldModuleName);
			ConfigurationTreeActions.editNodeName(tree);
			//String newModuleName = ((ModuleInstance)node).name();
			//System.out.println("NEW MODULE NAME: " + oldModuleName);

			//TODO: Here go through all EDAliases and rename VPSet name(s) if necessary
			//this will need new method in ConfigurationTreeAction
			//ConfigurationTreeActions.renameEDAliasVPSets(tree);
		}
		// replace the module by the selected one
		else {
			ConfigurationTreeActions.replaceModuleInternally(tree, (ModuleInstance) node, action);
		}
	}
}

/**
 * listen to actions from the 'Contents' popup menu
 */
class ContentMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** standard constructor */
	public ContentMenuListener(JTree tree) {
		this.tree = tree;
	}

	/** ActionListener: actionPerformed() */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		String action = source.getActionCommand();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		if (action.equals("ADD")) {
			ConfigurationTreeActions.insertContent(tree);
		} else if (action.equals("REMOVE")) {
			ConfigurationTreeActions.removeContent(tree);
		} else if (action.equals("RENAME")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else if (action.startsWith("SHOW:")) {
			model.setContentMode(action.split(":")[1]);
			Iterator<EventContent> itC = config.contentIterator();
			while (itC.hasNext())
				model.nodeStructureChanged(itC.next());
		}
	}
}

/**
 * listen to actions from the 'Streams' popup menu
 */
class StreamMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** frame */
	private JFrame frame = null;

	/** standard constructor */
	public StreamMenuListener(JTree tree, JFrame frame) {
		this.tree = tree;
		this.frame = frame;
	}

	/** ActionListener: actionPerformed() */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		String action = source.getActionCommand();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		if (action.equals("ADD")) {

			CreateStreamDialog dlg = new CreateStreamDialog(frame, config);
			dlg.pack();
			dlg.setLocationRelativeTo(frame);
			dlg.setVisible(true);
			if (dlg.isSuccess())
				ConfigurationTreeActions.insertStream(tree, dlg.stream());
		} else if (action.equals("ADDPATH")) {
			ConfigurationTreeActions.addPathToStream(tree, cmd);
		} else if (action.startsWith("ADDDATASETTO:")) {
			String streamName = action.split(":")[1];
			CreateDatasetDialog dlg = new CreateDatasetDialog(frame, config);
			dlg.fixStreamName(streamName);
			dlg.pack();
			dlg.setLocationRelativeTo(frame);
			dlg.setVisible(true);
			if (dlg.isSuccess())
				ConfigurationTreeActions.insertPrimaryDataset(tree, dlg.dataset());
		} else if (action.equals("FRACTION")) {
			Stream stream = (Stream) node;
			String fractionAsString = JOptionPane.showInputDialog(null,
					"Enter fraction-to-disk " + "for Stream " + stream.name() + " [" + stream.fractionToDisk() + "]",
					"", JOptionPane.QUESTION_MESSAGE);
			try {
				double fraction = Double.parseDouble(fractionAsString);
				stream.setFractionToDisk(fraction);
				model.nodeChanged(stream);
			} catch (NumberFormatException ex) {
				System.err.println(ex.getMessage());
			}
		} else if (action.equals("REMOVE")) {
			ConfigurationTreeActions.removeStream(tree);
		} else if (action.equals("REMOVEUNASSIGNED")) {
			ConfigurationTreeActions.removeUnassignedPathsFromStream(tree);
		} else if (action.equals("RENAME")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else if (action.equals("REMOVEPATH")) {
			ConfigurationTreeActions.removePathFromStream(tree);
		} else if (action.equals("REMOVEDATASET")) {
			ConfigurationTreeActions.removePrimaryDataset(tree);
		} else if (action.startsWith("SHOW:")) {
			model.setStreamMode(action.split(":")[1]);
			Iterator<Stream> itS = config.streamIterator();
			while (itS.hasNext())
				model.nodeStructureChanged(itS.next());
		} else if (action.startsWith("ADDPATHTO:")) {
			String datasetName = action.split(":")[1];
			ConfigurationTreeActions.addPathToDataset(tree, datasetName);
		}
	}
}

/**
 * listen to actions from the 'PrimaryDatasets' popup menu
 */
class DatasetMenuListener implements ActionListener {
	/** reference to the tree to be manipulated */
	private JTree tree = null;

	/** frame */
	private JFrame frame = null;

	/** standard constructor */
	public DatasetMenuListener(JTree tree, JFrame frame) {
		this.tree = tree;
		this.frame = frame;
	}

	/** ActionListener: actionPerformed() */
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String cmd = source.getText();
		String action = source.getActionCommand();
		TreePath treePath = tree.getSelectionPath();
		Object node = treePath.getLastPathComponent();

		ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
		Configuration config = (Configuration) model.getRoot();

		if (action.startsWith("EDIT")) {
			String datasetName = action.split(" ")[1];
			PrimaryDataset dataset = config.dataset(datasetName);

			// Get the list to compare later:
			ArrayList<Path> paths = new ArrayList<Path>();
			Iterator<Path> paths_ = dataset.pathIterator();
			while (paths_.hasNext())
				paths.add(paths_.next());

			EditDatasetDialog dlg = new EditDatasetDialog(frame, config, dataset);

			dlg.pack();
			dlg.setLocationRelativeTo(frame);
			dlg.setVisible(true);
			if (dataset.hasChanged()) {
				model.nodeStructureChanged(dataset);
				model.nodeStructureChanged(dataset.parentStream());
				model.updateLevel1Nodes();

				/////////////
				// bug 86605:
				// Editing datasets must update OutputModules from here and not
				// when opening the OutputModule.
				// It also must synch the modules of FilterModules (prescales)
				// and Output Modules by checking the paths differences.
				// Only recently added paths must be prescaled with 1.
				// Compare list
				ArrayList<Path> paths_later = new ArrayList<Path>();
				paths_ = dataset.pathIterator();
				while (paths_.hasNext())
					paths_later.add(paths_.next());

				// Select only new paths:
				ArrayList<Path> newpaths = new ArrayList<Path>();
				if (!paths.containsAll(paths_later)) {
					for (int i = 0; i < paths_later.size(); i++) {
						if (!paths.contains(paths_later.get(i)))
							newpaths.add(paths_later.get(i));
					}
				}

				Stream parentStream = dataset.parentStream();
				ConfigurationTreeActions.updateFilter(config, parentStream, newpaths);
			}
		} else if (action.equals("ADD")) {
			CreateDatasetDialog dlg = new CreateDatasetDialog(frame, config);
			dlg.pack();
			dlg.setLocationRelativeTo(frame);
			dlg.setVisible(true);
			if (dlg.isSuccess())
				ConfigurationTreeActions.insertPrimaryDataset(tree, dlg.dataset());
		} else if (action.equals("ADDPATH")) {

			// if (cmd.startsWith("<html><b>")) cmd = cmd.substring(9);
			// if (cmd.endsWith("</b></html>")) cmd = cmd.substring(0,cmd.length()-11);
			cmd = cleanHtmlTags(cmd);
			ConfigurationTreeActions.addPathToDataset(tree, cmd);
		} else if (action.equals("REMOVE")) {
			ConfigurationTreeActions.removePrimaryDataset(tree);
		} else if (action.equals("RENAME")) {
			ConfigurationTreeActions.editNodeName(tree);
		} else if (action.equals("REMOVEPATH")) {
			ConfigurationTreeActions.removePathFromDataset(tree);
		} else if (action.startsWith("MOVEPATH:")) {
			String targetDatasetName = action.split(":")[1];
			ConfigurationTreeActions.movePathToDataset(tree, targetDatasetName);
		}
	}

	private String cleanHtmlTags(String cmd) {
		String cmdR = cmd;
		cmdR = cmdR.replaceAll("\\<.*?\\>", "");

		return cmdR;
	}
}

/**
 * listen to item events from the ESSource / ESModule menus
 */
class ESPreferableItemListener implements ItemListener {
	/** reference to tree */
	private JTree tree = null;

	/** constructor */
	public ESPreferableItemListener(JTree tree) {
		this.tree = tree;
	}

	/** ItemListener.itemStateChanged() */
	public void itemStateChanged(ItemEvent e) {
		JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getItemSelectable();
		ConfigurationTreeActions.setPreferred(tree, item.isSelected());
	}
}

/**
 * listen to item events of the Paths menu
 */
class PathItemListener implements ItemListener {
	/** reference to tree */
	private JTree tree = null;

	/** constructor */
	public PathItemListener(JTree tree) {
		this.tree = tree;
	}

	/** ItemListener.itemStateChanged() */
	public void itemStateChanged(ItemEvent e) {
		JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getItemSelectable();
		ConfigurationTreeActions.setPathAsEndpath(tree, item.isSelected());
	}

}

package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Iterator;
import java.util.EventObject;
import java.util.ArrayList;

import confdb.data.*;

/**
 * ConfigurationTreeEditor
 * ----------------------
 * @author Philipp Schieferdecker
 *
 */
class ConfigurationTreeEditor extends DefaultTreeCellEditor {
	//
	// data members
	//

	/** Referencable to be edited */
	private Object toBeEdited = null;

	/** the configuration tree model */
	private ConfigurationTreeModel treeModel = null;

	//
	// construction
	//

	/** standard constructor */
	public ConfigurationTreeEditor(JTree tree, DefaultTreeCellRenderer renderer) {

		super(tree, renderer);
		treeModel = (ConfigurationTreeModel) tree.getModel();
	}

	//
	// member functions
	//

	/** is the cell editable? don't respond to double clicks */
	public boolean isCellEditable(EventObject e) {
		if (e instanceof MouseEvent)
			return false;
		return true;
	}

	/** DefaultTreeCellEditor's 'getCellEditorValue' */
	public Object getCellEditorValue() {

		Object value = super.getCellEditorValue();
		String name = value.toString();

		if (toBeEdited == null)
			return null;
		
		IConfiguration config = (IConfiguration) treeModel.getRoot();

		if (toBeEdited instanceof Referencable) {
			Referencable referencable = (Referencable) toBeEdited;
			try {
				if (referencable instanceof ModuleInstance) {
					ModuleInstance module = (ModuleInstance) referencable;
					module.setNameAndPropagate(name);
					// Bug 83721: Update Sequences and Paths root container.
					// propagateModuleName(referencable, treeModel.sequencesNode());
					treeModel.nodeStructureChanged(treeModel.sequencesNode());
					treeModel.nodeStructureChanged(treeModel.tasksNode());
					treeModel.nodeStructureChanged(treeModel.switchProducersNode());
					treeModel.nodeStructureChanged(treeModel.pathsNode());

				} else if (referencable instanceof EDAliasInstance) {
					EDAliasInstance globalEDAliasInstance = (EDAliasInstance) referencable;
					globalEDAliasInstance.setNameAndPropagate(name);
				} else if (referencable instanceof Path) {
					Path path = (Path) referencable;
					path.setNameAndPropagate(name);
				} else if (referencable instanceof SwitchProducer) {
					SwitchProducer switchProducer = (SwitchProducer) referencable;
					switchProducer.setNameAndPropagate(name);				
				} else {
					referencable.setName(name);
				}
				treeModel.nodeChanged(referencable);
				for (int i = 0; i < referencable.referenceCount(); i++)
					treeModel.nodeChanged(referencable.reference(i));
			} catch (DataException e) {
				System.err.println(e.getMessage());
			}
		} else if (toBeEdited instanceof Instance) {
			Instance instance = (Instance) toBeEdited;
			try {
				instance.setName(name);
				treeModel.nodeChanged(instance);
			} catch (DataException e) {
				System.err.println(e.getMessage());
			}
		} else if (toBeEdited instanceof OutputModule) {
			OutputModule output = (OutputModule) toBeEdited;
			try {
				output.setName(name);
				treeModel.nodeChanged(output);
			} catch (DataException e) {
				System.err.println(e.getMessage());
			}
		} else if (toBeEdited instanceof ModuleReference) {
			ModuleReference reference = (ModuleReference) toBeEdited;
			ModuleInstance instance = (ModuleInstance) reference.parent();
			try {
				instance.setName(name);
				treeModel.nodeChanged(instance);
				for (int i = 0; i < instance.referenceCount(); i++)
					treeModel.nodeChanged(instance.reference(i));
			} catch (DataException e) {
				System.err.println(e.getMessage());
			}
		} else if (toBeEdited instanceof EDAliasReference) {
			EDAliasReference reference = (EDAliasReference) toBeEdited;
			EDAliasInstance instance = (EDAliasInstance) reference.parent();
			try {
				instance.setName(name);
				treeModel.nodeChanged(instance);
				for (int i = 0; i < instance.referenceCount(); i++)
					treeModel.nodeChanged(instance.reference(i));
			} catch (DataException e) {
				System.err.println(e.getMessage());
			}
		} else if (toBeEdited instanceof EventContent) {
			try {
			    EventContent content = (EventContent) toBeEdited;
			    content.setName(name);
			    treeModel.nodeChanged(content);

			    // copy Configuration.contents (EventContents) to a separate array "ec_array",
			    // and use the latter when calling ConfigurationTreeModel::nodeChanged,
			    // (the nodeChanged method can in principle modify the order of Configuration.contents,
			    // because it implicitly calls methods like Configuration.indexOfContent(ec),
			    // which apply sorting to Configuration.contents)
			    // Ref: https://github.com/cms-sw/hlt-confdb/pull/69
			    ArrayList<EventContent> ec_array = new ArrayList<EventContent>();
			    for (int idx = 0; idx < config.contentCount(); ++idx) {
				ec_array.add(config.content(idx));
			    }
			    for (int idx = 0; idx < ec_array.size(); ++idx) {
				treeModel.nodeChanged(ec_array.get(idx));
			    }
			} catch (Exception e) {
			    System.err.println(e.getMessage());
			}
		} else if (toBeEdited instanceof Stream) {
			Stream stream = (Stream) toBeEdited;
			if(config.stream(name)!=null){				
				return toBeEdited;
			}
			stream.setName(name);
			treeModel.nodeChanged(stream);
			treeModel.nodeChanged(stream.outputModule());			
			treeModel.nodeStructureChanged(treeModel.streamsNode());
			treeModel.nodeStructureChanged(treeModel.outputsNode());
			Path streamOutputPath = config.path(stream.outputPathName());
			if(streamOutputPath!=null){
				treeModel.nodeChanged(streamOutputPath);
				treeModel.nodeStructureChanged(treeModel.pathsNode());
			}
		} else if (toBeEdited instanceof PrimaryDataset) {
			PrimaryDataset dataset = (PrimaryDataset) toBeEdited;
			
			//so first we check if a dataset of that name exists
			//this is important as splitSiblings will have their instance number
			//we dont want to say rename EGamma[1-9] to Muon[1-9] if Muon already exists			
			//we also check for  name clash for the dataset path to be safe
			if(config.dataset(name)!=null || !config.isUniqueQualifier(PrimaryDataset.datasetPathName(name))) {
				return toBeEdited;	
			}
			
			//so the challenge is we want to make sure there are no name clashes
			//before making any changes
			//so for some reason this function calls twice, why I dont know 
			//however it'll abort the second time due to a  name clash so all is well? sigh
			ArrayList<PrimaryDataset> splitSiblings = dataset.getSplitSiblings();			
			ArrayList<String> splitSiblingNewNames = new ArrayList<String>();
			for(int index = 0;index<splitSiblings.size();index++){
				PrimaryDataset splitSibling = splitSiblings.get(index);
				String  newName = new String(name);
				if(splitSiblings.size()!=1){
					newName += splitSibling.splitInstanceNumber();
				}
				splitSiblingNewNames.add(newName);
				if(config.dataset(newName)!=null ||
					!config.isUniqueQualifier(PrimaryDataset.datasetPathName(newName)) ){
					return toBeEdited;
				}
			}


			for(int index = 0;index<splitSiblings.size();index++){			
				PrimaryDataset splitSibling = splitSiblings.get(index);
				splitSibling.setName(splitSiblingNewNames.get(index));
				treeModel.nodeChanged(splitSibling);			
			}
			treeModel.nodeStructureChanged(treeModel.datasetsNode());
		}

		return toBeEdited;
	}

	/**
	 * to propagate module's name throughout the tree. Next method was created to
	 * solve the problem/bug83721 Method was commented because didn't work 3th level
	 * deep. Note: Don't understand why.
	 */
	/*
	 * private boolean propagateModuleName(Referencable referencable, Object
	 * parentNode) { boolean result = false;
	 * 
	 * if(referencable instanceof ModuleInstance) { int jj[] = new int[1]; int Count
	 * = treeModel.getChildCount(parentNode); for(int j=0; j < Count; j++) { Object
	 * Item = treeModel.getChild(parentNode, j);
	 * 
	 * jj[0] = j; if(Item instanceof ModuleReference) { ModuleReference module =
	 * (ModuleReference)Item; if(module.name().equals(referencable.name())) {
	 * treeModel.nodeStructureChanged(parentNode); // Refresh structure. result =
	 * true; }
	 * 
	 * } else if(Item instanceof OutputModuleReference) { OutputModuleReference
	 * module = (OutputModuleReference)Item;
	 * if(module.name().equals(referencable.name())) {
	 * treeModel.nodeStructureChanged(parentNode); // Refresh structure. result =
	 * true; }
	 * 
	 * } else if( (Item instanceof Sequence) || (Item instanceof
	 * SequenceReference)|| (Item instanceof Path) ){
	 * propagateModuleName(referencable, Item); } } // end for } return result; }
	 */

	/** to determine the offset ;) */
	protected void determineOffset(JTree tree, Object value, boolean isSelected, boolean isExpanded, boolean isLeaf,
			int row) {
		super.determineOffset(tree, value, isSelected, isExpanded, isLeaf, row);
		Component rendererComponent = super.renderer.getTreeCellRendererComponent(tree, value, isSelected, isExpanded,
				isLeaf, row, true);
		if (rendererComponent instanceof JLabel) {
			super.editingIcon = ((JLabel) rendererComponent).getIcon();
		}
	}

	/** TreeCellEditor's 'getTreeCellEditorComponent' */
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row) {
		if (value instanceof Referencable || value instanceof Reference || value instanceof Instance
				|| value instanceof OutputModule || value instanceof EventContent || value instanceof Stream
				|| value instanceof PrimaryDataset)
			toBeEdited = value;
		return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
	}

}

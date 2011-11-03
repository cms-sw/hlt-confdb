package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Iterator;
import java.util.EventObject;

import confdb.data.*;


/**
 * ConfigurationTreeEditor
 * ----------------------
 * @author Philipp Schieferdecker
 *
 */
class ConfigurationTreeEditor extends DefaultTreeCellEditor
{
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
    public ConfigurationTreeEditor(JTree tree,DefaultTreeCellRenderer renderer)
    {
    	
	super(tree,renderer);
	treeModel = (ConfigurationTreeModel)tree.getModel();
    }
    
    
    //
    // member functions
    //
    
    /** is the cell editable? don't respond to double clicks */
    public boolean isCellEditable(EventObject e)
    {
	if (e instanceof MouseEvent) return false;
	return true;
    }

    /**  DefaultTreeCellEditor's 'getCellEditorValue' */
    public Object getCellEditorValue()
    {
    	
    	
	Object value = super.getCellEditorValue();
	String name  = value.toString();

	if (toBeEdited == null) return null;
	
	IConfiguration config = (IConfiguration)treeModel.getRoot();
	
	if (toBeEdited instanceof Referencable) {
	    Referencable referencable = (Referencable)toBeEdited;
	    try {
		if (referencable instanceof ModuleInstance) {
		    ModuleInstance module = (ModuleInstance)referencable;
		    module.setNameAndPropagate(name);
		    // Bug 83721: Update Sequences and Paths root container.
		    //propagateModuleName(referencable, treeModel.sequencesNode());
		    treeModel.nodeStructureChanged(treeModel.sequencesNode());
		    treeModel.nodeStructureChanged(treeModel.pathsNode());

		}
		else if (referencable instanceof Path) {
		    Path path = (Path)referencable;
		    path.setNameAndPropagate(name);
		}
		else {
		    referencable.setName(name);
		}
		treeModel.nodeChanged(referencable);
		for (int i=0;i<referencable.referenceCount();i++)
		    treeModel.nodeChanged(referencable.reference(i));
	    }
	    catch (DataException e) {
		System.err.println(e.getMessage());
	    }
	}
	else if (toBeEdited instanceof Instance) {
	    Instance instance = (Instance)toBeEdited;
	    try {
		instance.setName(name);
		treeModel.nodeChanged(instance);
	    }
	    catch (DataException e) {
		System.err.println(e.getMessage());
	    }
	}
	else if (toBeEdited instanceof OutputModule) {
	    OutputModule output = (OutputModule)toBeEdited;
	    try {
		output.setName(name);
		treeModel.nodeChanged(output);
	    }
	    catch (DataException e) {
		System.err.println(e.getMessage());
	    }
	}
	else if (toBeEdited instanceof ModuleReference) {
	    ModuleReference reference = (ModuleReference)toBeEdited;
	    ModuleInstance  instance  = (ModuleInstance)reference.parent();
	    try {
		instance.setName(name);
		treeModel.nodeChanged(instance);
		for (int i=0;i<instance.referenceCount();i++)
		    treeModel.nodeChanged(instance.reference(i));
	    }
	    catch (DataException e) {
		System.err.println(e.getMessage());
	    }
	}
	else if (toBeEdited instanceof EventContent) {
	    EventContent content = (EventContent)toBeEdited;
	    content.setName(name);
	    treeModel.nodeChanged(content);
	    Iterator<EventContent> itC = config.contentIterator();
	    while (itC.hasNext()) treeModel.nodeChanged(itC.next());
	}
	else if (toBeEdited instanceof Stream) {
	    Stream stream = (Stream)toBeEdited;
	    stream.setName(name);
	    treeModel.nodeChanged(stream);
	    treeModel.nodeChanged(stream.outputModule());
	    treeModel.nodeStructureChanged(treeModel.streamsNode());
	    treeModel.nodeStructureChanged(treeModel.outputsNode());
	}
	else if (toBeEdited instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)toBeEdited;
	    dataset.setName(name);
	    treeModel.nodeChanged(dataset);
	}

	return toBeEdited;
    }
    
    /** to propagate module's name throughout the tree. 
     * Next method was created to solve the problem/bug83721
     * Method was commented because didn't work 3th level deep.
     * Note: Don't understand why. */
    /*
    private boolean propagateModuleName(Referencable referencable, Object parentNode) {
    	boolean result = false;
    	
    	if(referencable instanceof ModuleInstance) {
    		int jj[] = new int[1];
		    int Count	= treeModel.getChildCount(parentNode);
		    for(int j=0; j < Count; j++) {
		    	Object Item = treeModel.getChild(parentNode, j);
		    	
		    	jj[0] = j;
		    	if(Item instanceof ModuleReference) {
		    		ModuleReference module = (ModuleReference)Item;
		    		if(module.name().equals(referencable.name())) {
		    			treeModel.nodeStructureChanged(parentNode); // Refresh structure.
		    			result = true;
		    		}
		    		
		    	} else if(Item instanceof OutputModuleReference) {
		    		OutputModuleReference module = (OutputModuleReference)Item;
		    		if(module.name().equals(referencable.name())) {
		    			treeModel.nodeStructureChanged(parentNode); // Refresh structure.
		    			result = true;
		    		}
		    		
		    	} else if(	(Item instanceof Sequence)			||
		    				(Item instanceof SequenceReference)||
		    				(Item instanceof Path)				){
		    		propagateModuleName(referencable, Item);
		    	}
		    } // end for
    	}
    	return result;
    }
    */
    
	
    
    
    /** to determine the offset ;) */
    protected void determineOffset(JTree tree,
				   Object value,
				   boolean isSelected,
				   boolean isExpanded,
				   boolean isLeaf,
				   int row)
    {
	super.determineOffset(tree, value, isSelected, isExpanded, isLeaf, row);
	Component rendererComponent =
	    super.renderer.getTreeCellRendererComponent(tree,
							value,
							isSelected,
							isExpanded,
							isLeaf,
							row,
							true);
	if (rendererComponent instanceof JLabel) {
	    super.editingIcon = ((JLabel)rendererComponent).getIcon();
	}
    }
    
    /** TreeCellEditor's 'getTreeCellEditorComponent' */
    public Component getTreeCellEditorComponent(JTree   tree,
						Object  value,
						boolean isSelected,
						boolean expanded,
						boolean leaf,
						int     row)
    {
	if (value instanceof Referencable||
	    value instanceof Reference||
	    value instanceof Instance||
	    value instanceof OutputModule||
	    value instanceof EventContent||
	    value instanceof Stream||
	    value instanceof PrimaryDataset) toBeEdited = value;
	return super.getTreeCellEditorComponent(tree,value,
						isSelected,expanded,
						leaf,row);
    }
    
}

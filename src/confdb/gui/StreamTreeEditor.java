package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;

import confdb.data.*;


/**
 * StreamTreeEditor
 * ----------------
 * @author Philipp Schieferdecker
 *
 */
class StreamTreeEditor extends DefaultTreeCellEditor
{
    //
    // data members
    //
    
    /** Stream be edited */
    private Stream toBeEdited = null;
    
    /** the tree model */
    private StreamTreeModel treeModel = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public StreamTreeEditor(JTree tree,DefaultTreeCellRenderer renderer)
    {
	super(tree,renderer);
	treeModel = (StreamTreeModel)tree.getModel();
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
	String label = value.toString();

	if (toBeEdited == null) return null;

	Configuration config = treeModel.getConfiguration();
	
	for (int i=0;i<config.streamCount();i++) {
	    if (label.equals(config.stream(i).label())) {
		toBeEdited.setLabel("<ENTER UNIQUE LABEL>");
		return toBeEdited;
	    }
	}
	
	toBeEdited.setLabel(label);
	return toBeEdited;
    }
    
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
	if (value instanceof Stream) toBeEdited = (Stream)value;
	return super.getTreeCellEditorComponent(tree,value,
						isSelected,expanded,
						leaf,row);
    }
    
}

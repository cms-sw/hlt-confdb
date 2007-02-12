package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;

import confdb.data.ReferenceContainer;


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
    
    /** Path/Sequence to be edited */
    private ReferenceContainer container = null;
    
    //
    // construction
    //
    
    /** standard constructor */
    public ConfigurationTreeEditor(JTree tree,DefaultTreeCellRenderer renderer)
    {
	super(tree,renderer);
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
	if (container == null) return null;
	container.setName(value.toString());
	return container;
    }

    /** */
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
	if (value instanceof ReferenceContainer)
	    container = (ReferenceContainer)value;
	return super.getTreeCellEditorComponent(tree,value,isSelected,expanded,
						leaf,row);
    }
    
}

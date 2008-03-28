package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;

import confdb.data.Directory;
import confdb.data.ConfigInfo;


/**
 * DirectoryTreeCellEditor
 * -----------------------
 * @author Philipp Schieferdecker
 *
 * Cell editor for tree view of configurations available in a
 * configuration database instance.
 */
public class DirectoryTreeCellEditor extends DefaultTreeCellEditor
{
    //
    // member data
    //

    /** Directory to be edited */
    private Directory dir = null;
    
    //
    // construction
    //
    
    /** standard constructor */
    public DirectoryTreeCellEditor(JTree tree,DefaultTreeCellRenderer renderer)
    {
	super(tree,renderer);
    }
    
    
    //
    // member functions
    //
    
    /** is cell editable? don't respond to double clicks */
    public boolean isCellEditable(EventObject e)
    {
	if (e instanceof MouseEvent) return false;
	return true;
    }
    
    /**  DefaultTreeCellEditor's 'getCellEditorValue' */
    public Object getCellEditorValue()
    {
	Object value = super.getCellEditorValue();
	if (dir == null) {
	    System.out.println("DirectoryTreeCellEditor::getCellEditorValue(): "+
			       "dir is null!");
	    return null;
	}
	Directory parentDir = dir.parentDir();
	String newDirName = parentDir.name();
	if (!newDirName.equals("/")) newDirName+="/";
	newDirName+=value.toString();
	dir.setName(newDirName);
	return dir;
    }
    
    /** TreeCellEditor's 'getTreeCellEditorComponent' */
    public Component getTreeCellEditorComponent(JTree   tree,
						Object  value,
						boolean isSelected,
						boolean expanded,
						boolean leaf,
						int     row)
    {
	if (value instanceof Directory) {
	    dir = (Directory)value;
	    System.out.println("value = "+value.toString()+" IS a Directory!");
	}
	else {
	    System.out.println("value = "+value.toString()+" is NOT a Directory!");
	}
	return super.getTreeCellEditorComponent(tree,
						value,
						isSelected,
						expanded,
						leaf,
						row);
    }
    
}
    

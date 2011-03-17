/**
 * 
 */
package confdb.gui;

import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.tree.DefaultTreeCellEditor.DefaultTextField;

import confdb.data.Directory;
import confdb.data.Configuration;
import confdb.data.ConfigInfo;

import confdb.db.ConfDB;
import confdb.db.DatabaseException;

/**
 * @author raul.jimenez.estupinan@cern.ch
 * bug: Bug77084
 * The main purpose of this file is to provide focus features to GUI by adding a focus listener.
 * E.g. Focus listener allows the Directory Tree to save the current edit by clicking elsewhere
 * instead of having to press Enter.
 * This feature does not come as part of the default JTree implementation.
 * It is a special customization in order to create some particular functionalities. 
 * When the tree gains focus, if the node is being edited, then save the node rename.
 * 
 */
public class TreeDirectoryFocusListener implements FocusListener {
	
    /** the directory tree being serviced */
    private JTree directoryTree = null;

    /** the directory tree-model */
    private DirectoryTreeModel directoryTreeModel = null;
    
    /** the root directory */
    private Directory rootDir = null;
    
    /** the database which is being described */
    private ConfDB database = null;
    
    /** point to the last directory focused */
    private Directory lastdirectory = null;
    
	
	/** constructor */
	public TreeDirectoryFocusListener (JTree directoryTree,ConfDB database) {
		this.directoryTree      = directoryTree;
		this.directoryTreeModel = (DirectoryTreeModel)directoryTree.getModel();
		this.rootDir            = (Directory)directoryTreeModel.getRoot();
		this.database           = database;
	}
	
	/** Perform some operations when the focus goes over an UI component */
	public void focusGained(FocusEvent e) {
		//displayMessage("FocusGained", e);

		if(lastdirectory != null) {
		    Directory parentDir = lastdirectory.parentDir();
		    
		    // The folder is only saved when its dbID is -1.
		    if(lastdirectory.dbId() == -1) {
				try {
				    // Check again if the folder name contains spaces 
				    if(lastdirectory.name().split(" ").length>1) 
				     	lastdirectory.setName(lastdirectory.name().replace(" ", "_"));
				    			    
				    // ddbb Insertion
				    database.insertDirectory(lastdirectory);
					lastdirectory = null; // releasing the pointer for next folder interaction.
				}
				catch (DatabaseException ex) {
				    //ex.printStackTrace(); 					// no need to print stack just return the focus to the edit component.
				    if((e.getOppositeComponent() instanceof DefaultTextField)){
				    	e.getOppositeComponent().requestFocusInWindow();
				    }
				}
		    }
		}
		
		

	}

	/** Perform some operations when the focus has left an UI component */
	public void focusLost(FocusEvent e) {
		//displayMessage("FocusLost", e);
		
		if((e.getComponent() instanceof JTree)) {
			JTree    tree     = (JTree)e.getComponent();
			TreePath treePath = tree.getSelectionPath();
			if (treePath!=null) {
				tree.setSelectionPath(treePath);
				Object o = treePath.getLastPathComponent();
				if (o instanceof Directory) {
				    Directory  selectedDir = (Directory)o;

				    // point to unsaved directory
				    if (selectedDir.dbId() == -1) lastdirectory = selectedDir;
				}
			}
		}
	}
	
	
	/** Displays the Focus Lost/Gain information and also its Opposite components. */
    void displayMessage(String prefix, FocusEvent e) {
    	String message = "";
        message = prefix
                + (e.isTemporary() ? " (temporary):" : ":")
                + e.getComponent().getClass().getName()
                + "; \n Opposite component: "
                + (e.getOppositeComponent() != null ?
                    e.getOppositeComponent().getClass().getName() : "null")
                    + "\n";
        System.out.println("[TreeDirectoryFocusListener.java] " + message);
        
    }
    

}
	

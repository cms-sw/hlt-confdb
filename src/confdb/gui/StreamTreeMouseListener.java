package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import confdb.gui.menu.ScrollableMenu;

import confdb.data.*;


/**
 * StreamTreeMouseListener
 * -----------------------
 * @author Philipp Schieferdecker
 *
 * Listen to mouse events on the stream-tree, create and show popup
 * menus, execute corresponding actions.
 */
public class StreamTreeMouseListener extends    MouseAdapter
                                     implements TreeModelListener,
						ActionListener
{
    /** the tree being manipulated */
    private JTree tree = null;

    /** standard constructor */
    public StreamTreeMouseListener(JTree tree) { this.tree = tree; }

    /** MouseAdapter: mousePressed() */
    public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
    
    /** MouseAdapter: mouseReleased() */
    public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
    
    /** check if this event should really trigger the menu to be displayed */
    private void maybeShowPopup(MouseEvent e)
    {
	StreamTreeModel treeModel = (StreamTreeModel)tree.getModel();
	Configuration   config    = treeModel.getConfiguration();	

	if (!e.isPopupTrigger()) return;
	if (!tree.isEditable()) return;
	if (config.name().length()==0) return;

	TreePath treePath = tree.getPathForLocation(e.getX(),e.getY());
	if (treePath==null) return;
	tree.setSelectionPath(treePath);
	
	JMenuItem  menuItem = null;
	JPopupMenu popup    = new JPopupMenu();
	
	Object selectedNode = treePath.getLastPathComponent();
	
	if (selectedNode==treeModel.getRoot()||selectedNode instanceof Stream) {
	    menuItem = new JMenuItem("Create Stream");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);
	}
	
	if (selectedNode instanceof Stream) {
	    Stream stream = (Stream)selectedNode;
	    
	    menuItem = new JMenuItem("Rename Stream");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);
	    
	    menuItem = new JMenuItem("Remove Stream");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);
	    
	    popup.addSeparator();

	    JMenu menu = new ScrollableMenu("Add Path");
	    menuItem = new JMenuItem("All");
	    menuItem.setActionCommand("ADDALLPATHS");
	    menuItem.addActionListener(this);
	    menu.add(menuItem);
	    Iterator<Path> itP = config.pathIterator();
	    while (itP.hasNext()) {
		Path path = itP.next();
		if (path.isEndPath()) continue;
		menuItem = new JMenuItem(path.name());
		if (stream.indexOfPath(path)<0) {
		    menuItem.setActionCommand("ADDPATH");
		    menuItem.addActionListener(this);
		    menu.add(menuItem);
		}
	    }
	    popup.add(menu);
	}
	
	if (selectedNode instanceof Path) {
	    menuItem = new JMenuItem("Remove Path");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);
	}
	
	popup.show(tree,e.getX(),e.getY());
    }
    

    /** TreeModelListener: treeNodesChanged() */
    public void treeNodesChanged(TreeModelEvent e)
    {
	StreamTreeModel treeModel = (StreamTreeModel)tree.getModel();
	TreePath        treePath  = e.getTreePath();
	Configuration   config    = treeModel.getConfiguration();
	
	if (treePath==null || config==null) return;

	Object  changedNode = e.getChildren()[0];
	boolean valid = true;
	
	if (changedNode instanceof Stream) {
	    valid = false;
	    Stream stream = (Stream)changedNode;
	    String label  = stream.label();
	    if (!label.equals("<ENTER UNIQUE LABEL>")) valid = true;
	}
	
	if (!valid) {
	    tree.setSelectionPath(treePath.pathByAddingChild(changedNode));
	    StreamTreeActions.editNodeName(tree);
	}
    }
    
    /** TreeModelListener: treeNodesInserted() */
    public void treeNodesInserted(TreeModelEvent e) {}
    
    /** TreeModelListener: treeNodesRemoved() */
    public void treeNodesRemoved(TreeModelEvent e) {}

    /** TreeModelListener: treeStructureChanged() */
    public void treeStructureChanged(TreeModelEvent e) {}
    
    /** ActionListener: actionPerformed */
    public void actionPerformed(ActionEvent e)
    {
	JMenuItem src    = (JMenuItem)e.getSource();
	String    cmd    = src.getText();
	String    action = src.getActionCommand();
	
	if (cmd.equals("Create Stream")) {
	    StreamTreeActions.insertStream(tree);
	}
	else if (cmd.equals("Rename Stream")) {
	    StreamTreeActions.editNodeName(tree);
	}
	else if (cmd.equals("Remove Stream")) {
	    StreamTreeActions.removeStream(tree);
	}
	else if (cmd.equals("Remove Path")) {
	    StreamTreeActions.removePath(tree);
	}
	else if (action.equals("ADDPATH")) {
	    StreamTreeActions.addPath(tree,cmd);
	}
	else if (action.equals("ADDALLPATHS")) {
	    StreamTreeActions.addAllPaths(tree);
	}
    }
    
}

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
 * PrimaryDatasetTreeMouseListener
 * -------------------------------
 * @author Philipp Schieferdecker
 *
 * Listen to mouse events on the primary dataset tree, create and show
 * popup menus, execute corresponding actions.
 */
public class PrimaryDatasetTreeMouseListener extends    MouseAdapter
                                             implements TreeModelListener,
	                                                ActionListener
{
    /** the tree being manipulated */
    private JTree tree = null;
    
    /** standard constructor */
    public PrimaryDatasetTreeMouseListener(JTree tree) { this.tree = tree; }
    
    /** MouseAdapter: mousePressed() */
    public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
    
    /** MouseAdapter: mouseReleased() */
    public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
    
    /** check if this event should really trigger the menu to be displayed */
    private void maybeShowPopup(MouseEvent e)
    {
	PrimaryDatasetTreeModel treeModel=(PrimaryDatasetTreeModel)tree.getModel();
	Configuration config=treeModel.getConfiguration();	

	if (!e.isPopupTrigger()) return;
	if (!tree.isEditable()) return;
	if (config.name().length()==0) return;

	TreePath treePath=tree.getPathForLocation(e.getX(),e.getY());
	if (treePath==null) return;
	tree.setSelectionPath(treePath);
	
	JMenuItem  menuItem = null;
	JPopupMenu popup    = new JPopupMenu();
	
	Object selectedNode = treePath.getLastPathComponent();
	
	if (selectedNode instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)selectedNode;
	    
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
		if (dataset.indexOfPath(path)<0) {
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
    public void treeNodesChanged(TreeModelEvent e) {}

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
	
	if (cmd.equals("Remove Path")) {
	    PrimaryDatasetTreeActions.removePath(tree);
	}
	else if (action.equals("ADDPATH")) {
	    PrimaryDatasetTreeActions.addPath(tree,cmd);
	}
	else if (action.equals("ADDALLPATHS")) {
	    PrimaryDatasetTreeActions.addAllPaths(tree);
	}
    }
    
}

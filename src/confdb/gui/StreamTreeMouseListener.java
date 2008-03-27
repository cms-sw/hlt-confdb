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
import confdb.db.*;


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

    /** database instance, for list of valid stream labels */
    private ConfDB database = null;

    /** standard constructor */
    public StreamTreeMouseListener(JTree tree,ConfDB database)
    {
	this.tree = tree;
	this.database = database;
    }

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
	if (config.name().length()==0) return;

	TreePath treePath = tree.getPathForLocation(e.getX(),e.getY());
	if (treePath==null) return;
	tree.setSelectionPath(treePath);
	
	JMenuItem  menuItem = null;
	JPopupMenu popup    = new JPopupMenu();
	
	Object selectedNode = treePath.getLastPathComponent();
	
	if (selectedNode==treeModel.getRoot()) {
	    JMenu menu = new ScrollableMenu("Add Stream");
	    try {
		Iterator<String> itS = database.streamLabelIterator();
		while (itS.hasNext()) {
		    String streamLabel = itS.next();
		    if (config.stream(streamLabel)==null) {
			menuItem = new JMenuItem(streamLabel);
			menuItem.setActionCommand("ADDSTREAM");
			menuItem.addActionListener(this);
			menu.add(menuItem);
		    }
		}
		popup.add(menu);
	    }
	    catch (DatabaseException ex) {
		System.err.println(ex.getMessage());
	    }
	}
	else if (selectedNode instanceof Stream) {
	    Stream stream = (Stream)selectedNode;
	    
	    JMenu menu = new ScrollableMenu("Add Primary Dataset");
	    Iterator<PrimaryDataset> itD = config.datasetIterator();
	    while (itD.hasNext()) {
		PrimaryDataset dataset = itD.next();
		if (stream.indexOfDataset(dataset)<0&&
		    dataset.parentStream()==null) {
		    menuItem = new JMenuItem(dataset.label());
		    menuItem.setActionCommand("ADDDATASET");
		    menuItem.addActionListener(this);
		    menu.add(menuItem);
		}
	    }
	    popup.add(menu);
	    
	    menuItem = new JMenuItem("Remove Stream");
	    menuItem.setActionCommand("RMVSTREAM");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);
	}
	else if (selectedNode instanceof PrimaryDataset) {
	    menuItem = new JMenuItem("Remove Primary Dataset");
	    menuItem.setActionCommand("RMVDATASET");
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
	
	if (action.equals("ADDSTREAM")) {
	    StreamTreeActions.addStream(tree,cmd);
	}
	if (action.equals("RMVSTREAM")) {
	    StreamTreeActions.removeStream(tree);
	}
	else if (action.equals("ADDDATASET")) {
	    StreamTreeActions.addDataset(tree,cmd);
	}
	else if (action.equals("RMVDATASET")) {
	    StreamTreeActions.removeDataset(tree);
	}
    }
    
}

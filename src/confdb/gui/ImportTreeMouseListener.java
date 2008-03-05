package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import confdb.data.*;

/**
 * ImportTreeMouseListener
 * -----------------------
 * @author Philipp Schieferdecker
 *
 */
public class ImportTreeMouseListener extends MouseAdapter
{
    //
    // member data
    //
    
    /** reference to import tree */
    private JTree importTree;
    
    /** reference to current tree */
    private JTree currentTree;

    
    //
    // constructor
    //
    
    /** standard constructor */
    public ImportTreeMouseListener(JTree importTree,JTree currentTree)
    {
	this.importTree  = importTree;
	this.currentTree = currentTree;
    }
    
    
    //
    // member functions
    //

    /** MouseAdapter: mousePressed() */
    public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
    
    /** MouseAdapter: mouseReleased() */
    public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
    
    /** check if this event should really trigger the menu to be displayed */
    private void maybeShowPopup(MouseEvent e)
    {
	if (!e.isPopupTrigger()) return;
	
	ConfigurationTreeModel tm  = (ConfigurationTreeModel)importTree.getModel();
	IConfiguration         cfg = (IConfiguration)tm.getRoot();
	TreePath               tp  = importTree.getPathForLocation(e.getX(),
								   e.getY());
	if(tp==null) return;
	
	importTree.setSelectionPath(tp);
	currentTree.setSelectionPath(null);
	
	Object node = tp.getLastPathComponent();
	
	if (node instanceof ReferenceContainer) {
	    ReferenceContainer container = (ReferenceContainer)node;
	    JPopupMenu popup = new JPopupMenu();
	    JMenuItem  item = new JMenuItem("Import " + container.name());
	    item.addActionListener(new AddContainerListener(currentTree,container));
	    popup.add(item);
	    popup.show(e.getComponent(),e.getX(),e.getY());
	}
    }
    
}


// listener class
class AddContainerListener implements ActionListener
{
    // member data
    private JTree              targetTree;
    private ReferenceContainer container;

    // construction
    public AddContainerListener(JTree targetTree,
				 ReferenceContainer container)
    {
	this.targetTree = targetTree;
	this.container  = container;
    }

    // member functions
    public void actionPerformed(ActionEvent e)
    {
	ConfigurationTreeActions.importReferenceContainer(targetTree,container);
    }
}

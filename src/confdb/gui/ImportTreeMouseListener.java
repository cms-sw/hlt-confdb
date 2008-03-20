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
	Object node = tp.getLastPathComponent();
	
	if (node instanceof ReferenceContainer) {
	    currentTree.setSelectionPath(null);
	    ReferenceContainer container = (ReferenceContainer)node;
	    JPopupMenu popup = new JPopupMenu();
	    JMenuItem  item = new JMenuItem("Import " + container.name());
	    item.addActionListener(new AddContainerListener(currentTree,container));
	    popup.add(item);
	    popup.show(e.getComponent(),e.getX(),e.getY());
	}
	else if (node instanceof ModuleInstance) {
	    ModuleInstance module = (ModuleInstance)node;
	    JPopupMenu popup = new JPopupMenu();
	    JMenuItem  item  = new JMenuItem("Import " + module.name());
	    item.addActionListener(new AddModuleListener(currentTree,module));
	    popup.add(item);
	    popup.show(e.getComponent(),e.getX(),e.getY());
	}
	else if (node instanceof Instance) {
	    Instance instance = (Instance)node;
	    JPopupMenu popup = new JPopupMenu();
	    JMenuItem  item  = new JMenuItem("Import " + instance.name());
	    item.addActionListener(new AddInstanceListener(currentTree,instance));
	    popup.add(item);
	    popup.show(e.getComponent(),e.getX(),e.getY());
	}
    }
    
}


// container listener class
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

// module listener class
class AddModuleListener implements ActionListener
{
    // member data
    private JTree          targetTree;
    private ModuleInstance module;

    // construction
    public AddModuleListener(JTree targetTree,ModuleInstance module)
    {
	this.targetTree = targetTree;
	this.module     = module;
    }

    // member functions
    public void actionPerformed(ActionEvent e)
    {
	ConfigurationTreeActions.importModule(targetTree,module);
    }
}

// instance listener class
class AddInstanceListener implements ActionListener
{
    // member data
    private JTree    targetTree;
    private Instance instance;

    // construction
    public AddInstanceListener(JTree targetTree,Instance instance)
    {
	this.targetTree = targetTree;
	this.instance   = instance;
    }
    
    // member functions
    public void actionPerformed(ActionEvent e)
    {
	ConfigurationTreeActions.importInstance(targetTree,instance);
    }
}

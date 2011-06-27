package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;


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
	
	// Import all container's items.
	if(node instanceof StringBuffer) {
		if(tm.getChildCount(node) != 0) {
			Object child = tm.getChild(node, 0);
			if (child instanceof ReferenceContainer) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem  item = null;
				if 		(child instanceof Path)		item = new JMenuItem("Import all Paths")	;
				else if (child instanceof Sequence)	item = new JMenuItem("Import all Sequences");
				
			    item.addActionListener(new AddAllReferencesListener(currentTree, importTree, node));
			    popup.add(item);
			    popup.show(e.getComponent(),e.getX(),e.getY());
			 } else if (child instanceof Instance) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem	item = null;
				if 		(child instanceof ServiceInstance) 	item = new JMenuItem("Import all Services")	;	
				else if (child instanceof ESModuleInstance)	item = new JMenuItem("Import all ESModules");
				else if (child instanceof ESSourceInstance)	item = new JMenuItem("Import all ESSources");
				else if (child instanceof EDSourceInstance)	item = new JMenuItem("Import all EDSources");
				else if (child instanceof ModuleInstance)	item = new JMenuItem("Update all existing Modules")	;
				if(item != null) {
					// Adding listeners:
					if(child instanceof ModuleInstance) 
						item.addActionListener(new AddUpdateAllModulesListener(currentTree, importTree, node));
					else 
						item.addActionListener(new AddAllInstancesListener(currentTree, importTree, node));
					
				    popup.add(item);
				    popup.show(e.getComponent(),e.getX(),e.getY());					
				}
			} else if(child instanceof PSetParameter) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem	item = null;
				item = new JMenuItem("Import All PSets")	;
			    item.addActionListener(new AddAllPSetsListener(currentTree, importTree, node));
			    popup.add(item);
			    popup.show(e.getComponent(),e.getX(),e.getY());
			}
		}
	}
	
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
	else if (node instanceof EventContent) {
	    EventContent content = (EventContent)node;
	    JPopupMenu popup = new JPopupMenu();
	    JMenuItem  item = new JMenuItem("Import " + content.name());
	    item.addActionListener(new AddContentListener(currentTree,content));
	    popup.add(item);
	    popup.show(e.getComponent(),e.getX(),e.getY());
	}
	else if (node instanceof Stream) {
	    Stream     stream = (Stream)node;
	    JPopupMenu popup  = new JPopupMenu();
	    JMenuItem  item   = new JMenuItem("Import " + stream.name());
	    item.addActionListener(new AddStreamListener(currentTree,stream));
	    popup.add(item); 
	    popup.show(e.getComponent(),e.getX(),e.getY());
	} 
	else if (node instanceof PrimaryDataset) {
	    PrimaryDataset dataset = (PrimaryDataset)node;
	    JPopupMenu     popup   = new JPopupMenu();
	    JMenuItem      item    = new JMenuItem("Import " + dataset.name());
	    item.addActionListener(new AddDatasetListener(currentTree,dataset));
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


//All Paths / sequences listener class
class AddAllReferencesListener  implements ActionListener {
	private JTree	targetTree	;
	private JTree	sourceTree	;
	private Object 	container	;
	
	public AddAllReferencesListener(JTree targetTree, JTree sourceTree, Object parentPathNode) {
		this.targetTree = targetTree		;
		this.sourceTree = sourceTree		;
		this.container	= parentPathNode	;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		// execute an action.
		ConfigurationTreeActions.importAllReferenceContainers(targetTree, sourceTree, container);
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

// Import all instances listener class
class AddAllInstancesListener implements ActionListener {
	// member data
	private JTree		targetTree;
	private JTree		sourceTree;
	private Object		container;
	
	// constructor
	public AddAllInstancesListener(JTree targetTree, JTree sourceTree, Object container) {
		this.targetTree	= targetTree;
		this.sourceTree	= sourceTree;
		this.container	= container;
	}
	
	// member functions
	public void actionPerformed(ActionEvent e) {
		ConfigurationTreeActions.ImportAllInstances(targetTree, sourceTree, container);
	}
}


//Update shared Modules listener class
class AddUpdateAllModulesListener implements ActionListener {
	// member data
	private JTree		targetTree;
	private JTree		sourceTree;
	private Object		container;
	
	// constructor
	public AddUpdateAllModulesListener(JTree targetTree, JTree sourceTree, Object container) {
		this.targetTree	= targetTree;
		this.sourceTree	= sourceTree;
		this.container	= container;
	}
	
	// member functions
	public void actionPerformed(ActionEvent e) {
		//ConfigurationTreeActions.ImportAllESModules(targetTree, sourceTree, container);
		ConfigurationTreeActions.UpdateAllModules(targetTree, sourceTree, container);
	}
}


//Import all instances listener class
class AddAllPSetsListener implements ActionListener {
	// member data
	private JTree		targetTree;
	private JTree		sourceTree;
	private Object		container;
	
	// constructor
	public AddAllPSetsListener(JTree targetTree, JTree sourceTree, Object container) {
		this.targetTree	= targetTree;
		this.sourceTree	= sourceTree;
		this.container	= container;
	}
	
	// member functions
	public void actionPerformed(ActionEvent e) {
		ConfigurationTreeActions.ImportAllPSets(targetTree, sourceTree, container);
	}
}


// event content listener class
class AddContentListener implements ActionListener
{
    // member data
    private JTree        targetTree;
    private EventContent content;

    // construction
    public AddContentListener(JTree targetTree,EventContent content)
    {
	this.targetTree = targetTree;
	this.content    = content;
    }
    
    // member functions
    public void actionPerformed(ActionEvent e)
    {
	ConfigurationTreeActions.importContent(targetTree,content);
    }
}


// event stream listener class
class AddStreamListener implements ActionListener
{
    // member data
    private JTree  targetTree;
    private Stream stream;

    // construction
    public AddStreamListener(JTree targetTree,Stream stream)
    {
	this.targetTree = targetTree;
	this.stream     = stream;
    }
    
    // member functions
    public void actionPerformed(ActionEvent e)
    {
	ConfigurationTreeActions.importStream(targetTree,"",stream);
    }
}


// event dataset listener class
class AddDatasetListener implements ActionListener
{
    // member data
    private JTree          targetTree;
    private PrimaryDataset dataset;

    // construction
    public AddDatasetListener(JTree targetTree,PrimaryDataset dataset)
    {
	this.targetTree = targetTree;
	this.dataset    = dataset;
    }
    
    // member functions
    public void actionPerformed(ActionEvent e)
    {
	ConfigurationTreeActions.importPrimaryDataset(targetTree,"",dataset);
    }
}

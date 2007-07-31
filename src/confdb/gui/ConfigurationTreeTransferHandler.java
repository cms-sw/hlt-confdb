package confdb.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.tree.*;

import confdb.data.*;


/**
 * ConfigurationTreeTransferHandler
 * --------------------------------
 * @author Philipp Schieferdecker
 *
 * <p>Transfer handler implementation that supports to move a selected
 * tree node within a <code>JTree</code>.</p>
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * @version 1, 03.08.2005
 */
public class ConfigurationTreeTransferHandler extends TransferHandler
{
    //
    // member data
    //
    
    /** source JTree */
    private static JTree sourceTree = null;
    
    /** node being dragged */
    private static Object sourceNode = null;

    /** drag image */
    private static BufferedImage dragImage = null;

    /** target JTree */
    private JTree targetTree = null;

    /** target software release */
    private SoftwareRelease targetRelease = null;
    
    
    //
    // construction
    //
    
    /**standard constructor */
    public ConfigurationTreeTransferHandler(JTree targetTree,
					    SoftwareRelease targetRelease)
    {
	super();
	this.targetTree = targetTree;
	this.targetRelease = targetRelease;
    }
    
    
    //
    // member functions
    //

    /** get a drag image from the currently dragged node (if any) */
    public static BufferedImage getDragImage() { return dragImage; }

    /** get the dragged source node */
    public static Object getSourceNode() { return sourceNode; }

    /**create transferable which contains all paths that are currently selected */
    protected Transferable createTransferable(JComponent c)
    {
  	Transferable t = null;
	if(c instanceof JTree) {
	    sourceTree  = (JTree)c;
	    sourceNode  = sourceTree.getSelectionPath().getLastPathComponent();
	    if (sourceNode instanceof ReferenceContainer ||
		sourceNode instanceof Reference ||
		sourceNode instanceof Instance ||
		sourceNode instanceof Parameter) {
		ConfigurationTreeTransferHandler.setDragImage();
		t = new GenericTransferable(sourceNode);
		//System.out.println("sourceNode = " + sourceNode);
	    }
	}
	return t;
    }

    /** wether a certain data flavor can be imported or not */
    public boolean canImport(JComponent comp,DataFlavor[] transferFlavors)
    {
	return true;
    }
    
    /** import data which is being dragged */
    public boolean importData(JComponent comp,Transferable t)
    {
	if (sourceNode==null) return false;
	if (!(comp instanceof JTree)) return false;
	if (!((JTree)comp).isEditable()) return false;

	TreePath               targetPath  = targetTree.getSelectionPath();
	Object                 targetNode  = targetPath.getLastPathComponent();
	ConfigurationTreeModel targetModel =
	    (ConfigurationTreeModel)targetTree.getModel();
	Configuration          targetConfig= (Configuration)targetModel.getRoot();
	
	
	//
	// IMPORT & EDIT
	//
	
	// import parameter into a PSet
	if (sourceNode instanceof Parameter &&
	    targetNode instanceof PSetParameter) {
	    Parameter     p    = (Parameter)((Parameter)sourceNode).clone(null);
	    PSetParameter pset = (PSetParameter)targetNode;
	    pset.addParameter(p);
	    targetModel.nodeInserted(pset,pset.parameterCount()-1);
	    targetConfig.setHasChanged(true);
	    targetModel.updateLevel1Nodes();
	    return true;
	}
	
	// insert a PSet into a VPSet
	if (sourceNode instanceof PSetParameter &&
	    targetNode instanceof VPSetParameter) {
	    PSetParameter  pset  =
		(PSetParameter)((Parameter)sourceNode).clone(null);
	    VPSetParameter vpset = (VPSetParameter)targetNode;
	    vpset.addParameterSet(pset);
	    targetModel.nodeInserted(vpset,vpset.parameterSetCount()-1);
	    targetConfig.setHasChanged(true);
	    targetModel.updateLevel1Nodes();
	    return true;
	}
	
	
	//
	// IMPORT
	//
	if (sourceTree != targetTree) {
	    // insert global PSet
	    if (sourceNode instanceof PSetParameter &&
		targetNode == targetModel.psetsNode()) {
		PSetParameter pset =
		    (PSetParameter)((Parameter)sourceNode).clone(null);
		targetConfig.insertPSet(pset);
		targetModel.nodeInserted(targetModel.psetsNode(),
					 targetConfig.psetCount()-1);
		targetModel.updateLevel1Nodes();
		return true;
	    }

	    // insert EDSource
	    if (sourceNode instanceof EDSourceInstance &&
		targetNode == targetModel.edsourcesNode()) {
		EDSourceInstance source = (EDSourceInstance)sourceNode;
		if (!targetConfig.isUniqueQualifier(source.name())) return false;
		if (targetConfig.edsourceCount()>0) return false;
		EDSourceInstance target =
		    targetConfig.insertEDSource(source.template().name());
		for (int i=0;i<target.parameterCount();i++)
		    target.updateParameter(i,source.parameter(i).valueAsString());
		target.setDatabaseId(source.databaseId());
		targetModel.nodeInserted(targetModel.edsourcesNode(),
					 targetConfig.edsourceCount()-1);
	    	targetModel.updateLevel1Nodes();
		return true;
	    }
	    
	    // insert ESSource
	    if (sourceNode instanceof ESSourceInstance &&
		targetNode == targetModel.essourcesNode()) {
		ESSourceInstance source = (ESSourceInstance)sourceNode;
		if (!targetConfig.isUniqueQualifier(source.name())) return false;
		ESSourceInstance target =
		    targetConfig.insertESSource(targetConfig.essourceCount(),
						source.template().name(),
						source.name());
		for (int i=0;i<target.parameterCount();i++)
		    target.updateParameter(i,source.parameter(i).valueAsString());
		target.setDatabaseId(source.databaseId());
		targetModel.nodeInserted(targetModel.essourcesNode(),
					 targetConfig.essourceCount()-1);
	    	targetModel.updateLevel1Nodes();
		return true;
	    }
	    
	    // insert ESModule
	    if (sourceNode instanceof ESModuleInstance &&
		targetNode == targetModel.esmodulesNode()) {
		ESModuleInstance source = (ESModuleInstance)sourceNode;
		if (!targetConfig.isUniqueQualifier(source.name())) return false;
		ESModuleInstance target =
		    targetConfig.insertESModule(targetConfig.esmoduleCount(),
						source.template().name(),
						source.name());
		for (int i=0;i<target.parameterCount();i++)
		    target.updateParameter(i,source.parameter(i).valueAsString());
		target.setDatabaseId(source.databaseId());
		targetModel.nodeInserted(targetModel.esmodulesNode(),
					 targetConfig.esmoduleCount()-1);
	    	targetModel.updateLevel1Nodes();
		return true;
	    }
	    
	    // insert Service
	    if (sourceNode instanceof ServiceInstance &&
		targetNode == targetModel.servicesNode()) {
		ServiceInstance source = (ServiceInstance)sourceNode;
		if (!targetConfig.isUniqueQualifier(source.name())) return false;
		ServiceInstance target =
		    targetConfig.insertService(targetConfig.serviceCount(),
					       source.template().name());
		for (int i=0;i<target.parameterCount();i++)
		    target.updateParameter(i,source.parameter(i).valueAsString());
		target.setDatabaseId(source.databaseId());
		targetModel.nodeInserted(targetModel.servicesNode(),
					 targetConfig.serviceCount()-1);
		targetModel.updateLevel1Nodes();
		return true;
	    }

	    // if a reference is dragged, consider the parent referancable
	    if (sourceNode instanceof Reference) {
		Reference r = (Reference)sourceNode;
		sourceNode  = r.parent();
	    }
	    
	    // insert Module
	    if (sourceNode instanceof ModuleInstance) {
		ModuleInstance source = (ModuleInstance)sourceNode;
		
		if (!targetConfig.isUniqueQualifier(source.name())) return false;
		
		ReferenceContainer parent        = null;
		ModuleInstance     target        = null;
		int                insertAtIndex = 0;

		if (targetNode instanceof ReferenceContainer) {
		    parent = (ReferenceContainer)targetNode;
		    ModuleReference reference =
			targetConfig.insertModuleReference(parent,0,
							   source.template().name(),
							   source.name());
		    target = (ModuleInstance)reference.parent();
		}
		else if (targetNode instanceof Reference) {
		    Reference selectedRef = (Reference)targetNode;
		    parent = selectedRef.container();
		    insertAtIndex = parent.indexOfEntry(selectedRef) + 1;
		    ModuleReference reference =
			targetConfig.insertModuleReference(parent,insertAtIndex,
							   source.template().name(),
							   source.name());
		    target = (ModuleInstance)reference.parent();
		}
		
		if (target != null) {
		    for (int i=0;i<target.parameterCount();i++)
			target.updateParameter(i,source.parameter(i).valueAsString());
		    target.setDatabaseId(source.databaseId());
		    targetModel.nodeInserted(parent,insertAtIndex);
		    targetModel.nodeInserted(targetModel.modulesNode(),
					     targetConfig.moduleCount()-1);
		    targetModel.updateLevel1Nodes();
		    return true;
		}
	    }
	    
	    // set those, as the entries of paths/sequences are imported the same way
	    ReferenceContainer sourceContainer = null;
	    ReferenceContainer targetContainer = null;
	    
	    // insert Path
	    if (sourceNode instanceof Path) {
		Path source = (Path)sourceNode;
		
		if (!targetConfig.hasUniqueQualifier(source)) return false;
		if (!targetConfig.hasUniqueEntries(source)) return false;
		
		Path target = null;
		int  insertAtIndex = 0;
		
		if (targetNode == targetModel.pathsNode()) {
		    target = targetConfig.insertPath(0,source.name());
		}
		else if (targetNode instanceof Path) {
		    Path p = (Path)targetNode;
		    insertAtIndex = targetConfig.indexOfPath(p) + 1;
		    target = targetConfig.insertPath(insertAtIndex,source.name());
		}

		if (target != null) {
		    targetModel.nodeInserted(targetModel.pathsNode(),insertAtIndex);
		    sourceContainer = source;
		    targetContainer = target;
		}
	    }

	    // insert Sequences
	    if (sourceNode instanceof Sequence) {
		Sequence source = (Sequence)sourceNode;
		
		if (!targetConfig.hasUniqueQualifier(source)) return false;
		if (!targetConfig.hasUniqueEntries(source)) return false;
		
		Sequence target = null;
		int      insertAtIndex = 0;
		
		if (targetNode == targetModel.sequencesNode()) {
		    target = targetConfig.insertSequence(0,source.name());
		}
		else if (targetNode instanceof Sequence) {
		    Sequence s = (Sequence)targetNode;
		    insertAtIndex = targetConfig.indexOfSequence(s) + 1;
		    target = targetConfig.insertSequence(insertAtIndex,source.name());
		}
		
		if (target != null) {
		    targetModel.nodeInserted(targetModel.sequencesNode(),
					     insertAtIndex);
		    sourceContainer = source;
		    targetContainer = target;
		}
	    }
	    
	    // entries of reference container, if any
	    if (sourceContainer != null && targetContainer != null) {
		insertContainerEntries(targetConfig,targetModel,
				       sourceContainer,targetContainer);
		targetContainer.setDatabaseId(sourceContainer.databaseId());
		targetModel.updateLevel1Nodes();
		return true;
	    }
	}
	
	return false;
    }
    
    /** insert entries of an external reference container into the local copy */
    private void insertContainerEntries(Configuration          config,
					ConfigurationTreeModel treeModel,
					ReferenceContainer     sourceContainer,
					ReferenceContainer     targetContainer)
    {
	for (int i=0;i<sourceContainer.entryCount();i++) {
	    Reference entry = sourceContainer.entry(i);
	    
	    if (entry instanceof ModuleReference) {
		ModuleReference sourceRef = (ModuleReference)entry;
		ModuleInstance  source    = (ModuleInstance)sourceRef.parent();
		ModuleReference targetRef =
		    config.insertModuleReference(targetContainer,i,
						 source.template().name(),
						 source.name());
		ModuleInstance  target = (ModuleInstance)targetRef.parent();
		for (int j=0;j<target.parameterCount();j++)
		    target.updateParameter(j,source.parameter(j).valueAsString());
		treeModel.nodeInserted(targetContainer,i);
		treeModel.nodeInserted(treeModel.modulesNode(),
				       config.moduleCount()-1);
		target.setDatabaseId(source.databaseId());
	    }
	    else if (entry instanceof PathReference) {
		PathReference sourceRef = (PathReference)entry;
		Path          source    = (Path)sourceRef.parent();
		Path          target    = config.insertPath(config.pathCount(),
							    sourceRef.name());
		PathReference targetRef = config.insertPathReference(targetContainer,
								     i,target);
		treeModel.nodeInserted(targetContainer,i);
		treeModel.nodeInserted(treeModel.pathsNode(),
				       config.pathCount()-1);
		insertContainerEntries(config,treeModel,source,target);
		target.setDatabaseId(source.databaseId());
	    }
	    else if (entry instanceof SequenceReference) {
		SequenceReference sourceRef = (SequenceReference)entry;
		Sequence          source    = (Sequence)sourceRef.parent();
		Sequence          target    = config.insertSequence(config
								    .sequenceCount(),
								    sourceRef.name());
		SequenceReference targetRef =
		    config.insertSequenceReference(targetContainer,i,target);
		treeModel.nodeInserted(targetContainer,i);
		treeModel.nodeInserted(treeModel.sequencesNode(),
				       config.sequenceCount()-1);
		insertContainerEntries(config,treeModel,source,target);
		target.setDatabaseId(source.databaseId());
	    }
	}
    }
    
    /** move selected paths when export of drag is done */
    protected void exportDone(JComponent source,Transferable data,int action)
    {
	//ConfigurationTreeModel model =
	//    (ConfigurationTreeModel)sourceTree.getModel();
	//System.out.println("export from "+model.getRoot().toString()+" done.");
	super.exportDone(source,data,action);
    }
    
    
    /** Returns the type of transfer actions supported by the source */
    public int getSourceActions(JComponent c) { return COPY_OR_MOVE; }
    
    /** set the current drag image */
    public static void setDragImage()
    {
	JTree tree = sourceTree;
	try {
	    TreePath         dragPath   = tree.getSelectionPath();
	    Rectangle        pathBounds = tree.getPathBounds(dragPath);
	    TreeCellRenderer r          = tree.getCellRenderer();
	    TreeModel        m          = (TreeModel)tree.getModel();
	    boolean          nIsLeaf    = m.isLeaf(dragPath
						   .getLastPathComponent());
	    JComponent lbl =
		(JComponent)r.getTreeCellRendererComponent(tree,
							   sourceNode,
							   false, 
							   tree
							   .isExpanded(dragPath),
							   nIsLeaf,0,false);
	    lbl.setBounds(pathBounds);
	    dragImage = new BufferedImage(lbl.getWidth(),
					  lbl.getHeight(), 
					  java.awt.image
					  .BufferedImage.TYPE_INT_ARGB_PRE);
	    Graphics2D graphics = dragImage.createGraphics();
	    graphics.setComposite(AlphaComposite
				  .getInstance(AlphaComposite.SRC_OVER, 0.5f));
	    lbl.setOpaque(false);
	    lbl.paint(graphics);
	    graphics.dispose();
	}
	catch (RuntimeException re) {
	    dragImage = null;
	}
    }
    
}

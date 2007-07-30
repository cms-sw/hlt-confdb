package confdb.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.tree.*;


/**
 * ConfigurationTreeTransferHandler.java
 * -------------------------------------
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

    
    //
    // construction
    //
    
    /**standard constructor */
    public ConfigurationTreeTransferHandler(JTree targetTree)
    {
	super();
	this.targetTree = targetTree;
    }
    
    
    //
    // member functions
    //
    
    /**create transferable which contains all paths that are currently selected */
    protected Transferable createTransferable(JComponent c)
    {
  	Transferable t = null;
	if(c instanceof JTree) {
	    ConfigurationTreeTransferHandler.sourceTree  = (JTree)c;
	    ConfigurationTreeTransferHandler.sourceNode  =
		sourceTree.getSelectionPath().getLastPathComponent();
	    ConfigurationTreeTransferHandler.setDragImage();
	    t = new GenericTransferable(sourceNode);
	    //System.out.println("sourceNode = " + sourceNode);
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
	System.out.println("importData");
	if (null==sourceNode) return false;
	
	if (comp instanceof JTree) {
	    JTree tree = (JTree)comp;
	    ConfigurationTreeModel model = (ConfigurationTreeModel)tree.getModel();
	    
	    System.out.println("source configuration: " + model.getRoot().toString());
	    
	    TreePath targetPath = targetTree.getSelectionPath();
	    Object   targetNode = targetPath.getLastPathComponent();
	    
	    System.out.println("sourceNode: "+sourceNode.toString());
	    System.out.println("targetNode: "+targetNode.toString());
	    
	    return true;
	}

	return false;
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
    
    /** get a drag image from the currently dragged node (if any) */
    public static BufferedImage getDragImage() { return dragImage; }

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

package confdb.gui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.tree.*;

import confdb.data.*;


/**
 * ConfigurationTreeDropTarget
 * ---------------------------
 * @author Philipp Schieferdecker
 *
 * <p>A drop target for class <code>JTree</code> that implements
 * autoscrolling, automatic tree node expansion and a custom drag
 * image during the drag part of a drag and drop operation.</p>
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file  in the distribution
 *      package of this software
 *
 * @version 1, 03.08.2005
 */
public class ConfigurationTreeDropTarget extends DropTarget
{
    //
    // member data
    //
    
    /** bounding rectangle of last row a dragOver was recorded for */
    private Rectangle lastRowBounds;

    /** height of gap between any two node rows to treat as area for inserts */
    private int insertAreaHeight = 8;
  
    /** insets for autoscroll */
    private Insets autoscrollInsets = new Insets(20, 20, 20, 20);
  
    /** rectangle to clear (where the last image was drawn) */
    private Rectangle rect2D = new Rectangle();
  
    private Point mostRecentLocation;


    //
    // construction
    //

    /** standard constructor */
    public ConfigurationTreeDropTarget()
    {
	super();
    }
    

    //
    // member functions
    //
    
    /**constantly update drag mark and drag image as, plus auto-scrolling */
    public void dragOver(DropTargetDragEvent dtde)
    {
	JTree tree = (JTree)dtde.getDropTargetContext().getComponent();
	Point loc = dtde.getLocation();
	updateDragMark(tree,loc);
	paintImage(tree,loc);
	autoscroll(tree,loc);
	
	TreePath selectedPath = tree.getSelectionPath();
	Object   selectedNode =
	    (selectedPath==null) ? null : selectedPath.getLastPathComponent();
	Object   sourceNode   = ConfigurationTreeTransferHandler.getSourceNode();
	if (selectedNode!=null&&
	    acceptAsDropTarget(tree,sourceNode,selectedNode)) {
	    dtde.acceptDrag(dtde.getDropAction());
	}
	else {
	    dtde.rejectDrag();
	}
	   
	super.dragOver(dtde);
    }

    /**clear the drawings on exit */
    public void dragExit(DropTargetEvent dtde)
    {
	clearImage((JTree)dtde.getDropTargetContext().getComponent());
	super.dragExit(dtde);
    }
    
    /**clear the drawings on drop */
    public void drop(DropTargetDropEvent dtde)
    {
	JTree targetTree=(JTree)dtde.getDropTargetContext().getComponent();
	ConfigurationTreeTransferHandler dndHandler=
	    (ConfigurationTreeTransferHandler)targetTree.getTransferHandler();
	
	TreePath targetPath = targetTree.getSelectionPath();
	Object   targetNode = targetPath.getLastPathComponent();
	Object   sourceNode = ConfigurationTreeTransferHandler.getSourceNode();
	
	if (acceptAsDropTarget(targetTree,sourceNode,targetNode)) {
	    dtde.acceptDrop(dtde.getDropAction());
	    
	    try {
		Transferable t = dtde.getTransferable();
		dtde.dropComplete(dndHandler.importData(targetTree,t));
	    }
	    catch (RuntimeException re) {
		dtde.dropComplete(false);
	    }
	}
	else {
	    dtde.rejectDrop();
	}
	
	clearImage(targetTree);
	super.drop(dtde);
    }
    
    /** paint the dragged node  */
    private final void paintImage(JTree tree, Point pt)
    {
	BufferedImage image = ConfigurationTreeTransferHandler.getDragImage();
  	if(image != null) {
	    tree.paintImmediately(rect2D.getBounds());
	    rect2D.setRect((int)pt.getX()-15,(int)pt.getY()-15,
			   image.getWidth(),image.getHeight());
	    tree.getGraphics().drawImage(image,(int)pt.getX()-15,(int)pt.getY()-15,
					 tree);
  	}
    }
    
    /** clear drawings */
    private final void clearImage(JTree tree)
    {
	tree.paintImmediately(rect2D.getBounds());
    }
    
    /** get autoscroll insets */
    private Insets getAutoscrollInsets() { return autoscrollInsets; }
    
    /** scroll visible tree parts */
    private void autoscroll(JTree tree, Point cursorLocation)
    {
	Insets    insets = getAutoscrollInsets();
	Rectangle outer  = tree.getVisibleRect();
	Rectangle inner  = new Rectangle(outer.x+insets.left,
					 outer.y+insets.top,
					 outer.width-(insets.left+insets.right),
					 outer.height-(insets.top+insets.bottom));
	if (!inner.contains(cursorLocation))  {
	    Rectangle scrollRect = new Rectangle(cursorLocation.x-insets.left,
						 cursorLocation.y-insets.top,
						 insets.left+insets.right,
						 insets.top+insets.bottom);
	    tree.scrollRectToVisible(scrollRect);
	}
    }
    
    /** manage display of a drag mark (highlighting / drawinginsertion mark) */
    public void updateDragMark(JTree tree, Point location)
    {
  	mostRecentLocation = location;
	int row = tree.getRowForPath(tree.getClosestPathForLocation(location.x,
								    location.y));
	TreePath path = tree.getPathForRow(row);
	if(path != null) {
	    Rectangle rowBounds = tree.getPathBounds(path);
	    
	    // find out if we have to mark a tree node or if we 
	    // have to draw an insertion marker
	    int rby = rowBounds.y;
	    int topBottomDist = insertAreaHeight / 2;
	    // x = top, y = bottom of insert area
	    Point topBottom = new Point(rby - topBottomDist, rby + topBottomDist);
	    if(topBottom.x <= location.y && topBottom.y >= location.y) {
		// we are inside an insertArea
		//paintInsertMarker(tree, location);
	    }
	    else {
		// we are inside a node
		markNode(tree, location);
	    }
	}
    }
    
    /**get the most recent mouse location, i.e. the drop location when called */
    public Point getMostRecentDragLocation() { return mostRecentLocation; }
  
    /**mark the node that is closest to the current mouse location */
    private void markNode(JTree tree, Point location)
    {
	TreePath path = tree.getClosestPathForLocation(location.x, location.y);
	if(path != null) {
	    if(lastRowBounds != null) {
		Graphics g = tree.getGraphics();
		g.setColor(Color.white);
		g.drawLine(lastRowBounds.x, lastRowBounds.y, 
			   lastRowBounds.x + lastRowBounds.width, lastRowBounds.y);
	    }
	    tree.setSelectionPath(path);

	    // only expand the path if it makes sense
	    ConfigurationTreeModel model = (ConfigurationTreeModel)tree.getModel();
	    Object selectedNode = path.getLastPathComponent();
	    Object sourceNode = ConfigurationTreeTransferHandler.getSourceNode();
	    if (expandOnDragOver(tree,sourceNode,selectedNode)) tree.expandPath(path);
	}
    }
    
    /** paint insert marker between nodes closest to current mouse location */
    private void paintInsertMarker(JTree tree, Point location)
    {
	Graphics g = tree.getGraphics();
	tree.clearSelection();
	int row = tree.getRowForPath(tree.getClosestPathForLocation(location.x,
								    location.y));
	TreePath path = tree.getPathForRow(row);
	if(path != null) {
	    Rectangle rowBounds = tree.getPathBounds(path);
	    if(lastRowBounds != null) {
		g.setColor(Color.white);
		g.drawLine(lastRowBounds.x, lastRowBounds.y, 
			   lastRowBounds.x + lastRowBounds.width, lastRowBounds.y);
	    }
	    if(rowBounds != null) {
		g.setColor(Color.black);
		g.drawLine(rowBounds.x, rowBounds.y,
			   rowBounds.x + rowBounds.width, rowBounds.y);
	    }
	    lastRowBounds = rowBounds;
	}
    }
    

    /** check if the current node should be expanded on drag-over */
    private boolean expandOnDragOver(JTree selectedTree,
				     Object sourceNode,Object selectedNode)
    {
	if (!selectedTree.isEditable()) return false;

	ConfigurationTreeModel model =
	    (ConfigurationTreeModel)selectedTree.getModel();
	
	if ((sourceNode instanceof EDSourceInstance &&
	     selectedNode == model.edsourcesNode()) ||
	    (sourceNode instanceof ESSourceInstance &&
	     selectedNode == model.essourcesNode()) ||
	    (sourceNode instanceof ESModuleInstance &&
	     selectedNode == model.esmodulesNode()) ||
	    (sourceNode instanceof ServiceInstance &&
	     selectedNode == model.servicesNode()) ||
	    ((sourceNode instanceof ModuleInstance ||
	      sourceNode instanceof ReferenceContainer ||
	      sourceNode instanceof Reference) &&
	     (selectedNode instanceof ReferenceContainer ||
	      selectedNode == model.pathsNode() ||
	      selectedNode == model.sequencesNode())) ||
	    (sourceNode instanceof Parameter)) return true;
	return false;
    }
    

    /** check if the node is accepted as a drop-target */
    private boolean acceptAsDropTarget(JTree targetTree,
				       Object sourceNode,Object targetNode)
    {
	if (!targetTree.isEditable()) return false;

	ConfigurationTreeModel model =
	    (ConfigurationTreeModel)targetTree.getModel();
	
	if ((sourceNode instanceof EDSourceInstance &&
	     (targetNode == model.edsourcesNode()||
	      targetNode instanceof EDSourceInstance)) ||
	    (sourceNode instanceof ESSourceInstance &&
	     (targetNode == model.essourcesNode()||
	      targetNode instanceof ESSourceInstance)) ||
	    (sourceNode instanceof ESModuleInstance &&
	     (targetNode == model.esmodulesNode()||
	      targetNode instanceof ESModuleInstance)) ||
	    (sourceNode instanceof ServiceInstance &&
	     (targetNode == model.servicesNode()||
	      targetNode instanceof ServiceInstance)) ||
	    ((sourceNode instanceof ModuleInstance ||
	      sourceNode instanceof ReferenceContainer ||
	      sourceNode instanceof Reference) &&
	     (targetNode instanceof ReferenceContainer ||
	      targetNode instanceof Reference ||
	      targetNode == model.pathsNode() ||
	      targetNode == model.sequencesNode())) ||
	    (sourceNode instanceof Parameter &&
	     targetNode instanceof Parameter)) return true;
	return false;
    }

}

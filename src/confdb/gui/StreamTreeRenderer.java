package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import confdb.data.*;


/**
 * StreamTreeRenderer
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
class StreamTreeRenderer extends DefaultTreeCellRenderer
{
    //
    // member data
    //

    /** selected node */
    private Object node = null;
    
    /** reference to the tree model */
    private StreamTreeModel treeModel = null;
    
    /** stream icon */
    private ImageIcon streamIcon = null;
    
    /** path icon */
    private ImageIcon pathIcon = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public StreamTreeRenderer()
    {
	super();
	streamIcon = new ImageIcon(getClass().getResource("/StreamIcon.png")); //!
	pathIcon   = new ImageIcon(getClass().getResource("/PathIcon.png"));

    }
    
    
    //
    // member functions
    //
    
    /** prepare the appropriate icon */
    public Icon prepareIcon()
    {
	if (node==null||node.equals(treeModel.getRoot())) return null;
	else if (node instanceof Stream) return streamIcon;
	else if (node instanceof Path)   return pathIcon;
	return null;
    }
    
    /** prepare the appropriate text */
    public String prepareText()
    {
	String result = getText();
	if (node==treeModel.getRoot()) {
	    result = "<html><font size=+1><b>"+result+"</b></font></html>";
	}
	else if (node instanceof Stream) {
	    Stream stream = (Stream)node;
	    int    count  = stream.pathCount();
 	    result = "<html><b>"+stream+"</b> ("+count+")</html>";
	}
	return result;
    }
    
    /** get the leaf icon, for editing */
    public Icon getLeafIcon() { return prepareIcon(); }
    
    /** get the leaf icon, for editing */
    public Icon getOpenIcon() { return prepareIcon(); }
    
    /** get the leaf icon, for editing */
    public Icon getClosedIcon() { return prepareIcon(); }
    
    /** TreeCellRenderer interface, overwrite Default implementation */
    public Component getTreeCellRendererComponent(JTree   tree,
						  Object  value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf,
						  int     row,
						  boolean hasFocus)
    {
	if (treeModel == null)
	    treeModel = (StreamTreeModel)tree.getModel();
	
	super.getTreeCellRendererComponent(tree,value,sel,
					   expanded,leaf,row,
					   hasFocus);
	node = value;
	setIcon(prepareIcon());
	setText(prepareText());
	return this;
    }
    
}

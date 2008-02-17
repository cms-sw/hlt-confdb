package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;

import confdb.data.ConfigInfo;


/**
 * DirectoryTreeCellRenderer
 * -------------------------
 * @author Philipp Schieferdecker
 *
 * Cell renderer for tree view of configurations available in a
 * configuration database instance.
 */
public class DirectoryTreeCellRenderer extends DefaultTreeCellRenderer
{
    //
    // member data
    //

    /** icon for locked configurations */
    private ImageIcon lockedConfigIcon =
	new ImageIcon(getClass().getResource("/LockedConfigIcon.png"));
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public DirectoryTreeCellRenderer()
    {
	super();
	setLeafIcon(new ImageIcon(getClass().getResource("/ConfigIcon.png")));
	setOpenIcon(new ImageIcon(getClass().getResource("/DirIcon.png")));
	setClosedIcon(new ImageIcon(getClass().getResource("/DirIcon.png")));
    }

    
    //
    // member functions
    //
    
    /** TreeCellRenderer interface, overwrite Default implementation */
    public Component getTreeCellRendererComponent(JTree   tree,
						  Object  value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf,
						  int     row,
						  boolean hasFocus)
    {
	super.getTreeCellRendererComponent(tree,value,sel,
					   expanded,leaf,row,
					   hasFocus);
	if (value instanceof ConfigInfo) {
	    ConfigInfo configInfo = (ConfigInfo)value;
	    if (configInfo.isLocked()) {
		setIcon(lockedConfigIcon);
		setText("<html>"+getText() +
			" <font color=#ff0000>LOCKED</font></html>");
	    }
	}
	
	return this;
    }
}
    

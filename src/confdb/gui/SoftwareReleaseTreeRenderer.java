package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import confdb.data.*;


/**
 * SoftwareReleaseTreeRenderer
 * ---------------------------
 * @author Philipp Schieferdecker
 *
 * Render icon and text of a software release tree-node.
 */
public class SoftwareReleaseTreeRenderer extends DefaultTreeCellRenderer
{
    //
    //  member data
    //

    /** currenlty selected node */
    private Object node = null;

    /** the software release tree model */
    private SoftwareReleaseTreeModel treeModel = null;
    
    /** icons to be displayed */
    private ImageIcon subsystemIcon = null;
    private ImageIcon packageIcon   = null;
    private ImageIcon templateIcon  = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public SoftwareReleaseTreeRenderer()
    {
	super();
	subsystemIcon = new ImageIcon(getClass().getResource("/SubsystemIcon.png"));
	packageIcon   = new ImageIcon(getClass().getResource("/PackageIcon.png"));
	templateIcon  = new ImageIcon(getClass().getResource("/TemplateIcon.png"));
    }

    
    //
    // member functions
    //
    
    /** prepare the icon for the current node */
    public Icon prepareIcon()
    {
	if (node==null||node.equals(treeModel.getRoot())) return null;
	if (node instanceof SoftwareSubsystem) return subsystemIcon;
	if (node instanceof SoftwarePackage)   return packageIcon;
	if (node instanceof Template)          return templateIcon;
	System.err.println("SoftwareReleaseTreeRenderer.prepareIcon() ERROR");
	return null;
    }
    
    /** prepare the node text */
    public String prepareText()
    {
	String result = getText();
	if (node instanceof SoftwareSubsystem) {
	    SoftwareSubsystem s = (SoftwareSubsystem)node;
	    int count = s.referencedPackageCount();
	    int max   = s.packageCount();
	    result = (count > 0) ?
		"<html><b>"+s.name()+" ("+count+"/"+max+")</b></html>":
		"<html>"   +s.name()+" ("+count+"/"+max+")</html>";
		
	}
	else if (node instanceof SoftwarePackage) {
	    SoftwarePackage p = (SoftwarePackage)node;
	    int count = p.instantiatedTemplateCount();
	    int max   = p.templateCount();
	    result =(count > 0) ?
		"<html><b>"+
		p.cvsTag()+" "+p.name()+" ("+count+"/"+max+")</b></html>":
		"<html>"
		+p.cvsTag()+" "+p.name()+" ("+count+"/"+max+")</html>";
	}
	else if (node instanceof Template) {
	    Template t = (Template)node;
	    int count = t.instanceCount();
	    result = (count > 0) ?
		"<html><b>"+t.name()+" ["+t.type()+"] ("+count+")</b></html>":
		"<html>"   +t.name()+" ["+t.type()+"] ("+count+")</html>";
	}
	return result;
    }

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
	    treeModel = (SoftwareReleaseTreeModel)tree.getModel();
	
	super.getTreeCellRendererComponent(tree,value,sel,
					   expanded,leaf,row,
					   hasFocus);
	node = value;
	setIcon(prepareIcon());
	setText(prepareText());
	return this;
    }}


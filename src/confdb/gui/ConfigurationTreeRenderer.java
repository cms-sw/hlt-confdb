package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import confdb.data.*;


/**
 * ConfigurationTreeRenderer
 * -------------------------
 * @author Philipp Schieferdecker
 *
 */
class ConfigurationTreeRenderer extends DefaultTreeCellRenderer
{
    //
    // member data
    //

    /** selected node */
    private Object node = null;
    
    /** reference to the tree model */
    private ConfigurationTreeModel treeModel = null;
    
    /** edsource dir icons */
    private ImageIcon edsourceDirIcon = null;
    
    /** edsource icons */
    private ImageIcon edsourceIcon = null;
    
    /** essources dir icon */
    private ImageIcon essourcesDirIcon = null;

    /** essource icon */
    private ImageIcon essourceIcon = null;

    /** service dir icon */
    private ImageIcon servicesDirIcon = null;

    /** service icon */
    private ImageIcon serviceIcon = null;

    /** paths dir icon */
    private ImageIcon pathsDirIcon = null;

    /** path icon */
    private ImageIcon pathIcon = null;

    /** modules dir icon */
    private ImageIcon modulesDirIcon = null;

    /** module icon */
    private ImageIcon moduleIcon = null;

    /** sequences dir icon */
    private ImageIcon sequencesDirIcon = null;
    
    /** sequence icon */
    private ImageIcon sequenceIcon = null;
    
    /** ParameterSet icon */
    private ImageIcon psetIcon = null;

    /** vector<ParameterSet> icon */
    private ImageIcon vpsetIcon = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public ConfigurationTreeRenderer()
    {
	super();
	edsourceDirIcon  = new ImageIcon("icons/EDSourceDirIcon.png");
	edsourceIcon     = new ImageIcon("icons/EDSourceIcon.png");
	essourcesDirIcon = new ImageIcon("icons/ESSourcesDirIcon.png");
	essourceIcon     = new ImageIcon("icons/ESSourceIcon.png");
	servicesDirIcon  = new ImageIcon("icons/ServicesDirIcon.png");
	serviceIcon      = new ImageIcon("icons/ServiceIcon.png");
	pathsDirIcon     = new ImageIcon("icons/PathsDirIcon.png");
	pathIcon         = new ImageIcon("icons/PathIcon.png");
	modulesDirIcon   = new ImageIcon("icons/ModulesDirIcon.png");
	moduleIcon       = new ImageIcon("icons/ModuleIcon.png");
	sequencesDirIcon = new ImageIcon("icons/SequencesDirIcon.png");
	sequenceIcon     = new ImageIcon("icons/SequenceIcon.png");
	psetIcon         = new ImageIcon("icons/PSetIcon.png");      
	vpsetIcon        = new ImageIcon("icons/VPSetIcon.png");      
    }

    
    //
    // member functions
    //
    
    /** prepare the appropriate icon */
    public Icon prepareIcon()
    {
	if (node==null||node.equals(treeModel.getRoot())) return null;
	if (node instanceof StringBuffer) {
	    if (node.equals(treeModel.edsourcesNode())) return edsourceDirIcon;
	    if (node.equals(treeModel.essourcesNode())) return essourcesDirIcon;
	    if (node.equals(treeModel.servicesNode()))  return servicesDirIcon;
	    if (node.equals(treeModel.pathsNode()))     return pathsDirIcon;
	    if (node.equals(treeModel.modulesNode()))   return modulesDirIcon;
	    if (node.equals(treeModel.sequencesNode())) return sequencesDirIcon;
	}
	
	else if (node instanceof EDSourceInstance)  return edsourceIcon;
	else if (node instanceof ESSourceInstance)  return essourceIcon;
	else if (node instanceof ServiceInstance)   return serviceIcon;
	else if (node instanceof ModuleInstance||
		 node instanceof ModuleReference)   return moduleIcon;
	else if (node instanceof Path||
		 node instanceof PathReference)     return pathIcon;
	else if (node instanceof Sequence||
		 node instanceof SequenceReference) return sequenceIcon;
	//else if (node instanceof PSetParameter)     return psetIcon;
	//else if (node instanceof VPSetParameter)    return vpsetIcon;

	return null;
    }
    
    /** prepare the appropriate text */
    public String prepareText()
    {
	String result = getText();
	if (node instanceof Path) {
	    Path path = (Path)node;
	    int  count = path.unsetTrackedParameterCount();
	    result = "<html><b>"+getText()+"</b> ("+path.entryCount()+")";
	    if (count>0) result += " <font color=#ff0000>["+count+"]</font>";
	    result += "</html>";
	}
	else if (node instanceof PathReference) {
	    PathReference reference = (PathReference)node;
	    Path          path      = (Path)reference.parent();
	    int           count     = path.unsetTrackedParameterCount();
	    result = "<html>"+getText()+" ("+path.entryCount()+")";
	    if (count>0) result += " <font color=#ff0000>["+count+"]</font>";
	    result += "</html>";
	}
	else if (node instanceof Sequence) {
	    Sequence sequence = (Sequence)node;
	    int      count    = sequence.unsetTrackedParameterCount();
	    result = "<html>"+getText()+" ("+sequence.entryCount()+")";
	    if (count>0) result += " <font color=#ff0000>["+count+"]</font>";
	    result += "</html>";
	}
	else if (node instanceof SequenceReference) {
	    SequenceReference reference = (SequenceReference)node;
	    Sequence          sequence  = (Sequence)reference.parent();
	    int               count     = sequence.unsetTrackedParameterCount();
	    result = "<html>"+getText()+" ("+sequence.entryCount()+")";
	    if (count>0) result += " <font color=#ff0000>["+count+"]</font>";
	    result += "</html>";
	}
	if (node instanceof PSetParameter||
	    node instanceof VPSetParameter) {
	    Parameter p = (Parameter)node;
	    result = "<font color=#00ff00>" +p.type() + "</font> " + p.name();
	    result = "<html><font size=-2><b>" + result + "</b></font></html>";
	}
	else if (node instanceof Parameter) {
	    Parameter p = (Parameter)node;
	    result = "<font color=#00ff00>"+p.type()+"</font>  "+p.name()+" = ";
	    if (!p.isValueSet()) {
		if (p.isTracked())
		    result += "<font color=#ff0000> ? </font>";
		else
		    result += "<font color=#0000ff> ? </font>";
	    }
	    else if (p.isDefault()) {
		result+="<font color=#0000ff>"+p.valueAsString()+"</font>";
	    }
	    else {
		result+="<font color=#ff0000>"+p.valueAsString()+"</font>";
	    }
	    result = "<html><font size=-2><b>" + result + "</b></font></html>";
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
	    treeModel = (ConfigurationTreeModel)tree.getModel();
	
	super.getTreeCellRendererComponent(tree,value,sel,
					   expanded,leaf,row,
					   hasFocus);
	node = value;
	setIcon(prepareIcon());
	setText(prepareText());
	return this;
    }
	
}

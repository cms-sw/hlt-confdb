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
public class ConfigurationTreeRenderer extends DefaultTreeCellRenderer
{
    //
    // member data
    //

    /** selected node */
    private Object node = null;
    
    /** reference to the tree model */
    private ConfigurationTreeModel treeModel = null;
    
    /** flag indicating if InputTags are to be tracked */
    private boolean doDisplayUnresolvedInputTags = false;
    
    /** pset dir icons */
    private ImageIcon psetDirIcon = null;
    
    /** edsource dir icons */
    private ImageIcon edsourceDirIcon = null;
    
    /** edsource icons */
    private ImageIcon edsourceIcon = null;
    
    /** essources dir icon */
    private ImageIcon essourcesDirIcon = null;

    /** essource icon */
    private ImageIcon essourceIcon = null;

    /** esmodules dir icon */
    private ImageIcon esmodulesDirIcon = null;

    /** esmodule icon */
    private ImageIcon esmoduleIcon = null;

    /** service dir icon */
    private ImageIcon servicesDirIcon = null;

    /** service icon */
    private ImageIcon serviceIcon = null;

    /** paths dir icon */
    private ImageIcon pathsDirIcon = null;

    /** path icon */
    private ImageIcon pathIcon = null;

    /** endpath icon */
    private ImageIcon endpathIcon = null;

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

	psetDirIcon    = null;
	edsourceDirIcon= null;
	edsourceIcon   =
	    new ImageIcon(getClass().getResource("/EDSourceIcon.png"));
	essourcesDirIcon = null;
	essourceIcon   = 
	    new ImageIcon(getClass().getResource("/ESSourceIcon.png"));
	esmodulesDirIcon =
	    null;
	esmoduleIcon   =
	    new ImageIcon(getClass().getResource("/ESModuleIcon.png"));
	servicesDirIcon= null;
	serviceIcon    =
	    new ImageIcon(getClass().getResource("/ServiceIcon.png"));
	pathsDirIcon   = null;
	pathIcon       =
	    new ImageIcon(getClass().getResource("/PathIcon.png"));
	endpathIcon    =
	    new ImageIcon(getClass().getResource("/EndpathIcon.png"));
	modulesDirIcon = null;
	moduleIcon     =
	    new ImageIcon(getClass().getResource("/ModuleIcon.png"));
	sequencesDirIcon = null;
	sequenceIcon   =
	    new ImageIcon(getClass().getResource("/SequenceIcon.png"));
	psetIcon       =
	    new ImageIcon(getClass().getResource("/PSetIcon.png"));
	vpsetIcon      =
	    new ImageIcon(getClass().getResource("/VPSetIcon.png"));
    }
    
    
    //
    // member functions
    //
    
    /** set flag indicating if InputTags are to be tracked */
    public void displayUnresolvedInputTags(Boolean display)
    {
	this.doDisplayUnresolvedInputTags = display;
    }

    /** prepare the appropriate icon */
    public Icon prepareIcon()
    {
	if (node==null||node.equals(treeModel.getRoot())) return null;
	if (node instanceof StringBuffer) {
	    if (node.equals(treeModel.psetsNode()))     return psetDirIcon;
	    if (node.equals(treeModel.edsourcesNode())) return edsourceDirIcon;
	    if (node.equals(treeModel.essourcesNode())) return essourcesDirIcon;
	    if (node.equals(treeModel.esmodulesNode())) return esmodulesDirIcon;
	    if (node.equals(treeModel.servicesNode()))  return servicesDirIcon;
	    if (node.equals(treeModel.pathsNode()))     return pathsDirIcon;
	    if (node.equals(treeModel.sequencesNode())) return sequencesDirIcon;
	    if (node.equals(treeModel.modulesNode()))   return modulesDirIcon;
	}
	
	else if (node instanceof EDSourceInstance)  return edsourceIcon;
	else if (node instanceof ESSourceInstance)  return essourceIcon;
	else if (node instanceof ESModuleInstance)  return esmoduleIcon;
	else if (node instanceof ServiceInstance)   return serviceIcon;
	else if (node instanceof ModuleInstance||
		 node instanceof ModuleReference)   return moduleIcon;
	else if (node instanceof Path||node instanceof PathReference) {
	    if (node instanceof PathReference) node = ((Reference)node).parent();
	    Path path = (Path)node;
	    return (path.isEndPath()) ? endpathIcon : pathIcon;
	}
	else if (node instanceof Sequence||
		 node instanceof SequenceReference) return sequenceIcon;
	else if (node instanceof PSetParameter) {
	    IConfiguration config = (IConfiguration)treeModel.getRoot();
	    if (config.indexOfPSet((PSetParameter)node)>=0) return psetIcon;
	}
	//else if (node instanceof VPSetParameter)    return vpsetIcon;

	return null;
    }
    
    /** prepare the appropriate text */
    public String prepareText()
    {
	String result = getText();
	if (node instanceof Instance) {
	    Instance instance      = (Instance)node;
	    int      count         = instance.unsetTrackedParameterCount();
	    result="<html>";
	    if (instance instanceof ESPreferable) {
		ESPreferable esp = (ESPreferable)instance;
		if   (esp.isPreferred()) result += "<b>"+instance.name()+"</b>";
		else                     result += instance.name();
	    }
	    else result += instance.name();
	    if (count>0) result += " <font color=#ff0000>["+count+"]</font>";
	    result+="</html>";
	}
	else if (node instanceof Path) {
	    Path path       = (Path)node;
	    int  entryCount = path.entryCount();
	    int  count      = path.unsetTrackedParameterCount();
	    result = "<html>"+getText()+" ";
	    result += (entryCount>0) ?
		"("+entryCount+")":"<font color=#ff0000>("+entryCount+")</font>";
	    if (count>0) result += " <font color=#ff0000>["+count+"]</font>";
	    if (doDisplayUnresolvedInputTags) {
		count = path.unresolvedInputTagCount();
		if (count>0) result += " <font color=#0000ff>["+count+"]</font>";
	    }
	    if (path.isEndPath()) result += " <font color=#ff11a9>[endpath]</font>";
	    result += "</html>";
	}
	else if (node instanceof PathReference) {
	    PathReference reference  = (PathReference)node;
	    Path          path       = (Path)reference.parent();
	    int           entryCount = path.entryCount();
	    int           count      = path.unsetTrackedParameterCount();
	    result = "<html>"+getText();
	    result += (entryCount>0) ?
		"("+entryCount+")":"<font color=#ff0000>("+entryCount+")</font>";
	    if (count>0) result += " <font color=#ff0000>["+count+"]</font>";
	    result += "</html>";
	}
	else if (node instanceof Sequence) {
	    Sequence sequence   = (Sequence)node;
	    int      refCount   = sequence.parentPaths().length;
	    int      entryCount = sequence.entryCount();
	    int      count      = sequence.unsetTrackedParameterCount();
	    result = (refCount>0) ?
		"<html>"+getText():"<html><font color=#808080>"+getText()+"</font>";
	    result += (entryCount>0) ?
		" ("+entryCount+")":"<font color=#ff0000>("+entryCount+")</font>";
	    if (count>0) result += " <font color=#ff0000>["+count+"]</font>";
	    result += "</html>";
	}
	else if (node instanceof SequenceReference) {
	    SequenceReference reference  = (SequenceReference)node;
	    Sequence          sequence   = (Sequence)reference.parent();
	    int               entryCount = sequence.entryCount();
	    int               count      = sequence.unsetTrackedParameterCount();
	    result = "<html>"+getText();
	    result += (entryCount>0) ?
		" ("+entryCount+")":"<font color=#ff0000>("+entryCount+")</font>";
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

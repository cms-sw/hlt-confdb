package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import java.util.Enumeration;
import java.util.ArrayList;

import confdb.gui.treetable.*;


import confdb.data.Parameter;
import confdb.data.ParameterContainer;
import confdb.data.VectorParameter;
import confdb.data.PSetParameter;
import confdb.data.VPSetParameter;

/**
 * ParameterTreeModel
 * ------------------
 * @author Philipp Schieferdecker
 *
 * TreeModel (TreeTableTreeModel) do display a set of parameters.
 */
public class ParameterTreeModel extends AbstractTreeTableTreeModel
{
    //
    // member data
    //
    
    /** column names */
    private static String[] columnNames = { "name",
					    "type",
					    "value",
					    "dflt",
					    "trkd" };
    
    /** column class types */
    private static Class[] columnTypes = { TreeTableTreeModel.class,
					   String.class,
					   String.class,
					   Boolean.class,
					   Boolean.class };

    /** list of parameters to be displayed */
    //private ArrayList<Parameter> parameterList = null;
    

    //
    // construction
    //

    /** standard constructor */
    ParameterTreeModel()
    {
	super(null);
    }
    
    
    //
    // member functions
    //
    
    /** AbstractTreeTableTreeModel: getParent() */
    public Object getParent(Object node)
    {
	if (getRoot()==null||node==getRoot()) return null;
	Parameter p = (Parameter)node;
	return p.parent();
    }

    /** Treemodel: number of children of the node */
    public int getChildCount(Object node)
    {
	if (getRoot()==null) return 0;
	if (node instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)node;
	    return pset.parameterCount();
	}
	if (node instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)node;
	    return vpset.parameterSetCount();
	}
	Object[] children = getChildren(node);
	if (children!=null)  return children.length;
	return 0;
    }
    
    /** TreeModel; retreive the i-th child of the node */
    public Object getChild(Object node, int i)
    { 
	if (node instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)node;
	    return pset.parameter(i);
	}
	if (node instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)node;
	    return vpset.parameterSet(i);
	}
	return getChildren(node)[i]; 
    }
    
    /** TreeModel: Is the node a leaf? */
    public boolean isLeaf(Object node)
    {
	boolean result = true;
	if (node.equals(root)) result = false;
	if (node instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)node;
	    if (pset.parameterCount()>0) result = false;
	}
	if (node instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)node;
	    if (vpset.parameterSetCount()>0) result = false;
	}
	return result;
    }
    
    /** TreeTableTreeModel: get number of table columns */
    public int getColumnCount() { return columnNames.length; }

    /** TreeTableTreeModel: get the name of the i-th table column */
    public String getColumnName(int column) { return columnNames[column]; }

    /** TreeTableTreeModel: get the class of the i-th table column */
    public Class getColumnClass(int column) { return columnTypes[column]; }

    /** TreeTableTreeModel: indicate if the cell is editable */
    public boolean isCellEditable(Object node,int column)
    {
	if (getColumnClass(column) == TreeTableTreeModel.class) return true;
	if (column!=2||node.equals(root)) return false;
	if (node instanceof PSetParameter||
	    node instanceof VPSetParameter) return false;
	return true;
    }
    
    /** TreeTableTreeModel: return the value of the i-th table column */
    public Object getValueAt(Object node,int column)
    {
	if (node.equals(root)) return (column==0) ? node.toString() : null;
	
	Parameter p      = (Parameter)node;
	boolean   isPSet = (p instanceof PSetParameter||
			    p instanceof VPSetParameter);
	
	switch (column) {
	case 0: return p.name();
	case 1: return p.type();
	case 2: return (isPSet) ? "" : p.valueAsString();
	case 3: return new Boolean(p.isDefault());
	case 4: return new Boolean(p.isTracked());
	}
	return null;
    }
    
    /** TreeTableTreeModel: set the value of a parameter */
    public void setValueAt(Object value, Object node, int col)
    {
	if (col!=2) return;
	
	if (node instanceof Parameter) {
	    Parameter parameter = (Parameter)node;
	    ParameterContainer container = parameter.getParentContainer();
	    if (container!=null)
		container.updateParameter(parameter.fullName(),parameter.type(),
					  value.toString());
	    else
		parameter.setValue(value.toString(),"");		
	    
	    nodeChanged(parameter);
	}
    }
    
    /** retrieve the children of a Parameter node */
    private Object[] getChildren(Object node)
    {
	if (getRoot() instanceof String) return new Object[0];
	if (node.equals(getRoot())) {
	    ParameterContainer container = (ParameterContainer)node;
	    Object[] children = new Parameter[container.parameterCount()];
	    for (int i=0;i<container.parameterCount();i++)
		children[i] = container.parameter(i);
	    return children;
	}
	if (node instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)node;
	    Object[] children = new Parameter[pset.parameterCount()];
	    for (int i=0;i<pset.parameterCount();i++)
		children[i] = pset.parameter(i);
	    return children;
	}
	else if (node instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)node;
	    Object[] children = new PSetParameter[vpset.parameterSetCount()];
	    for (int i=0;i<vpset.parameterSetCount();i++)
		children[i] = vpset.parameterSet(i);
	    return children;
	}
	return null;
    }

    /** set the parameter container to be displayed (root) */
    public void setParameterContainer(Object container)
    {
	this.root = (container==null) ? new String() : container;
	nodeStructureChanged(root);
    }

    /** display a new set of parameters */
    /**
       public void setParameters(ArrayList<Parameter> parameterList)
       {
       this.parameterList = parameterList;
       nodeStructureChanged(root);
       }
    */

}

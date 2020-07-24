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
import confdb.data.InputTagParameter;
import confdb.data.ESInputTagParameter;
import confdb.data.VInputTagParameter;
import confdb.data.VESInputTagParameter;
import confdb.data.IConfiguration;
import confdb.data.*;

/**
 * ParameterTreeModel
 * ------------------
 * @author Philipp Schieferdecker
 *
 * TreeModel (TreeTableTreeModel) do display a set of parameters.
 */
public class ParameterTreeModel extends AbstractTreeTableTreeModel {
	//
	// member data
	//

	/** root of the tree = configuration */
	private IConfiguration config = null;

	/** column names */
	private static String[] columnNames = { "name", "type", "value", "dflt", "trkd" };

	/** column class types */
	private static Class[] columnTypes = { TreeTableTreeModel.class, String.class, String.class, Boolean.class,
			Boolean.class };

	/** list of parameters to be displayed */
	// private ArrayList<Parameter> parameterList = null;

	//
	// construction
	//

	/** standard constructor */
	ParameterTreeModel() {
		super(null);
		this.config = null;
	}

	ParameterTreeModel(IConfiguration config) {
		super(null);
		this.config = config;
	}

	//
	// member functions
	//

	public void setConfiguration(IConfiguration config) {
		this.config = config;
	}

	/** AbstractTreeTableTreeModel: getParent() */
	public Object getParent(Object node) {
		if (getRoot() == null || node == getRoot())
			return null;
		Parameter p = (Parameter) node;
		return p.parent();
	}

	/** Treemodel: number of children of the node */
	public int getChildCount(Object node) {
		if (getRoot() == null)
			return 0;
		if (node instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter) node;
			return pset.parameterCount();
		}
		if (node instanceof VPSetParameter) {
			VPSetParameter vpset = (VPSetParameter) node;
			return vpset.parameterSetCount();
		}
		Object[] children = getChildren(node);
		if (children != null)
			return children.length;
		return 0;
	}

	/** TreeModel; retreive the i-th child of the node */
	public Object getChild(Object node, int i) {
		if (node instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter) node;
			return pset.parameter(i);
		}
		if (node instanceof VPSetParameter) {
			VPSetParameter vpset = (VPSetParameter) node;
			return vpset.parameterSet(i);
		}
		return getChildren(node)[i];
	}

	/** TreeModel: Is the node a leaf? */
	public boolean isLeaf(Object node) {
		boolean result = true;
		if (node.equals(root))
			result = false;
		if (node instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter) node;
			if (pset.parameterCount() > 0)
				result = false;
		}
		if (node instanceof VPSetParameter) {
			VPSetParameter vpset = (VPSetParameter) node;
			if (vpset.parameterSetCount() > 0)
				result = false;
		}
		return result;
	}

	/** TreeTableTreeModel: get number of table columns */
	public int getColumnCount() {
		return columnNames.length;
	}

	/** TreeTableTreeModel: get the name of the i-th table column */
	public String getColumnName(int column) {
		return columnNames[column];
	}

	/** TreeTableTreeModel: get the class of the i-th table column */
	public Class getColumnClass(int column) {
		return columnTypes[column];
	}

	/** TreeTableTreeModel: indicate if the cell is editable */
	public boolean isCellEditable(Object node, int column) {
		/*
		 * Parameter p = (Parameter) node; System.out.println("COLUMN " + column +
		 * " TYPE: " + p.type() + " PARENT " + p.parent().getClass()); if
		 * (p.type().matches("VPSet") && p.parent() instanceof EDAliasInstance) {
		 * System.out.println("COLUMN 0 EDITABLE NOW"); if (column == 0) return true; }
		 */
		if (getColumnClass(column) == TreeTableTreeModel.class)
			return true;
		if (node instanceof PSetParameter || node instanceof VPSetParameter) {
			return false;
		}
		if (column != 2 || node.equals(root))
			return false;
		return true;
	}

	/** TreeTableTreeModel: return the value of the i-th table column */
	public Object getValueAt(Object node, int column) {
		if (node.equals(root))
			return (column == 0) ? node.toString() : null;

		Parameter p = (Parameter) node;
		boolean isPSet = (p instanceof PSetParameter || p instanceof VPSetParameter);
		
		//System.out.println("NODE TYPE: " + node.getClass().toString());
		//System.out.println("PARAMETER TYPE: " + p.type());
		//System.out.println("COLUMN: " + column);

		String result = new String();
		if (column == 0) {
			/*
			 * boolean ok = true; if (p.type().equals("VPSet")) { if (p.parent() instanceof
			 * EDAliasInstance) { ok = (config.module(p.name()) != null); } }
			 */
			result = p.name();
			/*
			 * if (!ok) { p.setRedName(result); } else { p.setName(result); }
			 */
			return result;
		} else if (column == 1) {
			boolean ok = true;
			String label = null;
			if (p.type().equals("InputTag")) {
				InputTagParameter it = (InputTagParameter) p;
				label = it.label() == null ? "" : it.label();
				ok = ((label.equals("")) || (label.equals("rawDataCollector")) || (label.equals("source"))
						|| (config.module(label) != null));
			} else if (p.type().equals("ESInputTag")) {
				ESInputTagParameter it = (ESInputTagParameter) p;
				label = it.module() == null ? "" : it.module();
				ok = ((label.equals("")) || (config.essource(label) != null) || (config.esmodule(label) != null));
			} else if (p.type().equals("VInputTag")) {
				VInputTagParameter vit = (VInputTagParameter) p;
				for (int i = 0; i < vit.vectorSize(); i++) {
					label = vit.label(i) == null ? "" : vit.label(i);
					ok = ((label.equals("")) || (label.equals("rawDataCollector")) || (label.equals("source"))
							|| (config.module(label) != null));
					if (!ok)
						break;
				}
			} else if (p.type().equals("VESInputTag")) {
				VESInputTagParameter vit = (VESInputTagParameter) p;
				for (int i = 0; i < vit.vectorSize(); i++) {
					label = vit.module(i) == null ? "" : vit.module(i);
					ok = ((label.equals("")) || (config.essource(label) != null) || (config.esmodule(label) != null));
					if (!ok)
						break;
				}
			} else if (p.type().equals("VPSet")) {
				if (p.parent() instanceof EDAliasInstance) {
					ok = (config.module(p.name()) != null);
				}
			}
			result = p.type();
			if (!ok)
				result = "<html><font color=#ff0000>" + result + "</font></html>";
			return result;
		} else if (column == 2) {
			result = (isPSet) ? "" : p.valueAsString();
			return result;
		} else if (column == 3) {
			return new Boolean(p.isDefault());
		} else if (column == 4) {
			return new Boolean(p.isTracked());
		}
		return null;
	}
	

	/** TreeTableTreeModel: set the value of a parameter */
	public void setNameAt(Object value, Object node, int col) {
		//boolean ok = true;
		if (value == null) return;
		if (col != 0)
			return;
		if (node instanceof Parameter) {
			Parameter parameter = (Parameter) node;
			ParameterContainer container = parameter.getParentContainer();
			/*
			 * if (parameter.type().equals("VPSet")) { if (parameter.parent() instanceof
			 * EDAliasInstance) { ok = (config.module(value.toString()) != null); } }
			 */
			if (container != null) {
				//System.out.println("PARAMETER FOUND");
				//if (ok)
					container.updateName(parameter.name(), parameter.type(), value.toString());
				//else
					//container.updateRedName(parameter.name(), parameter.type(), value.toString());
			}
			else {
				//System.out.println("PARAMETER NOT FOUND");
				//if (ok)
					parameter.setName(value.toString());
				//else
					//parameter.setRedName(value.toString());
			}

			nodeChanged(parameter);
		}
	}

	/** TreeTableTreeModel: set the value of a parameter */
	public void setValueAt(Object value, Object node, int col) {
		if (col != 2)
			return;
		if (node instanceof Parameter) {
			Parameter parameter = (Parameter) node;
			ParameterContainer container = parameter.getParentContainer();
			if (container != null) {
				System.out.println("CONTIANER: " + container);
				container.updateParameter(parameter.fullName(), parameter.type(), value.toString());
			}
			else
				parameter.setValue(value.toString());

			nodeChanged(parameter);
		}
	}

	/** retrieve the children of a Parameter node */
	private Object[] getChildren(Object node) {
		if (getRoot() instanceof String)
			return new Object[0];
		if (node.equals(getRoot())) {
			ParameterContainer container = (ParameterContainer) node;
			Object[] children = new Parameter[container.parameterCount()];
			for (int i = 0; i < container.parameterCount(); i++)
				children[i] = container.parameter(i);
			return children;
		}
		if (node instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter) node;
			Object[] children = new Parameter[pset.parameterCount()];
			for (int i = 0; i < pset.parameterCount(); i++)
				children[i] = pset.parameter(i);
			return children;
		} else if (node instanceof VPSetParameter) {
			VPSetParameter vpset = (VPSetParameter) node;
			Object[] children = new PSetParameter[vpset.parameterSetCount()];
			for (int i = 0; i < vpset.parameterSetCount(); i++)
				children[i] = vpset.parameterSet(i);
			return children;
		}
		return null;
	}

	/** set the parameter container to be displayed (root) */
	public void setParameterContainer(Object container) {
		this.root = (container == null) ? new String() : container;
		nodeStructureChanged(root);
	}

}

package confdb.gui.treetable;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/**
   TreeTableTableModel
   -------------------
   @author Philipp Schieferdecker
   
   TableModel for the TreeTable.
*/
public class TreeTableTableModel extends AbstractTableModel
{
    //
    // member data
    //

    /** reference to the tree which is displayed as the first column */
    private JTree tree = null;

    /** reference to that tree's TreeModel */
    private AbstractTreeTableTreeModel treeModel = null;

    /** reference to the last node changed node */
    private Object changedNode = null;
    
    /** reference to the last node removed/inserted (child of changedNode!) */
    private Object childNode = null;
    
    /** >=0 if a child was removed from 'changedNode' */
    private int childIndex = -1;

    /** type of change: CHANGE, INSERT, REMOVE */
    private String typeOfChange = "";


    //
    // construction
    //
    
    /** standard constructor */
    public TreeTableTableModel(JTree tree,AbstractTreeTableTreeModel treeModel)
    {
	this.tree      = tree;
	this.treeModel = treeModel;

	tree.addTreeExpansionListener(new TreeExpansionListener()
	    {
		public void treeExpanded(TreeExpansionEvent e)
		{
		    fireTableDataChanged();
		}
		public void treeCollapsed(TreeExpansionEvent e)
		{
		    fireTableDataChanged();
		}
	    });

	treeModel.addTreeModelListener(new TreeModelListener()
	    {
		public void treeNodesChanged(TreeModelEvent e)
		{
		    changedNode  = e.getChildren()[0];
		    childNode    = null;
		    typeOfChange = "CHANGE";
		    delayedFireTableDataChanged();
		}
		
		public void treeNodesInserted(TreeModelEvent e)
		{
		    if (TreeTableTableModel.this.treeModel.nextFromListener()) {
			changedNode  = null;
			childNode    = null;
			childIndex   = -1;
			typeOfChange = "";
		    }
		    else {
			changedNode  = e.getTreePath().getLastPathComponent();
			childNode    = e.getChildren()[0];
			childIndex   =  e.getChildIndices()[0];
			typeOfChange = "INSERT";
		    }
		    delayedFireTableDataChanged();
		}
		
		public void treeNodesRemoved(TreeModelEvent e)
		{
		    changedNode  = e.getTreePath().getLastPathComponent();
		    childNode    = e.getChildren()[0];
		    childIndex   = e.getChildIndices()[0];
		    typeOfChange = "REMOVE";
		    delayedFireTableDataChanged();
		}
		
		public void treeStructureChanged(TreeModelEvent e)
		{
		    changedNode = null;
		    childNode   = null;
		    childIndex  = -1;
		    typeOfChange = "STRUCTURE";
		    delayedFireTableDataChanged();
		}
	    });
	
	
    }


    //
    // member functions
    //

    /** get last changed node */
    public Object changedNode() { return changedNode; }

    /** get last child node (child of changedNode, removed or inserted) */
    public Object childNode() { return childNode; }

    /** get index of the last child node */
    public int childIndex() { return childIndex; }

    /** get type of last change */
    public String typeOfChange() { return typeOfChange; }

    /** convert the table row into the respective tree node */
    public Object nodeForRow(int row)
    {
	TreePath treePath = tree.getPathForRow(row);
	return treePath.getLastPathComponent();
    }
    
    /** TableModel interface */
    public int getColumnCount() { return treeModel.getColumnCount(); }
    
    public String getColumnName(int column)
    {
	return treeModel.getColumnName(column);
    }
    
    public Class  getColumnClass(int column)
    {
	return treeModel.getColumnClass(column);
    }

    public int getRowCount() { return tree.getRowCount(); }

    public Object getValueAt(int row,int column)
    {
	return treeModel.getValueAt(nodeForRow(row),column);
    }

    public boolean isCellEditable(int row,int column)
    {
	return treeModel.isCellEditable(nodeForRow(row),column);
    }

    public void setValueAt(Object value,int row,int column)
    {
	treeModel.setValueAt(value, nodeForRow(row), column);
	changedNode = nodeForRow(row);
    }
    
    /** notify table model of changes *after* pending events have been processed */
    protected void delayedFireTableDataChanged()
    {
	SwingUtilities.invokeLater(new Runnable()
	    { 
		public void run()
		{
		    fireTableDataChanged();
		}
	    });
    }

}

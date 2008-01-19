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
		    delayedFireTableDataChanged();
		}
		
		public void treeNodesInserted(TreeModelEvent e)
		{
		    delayedFireTableDataChanged();
		}
		
		public void treeNodesRemoved(TreeModelEvent e)
		{
		    delayedFireTableDataChanged();
		}
		
		public void treeStructureChanged(TreeModelEvent e)
		{
		    delayedFireTableDataChanged();
		}
	    });
	
	
    }


    //
    // member functions
    //

    /** convert the table row into the respective tree node */
    public Object nodeForRow(int row)
    {
	TreePath treePath = tree.getPathForRow(row);
	return treePath.getLastPathComponent();
    }
    
    /** TableModel::getColumnCount() */
    public int getColumnCount() { return treeModel.getColumnCount(); }
    
    /** TableModel::getColumnName() */
    public String getColumnName(int column)
    {
	return treeModel.getColumnName(column);
    }
    
    /** TableModel::getColumnClass() */
    public Class getColumnClass(int column)
    {
	return treeModel.getColumnClass(column);
    }

    /** TableModel::getRowCount() */
    public int getRowCount() { return tree.getRowCount(); }

    /** TableModel::getValueAt() */
    public Object getValueAt(int row,int column)
    {
	return treeModel.getValueAt(nodeForRow(row),column);
    }

    /** TableMode::isCellEditable() */
    public boolean isCellEditable(int row,int column)
    {
	if (!tree.isEditable()) return false;
	return treeModel.isCellEditable(nodeForRow(row),column);
    }
    
    /** TableModel::setValueAt() */
    public void setValueAt(Object value,int row,int column)
    {
	treeModel.setValueAt(value, nodeForRow(row), column);
    }
    
    /** notify table model of changes *after* pending events have been processed */
    protected void delayedFireTableDataChanged()
    {
	SwingUtilities.invokeLater(new Runnable() { 
		public void run() { fireTableDataChanged(); }
	    });
    }

}

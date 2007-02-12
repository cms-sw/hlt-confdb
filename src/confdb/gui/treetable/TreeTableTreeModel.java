package confdb.gui.treetable;

import javax.swing.tree.TreeModel;


/**
   TreeTableTreeModel
   ------------------
   @author Philipp Schieferdecker
   
   TreeModel of the TreeTable, in charge!
*/
public interface TreeTableTreeModel extends TreeModel
{
    /** number of columns */
    public int getColumnCount();
    
    /** get the i-th column name */
    public String getColumnName(int column);

    /** get the i-th column class */
    public Class getColumnClass(int column);

    /** a tree node corresponds to a table row, get the value of i-th column */
    public Object getValueAt(Object node,int column);

    /** can the user edit a cell or not */
    public boolean isCellEditable(Object node, int column);

    /** a tree node corresponds to a table row, get the value of i-th column */
    public void setValueAt(Object value, Object node, int column);

}

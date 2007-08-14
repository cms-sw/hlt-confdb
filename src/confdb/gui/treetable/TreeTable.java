package confdb.gui.treetable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;


/**
 * TreeTable
 * ---------
 * @author Philipp Schieferdecker
 *
 * A view combining a tree and a table.
 */
public class TreeTable extends JTable
{
    //
    // member data
    //
    
    /** table cell renderer for the tree column in the table */
    private TreeTableTableCellRenderer cellRenderer = null;

    
    //
    // construction
    //

    /** standard constructor */
    public TreeTable(AbstractTreeTableTreeModel treeModel)
    {
	super();
	cellRenderer = new TreeTableTableCellRenderer(treeModel);
	setModel(new TreeTableTableModel(cellRenderer, treeModel));
	
	ListToTreeSelectionModelWrapper selectionWrapper =
	    new ListToTreeSelectionModelWrapper();
	cellRenderer.setSelectionModel(selectionWrapper);
	setSelectionModel(selectionWrapper.getListSelectionModel());
	
	DefaultTableCellRenderer alignCenter = new DefaultTableCellRenderer();
	alignCenter.setHorizontalAlignment(SwingConstants.CENTER);
	
	setDefaultRenderer(TreeTableTreeModel.class, cellRenderer);
	setDefaultRenderer(String.class, alignCenter);
	setDefaultRenderer(Boolean.class, new CheckBoxTableCellRenderer());
	setDefaultEditor(TreeTableTreeModel.class, new TreeTableTableCellEditor());
    
	setShowGrid(false);
	setIntercellSpacing(new Dimension(0,0));
	if (cellRenderer.getRowHeight()<1) setRowHeight(18);
	
	getTableHeader().setReorderingAllowed(false);
    }
    
    
    //
    // member functions
    //
    
    /** set the renderer of the tree */
    public void setTreeCellRenderer(TreeCellRenderer treeCellRenderer)
    {
	cellRenderer.setCellRenderer(treeCellRenderer);
    }
    
    /** call the tree updateUI whenever the table updateUI is called */
    public void updateUI()
    {
	super.updateUI();
	if (cellRenderer != null) cellRenderer.updateUI();
	LookAndFeel.installColorsAndFont(this,
					 "Tree.background",
					 "Tree.foreground",
					 "Tree.font");
    }
    
    /** make sure the UI never tries to paint the editor [?] */
    public int getEditingRow()
    {
	return (getColumnClass(editingColumn) == TreeTableTreeModel.class) ?
	    -1 : editingRow;
    }
    
    /** pass the new row height to the tree */
    public void setRowHeight(int rowHeight)
    {
	super.setRowHeight(rowHeight);
	if (cellRenderer != null && cellRenderer.getRowHeight() != rowHeight)
	    cellRenderer.setRowHeight(getRowHeight());
    }
    
    /** get the tree which is shared between the models */
    public JTree getTree() { return cellRenderer; }
    
    /** expand the tree */
    public void expandTree()
    {
	JTree tree = getTree();
	int   row  = 0;
	while (row<tree.getRowCount()) {
	    tree.expandRow(row);
	    row++;
	}
    }
    
    
    //
    // class definitions
    //
    
    /**
     * TreeTableTableCellRenderer
     */
    public class TreeTableTableCellRenderer extends JTree implements TableCellRenderer
    {
	//
	// member data
	//
	
	/** last [table/tree] row to be rendered */
	protected int visibleRow;
	
	
	//
	// construction
	//
	
	/** standard constructor */
	public TreeTableTableCellRenderer(TreeModel treeModel)
	{
	    super(treeModel); // JTree!
	    setRootVisible(false);
	}
	
	
	//
	// member functions
	//

	/** match colors between table and tree */
	public void updateUI()
	{
	    super.updateUI();
	    TreeCellRenderer treeCellRenderer = getCellRenderer();
	    if (treeCellRenderer instanceof DefaultTreeCellRenderer) {
		DefaultTreeCellRenderer renderer =
		    (DefaultTreeCellRenderer)treeCellRenderer;
		renderer
		    .setTextSelectionColor(UIManager
					   .getColor
					   ("Table.selectionForeground"));
		renderer
		    .setBackgroundSelectionColor(UIManager
						 .getColor
						 ("Table.selectionBackground"));
	    }
	}
	
	/** forward the reset row height to the table */
	public void setRowHeight(int rowHeight)
	{
	    if (rowHeight>0) {
		super.setRowHeight(rowHeight);
		if (TreeTable.this!=null&&TreeTable.this.getRowHeight()!=rowHeight)
		    TreeTable.this.setRowHeight(rowHeight);
	    }
	}
	
	/** set the active tree bounds to the table geometry */
	public void setBounds(int x, int y, int w, int h)
	{
	    super.setBounds(x,0,w,TreeTable.this.getHeight());
	}
	
	/** draw the last visible row at 0,0 */
	public void paint(Graphics g)
	{
	    g.translate(0,-visibleRow*getRowHeight());
	    super.paint(g);
	}
	
	/** TableCellRenderer interface method. */
	public Component getTableCellRendererComponent(JTable  table,
						       Object  value,
						       boolean isSelected,
						       boolean hasFocus,
						       int     row,
						       int     column)
	{
	    if(isSelected) setBackground(table.getSelectionBackground());
	    else           setBackground(table.getBackground());
	    visibleRow = row;
	    return this;
	}
	

    } // class TreeTableTableCellRenderer
    
    
    /**
     * TreeTableTableCellEditor
     */
    public class TreeTableTableCellEditor extends AbstractCellEditor
	implements TableCellEditor
    {
	//
	// member functions
	//
	
	/** TableCellEditor interface: getTableCellEditorComponent() */
	public Component getTableCellEditorComponent(JTable  table,
						     Object  value,
						     boolean isSelected,
						     int     row,
						     int     column)
	{
	    return cellRenderer;
	}
	
	/** return false, forward mouse events to the tree */
	public boolean isCellEditable(EventObject e)
	{
	    if (e instanceof MouseEvent) {
		for (int counter=getColumnCount()-1;counter>=0;counter--) {
		    if (getColumnClass(counter)==TreeTableTreeModel.class) {
			MouseEvent me    = (MouseEvent)e;
			MouseEvent newME = new MouseEvent(cellRenderer,
							  me.getID(),
							  me.getWhen(),
							  me.getModifiers(),
							  me.getX() -
							  getCellRect(0,
								      counter,
								      true).x,
							  me.getY(),
							  me.getClickCount(),
							  me.isPopupTrigger());
			cellRenderer.dispatchEvent(newME);
			break;
		    }
		}
	    }
	    return false;
	}
	
	/** get the cell editor value */
	public Object getCellEditorValue() { return null; }
	
    } // class TreeTableTableCellEditor
    
    
    /**
     * ListToTreeSelectionModelWrapper
     */
    class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
    { 
	//
	// member data
	//
	
	/** indicate if the ListSelectionModel is being updated. */
	protected boolean updatingListSelectionModel;
	
	//
	// construction
	//
	
	/** standard constructor */
	public ListToTreeSelectionModelWrapper()
	{
	    super();
	    getListSelectionModel()
		.addListSelectionListener(createListSelectionListener());
	}
	
	/** retrieve the list selection model */
	public ListSelectionModel getListSelectionModel()
	{
	    return listSelectionModel;
	}
	
	/** reset row selection, set updating-flag */
	public void resetRowSelection()
	{
	    if(!updatingListSelectionModel) {
		updatingListSelectionModel = true;
		try {
		    super.resetRowSelection();
		}
		finally {
		    updatingListSelectionModel = false;
		}
	    }
	}
	
	/** create instance of ListSelectionHandler */
	protected ListSelectionListener createListSelectionListener()
	{
	    return new ListSelectionHandler();
	}
	
	/** update tree selection model */
	protected void updateSelectedPathsFromSelectedRows()
	{
	    if(!updatingListSelectionModel) {
		updatingListSelectionModel = true;
		try {
		    // This is way expensive, ListSelectionModel needs an
		    // enumerator for iterating.
		    int min = listSelectionModel.getMinSelectionIndex();
		    int max = listSelectionModel.getMaxSelectionIndex();

		    clearSelection();
		    if(min!=-1 && max!=-1) {
			for(int counter=min;counter<=max; counter++) {
			    if(listSelectionModel.isSelectedIndex(counter)) {
				TreePath selPath=cellRenderer.getPathForRow(counter);
				if(selPath != null) addSelectionPath(selPath);
			    }
			}
		    }
		}
		finally {
		    updatingListSelectionModel = false;
		}
	    }
	}
	
	
	//
	// classes
	//
	
	/**
	 * ListSelectionHandler
	 */
	public class ListSelectionHandler implements ListSelectionListener
	{
	    //
	    // member functions
	    //
	    
	    /** responsible to update table-selection if list has changed */
	    public void valueChanged(ListSelectionEvent e)
	    {
		updateSelectedPathsFromSelectedRows();
	    }

	} // class ListSelectionHandler
	
    } // class ListToTreeSelectionModelWrapper
    
} // class TreeTable

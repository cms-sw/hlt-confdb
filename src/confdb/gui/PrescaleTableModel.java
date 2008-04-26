package confdb.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;


/**
 * PrescaleTableModel
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class PrescaleTableModel extends AbstractTableModel
{
    /** number of columns */
    public int getColumnCount() { return 5; }
    
    /** number of rows */
    public int getRowCount() { return 10; }

    /** get column name for colimn 'col' */
    public String getColumnName(int col) { return new String("Col"+col); }
    
    /** get the value for row,col */
    public Object getValueAt(int row, int col) { return new Integer(10); }
    
    /** get the class of the column 'c' */
    public Class getColumnClass(int c) { return Integer.class; }
    
    /** is a cell editable or not? */
    public boolean isCellEditable(int row, int col) { return false; }

    /** set the value of a table cell */
    public void setValueAt(Object value,int row, int col) {}
}


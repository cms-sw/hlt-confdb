package confdb.gui.treetable;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * CheckBoxTableCellRenderer
 * -------------------------
 * @author Philipp Schieferdecker
 *  
 * Enables boolean table column entries to appear as check boxes.
 **/  
public class CheckBoxTableCellRenderer extends JCheckBox implements TableCellRenderer
{
    //
    // construction
    //
    public CheckBoxTableCellRenderer()
    {
	setOpaque(false);
    }
    
    //
    // member functions
    //
    
    /** TableCellRenderer interface */
    public Component getTableCellRendererComponent(JTable table,Object value,
						   boolean isSelected,
						   boolean hasFocus,
						   int row,int column)
    {
	if (value != null) this.setSelected(((Boolean)value).booleanValue());
	else this.setSelected (false);
	setHorizontalAlignment(SwingConstants.CENTER); 
	return this;
    }
    
}

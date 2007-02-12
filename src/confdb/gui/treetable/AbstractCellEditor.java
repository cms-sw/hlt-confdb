package confdb.gui.treetable;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.Component;
import java.awt.event.*;
import java.awt.AWTEvent;

import java.util.EventObject;

import java.io.Serializable;

/**
 * AbstractCellEditor
 * ------------------
 * @author Philipp Schieferdecker
 *
 *
 */
public class AbstractCellEditor implements CellEditor
{
    //
    // member data
    //

    /** list of event listeners */
    protected EventListenerList listenerList = new EventListenerList();
    
    /** CellEditor: getCellEditorValue() */
    public Object getCellEditorValue() { return null; }
    
    /** CellEditor: isCellEditable() */
    public boolean isCellEditable(EventObject e) { return true; }

    /** CellEditor: shouldSelectCell() */
    public boolean shouldSelectCell(EventObject anEvent) { return false; }

    /** CellEditor: stopCellEditing() */
    public boolean stopCellEditing() { return true; }

    /** CellEditor: cancelCellEditing() */
    public void cancelCellEditing() {}
    
    /** add a listener */
    public void addCellEditorListener(CellEditorListener l)
    {
	listenerList.add(CellEditorListener.class, l);
    }

    /** remove a listener */
    public void removeCellEditorListener(CellEditorListener l)
    {
	listenerList.remove(CellEditorListener.class, l);
    }

    /** notify listeners which have registered for this event */
    protected void fireEditingStopped() {
	Object[] listeners = listenerList.getListenerList();
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==CellEditorListener.class) {
		((CellEditorListener)listeners[i+1])
		    .editingStopped(new ChangeEvent(this));
	    }	       
	}
    }
    
    /** notify listeners which have registered for this event */
    protected void fireEditingCanceled()
    {
	Object[] listeners = listenerList.getListenerList();
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==CellEditorListener.class) {
		((CellEditorListener)listeners[i+1])
		    .editingCanceled(new ChangeEvent(this));
	    }	       
	}
    }
    
}

package confdb.gui;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

import confdb.data.*;

import confdb.gui.treetable.*;

/**
 * ParameterTableMouseListener
 * ---------------------------
 * @author Philipp Schieferdecker
 *
 */
public class ParameterTableMouseListener extends MouseAdapter implements ActionListener {
	//
	// member data
	//

	/** frame, to position dialogs */
	private JFrame frame = null;

	/** the corresponding TreeTable */
	private TreeTable treeTable = null;

	/** the corresponding TreeTableTableModel */
	private TreeTableTableModel tableModel = null;

	/** the corresponding TreeTableTreeModel */
	private ParameterTreeModel treeModel = null;

	/** current parameter (set by MouseAdapter for ActionListener) */
	private Parameter parameter = null;

	/** Is current parameter parent parent EDAlias **/
	private boolean isParentParentEDAlias;

	/** Is current parameter parent EDAlias **/
	private boolean isParentEDAlias;

	//
	// construction
	//

	public ParameterTableMouseListener(JFrame frame, TreeTable treeTable) {
		this.frame = frame;
		this.treeTable = treeTable;
		this.treeModel = (ParameterTreeModel) treeTable.getTree().getModel();
		this.tableModel = (TreeTableTableModel) treeTable.getModel();
		this.isParentParentEDAlias = false;
		this.isParentEDAlias = false;
	}

	//
	// member functions
	//

	/** MouseAdapter: mousePressed() */
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	/** MouseAdapter: mousePressed() */
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	/** popup triggered */
	private void maybeShowPopup(MouseEvent e) {
		if (!e.isPopupTrigger())
			return;

		Point pnt = e.getPoint();
		int col = treeTable.columnAtPoint(pnt);
		int row = treeTable.rowAtPoint(pnt);

		parameter = (Parameter) tableModel.nodeForRow(row);

		if (col != 0)
			return;

		JPopupMenu popup = new JPopupMenu();
		Object parent = parameter.parent();
		Boolean bRemoveParam = false;
		Object parentParent = null;

		int dotIndex = parent.getClass().toString().lastIndexOf(".");
		String parentClassName = parent.getClass().toString().substring(dotIndex + 1);
		String parentParentClassName = null;

		isParentEDAlias = (parentClassName.equals("EDAliasInstance")) ? true : false;

		if (parentClassName.equals("VPSetParameter")) {
			parentParent = ((Parameter) parent).parent();
			dotIndex = parentParent.getClass().toString().lastIndexOf(".");
			parentParentClassName = parentParent.getClass().toString().substring(dotIndex + 1);
			isParentParentEDAlias = parentParentClassName.matches("EDAliasInstance") ? true : false;
		}

		if (parent instanceof ParameterContainer) {
			ParameterContainer container = (ParameterContainer) parent;
			bRemoveParam = container.isParameterRemovable(parameter);
		}

		if (parameter instanceof VPSetParameter) {
			JMenuItem menuItem = new JMenuItem("Add PSet");
			menuItem.addActionListener(this);
			popup.add(menuItem);
			if (this.isParentEDAlias) {
				menuItem = new JMenuItem("Rename Module (VPSet)");
				menuItem.addActionListener(this);
				popup.add(menuItem);
			}
			if (parent instanceof PSetParameter || bRemoveParam) {
				popup.addSeparator();
				menuItem = new JMenuItem("Remove Parameter");
				menuItem.addActionListener(this);
				popup.add(menuItem);
			}
			popup.show(e.getComponent(), e.getX(), e.getY());
		} else if (parameter instanceof PSetParameter) {
			JMenuItem menuItem = new JMenuItem("Add Parameter");
			menuItem.addActionListener(this);
			popup.add(menuItem);
			if (parent instanceof VPSetParameter || parent instanceof PSetParameter || bRemoveParam) {
				popup.addSeparator();
				menuItem = new JMenuItem("Remove Parameter");
				menuItem.addActionListener(this);
				popup.add(menuItem);
			}
			popup.show(e.getComponent(), e.getX(), e.getY());
		} else if (parent instanceof PSetParameter || bRemoveParam) {
			JMenuItem menuItem = new JMenuItem("Remove Parameter");
			menuItem.addActionListener(this);
			popup.add(menuItem);
			popup.show(e.getComponent(), e.getX(), e.getY());
		}

	}

	/** ActionListener: actionPerformed() */
	public void actionPerformed(ActionEvent e) {
		JMenuItem src = (JMenuItem) e.getSource();
		String cmd = src.getText();
		Object parent = parameter.parent();

		// vpset
		if (parameter instanceof VPSetParameter) {
			if (cmd.equals("Add PSet")) {
				VPSetParameter vpset = (VPSetParameter) parameter;
				addParameterSet(vpset);
			} else if (cmd.equals("Rename Module (VPSet)")) {
		        String name = JOptionPane.showInputDialog(frame, "Enter new module (VPSet) name", null);
		        treeModel.setNameAt(name, parameter, 0);    	
			} else if (parent instanceof PSetParameter) {
				PSetParameter pset = (PSetParameter) parent;
				if (cmd.equals("Remove Parameter")) {
					int index = pset.removeParameter(parameter);
					treeModel.nodeRemoved(pset, index, parameter);
					notifyParent(pset);
				}
			} else if (parent instanceof ParameterContainer) {
				ParameterContainer container = (ParameterContainer) parent;
				if (cmd.equals("Remove Parameter")) {
					treeModel.nodeRemoved(container, container.indexOfParameter(parameter), parameter);
					container.removeUntrackedParameter(parameter);
				}
			}
		}
		// pset
		else if (parameter instanceof PSetParameter) {
			if (cmd.equals("Add Parameter")) {
				PSetParameter pset = (PSetParameter) parameter;
				addParameter(pset);
			} else if (parent instanceof VPSetParameter) {
				VPSetParameter vpset = (VPSetParameter) parent;
				PSetParameter pset = (PSetParameter) parameter;
				if (cmd.equals("Remove Parameter")) {
					int index = vpset.removeParameterSet(pset);
					treeModel.nodeRemoved(vpset, index, pset);
					notifyParent(vpset);
				}
			} else if (parent instanceof PSetParameter) {
				PSetParameter pset = (PSetParameter) parent;
				if (cmd.equals("Remove Parameter")) {
					int index = pset.removeParameter(parameter);
					treeModel.nodeRemoved(pset, index, parameter);
					notifyParent(pset);
				}
			} else if (parent instanceof ParameterContainer) {
				ParameterContainer container = (ParameterContainer) parent;
				if (cmd.equals("Remove Parameter")) {
					treeModel.nodeRemoved(container, container.indexOfParameter(parameter), parameter);
					container.removeUntrackedParameter(parameter);
				}
			}

		}
		// regular parameter with PSet parent
		else if (parent instanceof PSetParameter) {
			PSetParameter pset = (PSetParameter) parent;
			if (cmd.equals("Remove Parameter")) {
				int index = pset.removeParameter(parameter);
				treeModel.nodeRemoved(pset, index, parameter);
				notifyParent(pset);
			}
		}
		// untracked parameter
		else if (parent instanceof ParameterContainer) {
			ParameterContainer container = (ParameterContainer) parent;
			if (cmd.equals("Remove Parameter")) {
				treeModel.nodeRemoved(container, container.indexOfParameter(parameter), parameter);
				container.removeUntrackedParameter(parameter);
			}
		}
	}

	/** show dialog to add parameter to pset */
	private void addParameter(PSetParameter pset) {
		AddParameterDialog dlg = new AddParameterDialog(frame, pset.isTracked());
		if (this.isParentParentEDAlias) {
			dlg.addString();
			dlg.disableTrackedCheckbox();
			dlg.setName("type");
		}
		dlg.pack();
		dlg.setLocationRelativeTo(frame);
		if (this.isParentParentEDAlias)
			dlg.setValueFocus();
		dlg.setVisible(true);
		if (dlg.validChoice()) {
			Parameter p = ParameterFactory.create(dlg.type(), dlg.name(), dlg.valueAsString(), dlg.isTracked());
			pset.addParameter(p);
			treeModel.nodeInserted(pset, pset.parameterCount() - 1);
			notifyParent(pset);

			TreePath treePath = new TreePath(treeModel.getPathToRoot(pset));
			treeTable.getTree().expandPath(treePath);
		}
	}

	/** show dialog to add PSet to VPSet */
	private void addParameterSet(VPSetParameter vpset) {
		PSetParameter pset = (PSetParameter) ParameterFactory.create("PSet", "", "", vpset.isTracked());

		vpset.addParameterSet(pset);
		treeModel.nodeInserted(vpset, vpset.parameterSetCount() - 1);
		notifyParent(vpset);

		TreePath treePath = new TreePath(treeModel.getPathToRoot(vpset));
		treeTable.getTree().expandPath(treePath);
	}

	/** notify parent instance of change */
	private void notifyParent(Parameter p) {
		Object parent = p.parent();

		while (parent != null) {
			if (parent instanceof Reference) {
				Reference r = (Reference) parent;
				DatabaseEntry dbEntry = (DatabaseEntry) r.parent();
				dbEntry.setHasChanged();
				parent = null;
			} else if (parent instanceof DatabaseEntry) {
				DatabaseEntry dbEntry = (DatabaseEntry) parent;
				dbEntry.setHasChanged();
				parent = null;
			} else if (parent instanceof Parameter) {
				p = (Parameter) parent;
				parent = p.parent();
			} else {
				parent = null;
			}
		}
	}
	
}

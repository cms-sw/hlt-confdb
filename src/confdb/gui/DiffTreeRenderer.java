package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import confdb.diff.*;
import confdb.data.*;

/**
 * DiffTreeRenderer
 * ----------------
 * @author Philipp Schieferdecker
 *
 */
public class DiffTreeRenderer extends DefaultTreeCellRenderer {
	//
	// member data
	//

	/** selected node */
	private Object node = null;

	/** reference to the tree model */
	private DiffTreeModel treeModel = null;

	/** icons */
	private ImageIcon psetIcon;
	private ImageIcon edsourceIcon;
	private ImageIcon essourceIcon;
	private ImageIcon esmoduleIcon;
	private ImageIcon serviceIcon;
	private ImageIcon pathIcon;
	private ImageIcon sequenceIcon;
	private ImageIcon taskIcon;
	private ImageIcon switchProducerIcon;
	private ImageIcon moduleIcon;
	private ImageIcon edAliasIcon;

	private ImageIcon changedIcon;
	private ImageIcon addedIcon;
	private ImageIcon removedIcon;

	//
	// construction
	//

	/** standard constructor */
	public DiffTreeRenderer() {
		super();

		psetIcon = new ImageIcon(getClass().getResource("/PSetIcon.png"));
		edsourceIcon = new ImageIcon(getClass().getResource("/EDSourceIcon.png"));
		essourceIcon = new ImageIcon(getClass().getResource("/ESSourceIcon.png"));
		esmoduleIcon = new ImageIcon(getClass().getResource("/ESModuleIcon.png"));
		serviceIcon = new ImageIcon(getClass().getResource("/ServiceIcon.png"));
		pathIcon = new ImageIcon(getClass().getResource("/PathIcon.png"));
		moduleIcon = new ImageIcon(getClass().getResource("/ModuleIcon.png"));
		edAliasIcon = new ImageIcon(getClass().getResource("/EDAliasIcon.png"));
		sequenceIcon = new ImageIcon(getClass().getResource("/SequenceIcon.png"));
		taskIcon = new ImageIcon(getClass().getResource("/TaskIcon.png"));
		switchProducerIcon = new ImageIcon(getClass().getResource("/SwitchProducerIcon.png"));
		changedIcon = new ImageIcon(getClass().getResource("/ChangedIcon.png"));
		addedIcon = new ImageIcon(getClass().getResource("/AddIcon.png"));
		removedIcon = new ImageIcon(getClass().getResource("/DeleteIcon.png"));
	}

	//
	// member functions
	//

	/** prepare the appropriate icon */
	public Icon prepareIcon() {
		if (node == null || node.equals(treeModel.getRoot()))
			return null;
		if (node instanceof StringBuffer)
			return null;
		else if (node instanceof Comparison) {
			Comparison c = (Comparison) node;
			if (c.isChanged())
				return changedIcon;
			if (c.isAdded())
				return addedIcon;
			if (c.isRemoved())
				return removedIcon;
		}
		return null;
	}

	/** prepare the appropriate text */
	public String prepareText() {
		String result = getText();
		if (node instanceof Comparison) {
			Comparison c = (Comparison) node;
			result = c.toHtml();
		}

		return result;
	}

	/** get the leaf icon, for editing */
	public Icon getLeafIcon() {
		return prepareIcon();
	}

	/** get the leaf icon, for editing */
	public Icon getOpenIcon() {
		return prepareIcon();
	}

	/** get the leaf icon, for editing */
	public Icon getClosedIcon() {
		return prepareIcon();
	}

	/** TreeCellRenderer interface, overwrite Default implementation */
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		if (treeModel == null)
			treeModel = (DiffTreeModel) tree.getModel();

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		node = value;
		setIcon(prepareIcon());
		setText(prepareText());
		return this;
	}

}

package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import confdb.data.*;

/**
 * ConfigurationTreeRenderer
 * -------------------------
 * @author Philipp Schieferdecker
 *
 * Define how each node in the configuration tree view is being
 * displayed.
 */
@SuppressWarnings("serial")
public class ConfigurationTreeRenderer extends DefaultTreeCellRenderer {
	//
	// member data
	//

	/** selected node */
	private Object node = null;

	/** reference to the tree model */
	private ConfigurationTreeModel treeModel = null;
	private Path xpath = null;

	/** flag indicating if InputTags are to be tracked */
	private boolean doDisplayUnresolvedInputTags = false;

	/** edsource icons */
	private ImageIcon edsourceIcon = null;

	/** essource icon */
	private ImageIcon essourceIcon = null;

	/** esmodule icon */
	private ImageIcon esmoduleIcon = null;

	/** service icon */
	private ImageIcon serviceIcon = null;

	/** path icon */
	private ImageIcon pathIcon = null;

	/** endpath icon */
	private ImageIcon endpathIcon = null;

	/** module icon */
	private ImageIcon moduleIcon = null;
	
	/** edAlias icon */
	private ImageIcon edAliasIcon = null;

	/** output module icon */
	private ImageIcon outputIcon = null;

	/** sequence icon */
	private ImageIcon sequenceIcon = null;

	/** task icon */
	private ImageIcon taskIcon = null;
	
	/** switch producer icon */
	private ImageIcon switchProducerIcon = null;

	/** ParameterSet icon */
	private ImageIcon psetIcon = null;

	/** vector<ParameterSet> icon */
	private ImageIcon vpsetIcon = null;

	/** event content icon */
	private ImageIcon contentIcon = null;

	/** stream icon */
	private ImageIcon streamIcon = null;

	/** primary dataset icon */
	private ImageIcon datasetIcon = null;

	//
	// construction
	//

	/** standard constructor */
	public ConfigurationTreeRenderer() {
		super();

		edsourceIcon = new ImageIcon(getClass().getResource("/EDSourceIcon.png"));
		essourceIcon = new ImageIcon(getClass().getResource("/ESSourceIcon.png"));
		esmoduleIcon = new ImageIcon(getClass().getResource("/ESModuleIcon.png"));
		serviceIcon = new ImageIcon(getClass().getResource("/ServiceIcon.png"));
		pathIcon = new ImageIcon(getClass().getResource("/PathIcon.png"));
		endpathIcon = new ImageIcon(getClass().getResource("/EndpathIcon.png"));
		moduleIcon = new ImageIcon(getClass().getResource("/ModuleIcon.png"));
		edAliasIcon = new ImageIcon(getClass().getResource("/EDAliasIcon.png"));
		outputIcon = new ImageIcon(getClass().getResource("/OutputIcon.png"));
		sequenceIcon = new ImageIcon(getClass().getResource("/SequenceIcon.png"));
		taskIcon = new ImageIcon(getClass().getResource("/TaskIcon.png"));
		switchProducerIcon = new ImageIcon(getClass().getResource("/SwitchProducerIcon.png"));
		psetIcon = new ImageIcon(getClass().getResource("/PSetIcon.png"));
		vpsetIcon = new ImageIcon(getClass().getResource("/VPSetIcon.png"));
		contentIcon = new ImageIcon(getClass().getResource("/ContentIcon.png"));
		streamIcon = new ImageIcon(getClass().getResource("/StreamIcon.png"));
		datasetIcon = new ImageIcon(getClass().getResource("/DatasetIcon.png"));
	}

	//
	// member functions
	//

	/** set flag indicating if InputTags are to be tracked */
	public void displayUnresolvedInputTags(Boolean display) {
		this.doDisplayUnresolvedInputTags = display;
	}

	/** prepare the appropriate icon */
	public Icon prepareIcon() {
		if (node == null || node.equals(treeModel.getRoot()))
			return null;
		if (node instanceof StringBuffer)
			return null;

		else if (node instanceof EDSourceInstance)
			return edsourceIcon;
		else if (node instanceof ESSourceInstance)
			return essourceIcon;
		else if (node instanceof ESModuleInstance)
			return esmoduleIcon;
		else if (node instanceof ServiceInstance)
			return serviceIcon;
		else if (node instanceof ModuleInstance || node instanceof ModuleReference)
			return moduleIcon;
		else if (node instanceof EDAliasInstance || node instanceof EDAliasReference)
			return edAliasIcon;
		else if (node instanceof OutputModule || node instanceof OutputModuleReference)
			return outputIcon;
		else if (node instanceof Path || node instanceof PathReference) {
			if (node instanceof PathReference)
				node = ((Reference) node).parent();
			Path path = (Path) node;
			return (path.isEndPath()) ? endpathIcon : pathIcon;
		} else if (node instanceof Sequence || node instanceof SequenceReference) {
			return sequenceIcon;
		} else if (node instanceof Task || node instanceof TaskReference) {
			return taskIcon;
		} else if (node instanceof SwitchProducer || node instanceof SwitchProducerReference) {
			return switchProducerIcon;
		} else if (node instanceof PSetParameter) {
			IConfiguration config = (IConfiguration) treeModel.getRoot();
			if (config.indexOfPSet((PSetParameter) node) >= 0)
				return psetIcon;
		} else if (node instanceof VPSetParameter)
			return vpsetIcon;
		else if (node instanceof EventContent)
			return contentIcon;
		else if (node instanceof Stream)
			return streamIcon;
		else if (node instanceof PrimaryDataset)
			return datasetIcon;
		else if (node instanceof ConfigurationTreeNode) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			if (treeNode.object() instanceof Path)
				return pathIcon;
			if (treeNode.object() instanceof Stream)
				return streamIcon;
			if (treeNode.object() instanceof PrimaryDataset)
				return datasetIcon;
			if (treeNode.object() instanceof StringBuffer)
				return datasetIcon;
		}
		return null;
	}

	/** prepare the appropriate text */
	public String prepareText() {
		String result = getText();

		if (node instanceof Instance) {
			Instance instance = (Instance) node;
			int count = instance.unsetTrackedParameterCount();
			int unresolved = instance.unresolvedESInputTagCount();
			result = "<html>";
			if (instance instanceof ESPreferable) {
				ESPreferable esp = (ESPreferable) instance;
				if (esp.isPreferred())
					result += "<b>" + instance.name() + "</b>";
				else
					result += instance.name();
			} else
				result += instance.name();
			if (count > 0)
				result += " <font color=#ff0000>[" + count + "]</font>";
			if (unresolved > 0)
				result += " <font color=#00ff00>[" + unresolved + "]</font>";
			result += "</html>";
		} else if (node instanceof Path) {
			Path path = (Path) node;
			int entryCount = path.entryCount();
			int unsetCount = path.unsetTrackedParameterCount();
			int unresolvedCount = path.unresolvedESInputTagCount();
			result = "<html>";
			if (!path.isEndPath() && path.datasetCount() == 0)
				result += "<font color=#ff0000>" + getText() + "</font>";
			else
				result += getText();
			result += " ";
			result += (entryCount > 0) ? "(" + entryCount + ")" : "<font color=#ff0000>(" + entryCount + ")</font>";
			if (unsetCount > 0)
				result += " <font color=#ff0000>[" + unsetCount + "]</font>";
			if (unresolvedCount > 0)
				result += " <font color=#00ff00>[" + unresolvedCount + "]</font>";
			if (doDisplayUnresolvedInputTags) {
				unresolvedCount = path.unresolvedInputTagCount();
				if (unresolvedCount > 0)
					result += " <font color=#0000ff>[" + unresolvedCount + "]</font>";
			}
			if (path.isEndPath())
				result += " <font color=#ff11a9>[endpath]</font>";
			result += "</html>";
		} else if (node instanceof PathReference) {
			PathReference reference = (PathReference) node;
			Path path = (Path) reference.parent();
			int entryCount = path.entryCount();
			int count = path.unsetTrackedParameterCount();
			int unresolved = path.unresolvedESInputTagCount();
			result = "<html>" + reference.getOperatorAndName();
			result += (entryCount > 0) ? "(" + entryCount + ")" : "<font color=#ff0000>(" + entryCount + ")</font>";
			if (count > 0)
				result += " <font color=#ff0000>[" + count + "]</font>";
			if (unresolved > 0)
				result += " <font color=#00ff00>[" + unresolved + "]</font>";
			result += "</html>";
		} else if (node instanceof Sequence) {
			Sequence sequence = (Sequence) node;
			int refCount = sequence.parentPaths().length;
			int entryCount = sequence.entryCount();
			int count = sequence.unsetTrackedParameterCount();
			int unresolved = sequence.unresolvedESInputTagCount();
			result = (refCount > 0) ? "<html>" + getText() : "<html><font color=#808080>" + getText() + "</font>";
			result += (entryCount > 0) ? " (" + entryCount + ")" : "<font color=#ff0000>(" + entryCount + ")</font>";
			if (count > 0)
				result += " <font color=#ff0000>[" + count + "]</font>";
			if (unresolved > 0)
				result += " <font color=#00ff00>[" + unresolved + "]</font>";
			result += "</html>";
		} else if (node instanceof SequenceReference) {
			SequenceReference reference = (SequenceReference) node;
			Sequence sequence = (Sequence) reference.parent();
			int entryCount = sequence.entryCount();
			int count = sequence.unsetTrackedParameterCount();
			int unresolved = sequence.unresolvedESInputTagCount();
			result = "<html>" + reference.getOperatorAndName();
			result += (entryCount > 0) ? " (" + entryCount + ")" : "<font color=#ff0000>(" + entryCount + ")</font>";
			if (count > 0)
				result += " <font color=#ff0000>[" + count + "]</font>";
			if (unresolved > 0)
				result += " <font color=#00ff00>[" + unresolved + "]</font>";
			if (doDisplayUnresolvedInputTags && (xpath != null)) {
				int n = 0;
				String label = ((Reference) node).name();
				String[] Unresolved = xpath.unresolvedInputTags();
				for (String un : Unresolved) {
					String[] tokens = un.split("[/:]");
					for (int i = 0; i < tokens.length; i++) {
						if (label.equals(tokens[i])) {
							n++;
							break;
						}
					}
				}
				if (n > 0)
					result += " <font color=#0000ff>[" + n + "]</font>";
			}
			result += "</html>";
		} else if (node instanceof Task) {
			Task task = (Task) node;
			int refCount = task.parentPaths().length;
			int entryCount = task.entryCount();
			int count = task.unsetTrackedParameterCount();
			int unresolved = task.unresolvedESInputTagCount();
			result = (refCount > 0) ? "<html>" + getText() : "<html><font color=#808080>" + getText() + "</font>";
			result += (entryCount > 0) ? " (" + entryCount + ")" : "<font color=#ff0000>(" + entryCount + ")</font>";
			if (count > 0)
				result += " <font color=#ff0000>[" + count + "]</font>";
			if (unresolved > 0)
				result += " <font color=#00ff00>[" + unresolved + "]</font>";
			result += "</html>";
		} else if (node instanceof TaskReference) {
			TaskReference reference = (TaskReference) node;
			Task task = (Task) reference.parent();
			int entryCount = task.entryCount();
			int count = task.unsetTrackedParameterCount();
			int unresolved = task.unresolvedESInputTagCount();
			result = "<html>" + reference.getOperatorAndName();
			result += (entryCount > 0) ? " (" + entryCount + ")" : "<font color=#ff0000>(" + entryCount + ")</font>";
			if (count > 0)
				result += " <font color=#ff0000>[" + count + "]</font>";
			if (unresolved > 0)
				result += " <font color=#00ff00>[" + unresolved + "]</font>";
			if (doDisplayUnresolvedInputTags && (xpath != null)) {
				int n = 0;
				String label = ((Reference) node).name();
				String[] Unresolved = xpath.unresolvedInputTags();
				for (String un : Unresolved) {
					String[] tokens = un.split("[/:]");
					for (int i = 0; i < tokens.length; i++) {
						if (label.equals(tokens[i])) {
							n++;
							break;
						}
					}
				}
				if (n > 0)
					result += " <font color=#0000ff>[" + n + "]</font>";
			}
			result += "</html>";
		} else if (node instanceof SwitchProducer) {
			SwitchProducer switchProducer = (SwitchProducer) node;
			int refCount = switchProducer.parentPaths().length;
			int entryCount = switchProducer.entryCount();
			int count = switchProducer.unsetTrackedParameterCount();
			int unresolved = switchProducer.unresolvedESInputTagCount();
			result = (refCount > 0) ? "<html>" + getText() : "<html><font color=#808080>" + getText() + "</font>";
			result += (entryCount > 0) ? " (" + entryCount + ")" : "<font color=#ff0000>(" + entryCount + ")</font>";
			if (count > 0)
				result += " <font color=#ff0000>[" + count + "]</font>";
			if (unresolved > 0)
				result += " <font color=#00ff00>[" + unresolved + "]</font>";
			result += "</html>";
		} else if (node instanceof SwitchProducerReference) {
			SwitchProducerReference reference = (SwitchProducerReference) node;
			SwitchProducer switchProducer = (SwitchProducer) reference.parent();
			int entryCount = switchProducer.entryCount();
			int count = switchProducer.unsetTrackedParameterCount();
			int unresolved = switchProducer.unresolvedESInputTagCount();
			result = "<html>" + reference.getOperatorAndName();
			result += (entryCount > 0) ? " (" + entryCount + ")" : "<font color=#ff0000>(" + entryCount + ")</font>";
			if (count > 0)
				result += " <font color=#ff0000>[" + count + "]</font>";
			if (unresolved > 0)
				result += " <font color=#00ff00>[" + unresolved + "]</font>";
			if (doDisplayUnresolvedInputTags && (xpath != null)) {
				int n = 0;
				String label = ((Reference) node).name();
				String[] Unresolved = xpath.unresolvedInputTags();
				for (String un : Unresolved) {
					String[] tokens = un.split("[/:]");
					for (int i = 0; i < tokens.length; i++) {
						if (label.equals(tokens[i])) {
							n++;
							break;
						}
					}
				}
				if (n > 0)
					result += " <font color=#0000ff>[" + n + "]</font>";
			}
			result += "</html>";
		} else if (node instanceof ModuleReference) {
			result = "<html>";
			result += ((Reference) node).getOperatorAndName();
			if (doDisplayUnresolvedInputTags && (xpath != null)) {
				int n = 0;
				String label = ((Reference) node).name();
				String[] unresolved = xpath.unresolvedInputTags();
				for (String un : unresolved) {
					String[] tokens = un.split("[/:]");
					for (int i = 0; i < tokens.length; i++) {
						if (label.equals(tokens[i])) {
							n++;
							break;
						}
					}
				}
				if (n > 0)
					result += " <font color=#0000ff>[" + n + "]</font>";
			}
			result += "</html>";
		} else if (node instanceof EDAliasReference) {
			result = "<html>";
			result += ((Reference) node).getOperatorAndName();
			if (doDisplayUnresolvedInputTags && (xpath != null)) {
				int n = 0;
				String label = ((Reference) node).name();
				String[] unresolved = xpath.unresolvedInputTags();
				for (String un : unresolved) {
					String[] tokens = un.split("[/:]");
					for (int i = 0; i < tokens.length; i++) {
						if (label.equals(tokens[i])) {
							n++;
							break;
						}
					}
				}
				if (n > 0)
					result += " <font color=#0000ff>[" + n + "]</font>";
			}
			result += "</html>";
		} else if (node instanceof Reference)
			result = ((Reference) node).getOperatorAndName();

		if (node instanceof PSetParameter || node instanceof VPSetParameter) {
			Parameter p = (Parameter) node;
			result = "<font color=#00ff00>" + p.type() + "</font> " + p.name();
			result = "<html>" + result + "</html>";
		} else if (node instanceof Parameter) {
			Parameter p = (Parameter) node;
			result = "<font color=#00ff00>" + p.type() + "</font>  " + p.name() + " = ";
			if (!p.isValueSet()) {
				if (p.isTracked())
					result += "<font color=#ff0000><b> ? </b></font>";
				else
					result += "<font color=#0000ff><b> ? </b></font>";
			} else if (p.isDefault()) {
				result += "<font color=#0000ff>" + p.valueAsString() + "</font>";
			} else {
				result += "<font color=#ff0000>" + p.valueAsString() + "</font>";
			}
			result = "<html>" + result + "</html>";
		} else if (node instanceof OutputModule) {
			OutputModule output = (OutputModule) node;
			if (output.referenceCount() == 0)
				result = "<html><font color=#ff0000>" + getText() + "</font></html>";
		} else if (node instanceof Stream) {
			Stream stream = (Stream) node;
			result = "<html>" + stream.toString();
			if (stream.unassignedPathCount() > 0)
				result += " <font color=#ff0000>(" + stream.unassignedPathCount() + ")</font>";
			else
				result += " <font color=#00ff00>(" + stream.unassignedPathCount() + ")</font>";
			if (stream.fractionToDisk() < 1.0)
				result += "  [<i><font color=#ff0000>" + stream.fractionToDisk() * 100.
						+ " %</font> written to disk</i>]";
			result += "</html>";
		} else if (node instanceof ConfigurationTreeNode) {
			ConfigurationTreeNode treeNode = (ConfigurationTreeNode) node;
			if ((treeNode.object() instanceof Path) && (treeNode.parent() instanceof Stream)) {
				Path path = (Path) treeNode.object();
				Stream stream = (Stream) treeNode.parent();
				if (stream.listOfUnassignedPaths().indexOf(path) >= 0)
					result = "<html><font color=#ff0000>" + result + "</font></html>";
			}
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
			treeModel = (ConfigurationTreeModel) tree.getModel();

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		node = value;
		setIcon(prepareIcon());

		xpath = null;
		TreePath tp = tree.getPathForRow(row);
		if (tp == null) {
			setText(prepareText());
		} else if ((node instanceof ModuleReference) && (tp.getPathCount() == 3)
				&& (tp.getLastPathComponent() instanceof Path)
				&& (!tp.getLastPathComponent().toString().equals(node.toString()))) {
			// workaround for TreeCellRenderer + getPathForRow bug: see
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4433885
			// just need to provide a sufficiently long string...
			setText(prepareText().replaceAll("</html>", "XXXX</html>"));
		} else {
			if ((tp.getPathCount() > 2) && (tp.getPathComponent(2) instanceof Path))
				xpath = (Path) (tp.getPathComponent(2));
			setText(prepareText());
		}

		return this;
	}

}

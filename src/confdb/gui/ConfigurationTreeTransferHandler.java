package confdb.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.tree.*;

import confdb.data.*;

/**
 * ConfigurationTreeTransferHandler
 * --------------------------------
 * @author Philipp Schieferdecker
 *
 * <p>Transfer handler implementation that supports to move a selected
 * tree node within a <code>JTree</code>.</p>
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * @version 1, 03.08.2005
 */
public class ConfigurationTreeTransferHandler extends TransferHandler {
	//
	// member data
	//

	/** source JTree */
	private static JTree sourceTree = null;

	/** node being dragged */
	private static Object sourceNode = null;

	/** drag image */
	private static BufferedImage dragImage = null;

	/** target JTree */
	private JTree targetTree = null;

	/** target software release */
	private SoftwareRelease targetRelease = null;

	/** parameter tree (-table) model, to be notified of changes */
	private ParameterTreeModel parameterTreeModel = null;

	//
	// construction
	//

	/** standard constructor */
	public ConfigurationTreeTransferHandler(JTree targetTree, SoftwareRelease targetRelease,
			ParameterTreeModel parameterTreeModel) {
		super();
		this.targetTree = targetTree;
		this.targetRelease = targetRelease;
		this.parameterTreeModel = parameterTreeModel;
	}

	//
	// member functions
	//

	/** get a drag image from the currently dragged node (if any) */
	public static BufferedImage getDragImageUser() {
		return dragImage;
	}

	/** get the dragged source node */
	public static Object getSourceNode() {
		return sourceNode;
	}

	/** create transferable which contains all paths that are currently selected */
	protected Transferable createTransferable(JComponent c) {

		Transferable t = null;
		if (c instanceof JTree) {
			sourceTree = (JTree) c;
			sourceNode = sourceTree.getSelectionPath().getLastPathComponent();

			if (sourceNode instanceof ReferenceContainer || sourceNode instanceof Reference
					|| sourceNode instanceof Instance || sourceNode instanceof EventContent
					|| sourceNode instanceof Parameter) {
				ConfigurationTreeTransferHandler.setDragImage();
				t = new GenericTransferable(sourceNode);
			}

			// make PrimaryDataset transferable. feature/bug 88066: Allow moving datasets
			// from one stream to another
			if (sourceNode instanceof ConfigurationTreeNode) {
				if (((ConfigurationTreeNode) sourceNode).object() instanceof PrimaryDataset) {
					ConfigurationTreeTransferHandler.setDragImage();
					t = new GenericTransferable(sourceNode);
				}
			}

			// make Path inside a PrimaryDataset transferable. feature/bug #82526:
			// add/remove path to/from a primary dataset.
			if (sourceNode instanceof ConfigurationTreeNode) {
				if (((ConfigurationTreeNode) sourceNode).object() instanceof Path) {
					Path transferablePath = (Path) ((ConfigurationTreeNode) sourceNode).object();
					ConfigurationTreeTransferHandler.setDragImage();
					t = new GenericTransferable(transferablePath); // passing the Path not the Node.
				}
			}
		}
		return t;
	}

	/** Weather a certain data flavor can be imported or not */
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return true;
	}

	/** import data which is being dragged */
	public boolean importData(JComponent comp, Transferable t) {
		if (sourceNode == null)
			return false;
		if (!(comp instanceof JTree))
			return false;
		if (!((JTree) comp).isEditable())
			return false;

		TreePath targetPath = targetTree.getSelectionPath();
		Object targetNode = targetPath.getLastPathComponent();
		ConfigurationTreeModel targetModel = (ConfigurationTreeModel) targetTree.getModel();
		Configuration targetConfig = (Configuration) targetModel.getRoot();

		//
		// IMPORT & EDIT
		//

		if (sourceNode instanceof Parameter && targetNode instanceof Parameter) {
			Parameter parameter = (Parameter) sourceNode;
			return ConfigurationTreeActions.insertParameter(targetTree, parameter, parameterTreeModel);
		}

		if (sourceTree != targetTree) {
			// insert global PSet
			if (sourceNode instanceof PSetParameter && targetNode == targetModel.psetsNode()) {
				PSetParameter pset = (PSetParameter) ((Parameter) sourceNode).clone(null);
				return ConfigurationTreeActions.insertPSet(targetTree, pset);
			}

			// insert EDSource
			if (sourceNode instanceof EDSourceInstance && targetNode == targetModel.edsourcesNode()) {
				EDSourceInstance source = (EDSourceInstance) sourceNode;
				return ConfigurationTreeActions.importEDSource(targetTree, source);
			}

			// insert ESSource
			if (sourceNode instanceof ESSourceInstance
					&& (targetNode == targetModel.essourcesNode() || targetNode instanceof ESSourceInstance)) {
				ESSourceInstance source = (ESSourceInstance) sourceNode;
				return ConfigurationTreeActions.importESSource(targetTree, source);
			}

			// insert ESModule
			if (sourceNode instanceof ESModuleInstance
					&& (targetNode == targetModel.esmodulesNode() || targetNode instanceof ESModuleInstance)) {
				ESModuleInstance source = (ESModuleInstance) sourceNode;
				return ConfigurationTreeActions.importESModule(targetTree, source);
			}

			// insert Service
			if (sourceNode instanceof ServiceInstance
					&& (targetNode == targetModel.servicesNode() || targetNode instanceof ServiceInstance)) {
				ServiceInstance source = (ServiceInstance) sourceNode;
				return ConfigurationTreeActions.importService(targetTree, source);
			}

			// if a reference is dragged, consider the parent referancable
			if (sourceNode instanceof Reference) {
				Reference r = (Reference) sourceNode;
				sourceNode = r.parent();
			}

			// insert Module
			if (sourceNode instanceof ModuleInstance) {
				ModuleInstance source = (ModuleInstance) sourceNode;
				return ConfigurationTreeActions.importModule(targetTree, source);
			}
			
			// insert EDAlias
			if (sourceNode instanceof EDAliasInstance) {
				EDAliasInstance source = (EDAliasInstance) sourceNode;
				boolean success = false;
				if ((success = ConfigurationTreeActions.importEDAlias(targetTree, source)) == false)
						success = ConfigurationTreeActions.importGlobalEDAlias(targetTree, source);
				return success;
			}

			// insert Path/Sequence/Task/SwitchProducer
			if (sourceNode instanceof ReferenceContainer) {
				ReferenceContainer container = (ReferenceContainer) sourceNode;
				return ConfigurationTreeActions.importReferenceContainer(targetTree, container);
			}
		}
		// sourceTree *is* targetTree -> reordering
		else {
			// move a path
			if (sourceNode instanceof Path && (targetNode instanceof Path || targetNode == targetModel.pathsNode())) {
				Path source = (Path) sourceNode;
				return ConfigurationTreeActions.movePath(targetTree, source);
			}

			// move a sequence
			if (sourceNode instanceof Sequence
					&& (targetNode instanceof Sequence || targetNode == targetModel.sequencesNode())) {
				Sequence source = (Sequence) sourceNode;
				return ConfigurationTreeActions.moveSequence(targetTree, source);
			}

			// move a task
			if (sourceNode instanceof Task && (targetNode instanceof Task || targetNode == targetModel.tasksNode())) {
				Task source = (Task) sourceNode;
				return ConfigurationTreeActions.moveTask(targetTree, source);
			}
			
			// move a switch producer
			if (sourceNode instanceof SwitchProducer && (targetNode instanceof SwitchProducer || 
					targetNode == targetModel.switchProducersNode())) {
				SwitchProducer source = (SwitchProducer) sourceNode;
				return ConfigurationTreeActions.moveSwitchProducer(targetTree, source);
			}

			// move a reference within its parent container
			if (sourceNode instanceof Reference
					&& (targetNode instanceof Reference || targetNode instanceof ReferenceContainer)) {
				Reference source = (Reference) sourceNode;
				return ConfigurationTreeActions.moveReference(targetTree, source);
			}

			// Also allow PrimaryDatasets - bug #88066.
			if (sourceNode instanceof ConfigurationTreeNode) {
				// sourceNode could be a PrimaryDataset:

				ConfigurationTreeNode sctn = (ConfigurationTreeNode) sourceNode;
				if (sctn.object() instanceof PrimaryDataset) {

					// sourceNode IS a PrimaryDataset so check the targetNode:
					if (targetNode instanceof ConfigurationTreeNode) {
						// targetNode could be another PrimaryDataset inside a Stream.
						ConfigurationTreeNode tctn = (ConfigurationTreeNode) targetNode;

						if ((sctn.object() instanceof PrimaryDataset) && (tctn.object() instanceof PrimaryDataset)
								&& (tctn.parent() instanceof Stream)) {
							// target is a PrimaryDataset inside a Streams
							PrimaryDataset pset = (PrimaryDataset) sctn.object();
							TreePath TargetPath = new TreePath(targetModel.getPathToRoot(tctn.parent())); // Set
																											// treePath
																											// to the
																											// targetStream
							targetTree.setSelectionPath(TargetPath);
							// MOVE PSET:
							ConfigurationTreeActions.movePrimaryDataset(sourceTree, targetTree, pset);
						}
					} else if (targetNode instanceof Stream) {
						// targetNode IS a Stream.
						PrimaryDataset pset = (PrimaryDataset) sctn.object();
						TreePath TargetPath = new TreePath(targetModel.getPathToRoot(targetNode)); // point to the
																									// Stream
						targetTree.setSelectionPath(TargetPath);
						// MOVE PSET:
						ConfigurationTreeActions.movePrimaryDataset(sourceTree, targetTree, pset);
					}
				}
			}

			// bug #82526: add/remove path to/from a primary datase
			if (sourceNode instanceof ConfigurationTreeNode) {
				ConfigurationTreeNode sctn = (ConfigurationTreeNode) sourceNode;
				if (sctn.object() instanceof Path) {
					if (targetNode instanceof ConfigurationTreeNode) {
						ConfigurationTreeNode tctn = (ConfigurationTreeNode) targetNode;
						if (tctn.object() instanceof Path) {
							Object parent = tctn.parent();
							if (parent instanceof PrimaryDataset) {
								// targetNode IS a Stream.
								TreePath TargetPath = new TreePath(targetModel.getPathToRoot(parent)); // point to the
																										// Dataset
								targetTree.setSelectionPath(TargetPath);
								ConfigurationTreeActions.movePathsBetweenDatasets(sourceTree, targetTree, sctn);
							}
						} else if (tctn.object() instanceof PrimaryDataset) {
							// targetNode IS a Stream.
							TreePath TargetPath = new TreePath(targetModel.getPathToRoot(tctn.object())); // point to
																											// the
																											// Dataset
							targetTree.setSelectionPath(TargetPath);
							ConfigurationTreeActions.movePathsBetweenDatasets(sourceTree, targetTree, sctn);
						}
					}
				}
			}

		}

		return false;
	}

	/** move selected paths when export of drag is done */
	protected void exportDone(JComponent source, Transferable data, int action) {
		super.exportDone(source, data, action);
	}

	/** Returns the type of transfer actions supported by the source */
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	/** set the current drag image */
	public static void setDragImage() {
		JTree tree = sourceTree;
		try {
			TreePath dragPath = tree.getSelectionPath();
			Rectangle pathBounds = tree.getPathBounds(dragPath);
			TreeCellRenderer r = tree.getCellRenderer();
			TreeModel m = (TreeModel) tree.getModel();
			boolean nIsLeaf = m.isLeaf(dragPath.getLastPathComponent());
			JComponent lbl = (JComponent) r.getTreeCellRendererComponent(tree, sourceNode, false,
					tree.isExpanded(dragPath), nIsLeaf, 0, false);
			lbl.setBounds(pathBounds);
			dragImage = new BufferedImage(lbl.getWidth(), lbl.getHeight(),
					java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
			Graphics2D graphics = dragImage.createGraphics();
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			lbl.setOpaque(false);
			lbl.paint(graphics);
			graphics.dispose();
		} catch (RuntimeException re) {
			dragImage = null;
		}
	}

}

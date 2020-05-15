package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Iterator;
import java.util.EventObject;

import java.io.File;

import confdb.data.*;

/**
 * ConvertConfigurationDialog 
 * ------------------------
 * @author Philipp Schieferdecker
 *
 */
public class ConvertConfigurationDialog extends JDialog {
	//
	// member data
	//

	/** modified configuration according to filter settings */
	private ConfigurationModifier modifier = null;

	/** modifications choosen by the user via the filter tree */
	private ModifierInstructions modifications = null;

	/** indicate that the dialoged was canceled */
	private boolean isCanceled = true;

	/** GUI components */
	private ButtonGroup buttonGroupFormat = new ButtonGroup();
	private JButton jButtonBrowseFileName = new JButton();
	private JButton jButtonBrowseInputFiles = new JButton();
	private JButton jButtonBrowseOutputFile = new JButton();
	private JCheckBox jCheckBoxCff = new JCheckBox();
	private JButton jButtonCancel = new JButton();
	private JButton jButtonConvert = new JButton();
	private JLabel jLabelFileName = new JLabel();
	private JLabel jLabelFormat = new JLabel();
	private JLabel jLabelInputFiles = new JLabel();
	private JLabel jLabelOutputFile = new JLabel();
	private JRadioButton jRadioButtonAscii = new JRadioButton();
	private JRadioButton jRadioButtonPython = new JRadioButton();
	private JRadioButton jRadioButtonHtml = new JRadioButton();
	private JScrollPane jScrollPaneFilter = new JScrollPane();
	private JTextField jTextFieldFileName = new JTextField();
	private JTextField jTextFieldInputFiles = new JTextField();
	private JTextField jTextFieldOutputFile = new JTextField();
	private JTree jTreeFilter = null;

	/** configuration tree model */
	private ConfigurationTreeModel treeModel = null;

	//
	// construction
	//

	/** standard constructor */
	public ConvertConfigurationDialog(JFrame frame, IConfiguration config) {
		super(frame, true);
		this.modifier = new ConfigurationModifier(config);
		this.modifications = new ModifierInstructions();

		treeModel = new ConfigurationTreeModel(config, false);
		jTreeFilter = new JTree(treeModel);
		jTreeFilter.setShowsRootHandles(true);
		jTreeFilter.setRootVisible(false);
		jTreeFilter.setEditable(true);
		jTreeFilter.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		ConvertConfigTreeRenderer r1 = new ConvertConfigTreeRenderer(modifications);
		ConvertConfigTreeRenderer r2 = new ConvertConfigTreeRenderer(modifications);
		jTreeFilter.setCellRenderer(r1);
		jTreeFilter.setCellEditor(new ConvertConfigTreeEditor(jTreeFilter, modifications, r2));

		jTextFieldFileName.setText(modifier.name() + "_V" + modifier.version() + ".cfg");

		setTitle("Convert configuration");

		setContentPane(initComponents());

		// register action listeners
		jCheckBoxCff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jCheckBoxCffActionPerformed(e);
			}
		});
		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonCancelActionPerformed(e);
			}
		});
		jButtonConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonConvertActionPerformed(e);
			}
		});
		jButtonBrowseFileName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonBrowseFileNameActionPerformed(e);
			}
		});
		jButtonBrowseInputFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonBrowseInputFilesActionPerformed(e);
			}
		});
		jButtonBrowseOutputFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonBrowseOutputFileActionPerformed(e);
			}
		});
		jRadioButtonAscii.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jRadioButtonAsciiActionPerformed(e);
			}
		});
		jRadioButtonPython.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jRadioButtonPythonActionPerformed(e);
			}
		});
		jRadioButtonHtml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jRadioButtonHtmlActionPerformed(e);
			}
		});

		// initialize format button-group
		jRadioButtonAscii.setActionCommand("Ascii");
		jRadioButtonPython.setActionCommand("Python");
		jRadioButtonHtml.setActionCommand("Html");
		buttonGroupFormat.add(jRadioButtonAscii);
		buttonGroupFormat.add(jRadioButtonPython);
		buttonGroupFormat.add(jRadioButtonHtml);
		jRadioButtonAscii.setSelected(true);
		// jRadioButtonPython.setEnabled(false);

		// ensure that the text field gets focus
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				jTextFieldFileName.requestFocusInWindow();
				jTextFieldFileName.selectAll();
			}
		});
	}

	//
	// member functions
	//

	/** retrieve configuration to be converted */
	public IConfiguration configToConvert() {
		String inputFiles = jTextFieldInputFiles.getText();
		String outputFile = jTextFieldOutputFile.getText();
		if (inputFiles.length() > 0)
			modifications.insertPoolSource(inputFiles);
		if (outputFile.length() > 0)
			modifications.insertPoolOutputModule(outputFile);
		if (asFragment()) {
			modifications.filterAllEDSources(true);
			modifications.filterAllOutputModules(true);
		}
		modifier.modify(modifications);
		return modifier;
	}

	/** check if the dialog was canceled */
	public boolean isCanceled() {
		return this.isCanceled;
	}

	/** get file name */
	public String fileName() {
		return jTextFieldFileName.getText();
	}

	/** get format */
	public String format() {
		return buttonGroupFormat.getSelection().getActionCommand();
	}

	/** convert as a fragment? */
	public boolean asFragment() {
		return jCheckBoxCff.isSelected();
	}

	//
	// private member functions
	//

	/** CFF checkbox */
	private void jCheckBoxCffActionPerformed(ActionEvent e) {
		setFileNameExtension();
	}

	/** CANCEL button */
	private void jButtonCancelActionPerformed(ActionEvent e) {
		isCanceled = true;
		setVisible(false);
	}

	/** CONVERT button */
	private void jButtonConvertActionPerformed(ActionEvent e) {
		if (fileName().length() > 0)
			isCanceled = false;
		setVisible(false);
	}

	/** BROWSE FILE NAME button */
	private void jButtonBrowseFileNameActionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			jTextFieldFileName.setText(file.getAbsolutePath());
		}
	}

	/** BROWSE INPUT FILES button */
	private void jButtonBrowseInputFilesActionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new RootFileFilter());
		fileChooser.setAcceptAllFileFilterUsed(false);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			jTextFieldInputFiles.setText(file.getAbsolutePath());
		}
	}

	/** BROWSE OUTPUT FILE button */
	private void jButtonBrowseOutputFileActionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			jTextFieldOutputFile.setText(file.getAbsolutePath());
		}
	}

	/** ascii format choosen */
	private void jRadioButtonAsciiActionPerformed(ActionEvent e) {
		setFileNameExtension();
	}

	/** python format choosen */
	private void jRadioButtonPythonActionPerformed(ActionEvent e) {
		setFileNameExtension();
	}

	/** html format choosen */
	private void jRadioButtonHtmlActionPerformed(ActionEvent e) {
		setFileNameExtension();
	}

	private void setFileNameExtension() {
		String extension = "";
		String format = format();
		if (format.equalsIgnoreCase("ascii"))
			extension = (asFragment()) ? "cff" : "cfg";
		else if (format.equalsIgnoreCase("python"))
			extension = (asFragment()) ? "py" : "py";
		else if (format.equalsIgnoreCase("html"))
			extension = (asFragment()) ? "html" : "html";
		String fileName = fileName();
		if (fileName.length() > 0) {
			int index = fileName.lastIndexOf(".");
			if (index > 0)
				fileName = fileName.substring(0, index);
			jTextFieldFileName.setText(fileName + "." + extension);
		}
	}

	/** init GUI components */
	private JPanel initComponents() {
		JPanel jPanel = new JPanel();

		jLabelFileName.setText("File Name:");
		jLabelInputFiles.setText("Input Files:");
		jLabelOutputFile.setText("Output File:");

		jCheckBoxCff.setBackground(new java.awt.Color(255, 255, 255));
		jCheckBoxCff.setText("cff");
		jCheckBoxCff.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckBoxCff.setMargin(new java.awt.Insets(0, 0, 0, 0));

		jButtonCancel.setText("Cancel");

		jButtonConvert.setFont(new java.awt.Font("Lucida Grande", 1, 13));
		jButtonConvert.setText("Convert");

		jRadioButtonAscii.setText("Ascii");
		jRadioButtonAscii.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jRadioButtonAscii.setMargin(new java.awt.Insets(0, 0, 0, 0));

		jRadioButtonPython.setText("Python");
		jRadioButtonPython.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jRadioButtonPython.setMargin(new java.awt.Insets(0, 0, 0, 0));

		jRadioButtonHtml.setText("HTML");
		jRadioButtonHtml.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jRadioButtonHtml.setMargin(new java.awt.Insets(0, 0, 0, 0));

		jLabelFormat.setText("Format:");

		jScrollPaneFilter.setViewportView(jTreeFilter);

		jButtonBrowseFileName.setText("...");
		jButtonBrowseInputFiles.setText("...");
		jButtonBrowseOutputFile.setText("...");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout
										.createSequentialGroup().addContainerGap().addComponent(jScrollPaneFilter,
												javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE))
								.addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(layout.createSequentialGroup().addComponent(jCheckBoxCff).addGap(28)
												.addComponent(jButtonCancel, javax.swing.GroupLayout.DEFAULT_SIZE, 122,
														Short.MAX_VALUE)
												.addGap(14)
												.addComponent(jButtonConvert, javax.swing.GroupLayout.DEFAULT_SIZE, 122,
														Short.MAX_VALUE)
												.addGap(19))
										.addGroup(layout.createSequentialGroup().addGroup(layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(jLabelFileName).addComponent(jLabelInputFiles)
												.addComponent(jLabelOutputFile).addComponent(jLabelFormat))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(layout.createSequentialGroup()
																.addComponent(jRadioButtonAscii).addGap(41)
																.addComponent(jRadioButtonPython)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																		43, Short.MAX_VALUE)
																.addComponent(jRadioButtonHtml))
														.addComponent(jTextFieldOutputFile,
																javax.swing.GroupLayout.DEFAULT_SIZE, 260,
																Short.MAX_VALUE)
														.addComponent(jTextFieldInputFiles,
																javax.swing.GroupLayout.DEFAULT_SIZE, 260,
																Short.MAX_VALUE)
														.addComponent(jTextFieldFileName,
																javax.swing.GroupLayout.DEFAULT_SIZE, 260,
																Short.MAX_VALUE))))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(jButtonBrowseOutputFile, 0, 0, Short.MAX_VALUE)
												.addComponent(jButtonBrowseInputFiles, 0, 0, Short.MAX_VALUE)
												.addComponent(jButtonBrowseFileName,
														javax.swing.GroupLayout.PREFERRED_SIZE, 41, Short.MAX_VALUE))))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelFileName).addComponent(jButtonBrowseFileName)
								.addComponent(jTextFieldFileName, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jTextFieldInputFiles, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelInputFiles).addComponent(jButtonBrowseInputFiles))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jTextFieldOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelOutputFile).addComponent(jButtonBrowseOutputFile))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jRadioButtonHtml).addComponent(jRadioButtonAscii)
								.addComponent(jLabelFormat).addComponent(jRadioButtonPython))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPaneFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jCheckBoxCff).addComponent(jButtonConvert).addComponent(jButtonCancel))
						.addContainerGap()));

		return jPanel;
	}

	//
	// innner classes
	//

	/** ConvertConfigTreeRenderer */
	class ConvertConfigTreeRenderer extends DefaultTreeCellRenderer {
		/** current modifications */
		private ModifierInstructions modifications = null;

		/** the check box */
		private JCheckBox checkBox = new JCheckBox();

		/** tree colors */
		Color selectionForeground = null;
		Color selectionBackground = null;
		Color textForeground = null;
		Color textBackground = null;

		/** constructor */
		public ConvertConfigTreeRenderer(ModifierInstructions modifications) {
			this.modifications = modifications;

			Font fontValue = UIManager.getFont("Tree.font");
			if (fontValue != null)
				checkBox.setFont(fontValue);
			Boolean bFocus = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
			checkBox.setFocusPainted((bFocus != null) && (bFocus.booleanValue()));

			selectionForeground = UIManager.getColor("Tree.selectionForeground");
			selectionBackground = UIManager.getColor("Tree.selectionBackground");
			textForeground = UIManager.getColor("Tree.textForeground");
			textBackground = UIManager.getColor("Tree.textBackground");
		}

		/** retrieve the checkbox */
		public JCheckBox getCheckBox() {
			return checkBox;
		}

		/** DefaultTreeCellRenderer.getTreeCellRendererComponent */
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			if (selected) {
				checkBox.setForeground(selectionForeground);
				checkBox.setBackground(selectionBackground);
			} else {
				checkBox.setForeground(textForeground);
				checkBox.setBackground(textBackground);
			}

			ConfigurationTreeModel treeModel = (ConfigurationTreeModel) tree.getModel();
			IConfiguration config = (IConfiguration) treeModel.getRoot();

			if (value instanceof StringBuffer) {
				if (value == treeModel.psetsNode()) {
					checkBox.setSelected(!modifications.doFilterAllPSets() && (config.psetCount() > 0));
					checkBox.setEnabled(config.psetCount() > 0);
				} else if (value == treeModel.edsourcesNode()) {
					checkBox.setSelected(!modifications.doFilterAllEDSources() && config.edsourceCount() > 0);
					checkBox.setEnabled(config.edsourceCount() > 0);
				} else if (value == treeModel.essourcesNode()) {
					checkBox.setSelected(!modifications.doFilterAllESSources() && config.essourceCount() > 0);
					checkBox.setEnabled(config.essourceCount() > 0);
				} else if (value == treeModel.esmodulesNode()) {
					checkBox.setSelected(!modifications.doFilterAllESModules() && config.esmoduleCount() > 0);
					checkBox.setEnabled(config.esmoduleCount() > 0);
				} else if (value == treeModel.servicesNode()) {
					checkBox.setSelected(!modifications.doFilterAllServices() && config.serviceCount() > 0);
					checkBox.setEnabled(config.serviceCount() > 0);
				} else if (value == treeModel.pathsNode()) {
					checkBox.setSelected(!modifications.doFilterAllPaths() && config.pathCount() > 0);
					checkBox.setEnabled(config.pathCount() > 0);
				} else if (value == treeModel.sequencesNode()) {
					boolean isSelected = false;
					if (modifications.requestedSequenceIterator().hasNext())
						isSelected = true;
					else {
						Iterator<Sequence> it = config.sequenceIterator();
						while (!isSelected && it.hasNext()) {
							Sequence sequence = it.next();
							Path[] paths = sequence.parentPaths();
							for (Path p : paths) {
								if (!modifications.isInBlackList(p)) {
									isSelected = true;
									break;
								}
							}
						}
					}
					checkBox.setSelected(isSelected);
					checkBox.setEnabled(false);
				} else if (value == treeModel.modulesNode()) {
					boolean isSelected = false;
					if (modifications.requestedModuleIterator().hasNext())
						isSelected = true;
					else {
						Iterator<ModuleInstance> it = config.moduleIterator();
						while (!isSelected && it.hasNext()) {
							ModuleInstance module = it.next();
							Path[] paths = module.parentPaths();
							for (Path p : paths) {
								if (!modifications.isInBlackList(p)) {
									isSelected = true;
									break;
								}
							}
						}
					}
					checkBox.setSelected(isSelected);
					checkBox.setEnabled(false);
				}
			} else if (value instanceof Sequence || value instanceof ModuleInstance) {
				checkBox.setEnabled(true);
				Referencable moduleOrSequence = (Referencable) value;
				if (modifications.isRequested(moduleOrSequence))
					checkBox.setSelected(true);
				else if (modifications.doFilterAllPaths() || modifications.isUndefined(moduleOrSequence))
					checkBox.setSelected(false);
				else {
					boolean isSelected = false;
					Path[] parentPaths = moduleOrSequence.parentPaths();
					for (Path p : parentPaths) {
						if (!modifications.isInBlackList(p)) {
							isSelected = true;
							break;
						}
					}
					checkBox.setSelected(isSelected);
					if (!ConvertConfigurationDialog.this.asFragment() && isSelected)
						checkBox.setEnabled(false);
				}
			} else {
				checkBox.setEnabled(true);
				checkBox.setSelected(!modifications.isInBlackList(value));
			}

			checkBox.setText(value.toString());

			return checkBox;
		}
	}

	/** ConvertConfigTreeEditor */
	class ConvertConfigTreeEditor extends AbstractCellEditor implements TreeCellEditor {
		/** tree */
		private JTree tree = null;

		/** modifications */
		private ModifierInstructions modifications = null;

		/** renderer */
		private ConvertConfigTreeRenderer renderer = null;

		/** current value */
		private Object value = null;

		/** standard constructor */
		public ConvertConfigTreeEditor(JTree tree, ModifierInstructions modifications,
				ConvertConfigTreeRenderer renderer) {
			this.tree = tree;
			this.modifications = modifications;
			this.renderer = renderer;
		}

		/** getCellEditorValue */
		public Object getCellEditorValue() {
			ConfigurationTreeModel treeModel = (ConfigurationTreeModel) tree.getModel();
			IConfiguration config = (IConfiguration) treeModel.getRoot();

			if (value instanceof StringBuffer) {
				if (value == treeModel.psetsNode()) {
					modifications.filterAllPSets(!modifications.doFilterAllPSets(), config);
				} else if (value == treeModel.edsourcesNode()) {
					modifications.filterAllEDSources(!modifications.doFilterAllEDSources(), config);
				} else if (value == treeModel.essourcesNode()) {
					modifications.filterAllESSources(!modifications.doFilterAllESSources(), config);
				} else if (value == treeModel.esmodulesNode()) {
					modifications.filterAllESModules(!modifications.doFilterAllESModules(), config);
				} else if (value == treeModel.servicesNode()) {
					modifications.filterAllServices(!modifications.doFilterAllServices(), config);
				} else if (value == treeModel.pathsNode()) {
					modifications.filterAllPaths(!modifications.doFilterAllPaths(), config);
				}
			} else if (value instanceof Sequence) {
				Sequence s = (Sequence) value;

				boolean isReferenced = false;
				Path[] parentPaths = s.parentPaths();
				for (Path p : parentPaths)
					if (!modifications.isInBlackList(p)) {
						isReferenced = true;
						break;
					}

				if (isReferenced) {
					if (modifications.isUndefined(s))
						modifications.redefineSequence(s.name());
					else
						modifications.undefineSequence(s.name());
				} else {
					if (modifications.isRequested(s))
						modifications.unrequestSequence(s.name());
					else
						modifications.requestSequence(s.name());
				}
			} else if (value instanceof ModuleInstance) {
				ModuleInstance m = (ModuleInstance) value;

				boolean isReferenced = false;
				Path[] parentPaths = m.parentPaths();
				for (Path p : parentPaths)
					if (!modifications.isInBlackList(p)) {
						isReferenced = true;
						break;
					}

				if (isReferenced) {
					if (modifications.isUndefined(m))
						modifications.redefineModule(m.name());
					else
						modifications.undefineModule(m.name());
				} else {
					if (modifications.isRequested(m))
						modifications.unrequestModule(m.name());
					else
						modifications.requestModule(m.name());
				}
			} else {
				boolean isFiltered = modifications.isInBlackList(value);
				if (isFiltered) {
					modifications.removeFromBlackList(value);
				} else {
					int n = modifications.insertIntoBlackList(value);
					if (n == config.componentCount(value.getClass()))
						modifications.filterAll(value.getClass(), true);
				}
				treeModel.nodeChanged(treeModel.getParent(value));
			}

			return value.toString();
		}

		/** isCellEditable */
		public boolean isCellEditable(EventObject event) {
			if (event instanceof MouseEvent) {
				MouseEvent mouseEvent = (MouseEvent) event;
				TreePath treePath = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
				if (treePath == null)
					return false;

				if (!ConvertConfigurationDialog.this.asFragment()) {
					Object o = treePath.getLastPathComponent();
					if (o instanceof Sequence || o instanceof ModuleInstance) {
						Referencable moduleOrSequence = (Referencable) o;
						Path[] paths = moduleOrSequence.parentPaths();
						for (Path p : paths)
							if (!modifications.isInBlackList(p))
								return false;
					}
				}

			}
			return true;
		}

		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row) {
			this.value = value;

			Component editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);

			// editor always selected / focused
			ItemListener itemListener = new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if (stopCellEditing()) {
						fireEditingStopped();
					}
				}
			};

			if (editor instanceof JCheckBox) {
				((JCheckBox) editor).addItemListener(itemListener);
			}

			return editor;
		}
	}

	/** ROOT file-filter */
	class RootFileFilter extends FileFilter {
		/** FileFilter.accept() */
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;

			String extension = getExtension(f);
			if (extension != null) {
				if (extension.equals("root") || extension.equals("list"))
					return true;
				else
					return false;
			}
			return false;
		}

		/* get description of this filter */
		public String getDescription() {
			return "ROOT file or list of ROOT files (*.root, *.list)";
		}

		/** get extension of a file name */
		public String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1)
				ext = s.substring(i + 1).toLowerCase();
			return ext;
		}
	}

}

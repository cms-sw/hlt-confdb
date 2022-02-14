package confdb.gui;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import confdb.data.Configuration;
import confdb.data.Stream;
import confdb.data.PrimaryDataset;
import confdb.data.Path;

/**
 * EditDatasetDialog
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Edit which paths are part of a particular primary dataset.
 */
public class EditDatasetDialog extends JDialog {
	//
	// member data
	//

	/** reference to the configuration */
	private Configuration config = null;

	/** reference to the dataset being edited */
	private PrimaryDataset dataset = null;

	/** collection of check boxes, corresponding to above paths (!) */
	private ArrayList<JCheckBox> pathCheckBoxes = new ArrayList<JCheckBox>();

	/** GUI components */
	JList jListPaths = new JList();
	JButton jButtonOk = new javax.swing.JButton();
	JButton jButtonCancel = new javax.swing.JButton();

	//
	// construction
	//
	public EditDatasetDialog(JFrame frame, Configuration config, PrimaryDataset dataset) {
		super(frame, true);
		this.config = config;
		this.dataset = dataset;
		setContentPane(initComponents(frame.getSize()));
		setTitle("Edit Primary Dataset > " + dataset.name() + " <");

		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setVisible(false);
			}
		});

		jButtonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButtonOkActionPerformed(evt);

			}
		});
	}

	//
	// member functions
	//

	/** update path list based on check boxes */
	private void updatePathList() {
		DefaultListModel lm = (DefaultListModel) (jListPaths.getModel());
		lm.removeAllElements();
		Iterator<JCheckBox> itCB = pathCheckBoxes.iterator();
		while (itCB.hasNext()) {
			JCheckBox cb = itCB.next();
			if (cb.isSelected()) {
				String pathName = cb.getActionCommand();
				if (dataset.path(pathName) == null)
					pathName = "<html><font color=#ff0000>" + pathName + "</font></html>";
				lm.addElement(pathName);
			}
		}
	}

	/** ItemListener for path check-boxes */
	private void cbItemStateChanged(ItemEvent e) {
		updatePathList();
	}

	/** ActionListener for OK button */
	private void jButtonOkActionPerformed(ActionEvent e) {
		ArrayList<String> pathNames = new ArrayList<String>();
		Iterator<JCheckBox> itCB = pathCheckBoxes.iterator();

		while (itCB.hasNext()) {
			JCheckBox cb = itCB.next();
			if (cb.isSelected())
				pathNames.add(cb.getActionCommand());
		}
		dataset.clear();
		Iterator<String> itS = pathNames.iterator();
		while (itS.hasNext())
			dataset.insertPath(config.path(itS.next()));
		setVisible(false);
	}

	/** initizlize GUI components */
	private JPanel initComponents(Dimension size) {
		JPanel jPanel = new JPanel();
		JScrollPane jScrollPanePaths = new JScrollPane();
		JScrollPane jScrollPaneList = new JScrollPane();
		JPanel jPanelPaths = new JPanel();

		jListPaths.setModel(new DefaultListModel());

		jPanelPaths.setLayout(new GridLayout(0, 2));
		ArrayList<Path> paths = new ArrayList<Path>();
		Iterator<Path> itP = config.pathIterator();
		while (itP.hasNext()) {
			Path path = itP.next();
			if (path.isStdPath())
				paths.add(path);
		}
		Collections.sort(paths);
		itP = paths.iterator();
		while (itP.hasNext()) {
			Path path = itP.next();
			Stream stream = dataset.parentStream();
			String cbText = path.name();

			JCheckBox cb = new JCheckBox(cbText);
			cb.setActionCommand(path.name());

			if (dataset.indexOfPath(path) >= 0)
				cb.setSelected(true);
			// Blue if unassigned path.
			if (stream.listOfUnassignedPaths().indexOf(path) >= 0)
				cb.setBackground(Color.blue);

			// Red if this exist in any other dataset of the same stream.
			ArrayList<PrimaryDataset> pds = stream.datasets(path);
			for (int i = 0; i < pds.size(); i++) {
				PrimaryDataset ds = pds.get(i);
				if (!ds.equals(dataset)) {
					cb.setBackground(Color.red);
					break;
				}
			}

			cb.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					cbItemStateChanged(e);
				}
			});
			pathCheckBoxes.add(cb);
			jPanelPaths.add(cb);
		}

		updatePathList();

		jScrollPanePaths.setViewportView(jPanelPaths);
		jButtonOk.setText("Ok");
		jButtonCancel.setText("Cancel");
		jScrollPaneList.setViewportView(jListPaths);

		JTextArea unassignedPath = new JTextArea("unassigned path");
		unassignedPath.setBackground(null);
		JTextArea blueBox = new JTextArea(" ");
		blueBox.setBackground(Color.blue);
		JTextArea sharedPath = new JTextArea("already in stream");
		sharedPath.setBackground(null);
		JTextArea redBox = new JTextArea(" ");
		redBox.setBackground(Color.red);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addComponent(jScrollPaneList, javax.swing.GroupLayout.PREFERRED_SIZE, 200,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jScrollPanePaths, javax.swing.GroupLayout.DEFAULT_SIZE,
												(int) (size.getWidth() - 205), Short.MAX_VALUE)
										.addContainerGap())
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup()
												.addComponent(blueBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(unassignedPath, javax.swing.GroupLayout.PREFERRED_SIZE,
														140, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(redBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(sharedPath, javax.swing.GroupLayout.PREFERRED_SIZE, 140,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jButtonCancel)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jButtonOk).addContainerGap()))));

		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { jButtonCancel, jButtonOk });

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jScrollPanePaths, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
								.addComponent(jScrollPaneList, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
						.addGap(8)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonOk).addComponent(jButtonCancel)
								.addComponent(redBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(sharedPath)
								.addComponent(blueBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(unassignedPath))
						.addContainerGap()));

		return jPanel;
	}
}

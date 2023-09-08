package confdb.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.*;
import java.util.Scanner;
import java.util.regex.*;

import confdb.data.*;

/**
 * PrescaleDialog
 * --------------
 * @author Philipp Schieferdecker
 *
 * Edit the prescale table, which is encoded in the configuration of the
 * PrescaleService.
 */
public class PrescaleDialog extends JDialog {
	//
	// member data
	//

	/** reference to the configuration */
	private Configuration config;

	/** GUI components */
	private JTextField jTextFieldFile = new javax.swing.JTextField();
	private JTextField jTextFieldHLT = new javax.swing.JTextField();
	private JComboBox jComboBoxModule = new javax.swing.JComboBox();
	private JButton jButtonOK = new javax.swing.JButton();
	private JButton jButtonApply = new javax.swing.JButton();
	private JCheckBox jCheckBoxOverrideTbl = new javax.swing.JCheckBox();
	private JButton jButtonCancel = new javax.swing.JButton();
	private JTable jTable = new javax.swing.JTable();
	private DefaultComboBoxModel cmbModule;

	/** model for the prescale table */
	private PrescaleTableModel tableModel;

	/** index of the selected column */
	private int iColumn = -1;

	//
	// construction
	//

	/** standard constructor */
	public PrescaleDialog(JFrame jFrame, Configuration config) {
		super(jFrame, true);
		this.config = config;

		tableModel = new PrescaleTableModel();
		tableModel.initialize(config);

		jTable.setModel(tableModel);
		jTable.setDefaultRenderer(Integer.class, new PrescaleTableCellRenderer());
		jTextFieldFile.setText("/home/sharper/hionps.csv");
		jTextFieldHLT.setText(config.toString());

		cmbModule = (DefaultComboBoxModel) jComboBoxModule.getModel();
		cmbModule.removeAllElements();
		cmbModule.addElement(tableModel.defaultName());
		if (tableModel.defaultName() != "")
			cmbModule.addElement("");
		for (int i = 1; i < tableModel.getColumnCount(); ++i) {
			cmbModule.addElement(tableModel.getColumnName(i));
		}
		jComboBoxModule.setSelectedIndex(0);
		// tableModel.setDefaultName((String)jComboBoxModule.getSelectedItem());

		jComboBoxModule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jComboBoxModelActionPerformed(e);
			}
		});
		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		jButtonApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePrescaleService();
				updatePrescaleServiceFromFile(fileName());
			}
		});
		jButtonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePrescaleService();
				updatePrescaleServiceFromFile(fileName());
				setVisible(false);
			}
		});
		jTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				jTableShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				jTableShowPopup(e);
			}
		});

		setTitle("Prescale Editor");
		setContentPane(initComponents());
		adjustTableColumnWidths();
	}

	public String fileName() {
		return jTextFieldFile.getText();
	}

	//
	// private member functions
	//

	/** update the configurations PrescaleService according to table data */
	private void updatePrescaleService() {
		tableModel.updatePrescaleService(config);
	}

	private void updatePrescaleServiceFromFile(String fileName) {
		tableModel.updatePrescaleTableFromFile(fileName);
		tableModel.updatePrescaleService(config);
	}

	/** adjust the width of each table column */
	private void adjustTableColumnWidths() {
		int tableWidth = jTable.getPreferredSize().width;
		int columnCount = jTable.getColumnModel().getColumnCount();
		int headerWidth = (int) (tableWidth * 0.4);
		jTable.getColumnModel().getColumn(0).setPreferredWidth(headerWidth);
		for (int i = 1; i < columnCount; i++) {
			int columnWidth = (tableWidth - headerWidth) / (columnCount - 1);
			jTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth);
		}
	}

	// listener callbacks
	private void jTableShowPopup(MouseEvent e) {
		if (!e.isPopupTrigger())
			return;
		iColumn = jTable.columnAtPoint(e.getPoint());
		JPopupMenu popup = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Add Column");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tableModel.addColumn(iColumn, JOptionPane.showInputDialog("Enter the label for the new column:"), Math
						.abs(Long.decode(JOptionPane.showInputDialog("Enter the prescale value for the new column:"))));
				adjustTableColumnWidths();
			}
		});
		popup.add(menuItem);

		menuItem = new JMenuItem("Duplicate Column");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tableModel.duplicateColumn(iColumn,
						JOptionPane.showInputDialog("Enter the label for the column to be duplicated:"));
				adjustTableColumnWidths();
			}
		});
		popup.add(menuItem);

		menuItem = new JMenuItem("Reorder Columns");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tableModel.reorderColumns(iColumn,
						JOptionPane.showInputDialog("Enter the new order of labels as , separated list:"));
				adjustTableColumnWidths();
			}
		});
		popup.add(menuItem);

		if (iColumn > 0) {
			menuItem = new JMenuItem("Remove Column");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					tableModel.removeColumn(iColumn);
					adjustTableColumnWidths();
				}
			});
			popup.add(menuItem);

			menuItem = new JMenuItem("Rename Column");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					Object label = JOptionPane.showInputDialog(null,
							"Enter the new label for the column \"" + tableModel.getColumnName(iColumn) + "\" :",
							"Rename column", JOptionPane.PLAIN_MESSAGE, null, null, tableModel.getColumnName(iColumn));
					if (label != null) {
						tableModel.renameColumn(iColumn, (String) label);
						adjustTableColumnWidths();
					}
				}
			});
			popup.add(menuItem);
		}
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

	private void jComboBoxModelActionPerformed(ActionEvent e) {
		tableModel.setDefaultName((String) jComboBoxModule.getSelectedItem());

		cmbModule.removeAllElements();
		cmbModule.addElement(tableModel.defaultName());
		if (tableModel.defaultName() != "")
			cmbModule.addElement("");
		for (int i = 1; i < tableModel.getColumnCount(); ++i) {
			cmbModule.addElement(tableModel.getColumnName(i));
		}
	}

	/** initialize GUI components */
	private JPanel initComponents() {
		JPanel jPanel = new JPanel();

		JLabel jLabel1 = new javax.swing.JLabel();
		JLabel jLabel2 = new javax.swing.JLabel();
		JLabel jLabel3 = new javax.swing.JLabel();
		JLabel jLabel4 = new javax.swing.JLabel();

		JScrollPane jScrollPane = new javax.swing.JScrollPane();

		jLabel1.setText("HLT:");
		jLabel2.setText("Default:");
		jLabel3.setText("File:");
		jLabel4.setText("Override PSTbl:");

		jTextFieldFile.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

		jTextFieldHLT.setEditable(false);
		jTextFieldHLT.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

		jComboBoxModule.setEditable(false);
		jComboBoxModule.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

		jScrollPane.setViewportView(jTable);

		jButtonOK.setText("OK");
		jButtonApply.setText("Apply");
		jButtonCancel.setText("Cancel");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 708, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup().addComponent(jLabel1)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jTextFieldHLT, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
								.addGap(18).addComponent(jLabel2)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
										jComboBoxModule, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
						.addGroup(
								layout.createSequentialGroup().addComponent(jLabel3).addGap(18).addComponent(
										jTextFieldFile, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jLabel4).addGap(18)
										.addComponent(
											jCheckBoxOverrideTbl, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addComponent(jButtonCancel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jButtonApply)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jButtonOK)))
				.addContainerGap()));

		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { jButtonApply, jButtonCancel, jButtonOK });

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGap(22)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jTextFieldHLT, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jComboBoxModule, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jTextFieldFile,javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jCheckBoxOverrideTbl,javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonOK).addComponent(jButtonApply).addComponent(jButtonCancel))
						.addContainerGap()));
		return jPanel;
	}

}

//
//PrescaleTableModel
//
class PrescaleTableModel extends AbstractTableModel {
	/** the prescale table data structure */
	protected PrescaleTable prescaleTable;

	public String defaultName() {
		return prescaleTable.defaultName();
	}

	public void setDefaultName(String name) {
		prescaleTable.setDefaultName(name);
		fireTableDataChanged();
	}

	/** update the table model according to configuration's PrescaleService */
	public void initialize(IConfiguration config) {
		prescaleTable = new PrescaleTable(config);
		fireTableDataChanged();
	}

	/** update the table model according to configuration's PrescaleService */
	public void initialize(PrescaleTable prescaleTable) {
		this.prescaleTable = prescaleTable;
		fireTableDataChanged();
	}

	/** update the PrescaleService in configuration according to table data */
	public void updatePrescaleService(IConfiguration config) {
		ServiceInstance prescaleSvc = config.service("PrescaleService");
		if (prescaleSvc == null) {
			System.err.println("No PrescaleService found.");
			return;
		}

		prescaleSvc.updateParameter("lvl1DefaultLabel", "string", prescaleTable.defaultName());

		StringBuffer labelsAsString = new StringBuffer();
		for (int i = 0; i < prescaleTable.prescaleCount(); i++) {
			if (labelsAsString.length() > 0)
				labelsAsString.append(",");
			labelsAsString.append(prescaleTable.prescaleColumnName(i));
		}
		prescaleSvc.updateParameter("lvl1Labels", "vstring", labelsAsString.toString());

		VPSetParameter vpsetPrescaleTable = (VPSetParameter) prescaleSvc.parameter("prescaleTable", "VPSet");
		if (vpsetPrescaleTable == null) {
			System.err.println("No VPSet prescaleTable found.");
			return;
		}
		vpsetPrescaleTable.setValue("");

		for (int iPath = 0; iPath < prescaleTable.pathCount(); iPath++) {
			if (!prescaleTable.isPrescaled(iPath))
				continue;
			String pathName = prescaleTable.pathName(iPath);
			String prescalesAsString = prescaleTable.prescalesAsString(iPath);
			ArrayList<Parameter> params = new ArrayList<Parameter>();
			StringParameter sPathName = new StringParameter("pathName", pathName, true);
			VUInt32Parameter vPrescales = new VUInt32Parameter("prescales", prescalesAsString, true);
			params.add(sPathName);
			params.add(vPrescales);
			vpsetPrescaleTable.addParameterSet(new PSetParameter("", params, true));
		}
		prescaleSvc.setHasChanged();
	}

	public void updatePrescaleTableFromFile(String fileName) {
		if (fileName.equals("")) {
			return;
		}
		System.out.println("Updating PrescaleTable from file: " + fileName);

		String defaultName = new String("");
		ArrayList<String> columnNames = new ArrayList<String>();
		columnNames.clear();
		ArrayList<Long> indices = new ArrayList<Long>();
		indices.clear();
		ArrayList<PrescaleTableRow> prescaleFile = new ArrayList<PrescaleTableRow>();
		prescaleFile.clear();

		// Read Input File
		System.out.println("Reading Input File containing Prescale Table!");
		try {
			Scanner tableScanner = new Scanner(new FileInputStream(fileName), "UTF-8");

			// Header line (csv strings): dummy, followed by prescale column labels
			if (tableScanner.hasNextLine()) {
				Scanner lineScanner = new Scanner(tableScanner.nextLine());
				lineScanner.useDelimiter(",");
				if (lineScanner.hasNext()) {
					defaultName = lineScanner.next().trim();
				}
				while (lineScanner.hasNext()) {
					columnNames.add(lineScanner.next().trim());
				}
			}
			System.out.println(
					"Header / # of prescale columns found in file: " + defaultName + " / " + columnNames.size());
			if (columnNames.size() == 0) {
				System.out.println("No prescale columns found in file - aborting!");
				return;
			}
			while(prescaleTable.prescaleCount() > 0){
				prescaleTable.removePrescaleColumn(1);
			}
			for (int i = 0; i < columnNames.size(); i++) {
					String label = columnNames.get(i);
					prescaleTable.addPrescaleColumn(i, label, 1);
					System.out.println(" i/Label: "+i+"/"+label);
			}
			
			// Indices to map found columnNames into PrescaleTable columnNames
			for (int i = 0; i < columnNames.size(); i++) {
				
				indices.add((long) (prescaleTable.prescaleCount() + 1));
				String label = columnNames.get(i);
				
				for (int j = 0; j < prescaleTable.prescaleCount(); j++) {
					String Label = new String(prescaleTable.prescaleColumnName(j));
					// System.out.println(" j/Label: "+j+"/"+Label);
					if (label.equals(Label)) {
						indices.set(i, (long) j);
					}
				}
				System.out.println("Mapping of input-file label '" + label + "' to PrescaleService is " + i + " => "
						+ indices.get(i));
				if (indices.get(i) == prescaleTable.prescaleCount() + 1) {
					System.out.println(
							"Column name in file not found in PrescaleService config (add there first) - aborting! Label="
									+ label);
					return;
				}
			}

			// Body of prescale table
			while (tableScanner.hasNextLine()) {
				String pathName = new String("");
				ArrayList<Long> prescales = new ArrayList<Long>();
				prescales.clear();
				Scanner lineScanner = new Scanner(tableScanner.nextLine());
				lineScanner.useDelimiter(",");
				if (lineScanner.hasNext()) {
					pathName = lineScanner.next().trim();
					// removeVersion (might be different due to re-versioning)
					pathName = pathName.replaceAll("_v[0-9]+$", "");
				}
				while (lineScanner.hasNext()) {
					prescales.add(Long.valueOf(lineScanner.next().trim()));
				}
				System.out
						.println("Line read with " + prescales.size() + " prescale values for path '" + pathName + "'");
				if (columnNames.size() == prescales.size()) {
					PrescaleTableRow row = new PrescaleTableRow(pathName, prescales);
					prescaleFile.add(row);
				} else {
					System.out.println("Error in input file line (# of columns) - skipping path: " + pathName);
				}
			}
			System.out.println("# of valid path rows found in file: " + prescaleFile.size());
			if (prescaleFile.size() == 0) {
				System.out.println("No valid path rows found in file - aborting!");
				return;
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			System.out.println("Aborting!");
			return;
		}

		// Update PrescaleTable with PrescaleFile
		System.out.println("Updating PrescaleTable with PrescaleFile!");
		String fullName = null;
		for (int i = 0; i < prescaleFile.size(); i++) {
			PrescaleTableRow row = prescaleFile.get(i);
			// check if path is in PrescaleTable and if so, update to full name
			String pathName = row.pathName;
			System.out.println("Searching PrescaleTable for path matching with: " + pathName);
			int found = 0;
			int index = 0;
			for (int j = 0; j < prescaleTable.pathCount(); ++j) {
				if (pathName.equals(prescaleTable.pathName(j).replaceAll("_v[0-9]+$", ""))) {
					found += 1;
					index = j;
					fullName = new String(prescaleTable.pathName(j));
					System.out.println("  Found match with: " + fullName);
				}
			}
			if (found == 0) {
				System.out.println(
						"  No matching path found in PrescaleTable - skipping (requested update of) path: " + pathName);
			} else if (found > 1) {
				System.out.println(
						"  More than one matching path found in PrescaleTable - skipping (requested update of) path: "
								+ pathName);
			} else {
				System.out.println("  Updating prescales of path: " + fullName);
				for (int j = 0; j < row.prescales.size(); j++) {
					int k = (int) ((long) indices.get(j));
					Long prescale = row.prescales.get(j);
					prescaleTable.setPrescale(index, k, prescale);
				}
				fireTableDataChanged();
			}
		}
	}

	/** add an additional column (-> lvl1Label) */
	public void addColumn(int i, String lvl1Label, long prescale) {
		prescaleTable.addPrescaleColumn(i, lvl1Label, prescale);
		fireTableStructureChanged();
	}

	public void reorderColumns(int i, String lvl1Labels) {
		if (lvl1Labels == null)
			return;
		String[] split = lvl1Labels.replace(" ", "").split(",");
		ArrayList<String> newOrder = new ArrayList<String>();
		for (int il = 0; il < split.length; il++) {
			newOrder.add(split[il]);
		}
		prescaleTable.reorderPrescaleColumns(newOrder);
		fireTableStructureChanged();
		fireTableDataChanged();
	}

	public void renameColumn(int i, String lvl1Label) {
		prescaleTable.renamePrescaleColumn(i, lvl1Label);
		fireTableStructureChanged();
		fireTableDataChanged();
	}

	public void duplicateColumn(int i, String lvl1Label) {
		prescaleTable.duplicatePrescaleColumn(i, lvl1Label);
		fireTableStructureChanged();
		fireTableDataChanged();
	}

	/** remove a column of prescales */
	public void removeColumn(int i) {
		prescaleTable.removePrescaleColumn(i);
		fireTableStructureChanged();
	}

	/** number of columns */
	public int getColumnCount() {
		return prescaleTable.prescaleCount() + 1;
	}

	/** number of rows */
	public int getRowCount() {
		return prescaleTable.pathCount();
	}

	/** get column name for column 'col' */
	public String getColumnName(int col) {
		return (col == 0) ? "Path" : prescaleTable.prescaleColumnName(col - 1);
	}

	/** get the value for row,col */
	public Object getValueAt(int row, int col) {
		return (col == 0) ? prescaleTable.pathName(row) : prescaleTable.prescale(row, col - 1);
	}

	/** get the class of the column 'c' */
	public Class getColumnClass(int c) {
		return (c == 0) ? String.class : Integer.class;
	}

	/** is a cell editable or not? */
	public boolean isCellEditable(int row, int col) {
		return col > 0;
	}

	/** set the value of a table cell */
	public void setValueAt(Object value, int row, int col) {
		prescaleTable.setPrescale(row, col - 1, (Integer) value);

		fireTableDataChanged(); // Fire event to alert a listener at ConfDbGUI.java (bug 89524).
	}

	/** check if a certain path is already in the list of rows */
	private boolean rowsContainPath(String pathName) {
		for (int iPath = 0; iPath < prescaleTable.pathCount(); iPath++)
			if (pathName.equals(prescaleTable.pathName(iPath)))
				return true;
		return false;
	}
}

//
// PrescaleTableCellRenderer
//
class PrescaleTableCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		setText(value.toString());
		if (value instanceof Long) {
			setHorizontalAlignment(SwingConstants.CENTER);
			long valueAsLong = (Long) value;
			if (valueAsLong == 0)
				setBackground(Color.RED);
			else if (valueAsLong == 1)
				setBackground(Color.GREEN);
			else
				setBackground(Color.ORANGE);
		}
		return this;
	}
}

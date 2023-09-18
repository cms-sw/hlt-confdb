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
		jTextFieldFile.setText("");
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
				clearPrescaleFileSettings();
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

	public void clearPrescaleFileSettings(){
		jTextFieldFile.setText("");
		jCheckBoxOverrideTbl.setSelected(false);
	}
	//
	// private member functions
	//

	/** update the configurations PrescaleService according to table data */
	private void updatePrescaleService() {
		tableModel.updatePrescaleService(config);
	}

	private void updatePrescaleServiceFromFile(String fileName) {
		tableModel.updatePrescaleTableFromFile(fileName,jCheckBoxOverrideTbl.isSelected());
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
		jLabel4.setText("Override Prescale Table:");

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

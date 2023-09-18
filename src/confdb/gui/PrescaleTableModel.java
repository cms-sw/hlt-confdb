package confdb.gui;

import java.util.ArrayList;

import java.io.*;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.*;

import confdb.data.*;

//
//PrescaleTableModel
//


public class PrescaleTableModel extends AbstractTableModel {
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

	public boolean updatePrescaleTableFromFile(String fileName,boolean overrideTbl) {
		if (fileName.equals("")) {
			return false;
		}
		System.out.println("Updating PrescaleTable from file: " + fileName);

		String defaultName = new String("");
		ArrayList<String> columnNames = new ArrayList<String>();
		columnNames.clear();
		ArrayList<Long> indices = new ArrayList<Long>();
		indices.clear();
		ArrayList<PrescaleTableRow> prescaleFile = new ArrayList<PrescaleTableRow>();
		prescaleFile.clear();

		ArrayList<String> skippedPaths = new ArrayList<String>();

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
				return false;
			}
			if(overrideTbl){
				System.out.println("Overriding PrescaleTable with Columns from the file");
				while(prescaleTable.prescaleCount() > 0){
					prescaleTable.removePrescaleColumn(1);
				}
				for (int i = 0; i < columnNames.size(); i++) {
					String label = columnNames.get(i);
					prescaleTable.addPrescaleColumn(i, label, 1);
					System.out.println(" i/Label: "+i+"/"+label);
				}
				fireTableStructureChanged();
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
							"Column name in file not found in PrescaleService config (add there first or use override) - aborting! Label="
									+ label);					
					return false;
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
					skippedPaths.add("Error in input file line (# of columns) - skipping path: " + pathName);
				}
			}
			System.out.println("# of valid path rows found in file: " + prescaleFile.size());
			if (prescaleFile.size() == 0) {
				System.out.println("No valid path rows found in file - aborting!");				
				return false;
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			System.out.println("Aborting!");			
			return false;
		}

		// Update PrescaleTable with PrescaleFile
		System.out.println("Updating PrescaleTable with PrescaleFile!");
		String fullName = null;
		for (int i = 0; i < prescaleFile.size(); i++) {
			PrescaleTableRow row = prescaleFile.get(i);
			// check if path is in PrescaleTable and if so, update to full name
			String pathName = row.pathName;
			//System.out.println("Searching PrescaleTable for path matching with: " + pathName);
			int found = 0;
			int index = 0;
			for (int j = 0; j < prescaleTable.pathCount(); ++j) {
				if (pathName.equals(prescaleTable.pathName(j).replaceAll("_v[0-9]+$", ""))) {
					found += 1;
					index = j;
					fullName = new String(prescaleTable.pathName(j));
					//System.out.println("  Found match with: " + fullName);
				}
			}
			if (found == 0) {
				System.out.println(
						"  No matching path found in PrescaleTable - skipping (requested update of) path: " + pathName);
				skippedPaths.add(pathName+" is not known, possible typo?");
			} else if (found > 1) {
				System.out.println(
						"  More than one matching path found in PrescaleTable - skipping (requested update of) path: "
								+ pathName);
				skippedPaths.add(pathName+" has multiple entries in input file");
			} else {
				//System.out.println("  Updating prescales of path: " + fullName);
				for (int j = 0; j < row.prescales.size(); j++) {
					int k = (int) ((long) indices.get(j));
					Long prescale = row.prescales.get(j);
					prescaleTable.setPrescale(index, k, prescale);
				}
				fireTableDataChanged();
			}
		}	
		//eh, probably not great and probably should be moved to the calling PrescaleDialog
		if (!skippedPaths.isEmpty()) {
			String msg = "The input prescale file had the following errors and must be fixed before uploading can occur\nPlease note when dealing with path names, we ignore version numbers";
			for(String error : skippedPaths){
				msg += "\n"+error;
			}
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			textArea.setColumns(80);
			JOptionPane.showMessageDialog(null,scrollPane, "Invalid Prescale File", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
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

package confdb.gui;

import javax.swing.*;
import javax.swing.table.*;

import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;

/**
 * ConfigVersionTableModel
 * -----------------------
 * @author Philipp Schieferdecker
 *
 * Display all available versions for a selected configuration.
 */
public class ConfigVersionTableModel extends AbstractTableModel {
	//
	// member data
	//

	/** column names */
	private String[] columnNames = { "version", "created", "creator", "releaseTag" };

	/** configuration info object to be displayed */
	private ConfigInfo configInfo = null;

	/** fix releaseTag */
	private String releaseTag = new String();

	//
	// member functions
	//

	/** set the configInfo to be displayed */
	public void setConfigInfo(ConfigInfo configInfo) {
		this.configInfo = configInfo;
		fireTableDataChanged();
	}

	/** fix releaseTag */
	public void fixReleaseTag(String releaseTag) {
		this.releaseTag = releaseTag;
		fireTableDataChanged();
	}

	/** number of columns */
	public int getColumnCount() {
		return columnNames.length;
	}

	/** number of rows */
	public int getRowCount() {
		return (configInfo != null) ? configInfo.versionCount(releaseTag) : 0;
	}

	/** get column name for colimn 'col' */
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/** get the value for row,col */
	public Object getValueAt(int row, int col) {
		if (configInfo == null)
			return null;

		ConfigVersion cVersion = configInfo.version(releaseTag, row);
		switch (col) {
		case 0:
			return new Integer(cVersion.version());
		case 1:
			return cVersion.created();
		case 2:
			return cVersion.creator();
		case 3:
			return cVersion.releaseTag();
		}
		return null;
	}

	/** get the class of the column 'c' */
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/** is a cell editable or not? */
	public boolean isCellEditable(int row, int col) {
		return false;
	}
}

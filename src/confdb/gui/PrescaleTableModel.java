package confdb.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

import confdb.data.*;


/**
 * PrescaleTableModel
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class PrescaleTableModel extends AbstractTableModel
{
    /** precale column labels */
    private ArrayList<String> columnNames = new ArrayList<String>();
    
    /** paths and prescales */
    private HashMap<String,ArrayList<Integer>> pathToPrescales =
	new HashMap<String,ArrayList<Integer>>();
    
    /** update the table model according to configuration's PrescaleService */
    public void update(IConfiguration config)
    {
	columnNames.clear();
	pathToPrescales.clear();
	
	ServiceInstance prescaleSvc = config.service("PrescaleService");
	if (prescaleSvc==null) return;
	
	VStringParameter vColumnNames =
	    (VStringParameter)prescaleSvc.parameter("vstring","lvl1Labels");
	if (vColumnNames==null) return;

	VPSetParameter vpsetPrescaleTable =
	    (VPSetParameter)prescaleSvc.parameter("VPSet","prescaleTable");
	if (vpsetPrescaleTable==null) return;

	for (int i=0;i<vColumnNames.vectorSize();i++)
	    columnNames.add((String)vColumnNames.value(i));
	
	for (int i=0;i<vpsetPrescaleTable.parameterSetCount();i++) {
	    PSetParameter pset = vpsetPrescaleTable.parameterSet(i);
	    StringParameter  sPathName =(StringParameter)pset.parameter("pathName");
	    VUInt32Parameter vPrescales=(VUInt32Parameter)pset.parameter("prescales");
	    if (vPrescales.vectorSize()!=columnNames.size()) continue;
	    ArrayList<Integer> prescales = new ArrayList<Integer>();
	    for (int ii=0;ii<vPrescales.vectorSize();ii++)
		prescales.add((Integer)vPrescales.value(ii));
	    pathToPrescales.put((String)sPathName.value(),prescales);
	}
	
	Iterator<Path> itP = config.pathIterator();
	while (itP.hasNext()) {
	    Path path = itP.next();
	    if (pathToPrescales.containsKey(path.name())) continue;
	    ArrayList<Integer> prescales = new ArrayList<Integer>();
	    for (int i=0;i<columnNames.size();i++) prescales.add(1);
	    pathToPrescales.put(path.name(),prescales);
	}

	fireTableDataChanged();
    }

    /** number of columns */
    public int getColumnCount() { return columnNames.size(); }
    
    /** number of rows */
    public int getRowCount() { return pathToPrescales.size(); }
    
    /** get column name for colimn 'col' */
    public String getColumnName(int col) { return columnNames.get(col); }
    
    /** get the value for row,col */
    public Object getValueAt(int row, int col) { return new Integer(10); }
    
    /** get the class of the column 'c' */
    public Class getColumnClass(int c) { return (c==0) ? String.class : Integer.class; }
    
    /** is a cell editable or not? */
    public boolean isCellEditable(int row, int col) { return col>0; }

    /** set the value of a table cell */
    public void setValueAt(Object value,int row, int col)
    {
	
    }
}


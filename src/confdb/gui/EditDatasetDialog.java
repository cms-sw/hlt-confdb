package confdb.gui;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

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
public class EditDatasetDialog extends JDialog
{
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
    JList   jListPaths    = new JList();
    JButton jButtonOk     = new javax.swing.JButton();
    JButton jButtonCancel = new javax.swing.JButton();
    

    //
    // construction
    //
    public EditDatasetDialog(JFrame frame,
			     Configuration config,
			     PrimaryDataset dataset)
    {
	super(frame,true);
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
    private void updatePathList()
    {
	DefaultListModel lm = (DefaultListModel)(jListPaths.getModel());
	lm.removeAllElements();
	Iterator<JCheckBox> itCB = pathCheckBoxes.iterator();
	while (itCB.hasNext()) {
	    JCheckBox cb = itCB.next();
	    if (cb.isSelected()) {
		String pathName = cb.getActionCommand();
		if (dataset.path(pathName)==null)
		    pathName="<html><font color=#ff0000>" + pathName + "</font></html>";
		lm.addElement(pathName);
	    }
	}
    }
    
    /** ItemListener for path check-boxes */
    private void cbItemStateChanged(ItemEvent e)
    {
	updatePathList();
    }

    /** ActionListener for OK button */
    private void jButtonOkActionPerformed(ActionEvent e)
    {
	ArrayList<String> pathNames = new ArrayList<String>();
	Iterator<JCheckBox> itCB = pathCheckBoxes.iterator();
	
	while (itCB.hasNext()) {
	    JCheckBox cb = itCB.next();
	    if (cb.isSelected()) pathNames.add(cb.getActionCommand());
	}
	dataset.clear();
	Iterator<String> itS = pathNames.iterator();
	while (itS.hasNext())
	    dataset.insertPath(config.path(itS.next()));
	setVisible(false);	
    }

    /** initizlize GUI components */
    private JPanel initComponents(Dimension size)
    {
	JPanel      jPanel           = new JPanel();
        JScrollPane jScrollPanePaths = new JScrollPane();
        JScrollPane jScrollPaneList  = new JScrollPane();
        JPanel      jPanelPaths      = new JPanel();

	jListPaths.setModel(new DefaultListModel());
	
	jPanelPaths.setLayout(new GridLayout(0,3));
	ArrayList<Path> paths = new ArrayList<Path>();
	Iterator<Path> itP = config.pathIterator();
	while (itP.hasNext()) {
	    Path path = itP.next();
	    if (!path.isEndPath()) paths.add(path);
	}
	Collections.sort(paths);
	itP = paths.iterator();
	while (itP.hasNext()) {
	    Path   path    = itP.next();
	    Stream stream = dataset.parentStream();
	    String cbText = path.name();
	    if (stream.listOfUnassignedPaths().indexOf(path)>=0)
		cbText = "<html><b>"+cbText+"</b></html>";
	    JCheckBox cb = new JCheckBox(cbText);
	    cb.setActionCommand(path.name());
	    /*
	    if (dataset.indexOfPath(path)>=0) cb.setSelected(true);
	    else if (stream.listOfAssignedPaths().indexOf(path)>=0)
	    	cb.setEnabled(false);
    	*/
	    
	    if (dataset.indexOfPath(path)>=0) cb.setSelected(true);
	    
	    // Red if this exist in any other dataset of the same stream.
	    ArrayList<PrimaryDataset> pds = stream.datasets(path);
    	for(int i = 0; i < pds.size(); i++){
    		PrimaryDataset ds = pds.get(i);
    		if(!ds.equals(dataset)) {
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


	org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel);
	jPanel.setLayout(layout);
        layout.setHorizontalGroup(
				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				  .add(layout.createSequentialGroup()
				       .addContainerGap()
				       .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					    .add(layout.createSequentialGroup()
						 .add(jScrollPaneList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						 .add(jScrollPanePaths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, (int)(size.getWidth()-205), Short.MAX_VALUE)
						 .addContainerGap())
					    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
						 .add(jButtonCancel)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						 .add(jButtonOk))))
				  );
	
        layout.linkSize(new java.awt.Component[] {jButtonCancel, jButtonOk}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
	
        layout.setVerticalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
				     .addContainerGap()
				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
					  .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPanePaths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
					  .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPaneList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
				     .add(8, 8, 8)
				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
					  .add(jButtonOk)
					  .add(jButtonCancel)))
				);
	
	return jPanel;
    }
}

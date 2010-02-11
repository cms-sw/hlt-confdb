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
		String pathName = cb.getText();
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
	    if (cb.isSelected()) pathNames.add(cb.getText());
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
	    Path path = itP.next();
	    JCheckBox cb = new JCheckBox(path.name());
	    if (dataset.indexOfPath(path)>=0) cb.setSelected(true);
	    else if (dataset.parentStream().listOfAssignedPaths().indexOf(path)>=0)
		cb.setEnabled(false);
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



        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
				  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				  .addGroup(layout.createSequentialGroup()
					    .addComponent(jScrollPaneList, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
					    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					    .addComponent(jScrollPanePaths, javax.swing.GroupLayout.DEFAULT_SIZE, (int)(size.getWidth()-205), Short.MAX_VALUE))
				  .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
					    .addContainerGap(407, Short.MAX_VALUE)
					    .addComponent(jButtonCancel)
					    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					    .addComponent(jButtonOk)
					    .addContainerGap())
				  );
	
        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonCancel, jButtonOk});
	
        layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
					  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
						    .addComponent(jScrollPanePaths, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
						    .addComponent(jScrollPaneList, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
					  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						    .addComponent(jButtonOk)
						    .addComponent(jButtonCancel)))
				);
	
	return jPanel;
    }
}	

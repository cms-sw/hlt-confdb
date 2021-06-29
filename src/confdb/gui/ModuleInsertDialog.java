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
import confdb.gui.*;
/**
 * ModuleInsertDialog
 * --------------
 * @author Sam Harper, using Philipp Schieferdecker as a guide
 *
 * Module insert dialog box
 */
public class ModuleInsertDialog extends JDialog {
	//
	// member data
	//

	/** reference to the configuration */
	private JTree tree;

	/** GUI components */
	private JComboBox jComboBoxModule = new javax.swing.JComboBox();
	private JButton jButtonOK = new javax.swing.JButton();
	private JButton jButtonCancel = new javax.swing.JButton();
	
	
	/** standard constructor */
	public ModuleInsertDialog(JFrame jFrame, JTree tree) {
		super(jFrame, "Module Inserter",true);
		this.tree = tree;

		
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
		jButtonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButtonActionPerformed(e);               
				setVisible(false);
			}
		});
		
		setContentPane(initComponents());
		
	}


	//
	// private member functions
	//

    private void okButtonActionPerformed(ActionEvent e) {
        ConfigurationTreeActions.insertReference(tree, "Module", (String) jComboBoxModule.getSelectedItem());
        System.err.println("clicked: "+jComboBoxModule.getSelectedItem());
        System.err.println("action: "+e);
    }

	private void jComboBoxModelActionPerformed(ActionEvent e) {
        System.err.println(jComboBoxModule.getSelectedItem());
	}

	/** initialize GUI components */
	private JPanel initComponents() {
		JPanel jPanel = new JPanel();

        jPanel.add(jComboBoxModule);
        jPanel.add(jButtonOK);
        jPanel.add(jButtonCancel);

        setComboBox();
		jButtonOK.setText("OK");		
		jButtonCancel.setText("Cancel");

		return jPanel;
	}

    private void setComboBox() {     
        ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
        Configuration config = (Configuration) model.getRoot(); 
		SoftwareRelease release = config.release();
		Iterator<ModuleTemplate> it = release.moduleTemplateIterator();        
        jComboBoxModule.removeAllItems();
		while (it.hasNext()) {
			ModuleTemplate t = it.next();
			String moduleType = t.type();
			if (moduleType.equals("OutputModule"))
				continue;
            jComboBoxModule.addItem(t.name());
        }
        jComboBoxModule.setEditable(true);
		jComboBoxModule.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
		AutoCompletion.enable(jComboBoxModule);
    }

}

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
    private JRadioButton jButtonNew = new JRadioButton("new instance");
    private JRadioButton jButtonExisting = new JRadioButton("existing instance");
    private JRadioButton jButtonClone = new JRadioButton("clone");


    private boolean existingModules = false;
	
	
	/** standard constructor */
	public ModuleInsertDialog(JFrame jFrame, JTree tree) {
		super(jFrame, "Module Inserter",true);
		this.tree = tree;
        this.existingModules = existingModules;

		
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
        
        jButtonNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newButtonActionPerformed(e);               				
			}
		});
        jButtonNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newButtonActionPerformed(e);               				
			}
		});
        jButtonExisting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				existingButtonActionPerformed(e);               
			}
		});
        jButtonClone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cloneButtonActionPerformed(e);               
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
    private void newButtonActionPerformed(ActionEvent e) {
        setComboBoxEntriesNewMod();
        System.err.println("newButton: ");
        System.err.println("action: "+e);
    }
    private void existingButtonActionPerformed(ActionEvent e) {
        System.err.println("existingButton: ");
        System.err.println("action: "+e);
        setComboBoxEntriesExistingMod(true);
    }
    private void cloneButtonActionPerformed(ActionEvent e) {
        System.err.println("cloneButton: ");
        System.err.println("action: "+e);
        setComboBoxEntriesExistingMod(true);
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
        jPanel.add(jButtonNew);
        jPanel.add(jButtonExisting);
        jPanel.add(jButtonClone);
        
        
        ButtonGroup addGroup  = new ButtonGroup();
        addGroup.add(jButtonNew);
        addGroup.add(jButtonExisting);
        addGroup.add(jButtonClone);
        
        initComboBox();
        setComboBoxEntriesExistingMod(true);
        jButtonExisting.setEnabled(true);
        jButtonOK.setText("OK");		
		jButtonCancel.setText("Cancel");

		return jPanel;
	}

    private void initComboBox() {     
        jComboBoxModule.setEditable(true);
		jComboBoxModule.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
		AutoCompletion.enable(jComboBoxModule);
    }
    private void setComboBoxEntriesNewMod() {     
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
    }

    private void setComboBoxEntriesExistingMod(boolean prefixType) {     
        ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
        Configuration config = (Configuration) model.getRoot(); 
        SoftwareRelease release = config.release();
		Iterator<ModuleInstance> it = config.moduleIterator();        
            
        jComboBoxModule.removeAllItems();
		while (it.hasNext()) {
			ModuleInstance t = it.next();
            if(prefixType) {
                if(t.template()!=null){
                    jComboBoxModule.addItem(t.template().name()+":"+t.name());
                }else{
                    jComboBoxModule.addItem("NULL:"+t.name());
                }
            }else{
                jComboBoxModule.addItem(t.name());  
            }
			
        }
    }
}

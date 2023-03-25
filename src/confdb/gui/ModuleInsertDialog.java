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
import java.util.Collections;
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


    private boolean clone = false;
    private Boolean existingMods = null;
	
	
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
        if(clone){
            ConfigurationTreeActions.insertReference(tree, "Module", "copy:"+(String) jComboBoxModule.getSelectedItem());
        }else{
            ConfigurationTreeActions.insertReference(tree, "Module", (String) jComboBoxModule.getSelectedItem());
        }
    }
    private void newButtonActionPerformed(ActionEvent e) {
        clone = false;
        setComboBoxEntriesNewMod();
    }
    private void existingButtonActionPerformed(ActionEvent e) {
        clone = false;
        setComboBoxEntriesExistingMod(true);
    }
    private void cloneButtonActionPerformed(ActionEvent e) {
        clone = true;
        setComboBoxEntriesExistingMod(true);
    }
    

	private void jComboBoxModelActionPerformed(ActionEvent e) {
        jComboBoxModule.getSelectedItem();
	}

	/** initialize GUI components */
	private JPanel initComponents() {
		JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;

        jPanel.add(jComboBoxModule,constraints);
        constraints.gridx = 1;
        jPanel.add(jButtonNew);
        constraints.gridx = 2;
        jPanel.add(jButtonExisting,constraints);
        constraints.gridx = 3;
        jPanel.add(jButtonClone,constraints);
        constraints.gridx = 1;
        constraints.gridy = 1;
        jPanel.add(jButtonOK,constraints);
        constraints.gridx = 2;
        jPanel.add(jButtonCancel,constraints);
        
        
        ButtonGroup addGroup  = new ButtonGroup();
        addGroup.add(jButtonNew);
        addGroup.add(jButtonExisting);
        addGroup.add(jButtonClone);
        
        initComboBox();
        setComboBoxEntriesNewMod();
        jButtonNew.setSelected(true);
        jButtonOK.setText("OK");		
		jButtonCancel.setText("Cancel");

		return jPanel;
	}

    private void initComboBox() {     
        jComboBoxModule.setEditable(true);
		jComboBoxModule.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
		jComboBoxModule.setPreferredSize(new Dimension(800,jComboBoxModule.getPreferredSize().height));
        AutoCompletion.enable(jComboBoxModule);
    }
    private void setComboBoxEntriesNewMod() {     
        //if allready set, dont reset
        if(existingMods!=null && existingMods==false) {
            return;
        }
        existingMods = false;
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
        //if allready set, dont reset  
        if(existingMods!=null && existingMods==true) {
            return;
        }
        existingMods = true;
        ConfigurationTreeModel model = (ConfigurationTreeModel) tree.getModel();
        Configuration config = (Configuration) model.getRoot(); 
        SoftwareRelease release = config.release();
        Iterator<ModuleInstance> it = config.moduleIterator();
        jComboBoxModule.removeAllItems();
        ArrayList<String> items = new ArrayList<String>();
        while (it.hasNext()) {
            ModuleInstance t = it.next();
            if (prefixType) {
                if (t.template() != null) {
                    items.add(t.template().name() + ":" + t.name());
                } else {
                    items.add("NULL:" + t.name());
                }
            } else {
                items.add(t.name());
            }
        }
        Collections.sort(items);
        for(int idx=0;idx<items.size();idx++){
            jComboBoxModule.addItem(items.get(idx));
        }
    }
}

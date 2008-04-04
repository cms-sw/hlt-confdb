package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.data.*;


/**
 * OutputModuleDialog
 * ------------------
 * @author Philipp Schieferdecker
 *
 * Editor to help set the SelectEvents and outputCommand parameters of
 * OutputModules.
 */
public class OutputModuleDialog extends JDialog
{
    //
    // member data
    //

    /** reference to the configuration */
    private Configuration config;

    /** current output module being edited */
    private ModuleInstance outputModule = null;

    /** GUI components */
    private JComboBox   jComboBoxOutputModule = new javax.swing.JComboBox();
    private JButton     jButtonAddPaths       = new javax.swing.JButton();
    private JComboBox   jComboBoxAddPaths     = new javax.swing.JComboBox();
    private JList       jListPaths            = new javax.swing.JList();
    private JTextField  jTextFieldSearch      = new javax.swing.JTextField();
    private JTable      jTableModules         = new javax.swing.JTable();
    private JButton     jButtonCancel         = new javax.swing.JButton();
    private JButton     jButtonApply          = new javax.swing.JButton();
    private JButton     jButtonOK             = new javax.swing.JButton();

    
    //
    // construction
    //
    
    /** standard constructor */
    public OutputModuleDialog(JFrame jFrame, Configuration config)
    {
	super(jFrame,true);
	this.config = config;

	DefaultComboBoxModel m=(DefaultComboBoxModel)jComboBoxOutputModule.getModel();
	m.removeAllElements();
	m.addElement("");
	Iterator<ModuleInstance> itM = config.moduleIterator();
	while (itM.hasNext()) {
	    ModuleInstance module = itM.next();
	    if (module.template().type().equals("OutputModule"))
		m.addElement(module.name());
	}
	
	jListPaths.setModel(new DefaultListModel());
	
	// register action listeners
	jComboBoxOutputModule.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxOutputModuleActionPerformed(e);
		}
	    });
	jButtonAddPaths.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonAddPathsActionPerformed(e);
		}
	    });
	jComboBoxAddPaths.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxAddPathsActionPerformed(e);
		}
	    });
	jListPaths.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    jListPathsMousePressed(e);
		}
	    });
	jTextFieldSearch.getDocument()
	    .addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jTextFieldSearchInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jTextFieldSearchRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});
	jButtonCancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonCancelActionPerformed(e);
		}
	    });
	jButtonApply.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonApplyActionPerformed(e);
		}
	    });
	jButtonOK.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonOKActionPerformed(e);
		}
	    });
	
	setTitle("OutputModule Editor");
	setContentPane(initComponents());
    }
    

    //
    // member functions
    //

    /** set the output module to be edited */
    public void setOutputModule(String label)
    {
	outputModule = config.module(label);
	DefaultListModel m = (DefaultListModel)jListPaths.getModel();
	m.removeAllElements();
	
	if (outputModule==null) return;
	
	PSetParameter psetSelectEvents =
	    (PSetParameter)outputModule.parameter("SelectEvents","PSet");
	VStringParameter vsSelectEvents =
	    (VStringParameter)psetSelectEvents.parameter("SelectEvents");
	
	if (vsSelectEvents==null||vsSelectEvents.vectorSize()==0) {
	    Iterator<Path> itP=config.pathIterator();
	    while (itP.hasNext()) m.addElement(itP.next().name());
	}
	else {
	    for (int i=0;i<vsSelectEvents.vectorSize();i++) {
		String pathName = (String)vsSelectEvents.value(i);
		Path   path     = config.path(pathName);
		if (path!=null&&!path.isEndPath()) m.addElement(pathName);
		else System.err.println("invalid path '"+pathName+"'");
	    }
	}
	
    }
    
    
    //
    // private member functions
    //
    
    // listener callbacks
    public void jComboBoxOutputModuleActionPerformed(ActionEvent e)
    {
	JComboBox jComboBox  = (JComboBox)e.getSource();
	String    name       = (String)jComboBox.getSelectedItem();
	setOutputModule(name);

    }
    public void jButtonAddPathsActionPerformed(ActionEvent e)
    {
	
    }
    public void jComboBoxAddPathsActionPerformed(ActionEvent e)
    {
	
    }
    public void jListPathsMousePressed(MouseEvent e)
    {

    }
    public void jTextFieldSearchInsertUpdate(DocumentEvent e)
    {

    }
    public void jTextFieldSearchRemoveUpdate(DocumentEvent e)
    {
	
    }
    public void jButtonCancelActionPerformed(ActionEvent e)
    {
	setVisible(false);
    }
    public void jButtonApplyActionPerformed(ActionEvent e)
    {
	
    }
    public void jButtonOKActionPerformed(ActionEvent e)
    {

    }
    
    
    /** init GUI components */
    private JPanel initComponents()
    {
	JPanel jPanel = new JPanel();
	
        JLabel      jLabel1 = new javax.swing.JLabel();
        JLabel      jLabel3 = new javax.swing.JLabel();
        JLabel      jLabel4 = new javax.swing.JLabel();
        JLabel      jLabel5 = new javax.swing.JLabel();
        JLabel      jLabel6 = new javax.swing.JLabel();

	JSplitPane  jSplitPane = new javax.swing.JSplitPane();
        JPanel      jPanelPaths = new javax.swing.JPanel();
        JPanel      jPanelModules = new javax.swing.JPanel();
        JScrollPane jScrollPanePaths = new javax.swing.JScrollPane();
        JScrollPane jScrollPaneModules = new javax.swing.JScrollPane();



        jSplitPane.setDividerLocation(280);
        jSplitPane.setResizeWeight(0.5);

        jComboBoxAddPaths.setEditable(true);

        jScrollPanePaths.setViewportView(jListPaths);

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 16));
        jLabel5.setText("Selected Paths:");

        jButtonAddPaths.setText("Add");
	
        org.jdesktop.layout.GroupLayout jPanelPathsLayout = new org.jdesktop.layout.GroupLayout(jPanelPaths);
        jPanelPaths.setLayout(jPanelPathsLayout);
        jPanelPathsLayout.setHorizontalGroup(
					     jPanelPathsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelPathsLayout.createSequentialGroup()
                .add(6, 6, 6)
                .add(jPanelPathsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelPathsLayout.createSequentialGroup()
                        .add(jButtonAddPaths)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxAddPaths, 0, 223, Short.MAX_VALUE))
                    .add(jPanelPathsLayout.createSequentialGroup()
                        .add(jLabel5)
                        .addContainerGap(164, Short.MAX_VALUE))))
            .add(jScrollPanePaths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
        );
        jPanelPathsLayout.setVerticalGroup(
            jPanelPathsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelPathsLayout.createSequentialGroup()
                .add(13, 13, 13)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelPathsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxAddPaths, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonAddPaths))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPanePaths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE))
        );

        jPanelPathsLayout.linkSize(new java.awt.Component[] {jButtonAddPaths, jComboBoxAddPaths}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jSplitPane.setLeftComponent(jPanelPaths);

        jLabel3.setText("C");

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 12));
        jLabel4.setText("Search:");

        jScrollPaneModules.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPaneModules.setViewportView(jTableModules);
	
        jLabel6.setFont(new java.awt.Font("Dialog", 1, 16));
        jLabel6.setText("Products:");

        org.jdesktop.layout.GroupLayout jPanelModulesLayout = new org.jdesktop.layout.GroupLayout(jPanelModules);
        jPanelModules.setLayout(jPanelModulesLayout);
        jPanelModulesLayout.setHorizontalGroup(
            jPanelModulesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelModulesLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addContainerGap(369, Short.MAX_VALUE))
            .add(jPanelModulesLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(jScrollPaneModules, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
        );
        jPanelModulesLayout.setVerticalGroup(
            jPanelModulesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelModulesLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelModulesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPaneModules, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE))
        );

        jSplitPane.setRightComponent(jPanelModules);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
        jLabel1.setText("OutputModule:");

        jComboBoxOutputModule.setBackground(new java.awt.Color(255, 255, 255));
	
        jButtonCancel.setText("Cancel");
        jButtonApply.setText("Apply");
        jButtonOK.setText("OK");
	
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxOutputModule, 0, 656, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jButtonCancel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonApply)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonOK)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jButtonApply, jButtonCancel, jButtonOK}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jComboBoxOutputModule, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonOK)
                    .add(jButtonApply)
                    .add(jButtonCancel))
                .addContainerGap())
        );

	return jPanel;
    }
}

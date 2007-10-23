package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import org.jdesktop.layout.*;

import java.util.EventObject;

import confdb.data.Directory;
import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;

import confdb.db.ConfDB;

    
/**
 * OpenConfigurationDialog
 * -----------------------
 * @author Philipp Schieferdecker
 *
 */
public class OpenConfigurationDialog extends ConfigurationDialog
{
    //
    // member data
    //

    /** configuration info */
    private ConfigInfo configInfo = null;
    
    /** configuration version table */
    private JTree   jTreeDirectories = null;
    private JTable  jTableConfig     = null;
    private JButton jButtonOk        = new JButton();
    private JButton jButtonCancel    = new JButton();

    /** configuration version table-model */
    private ConfigVersionTableModel configTableModel = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public OpenConfigurationDialog(JFrame frame,ConfDB database)
    {
	super(frame,database);
	setTitle("Open Configuration");
	setContentPane(initComponents());
	addTreeSelectionListener(new OpenConfigTreeSelListener());
    }
	

    //
    // member functions
    //

    /** retrieve configuration information */
    public ConfigInfo configInfo() { return configInfo; }

    /** OK button callback */
    public void jButtonOkActionPerformed(ActionEvent evt)
    {
	validChoice = false;
	if (configInfo!=null) validChoice = true;
	setVisible(false);
    }

    /** Cancel button callback */
    public void jButtonCancelActionPerformed(ActionEvent evt)
    {
	validChoice = false;
	setVisible(false);
    }

    
    //
    // classes
    //
    
    /**
     * OpenConfigListSelListener
     * -------------------------
     * @author Philipp Schieferdecker
     */
    public class OpenConfigListSelListener implements ListSelectionListener
    {
	/** ListSelectionListener: valueChanged() */
	public void valueChanged(ListSelectionEvent ev)
	{
	    ConfigInfo configInfo = OpenConfigurationDialog.this.configInfo();
	    if (ev.getValueIsAdjusting()||configInfo==null) return;
	    ListSelectionModel lsm = (ListSelectionModel)ev.getSource();
	    if (!lsm.isSelectionEmpty()) {
		configInfo.setVersionIndex(lsm.getMinSelectionIndex());
	    }
	}
    }
    

    /**
     * OpenConfigTreeSelListener
     * -------------------------
     * @author Philipp Schieferdecker
     */
    public class OpenConfigTreeSelListener implements TreeSelectionListener
    {
	/** TreeSelectionListener: valueChanged() */
	public void valueChanged(TreeSelectionEvent ev)
	{
	    JTree  dirTree = (JTree)ev.getSource();
	    Object o       = dirTree.getLastSelectedPathComponent();
	    if (o instanceof Directory) {
		Directory d = (Directory)o;
		if (configInfo!=null) {
		    configInfo.setVersionIndex(0);
		    configInfo = null;
		}
		jButtonOk.setEnabled(false);
		configTableModel.setConfigInfo(configInfo);
	    }
	    else if (o instanceof ConfigInfo) {
		configInfo = (ConfigInfo)o;
		jButtonOk.setEnabled(true);
		configTableModel.setConfigInfo(configInfo);
		jTableConfig.getSelectionModel().setSelectionInterval(0,0);
	    }
	}
	
    }

    /**
     * ToolTipTableCellRenderer
     * ------------------------
     * @author Philipp Schieferdecker
     */
    public class ToolTipTableCellRenderer extends DefaultTableCellRenderer
    {
	/** getTableCellRendererComponent */
	public Component getTableCellRendererComponent(JTable  table,
						       Object  object,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column)
	{
	    ConfigInfo configInfo = OpenConfigurationDialog.this.configInfo();
	    if (isSelected&&configInfo!=null)
		setToolTipText("HLT_KEY = " + configInfo.dbId());
	    return super.getTableCellRendererComponent(table,object,isSelected,
						       hasFocus,row,column);
	}
    }

    //
    // private member functions
    //
    private JPanel initComponents()
    {
	JPanel contentPane = new JPanel();

        JScrollPane jScrollPane1 = new JScrollPane();
        JScrollPane jScrollPane2 = new JScrollPane();
	
	// tree
	createTreeView(new Dimension(200,200));
	jTreeDirectories = this.dirTree;
	jScrollPane1.setViewportView(jTreeDirectories);
        
	// table
	configTableModel = new ConfigVersionTableModel();
	jTableConfig = new JTable(configTableModel);
	jTableConfig.getSelectionModel()
	    .addListSelectionListener(new OpenConfigListSelListener());
	jTableConfig.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	jTableConfig.setShowGrid(false);
	jTableConfig.getTableHeader().setReorderingAllowed(false);
	jTableConfig.getColumnModel().getColumn(0).setPreferredWidth(50);
	jTableConfig.getColumnModel().getColumn(1).setPreferredWidth(150);
	jTableConfig.getColumnModel().getColumn(2).setPreferredWidth(60);
	jTableConfig.getColumnModel().getColumn(3).setPreferredWidth(120);
	
	ToolTipTableCellRenderer renderer = new ToolTipTableCellRenderer();
	renderer.setHorizontalAlignment(SwingConstants.CENTER);
	jTableConfig.getColumnModel().getColumn(0).setCellRenderer(renderer);
	jTableConfig.getColumnModel().getColumn(1).setCellRenderer(renderer);
	jTableConfig.getColumnModel().getColumn(2).setCellRenderer(renderer);
	jTableConfig.getColumnModel().getColumn(3).setCellRenderer(renderer);
	
        jScrollPane2.setViewportView(jTableConfig);
	jTableConfig.getParent().setBackground(new Color(255,255,255));
	
	//buttons
        jButtonOk.setText("OK");
        jButtonOk.setEnabled(false);
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
	jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    jButtonCancelActionPerformed(evt);
		}
	    });
	     
	org.jdesktop.layout.GroupLayout layout =
	    new org.jdesktop.layout.GroupLayout(contentPane);
        contentPane.setLayout(layout);
        layout.setHorizontalGroup(layout
				  .createParallelGroup(org.jdesktop.layout
						       .GroupLayout.LEADING)
				  .add(layout.createSequentialGroup()
				       .addContainerGap()
				       .add(layout
					    .createParallelGroup(org.jdesktop.layout
								 .GroupLayout
								 .LEADING)
					    .add(layout.createSequentialGroup()
						 .add(jButtonOk,
						      org.jdesktop.layout
						      .GroupLayout
						      .DEFAULT_SIZE, 113,
						      Short.MAX_VALUE)
						 .addPreferredGap(org.jdesktop
								  .layout
								  .LayoutStyle
								  .RELATED)
						 .add(jButtonCancel,
						      org.jdesktop
						      .layout
						      .GroupLayout
						      .DEFAULT_SIZE, 107,
						      Short.MAX_VALUE))
					    .add(jScrollPane1,
						 org.jdesktop
						 .layout
						 .GroupLayout
						 .DEFAULT_SIZE, 226,
						 Short.MAX_VALUE))
				       .addPreferredGap(org.jdesktop
							.layout
							.LayoutStyle.RELATED)
				       .add(jScrollPane2,
					    org.jdesktop
					    .layout
					    .GroupLayout
					    .DEFAULT_SIZE, 396,
					    Short.MAX_VALUE)
				       .addContainerGap())
				  );
        layout.setVerticalGroup(layout
				.createParallelGroup(org.jdesktop
						     .layout
						     .GroupLayout.LEADING)
				.add(org.jdesktop
				     .layout
				     .GroupLayout
				     .TRAILING,
				     layout.createSequentialGroup()
				     .add(layout
					  .createParallelGroup(org.jdesktop
							       .layout
							       .GroupLayout.LEADING)
					  .add(org.jdesktop
					       .layout
					       .GroupLayout.TRAILING,
					       layout.createSequentialGroup()
					       .addContainerGap(389, Short.MAX_VALUE)
					       .add(jButtonOk))
					  .add(org.jdesktop
					       .layout
					       .GroupLayout
					       .TRAILING,
					       layout.createSequentialGroup()
					       .add(0, 0, 0)
					       .add(jScrollPane2,
						    org.jdesktop
						    .layout
						    .GroupLayout
						    .DEFAULT_SIZE, 414,
						    Short.MAX_VALUE)))
				     .addContainerGap())
				.add(org.jdesktop
				     .layout
				     .GroupLayout
				     .TRAILING,
				     layout.createSequentialGroup()
				     .addContainerGap(389, Short.MAX_VALUE)
				     .add(jButtonCancel)
				     .add(12, 12, 12))
				.add(layout.createSequentialGroup()
				     .addContainerGap()
				     .add(jScrollPane1)
				     .add(51, 51, 51))
				);
	
	/*
	  GroupLayout layout = new GroupLayout(contentPane);
	  contentPane.setLayout(layout);
	  layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.LEADING)
	  .add(layout.createSequentialGroup()
	  .addContainerGap()
	  .add(layout.createParallelGroup(GroupLayout.LEADING)
	  .add(layout.createSequentialGroup()
	  .add(jButtonOk,
	  GroupLayout.PREFERRED_SIZE,
	  113, GroupLayout.PREFERRED_SIZE)
	  .addPreferredGap(LayoutStyle.RELATED)
	  .add(jButtonCancel,
	  GroupLayout.PREFERRED_SIZE,
	  107, GroupLayout.PREFERRED_SIZE))
	  .add(jScrollPane1,
	  GroupLayout.PREFERRED_SIZE,
	  226, GroupLayout.PREFERRED_SIZE))
	  .addPreferredGap(LayoutStyle.RELATED)
	  .add(jScrollPane2,GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
	  .addContainerGap())
	  );
	  layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING)
	  .add(layout.createSequentialGroup()
	  .add(layout.createParallelGroup(GroupLayout.LEADING)
	  .add(GroupLayout.TRAILING, layout.createSequentialGroup()
	  .add(12, 12, 12)
	  .add(jButtonOk))
	  .add(GroupLayout.TRAILING, layout.createSequentialGroup()
	  .addContainerGap()
	  .add(jScrollPane2,
	  GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)))
	  .addContainerGap())
	  .add(GroupLayout.TRAILING,
	  layout.createSequentialGroup()
	  .addContainerGap(389, Short.MAX_VALUE)
	  .add(jButtonCancel)
	  .add(12, 12, 12))
	  .add(layout.createSequentialGroup()
	  .addContainerGap()
	  .add(jScrollPane1,
	  GroupLayout.PREFERRED_SIZE,
	  GroupLayout.DEFAULT_SIZE,
	  GroupLayout.PREFERRED_SIZE)
	  .add(51, 51, 51))
	  );
	*/
	
	return contentPane;
    }

}

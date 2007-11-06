package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import java.util.EventObject;

import confdb.data.Directory;
import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;

import confdb.db.ConfDB;

    
/**
 * ImportConfigurationDialog
 * -----------------------
 * @author Philipp Schieferdecker
 *
 */
public class ImportConfigurationDialog extends ConfigurationDialog
{
    //
    // member data
    //
    
    /** release tag (only allow the user to choose consistent configs) */
    private String releaseTag = null;

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
    public ImportConfigurationDialog(JFrame frame,ConfDB database,
				     String releaseTag)
    {
	super(frame,database);
	this.releaseTag = releaseTag;
	setTitle("Import Configuration");
	setContentPane(initComponents());
	addTreeSelectionListener(new ImportConfigTreeSelListener());
    }
	

    //
    // member functions
    //

    /** release tag */
    public String releaseTag() { return releaseTag; }
    
    /** retrieve configuration information */
    public ConfigInfo configInfo() { return configInfo; }
    
    /** 'OK' button callback */
    public void jButtonOkActionPerformed(ActionEvent e)
    {
	validChoice = false;
	if (configInfo!=null) validChoice = true;
	setVisible(false);
    }
    
    /** 'Cancel' button callback */
    public void jButtonCancelActionPerformed(ActionEvent e)
    {
	validChoice = false;
	setVisible(false);
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
	    .addListSelectionListener(new ImportConfigListSelListener());
	jTableConfig.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	jTableConfig.setShowGrid(false);
	jTableConfig.getTableHeader().setReorderingAllowed(false);
	jTableConfig.getColumnModel().getColumn(0).setPreferredWidth(50);
	jTableConfig.getColumnModel().getColumn(1).setPreferredWidth(150);
	jTableConfig.getColumnModel().getColumn(2).setPreferredWidth(60);
	jTableConfig.getColumnModel().getColumn(3).setPreferredWidth(120);
	
	DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
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
	

	return contentPane;
    }
    
    //
    // classes
    //
    
    /**
     * ImportConfigListSelListener
     * ---------------------------
     * @author Philipp Schieferdecker
     */
    public class ImportConfigListSelListener implements ListSelectionListener
    {
	/** ListSelectionListener: valueChanged() */
	public void valueChanged(ListSelectionEvent ev)
	{
	    ConfigInfo configInfo = ImportConfigurationDialog.this.configInfo();
	    if (ev.getValueIsAdjusting()||configInfo==null) return;
	    ListSelectionModel lsm = (ListSelectionModel)ev.getSource();
	    if (!lsm.isSelectionEmpty()) {
		int selectedIndex = lsm.getMinSelectionIndex();
		if (releaseTag.equals(configInfo.version(selectedIndex).releaseTag()))
		    configInfo.setVersionIndex(selectedIndex);
	    }
	}
    }
    

    /**
     * ImportConfigTreeSelListener
     * -------------------------
     * @author Philipp Schieferdecker
     */
    public class ImportConfigTreeSelListener implements TreeSelectionListener
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
		configTableModel.setConfigInfo(configInfo);

		int selectedIndex = -1;
		int iVersion=0;
		while (selectedIndex<0&&iVersion<configInfo.versionCount()) {
		    if (releaseTag.equals(configInfo.version(iVersion).releaseTag()))
			selectedIndex=iVersion;
		    ++iVersion;
		}
		if (selectedIndex<0) {
		    configInfo = null;
		    jButtonOk.setEnabled(false);
		    return;
		}
		
		jTableConfig.getSelectionModel().setSelectionInterval(selectedIndex,
								      selectedIndex);
		jButtonOk.setEnabled(true);
	    }
	}
    }
    

    /**
     * ImportConfigTableCellRenderer
     * -----------------------------
     * @author Philipp Schieferdecker
     *
     */
    public class ImportConfigTableCellRenderer extends DefaultTableCellRenderer
    {
	/** store colors */
	private Color activeColor   = new Color(0,0,0);
	private Color inactiveColor = new Color(160,160,160);
	
	/** render the current cell */
	public Component getTableCellRendererComponent(JTable  table,
						       Object  value,
						       boolean isSelected,
						       boolean hasFocus,
						       int     rowIndex,
						       int     vColIndex)
	{
            setText(value.toString());

	    if (isSelected) {
		super.setForeground(table.getSelectionForeground());
		super.setBackground(table.getSelectionBackground());
	    }
	    else {
		boolean active = false;
		if (ImportConfigurationDialog.this.configInfo()!=null) {
		    String releaseTag = ImportConfigurationDialog.this.releaseTag();
		    String rowReleaseTag = ImportConfigurationDialog.this.configInfo()
			.version(rowIndex).releaseTag();
		    active = releaseTag.equals(rowReleaseTag);
		}
		if (active) super.setForeground(activeColor);
		else        super.setForeground(inactiveColor);
		super.setBackground(table.getBackground());
	    }
            
	    setFont(table.getFont());

	    return this;
        }
    }
}

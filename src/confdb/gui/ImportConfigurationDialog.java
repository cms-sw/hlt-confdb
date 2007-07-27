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

import confdb.db.CfgDatabase;

    
/**
 * ImportConfigurationDialog
 * -----------------------
 * @author Philipp Schieferdecker
 *
 */
public class ImportConfigurationDialog
    extends ConfigurationDialog implements ActionListener
{
    //
    // member data
    //
    
    /** release tag (only allow the user to choose consistent configs) */
    private String releaseTag = null;

    /** configuration info */
    private ConfigInfo configInfo = null;
    
    /** configuration version table */
    private JTable configTable = null;
    
    /** configuration version table-model */
    private ConfigVersionTableModel configTableModel = null;
    
        /** Cancel / OK buttons */
    private JButton okButton = null;
    private JButton cancelButton = null;

    /** action commands */
    private static final String OK     = new String("OK");
    private static final String CANCEL = new String("Cancel");
    

    //
    // construction
    //
    
    /** standard constructor */
    public ImportConfigurationDialog(JFrame frame,CfgDatabase database,
				     String releaseTag)
    {
	super(frame,database);
	this.releaseTag = releaseTag;
	setTitle("Import Configuration");
	setContentPane(createContentPane());
	addTreeSelectionListener(new ImportConfigTreeSelListener());
    }
	

    //
    // member functions
    //

    /** release tag */
    public String releaseTag() { return releaseTag; }
    
    /** retrieve configuration information */
    public ConfigInfo configInfo() { return configInfo; }
    
    /** create the content pane */
    public JPanel createContentPane()
    {
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0.5;
	
	JPanel contentPane = new JPanel(new GridBagLayout());
	
	JPanel topPanel = new JPanel(new GridBagLayout());
	Dimension dimTreeView = new Dimension(200,300);
	c.gridx=0; c.gridy=0;
	topPanel.add(createTreeView(dimTreeView),c);

	Dimension dimTableView = new Dimension(400,300);
	c.gridx=1;c.gridy=0;
	topPanel.add(createTableView(dimTableView),c);
	
	contentPane.add(topPanel);
	
	c.gridx=0;c.gridy=1;c.gridwidth=2;
	contentPane.add(createButtonPanel(),c);

	return contentPane;
    }

    /** create the table view */
    public JScrollPane createTableView(Dimension dim)
    {
	configTableModel = new ConfigVersionTableModel();
	configTable = new JTable(configTableModel);
	configTable.getSelectionModel()
	    .addListSelectionListener(new ImportConfigListSelListener());
	configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	configTable.setPreferredSize(new Dimension(dim.width,dim.height-20));
	configTable.setShowGrid(false);
	configTable.getTableHeader().setReorderingAllowed(false);
	configTable.getColumnModel().getColumn(0).setPreferredWidth(50);
	configTable.getColumnModel().getColumn(1).setPreferredWidth(150);
	configTable.getColumnModel().getColumn(2).setPreferredWidth(150);

	ImportConfigTableCellRenderer cellRenderer =
	    new ImportConfigTableCellRenderer();
	cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
	configTable.setDefaultRenderer(Integer.class,cellRenderer);
	configTable.setDefaultRenderer(String.class,cellRenderer);
	
	JScrollPane result = new JScrollPane(configTable);
	result.setPreferredSize(dim);
	return result;
    }
    
    /** create button panel */
    public JPanel createButtonPanel()
    {
	JPanel result = new JPanel(new FlowLayout());
	okButton = new JButton(OK);
	okButton.addActionListener(this);
	okButton.setActionCommand(OK);
	okButton.setEnabled(false);
	cancelButton = new JButton(CANCEL);
	cancelButton.addActionListener(this);
	cancelButton.setActionCommand(CANCEL);
	result.add(cancelButton);
	result.add(okButton);
	return result;
    }

    /** ActionListener: actionPerformed() */
    public void actionPerformed(ActionEvent e)
    {
	validChoice = false;
	if (e.getActionCommand().equals(OK)) {
	    if (configInfo!=null) validChoice = true;
	}
	setVisible(false);
    }
    
    
    //
    // classes
    //
    
    /**
     * ImportConfigListSelListener
     * -------------------------
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
		okButton.setEnabled(false);
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
		    okButton.setEnabled(false);
		    return;
		}
		
		configTable.getSelectionModel().setSelectionInterval(selectedIndex,
								     selectedIndex);
		okButton.setEnabled(true);
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

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
 * OpenConfigurationDialog
 * -----------------------
 * @author Philipp Schieferdecker
 *
 */
public class OpenConfigurationDialog
    extends ConfigurationDialog implements ActionListener
{
    //
    // member data
    //

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
    public OpenConfigurationDialog(JFrame frame,CfgDatabase database)
    {
	super(frame,database);
	setTitle("Open Configuration");
	setContentPane(createContentPane());
	addTreeSelectionListener(new OpenConfigTreeSelListener());
    }
	

    //
    // member functions
    //

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
	    .addListSelectionListener(new OpenConfigListSelListener());
	configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	configTable.setPreferredSize(new Dimension(dim.width,dim.height-20));
	configTable.setShowGrid(false);
	configTable.getTableHeader().setReorderingAllowed(false);
	configTable.getColumnModel().getColumn(0).setPreferredWidth(50);
	configTable.getColumnModel().getColumn(1).setPreferredWidth(150);
	configTable.getColumnModel().getColumn(2).setPreferredWidth(150);
	((DefaultTableCellRenderer)configTable.getDefaultRenderer(Integer.class))
	    .setHorizontalAlignment(SwingConstants.CENTER);
	((DefaultTableCellRenderer)configTable.getDefaultRenderer(String.class))
	    .setHorizontalAlignment(SwingConstants.CENTER);
	
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
		okButton.setEnabled(false);
		configTableModel.setConfigInfo(configInfo);
	    }
	    else if (o instanceof ConfigInfo) {
		configInfo = (ConfigInfo)o;
		okButton.setEnabled(true);
		configTableModel.setConfigInfo(configInfo);
		configTable.getSelectionModel().setSelectionInterval(0,0);
	    }
	}
	
    }

}

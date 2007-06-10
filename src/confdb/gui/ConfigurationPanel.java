package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.io.FileWriter;
import java.io.IOException;

import org.jdesktop.layout.*;

import confdb.data.Configuration;


/**
 * ConfigurationPanel
 * ------------------
 * @author Philipp Schieferdecker
 *
 */
public class ConfigurationPanel extends JPanel
{
    //
    // member data
    //

    /** the configuration being displayed */
    private Configuration currentConfig = null;
    
    /** configuration tree */
    private JTree currentTree = null;

    /** import configuration tree */
    private JTree importTree = null;
    
    /** reference to the ConverterService */
    private ConverterService converterService = null;

    /** GUI components */
    private JTextField   jTextFieldName       = new JTextField();
    private JTextField   jTextFieldDirectory  = new JTextField();
    private JTextField   jTextFieldVersion    = new JTextField();
    private JTextField   jTextFieldCreated    = new JTextField();
    private JTextField   jTextFieldRelease    = new JTextField();

    private JTabbedPane  jTabbedPaneConvert   = new JTabbedPane();  
    
    private JTextField   jTextFieldFileName   = new JTextField();
    private JTextField   jTextFieldInput      = new JTextField();
    private JTextField   jTextFieldOutput     = new JTextField();

    private JTextField   jTextFieldImportDirectory = new JTextField();
    private JTextField   jTextFieldImportName      = new JTextField();
    private JTextField   jTextFieldImportVersion   = new JTextField();

    private ButtonGroup  formatButtonGroup    = new ButtonGroup();
    private JRadioButton jRadioButtonAscii    = new JRadioButton();
    private JRadioButton jRadioButtonPython   = new JRadioButton();
    private JRadioButton jRadioButtonHtml     = new JRadioButton();

    private JButton      convertButton        = new JButton();
    private JButton      importButton         = new JButton();
    private JButton      browseFileNameButton = new JButton();
    private JButton      browseInputButton    = new JButton();
    private JButton      browseOutputButton   = new JButton();

    private JTabbedPane  jTabbedPaneTree      = new JTabbedPane();
    private JEditorPane  jEditorPaneAscii     = new JEditorPane("text/plain","");
    private JEditorPane  jEditorPanePython    = new JEditorPane("text/plain","");
    private JEditorPane  jEditorPaneHtml      = new JEditorPane("text/html","");

    private JScrollPane  jScrollPaneTreeTab     = new JScrollPane();
    private JScrollPane  jScrollPaneCurrentTree = new JScrollPane();
    private JScrollPane  jScrollPaneImportTree  = new JScrollPane();
    private JSplitPane   jSplitPaneTree         = new JSplitPane();
        

    //
    // construction
    //

    /** default constructor */
    public ConfigurationPanel(JTree            currentTree,
			      JTree            importTree,
			      ConverterService converterService)
    {
	this.currentTree      = currentTree;
	this.importTree       = importTree;
	this.converterService = converterService;
	
	initComponents();
	addListeners();
    }
    
    
    //
    // member functions
    //

    /** 'Convert' button pressed */
    public void convertButtonActionPerformed(ActionEvent ev)
    {
	String input  = jTextFieldInput.getText();
	String format = formatButtonGroup.getSelection().getActionCommand();
	
	converterService.setInput(input);
	converterService.setFormat(format);
	String configAsString = converterService.convertConfiguration(currentConfig);

	if (configAsString.length()>0) {
	    FileWriter outputStream=null;
	    String     fileName    =jTextFieldFileName.getText();
	    
	    if      (format.toUpperCase().equals("ASCII")&&
		     !fileName.endsWith(".cfg"))       fileName += ".cfg";
	    else if (format.toUpperCase().equals("PYTHON")&&
		     !fileName.endsWith(".py"))	  fileName += ".py";
	    else if (format.toUpperCase().equals("HTML")&&
		     !fileName.endsWith(".html")) fileName += ".html";
	    
	    try {
		outputStream = new FileWriter(fileName);
		outputStream.write(configAsString,0,configAsString.length());
		outputStream.close();
	    }
	    catch (Exception e) {
		String msg = "Failed to convert configuration: " + e.getMessage();
		System.out.println(msg);
	    }
	}
    }

    /** 'Import' button pressed */
    public void importButtonActionPerformed(ActionEvent ev)
    {
	System.out.println("Import!");
    }

    
    /** Browse FileName [...] button pressed */
    public void browseFileNameButtonActionPerformed(ActionEvent ev)
    {
	System.out.println("BrowseFileName!");
    }

    /** Browse FileName [...] button pressed */
    public void browseInputButtonActionPerformed(ActionEvent ev)
    {
	System.out.println("BrowseInput!");
    }

    /** Browse FileName [...] button pressed */
    public void browseOutputButtonActionPerformed(ActionEvent ev)
    {
	System.out.println("BrowseOutput!");
    }

    /** set the current configuration and update fields accordingly */
    public void setCurrentConfig(Configuration config)
    {
	currentConfig = config;
	
	if (currentConfig.isEmpty()) {
	    jTextFieldName.setText("");
	    jTextFieldDirectory.setText("");
	    jTextFieldVersion.setText("");
	    jTextFieldCreated.setText("");
	    jTextFieldRelease.setText("");
	    
	    jTextFieldFileName.setText("");
	    jTextFieldInput.setText("");
	    jTextFieldOutput.setText("");
	    
	    jTextFieldFileName.setEditable(false);
	    jTextFieldInput.setEditable(false);
	    jTextFieldOutput.setEditable(false);
	    convertButton.setEnabled(false);
	}
	else {
	    String fileName = currentConfig.name() + "_V" + currentConfig.version();
	    String format   = formatButtonGroup.getSelection().getActionCommand();
	    
	    if      (format.equals("ASCII"))  fileName += ".cfg";
	    else if (format.equals("PYTHON")) fileName += ".py";
	    else if (format.equals("HTML"))   fileName += ".html";
	    
	    jTextFieldFileName.setText(fileName);
	    
	    jTextFieldFileName.setEditable(true);
	    jTextFieldInput.setEditable(true);
	    jTextFieldOutput.setEditable(true);
	    convertButton.setEnabled(true);
	    
	    jTextFieldName.setText(currentConfig.name());
	    
	    if (currentConfig.parentDir()!=null)
		jTextFieldDirectory.setText(currentConfig.parentDir().name());
	    else jTextFieldDirectory.setText("");
	    
	    if (currentConfig.version()>0)
		jTextFieldVersion.setText(Integer.toString(currentConfig.version()));
	    else jTextFieldVersion.setText("");
	    
	    jTextFieldCreated.setText(currentConfig.created());
	    jTextFieldRelease.setText(currentConfig.releaseTag());
	}
    }
    
    //
    // private member functions
    //

    /** init GUI components [generated by NetBeans] */
    private void initComponents()
    {
	JPanel       jPanel1 = new JPanel();
	
	JLabel       jLabel1 = new JLabel();
        JLabel       jLabel2 = new JLabel();
        JLabel       jLabel3 = new JLabel();
        JLabel       jLabel4 = new JLabel();
        JLabel       jLabel5 = new JLabel();
        
	JPanel       jPanelConvert = new JPanel();
        JLabel       jLabel6       = new JLabel();
        JLabel       jLabel7       = new JLabel();
        JLabel       jLabel8       = new JLabel();
        
        JPanel       jPanelImport = new JPanel();
        JLabel       jLabel9      = new JLabel();
        JLabel       jLabel10     = new JLabel();
        JLabel       jLabel11     = new JLabel();
	
        JScrollPane  jScrollPane2 = new JScrollPane();
        JScrollPane  jScrollPane3 = new JScrollPane();
        JScrollPane  jScrollPane4 = new JScrollPane();
        

        jSplitPaneTree.setContinuousLayout(true);
        jSplitPaneTree.setOneTouchExpandable(true);
	
        jPanel1.setBorder(BorderFactory
		       .createTitledBorder(null,
					   "Current Configuration",
					   TitledBorder.DEFAULT_JUSTIFICATION,
					   TitledBorder.DEFAULT_POSITION,
					   new Font("Dialog", 1, 12)));
        jLabel1.setText("Name:");
        jLabel2.setText("Directory:");
        jLabel3.setText("Version:");
        jLabel4.setText("Created:");
        jLabel5.setText("Release:");

        jTextFieldName.setBackground(new Color(255, 255, 255));
        jTextFieldName.setEditable(false);
        jTextFieldName.setFont(new Font("Dialog", 1, 12));
        jTextFieldName.setBorder(BorderFactory.createBevelBorder(BevelBorder
								 .LOWERED));
	
        jTextFieldDirectory.setBackground(new Color(255, 255, 255));
        jTextFieldDirectory.setEditable(false);
        jTextFieldDirectory.setBorder(BorderFactory
				      .createBevelBorder(BevelBorder.LOWERED));

        jTextFieldVersion.setBackground(new Color(255, 255, 255));
        jTextFieldVersion.setEditable(false);
        jTextFieldVersion.setBorder(BorderFactory
				    .createBevelBorder(BevelBorder.LOWERED));

        jTextFieldCreated.setBackground(new Color(255, 255, 255));
        jTextFieldCreated.setEditable(false);
        jTextFieldCreated.setBorder(BorderFactory
				    .createBevelBorder(BevelBorder.LOWERED));

        jTextFieldRelease.setBackground(new Color(255, 255, 255));
        jTextFieldRelease.setEditable(false);
        jTextFieldRelease.setForeground(Color.red);
        jTextFieldRelease.setBorder(BorderFactory
				    .createBevelBorder(BevelBorder.LOWERED));
	
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout
	    .setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.LEADING)
					 .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel5))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(GroupLayout.LEADING)
                    .add(jTextFieldRelease,
			 GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .add(jTextFieldCreated,
			 GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .add(jTextFieldVersion,
			 GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .add(jTextFieldName,
			 GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .add(jTextFieldDirectory,
			 GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel1,
			 GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldName,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextFieldDirectory,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jTextFieldVersion,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(jLabel4)
                    .add(jTextFieldCreated,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jTextFieldRelease,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jLabel6.setText("Filename:");
        jLabel7.setText("Input:");
        jLabel8.setText("Output:");

        jRadioButtonAscii.setText("ASCII");
        jRadioButtonAscii.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonAscii.setMargin(new Insets(0, 0, 0, 0));

        jRadioButtonPython.setText("Python");
        jRadioButtonPython.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonPython.setMargin(new Insets(0, 0, 0, 0));

        jRadioButtonHtml.setText("HTML");
        jRadioButtonHtml.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonHtml.setMargin(new Insets(0, 0, 0, 0));

        convertButton.setText("Convert");
        
	jTextFieldFileName.setBorder(BorderFactory.createBevelBorder(BevelBorder
								     .LOWERED));
	jTextFieldInput.setBorder(BorderFactory.createBevelBorder(BevelBorder
								  .LOWERED));
	jTextFieldOutput.setBorder(BorderFactory.createBevelBorder(BevelBorder
								   .LOWERED));
	
        browseInputButton.setText("...");
        browseFileNameButton.setText("...");
        browseOutputButton.setText("...");

        GroupLayout jPanelConvertLayout = new GroupLayout(jPanelConvert);
        jPanelConvert.setLayout(jPanelConvertLayout);
        jPanelConvertLayout.setHorizontalGroup(
            jPanelConvertLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelConvertLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelConvertLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jLabel7)
                    .add(jLabel8)
                    .add(jRadioButtonAscii)
                    .add(jRadioButtonPython)
                    .add(jRadioButtonHtml))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelConvertLayout
		     .createParallelGroup(GroupLayout.LEADING, false)
                    .add(jPanelConvertLayout.createSequentialGroup()
                        .add(jPanelConvertLayout
			     .createParallelGroup(GroupLayout.LEADING, false)
                            .add(jTextFieldOutput)
                            .add(jTextFieldFileName)
                            .add(jTextFieldInput,
				 GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(jPanelConvertLayout
			     .createParallelGroup(GroupLayout.LEADING)
                            .add(browseOutputButton, 0, 0, Short.MAX_VALUE)
                            .add(browseFileNameButton, 0, 0, Short.MAX_VALUE)
                            .add(browseInputButton,
				 GroupLayout.PREFERRED_SIZE, 21,
				 GroupLayout.PREFERRED_SIZE)))
                    .add(convertButton,
			 GroupLayout.DEFAULT_SIZE,
			 GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelConvertLayout.setVerticalGroup(
            jPanelConvertLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelConvertLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelConvertLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jTextFieldFileName,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		     .add(browseFileNameButton,
			  GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelConvertLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jTextFieldInput,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(browseInputButton,
			 GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelConvertLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(jTextFieldOutput,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(browseOutputButton,
			 GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelConvertLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(jPanelConvertLayout.createSequentialGroup()
                        .add(jRadioButtonAscii)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(jRadioButtonPython)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(jRadioButtonHtml))
                    .add(convertButton,
			 GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPaneConvert.addTab("Convert", jPanelConvert);

        jLabel9.setText("Name:");
        jLabel10.setText("Directory:");
        jLabel11.setText("Version:");

        jTextFieldImportDirectory.setBackground(new Color(255, 255, 255));
        jTextFieldImportDirectory.setEditable(false);
        jTextFieldImportDirectory.setBorder(BorderFactory
					    .createBevelBorder(BevelBorder.LOWERED));

        jTextFieldImportName.setBackground(new Color(255, 255, 255));
        jTextFieldImportName.setEditable(false);
        jTextFieldImportName.setBorder(BorderFactory
				       .createBevelBorder(BevelBorder.LOWERED));

        jTextFieldImportVersion.setBackground(new Color(255, 255, 255));
        jTextFieldImportVersion.setEditable(false);
        jTextFieldImportVersion.setBorder(BorderFactory
					  .createBevelBorder(BevelBorder.LOWERED));

        importButton.setText("Import");

        GroupLayout jPanelImportLayout = new GroupLayout(jPanelImport);
        jPanelImport.setLayout(jPanelImportLayout);
        jPanelImportLayout.setHorizontalGroup(
            jPanelImportLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, jPanelImportLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelImportLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING,
			 importButton, GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .add(jPanelImportLayout.createSequentialGroup()
                        .add(jPanelImportLayout
			     .createParallelGroup(GroupLayout.LEADING)
                            .add(jLabel10)
                            .add(jLabel9)
                            .add(jLabel11))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(jPanelImportLayout
			     .createParallelGroup(GroupLayout.LEADING)
                            .add(jTextFieldImportName,
				 GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                            .add(jTextFieldImportDirectory,
				 GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                            .add(jTextFieldImportVersion,
				 GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanelImportLayout.setVerticalGroup(
            jPanelImportLayout.createParallelGroup(GroupLayout.LEADING)
            .add(jPanelImportLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelImportLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jTextFieldImportName,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelImportLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(jTextFieldImportDirectory,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jPanelImportLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(jTextFieldImportVersion,
			 GroupLayout.PREFERRED_SIZE,
			 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(importButton, GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                .addContainerGap())
        );
        jTabbedPaneConvert.addTab("Import", jPanelImport);

        jScrollPaneCurrentTree.setViewportView(currentTree);
        importTree.setBackground(UIManager.getDefaults()
				 .getColor("Button.background"));
        jScrollPaneImportTree.setViewportView(importTree);
	
        jSplitPaneTree.setDividerLocation(1.0);
        jSplitPaneTree.setResizeWeight(1.0);
        jSplitPaneTree.setDividerSize(1);
        jSplitPaneTree.setLeftComponent(jScrollPaneCurrentTree);
        //jSplitPaneTree.setRightComponent(jScrollPaneImportTree);
	jSplitPaneTree.setRightComponent(null);

	jTabbedPaneTree.addTab("Tree", jSplitPaneTree);
	
        jEditorPaneAscii.setEditable(false);
        jScrollPane2.setViewportView(jEditorPaneAscii);

        jTabbedPaneTree.addTab("Ascii", jScrollPane2);

        jEditorPanePython.setEditable(false);
        jScrollPane3.setViewportView(jEditorPanePython);

        jTabbedPaneTree.addTab("Python", jScrollPane3);

        jEditorPaneHtml.setEditable(false);
        jScrollPane4.setViewportView(jEditorPaneHtml);

        jTabbedPaneTree.addTab("Html", jScrollPane4);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
		 .add(jPanel1, GroupLayout.DEFAULT_SIZE,
		      GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jTabbedPaneConvert,
		     GroupLayout.PREFERRED_SIZE, 247, GroupLayout.PREFERRED_SIZE))
            .add(jTabbedPaneTree, GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                    .add(jPanel1, GroupLayout.DEFAULT_SIZE,
			 GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jTabbedPaneConvert,
			 GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jTabbedPaneTree, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE))
        );
    
	// initialize conversion-format button group
	jRadioButtonAscii.setActionCommand("ASCII");
	jRadioButtonPython.setActionCommand("PYTHON");
	jRadioButtonHtml.setActionCommand("HTML");
	formatButtonGroup.add(jRadioButtonAscii);
	formatButtonGroup.add(jRadioButtonPython);
	formatButtonGroup.add(jRadioButtonHtml);
	jRadioButtonAscii.setSelected(true);
	jRadioButtonPython.setSelected(false);
	jRadioButtonHtml.setSelected(false);
	jRadioButtonPython.setEnabled(false);

    }
    
    /** add ActionListeners to releavant components */
    private void addListeners()
    {
	jTextFieldInput.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ConfigurationPanel.this.converterService
			.setInput(jTextFieldInput.getText());
		}
	    });
	
	/*
	  jTextFieldOutput.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	  ConfigurationPanel.this.converterService
	  .setOutput(jTextFieldOutput.getText());
	  }
	  });
	*/
	
	convertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                convertButtonActionPerformed(evt);
            }
        });
        
	browseFileNameButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    browseFileNameButtonActionPerformed(evt);
		}
	    });

	browseInputButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    browseInputButtonActionPerformed(evt);
		}
	    });

	browseOutputButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    browseOutputButtonActionPerformed(evt);
		}
	    });

	importButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    importButtonActionPerformed(evt);
		}
	    });

	jRadioButtonAscii.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ConfigurationPanel.this.converterService.setFormat("ASCII");
		}
	    });
	
	jRadioButtonPython.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ConfigurationPanel.this.converterService.setFormat("PYTHON");
		}
	    });
	
	jRadioButtonHtml.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ConfigurationPanel.this.converterService.setFormat("HTML");
		}
	    });
	
	jTabbedPaneTree.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    JTabbedPane pane = (JTabbedPane)e.getSource();
		    JEditorPane editorPane = null;
		    String      format = null;
		    int         sel = pane.getSelectedIndex();
		    switch (sel) {
		    case 0 : return;
		    case 1 : editorPane = jEditorPaneAscii;  format="ascii";  break; 
		    case 2 : editorPane = jEditorPanePython; format="python"; break; 
		    case 3 : editorPane = jEditorPaneHtml;   format="html";   break; 
		    default : return;
		    }
		    
		    if (currentConfig==null) { editorPane.setText(""); }
		    else {
			converterService.setFormat(format);
			String  configAsString =
			    converterService.convertConfiguration(currentConfig);
			editorPane.setText(configAsString);
		    }
		}
	    });
	
	jTabbedPaneConvert.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    JTabbedPane pane = (JTabbedPane)e.getSource();
		    int         sel  = pane.getSelectedIndex();
		    if (sel==0) {
			jSplitPaneTree.setRightComponent(null);
			jSplitPaneTree.setDividerLocation(1);
			jSplitPaneTree.setDividerSize(1);
		    }
		    if (sel==1) {
			jSplitPaneTree.setRightComponent(jScrollPaneImportTree);
			jSplitPaneTree.setDividerLocation(0.5);
			jSplitPaneTree.setDividerSize(8);
		    }
		}
	    });
    }

}

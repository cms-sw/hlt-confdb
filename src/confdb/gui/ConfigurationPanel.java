package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.FileWriter;
import java.io.IOException;

import confdb.data.Configuration;
import confdb.data.Template;
import confdb.data.EDSourceTemplate;
import confdb.data.EDSourceInstance;
import confdb.data.DataException;

import confdb.converter.ConverterFactory;
import confdb.converter.Converter;

import confdb.db.CfgDatabase;


/**
 * ConfigurationPanel
 * ------------------
 * @author Philipp Schieferdecker
 *
 * display information about the current configuration.
 */
public class ConfigurationPanel extends JPanel implements ActionListener
{
    //
    // member data
    //

    /** reference to the database interface */
    private CfgDatabase database = null;
    
    /** the configuration being displayed */
    private Configuration configuration = null;

    /** configuration name */
    private JLabel labelConfigName = null;
    private JTextField valueConfigName = null;

    /** configuration directory */
    private JLabel labelConfigDir = null;
    private JTextField valueConfigDir = null;

    /** configuration version */
    private JLabel labelConfigVersion = null;
    private JTextField valueConfigVersion = null;

    /** configuration creation date */
    private JLabel labelConfigCreated = null;
    private JTextField valueConfigCreated = null;

    /** configuration version */
    private JLabel labelReleaseTag = null;
    private JTextField valueReleaseTag = null;

    /** converter text fields */
    private JTextField  textFieldFilePath  = null;
    private JTextField  textFieldFileName  = null;
    private JTextField  textFieldInput     = null;
    private ButtonGroup formatButtonGroup  = null;

    /** converter button */
    private JButton buttonConvert = null;
    
    
    //
    // construction
    //

    /** default constructor */
    public ConfigurationPanel(CfgDatabase database)
    {
	super(new GridLayout(1,2));
	this.database = database;
	
	JPanel infoPanel = new JPanel(new SpringLayout());
	infoPanel.setBorder(BorderFactory
			    .createTitledBorder("Current Configuration"));

	labelConfigName = new JLabel("Name:");
	valueConfigName = new JTextField(10);
	valueConfigName.setEditable(false);
	valueConfigName.setBackground(Color.WHITE);
	labelConfigName.setLabelFor(valueConfigName);
	infoPanel.add(labelConfigName);
	infoPanel.add(valueConfigName);
	
	labelConfigDir = new JLabel("Directory:");
	valueConfigDir = new JTextField(10);
	valueConfigDir.setEditable(false);
	valueConfigDir.setBackground(Color.WHITE);
	labelConfigDir.setLabelFor(valueConfigDir);
	infoPanel.add(labelConfigDir);
	infoPanel.add(valueConfigDir);
	
	labelConfigVersion = new JLabel("Version:");
	valueConfigVersion = new JTextField(10);
	valueConfigVersion.setEditable(false);
	valueConfigVersion.setBackground(Color.WHITE);
	labelConfigVersion.setLabelFor(valueConfigVersion);
	infoPanel.add(labelConfigVersion);
	infoPanel.add(valueConfigVersion);
	
	labelConfigCreated = new JLabel("Created:");
	valueConfigCreated = new JTextField(10);
	valueConfigCreated.setEditable(false);
	valueConfigCreated.setBackground(Color.WHITE);
	labelConfigCreated.setLabelFor(valueConfigCreated);
	infoPanel.add(labelConfigCreated);
	infoPanel.add(valueConfigCreated);
	
	labelReleaseTag = new JLabel("Release:");
	valueReleaseTag = new JTextField(10);
	valueReleaseTag.setEditable(false);
	valueReleaseTag.setBackground(Color.WHITE);
	valueReleaseTag.setForeground(Color.RED);
	labelReleaseTag.setLabelFor(valueReleaseTag);
	infoPanel.add(labelReleaseTag);
	infoPanel.add(valueReleaseTag);
	
	SpringUtilities.makeCompactGrid(infoPanel,5,2,6,6,6,6);
	
	add(infoPanel);
	
	textFieldFilePath = new JTextField(10);
	textFieldFilePath.setEditable(false);
	
	textFieldFileName = new JTextField(10);
	textFieldFileName.setEditable(false);
	
	textFieldInput = new JTextField(10);
	textFieldInput.setEditable(false);
	
	FlowLayout layout = new FlowLayout();
	layout.setVgap(0);
	layout.setHgap(1);
	
	JPanel       formatPanel       = new JPanel(layout);
	JRadioButton asciiFormatButton = new JRadioButton("ASCII");
	JRadioButton pythonFormatButton = new JRadioButton("Python");
	JRadioButton htmlFormatButton  = new JRadioButton("HTML");
	asciiFormatButton.setActionCommand("ASCII");
	pythonFormatButton.setActionCommand("PYTHON");
	htmlFormatButton.setActionCommand("HTML");
	formatButtonGroup = new ButtonGroup();
	formatButtonGroup.add(asciiFormatButton);
	formatButtonGroup.add(pythonFormatButton);
	formatButtonGroup.add(htmlFormatButton);
	formatPanel.add(asciiFormatButton);
	formatPanel.add(pythonFormatButton);
	formatPanel.add(htmlFormatButton);
	asciiFormatButton.setSelected(true);
	pythonFormatButton.setSelected(false);
	htmlFormatButton.setSelected(false);
	pythonFormatButton.setEnabled(false);
	htmlFormatButton.setEnabled(false);

	buttonConvert = new JButton("Convert");
	buttonConvert.addActionListener(this);
	buttonConvert.setEnabled(false);
	
	JPanel converterPanel = new JPanel(new SpringLayout());
	converterPanel.setBorder(BorderFactory.createTitledBorder("Converter"));

	JLabel filePathLabel  = new JLabel("Path:");
	JLabel fileNameLabel  = new JLabel("Filename:");
	JLabel inputLabel     = new JLabel("Input:");
	JLabel formatLabel    = new JLabel("Format:");
	
	converterPanel.add(filePathLabel);
	converterPanel.add(textFieldFilePath);
	filePathLabel.setLabelFor(textFieldFilePath);
	
	converterPanel.add(fileNameLabel);
	converterPanel.add(textFieldFileName);
	fileNameLabel.setLabelFor(textFieldFileName);
	
	converterPanel.add(inputLabel);
	converterPanel.add(textFieldInput);
	inputLabel.setLabelFor(textFieldInput);
	
	converterPanel.add(formatLabel);
	converterPanel.add(formatPanel);
	formatLabel.setLabelFor(formatPanel);
	
	converterPanel.add(new JLabel(""));
	converterPanel.add(buttonConvert);
	
	SpringUtilities.makeCompactGrid(converterPanel,5,2,3,3,3,3);
	add(converterPanel);
    }
    

    //
    // member functions
    //
    
    /** update information */
    public void update(Configuration config)
    {
	configuration = config;
	
	if (configuration.isEmpty()) {
	    valueConfigName.setText("");
	    valueConfigDir.setText("");
	    valueConfigVersion.setText("");
	    valueConfigCreated.setText("");
	    valueReleaseTag.setText("");
	    
	    textFieldFilePath.setText("");
	    textFieldFileName.setText("");
	    textFieldInput.setText("");
	    
	    textFieldFilePath.setEditable(false);
	    textFieldFileName.setEditable(false);
	    textFieldInput.setEditable(false);
	    buttonConvert.setEnabled(false);
	}
	else {
	    String fileName = configuration.name() + "_V" + configuration.version();
	    String format   = formatButtonGroup.getSelection().getActionCommand();
	    
	    if      (format.equals("ASCII"))  fileName += ".cfg";
	    else if (format.equals("PYTHON")) fileName += ".py";
	    else if (format.equals("HTML"))   fileName += ".html";
	    
	    textFieldFileName.setText(fileName);
	    
	    textFieldFilePath.setEditable(true);
	    textFieldFileName.setEditable(true);
	    textFieldInput.setEditable(true);
	    buttonConvert.setEnabled(true);
	    
	    valueConfigName.setText(configuration.name());
	    
	    if (configuration.parentDir()!=null)
		valueConfigDir.setText(configuration.parentDir().name());
	    else valueConfigDir.setText("");
	    
	    if (configuration.version()>0)
		valueConfigVersion.setText(Integer.toString(configuration.version()));
	    else valueConfigVersion.setText("");
	    
	    valueConfigCreated.setText(configuration.created());
	    valueReleaseTag.setText(configuration.releaseTag());
	}
    }

    /** ActionListener interface: actionPerformed() */
    public void actionPerformed(ActionEvent ev)
    {
	FileWriter outputStream=null;
	String     filePath    =textFieldFilePath.getText();
	String     fileName    =textFieldFileName.getText();
	String     input       =textFieldInput.getText();
	String     format      =formatButtonGroup.getSelection().getActionCommand();
	
	if (filePath.length()>0) fileName = filePath += "/" + fileName;

	if (format.equals("ASCII")&&!fileName.endsWith(".cfg"))
	    fileName += ".cfg";
	else if (format.equals("PYTHON")&&!fileName.endsWith(".py"))
	    fileName += ".py";
	else if (format.equals("HTML")&&!fileName.endsWith(".html"))
	    fileName += ".html";
	
	try {
	    ConverterFactory factory        = ConverterFactory.getFactory("default");
	    Converter        converter      = factory.getConverter(format);
	    
	    if (input.length()>0)
		converter.overrideEDSource(getPoolSource(input));
	    
	    String           configAsString = converter.convert(configuration);
	    
	    outputStream = new FileWriter(fileName);
	    outputStream.write(configAsString,0,configAsString.length());
	    outputStream.close();
	}
	catch (Exception e) {
	    String msg = "Failed to convert configuration: " + e.getMessage();
	    System.out.println(msg);
	}
    }
    
    /** make a PoolSource */
    public EDSourceInstance getPoolSource(String input)
    {
	EDSourceTemplate template = database
	    .loadEDSourceTemplate(configuration.releaseTag(),"PoolSource");
	EDSourceInstance result = null;
	try {
	    result = (EDSourceInstance)template.instance();
	}
	catch (DataException e) {
	    System.out.println("FAILED to create instance of PoolSource:"+
			       e.getMessage());
	}
	return result;
    }
    
}

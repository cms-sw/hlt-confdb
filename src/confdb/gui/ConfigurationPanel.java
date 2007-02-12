package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import confdb.data.Configuration;


/**
 * ConfigurationPanel
 * ------------------
 * @author Philipp Schieferdecker
 *
 * display information about the current configuration.
 */
public class ConfigurationPanel extends JPanel
{
    //
    // member data
    //
    
    /** configuration name */
    private JLabel labelConfigName = null;
    private JLabel valueConfigName = null;

    /** configuration directory */
    private JLabel labelConfigDir = null;
    private JLabel valueConfigDir = null;

    /** configuration version */
    private JLabel labelConfigVersion = null;
    private JLabel valueConfigVersion = null;

    /** configuration creation date */
    private JLabel labelConfigCreated = null;
    private JLabel valueConfigCreated = null;

    /** configuration version */
    private JLabel labelReleaseTag = null;
    private JLabel valueReleaseTag = null;

    
    //
    // construction
    //

    /** default constructor */
    public ConfigurationPanel()
    {
	super(new GridBagLayout());
	setBorder(BorderFactory.createTitledBorder("Current Configuration"));

	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 0.5;
	
	labelConfigName = new JLabel("Name:");
	valueConfigName = new JLabel(" - ");
	
	labelConfigDir = new JLabel("Directory:");
	valueConfigDir = new JLabel(" - ");
	
	labelConfigVersion = new JLabel("Version:");
	valueConfigVersion = new JLabel(" - ");
	
	labelConfigCreated = new JLabel("Created:");
	valueConfigCreated = new JLabel(" - ");
	
	labelReleaseTag = new JLabel("Release:");
	valueReleaseTag = new JLabel(" - ");
	
	c.gridx=0;c.gridy=0;c.gridwidth=1;
	add(labelConfigName,c);
	c.gridx=1;c.gridy=0;c.gridwidth=1;
	add(valueConfigName,c);
	c.gridx=0;c.gridy=1;c.gridwidth=1;
	add(labelConfigDir,c);
	c.gridx=1;c.gridy=1;c.gridwidth=1;
	add(valueConfigDir,c);
	c.gridx=0;c.gridy=2;c.gridwidth=1;
	add(labelConfigVersion,c);
	c.gridx=1;c.gridy=2;c.gridwidth=1;
	add(valueConfigVersion,c);
	c.gridx=0;c.gridy=3;c.gridwidth=1;
	add(labelConfigCreated,c);
	c.gridx=1;c.gridy=3;c.gridwidth=1;
	add(valueConfigCreated,c);
	c.gridx=0;c.gridy=4;c.gridwidth=1;
	add(labelReleaseTag,c);
	c.gridx=1;c.gridy=4;c.gridwidth=1;
	add(valueReleaseTag,c);
    }
    

    //
    // member functions
    //
    
    /** update information */
    public void update(Configuration config)
    {
	valueConfigName.setText(config.name());

	if (config.parentDir()!=null)
	    valueConfigDir.setText(config.parentDir().name());
	else valueConfigDir.setText("");
	
	if (config.version()>0)
	    valueConfigVersion.setText(Integer.toString(config.version()));
	else valueConfigVersion.setText("");

	valueConfigCreated.setText(config.created());
	valueReleaseTag.setText(config.releaseTag());
    }

}

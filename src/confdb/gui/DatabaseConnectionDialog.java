package confdb.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Properties;

import java.io.InputStream;
import java.io.IOException;

import confdb.db.ConfDBSetups;


/**
 * DatabaseConnectionDialog
 * ------------------------
 * @author Philipp Schieferdecker
 *
 * prompt the user for the parameters to connect to the database.
 *
 */

public class DatabaseConnectionDialog 
    extends JDialog
    implements ActionListener, FocusListener, PropertyChangeListener
{
    //
    // member data
    //

    /** possible choices of dbType*/
    private static final String dbTypeMySQL = "mysql";
    private static final String dbTypeOracle = "oracle";

    /** indicate if a valid choice was made */
    private boolean validChoice = false;

    /** database setup */
    private ConfDBSetups dbSetups = new ConfDBSetups();

    /** GUI components */
    private JComboBox      comboBoxDbSetup   = null;
    private ButtonGroup    buttonGroupDbType = null;
    private JRadioButton   mysqlButton       = null;
    private JRadioButton   oracleButton      = null;
    private JRadioButton   noneButton        = null;
    private JTextField     textFieldDbHost   = null;
    private JTextField     textFieldDbPort   = null;
    private JTextField     textFieldDbName   = null;
    private JTextField     textFieldDbUser   = null;
    private JPasswordField textFieldDbPwrd   = null;    

    /** option pane */
    private JOptionPane optionPane = null;
    
    /** option button labels */
    private static final String okString     = "OK";
    private static final String cancelString = "Cancel";
    

    //
    // member functions
    //
    
    /** constructor */
    public DatabaseConnectionDialog(JFrame frame)
    {
	super(frame,"Estabish Database connection",true);
	
	validChoice = false;

	comboBoxDbSetup = new JComboBox(dbSetups.labelsAsArray());
	comboBoxDbSetup.setEditable(false);
	comboBoxDbSetup.setSelectedIndex(0);
	comboBoxDbSetup.setBackground(new Color(255, 255, 255));
	comboBoxDbSetup.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    comboBoxDbSetupActionPerformed(evt);
		}
	    });
	
	mysqlButton = new JRadioButton("MySQL");
	mysqlButton.setMnemonic(KeyEvent.VK_M);
        mysqlButton.setActionCommand(dbTypeMySQL);
        mysqlButton.setSelected(true);

	oracleButton = new JRadioButton("Oracle");
	oracleButton.setMnemonic(KeyEvent.VK_O);
        oracleButton.setActionCommand(dbTypeOracle);
	
	noneButton = new JRadioButton("NONE");
	
	buttonGroupDbType = new ButtonGroup();
	buttonGroupDbType.add(mysqlButton);
	buttonGroupDbType.add(oracleButton);
	buttonGroupDbType.add(noneButton);

	noneButton.setSelected(true);
	
	textFieldDbHost = new JTextField(15);
	textFieldDbPort = new JTextField(6);
	textFieldDbName = new JTextField(15);
	textFieldDbUser = new JTextField(15);
	textFieldDbPwrd = new JPasswordField(15);
	
	JPanel panelDbType = new JPanel(new FlowLayout());
	panelDbType.add(mysqlButton);
	panelDbType.add(oracleButton);
	
	// create the option pane
	String labelDbStup = "Setup:";
	String labelDbHost = "Host:";
	String labelDbPort = "Port:";
	String labelDbName = "DB Name:";
	String labelDbUser = "User:";
	String labelDbPwrd = "Password:";
	
	Object[] inputs = { labelDbStup,comboBoxDbSetup,
	                    panelDbType,
			    labelDbHost,textFieldDbHost,
			    labelDbPort,textFieldDbPort,
			    labelDbName,textFieldDbName,
			    labelDbUser,textFieldDbUser,
			    labelDbPwrd,textFieldDbPwrd };

	ImageIcon icon = new ImageIcon(getClass().getResource("/dbicon.gif"));

	Object[] options = { okString,cancelString };
	
	optionPane = new JOptionPane(inputs,
				     JOptionPane.QUESTION_MESSAGE,
				     JOptionPane.YES_NO_OPTION,
				     icon, //null,
				     options,
				     options[0]);
	
	// make this dialog display the created content pane
	setContentPane(optionPane);
	
	//handle window closing correctly
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent we)
		{
		    optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
		}
	    });
	
	// ensure the database user field always get the first focus
	addComponentListener(new ComponentAdapter()
	    {
		public void componentShown(ComponentEvent ce)
		{
		    textFieldDbHost.requestFocusInWindow();
		    textFieldDbHost.selectAll();
		}
	    });
	
	// register event handlers to put text into the fields
	textFieldDbHost.addActionListener(this);
	textFieldDbHost.addFocusListener(this);
	
	textFieldDbPort.addActionListener(this);
	textFieldDbPort.addFocusListener(this);
	
	textFieldDbName.addActionListener(this);
	textFieldDbName.addFocusListener(this);
	
	textFieldDbUser.addActionListener(this);
	textFieldDbUser.addFocusListener(this);
	
	textFieldDbPwrd.addActionListener(this);
	textFieldDbPwrd.addFocusListener(this);
	
	// register an event handler to react to option pane state changes
	optionPane.addPropertyChangeListener(this);
    }

    /** was a valid choice made? */
    public boolean validChoice() { return validChoice; }

    /** get database type */
    public String getDbType()
    {
	return buttonGroupDbType.getSelection().getActionCommand();
    }
    
    /** get database host */
    public String getDbHost() { return textFieldDbHost.getText(); }

    /** get database port number */
    public String getDbPort() {	return textFieldDbPort.getText(); }

    /** get database name */
    public String getDbName() {	return textFieldDbName.getText(); }
    
    /** get database url */
    public String getDbUrl()
    {
	String result = null;
	String type = getDbType();
	String host = getDbHost();
	String port = getDbPort();
	String name = getDbName();
	if (host.length()==0 || port.length()==0 || name.length()==0) return result;
	if (type.equals(dbTypeMySQL))       result = "jdbc:mysql://";
	else if (type.equals(dbTypeOracle)) result = "jdbc:oracle:thin:@//";
	else return result;
	result += host + ":" + port + "/" + name;
	return result;
    }

    /** get database user name */
    public String getDbUser() {	return textFieldDbUser.getText(); }
    
    /** get database password */
    public String getDbPassword()
    {
	return new String(textFieldDbPwrd.getPassword());
    }

    /** type choosen from the combo box */
    public void comboBoxDbSetupActionPerformed(ActionEvent e)
    {
	int selectedIndex = comboBoxDbSetup.getSelectedIndex();
	textFieldDbHost.setText(dbSetups.host(selectedIndex));
	textFieldDbPort.setText(dbSetups.port(selectedIndex));
	textFieldDbName.setText(dbSetups.name(selectedIndex));
	textFieldDbUser.setText(dbSetups.user(selectedIndex));
	String dbType = dbSetups.type(selectedIndex);
	if      (dbType.equals("mysql"))  mysqlButton.setSelected(true);
	else if (dbType.equals("oracle")) oracleButton.setSelected(true);
	else                              noneButton.setSelected(true);
	
	textFieldDbUser.requestFocusInWindow();
	textFieldDbUser.selectAll();
    }
    

    /** callback to handle text field events. (Hitting <RETURN> will be like <OK>) */
    public void actionPerformed(ActionEvent e)
    {
	optionPane.setValue(okString);
    }

    /** if a text failed focus */
    public void focusGained(FocusEvent e)
    {
	JTextField textField = (JTextField)e.getComponent();
	if (textField!=null) textField.selectAll();
    }

    /** if a text field looses focus, do nothing */
    public void focusLost(FocusEvent e)
    {
	JTextField textField = (JTextField)e.getComponent();
	if (textField!=null) textField.select(0,0);
    }
    
    /** callback to handle option pane state changes */
    public void propertyChange(PropertyChangeEvent e)
    {
	String property = e.getPropertyName();
	
	if (isVisible() && 
	    (e.getSource()==optionPane) &&
	    (JOptionPane.VALUE_PROPERTY.equals(property) ||
	     JOptionPane.INPUT_VALUE_PROPERTY.equals(property))) {
	    
	    // retrieve current value, check if initialized
	    Object value = optionPane.getValue();
	    if (value==JOptionPane.UNINITIALIZED_VALUE)  return;
	    
	    // reset current value
	    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
	    
	    if (okString.equals(value)) {
		validChoice = true;
		setVisible(false);
	    }
	    else {
		setVisible(false);
		JOptionPane.showMessageDialog(optionPane.getRootFrame(),
					      "No database connection established.",
					      "",
					      JOptionPane.WARNING_MESSAGE);
	    }
	}
    }
    
}

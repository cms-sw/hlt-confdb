package confdb.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

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
    private String dbSetup = null;
    private JComboBox comboBoxDbSetup = null;
    private static final String[] dbSetupChoices =
    { "","Tutorial Aug 1st 07",
      "CMS Online","HLT Development","MySQL - local","Oracle XE - local"};
    
    /** database type (mysql/oracle) */
    private String dbType = null;
    private ButtonGroup buttonGroupDbType = null;
    private JRadioButton mysqlButton = null;
    private JRadioButton oracleButton = null;
    private JRadioButton noneButton = null;
    
    /** database host */
    private String dbHost = null;
    private JTextField textFieldDbHost = null;

    /** database port */
    private String dbPort = null;
    private JTextField textFieldDbPort = null;

    /** database name */
    private String dbName = null;
    private JTextField textFieldDbName = null;
    
    /** database user name */
    private String dbUser = null;
    private JTextField textFieldDbUser = null;

    /** database user password */
    private String dbPwrd = null;
    private JPasswordField textFieldDbPwrd = null;    

    
    /** option pane */
    private JOptionPane optionPane = null;
    
    /** option button labels */
    private static final String okString = "OK";
    private static final String cancelString = "Cancel";
    

    //
    // member functions
    //
    
    /** constructor */
    public DatabaseConnectionDialog(JFrame frame)
    {
	super(frame,"Estabish Database connection",true);
	
	validChoice = false;

	// create the text fields for the database connection attributes
	comboBoxDbSetup = new JComboBox(dbSetupChoices);
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
	textFieldDbHost.setText("");
	
	textFieldDbPort = new JTextField(6);
	textFieldDbPort.setText("");
	
	textFieldDbName = new JTextField(15);
	textFieldDbName.setText("");
	
	textFieldDbUser = new JTextField(15);
	textFieldDbUser.setText("");
	
	textFieldDbPwrd = new JPasswordField(15);
	textFieldDbPwrd.setText("");
	
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

	ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("dbicon.gif"));

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
    public String getDbType() {	return dbType; }

    /** get database host */
    public String getDbHost() { return dbHost; }

    /** get database port number */
    public String getDbPort() {	return dbPort; }

    /** get database name */
    public String getDbName() {	return dbName; }
    
    /** get database url */
    public String getDbUrl()
    {
	String result = null;
	if (dbHost==null || dbPort==null || dbName==null) return result;
	if (dbType.equals(dbTypeMySQL))
	    result = "jdbc:mysql://";
	else if (dbType.equals(dbTypeOracle))
	    result = "jdbc:oracle:thin:@//";
	else return result;
	result += dbHost + ":" + dbPort + "/" + dbName;
	return result;
    }

    /** get database user name */
    public String getDbUser() {	return dbUser; }
    
    /** get database password */
    public String getDbPassword() { return dbPwrd; }

    /** type choosen from the combo box */
    public void comboBoxDbSetupActionPerformed(ActionEvent e)
    {
	String setup = (String)comboBoxDbSetup.getSelectedItem();
	if (setup.equals(new String())) {
	    noneButton.setSelected(true);
	    textFieldDbHost.setText("");
	    textFieldDbPort.setText("");
	    textFieldDbName.setText("");
	    textFieldDbUser.setText("");
	    textFieldDbPwrd.setText("");
	    textFieldDbHost.requestFocusInWindow();
	    textFieldDbHost.selectAll();
	}
	else if (setup.equals("Tutorial Aug 1st 07")) {
	    mysqlButton.setSelected(true);
	    textFieldDbHost.setText("lxcmsmz01.cern.ch");
	    textFieldDbPort.setText("3306");
	    textFieldDbName.setText("HLTConfDB");
	    textFieldDbUser.setText("tutorial");
	    textFieldDbPwrd.setText("");
	    textFieldDbPwrd.requestFocusInWindow();
	    textFieldDbPwrd.selectAll();
	}
	else if (setup.equals("MySQL - local")) {
	    mysqlButton.setSelected(true);
	    textFieldDbHost.setText("localhost");
	    textFieldDbPort.setText("3306");
	    textFieldDbName.setText("hltdb");
	    textFieldDbUser.setText("username");
	    textFieldDbPwrd.setText("");
	    textFieldDbUser.requestFocusInWindow();
	    textFieldDbUser.selectAll();
	}
	else if (setup.equals("Oracle XE - local")) {
	    oracleButton.setSelected(true);
	    textFieldDbHost.setText("localhost");
	    textFieldDbPort.setText("1521");
	    textFieldDbName.setText("XE");
	    textFieldDbUser.setText("username");
	    textFieldDbPwrd.setText("");
	    textFieldDbUser.requestFocusInWindow();
	    textFieldDbUser.selectAll();
	}
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
		dbType = buttonGroupDbType.getSelection().getActionCommand();
		dbHost = textFieldDbHost.getText();
		dbPort = textFieldDbPort.getText();
		dbName = textFieldDbName.getText();
		dbUser = textFieldDbUser.getText();
		dbPwrd = new String(textFieldDbPwrd.getPassword());
		validChoice = true;
		setVisible(false);
	    }
	    else {
		dbType=null; dbHost=null; dbPort=null;
		dbName=null; dbUser=null; dbPwrd=null;
		setVisible(false);
		JOptionPane.showMessageDialog(optionPane.getRootFrame(),
					      "No database connection established.",
					      "",
					      JOptionPane.WARNING_MESSAGE);
	    }
	}
    }
    
}

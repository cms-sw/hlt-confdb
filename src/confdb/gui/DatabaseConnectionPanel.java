package confdb.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

/**
 * DatabaseConnectionPanel
 * -----------------------
 * @author Philipp Schieferdecker
 *
 * Panel to prompt the user for database connection information.
 */
public class DatabaseConnectionPanel extends JPanel implements FocusListener
{
    //
    // member data
    //

    /** possible choices of dbType*/
    public static final String DBTYPE_MYSQL  = "mysql";
    public static final String DBTYPE_ORACLE = "oracle";
    
    /** database type (mysql/oracle) */
    private ButtonGroup  buttonGroupDbType = null;
    private JRadioButton mysqlButton       = null;
    private JRadioButton oracleButton      = null;
    
    /** database host */
    private JTextField textFieldDbHost = null;

    /** database port */
    private JTextField textFieldDbPort = null;

    /** database name */
    private JTextField textFieldDbName = null;
    
    /** database user name */
    private JTextField textFieldDbUser = null;
    
    /** database user password */
    private JPasswordField textFieldDbPwrd = null;  

    
    //
    // construction
    //

    /** standard constructor */
    public DatabaseConnectionPanel()
    {
	super(new SpringLayout());

	// type
	JLabel labelDbType = new JLabel("Type:");
	mysqlButton = new JRadioButton("MySQL");
	mysqlButton.setMnemonic(KeyEvent.VK_M);
        mysqlButton.setActionCommand(DBTYPE_MYSQL);
        mysqlButton.setSelected(true);
	oracleButton = new JRadioButton("Oracle");
	oracleButton.setMnemonic(KeyEvent.VK_O);
        oracleButton.setActionCommand(DBTYPE_ORACLE);
	buttonGroupDbType = new ButtonGroup();
	buttonGroupDbType.add(mysqlButton);
	buttonGroupDbType.add(oracleButton);
	JPanel buttonPanel = new JPanel(new FlowLayout());
	labelDbType.setLabelFor(buttonPanel);
	buttonPanel.add(mysqlButton);
	buttonPanel.add(oracleButton);
	add(labelDbType);
	add(buttonPanel);
	
	// host
	JLabel labelDbHost = new JLabel("Host:");
	textFieldDbHost = new JTextField(12);
	labelDbHost.setLabelFor(textFieldDbHost);
	add(labelDbHost);
	add(textFieldDbHost);

	// port
	JLabel labelDbPort = new JLabel("Port:");
	textFieldDbPort = new JTextField(12);
	labelDbPort.setLabelFor(textFieldDbPort);
	add(labelDbPort);
	add(textFieldDbPort);

	// dbname
	JLabel labelDbName = new JLabel("Name:");
	textFieldDbName = new JTextField(12);
	labelDbName.setLabelFor(textFieldDbName);
	add(labelDbName);
	add(textFieldDbName);

	// user
	JLabel labelDbUser = new JLabel("User:");
	textFieldDbUser = new JTextField(12);
	labelDbUser.setLabelFor(textFieldDbUser);
	add(labelDbUser);
	add(textFieldDbUser);
	
	// pwd
	JLabel labelDbPwrd = new JLabel("Password:");
	textFieldDbPwrd = new JPasswordField(12);
	labelDbPwrd.setLabelFor(textFieldDbPwrd);
	add(labelDbPwrd);
	add(textFieldDbPwrd);

	SpringUtilities.makeCompactGrid(this,6,2,
					6,10,6,10);
	
	textFieldDbHost.addFocusListener(this);
	textFieldDbPort.addFocusListener(this);
	textFieldDbName.addFocusListener(this);
	textFieldDbUser.addFocusListener(this);
	textFieldDbPwrd.addFocusListener(this);
    }

    
    //
    // member functions
    //
    
    /** set defaults */
    public void setDefaults(String dbType,String dbHost,String dbPort,
			    String dbName,String dbUser,String dbPwrd)
    {
	if (dbType.equals(DBTYPE_MYSQL)) {
	    mysqlButton.setSelected(true);
	    oracleButton.setSelected(false);
	}
	else if (dbType.equals(DBTYPE_ORACLE)) {
	    mysqlButton.setSelected(false);
	    oracleButton.setSelected(true);
	}
	textFieldDbHost.setText(dbHost);
	textFieldDbPort.setText(dbPort);
	textFieldDbName.setText(dbName);
	textFieldDbUser.setText(dbUser);
	textFieldDbPwrd.setText(dbPwrd);
    }
    
    /** request focus on user field */
    public void requestFocusOnUser()
    {
	textFieldDbUser.requestFocusInWindow();
	textFieldDbUser.selectAll();
    }

    /** retrieve db type choice */
    public String dbType() 
    {
	return buttonGroupDbType.getSelection().getActionCommand();
    }
    
    /** retrieve db host choice */
    public String dbHost() { return textFieldDbHost.getText(); }

    /** retrieve db port choice */
    public String dbPort() { return textFieldDbPort.getText(); }

    /** retrieve db name choice */
    public String dbName() { return textFieldDbName.getText(); }

    /** retrieve db user choice */
    public String dbUser() { return textFieldDbUser.getText(); }

    /** retrieve db pwrd choice */
    public String dbPassword() { return new String(textFieldDbPwrd.getPassword()); }

    /** retrieve db url */
    public String dbUrl()
    {
	String result = null;
	if (dbHost()=="" || dbPort()=="" || dbName()=="") return result;
	if (dbType().equals(DBTYPE_MYSQL))       result = "jdbc:mysql://";
	else if (dbType().equals(DBTYPE_ORACLE)) result = "jdbc:oracle:thin:@//";
	else return result;
	result += dbHost() + ":" + dbPort() + "/" + dbName();
	return result;
    }
    
    /** add an external action listener */
    public void addActionListener(ActionListener l)
    {
	textFieldDbHost.addActionListener(l);
	textFieldDbPort.addActionListener(l);
	textFieldDbName.addActionListener(l);
	textFieldDbUser.addActionListener(l);
	textFieldDbPwrd.addActionListener(l);
    }

    /** FocusListener.focusGained() */
    public void focusGained(FocusEvent e)
    {
	JTextField textField = (JTextField)e.getComponent();
	if (textField!=null) textField.selectAll();
    }

    /**  FocusListener.focusLost() */
    public void focusLost(FocusEvent e) {}
    

}

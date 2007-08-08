package confdb.gui;

import javax.swing.*;
import java.awt.*;


/**
 * DatabaseInfoPanel
 * -----------------
 * @author Philipp Schieferdecker
 *
 * display the relevant information about the connection to the database.
 *
 */


public class DatabaseInfoPanel extends JPanel
{
    //
    // member data
    //
    
    /** connection status */
    private JLabel connectionStatus = null;
    
    /** database type */
    private JLabel labelDbType = null;
    private ImageIcon iconDbTypeMySQL = null;
    private ImageIcon iconDbTypeOracle = null;

    /** database host */
    private JLabel labelDbHost = null;
    private JLabel valueDbHost = null;

    /** database port */
    private JLabel labelDbPort = null;
    private JLabel valueDbPort = null;

    /** database host */
    private JLabel labelDbName = null;
    private JLabel valueDbName = null;

    /** database host */
    private JLabel labelDbUser = null;
    private JLabel valueDbUser = null;

    
    //
    // construction
    //

    /** default constructor */
    public DatabaseInfoPanel()
    {
	super(new FlowLayout());
	
	setBackground(Color.WHITE);

	connectionStatus = new JLabel("<html><font color=\"#ff0000\">" +
				      " <b>Disconnected</b> " +
				      "</font></html>");
	connectionStatus.setBorder(BorderFactory.createRaisedBevelBorder());
	
	labelDbType = new JLabel();
	iconDbTypeMySQL  = new ImageIcon(getClass().getResource("/mysql.gif"));
	iconDbTypeOracle = new ImageIcon(getClass().getResource("/oracle.gif"));

	labelDbHost = new JLabel("<html> <b>Host:</b></html>");
	labelDbPort = new JLabel("<html> <b>Port:</b></html>");
	labelDbName = new JLabel("<html> <b>Name:</b></html>");
	labelDbUser = new JLabel("<html> <b>User:</b></html>");
	
	valueDbHost = new JLabel(" - ");
	valueDbPort = new JLabel(" - ");
	valueDbName = new JLabel(" - ");
	valueDbUser = new JLabel(" - ");
	
	valueDbHost.setBorder(BorderFactory.createLoweredBevelBorder());
	valueDbPort.setBorder(BorderFactory.createLoweredBevelBorder());
	valueDbName.setBorder(BorderFactory.createLoweredBevelBorder());
	valueDbUser.setBorder(BorderFactory.createLoweredBevelBorder());
	
	add(labelDbType);
	add(connectionStatus);
	add(labelDbHost);
	add(valueDbHost);
	add(labelDbPort);
	add(valueDbPort);
	add(labelDbName);
	add(valueDbName);
	add(labelDbUser);
	add(valueDbUser);
    }
    

    //
    // member functions
    //
    
    /** connected to databse, update the database information */
    public void connectedToDatabase(String type,String host,String port,
				    String name,String user)
    {
	connectionStatus.setText("<html><font color=\"#00ff00\">" +
				 " <b>Connected</b> " +
				 "</color></html>");

	if (type.equals("mysql")) labelDbType.setIcon(iconDbTypeMySQL);
	if (type.equals("oracle")) labelDbType.setIcon(iconDbTypeOracle);
	
	valueDbHost.setText(" "+host+" ");
	valueDbPort.setText(" "+port+" ");
	valueDbName.setText(" "+name+" ");
	valueDbUser.setText(" "+user+" ");
    }

    /** connected to databse, update the database information */
    public void disconnectedFromDatabase()
    {
	connectionStatus.setText("<html><font color=\"#ff0000\">" +
				 " <b>Disconnected</b> " +
				 "</color></html>");

	labelDbType.setIcon(null);
	valueDbHost.setText(" - ");
	valueDbPort.setText(" - ");
	valueDbName.setText(" - ");
	valueDbUser.setText(" - ");
    }

}

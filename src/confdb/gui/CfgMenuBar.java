package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * MenuCommandHandler
 * ------------------
 * @author Philipp Schieferdecker
 *
 * handle ConfDbGUI menu bar comands.
 */
public class CfgMenuBar implements ActionListener
{
    //
    // member data
    //
    
    /** the parent application main frame */
    private JFrame frame = null;

    /** the parent application */
    private ConfDbGUI app = null;
    
    /** the menu bar */
    private JMenuBar menuBar = null;
    
    /** confdbMenu */
    private static final String confdbMenuAbout = "About";
    private static final String confdbMenuQuit  = "Quit";
    
    /** configMenu */
    private static final String configMenuNew        = "New";
    private static final String configMenuOpen       = "Open";
    private static final String configMenuClose      = "Close";
    private static final String configMenuSave       = "Save";
    private static final String configMenuSaveAs     = "Save As";
    private static final String configMenuMigrate    = "Migrate";

    /** dbMenu */
    private static final String dbMenuConnectToDB      = "Connect to DB";
    private static final String dbMenuDisconnectFromDB = "Disconnect from DB";
    private static final String dbMenuExportConfig     = "Export Configuration";
    private static final String dbMenuCreateTemplates  = "Create Templates";
    

    //
    // construction
    //
    public CfgMenuBar(JFrame frame,ConfDbGUI parentApp)
    {
	this.frame = frame;
	this.app = parentApp;
	
	menuBar = new JMenuBar();
	populateMenuBar();
	frame.setJMenuBar(menuBar);
    }
    
    
    //
    // memeber functions
    //
    
    /** populate the menu bar with all menus and their items */
    private void populateMenuBar()
    {
	JMenuItem menuItem;
	
	// the 'ConfDbGUI' menu
	JMenu confdbMenu = new JMenu("ConfDbGUI");
	confdbMenu.setMnemonic(KeyEvent.VK_E);
	menuBar.add(confdbMenu);
	menuItem = new JMenuItem(confdbMenuAbout,KeyEvent.VK_A);
	menuItem.addActionListener(this);
	confdbMenu.add(menuItem);
	menuItem = new JMenuItem(confdbMenuQuit,KeyEvent.VK_Q);
	menuItem.addActionListener(this);
	confdbMenu.add(menuItem);
	
	JMenu configMenu = new JMenu("Configurations");
	configMenu.setMnemonic(KeyEvent.VK_C);
	menuBar.add(configMenu);
	menuItem = new JMenuItem(configMenuNew,KeyEvent.VK_N);
	menuItem.addActionListener(this);
	configMenu.add(menuItem);
	menuItem = new JMenuItem(configMenuOpen,KeyEvent.VK_O);
	menuItem.addActionListener(this);
	configMenu.add(menuItem);
	configMenu.addSeparator();
	menuItem = new JMenuItem(configMenuClose,KeyEvent.VK_C);
	menuItem.addActionListener(this);
	configMenu.add(menuItem);
	menuItem = new JMenuItem(configMenuSave,KeyEvent.VK_S);
	menuItem.addActionListener(this);
	configMenu.add(menuItem);
	menuItem = new JMenuItem(configMenuSaveAs,KeyEvent.VK_A);
	menuItem.addActionListener(this);
	configMenu.add(menuItem);
	menuItem = new JMenuItem(configMenuMigrate,KeyEvent.VK_M);
	menuItem.addActionListener(this);
	configMenu.add(menuItem);
	
	JMenu dbMenu = new JMenu("Database");
	dbMenu.setMnemonic(KeyEvent.VK_D);
	menuBar.add(dbMenu);
	menuItem = new JMenuItem(dbMenuConnectToDB,KeyEvent.VK_C);
	menuItem.addActionListener(this);
	dbMenu.add(menuItem);
	menuItem = new JMenuItem(dbMenuDisconnectFromDB,KeyEvent.VK_D);
	menuItem.addActionListener(this);
	dbMenu.add(menuItem);
	menuItem = new JMenuItem(dbMenuExportConfig,KeyEvent.VK_E);
	menuItem.addActionListener(this);
	dbMenu.add(menuItem);
	menuItem = new JMenuItem(dbMenuCreateTemplates,KeyEvent.VK_T);
	menuItem.addActionListener(this);
	dbMenu.add(menuItem);
	
    }
    
    /** ActionListener callback to handle each and every menu commands */
    public void actionPerformed(ActionEvent e)
    {
	JMenuItem source = (JMenuItem)(e.getSource());
	String    command = source.getText();
	
	// confdbMenu
	if (command.equals(confdbMenuAbout)) {
	    AboutDialog dialog = new AboutDialog(frame);
	    dialog.pack();
	    dialog.setLocationRelativeTo(frame);
	    dialog.setVisible(true);
	}
	if (command.equals(confdbMenuQuit)) {
	    app.closeConfiguration();
	    app.disconnectFromDatabase();
	    System.exit(0);
	}
	
	// configMenu
	if (command.equals(configMenuNew))     app.newConfiguration();
	if (command.equals(configMenuOpen))    app.openConfiguration();
	if (command.equals(configMenuClose))   app.closeConfiguration();
	if (command.equals(configMenuSave))    app.saveConfiguration();
	if (command.equals(configMenuSaveAs))  app.saveAsConfiguration();
	if (command.equals(configMenuMigrate)) app.migrateConfiguration();
	
	// dbMenu
	if (command.equals(dbMenuConnectToDB))      app.connectToDatabase();
	if (command.equals(dbMenuDisconnectFromDB)) app.disconnectFromDatabase();
	if (command.equals(dbMenuExportConfig))     app.exportConfiguration();
	if (command.equals(dbMenuCreateTemplates))  app.createTemplates();
    }

}

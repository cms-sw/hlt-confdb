package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * ConfDBMenuBar
 * -------------
 * @author Philipp Schieferdecker
 *
 * handle ConfDbGUI menu bar comands.
 */
public class ConfDBMenuBar implements ActionListener
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

    /** menu bar item names: confdbMenu */
    private static final String confdbMenuAbout = "About";
    private static final String confdbMenuQuit  = "Quit";
    
    /** menu bar item names: configMenu */
    private static final String configMenuNew        = "New";
    private static final String configMenuParse      = "Parse";
    private static final String configMenuOpen       = "Open";
    private static final String configMenuClose      = "Close";
    private static final String configMenuSave       = "Save";
    private static final String configMenuSaveAs     = "Save As";
    private static final String configMenuImport     = "Import";
    private static final String configMenuMigrate    = "Migrate";

    /** menu bar item names: dbMenu */
    private static final String dbMenuConnectToDB      = "Connect to DB";
    private static final String dbMenuDisconnectFromDB = "Disconnect from DB";
    private static final String dbMenuExportConfig     = "Export Configuration";
    private static final String dbMenuCreateTemplates  = "Create Templates";
    
    /** meny bar items */
    private JMenuItem confdbMenuAboutItem       = null;
    private JMenuItem confdbMenuQuitItem        = null;
    
    private JMenuItem configMenuNewItem         = null;
    private JMenuItem configMenuParseItem       = null;
    private JMenuItem configMenuOpenItem        = null;
    private JMenuItem configMenuCloseItem       = null;
    private JMenuItem configMenuSaveItem        = null;
    private JMenuItem configMenuSaveAsItem      = null;
    private JMenuItem configMenuImportItem      = null;
    private JMenuItem configMenuMigrateItem     = null;
    
    private JMenuItem dbMenuConnectItem         = null;
    private JMenuItem dbMenuDisconnectItem      = null;
    private JMenuItem dbMenuExportItem          = null;
    private JMenuItem dbMenuCreateTemplatesItem = null;
    
    

    //
    // construction
    //
    public ConfDBMenuBar(JFrame frame,ConfDbGUI parentApp,boolean enableParse)
    {
	this.frame = frame;
	this.app = parentApp;
	
	menuBar = new JMenuBar();
	populateMenuBar(enableParse);
	frame.setJMenuBar(menuBar);
    }
    
    
    //
    // memeber functions
    //
    
    /** a configuration was opened */
    public void configurationIsOpen()
    {
	dbConnectionIsEstablished();
	configMenuNewItem.setEnabled(true);
	if (configMenuParseItem!=null) configMenuParseItem.setEnabled(true);
	configMenuOpenItem.setEnabled(true);
	configMenuCloseItem.setEnabled(true);
	configMenuSaveItem.setEnabled(true);
	configMenuSaveAsItem.setEnabled(true);
	configMenuImportItem.setEnabled(true);
	configMenuMigrateItem.setEnabled(true);
	dbMenuExportItem.setEnabled(true);
    }

    /** no configuration is open */
    public void configurationIsNotOpen()
    {
	configMenuCloseItem.setEnabled(false);
	configMenuSaveItem.setEnabled(false);
	configMenuSaveAsItem.setEnabled(false);
	configMenuImportItem.setEnabled(false);
	configMenuMigrateItem.setEnabled(false);
	dbMenuExportItem.setEnabled(false);
    }
    
    /** database connection is established */
    public void dbConnectionIsEstablished()
    {
	configMenuNewItem.setEnabled(true);
	if (configMenuParseItem!=null) configMenuParseItem.setEnabled(true);
	configMenuOpenItem.setEnabled(true);
	dbMenuDisconnectItem.setEnabled(true);
	dbMenuCreateTemplatesItem.setEnabled(true);
    }
    
    /** no database connection is established */
    public void dbConnectionIsNotEstablished()
    {
	configurationIsNotOpen();
	configMenuNewItem.setEnabled(false);
	if (configMenuParseItem!=null) configMenuParseItem.setEnabled(false);
	configMenuOpenItem.setEnabled(false);
	dbMenuDisconnectItem.setEnabled(false);
	dbMenuCreateTemplatesItem.setEnabled(false);
    }

    /** populate the menu bar with all menus and their items */
    private void populateMenuBar(boolean enableParse)
    {
	JMenuItem menuItem;
	
	// the 'ConfDbGUI' menu
	JMenu confdbMenu = new JMenu("ConfDbGUI");
	confdbMenu.setMnemonic(KeyEvent.VK_E);
	menuBar.add(confdbMenu);
	confdbMenuAboutItem = new JMenuItem(confdbMenuAbout,KeyEvent.VK_A);
	confdbMenuAboutItem.addActionListener(this);
	confdbMenu.add(confdbMenuAboutItem);
	confdbMenuQuitItem = new JMenuItem(confdbMenuQuit,KeyEvent.VK_Q);
	confdbMenuQuitItem.addActionListener(this);
	confdbMenu.add(confdbMenuQuitItem);
	
	JMenu configMenu = new JMenu("Configurations");
	configMenu.setMnemonic(KeyEvent.VK_C);
	menuBar.add(configMenu);
	configMenuNewItem = new JMenuItem(configMenuNew,KeyEvent.VK_N);
	configMenuNewItem.addActionListener(this);
	configMenu.add(configMenuNewItem);
	if (enableParse) {
	    configMenuParseItem = new JMenuItem(configMenuParse,KeyEvent.VK_P);
	    configMenuParseItem.addActionListener(this);
	    configMenu.add(configMenuParseItem);
	}
	configMenuOpenItem = new JMenuItem(configMenuOpen,KeyEvent.VK_O);
	configMenuOpenItem.addActionListener(this);
	configMenu.add(configMenuOpenItem);
	configMenuCloseItem = new JMenuItem(configMenuClose,KeyEvent.VK_C);
	configMenuCloseItem.addActionListener(this);
	configMenu.add(configMenuCloseItem);
	configMenu.addSeparator();
	configMenuSaveItem = new JMenuItem(configMenuSave,KeyEvent.VK_S);
	configMenuSaveItem.addActionListener(this);
	configMenu.add(configMenuSaveItem);
	configMenuSaveAsItem = new JMenuItem(configMenuSaveAs,KeyEvent.VK_A);
	configMenuSaveAsItem.addActionListener(this);
	configMenu.add(configMenuSaveAsItem);
	configMenu.addSeparator();
	configMenuImportItem = new JMenuItem(configMenuImport,KeyEvent.VK_I);
	configMenuImportItem.addActionListener(this);
	configMenu.add(configMenuImportItem);
	configMenuMigrateItem = new JMenuItem(configMenuMigrate,KeyEvent.VK_M);
	configMenuMigrateItem.addActionListener(this);
	configMenu.add(configMenuMigrateItem);
	
	JMenu dbMenu = new JMenu("Database");
	dbMenu.setMnemonic(KeyEvent.VK_D);
	menuBar.add(dbMenu);
	dbMenuConnectItem = new JMenuItem(dbMenuConnectToDB,KeyEvent.VK_C);
	dbMenuConnectItem.addActionListener(this);
	dbMenu.add(dbMenuConnectItem);
	dbMenuDisconnectItem = new JMenuItem(dbMenuDisconnectFromDB,KeyEvent.VK_D);
	dbMenuDisconnectItem.addActionListener(this);
	dbMenu.add(dbMenuDisconnectItem);
	dbMenuExportItem = new JMenuItem(dbMenuExportConfig,KeyEvent.VK_E);
	dbMenuExportItem.addActionListener(this);
	dbMenu.add(dbMenuExportItem);
	dbMenuCreateTemplatesItem=new JMenuItem(dbMenuCreateTemplates,KeyEvent.VK_T);
	dbMenuCreateTemplatesItem.addActionListener(this);
	dbMenu.add(dbMenuCreateTemplatesItem);
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
	if (command.equals(configMenuParse))   app.parseConfiguration();
	if (command.equals(configMenuOpen))    app.openConfiguration();
	if (command.equals(configMenuClose))   app.closeConfiguration();
	if (command.equals(configMenuSave))    app.saveConfiguration();
	if (command.equals(configMenuSaveAs))  app.saveAsConfiguration();
	if (command.equals(configMenuImport))  app.importConfiguration();
	if (command.equals(configMenuMigrate)) app.migrateConfiguration();
	
	// dbMenu
	if (command.equals(dbMenuConnectToDB))      app.connectToDatabase();
	if (command.equals(dbMenuDisconnectFromDB)) app.disconnectFromDatabase();
	if (command.equals(dbMenuExportConfig))     app.exportConfiguration();
	if (command.equals(dbMenuCreateTemplates))  app.createTemplates();
    }

}

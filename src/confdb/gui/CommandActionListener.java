package confdb.gui;

import javax.swing.*;
import java.awt.event.*;


/**
 * CommandActionListener
 * ---------------------
 * @author Philipp Schieferdecker
 */
public class CommandActionListener implements ActionListener
{
    /** reference to the gui application */
    private ConfDbGUI app = null;

    /** action commands */
    private static final String cmdAbout            = "About";
    private static final String cmdQuit             = "Quit";

    private static final String cmdNew              = "New";
    private static final String cmdParse            = "Parse";
    private static final String cmdOpen             = "Open";
    private static final String cmdClose            = "Close";
    private static final String cmdSave             = "Save";
    private static final String cmdCommentSave      = "Comment&Save";
    private static final String cmdSaveAs           = "Save As";

    private static final String cmdImport           = "Import";
    private static final String cmdMigrate          = "Migrate";
    private static final String cmdConvert          = "Convert";
    
    private static final String cmdReplace          = "Search&Replace";
    private static final String cmdDiff             = "Compare (Diff)";
    private static final String cmdOMEditor         = "Edit OutputModules";
    private static final String cmdPSEditor         = "Edit Prescales";
    private static final String cmdMLEditor         = "Edit MessageLogger";


    private static final String cmdTrack            = "Track InputTags";
    
    private static final String cmdConnectToDB      = "Connect to DB";
    private static final String cmdDisconnectFromDB = "Disconnect from DB";
    private static final String cmdExportConfig     = "Export Configuration";
    
    /** standard constructor */
    public CommandActionListener(ConfDbGUI app)
    {
	this.app = app;
    }

    /** ActionListener.actionPerformed() */
    public void actionPerformed(ActionEvent e)
    {
	AbstractButton source  = (AbstractButton)(e.getSource());
	String         command = source.getActionCommand();
	
	System.out.println("In comand action listener");

	if (command.equals(cmdAbout))            app.showAboutDialog();
	if (command.equals(cmdQuit))             app.quitApplication();

	if (command.equals(cmdNew))              app.newConfiguration();
	if (command.equals(cmdParse))            app.parseConfiguration();
	if (command.equals(cmdOpen))             app.openConfiguration();
	if (command.equals(cmdClose))            app.closeConfiguration();
	if (command.equals(cmdSave))             app.saveConfiguration(false);
	if (command.equals(cmdCommentSave))      app.saveConfiguration(true);
	if (command.equals(cmdSaveAs))           app.saveAsConfiguration();

	if (command.equals(cmdImport))           app.importConfiguration();
	if (command.equals(cmdMigrate))          app.migrateConfiguration();
	if (command.equals(cmdConvert))          app.convertConfiguration();
	
	if (command.equals(cmdReplace))          app.searchAndReplace();
	if (command.equals(cmdDiff))             app.diffConfigurations();
	if (command.equals(cmdOMEditor))         app.openOutputModuleEditor();
	if (command.equals(cmdPSEditor))         app.openPrescaleEditor();
	if (command.equals(cmdMLEditor))         app.openMessageLoggerEditor();
	
	if (command.equals(cmdTrack)) app.setOptionTrackInputTags(source.
								  isSelected());

	if (command.equals(cmdConnectToDB))      app.connectToDatabase();
	if (command.equals(cmdDisconnectFromDB)) app.disconnectFromDatabase();
	if (command.equals(cmdExportConfig))     app.exportConfiguration();
    }

}

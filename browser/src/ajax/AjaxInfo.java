
package ajax;

import java.util.ArrayList;

import javax.servlet.ServletContext;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import confdb.converter.BrowserConverter;
import confdb.converter.ConverterBase;
import confdb.converter.ConverterException;
import confdb.converter.DbProperties;
import confdb.converter.RcmsDbProperties;
import confdb.data.IConfiguration;
import confdb.db.ConfDBSetups;


public class AjaxInfo implements Runnable
{
	private Flag flag = new Flag();
	static private Thread thread = null;
	static private AjaxInfo instance = null;
	
	static {
		Logger logger = Logger.getLogger( "org.directwebremoting" );
		ConsoleAppender consoleAppender = new ConsoleAppender( new SimpleLayout() );
		logger.addAppender( consoleAppender );
		// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
		logger.setLevel( Level.WARN );
		logger = Logger.getLogger( "ajax" );
		logger.removeAllAppenders();
		PatternLayout layout = new PatternLayout( "%d{HH:mm:ss} %p - %m%n" );
		logger.addAppender( new ConsoleAppender(  layout ) );
		logger.setLevel( Level.DEBUG );
		logger.info( "AjaxInfo loaded" );
	}

	public AjaxInfo()
    {
		WebContext webContext = WebContextFactory.get();
		if ( webContext == null )
			return;
		//currentPage = webContext.getCurrentPage();
		ServletContext servletContext = webContext.getServletContext();
		if ( servletContext == null )
			return;
		//ServerContext serverContext = ServerContextFactory.get( servletContext );
		webContext.getScriptSessionsByPage( "" );  // have a look at publisher demo!
    	if ( thread == null )
    	{
    		thread = new Thread( this );
    		thread.start();
    	}
    	if ( instance == null )
    		instance = this;
    }

    public String[] listDBs()
    {
    	return BrowserConverter.listDBs();
    }
    
    public String[] getAnchors( int dbIndex, int configKey )
    {
    	return BrowserConverter.getAnchors( dbIndex, configKey );
    }
    
	public String getRcmsDbInfo()
	{
		DbProperties dbProperties;
		try {
			dbProperties = DbProperties.getDefaultDbProperties();
		} catch (Exception e1) {
			return e1.toString();
		}
		if ( !(dbProperties instanceof RcmsDbProperties) )
		{
			try {
				new RcmsDbProperties();
			} catch (Exception e) {
				return e.toString();
			}
		}
		
		return dbProperties.getDbURL();
	}
	

    
	public void run()
	{
		while ( thread != null )
		{
			synchronized( flag )
			{
				try {
					flag.wait( 30000 );
				} catch (InterruptedException e) {
				}
				if ( !flag.set )
				{
				}
				flag.set =false;
			}
		}
		
	}
	
    class Flag
    {
		private static final long serialVersionUID = 1L;
		boolean set = false;
    }

}

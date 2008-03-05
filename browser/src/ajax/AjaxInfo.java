
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

import rcms.fm.context.RCMSRuntimeInfo;

import browser.BrowserConverter;

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
		RCMSRuntimeInfo.servletContext = servletContext;
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
    	ConfDBSetups dbs = new ConfDBSetups();
    	ArrayList<String> list = new ArrayList<String>();
     	for ( int i = 0; i < dbs.setupCount(); i++ )
     	{
    		String name = dbs.name(i);
    		if ( name != null && name.length() > 0  )
    		{
    			String host = dbs.host(i);
    			if (     host != null 
       				 && !host.equalsIgnoreCase("localhost") 
   				     && !host.endsWith( ".cms") )
   				     {
   				    	 list.add( dbs.labelsAsArray()[i] );
   				     }
    		}
    	}
    	return list.toArray( new String[ list.size() ] );
    }
    
    public String[] getAnchors( int dbIndex, int configKey )
    {
		ArrayList<String> list = new ArrayList<String>();
		ConverterBase converter = null;
    	try {
			converter = BrowserConverter.getConverter( dbIndex );
			IConfiguration conf = converter.getConfiguration( configKey );
			if ( conf == null )
				list.add( "??" );
			else
			{
				if ( conf.pathCount() > 0 )
					list.add( "paths" );
				if ( conf.sequenceCount() > 0 )
					list.add( "sequences" );
				if ( conf.moduleCount() > 0 )
					list.add( "modules" );
				if ( conf.edsourceCount() > 0 )
					list.add( "ed_sources" );
				if ( conf.essourceCount() > 0 )
					list.add( "es_sources" );
				if ( conf.esmoduleCount() > 0 )
					list.add( "es_modules" );
				if ( conf.serviceCount() > 0 )
					list.add( "services" );
			}
		} catch (ConverterException e) {
			list.add( e.toString() );
			if ( converter != null )
				BrowserConverter.deleteConverter( converter );
		}
		return list.toArray( new String[ list.size() ] );
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

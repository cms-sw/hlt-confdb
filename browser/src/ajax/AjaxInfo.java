
package ajax;

import java.util.ArrayList;

import javax.servlet.ServletContext;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.directwebremoting.ServerContext;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import confdb.converter.ConfCache;
import confdb.data.Configuration;
import confdb.db.ConfDBSetups;


public class AjaxInfo implements Runnable
{
	private ServerContext serverContext;
	private String currentPage;
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
		currentPage = webContext.getCurrentPage();
		ServletContext servletContext = webContext.getServletContext();
		if ( servletContext == null )
			return;
		serverContext = ServerContextFactory.get( servletContext );
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
    			if ( host != null && !host.equalsIgnoreCase("localhost") )
    				list.add( dbs.labelsAsArray()[i] );
    			
    		}
    	}
    	return list.toArray( new String[ list.size() ] );
    }
    
    public String[] getAnchors( int dbIndex, int configKey )
    {
    	String cacheKey = "db:" + dbIndex + " key:" + configKey;
    	ConfCache cache = ConfCache.getInstance();
    	Configuration conf = cache.getConf( cacheKey  );
    	ArrayList<String> list = new ArrayList<String>();
    	if ( conf == null )
    		list.add( "??" );
    	else
    	{
    		if ( conf.pathCount() > 0 )
    			list.add( "paths" );
    		if ( conf.moduleCount() > 0 )
    			list.add( "modules" );
    	}
    	return list.toArray( new String[ list.size() ] );
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

package browser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import confdb.converter.ConverterBase;
import confdb.converter.ConverterException;
import confdb.converter.DbProperties;
import confdb.data.IConfiguration;
import confdb.db.ConfDB;
import confdb.db.ConfDBSetups;

public class BrowserConverter extends ConverterBase 
{
	static private HashMap<Integer,BrowserConverter> map = new HashMap<Integer,BrowserConverter>();

	private BrowserConverter( String dbType, String dbUrl, String dbUser, String dbPwrd ) throws ConverterException
	{
		super( "HTML", dbType, dbUrl, dbUser, dbPwrd );	
	}
		
	public ConfDB getDatabase()
	{
		return super.getDatabase();
	}
	
	public IConfiguration getConfiguration( int key )
	{
		return super.getConfiguration( key );
	}
		
	public String convert( IConfiguration conf )
	{
		return getConverterEngine().convert( conf );
	}

	protected void finalize() throws Throwable
	{
		super.finalize();
		ConfDB db = getDatabase();
		if ( db != null )
			db.disconnect();
	}
	
	static public BrowserConverter getConverter( int dbIndex ) throws ConverterException 
	{
		ConfDBSetups dbs = new ConfDBSetups();
	    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
	   	dbProperties.setDbUser( "cms_hlt_reader" );
		BrowserConverter converter = map.get( new Integer( dbIndex ) );
		if ( converter == null )
		{
			converter = new BrowserConverter( dbs.type( dbIndex ), dbProperties.getDbURL(), dbProperties.getDbUser(), "convertme!" );		
			map.put( new Integer( dbIndex ), converter );
		}
		return converter;
	}

	static public void deleteConverter( BrowserConverter converter )
	{
		Set<Map.Entry<Integer,BrowserConverter>> entries = map.entrySet();
		for ( Map.Entry<Integer,BrowserConverter> entry : entries )
		{
			if ( entry.getValue() == converter )
			{
				map.remove( entry.getKey() );
				return;
			}
		}
	}
	
	
}

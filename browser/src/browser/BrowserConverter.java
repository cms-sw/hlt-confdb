package browser;

import java.util.HashMap;

import confdb.converter.ConverterBase;
import confdb.converter.ConverterException;
import confdb.converter.DbProperties;
import confdb.data.IConfiguration;
import confdb.db.ConfDBSetups;

public class BrowserConverter extends ConverterBase 
{
	static private HashMap<Integer,BrowserConverter> map = new HashMap<Integer,BrowserConverter>();

	private BrowserConverter( String dbUrl, String dbUser, String dbPwrd ) throws ConverterException
	{
		super( "HTML", "oracle", dbUrl, dbUser, dbPwrd );	
	}
		
	public IConfiguration getConfiguration( int key )
	{
		return super.getConfiguration( key );
	}
		
	public String convert( IConfiguration conf )
	{
		return getConverterEngine().convert( conf );
	}

	static public BrowserConverter getConverter( int dbIndex ) throws ConverterException 
	{
		ConfDBSetups dbs = new ConfDBSetups();
	    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
	   	dbProperties.setDbUser( "cms_hlt_reader" );
		BrowserConverter converter = map.get( new Integer( dbIndex ) );
		if ( converter == null )
		{
			converter = new BrowserConverter( dbProperties.getDbURL(), dbProperties.getDbUser(), "convertme!" );		
			map.put( new Integer( dbIndex ), converter );
		}
		return converter;
	}


	
	
}

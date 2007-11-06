package confdb.converter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import confdb.data.IConfiguration;
import confdb.db.ConfDB;
import confdb.db.DatabaseException;

public class ConverterBase 
{
	private ConfDB database = null;
	private ConverterEngine converterEngine = null;
	
	private HashMap<Integer, ConfWrapper> confCache = new HashMap<Integer, ConfWrapper>();
	private int maxCacheEntries = 10;
	

	
	protected ConverterBase( String format, Connection connection ) throws DatabaseException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
		database = new ConfDB();
		database.connect( connection );
		converterEngine = ConverterFactory.getConverterEngine( format );
    }
	
    protected ConverterBase( String format,
			                 String dbType, String dbUrl, String dbUser, String dbPwrd ) throws DatabaseException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
    	database = new ConfDB();
    	database.connect( dbType, dbUrl, dbUser, dbPwrd );
		converterEngine = ConverterFactory.getConverterEngine( format );
    }

    
	public int getMaxCacheEntries() {
		return maxCacheEntries;
	}

	public void setMaxCacheEntries(int maxCacheEntries) {
		this.maxCacheEntries = maxCacheEntries;
	}

    
	
	protected ConfDB getDatabase() 
	{
		return database;
	}
	
	protected IConfiguration getConfiguration( int key ) 
	{
		ConfWrapper conf = confCache.get( new Integer( key ) );
		if ( conf != null )
			return conf.getConfiguration();
		IConfiguration configuration = database.loadConfiguration( key );
		put( key, configuration );
		return configuration;
	}
		
	synchronized private void put( Integer key, IConfiguration conf )
	{
		if ( confCache.size() > maxCacheEntries )
		{
			List<ConfWrapper> list = 
					new ArrayList<ConfWrapper>( confCache.values() );
			Collections.sort(list);
			confCache.remove( list.get(0).getKey() );
		}
		confCache.put( key, new ConfWrapper( key, conf ) );
	}
		
	protected ConverterEngine getConverterEngine() 
	{
		return converterEngine;
	}

	
	private class ConfWrapper implements Comparable<ConfWrapper>
	{
		private Integer key;
		private IConfiguration configuration = null;
		private long timestamp;
		
		ConfWrapper( Integer key, IConfiguration conf ) 
		{
			this.key = key;
			configuration = conf;
			timestamp = System.currentTimeMillis();
		}

		IConfiguration getConfiguration() 
		{
			timestamp = System.currentTimeMillis();
			return configuration;
		}


		public int compareTo(ConfWrapper o) 
		{
			return (int)(timestamp - o.timestamp);
		}
		
		public Integer getKey()
		{
			return key;
		}
	}


}

package confdb.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import confdb.data.IConfiguration;
import confdb.db.ConfDB;
import confdb.db.DatabaseException;

public class ConfCache 
{
	static private ConfCache cache = null;
    static private int fillUpToMB = 40;
    static private int clearLessThanMB = 30;
	
    private HashMap<String, ConfWrapper> confCache = new HashMap<String, ConfWrapper>();

    static public ConfCache getCache()
    {
    	if ( cache == null )
    		cache = new ConfCache();
    	return cache;
    }
	
    static public int getNumberCacheEntries() 
    {
    	return getCache().confCache.size();
    }

    static public void clearCache()
    {
        getCache().confCache = new HashMap<String, ConfWrapper>();
    }
	
    public IConfiguration getConfiguration( int key, ConfDB database ) throws DatabaseException
    {
		ConfWrapper conf = confCache.get( getCacheKey( key, database ) );
		if ( conf != null )
			return conf.getConfiguration();
		IConfiguration configuration = null;
		Runtime.getRuntime().gc();
		try {
			configuration = database.loadConfiguration(key);
		} catch (DatabaseException e) {
			// wait, reconnect and try again
			database.disconnect();
			try {
				Thread.sleep( 2000 );
			} catch (InterruptedException e1) {
			}
			database.connect();
			configuration = database.loadConfiguration(key);
		}
		put( key, configuration, database );
		return configuration;
    }
		
    synchronized private void put( int key, IConfiguration conf, ConfDB database )
    {
    	Runtime runtime = Runtime.getRuntime();
    	float freeMemory = 
    		( runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory() ) / 1024 /1024;
    	if ( freeMemory < fillUpToMB )
	    {
        	if ( freeMemory < clearLessThanMB )
        		confCache.clear();
        	else
        	{
        		List<ConfWrapper> list = new ArrayList<ConfWrapper>( confCache.values() );
        		Collections.sort(list);
        		confCache.remove( list.get(0).getCacheKey() );
        	}
	    }
    	confCache.put( getCacheKey( key,database ), new ConfWrapper( getCacheKey( key, database ), conf ) );
    }

    
	private String getCacheKey( int key, ConfDB database )
	{
		return "" + key + "@" + database.dbUrl();
	}
	
	
    private class ConfWrapper implements Comparable<ConfWrapper>
    {
    	private String cacheKey;
    	private IConfiguration configuration = null;
    	private long timestamp;
		
    	ConfWrapper( String key, IConfiguration conf ) 
    	{
    		cacheKey = key;
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
		
    	public String getCacheKey()
    	{
    		return cacheKey;
    	}
    }


	

}

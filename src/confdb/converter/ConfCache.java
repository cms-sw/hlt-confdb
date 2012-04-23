package confdb.converter;


import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import confdb.data.IConfiguration;
import confdb.db.ConfDB;
import confdb.db.DatabaseException;

public class ConfCache 
{
	static private ConfCache cache = null;
    static private int fillUpToMB = 40;
    static private int clearLessThanMB = 30;
    static private DiskCache diskCache = null;
	
    private HashMap<String, ConfWrapper> confCache = new HashMap<String, ConfWrapper>();
    private int allRequests = 0;
    private Statistics dbRequests = new Statistics();
    private DbLock dbLock = new DbLock();


    static public ConfCache getCache()
    {
    	if ( cache == null )
    	{
    		cache = new ConfCache();
    		long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
    		clearLessThanMB = Math.min( 30, (int)max / 4 );
    		fillUpToMB = clearLessThanMB + 10;
    		try {
				setPath( "/afs/cern.ch/project/jps/reps/cms-project-co", 485 );
			} catch (Exception e) {
	    		try {
					setPath( "/afs/cern.ch/project/jps/reps/cms-confdb", 485 );
				} catch (Exception e2) {
					diskCache = null;
				}
			}
    	}
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
	
    static public int checkSoftReferences() 
    {
    	return getCache().checkMySoftReferences();
    }
    
    static public void setPath( String p, int size  ) throws IOException, SecurityException
    {
    	if ( p == null || size <= 0 )
    		diskCache = null;
    	else
    		diskCache = new DiskCache( p, size, 10 );
    }

    public IConfiguration getConfiguration( int key, ConfDB database ) throws DatabaseException
    {
    	allRequests += 1;
		IConfiguration configuration = getConfigFromCache(key, database);
		if ( configuration != null )
			return configuration;
		
		synchronized( dbLock ) {
			dbLock.configKey = getCacheKey(key, database);
			configuration = getConfigFromCache(key, database);
			if ( configuration == null )
			{
				long start = System.currentTimeMillis();

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
				dbRequests.add( System.currentTimeMillis() - start );
				put( key, configuration, database );
				if ( diskCache != null )
					writeToDisk( key, configuration, database );
			}
		}
		return configuration;
    }

    private synchronized IConfiguration getConfigFromCache( int key, ConfDB database )
    {
		IConfiguration configuration = null;
		ConfWrapper conf = confCache.get( getCacheKey( key, database ) );
		if ( conf != null )
		{
			configuration = conf.getConfiguration();
			if ( configuration != null )
				return configuration;
			confCache.remove( conf.getCacheKey() );
		}
		
		if ( diskCache != null )
		{
			configuration = loadFromDisk(key, database);
			if ( configuration != null )
			{
				put( key, configuration, database );
				return configuration;
			}
		}
		
		return null;
    }
    
    private void writeToDisk( int key, IConfiguration conf, ConfDB database )
    {
    	diskCache.writeToDisk( conf, getCacheKey(key, database) );
    }
    
    private IConfiguration loadFromDisk( int key, ConfDB database )
    {
    	return (IConfiguration)diskCache.loadFromDisk( getCacheKey(key, database) );
    }
    
    private void put( int key, IConfiguration conf, ConfDB database )
    {
    	Runtime runtime = Runtime.getRuntime();
    	float freeMemory = 
    		( runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory() ) / 1024 /1024;
    	if ( freeMemory < fillUpToMB )
	    {
    		List<ConfWrapper> list = new ArrayList<ConfWrapper>( confCache.values() );
    		if ( list.size() > 0 )
    		{
    		  Collections.sort(list);
    		  String key1 = list.get(0).getCacheKey();
        	  if ( freeMemory < clearLessThanMB  &&  list.size() > 1 )
        		confCache.remove( list.get(1).getCacheKey() );
    		  confCache.remove( key1 );
    		}
	    }
    	confCache.put( getCacheKey( key,database ), new ConfWrapper( getCacheKey( key, database ), conf ) );
    	if ( confCache.size() > 20 )
    		checkSoftReferences();
    }

    
	private String getCacheKey( int key, ConfDB database )
	{
		return (database.dbUrl() + " " + database.getDbUser()).hashCode() + "." + key;
	}
	
    private synchronized int checkMySoftReferences() 
    {
    	int dereferenced = 0;
    	for ( Iterator<ConfWrapper> i = confCache.values().iterator(); i.hasNext(); )
    	{
    		if ( i.next().getConfiguration() == null )
    		{
    			dereferenced += 1;
    			i.remove();
    		}
    	}
    	return dereferenced;
    }

    private class ConfWrapper implements Comparable<ConfWrapper>
    {
    	private String cacheKey;
    	private SoftReference<IConfiguration> configuration = null;
    	private long timestamp;
		
    	ConfWrapper( String key, IConfiguration conf ) 
    	{
    		cacheKey = key;
    		configuration = new SoftReference<IConfiguration>( conf );
    		timestamp = System.currentTimeMillis();
    	}

    	IConfiguration getConfiguration() 
    	{
    		timestamp = System.currentTimeMillis();
    		return configuration.get();
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



	public int getAllRequests() {
		return allRequests;
	}

	public Statistics getDbRequests() {
		return dbRequests;
	}

	public DiskCache getDiskCache() {
		return diskCache;
	}

	class DbLock {
		String configKey = "";
	}
	
	
}




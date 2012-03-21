package confdb.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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
    static private String path = null;
	
    private HashMap<String, ConfWrapper> confCache = new HashMap<String, ConfWrapper>();
    private int allRequests = 0;
    private Statistics diskcacheHits = new Statistics();
    private Statistics dbRequests = new Statistics();
    private Statistics serialize = new Statistics();

    static public ConfCache getCache()
    {
    	if ( cache == null )
    	{
    		cache = new ConfCache();
    		long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
    		clearLessThanMB = Math.min( 30, (int)max / 4 );
    		fillUpToMB = clearLessThanMB + 10;
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
    
    static public void setPath( String p ) throws IOException, SecurityException
    {
    	File file = new File( p + ".start" );
    	if ( file.exists() )
    		file.delete();
    	if ( !file.createNewFile() )
    		return;
    	path = p;
    }

    public synchronized IConfiguration getConfiguration( int key, ConfDB database ) throws DatabaseException
    {
    	allRequests += 1;
		IConfiguration configuration = null;
		ConfWrapper conf = confCache.get( getCacheKey( key, database ) );
		if ( conf != null )
		{
			configuration = conf.getConfiguration();
			if ( configuration != null )
				return configuration;
			confCache.remove( conf.getCacheKey() );
		}
		
    	long start = System.currentTimeMillis();
		if ( path != null )
		{
			configuration = loadFromDisk(key, database);
			if ( configuration != null )
			{
				put( key, configuration, database );
				diskcacheHits.add( System.currentTimeMillis() - start );
				return configuration;
			}
		}
		
		//Runtime.getRuntime().gc();
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
		if ( path != null )
			writeToDisk( key, configuration, database );
		return configuration;
    }

    private void writeToDisk( int key, IConfiguration conf, ConfDB database )
    {
    	long start = System.currentTimeMillis();
    	String name = getFileName(key, database);
        try {
        	FileOutputStream fos = new FileOutputStream( name );
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject( conf );
	        oos.close();
	        fos.close();
			serialize.add( System.currentTimeMillis() - start );
        	System.out.println( "conf written to " + name );
		} catch (Exception e) {
			try {
				e.printStackTrace( new PrintWriter( path + "error" ) );
			} catch (Exception x) {
			}
		}
    }
    
    private IConfiguration loadFromDisk( int key, ConfDB database )
    {
    	long now = System.currentTimeMillis();
    	String name = getFileName(key, database);
    	FileInputStream fis = null;
        try {
        	fis = new FileInputStream( name );
		} catch (FileNotFoundException e) {
			return null;
		}

		try {
			System.out.println( "loading conf from " + name );
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        Object o = ois.readObject();
	        ois.close();
	        long dt = System.currentTimeMillis() - now;
	        System.out.println( "" + dt + " msecs to load conf" );
	        return (IConfiguration)o;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
		return "" + key + "@" + database.hashCode();
	}
	
	private String getFileName( int key, ConfDB database )
	{
		return path + database.dbUrl().hashCode() + "." + key;
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

	public Statistics getDiskcacheHits() {
		return diskcacheHits;
	}

	public Statistics getDbRequests() {
		return dbRequests;
	}

	public Statistics getSerialize() {
		return serialize;
	}

}




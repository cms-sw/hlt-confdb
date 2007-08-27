package confdb.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import confdb.data.Configuration;

public class ConfCache implements Runnable 
{
	static private ConfCache instance = null;

	private HashMap<String, ConfWrapper> cache = new HashMap<String, ConfWrapper>();
	private int maxEntries = 10;
	
	static public ConfCache getInstance()
	{
		synchronized ( ConfCache.class ) 
		{
			if ( instance == null )
				instance = new ConfCache();
			return instance;
		}
	}
	
	
	private ConfCache()
	{
	}
	
	
	synchronized public Configuration get( String key ) 
	{
		ConfWrapper conf = cache.get( key );
		if ( conf == null )
			return null;
		else
			return conf.getConfiguration();
	}
	

	synchronized public void put( String key, Configuration conf )
	{
		if ( cache.size() > maxEntries )
		{
			List<ConfWrapper> list = 
				new ArrayList<ConfWrapper>( cache.values() );
			Collections.sort(list);
			cache.remove( list.get(0).key );
		}
		cache.put( key, new ConfWrapper( key, conf ) );
	}
	
	
	public void run()
	{
		
	}


	public int getMaxEntries() {
		return maxEntries;
	}


	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}
	
	
	private class ConfWrapper implements Comparable<ConfWrapper>
	{
		String key;
		Configuration configuration;
		long timestamp;
		
		ConfWrapper( String key, Configuration conf ) 
		{
			this.key = key;
			configuration = conf;
			timestamp = System.currentTimeMillis();
		}

		Configuration getConfiguration() 
		{
			timestamp = System.currentTimeMillis();
			return configuration;
		}

		public int compareTo(ConfWrapper o) 
		{
			return (int)(timestamp - o.timestamp);
		}
	}
	
}

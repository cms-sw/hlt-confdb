package confdb.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import confdb.data.IConfiguration;

public class ConfCache
{
	static private ConfCache instance = null;

	private HashMap<String, ConfWrapper> confCache = new HashMap<String, ConfWrapper>();
	private HashMap<String, ConfWrapper> stringCache = new HashMap<String, ConfWrapper>();
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
	
	
	synchronized public IConfiguration getConf( String key ) 
	{
		ConfWrapper conf = confCache.get( key );
		if ( conf == null )
			return null;
		else
			return conf.getConfiguration();
	}
	

	synchronized public String getConfString( String key ) 
	{
		ConfWrapper conf = confCache.get( key );
		if ( conf == null )
			return null;
		else
			return conf.getConfString();
	}
	

	synchronized public void put( String key, IConfiguration conf )
	{
		if ( confCache.size() > maxEntries )
		{
			List<ConfWrapper> list = 
				new ArrayList<ConfWrapper>( confCache.values() );
			Collections.sort(list);
			confCache.remove( list.get(0).key );
		}
		confCache.put( key, new ConfWrapper( key, conf ) );
	}
	
	
	synchronized public void put( String key, String conf )
	{
		if ( stringCache.size() > maxEntries )
		{
			List<ConfWrapper> list = 
				new ArrayList<ConfWrapper>( stringCache.values() );
			Collections.sort(list);
			stringCache.remove( list.get(0).key );
		}
		stringCache.put( key, new ConfWrapper( key, conf ) );
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
		IConfiguration configuration = null;
		String        confString = null;
		long timestamp;
		
		ConfWrapper( String key, IConfiguration conf ) 
		{
			this.key = key;
			configuration = conf;
			timestamp = System.currentTimeMillis();
		}

		ConfWrapper( String key, String conf ) 
		{
			this.key = key;
			confString = new String( conf );
			timestamp = System.currentTimeMillis();
		}

		IConfiguration getConfiguration() 
		{
			timestamp = System.currentTimeMillis();
			return configuration;
		}

		String getConfString() 
		{
			timestamp = System.currentTimeMillis();
			return confString;
		}

		public int compareTo(ConfWrapper o) 
		{
			return (int)(timestamp - o.timestamp);
		}
	}
	
}

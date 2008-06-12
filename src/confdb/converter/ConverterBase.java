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
	
    static private HashMap<String, ConfWrapper> confCache = new HashMap<String, ConfWrapper>();
    static private int maxCacheEntries = 10;
	
    public ConverterBase( String format, Connection connection ) throws ConverterException
    {
    	database = new ConfDB();
    	try {
    		database.connect( connection );
    		converterEngine = ConverterFactory.getConverterEngine( format );
    	} catch (Exception e) {
    		throw new ConverterException( "can't construct converter", e );
    	}
    }
	
    public ConverterBase( String format, String dbType, String dbUrl, String dbUser, String dbPwrd ) throws ConverterException
    {
    	this( format );
    	initDB( dbType, dbUrl, dbUser, dbPwrd );
    }
  
    protected ConverterBase( String format ) throws ConverterException
    {
		try {
			converterEngine = ConverterFactory.getConverterEngine( format );
		} catch (Exception e) {
    		throw new ConverterException( "can't construct converter", e );
		}
    }
    
    protected void initDB( String dbType, String dbUrl, String dbUser, String dbPwrd ) throws ConverterException
    {
    	database = new ConfDB();
    	try {
    		database.connect( dbType, dbUrl, dbUser, dbPwrd );
    	} catch (Exception e) {
    		throw new ConverterException( "can't init database connection", e );
    	}
    }
  
    
    static public int getNumberCacheEntries() 
    {
    	return confCache.size();
    }

    static public int getMaxCacheEntries() 
    {
    	return maxCacheEntries;
    }

    static public void setMaxCacheEntries(int maxCacheEntries) 
    {
    	ConverterBase.maxCacheEntries = maxCacheEntries;
    }

    static public void clearCache()
    {
        confCache = new HashMap<String, ConfWrapper>();
    }
	
    public ConfDB getDatabase() 
    {
    	return database;
    }
	
    public IConfiguration getConfiguration( int key ) throws ConverterException 
    {
    	ConfWrapper conf = confCache.get( getCacheKey( key ) );
    	if ( conf != null )
    		return conf.getConfiguration();
    	IConfiguration configuration = null;
    	try {
    		configuration = database.loadConfiguration(key);
    		put( key, configuration );
    		return configuration;
    	}
    	catch (DatabaseException e) {
    		String errMsg = "ConverterBase::getConfiguration(key="+key+") failed.";
    		throw new ConverterException(errMsg,e);
    	}
    }
		
    synchronized private void put( int key, IConfiguration conf )
    {
    	if ( confCache.size() > maxCacheEntries )
	    {
    		List<ConfWrapper> list = new ArrayList<ConfWrapper>( confCache.values() );
    		Collections.sort(list);
    		confCache.remove( list.get(0).getCacheKey() );
	    }
    	confCache.put( getCacheKey( key ), new ConfWrapper( getCacheKey( key ), conf ) );
    }
		
    public ConverterEngine getConverterEngine() 
    {
    	return converterEngine;
    }

	public void setConverterEngine(ConverterEngine converterEngine) 
	{
		this.converterEngine = converterEngine;
	}

	
	private String getCacheKey( int key )
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

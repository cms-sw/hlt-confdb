<%@page import="confdb.converter.Statistics"%>
<%@page import="org.json.JSONArray"%>
<%@page import="confdb.converter.DiskCache"%>
<%@page import="org.json.JSONString"%>
<%@page import="org.json.JSONObject"%>
<%@page import="confdb.converter.ConfCache"%>
<%@ page language="java" contentType="text/plain"
    pageEncoding="ISO-8859-1"%>

<%!
	public int getThreads()
	{
		return Thread.activeCount();
	}
	
	public int getFreeMemory()
	{
		return toMB( Runtime.getRuntime().freeMemory() ); 
	}
	
	public int getMaxMemory()
	{
		return toMB( Runtime.getRuntime().maxMemory() );
	}
	
	public int getTotalMemory()
	{
		return toMB( Runtime.getRuntime().totalMemory() );
	}

	public int getN()
	{
		return ConfCache.getCache().getAllRequests();
	}
	
	public int getInMemCache()
	{
		return ConfCache.getNumberCacheEntries();
	}
	
	private int toMB( long bytes )
	{
		int MB = 1024 * 1024;
		return (int) (( bytes + MB / 2 ) / MB);
	}
	
	
	private String stringify( Object o )
	{
		if ( o instanceof JSONString )
			return ((JSONString)o).toJSONString();
		return new JSONObject( o ).toString();
	}

%>

<% 	  	
	out.clearBuffer();

	JSONObject reply = new JSONObject();

	JSONObject data = new JSONObject();
	data.put( "freeMemory", getFreeMemory() ); 
	data.put( "maxMemory", getMaxMemory() );
	data.put( "threads", getThreads() );
	data.put( "totalMemory", getTotalMemory() );
	data.put( "configRequests", getN() ); 
	data.put( "db", new JSONObject( ConfCache.getCache().getDbRequests() ) );

	JSONObject cache = new JSONObject();
	cache.put( "inMemCache", getInMemCache() );
	DiskCache diskCache = ConfCache.getCache().getDiskCache();
	if ( diskCache != null )
	{
		cache.put( "availableSpace", diskCache.getAvailableSpace( false ) );
		Statistics stats = diskCache.getDeserialize();
		if ( stats.getN() > 0 )
			cache.put( "fileCache", new JSONObject( stats ) );
		stats = diskCache.getSerialize();
		if ( stats.getN() > 0 )
			cache.put( "serialize", new JSONObject( stats ) );
		JSONArray exceptions = new JSONArray( diskCache.getExceptions().toArray() );
		data.put( "exceptions", exceptions );
	}
	data.put( "cache", cache );	
	
	
	reply.put( "data", data );

	out.println( reply.toString( 2 ) );
%>


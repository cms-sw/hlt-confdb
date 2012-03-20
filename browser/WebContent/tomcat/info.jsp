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

	JSONObject cache = new JSONObject();
	cache.put( "requests", getN() ); 
	cache.put( "inMemCache", getInMemCache() );
	cache.put( "fileCache", new JSONObject( ConfCache.getCache().getDiskcacheHits() ) );
	cache.put( "db", new JSONObject( ConfCache.getCache().getDbRequests() ) );

	data.put( "cache", cache );	
	reply.put( "data", data );

	out.println( reply.toString() );
%>


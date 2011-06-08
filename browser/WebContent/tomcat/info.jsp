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

	private int toMB( long bytes )
	{
		int MB = 1024 * 1024;
		return (int) (( bytes + MB / 2 ) / MB);
	}
%>

<% 	  	     
	out.println( "{\"data\":{"
		+ "\"freeMemory\":" + getFreeMemory() 
		+ ",\"maxMemory\":" + getMaxMemory() 
		+ ",\"threads\":" + getThreads() 
		+ ",\"totalMemory\":" + getTotalMemory()
		+ "},\"exception\":null}" );
%>


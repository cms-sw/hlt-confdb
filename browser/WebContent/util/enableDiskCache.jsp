<%@page import="java.io.File"%>
<%@page import="confdb.converter.ConfCache"%>
<%@ page language="java" contentType="text/plain"
    pageEncoding="ISO-8859-1"%>


<% 	  	
	out.clearBuffer();

	String path = request.getParameter( "path" );
	if ( path == null )
	{
		out.println( "ERROR: no path specified" );
		return;
	}
	
	if ( path.equalsIgnoreCase( "." ) )
		path = getServletContext().getRealPath("/") + File.separator;
	
	ConfCache.setPath( path );

	out.println( "disk caching in " + path + " enabled" );
%>


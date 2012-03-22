<%@page import="java.text.Format"%>
<%@page import="com.sun.tools.javac.code.Type.ForAll"%>
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
	
	int size = 50;
	String s = request.getParameter( "size" );
	if ( s != null )
		size = Integer.parseInt( s );
	
	if ( path.equalsIgnoreCase("null") || size <= 0 )
	{
		ConfCache.setPath( null, 0 );
		out.println( "disk caching disabled" );
		return;
	}
	
	if ( !path.startsWith( "/" ) )
		path = getServletContext().getRealPath("/") + File.separator + path + File.separator;
	
	
	ConfCache.setPath( path, size );
	
	out.println( "disk caching in " + path + " enabled" );
	out.println( "used for caching: " + String.format( "%3d", size ) + " MB" );
	out.println( "available space : " 
			+ String.format( "%3d", ConfCache.getCache().getDiskCache().getAvailableSpace() ) + " MB");
%>


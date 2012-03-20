<%@page import="confdb.converter.ConfCache"%>
<%@ page language="java" contentType="text/plain"
    pageEncoding="ISO-8859-1"%>


<% 	  	
	out.clearBuffer();

	ConfCache.setPath( "/afs/cern.ch/project/jps/reps/test--confdb/" );

	out.println( "done" );
%>


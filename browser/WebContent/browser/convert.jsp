<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.converter.DbProperties"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="confdb.converter.Converter"%>
<%@ page contentType="text/plain" %>
<%
  try {
	Converter converter = Converter.getConverter();
	int configKey = Integer.parseInt( request.getParameter( "configKey" ) );
	String index = request.getParameter( "dbIndex" );
	if ( index != null )
	{
		int dbIndex = Integer.parseInt( index );
		ConfDBSetups dbs = new ConfDBSetups();
	    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
    	dbProperties.setDbUser( "cms_hlt_reader" );
    	converter.setDbProperties( dbProperties );
	}

	String confStr = converter.readConfiguration(configKey);
	if ( confStr == null )
	  out.print( "ERROR!\nconfig " + configKey + " not found!" );
	else
	  out.print( confStr );
  } catch ( Exception e ) {
	  out.print( "ERROR!\n\n" ); 
	  ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	  PrintWriter writer = new PrintWriter( buffer );
	  e.printStackTrace( writer );
	  writer.close();
	  out.println( buffer.toString() );
  }
%>



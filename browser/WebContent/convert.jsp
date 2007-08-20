<%@page import="confdb.converter.Converter"%>
<%@page import="confdb.converter.DbProperties"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@ page contentType="application/octet-stream" %>
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
  } catch ( confdb.db.DatabaseException e) {
    out.print( "ERROR!\ndatabase exception: " + e.getMessage() ); 
  } catch (ClassNotFoundException e) {
    out.print( "ERROR!\nClassNotFoundException: " + e.getMessage() );
  } catch (InstantiationException e) {
    out.print( "ERROR!\nInstantiationException: " + e.getMessage() );
  } catch (IllegalAccessException e) {
    out.print( "ERROR!\nIllegalAccessException: " + e.getMessage() );
  } catch ( java.sql.SQLException e) {
    out.print( "ERROR!\nsql exception: " + e.getMessage() ); 
  }
%>



<%@ page contentType="text/plain" %>
<%
  try {
	int configKey = Integer.parseInt( request.getParameter( "configKey" ) );
	String confStr = confdb.converter.Converter.getConverter().readConfiguration(configKey);
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



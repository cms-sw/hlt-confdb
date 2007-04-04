<%@ page contentType="text/plain" %>
<%
  String configKey = request.getParameter( "configKey" );
  if ( configKey == null )
  {
    out.print( "ERROR!\nno configKey specified!\n" );
    return;
  }

  String CMSSWrelease = 
    confdb.converter.Converter.getPrefs().get( "CMSSWrelease", "CMSSW_1_2_0_pre5" );
  String dbName = confdb.converter.Converter.getPrefs().get( "dbName", "hltdb" );
  String dbType = confdb.converter.Converter.getPrefs().get( "dbType", "mysql" );
  String dbHost = confdb.converter.Converter.getPrefs().get( "dbHost", "localhost" );
  String dbUser = confdb.converter.Converter.getPrefs().get( "dbUser", "hlts" );
  String dbPwrd = confdb.converter.Converter.getPrefs().get( "dbPwrd", "cms" );

  String dbUrl = "jdbc:mysql://" + dbHost + ":3306/" + dbName;
  if ( dbType.equals("oracle") )
    dbUrl = "jdbc:oracle:thin:@//" + dbHost + "/" + dbName;

  confdb.db.CfgDatabase database = new confdb.db.CfgDatabase();
  try {
    database.connect( dbType, dbUrl, dbUser, dbPwrd );
    database.prepareStatements();

    confdb.converter.Converter converter = 
       confdb.converter.ConverterFactory.getFactory( CMSSWrelease ).getConverter();
    converter.setDatabase( database );
    if ( !converter.readConfiguration( configKey ) )
      {
        out.print( "ERROR!\nconfig " + configKey + " doesn't exist!");
	return;
      }
    String configStr = converter.convertConfiguration();
    out.print( configStr );
    database.disconnect();
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



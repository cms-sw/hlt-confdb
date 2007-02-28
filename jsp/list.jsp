<html>
<head>
<title>HLT configurations</title>
</head>
<body bgcolor="lightgrey"> 
<center>
<table cellpadding="10" cellspacing="0" border="0">
<thead>
<tr align="center">
</tr>
</thead>
<tbody>
<%!
    String writeAll( int indent, confdb.data.Directory directory, confdb.converter.Converter converter )
    {
      String str = "<tr>" + indent( indent );
      str += "<td><img src=\"open_folder.gif\"></td><td>" + directory + "</td></tr>";
      confdb.data.ConfigInfo[] configs = converter.listConfigs( directory );
      for ( int i = 0; i < configs.length; i++ )
      {
	    str += "<tr>" + indent( indent + 1 ) + "<td>";
	    confdb.data.ConfigVersion versionInfo = configs[i].version( configs[i].versionCount() - 1 ); 
 	    str += "<a href=\"convert.jsp?configKey=" + versionInfo.dbId() + "\">" 
	      + configs[i].name() + " V" + versionInfo.version() + "</a>";
	    for ( int ii = configs[i].versionCount() - 2; ii >= 0; ii-- )
        {
	      versionInfo = configs[i].version( ii ); 
 	      str += " <a href=\"convert.jsp?configKey=" + versionInfo.dbId() + "\"> V" 
              + versionInfo.version() + "</a>";
        }
	    str += "</td></tr>";
      }
      confdb.data.Directory[] list = converter.listSubDirectories( directory );
      for ( int i = 0; i < list.length; i++ )
	    str += writeAll( indent + 1, list[i], converter );
      return str;
    }

    String indent( int n )
    {
      String str = "";
      for ( int i = 0; i < n; i++ )
        str += "<td></td>";
      return str;
    }
%>
<%
  try {
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
    database.connect( dbType, dbUrl, dbUser, dbPwrd );
    database.prepareStatements();

    confdb.converter.Converter converter = 
       confdb.converter.ConverterFactory.getFactory( CMSSWrelease ).getConverter();
    converter.setDatabase( database );
    confdb.data.Directory root = converter.getRootDirectory();
    out.println( writeAll( 0, root, converter ) );

    database.disconnect();
  } catch (Exception e) {
    out.println( "<tr><td>" + e.toString() + "</td></tr>" );
  }
%>
</tbody>
</table>
</body>
</html>


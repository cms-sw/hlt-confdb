<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.converter.DbProperties"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="confdb.converter.Converter"%>
<%@page import="cache.ConfCache"%>
<%@page import="confdb.data.Configuration"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config</title>

<script type="text/javascript" src="../js/yui/yahoo/yahoo.js"></script>
<script type="text/javascript" src="../js/yui/utilities/utilities.js"></script>
<script type="text/javascript" src="../js/yui/event/event.js"></script>

<style type="text/css">

body {
	margin:0;
	padding:0;
<%
  String background = request.getParameter( "bgcolor" );
  if ( background != null )
	  out.println( "background:#" + background + ";" );
%>
}

</style>


<script type="text/javascript">

function signalReady()
{
  if ( parent &&  parent.iframeReady )
    parent.iframeReady();
}

//YAHOO.util.Event.onDOMReady( signalReady );

 </script>

</head>

<body onload="signalReady()">
<pre>
<%
  try {
	Converter converter = Converter.getConverter( "HTML" );
	int configKey = Integer.parseInt( request.getParameter( "configKey" ) );
	int dbIndex = -1;
	String index = request.getParameter( "dbIndex" );
	if ( index != null )
	  dbIndex = Integer.parseInt( index );
	String cacheKey = "db:" + dbIndex + " key:" + configKey;
	ConfCache cache = ConfCache.getInstance();
	Configuration conf = cache.get( cacheKey  );
	if ( conf == null )
	{
		ConfDBSetups dbs = new ConfDBSetups();
	    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
    	dbProperties.setDbUser( "cms_hlt_reader" );
    	converter.setDbProperties( dbProperties );
    	converter.connectToDatabase();
    	conf = converter.loadConfiguration( configKey );
    	converter.disconnectFromDatabase();
    	cache.put( cacheKey, conf );
	}

	String confStr = converter.convert( conf );
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
</pre>
</body>
</html>


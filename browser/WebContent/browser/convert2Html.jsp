<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.data.IConfiguration"%>
<%@page import="confdb.converter.ConverterBase"%>
<%@page import="confdb.converter.OnlineConverter"%>
<%@page import="confdb.converter.ConverterException"%>
<%@page import="confdb.converter.BrowserConverter"%>
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
	int dbIndex = -1;
	String index = request.getParameter( "dbIndex" );
	if ( index != null )
		dbIndex = Integer.parseInt( index );

	ConverterBase converter = BrowserConverter.getConverter( dbIndex );

	String configName = request.getParameter( "configName" );
	String configId = request.getParameter( "configKey" );
	if ( configId == null  &&  configName == null )
	{
		out.print( "ERROR!\nconfigKey or configName must be specified!");
		return;
	}

	int configKey = ( configId != null ) ?
    	Integer.parseInt(configId) : converter.getDatabase().getConfigId(configName);

	IConfiguration conf = converter.getConfiguration( configKey );

	if ( conf == null )
		out.print( "ERROR!\nconfig " + configKey + " not found!" );
	else
	{
		String confString = null;
		try {
			if ( converter instanceof OnlineConverter )
				confString = ((OnlineConverter)converter).getEpConfigString( configKey );
			else
				confString = converter.getConverterEngine().convert( conf );
		} catch ( ConverterException e1 ) {
			converter.removeFromCache( configKey );
			System.out.println( "reloading config " + configKey );
			if ( converter instanceof OnlineConverter )
				confString = ((OnlineConverter)converter).getEpConfigString( configKey );
			else
				confString = converter.getConverterEngine().convert( converter.getConfiguration( configKey ) );
		}
		out.println( confString );
	}
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


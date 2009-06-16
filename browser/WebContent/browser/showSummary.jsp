<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="confdb.converter.ConverterBase"%>
<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.converter.OnlineConverter"%>
<%@page import="confdb.data.ModifierInstructions"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.PrintWriter"%>

<%@page import="confdb.data.IConfiguration"%>
<%@page import="confdb.converter.OfflineConverter"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>config summary</title>

<%
  String db = request.getParameter( "dbName" );
  String yui = "../js/yui";
  String css = "../css";
  String js = "../js";
  String img = "../img";
  boolean online = false;
  if ( request.getParameter( "online" ) != null )
  {
	online = true;
  	yui = "../../../gui/yui";
    css = "../../css";
    js = "../../js";
    img = "../../img";
  }
%>



<!-- css -->
<link type="text/css" rel="stylesheet" href="<%=css%>/columnlist.css" />
<link rel="stylesheet" type="text/css" href="<%=css%>/confdb.css" />

<!-- js -->
<script type="text/javascript" src="<%=yui%>/yahoo/yahoo-min.js"></script> 
<script type="text/javascript" src="<%=yui%>/dom/dom-min.js"></script>
<script type="text/javascript" src="<%=js%>/sortabletable.js"></script>
<script type="text/javascript" src="<%=js%>/columnlist.js"></script>

<style>

body {
	margin:0px; 
	padding:0px; 
	padding-top:5px;
	overflow:hidden;
}

#container {
  margin:0px; 
  padding:0px;
  width:100%;
}

</style>

</head>
<body class="skin1 tab1">
<%
	BrowserConverter.UrlParameter paras = 
		BrowserConverter.getUrlParameter( request.getParameterMap() );

	paras.format = "summary.html";
	String result = "";
	ConverterBase converter = null;
	try {

		ModifierInstructions modifierInstructions = new ModifierInstructions();
	    //modifierInstructions.interpretArgs( paras.toModifier );
	    if ( paras.dbName.equals( "online" ) )
	    	converter = OnlineConverter.getConverter();
	    else
	        converter = BrowserConverter.getConverter( paras.dbName );
        if ( paras.configId == -1 )
			paras.configId = converter.getDatabase().getConfigId( paras.configName );

        IConfiguration conf = converter.getConfiguration( paras.configId );
        OfflineConverter helper = new OfflineConverter( "HTML" );
	    result = helper.getConfigString( conf, paras.format,
						      modifierInstructions,
						      paras.asFragment);
	} catch (Exception e) {
    	Throwable cause = e.getCause(); 
	    out.print(e.getMessage()+"\n"); 
	    if ( cause != null )
		    out.print( "cause: " + cause.getMessage() + "\n\n");
	    else
	    	out.print( "\n" );
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    PrintWriter writer = new PrintWriter(buffer);
	    e.printStackTrace(writer);
	    writer.close();
	    out.println(buffer.toString());
	    if ( converter != null )
	        BrowserConverter.deleteConverter( converter );
	    return;
	}
	int columns = 5;
	int start = result.indexOf( "<!--" );
	if ( start != -1 )
	{
		start += 4;
		int end = result.indexOf( "columns -->", start );
		if ( end != -1 )
		{
			String columnStr = result.substring( start, end );
			if ( columnStr.length() > 0 )
				columns = Integer.parseInt( columnStr.trim() );
		}
	}
%>

<div id="container" class="webfx-columnlist">
  <div id="head" class="webfx-columnlist-head">
    <table cellspacing="0" cellpadding="0" width=100%>
	  <tr>
	    <td><b>trigger name</b><img src="<%=img%>/asc.png"/></td>
	    <td><b>L1 condition</b><img src="<%=img%>/asc.png"/></td>
<%
  for ( int i = 1; i <= columns - 2; i++ )
	  out.println( "<td><b>filter " + i + "</b><img src='" + img + "/asc.png'/></td>" );
%>
	  </tr>
	</table>
  </div>
  <div id="body" class="webfx-columnlist-body">
    <table cellspacing="0" cellpadding="0" style="width:100%;">
<%
    out.println( "<colgroup span='" + columns + "'>" );
    int perColumn = 100 / columns;
    for ( int i = 0; i < columns; i++ )
	  out.println( "<col style='width:" + perColumn + "%;' />" );
    out.println( "</colgroup>" );

	start = result.indexOf( "<table" );
	if ( start < 0 )
		out.println( result );
	start = result.indexOf( '>', start );
	if ( start < 0 )
		out.println( result );
	int stop = result.indexOf( "</table>" );
	if ( stop < 0 )
		out.println( result.substring( start + 1 ) );
	else
		out.println( result.substring( start + 1, stop ) );
%>
    </table>
  </div>
</div>

<script type="text/javascript">

var	myTable,
	paddingY =  5,
	Dom = YAHOO.util.Dom;
	
	
  signalReady();
  window.onresize = resize;
  Dom.setStyle( 'container', 'height', (Dom.getViewportHeight() - paddingY) + 'px' );
  myTable = new WebFXColumnList();
  var rc = myTable.bind( document.getElementById('container'), 
                         document.getElementById('head'), 
                         document.getElementById('body') );
  if ( rc != 0 )
    alert( myTable.error + "\n" + myTable._eHeadCols.length + '!=' + myTable._eBodyCols.length );                       
                         
<%
  out.println( "var columns  = " + columns + ";" );
  out.println( "myTable.sortAscImage   = '" + img + "/asc.png';" );
  out.println( "myTable.sortDescImage  = '" + img + "/desc.png'" );;
%>

                         
  if ( myTable._rows > 0 )
  {
    var sortTypes = new Array();
    for ( var i = 0; i < columns; i++ )
      sortTypes.push( 'TYPE_STRING' );
    myTable.setSortTypes( sortTypes );
  }


  //infoWindow = window.open( "", "infoWindow", "width=300,height=200,left=100,top=200" );
  //infoWindow.document.writeln( "<b>debug</b><br><div id='debugDiv'></div>" );
  //infoWindow.document.close();
  //o.resize(640, 480);
  //o.sort(0);


function signalReady()
{
  if ( parent &&  parent.iframeReady )
    parent.iframeReady();
}

function resize()
{
  myTable.resize( Dom.getViewportWidth(), Dom.getViewportHeight() - paddingY );
}


</script>

</body>
</html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.data.ModifierInstructions"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.PrintWriter"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>config summary</title>

<!-- css -->
<link type="text/css" rel="stylesheet" href="../assets/css/columnlist.css" />

<!-- js -->
<script type="text/javascript" src="../js/yui/yahoo/yahoo-min.js"></script> 
<script type="text/javascript" src="../js/yui/dom/dom-min.js"></script>
<script type="text/javascript" src="../js/sortabletable.js"></script>
<script type="text/javascript" src="../js/columnlist.js"></script>

<style>

body {
	margin:0px; 
	padding:0px; 
	overflow:hidden;
}

#container {
  margin:0px; 
  padding:0px; 
  width:100%;
}

</style>

</head>
<body>

<%
	BrowserConverter.UrlParameter paras = 
		BrowserConverter.getUrlParameter( request.getParameterMap() );

	paras.format = "summary.html";
	String result = "";
	BrowserConverter converter = null;
	try {
	    ModifierInstructions modifierInstructions = new ModifierInstructions();
	    //modifierInstructions.interpretArgs( paras.toModifier );
        converter = BrowserConverter.getConverter( paras.dbName );
        if ( paras.configId == -1 )
			paras.configId = converter.getDatabase().getConfigId( paras.configName );

	    result = converter.getConfigString( paras.configId, paras.format,
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
	    {
	        BrowserConverter.deleteConverter( converter );
	        BrowserConverter.clearCache();
	    }
	}
%>


<div id="container" class="webfx-columnlist">
  <div id="head" class="webfx-columnlist-head">
    <table cellspacing="0" cellpadding="0" style="width:100%;">
	  <tr>
	    <td>trigger name<img src="images/asc.png"/></td>
	    <td>L1 condition<img src="images/asc.png"/></td>
	    <td>filter 1<img src="images/asc.png"/></td>
	    <td>filter 2<img src="images/asc.png"/></td>
	    <td>filter 3<img src="images/asc.png"/></td>
	  </tr>
	</table>
  </div>
  <div id="body" class="webfx-columnlist-body">
    <table cellspacing="0" cellpadding="0" style="width:100%;">
					<colgroup span="5">
						<col style="width: 20%;" />
						<col style="width: 20%;" />
						<col style="width: 20%;" />
						<col style="width: 20%;" />
						<col style="width: 20%;" />
					</colgroup>
<%
	int start = result.indexOf( "<table" );
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

var	Dom = YAHOO.util.Dom,
	myTabel; 
	
  signalReady();
  window.onresize = resize;
  Dom.setStyle( 'container', 'height', Dom.getViewportHeight() + 'px' );
  myTable = new WebFXColumnList();
  var rc = myTable.bind( document.getElementById('container'), 
                         document.getElementById('head'), 
                         document.getElementById('body') );
  myTable.setSortTypes([TYPE_STRING, TYPE_STRING, TYPE_STRING, TYPE_STRING, TYPE_STRING]);
  myTable._setAlignment();
  //o.resize(640, 480);
  //o.sort(0);


function signalReady()
{
  if ( parent &&  parent.iframeReady )
    parent.iframeReady();
}

function resize()
{
  myTable.resize( Dom.getViewportWidth(), Dom.getViewportHeight() );
}


</script>

</body>
</html>
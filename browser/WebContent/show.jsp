<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.data.ConfigInfo"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT conf db</title>
<link rel="stylesheet" type="text/css" href="css/smoothness/jquery-ui-1.8.6.custom.css" rel="stylesheet" />	
<link rel="stylesheet" type="text/css" href="css/confdb-jq.css" rel="stylesheet" />	

<!-- js -->
<script type="text/javascript" src="js/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.6.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.scrollTo-1.4.2-min.js"></script>
<script type="text/javascript" src="js/jquery.cookie.js"></script>
<script type="text/javascript" src="js/json2.js"></script>

<style type="text/css">

body {
	overflow: hidden;
	position: fixed;
}

.ui-tabs .ui-tabs-nav li a:focus {
	outline:0;
}

.ui-tabs .ui-tabs-nav li {
	float:right;
	position:relative;
}

.ui-tabs .ui-tabs-nav li a {
	padding:3px;
}

.ui-tabs {
  border:0px;
  background:#f8f8f8;
  position: relative;
  top: -10px;
}

.ui-tabs .ui-tabs-panel {
  border: 1px solid grey;
  background:#f8f8f8;
  padding: 2px 5px 2px 2px;
}

.ui-tabs .ui-tabs-nav {
  border:0px;
  padding: 0px;
  background:#f8f8f8;
}

.ui-tabs .ui-tabs-selected {
//background: #b2b2b2;
}

.ui-tabs .ui-tabs-hide {
    position: absolute !important;
    left: -10000px !important;
    display: block !important;
}



#dialog-form select {
	border: 0px;
}

#main {
	width: 800px;
}

#header {
	width: 600px;
	z-index: 1000;
	background: #F2D685;
	position: relative;
	top: 10px;
	left: 10px;
	padding: 1px 10px 1px 10px;
	border: 1px solid #606060;
	-moz-border-radius: 5px;
	-webkit-border-radius: 5px;
	border-radius: 5px;
}

#fullName {
	font-size: 1.4em;
	float: left;
	min-width: 400px;
}

#fullName:hover {
	cursor: pointer;
}


#secondaryInfo {
	color: grey;
}


#debugDiv {
	position: absolute;
	top:    5px;
	right: 10px;
}

#download {
	position: absolute;
	top:    5px;
	right: 10px;
}

#download a:link {
	color: grey;
}

#streamsDiv {
  padding-top: 2px;
}

#detailsHeader {
	margin-bottom: 10px;
}

.detailsHeaderTD {
	padding: 5px 10px 5px 10px;
	font-size: 0.9em;
}

</style>

<%
	BrowserConverter.UrlParameter paras = null;
	BrowserConverter converter = null;
	
	String comment = "";
	String created = "";
	
	if ( request.getParameterMap().isEmpty() )
	{
	    out.println( "<script type=\"text/javascript\">" );
	    out.println( "var config = { name: \"\" };" );
	    out.println( "</script>" );
	}
	else
	{
		try {
			paras = BrowserConverter.getUrlParameter( request.getParameterMap() );

	    	if ( paras.runNumber != -1 )
	    		paras.dbName = "ORCOFF";
	    
	   		converter = BrowserConverter.getConverter( paras.dbName );
	    	if ( paras.runNumber != -1 )
	    	{
	    		paras.configName = null;
	    		paras.configId = converter.getKeyFromRunSummary( paras.runNumber );
	    		if ( paras.configId <= 0 )
	    			throw new Exception( "CONFIG_NOT_FOUND" );
	    	}

	    	if ( paras.configName != null )
	    		paras.configId = converter.getDatabase().getConfigId( paras.configName );
		    ConfigInfo info = converter.getDatabase().getConfigInfo( paras.configId );
		    if ( paras.configName == null )
				paras.configName = info.parentDir().name() + "/" + info.name() + "/V" + info.version();
			if ( info.version() != 1 )
				comment = info.comment();
			String date = info.created();
			created = date.substring( 0, date.length() - 2 );
		    
	    	out.println( "<script type=\"text/javascript\">" );
	    	out.println( "var config = { id: " + paras.configId + "," ); 
	    	out.println( "               name: \"" + paras.configName + "\"," );
	    	out.println( "               dbName: \"" + paras.dbName + "\"," );
	    	out.println( "               runNumber: \"" + paras.runNumber + "\" };" );
	    	out.println( "</script>" );
	    
		} catch (Exception e) {
			out.println( "</head><body>ERROR" );
			out.println( e.toString() );
			out.println( "</body></html>" );
			return;
		}
	}
	
%>



<script type="text/javascript">

var tabsHeight = 200;
var tabs,
	cookie = null;

var tabList = [ 
  { src: 'browser/showSummary.jsp?configKey=' + config.id + '&dbName=' + config.dbName,
    name: 'summary' },
  { src: 'browser/convert2Html.jsp?scrollDiv=false&configKey=' + config.id + '&dbName=' + config.dbName,
    name: 'details' }
]; 

function iframeReady()
{
  if ( tabList.length > 0 )
  {
	var next = tabList.pop();
	var html = '<iframe src="' + next.src + '" name="' + next.name + 'Frame" id="' + next.name + 'Frame" frameborder="0" width="100%" height="' + (tabsHeight - 50) + '"></iframe>';
	$( '#' + next.name + 'Div' ).html( html );
  }
  else
  {
	  $('#download').html( '<a href="' + config.name.replace( /\//g, '-' ) + '.py?format=python&configId=' + config.id + '&dbName=' + config.dbName + '">download .py</a>' );
	  if ( cookie ) 
	  {
		  if ( cookie.activeTab )
			tabs.tabs( "select", cookie.activeTab );
	  	$('#tabs').bind( 'tabsshow', saveCookie );
	  }
  }
}

var tabNames = { details : 2, summary : 1, streams : 0 };

function scrollTo( anchor, page )
{
	tabs.tabs( "select", tabNames[ page ] );
//	frames[ 'detailsFrame' ].scrollTo( anchor );
	$( "body", frames.detailsFrame.document ).scrollTo(  "a[name='" + anchor + "']", { axis: 'y' } );
	//var href = frames[ "detailsFrame" ].location.href;
//	window.setTimeout( "scrollNow()", 100 );
	//alert( href );
	//frames[ "detailsFrame" ].location.href = frames[ "detailsFrame" ].location.href + "#HLTBeginSequence";
}

function showNew()
{
	$( '#dialog-form' ).dialog( "close" );
	var configXX = $( '#configXX' ).val();
	window.location.href = "show.jsp?dbName=" + $('#dbName').val() + "&" + configXX + '=' + $('#configName').val(); 
	return false;
}

function saveCookie()
{
	var active = tabs.tabs( 'option', 'selected' ); 
	if ( cookie )
	{
		cookie.activeTab = active;
		$.cookie( getCookieName(), JSON.stringify( cookie ) );
	}
}

function getCookieName()
{
	return encodeURIComponent( "show.jsp4" + config.dbName );
}


var hideDebugDivTimeout = null;

function resizeAll( ev )
{
	var width = $(window).width();
	var height = $(window).height();
	$('#main').width( width );
	$('#main').height( height );
//	$('#header').width( width );
	
	adjust( width );
	$('#streamsFrame').height( tabsHeight );
	$('#detailsFrame').height( tabsHeight - 50 );
	
  	$('#download').hide();
  	$('#debugDiv').show();
  	$('#debugDiv').html( 'resize: ' + width + ' x ' + height );
  	if ( hideDebugDivTimeout != null )
  		window.clearTimeout( hideDebugDivTimeout );
  	hideDebugDivTimeout = window.setTimeout( "hideDebugDiv()", 2000 );
}

function hideDebugDiv()
{
  	$('#debugDiv').hide();
  	$('#download').show();
  	hideDebugDivTimeout = null;
}

function adjust( width )
{
	var gap = $( '#detailsTab' ).offset().left - 650; 
	if ( gap < 0 )
	{
		  $( '#tabs' ).css( 'top', '20px' );
	  $('#header').width( width - 150 );
	}
	else
	{
	  $( '#tabs' ).css( 'top', '0px' );
	  if ( $('#secondaryInfo').offset().left < 430 )
		  $('#header').width( $('#header').width() + gap );
	}
	var y1 = $('#streams').offset().top;
	var y2 = $(window).height();
	tabsHeight = Math.floor( y2 - y1 - 10 );
}



$(function()
{
  var width = $(window).width();
  $( "#main" ).width( width );

  $( "#dialog-form" ).dialog( {
	  		position: [ 10, 10 ],
			autoOpen: false,
			height: 200,
			width: 700,
			modal: true,
			buttons: {
				"show": showNew,
				Cancel: function() {
					$( this ).dialog( "close" );
				}
			},
			close: function() {
			}
		});

  if ( config.name.length == 0 )
  {
  	$('#dialog-form').dialog( 'open' );
  	return;
  }
  
  $( "#fullName" ).click(function() {
	    $('#configName').val( config.name );
		$('#dialog-form').dialog( "open" );
		$('#configName').focus();
	});

  // Tabs
  tabs = $('#tabs').tabs( { cache: true } );

  adjust( width );
  
  $("#streamsDiv").html( '<iframe src="browser/showStreams.jsp?configKey=' + config.id + '&dbName=' + config.dbName +'" name="streamsFrame" id="streamsFrame" width="100%" height="' + tabsHeight + '" frameborder="0"></iframe>' );    

  if ( config.runNumber != -1 && parent && parent.expandTree )
	  parent.expandTree( config.name, true );

  if ( document.cookie )
  {
	  var cookieStr = $.cookie( getCookieName() );
	  if ( !cookieStr )
		  cookie = {};
	  else
		  cookie = JSON.parse( cookieStr );
  }
  
  $( window ).bind( 'resize', resizeAll );
});
	
</script>

</head>
<body>
<div id="main">
<div id="header">
 <table width='100%'><tr>
  <td align='left'><div id="fullName"><b><%=(paras != null ? paras.configName : "")%></b></div></td>
  <td align='right'>
    <table id='secondaryInfo'>
      <tr><td colspan='2'><%=comment%></td></tr>
      <tr><td>created:</td><td><%=created%></td></tr>
    </table>
  </td>
 </tr></table>
</div>
<div id="tabs">
  <ul>
    <li><a href="#streams">streams</a></li>
    <li><a href="#summary">summary</a></li>
    <li id='detailsTab'><a href="#details">details</a></li>
  </ul>
  <div id="details">
  	<div class='ui-widget-header' id="detailsHeader">
  	  <table><tr>
        <td class='detailsHeaderTD'><a href="javascript:scrollTo( 'paths', 'details')">Paths</a></td>
        <td class='detailsHeaderTD'><a href="javascript:scrollTo( 'sequences', 'details')">Sequences</a></td>
        <td class='detailsHeaderTD'><a href="javascript:scrollTo( 'modules', 'details')">Modules</a></td>
        <td class='detailsHeaderTD'><a href="javascript:scrollTo( 'ed_sources', 'details')">ed_sources</a></td> 
        <td class='detailsHeaderTD'><a href="javascript:scrollTo( 'es_sources', 'details')">es_sources</a></td>
        <td class='detailsHeaderTD'><a href="javascript:scrollTo( 'es_modules', 'details')">es_modules</a></td>
        <td class='detailsHeaderTD'><a href="javascript:scrollTo( 'services', 'details')">Services</a></td>
  	  </tr></table>
    </div>
    <div id="detailsDiv"></div>
  </div>
  <div id="summary">
    <div id="summaryDiv"></div>
  </div>
  <div id="streams">
    <div id="streamsDiv"></div>
  </div>
</div>
<div id="download"><img src="img/wait.gif"></div>
<div id="debugDiv"></div>
</div>

<div id="dialog-form" title="select config">
  <form onsubmit='return showNew()'>
    <table width='100%'>
     <tr>
      <td><select id='configXX'><option selected>configName</option><option>configId</option><option>runNumber</option></select></td>
      <td align='left'>:</td>
	  <td><input type="text" id="configName" size="50"></td>
	 </tr>
	 <tr>
	  <td><label for="db">database</label></td>
      <td align='left'>:</td>
	  <td><select name='dbName' id='dbName'>
<%
	   	String[] list = BrowserConverter.listDBs();
		String thisDB = paras != null ? paras.dbName : "";
		for ( String db : list )
			out.println( "<option" + (db.equalsIgnoreCase( thisDB ) ? " selected" : "" ) + ">" + db + "</option>" );
%>
	   </select></td>
      </tr>
    </table>
  </form>
</div>

</body>
</html>

 


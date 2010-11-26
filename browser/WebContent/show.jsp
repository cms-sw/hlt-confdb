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
<script type="text/javascript" src="js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.6.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.scrollTo-1.4.2-min.js"></script>

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
	font-size: 14px;
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


#main {
	width: 800px;
}

#header {
	z-index: 1000;
	background: #F2D685;
	position: relative;
	top: 10px;
	left: 10px;
	width: 600px;
	padding: 10px;
	border: 1px solid #606060;
	-moz-border-radius: 5px;
	-webkit-border-radius: 5px;
	border-radius: 5px;
}

#streamsDiv {
  padding-top: 2px;
}

#detailsHeader {
	margin-bottom: 10px;
}

.detailsHeaderTD {
	padding: 5px 10px 5px 10px;
	font-size: 12px;
}

</style>

<%
	BrowserConverter.UrlParameter paras = null;
	BrowserConverter converter = null;
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
	    else
	    {
	    	ConfigInfo info = converter.getDatabase().getConfigInfo( paras.configId );
			paras.configName = info.parentDir().name() + "/" + info.name() + "/V" + info.version();
	    }
	  
	    out.println( "<script type=\"text/javascript\">" );
	    out.println( "var config = { id: " + paras.configId + ",dbName: \"" + paras.dbName + "\" };" );
	    out.println( "</script>" );
	    
	} catch (Exception e) {
		out.println( "</head><body>ERROR" );
		out.println( e.toString() );
		out.println( "</body></html>" );
		return;
	}
	
%>



<script type="text/javascript">

var tabsHeight = 200;
var tabs;

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
	var html = '<iframe src="' + next.src + '" name="' + next.name + 'Frame" id="' + next.name + 'Frame" frameborder="0" width="100%" height="' + tabsHeight + '"></iframe>';
	$( '#' + next.name + 'Div' ).html( html );
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

$(function()
{
  var width = $(window).width();
  $( "#main" ).width( width );

  // Tabs
  tabs = $('#tabs').tabs( { cache: true } );

  var y1 = $('#streams').offset().top;
  var y2 = $(window).height();
  tabsHeight = Math.floor( y2 - y1 - 10 );
  //tabsWidth = $(window).width() - 36;

  $("#streamsDiv").html( '<iframe src="browser/showStreams.jsp?configKey=' + config.id + '&dbName=' + config.dbName +'" name="streamsFrame" id="streamsFrame" width="100%" height="' + tabsHeight + '" frameborder="0"></iframe>' );    
  
});

</script>

</head>
<body>
<div id="main">
<div id="header">
<div id="fullName"><b><%=paras.configName%></b></div>
</div>
<div id="tabs">
  <ul>
    <li><a href="#streams">streams</a></li>
    <li><a href="#summary">summary</a></li>
    <li><a href="#details">details</a></li>
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
</div>
</body>
</html>

 


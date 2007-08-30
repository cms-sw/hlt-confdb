<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config browser</title>

<link rel="stylesheet" type="text/css" href="../js/yui/reset-fonts-grids/reset-fonts-grids.css" />

<link rel="stylesheet" type="text/css" href="../js/yui/container/assets/skins/sam/container.css" />
<script type="text/javascript" src="../js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../js/yui/container/container.js"></script>
<script type='text/javascript' src='../dwr/interface/AjaxInfo.js'></script>
<script type='text/javascript' src='../dwr/engine.js'></script>
<script type='text/javascript' src='../dwr/util.js'></script>

<style type="text/css">

/*margin and padding on body element
  can introduce errors in determining
  element position and are not recommended;
  we turn them off as a foundation for YUI
  CSS treatments. */
body {
	margin:0;
	padding:0;
}

.yui-module { padding:0px; margin-left:5px; margin-right:5px; margin-bottom:0px; display:none; }
.yui-module .hd { margin-bottom:10px; margin-top:5px; padding-left:5px; background-color:#FFE19A }
.yui-gd { margin-bottom:0px; }
#doc3 { margin-bottom:0px; margin-top:5px; }
/*
.yui-module .ft { border:1px solid blue;padding:5px; }
*/



body { background:#edf5ff }
#mainLeft { background:#edf5ff; border: 0px solid #B6CDE1; margin:0px; padding:0px }
#mainRight { margin:0px; padding:0px; background-color:#FFF5DF; border: 1px solid #B6CDE1; }
.headerDiv { margin:0px; padding:0.4em; background:white }

</style>

<script type="text/javascript">

var loadingModule;
var configModule;
var configFrameUrl;
var configKey;
var dbIndex;
	
function init() 
{
	//handler for expanding all nodes
	YAHOO.util.Event.on("expand", "click", function(e) {
			treeFrame.tree.expandAll();
			YAHOO.util.Event.preventDefault(e);
		});
		
	//handler for collapsing all nodes
	YAHOO.util.Event.on("collapse", "click", function(e) {
			treeFrame.tree.collapseAll();
			YAHOO.util.Event.preventDefault(e);
		});
		
    loadingModule = new YAHOO.widget.Module("loading", { visible: false });
    loadingModule.render();

    configModule = new YAHOO.widget.Module("config", { visible: false });
	configModule.setBody( '<iframe name="configIFrame" id="configFrame" width="100%" height="600" frameborder="0"></iframe>');
    configModule.render();

    var height = 500;
    if ( parent.tabHeight )
    	height = parent.tabHeight - 50;
    document.getElementById( "treeFrame" ).height = height;
}
	
	
function labelClicked( node )
{
  document.getElementById("configFrame").height = 
  	YAHOO.util.Dom.getViewportHeight() - 75;

  if ( !node.data.key )
  	return;
  configKey = node.data.key;
  dbIndex = node.data.dbIndex;
  configFrameUrl = "convert2Html.jsp?configKey=" + node.data.key + "&dbIndex=" + node.data.dbIndex + "&bgcolor=FFF5DF"; 
  document.getElementById("configFrame").src = configFrameUrl;
  
  loadingModule.show();
  configModule.hide();
  var header = "<b>" + node.data.name + " " + node.data.label + "</b>";
  loadingModule.setHeader( header );
  loadingModule.render();

  var fileName = node.data.name.replace( '//s/g', '_' ) + "_V" + node.data.version;
  header += '<a style="position:absolute; right:20px;" href="' + fileName + '.cfg?configKey='+ node.data.key + '&dbIndex=' + node.data.dbIndex + '">download cfg</a>';
    
  var jumpTo = '<div id="jumpTo">'
 	+  '<a href="' + configFrameUrl + '#paths" target="configIFrame">paths</a>  ' 
    + '</div>';
  configModule.setHeader( "<div>" +  header + "</div>" + jumpTo);
  configModule.render();
  treeReady();
}
  
function updateJumpTo( list )
{   
  var html = "";
  for ( var i = 0; i < list.length; i++ )
  {
    html += '<a href="' + configFrameUrl + '#' + list[i] + '" target="configIFrame">' + list[i] + '</a>  ';
  }
  $('jumpTo').innerHTML = html;
}
	
function iframeReady()
{
  AjaxInfo.getAnchors( dbIndex, configKey, updateJumpTo );
  loadingModule.hide();
  configModule.show();
}
	
function treeReady()
{
  $('info').innerHTML = "";
}
	
//When the DOM is done loading, we can initialize our TreeView
//instance:
YAHOO.util.Event.onContentReady( "doc3", init );
	
</script>

</head>
<body class="yui-skin-sam" style="background:#edf5ff;">

<%
  String treeUrl = "treeFrame.jsp?db=" + request.getParameter( "db" );
%>

<div id="doc3" class="yui-gd">
  <div class="yui-u first">
    <div id="mainLeft"> 
      <div class="headerDiv">
            <a id="expand" href="#">Expand all</a>
            <a id="collapse" href="#">Collapse all</a>
      </div>
	  <iframe name="treeFrame" id="treeFrame" width="100%" frameborder="0" src="<%= treeUrl%>" ></iframe>
    </div>
  </div> 
  <div class="yui-u">
    <div id="mainRight">
      <div id="loading"><img src="assets/img/default/loading.gif"></div>
	  <div id="config"></div>
    </div>
  </div> 
</div>
<div id="info" align="left"><img src="assets/img/default/loading.gif"></div>
</body>
</html>

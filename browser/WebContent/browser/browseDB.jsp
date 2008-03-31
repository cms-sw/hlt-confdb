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

body { background:#edf5ff }

.yui-module { padding:0px; margin-left:5px; margin-right:5px; margin-bottom:0px; display:none; }
.yui-module .hd { margin-bottom:10px; margin-top:5px; padding-left:5px; background-color:#FFE19A }
.yui-gd { margin-bottom:0px; }
#doc3 { margin-bottom:0px; margin-top:5px; }
#mainLeft { background:#edf5ff; border: 0px solid #B6CDE1; margin:0px; padding:0px }
#mainRight { margin:0px; padding:0px; background-color:#FFF5DF; border: 0px; }
.headerDiv { margin:0px; padding:0.4em; background:white; border: 1px solid #B6CDE1; border-bottom:0px; }
#info { background:white; border: 1px solid #B6CDE1; border-top:0px; }

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
    configModule.render();

    var height = 500;
    if ( parent.tabHeight )
    	height = parent.tabHeight - 50;
    document.getElementById( "treeFrame" ).height = height;
}
	
	
function labelClicked( node )
{
  if ( !node.data.key )
  	return;
  configModule.hide();

  document.getElementById( "mainRight" ).style.border = '1px solid #B6CDE1';

  configKey = node.data.key;
  dbIndex = node.data.dbIndex;
  configFrameUrl = "convert2Html.jsp?configKey=" + node.data.key + "&dbIndex=" + node.data.dbIndex + "&bgcolor=FFF5DF"; 
  var height = YAHOO.util.Dom.getViewportHeight() - 75;
  configModule.setBody( '<iframe src="' + configFrameUrl + '" name="configIFrame" id="configFrame" width="100%" height="'+ height + '" frameborder="0"></iframe>');
  
  loadingModule.show();
  var header = "<b>" + node.data.name + " " + node.data.versionInfo + "</b>";
  loadingModule.setHeader( header );
  loadingModule.render();

  var fileName = node.data.name.replace( '//s/g', '_' ) + "_V" + node.data.version;
  header += '<span style="position:absolute; right:20px;">download <a href="' + fileName + '.cfg?configId='+ node.data.key + '&dbIndex=' + node.data.dbIndex + '">cfg</a>';
  header += '  <a href="' + fileName + '.py?format=python&configId='+ node.data.key + '&dbIndex=' + node.data.dbIndex + '">py</a></span>';
    
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
  loadingModule.hide();
  AjaxInfo.getAnchors( dbIndex, configKey, updateJumpTo );
  configModule.show();
}
	
function treeReady()
{
  document.getElementById( "info" ).innerHTML = "";
  document.getElementById( "info" ).style.border = '0px';
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
      <div id="info"><img src="../img/loading.gif"></div>
	  <iframe name="treeFrame" id="treeFrame" width="100%" frameborder="0" src="<%= treeUrl%>" ></iframe>
    </div>
  </div> 
  <div class="yui-u">
    <div id="mainRight">
      <div id="loading"><img src="../img/loading.gif"></div>
	  <div id="config"></div>
    </div>
  </div> 
</div>
</body>
</html>

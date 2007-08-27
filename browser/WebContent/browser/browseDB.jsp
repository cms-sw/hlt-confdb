<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config browser</title>

<link rel="stylesheet" type="text/css" href="../js/yui/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/tabview/assets/skins/sam/tabview.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/container/assets/skins/sam/container.css" />
<script type="text/javascript" src="../js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../js/yui/container/container.js"></script>

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

.yui-module { padding:0px;margin-left:5px; margin-right:5px; display:none; }
.yui-module .hd { margin-bottom:10px; margin-top:5px; padding-left:5px; background-color:#FFE19A }
/*
.yui-module .bd { border:1px solid green; }
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
	configModule.setBody( '<iframe name="configIFrame" id="configFrame" width="100%" height="500" frameborder="0"></iframe>');
    configModule.render();

    var height = 500;
    if ( parent.tabHeight )
    	height = parent.tabHeight - 46;
    document.getElementById( "treeFrame" ).height = height;
}
	
	
function labelClicked( node )
{
  document.getElementById("configFrame").height = 
  	YAHOO.util.Dom.getViewportHeight() - 70;

  if ( !node.data.key )
  	return;
  var configFrameUrl = "convert2Html.jsp?configKey=" + node.data.key + "&dbIndex=" + node.data.dbIndex + "&bgcolor=FFF5DF"; 
  document.getElementById("configFrame").src = configFrameUrl;
  
  loadingModule.show();
  configModule.hide();
  var header = "<b>" + node.data.name + " " + node.data.label + "</b>";
  loadingModule.setHeader( header );
  loadingModule.render();

  var fileName = node.data.name.replace( '//s/g', '_' ) + "_V" + node.data.version;
  header += '<a style="position:absolute; right:20px;" href="' + fileName + '.cfg?configKey='+ node.data.key + '&dbIndex=' + node.data.dbIndex + '">download cfg</a>';
  var jumpTo = '<div>'
 	+  '<a class="navi" href="' + configFrameUrl + '#paths" target="configIFrame">paths</a>  ' 
 	+  '<a href="' + configFrameUrl + '#modules" target="configIFrame">modules</a>  ' 
   	+  '<a href="' + configFrameUrl + '#services" target="configIFrame">services</a>  ' 
   	+  '<a href="' + configFrameUrl + '#edsources" target="configIFrame">ed_sources</a>  ' 
    +  '<a href="' + configFrameUrl + '#essources" target="configIFrame">es_sources</a>  ' 
    +  '<a href="' + configFrameUrl + '#esmodules" target="configIFrame">es_modules</a>  ' 
    + '</div>';
  configModule.setHeader( "<div>" +  header + "</div>" + jumpTo);
  configModule.render();
}
	
function iframeReady()
{
  loadingModule.hide();
  configModule.show();
}
	
//When the DOM is done loading, we can initialize our TreeView
//instance:
YAHOO.util.Event.onContentReady( "doc3", init );
	
</script>

</head>
<body class="yui-skin-sam" style="background:#edf5ff">

<%
  String treeUrl = "treeFrame.jsp?db=" + request.getParameter( "db" );
%>

<div id="doc3"> 
  <div id="hd"><!-- header --></div>  
  <div id="bd"><!-- body --></div>  
    <div class="yui-gd"> 
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
	      <div id="config">
		  </div>
		</div>
	  </div> 
	</div>
  <div id="ft"><!-- footer --></div>  
</div>
</body>
</html>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config</title>

<%
  String db = request.getParameter( "db" );
  String yui = "../js/yui";
  String css = "../css";
  String js = "../js";
  String img = "../img";
  boolean online = false;
  if ( db.equals( "online" ) )
  {
	online = true;
  	yui = "../../../gui/yui";
    css = "../../css";
    js = "../../js";
    img = "../../img";
  }
%>

<link rel="stylesheet" type="text/css" href="<%=yui%>/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="<%=yui%>/container/assets/skins/sam/container.css" />
<link rel="stylesheet" type="text/css" href="<%=yui%>/resize/assets/skins/sam/resize.css"" />
<link rel="stylesheet" type="text/css" href="<%=yui%>/treeview/assets/skins/sam/treeview.css" />
<link rel="stylesheet" type="text/css" href="<%=css%>/folders/tree.css">
<link rel="stylesheet" type="text/css" href="<%=css%>/confdb.css" />

<script type="text/javascript" src="<%=yui%>/utilities/utilities.js"></script>
<script type="text/javascript" src="<%=yui%>/cookie/cookie-min.js"></script>
<script type="text/javascript" src="<%=yui%>/datasource/datasource-beta-min.js"></script>
<script type="text/javascript" src="<%=yui%>/resize/resize-min.js"></script>
<script type="text/javascript" src="<%=yui%>/json/json-min.js"></script>
<script type="text/javascript" src="<%=yui%>/treeview/treeview-min.js"></script>
<script type="text/javascript" src="<%=yui%>/container/container-min.js"></script>
<script type="text/javascript" src="<%=yui%>/button/button-min.js"></script>
<script type="text/javascript" src="<%=js%>/jquery-1.6.4.min.js"></script>



<style>

body, #pg, .blindTable { 
    padding:0px; 
    margin:0px; 
}

body {
    overflow: hidden;
    position: fixed;
}

.blindTable td {
  border:0px;
}


#mainLeft { 
	float: left;
	width: 33%;
	margin:0px; 
	padding: 2px 5px 0px 1px;
}

#mainRight { 
	margin:0px; 
	padding: 2px 1px 0px 0px;
	float: right;
}


#leftHeaderDiv { 
	margin:0px; 
	padding-top: 1px;
	padding-left: 1px;
	padding-bottom: 2px;
	border-bottom: 0px;
	overflow: auto;
}

#rightHeaderDiv {
	border-width: 1px;
	border-style: solid;
	border-bottom: 0px;
	padding:2px; 
}


.yui-nav,
.yui-content {
	border-width: 1px; 
	border-style: solid;
}

.yui-nav {
	border-top: 0px; 
	border-bottom: 0px; 
}

#treeDiv {
    overflow: auto;
}

</style>

<script type="text/javascript">

<%
  String height = request.getParameter( "height" );
  if ( height == null )
	  out.println( "var displayHeight = 0;" );
  else
	  out.println( "var displayHeight = " + height + ";" );
  String width = request.getParameter( "width" );
  if ( width == null )
	  out.println( "var displayWidth = 0;" );
  else
	  out.println( "var displayWidth = " + width + ";" );

  out.println( "var imgDir = '" + img + "';" );
  String filter = request.getParameter( "filter" );
  if ( filter == null )
	  out.println( "var filter = '';" );
  else
	  out.println( "var filter = '" + filter + "';" );
  
  if ( db != null )
  {
	  if ( online )
	  {
		out.println( "var dbName = 'online';" );
		String tabId = request.getParameter( "tabId" );
		if ( tabId == null )
		      out.println( "var pageId = 'online';" );
		else
		      out.println( "var pageId = '" + tabId + "';" );
		out.println( "var onlineMode = true;" );
		out.println( "AjaxInfoDir = '../jsp/hlt/';" );
	  }
	  else
	  {
		out.println( "var dbName = '" + db + "';" );
		out.println( "var onlineMode = false;" );
		out.println( "var pageId = '" + db + "';" );
		out.println( "AjaxInfoDir = './';" );
	  }
  }
  else
  {
	  out.println( "var onlineMode = false;" );
	  out.println( "var pageId = \"any\";" );
	  out.println( "var dbName = null;" );
	  out.println( "AjaxInfo._path = '../browser/AjaxInfo.jsp';" );
  }
%>


var configFrameUrl,
	configKey,
	fullName,
	Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event,
    mainLeft = null,
    mainRight = null,
    displayWidth,
    resize,
    oldWidth = "200px",
    treeWidth,
    hltCookie = null,
    cookieExpires,
    tree,
  	iframe = "", 
  	tooltipElements = [];

	
function init() 
{
    cookieExpires = new Date();
    cookieExpires.setFullYear( cookieExpires.getFullYear() + 1 );
    if ( parent && parent.cookie )
    {
      hltCookie = YAHOO.util.Cookie.getSubs( pageId );
      if ( hltCookie == null )
    	hltCookie = new Object();
    }

    mainLeft = Dom.get('mainLeft');
    mainRight = Dom.get('mainRight');

	if ( displayHeight == 0 )
		displayHeight = Dom.getViewportHeight();
	if ( displayWidth == 0 )
		displayWidth = Dom.getViewportWidth();

//    Dom.setStyle( 'collapseDiv', 'visibility', 'hidden' );
    Dom.setStyle( 'expandDiv', 'visibility', 'hidden' );
    Dom.setStyle(  'allDiv', 'height',  displayHeight + 'px' );

	var treeHeight = displayHeight - 50;
    $('#treeDiv').height( treeHeight );
//    Dom.setStyle( 'treeDiv', 'max-height',  treeHeight + 'px' );

    resize = new YAHOO.util.Resize('mainLeft', {
            proxy: true,
            ghost: true,
            handles: ['r'],
            maxWidth: displayWidth,
            minWidth: 10
        });
    resize.on('startResize', function(ev) {
//		if ( YAHOO.env.ua.ie > 0 ) 
//  		  Dom.setStyle( 'treeDiv', 'visibility', 'hidden' );
        });

    resize.on('resize', function(ev) {
            treeWidth = ev.width;
            if ( hltCookie && treeWidth > 10 )
            {
              hltCookie.treeWidth = treeWidth;
		  	  YAHOO.util.Cookie.setSubs( pageId, hltCookie, { expires: cookieExpires } );
		  	}
            var width = displayWidth - treeWidth - 8;
            Dom.setStyle( mainRight, 'height', displayHeight + 'px' );
            Dom.setStyle( mainRight, 'width', width + 'px');
            Dom.setStyle( 'configInput', 'width', (w - 50)+ 'px');
          	$('#debugDiv').html( 'resize: ' + width + ' x ' + displayHeight );

//          if ( iframe.length > 0 )
//            Dom.get( mainRight ).innerHTML = iframe;
//        	Dom.get( 'mainRight' ).innerHTML = 'resize ' + width + ' x ' + ev.height;
    });

  treeWidth = displayWidth / 3;
  if ( hltCookie && hltCookie.treeWidth )
	  	treeWidth = hltCookie.treeWidth;
  resize.resize(null, displayHeight, treeWidth, 0, 0, true);


  //handler for expanding all nodes
  Event.on("expand", "click", function(e) {
			tree.expandAll();
			Event.preventDefault(e);
		});
		
  //handler for collapsing all nodes
  Event.on("collapse", "click", function(e) {
			tree.collapseAll();
			Event.preventDefault(e);
		});

  //handler for collapseDiv
  Event.on("collapseDiv", "click", function(e) {
			oldWidth = Dom.getStyle( mainLeft, 'width' );
	        resize.resize( null, displayHeight, 1, 0, 0, true);
            Dom.setStyle( mainLeft, 'visibility', 'hidden' );
            Dom.setStyle( 'expandDiv', 'visibility', 'visible' );
		});

  //handler for expandDiv
  Event.on( "expandDiv", "click", function(e) {
            Dom.setStyle( 'expandDiv', 'visibility', 'hidden' );
	        resize.resize( null, displayHeight, oldWidth, 0, 0, true);
            Dom.setStyle( mainLeft, 'visibility', 'visible' );
		});


  if ( onlineMode )
  {
    var submitButton = new YAHOO.widget.Button( { label: "Submit", id: "submitbutton", container: "buttonTD" });
    submitButton.on("click", onSubmitClick ); 	
  
    Dom.setStyle( 'buttonTD', 'visibility', 'hidden' );
    Dom.setStyle( 'downloadTD', 'visibility', 'hidden' );
  }

  $.ajaxSetup( { timeout: 60000 } );
  $.getJSON( AjaxInfoDir + 'AjaxInfo.jsp', { method: 'getTree', p1: 'string:' + dbName, p2: 'string:' + filter }, createTree ).fail( ajaxFailure );
	  
  $( window ).bind( 'resize', resizeAll );
}

function ajaxFailure( jqXHR )
{
	$('#leftHeaderDiv').html('');
	alert( "ajax failure: " + jqXHR.statusText + ". Maybe you try again...");
}

function resizeAll( ev )
{
	displayWidth = $(window).width();
	displayHeight = $(window).height();
	
	$('#treeDiv').height( displayHeight - 50 );
	$('#mainRight').height( displayHeight );
	
    var width = displayWidth - treeWidth - 8;
	$('#mainRight').width( width );


  	if ( iframe.length > 0 )
  	{
  		$('#configIFrame').height( displayHeight );
//		$('#configIFrame').width( width );
	  	$('#debugDiv').html( 'resizeAll: ' + width + ' x ' + displayHeight );
	}
  		
//    Dom.get( mainRight ).innerHTML = iframe;
}


function onSubmitClick( event ) 
{ 
  if ( parent && parent.submitConfig )
    parent.submitConfig( configKey, fullName );
} 
	
function showNew()
{
	var config = $('#configInput').val();
	if ( !isNaN( config ) )
		showConfigForRun( config );
	else
	{		
		expandTree( config, true );
		showConfig( config );
	}
	return false;
}
	
function createTree( treeData, textStatus, jqXHR )
{
	if ( treeData.exceptionThrown )
	{
		$('#leftHeaderDiv').html('');
		alert( treeData.exception + ': ' + treeData.message );
		return;
	}

    if ( treeData.ajaxFailure )
    {
      $('#leftHeaderDiv').html('');
	  alert( 'Ajax failure: ' + treeData.ajaxFailure );
	  return;
    }

	tree = new YAHOO.widget.TreeView("treeDiv");
	var parentNode = tree.getRoot();
	createTreeRecursiveLoop( parentNode, treeData );
	tree.render();
	tree.subscribe( "clickEvent", configSelected );
	var header = '<table width="100%"><tr>';
	if ( filter && filter.length > 0 )
		header += "<td><b>filter: "  + filter + "</b></td><td><div style='width:50px'></div></td>";
	else
		header += "<td align='left'colspan='2'><form onsubmit='return showNew()'><input id='configInput' type='text'></form></td><td align='right'><div id='collapseDiv'><img src='<%=img%>/collapse.gif'></div></td></tr><tr>";
	header += '<td align="center"><a id="expand" href="#">Expand all</a></td><td><a id="collapse" href="#">Collapse all</a></td></tr></table>';
  	Dom.get( 'leftHeaderDiv' ).innerHTML = header; 
  	Dom.setStyle( 'collapseDiv', 'visibility', 'visible' );
  	
  	// uses too much CPU power!
	//tooltip = new YAHOO.widget.Tooltip( "tooltip", { context: tooltipElements } ); 

	if ( hltCookie )
  	{
    	var config = hltCookie.selectedConfig;
    	if ( config != null )
        	expandTree( config, false );
  	}
	$('#configInput').width( treeWidth - 50 );
	$('#configInput').css( 'color', 'lightgrey' );
	if ( dbName == 'ORCOFF' )
		$('#configInput').val( 'enter config name or run number here' );
	else
		$('#configInput').val( 'enter config name here' );
	$('#configInput').focus( function() {
		if ( $(this).val().search( /^enter/ ) != -1 )
		{
			$(this).val( '' );
			$(this).css( 'color', 'black' );
		}
	});
}
	

function createTreeRecursiveLoop( parentNode, treeData )
{
	for ( var i = 0; i < treeData.configs.length; i++ )
	{    
		var config = treeData.configs[i];
		config.nodeData.expanded = false;
		var configNode = new YAHOO.widget.ConfigNode( config.nodeData, parentNode );
		if ( config.nodeData.title )
		  tooltipElements.push( configNode.labelElId );
		for ( var ii = 0; ii < config.subnodes.length; ii++ )
		{    
		  config.subnodes[ii].nodeData.expanded = false;
		  var subnode = new YAHOO.widget.ConfigNode( config.subnodes[ii].nodeData, configNode );
		  if ( config.subnodes[ii].nodeData.title )
		    tooltipElements.push( subnode.labelElId );
		}
	}

	for ( var i = 0; i < treeData.dirs.length; i++ )
	{    
		var dir = treeData.dirs[i];
	    var name = dir.name;
		var dirNode = new YAHOO.widget.TextNode( { label: name, expanded: false }, parentNode );
		createTreeRecursiveLoop( dirNode, dir );
	}
}


function expandTree( config, focus )
{
	var node = tree.getRoot();
	var subdirs = config.split( "/" );
	if ( subdirs.length > 1 && subdirs[0] == "" )
	{
		subdirs.shift();
		subdirs[0] = '/' + subdirs[0];
		findNode( node, config, subdirs, focus );
	}
}


function findNode( node, config, subdirs, focus )
{  
  if ( !node.hasChildren() )
    return;
  if ( subdirs.length == 0 )
    return;
  var nodes = node.children;
  for ( var i = 0;  i < nodes.length; i++ )
  {
    var fullName = nodes[i].data.fullName;
    if ( fullName && fullName == config )
    {
        if ( focus )
	        nodes[i].focus();
	    return;
    }
  }

  var subdir = subdirs.shift();
  for ( var i = 0;  i < nodes.length; i++ )
  {
    var label = nodes[i].label;
    if ( label == subdir )
    {
      nodes[i].expand();
      findNode( nodes[i], config, subdirs, focus );
    }
  }
}	
	
	
function configSelected( event )
{
  var node = event.node;
  if ( !node.data.key )
  	return;
  	
  node.focus();
  
  configKey = node.data.key;
  fullName = node.data.fullName;
  showConfig( fullName );
  return false;
}

function showConfig( configName )
{
  iframe = '<iframe src="../show.jsp?dbName=' + dbName + '&configName=' + configName + '" name="configIFrame" id="configIFrame" width="100%" height="' + displayHeight + '" frameborder="0"></iframe>';
  Dom.get( mainRight ).innerHTML = iframe;

  if ( hltCookie )
  {
    hltCookie.selectedConfig = configName;
    YAHOO.util.Cookie.setSubs( pageId, hltCookie, { expires: cookieExpires } );
  }
}


function showConfigForRun( runNumber )
{
  iframe = '<iframe src="../show.jsp?runNumber=' + runNumber + '" name="configIFrame" id="configIFrame" width="100%" height="' + displayHeight + '" frameborder="0"></iframe>';
  Dom.get( mainRight ).innerHTML = iframe;
}



	
YAHOO.widget.ConfigNode = function(oData, oParent ) 
{
  //this.labelStyle = "icon-gen";
  //this.href = "javascript:dummy()";
  //oData.href = "javascript:dummy()";
  YAHOO.widget.ConfigNode.superclass.constructor.call( this, oData, oParent );
};

YAHOO.extend(YAHOO.widget.ConfigNode, YAHOO.widget.TextNode, 
{
  configNode: true,

  updateIcon: function() {
        if (this.hasIcon) {
            var el = this.getToggleEl();
            if (el) {
                el.className = this.getStyle();
            }
        }
    },
    
   getDepthStyle: function(depth) {
     if ( !this.hasChildren(false) && depth >= this.depth - 1 )
       return "ygtvblankdepthcell";
     else
       return (this.getAncestor(depth).nextSibling) ? 
            "ygtvdepthcell" : "ygtvblankdepthcell";
    },

    /**
     * Returns the css style name for the toggle
     * @method getStyle
     * @return {string} the css class for this node's toggle
     */
  getStyle: function() 
  {
     // location top or bottom, middle nodes also get the top style
     var loc = (this.nextSibling) ? "t" : "l";

     // type p=plus(expand), m=minus(collapase), n=none(no children)
     var type = "n";
     if ( this.hasChildren(false) )
     {
       if ( this.expanded )
         return "xygtv" + loc + "m";
       else
         return "xygtv" + loc + "p";
     }
     return "ygtv" + loc + type;
  }
});


function dummy( node )
{
}

//When the DOM is done loading, we can initialize our TreeView
//instance:
YAHOO.util.Event.onContentReady( "allDiv", init );
	
</script>

</head>
<body class="yui-skin-sam">
<div id="allDiv">
  <div class="skin1" id="mainLeft">
    <div id="leftHeaderDiv" class='tree1'><img src="<%=img%>/wait.gif"></div>
    <div style="position:absolute; left:0px; top:2px; z-index:2; cursor:pointer;" id="expandDiv" ><img src="<%=img%>/tree/expand.gif"></div>
    <div align="left" id="treeDiv" class="tree1"></div>
  </div>
  <div id="mainRight"></div>
</div>
<div style="position:absolute; right:5px; top:2px; visibility:hidden;" id="debugDiv" ></div>
</body>
</html>


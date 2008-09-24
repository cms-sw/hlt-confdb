<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config</title>

<link rel="stylesheet" type="text/css" href="../js/yui/reset-fonts-grids/reset-fonts-grids.css" />
<!-- 
<link rel="stylesheet" type="text/css" href="../js/yui/base/base-min.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/datatable/assets/skins/sam/datatable.css" />
 -->
<link rel="stylesheet" type="text/css" href="../js/yui/container/assets/skins/sam/container.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/resize/assets/skins/sam/resize.css"" />
<link rel="stylesheet" type="text/css" href="../js/yui/treeview/assets/skins/sam/treeview.css" />
<link rel="stylesheet" type="text/css" href="../css/folders/tree.css">
<link rel="stylesheet" type="text/css" href="../css/confdb.css" />

<script type="text/javascript" src="../js/yui/utilities/utilities.js"></script>
<script type="text/javascript" src="../js/yui/cookie/cookie-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/datasource/datasource-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/resize/resize-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/json/json-min.js"></script>
<script type="text/javascript" src="../js/yui/treeview/treeview.js"></script>
<script type="text/javascript" src="../js/yui/container/container-min.js"></script>
<script type="text/javascript" src="../js/HLT.js"></script>
<script type="text/javascript" src="../js/AjaxInfo.js"></script>




<style>

body {
	margin:0px; 
	padding:0px; 
    overflow: hidden;
    position: fixed;
}
.blindTable {
	margin:0px; 
	padding:0px; 
}

.blindTable td {
  border:0px;
}


.topDiv {
	height: 1.2em;
}

#doc3 { 
    padding:0px; 
    margin:0px; 
}

#pg {
	margin:0px; 
	padding:0px; 
}

#pg .yui-g {
}

#pg .yui-u {
}

#mainLeft { 
	border: 0px solid #B6CDE1; 
	margin:0px; 
	padding-right:5px;
	padding-left:1px; 
	padding-top:1px; 
}

#mainRight { 
}




#headerDiv { 
	margin:0px; 
	padding:0.4em; 
	background:white; 
	border: 1px solid #B6CDE1; 
	border-bottom: 0px;
}

#rightHeaderDiv {
	background-color:#FFE19A;
	border: 1px solid #B6CDE1; 
	margin:0px; 
	margin-top:1px; 
	margin-bottom:3px; 
	padding:1px; 
}

#rightHeaderBottomDiv {
	height:20px;
}

#configDiv {
    overflow: hidden;
}

#treeDiv {
    overflow: auto;
}

</style>

<%
  String db = request.getParameter( "db" );
  out.println( "<script type=\"text/javascript\">" );
  if ( db != null )
  {
	  out.println( "var pageId = \"" + db + "\";" );
	  out.println( "var dbName = \"" + db + "\";" );
  }
  else
  {
	  out.println( "var pageId = \"any\";" );
	  out.println( "var dbName = null;" );
  }
	  
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
  out.println( "</script>" );
%>

<script type="text/javascript">

var configFrameUrl,
	configKey,
	dbIndex,
	Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event,
    mainLeft = null,
    mainRight = null,
    displayWidth,
    resize,
    oldWidth = "200px",
    detailsMode = true,
    cookie = null,
    cookieExpires,
    tree,
  	tooltip, 
  	tooltipElements = []; 
	
function init() 
{
	AjaxInfo._path = '../browser/AjaxInfo.jsp';

    cookieExpires = new Date();
    cookieExpires.setFullYear( cookieExpires.getFullYear() + 1 );
    if ( parent && parent.cookie )
    {
      cookie = YAHOO.util.Cookie.getSubs( pageId );
      if ( cookie == null )
    	cookie = new Object();
    }

    mainLeft = Dom.get('mainLeft');
    mainRight = Dom.get('mainRight');

	if ( displayHeight == 0 )
		displayHeight = Dom.getViewportHeight();
	if ( displayWidth == 0 )
		displayWidth = Dom.getViewportWidth();

    Dom.setStyle( 'collapseDiv', 'visibility', 'hidden' );
    Dom.setStyle( 'expandDiv', 'visibility', 'hidden' );
    Dom.setStyle( mainRight, 'visibility', 'hidden' );
    Dom.setStyle(  'doc3', 'height',  displayHeight + 'px' );
    Dom.setStyle(  'pg', 'height',  displayHeight + 'px' );
    Dom.setStyle(  'pg', 'width',  displayWidth + 'px' );

	var treeHeight = displayHeight - 30;
    Dom.setStyle( 'treeDiv', 'max-height',  treeHeight + 'px' );

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
            var w = ev.width;
            if ( cookie && w > 10 )
            {
              cookie.treeWidth = w;
		  	  YAHOO.util.Cookie.setSubs( pageId, cookie, { expires: cookieExpires } );
		  	}
            var width = displayWidth - w - 8;
            Dom.setStyle( mainRight, 'height', displayHeight + 'px' );
            Dom.setStyle( mainRight, 'width', width + 'px');
            Dom.setStyle( 'configDiv', 'width', width + 'px');
        });

  var treeWidth = 200;
  if ( cookie && cookie.treeWidth )
  	treeWidth = cookie.treeWidth;
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

  Event.on( "detailsButton", "click", selectView, "details" );
  Event.on( "summaryButton", "click", selectView, "summary" );

  AjaxInfo.getTree( dbName, createTree );	
}
	
	
function createTree( treeData )
{
	if ( treeData.exceptionThrown )
	{
		alert( treeData.exception + ': ' + treeData.message );
		return;
	}

	tree = new YAHOO.widget.TreeView("treeDiv");
	var parentNode = tree.getRoot();
	createTreeRecursiveLoop( parentNode, treeData );
	tree.draw();
	tree.subscribe( "labelClick", labelClicked );
  	Dom.get( 'headerDiv' ).innerHTML = '<a id="expand" href="#">Expand all</a> <a id="collapse" href="#">Collapse all</a>'; 
  	Dom.setStyle( 'collapseDiv', 'visibility', 'visible' );
  	
  	// uses too much CPU power!
	//tooltip = new YAHOO.widget.Tooltip( "tooltip", { context: tooltipElements } ); 

	if ( cookie )
  	{
    	var config = cookie.selectedConfig;
    	if ( config != null )
    	{ 
      	  var node = tree.getRoot();
      	  var subdirs = config.split( "/" );
      	  if ( subdirs.length > 1 && subdirs[0] == "" )
      	  {
        	subdirs.shift();
        	subdirs[0] = '/' + subdirs[0];
      		findNode( node, config, subdirs );
      	  }
    	}
  	}
}
	

function createTreeRecursiveLoop( parentNode, treeData )
{
	for ( var i = 0; i < treeData.configs.length; i++ )
	{    
		var config = treeData.configs[i];
		var configNode = new YAHOO.widget.ConfigNode( config.nodeData, parentNode, false );
		if ( config.nodeData.title )
		  tooltipElements.push( configNode.labelElId );
		for ( var ii = 0; ii < config.subnodes.length; ii++ )
		{    
		  var subnode = new YAHOO.widget.ConfigNode( config.subnodes[ii].nodeData, configNode, false );
		  if ( config.subnodes[ii].nodeData.title )
		    tooltipElements.push( subnode.labelElId );
		}
	}

	for ( var i = 0; i < treeData.dirs.length; i++ )
	{    
		var dir = treeData.dirs[i];
	    var name = dir.name;
		var dirNode = new YAHOO.widget.TextNode( name, parentNode, false );
		createTreeRecursiveLoop( dirNode, dir );
	}
}

function findNode( node, config, subdirs )
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
      return;
  }

  var subdir = subdirs.shift();
  for ( var i = 0;  i < nodes.length; i++ )
  {
    var label = nodes[i].label;
    if ( label == subdir )
    {
      nodes[i].expand();
      findNode( nodes[i], config, subdirs );
    }
  }
}	
	
	
function labelClicked( node )
{
  if ( !node.data.key )
  	return;

  var mode = 'details';
  if ( cookie && cookie.mode )
    mode = cookie.mode;
  if ( mode == "summary" )
  	detailsMode = false;
  else
  	detailsMode = true;

  Dom.setStyle( mainRight, 'visibility', 'visible' );

  configKey = node.data.key;
  dbIndex = node.data.dbIndex;
  var fullName = node.data.fullName;
  Dom.get( 'fullNameTD' ).innerHTML = "<b>" + fullName + "</b>";
  var fileName = node.data.name.replace( '//s/g', '_' ) + "_V" + node.data.version;

  Dom.get( 'downloadTD' ).innerHTML = 'download ' 
    + '<a href="' + fileName + '.cfg?configId='+ configKey + '&dbIndex=' + dbIndex + '">cfg</a> '
    + '<a href="' + fileName + '.py?format=python&configId='+ configKey + '&dbIndex=' + dbIndex + '">py</a>';

  showConfig();
  if ( cookie )
  {
    cookie.selectedConfig = fullName;
    YAHOO.util.Cookie.setSubs( pageId, cookie, { expires: cookieExpires } );
  }
}

function showConfig()
{  
  Dom.get( 'rightHeaderBottomDiv' ).innerHTML = '<img src="../assets/img/wait.gif">';
  if ( detailsMode == true )
  {
    Dom.setStyle( 'summaryButton', 'background-image', 'url(../assets/img/tree/expand.gif)' );
    Dom.setStyle( 'detailsButton', 'background-image', 'url(../assets/img/tree/collapse.gif)' );
    configFrameUrl = "convert2Html.jsp?configKey=" + configKey + "&dbIndex=" + dbIndex + "&bgcolor=FFF5DF"; 
  }
  else
  {
    Dom.setStyle( 'detailsButton', 'background-image', 'url(../assets/img/tree/expand.gif)' );
    Dom.setStyle( 'summaryButton', 'background-image', 'url(../assets/img/tree/collapse.gif)' );
    configFrameUrl = "showSummary.jsp?configKey=" + configKey + "&dbIndex=" + dbIndex + "&bgcolor=FFF5DF"; 
  }
  Dom.setStyle( 'detailsButton', 'background-repeat', 'no-repeat' );
  Dom.setStyle( 'detailsButton', 'background-position', 'left' );
  Dom.setStyle( 'summaryButton', 'background-repeat', 'no-repeat' );
  Dom.setStyle( 'summaryButton', 'background-position', 'left' );
  var xy = Dom.getXY( 'configDiv' );
  var height = displayHeight - xy[1] - 2;
  var configDiv = Dom.get( 'configDiv' );
  Dom.setStyle( configDiv, 'height', height + 'px' );
  configDiv.innerHTML = '<iframe src="' + configFrameUrl + '" name="configIFrame" id="configFrame" width="100%" height="'+ height + '" frameborder="0" ' + (detailsMode ? '' : ' scrolling="no"') + '></iframe>';
}
  
function updateJumpTo( list )
{   
  var html = "";
  for ( var i = 0; i < list.length; i++ )
  {
    html += '<a href="' + configFrameUrl + '#' + list[i] + '" target="configIFrame">' + list[i] + '</a>  ';
  }
  Dom.get( 'rightHeaderBottomDiv' ).innerHTML = html;
}
	
  
  
function iframeReady()
{
  if ( detailsMode == true )
    AjaxInfo.getAnchors( dbIndex, configKey, updateJumpTo );
  else
	Dom.get( 'rightHeaderBottomDiv' ).innerHTML = "";
}
	
	
function selectView( event, selected )
{
  YAHOO.util.Event.preventDefault(event);
  if ( selected == "details"  &&  detailsMode == true )
  	return;
  if ( selected == "summary"  &&  detailsMode == false )
  	return;

  if ( selected == "details" )
  	detailsMode = true;
  else
  	detailsMode = false;

  if ( cookie )
  {
    cookie.mode = selected;
	YAHOO.util.Cookie.setSubs( pageId, cookie, { expires: cookieExpires } );
  }
  
  showConfig();
}

YAHOO.widget.ConfigNode = function(oData, oParent, expanded) 
{
	this.labelStyle = "icon-gen";
	this.href = "javascript:dummy()";
	if (oData) { 
		this.init(oData, oParent, expanded);
		this.setUpLabel(oData);
	}

};

YAHOO.extend(YAHOO.widget.ConfigNode, YAHOO.widget.TextNode, {

	configNode: true,

    /**
     * Returns the css style name for the toggle
     * @method getStyle
     * @return {string} the css class for this node's toggle
     */
    getStyle: function() {
        if (this.isLoading) {
            return "ygtvloading";
        } else {
            // location top or bottom, middle nodes also get the top style
            var loc = (this.nextSibling) ? "t" : "l";

            // type p=plus(expand), m=minus(collapase), n=none(no children)
            var type = "n";
            if (this.hasChildren(true) || (this.isDynamic() && !this.getIconMode())) {
            // if (this.hasChildren(true)) {
                type = (this.expanded) ? "m" : "p";
            }

            return "xygtv" + loc + type;
        }
    },

    toString: function() { 
        return "ConfigNode (" + this.index + ") " + this.label;
    }

});


function dummy( node )
{
}

	
	
//When the DOM is done loading, we can initialize our TreeView
//instance:
YAHOO.util.Event.onContentReady( "doc3", init );
	
</script>

</head>
<body class="yui-skin-sam">


<div id="doc3">
  <div id="pg">
    <div class="yui-g" id="pg-yui-g">
	  <div class="yui-u first" id="mainLeft">
    	 <div id="headerDiv" class="topDiv">
			<img src="../assets/img/wait.gif">
         </div>
         <div style="position:absolute; right:8px; top:3px; z-index:1; cursor:pointer" id="collapseDiv" ><img src="../assets/img/collapse.gif"></div>
         <div style="position:absolute; left:0px; top:2px; z-index:2; cursor:pointer;" id="expandDiv" ><img src="../assets/img/tree/expand.gif"></div>
         <div align="left" id="treeDiv" style="background:white; border: 1px solid #B6CDE1; border-top:0px;">
         </div>
   	  </div>

      <div class="yui-u" id="mainRight">
        <div id="rightHeaderDiv">
  		  <table width='100%' class='blindTable'><tr>
  		    <td id='fullNameTD'><b>/PATH/CONFIG/VERSION</b></td>
  		    <td></td>
  		    <td><div class="dropDownButton" id="detailsButton">details</div></td>
  			<td><div class="dropDownButton" id="summaryButton">summary</div></td>
  			<td align="right" id='downloadTD'> download cfg py</td>
		  </tr></table>
          <div id="rightHeaderBottomDiv"></div>
        </div>
		<div id="configDiv"></div>
      </div>
    </div>
  </div>
</div>
</body>
</html>


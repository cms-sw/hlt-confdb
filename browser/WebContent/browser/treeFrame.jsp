<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="confdb.converter.DbProperties"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="confdb.converter.Converter"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config tree</title>

<link rel="stylesheet" type="text/css" href="../js/yui/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/treeview/assets/skins/sam/treeview.css" />
<script type="text/javascript" src="../js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../js/yui/treeview/treeview.js"></script>

<link rel="stylesheet" type="text/css" href="assets/css/folders/tree.css"><!-- Some custom style for the expand/contract section-->

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

.icon-gen, .icon-gen:link, .icon-gen:visited, .icon-gen:hover  { padding-left: 20px; background: transparent url(../img/icons.png) 0 -108px no-repeat; 	text-decoration: none;
}
 
 
/* via css class selector */
.xygtvtn {background: transparent;  width:1em; height:20px; }
.xygtvtm { background: url(assets/img/menu/collapse.gif) 0 6px no-repeat; width:1em; height:22px; cursor:pointer }
.xygtvtmh { background: url(assets/img/menu/collapseh.gif) 0 6px no-repeat; width:1em; height:22px; cursor:pointer }
.xygtvtp { background: url(assets/img/menu/expand.gif) 0 6px no-repeat; width:1em; height:22px; cursor:pointer }
.xygtvtph { background: url(assets/img/menu/expandh.gif) 0 6px no-repeat; width:1em; height:22px; cursor:pointer }
.xygtvln { background: transparent; width:1em; height:20px; }
.xygtvlm { background: url(assets/img/menu/collapse.gif) 0 6px no-repeat; width:1em; height:22px; cursor:pointer }
.xygtvlmh { background: url(assets/img/menu/collapseh.gif) 0 6px no-repeat; width:1em; height:22px; cursor:pointer }
.xygtvlp { background: url(assets/img/menu/expand.gif) 0 6px no-repeat; width:1em; height:22px; cursor:pointer }
.xygtvlph { background: url(assets/img/menu/expandh.gif) 0 6px no-repeat; width:1em; height:22px; cursor:pointer }

</style>

<script type="text/javascript">

YAHOO.widget.ConfigNode = function(oData, oParent, expanded) {
	if (oData) { 
		this.init(oData, oParent, expanded);
		this.setUpLabel(oData);
	}

};

YAHOO.extend(YAHOO.widget.ConfigNode, YAHOO.widget.TextNode, {

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


var tree; //will hold our TreeView instance
	
	
function init() 
{
	//instantiate the tree:
	tree = new YAHOO.widget.TreeView("treeDiv1");
		
	buildTree();
		
	tree.draw();

	tree.subscribe( "labelClick", parent.labelClicked );
}
	
YAHOO.util.Event.onContentReady( "treeDiv1", init );
	
</script>

</head>
<body class="yui-skin-sam" style="background:#edf5ff">

<%!
String prepareTree( String parentNode, confdb.data.Directory directory, confdb.converter.Converter converter )
{
	String str = "";
	confdb.data.ConfigInfo[] configs = converter.listConfigs( directory );
  	for ( int i = 0; i < configs.length; i++ )
  	{
 		confdb.data.ConfigVersion versionInfo = configs[i].version( 0 ); 
		String name = configs[i].name();

		str += "configNode = new YAHOO.widget.ConfigNode( \"" + name + "\", " + parentNode + ", false );\n"
			+ "configNode.labelStyle = \"icon-gen\";";
	
		for ( int ii = 0; ii < configs[i].versionCount(); ii++ )
    	{
	  	  versionInfo = configs[i].version( ii );
	  	  int key = versionInfo.dbId();
	  	  String vx = "V" + versionInfo.version() + "  -  " + versionInfo.created();
	  		str += "var nodeData = { version:\"" + versionInfo.version() + "\", label: \"" + vx + "\", key:\"" + key + "\", name:\"" + name + "\", dbIndex:dbIndex };\n"
			    + "versionNode = new YAHOO.widget.ConfigNode( nodeData, configNode, false);\n";
	    }
	}

	confdb.data.Directory[] list = converter.listSubDirectories( directory );
	for ( int i = 0; i < list.length; i++ )
   	{
		String dirNode = parentNode + i;
		String name = list[i].name();
		int start = name.lastIndexOf( '/' );
		if ( start > 0 )
			name = name.substring( start + 1 );
		str += "var " + dirNode + " = new YAHOO.widget.TextNode( \"" + name + "\", " + parentNode + ", false);\n";
        str += prepareTree( dirNode, list[i], converter );
    }
    return str;
}
%>

<%
  String tree = "";
  Converter converter = Converter.getConverter();
  try {
	  	ConfDBSetups dbs = new ConfDBSetups();
	  	int dbIndex = 1;
	  	String db = request.getParameter( "db" );
	  	if ( db != null )
	  	{
	  		String[] labels = dbs.labelsAsArray();
	  		for ( int i = 0; i < dbs.setupCount(); i++ )
	  		{
	  			if ( db.equalsIgnoreCase( labels[i] ) )
	  			{
	  				dbIndex = i;
	  				break;
	  			}
	  		}
	  	}
	    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
	    dbProperties.setDbUser( "cms_hlt_reader" );
	    converter.setDbProperties( dbProperties );
	    converter.connectToDatabase();
	    confdb.data.Directory root = converter.getRootDirectory();
	    tree = "<script>\n"
	        + "var dbIndex = " + dbIndex + ";\n"
	        + "function buildTree()\n"
	    	+ "{\n"
	    	+ "var parentNode\n"
	    	+ "var configNode\n"
	    	+ "var versionNode\n"
	    	+ "var nodeData\n"
	    	+ "var dir = tree.getRoot();\n";
        tree += prepareTree( "dir", root, converter )
    		 + "}\n</script>\n";
  } catch (Exception e) {
	  out.println( "<script>\nfunction buildTree(){}\n</script>\n"
			  	+ e.toString() + "</body></html>" );
      return;
  }
  finally {
  	converter.disconnectFromDatabase();
  }
%>

<%= tree %>

<div id="treeDiv1" style="background:white"></div>
</body>
</html>

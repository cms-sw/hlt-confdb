<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="confdb.converter.DbProperties"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="confdb.converter.Converter"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config browser</title>

<link rel="stylesheet" type="text/css" href="../js/yui/fonts/fonts-min.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/treeview/assets/skins/sam/treeview.css" />
<script type="text/javascript" src="../js/yui/yahoo/yahoo.js"></script>
<script type="text/javascript" src="../js/yui/event/event.js"></script>
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

<style>
#expandcontractdiv {border:1px dotted #dedede; background-color:#EBE4F2; margin:0 0 .5em 0; padding:0.4em;}
#treeDiv1 { background: #fff; padding:1em; margin-top:1em; }
</style>

</head>
<body class="yui-skin-sam">

<%
  String tree = "";
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
	    Converter converter = Converter.getConverter();
	    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
	    dbProperties.setDbUser( "cms_hlt_reader" );
	    converter.setDbProperties( dbProperties );
	    converter.connectToDatabase();
	    confdb.data.Directory root = converter.getRootDirectory();
	    tree = "<script>\n" 
    	    + "function buildTree()\n"
        	+ "{\n"
    		+ "var parentNode\n"
    		+ "var configNode\n"
    		+ "var versionNode\n"
    		+ "var dir = tree.getRoot();\n"
        	+ prepareTree( "dir", root, converter )
        	+ "}\n"
			+ "</script>\n";
    	converter.disconnectFromDatabase();
%>
    <!-- markup for expand/contract links -->
    <div id="expandcontractdiv">
    	<a id="expand" href="#">Expand all</a>
    	<a id="collapse" href="#">Collapse all</a>
    </div>

    <div id="treeDiv1"></div>
<%
		out.println( tree );
  } catch (Exception e) {
    out.println( "<tr><td>" + e.toString() + "</td></tr>" );
    e.printStackTrace();
  }
%>

<!-- markup for expand/contract links -->

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
	
function treeInit() 
{
	//instantiate the tree:
	tree = new YAHOO.widget.TreeView("treeDiv1");
		
	buildTree();
		
	//once it's all built out, we need to render
	//our TreeView instance:
	tree.draw();

	//handler for expanding all nodes
	YAHOO.util.Event.on("expand", "click", function(e) {
			tree.expandAll();
			YAHOO.util.Event.preventDefault(e);
		});
		
	//handler for collapsing all nodes
	YAHOO.util.Event.on("collapse", "click", function(e) {
			tree.collapseAll();
			YAHOO.util.Event.preventDefault(e);
		});
}
	
//When the DOM is done loading, we can initialize our TreeView
//instance:
YAHOO.util.Event.onDOMReady(treeInit);
	
</script>

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
	  	  String vx = "V" + versionInfo.version() + "  -  " + versionInfo.created();
			str += "versionNode = new YAHOO.widget.ConfigNode( \"" + vx + "\", configNode, false);\n";

/*
	  	  str += "d.add(" + nodeCounter.incrementAndGet() + ", " + thisParent + ", '" + vx 
	     + "','javascript:showSelectedConfig( \\'" + name + "\\', " 
	     + versionInfo.version() + ", " + versionInfo.dbId() + ");', '', '', '../img/spacer.gif' )\n";
    	    }
*/
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
</body>
</html>

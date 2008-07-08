<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="confdb.db.ConfDB"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="confdb.data.Directory"%>
<%@page import="confdb.data.ConfigInfo"%>
<%@page import="confdb.converter.ConverterBase"%>
<%@page import="confdb.converter.ConverterException"%>
<%@page import="confdb.converter.BrowserConverter"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config tree</title>

<link rel="stylesheet" type="text/css" href="../js/yui/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/treeview/assets/skins/sam/treeview.css" />
<script type="text/javascript" src="../js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../js/yui/treeview/treeview.js"></script>

<link rel="stylesheet" type="text/css" href="../assets/css/folders/tree.css">
<link rel="stylesheet" type="text/css" href="../assets/css/confdb.css">


<script type="text/javascript">

YAHOO.widget.ConfigNode = function(oData, oParent, expanded) {
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


var tree; //will hold our TreeView instance
	
	
function init() 
{
	//instantiate the tree:
	tree = new YAHOO.widget.TreeView("treeDiv1");
		
	buildTree();
		
	tree.draw();

	tree.subscribe( "labelClick", parent.labelClicked );
	/*
	tree.subscribe("expand", function(node) {
	    if ( !node.configNode )
	    	return true; 
	   	alert(node.data.key); 
		return true; // return false to cancel the expand 
	       }); 
	       */
}
	
YAHOO.util.Event.onContentReady( "treeDiv1", init );
	
function signalReady()
{
  if ( parent &&  parent.treeReady )
    parent.treeReady();
}

function dummy( node )
{
}

</script>

</head>
<body class="yui-skin-sam" onload="signalReady()">

<%!

String info = "";

String prepareTree( String parentNode, Directory directory )
{
	info = parentNode;
	String str = "";
	ConfigInfo[] configs = directory.listOfConfigurations();
  	for ( int i = 0; i < configs.length; i++ )
  	{
 		confdb.data.ConfigVersion versionInfo = configs[i].version( 0 ); 
		String name = configs[i].name();
		int key = versionInfo.dbId();
		String fullName = configs[i].parentDir().name() + "/" + name + "/V" + versionInfo.version();

	  	String vx = "V" + versionInfo.version() + "  -  " + versionInfo.created();
		str += "var nodeData = { version:\"" + versionInfo.version() + "\", versionInfo: \"" + vx + "\", label: \"" + name + "\", key:\"" + key + "\", name:\"" + name + "\", fullName:\"" + fullName + "\", dbIndex:dbIndex };\n"
			+ "configNode = new YAHOO.widget.ConfigNode( nodeData, " + parentNode + ", false );\n"
			+ "configNode.labelStyle = \"icon-gen\";\n"
			+ "configNode.href = \"javascript:dummy()\";\n";
	
		for ( int ii = 0; ii < configs[i].versionCount(); ii++ )
    	{
	  	  versionInfo = configs[i].version( ii );
		  fullName = configs[i].parentDir().name() + "/" + name + "/V" + versionInfo.version();
	  	  key = versionInfo.dbId();
	  	  vx = "V" + versionInfo.version() + "  -  " + versionInfo.created();
	  	  str += "var nodeData = { version:\"" + versionInfo.version() + "\", versionInfo: \"" + vx +"\", label: \"" + vx + "\", key:\"" + key + "\", name:\"" + name + "\", fullName:\"" + fullName + "\", dbIndex:dbIndex };\n"
			  + "versionNode = new YAHOO.widget.ConfigNode( nodeData, configNode, false);\n";
	    }
	}

	Directory[] list = directory.listOfDirectories();
	for ( int i = 0; i < list.length; i++ )
   	{
		String dirNode = parentNode + i;
		String name = list[i].name();
		int start = name.lastIndexOf( '/' );
		if ( start > 0 )
			name = name.substring( start + 1 );
		str += "var " + dirNode + " = new YAHOO.widget.TextNode( \"" + name + "\", " + parentNode + ", false);\n";
        str += prepareTree( dirNode, list[i] );
    }
    return str;
}
%>

<%
  String tree = "";
  ConverterBase converter = null;
  int dbIndex = 1;
  try {
	  	ConfDBSetups dbs = new ConfDBSetups();
	  	dbIndex = 1;
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
	 	converter = BrowserConverter.getConverter( dbIndex );
		ConfDB confDB = converter.getDatabase();
		
	    Directory root = confDB.loadConfigurationTree();
	    tree = "<script>\n"
	        + "var dbIndex = " + dbIndex + ";\n"
	        + "function buildTree()\n"
	    	+ "{\n"
	    	+ "var parentNode\n"
	    	+ "var configNode\n"
	    	+ "var versionNode\n"
	    	+ "var nodeData\n"
	    	+ "var dir = tree.getRoot();\n";
        tree += prepareTree( "dir", root )
    		 + "}\n</script>\n";
  } catch (Exception e) {
	String errorMessage = "\nERROR!\n";
	if ( e instanceof ConverterException )
	{
	  errorMessage += e.toString();
	  if ( e.getMessage().startsWith( "can't init database connection" )	)
		  errorMessage += " (host = " + (new ConfDBSetups()).host( dbIndex ) + ")";
	}
	else
	{
	  ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	  PrintWriter writer = new PrintWriter( buffer );
	  e.printStackTrace( writer );
	  writer.close();
	  errorMessage += buffer.toString();
	}
	out.println( "<script>\nfunction buildTree(){}\n</script>\n" 
			  + errorMessage + "</body></html>" );
	if ( converter != null )
		  BrowserConverter.deleteConverter( converter );
    return;
  }
%>

<%= tree %>

<div id="treeDiv1" style="background:white; border: 1px solid #B6CDE1; border-top:0px;"></div>
</body>
</html>

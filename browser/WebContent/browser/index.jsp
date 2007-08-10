<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config browser</title>
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
</style>

<link rel="stylesheet" type="text/css" href="../js/yui/fonts/fonts-min.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/treeview/assets/skins/sam/treeview.css" />
<script type="text/javascript" src="../js/yui/yahoo/yahoo.js"></script>
<script type="text/javascript" src="../js/yui/event/event.js"></script>
<script type="text/javascript" src="../js/yui/treeview/treeview.js"></script>

<link rel="stylesheet" type="text/css" href="assets/css/folders/tree.css"><!-- Some custom style for the expand/contract section-->

<style>
#expandcontractdiv {border:1px dotted #dedede; background-color:#EBE4F2; margin:0 0 .5em 0; padding:0.4em;}
#treeDiv1 { background: #fff; padding:1em; margin-top:1em; }
</style>

</head>
<body class="yui-skin-sam">

<%
  String tree = "";
  try {
    confdb.converter.Converter converter = 
       confdb.converter.Converter.getConverter();
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
    <h3>xxxxx</h3>

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
  }
%>

<!-- markup for expand/contract links -->

<script type="text/javascript">

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
	String str = "parentNode = " + parentNode + ";\n";
	confdb.data.ConfigInfo[] configs = converter.listConfigs( directory );
  	for ( int i = 0; i < configs.length; i++ )
  	{
 		confdb.data.ConfigVersion versionInfo = configs[i].version( 0 ); 
		String name = configs[i].name();
	
		str += "configNode = new YAHOO.widget.TextNode( \"" + name + "\", parentNode, false);\n";

/*	
	str += "d.add(" + nodeCounter.incrementAndGet() + ", " + parentNode + ", '" + name 
	     + "','javascript:showSelectedConfig( \\'" + name + "\\', " 
	     + versionInfo.version() + ", " + versionInfo.dbId() 
             + ");', '', '', '../img/page.gif', '../img/page.gif'  );\n";
*/
	
		for ( int ii = 0; ii < configs[i].versionCount(); ii++ )
    	{
	  	  versionInfo = configs[i].version( ii );
	  	  String vx = "V" + versionInfo.version() + "  -  " + versionInfo.created();
			str += "versionNode = new YAHOO.widget.TextNode( \"" + name + vx + "\", configNode, false);\n";

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
		str += "var " + dirNode + " = new YAHOO.widget.TextNode( \"" + list[i].name() + "\", parentNode, false);\n";
        str += prepareTree( dirNode, list[i], converter );
    }
    return str;
}
%>
</body>
</html>

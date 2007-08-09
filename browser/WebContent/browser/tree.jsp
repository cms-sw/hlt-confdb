<%@ page language="java" contentType="text/html" %>
<%@ page import="java.util.concurrent.atomic.AtomicInteger" %>
<%
    response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
    response.setHeader("Pragma","no-cache"); //HTTP 1.0
    response.setDateHeader ("Expires", 0); //prevents caching at the proxyserver
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>

<head>
	<title>HLT config tree</title>

	<link rel="StyleSheet" href="../css/dtree.css" type="text/css" />
	<link rel="StyleSheet" href="../css/common.css" type="text/css" />
	<script type="text/javascript" src="../js/dtree.js"></script>
	<script type="text/javascript" src="../js/common.js"></script>
<script>

var configName;
var configVersion;
var configKey;

function onLoad() 
{
}
		
function onClickOpenAllNodes() 
{
  d.openAll();
}
		
function onClickCloseAllNodes() 
{
  d.closeAll();
}
		
function showSelectedConfig( name, version, id )
{
  configName = name;
  configVersion = version;
  configKey = id;
  document.getElementById( "selectedConfig" ).innerHTML = "<b>" + name + " V" + version + "</b>";
  document.getElementById("okButton").style.visibility = "visible";
  document.getElementById("config").src = "convert.jsp?configKey=" + id;
}

function onClickSend()
{
  window.close();
  var config = configName + " V" + configVersion;
  opener.alert( config );
  opener.document.getElementById("currentConfig").innerHTML = config;
}

</script>
</head>

<%!
    String prepareTree( AtomicInteger nodeCounter, int parentNode, 
                        confdb.data.Directory directory, confdb.converter.Converter converter )
    {
      String str = "";
      confdb.data.ConfigInfo[] configs = converter.listConfigs( directory );
      for ( int i = 0; i < configs.length; i++ )
      {
        confdb.data.ConfigVersion versionInfo = configs[i].version( 0 ); 
	String name = configs[i].name();
	str += "d.add(" + nodeCounter.incrementAndGet() + ", " + parentNode + ", '" + name 
	     + "','javascript:showSelectedConfig( \\'" + name + "\\', " 
	     + versionInfo.version() + ", " + versionInfo.dbId() 
             + ");', '', '', '../img/page.gif', '../img/page.gif'  );\n";
	int thisParent = nodeCounter.get();	 
	for ( int ii = 0; ii < configs[i].versionCount(); ii++ )
        {
	  versionInfo = configs[i].version( ii );
	  String vx = "V" + versionInfo.version() + "  -  " + versionInfo.created();
	  str += "d.add(" + nodeCounter.incrementAndGet() + ", " + thisParent + ", '" + vx 
	     + "','javascript:showSelectedConfig( \\'" + name + "\\', " 
	     + versionInfo.version() + ", " + versionInfo.dbId() + ");', '', '', '../img/spacer.gif' )\n";
        }
      }
      confdb.data.Directory[] list = converter.listSubDirectories( directory );
      for ( int i = 0; i < list.length; i++ )
      {
	str += "d.add(" + nodeCounter.incrementAndGet() + ", " + parentNode + ", '" + list[i].name() 
            + "','javascript:d.openTo(" + nodeCounter.get() +",false);');\n";
        str += prepareTree( nodeCounter, nodeCounter.get(), list[i], converter );
      }
      return str;
    }
%>
<body onload="onLoad()">
<table width="100%" border="1" cellpadding="0" cellspacing="0">
<tbody id="treeSection">
<tr>
<%
  String tree = "";
  try {
    confdb.converter.Converter converter = 
       confdb.converter.Converter.getConverter();
    converter.connectToDatabase();
    confdb.data.Directory root = converter.getRootDirectory();
    tree = "<script>\n" 
        + "d = new dTree('d');\n"
        + "d.add(0,-1,'HLT configurations','javascript: void(0);');\n"
        + prepareTree( new AtomicInteger(1), 0, root, converter )
        + "document.write(d);\n"
	+ "</script>\n";
    converter.disconnectFromDatabase();
  } catch (Exception e) {
    out.println( "<tr><td>" + e.toString() + "</td></tr>" );
  }
%>
<td class="label_center" >
 <input type="button" id="openAllNodes" value="Open All Nodes" name="Open All Nodes" onclick="onClickOpenAllNodes()">
 <input type="button" id="closeAllNodes" value="Close All Nodes" name="Close All Nodes" onclick="onClickCloseAllNodes()">
</td>
<td>selected: 
 <span id="selectedConfig"></span>
 <span id="okButton" style="text-align:right; margin-left:10px; visibility:hidden"><input type="button" value="OK" onclick="onClickSend()"></span>
</td>
</tr>
<tr>
<td valign="top">
<%= tree %>
</td>
<td>
<iframe id="config" width="100%" height="650" marginheight="10" marginwidth="10" frameborder="0">
</iframe>
</td>
</tr>
</tbody>
</table>
</body>

</html>


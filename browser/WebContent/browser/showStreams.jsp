<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.converter.ConverterBase"%>
<%@page import="confdb.data.ModifierInstructions"%>
<%@page import="confdb.converter.OnlineConverter"%>
<%@page import="confdb.data.IConfiguration"%>
<%@page import="confdb.converter.OfflineConverter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.Iterator"%>
<%@page import="confdb.data.Stream"%>
<%@page import="confdb.data.PrimaryDataset"%>
<%@page import="confdb.data.Path"%>
<%@page import="confdb.data.ModuleInstance"%>
<%@page import="java.util.ArrayList"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Streams </title>

<%
  String db = request.getParameter( "dbName" );
  String yui = "../js/yui";
  String css = "../css";
  String js = "../js";
  String img = "../img";
  boolean online = false;
  if ( request.getParameter( "online" ) != null )
  {
	online = true;
  	yui = "../../../gui/yui";
    css = "../../css";
    js = "../../js";
    img = "../../img";
  }
%>


<!-- css -->
<link rel="stylesheet" type="text/css" href="../css/jquery.treeTable.css" />

<!-- js -->
<script type="text/javascript" src="../js/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="../js/jquery.cookie.js"></script>
<script type="text/javascript" src="../js/jquery.treeTable.js"></script>
<script type="text/javascript" src="../js/json2.js"></script>

<!-- 
<style>


body {
	margin:0px; 
	padding:0px; 
	padding-top:5px;
	overflow:hidden;
}

#container {
  margin:0px; 
  padding:0px;
  width:100%;
}



</style>
 -->


<script type="text/javascript">

var COOKIE_NAME = 'createConfigParas';
var params = {};
var rows = 0;

function setCookie( cookie )
{
	var cookieStr = JSON.stringify( cookie );
	$.cookie( COOKIE_NAME, cookieStr );
//	alert( cookieStr );
}

function signalReady()
{
  if ( parent &&  parent.iframeReady )
    parent.iframeReady();
}




$(function(){

	var cookieStr = $.cookie( COOKIE_NAME );
	if ( cookieStr != null )
	{
		params = eval('(' + cookieStr + ')');
		//$("#doc").hide();
	}
	else
		params = {  dbhost: "cmsr1-v.cern.ch", 
		    		dbname: "cms_cond.cern.ch",  
		    		dbuser: "cms_hltdev_writer", 
		    		master: "/dev/CMSSW_3_4_0/pre6/HLT", 
		    		target: "/dev/CMSSW_3_4_0/pre6", 
		    		tables: [ { name: "1E31" }, { name: "8E29" } ] };

	/*
	$( "#dbhost" ).val( params.dbhost );
	$( "#dbname" ).val( params.dbname );
	$( "#dbuser" ).val( params.dbuser );
	$( "#user" ).val( params.user );
	$( "#master" ).val( params.master );
	$( "#target" ).val( params.target );
	
    $("#submitButton").click( doIt ); 

    for ( var i in params.tables )
    {
        $("#tables").append( '<tr><td><input type="text" size="20" value="' 
                + params.tables[i].name + '" name="table' + i + '"></td>'
                + '<td><input type="file" name="file' + i + '" size="50" maxlength="100000" accept="text/*"></td>'
                + '<td><input type="checkbox" name="checked' + i + '" checked value="on"></input></td></tr>' );
    }
	rows = params.tables;
	*/

    $("#main").treeTable( { clickableNodeNames: true } );
    signalReady();
});

</script>


</head>
<body>
<%!
private static final String l1TemplateName    = "HLTLevel1GTSeed";
private static final String l1CondParamName   = "L1SeedsLogicalExpression";

public String getL1Seed( Path path )
{
	ModuleInstance l1Module = null;
	ArrayList<ModuleInstance> filters = new ArrayList<ModuleInstance>();
	Iterator<ModuleInstance> itM = path.moduleIterator();
	while (itM.hasNext()) 
	{
    	ModuleInstance module = itM.next();
    	String templateName = module.template().name();
    	String templateType = module.template().type();
    	if ( templateName.equals( l1TemplateName ) )
    	{
    		l1Module = module;
    		break;
    	}
	}

	boolean	isNoTrigger = (l1Module==null);

	String l1Condition = isNoTrigger ?
    	"-" : l1Module.parameter(l1CondParamName,"string").valueAsString();
	if (l1Condition.startsWith("\""))
    	l1Condition = l1Condition.substring(1);
	if (l1Condition.endsWith("\""))
    	l1Condition = l1Condition.substring(0,l1Condition.length()-1);
	return l1Condition;
}

%>

<%
	BrowserConverter.UrlParameter paras = 
		BrowserConverter.getUrlParameter( request.getParameterMap() );

	paras.format = "summary.html";
	String result = "";
	ConverterBase converter = null;
	IConfiguration conf = null;	
	try {

		ModifierInstructions modifierInstructions = new ModifierInstructions();
	    //modifierInstructions.interpretArgs( paras.toModifier );
	    if ( paras.dbName.equals( "online" ) )
	    	converter = OnlineConverter.getConverter();
	    else
	        converter = BrowserConverter.getConverter( paras.dbName );
        if ( paras.configId == -1 )
			paras.configId = converter.getDatabase().getConfigId( paras.configName );

        conf = converter.getConfiguration( paras.configId );
        //OfflineConverter helper = new OfflineConverter( "HTML" );
	    //result = helper.getConfigString( conf, paras.format,
		//				      modifierInstructions,
		//				      paras.asFragment);
	} catch (Exception e) {
    	Throwable cause = e.getCause(); 
	    out.print(e.getMessage()+"\n"); 
	    if ( cause != null )
		    out.print( "cause: " + cause.getMessage() + "\n\n");
	    else
	    	out.print( "\n" );
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    PrintWriter writer = new PrintWriter(buffer);
	    e.printStackTrace(writer);
	    writer.close();
	    out.println(buffer.toString());
	    if ( converter != null )
	        BrowserConverter.deleteConverter( converter );
	    return;
	}
%>
<center>

<table id="main" rules="groups" border="1" style="padding-left:10px" width="100%">
<colgroup>
    <col width="15%">
    <col width="15%">
    <col width="30%">
    <col width="30%">
  </colgroup>
<thead>
<tr><th align='left'>Stream</th><th align='left'>Primary Dataset</th><th align='left'>HLT path</th><th align='left'>L1 seed</th></tr>
</thead>
<tbody>
<%
	Iterator<Stream> it = conf.streamIterator();
	while ( it.hasNext() )
	{
		Stream stream = it.next();
		out.println( "<tr id='s-" + stream.label() + "'><td style='padding-left:20px;'>" + stream.label() + "</td></tr>" );
		Iterator<PrimaryDataset> datasets = stream.datasetIterator();
		while ( datasets.hasNext() )
		{
			PrimaryDataset dataset = datasets.next();
			out.println( "<tr id='pd-" + dataset.label() + "' class='child-of-s-" + stream.label() + "'><td align='right'></td><td>" + dataset.label() + "</td></tr>" );
			Iterator<Path> paths = dataset.pathIterator();
			while ( paths.hasNext() )
			{
				Path path = paths.next();
				out.println( "<tr id='p-" + path.name() + "' class='child-of-pd-" + dataset.label() + "'>" 
						+ "<td></td><td></td>" 
						+ "<td>" + path.name() + "</td>" 
						+ "<td>" + getL1Seed(path) + "</td>" 
						+ "</tr>" );
			}
		}
	}
%>

</tbody>
</table>
</center>

</body>
</html>
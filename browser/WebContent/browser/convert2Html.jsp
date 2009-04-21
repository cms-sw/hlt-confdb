<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.data.IConfiguration"%>
<%@page import="confdb.converter.ConverterBase"%>
<%@page import="confdb.converter.OnlineConverter"%>
<%@page import="confdb.converter.ConverterException"%>
<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.db.ConfDBSetups"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config</title>

<%
  String db = request.getParameter( "dbName" );
  String yui = "../js/yui";
  String css = "../css";
//  String js = "../js";
//  String img = "../img";

  if ( request.getParameter( "online" ) != null )
  {
  	yui = "../../../gui/yui";
    css = "../../css";
//    js = "../../js";
//    img = "../../img";
  }
%>


<link rel="stylesheet" type="text/css" href="<%=yui%>/reset-fonts/reset-fonts.css" />
<link rel="stylesheet" type="text/css" href="<%=yui%>/container/assets/skins/sam/container.css" />
<link rel="stylesheet" type="text/css" href="<%=css%>/confdb.css" />

<script type="text/javascript" src="<%=yui%>/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="<%=yui%>/container/container-min.js"></script>


<script type="text/javascript">

YAHOO.util.Event.addListener(window, "load", init);

var dialog,
    moduleName,
    configRelease,
    configPackage,
    configSubsystem,
    Dom = YAHOO.util.Dom,
    lxrURL = 'http://cmslxr.fnal.gov/lxr/ident',
    cvsURL = 'http://cmssw.cvs.cern.ch/cgi-bin/cmssw.cgi/CMSSW/';

function init()
{
  var displayHeight = Dom.getViewportHeight();
  Dom.setStyle(  'headerDiv', 'height',  '30px' );
  Dom.setStyle(  'mainDiv', 'max-height',  (displayHeight - 35) + 'px' );


  var handleCancel = function() {
		this.cancel();
		dialog.hide();
  };

  for ( var i = 0; i < 10; i++ )
  {
    var option = new Option( i, i, false, false );
    document.dialogForm.d1.options[i] = option;
    var option = new Option( i, i, false, false );
    document.dialogForm.d2.options[i] = option;
    var option = new Option( i, i, false, false );
    document.dialogForm.d3.options[i] = option;
  }

  dialog = new YAHOO.widget.Dialog( "dialog1",  
            { width: "500px", 
              fixedcenter: true, 
              draggable: false, 
//              zindex:4,
              modal: true,
              visible: false,
              buttons : [ { text:"Submit", handler:handleSubmit, isDefault:true },
								      { text:"Cancel", handler:handleCancel } ]
            } 
  );
  dialog.render(document.body);
}

function signalReady()
{
  if ( parent &&  parent.iframeReady )
    parent.iframeReady();
}



function showSource( release, type, cmspackage, subsystem )
{
  moduleName = type;
  configRelease = release;
  configPackage = cmspackage;
  configSubsystem = subsystem;
  if ( release.match( /^CMSSW_\d+_\d+_\d+$/ ) )
  {
    var w = window.open( lxrURL + "?v=" + release + ";i=" + type,  "_blank" );
    w.focus();
    return;
  }
  
  if ( parent && parent.releaseMap  &&  parent.releaseMap[ release ] )
  {
  	release = parent.releaseMap[ release ];
    var w = window.open( getURL( release ),  "_blank" );
    w.focus();
    return;
  }

  var dx = release.match( /^CMSSW_\d+_/ );
  if ( dx )
  {
    var str = new String( dx );
    document.dialogForm.d1.selectedIndex = Number( str.substring( 6, str.length - 1 ) );
    dx = release.match( /^CMSSW_\d+_\d+_/ );
    if ( dx )
    {
      str = new String( dx );
      document.dialogForm.d2.selectedIndex = 
      	Number( str.substring( str.indexOf( '_', 6) + 1, str.length - 1 ) );
      dx = release.match( /^CMSSW_\d+_\d+_\d+/ );
      if ( dx )
      {
        str = new String( dx );
        document.dialogForm.d3.selectedIndex = 
      	  Number( str.substring( str.lastIndexOf( '_' ) + 1, str.length ) );
      }
    }
  }
      
  document.dialogForm.lxrCheckbox[0].checked = true;
  
  dialog.setHeader( "Release " + release + " not available in LXR!" );
  dialog.show();
}

function handleSubmit() 
{
  	var data = this.getData();
	this.cancel();
	dialog.hide();
	
	var release = 'CMSSW_' + data.d1 + '_' + data.d2 + '_' + data.d3;
	if ( data.lxrCheckbox != 'lxr' )
	  release = data.lxrCheckbox;
	
	if ( parent )
	{
	  if ( !parent.releaseMap ) 
	  	parent.releaseMap = [];
	  parent.releaseMap[ configRelease ] = release;
	}

    var w = window.open( getURL( release ),  "_blank" );
    w.focus();
}


function getURL( release )
{
  var url = lxrURL + "?v=" + release + ";i=" + moduleName;
  if ( release.match( /^cvs/ ) )
  {
    url = cvsURL + configSubsystem + '/' + configPackage;
	if ( release == 'cvs-h' )
      url += '/interface/' + moduleName + '.h?view=markup&pathrev=' + configRelease;
	else if ( release == 'cvs' )
      url += '?pathrev=' + configRelease;
    else
    {
      if ( configPackage == 'HLTcore' )
        url += '/plugins/' + moduleName + '.cc?view=markup&pathrev=' + configRelease;
      else
        url += '/src/' + moduleName + '.cc?view=markup&pathrev=' + configRelease;
    }
  }
  return url;
}

</script>


</head>

<body class="yui-skin-sam skin1" style="background:white" onload="signalReady()">
<div id="headerDiv" class="tab1" style="padding-left:10px; padding-top:5px;">
<%
  String confString = "";
  try {
	String index = request.getParameter( "dbIndex" );
	ConverterBase converter = null;
	if ( index != null )
		converter = BrowserConverter.getConverter( Integer.parseInt( index ) );
	else
	{
		String dbName = request.getParameter( "dbName" );
		if ( dbName == null ) 
		{
			out.print( "ERROR!\ndbIndex or dbName must be specified!");
			return;
		}
		if ( dbName.equals( "online" ) )
			converter = OnlineConverter.getConverter();
		else	
		{
			if ( dbName.equalsIgnoreCase( "hltdev" ) )
				dbName = "HLT Development";
			ConfDBSetups dbs = new ConfDBSetups();
		  	String[] labels = dbs.labelsAsArray();
	  		for ( int i = 0; i < dbs.setupCount(); i++ )
	  		{
	  			if ( dbName.equalsIgnoreCase( labels[i] ) )
	  			{
	  				index = "" + i;
	  				break;
	  			}
	  		}
	  		if ( index == null  )
	  		{
	  			out.print( "ERROR!\ninvalid dbName!");
	  			return;
	  		}
	  		int dbIndex = Integer.parseInt( index );
	  		converter = BrowserConverter.getConverter( dbIndex );
	  	}
	}

	String configName = request.getParameter( "configName" );
	String configId = request.getParameter( "configKey" );
	if ( configId == null  &&  configName == null )
	{
		out.print( "ERROR!\nconfigKey or configName must be specified!");
		return;
	}

	int configKey = ( configId != null ) ?
    	Integer.parseInt(configId) : converter.getDatabase().getConfigId(configName);

	IConfiguration conf = converter.getConfiguration( configKey );

	if ( conf == null )
		out.print( "ERROR!\nconfig " + configKey + " not found!" );
	else
	{
		String refs = " ";
		if ( conf.pathCount() > 0 )
			refs += "<a href=\"#paths\">paths</a> ";
		if ( conf.sequenceCount() > 0 )
			refs += "<a href=\"#sequences\">sequences</a> ";
		if ( conf.moduleCount() > 0 )
			refs += "<a href=\"#modules\">modules</a> ";
		if ( conf.edsourceCount() > 0 )
			refs += "<a href=\"#ed_sources\">ed_sources</a> ";
		if ( conf.essourceCount() > 0 )
			refs += "<a href=\"#es_sources\">es_sources</a> ";
		if ( conf.esmoduleCount() > 0 )
			refs += "<a href=\"#es_modules\">es_modules</a> ";
		if ( conf.serviceCount() > 0 )
			refs += "<a href=\"#services\">services</a> ";
		out.println( refs );
		try {
//			if ( converter instanceof OnlineConverter )
//				confString = ((OnlineConverter)converter).getEpConfigString( configKey );
//			else
				confString = converter.getConverterEngine().convert( conf );
		} catch ( ConverterException e1 ) {
			System.out.println( "reloading config " + configKey );
			if ( converter instanceof OnlineConverter )
				confString = ((OnlineConverter)converter).getEpConfigString( configKey );
			else
				confString = converter.getConverterEngine().convert( converter.getConfiguration( configKey ) );
		}
	}
  } catch ( Exception e ) {
	  out.print( "ERROR!\n\n" );
	  out.print( e.toString() + "\n\n" );
	  if ( request.getParameter( "stacktrace" ) != null )
	  {  
	  	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	  	PrintWriter writer = new PrintWriter( buffer );
	  	e.printStackTrace( writer );
	  	writer.close();
		out.println( buffer.toString() );
	  }
  }
%>
</div>
<div id="mainDiv" style="overflow:auto; max-height:600px;">
<pre style="line-height:140%">

<%=confString%>
</pre>
</div>
<div id="dialog1">
 <div class="hd">Release not in LXR!</div>
 <div class="bd">
  <form name="dialogForm">
   <table>
    <tr>
     <td>use</td><td><b>CMSSW_</b></td>
     <td><select name="d1"></select></td>
     <td><b>_</b></td>
     <td><select name="d2"></select></td>
     <td><b>_</b></td>
     <td><select name="d3"></select></td>
     <td></td><td><input type="radio" name="lxrCheckbox" value="lxr"></td>
    </tr>
    <tr><td>or</td></tr>
    <tr><td colspan="7">use CVS browser and go to </td></tr>
    <tr><td></td><td colspan="6">.cc</td><td></td><td><input type="radio" name="lxrCheckbox" value="cvs-cc"></td>
    <tr><td></td><td colspan="6">.h</td><td></td><td><input type="radio" name="lxrCheckbox" value="cvs-h"></td>
    <tr><td></td><td colspan="6">package</td><td></td><td><input type="radio" name="lxrCheckbox" value="cvs"></td>
    </tr>
   </table>
   <div class="clear"></div>
  </form>
 </div>
</div>

</body>
</html>

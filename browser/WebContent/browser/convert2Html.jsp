<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.lang.reflect.Modifier"%>
<%@page import="java.lang.reflect.InvocationTargetException"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HLT config</title>

<%
  String yui = "../js/yui";
  String css = "../css";
  if ( request.getParameter( "online" ) != null )
  {
  	yui = "../../../gui/yui";
    css = "../../css";
  }

  out.println( "<script type='text/javascript'>" );
  out.println( "var scrollDiv = " + ( request.getParameter( "scrollDiv" ) != null ? request.getParameter( "scrollDiv" ) : "true" ) + ";" ); 
  out.println( "</script>" );
%>  

<link rel="stylesheet" type="text/css" href="<%=yui%>/reset-fonts/reset-fonts.css" />
<link rel="stylesheet" type="text/css" href="<%=yui%>/container/assets/skins/sam/container.css" />
<link rel="stylesheet" type="text/css" href="<%=css%>/confdb.css" />
<link rel="stylesheet" type="text/css" href="<%=css%>/confdb-jq.css" />

<script type="text/javascript" src="<%=yui%>/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="<%=yui%>/container/container-min.js"></script>


<style type="text/css">

hr {
	color: lightgrey;
	background-color: white;
	height: 3px;
	margin: 2em;
	border: 1px solid lightgrey;
}

</style>
  
<%!

class MethodParams {
	public Class<?>[] paramClasses;
    public Object[] params;
}

private MethodParams getMethodParams( String[] paramList )
{
	MethodParams methodParams = new MethodParams();
	methodParams.paramClasses = new Class[ paramList.length ];
	methodParams.params = new Object[ paramList.length ];
    for ( int i = 0; i < methodParams.params.length; i++ )
    {
    	String param = paramList[ i ];
    	String paramClass = param;
    	int split = param.indexOf( ':' );
    	if ( split != -1 )
    		paramClass = param.substring( 0, split );
    	else
			methodParams.params[i] = null;
		if ( paramClass.equals( "int" ) )
		{
			methodParams.paramClasses[i] = Integer.TYPE;
			if ( split != -1 )
				methodParams.params[i] = new Integer( param.substring( split + 1 ) );
		}
		else if ( paramClass.equals( "string" ) )
		{
			methodParams.paramClasses[i] = String.class;
			if ( split != -1 )
				methodParams.params[i] = param.substring( split + 1 );
		}
   	}
    return methodParams;
}

private Object exec( Object object, String methodName, String[] paramList ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
{
	MethodParams methodParams = getMethodParams( paramList );
    Method method = object.getClass().getMethod( methodName, methodParams.paramClasses );
    Object result = null;
    if ( Modifier.isStatic( method.getModifiers() ) )
    	result = method.invoke( null, methodParams.params );
    else
    	result = method.invoke( object, methodParams.params );
    return result;
}
    
private Object execStatic( String className, String methodName, String[] paramList ) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
{
	Class<?> execClass = Class.forName( className );
	MethodParams methodParams = getMethodParams( paramList );
    Method method = execClass.getMethod( methodName, methodParams.paramClasses );
    return method.invoke( null, methodParams.params );
}
    
private Object getObject( String className, HttpSession session ) throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	Class<?> jsonClass = Class.forName( className );
	Object jsonObject = this;
	if ( session == null )
		jsonObject = jsonClass.newInstance();
	else
	{
		jsonObject = session.getAttribute( jsonClass.getCanonicalName() );
		if ( jsonObject != null  &&  jsonObject.getClass() != this.getClass() )
			jsonObject = null;
		if ( jsonObject == null )
		{
			jsonObject = jsonClass.newInstance();
			session.setAttribute( jsonClass.getCanonicalName(), jsonObject );
			System.out.println( "new object for session " + session.getId() );
		}
	}
	return jsonObject;
}

  
private Object exec( String className, String methodName, String[] paramList, HttpSession session ) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException 
{
	Object jsonObject = getObject( className, session );
	return exec( jsonObject, methodName, paramList );
}

  
%>

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
  if ( scrollDiv )
  {
	Dom.setStyle(  'mainDiv', 'max-height',  (displayHeight - 35) + 'px' );
	Dom.setStyle(  'mainDiv', 'overflow',  'auto' );
	Dom.setStyle(  'headerDiv', 'height',  '30px' );
  }
  else
	Dom.setStyle(  'headerDiv', 'visibility',  'collapse' );


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

<body class="yui-skin-sam skin1" onload="signalReady()">
<div id="headerDiv" class="tab1" style="padding-left:10px; padding-top:5px;">
<%
  String confString = "";
  try {
	String dbIndex = request.getParameter( "dbIndex" );
	String dbName = request.getParameter( "dbName" );
	String configName = request.getParameter( "configName" );
	String configId = request.getParameter( "configKey" );
	String[] params = new String[4];
	params[0] = dbIndex != null ? ("string:" + dbIndex) : "string";
	params[1] = dbName != null ? ("string:" + dbName) : "string";
	params[2] = configName != null ? ("string:" + configName) : "string";
	params[3] = configId != null ? ("string:" + configId) : "string";
	Object proxy = getObject( "confdb.converter.AjaxJsp", session );
	Object result = exec( proxy, "loadConfig", params );
	if ( result != null && result instanceof String )
		out.print( result );
	else
	{
		result = exec( proxy, "getHRefs", new String[0] );
		if ( result != null && result instanceof String )
			out.println( result );
//		try {
//			if ( converter instanceof OnlineConverter )
//				confString = ((OnlineConverter)converter).getEpConfigString( configKey );
//			else
		result = exec( proxy, "getConfString", new String[0] );
		if ( result != null && result instanceof String )
				confString = (String)result;
//		} catch ( ConverterException e1 ) {
//			System.out.println( "reloading config " + configKey );
//			if ( converter instanceof OnlineConverter )
//				confString = ((OnlineConverter)converter).getEpConfigString( configKey );
//			else
//				confString = converter.getConverterEngine().convert( converter.getConfiguration( configKey ) );
//		}
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
<div id="mainDiv">
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

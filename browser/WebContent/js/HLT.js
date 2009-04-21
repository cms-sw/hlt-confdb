
if ( HLTjs == null ) 
  var HLTjs = {};
if (HLTjs.json == null) 
  HLTjs.json = {};

HLTjs.json.failureHandler = function( o ) 
{
	if ( !YAHOO.util.Connect.isCallInProgress(o) )
	{
	  var failure = new Object();
	  if ( o.status == -1 )
	    failure.ajaxFailure = 'timeout'; 
	  else if ( o.status == 0 )
	    failure.ajaxFailure = o.statusText;
	  else
	  {
	    var message = "HTTP error " + o.status;
	    if ( o.status == 404 ) 
	      message += ": " + o.statusText + " is not available";
	    failure.ajaxFailure = message;
	  }
	  (o.argument)( failure );
    }
}

HLTjs.json.jsonFailureHandler = function( o ) 
{
	alert("AJAX-JSON failure: " + o.responseText );
}

HLTjs.json.successHandler = function( o ) 
{
  // Process the JSON data returned from the server
  try {
    var data = YAHOO.lang.JSON.parse(o.responseText);
  }
  catch (x) {
  	alert( x );
  	HLTjs.json.jsonFailureHandler( o );
    return;
  }
  (o.argument)( data );
}
  


HLTjs.json._execute = function(path, className, method, vararg_params ) 
{
  var callbackArg = arguments[arguments.length - 1];
  var postData = "method=" + method;
  if ( className != null )
	postData += "&class=" + className ;
  for ( var i = 0; i < arguments.length - 4; i++ ) 
    postData += "&p" + (i + 1) + "=" + arguments[i + 3];
  
  var callbacks = {
  	  success : HLTjs.json.successHandler,
  	  failure : HLTjs.json.failureHandler,
	  timeout : 20000,
	  argument : callbackArg 
  }
  YAHOO.util.Connect.asyncRequest( 'POST', path, callbacks, postData);
}

HLTjs.setValue = function( ele, val ) 
{
  if ( val == null ) 
  	val = "";
  if ( typeof ele == "string" ) 
    ele = YAHOO.util.Dom.get( ele );
  if ( ele == null ) 
    return;

  ele.innerHTML = val;
}

if (HLTjs == null) var HLTjs = {};
if (HLTjs.json == null) HLTjs.json = {};

if (AjaxInfo == null) var AjaxInfo = {};
AjaxInfo._path = '../json/AjaxInfo.jsp';

AjaxInfo.listDBs = function(callback) {
  HLTjs.json._execute(AjaxInfo._path, 'confdb.converter.BrowserConverter', 'listDBs', callback);
}
AjaxInfo.getAnchors = function(p0, p1, callback) {
  HLTjs.json._execute(AjaxInfo._path, 'confdb.converter.BrowserConverter', 'getAnchors', 'int:' + p0, 'int:' + p1, callback);
}
AjaxInfo.getRcmsDbInfo = function(callback) {
  HLTjs.json._execute(AjaxInfo._path, null, 'getRcmsDbInfo', callback);
}
AjaxInfo.clearCache = function(callback) {
  HLTjs.json._execute(AjaxInfo._path, 'confdb.converter.ConfCache', 'clearCache', callback);
}
AjaxInfo.getMemInfo = function(callback) {
  HLTjs.json._execute(AjaxInfo._path, null, 'getMemInfo', callback);
}
AjaxInfo.getTree = function( p0, p1, callback) {
  HLTjs.json._execute(AjaxInfo._path, null, 'getTree', 'string:' + p0, 'string:' + p1, callback);
}

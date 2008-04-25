
if (dwr == null) 
  var dwr = {};
if (dwr.engine == null) 
  dwr.engine = {};
if (DWREngine == null) 
  var DWREngine = dwr.engine;

if ( getJsonPath == null )
  var getJsonPath = "../json/";

dwr.engine.failureHandler = function( o ) 
{
	if ( !YAHOO.util.Connect.isCallInProgress(o) )
	{
		if ( o.status == -1 )
	    	alert("AJAX timeout!");
	    else if ( o.status == 0 )
	    	alert("AJAX failure: " + o.statusText );
	    else
	    {
	    	var message = "AJAX failure!\nHTTP error " + o.status;
	    	if ( o.status == 404 ) 
	    		message += ": " + o.statusText + " is not available";
	    	alert( message );
	    }
    }
}

dwr.engine.jsonFailureHandler = function( o ) 
{
	alert("AJAX-JSON failure: " + o.responseText );
}

dwr.engine.successHandler = function( o ) 
{
  // Process the JSON data returned from the server
  try {
    var data = YAHOO.lang.JSON.parse(o.responseText);
  }
  catch (x) {
  	alert( x );
  	dwr.engine.jsonFailureHandler( o );
    return;
  }
  (o.argument)( data );
}
  


dwr.engine._execute = function(path, className, method, vararg_params ) 
{
  var callbackArg = arguments[arguments.length - 1];
  var postData = "class=" + className + "&method=" + method;
  for ( var i = 0; i < arguments.length - 4; i++ ) 
  {
    postData += "&p" + (i + 1) + "=" + arguments[i + 3];
  }
  
  var callbacks = {
  	  success : dwr.engine.successHandler,
  	  failure : dwr.engine.failureHandler,
	  timeout : 10000,
	  argument : callbackArg 
  }
  YAHOO.util.Connect.asyncRequest( 'POST', getJsonPath + "getJSON.jsp", callbacks, postData);
}




if ( HLTjs == null ) 
  var HLTjs = {};
if (HLTjs.json == null) 
  HLTjs.json = {};

HLTjs.json.failureHandler = function( o ) 
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



<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.data.ModifierInstructions"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="application/octet-stream" %>
<%
	out.clearBuffer();
	Map<String,String[]> map = request.getParameterMap();
	if ( map.isEmpty())
	{
		out.println("ERROR: configId or configName must be specified!");
		return;
	}
		
	Set<Map.Entry<String,String[]>> parameters = map.entrySet(); 

	boolean asFragment = false;
	String format = "ascii";
	String configId = null;
	String configName = null;
	String dbIndex = "1";
	HashMap<String,String> toModifier = new HashMap<String,String>();

	for ( Map.Entry<String,String[]> entry : parameters )
	{
		if ( entry.getValue().length > 1 )
		{
			out.println( "ERROR: Only one parameter '" + entry.getKey() + "' allowed!" );
			return;
		}
		
		String value = entry.getValue()[ 0 ];
		String key = entry.getKey();
		if ( key.equals( "configId" ) )
			configId = value;
		else if ( key.equals( "dbIndex" ) )
			dbIndex = value;
		else if ( key.equals( "configName" ) )
			configName = value;
		else if ( key.equals( "cff" ) )
			asFragment =true;
		else if ( key.equals( "format" ) )
			format = value;
		else
			toModifier.put( entry.getKey(), value );
	}
	
	if ( configId == null  &&  configName == null )
	{
		out.println("ERROR: configId or configName must be specified!");
		return;
	}

	if ( configId != null  &&  configName != null )
	{
		out.println("ERROR: configId *OR* configName must be specified!");
		return;
	}

	BrowserConverter converter = null;
	try {
		//ModifierInstructions modifierInstructions = new ModifierInstructions();
		//modifierInstructions.interpretArgs( toModifier );

		converter = BrowserConverter.getConverter( Integer.parseInt(dbIndex) );
	    int id = ( configId != null ) ?
	    		Integer.parseInt(configId) : converter.getDatabase().getConfigId(configName);
	    if ( id <= 0 ) 
	    {
	    	out.println( "ERROR: configuration not found!" );
	    	return;
	    }

		String result = converter.getConfigString( id, format,
												   null, asFragment );
		out.print( result );
	  } catch ( Exception e ) {
		  out.print( "ERROR!\n\n" ); 
		  ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		  PrintWriter writer = new PrintWriter( buffer );
		  e.printStackTrace( writer );
		  writer.close();
		  out.println( buffer.toString() );
		  if ( converter != null )
			  BrowserConverter.deleteConverter( converter );
	  }

		

%>



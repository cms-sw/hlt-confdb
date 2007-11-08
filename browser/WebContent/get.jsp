<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="browser.BrowserConverter"%>
<%@page import="confdb.data.ModifierInstructions"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/plain"%>
<%
	out.clearBuffer();
	try {
		Map<String,String[]> map = request.getParameterMap();
		Set<Map.Entry<String,String[]>> parameters = map.entrySet(); 

		HashMap<String,String> toModifier = new HashMap<String,String>();
	
		String configId = "0";
		boolean asFragment = false;
		String format = "ascii";
		for ( Map.Entry<String,String[]> entry : parameters )
		{
			String value = "";
			for ( String para : entry.getValue() )
				value += (value.length() == 0) ? para : ("," + para);
			if ( entry.getKey().equals( "configId" ) )
				configId = value;
			else if ( entry.getKey().equals( "cff" ) )
				asFragment =true;
			else if ( entry.getKey().equals( "format" ) )
				format = value;
			else
				toModifier.put( entry.getKey(), value );
		}
		ModifierInstructions modifierInstructions = new ModifierInstructions();
		modifierInstructions.interpretArgs( toModifier );
		BrowserConverter converter = BrowserConverter.getConverter( 1 );
		String result = converter.getConfigString( Integer.parseInt(configId),
													format,
													modifierInstructions,
													asFragment );
		out.print( result );
	  } catch ( Exception e ) {
		  out.print( "ERROR!\n\n" ); 
		  ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		  PrintWriter writer = new PrintWriter( buffer );
		  e.printStackTrace( writer );
		  writer.close();
		  out.println( buffer.toString() );
	  }

		

%>



<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.data.ModifierInstructions"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/plain"%>
<%
    out.clearBuffer();
	BrowserConverter converter = null;
	try {
		BrowserConverter.UrlParameter paras = BrowserConverter.getUrlParameter( request.getParameterMap() );

	    ModifierInstructions modifierInstructions = new ModifierInstructions();
	    modifierInstructions.interpretArgs( paras.toModifier );
	    
	    if ( paras.runNumber != -1 )
	    	paras.dbName = "ORCOFF";
	    
	   	converter = BrowserConverter.getConverter( paras.dbName );
	    if ( paras.runNumber != -1 )
	    {
	    	paras.configName = null;
	    	paras.configId = converter.getKeyFromRunSummary( paras.runNumber );
	    	if ( paras.configId <= 0 )
            {
            	out.println( "ERROR: CONFIG_NOT_FOUND" );
                return;
            }
	    }

	    if ( paras.configName != null )
	    	paras.configId = converter.getDatabase().getConfigId( paras.configName );

	    String result = converter.getConfigString( paras.configId, paras.format, modifierInstructions, paras.asFragment);
	    out.print(result);
	    out.close();
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
	    if (converter!=null)
	        BrowserConverter.deleteConverter( converter );
	}
%>



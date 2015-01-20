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
        BrowserConverter.UrlParameter params = BrowserConverter.getUrlParameter( request.getParameterMap() );

        ModifierInstructions modifierInstructions = new ModifierInstructions();
        modifierInstructions.interpretArgs( params.toModifier );

        if (params.runNumber != -1)
            params.dbName = "ORCOFF";

        converter = BrowserConverter.getConverter( params.dbName );

        if (params.runNumber != -1) {
            params.configName = null;
            params.configId = converter.getKeyFromRunSummary( params.runNumber );
            if (params.configId <= 0) {
                out.println( "ERROR: CONFIG_NOT_FOUND" );
                return;
            }
        }

        if (params.configName != null)
            params.configId = converter.getDatabase().getConfigId( params.configName );

        String result = converter.getConfigString( params.configId, params.format, modifierInstructions, params.asFragment);
        out.print(result);

    } catch (Exception e) {
        // print the error message
        out.print(e.getMessage()+"\n");
        // in the case of a chained exception, print also the underlying cause's error message
        Throwable cause = e.getCause();
        if (cause != null)
            out.print("caused by: " + cause.getMessage() + "\n");
        out.print("\n");
        // print the stack trace
        e.printStackTrace(new PrintWriter(out, true));

        if (converter != null)
            BrowserConverter.deleteConverter( converter );
    }
%>



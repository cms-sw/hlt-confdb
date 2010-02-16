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
	Map<String,String[]> map = request.getParameterMap();
	if ( map.isEmpty())
	{
		out.println("ERROR: configId or configName must be specified!");
		return;
	}
		
	Set<Map.Entry<String,String[]>> parameters = map.entrySet(); 

	boolean asFragment = false;
  	String format = "python";
	String configId = null;
	String configName = null;
	String dbName = null;
	String dbIndexStr = "1";
	String runNumber = null;
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
		if (key.equals("configId")) {
		    configId = value;
		}
		else if (key.equals( "configName")) {  
		    configName = value;
		}
		else if (key.equals( "cff")) {
		    asFragment =true;
		    toModifier.put( key, value );
		}
		else if (key.equals( "format")) {
		    format = value;
		}
		else if ( key.equals( "dbName" ) )
			dbName = value;
		else if ( key.equals( "dbIndex" ) )
			dbIndexStr = value;
		else if ( key.equalsIgnoreCase( "runNumber" ) )
			runNumber = value;
		else 
		    toModifier.put(entry.getKey(),value);
	}

	if ( configId == null  &&  configName == null  && runNumber == null )
	{
		out.println("ERROR: configId or configName or runNumber must be specified!");
		return;
	}

	int moreThanOne = ( configId != null ? 1 : 0 ) 
		+  ( configName != null ? 1 : 0 )
		+  ( runNumber != null ? 1 : 0 );
	if ( moreThanOne > 1 ) 
	{
		out.println("ERROR: configId *OR* configName *OR* runNumber must be specified!");
		return;
	}

	BrowserConverter converter = null;
	try {
		if ( runNumber != null )
			dbName = "ORCOFF";
		if ( dbName != null )
		{
	        // TMP HACK
	        if ( dbName.equalsIgnoreCase("test") ) 
	        	dbName = "test2r";
		
			if ( !dbName.equalsIgnoreCase( "hltdev" ) )
			{
		  		ConfDBSetups dbs = new ConfDBSetups();
	  			String[] labels = dbs.labelsAsArray();
  				for ( int i = 0; i < dbs.setupCount(); i++ )
  				{
  					if ( dbName.equalsIgnoreCase( labels[i] ) )
  					{
  						dbIndexStr = "" + i;
  						break;
  					}
  				}
  			}
  		}

	    int dbIndex = Integer.parseInt( dbIndexStr );
	    ModifierInstructions modifierInstructions = new ModifierInstructions();
	    modifierInstructions.interpretArgs( toModifier );
        converter = BrowserConverter.getConverter( dbIndex );
        int id = -1;
        if ( runNumber != null )
        	id = converter.getKeyFromRunSummary( Integer.parseInt( runNumber ) );
        else        
    	    id = ( configId != null ) ?
    		    	Integer.parseInt(configId) :
    			    converter.getDatabase().getConfigId(configName);

    	String result = "";
    	if ( id <= 0 )
    		result = "ERROR: CONFIG_NOT_FOUND";
    	else
    		result = converter.getConfigString(id,format,
						      modifierInstructions,
						      asFragment);
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



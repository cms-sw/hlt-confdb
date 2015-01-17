<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.db.ConfDB"%>
<%@page import="confdb.data.SoftwareRelease"%>
<%@page import="confdb.data.Configuration"%>
<%@page import="confdb.data.ConfigInfo"%>
<%@page import="confdb.data.ConfigVersion"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page contentType="text/html"%>
<%
        out.clearBuffer();
	BrowserConverter converter = null;
	Map<String,String[]> map = request.getParameterMap();
	if ( map.isEmpty())
	{
		out.println("ERROR: don't know what to do!");
		out.println("parameters: list" );
		out.println("            select" );

		return;
	}
		

	Set<Map.Entry<String,String[]>> parameters = map.entrySet(); 
	
	String  dbIndexStr     =    "1";
	boolean dolist         = true;
	boolean doselect     = false;
	
	for ( Map.Entry<String,String[]> entry : parameters )
	{
		if ( entry.getValue().length > 1 )
		{
			out.println( "ERROR: Only one parameter '" + entry.getKey() + "' allowed!" );
			return;
		}
		
		String value = entry.getValue()[0];
		if ( entry.getKey().equals( "dbName" ) )
		{
			if ( !value.equalsIgnoreCase( "hltdev" ) )
			{
			  	ConfDBSetups dbs = new ConfDBSetups();
		  		String[] labels = dbs.labelsAsArray();
	  			for ( int i = 0; i < dbs.setupCount(); i++ )
	  			{
	  				if ( value.equalsIgnoreCase( labels[i] ) )
	  				{
	  					dbIndexStr = "" + i;
	  					break;
	  				}
	  			}
	  		}
	  	}
		else if ( entry.getKey().equals( "dbIndex" ) )
			dbIndexStr = value;
		else if ( entry.getKey().equals( "list" ) ) {
		    dolist = true;
		}				
		else if ( entry.getKey().equals( "select" ) ){
		    dolist = false;
		    doselect = true;
                }
		else
		{
		    out.println("ERROR: invalid option " +
				entry.getKey());
			return;
		}
	}

	
    try {
	int dbIndex = Integer.parseInt( dbIndexStr );
	converter = BrowserConverter.getConverter( dbIndex );
	ConfDB database = converter.getDatabase();
	
	if (dolist) {
	    String[] allConfigs = database.getSwArchNames();
	    for (String s : allConfigs)
		 out.println(s);
	    return;
	}
	
	
	if (doselect) {
	    String[] allConfigs = database.getSwArchNames();
            out.println("Select SW_ARCH:<select id=chosenSwArch>");
	    for (String s : allConfigs)
		 out.println("<option>"+s+"</option>");
            out.println("</select>");
	    return;
	}
    }
    catch ( Exception e ) {
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


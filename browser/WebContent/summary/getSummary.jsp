<%@page import="confdb.data.Path"%>
<%@page import="java.util.Iterator"%>
<%@page import="confdb.data.IConfiguration"%>
<%@page import="confdb.data.ModifierInstructions"%>
<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/plain"%>
<%!
	static int seedCounter = 1;
	static int filterCounter = 1;

	public class Summary
	{
		ArrayList<Row> list = new ArrayList<Row>();
		
		public void addRow( String name )
		{
			list.add( new Row( name ) );
		}
		
		public Row[] getRows()
		{
			return list.toArray( new Row[0] );
		}
		
		public class Row
		{
			String seed = "";
			String trigger;
			ArrayList<Filter> filters = new ArrayList<Filter>();

			Row( String trigger )
			{
				this.trigger = trigger;
				seed = "L1_" + seedCounter++;
				filters.add( new Filter() );
				filters.add( new Filter() );
			}
			
			public String getTrigger()
			{
				return trigger;
			}

			public String getL1Seed()
			{
				return seed;
			}

			public int getPrescale()
			{
				return 1;
			}
			
			public Filter[] getFilters()
			{
				return filters.toArray( new Filter[0] );
			}
			
			
			public class Filter 
			{
				String name = "";
					
				Filter()
				{
					name = "muX" + filterCounter++;
				}
				
				public String getName()
				{
					return name;
				}
				
				public double getPTmin()
				{
					return 5 + Math.rint( Math.random() * 20 );
				}
				
				public double getEtaMax()
				{
					return 0.5 + Math.rint( Math.random() * 5 );
				}
			}
		}
	}
%>
	
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
	String dbIndexStr = "1";
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
		else if ( key.equals( "dbIndex" ) )
			dbIndexStr = value;
		else {
		    toModifier.put(entry.getKey(),value);
		}
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
	    int dbIndex = Integer.parseInt( dbIndexStr );
	    ModifierInstructions modifierInstructions = new ModifierInstructions();
	    modifierInstructions.interpretArgs( toModifier );
        converter = BrowserConverter.getConverter( dbIndex );
    	int id = ( configId != null ) ?
    		    	Integer.parseInt(configId) :
    			    converter.getDatabase().getConfigId(configName);
		IConfiguration conf = converter.getConfiguration( id );
		Summary summary = new Summary();
		Iterator<Path> iterator = conf.pathIterator();
		while ( iterator.hasNext() )
		{
			summary.addRow( iterator.next().name() );
		}
		out.println( new JSONSerializer().include( "rows.filters" ).exclude( "*.class" ).serialize( summary ) );

		/*
	    String result = converter.getConfigString(id,format,
						      modifierInstructions,
						      asFragment);
	    out.print(result);
	    */
	}
        catch (Exception e) {
	    out.print(e.getMessage()+"\n\n"); 
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    PrintWriter writer = new PrintWriter(buffer);
	    e.printStackTrace(writer);
	    writer.close();
	    out.println(buffer.toString());
	    if (converter!=null)
	        BrowserConverter.deleteConverter( converter );
	}
%>



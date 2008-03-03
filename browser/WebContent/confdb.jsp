<%@page import="browser.BrowserConverter"%>
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
<%@page contentType="text/plain"%>
<%
    out.clearBuffer();
	BrowserConverter converter = null;
	Map<String,String[]> map = request.getParameterMap();
	if ( map.isEmpty())
	{
		out.println("ERROR: don't know what to do!");
		out.println("parameters: list[=configNameStartsWith]" );
		out.println("            config[ID][Name]=xxx&versions" );
		out.println("                                &packages" );

		return;
	}
		

	Set<Map.Entry<String,String[]>> parameters = map.entrySet(); 
	
	String  configId       =    "";
	String  configName     =    "";
	boolean dolist         = false;
	boolean doversions     = false;
	boolean dopackages     = false;
	String  listBeginsWith =    "";
	
	for ( Map.Entry<String,String[]> entry : parameters )
	{
		if ( entry.getValue().length > 1 )
		{
			out.println( "ERROR: Only one parameter '" + entry.getKey() + "' allowed!" );
			return;
		}
		
		String value = entry.getValue()[0];
		if ( entry.getKey().equals( "configId" ) )
		    configId = value;
		else if ( entry.getKey().equals( "configName" ) )
		    configName = value;
		else if ( entry.getKey().equals( "list" ) ) {
		    dolist = true;
		    listBeginsWith = value;
		}				
		else if ( entry.getKey().equals( "versions" ) )
		    doversions = true;
		else if ( entry.getKey().equals( "packages" ) )
		    dopackages = true;
		else
		{
		    out.println("ERROR: invalid option " +
				entry.getKey());
			return;
		}
	}

	
    try {
	converter = BrowserConverter.getConverter( 1 );
	ConfDB database = converter.getDatabase();
	
	if (dolist) {
	    String[] allConfigs = database.getConfigNames();
	    for (String s : allConfigs)
		if (s.startsWith(listBeginsWith))  out.println(s);
	    return;
	}
	
	if (configId.length()==0&&configName.length()==0) {
	    out.println("ERROR: configId *OR* configName must be specified");
	    return;
	}
	
	if (configId.length()> 0&&configName.length()> 0) {
	    out.println("ERROR: specify either configId *OR* configName");
	    return;
	}

	int id = (configId.length()>0) ?
	    Integer.parseInt(configId) : database.getConfigId(configName);
	if (id<=0) {
	    out.println("ERROR: configuration not found!");
	    return;
	}
	    	    
	if (dopackages) {
	    Configuration   cfg     = database.loadConfiguration(id);
	    SoftwareRelease release = cfg.release();
	    Iterator<String> it = release.listOfReferencedPackages().iterator();
	    while (it.hasNext()) {
		String pkg = it.next();
		out.println(pkg);
	    }
	    return;		
	}
	
	if (doversions) {
	    ConfigInfo info = database.getConfigInfo(id);
	    System.out.println("name=" + info.parentDir().name() + "/" +
			       info.name());
	    for (int i=0;i<info.versionCount();i++) {
		ConfigVersion version = info.version(i);
		out.println(version.version()+"\t"+
			    version.dbId()+"\t"+
			    version.releaseTag()+"\t"+
			    version.created()+"\t"+
			    version.creator());
		if (version.comment().length()>0)
		    out.println("  -> " + version.comment());
	    }
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


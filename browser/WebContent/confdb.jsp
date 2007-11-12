<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.db.ConfDB"%>
<%@page import="confdb.data.SoftwareRelease"%>
<%@page import="confdb.data.Configuration"%>
<%@page import="confdb.data.ConfigInfo"%>
<%@page import="confdb.data.ConfigVersion"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page contentType="text/plain"%>
<%

    out.clearBuffer();
    ConfDB database = new ConfDB();
    
    try {
	Map<String,String[]> map = request.getParameterMap();
	Set<Map.Entry<String,String[]>> parameters = map.entrySet(); 
	
	String  configId       =    "";
	String  configName     =    "";
	boolean dolist         = false;
	boolean doversions     = false;
	boolean dopackages     = false;
	String  listBeginsWith =    "";
	
	for ( Map.Entry<String,String[]> entry : parameters )
	    {
		String value = "";
		for ( String para : entry.getValue() )
		    value += (value.length() == 0) ? para : ("," + para);
		
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
		    out.println("ERROR: invalid option " +
				entry.getKey());
	    }
	
	database.connect("oracle",
			 "jdbc:oracle:thin:@//int2r1-v.cern.ch:10121" +
			 "/int2r_lb.cern.ch",
			 "cms_hlt_reader",
			 "convertme!");
	
	if (dolist) {
	    String[] allConfigs = database.getConfigNames();
	    for (String s : allConfigs)
		if (s.startsWith(listBeginsWith))  out.println(s);
	}
	else if (configId.length()==0&&configName.length()==0) {
	    out.println("ERROR: configId *OR* configName must be specified");
	}
	else if (configId.length()> 0&&configName.length()> 0) {
	    out.println("ERROR: specify either configId *OR* configName");
	}
	else {
	    int id = (configId.length()>0) ?
		Integer.parseInt(configId) : database.getConfigId(configName);
	    if (id<=0) {
		out.println("ERROR: configuration not found!");
	    }
	    if (dopackages) {
		Configuration   cfg     = database.loadConfiguration(id);
		SoftwareRelease release = cfg.release();
		Iterator it = release.listOfReferencedPackages().iterator();
		while (it.hasNext()) {
		    String pkg = (String)it.next();
		    out.println(pkg);
		}
	    }
	    else if (doversions) {
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
    }
    catch ( Exception e ) {
	out.print( "ERROR!\n\n" ); 
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	PrintWriter writer = new PrintWriter( buffer );
	e.printStackTrace( writer );
	writer.close();
	out.println( buffer.toString() );
    }
    finally {
	try { database.disconnect(); } catch (Exception e) {}
    }    

%>


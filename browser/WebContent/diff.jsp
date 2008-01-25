<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="confdb.db.ConfDB"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="confdb.db.DatabaseException"%>
<%@page import="confdb.converter.DbProperties"%>
<%@page import="confdb.data.*"%>
<%@page import="confdb.diff.*"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/plain"%>
<%
    //{
    out.clearBuffer();
    Map<String,String[]> map = request.getParameterMap();
    if (map.isEmpty()){
	out.println("ERROR: configurations must be specified!");
	return;
    }
    
    Set<Map.Entry<String,String[]>> parameters = map.entrySet();
    
    String configs = "";
    
    for (Map.Entry<String,String[]> entry : parameters)	{
	if (entry.getValue().length > 1) {
	    out.println("ERROR: Only one parameter '"+entry.getKey()+"' allowed!");
	    return;
	}
	
	String value = entry.getValue()[0];
	String key = entry.getKey();
	if ( key.equals("configs")) {
	    configs = value;
	}
	else {
	    out.println("ERROR: unknown option '"+key+"'");
	    return;
	}
    }
    
    try {
	Diff.initDatabase();
	String a[] = configs.split(",");
	Configuration config1 = Diff.getConfiguration(a[0]);
	Configuration config2 = Diff.getConfiguration(a[1]);
	out.println("config1: " + config1.toString());
	out.println("config2: " + config2.toString());	
	
	Diff diff = new Diff(config1,config2);
	diff.compare();
	
	// global psets
	if (diff.psetCount()>0) {
	    out.println("----------------------------------------"+
			"----------------------------------------");
	    out.println("Global PSets (" + diff.psetCount()+"):");
	    out.println(diff.printInstanceComparisons(diff.psetIterator()));
	}
	
	
	// edsources
	if (diff.edsourceCount()>0) {
	    out.println("----------------------------------------"+
			"----------------------------------------");
	    out.println("EDSources (" + diff.edsourceCount() + "):");
	    out.println(diff.printInstanceComparisons(diff.edsourceIterator()));
	}
	
	// essources
	if (diff.essourceCount()>0) {
	    out.println("----------------------------------------"+
			"----------------------------------------");
	    out.println("ESSources (" + diff.essourceCount() + "):");
	    out.println(diff.printInstanceComparisons(diff.essourceIterator()));
	}
	
	// esmodules
	if (diff.esmoduleCount()>0) {
	    out.println("----------------------------------------"+
			"----------------------------------------");
	    out.println("ESModules (" + diff.esmoduleCount() + "):");
	    out.println(diff.printInstanceComparisons(diff.esmoduleIterator()));
	}
	
	// services
	if (diff.serviceCount()>0) {
	    out.println("----------------------------------------"+
			"----------------------------------------");
	    out.println("Services (" + diff.serviceCount() + "):");
	    out.println(diff.printInstanceComparisons(diff.serviceIterator()));
	}
	
	// paths
	if (diff.pathCount()>0) {
	    out.println("----------------------------------------"+
			"----------------------------------------");
	    out.println("Paths (" + diff.pathCount() + "):");
	    out.println(diff.printContainerComparisons(diff.pathIterator()));
	}
	
	// sequences
	if (diff.sequenceCount()>0) {
	    out.println("----------------------------------------"+
			"----------------------------------------");
	    out.println("Sequences (" + diff.sequenceCount() + "):");
	    out.println(diff.printContainerComparisons(diff.sequenceIterator()));
	}
	
	// modules
	if (diff.moduleCount()>0) {
	    out.println("----------------------------------------"+
			"----------------------------------------");
	    out.println("Modules (" + diff.moduleCount() + "):");
	    out.println(diff.printInstanceComparisons(diff.moduleIterator()));
	}
    }
    catch (DiffException e) {
	out.print(e.getMessage()+"\n");
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	PrintWriter writer = new PrintWriter(buffer);
	e.getCause().printStackTrace(writer);
	writer.close();
	out.println(buffer.toString());
    }
    catch (Exception e) {
	out.print(e.getMessage()+"\n\n");
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	PrintWriter writer = new PrintWriter(buffer);
	e.printStackTrace(writer);
	writer.close();
	out.println(buffer.toString());
    }
    
    
//}
%>

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
    String search  = "";
    
    for (Map.Entry<String,String[]> entry : parameters)	{

	if (entry.getValue().length > 1) {
	    out.println("ERROR: Only one parameter '"+entry.getKey()+"' allowed!");
	    return;
	}
	
	String value = entry.getValue()[0];
	String key   = entry.getKey();

	if      ( key.equals("configs")) { configs = value; }
	else if ( key.equals("search"))  { search  = value; }
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
	out.println("old config: " + config1.toString());
	out.println("new config: " + config2.toString());	
	
	Diff diff = new Diff(config1,config2);
	if (search.length()>0) diff.compare(search); else diff.compare();
	out.println(diff.printAll());
	
    }
    catch (DiffException e) {
	out.println(e.getMessage());
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	PrintWriter writer = new PrintWriter(buffer);
	e.getCause().printStackTrace(writer);
	writer.close();
	out.println(buffer.toString());
    }
    catch (Exception e) {
	out.println(e.getMessage());
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	PrintWriter writer = new PrintWriter(buffer);
	e.printStackTrace(writer);
	writer.close();
	out.println(buffer.toString());
    }
    
    
//}
%>

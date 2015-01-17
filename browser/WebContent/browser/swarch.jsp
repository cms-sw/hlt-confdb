<%@page import="java.io.PrintWriter"%>
<%@page import="java.net.URL" %>
<%@page import="java.net.URLClassLoader" %>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page contentType="text/html"%>
<%
        out.clearBuffer();
//	BrowserConverter converter = null;
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
		if ( entry.getKey().equals( "list" ) ) {
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

	
        Object converter = null;
        Object database = null;
    try {
        //String fmClassPath = (String)application.getAttribute("fmClassPath");
        //String fmClassPath = application.getRealPath("/").replace('\\', '/')+"gui"+request.getParameter("groupID")+"/";
        String fmClassPath = application.getRealPath("/")+"gui/";

        URLClassLoader ldr = new java.net.URLClassLoader(new java.net.URL[] { new java.net.URL("file://" + fmClassPath) }, this.getClass().getClassLoader());
        Class<?> classConverterBase = ldr.loadClass("confdb.converter.ConverterBase");
        Class<?> classBrowserConverter = ldr.loadClass("confdb.converter.BrowserConverter");
        Class<?> classOnlineConverter = ldr.loadClass("confdb.converter.OnlineConverter");
        Class<?> classDbProperties=ldr.loadClass("confdb.converter.DbProperties");
        Class<?> classConfDbSetups=ldr.loadClass("confdb.db.ConfDBSetups");
        Class<?> classConfDb=ldr.loadClass("confdb.db.ConfDB");
       // Class<?> classConverterException = ldr.loadClass("confdb.converter.ConverterException");
       // Class<?> classConfDBSetups = ldr.loadClass("confdb.db.ConfDBSetups");

//        Object paras = classBrowserConverter.getMethod("getUrlParameter", java.util.Map.class).invoke(null, request.getParameterMap());

	int dbIndex = Integer.parseInt( dbIndexStr );
//	converter = BrowserConverter.getConverter( dbIndex );
//	ConfDB database = converter.getDatabase();

       

            //if (( "online".equals(paras.getClass().getField("dbName").get()) )|| (paras.getClass().getField("dbName").get().equals("")) 
            if (dbIndex==11){
                 out.println("Online!!");
                converter = classOnlineConverter.getMethod("getConverter").invoke(null);
            }
            else
//               converter = classBrowserConverter.getMethod("getConverter", String.class).invoke(null, paras.getClass().getField("dbName").get(paras));
               converter = classBrowserConverter.getMethod("getConverter", int.class).invoke(null, dbIndex );

           database = converter.getClass().getMethod("getDatabase").invoke(converter);
           database.getClass().getMethod("reconnect").invoke(database);

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
//            if ( converter != null )
//                classBrowserConverter.getMethod("deleteConverter", ldr.loadClass("confdb.converter.ConverterBase")).invoke(null, converter);
            return;
        }
	
	if (dolist) {
	  Object allConfigs = database.getClass().getMethod("getSwArchNames").invoke(database);
          for (String s : (String []) allConfigs)
		 out.println(s);
	    return;
	}
	
	
	if (doselect) {
	    Object allConfigs = database.getClass().getMethod("getSwArchNames").invoke(database);
          out.println("Select SW_ARCH:<select id=chosenSwArch>");
          for (String s : (String []) allConfigs)
		 out.println("<option>"+s+"</option>");
            out.println("</select>");
	    return;
	}

%>


<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="java.util.Map"%>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@page import="java.util.List"%>
<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.Reader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.OutputStream"%>
<%@page import="java.io.PrintStream"%>
<%@page import="confdb.db.ConfDBCreateConfig"%>
<%@page import="java.util.HashMap"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ConfDBCreateConfig output</title>
</head>
<body>
<pre>
<%
	//Check that we have a file upload request
	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	if ( !isMultipart )
	{
		Map<String,String[]> map = request.getParameterMap();
		out.println( new JSONSerializer().exclude( "*.class" ).deepSerialize( map ) );
		return;
	}
	
	// Create a factory for disk-based file items
	DiskFileItemFactory factory = new DiskFileItemFactory();

	// Create a new file upload handler
	ServletFileUpload upload = new ServletFileUpload(factory);

	// Parse the request
	List<FileItem> items = upload.parseRequest(request);
	
	String dbHost = "";
	String dbName = "";
	String dbUser = "";
	String dbPwrd = "";
	String masterConfigName = "";
	String target = "";
	String user = "";
	
	HashMap<String,String> nameMap = new HashMap<String,String>();
	HashMap<String,String> checkedMap = new HashMap<String,String>();
	
	// loop over all form parameters which are NOT files
	for ( FileItem item : items )
	{
		if ( item.isFormField() ) 
		{
		    String name = item.getFieldName();
		    String value = item.getString();
		    if ( name.equalsIgnoreCase( "dbhost" ) )
		    	dbHost = value;
		    else if ( name.equalsIgnoreCase( "dbname" ) )
		    	dbName = value;
		    else if ( name.equalsIgnoreCase( "dbuser" ) )
		    	dbUser = value;
		    else if ( name.equalsIgnoreCase( "dbpwrd" ) )
		    	dbPwrd = value;
		    else if ( name.equalsIgnoreCase( "master" ) )
		    	masterConfigName = value;
		    else if ( name.equalsIgnoreCase( "target" ) )
		    	target = value;
		    else if ( name.equalsIgnoreCase( "user" ) )
		    	user = value;
		    else if ( name.startsWith( "table" ) )
		    {
		    	String id = name.substring( 5 );
		    	if ( id.length() > 0 )
			    	nameMap.put( id, value );
		    }
		    else if ( name.startsWith( "checked" ) )
		    {
		    	String id = name.substring( 7 );
		    	if ( id.length() > 0 )
		    	{
		    		if ( value.length() > 0  && value.equals( "on" ) )
		    			checkedMap.put( id, "+" );
		    	}
		    }
		    else
				out.println( "ERROR unknown name= " + name + ", value=" + value );
		}
	}
	
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	PrintStream outStream = new PrintStream( buffer );
	// now get all the files
	try {
		for ( FileItem item : items )
		{
			if ( !item.isFormField() ) 
			{
				String name = item.getFieldName();
		    	if ( name.startsWith( "file" ) )
		    	{
		    		String id = name.substring( 4 );
		    		if ( checkedMap.get( id ) != null  &&  nameMap.get( id ) != null )
		    		{
			    		String newConfigName = target + "/" + nameMap.get( id );
						String fileName = item.getName();
						//String contentType = item.getContentType();
						//boolean isInMemory = item.isInMemory();
						//long sizeInBytes = item.getSize();
						if ( fileName.length() > 0 )
						{
							out.println( "name=" + newConfigName + ", file=" + fileName );
							String dbUrl = ConfDBCreateConfig.buildUrl( dbHost, dbName, "oracle", "10121" );
							HashSet<String> pathsToInclude = ConfDBCreateConfig.decodePathList( new InputStreamReader( item.getInputStream() ), outStream );
							ConfDBCreateConfig.doIt( "oracle", dbUrl, dbUser, dbPwrd, 
								  masterConfigName, newConfigName, pathsToInclude, outStream, user );
						}
		    		}
				}
			}
		}
	} catch (Exception e) { 
		e.printStackTrace( outStream ); 
	}
	outStream.close();
	out.println( buffer.toString() );
%>


</pre>
</body>
</html>
<%@page import="java.util.ArrayList"%>
<%@page import="java.lang.reflect.Modifier"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/plain"%>
<% 	  	     
    out.clearBuffer();
	Map<String,String[]> map = request.getParameterMap();

	String[] methods = map.get( "method" );
	if ( methods == null )
	{
		out.println("ERROR: missing method parameter");
		return;
	}
	if ( methods.length > 1 )
	{
		out.println("ERROR: exactly 1 method parameter allowed");
		return;
	}
	
	String methodName = methods[0];
	
	String[] classes = map.get( "class" );
	if ( classes != null  &&  classes.length > 1 )
	{
		out.println("ERROR: exactly 1 class parameter allowed");
		return;
	}
	
	String className = (classes != null) && (classes.length > 0) ? classes[0] : null; 
	
	int split = methodName.indexOf( ':' );
	if ( split != -1 )
	{
		if ( className != null )
		{
			out.println("ERROR: exactly 1 class name allowed");
			return;
		}
		className = methodName.substring( 0, split );
		methodName = methodName.substring( split + 1 );
	}
	
	if ( className == null )
	{
		out.println("ERROR: class name missing");
		return;
	}

	ArrayList<String> paramList = new ArrayList<String>();
	int paramN = 1;
	boolean parameterFound = true;
	while ( parameterFound )
	{
		String[] parameterN = map.get( "p" + paramN);
		if ( parameterN == null )
			parameterFound = false;
		else if ( parameterN.length > 1 )
		{
			out.println("ERROR: exactly 1 parameter p" + paramN + " allowed");
			return;
		}
		else
			paramList.add( parameterN[0] );
		paramN += 1;
	}
	
	try {
		Class<?> jsonClass = Class.forName( className );
		Class<?>[] paramClasses = new Class[ paramList.size() ];
		Object[] params = new Object[ paramList.size() ];
		for ( int i = 0; i < params.length; i++ )
		{
			String param = paramList.get( i );
			split = param.indexOf( ':' );
			if ( split != -1 )
			{
				String paramClass = param.substring( 0, split );
				if ( paramClass.equals( "int" ) )
				{
					paramClasses[i] = Integer.TYPE;
					params[i] = new Integer( param.substring( split + 1 ) );
				}
			}
		}
		Method method = jsonClass.getMethod( methodName, paramClasses );
		Object result = null;
		if ( Modifier.isStatic( method.getModifiers() ) )
			result = method.invoke( null, params );
		else
			result = method.invoke( jsonClass.newInstance(), params );
		out.println( new JSONSerializer().serialize( result ) );
	} catch (Exception e) {
	   out.print( "EXCEPTION: " + e.getMessage()+"\n\n" ); 
	   ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	   PrintWriter writer = new PrintWriter(buffer);
	   e.printStackTrace(writer);
	   writer.close();
	   out.println(buffer.toString());
	}
	
%>



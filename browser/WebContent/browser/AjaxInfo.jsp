<%@page import="java.lang.reflect.InvocationTargetException"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.lang.reflect.Modifier"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/plain"%>
<%!


public class AjaxException
{
	public boolean exceptionThrown = true;
	public String exception = "";
	public String message = "";
	public String stacktrace = "";

	public AjaxException( Exception ajaxE )
	{
	  Throwable e = ajaxE;
	  if ( e instanceof InvocationTargetException && e.getCause() != null )
		  e = e.getCause();
	  message = e.getMessage();
	  exception = e.getClass().getCanonicalName();	
	  ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	  PrintWriter writer = new PrintWriter(buffer);
	  e.printStackTrace(writer);
	  writer.close();
	  stacktrace = buffer.toString();
	}
}


public void gc()
{
	Runtime.getRuntime().gc();
	
}



private Object exec( String className, String methodName, ArrayList<String> paramList, HttpSession session ) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException 
{
	Class<?> jsonClass = this.getClass();
	if ( className != null )
	  jsonClass = Class.forName( className );
	Class<?>[] paramClasses = new Class[ paramList.size() ];
	Object[] params = new Object[ paramList.size() ];
	for ( int i = 0; i < params.length; i++ )
	{
		String param = paramList.get( i );
		int split = param.indexOf( ':' );
		if ( split != -1 )
		{
			String paramClass = param.substring( 0, split );
			if ( paramClass.equals( "int" ) )
			{
				paramClasses[i] = Integer.TYPE;
				params[i] = new Integer( param.substring( split + 1 ) );
			}
			else if ( paramClass.equals( "string" ) )
			{
				paramClasses[i] = String.class;
				params[i] = param.substring( split + 1 );
			}
		}
	}
	Method method = jsonClass.getMethod( methodName, paramClasses );
	Object result = null;
	if ( Modifier.isStatic( method.getModifiers() ) )
		result = method.invoke( null, params );
	else
	{
		Object jsonObject = this;
		if ( className != null )
		{
			if ( session == null )
				jsonObject = jsonClass.newInstance();
			else
			{
				jsonObject = session.getAttribute( jsonClass.getCanonicalName() );
				if ( jsonObject != null  &&  jsonObject.getClass() != this.getClass() )
					jsonObject = null;
				if ( jsonObject == null )
				{
					jsonObject = jsonClass.newInstance();
					session.setAttribute( jsonClass.getCanonicalName(), jsonObject );
					System.out.println( "new object for session " + session.getId() );
				}
			}
		}
		result = method.invoke( jsonObject, params );
	}
	return result;
}

%>

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
		className = "confdb.converter.AjaxJsp";
	
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
		Class<?> jsonClass = this.getClass();
		if ( className != null )
			jsonClass = Class.forName( className );
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
				else if ( paramClass.equals( "string" ) )
				{
					paramClasses[i] = String.class;
					params[i] = param.substring( split + 1 );
				}
			}
		}
		Method method = jsonClass.getMethod( methodName, paramClasses );
		Object result = null;
		if ( Modifier.isStatic( method.getModifiers() ) )
			result = method.invoke( null, params );
		else
		{
			Object jsonObject = this;
			if ( className != null )
			{
				HttpSession jsonSession = request.getSession( false );
				if ( jsonSession == null )
					jsonObject = jsonClass.newInstance();
				else
				{
					jsonObject = jsonSession.getAttribute( jsonClass.getCanonicalName() );
					if ( jsonObject != null  &&  jsonObject.getClass() != this.getClass() )
						jsonObject = null;
					if ( jsonObject == null )
					{
						jsonObject = jsonClass.newInstance();
						jsonSession.setAttribute( jsonClass.getCanonicalName(), jsonObject );
						System.out.println( "new object for session " + jsonSession.getId() );
					}
				}
			}
			result = method.invoke( jsonObject, params );
		}
		if ( result instanceof String )
			out.println( result );
		else
			out.println( new JSONSerializer().exclude( "*.class" ).deepSerialize( result ) );
	} catch (Exception e) {
	   AjaxException ajaxE = new AjaxException( e );
	   out.println( new JSONSerializer().exclude( "*.class" ).serialize( ajaxE ) );
	}
%>



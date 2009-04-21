<%@page import="java.lang.reflect.InvocationTargetException"%>

<%@page import="confdb.data.ConfigVersion"%>
<%@page import="confdb.data.ConfigInfo"%>
<%@page import="confdb.converter.ConverterException"%>
<%@page import="confdb.data.Directory"%>
<%@page import="confdb.db.ConfDB"%>
<%@page import="confdb.db.ConfDBSetups"%>
<%@page import="confdb.converter.ConverterBase"%>

<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.converter.OnlineConverter"%>
<%@page import="confdb.converter.RcmsDbProperties"%>
<%@page import="confdb.converter.DbProperties"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.lang.reflect.Modifier"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/plain"%>
<%!

public class NodeData
{
	public int    version;
	//public String versionInfo;
	public String label;
	public int    key;
	public String name;
	public String fullName;
	public String title;
}

public class AjaxTreeNode
{
	public NodeData nodeData;
	public ArrayList<AjaxTreeNode> subnodes = new ArrayList<AjaxTreeNode>();
	
	AjaxTreeNode( NodeData nodeData )
	{
		this.nodeData = nodeData;
	}
}

public class AjaxTree
{
	String name = "";
	ArrayList<AjaxTree> dirs = new ArrayList<AjaxTree>();
	ArrayList<AjaxTreeNode> configs = new ArrayList<AjaxTreeNode>();
	
	public AjaxTree( String name )
	{
		this.name = name;
	}
	
	public AjaxTree[] getDirs()
	{
		return dirs.toArray( new AjaxTree[ dirs.size() ] );
	}
	
	public AjaxTreeNode[] getConfigs()
	{
		return configs.toArray( new AjaxTreeNode[ configs.size() ] );
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addSubTree( AjaxTree tree )
	{
		dirs.add( tree );
	}
	
	public void addNode( AjaxTreeNode node )
	{
		configs.add( node );
	}
	
}

public class MemInfo
{
	public int getFreeMemory()
	{
		return toMB( Runtime.getRuntime().freeMemory() ); 
	}
	
	public int getMaxMemory()
	{
		return toMB( Runtime.getRuntime().maxMemory() );
	}
	
	public int getTotalMemory()
	{
		return toMB( Runtime.getRuntime().totalMemory() );
	}

	public int getCacheEntries()
	{
		return BrowserConverter.getCacheEntries();
	}
	
	private int toMB( long bytes )
	{
		int MB = 1024 * 1024;
		return (int) (( bytes + MB / 2 ) / MB);
	}
}


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


public MemInfo getMemInfo()
{
	return new MemInfo();
}

public void gc()
{
	Runtime.getRuntime().gc();
	
}


public String getTree( String dbName, String filterStr ) throws ConverterException
{
	//System.out.println( "filter: " + filterStr );
	int dbIndex = 1;
	AjaxTree tree = new AjaxTree( "" );
	ConverterBase converter = null;
	try {
		if ( dbName.equals( "online" ) )
			converter = OnlineConverter.getConverter();
		else
		{
			ConfDBSetups dbs = new ConfDBSetups();
			if ( dbName != null && dbName.length() > 0 )
			{
			  	String[] labels = dbs.labelsAsArray();
		 		for ( int i = 0; i < dbs.setupCount(); i++ )
		  		{
		    		if ( dbName.equalsIgnoreCase( labels[i] ) )
		  	  		{
		  				dbIndex = i;
		  				break;
		  	  		}
		  		}
			}
			converter = BrowserConverter.getConverter( dbIndex );
		}
		ConfDB confDB = converter.getDatabase();
		Directory root = confDB.loadConfigurationTree();
		buildTree( tree, root, filterStr );
		String result = new JSONSerializer().include( "dirs" ).include( "configs" ).exclude( "*.class" ).deepSerialize( tree );
		return result;
	} catch (Exception e) {
		String errorMessage = "ERROR!\n";
		if ( e instanceof ConverterException )
		{
		  errorMessage += e.toString();
		  if ( e.getMessage().startsWith( "can't init database connection" )	)
			  errorMessage += " (host = " + (new ConfDBSetups()).host( dbIndex ) + ")";
		}
		else
		{
		  ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		  PrintWriter writer = new PrintWriter( buffer );
		  e.printStackTrace( writer );
		  writer.close();
		  errorMessage += buffer.toString();
		}
		if ( converter != null )
			  BrowserConverter.deleteConverter( converter );
	    throw new ConverterException( errorMessage );
	}
}

public String getRcmsDbInfo()
{
		DbProperties dbProperties;
		try {
			dbProperties = DbProperties.getDefaultDbProperties();
		} catch (Exception e1) {
			return e1.toString();
		}
		if ( !(dbProperties instanceof RcmsDbProperties) )
		{
			try {
				new RcmsDbProperties();
			} catch (Exception e) {
				return e.toString();
			}
		}
		
		return dbProperties.getDbURL();
}

	
private boolean buildTree( AjaxTree parentNode, Directory directory, String filter )
{
	boolean addDir = false;
	Directory[] list = directory.listOfDirectories();
	for ( int i = 0; i < list.length; i++ )
	{
		String name = list[i].name();
		int start = name.lastIndexOf( '/' );
		if ( start > 0 )
			name = name.substring( start + 1 );
		AjaxTree subtree = new AjaxTree( name );
		boolean addSubTree = buildTree( subtree, list[i], filter );
		if ( addSubTree )
		{
			parentNode.addSubTree( subtree );
			addDir = true;
		}
	}

	ConfigInfo[] configs = directory.listOfConfigurations();
	for ( int i = 0; i < configs.length; i++ )
	{
		ConfigInfo config = configs[i];
		String name = config.name();
		ConfigVersion versionInfo = config.version( 0 ); 
		NodeData nodeData = new NodeData();		
		nodeData.version = versionInfo.version();
		nodeData.label = name;
		nodeData.key = versionInfo.dbId();
		nodeData.name = name;
		String fullPath = config.parentDir().name() + "/" + name;
		nodeData.fullName = fullPath + "/V" + versionInfo.version();
		nodeData.title = "V" + versionInfo.version() + "  -  " + versionInfo.created();
		if ( filter == null  || filter.length() == 0  || fullPath.matches( filter ) )
		{
		  addDir |= true;
		  AjaxTreeNode node = new AjaxTreeNode( nodeData );
		  parentNode.addNode( node );
		
		  for ( int ii = 0; ii < config.versionCount(); ii++ )
	      {
			versionInfo = config.version( ii );
			nodeData = new NodeData();		
			nodeData.version = versionInfo.version();
			nodeData.label = "V" + versionInfo.version() + "  -  " + versionInfo.created();
			nodeData.key = versionInfo.dbId();
			nodeData.name = name;
			nodeData.fullName = config.parentDir().name() + "/" + name + "/V" + versionInfo.version();
			nodeData.title = versionInfo.comment();
			node.subnodes.add( new AjaxTreeNode( nodeData ) );
	      }
		}
	}
	return addDir;
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
			out.println( new JSONSerializer().exclude( "*.class" ).serialize( result ) );
	} catch (Exception e) {
	   AjaxException ajaxE = new AjaxException( e );
	   out.println( new JSONSerializer().exclude( "*.class" ).serialize( ajaxE ) );
	}
%>



package confdb.converter;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;
import confdb.data.Directory;
import confdb.data.IConfiguration;
import confdb.db.ConfDB;
import confdb.db.ConfDBSetups;
import confdb.db.DatabaseException;

public class AjaxJsp 
{
	private ConverterBase converter = null;
	private IConfiguration conf = null;

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

    public Object getTree( String dbName, String filterStr ) throws ConverterException
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
    		//String result = new JSONSerializer().include( "dirs" ).include( "configs" ).exclude( "*.class" ).deepSerialize( tree );
    		return tree;
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

    
    public String loadConfig( String dbIndex, String dbName, String configName, String configId ) throws ConverterException, NumberFormatException, DatabaseException
    {
    	if ( dbIndex != null )
    		converter = BrowserConverter.getConverter(  dbIndex );
    	else
    	{
    		if ( dbName == null ) 
    			return "ERROR!\ndbIndex or dbName must be specified!";
    		if ( dbName.equals( "online" ) )
    			converter = OnlineConverter.getConverter();
    		else	
    		{
    			if ( dbName.equalsIgnoreCase( "hltdev" ) )
    				dbName = "HLT Development";
    			ConfDBSetups dbs = new ConfDBSetups();
    		  	String[] labels = dbs.labelsAsArray();
    	  		for ( int i = 0; i < dbs.setupCount(); i++ )
    	  		{
    	  			if ( dbName.equalsIgnoreCase( labels[i] ) )
    	  			{
    	  				dbIndex = "" + i;
    	  				break;
    	  			}
    	  		}
    	  		if ( dbIndex == null  )
    	  			return "ERROR!\ninvalid dbName!";
    	  		converter = BrowserConverter.getConverter( Integer.parseInt( dbIndex ) );
    	  	}
    	}

    	if ( configId == null  &&  configName == null )
    		return "ERROR!\nconfigKey or configName must be specified!";

    	int configKey = ( configId != null ) ?
        	Integer.parseInt(configId) : converter.getDatabase().getConfigId(configName);

        conf = converter.getConfiguration( configKey );

        if ( conf == null )
    		return "ERROR!\nconfig " + configKey + " not found!";

        return null;
    }
    
    public String getHRefs()
    {
    	if ( conf == null )
    		return "ERROR!\nno config found!";
		String refs = " ";
		if ( conf.pathCount() > 0 )
			refs += "<a href=\"#paths\">paths</a> ";
		if ( conf.sequenceCount() > 0 )
			refs += "<a href=\"#sequences\">sequences</a> ";
		if ( conf.moduleCount() > 0 )
			refs += "<a href=\"#modules\">modules</a> ";
		if ( conf.edsourceCount() > 0 )
			refs += "<a href=\"#ed_sources\">ed_sources</a> ";
		if ( conf.essourceCount() > 0 )
			refs += "<a href=\"#es_sources\">es_sources</a> ";
		if ( conf.esmoduleCount() > 0 )
			refs += "<a href=\"#es_modules\">es_modules</a> ";
		if ( conf.serviceCount() > 0 )
			refs += "<a href=\"#services\">services</a> ";
		return refs;
    }
    
    public String getConfString() throws ConverterException
    {
    	return converter.getConverterEngine().convert( conf );
    }

    static public String getRcmsDbInfo()
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

    public MemInfo getMemInfo()
    {
    	return new MemInfo();
    }




}

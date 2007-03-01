package confdb.converter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;

import confdb.data.ConfigInfo;
import confdb.data.Configuration;
import confdb.data.Directory;
import confdb.data.Template;
import confdb.db.CfgDatabase;
import confdb.db.DatabaseException;

public class Converter {
	private CfgDatabase database = null;
	private Configuration configuration = null;
	private IConfigurationWriter configurationWriter = null;
	private IParameterWriter parameterWriter = null;

	private IEDSourceWriter edsourceWriter  = null;
	private IESSourceWriter essourceWriter = null;
	private IServiceWriter serviceWriter = null;
	private IModuleWriter moduleWriter = null;
	private IPathWriter pathWriter = null;
	private ISequenceWriter sequenceWriter = null;

	private ArrayList<Template> edsourceTemplateList = new ArrayList<Template>();
	private ArrayList<Template> essourceTemplateList = new ArrayList<Template>();
	private ArrayList<Template> serviceTemplateList = new ArrayList<Template>();
	private ArrayList<Template> moduleTemplateList = new ArrayList<Template>();

	static private Preferences  prefs = Preferences.userNodeForPackage( Converter.class );
	
	static final public String newline = "\n";

	private static String CMSSWrelease = getPrefs().get( "CMSSWrelease", "CMSSW_1_3_0_pre3" );
	private static String dbName = getPrefs().get( "dbName", "hltdb" );
	private static String dbType = getPrefs().get( "dbType", "mysql" );
	private static String dbHost = getPrefs().get( "dbHost", "localhost" );
	private static String dbUser = getPrefs().get( "dbUser", "hlt" );
	private static String dbPwrd = getPrefs().get( "dbPwrd", "hlt" );
	
	private static HashMap<Integer, String> cache = new HashMap<Integer, String>();

	static 
	{
		cache.put( new Integer(-1), "file:/home/daqpro/cms/triggertables/emulator_async.cfg" );
		cache.put( new Integer(-2), "file:/home/daqpro/cms/triggertables/emulator_async_2.cfg" );
	}
	
	
	protected Converter()
	{
	}
	
	public String readConfiguration( int configKey ) throws DatabaseException, SQLException
	{
		if ( configKey < 0 )
			return cache.get( new Integer(configKey) );

		String dbUrl = "jdbc:mysql://" + dbHost + ":3306/" + dbName;
		if ( dbType.equals("oracle") )
		    dbUrl = "jdbc:oracle:thin:@//" + dbHost + "/" + dbName;
		
		try {
			database.connect( dbType, dbUrl, dbUser, dbPwrd );
			database.prepareStatements();
			
			if ( !loadConfiguration(configKey) )
				return null;
			return convertConfiguration();
		} finally {
			database.disconnect();
		}
	}

		
		
	public boolean loadConfiguration( int configKey ) throws SQLException
	{
		ConfigInfo configInfo = findConfig(configKey);
		if ( configInfo == null )
			return false;
		configuration = loadConfiguration(configInfo);
		if ( configuration == null )
			return false;
		else
			return true;
	}
	
	protected Configuration loadConfiguration( ConfigInfo configInfo ) throws SQLException
	{
		return database.loadConfiguration( configInfo, 
										edsourceTemplateList, 
										essourceTemplateList, 
										serviceTemplateList, 
										moduleTemplateList );
	}
	
	public String convertConfiguration()
	{
		String str = "// " + configuration.name() + " V" + configuration.version()
			+ " (" + configuration.releaseTag() + ")" + getNewline();
		return str + configurationWriter.toString( configuration, this );
	}
	

	public ConfigInfo findConfig( int key )
	{
		return findConfig( key, getRootDirectory() );
	}

	public Directory getRootDirectory()
	{
		return database.loadConfigurationTree();
	}

	protected ConfigInfo findConfig( int key, Directory directory )
	{
		for ( int i = 0; i < directory.configInfoCount(); i++ )
		{
			ConfigInfo configInfo = directory.configInfo(i);
			if ( configInfo.dbId() == key ) 
				return configInfo;
		}
		for ( int i = 0; i < directory.childDirCount(); i++ )
		{
			ConfigInfo configInfo = findConfig( key, directory.childDir(i) );
			if ( configInfo != null )
				return configInfo;
		}
		return null;
	}
	
	public Directory[] listSubDirectories( Directory directory )
	{
		Directory[] list = new Directory[ directory.childDirCount() ];
		for ( int i = 0; i < directory.childDirCount(); i++ )
			list[i] = directory.childDir(i);
		return list;
	}
	
	
	public ConfigInfo[] listConfigs( Directory directory )
	{
		ConfigInfo[] list = new ConfigInfo[ directory.configInfoCount() ];
		for ( int i = 0; i < directory.configInfoCount(); i++ )
			list[i] = directory.configInfo(i);
		return list;
	}
	
	
	public static void main(String[] args) 
	{
		String usage = "java " + Converter.class.getName() 
		  + "  configKey [ CMSSWrelease dbName dbType dbHost dbUser dbPwrd]\n";
		
		if ( args.length < 1 )
		{
			System.err.println( "usage:" );
			System.err.println( usage );
			System.exit(1);
		}

		int configKey = Integer.parseInt( args[0] );
		
		int argI = 1;
		
		if ( args.length > argI )
		{
			CMSSWrelease = args[argI++];
			prefs.put( "CMSSWrelease", CMSSWrelease );
		}
					
		if ( args.length > argI )
		{
			dbName = args[argI++];
			prefs.put( "dbName", dbName );
		}

		if ( args.length > argI )
		{
			dbType = args[argI++];
			prefs.put( "dbType", dbType );
		}

		if ( args.length > argI )
		{
			dbHost = args[argI++];
			prefs.put( "dbHost", dbHost );
		}

		if ( args.length > argI )
		{
			dbUser = args[argI++];
			prefs.put( "dbUser", dbUser );
		}

		if ( args.length > argI )
		{
			dbPwrd = args[argI++];
			prefs.put( "dbPwrd", dbPwrd );
		}

		try {
			Converter converter = Converter.getConverter();
			String config = converter.readConfiguration(configKey);
			if ( config == null )
				System.out.println( "config " + configKey + " not found!" );
			else
				System.out.println( config );
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public String getNewline()
	{
		return newline;
	}

	public CfgDatabase getDatabase() {
		return database;
	}


	public void setDatabase(CfgDatabase database) {
		this.database = database;
	}
	
	public IConfigurationWriter getConfigurationWriter() {
		return configurationWriter;
	}

	protected void setConfigurationWriter(IConfigurationWriter configurationWriter) {
		this.configurationWriter = configurationWriter;
	}


	public IPathWriter getPathWriter() {
		return pathWriter;
	}


	protected void setPathWriter(IPathWriter pathWriter) {
		this.pathWriter = pathWriter;
	}


	public IServiceWriter getServiceWriter() {
		return serviceWriter;
	}


	protected void setServiceWriter(IServiceWriter serviceWriter) {
		this.serviceWriter = serviceWriter;
	}


	public IParameterWriter getParameterWriter() {
		return parameterWriter;
	}


	protected void setParameterWriter(IParameterWriter parameterWriter) {
		this.parameterWriter = parameterWriter;
	}


	public IEDSourceWriter getEDSourceWriter() {
		return edsourceWriter;
	}


	protected void setEDSourceWriter(IEDSourceWriter edsourceWriter) {
		this.edsourceWriter = edsourceWriter;
	}


	public IESSourceWriter getESSourceWriter() {
		return essourceWriter;
	}


	protected void setESSourceWriter(IESSourceWriter essourceWriter) {
		this.essourceWriter = essourceWriter;
	}


	public IModuleWriter getModuleWriter() {
		return moduleWriter;
	}


	protected void setModuleWriter(IModuleWriter moduleWriter) {
		this.moduleWriter = moduleWriter;
	}


	public ISequenceWriter getSequenceWriter() {
		return sequenceWriter;
	}


	protected void setSequenceWriter(ISequenceWriter sequenceWriter) {
		this.sequenceWriter = sequenceWriter;
	}


	public static Preferences getPrefs() {
		return prefs;
	}

	
	public static Converter getConverter() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		CfgDatabase database = new CfgDatabase();
		Converter converter = ConverterFactory.getFactory( CMSSWrelease ).getConverter();
		converter.setDatabase( database );
		
		return converter;
	}

	public static String getCMSSWrelease() {
		return CMSSWrelease;
	}

	public static void setCMSSWrelease(String wrelease) {
		CMSSWrelease = wrelease;
	}

	public static String getDbName() {
		return dbName;
	}

	public static void setDbName(String dbName) {
		Converter.dbName = dbName;
	}

	public static String getDbType() {
		return dbType;
	}

	public static void setDbType(String dbType) {
		Converter.dbType = dbType;
	}

	public static String getDbHost() {
		return dbHost;
	}

	public static void setDbHost(String dbHost) {
		Converter.dbHost = dbHost;
	}

	public static String getDbUser() {
		return dbUser;
	}

	public static void setDbUser(String dbUser) {
		Converter.dbUser = dbUser;
	}

	public static String getDbPwrd() {
		return dbPwrd;
	}

	public static void setDbPwrd(String dbPwrd) {
		Converter.dbPwrd = dbPwrd;
	}

	public static void addToCache( int key, String info )
	{
		cache.put( new Integer( key ), info );
	}
}

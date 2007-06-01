package confdb.converter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import confdb.data.ConfigInfo;
import confdb.data.ConfigVersion;
import confdb.data.Configuration;
import confdb.data.Directory;
import confdb.data.EDSourceInstance;
import confdb.data.Path;
import confdb.db.CfgDatabase;
import confdb.db.DatabaseException;

/**
 * @author behrens
 *
 */
public class Converter implements IConverter 
{
	private CfgDatabase database = null;

	private IConfigurationWriter configurationWriter = null;
	private IParameterWriter parameterWriter = null;
	private IEDSourceWriter edsourceWriter  = null;
	private IESSourceWriter essourceWriter = null;
	private IServiceWriter serviceWriter = null;
	private IModuleWriter moduleWriter = null;
	private IPathWriter pathWriter = null;
	private ISequenceWriter sequenceWriter = null;

	private Path newEndpath = null;
	
	static final private String newline = "\n";
	
	final private String configurationHeader = "process FU = {" + newline;
	final private String configurationTrailer = "}" + newline;


	private static String CMSSWrelease = "CMSSW_1_4_0_pre1";
	private String dbName = null;
	private String dbType = null;
	private String dbHost = null;
	private String dbUser = null;
	private String dbPwrd = null;

	private static HashMap<Integer, String> cache = new HashMap<Integer, String>();

	static 
	{
		cache.put( new Integer(-1), "file:/home/daqpro/cms/triggertables/emulator_async.cfg" );
		cache.put( new Integer(-2), "file:/home/daqpro/cms/triggertables/emulator_async_2.cfg" );
	}
	
	
	protected Converter()
	{
	}
	
	static public String readConfiguration( int configKey, String typeOfConverter ) throws DatabaseException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
	{
		Converter converter = ConverterFactory.getFactory().getConverter(typeOfConverter);

		converter.loadProperties();
		converter.setDatabase( new CfgDatabase() );
		return converter.readConfiguration(configKey);
	}

	public String readConfiguration( int configKey ) throws DatabaseException, SQLException
	{
		if ( configKey < 0 )
			return cache.get( new Integer(configKey) );

		try {
			connectToDatabase();

			Configuration configuration = loadConfiguration(configKey);
			if ( configuration == null )
				return null;
			return convert( configuration );
		} finally {
			database.disconnect();
		}
	}

		
		
	public Configuration loadConfiguration( int configKey ) throws SQLException
	{
		ConfigInfo configInfo = findConfig(configKey);
		if ( configInfo == null )
			return null;
		return loadConfiguration(configInfo);
	}
	
	protected Configuration loadConfiguration( ConfigInfo configInfo ) throws SQLException
	{
		return database.loadConfiguration( configInfo );
	}
	
	public String convert( Configuration configuration )
	{
		if ( newEndpath != null )
			return configurationWriter.toString( new ConfigModifier( configuration, newEndpath )  );
		return configurationWriter.toString( configuration );
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
			for ( int ii = 0; ii < configInfo.versionCount(); ii++ )
			{
				ConfigVersion version = configInfo.version(ii);
				if ( version.dbId() == key )
				{
					configInfo.setVersionIndex( ii );
					return configInfo;
				}
			}
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
	
	protected void loadProperties() throws IOException
	{
		InputStream inStream = getClass().getResourceAsStream( "/conf/confdb.properties" );
		Properties properties = new Properties();
		properties.load(inStream);

		String property = properties.getProperty( "confdb.dbName" );
		if ( property != null )
			dbName = new String( property );
		property = properties.getProperty( "confdb.dbType" );
		if ( property != null )
			dbType = new String( property );
		property = properties.getProperty( "confdb.dbHost" );
		if ( property != null )
			dbHost = new String( property );
		property = properties.getProperty( "confdb.dbUser" );
		if ( property != null )
			dbUser = new String( property );
		property = properties.getProperty( "confdb.dbPwrd" );
		if ( property != null )
			dbPwrd = new String( property );
	}

	public void connectToDatabase() throws DatabaseException
	{
		String dbUrl = "jdbc:mysql://" + dbHost + ":3306/" + dbName;
		if ( dbType.equals("oracle") )
		    dbUrl = "jdbc:oracle:thin:@//" + dbHost + "/" + dbName;
		
		database.connect( dbType, dbUrl, dbUser, dbPwrd );
		database.prepareStatements();
	}

	public void disconnectFromDatabase() throws DatabaseException
 	{
		database.disconnect();
 	}
	
	/**
	 * call this method to specify source to be used in output instead of data
	 * coming from database
	 */
	public void overrideEDSource( EDSourceInstance source )
	{
		setEDSourceWriter( new EDSourceOverrider( source, getEDSourceWriter() ));
	}
	
	/**
	 * call this method to specify endpath to be used in output instead of endpath
	 * coming from database
	 */
	public void overrideEndPath( Path endpath )
	{
		newEndpath = endpath;
	}
	
	
	public static void main(String[] args) 
	{
		String usage = "java " + Converter.class.getName() + "  configKey [typeOfConversion:ascii or html]\n";
		
		if ( args.length < 1 )
		{
			System.err.println( "usage:" );
			System.err.println( usage );
			System.exit(1);
		}

		int configKey = Integer.parseInt( args[0] );
		
	
		
		try {
			String config = null;
			if ( args.length > 1 )
				config = Converter.readConfiguration( configKey, args[1] );
			else
				config = Converter.readConfiguration( configKey, "ascii" );
			if ( config == null )
				System.out.println( "config " + configKey + " not found!" );
			else
				System.out.println( config );
		} catch (Exception e) {
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
		this.configurationWriter.setConverter(this);
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
		this.moduleWriter.setConverter(this);
	}


	public ISequenceWriter getSequenceWriter() {
		return sequenceWriter;
	}


	protected void setSequenceWriter(ISequenceWriter sequenceWriter) {
		this.sequenceWriter = sequenceWriter;
	}


	public static Converter getConverter() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
	{
		CfgDatabase database = new CfgDatabase();
		Converter converter = ConverterFactory.getFactory( CMSSWrelease ).getConverter();
		converter.loadProperties();
		converter.setDatabase( database );
		
		return converter;
	}

	public static String getCMSSWrelease() {
		return CMSSWrelease;
	}

	public static void setCMSSWrelease(String wrelease) {
		CMSSWrelease = wrelease;
	}

	public static void addToCache( int key, String info )
	{
		cache.put( new Integer( key ), info );
	}

	public String getConfigurationHeader() {
		return configurationHeader;
	}

	public String getConfigurationTrailer() {
		return configurationTrailer;
	}
	
	static protected String getAsciiNewline()
	{
		return newline;
	}

}

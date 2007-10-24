package confdb.converter;

import java.io.IOException;
import java.sql.SQLException;

import confdb.converter.IConfigurationWriter.WriteProcess;
import confdb.data.ConfigInfo;
import confdb.data.IConfiguration;
import confdb.data.Directory;
import confdb.db.ConfDB;
import confdb.db.ConfDBSetups;
import confdb.db.DatabaseException;

public class Converter implements IConverter 
{
	static private DbProperties defaultDbProperties = null;
	
	private ConfDB database = null;
	private DbProperties dbProperties = null;
	private ConverterEngine converterEngine = null;
	
	private Converter( String typeOfConverter ) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		converterEngine = ConverterFactory.getConverterEngine(typeOfConverter);
	}
	
	public static void setDefaultDbProperties( DbProperties defaultDbProperties) 
	{
		Converter.defaultDbProperties = defaultDbProperties;
	}

	static public String readConfiguration( int configKey, String typeOfConverter ) throws DatabaseException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
	{
		return getConverter( typeOfConverter ).readConfiguration(configKey);
	}
	
	public static Converter getConverter( String typeOfConverter ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
	{
		Converter converter = new Converter(typeOfConverter);
		if ( defaultDbProperties == null ) 
			defaultDbProperties = DbProperties.getDefaultDbProperties();
		converter.setDbProperties( defaultDbProperties );
		converter.setDatabase( new ConfDB() );
		
		return converter;
	}

	public static Converter getConverter() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException 
	{
		return getConverter( null );
	}



		
	public String readConfiguration( int configKey ) throws DatabaseException, SQLException
	{
		try {
			connectToDatabase();

			IConfiguration configuration = loadConfiguration(configKey);
			if ( configuration == null ) 
				return null;
			return convert( configuration );
		}
		finally {
			database.disconnect();
		}
	}

		
		
	public IConfiguration loadConfiguration( int configKey )
	{
		return database.loadConfiguration( configKey );
	}
	
    /*
      protected Configuration loadConfiguration( ConfigInfo configInfo ) throws SQLException
      {
      return database.loadConfiguration( configInfo );
      }
    */
	public String convert( IConfiguration configuration )
	{
		return converterEngine.getConfigurationWriter().toString( configuration, WriteProcess.YES );
	}
	

    /*
      public ConfigInfo findConfig( int key ) throws SQLException, DatabaseException
      {
      return findConfig( key, getRootDirectory() );
      }
    */
    
	public Directory getRootDirectory() throws SQLException, DatabaseException
	{
		return database.loadConfigurationTree();
	}

    /*
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
    */
    
        public Directory[] listSubDirectories( Directory directory )
	{
	    return directory.listOfDirectories();
	    /*
	      Directory[] list = new Directory[ directory.childDirCount() ];
	      for ( int i = 0; i < directory.childDirCount(); i++ )
	      list[i] = directory.childDir(i);
	      return list;
	    */
	}
	
	
	public ConfigInfo[] listConfigs( Directory directory )
	{
	    return directory.listOfConfigurations();
	    /*
	      ConfigInfo[] list = new ConfigInfo[ directory.configInfoCount() ];
	      for ( int i = 0; i < directory.configInfoCount(); i++ )
	      list[i] = directory.configInfo(i);
	      return list;
	    */
	}
	
	public void connectToDatabase() throws DatabaseException, SQLException
	{
		database.connect( dbProperties.dbType, dbProperties.dbURL, dbProperties.dbUser, dbProperties.dbPwrd );
	}

	public void disconnectFromDatabase() throws DatabaseException
 	{
		database.disconnect();
 	}
	
	
	public static void main(String[] args) 
	{
		String usage = "java " + Converter.class.getName() + "  configKey [dbIndex]\n";
		
		
		try {
		    Converter converter = Converter.getConverter();
		    
			if ( args.length < 1 )
			{
				System.err.println( "usage:" );
				System.err.println( usage );
				System.exit(1);
			}

			int configKey = Integer.parseInt( args[0] );
			
			String config = null;
			if ( args.length > 1 )
			{
				int dbIndex = Integer.parseInt( args[1] );
				ConfDBSetups dbs = new ConfDBSetups();
			    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
		    	dbProperties.setDbUser( "cms_hlt_reader" );
		    	converter.setDbProperties( dbProperties );
			}
				
			config = converter.readConfiguration( configKey );

			if ( config == null )
				System.out.println( "config " + configKey + " not found!" );
			else
				System.out.println( config );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public ConfDB getDatabase() {
		return database;
	}


	public void setDatabase(ConfDB database) {
		this.database = database;
	}
	

	public String getDbURL() {
		return dbProperties.dbURL;
	}

	public String getDbName() {
		return dbProperties.dbName;
	}

	public String getDbUser() {
		return dbProperties.dbUser;
	}

	public DbProperties getDbProperties() {
		return dbProperties;
	}

	public void setDbProperties(DbProperties dbProperties) {
		this.dbProperties = dbProperties;
	}

	public ConverterEngine getConverterEngine() {
		return converterEngine;
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}

	

}

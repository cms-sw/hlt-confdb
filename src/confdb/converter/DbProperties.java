package confdb.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import confdb.db.ConfDBSetups;

public class DbProperties 
{
	String dbType = null;
	private String dbURL = null;
	private String dbName = null;
	private String dbHost = null;
	private String dbUser = null;
	private String dbPwrd = null;

	public DbProperties( Properties properties ) throws ConverterException
	{
		init( properties );
	}
	
	public DbProperties( InputStream stream ) throws IOException, ConverterException
	{
		init( stream );
	}

	public DbProperties( String resource ) throws IOException, ConverterException
	{
		init( getClass().getResourceAsStream( resource ) );
	}

	
	public DbProperties( ConfDBSetups dbList, int index, String password )
	{
		dbName = dbList.name( index );
		dbType = dbList.type( index );
		dbHost = dbList.host( index );
		dbUser = dbList.user( index );
		dbPwrd = password;

		initURL();
	}

	protected DbProperties()
	{
	}
	
	private void init( InputStream stream ) throws IOException, ConverterException
	{
		Properties properties = new Properties();
		properties.load( stream );
		init( properties );
	}

	protected void init( Properties properties ) throws ConverterException
	{
		String property = properties.getProperty( "confdb.dbType" );
		if ( property == null )
			throw new ConverterException( "DbProperties: confdb.dbType not defined!" );
		dbType = new String( property );
		
		property = properties.getProperty( "confdb.dbUser" );
		if ( property == null )
			throw new ConverterException( "DbProperties: confdb.dbUser not defined!" );
		dbUser = new String( property );
		
		property = properties.getProperty( "confdb.dbPwrd" );
		if ( property == null )
			throw new ConverterException( "DbProperties: confdb.dbPwrd not defined!" );
		dbPwrd = new String( property );

		property = properties.getProperty( "confdb.dbURL" );
		if ( property != null )
			dbURL = property;
		else
		{
			property = properties.getProperty( "confdb.dbName" );
			if ( property == null )
				throw new ConverterException( "DbProperties: confdb.dbName not defined!" );
			dbName = new String( property );
			
			property = properties.getProperty( "confdb.dbHost" );
			if ( property == null )
				throw new ConverterException( "DbProperties: confdb.dbHost not defined!" );
			dbHost = new String( property );
			
			initURL();
		}
	}
		

	private void initURL()
	{
		dbURL = "jdbc:mysql://" + dbHost + ":3306/" + dbName;
		if ( dbType.equals("oracle") ) {
			//dbURL = "jdbc:oracle:thin:@//" + dbHost + ":10121/" + dbName;
			// Change to make load balancing:
			dbURL = createOracleURL();
		}
	}
	
	// This method creates TNS descriptor according to Oracle Standards.
	// This is to include loadBalancing when there are many hosts in confdb.properties.
	// @see /confdb.db/confDB.java
    public String createOracleURL() {
		String url = "jdbc:oracle:thin:@(DESCRIPTION =";
		String[] hosts = dbHost.split(",");
		
		if(hosts.length == 0) return "ERROR TNSNAME FORMAT";
		
		for(int i = 0; i < hosts.length; i++) {
				url+= "(ADDRESS = (PROTOCOL = TCP)(HOST = "+hosts[i]+")(PORT = 10121))\n";
		}

	    url+="(ENABLE=BROKEN) ";
	    url+="(LOAD_BALANCE = yes) ";
	    url+="(CONNECT_DATA = ";
	    url+="  (SERVER = DEDICATED) ";
	    url+=" (SERVICE_NAME = "+dbName+") ";
	    url+="   (FAILOVER_MODE = (TYPE = SELECT)(METHOD = BASIC)(RETRIES = 200)(DELAY = 15)) ";
	    url+=") )";
		
		return url;
    }
    
	static public DbProperties getDefaultDbProperties() throws IOException, ConverterException
	{
		try {
			return new RcmsDbProperties();
		} catch (Exception e) {
		}
		return new DbProperties( "/conf/confdb.properties" );
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbURL() {
		return dbURL;
	}

	public String getDbPwrd() {
		return dbPwrd;
	}

}

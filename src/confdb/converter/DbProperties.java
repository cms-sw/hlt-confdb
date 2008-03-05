package confdb.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import confdb.db.ConfDBSetups;

public class DbProperties 
{
	String dbURL = null;
	String dbName = null;
	String dbType = null;
	String dbHost = null;
	String dbUser = null;
	String dbPwrd = null;

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
		String property = properties.getProperty( "confdb.dbName" );
		if ( property == null )
			throw new ConverterException( "DbProperties: confdb.dbName not defined!" );
		dbName = new String( property );
		
		property = properties.getProperty( "confdb.dbType" );
		if ( property == null )
			throw new ConverterException( "DbProperties: confdb.dbType not defined!" );
		dbType = new String( property );
		
		property = properties.getProperty( "confdb.dbHost" );
		if ( property == null )
			throw new ConverterException( "DbProperties: confdb.dbHost not defined!" );
		dbHost = new String( property );
		
		property = properties.getProperty( "confdb.dbUser" );
		if ( property == null )
			throw new ConverterException( "DbProperties: confdb.dbUser not defined!" );
		dbUser = new String( property );
		
		property = properties.getProperty( "confdb.dbPwrd" );
		if ( property == null )
			throw new ConverterException( "DbProperties: confdb.dbPwrd not defined!" );
		dbPwrd = new String( property );
		initURL();
	}
		

	private void initURL()
	{
		dbURL = "jdbc:mysql://" + dbHost + ":3306/" + dbName;
		if ( dbType.equals("oracle") )
		    dbURL = "jdbc:oracle:thin:@//" + dbHost + ":10121/" + dbName;
		
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

	public String getDbName() {
		return dbName;
	}

	public String getDbHost() {
		return dbHost;
	}

}

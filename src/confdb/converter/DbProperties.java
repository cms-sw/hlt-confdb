package confdb.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbProperties 
{
	String dbURL = null;
	String dbName = null;
	String dbType = null;
	String dbHost = null;
	String dbUser = null;
	String dbPwrd = null;

	public DbProperties( Properties properties )
	{
		init( properties );
	}
	
	public DbProperties( InputStream stream ) throws IOException
	{
		init( stream );
	}

	public DbProperties( String resource ) throws IOException
	{
		init( getClass().getResourceAsStream( resource ) );
	}

	
	private void init( InputStream stream ) throws IOException
	{
		Properties properties = new Properties();
		properties.load( stream );
		init( properties );
	}

	private void init( Properties properties )
	{
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

		dbURL = "jdbc:mysql://" + dbHost + ":3306/" + dbName;
		if ( dbType.equals("oracle") )
		    dbURL = "jdbc:oracle:thin:@//" + dbHost + ":10121/" + dbName;
		
	}

	static public DbProperties getDefaultDbProperties() throws IOException
	{
		return new DbProperties( "/conf/confdb.properties" );
	}

}

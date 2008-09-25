package confdb.converter;

import java.sql.Connection;

import confdb.data.IConfiguration;
import confdb.db.ConfDB;

public class ConverterBase 
{
    private ConfDB database = null;
    private ConverterEngine converterEngine = null;
	
	
    public ConverterBase( String format, Connection connection ) throws ConverterException
    {
    	database = new ConfDB();
    	try {
    		database.connect( connection );
    		converterEngine = ConverterFactory.getConverterEngine( format );
    	} catch (Exception e) {
    		throw new ConverterException( "can't construct converter", e );
    	}
    }
	
    public ConverterBase( String format, String dbType, String dbUrl, String dbUser, String dbPwrd ) throws ConverterException
    {
    	this( format );
    	initDB( dbType, dbUrl, dbUser, dbPwrd );
    }
  
    protected ConverterBase( String format ) throws ConverterException
    {
		try {
			converterEngine = ConverterFactory.getConverterEngine( format );
		} catch (Exception e) {
    		throw new ConverterException( "can't construct converter", e );
		}
    }
    
    protected void initDB( String dbType, String dbUrl, String dbUser, String dbPwrd ) throws ConverterException
    {
    	database = new ConfDB();
    	try {
    		database.connect( dbType, dbUrl, dbUser, dbPwrd );
    	} catch (Exception e) {
    		throw new ConverterException( "can't init database connection", e );
    	}
    }
  
    
    public ConfDB getDatabase() 
    {
    	return database;
    }
	
    public ConverterEngine getConverterEngine() 
    {
    	return converterEngine;
    }

	public void setConverterEngine(ConverterEngine converterEngine) 
	{
		this.converterEngine = converterEngine;
	}

    public IConfiguration getConfiguration( int key ) throws ConverterException
    {
    	return ConfCache.getCache().getConfiguration( key, getDatabase() );
    }

	

}

package confdb.converter;

import java.sql.Connection;

import java.util.Iterator;

import confdb.data.IConfiguration;
import confdb.data.Path;
import confdb.data.Stream;
import confdb.data.PrimaryDataset;
import confdb.data.PSetParameter;
import confdb.data.VStringParameter;

import confdb.db.ConfDB;
import confdb.db.DatabaseException;

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
    	try {
			return ConfCache.getCache().getConfiguration( key, getDatabase() );
		} catch (DatabaseException e) {
			throw new ConverterException( "DatabaseException", e );
		}
    }

    
    /** add a pset to the passed configuration containing all streams */
    protected void addPSetForStreams(IConfiguration config)
    {
    	if ( config.streamCount() == 0 ) 
    		return;

    	if ( config.pset("streams") != null )
    		return;
    	
    	PSetParameter pset = new PSetParameter("streams","",true);
    	Iterator<Stream> itS = config.streamIterator();
    	while (itS.hasNext()) {
    		Stream stream = itS.next();
    		StringBuffer valueAsString = new StringBuffer();
    		Iterator<PrimaryDataset> itD = stream.datasetIterator();
    		while (itD.hasNext()) {
    			if ( valueAsString.length() > 0 ) 
    				valueAsString.append(",");
    			valueAsString.append(itD.next().name());
    		}
    		pset.addParameter(new VStringParameter(stream.name(),
						   valueAsString.toString(),
						   true));
    	}
    	config.insertPSet(pset);
    }

    /** add pset to the passed configuration containing all datasets */
    protected void addPSetForDatasets(IConfiguration config)
    {
    	if ( config.datasetCount() == 0 ) 
    		return;
	
    	if ( config.pset("datasets") != null )
    		return;
    	
    	PSetParameter pset = new PSetParameter("datasets","",true);
    	Iterator<PrimaryDataset> itD = config.datasetIterator();
    	while ( itD.hasNext() ) 
    	{
    		PrimaryDataset dataset = itD.next();
    		StringBuffer valueAsString = new StringBuffer();
    		Iterator<Path> itP = dataset.pathIterator();
    		while (itP.hasNext()) {
    			if ( valueAsString.length() > 0 ) 
    				valueAsString.append(",");
    			valueAsString.append(itP.next().name());
    		}
    		pset.addParameter(new VStringParameter(dataset.name(),
						   valueAsString.toString(),
						   true));
    	}
    	config.insertPSet(pset);
    }
    
}

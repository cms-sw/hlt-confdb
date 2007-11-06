package confdb.converter;

import confdb.data.IConfiguration;
import confdb.db.ConfDB;

public class ConverterBase 
{
	private ConfDB database = null;
	private IConfiguration configuration = null;
	private ConverterEngine converterEngine = null;
	
	
	protected ConfDB getDatabase() {
		return database;
	}
	protected IConfiguration getConfiguration() {
		return configuration;
	}
	protected ConverterEngine getConverterEngine() {
		return converterEngine;
	}

}

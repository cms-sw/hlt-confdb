package confdb.converter;

import confdb.data.IConfiguration;

public interface IConfigurationWriter extends ConverterEngineSetter 
{
	static enum WriteProcess { YES, NO };
	
	public String toString( IConfiguration configuration, WriteProcess yesNo ) throws ConverterException;
	
}

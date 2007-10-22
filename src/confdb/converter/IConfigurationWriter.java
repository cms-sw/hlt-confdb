package confdb.converter;

import confdb.data.Configuration;

public interface IConfigurationWriter extends ConverterEngineSetter 
{
	static enum WriteProcess { YES, NO };
	
	public String toString( Configuration configuration, WriteProcess yesNo );
	
}

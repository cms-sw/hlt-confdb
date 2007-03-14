package confdb.converter;

import confdb.data.Configuration;

public interface IConfigurationWriter extends ConverterSetter {

	public String toString( Configuration configuration );
	
}

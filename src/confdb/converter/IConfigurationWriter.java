package confdb.converter;

import confdb.data.Configuration;

public interface IConfigurationWriter {

	public String toString( Configuration configuration, Converter converter );
	
}

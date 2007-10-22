package confdb.converter;

import confdb.data.Path;

public interface IPathWriter {
	
	public String toString( Path path, ConverterEngine converterEngine, String indent );

}

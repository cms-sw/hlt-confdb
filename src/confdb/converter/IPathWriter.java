package confdb.converter;

import confdb.data.Path;

public interface IPathWriter {
	
	public String toString( Path path, Converter converter, String indent );

}

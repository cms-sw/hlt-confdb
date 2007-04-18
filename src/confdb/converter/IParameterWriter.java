package confdb.converter;

import confdb.data.Parameter;

public interface IParameterWriter {
	
	public String toString( Parameter parameter, Converter converter, String indent );

}

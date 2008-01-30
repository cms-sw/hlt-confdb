package confdb.converter;

import confdb.data.Parameter;

public interface IParameterWriter {
	
	public String toString( Parameter parameter, ConverterEngine converterEngine, String indent ) throws ConversionException;

}

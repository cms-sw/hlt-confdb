package confdb.converter;

import confdb.data.ESSourceInstance;

public interface IESSourceWriter {

	public String toString( ESSourceInstance essource, ConverterEngine converterEngine, String indent ) throws ConversionException;
	
}

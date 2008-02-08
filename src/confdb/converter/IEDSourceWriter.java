package confdb.converter;

import confdb.data.EDSourceInstance;

public interface IEDSourceWriter {

	public String toString( EDSourceInstance edsource, ConverterEngine converterEngine, String indent ) throws ConverterException;
	
}

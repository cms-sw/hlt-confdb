package confdb.converter;

import confdb.data.ServiceInstance;

public interface IServiceWriter {
	
	public String toString( ServiceInstance service, ConverterEngine converterEngine, String indent ) throws ConversionException;

}

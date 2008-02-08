package confdb.converter.python;

import confdb.converter.ConversionException;
import confdb.converter.ConverterEngine;
import confdb.converter.IServiceWriter;
import confdb.data.ServiceInstance;


public class PythonServiceWriter extends PythonInstanceWriter implements IServiceWriter 
{
	public String toString( ServiceInstance service, ConverterEngine converterEngine, String indent ) throws ConversionException 
	{
		return toString( "service", service, converterEngine, indent);
	}

}

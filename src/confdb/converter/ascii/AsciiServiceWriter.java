package confdb.converter.ascii;

import confdb.converter.Converter;
import confdb.converter.IServiceWriter;
import confdb.data.ServiceInstance;


public class AsciiServiceWriter extends AsciiInstanceWriter implements IServiceWriter 
{
	public String toString( ServiceInstance service, Converter converter ) 
	{
		return toString( "service", service, converter);
	}

}

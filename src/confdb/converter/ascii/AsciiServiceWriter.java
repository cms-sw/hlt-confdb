package confdb.converter.ascii;

import confdb.converter.Converter;
import confdb.converter.IServiceWriter;
import confdb.converter.InstanceWriter;
import confdb.data.ServiceInstance;


public class AsciiServiceWriter extends InstanceWriter implements IServiceWriter 
{
	public String toString( ServiceInstance service, Converter converter ) 
	{
		return toString( "service", service, converter);
	}

}

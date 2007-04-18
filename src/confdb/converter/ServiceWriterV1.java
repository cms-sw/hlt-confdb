package confdb.converter;

import confdb.data.ServiceInstance;


public class ServiceWriterV1 extends InstanceWriter implements IServiceWriter 
{
	public String toString( ServiceInstance service, Converter converter ) 
	{
		return toString( "service", service, converter);
	}

}

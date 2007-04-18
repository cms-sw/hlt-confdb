package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IServiceWriter;
import confdb.data.ServiceInstance;


public class HtmlServiceWriter extends HtmlInstanceWriter implements IServiceWriter 
{
	
	public String toString( ServiceInstance service, Converter converter ) 
	{
		return toString( "service", service, converter );
	}

}

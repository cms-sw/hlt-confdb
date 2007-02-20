package confdb.converter;

import confdb.data.Parameter;
import confdb.data.ServiceInstance;


public class ServiceWriterV1 implements IServiceWriter {

	public String toString( ServiceInstance service, Converter converter ) 
	{
		IParameterWriter parameterWriter = converter.getParameterWriter();
		
		String str = "service = " + service.name() + " {";
		if ( service.parameterCount() > 0 )
			str += converter.getNewline();
		for ( int i = 0; i < service.parameterCount(); i++ )
		{
			Parameter parameter = service.parameter(i);
			str += " " + parameterWriter.toString( parameter, converter );
		}
		str += "}" + converter.getNewline();
		return str;
	}

}

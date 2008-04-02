package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IParameterWriter;
import confdb.converter.IServiceWriter;
import confdb.data.Parameter;
import confdb.data.ServiceInstance;


public class PythonServiceWriter implements IServiceWriter 
{
	public String toString( ServiceInstance instance, ConverterEngine converterEngine, String indent ) throws ConverterException 
	{
		if ( instance == null )
			return "";

		IParameterWriter parameterWriter = converterEngine.getParameterWriter();
		
		StringBuffer str = new StringBuffer( instance.name() + " = cms.Service( \""
		    + instance.name() + "\"," );
			
		str.append( converterEngine.getNewline() );
		for ( int i = 0; i < instance.parameterCount(); i++ )
		{
			Parameter parameter = instance.parameter(i);
			String para = parameterWriter.toString( parameter, converterEngine, indent + "  " );
			if ( para.length() > 0 )
			{
				if ( i < instance.parameterCount() - 1 )
					PythonFormatter.addComma( str, para );
				else
					str.append( para );
			}
		}

		str.append( ")\n" );

		return str.toString();
	}



}

package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IParameterWriter;
import confdb.data.EDSourceInstance;
import confdb.data.Parameter;

public class PythonEDSourceWriter implements IEDSourceWriter 
{
	
	public String toString( EDSourceInstance instance, ConverterEngine converterEngine, String indent ) throws ConverterException 
	{
		if ( instance == null )
				return "";

		IParameterWriter parameterWriter = converterEngine.getParameterWriter();
			
		StringBuffer str = new StringBuffer( "source = cms.Source( \""
			    + instance.name() + "\"," );
				
		str.append( converterEngine.getNewline() );
		for ( int i = 0; i < instance.parameterCount(); i++ )
		{
			Parameter parameter = instance.parameter(i);
			String para = parameterWriter.toString( parameter, converterEngine, indent + "  " );
			if ( para.length() > 0 )
				PythonFormatter.addComma( str, para );
		}
		PythonFormatter.removeComma( str );
		str.append( ")\n" );

		return str.toString();
		}
}

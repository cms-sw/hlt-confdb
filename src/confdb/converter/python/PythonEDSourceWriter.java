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
			
		StringBuffer str = new StringBuffer( "process.source = cms.Source( \""
			    + instance.name() + "\"," );
				
		str.append( converterEngine.getNewline() );
		for ( int i = 0; i < instance.parameterCount(); i++ )
		{
			Parameter parameter = instance.parameter(i);
			String para = parameterWriter.toString( parameter, converterEngine, indent + "  " );
			if ( para.length() > 0 )
			{
				if ( i < instance.parameterCount() - 1 )
				{
					if ( para.endsWith( "\n" ) )
					{
						str.append( para, 0, para.length() - 1 );
						str.append( ",\n" );
					}
					else
					{
						str.append( para );
						str.append( "," );
					}
				}
				else
					str.append( para );
			}
		}
		str.append( ")\n" );

		return str.toString();
		}
}

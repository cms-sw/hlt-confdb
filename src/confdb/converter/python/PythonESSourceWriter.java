package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IESSourceWriter;
import confdb.converter.IParameterWriter;
import confdb.data.ESSourceInstance;
import confdb.data.Parameter;

public class PythonESSourceWriter implements IESSourceWriter 
{
    public String toString( ESSourceInstance module, ConverterEngine converterEngine, String indent) throws ConverterException 
	{
    	IParameterWriter parameterWriter = converterEngine.getParameterWriter();
		
		String name = module.name();
		String className = module.template().name();
		
		StringBuffer str = new StringBuffer( name + " = cms.ESSource( \"" + className + "\"" );
		if ( module.parameterCount() == 0 )
		{
			str.append( " )\n" );
			return str.toString();
		}
			
		str.append( ",\n" );
		for ( int i = 0; i < module.parameterCount(); i++ )
		{
			Parameter parameter = module.parameter(i);
			String param = parameterWriter.toString( parameter, converterEngine, indent + "  " );
			if ( param.length() > 0 )
				PythonFormatter.addComma( str, param );
		}
		str.append( ")\n" );
		return str.toString();
	}

}

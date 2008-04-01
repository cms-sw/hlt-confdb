package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IESModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.data.ESModuleInstance;
import confdb.data.Parameter;

public class PythonESModuleWriter extends PythonModuleWriter implements IESModuleWriter 
{
    public String toString( ESModuleInstance module, ConverterEngine converterEngine, String indent) throws ConverterException 
	{
    	IParameterWriter parameterWriter = converterEngine.getParameterWriter();
		
		String name = module.name();
		String className = module.template().name();
		//String type = module.template().type();
		
		StringBuffer str = new StringBuffer( "process." + name + " = cms.ESProducer( \"" + className + "\"" );
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

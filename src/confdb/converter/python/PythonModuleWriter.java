package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;

public class PythonModuleWriter implements IModuleWriter
{
	private IParameterWriter parameterWriter = null;
	private ConverterEngine converterEngine = null;
	private static final String indent = "  ";

	public String toString( ModuleInstance module ) throws ConverterException 
	{
		if ( parameterWriter == null )
			parameterWriter = converterEngine.getParameterWriter();
		
		String name = module.name();
		String className = module.template().name();
		String type = module.template().type();
		if ( type.equals( "HLTFilter" ) )
			type = "EDFilter";
		
		StringBuffer str = new StringBuffer( name + " = cms." + type + "( \"" + className + "\"" );
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
			if (  param.length() > 0 )
				PythonFormatter.addComma( str, param );
		}
		PythonFormatter.removeComma( str );
		str.append( ")\n" );
		return str.toString();
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}


	protected String decorate( String name )
	{
		return name;
	}


	
}

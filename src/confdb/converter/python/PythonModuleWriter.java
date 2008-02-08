package confdb.converter.python;

import confdb.converter.ConversionException;
import confdb.converter.ConverterEngine;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;

public class PythonModuleWriter implements IModuleWriter {
	
	private IParameterWriter parameterWriter = null;
	private ConverterEngine converterEngine = null;
	private static final String indent = "  ";

	public String toString( ModuleInstance module ) throws ConversionException 
	{
		if ( parameterWriter == null )
			parameterWriter = converterEngine.getParameterWriter();
		
		String name = module.name();
		String type = module.template().name();
		
		StringBuffer str = new StringBuffer( indent + "'" +  decorate( name ) + "' : Module( '" + type + "', {" );
		if ( module.parameterCount() == 0 )
		{
			str.append( "  })" );
			return str.toString();
		}
			
		str.append( "\n" );
		for ( int i = 0; i < module.parameterCount(); i++ )
		{
			Parameter parameter = module.parameter(i);
			String param = parameterWriter.toString( parameter, converterEngine, indent + "  " );
			if ( param.length() > 0 )
			{
				str.append( param );
				str.append( "," );
			}
		}
		str.append( indent + "  })" );
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

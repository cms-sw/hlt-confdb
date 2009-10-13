package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IOutputWriter;
import confdb.converter.IParameterWriter;

import confdb.data.OutputModule;
import confdb.data.Parameter;

public class PythonOutputWriter implements IOutputWriter
{
	private IParameterWriter parameterWriter = null;
	private ConverterEngine converterEngine = null;
	private static final String indent = "  ";

	public String toString( OutputModule output ) throws ConverterException 
	{
		if ( parameterWriter == null )
			parameterWriter = converterEngine.getParameterWriter();
		
		String name = output.name();
		String className = output.className();
		String type = "OutputModule";
		
		StringBuffer str = new StringBuffer( name + " = cms." + type + "( \"" + className + "\"" );
		if ( output.parameterCount() == 0 )
		{
			str.append( " )\n" );
			return str.toString();
		}
			
		str.append( ",\n" );
		for ( int i = 0; i < output.parameterCount(); i++ )
		{
			Parameter parameter = output.parameter(i);
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

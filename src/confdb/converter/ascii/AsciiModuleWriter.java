package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;

public class AsciiModuleWriter implements IModuleWriter {
	
	private IParameterWriter parameterWriter = null;
	private ConverterEngine converterEngine = null;
	private static final String indent = "  ";

	public String toString( ModuleInstance module ) throws ConverterException 
	{
		if ( parameterWriter == null )
			parameterWriter = converterEngine.getParameterWriter();
		
		String name = module.name();
		String type = module.template().name();
		
		String str = indent + "module " +  decorate( name ) + " = " + type + " {";
		if ( module.parameterCount() == 0 )
			return str + "}" + converterEngine.getNewline();
			
		str += converterEngine.getNewline();
		for ( int i = 0; i < module.parameterCount(); i++ )
		{
			Parameter parameter = module.parameter(i);
			str += parameterWriter.toString( parameter, converterEngine, indent + "  " );
		}
		str += indent + "}" + converterEngine.getNewline();
		return str;
	}


	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}


	protected String decorate( String name )
	{
		return name;
	}


	
}

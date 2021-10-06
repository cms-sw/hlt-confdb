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
		
		StringBuffer str = new StringBuffer( indent + "module " +  decorate( name ) + " = " );
		appendType( str, module );
		str.append( " {" );
		if ( module.parameterCount() == 0 )
			return str.toString() + "}" + converterEngine.getNewline();
			
		str.append( converterEngine.getNewline() );
		for ( int i = 0; i < module.parameterCount(); i++ )
		{
			Parameter parameter = module.parameter(i);
			str.append( parameterWriter.toString( parameter, converterEngine, indent + "  " ) );
		}
		str.append( indent + "}" + converterEngine.getNewline() );
		return str.toString();
	}


	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}

	protected String decorate( String name )
	{
		return name;
	}

	protected void appendType( StringBuffer str, ModuleInstance module )
	{
		str.append( module.template().name() );
	}
	
}

package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IOutputWriter;
import confdb.converter.IParameterWriter;

import confdb.data.OutputModule;
import confdb.data.Parameter;

public class AsciiOutputWriter implements IOutputWriter {
	
	private IParameterWriter parameterWriter = null;
	private ConverterEngine converterEngine = null;
	private static final String indent = "  ";

	public String toString( OutputModule output ) throws ConverterException 
	{
		if ( parameterWriter == null )
			parameterWriter = converterEngine.getParameterWriter();
		
		String name = output.name();
		
		StringBuffer str = new StringBuffer( indent + "module " +  decorate( name ) + " = " );
		appendType( str, output );
		str.append( " {" );
		if ( output.parameterCount() == 0 )
			return str.toString() + "}" + converterEngine.getNewline();
			
		str.append( converterEngine.getNewline() );
		for ( int i = 0; i < output.parameterCount(); i++ )
		{
			Parameter parameter = output.parameter(i);
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

	protected void appendType( StringBuffer str, OutputModule output )
	{
	        str.append( output.className() );
	}
	
}

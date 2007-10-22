package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.IParameterWriter;
import confdb.data.Instance;
import confdb.data.Parameter;

public class AsciiInstanceWriter {

	private static final String indent = "  ";

	private IParameterWriter parameterWriter = null;
	
	protected String toString(String type,Instance instance,ConverterEngine converterEngine)
        {
		if ( parameterWriter == null )
			parameterWriter = converterEngine.getParameterWriter();
		
		boolean needInstanceLabel = !(type.equals("source")||
					      type.equals("service"));

		String str = (needInstanceLabel)
		    ?
		    indent + type + " " +
		    instance.name() + " = " +
		    instance.template().name() + " {"
		    :
		    indent + type + " = " + instance.template().name() + " {";

		
		if ( instance.parameterCount() == 0 )
			return str + "}" + converterEngine.getNewline();
			
		str += converterEngine.getNewline();
		for ( int i = 0; i < instance.parameterCount(); i++ )
		{
			Parameter parameter = instance.parameter(i);
			str += parameterWriter.toString( parameter, converterEngine, indent + "  " );
		}
		str += indent + "}" + converterEngine.getNewline();
		return str;
	}


	
}

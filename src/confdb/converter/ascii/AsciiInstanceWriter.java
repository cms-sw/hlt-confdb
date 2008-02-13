package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IParameterWriter;
import confdb.data.Instance;
import confdb.data.ESPreferable;
import confdb.data.Parameter;

public class AsciiInstanceWriter 
{
	private IParameterWriter parameterWriter = null;
	
	protected String toString( String type, Instance instance, ConverterEngine converterEngine, String indent ) throws ConverterException
	{
		if ( parameterWriter == null )
			parameterWriter = converterEngine.getParameterWriter();
		
		boolean needInstanceLabel = !(type.equals("source")||
					      type.equals("service"));

		StringBuffer str = new StringBuffer( (needInstanceLabel)
		    ?
		    indent + type + " " +
		    instance.name() + " = " +
		    instance.template().name() + " {"
		    :
		    indent + type + " = " + instance.template().name() + " {" );

		
		if ( instance.parameterCount() == 0 )
			return str + "}" + converterEngine.getNewline();
			
		str.append( converterEngine.getNewline() );
		for ( int i = 0; i < instance.parameterCount(); i++ )
		{
			Parameter parameter = instance.parameter(i);
			str.append( parameterWriter.toString( parameter, converterEngine, indent + "  " ) );
		}
		str.append( indent + "}" + converterEngine.getNewline() );


		// quick fix by PS 11/02/07
		if (instance instanceof ESPreferable) {
		    ESPreferable esp = (ESPreferable)instance;
		    if (esp.isPreferred()) {
			str.append(indent + "es_prefer " +
				   instance.name() + " = " +
				   instance.template().name() + " {}" +
				   converterEngine.getNewline());
		    }
		}
		// end quick fix
		    
		return str.toString();
	}


	
}

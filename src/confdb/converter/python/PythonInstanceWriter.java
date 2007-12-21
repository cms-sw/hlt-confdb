package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.IParameterWriter;
import confdb.data.Instance;
import confdb.data.ESPreferable;
import confdb.data.Parameter;

public class PythonInstanceWriter 
{
	private IParameterWriter parameterWriter = null;
	
	protected String toString( String type, Instance instance, ConverterEngine converterEngine, String indent )
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
		    indent + "'" + instance.template().name() + "' : {" );

		
		if ( instance.parameterCount() == 0 )
			return str + "}" + converterEngine.getNewline();
			
		str.append( converterEngine.getNewline() );
		for ( int i = 0; i < instance.parameterCount(); i++ )
		{
			Parameter parameter = instance.parameter(i);
			String para = parameterWriter.toString( parameter, converterEngine, indent + "  " );
			if ( para.length() > 0 )
			{
				str.append( para );
				str.append( "," );
			}
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

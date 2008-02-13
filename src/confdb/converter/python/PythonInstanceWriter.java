package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IParameterWriter;
import confdb.data.Instance;
import confdb.data.Parameter;

public class PythonInstanceWriter 
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
		    indent + "'" + instance.name() + "' : " + type + "( '"
		    + instance.template().name() + "', {"
		    :
		    indent + "'" + instance.template().name() + "' : {" );

			
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

		if ( instance.parameterCount() > 0 )
			str.append( indent );
		
		str.append( "}" );
		if ( needInstanceLabel )
			str.append( ")"  );

		return str.toString();
	}


	
}

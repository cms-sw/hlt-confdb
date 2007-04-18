package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IParameterWriter;
import confdb.data.Instance;
import confdb.data.Parameter;

public class HtmlInstanceWriter {

	private IParameterWriter parameterWriter = null;
	
	protected String toString( String type, Instance instance, Converter converter ) 
	{
		if ( parameterWriter == null )
			parameterWriter = converter.getParameterWriter();
		
		String str = "<tr><td>"+ type + "</td><td>=</td><td>" + instance.name() + "</td></tr>\n";
		if ( instance.parameterCount() == 0 )
			return str;
			
		//str += "<tr>";
		for ( int i = 0; i < instance.parameterCount(); i++ )
		{
			Parameter parameter = instance.parameter(i);
			str += parameterWriter.toString( parameter, converter, "  " );
		}
		return str;
	}


	
}

package confdb.converter;

import confdb.data.ESSourceInstance;
import confdb.data.Parameter;



public class ESSourceWriterV1 implements IESSourceWriter {

	static final String header = "service = ";

	private IParameterWriter parameterWriter = null;

	
	public String toString( ESSourceInstance essource, Converter converter ) 
	{
		if ( parameterWriter == null )
			parameterWriter = converter.getParameterWriter();
		
		String str = header + essource.name() + "{";
		for ( int i = 0; i < essource.parameterCount(); i++ )
		{
			Parameter parameter = essource.parameter(i);
			str += parameterWriter.toString( parameter, converter );
		}
		str += converter.getNewline() + "}" + converter.getNewline();
		return str;
	}

}

package confdb.converter;

import confdb.data.EDSourceInstance;
import confdb.data.Parameter;



public class EDSourceWriterV1 implements IEDSourceWriter {

	private IParameterWriter parameterWriter = null;

	
	public String toString( EDSourceInstance edsource, Converter converter ) 
	{
		if ( parameterWriter == null )
			parameterWriter = converter.getParameterWriter();
		
		String str = "source = " + edsource.name() + " {";
		if ( edsource.parameterCount() > 0 )
			str += converter.getNewline();
		for ( int i = 0; i < edsource.parameterCount(); i++ )
		{
			Parameter parameter = edsource.parameter(i);
			str += " " + parameterWriter.toString( parameter, converter );
		}
		str += "}" + converter.getNewline();
		return str;
	}

}

package confdb.converter;

import confdb.data.ModuleInstance;
import confdb.data.Parameter;


public class ModuleWriterV1  implements IModuleWriter {

	public String toString( ModuleInstance module, Converter converter ) 
	{
		IParameterWriter parameterWriter = converter.getParameterWriter();

		String str = "module " + module.name() + " = " + module.template().name() + " {";
		if ( module.parameterCount() > 0 )
			str += converter.getNewline();
		for ( int i = 0; i < module.parameterCount(); i++ )
		{
			Parameter parameter = module.parameter(i);
			str += " " + parameterWriter.toString( parameter, converter );
		}
		str += "}" + converter.getNewline();
		return str;
	}

}

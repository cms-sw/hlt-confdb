package confdb.converter;

import confdb.data.ModuleInstance;
import confdb.data.Parameter;


public class ModuleWriterV1  implements IModuleWriter {
	
	protected Converter converter = null;
	protected IParameterWriter parameterWriter = null;

	public String toString( ModuleInstance module ) 
	{
		String str = "module " + module.name() + " = " + module.template().name() + " {";
		if ( module.parameterCount() > 0 )
			str += Converter.getAsciiNewline();
		for ( int i = 0; i < module.parameterCount(); i++ )
		{
			Parameter parameter = module.parameter(i);
			str += " " + parameterWriter.toString( parameter, converter );
		}
		str += "}" + Converter.getAsciiNewline();
		return str;
	}


	public void setConverter(Converter converter) {
		this.converter = converter;
		this.parameterWriter = converter.getParameterWriter();
	}

}

package confdb.converter;

import confdb.data.ModuleInstance;
import confdb.data.Parameter;

public class ModuleWriterV1 implements IModuleWriter {
	
	private IParameterWriter parameterWriter = null;
	private Converter converter = null;
	private static final String indent = "  ";

	public String toString( ModuleInstance module ) 
	{
		if ( parameterWriter == null )
			parameterWriter = converter.getParameterWriter();
		
		String name = module.name();
		String type = module.template().name();
		
		if (     module.template().instanceCount() == 1 
		     &&  name.equals( type )  )
			name = "";
		
		String str = indent + "module " +  name + " = " + type + " {";
		if ( module.parameterCount() == 0 )
			return str + "}" + converter.getNewline();
			
		str += converter.getNewline();
		for ( int i = 0; i < module.parameterCount(); i++ )
		{
			Parameter parameter = module.parameter(i);
			str += parameterWriter.toString( parameter, converter, indent + "  " );
		}
		str += indent + "}" + converter.getNewline();
		return str;
	}


	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	
}

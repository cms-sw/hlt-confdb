package confdb.converter;

import confdb.data.PSetParameter;
import confdb.data.Parameter;
import confdb.data.ScalarParameter;
import confdb.data.VPSetParameter;
import confdb.data.VectorParameter;

public class ParameterWriterV1  implements IParameterWriter {

	public String toString( Parameter parameter, Converter converter ) 
	{
		String str = (parameter.isTracked() ? "" : "untracked " )
			 + parameter.type() + " " + parameter.name() + " = ";
		
		if ( parameter instanceof ScalarParameter )
			str += parameter.valueAsString();
		else if ( parameter instanceof PSetParameter )
		{
			str += "{ "; 
			PSetParameter pset = (PSetParameter)parameter;
			for ( int i = 0; i < pset.parameterCount(); i++ )
				str += toString( (Parameter)pset.parameter(i), converter );
			str += " }"; 
		}
		else if ( parameter instanceof VectorParameter )
		{
			str += "{ "; 
			if ( parameter instanceof VPSetParameter )
			{
				VPSetParameter vpset = (VPSetParameter)parameter;
				for ( int i = 0; i < vpset.parameterSetCount(); i++ )
					str += toString( (Parameter)vpset.parameterSet(i), converter );
			}
			else
				str += parameter.valueAsString(); 
			str += " }"; 
		}
		
		str += converter.getNewline();
		return str;
	}

}

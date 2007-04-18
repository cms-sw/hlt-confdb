package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IParameterWriter;
import confdb.data.PSetParameter;
import confdb.data.Parameter;
import confdb.data.ScalarParameter;
import confdb.data.VPSetParameter;
import confdb.data.VectorParameter;

public class HtmlParameterWriter  implements IParameterWriter {

	public String toString( Parameter parameter, Converter converter, String indent ) 
	{
		String str = "<tr>";
		for ( int i = 0; i < indent.length(); i++ )
			str += "<td></td>";
		str += "<td>" + (parameter.isTracked() ? "" : "untracked " )
			 + parameter.type() + "</td><td>" + parameter.name() + "</td><td>=</td>";
		
		if ( parameter instanceof ScalarParameter )
			str += "<td>" + parameter.valueAsString() + "</td>";
		else if ( parameter instanceof PSetParameter )
		{
			PSetParameter pset = (PSetParameter)parameter;
			if ( pset.parameterCount() > 0 )
			{
				str += "</tr>\n"; 
				for ( int i = 0; i < pset.parameterCount(); i++ )
					str += toString( (Parameter)pset.parameter(i), converter, indent + " " );
			}
		}
		else if ( parameter instanceof VectorParameter )
		{
			if ( parameter instanceof VPSetParameter )
			{
				VPSetParameter vpset = (VPSetParameter)parameter;
				for ( int i = 0; i < vpset.parameterSetCount(); i++ )
					str += toString( (Parameter)vpset.parameterSet(i), converter, indent + " " );
			}
			else
				str += "<td>" + parameter.valueAsString() + "</td>"; 
		}
		
		str += "</tr>\n";
		return str;
	}

}

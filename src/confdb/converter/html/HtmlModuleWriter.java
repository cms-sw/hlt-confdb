package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;

public class HtmlModuleWriter implements IModuleWriter {
	
	protected Converter converter = null;
	private IParameterWriter parameterWriter = null;
	

	public String toString( ModuleInstance instance ) 
	{
		if ( parameterWriter == null )
			parameterWriter = converter.getParameterWriter();

		String name = instance.name();
		String type = instance.template().name();
		
		if (     instance.template().instanceCount() == 1 
		     &&  name.equals( type )  )
			name = "";
		
		String str = "<tr><td>module</td>";
		if ( name.length() != 0 )
			str += "<td>" + name + "</td>";
		str += "<td align=\"center\">=</td><td>" + type + "</td></tr>\n";
		if ( instance.parameterCount() == 0 )
			return str;
			
		str += "<tr><td></td><td colspan=\"5\"><table>";
		for ( int i = 0; i < instance.parameterCount(); i++ )
		{
			Parameter parameter = instance.parameter(i);
			str += parameterWriter.toString( parameter, converter, "  " );
		}
		str += "</table></td></tr>";
		return str;
	}



	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	
}

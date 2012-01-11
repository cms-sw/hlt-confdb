package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.data.Parameter;

public class PythonUntrackedParameterWriter extends PythonParameterWriter
{
	static private boolean first = true;

	public String toString( Parameter parameter, ConverterEngine converterEngine, String indent ) throws ConverterException 
	{
		if ( first )
		{
			first = false;
			System.out.println( getClass().getName() );
		}
		this.converterEngine = converterEngine;
		// if ( !parameter.isTracked() && parameter.isDefault() ) return "";

		return toString( parameter, indent );
	}

}

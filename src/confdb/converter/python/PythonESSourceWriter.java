package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.IESSourceWriter;
import confdb.data.ESSourceInstance;

public class PythonESSourceWriter extends PythonInstanceWriter implements IESSourceWriter 
{
	public String toString( ESSourceInstance essource, ConverterEngine converterEngine, String indent ) 
	{
		return toString( "ESSource", essource, converterEngine, indent );
	}

}

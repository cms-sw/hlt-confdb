package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IEDSourceWriter;
import confdb.data.EDSourceInstance;


public class AsciiEDSourceWriter extends AsciiInstanceWriter implements IEDSourceWriter 
{
	
	public String toString( EDSourceInstance edsource, ConverterEngine converterEngine, String indent ) throws ConverterException 
	{
		if ( edsource == null )
			return "";
		return toString( "source", edsource, converterEngine, indent );
	}

}

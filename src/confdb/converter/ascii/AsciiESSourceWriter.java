package confdb.converter.ascii;

import confdb.converter.Converter;
import confdb.converter.IESSourceWriter;
import confdb.data.ESSourceInstance;

public class AsciiESSourceWriter extends AsciiInstanceWriter implements IESSourceWriter 
{
	public String toString( ESSourceInstance essource, Converter converter ) 
	{
		return toString( "es_source", essource, converter );
	}

}

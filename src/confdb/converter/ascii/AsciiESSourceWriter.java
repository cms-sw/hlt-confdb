package confdb.converter.ascii;

import confdb.converter.Converter;
import confdb.converter.IESSourceWriter;
import confdb.converter.InstanceWriter;
import confdb.data.ESSourceInstance;

public class AsciiESSourceWriter extends InstanceWriter implements IESSourceWriter 
{
	public String toString( ESSourceInstance essource, Converter converter ) 
	{
		return toString( "service", essource, converter );
	}

}

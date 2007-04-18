package confdb.converter;

import confdb.data.ESSourceInstance;

public class ESSourceWriterV1 extends InstanceWriter implements IESSourceWriter 
{
	public String toString( ESSourceInstance essource, Converter converter ) 
	{
		return toString( "service", essource, converter );
	}

}

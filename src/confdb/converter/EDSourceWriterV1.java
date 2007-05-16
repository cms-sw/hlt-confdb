package confdb.converter;

import confdb.data.EDSourceInstance;


public class EDSourceWriterV1 extends InstanceWriter implements IEDSourceWriter 
{
	
	public String toString( EDSourceInstance edsource, Converter converter ) 
	{
		if ( edsource == null )
			return "";
		return toString( "source", edsource, converter );
	}

}

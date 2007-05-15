package confdb.converter;

import confdb.data.EDSourceInstance;

public class EDSourceOverrider implements IEDSourceWriter 
{
	private EDSourceInstance edsource;
	private IEDSourceWriter writer;
	
	public EDSourceOverrider( EDSourceInstance edsource, IEDSourceWriter writer )
	{
		this.edsource = edsource;
		this.writer = writer;
	}
	
	
	public String toString(EDSourceInstance dontUse, Converter converter) 
	{
		return writer.toString( edsource, converter );
	}

}

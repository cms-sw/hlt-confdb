package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IEDSourceWriter;
import confdb.data.EDSourceInstance;


public class HtmlEDSourceWriter extends HtmlInstanceWriter implements IEDSourceWriter 
{
	
	public String toString( EDSourceInstance edsource, Converter converter ) 
	{
		return toString( "source", edsource, converter );
	}

}

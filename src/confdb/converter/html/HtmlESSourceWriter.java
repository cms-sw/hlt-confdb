package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IESSourceWriter;
import confdb.data.ESSourceInstance;


public class HtmlESSourceWriter extends HtmlInstanceWriter implements IESSourceWriter 
{
	
	public String toString( ESSourceInstance essource, Converter converter ) 
	{
		return toString( "es_source", essource, converter );
	}

}

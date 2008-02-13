package confdb.converter.html;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IESSourceWriter;
import confdb.data.ESSourceInstance;


public class HtmlESSourceWriter extends HtmlInstanceWriter implements IESSourceWriter 
{
	
	public String toString( ESSourceInstance essource, ConverterEngine converterEngine, String indent ) throws ConverterException 
	{
		return toString( "es_source", essource, converterEngine, indent );
	}

}

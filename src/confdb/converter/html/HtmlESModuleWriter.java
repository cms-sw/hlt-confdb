package confdb.converter.html;

import confdb.converter.ConversionException;
import confdb.converter.ConverterEngine;
import confdb.converter.IESModuleWriter;
import confdb.data.ESModuleInstance;


public class HtmlESModuleWriter extends HtmlInstanceWriter implements IESModuleWriter 
{
	
	public String toString( ESModuleInstance esmodule, ConverterEngine converterEngine, String indent ) throws ConversionException 
	{
		return toString( "es_module", esmodule, converterEngine, indent );
	}

}

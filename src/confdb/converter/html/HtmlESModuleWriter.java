package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IESModuleWriter;
import confdb.data.ESModuleInstance;


public class HtmlESModuleWriter extends HtmlInstanceWriter implements IESModuleWriter 
{
	
	public String toString( ESModuleInstance esmodule, Converter converter ) 
	{
		return toString( "es_module", esmodule, converter );
	}

}

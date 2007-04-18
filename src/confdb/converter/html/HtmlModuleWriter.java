package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IModuleWriter;
import confdb.data.ModuleInstance;

public class HtmlModuleWriter  extends HtmlInstanceWriter implements IModuleWriter {
	
	protected Converter converter = null;

	public String toString( ModuleInstance module ) 
	{
		return toString( "module", module, converter );
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

}

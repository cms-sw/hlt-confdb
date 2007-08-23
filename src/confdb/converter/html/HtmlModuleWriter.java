package confdb.converter.html;

import confdb.converter.IModuleWriter;
import confdb.converter.ascii.AsciiModuleWriter;


public class HtmlModuleWriter extends AsciiModuleWriter implements IModuleWriter 
{
	
	protected String decorate( String name )
	{
		return "<a name=\"" + name + "\"><b>" + name + "</b></a>";
	}
	
}

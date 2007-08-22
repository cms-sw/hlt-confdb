package confdb.converter.html;

import confdb.converter.IPathWriter;
import confdb.converter.ascii.AsciiPathWriter;

public class HtmlPathWriter extends AsciiPathWriter implements IPathWriter 
{

	protected String decorate( String moduleName )
	{
		return "<a href=\"#M_" + moduleName + "\">" + moduleName + "</a>";
	}
	
}

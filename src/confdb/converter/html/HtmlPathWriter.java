package confdb.converter.html;

import confdb.converter.IPathWriter;
import confdb.converter.ascii.AsciiPathWriter;

public class HtmlPathWriter extends AsciiPathWriter implements IPathWriter 
{

	protected String decorate( String moduleName )
	{
		return "<a href=\"#" + moduleName + "\">" + moduleName + "</a>";
	}

	protected String decorateName( String pathName )
	{
		return "<a name=\"" + pathName + "\"><b>" + pathName + "</b></a>";
	}
	
}

package confdb.converter.html;

import confdb.converter.ISequenceWriter;
import confdb.converter.ascii.AsciiSequenceWriter;

public class HtmlSequenceWriter extends AsciiSequenceWriter implements ISequenceWriter 
{
	protected String decorate( String moduleName )
	{
		return "<a href=\"#" + moduleName + "\">" + moduleName + "</a>";
	}
	
	protected String decorateName( String sequenceName )
	{
		return "<a name=\"" + sequenceName + "\"><b>" + sequenceName + "</b></a>";
	}
}

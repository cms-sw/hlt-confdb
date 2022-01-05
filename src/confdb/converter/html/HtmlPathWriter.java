package confdb.converter.html;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.IPathWriter;
import confdb.data.Path;
import confdb.data.Reference;


public class HtmlPathWriter implements IPathWriter 
{
	public String toString( Path path, ConverterEngine converterEngine, String indent ) 
	{
		String str = indent;
		if ( path.isEndPath() )
			str += "endpath ";
		else if (path.isFinalPath() )
			str += "finalpath ";
		else
			str += "path ";
		str += decorateName( path.name() ) + " = ";
		if ( path.entryCount() > 0 )
		{
			String sep =  " + ";
			Iterator<Reference> list = path.entryIterator();
			str += decorate( list.next() );
			while ( list.hasNext() )
				str += sep + decorate( list.next() );
		}
		str += converterEngine.getNewline();
		return str;
	}


	protected String decorate( Reference reference )
	{
		return "<a href=\"#" + reference.name() + "\">" + reference.getOperatorAndName() + "</a>";
	}

	protected String decorateName( String pathName )
	{
		return "<a name=\"" + pathName + "\"><b>" + pathName + "</b></a>";
	}
	
}

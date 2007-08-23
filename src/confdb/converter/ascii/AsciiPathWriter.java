package confdb.converter.ascii;

import confdb.converter.Converter;
import confdb.converter.IPathWriter;
import confdb.data.Path;

public class AsciiPathWriter implements IPathWriter 
{
	public String toString( Path path, Converter converter, String indent ) 
	{
		String str = indent;
		if ( path.isEndPath() )
			str += "endpath ";
		else
			str += "path ";
		str += decorateName( path.name() ) + " = { ";
		for ( int i = 0; i < path.entryCount(); i++  )
		{
			str += decorate( path.entry(i).name() );
			if ( i + 1 < path.entryCount() )
				str += " & ";
		}
		str += " }" + converter.getNewline();
		return str;
	}

	
	protected String decorate( String name )
	{
		return name;
	}
	
	
	protected String decorateName( String name )
	{
		return name;
	}
	
}

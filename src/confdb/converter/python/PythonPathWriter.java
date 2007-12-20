package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.IPathWriter;
import confdb.data.Path;

public class PythonPathWriter implements IPathWriter 
{
	public String toString( Path path, ConverterEngine converterEngine, String indent ) 
	{
		String str = "";
		if ( path.isEndPath() )
			str = indent + "'"+ path.name() +  "' : EndPath( "; 
		else
			str = indent + "'"+ path.name() +  "' : Path( "; 
		for ( int i = 0; i < path.entryCount(); i++  )
		{
			str += decorate( path.entry(i).name() );
			if ( i + 1 < path.entryCount() )
				str += ", ";
		}
		str += " )";
		return str;
	}
	
	protected String decorateName( String name )
	{
		return name;
	}

	protected String decorate( String name )
	{
		return "'" + name + "'";
	}

}

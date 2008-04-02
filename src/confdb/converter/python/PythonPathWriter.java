package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.IPathWriter;
import confdb.data.Path;

public class PythonPathWriter implements IPathWriter 
{
	public String toString( Path path, ConverterEngine converterEngine, String object ) 
	{
		String str = "";
		if ( path.isEndPath() )
			str = object + path.name() +  " = cms.EndPath( "; 
		else
			str = object + path.name() +  " = cms.Path( "; 

		for ( int i = 0; i < path.entryCount(); i++  )
		{
			str += object + path.entry(i).name();
			if ( i + 1 < path.entryCount() )
				str += " + ";
		}
		str += " )\n";
		return str;
	}

}

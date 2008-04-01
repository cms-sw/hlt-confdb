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
			str = "process." + path.name() +  " = cms.EndPath( "; 
		else
			str = "process." + path.name() +  " = cms.Path( "; 

		for ( int i = 0; i < path.entryCount(); i++  )
		{
			str += "process." + path.entry(i).name();
			if ( i + 1 < path.entryCount() )
				str += " + ";
		}
		str += " )\n";
		return str;
	}

}

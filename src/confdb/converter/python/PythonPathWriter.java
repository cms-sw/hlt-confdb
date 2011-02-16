package confdb.converter.python;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.IPathWriter;
import confdb.data.Path;
import confdb.data.Reference;

public class PythonPathWriter implements IPathWriter 
{
	public String toString( Path path, ConverterEngine converterEngine, String object ) 
	{
		String str = "";
		if ( path.isEndPath() )
			str = object + path.name() +  " = cms.EndPath( "; 
		else
			str = object + path.name() +  " = cms.Path( "; 

		if ( path.entryCount() > 0 )
		{
			String sep =  " + ";
			Iterator<Reference> list = path.entryIterator();
			str += list.next().getPythonCode(object);
			while ( list.hasNext() )
				str += sep + list.next().getPythonCode(object);
		}
		
		str += " )\n";
		return str;
	}

}

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
			Iterator<Reference> list = path.entryIterator();
			Reference entry = list.next();
			str += object + entry.name();
			while ( list.hasNext() )
			{
				entry = list.next();
				str += entry.getOperator().getPythonHeader() + object + entry.name() + entry.getOperator().getPythonTrailer();
			}
		}
		
		str += " )\n";
		return str;
	}

}

package confdb.converter.python;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ISequenceWriter;
import confdb.data.Reference;
import confdb.data.Sequence;

public class PythonSequenceWriter implements ISequenceWriter 
{
	public String toString( Sequence sequence, ConverterEngine converterEngine, String object ) 
	{
		String str = object + sequence.name() +  " = cms.Sequence( ";
		if ( sequence.entryCount() > 0 )
		{
			String sep =  " + ";
			Iterator<Reference> list = sequence.entryIterator();
			str += list.next().getPythonCode(object);
			while ( list.hasNext() )
				str += sep + list.next().getPythonCode(object);
		}
		str += " )\n";
		return str;
	}

}

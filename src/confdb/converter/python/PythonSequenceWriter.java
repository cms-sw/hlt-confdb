package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ISequenceWriter;
import confdb.data.Sequence;

public class PythonSequenceWriter implements ISequenceWriter 
{
	public String toString( Sequence sequence, ConverterEngine converterEngine, String indent ) 
	{
		String str = "process." + sequence.name() +  " = cms.Sequence( "; 
		for ( int i = 0; i < sequence.entryCount(); i++  )
		{
			str += "process." + sequence.entry(i).name();
			if ( i + 1 < sequence.entryCount() )
				str += " + ";
		}
		str += " )\n";
		return str;
	}

}

package confdb.converter;

import confdb.data.Sequence;

public class SequenceWriterV1 implements ISequenceWriter {

	private static final String indent = "  ";

	public String toString( Sequence sequence, Converter converter ) 
	{
		String str = indent + "sequence " + sequence.name() + " = { ";
		for ( int i = 0; i < sequence.entryCount(); i++  )
		{
			str += sequence.entry(i).name();
			if ( i + 1 < sequence.entryCount() )
				str += ", ";
		}
		str += " }" + converter.getNewline();
		return str;
	}

}

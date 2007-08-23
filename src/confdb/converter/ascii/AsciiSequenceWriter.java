package confdb.converter.ascii;

import confdb.converter.Converter;
import confdb.converter.ISequenceWriter;
import confdb.data.Sequence;

public class AsciiSequenceWriter implements ISequenceWriter {

	private static final String indent = "  ";

	public String toString( Sequence sequence, Converter converter ) 
	{
		String str = indent + "sequence " + decorateName( sequence.name()) 
			       + " = { ";
		for ( int i = 0; i < sequence.entryCount(); i++  )
		{
			str += decorate( sequence.entry(i).name() );
			if ( i + 1 < sequence.entryCount() )
				str += " & ";
		}
		str += " }" + converter.getNewline();
		return str;
	}
	
	protected String decorateName( String name )
	{
		return name;
	}

	protected String decorate( String name )
	{
		return name;
	}

}

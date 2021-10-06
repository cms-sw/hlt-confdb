package confdb.converter.html;

import confdb.converter.ConverterEngine;
import confdb.converter.ISequenceWriter;
import confdb.converter.ascii.AsciiSequenceWriter;
import confdb.data.Sequence;

public class HtmlSequenceWriter implements ISequenceWriter 
{
	public String toString( Sequence sequence, ConverterEngine converterEngine, String indent ) 
	{
		String str = indent + "sequence " + decorateName( sequence.name()) 
			       + " = ";
		for ( int i = 0; i < sequence.entryCount(); i++  )
		{
			str += decorate( sequence.entry(i).name() );
			if ( i + 1 < sequence.entryCount() )
				str += " + ";
		}
		str += converterEngine.getNewline();
		return str;
	}
	

	protected String decorate( String entryName )
	{
		return "<a href=\"#" + entryName + "\">" + entryName + "</a>";
	}
	
	protected String decorateName( String sequenceName )
	{
		return "<a name=\"" + sequenceName + "\"><b>" + sequenceName + "</b></a>";
	}
}

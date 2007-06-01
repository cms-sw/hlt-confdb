package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.ISequenceWriter;
import confdb.data.Sequence;

public class HtmlSequenceWriter implements ISequenceWriter {

	private static final String indent = "  ";

	public String toString( Sequence sequence, Converter converter ) 
	{
		String str = "<tr>";
		for ( int i = 0; i < indent.length(); i++ )
			str += "<td></td>";
		str += "<td>";
		str += sequence.name() + "</td><td align=\"center\">=</td>";
		for ( int i = 0; i < sequence.entryCount(); i++  )
		{
			str += "<td>" + sequence.entry(i).name() + "</td>";
		}
		str += "</tr>\n";
		return str;
	}


}

package confdb.converter.html;

import confdb.converter.IParameterWriter;
import confdb.converter.ascii.AsciiParameterWriter;
import confdb.data.VStringParameter;
import confdb.data.VectorParameter;

public class HtmlParameterWriter extends AsciiParameterWriter implements IParameterWriter 
{

	protected void appendVector( StringBuffer str, VectorParameter vector )
	{
		if ( !(vector instanceof VStringParameter) || vector.vectorSize() < 2 )
			str.append( "{ " + vector.valueAsString() + " }" ); 
		else
		{
			str.append( "{ " );
			for ( int i = 0; i < vector.vectorSize(); i++ )
			{
				Object para = vector.value(i);
				str.append( "\"" + para.toString() + "\",\n" + getIndent() + "    " );
			}
			str.setLength( str.length() - 2 );
			str.setCharAt( str.lastIndexOf( "," ), ' ' );
			str.append( "}" );
		}
		
		
	}
	
}
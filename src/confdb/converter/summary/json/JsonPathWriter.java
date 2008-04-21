package confdb.converter.summary.json;

import confdb.converter.ConverterEngine;
import confdb.converter.IPathWriter;
import confdb.data.Path;

public class JsonPathWriter extends JsonWriter implements IPathWriter 
{
	public String toString( Path path, ConverterEngine converterEngine, String indent ) 
	{
		str = new StringBuffer( 100 );
		startObject();
		add( "trigger", path.name() );
		add( "l1Seed", "blabla" );
		add( "prescale", 1 );
		startArray( "filters" );
		for ( int i = 0; i < 2; i++ )
		{
			startObject();
			add( "name", "blabla" );
			add( "PTmin", 10 );
			add( "etaMax", 1 );
			closeObject();
		}
		closeArray();
		closeObject();
		str.append( "\n" );
		return str.toString();
	}

}

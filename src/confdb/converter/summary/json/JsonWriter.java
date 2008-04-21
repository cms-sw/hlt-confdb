package confdb.converter.summary.json;

public class JsonWriter
{
	protected StringBuffer str = new StringBuffer();

	protected void startObject()
	{
		str.append( '{' );
	}

	protected void closeObject()
	{
		str.append( "}," );
	}

	protected void add( String key, String value )
	{
		str.append( '"' + key + '"' + ':' );
		str.append( '"' + value + "\"," );
	}

	protected void add( String key, double value )
	{
		str.append( '"' + key + '"' + ':' );
		str.append( value + "," );
	}

	protected void startArray( String name )
	{
		str.append( '"' + name + '"' + ":[" );
	}

	protected void closeArray()
	{
		str.append( ']' );
	}

}

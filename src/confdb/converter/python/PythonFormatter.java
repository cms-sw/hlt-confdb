package confdb.converter.python;

public class PythonFormatter 
{
	public static void addComma( StringBuffer dest, String line )
	{
		if ( line.endsWith( "\n" ) )
		{
			dest.append( line, 0, line.length() - 1 );
			dest.append( ",\n" );
		}
		else
		{
			dest.append( line );
			dest.append( "," );
		}
	}

}

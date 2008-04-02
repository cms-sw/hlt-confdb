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

	public static void removeComma( StringBuffer str )
	{
		if ( str.charAt( str.length() - 1 ) == ',' )
			str.setLength( str.length() - 1 );
		else if (    str.charAt( str.length() - 1 ) == '\n' 
			      && str.charAt( str.length() - 2 ) == ','  )
		{
			str.setCharAt( str.length() - 2, '\n' );
			str.setLength( str.length() - 1 );
		}
	}

}

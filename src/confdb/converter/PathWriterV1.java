package confdb.converter;

import confdb.data.Path;

public class PathWriterV1 implements IPathWriter {

	

	public String toString( Path path, Converter converter, String indent ) 
	{
		String str = indent;
		if ( path.isEndPath() )
			str += "endpath ";
		else
			str += "path ";
		str += path.name() + " = { ";
		for ( int i = 0; i < path.entryCount(); i++  )
		{
			str += path.entry(i).name();
			if ( i + 1 < path.entryCount() )
				str += " & ";
		}
		str += " }" + converter.getNewline();
		return str;
	}

}

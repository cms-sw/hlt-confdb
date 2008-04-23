package confdb.converter.summary.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IPathWriter;
import confdb.data.IConfiguration;
import confdb.data.Path;

public class AsciiConfigurationWriter implements IConfigurationWriter 
{
	static public int maxLineWidth = 150;
	static public String rowSeparator = null;
	protected ConverterEngine converterEngine = null;

	public String toString( IConfiguration conf, WriteProcess writeProcess  ) throws ConverterException
	{
		if ( rowSeparator == null )
		{
			StringBuffer line = new StringBuffer( maxLineWidth + 1 );
			for ( int i = 0; i < maxLineWidth; i++ )
				line.append( '-' );
			line.append( '\n' );
			rowSeparator = line.toString();
		}
		
		StringBuffer str = new StringBuffer( 10000 );
		str.append( rowSeparator );
		IPathWriter pathWriter = converterEngine.getPathWriter();
		for ( int i = 0; i < conf.pathCount(); i++ )
		{
			Path path = conf.path(i);
			str.append( pathWriter.toString( path, converterEngine, "" ) );
			str.append( rowSeparator );
		}
		return str.toString();
	}

	public void setConverterEngine( ConverterEngine converterEngine ) 
	{
		this.converterEngine = converterEngine;
	}
	
}

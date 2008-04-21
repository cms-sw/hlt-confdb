package confdb.converter.summary.json;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IPathWriter;
import confdb.data.IConfiguration;
import confdb.data.Path;

public class JsonConfigurationWriter implements IConfigurationWriter 
{
	protected ConverterEngine converterEngine = null;

	public String toString( IConfiguration conf, WriteProcess writeProcess  ) throws ConverterException
	{
		StringBuffer str = new StringBuffer( 10000 );
		str.append( "{\"rows\":[\n" );
		IPathWriter pathWriter = converterEngine.getPathWriter();
		for ( int i = 0; i < conf.pathCount(); i++ )
		{
			Path path = conf.path(i);
			str.append( pathWriter.toString( path, converterEngine, "" ) );
		}
		str.append( "]}\n" );
		return str.toString();
	}

	public void setConverterEngine( ConverterEngine converterEngine ) 
	{
		this.converterEngine = converterEngine;
	}
	
}

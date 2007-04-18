package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.IServiceWriter;
import confdb.data.Configuration;
import confdb.data.EDSourceInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ModuleInstance;
import confdb.data.Path;
import confdb.data.Sequence;
import confdb.data.ServiceInstance;

public class HtmlConfigurationWriter implements IConfigurationWriter 
{
	protected Converter converter = null;

	public String toString( Configuration conf )
	{
		String str = "<table>\n";
		IPathWriter pathWriter = converter.getPathWriter();
		for ( int i = 0; i < conf.pathCount(); i++ )
		{
			Path path = conf.path(i);
			str += pathWriter.toString( path, converter, "" );
		}

		ISequenceWriter sequenceWriter = converter.getSequenceWriter();
		for ( int i = 0; i < conf.sequenceCount(); i++ )
		{
			Sequence sequence = conf.sequence(i);
			str += sequenceWriter.toString(sequence, converter );
		}


		IEDSourceWriter edsourceWriter = converter.getEDSourceWriter();
		for ( int i = 0; i < conf.edsourceCount(); i++ )
		{
			EDSourceInstance edsource = conf.edsource(i);
			str += edsourceWriter.toString(edsource, converter );
		}

		IESSourceWriter essourceWriter = converter.getESSourceWriter();
		for ( int i = 0; i < conf.essourceCount(); i++ )
		{
			ESSourceInstance essource = conf.essource(i);
			str += essourceWriter.toString(essource, converter);
		}


		IServiceWriter serviceWriter = converter.getServiceWriter();
		for ( int i = 0; i < conf.serviceCount(); i++ )
		{
			ServiceInstance service = conf.service(i);
			str += serviceWriter.toString( service, converter );
		}

		IModuleWriter moduleWriter = converter.getModuleWriter();
		for ( int i = 0; i < conf.moduleCount(); i++ )
		{
			ModuleInstance module = conf.module(i);
			str += moduleWriter.toString( module );
		}

		str += "</table>";
		return str;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}
	
}

package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.IServiceWriter;
import confdb.data.Configuration;
import confdb.data.EDSourceInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ModuleInstance;
import confdb.data.Path;
import confdb.data.Sequence;
import confdb.data.ServiceInstance;

public class HtmlConfigurationWriter implements IConfigurationWriter 
{
	protected Converter converter = null;

	public String toString( Configuration conf )
	{
		String str = "<table><th><td colspan=\"5\"><b>" + conf.name() + " V" + conf.version() + "</td></th>\n";
		str += "<tr><td colspan=\"2\"><b>Paths</td></tr>\n";
		IPathWriter pathWriter = converter.getPathWriter();
		for ( int i = 0; i < conf.pathCount(); i++ )
		{
			Path path = conf.path(i);
			str += pathWriter.toString( path, converter, "" );
		}

		str += "<tr></tr>\n";
		if ( conf.sequenceCount() > 0 )
			str += "<tr><td colspan=\"2\"><b>Sequences</td></tr>\n";
		ISequenceWriter sequenceWriter = converter.getSequenceWriter();
		for ( int i = 0; i < conf.sequenceCount(); i++ )
		{
			Sequence sequence = conf.sequence(i);
			str += sequenceWriter.toString(sequence, converter );
		}


		str += "<tr></tr>\n";
		if ( conf.edsourceCount() > 0 )
			str += "<tr><td colspan=\"2\"><b>Sources</td></tr>\n";
		IEDSourceWriter edsourceWriter = converter.getEDSourceWriter();
		for ( int i = 0; i < conf.edsourceCount(); i++ )
		{
			EDSourceInstance edsource = conf.edsource(i);
			str += edsourceWriter.toString(edsource, converter );
		}


		str += "<tr></tr>\n";
		IESSourceWriter essourceWriter = converter.getESSourceWriter();
		for ( int i = 0; i < conf.essourceCount(); i++ )
		{
			ESSourceInstance essource = conf.essource(i);
			str += essourceWriter.toString(essource, converter);
		}


		str += "<tr></tr>\n";
		IESModuleWriter esmoduleWriter = converter.getESModuleWriter();
		for ( int i = 0; i < conf.esmoduleCount(); i++ )
		{
			ESModuleInstance esmodule = conf.esmodule(i);
			str += esmoduleWriter.toString(esmodule, converter);
		}


		str += "<tr></tr>\n";
		if ( conf.serviceCount() > 0 )
			str += "<tr><td colspan=\"2\"><b>Services</td></tr>\n";
		IServiceWriter serviceWriter = converter.getServiceWriter();
		for ( int i = 0; i < conf.serviceCount(); i++ )
		{
			ServiceInstance service = conf.service(i);
			str += serviceWriter.toString( service, converter );
		}

		str += "<tr></tr>\n";
		if ( conf.moduleCount() > 0 )
			str += "<tr><td colspan=\"2\"><b>Modules</td></tr>\n";
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

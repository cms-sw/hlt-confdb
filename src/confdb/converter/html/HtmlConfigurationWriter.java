package confdb.converter.html;

import confdb.converter.ConverterEngine;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.IServiceWriter;
import confdb.data.IConfiguration;
import confdb.data.EDSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;
import confdb.data.Path;
import confdb.data.Sequence;
import confdb.data.ServiceInstance;


public class HtmlConfigurationWriter implements IConfigurationWriter 
{
	protected ConverterEngine converterEngine = null;
	private static final String spaces = "                                                   ";

	public String toString( IConfiguration conf, WriteProcess writeProcess )
	{
		String str = "";
		if ( writeProcess == WriteProcess.YES )
			str = "process <b>" + conf.processName() + "</b> = {" + converterEngine.getNewline();

		str += converterEngine.getNewline(); 

		if ( conf.psetCount() > 0 )
		{
			str += "<a name=\"psets\"></a>"; 
			IParameterWriter parameterWriter = converterEngine.getParameterWriter();
			for ( int i = 0; i < conf.psetCount(); i++ )
			{
				Parameter pset = conf.pset(i);
				str += parameterWriter.toString( pset, converterEngine, "  " );
			}
			str += converterEngine.getNewline(); 
		}

		if ( conf.pathCount() > 0 )
		{
			str += "<a name=\"paths\"></a>";
			IPathWriter pathWriter = converterEngine.getPathWriter();
			for ( int i = 0; i < conf.pathCount(); i++ )
			{
				Path path = conf.path(i);
				str += wrapLine( pathWriter.toString(path, converterEngine, "  "),
							     '&', 12 + path.name().length() );
			}
			if ( conf.sequenceCount() == 0 )
				str += converterEngine.getNewline();
		}

		
		if ( conf.sequenceCount() > 0 )
		{
			str += "<a name=\"sequences\"></a>" + converterEngine.getNewline(); 
			ISequenceWriter sequenceWriter = converterEngine.getSequenceWriter();
			for ( int i = 0; i < conf.sequenceCount(); i++ )
			{
				Sequence sequence = conf.sequence(i);
				str += wrapLine( sequenceWriter.toString(sequence, converterEngine ),
								 '&', 16 + sequence.name().length() );
			}
			str += converterEngine.getNewline(); 
		}

		
		if ( conf.moduleCount() > 0 )
		{
			str += "<a name=\"modules\"></a>"; 
			IModuleWriter moduleWriter = converterEngine.getModuleWriter();
			for ( int i = 0; i < conf.moduleCount(); i++ )
			{
				ModuleInstance module = conf.module(i);
				str += moduleWriter.toString( module );
			}
			str += converterEngine.getNewline(); 
		}

		if ( conf.edsourceCount() > 0 )
			str += "<a name=\"ed_sources\"></a>"; 
		IEDSourceWriter edsourceWriter = converterEngine.getEDSourceWriter();
		for ( int i = 0; i < conf.edsourceCount(); i++ )
		{
			EDSourceInstance edsource = conf.edsource(i);
			str += edsourceWriter.toString(edsource, converterEngine );
		}
		if ( conf.edsourceCount() == 0 )  // edsource may be overridden
			str += edsourceWriter.toString( null, converterEngine );
		if ( conf.edsourceCount() > 0 )
			str += converterEngine.getNewline(); 


		if ( conf.essourceCount() > 0 )
		{
			str += "<a name=\"es_sources\"></a>"; 
			IESSourceWriter essourceWriter = converterEngine.getESSourceWriter();
			for ( int i = 0; i < conf.essourceCount(); i++ )
			{
				ESSourceInstance essource = conf.essource(i);
				str += essourceWriter.toString(essource, converterEngine);
			}
			str += converterEngine.getNewline(); 
		}


		if ( conf.esmoduleCount() > 0 )
		{
			str += "<a name=\"es_modules\"></a>"; 
			IESModuleWriter esmoduleWriter = converterEngine.getESModuleWriter();
			for ( int i = 0; i < conf.esmoduleCount(); i++ )
			{
				ESModuleInstance esmodule = conf.esmodule(i);
				str += esmoduleWriter.toString(esmodule,converterEngine);
			}
			str += converterEngine.getNewline(); 
		}

		if ( conf.serviceCount() > 0 )
		{
			str += "<a name=\"services\"></a>"; 
			IServiceWriter serviceWriter = converterEngine.getServiceWriter();
			for ( int i = 0; i < conf.serviceCount(); i++ )
			{
				ServiceInstance service = conf.service(i);
				str += serviceWriter.toString( service, converterEngine );
			}
		}

		if ( writeProcess == WriteProcess.YES )
			str += converterEngine.getConfigurationTrailer();
		return str;
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}
	

	protected String wrapLine( String line, char separator, int nspaces )
	{
		if ( line.length() < converterEngine.getMaxLineLength() )
			return line;
		int split = line.lastIndexOf( separator, converterEngine.getMaxLineLength() );
		if ( split <= 0 )
			return line;
		String spacer = spaces.substring(0, nspaces );

		String firstLine = line.substring( 0, split + 2 );
		String secondLine = wrapLine( spacer + line.substring( split + 2 ), separator, nspaces);
		line = firstLine + converterEngine.getNewline() + secondLine;
		return line;
	}
}

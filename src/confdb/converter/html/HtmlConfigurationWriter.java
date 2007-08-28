package confdb.converter.html;

import confdb.converter.Converter;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.IServiceWriter;
import confdb.data.Configuration;
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
	protected Converter converter = null;

	public String toString( Configuration conf )
	{
		String str = "process <b>" + conf.processName() + "</b> = {" + converter.getNewline();

		str += converter.getNewline(); 

		if ( conf.psetCount() > 0 )
		{
			str += "<a name=\"psets\"></a>"; 
			IParameterWriter parameterWriter = converter.getParameterWriter();
			for ( int i = 0; i < conf.psetCount(); i++ )
			{
				Parameter pset = conf.pset(i);
				str += parameterWriter.toString( pset, converter, "  " );
			}
			str += converter.getNewline(); 
		}

		if ( conf.pathCount() > 0 )
		{
			str += "<a name=\"paths\"></a>"; 
			IPathWriter pathWriter = converter.getPathWriter();
			for ( int i = 0; i < conf.pathCount(); i++ )
			{
				Path path = conf.path(i);
				str += pathWriter.toString( path, converter, "  " );
			}
			str += converter.getNewline(); 
		}

		
		if ( conf.sequenceCount() > 0 )
		{
			str += "<a name=\"sequences\"></a>"; 
			ISequenceWriter sequenceWriter = converter.getSequenceWriter();
			for ( int i = 0; i < conf.sequenceCount(); i++ )
			{
				Sequence sequence = conf.sequence(i);
				str += sequenceWriter.toString(sequence, converter );
			}
			str += converter.getNewline(); 
		}

		
		if ( conf.moduleCount() > 0 )
		{
			str += "<a name=\"modules\"></a>"; 
			IModuleWriter moduleWriter = converter.getModuleWriter();
			for ( int i = 0; i < conf.moduleCount(); i++ )
			{
				ModuleInstance module = conf.module(i);
				str += moduleWriter.toString( module );
			}
			str += converter.getNewline(); 
		}

		if ( conf.edsourceCount() > 0 )
			str += "<a name=\"ed_sources\"></a>"; 
		IEDSourceWriter edsourceWriter = converter.getEDSourceWriter();
		for ( int i = 0; i < conf.edsourceCount(); i++ )
		{
			EDSourceInstance edsource = conf.edsource(i);
			str += edsourceWriter.toString(edsource, converter );
		}
		if ( conf.edsourceCount() == 0 )  // edsource may be overridden
			str += edsourceWriter.toString( null, converter );
		if ( conf.edsourceCount() > 0 )
			str += converter.getNewline(); 


		if ( conf.essourceCount() > 0 )
		{
			str += "<a name=\"es_sources\"></a>"; 
			IESSourceWriter essourceWriter = converter.getESSourceWriter();
			for ( int i = 0; i < conf.essourceCount(); i++ )
			{
				ESSourceInstance essource = conf.essource(i);
				str += essourceWriter.toString(essource, converter);
			}
			str += converter.getNewline(); 
		}


		if ( conf.esmoduleCount() > 0 )
		{
			str += "<a name=\"es_modules\"></a>"; 
			IESModuleWriter esmoduleWriter = converter.getESModuleWriter();
			for ( int i = 0; i < conf.esmoduleCount(); i++ )
			{
				ESModuleInstance esmodule = conf.esmodule(i);
				str += esmoduleWriter.toString(esmodule,converter);
			}
			str += converter.getNewline(); 
		}

		if ( conf.serviceCount() > 0 )
		{
			str += "<a name=\"services\"></a>"; 
			IServiceWriter serviceWriter = converter.getServiceWriter();
			for ( int i = 0; i < conf.serviceCount(); i++ )
			{
				ServiceInstance service = conf.service(i);
				str += serviceWriter.toString( service, converter );
			}
		}

		str += converter.getConfigurationTrailer();
		return str;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}
	

}

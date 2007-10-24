package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.IServiceWriter;
import confdb.data.IConfiguration;
import confdb.data.EDSourceInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;
import confdb.data.Path;
import confdb.data.Sequence;
import confdb.data.ServiceInstance;

public class AsciiConfigurationWriter implements IConfigurationWriter 
{
	protected ConverterEngine converterEngine = null;

	public String toString( IConfiguration conf, WriteProcess writeProcess  )
	{
		String str = "// " + conf.name() + " V" + conf.version()
		+ " (" + conf.releaseTag() + ")" + converterEngine.getNewline() + converterEngine.getNewline();

		if ( writeProcess == WriteProcess.YES )
			str += "process " + conf.processName() + " = {" + converterEngine.getNewline();

		ISequenceWriter sequenceWriter = converterEngine.getSequenceWriter();
		for ( int i = 0; i < conf.sequenceCount(); i++ )
		{
			Sequence sequence = conf.sequence(i);
			str += sequenceWriter.toString(sequence, converterEngine );
		}

		IPathWriter pathWriter = converterEngine.getPathWriter();
		for ( int i = 0; i < conf.pathCount(); i++ )
		{
			Path path = conf.path(i);
			str += pathWriter.toString( path, converterEngine, "  " );
		}

		IParameterWriter parameterWriter = converterEngine.getParameterWriter();
		for ( int i = 0; i < conf.psetCount(); i++ )
		{
			Parameter pset = conf.pset(i);
			str += parameterWriter.toString( pset, converterEngine, "  " );
		}


		IEDSourceWriter edsourceWriter = converterEngine.getEDSourceWriter();
		for ( int i = 0; i < conf.edsourceCount(); i++ )
		{
			EDSourceInstance edsource = conf.edsource(i);
			str += edsourceWriter.toString(edsource, converterEngine );
		}
		if ( conf.edsourceCount() == 0 )  // edsource may be overridden
			str += edsourceWriter.toString( null, converterEngine );

		IESSourceWriter essourceWriter = converterEngine.getESSourceWriter();
		for ( int i = 0; i < conf.essourceCount(); i++ )
		{
			ESSourceInstance essource = conf.essource(i);
			str += essourceWriter.toString(essource, converterEngine);
		}


		IESModuleWriter esmoduleWriter = converterEngine.getESModuleWriter();
		for ( int i = 0; i < conf.esmoduleCount(); i++ )
		{
			ESModuleInstance esmodule = conf.esmodule(i);
			str += esmoduleWriter.toString(esmodule,converterEngine);
		}


		IServiceWriter serviceWriter = converterEngine.getServiceWriter();
		for ( int i = 0; i < conf.serviceCount(); i++ )
		{
			ServiceInstance service = conf.service(i);
			str += serviceWriter.toString( service, converterEngine );
		}

		IModuleWriter moduleWriter = converterEngine.getModuleWriter();
		for ( int i = 0; i < conf.moduleCount(); i++ )
		{
			ModuleInstance module = conf.module(i);
			str += moduleWriter.toString( module );
		}

		if ( writeProcess == WriteProcess.YES )
			str += converterEngine.getConfigurationTrailer();
		return str;
	}

	public void setConverterEngine( ConverterEngine converterEngine ) 
	{
		this.converterEngine = converterEngine;
	}
	
}

package confdb.converter.ascii;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDAliasWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IOutputWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.IServiceWriter;
import confdb.converter.ISwitchProducerWriter;
import confdb.converter.ITaskWriter;
import confdb.data.Block;
import confdb.data.EDAliasInstance;
import confdb.data.IConfiguration;
import confdb.data.EDSourceInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ModuleInstance;
import confdb.data.OutputModule;
import confdb.data.Parameter;
import confdb.data.Path;
import confdb.data.Sequence;
import confdb.data.ServiceInstance;
import confdb.data.SwitchProducer;
import confdb.data.Task;

public class AsciiConfigurationWriter implements IConfigurationWriter 
{
	protected ConverterEngine converterEngine = null;

	public String toString( IConfiguration conf, WriteProcess writeProcess  ) throws ConverterException
	{
		String indent = "  ";
		StringBuffer str = new StringBuffer( 100000 );
		//String fullName = conf.parentDir().name() + "/" + conf.name() + "/V" + conf.version() ;		
		String fullName = conf.toString();
		str.append( "// " + fullName
  		   + "  (" + conf.releaseTag() + ")" + converterEngine.getNewline() + converterEngine.getNewline() );

		if ( writeProcess == WriteProcess.YES )
			str.append( "process " + conf.processName() + " = {" + converterEngine.getNewline() );
		else
			indent = "";

		ISequenceWriter sequenceWriter = converterEngine.getSequenceWriter();
		for ( int i = 0; i < conf.sequenceCount(); i++ )
		{
			Sequence sequence = conf.sequence(i);
			str.append( sequenceWriter.toString(sequence, converterEngine, indent ) );
		}
		
		ITaskWriter taskWriter = converterEngine.getTaskWriter();
		for ( int i = 0; i < conf.taskCount(); i++ )
		{
			Task task = conf.task(i);
			str.append(taskWriter.toString(task, converterEngine, indent ) );
		}
		
		ISwitchProducerWriter switchProducerWriter = converterEngine.getSwitchProducerWriter();
		for ( int i = 0; i < conf.switchProducerCount(); i++ )
		{
			SwitchProducer switchProducer = conf.switchProducer(i);
			str.append(switchProducerWriter.toString(switchProducer, converterEngine, indent ) );
		}

		IPathWriter pathWriter = converterEngine.getPathWriter();
		for ( int i = 0; i < conf.pathCount(); i++ )
		{
			Path path = conf.path(i);
			str.append( pathWriter.toString( path, converterEngine, indent ) );
		}

		IParameterWriter parameterWriter = converterEngine.getParameterWriter();
		for ( int i = 0; i < conf.psetCount(); i++ )
		{
			Parameter pset = conf.pset(i);
			str.append( parameterWriter.toString( pset, converterEngine, indent ) );
		}

		IEDAliasWriter globalEDAliasWriter = converterEngine.getGlobalEDAliasWriter();
		for ( int i = 0; i < conf.globalEDAliasCount(); i++ )
		{
			EDAliasInstance globalEDAlias = conf.globalEDAlias(i);
			str.append( globalEDAliasWriter.toString( globalEDAlias ) );
		}

		IEDSourceWriter edsourceWriter = converterEngine.getEDSourceWriter();
		for ( int i = 0; i < conf.edsourceCount(); i++ )
		{
			EDSourceInstance edsource = conf.edsource(i);
			str.append( edsourceWriter.toString(edsource, converterEngine, indent ) );
		}

		IESSourceWriter essourceWriter = converterEngine.getESSourceWriter();
		for ( int i = 0; i < conf.essourceCount(); i++ )
		{
			ESSourceInstance essource = conf.essource(i);
			str.append( essourceWriter.toString(essource, converterEngine, indent ) );
		}


		IESModuleWriter esmoduleWriter = converterEngine.getESModuleWriter();
		for ( int i = 0; i < conf.esmoduleCount(); i++ )
		{
			ESModuleInstance esmodule = conf.esmodule(i);
			str.append( esmoduleWriter.toString( esmodule, converterEngine, indent ) );
		}


		IServiceWriter serviceWriter = converterEngine.getServiceWriter();
		for ( int i = 0; i < conf.serviceCount(); i++ )
		{
			ServiceInstance service = conf.service(i);
			str.append( serviceWriter.toString( service, converterEngine, indent ) );
		}

		IModuleWriter moduleWriter = converterEngine.getModuleWriter();
		for ( int i = 0; i < conf.moduleCount(); i++ )
		{
			ModuleInstance module = conf.module(i);
			str.append( moduleWriter.toString( module ) );
		}
		
		IEDAliasWriter edAliasWriter = converterEngine.getEDAliasWriter();
		for ( int i = 0; i < conf.edAliasCount(); i++ )
		{
			EDAliasInstance edAlias = conf.edAlias(i);
			str.append( edAliasWriter.toString( edAlias ) );
		}

		IOutputWriter outputWriter = converterEngine.getOutputWriter();
		for ( int i = 0; i < conf.outputCount(); i++ )
		{
			OutputModule output = conf.output(i);
			str.append( outputWriter.toString( output ) );
		}

		Iterator<Block> blockIterator = conf.blockIterator();
		while ( blockIterator.hasNext() )
		{
			Block block = blockIterator.next();
			str.append( indent + "block " + block.name() + " = {\n" );
			Iterator<Parameter> parameterIterator = block.parameterIterator();
			while ( parameterIterator.hasNext() )
			{
				str.append( parameterWriter.toString( parameterIterator.next(), converterEngine, indent + "  " ) );
			}
			str.append( indent + "}\n" );
		}

		if ( writeProcess == WriteProcess.YES )
			str.append( converterEngine.getConfigurationTrailer() );
		return str.toString();
	}

	public void setConverterEngine( ConverterEngine converterEngine ) 
	{
		this.converterEngine = converterEngine;
	}
	
}

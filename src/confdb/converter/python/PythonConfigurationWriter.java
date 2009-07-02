package confdb.converter.python;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.IServiceWriter;
import confdb.data.Block;
import confdb.data.ESPreferable;
import confdb.data.IConfiguration;
import confdb.data.EDSourceInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;
import confdb.data.Path;
import confdb.data.Sequence;
import confdb.data.ServiceInstance;

public class PythonConfigurationWriter implements IConfigurationWriter 
{
	protected ConverterEngine converterEngine = null;

	public String toString( IConfiguration conf, WriteProcess writeProcess  ) throws ConverterException
	{
		String indent = "  ";
		StringBuffer str = new StringBuffer( 100000 );
		//String fullName = conf.parentDir().name() + "/" + conf.name() + "/V" + conf.version() ;		
		String fullName = conf.toString();
		str.append( "# " + fullName
  		   + " (" + conf.releaseTag() + ")" + converterEngine.getNewline() + converterEngine.getNewline() );

		str.append( "import FWCore.ParameterSet.Config as cms\n\n" );

		String object = "";
		if ( writeProcess == WriteProcess.YES )
		{
			object = "process.";
			str.append( "process = cms.Process( \"" + 
					conf.processName() + "\" )\n" );
		}
		else
			indent = "";

		str.append( "\n" + object + "HLTConfigVersion = cms.PSet(\n  tableName = cms.string('" + fullName + "')\n)\n\n" );
		
		if ( conf.psetCount() > 0 )
		{
			IParameterWriter parameterWriter = converterEngine.getParameterWriter();
			for ( int i = 0; i < conf.psetCount(); i++ )
			{
				Parameter pset = conf.pset(i);
				str.append( object + parameterWriter.toString( pset, converterEngine, "" ) );
			}
			str.append( "\n");
		}


		if ( conf.edsourceCount() > 0 )
		{
			IEDSourceWriter edsourceWriter = converterEngine.getEDSourceWriter();
			for ( int i = 0; i < conf.edsourceCount(); i++ )
			{
				EDSourceInstance edsource = conf.edsource(i);
				str.append( object );
				str.append( edsourceWriter.toString(edsource, converterEngine, indent ) );
			}
			str.append( "\n" );
		}

		if ( conf.essourceCount() > 0 )
		{
			IESSourceWriter essourceWriter = converterEngine.getESSourceWriter();
			for ( int i = 0; i < conf.essourceCount(); i++ )
			{
				ESSourceInstance essource = conf.essource(i);
				str.append( object );
				str.append( essourceWriter.toString(essource, converterEngine, indent ) );
			}

			for ( int i = 0; i < conf.essourceCount(); i++ )
			{
				ESSourceInstance instance = conf.essource(i);
				if ( instance instanceof ESPreferable) 
				{
					ESPreferable esp = (ESPreferable)instance;
					if ( esp.isPreferred() ) 
					{
						str.append( object );
						str.append( "es_prefer_" + instance.name() + " = cms.ESPrefer( \""
								+ instance.template().name() + "\", \"" + instance.name() + "\" )\n" ); 
					}
				}
			}
			str.append( "\n");
		}

		if ( conf.esmoduleCount() > 0 )
		{
			IESModuleWriter esmoduleWriter = converterEngine.getESModuleWriter();
			for ( int i = 0; i < conf.esmoduleCount(); i++ )
			{
				ESModuleInstance esmodule = conf.esmodule(i);
				str.append( object );
				str.append( esmoduleWriter.toString( esmodule, converterEngine, "" ) );
				if ( esmodule.isPreferred() ) 
				{
					str.append( object );
					str.append( "es_prefer_" + esmodule.name() + " = cms.ESPrefer( \""
							+ esmodule.template().name() + "\", \"" + esmodule.name() + "\" )\n" ); 
				}
			}
			
			str.append( "\n");
		}
		

		if ( conf.serviceCount() > 0 )
		{
			IServiceWriter serviceWriter = converterEngine.getServiceWriter();
			for ( int i = 0; i < conf.serviceCount(); i++ )
			{
				ServiceInstance service = conf.service(i);
				str.append( object );
				str.append( serviceWriter.toString( service, converterEngine, indent ) );
			}
			str.append( "\n");
		}

		
		if ( conf.moduleCount() > 0 )
		{
			IModuleWriter moduleWriter = converterEngine.getModuleWriter();
			for ( int i = 0; i < conf.moduleCount(); i++ )
			{
				ModuleInstance module = conf.module(i);
				str.append( object );
				str.append( moduleWriter.toString( module ) );
			}
			str.append( "\n");
		}

		if ( conf.sequenceCount() > 0 )
		{
			ISequenceWriter sequenceWriter = converterEngine.getSequenceWriter();
			Iterator<Sequence> sequenceIterator = conf.orderedSequenceIterator();
			while ( sequenceIterator.hasNext() )
			{
				Sequence sequence = sequenceIterator.next();
				str.append( sequenceWriter.toString(sequence, converterEngine, object ) );
			}
			str.append( "\n");
		}

		if ( conf.pathCount() > 0 )
		{
			IPathWriter pathWriter = converterEngine.getPathWriter();
			for ( int i = 0; i < conf.pathCount(); i++ )
			{
				Path path = conf.path(i);
				str.append( pathWriter.toString( path, converterEngine, object ) );
			}
			str.append( "\n");
		}

		IParameterWriter parameterWriter = converterEngine.getParameterWriter();
		Iterator<Block> blockIterator = conf.blockIterator();
		while ( blockIterator.hasNext() )
		{
			Block block = blockIterator.next();
			str.append( block.name() + " = cms.PSet(\n" );
			Iterator<Parameter> parameterIterator = block.parameterIterator();
			while ( parameterIterator.hasNext() )
			{
				str.append( parameterWriter.toString( parameterIterator.next(), converterEngine, indent ) );
			}
			str.append( ")\n" );
		}

		if ( writeProcess == WriteProcess.YES )
		{
		}
		else
		{
			if ( conf.pathCount() > 0 )
			{
				str.append( "\nHLTSchedule = cms.Schedule( " );
				for ( int i = 0; i < conf.pathCount(); i++ )
				{
					Path path = conf.path(i);
					str.append( path.name() + ", " );
				}
				int length = str.length();
				str.setCharAt( length - 2, ' ' );
				str.setLength( length - 1 );
				str.append( ")\n" );
			}
		}

		return str.toString();
	}

	public void setConverterEngine( ConverterEngine converterEngine ) 
	{
		this.converterEngine = converterEngine;
	}
	
}

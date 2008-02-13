package confdb.converter.python;

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
	static public String myPythonClasses =  
		"class Process:\n" +
		"  pass\n" +
		"class Path(tuple):\n" +
		"  isEndPath = False\n" +
		"  def __new__(cls,*args):\n" +
		"    return tuple.__new__(cls,args)\n" +
		"class EndPath(Path):\n" +
		"  isEndPath = True\n" +
		"class Sequence(tuple):\n" +
		"  def __new__(cls,*args):\n" +
		"    return tuple.__new__(cls,args)\n" +
		"class Parameter:\n" +
		"  isTracked = True\n" +
		"  def __init__(self, type, value ):\n" +
		"    self.type = type\n" +
		"    self.value = value\n" +
		"class UntrackedParameter( Parameter ):\n" +
		"  def __init__(self, type, value ):\n" +
		"    self.isTracked = False\n" +
		"    Parameter.__init__(self, type, value )\n" +
		"class PSet( Parameter, dict ):\n" +
		"  def __init__(self, *args, **kw ):\n" +
		"    dict.__init__(self, *args, **kw)\n" +
		"class UntrackedPSet( PSet):\n" +
		"  def __init__(self, *args, **kw ):\n" +
		"    PSet.__init__(self, *args, **kw)\n" +
		"    self.isTracked = False\n" +
		"class Module( dict ):\n" +
		"  def __init__(self, type, *args, **kw ):\n" +
		"    dict.__init__(self, *args, **kw)\n" +
		"    self.type = type\n" +
		"class ESSource( dict ):\n" +
		"  def __init__(self, type, *args, **kw ):\n" +
		"    dict.__init__(self, *args, **kw)\n" +
		"    self.type = type\n" +
		"class ESModule( dict ):\n" +
		"  def __init__(self, type, *args, **kw ):\n" +
		"    dict.__init__(self, *args, **kw)\n" +
		"    self.type = type\n" +
		"\n";

	public String toString( IConfiguration conf, WriteProcess writeProcess  ) throws ConverterException
	{
		String indent = "  ";
		String object = "";
		StringBuffer str = new StringBuffer( 100000 );
		str.append( "# " + conf.name() + " V" + conf.version()
  		   + " (" + conf.releaseTag() + ")" + converterEngine.getNewline() + converterEngine.getNewline() );

		if ( writeProcess == WriteProcess.YES )
		{
			object = conf.processName();
			str.append( myPythonClasses + object + " = Process()\n" );
		}
		else
			indent = "";

		if ( conf.sequenceCount() > 0 )
		{
			str.append( object + ".sequences = {\n" );
			ISequenceWriter sequenceWriter = converterEngine.getSequenceWriter();
			for ( int i = 0; i < conf.sequenceCount(); i++ )
			{
				Sequence sequence = conf.sequence(i);
				str.append( sequenceWriter.toString(sequence, converterEngine, indent ) );
				str.append( ",\n" );
			}
			str.append( "}\n");
		}

		if ( conf.pathCount() > 0 )
		{
			str.append( object + ".paths = {\n" );
			IPathWriter pathWriter = converterEngine.getPathWriter();
			for ( int i = 0; i < conf.pathCount(); i++ )
			{
				Path path = conf.path(i);
				str.append( pathWriter.toString( path, converterEngine, indent ) );
				str.append( ",\n" );
			}
			str.append( "}\n");
		}

		if ( conf.psetCount() > 0 )
		{
			str.append( object + ".psets = {\n" );
			IParameterWriter parameterWriter = converterEngine.getParameterWriter();
			for ( int i = 0; i < conf.psetCount(); i++ )
			{
				Parameter pset = conf.pset(i);
				str.append( parameterWriter.toString( pset, converterEngine, indent ) );
				str.append( ",\n" );
			}
			str.append( "}\n");
		}

		if ( conf.edsourceCount() > 0 )
		{
			str.append( object + ".sources = {\n" );
			IEDSourceWriter edsourceWriter = converterEngine.getEDSourceWriter();
			for ( int i = 0; i < conf.edsourceCount(); i++ )
			{
				EDSourceInstance edsource = conf.edsource(i);
				str.append( edsourceWriter.toString(edsource, converterEngine, indent ) );
				str.append( ",\n" );
			}
			str.append( "}\n");
		}

		if ( conf.essourceCount() > 0 )
		{
			str.append( object + ".es_sources = {\n" );
			IESSourceWriter essourceWriter = converterEngine.getESSourceWriter();
			for ( int i = 0; i < conf.essourceCount(); i++ )
			{
				ESSourceInstance essource = conf.essource(i);
				str.append( essourceWriter.toString(essource, converterEngine, indent ) );
				str.append( ",\n" );
			}
			str.append( "}\n");

			str.append( object + ".es_prefers = {\n" );
			for ( int i = 0; i < conf.essourceCount(); i++ )
			{
				ESSourceInstance instance = conf.essource(i);
				if ( instance instanceof ESPreferable) 
				{
					ESPreferable esp = (ESPreferable)instance;
					if ( esp.isPreferred() ) 
						str.append( indent + "'" + instance.name() 
								+ "' : '" + instance.template().name() + "',\n" );
				}
			}
			str.append( "}\n");
		}

		if ( conf.esmoduleCount() > 0 )
		{
			str.append( object + ".es_modules = {\n" );
			IESModuleWriter esmoduleWriter = converterEngine.getESModuleWriter();
			for ( int i = 0; i < conf.esmoduleCount(); i++ )
			{
				ESModuleInstance esmodule = conf.esmodule(i);
				str.append( esmoduleWriter.toString( esmodule, converterEngine, indent ) );
				str.append( ",\n" );
			}
			str.append( "}\n");
		}


		if ( conf.serviceCount() > 0 )
		{
			str.append( object + ".services = {\n" );
			IServiceWriter serviceWriter = converterEngine.getServiceWriter();
			for ( int i = 0; i < conf.serviceCount(); i++ )
			{
				ServiceInstance service = conf.service(i);
				str.append( serviceWriter.toString( service, converterEngine, indent ) );
				str.append( ",\n" );
			}
			str.append( "}\n");
		}

		if ( conf.moduleCount() > 0 )
		{
			str.append( object + ".modules = {\n" );
			IModuleWriter moduleWriter = converterEngine.getModuleWriter();
			for ( int i = 0; i < conf.moduleCount(); i++ )
			{
				ModuleInstance module = conf.module(i);
				str.append( moduleWriter.toString( module ) );
				str.append( ",\n" );
			}
			str.append( "}\n");
		}

		return str.toString();
	}

	public void setConverterEngine( ConverterEngine converterEngine ) 
	{
		this.converterEngine = converterEngine;
	}
	
}

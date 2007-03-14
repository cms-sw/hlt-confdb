package confdb.converter;

import java.util.HashMap;

public class ConverterFactory {

	static private HashMap<String, String> cmssw2version = null;
	static private final String writerPackage = ConverterFactory.class.getPackage().getName();

	private String version = null;
	
	static public ConverterFactory getFactory( String releaseTag )
	{
		if ( cmssw2version == null )
		{
			cmssw2version = new HashMap<String, String>();
			cmssw2version.put("default", "V1" );			
		}
		return new ConverterFactory( releaseTag );
	}
	
	
	public IConverter getAsciiConverter() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return getConverter();
	}
	
	
	public Converter getConverter() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Converter converter = new Converter();
		converter.setConfigurationWriter( getConfigurationWriter() );
		converter.setParameterWriter( getParameterWriter() );

		converter.setEDSourceWriter( getEDSourceWriter() );
		converter.setESSourceWriter( getESSourceWriter() );
		converter.setServiceWriter( getServiceWriter() );
		converter.setModuleWriter( getModuleWriter() );
		converter.setPathWriter( getPathWriter() );
		converter.setSequenceWriter( getSequenceWriter() );
		
		return converter;
	}

	public IConfigurationWriter getConfigurationWriter() 
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	String className = writerPackage + ".ConfigurationWriter" + version;
	Class c = Class.forName( className );
	Object o = c.newInstance();
	return (IConfigurationWriter)o;
}

	public IPathWriter getPathWriter() 
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	String className = writerPackage + ".PathWriter" + version;
	Class c = Class.forName( className );
	Object o = c.newInstance();
	return (IPathWriter)o;
}

	
	public IServiceWriter getServiceWriter() 
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	String className = writerPackage + ".ServiceWriter" + version;
	Class c = Class.forName( className );
	Object o = c.newInstance();
	return (IServiceWriter)o;
}

	
	public IParameterWriter getParameterWriter() 
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	String className = writerPackage + ".ParameterWriter" + version;
	Class c = Class.forName( className );
	Object o = c.newInstance();
	return (IParameterWriter)o;
}

	public IEDSourceWriter getEDSourceWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	String className = writerPackage + ".EDSourceWriter" + version;
	Class c = Class.forName( className );
	Object o = c.newInstance();
	return (IEDSourceWriter)o;
}

	public IESSourceWriter getESSourceWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	String className = writerPackage + ".ESSourceWriter" + version;
	Class c = Class.forName( className );
	Object o = c.newInstance();
	return (IESSourceWriter)o;
}

	public IModuleWriter getModuleWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	String className = writerPackage + ".ModuleWriter" + version;
	Class c = Class.forName( className );
	Object o = c.newInstance();
	return (IModuleWriter)o;
}

	
	public ISequenceWriter getSequenceWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
{
	String className = writerPackage + ".SequenceWriter" + version;
	Class c = Class.forName( className );
	Object o = c.newInstance();
	return (ISequenceWriter)o;
}

	
	
	private ConverterFactory( String releaseTag )
	{
		version = cmssw2version.get( releaseTag );
		if ( version == null )
			version = cmssw2version.get( "default" );
	}
	
	
}

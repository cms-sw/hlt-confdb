package confdb.converter;

import java.util.HashMap;

import confdb.converter.html.HtmlConfigurationWriter;
import confdb.converter.html.HtmlEDSourceWriter;
import confdb.converter.html.HtmlESSourceWriter;
import confdb.converter.html.HtmlModuleWriter;
import confdb.converter.html.HtmlParameterWriter;
import confdb.converter.html.HtmlPathWriter;
import confdb.converter.html.HtmlServiceWriter;


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
	

	static public ConverterFactory getFactory()
	{
		if ( cmssw2version == null )
		{
			cmssw2version = new HashMap<String, String>();
			cmssw2version.put("default", "V1" );			
		}
		return new ConverterFactory( null );
	}
	

	public Converter getConverter( String typeOfConverter ) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String type = typeOfConverter.toUpperCase();
		if ( type.equals( "ASCII") )
			return getConverter();
		if ( type.equals( "HTML") )
			return getHtmlConverter();
		return null;
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

	public Converter getHtmlConverter() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Converter converter = new Converter();
		converter.setConfigurationWriter( new HtmlConfigurationWriter() );
		converter.setParameterWriter( new HtmlParameterWriter() );

		converter.setEDSourceWriter( new HtmlEDSourceWriter() );
		converter.setESSourceWriter( new HtmlESSourceWriter() );
		converter.setServiceWriter( new HtmlServiceWriter() );
		converter.setModuleWriter( new HtmlModuleWriter() );
		converter.setPathWriter( new HtmlPathWriter() );
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

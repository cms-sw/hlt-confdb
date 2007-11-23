package confdb.converter;


public class ConverterFactory 
{
	static private final String thisPackage = ConverterFactory.class.getPackage().getName();

	private String writerPackage = "";
	private String outputFormat = "Ascii";
	

	static public ConverterEngine getConverterEngine( String typeOfConverter ) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String outputFormat = "Ascii";
		
		if ( typeOfConverter != null )
		{
			char[] type = typeOfConverter.toLowerCase().toCharArray();
			type[0] = Character.toUpperCase( type[0] );
			outputFormat = new String( type );
			if (    !outputFormat.equals( "Ascii") 
				 && !outputFormat.equals( "Html")
			     && !outputFormat.equals( "Python")  ) 
				return null;
		}
		return new ConverterFactory( outputFormat ).getConverterEngine();
	}
	
	
	private ConverterFactory( String typeOfConverter )
	{
		if ( typeOfConverter != null )
		{
			char[] type = typeOfConverter.toLowerCase().toCharArray();
			type[0] = Character.toUpperCase( type[0] );
			outputFormat = new String( type );
			if (    !outputFormat.equals( "Ascii") 
				 && !outputFormat.equals( "Html")
			     && !outputFormat.equals( "Python")  ) 
				return;
		}
	}
	
	
	public ConverterEngine getConverterEngine() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		writerPackage = thisPackage + "." + outputFormat.toLowerCase();
		ConverterEngine converterEngine = new ConverterEngine( outputFormat );
		converterEngine.setConfigurationWriter( getConfigurationWriter() );
		converterEngine.setParameterWriter( getParameterWriter() );

		converterEngine.setEDSourceWriter( getEDSourceWriter() );
		converterEngine.setESSourceWriter( getESSourceWriter() );
		converterEngine.setESModuleWriter( getESModuleWriter() );
		converterEngine.setServiceWriter( getServiceWriter() );
		converterEngine.setModuleWriter( getModuleWriter() );
		converterEngine.setPathWriter( getPathWriter() );
		converterEngine.setSequenceWriter( getSequenceWriter() );
		
		return converterEngine;
	}

	public IConfigurationWriter getConfigurationWriter() 
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (IConfigurationWriter)getWriter( "ConfigurationWriter" );
	}

	public IPathWriter getPathWriter() 
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (IPathWriter)getWriter( "PathWriter" );
	}

	
	public IServiceWriter getServiceWriter() 
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (IServiceWriter)getWriter( "ServiceWriter" );
	}
	
	public IParameterWriter getParameterWriter() 
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (IParameterWriter)getWriter( "ParameterWriter" );
	}

	public IEDSourceWriter getEDSourceWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (IEDSourceWriter)getWriter( "EDSourceWriter" );
	}

	public IESSourceWriter getESSourceWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (IESSourceWriter)getWriter( "ESSourceWriter" );
	}

	public IESModuleWriter getESModuleWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (IESModuleWriter)getWriter( "ESModuleWriter" );
	}

	public IModuleWriter getModuleWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (IModuleWriter)getWriter( "ModuleWriter" );
	}
	
	public ISequenceWriter getSequenceWriter()
	  throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return (ISequenceWriter)getWriter( "SequenceWriter" );
	}

	
	private Object getWriter( String type ) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String className = writerPackage + "." + outputFormat + type;
		Class<?> c = Class.forName( className );
		return c.newInstance();
	}

	
}

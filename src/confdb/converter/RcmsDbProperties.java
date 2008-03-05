package confdb.converter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class RcmsDbProperties extends DbProperties 
{
	protected String path = null;
	protected boolean exist = false;
	
	public RcmsDbProperties() throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, FileNotFoundException, IOException, ConverterException
	{
		// get path: RCMSRuntimeInfo.servletContext + RCMSConstants.RCMSPROPERTIES_FILE
		Class<?> rcmsInfo = Class.forName( "rcms.fm.context.RCMSRuntimeInfo" );
		Field servletContextField = rcmsInfo.getField( "servletContext" );
		Object servletContext = servletContextField.get( null );
		Class<?>[] paramClasses = new Class[1];
		paramClasses[0] = Class.forName("java.lang.String");
		Method method = servletContext.getClass().getMethod( "getRealPath", paramClasses );
		Object[] params = new Object[1];
		params[0] = new String( "/" );
		path = (String)method.invoke( servletContext, params );

		rcmsInfo = Class.forName( "rcms.fm.context.RCMSConstants" );
		Field propertiesFileField = rcmsInfo.getField( "RCMS_PROPERTIES_FILE" );
		String propertiesFile = (String)propertiesFileField.get( null );
		path += propertiesFile;
			
		Properties rcmsProperties = new Properties();
		rcmsProperties.load( new FileInputStream( path ) );

		init( rcmsProperties );
		exist = true;
	}

	public boolean exist()
	{
		return exist;
	}
	
	public String getPath()
	{
		return path;
	}

}

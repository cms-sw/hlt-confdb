package confdb.converter;

import java.sql.Connection;

import confdb.data.Template;
import confdb.data.EDSourceTemplates;
import confdb.data.ModuleTemplate;
import confdb.data.Parameter;
import confdb.data.DataException;


/**
 * OnlineConverter
 * ---------------
 * @author Philipp Schieferdecker
 *
 * Handle conversion of configurations stored in the database for
 * deployment to the online HLT filter farm.
 */
public class OnlineConverter extends ConverterBase
{
    //
    // member data
    //

    /** current configuration id */
    private int configId = -1;

    /** current release tag for templates */
    private String releaseTag = "";

    /** current configuration string for FUEventProcessor */
    private String epConfigString = null;

    /** current configuration string for StorageManager */
    private String smConfigString = null;

    /** EDSource template for FUEventProcessor */
    private EDSourceTemplate epSourceT = null;
    
    /** OutputModule template for FUEventProcessor */
    private ModuleTemplate   epOutputModuleT = null;

    /** EDSource template for StorageManager */
    private EDSourceTemplate smSourceT = null;

    /** OutputModule template for StorageManager */
    private ModuleTemplate   smOutputModuleT = null;
    
    
    //
    // construction
    //

    /** constructor based on Connection object */
    public OnlineConverter(String format,Connection connection)
    {
	super(format,connection);
    }

    /** constructor based on explicit connection information */
    public OnlineConverter(String format,
			   String dbType,String dbUrl,String dbUser,String dbPwrd)
    {
	super(format,dbType,dbUrl,dbUser,dbPwrd);
    }
    

    //
    // member functions
    //

    /** get the configuration string for FUEventProcessor */
    public getEpConfigString(int configId)
    {
	if (configId!=this.configId) convertConfiguration(configId);
	return epConfiString;
    }

    /** get the configuration string for StorageManager */
    {
	if (configId!=this.configId) convertConfiguration(configId);
	return smConfigString;
    }


    //
    // private member data
    //
    
    /** convert configuration and cache ep and sm configuration string */
    private void convertConfiguration(int configId) throws ConverterException
    {
	IConfiguration epConfig = getConfiguration(configId);
	
	if (config.releaseTag().equals(releaseTag)) {
	    epSourceT.removeAllInstances();
	    epOutputModuleT.removeAllInstances();
	    smSourceT.removeaAllInstances();
	    smOutputModuleT.removeAllInstances();
	}
	else {
	    releaseTag     =config.releaseTag();
	    epSourceT      =getDatabase().loadTemplate(releaseTag,"DaqSource");
	    epOutputModuleT=getDatabase().loadTemplate(releaseTag,"ShmStreamConsumer");
	    smSourceT      =getDatabase().loadTemplate(releaseTag,"FragmentSource");
	    smOutputModuleT=getDatabase().loadTemplate(releaseTag,"EventStreamFileWriter");
	}
	
	if (epSourceT==null)
	    throw new ConverterException("Failed to load epSourceT");
	if (epOutputModuleT==null)
	    throw new ConverterException("Failed to load epOutputModuleT");
	if (smSourceT==null)
	    throw new ConverterException("Failed to load smSourceT");
	if (sOutputModuleT==null)
	    throw new ConverterException("Failed to load smOutputModuleT");
	
	try {
	    EDSourceInstance epSource = epSourceT.instance();
	}
	catch (DataException e) {
	    throw new ConverterException(e.getMessage());
	}
	epSource.updateParameter("readerPluginName","string","FUShmReader");
	
	try {
	    EDSourceInstance smSource = smSourceT.instance();
	}
	catch (DataException e) {
	    throw new ConverterException(e.getMessage());
	}
	
	Configuration smConfig = new Configuration(new ConfigInfo(),
						   config.release());
	
	// TODO

	ConfigurationModifier epModifier = new ConfigurationModifier(epConfig);
	epModifier.replaceEDSource(epSource);
	epModifier.replaceOutputModules(epOutputModuleT);
	epModifier.modify();
	
	epConfigString = getConverterEngine().convert(epModifier);
	smConfigString = getConverterEngine().convert(smConfig);
    }


    /** */
    
}

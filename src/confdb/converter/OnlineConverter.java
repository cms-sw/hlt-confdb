package confdb.converter;

import java.util.ArrayList;
import java.util.Iterator;

import java.sql.Connection;

import confdb.data.*;


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

    /** StreamWriter template for StorageManager */
    private ModuleTemplate   smStreamWriterT = null;
    
    
    //
    // construction
    //

    /** constructor based on Connection object */
    public OnlineConverter(String format,Connection connection)
	throws ConverterException
    {
	super(format,connection);
    }

    /** constructor based on explicit connection information */
    public OnlineConverter(String format,
			   String dbType,String dbUrl,String dbUser,String dbPwrd)
	throws ConverterException
    {
	super(format,dbType,dbUrl,dbUser,dbPwrd);
    }
    

    //
    // member functions
    //

    /** get the configuration string for FUEventProcessor */
    public String getEpConfigString(int configId) throws ConverterException
    {
	if (configId!=this.configId) convertConfiguration(configId);
	return epConfigString;
    }
    
    /** get the configuration string for StorageManager */
    public String getSmConfigString(int configId) throws ConverterException
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
	
	if (epConfig.releaseTag().equals(releaseTag)) {
	    epSourceT.removeAllInstances();
	    epOutputModuleT.removeAllInstances();
	    smStreamWriterT.removeAllInstances();
	}
	else {
	    releaseTag = epConfig.releaseTag();
	    epSourceT  = (EDSourceTemplate)getDatabase()
		.loadTemplate(releaseTag,"DaqSource");
	    epOutputModuleT = (ModuleTemplate)getDatabase()
		.loadTemplate(releaseTag,"ShmStreamConsumer");
	    smStreamWriterT = (ModuleTemplate)getDatabase()
		.loadTemplate(releaseTag,"EventStreamFileWriter");
	}
	
	if (epSourceT==null)
	    throw new ConverterException("Failed to load epSourceT");
	if (epOutputModuleT==null)
	    throw new ConverterException("Failed to load epOutputModuleT");
	if (smStreamWriterT==null)
	    throw new ConverterException("Failed to load smStreamWriterT");
	
	EDSourceInstance epSource = null;
	try {
	    epSource = (EDSourceInstance)epSourceT.instance();
	}
	catch (DataException e) {
	    throw new ConverterException(e.getMessage());
	}
	epSource.updateParameter("readerPluginName","string","FUShmReader");
	
	SoftwareRelease smRelease = new SoftwareRelease();
	smRelease.addSubsystem(smStreamWriterT.parentPackage().subsystem());
	
	Configuration smConfig =
	    new Configuration(new ConfigInfo(epConfig.name(),
					     epConfig.parentDir(),
					     -1,epConfig.version(),
					     epConfig.created(),
					     epConfig.creator(),
					     epConfig.releaseTag(),
					     "SM"),smRelease);
	
	
	Path endpath  = smConfig.insertPath(0,"epstreams");
	Iterator itStream = epConfig.streamIterator();
	while (itStream.hasNext()) {
	    Stream stream = (Stream)itStream.next();
	    ModuleReference streamWriterRef =
		smConfig.insertModuleReference(endpath,
					       endpath.entryCount(),
					       smStreamWriterT.name(),
					       stream.label());
	    ModuleInstance streamWriter = (ModuleInstance)streamWriterRef.parent();
	    streamWriter.updateParameter("streamLabel","string",stream.label());
	    streamWriter.updateParameter("maxSize","int32","1073741824");
	    PSetParameter psetSelectEvents =
		new PSetParameter("SelectEvents","",false,false);
	    String valAsString = "";
	    Iterator itPath = stream.pathIterator();
	    while (itPath.hasNext()) {
		Path path = (Path)itPath.next();
		if (valAsString.length()>0) valAsString += ",";
		valAsString += path.name();
	    }
	    VStringParameter vstringSelectEvents =
		new VStringParameter("SelectEvents",valAsString,true,false);
	    psetSelectEvents.addParameter(vstringSelectEvents);
	    streamWriter.updateParameter("SelectEvents","PSet",
					 psetSelectEvents.valueAsString());
	}
	
	ConfigurationModifier epModifier = new ConfigurationModifier(epConfig);
	epModifier.replaceEDSource(epSource);
	//epModifier.replaceOutputModules(epOutputModuleT);
	epModifier.modify();
	
	epConfigString = getConverterEngine().convert(epModifier);
	smConfigString = getConverterEngine().convert(smConfig);
    }
    

    //
    // main
    //
    public static void main(String[] args)
    {
	String configId   =          "";
	String dbType     =     "mysql";
	String dbHost     = "localhost";
	String dbPort     =      "3306";
	String dbName     =     "hltdb";
	String dbUser     =          "";
	String dbPwrd     =          "";

	for (int iarg=0;iarg<args.length;iarg++) {
	    String arg = args[iarg];
	    if      (arg.equals("-c")) { iarg++; configId = args[iarg]; }
	    else if (arg.equals("-t")) { iarg++; dbType = args[iarg]; }
	    else if (arg.equals("-h")) { iarg++; dbHost = args[iarg]; }
	    else if (arg.equals("-p")) { iarg++; dbPort = args[iarg]; }
	    else if (arg.equals("-d")) { iarg++; dbName = args[iarg]; }
	    else if (arg.equals("-u")) { iarg++; dbUser = args[iarg]; }
	    else if (arg.equals("-s")) { iarg++; dbPwrd = args[iarg]; }
	}
	
	String dbUrl = "";
	if (dbType.equalsIgnoreCase("mysql")) {
	    dbUrl  = "jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbName;
	}
	else if (dbType.equalsIgnoreCase("oracle")) {
	    dbUrl = "jdbc:oracle:thin:@//"+dbHost+":"+dbPort+"/"+dbName;
	}
	else {
	    System.err.println("Unknwown db type '"+dbType+"'");
	    System.exit(0);
	}
	
	try {
	    OnlineConverter cnv = 
		new OnlineConverter("ascii",dbType,dbUrl,dbName,dbPwrd);
	    System.out.println(cnv.getEpConfigString(Integer.parseInt(configId)));
	    System.out.println(cnv.getSmConfigString(Integer.parseInt(configId)));
	}
	catch(ConverterException e) {
	    System.out.println(e.getMessage());
	}
	
    }
    
}

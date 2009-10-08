package confdb.converter;

import java.util.HashMap;
import java.util.Iterator;

import confdb.data.*;
import confdb.db.DatabaseException;

import confdb.converter.IConfigurationWriter.WriteProcess;


/**
 * OfflineConverter
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Handle conversion of configuraiton stored in the database for
 * offline use, e.g. the GUI, webserver, or command line tool.
 */
public class OfflineConverter extends ConverterBase
{
    //
    // construction
    //

    /** constructor based on format, no database connection */
    public OfflineConverter(String format)
	throws ConverterException
    {
	super(format);
    }
    
    /** constructor based on explicit connection information */
    public OfflineConverter(String format,
			    String dbType,String dbUrl,String dbUser,String dbPwrd)
	throws ConverterException
    {
	super(format,dbType,dbUrl,dbUser,dbPwrd);
    }

    
    //
    // member functions
    //
    
    /** retrieve the configuration string for the given configId */
    public String getConfigString(String configName,
				  ModifierInstructions modifications,
				  boolean asFragment)
	throws ConverterException
    {
	try {
	    int configId = getDatabase().getConfigId(configName);
	    return getConfigString(configId,modifications,asFragment);
	}
	catch (DatabaseException e) {
	    String errMsg =
		"OfflineConverter::getConfigString(configName="+configName+
		",modifications,asFragment="+asFragment+") failed.";
	    throw new ConverterException(errMsg,e);
	}
    }
    
    /** retrieve the configuration string for the given configId */
    public String getConfigString(String configName,
				  String format,
				  ModifierInstructions modifications,
				  boolean asFragment)
	throws ConverterException
    {
	try {
	    int configId = getDatabase().getConfigId(configName);
	    return getConfigString(configId,format,modifications,asFragment);
	}
	catch (DatabaseException e) {
	    String errMsg =
		"OfflineConverter::getConfigString(configName="+configName+
		",modifications,asFragment="+asFragment+") failed.";
	    throw new ConverterException(errMsg,e);
	}
    }
    
    /** retrieve the configuration string for the given configId */
    public String getConfigString(int configId,
				  ModifierInstructions modifications,
				  boolean asFragment)
	throws ConverterException
    {
	IConfiguration config = getConfiguration(configId);
	return getConfigString(config,modifications,asFragment);
    }
    
    
    /** retrieve the configuration string for an IConfiguration object */
    public String getConfigString(IConfiguration config,
			  ModifierInstructions modifications,
			  boolean asFragment) throws ConverterException
    {
    	return getConfigString(config, null, modifications, asFragment);
    }
    
    /** retrieve the configuration string for an IConfiguration object */
    public String getConfigString(IConfiguration config,
				  String format,
				  ModifierInstructions modifications,
				  boolean asFragment)  throws ConverterException
    {
    	ConfigurationModifier modifier = new ConfigurationModifier(config);
	
    	modifier.modify(modifications);
    	addPSetForStreams(modifier);
    	addPSetForDatasets(modifier);

    	ConverterEngine engine = getConverterEngine();
    	if ( format != null )
	    try {
		engine = ConverterFactory.getConverterEngine( format );
	    } catch (Exception e) {
		throw new ConverterException( "can't get ConverterEngine", e );
	    }
    	if (asFragment)
	    return engine.getConfigurationWriter().toString(modifier,WriteProcess.NO);
    	else
	    return engine.getConfigurationWriter().toString(modifier,WriteProcess.YES);
    }
    
    /** retrieve the configuration string for the given configId */
    public String getConfigString(int configId,
				  String format,
				  ModifierInstructions modifications,
				  boolean asFragment) throws ConverterException
    {
    	IConfiguration config = getConfiguration( configId );
    	return getConfigString( config, format, modifications, asFragment );
    }
    
    
    //
    // private memeber functions
    //
    
    /** create untracked pset with streams information */
    private void addPSetForStreams(IConfiguration config)
    {
	if (config.streamCount()==0) return;
	
	PSetParameter pset = new PSetParameter("streams","",false);
	Iterator<Stream> itS = config.streamIterator();
	while (itS.hasNext()) {
	    Stream stream = itS.next();
	    StringBuffer valueAsString = new StringBuffer();
	    Iterator<PrimaryDataset> itD = stream.datasetIterator();
	    while (itD.hasNext()) {
		if (valueAsString.length()>0) valueAsString.append(",");
		valueAsString.append(itD.next().label());
	    }
	    pset.addParameter(new VStringParameter(stream.label(),
						   valueAsString.toString(),
						   false));
	}
	config.insertPSet(pset);
    }

    /** create untracked pset with streams information */
    private void addPSetForDatasets(IConfiguration config)
    {
	if (config.datasetCount()==0) return;
	
	PSetParameter pset = new PSetParameter("datasets","",false);
	Iterator<PrimaryDataset> itD = config.datasetIterator();
	while (itD.hasNext()) {
	    PrimaryDataset dataset = itD.next();
	    StringBuffer valueAsString = new StringBuffer();
	    Iterator<Path> itP = dataset.pathIterator();
	    while (itP.hasNext()) {
		if (valueAsString.length()>0) valueAsString.append(",");
		valueAsString.append(itP.next().name());
	    }
	    pset.addParameter(new VStringParameter(dataset.label(),
						   valueAsString.toString(),
						   false));
	}
	config.insertPSet(pset);
    }



    //
    // main method, for testing
    //
    public static void main(String[] args)
    {
	String  configId    =                  "";
	String  configName  =                  "";
	String  format      =            "python";
	boolean asFragment  =               false;

	String  dbType      =            "oracle";
	String  dbHost      =   "cmsr1-v.cern.ch";
	String  dbPort      =             "10121";
	String  dbName      =  "cms_cond.cern.ch";
	String  dbUser      = "cms_hltdev_reader";
	String  dbPwrd      =                  "";

	HashMap<String,String> cnvArgs = new HashMap<String,String>();
	
	for (int iarg=0;iarg<args.length;iarg++) {
	    String arg = args[iarg];
	    if (arg.equals("-id")||arg.equals("--configId")) {
		iarg++; configId = args[iarg];
	    }
	    else if (arg.equals("-cfg")||arg.equals("--configName")) {
		if (!configId.equals("")) {
		    System.err.println("ERROR: can't specify "+
				       "config-id *and* config-name!");
			System.exit(0);
		}
		iarg++; configName = args[iarg];
	    }
	    else if (arg.equals("-f")||arg.equals("--format")) {
		iarg++; format = args[iarg];
	    }
	    else if (arg.equals("--cff")) {
		asFragment = true;
	    }
	    else if (arg.equals("-t")||arg.equals("--dbtype")) {
		iarg++; dbType = args[iarg];
	    }
	    else if (arg.equals("-h")||arg.equals("--dbhost")) {
		iarg++; dbHost = args[iarg];
	    }
	    else if (arg.equals("-p")||arg.equals("--dbport")) {
		iarg++; dbPort = args[iarg];
	    }
	    else if (arg.equals("-d")||arg.equals("--dbname")) {
		iarg++; dbName = args[iarg];
	    }
	    else if (arg.equals("-u")||arg.equals("--dbuser")) {
		iarg++; dbUser = args[iarg];
	    }
	    else if (arg.equals("-s")||arg.equals("--dbpwrd")) {
		iarg++; dbPwrd = args[iarg];
	    }
	    else if (arg.startsWith("--no")) {
		String key = arg.substring(2);
		String val = "";
		cnvArgs.put(key,val);
	    }
	    else if (arg.startsWith("--")) {
		String key = arg.substring(2);
		String val = args[++iarg];
		cnvArgs.put(key,val);
	    }
	    else {
		System.err.println("ERROR: invalid option '" + arg + "'!");
		System.exit(0);
	    }
	}
	
	if (configId.length()==0) {
	    if (configName.length()==0) {
		System.out.println("ERROR: no configuration specified!");
		System.exit(0);
	    }
	}

	if (!format.equals("ascii")&&
	    !format.equals("python")&&
	    !format.equals("summary.ascii")&&
	    !format.equals("html")) {
	    System.err.println("ERROR: Invalid format '"+format+"'");
	    System.exit(0);
	}
	
	String dbUrl = "";
	if (dbType.equalsIgnoreCase("mysql")) {
	    dbUrl  = "jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbName;
	}
	else if (dbType.equalsIgnoreCase("oracle")) {
	    dbUrl = "jdbc:oracle:thin:@//"+dbHost+":"+dbPort+"/"+dbName;
	}
	else {
	    System.err.println("ERROR: Unknwown db type '"+dbType+"'");
	    System.exit(0);
	}
	
	System.err.println("dbURl  = " + dbUrl);
	System.err.println("dbUser = " + dbUser);
	System.err.println("dbPwrd = " + dbPwrd);

	try {
	    ModifierInstructions modifications = new ModifierInstructions();
	    modifications.interpretArgs(cnvArgs);
	    OfflineConverter cnv = 
		new OfflineConverter(format,dbType,dbUrl,dbUser,dbPwrd);
	    if (configId.length()>0)
		System.out.println(cnv.getConfigString(Integer.parseInt(configId),
						       format,
						       modifications,
						       asFragment));
	    else
		System.out.println(cnv.getConfigString(configName,
						       format,
						       modifications,
						       asFragment));
	    
	}
	catch(Exception e) {
	    System.err.println("ERROR: " + e.getMessage());
	    e.printStackTrace();
	}

    }
    
}

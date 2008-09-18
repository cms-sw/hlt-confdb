package confdb.db;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.io.*;

import confdb.data.*;


/**
 * ConfDBCreateConfig
 * ------------------
 * @author Philipp Schieferdecker
 *
 * Create a new configuration in ConfDB based on an existing one.
 */
public class ConfDBCreateConfig
{
    //
    // MAIN
    //
    
    /** main */
    public static void main(String[] args)
    {
	String  masterConfigName="";
	String  pathList="";
	String  newConfigName="";
	
	String  dbType      =            "oracle";
	String  dbHost      =   "cmsr1-v.cern.ch";
	String  dbPort      =             "10121";
	String  dbName      =  "cms_cond.cern.ch";
	String  dbUser      = "cms_hltdev_writer";
	String  dbPwrd      =                  "";

	for (int iarg=0;iarg<args.length;iarg++) {
	    String arg = args[iarg];
	    if      (arg.equals("-m")||arg.equals("--master"))
		masterConfigName = args[++iarg];
	    else if (arg.equals("--paths"))
		pathList = args[++iarg];
	    else if (arg.equals("-n")||arg.equals("--name"))
		newConfigName = args[++iarg];
	    else if (arg.equals("-t")||arg.equals("--dbType"))
		dbType = args[++iarg];
	    else if (arg.equals("-h")||arg.equals("--dbHost"))
		dbHost = args[++iarg];
	    else if (arg.equals("-p")||arg.equals("--dbPort"))
		dbPort = args[++iarg];
	    else if (arg.equals("-d")||arg.equals("--dbName"))
		dbName = args[++iarg];
	    else if (arg.equals("-u")||arg.equals("--dbUser"))
		dbUser = args[++iarg];
	    else if (arg.equals("-s")||arg.equals("--dbPwrd"))
		dbPwrd = args[++iarg];
	    else {
		System.err.println("ERROR: invalid option '" + arg + "'!");
		System.exit(0);
	    }
	}
		
	if (masterConfigName.length()==0) {
	    System.err.println("master config-name must be specified "+
			       "(-m / --master)");
	    System.exit(0);
	}
	if (pathList.length()==0) {
	    System.err.println("path-list must be specified (-p / --paths)");
	    System.exit(0);
	}
	if (newConfigName.length()==0) {
	    System.err.println("new config-name must be specified (-n / --name)");
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
	
	
	ConfDB database = new ConfDB();
	
	try {
	    // connect to database
	    database.connect(dbType,dbUrl,dbUser,dbPwrd);

	    // check that master configuration exists
	    int configId = database.getConfigId(masterConfigName);
	    System.out.println("GOOD, found master config "+masterConfigName+".");

	    // check that directory of new configuration exists
	    String dirName
		=newConfigName.substring(0,newConfigName.lastIndexOf('/'));
	    int dirId = database.getDirectoryId(dirName);
	    System.out.println("GOOD, directory "+dirName+" does exist.");
	    Directory dir = database.getDirectoryHashMap().get(dirId);
	    
	    // check that new configuration does *not* exist
	    int checkId=-1;
	    try {
		checkId = database.getConfigId(newConfigName);
	    }
	    catch (DatabaseException e) {
		System.out.println("GOOD, "+newConfigName+" does not yet exist.");
	    }
	    if (checkId>=0) {
		String errmsg = newConfigName + " exists already!";
		throw new Exception(errmsg);
	    }
	    
	    // load master configuration
	    Configuration masterConfig = database.loadConfiguration(configId);
	    System.out.println("GOOD, "+masterConfigName+" loaded.");

	    // decode path list
	    HashSet<String> pathsToInclude = new HashSet<String>();
	    if (pathList.endsWith(".txt")) {
		BufferedReader input =
		    new BufferedReader(new FileReader(new File(pathList)));
		try {
		    String line = null;
		    while (( line = input.readLine()) != null) {
			pathsToInclude.add(line);
		    }
		}
		finally {
		    input.close();
		}
	    }
	    else {
		String[] paths = pathList.split(",");
		for (String s : paths) pathsToInclude.add(s);
	    }
	    if (pathsToInclude.size()==0) {
		String errmsg = "No paths specified to be included!";
		throw new Exception(errmsg);
	    }
	    System.out.println("GOOD, the following paths will be included:");
	    Iterator<String> it = pathsToInclude.iterator();
	    while (it.hasNext()) System.out.println(it.next());
	    
	    // remove paths which are not in the list
	    ArrayList<String> pathNames = new ArrayList<String>();
	    Iterator<Path> itP = masterConfig.pathIterator();
	    while (itP.hasNext()) pathNames.add(itP.next().name());
	    it = pathNames.iterator();
	    while (it.hasNext()) {
		String pathName = it.next();
		if (!pathsToInclude.contains(pathName)) {
		    System.out.println(" REMOVE "+pathName);
		    Path path = masterConfig.path(pathName);
		    masterConfig.removePath(path);
		}
		else pathsToInclude.remove(pathName);
	    }
	    
	    // check that all specified paths were found
	    if (pathsToInclude.size()!=0) {
		StringBuffer sberrmsg = new StringBuffer();
		sberrmsg.append("The following paths are not in the master: ");
		it = pathsToInclude.iterator();
		while (it.hasNext()) sberrmsg.append(it.next()).append(" ");
		throw new Exception(sberrmsg.toString());
	    }

	    // save the configuration under the new name
	    String configName  = newConfigName.substring(dirName.length()+1);
	    String userName    = System.getProperty("user.name");
	    String processName = masterConfig.processName();
	    String releaseTag  = masterConfig.releaseTag();
	    String comment     = "Created using ConfDBCreateConfig.";
	    
	    ConfigInfo ci = new ConfigInfo(configName,dir,releaseTag);
	    masterConfig.setConfigInfo(ci);

	    System.out.println("configName = " + configName);
	    
	    System.out.println("Store new configuration ...");
	    long startTime = System.currentTimeMillis();
	    database.insertConfiguration(masterConfig,userName,processName,comment);
	    long elapsedTime = System.currentTimeMillis() - startTime;
	    System.out.println(newConfigName+" STORED (" + elapsedTime + ")");
	}
	catch (DatabaseException e) {
	    System.err.println("Failed to connet to DB: " + e.getMessage());
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	finally {
	    try { database.disconnect(); }
	    catch (DatabaseException e) { e.printStackTrace(); }
	}
    }
    
}

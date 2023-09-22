package confdb.converter;

import java.util.HashMap;


import confdb.data.*;

import confdb.db.ConfDB;
import confdb.gui.*;


/**
 * PrescaleUpdator
 * ----------------
 * @author Sam Harper
 *
 * updates the configuration of the prescales via the command line
 */
public class PrescaleUpdator {
    /** constructor based on format, no database connection */
    public PrescaleUpdator(){}
	
    //
    // main method, for testing
    //
    public static void main(String[] args)
    {
	String  configId    =                  "";
	String  configName  =                  "";
	String  dbType      =            "oracle";
	String  dbHost      =   "cmsr1-v.cern.ch";
	String  dbPort      =             "10121";
	String  dbName      =      "cmsr.cern.ch";
	String  dbUser      =     "cms_hlt_gdr_r";
	String  dbPwrd      =        "convertme!";
  String  pstblfile   =                  "";

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
      else if (arg.equals("--pstblfile")) {
        iarg++; pstblfile = args[iarg];
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
		System.err.println("ERROR: no configuration specified!");
		System.exit(0);
	    }
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
	    ConverterBase cnv = 
		    new ConverterBase("python",dbType,dbUrl,dbUser,dbPwrd);
	    configId="";
	    if (configId.length()>0){
		    cnv.getConfiguration(Integer.parseInt(configId));
	    }else{
		    Configuration config = (Configuration) cnv.getConfiguration(configName);
        PrescaleTableModel psTblModel = new PrescaleTableModel();
        psTblModel.initialize(config);
        psTblModel.updatePrescaleTableFromFile(pstblfile,true);
        psTblModel.updatePrescaleService(config);
        ConfDB db = cnv.getDatabase();
        ServiceInstance psService = config.service("PrescaleService");
        System.out.println("labels "+psService.parameter("lvl1Labels").valueAsString());
        System.out.println("procesname "+config.processName());
        db.insertConfiguration(config,"pstool",config.processName(),"prescale table update");
	    }
	}
	catch(Exception e) {
	    System.err.println("ERROR: " + e.getMessage());
	    e.printStackTrace();
	}

    }
    
}

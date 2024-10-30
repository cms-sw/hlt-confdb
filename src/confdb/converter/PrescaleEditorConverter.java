package confdb.converter;

import java.util.HashMap;

import java.util.ArrayList;

import java.io.*;
import java.util.Scanner;

import confdb.data.*;

import confdb.db.ConfDB;
import confdb.gui.*;

/**
 * PrescaleUpdator
 * ----------------
 * 
 * @author Sam Harper
 *
 *         handles the writing of the new ps tool to the db
 */
public class PrescaleEditorConverter {
    /** constructor based on format, no database connection */
    public PrescaleEditorConverter() {
    }

    //
    // main method
    //
    public static void main(String[] args) {
        String configId = "";
        String configName = "";
        String dbType = "oracle";
        String dbHost = "cmsr1-v.cern.ch";
        String dbPort = "10121";
        String dbName = "cmsr.cern.ch";
        String dbUser = "cms_hlt_gdr_r";
        String dbPwrd = "convertme!";
        String pstblfile = "";

        HashMap<String, String> cnvArgs = new HashMap<String, String>();

        for (int iarg = 0; iarg < args.length; iarg++) {
            String arg = args[iarg];
            if (arg.equals("-cfg") || arg.equals("--configName")) {
                iarg++;
                configName = args[iarg];
            } else if (arg.equals("-t") || arg.equals("--dbtype")) {
                iarg++;
                dbType = args[iarg];
            } else if (arg.equals("-h") || arg.equals("--dbhost")) {
                iarg++;
                dbHost = args[iarg];
            } else if (arg.equals("-p") || arg.equals("--dbport")) {
                iarg++;
                dbPort = args[iarg];
            } else if (arg.equals("-d") || arg.equals("--dbname")) {
                iarg++;
                dbName = args[iarg];
            } else if (arg.equals("-u") || arg.equals("--dbuser")) {
                iarg++;
                dbUser = args[iarg];
            } else if (arg.equals("-s") || arg.equals("--dbpwrd")) {
                iarg++;
                dbPwrd = args[iarg];
            } else if (arg.equals("--pstblfile")) {
                iarg++;
                pstblfile = args[iarg];
            } else if (arg.startsWith("--no")) {
                String key = arg.substring(2);
                String val = "";
                cnvArgs.put(key, val);
            } else if (arg.startsWith("--")) {
                String key = arg.substring(2);
                String val = args[++iarg];
                cnvArgs.put(key, val);
            } else {
                System.err.println("ERROR: invalid option '" + arg + "'!");
                System.exit(0);
            }
        }

        if (configId.length() == 0) {
            if (configName.length() == 0) {
                System.err.println("ERROR: no configuration specified!");
                System.exit(0);
            }
        }

        String dbUrl = "";
        if (dbType.equalsIgnoreCase("mysql")) {
            dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
        } else if (dbType.equalsIgnoreCase("oracle")) {
            dbUrl = "jdbc:oracle:thin:@//" + dbHost + ":" + dbPort + "/" + dbName;
        } else {
            System.err.println("ERROR: Unknwown db type '" + dbType + "'");
            System.exit(0);
        }

        System.err.println("dbURl  = " + dbUrl);
        System.err.println("dbUser = " + dbUser);
        System.err.println("dbPwrd = " + dbPwrd);

        try {
            Configuration config = new Configuration();
            SoftwareRelease release = new SoftwareRelease();
            String releaseTag = new String("CMSSW_13_2_3");

            ConverterBase cnv = new ConverterBase("python", dbType, dbUrl, dbUser, dbPwrd);

            ConfDB db = cnv.getDatabase();
            db.loadSoftwareRelease(releaseTag, release);

            Directory rootDir = db.loadConfigurationTree();

            String basePSDirname = "/users/sharper/2024/test1/prescales";
            // first we check if the basePSDirname already exist and exit if not
            Directory basePSDir = getDirectoryByPath(rootDir, basePSDirname, false, db);
            if (basePSDir == null) {
                System.err.println("ERROR: base directory " + basePSDirname + " not found, this ");
                return;
            }
            System.err.println("release" + release);
            // its easier based on how directories are created start again from the rootDir
            // even though we have the basePSDir
            Directory configDir = getDirectoryByPath(rootDir, basePSDirname + configName, true, db);

            config.initialize(new ConfigInfo("prescales", configDir, releaseTag), release);

            ArrayList<String> pathNames = getPathNamesFromCSVFile(pstblfile);

            ModuleInstance module = config.insertModule("HLTBool", "hltBoolTrue");
            for (String pathName : pathNames) {
                if (pathName.endsWith("_v")) {
                    pathName = pathName + "1";
                }
                Path path = config.insertPath(config.pathCount(), pathName);
                // System.err.println("pathName = " + pathName);
                config.insertModuleReference(path, 0, module);
            }
            // ServiceTemplate psServiceTemplate =
            // release.serviceTemplate("PrescaleService");
            // ServiceInstance psService = new
            // ServiceInstance("PrescaleService",psServiceTemplate);
            config.insertService(0, "PrescaleService");

            PrescaleTableModel psTblModel = new PrescaleTableModel();
            psTblModel.initialize(config);
            psTblModel.updatePrescaleService(config);
            psTblModel.updatePrescaleTableFromFile(pstblfile, true);
            psTblModel.updatePrescaleService(config);

            ServiceInstance psService = config.service("PrescaleService");
            if (psService == null) {
                System.err.println("No PrescaleService found.");

            } else {
                System.out.println("labels " + psService.parameter("lvl1Labels").valueAsString());
                System.out.println("procesname " + config.processName());
            }
            db.insertConfiguration(config, "pstool", config.processName(), "prescale table update");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static ArrayList<String> getPathNamesFromCSVFile(String filename) {
        ArrayList<String> pathNames = new ArrayList<String>();
        if (filename.equals("")) {
            return pathNames;
        }

        try {
            Scanner tableScanner = new Scanner(new FileInputStream(filename), "UTF-8");
            if (tableScanner.hasNextLine()) {
                tableScanner.nextLine(); // first line is the header
            }
            while (tableScanner.hasNextLine()) {
                String line = tableScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    pathNames.add(parts[0]);
                }
            }
            tableScanner.close();
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return pathNames;
    }

    public static Directory getDirectoryByPath(Directory rootDir, String targetDirName, Boolean createIfNotFound,
            ConfDB db) {
        String[] targetDirBits = targetDirName.split("/");
        Directory currentDir = rootDir;
        String foundName = "";
        for (String part : targetDirBits) {
            if (part.equals("")) {
                continue;
            }
            foundName += "/" + part;
            // System.err.println("part = "+foundName);

            // System.err.println("curent dir: "+currentDir.name());
            // for( Directory dir : currentDir.listOfDirectories()){
            // System.err.println("curentdir child: "+dir.name());
            // }
            Directory child = getChildDirectory(currentDir, foundName);
            if (child == null) {
                if (!createIfNotFound) {
                    return null;
                } else {
                    System.err.println("create new directory: " + foundName);
                    child = new Directory(-1, foundName, "", currentDir);
                    currentDir.addChildDir(child);
                    try {
                        db.insertDirectory(child);
                    } catch (Exception e) {
                        System.err.println("ERROR: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }

                }
            }

            currentDir = child;
        }
        return currentDir;
    }

    public static Directory getChildDirectory(Directory parentDir, String childDirName) {
        Directory[] children = parentDir.listOfDirectories();
        for (int childNr = 0; childNr < children.length; childNr++) {
            Directory dir = children[childNr];
            if (dir.name().equals(childDirName)) {
                return dir;
            }
        }
        return null;
    }
}

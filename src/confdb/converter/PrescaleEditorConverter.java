package confdb.converter;

import java.util.HashMap;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.*;
import java.util.Scanner;

import confdb.data.*;

import confdb.db.ConfDB;
import confdb.db.ConfDBSetups;
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

    public PrescaleEditorConverter() {
    }

    //
    // main method
    //
    public static void main(String[] args) {
       
        String basePSDirname = "/users/sharper/2025/test1/prescales/v1";
        String prescalesCfgName = "prescales";
               
        String pstblfile = "";
        String configName = ""; //name of the config to export prescales to ultimately
        String configDBDSN = ""; //the dsn of the db where the confg is located
        String configDBUser = ""; //the user for the DB where that config is located
        String configDBPasswd = ""; //the password for the DB where that config is located
        String psDBDSN = ""; //the dsn of the db where the prescales will be imported to
        String psDBUser = ""; //the name of the db where the prescales will be imported to
        String psDBPasswd = ""; //the password for the DB where the prescales will be imported to
        

        for (int iarg = 0; iarg < args.length; iarg++) {
            String arg = args[iarg];
            if (arg.equals("--cfg") || arg.equals("--configName")) {
                iarg++;
                configName = args[iarg];
            } else if(arg.equals("--cfgdbdsn")){
                iarg++;
                configDBDSN = args[iarg];
            } else if(arg.equals("--cfgdbuser")){
                iarg++;
                configDBUser = args[iarg];
            }else if(arg.equals("--cfgdbpasswd")){
                iarg++;
                configDBPasswd = args[iarg];
            }else if(arg.equals("--psdbdsn")){
                iarg++;
                psDBDSN = args[iarg];
            }else if(arg.equals("--psdbuser")){
                iarg++;
                psDBUser = args[iarg];
            }else if (arg.equals("--psdbpasswd")){
                iarg++;
                psDBPasswd = args[iarg];
            }else if (arg.equals("--pstblfile")) {
                iarg++;
                pstblfile = args[iarg];
            } else {
                System.err.println("ERROR: invalid option '" + arg + "'!");
                System.exit(0);
            }
        }

        
        if (configName.length() == 0) {
            System.err.println("ERROR: no configuration specified!");
            System.exit(0);
        }
    

        try {
            Configuration config = new Configuration();
            SoftwareRelease release = new SoftwareRelease();
            ConverterBase cfgCnv = new ConverterBase("python", "oracle", "jdbc:oracle:thin:@//"+configDBDSN, configDBUser, configDBPasswd);
            ConfDB cfgDB = cfgCnv.getDatabase();
            int configId = cfgDB.getConfigNewId(configName);

            String releaseTag = cfgDB.getReleaseTagForConfig(configId);
            ConverterBase psCnv = new ConverterBase("python", "oracle", "jdbc:oracle:thin:@//"+psDBDSN, psDBUser, psDBPasswd);
            ConfDB psDB = psCnv.getDatabase();

            // check if we need to copy the release over or not to the psDB
            // this should only be the case when we're testing on the dev db
            int releaseIndex = Arrays.binarySearch(psDB.getReleaseTagsSorted(),releaseTag);
            if (releaseIndex < 0) {
                cfgDB.loadSoftwareRelease(releaseTag, release);
                psDB.insertRelease(releaseTag,release);
            }
            psDB.loadSoftwareRelease(releaseTag, release);

            Directory rootDir = psDB.loadConfigurationTree();

            
            // first we check if the basePSDirname already exist and exit if not
            Directory basePSDir = getDirectoryByPath(rootDir, basePSDirname, false, psDB);
            if (basePSDir == null) {
                System.err.println("ERROR: base directory " + basePSDirname + " not found, this ");
                return;
            }           
            // its easier based on how directories are created start again from the rootDir
            // even though we have the basePSDir
            Directory configDir = getDirectoryByPath(rootDir, basePSDirname + configName, true, psDB);
            ConfigInfo cfgInfo = null;
            for(int configInfoNr=0;configInfoNr<configDir.configInfoCount();configInfoNr++){
                if(configDir.configInfo(configInfoNr).name().equals(prescalesCfgName)){
                    cfgInfo = configDir.configInfo(configInfoNr);
                    break;
                }                
            }
            if (cfgInfo == null) {
                cfgInfo = new ConfigInfo(prescalesCfgName, configDir, releaseTag);             
            }
            config.initialize(cfgInfo, release);

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
            config.insertService(0, "PrescaleService");

            PrescaleTableModel psTblModel = new PrescaleTableModel();
            psTblModel.initialize(config);
            //psTblModel.updatePrescaleService(config);
            psTblModel.updatePrescaleTableFromFile(pstblfile, true);
            psTblModel.updatePrescaleService(config);

            ServiceInstance psService = config.service("PrescaleService");
            if (psService == null) {
                System.err.println("No PrescaleService found.");

            } else {
                System.out.println("labels " + psService.parameter("lvl1Labels").valueAsString());
                System.out.println("procesname " + config.processName());
            }

            psDB.insertConfiguration(config, "pstool", config.processName(), "prescale table update");
            //the backend server looks for this line in the logs to make sure the update was successful
            System.out.println("PSEditorConverter: WRITE SUCCESSFUL");
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
                //we need to skip to the paths
                //the first line is the table name and the second line is the column names
                //or the first line is the column names, so we either skip one or two lines
                if (tableScanner.nextLine().startsWith("tablename:")) {
                    tableScanner.nextLine(); //there was a table name so also need to skip the column names names    
                }                
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

package confdb.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Clob;
import java.sql.Types;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import java.util.LinkedList;
import java.util.Stack;

import java.io.*;

import confdb.data.*;
import confdb.gui.AboutDialog;
import confdb.gui.errorNotificationPanel;

import java.math.BigInteger;

import oracle.jdbc.pool.OracleDataSource;


/**
 * ConfDB
 * ------
 * @author Philipp Schieferdecker
 *
 * Handle all database access operations.
 */
public class ConfDB
{
    //
    // member data
    //

    /** define database arch types */
    public static final String dbTypeMySQL  = "mysql";
    public static final String dbTypeOracle = "oracle";

    /** define database table names */
    public static final String tableEDSourceTemplates = "EDSourceTemplates";
    public static final String tableESSourceTemplates = "ESSourceTemplates";
    public static final String tableESModuleTemplates = "ESModuleTemplates";
    public static final String tableServiceTemplates  = "ServiceTemplates";
    public static final String tableModuleTemplates   = "ModuleTemplates";
   
    /** database connector object, handles access to various DBMSs */
    private IDatabaseConnector dbConnector = null;

	/** database type */
    private String dbType = null;
    
    /** database url */
    private String dbUrl = null;
    
    /** database user */
    private String dbUser = null;
    
    /** database password */
    private String dbPwrd = null;
    
    // Db parameters: -- ONLY info. dbUrl already contain this.
    private String dbHost = null;
    private String dbPort = null;
    private String dbName = null;
    
    // database features:
    private boolean extraPathFieldsAvailability = false;
    private boolean operatorFieldForSequencesAvailability = true; 
    
    //sv
    private Integer countingParamIds=0;
    
    /** template table name hash map */
    private HashMap<String,String> templateTableNameHashMap = null;
    
    /** module type id hash map */
    private HashMap<String,Integer> moduleTypeIdHashMap = null;
    
    /** parameter type id hash map */
    private HashMap<String,Integer> paramTypeIdHashMap = null;
    
    /** vector/scalar parameter hash map */
    private HashMap<Integer,Boolean> isVectorParamHashMap = null;
    
    /** 'insert parameter' sql statement hash map */
    private HashMap<String,PreparedStatement> insertParameterHashMap = null;
    
    /** Original parameters DB hash map */
    private HashMap<Integer,Integer> dbidParamHashMap = null;

    /** Original parameters DB hash map */
    private HashMap<Integer,Integer> dbidTemplateHashMap = null;

    /** prepared sql statements */
    private PreparedStatement psSelectModuleTypes                 = null;
    private PreparedStatement psSelectParameterTypes              = null;

    private PreparedStatement psSelectDirectories                 = null;
    private PreparedStatement psSelectConfigurations              = null;
    private PreparedStatement psSelectLockedConfigurations        = null;
    private PreparedStatement psSelectUsersForLockedConfigs       = null;
    
    private PreparedStatement psSelectConfigNames                 = null;
    private PreparedStatement psSelectConfigNamesByRelease        = null;
    private PreparedStatement psSelectDirectoryId                 = null;
    private PreparedStatement psSelectConfigurationId             = null;
    private PreparedStatement psSelectConfigurationIdLatest       = null;
    private PreparedStatement psSelectConfigurationCreated        = null;

    private PreparedStatement psSelectReleaseTags                 = null;
    private PreparedStatement psSelectReleaseTagsSorted           = null;
    private PreparedStatement psSelectReleaseId                   = null;
    private PreparedStatement psSelectReleaseTag                  = null;
    private PreparedStatement psSelectReleaseTagForConfig         = null;
    
    private PreparedStatement psSelectSoftwareSubsystems          = null;
    private PreparedStatement psSelectSoftwarePackages            = null;

    private PreparedStatement psSelectEDSourceTemplate            = null;
    private PreparedStatement psSelectESSourceTemplate            = null;
    private PreparedStatement psSelectESModuleTemplate            = null;
    private PreparedStatement psSelectServiceTemplate             = null;
    private PreparedStatement psSelectModuleTemplate              = null;

    private PreparedStatement psSelectStreams                     = null;
    private PreparedStatement psSelectPrimaryDatasets             = null;
    //    private PreparedStatement psSelectStreamEntries               = null;
    private PreparedStatement psSelectPrimaryDatasetEntries       = null;


    private PreparedStatement psSelectPSetsForConfig              = null;
    private PreparedStatement psSelectEDSourcesForConfig          = null;
    private PreparedStatement psSelectESSourcesForConfig          = null;
    private PreparedStatement psSelectESModulesForConfig          = null;
    private PreparedStatement psSelectServicesForConfig           = null;
    private PreparedStatement psSelectSequencesForConfig          = null;
    private PreparedStatement psSelectPathsForConfig              = null;
    private PreparedStatement psSelectContentForConfig			  = null;

    private PreparedStatement psSelectModulesForSeq               = null;
    private PreparedStatement psSelectModulesForPath              = null;

    private PreparedStatement psSelectEDSourceTemplatesForRelease = null;
    private PreparedStatement psSelectESSourceTemplatesForRelease = null;
    private PreparedStatement psSelectESModuleTemplatesForRelease = null;
    private PreparedStatement psSelectServiceTemplatesForRelease  = null;
    private PreparedStatement psSelectModuleTemplatesForRelease   = null;

    private PreparedStatement psSelectParametersForSuperId        = null;
    private PreparedStatement psSelectPSetsForSuperId             = null;
    private PreparedStatement psSelectVPSetsForSuperId            = null;
    
    private PreparedStatement psSelectPSetId                      = null;
    private PreparedStatement psSelectEDSourceId                  = null;
    private PreparedStatement psSelectESSourceId                  = null;
    private PreparedStatement psSelectESModuleId                  = null;
    private PreparedStatement psSelectServiceId                   = null;
    private PreparedStatement psSelectSequenceId                  = null;
    private PreparedStatement psSelectPathId                      = null;
    private PreparedStatement psSelectModuleIdBySeq               = null;
    private PreparedStatement psSelectModuleIdByPath              = null;
    
    private PreparedStatement psSelectStreamByEventContent		  = null;
    private PreparedStatement psSelectStreamAssocByStream		  = null;
    private PreparedStatement psSelectECStatementByEventContent	  = null;
    private PreparedStatement psSelectECStatementByECStatement	  = null;

    private PreparedStatement psSelectTemplateId                  = null;

    private PreparedStatement psSelectReleaseCount                = null;
    private PreparedStatement psSelectConfigurationCount          = null;
    private PreparedStatement psSelectDirectoryCount              = null;
    private PreparedStatement psSelectSuperIdCount                = null;
    private PreparedStatement psSelectEDSourceTemplateCount       = null;
    private PreparedStatement psSelectEDSourceCount               = null;
    private PreparedStatement psSelectESSourceTemplateCount       = null;
    private PreparedStatement psSelectESSourceCount               = null;
    private PreparedStatement psSelectESModuleTemplateCount       = null;
    private PreparedStatement psSelectESModuleCount               = null;
    private PreparedStatement psSelectServiceTemplateCount        = null;
    private PreparedStatement psSelectServiceCount                = null;
    private PreparedStatement psSelectModuleTemplateCount         = null;
    private PreparedStatement psSelectModuleCount                 = null;
    private PreparedStatement psSelectSequenceCount               = null;
    private PreparedStatement psSelectPathCount                   = null;
    private PreparedStatement psSelectParameterCount              = null;
    private PreparedStatement psSelectParameterSetCount           = null;
    private PreparedStatement psSelectVecParameterSetCount        = null;

    private PreparedStatement psSelectEventContentEntries         = null;
    private PreparedStatement psSelectStreamEntries               = null;
    private PreparedStatement psSelectEventContentStatements      = null; 


    private PreparedStatement psInsertDirectory                   = null;
    private PreparedStatement psInsertConfiguration               = null;
    private PreparedStatement psInsertConfigurationVers           = null;
    private PreparedStatement psFindConfiguration                 = null;
    private PreparedStatement psInsertConfigurationLock           = null;
    //Insert Event Content
    private PreparedStatement psCheckContents                    = null;
    private PreparedStatement psInsertContents                    = null;
    private PreparedStatement psInsertContentIds                 = null;
    private PreparedStatement psInsertContentsConfigAssoc         = null;
    private PreparedStatement psInsertEventContentStatements      = null;
    private PreparedStatement psInsertStreams                     = null;
    private PreparedStatement psInsertStreamsIds                     = null;
    private PreparedStatement psInsertPrimaryDatasets             = null;
    private PreparedStatement psInsertECStreamAssoc               = null;
    private PreparedStatement psInsertPathStreamPDAssoc           = null;
    private PreparedStatement psInsertStreamDatasetAssoc          = null;
    private PreparedStatement psSelectStatementId                 = null;
    private PreparedStatement psSelectDatasetEntries              = null;
    private PreparedStatement psInsertECStatementAssoc            = null;
    private PreparedStatement psSelectPathStreamDatasetEntries    = null;

    //Work on going

    private PreparedStatement psInsertSuperId                     = null;
    private PreparedStatement psInsertGlobalPSet                  = null;
    private PreparedStatement psInsertEDSource                    = null;
    private PreparedStatement psInsertConfigEDSourceAssoc         = null;
    private PreparedStatement psInsertESSource                    = null;
    private PreparedStatement psInsertConfigESSourceAssoc         = null;
    private PreparedStatement psInsertESModule                    = null;
    private PreparedStatement psInsertConfigESModuleAssoc         = null;
    private PreparedStatement psInsertService                     = null;
    private PreparedStatement psInsertConfigServiceAssoc          = null;
    private PreparedStatement psCheckPathName                     = null;
    private PreparedStatement psCheckPathNoum                     = null;
    private PreparedStatement psInsertPathNoum                    = null;
    private PreparedStatement psInsertPath                        = null;
    private PreparedStatement psInsertPathIds                     = null;
    private PreparedStatement psInsertConfigPathAssoc             = null;
    private PreparedStatement psInsertHPathIds                    = null;
    private PreparedStatement psInsertHPathId2Path                = null;
    private PreparedStatement psInsertHPathId2Uq                  = null;
    private PreparedStatement psSelectPathId2Uq                   = null;
    private PreparedStatement psCheckHPathIdCrc                   = null;
    private PreparedStatement psInsertConfigHPathAssoc            = null;
    private PreparedStatement psInsertSequence                    = null;
    private PreparedStatement psInsertConfigSequenceAssoc         = null;
    private PreparedStatement psInsertModule                      = null;
    private PreparedStatement psInsertSequenceModuleAssoc         = null;
    private PreparedStatement psInsertSequenceOutputModuleAssoc   = null;
    private PreparedStatement psInsertPathPathAssoc               = null;
    private PreparedStatement psInsertPathSequenceAssoc           = null;
    private PreparedStatement psInsertSequenceSequenceAssoc       = null;
    private PreparedStatement psInsertPathModuleAssoc             = null;
    private PreparedStatement psInsertPathOutputModuleAssoc       = null;
    private PreparedStatement psInsertSuperIdReleaseAssoc         = null;
    private PreparedStatement psInsertServiceTemplate             = null;
    private PreparedStatement psInsertEDSourceTemplate            = null;
    private PreparedStatement psInsertESSourceTemplate            = null;
    private PreparedStatement psInsertESModuleTemplate            = null;
    private PreparedStatement psInsertModuleTemplate              = null;
    private PreparedStatement psInsertGPset 	                  = null;
    private PreparedStatement psInsertParameterGPset              = null;
    private PreparedStatement psInsertParameterEDS                = null;
    private PreparedStatement psInsertParameterESS                = null;
    private PreparedStatement psInsertParameterESM                = null;
    private PreparedStatement psInsertParameterSRV                = null;
    private PreparedStatement psInsertParameterMOE                = null;
    private PreparedStatement psInsertParameterMODT               = null;
    private PreparedStatement psInsertParameterOUTM               = null;
    private PreparedStatement psInsertPathElement                 = null;
    private PreparedStatement psInsertPathElementAssoc            = null;
    private PreparedStatement psInsertHPathElement                = null;
    private PreparedStatement psInsertMoElement                   = null;
    private PreparedStatement psInsertMod2Templ                    = null;
    private PreparedStatement psInsertPae2Moe                     = null;
    private PreparedStatement psInsertParameterSet                = null;
    private PreparedStatement psInsertVecParameterSet             = null;
    private PreparedStatement psInsertSuperIdParamAssoc           = null;
    private PreparedStatement psInsertSuperIdParamSetAssoc        = null;
    private PreparedStatement psInsertSuperIdVecParamSetAssoc     = null;
    private PreparedStatement psInsertBoolParamValue              = null;
    private PreparedStatement psInsertInt32ParamValue             = null;
    private PreparedStatement psInsertUInt32ParamValue            = null;
    private PreparedStatement psInsertInt64ParamValue             = null;
    private PreparedStatement psInsertUInt64ParamValue            = null;
    private PreparedStatement psInsertDoubleParamValue            = null;
    private PreparedStatement psInsertStringParamValue            = null;
    private PreparedStatement psInsertEventIDParamValue           = null;
    private PreparedStatement psInsertInputTagParamValue          = null;
    private PreparedStatement psInsertFileInPathParamValue        = null;
    private PreparedStatement psInsertVInt32ParamValue            = null;
    private PreparedStatement psInsertVUInt32ParamValue           = null;
    private PreparedStatement psInsertVInt64ParamValue            = null;
    private PreparedStatement psInsertVUInt64ParamValue           = null;
    private PreparedStatement psInsertVDoubleParamValue           = null;
    private PreparedStatement psInsertVStringParamValue           = null;
    private PreparedStatement psInsertVEventIDParamValue          = null;
    private PreparedStatement psInsertVInputTagParamValue         = null;

    private PreparedStatement psDeleteDirectory                   = null;
    private PreparedStatement psDeleteLock                        = null;
    private PreparedStatement psDeleteConfiguration               = null;
    private PreparedStatement psDeleteSoftwareRelease             = null;

    private PreparedStatement psDeletePSetsFromConfig             = null;
    private PreparedStatement psDeleteEDSourcesFromConfig         = null;
    private PreparedStatement psDeleteESSourcesFromConfig         = null;
    private PreparedStatement psDeleteESModulesFromConfig         = null;
    private PreparedStatement psDeleteServicesFromConfig          = null;
    private PreparedStatement psDeleteSequencesFromConfig         = null;
    private PreparedStatement psDeletePathsFromConfig             = null;
    private PreparedStatement psDeleteContentFromConfig			  = null;

    private PreparedStatement psDeleteChildSeqsFromParentSeq      = null;
    private PreparedStatement psDeleteChildSeqFromParentSeqs      = null;
    private PreparedStatement psDeleteChildSeqsFromParentPath     = null;
    private PreparedStatement psDeleteChildSeqFromParentPaths     = null;
    private PreparedStatement psDeleteChildPathsFromParentPath    = null;
    private PreparedStatement psDeleteChildPathFromParentPaths    = null;
    
    private PreparedStatement psDeletePathStreamDataSetAssoc	  = null;
    private PreparedStatement psDeletePathOutputModAssoc		  = null;
    
    private PreparedStatement psDeleteECStreamFromEventCont	  	  = null;
    private PreparedStatement psDeleteECStatementFromEventCont	  = null;
    private PreparedStatement psDeleteEventContentStatement		  = null;
    
    private PreparedStatement psDeleteModulesFromSeq              = null;
    private PreparedStatement psDeleteModulesFromPath             = null;    

    private PreparedStatement psDeleteTemplateFromRelease         = null;
    
    private PreparedStatement psDeleteParametersForSuperId        = null;
    private PreparedStatement psDeletePSetsForSuperId             = null;
    private PreparedStatement psDeleteVPSetsForSuperId            = null;
        
    private PreparedStatement psDeleteSuperId                     = null;
    private PreparedStatement psDeleteParameter                   = null;
    private PreparedStatement psDeletePSet                        = null;
    private PreparedStatement psDeleteVPSet                       = null;
    private PreparedStatement psDeleteSequence                    = null;
    private PreparedStatement psDeletePath                        = null;
    
    private CallableStatement csLoadTemplate                      = null;
    private CallableStatement csLoadTemplates                     = null;
    private CallableStatement csLoadTemplatesForConfig            = null;
    private CallableStatement csLoadConfiguration                 = null;

    private PreparedStatement psSelectTemplates                   = null;
    private PreparedStatement psSelectInstances                   = null;
    private PreparedStatement psSelectParametersTemplates         = null;
    private PreparedStatement psSelectParameters                  = null;
    private PreparedStatement psSelectBooleanValues               = null;
    private PreparedStatement psSelectIntValues                   = null;
    private PreparedStatement psSelectRealValues                  = null;
    private PreparedStatement psSelectStringValues                = null;
    private PreparedStatement psSelectCLOBsValues                 = null; // get Clobs - bug75950
    private PreparedStatement psSelectPathEntries                 = null;
    private PreparedStatement psSelectSequenceEntries             = null;
    private PreparedStatement psSelectSequenceEntriesAndOperator  = null; // bug #91797
    private PreparedStatement psPrepareSequenceEntries			  = null; // bug #91797
    


    private  PreparedStatement psSelectSoftwarePackageId           = null;
    private  PreparedStatement psInsertSoftwarePackage             = null;
    
    
    private  PreparedStatement psInsertReleaseTag                  = null;
    private  PreparedStatement psSelectSoftwareSubsystemId         = null;
    private  PreparedStatement psInsertSoftwareSubsystem           = null;
    private  PreparedStatement psInsertEDSourceTemplateRelease     = null;
    private  PreparedStatement psInsertESSourceTemplateRelease     = null;
    private  PreparedStatement psInsertESModuleTemplateRelease     = null;
    private  PreparedStatement psInsertServiceTemplateRelease     = null;
    private  PreparedStatement psInsertModuleTemplateRelease     = null;
    
    // New path fields extension:
    private  PreparedStatement psCheckPathFieldsExistence			= null;
    private  PreparedStatement psSelectPathExtraFields				= null;
    private	 PreparedStatement psInsertPathDescription				= null;
    
    // bug #91797: ConfDB operator IGNORE/NEGATE also for modules in a sequence.
    private	PreparedStatement psCheckOperatorForModuleSequenceAssoc	= null;
    

    private ArrayList<PreparedStatement> preparedStatements =
	new ArrayList<PreparedStatement>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ConfDB()
    {
	// template table name hash map
	templateTableNameHashMap = new HashMap<String,String>();
	templateTableNameHashMap.put("Service", tableServiceTemplates);
	templateTableNameHashMap.put("EDSource",tableEDSourceTemplates);
	templateTableNameHashMap.put("ESSource",tableESSourceTemplates);
	templateTableNameHashMap.put("ESModule",tableESModuleTemplates);
    }
    
    
    //
    // member functions
    //

    /** retrieve db url */
    public String dbUrl() { return this.dbUrl; }
    
    /** get db metadata **/
    public synchronized DatabaseMetaData getDatabaseMetaData() {
		try {
			return dbConnector.getConnection().getMetaData();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    

    /** getTnsnameFormat
     * ----------------------------------------------------------------
     * Use Oracle Java extensions to create TNSName Connection String
     * bug 86323.
     * */
    public String getTnsnameFormat() {
		OracleDataSource ods;
		try {
			ods = new OracleDataSource();
			ods.setUser(dbUser);
			ods.setPassword(dbPwrd);
			ods.setDriverType("thin");
			ods.setServiceName(dbName);
			ods.setServerName(dbHost);
			ods.setPortNumber(Integer.parseInt(dbPort));
			
			return ods.getURL();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "ERROR TNSNAME FORMAT";
    }
    
    public String setDbParameters(String dbPassword, String dbServiceName, String dbHostName, String dbPortNumber) {
        dbPwrd = dbPassword; 
        dbHost = dbHostName; 
        dbPort = dbPortNumber;
        dbName = dbServiceName;
        
        //return getTnsnameFormat();
        return getDbURL(); // allow load balancing connection string.
    }
    
    // Set the URL. All other parameters will be ignored. This will be used for load balancing.
    public String getDbURL() {

		String url = "jdbc:oracle:thin:@(DESCRIPTION =";
		String[] hosts = dbHost.split(",");
		
		if(hosts.length == 0) return "ERROR TNSNAME FORMAT";
		
		for(int i = 0; i < hosts.length; i++) {
				url+= "(ADDRESS = (PROTOCOL = TCP)(HOST = "+hosts[i]+")(PORT = 10121))\n";
		}

	    url+="(ENABLE=BROKEN) ";
	    url+="(LOAD_BALANCE = yes) ";
	    url+="(CONNECT_DATA = ";
	    url+="  (SERVER = DEDICATED) ";
	    url+=" (SERVICE_NAME = "+dbName+") ";
	    url+="   (FAILOVER_MODE = (TYPE = SELECT)(METHOD = BASIC)(RETRIES = 200)(DELAY = 15)) ";
	    url+=") )";
		
		return url;
    }
    
    public String getHostName() 	{ return dbHost; }
    public String getPortNumber() 	{ return dbPort; }
    public String getDbName()		{ return dbName; }
    public String getDbUser()		{ return dbUser; }
    
    // methods to retrieve db features:
    public boolean getExtraPathFieldsAvailability() { return extraPathFieldsAvailability; 	}
    public boolean getOperatorFieldForSequencesAvailability() { return operatorFieldForSequencesAvailability; }
    
    
    /** close all prepared statements */
    void closePreparedStatements() throws DatabaseException
    {
	for (PreparedStatement ps : preparedStatements) {
	    try { ps.close(); }
	    catch (SQLException e) {
	    	throw new DatabaseException("ConfDB::closePreparedStatements() failed (SQL)", e);
	    }catch (Exception e) {
	    	throw new DatabaseException("ConfDB::closePreparedStatements() failed", e);
	    }
	}
	preparedStatements.clear();
    }


    /** connect to the database */
    public synchronized void connect(String dbType,String dbUrl,String dbUser,String dbPwrd)
	throws DatabaseException
    {
	this.dbType = dbType;
	this.dbUrl  = dbUrl;
	this.dbUser = dbUser;
	this.dbPwrd  = dbPwrd;
	if (dbType.equals(dbTypeMySQL))
	    dbConnector = new MySQLDatabaseConnector(dbUrl,dbUser,dbPwrd);
	else if (dbType.equals(dbTypeOracle))
	    dbConnector = new OracleDatabaseConnector(dbUrl,dbUser,dbPwrd);
	
	dbConnector.openConnection();
	prepareStatements();
	checkDbFeatures(); // Check Clobs and Documentation fields availability.
    }
    
    /** connect to the database */
    public synchronized void connect() throws DatabaseException
    {
	if (dbType.equals(dbTypeMySQL))
	    dbConnector = new MySQLDatabaseConnector(dbUrl,dbUser,dbPwrd);
	else if (dbType.equals(dbTypeOracle))
	    dbConnector = new OracleDatabaseConnector(dbUrl,dbUser,dbPwrd);

	dbConnector.openConnection();
	prepareStatements();
	checkDbFeatures(); // Check Clobs and Documentation fields availability.
    }
    
    /** connect to the database */
    public synchronized void connect(Connection connection) throws DatabaseException
    {
	this.dbType = dbTypeOracle;
	this.dbUrl  = "UNKNOWN";
	dbConnector = new OracleDatabaseConnector(connection);
	prepareStatements();
	checkDbFeatures(); // Check Clobs and Documentation fields availability.
    }
    
    /** disconnect from database */
    public synchronized void disconnect() throws DatabaseException
    {
	if (dbConnector!=null) {
	    closePreparedStatements();
	    dbConnector.closeConnection();
	    dbConnector = null;
	}
    }
    
    /** reconnect to the database, if the connection appears to be down */
    public synchronized void reconnect() throws DatabaseException
    {
		if (dbConnector==null) return;
		ResultSet rs = null;
		
	     int retryCount = 5;
	     boolean transactionCompleted = false;
	     do {
	    	 Date dNow = new Date();
	         SimpleDateFormat ft = new SimpleDateFormat ("[yyyy.MM.dd@hh:mm:ss a]");
	    	 
	    	 if(retryCount != 5)
	    		 System.err.println("[ConfDB::reconnect]"+ft.format(dNow)+" Trying to connect... attemp (" +  (5 - retryCount) + ")");
	    	 
			try {
			    rs = psSelectUsersForLockedConfigs.executeQuery();
                            if (false) throw new SQLException();
			    
			    // If no exception is raised then reconnection is complete.
			    transactionCompleted = true;
                           
			} catch (SQLException e) {
				retryCount--;	
				
				System.err.println("[ConfDB::reconnect]"+ft.format(dNow)+" SQLException: " + e.getMessage());
				System.err.println("[ConfDB::reconnect]"+ft.format(dNow)+" ErrorCode:    " + e.getErrorCode());
				
			    if (!(dbConnector instanceof MySQLDatabaseConnector)&&!(dbConnector instanceof OracleDatabaseConnector))
			    	throw new DatabaseException("ConfDB::reconnect(): unknown connector type!",e);

			    try {
					closePreparedStatements();
			    } catch (DatabaseException dbe) {
			    	/* Ignore to reconnect */
			    	System.err.println("[Confdb::reconnect]"+ft.format(dNow)+" closePreparedStatements() FAILED!");
			    }
			    
			    try {
					dbConnector.closeConnection();
			    } catch (DatabaseException dbe) {
			    	/* Ignore to reconnect */
			    	System.err.println("[Confdb::reconnect]"+ft.format(dNow)+" dbConnector.closeConnection() FAILED!");
			    }
			    
			    try {
					dbConnector.openConnection();
			    } catch (DatabaseException dbe) {
			    	/* Ignore to reconnect */
			    	System.err.println("[Confdb::reconnect]"+ft.format(dNow)+" dbConnector.openConnection() FAILED!");
			    }					
				
			    
			    try {
			    	prepareStatements();
			    } catch (DatabaseException dbe) {
			    	/* Ignore to reconnect */
			    	System.err.println("[Confdb::reconnect]"+ft.format(dNow)+" prepareStatements() FAILED!");
			    }					
			    

			} finally {
			     if (rs != null) {
				   	  try {
				   	      rs.close();
				   	  } catch (SQLException sqlEx) {
				   		System.err.println("[ConfDB::reconnect()]"+ft.format(dNow)+" Error closing ResultSet! " + sqlEx.getMessage());
				   	  }
			     }
		         
			     if (dbConnector != null) dbConnector.release(rs);
			}
	     } while (!transactionCompleted && (retryCount > 0));
	     
	     // Raise exception when unable to connect.
	     if(!transactionCompleted)  throw new DatabaseException("ConfDB::reconnect(): Unable to connect!");
	     else if(retryCount < 5) System.out.println("[ConfDB::reconnect] connection reestablished!"); 
	     
    }
    

    public synchronized IDatabaseConnector getDbConnector() {
		return dbConnector;
	}
    
    /** Check database features.
     * NOTE: Depending on this, database queries and 
     * app. workflow would be modified. */
    public synchronized void checkDbFeatures() {
    	try {
            extraPathFieldsAvailability = checkExtraPathFields();
            operatorFieldForSequencesAvailability = checkOperatorFieldForSequences();
    	}
    	catch (DatabaseException e) {
    	    e.printStackTrace(); // DEBUG
    	}

    }

    
    /** list number of entries in (some) tables */
    public synchronized void listCounts() throws DatabaseException
    {
	reconnect();
	
	ResultSet rs = null;
	try {
	    rs = psSelectReleaseCount.executeQuery();
	    rs.next(); int releaseCount = rs.getInt(1);
	    rs = psSelectConfigurationCount.executeQuery();
	    rs.next(); int configurationCount = rs.getInt(1);
	    rs = psSelectDirectoryCount.executeQuery();
	    rs.next(); int directoryCount = rs.getInt(1);
	    rs = psSelectSuperIdCount.executeQuery();
	    rs.next(); int superIdCount = rs.getInt(1);
	    rs = psSelectEDSourceTemplateCount.executeQuery();
	    rs.next(); int edsourceTemplateCount = rs.getInt(1);
	    rs = psSelectEDSourceCount.executeQuery();
	    rs.next(); int edsourceCount = rs.getInt(1);
	    rs = psSelectESSourceTemplateCount.executeQuery();
	    rs.next(); int essourceTemplateCount = rs.getInt(1);
	    rs = psSelectESSourceCount.executeQuery();
	    rs.next(); int essourceCount = rs.getInt(1);
	    rs = psSelectESModuleTemplateCount.executeQuery();
	    rs.next(); int esmoduleTemplateCount = rs.getInt(1);
	    rs = psSelectESModuleCount.executeQuery();
	    rs.next(); int esmoduleCount = rs.getInt(1);
	    rs = psSelectServiceTemplateCount.executeQuery();
	    rs.next(); int serviceTemplateCount = rs.getInt(1);
	    rs = psSelectServiceCount.executeQuery();
	    rs.next(); int serviceCount = rs.getInt(1);
	    rs = psSelectModuleTemplateCount.executeQuery();
	    rs.next(); int moduleTemplateCount = rs.getInt(1);
	    rs = psSelectModuleCount.executeQuery();
	    rs.next(); int moduleCount = rs.getInt(1);
	    rs = psSelectSequenceCount.executeQuery();
	    rs.next(); int sequenceCount = rs.getInt(1);
	    rs = psSelectPathCount.executeQuery();
	    rs.next(); int pathCount = rs.getInt(1);
	    rs = psSelectParameterCount.executeQuery();
	    rs.next(); int parameterCount = rs.getInt(1);
	    rs = psSelectParameterSetCount.executeQuery();
	    rs.next(); int parameterSetCount = rs.getInt(1);
	    rs = psSelectVecParameterSetCount.executeQuery();
	    rs.next(); int vecParameterSetCount = rs.getInt(1);

	    System.out.println("\n"+
			       "\nConfigurations: "+configurationCount+
			       "\nReleases:       "+releaseCount+
			       "\nDirectories:    "+directoryCount+
			       "\nSuperIds:       "+superIdCount+
			       "\nEDSources (T):  "+edsourceCount+
			       " ("+edsourceTemplateCount+")"+
			       "\nESSources (T):  "+essourceCount+
			       " ("+essourceTemplateCount+")"+
			       "\nESModules (T):  "+esmoduleCount+
			       " ("+esmoduleTemplateCount+")"+
			       "\nServices (T):   "+serviceCount+
			       " ("+serviceTemplateCount+")"+
			       "\nModules (T):    "+moduleCount+
			       " ("+moduleTemplateCount+")"+
			       "\nSequences:      "+sequenceCount+
			       "\nPaths:          "+pathCount+
			       "\nParameters:     "+parameterCount+
			       "\nPSets:          "+parameterSetCount+
			       "\nVPSets:         "+vecParameterSetCount+
			       "\n");
	}
	catch (SQLException e) {
	    String errMsg = "ConfDB::listCounts() failed:"+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    
    /** load information about all stored configurations */
    public synchronized Directory loadConfigurationTree() throws DatabaseException
    {
	reconnect();
	
        ////System.out.println("loadConfigTree ");
	Directory rootDir = null;
	ResultSet rs = null;
	try {
	    HashMap<Integer,Directory> directoryHashMap =
		new HashMap<Integer,Directory>();

	    // DEBUG
	    //long startTime = System.currentTimeMillis();

	    rs = psSelectDirectories.executeQuery();
        ////System.out.println("Selected Directories ");

	    // DEBUG
	    //long dir1Time = System.currentTimeMillis();
	    
	    while (rs.next()) {

		int   dirId        = rs.getInt(1);
		int   parentDirId  = rs.getInt(2);
		String dirName     = rs.getString(3);
		String dirCreated  = rs.getTimestamp(4).toString();

                ////System.out.println("Retrieved Dirs: "+dirId+"- parent "+parentDirId+" "+dirName+" "+dirCreated);
		
		if (directoryHashMap.size()==0) {
                    ////System.out.println("New root DIrectrory: "+dirId+"- "+dirName+" "+dirCreated);
		    rootDir = new Directory(dirId,dirName,dirCreated,null);
                    ////System.out.println("New root DIrectrory  done");
		    directoryHashMap.put(dirId,rootDir);
		}
		else {
                    ////System.out.println("Now checking parenmt dir - "+parentDirId);

		    if (!directoryHashMap.containsKey(parentDirId))
			throw new DatabaseException("parentDir not found in DB"+
						    " (parentDirId="+parentDirId+
						    ")");
                    
                    ////System.out.println("getting parentdir "+parentDirId);
		    Directory parentDir = directoryHashMap.get(parentDirId);
                    ////System.out.println("New DIrectrory with parent: "+dirId+"- "+dirName+" "+dirCreated+ parentDirId);
		    Directory newDir    = new Directory(dirId,
							dirName,
							dirCreated,
							parentDir);
                    ////System.out.println("New DIrectrory with parent - done");
		    parentDir.addChildDir(newDir);
                    ////System.out.println("New DIrectrory - done adding child");
		    directoryHashMap.put(dirId,newDir);
                    ////System.out.println("New DIrectrory - done putting hash");
		}
	    }

	    // DEBUG
	    //long dir2Time = System.currentTimeMillis();
	    
            ////System.out.println("Done with Retrieving Dirs: ");
	    // retrieve list of configurations for all directories
	    HashMap<String,ConfigInfo> configHashMap =
		new HashMap<String,ConfigInfo>();

            ////System.out.println("Try to query COnfs");

	    rs = psSelectConfigurations.executeQuery();
            //svdbgSystem.out.println("Queried COnfs");

	    // DEBUG
	    //long config1Time = System.currentTimeMillis();
	    
	    while (rs.next()) {
		int    configId          = rs.getInt(1);
		int    parentDirId       = rs.getInt(2);
		String configName        = rs.getString(3);
		int    configVersion     = rs.getInt(4);
		String configCreated     = rs.getTimestamp(5).toString();
		String configCreator     = rs.getString(6);
		String configReleaseTag  = rs.getString(7);
		String configProcessName = rs.getString(8);
		String configComment     = rs.getString(9);

                //System.out.println("Retrieved Conf: "+configName+" "+configCreated+" "+configReleaseTag);
		
		if (configComment==null) configComment="";

		Directory dir = directoryHashMap.get(parentDirId);
		if (dir==null) {
		    String errMsg =
			"ConfDB::loadConfigurationTree(): can't find directory "+
			"for parentDirId="+parentDirId+".";
		    throw new DatabaseException(errMsg);
		}
		
		String configPathAndName = dir.name()+"/"+configName;
		
		if (configHashMap.containsKey(configPathAndName)) {
		    ConfigInfo configInfo = configHashMap.get(configPathAndName);
		    configInfo.addVersion(configId,
					  configVersion,
					  configCreated,
					  configCreator,
					  configReleaseTag,
					  configProcessName,
					  configComment);
		}
		else {
		    ConfigInfo configInfo = new ConfigInfo(configName,
							   dir,
							   configId,
							   configVersion,
							   configCreated,
							   configCreator,
							   configReleaseTag,
							   configProcessName,
							   configComment);
		    configHashMap.put(configPathAndName,configInfo);
		    dir.addConfigInfo(configInfo);
		}
	    }
	    
	    rs = psSelectLockedConfigurations.executeQuery();

	    while (rs.next()) {
		String dirName = rs.getString(1);
		String configName = rs.getString(2);
		String userName = rs.getString(3);
		String configPathAndName = dirName +"/" + configName;
		ConfigInfo configInfo = configHashMap.get(configPathAndName);
		if (configInfo==null) {
		    String errMsg =
			"ConfDB::loadConfigurationTree(): can't find locked "+
			"configuration '"+configPathAndName+"'.";
		    throw new DatabaseException(errMsg);
		}
		configInfo.lock(userName);
	    }

	    
	    // DEBUG
	    //int config2Time = System.currentTimeMillis();
	    //System.err.println("TIMING: "+
	    //	       (config2Time-startTime)+": "+
	    //	       (dir1Time-startTime)+" / "+
	    //	       (dir2Time-dir1Time)+" / "+
	    //	       (config1Time-dir2Time)+" / "+
	    //	       (config2Time-config1Time));
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::loadConfigurationTree() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
	
	return rootDir;
    }
    
    /** load a single template from a certain release */
    public synchronized Template loadTemplate(String releaseTag,String templateName)
	throws DatabaseException
    {
	int             releaseId = getReleaseId(releaseTag);
	SoftwareRelease release   = new SoftwareRelease();
	release.clear(releaseTag);
	try {
            //System.out.println("loadTemplate with ReleaseTag: "+releaseId+" "+templateName);
	    csLoadTemplate.setInt(1,releaseId);
	    csLoadTemplate.setString(2,templateName);
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::loadTemplate(releaseTag="+releaseTag+
		",templateName="+templateName+") failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	
	loadTemplates(csLoadTemplate,release);
	Iterator<Template> it = release.templateIterator();
	
	if (!it.hasNext()) {
	    String errMsg =
		"ConfDB::loadTemplate(releaseTag="+releaseTag+
		",templateName="+templateName+"): template not found.";
	    throw new DatabaseException(errMsg);
	}
	
	return it.next();
    }

    /** load a software release (all templates) */
    public synchronized void loadSoftwareRelease(int releaseId,SoftwareRelease release)
	throws DatabaseException
    {
	String releaseTag = getReleaseTag(releaseId);
	release.clear(releaseTag);
	try {
            //System.out.println("loadTemplates with ReleaseiD: "+releaseId+" "+release);
	    csLoadTemplates.setInt(1,releaseId);
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::loadSoftwareRelease(releaseId="+releaseId+
		",release) failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	loadTemplates(csLoadTemplates,release);
    }
    
    /** load a software release (all templates) */
    public synchronized void loadSoftwareRelease(String releaseTag,SoftwareRelease release)
	throws DatabaseException
    {
	int releaseId = getReleaseId(releaseTag);
        //System.out.println("loadSoftwareRelease with releaseTag(rec ID): "+releaseTag+" ("+releaseId+") "+release);
	loadSoftwareRelease(releaseId,release);
    }

    /** load a partial software release */
    public synchronized void loadPartialSoftwareRelease(int configId,
					   SoftwareRelease release)
	throws DatabaseException
    {
	String releaseTag = getReleaseTagForConfig(configId);
	release.clear(releaseTag);
	
	try {
            //System.out.println("loadPartialSwRel configId: "+configId+" "+release);
	    csLoadTemplatesForConfig.setInt(1,configId);
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::loadPartialSoftwareRelease(configId="+configId+
		",release) failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	loadTemplates(csLoadTemplatesForConfig,release);
    }
    
    /** load a partial software releaes */
    public synchronized void loadPartialSoftwareRelease(String configName,
					   SoftwareRelease release)
	throws DatabaseException
    {
	int configId = getConfigId(configName);
	loadPartialSoftwareRelease(configId,release);
    }
    
    /** load a full software release, based on stored procedures */
    private void loadTemplates(CallableStatement cs,SoftwareRelease release)
	throws DatabaseException
    {
	reconnect();

	ResultSet rsTemplates = null;
	
	HashMap<Integer,SoftwarePackage> idToPackage =
	    new HashMap<Integer,SoftwarePackage>();
	ArrayList<SoftwareSubsystem> subsystems = getSubsystems(idToPackage);
	
        //System.out.println("loadTemplates: release "+release);
	try {
	    cs.executeUpdate();
	    
	    HashMap<Integer,ArrayList<Parameter> > templateParams = getParameters(0);
	    
	    rsTemplates = psSelectTemplates.executeQuery();
	    
 ////System.out.println("loadTemplates: gotParameters");

	    while (rsTemplates.next()) {
		int    id     = rsTemplates.getInt(1);
		String type   = rsTemplates.getString(2);
		String name   = rsTemplates.getString(3);
		String cvstag = rsTemplates.getString(4);
		int    pkgId  = rsTemplates.getInt(5);
		
 //System.out.println("loadTemplates: id "+id+" type "+" name "+name+" cvstag "+cvstag+" pkgid "+pkgId);
		    //System.out.println("Template "+templateId+" "+templateName+" instance "+instanceName); 

		SoftwarePackage pkg = idToPackage.get(pkgId);

		Template template =
		    TemplateFactory.create(type,name,cvstag,null);
if (template==null) System.out.println("template NULL!!!");
if (pkg==null) System.out.println("pkg NULL!!!");
		
		ArrayList<Parameter> params = templateParams.remove(id);
		
		if (params!=null) {
		    int missingCount = 0;
		    Iterator<Parameter> it = params.iterator();
		    while (it.hasNext()) {
			Parameter p = it.next();
			if (p==null) missingCount++;
		    }
		    if (missingCount>0) {
		    	//TODO database check. This is happening.
			System.err.println("ERROR: "+missingCount+" parameter(s) "+
					   "missing from "+template.type()+
					   " Template '"+template.name()+"'");
		    }
		    else {
			template.setParameters(params);
			if (pkg.templateCount()==0) pkg.subsystem().addPackage(pkg);
			pkg.addTemplate(template);
		    }
		}
		else {
		    if (pkg.templateCount()==0) pkg.subsystem().addPackage(pkg);
		    pkg.addTemplate(template);
		}
		template.setDatabaseId(id);
	    }

	    for (SoftwareSubsystem s : subsystems) {
		if (s.packageCount()>0) {
		    s.sortPackages();
		    release.addSubsystem(s);
		}
	    }

	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::loadTemplates() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rsTemplates);
	}
	
	release.sortSubsystems();
	release.sortTemplates();
    }

    /** load a configuration & *all* release templates from the database */
    public synchronized Configuration loadConfiguration(int configId,
					   SoftwareRelease release)
	throws DatabaseException
    {
	ConfigInfo configInfo  = getConfigInfo(configId);
        ////System.out.println("loadTemplates with configid: "+configId+" release "+release);
	return loadConfiguration(configInfo,release);
    }
    
    
    /** load a configuration& *all* release templates from the database */
    public synchronized Configuration loadConfiguration(ConfigInfo configInfo,
					   SoftwareRelease release)
	throws DatabaseException
    {
	String releaseTag = configInfo.releaseTag();
	
        ////System.out.println("loadTemplates with configinfo: "+releaseTag+" release "+release);
	if (releaseTag==null) System.out.println("releaseTag = " + releaseTag);
	if (release==null) System.out.println("release is null");
	else if (release.releaseTag()==null) System.out.println("WHAT?!");
	
	if (release==null||!releaseTag.equals(release.releaseTag()))
	    loadSoftwareRelease(releaseTag,release);
	Configuration config = new Configuration(configInfo,release);
	loadConfiguration(config);
	config.setHasChanged(false);
	return config;
    }
    
    
    /** load configuration & *necessary* release templates from the database */
    public synchronized Configuration loadConfiguration(int configId)
	throws DatabaseException
    {
	ConfigInfo      configInfo = getConfigInfo(configId);
	String          releaseTag = configInfo.releaseTag();
	SoftwareRelease release    = new SoftwareRelease();
	release.clear(releaseTag);
        ////System.out.println("loadTemplates with only configid: "+configId);
	loadPartialSoftwareRelease(configId,release);
	Configuration config = new Configuration(configInfo,release);
	loadConfiguration(config);
	config.setHasChanged(false);
	return config;
    }
    
    /** fill an empty configuration *after* template hash maps were filled! */
    private void loadConfiguration(Configuration config)
	throws DatabaseException
    {
	reconnect();
	
	int       configId = config.dbId();
        Stack<Integer> idlifo= new Stack<Integer>();

        ////System.out.println("loadConfiguration with config "+config);

	ResultSet rsInstances       = null;
	
	ResultSet rsPathEntries     = null;
	ResultSet rsSequenceEntries = null;
	ResultSet rsPrepareSequence = null;
     
        ResultSet rsEventContentEntries = null;
	ResultSet rsStreamEntries = null;
	ResultSet rsEventContentStatements = null;
	ResultSet rsDatasetEntries = null;
	ResultSet rsPathStreamDataset = null;

	SoftwareRelease release = config.release();

	try {
		
            System.out.println("Trying loadConfiguration id="+configId);
	    csLoadConfiguration.setInt(1,configId);
	    csLoadConfiguration.executeUpdate();
            ////System.out.println("Done loadConfiguration id="+configId);

            ////System.out.println("Trying rs instances"+configId);
	    rsInstances       = psSelectInstances.executeQuery();
	    psSelectPathEntries.setInt(1,configId);
	    psSelectPathEntries.setInt(2,configId);
            ////System.out.println("Trying Pathentries"+configId);
	    rsPathEntries     = psSelectPathEntries.executeQuery();
	    
	    
	    // This is to fix the operator field bug for modules inside sequences. bug #91797
	    /*if(operatorFieldForSequencesAvailability)
            {
                psSelectSequenceEntriesAndOperator.setInt(1,configId);
	    	rsSequenceEntries = psSelectSequenceEntriesAndOperator.executeQuery();
            }
	    else
            {
                psSelectSequenceEntries.setInt(1,configId);
	    	rsSequenceEntries = psSelectSequenceEntries.executeQuery();
            }*/
	    	
            psSelectSequenceEntries.setInt(1,configId);
	    rsSequenceEntries = psSelectSequenceEntries.executeQuery();
	    
	    psSelectEventContentEntries.setInt(1,configId);
	    rsEventContentEntries = psSelectEventContentEntries.executeQuery();
	    psSelectStreamEntries.setInt(1,configId);
	    rsStreamEntries = psSelectStreamEntries.executeQuery();
	    psSelectDatasetEntries.setInt(1,configId);
	    rsDatasetEntries = psSelectDatasetEntries.executeQuery();
	    psSelectPathStreamDatasetEntries.setInt(1,configId);
	    rsPathStreamDataset = psSelectPathStreamDatasetEntries.executeQuery();

	    psSelectEventContentStatements.setInt(1,configId);
	    rsEventContentStatements = psSelectEventContentStatements.executeQuery();
	   
	    HashMap<Integer,Stream> idToStream = new HashMap<Integer,Stream>();
	    HashMap<Integer,PrimaryDataset> idToDataset =
		new HashMap<Integer,PrimaryDataset>();  


            ////System.out.println("getting params"+configId);
	    HashMap<Integer,ArrayList<Parameter> > idToParams = getParameters(configId);
            ////System.out.println("DOne params");
	    
	    HashMap<Integer,ModuleInstance> idToModules=
		new HashMap<Integer,ModuleInstance>();
	    HashMap<Integer,Path>    idToPaths    =new HashMap<Integer,Path>();
	    HashMap<Integer,Sequence>idToSequences=new HashMap<Integer,Sequence>();
	    

	    HashMap<EventContent,Integer> eventContentToId =
		new HashMap<EventContent,Integer>();
	    HashMap<Stream,Integer> streamToId  =new HashMap<Stream,Integer>();
	    HashMap<PrimaryDataset,Integer> primaryDatasetToId =
		new HashMap<PrimaryDataset,Integer>();
	    HashMap<Path,Integer> pathToId  =new HashMap<Path,Integer>();
	    HashMap<Sequence,Integer> sequenceToId  =new HashMap<Sequence,Integer>();
	    
	    while (rsInstances.next()) {
		int     id           = rsInstances.getInt(1);
		int     templateId   = rsInstances.getInt(2);
                String  type         = rsInstances.getString(3);
		String  instanceName = rsInstances.getString(4);
		boolean flag         = rsInstances.getBoolean(5);
		
		String templateName = null;
		
//System.out.println("found instance "+id
 //                                    +  "name="+instanceName+" templateid="+templateId+" entryType="+type);
		if (type.equals("PSet")) {
		    PSetParameter pset = (PSetParameter)ParameterFactory
			.create("PSet",instanceName,"",flag);
		    config.insertPSet(pset);
		    ArrayList<Parameter> psetParams = idToParams.remove(id);
		    if (psetParams!=null) {
			Iterator<Parameter> it = psetParams.iterator();
			while (it.hasNext()) {
			    Parameter p = it.next();
			    if (p!=null) pset.addParameter(p);
			}
		    }
                    else System.out.println("Found null PSet for instance "+instanceName+" "+id+"  templateid="+templateId+" entryType="+type);
		}
		else if (type.equals("EDSource")) {
		    templateName = release.edsourceTemplateName(templateId);
		    Instance edsource = config.insertEDSource(templateName);
		    edsource.setDatabaseId(id);
		    updateInstanceParameters(edsource,idToParams.remove(id));
		    
		}
		else if (type.equals("ESSource")) {
		    int insertIndex = config.essourceCount();
		    templateName = release.essourceTemplateName(templateId);
		    ESSourceInstance essource =
			config.insertESSource(insertIndex,templateName,
					      instanceName);
		    essource.setPreferred(flag);
		    essource.setDatabaseId(id);
		    updateInstanceParameters(essource,idToParams.remove(id));
		}
		else if (type.equals("ESModule")) {
		    int insertIndex = config.esmoduleCount();
		    templateName = release.esmoduleTemplateName(templateId);
		    ESModuleInstance esmodule =
			config.insertESModule(insertIndex,templateName,
					      instanceName);
		    esmodule.setPreferred(flag);
		    esmodule.setDatabaseId(id);
		    updateInstanceParameters(esmodule,idToParams.remove(id));
		}
		else if (type.equals("Service")) {
		    int insertIndex = config.serviceCount();
		    templateName = release.serviceTemplateName(templateId);
		    Instance service = config.insertService(insertIndex,
							    templateName);
		    service.setDatabaseId(id);
		    updateInstanceParameters(service,idToParams.remove(id));
		}
		else if (type.equals("Module")) {
                    //if (templateId==106) templateId=558;
                    //if (templateId==478) templateId=572;
                    //if (templateId==469) templateId=568;
		    templateName = release.moduleTemplateName(templateId);
		    ModuleInstance module = config.insertModule(templateName,
								instanceName);
		    module.setDatabaseId(id);
		    updateInstanceParameters(module,idToParams.remove(id));
		    idToModules.put(id,module);
		}
		else if (type.equals("Path")) {

			int  insertIndex = config.pathCount();
		    Path path = config.insertPath(insertIndex,instanceName);
		    
		    if(extraPathFieldsAvailability) {
				String  pathDesc = null;
				String	pathCont = null;
				psSelectPathExtraFields.setInt(1, id);
				ResultSet pathDescription = psSelectPathExtraFields.executeQuery();

			    while (pathDescription.next()) {
			    	pathDesc    = pathDescription.getString(2);
			    	pathCont	= pathDescription.getString(3);
			    }
			    path.setDescription(pathDesc);
			    path.setContacts(pathCont);		   	
		    }

		    
		    path.setAsEndPath(flag);
		    path.setDatabaseId(id);
		    idToPaths.put(id,path);
		}
		else if (type.equals("Sequence")) {
		    int insertIndex = config.sequenceCount();
		    Sequence sequence = config.insertSequence(insertIndex,
							      instanceName);
		    sequence.setDatabaseId(id);
		    idToSequences.put(id,sequence);
		}
	    }

	    while (rsEventContentEntries.next()) {
		int  eventContentId = rsEventContentEntries.getInt(1);
		String name =  rsEventContentEntries.getString(2);
                ////System.out.println("Evco id "+eventContentId+" name "+name);
		EventContent eventContent = config.insertContent(name);
		if(eventContent==null) continue;
		eventContent.setDatabaseId(eventContentId);
		eventContentToId.put(eventContent,eventContentId);
		
	    }


	    while (rsStreamEntries.next()) {
		int  streamId = rsStreamEntries.getInt(1);
		String streamLabel =  rsStreamEntries.getString(2);
		Double fracToDisk  =  rsStreamEntries.getDouble(3);
		int  eventContentId = rsStreamEntries.getInt(4);
		String name =  rsStreamEntries.getString(5);
		EventContent eventContent = config.content(name);
		if(eventContent==null) continue;

                //System.out.println("Stream id "+streamId+" Label "+streamLabel+" fracTo "+ fracToDisk+" Evco id "+eventContentId+" name "+name);

		Stream stream = eventContent.insertStream(streamLabel);
		stream.setFractionToDisk(fracToDisk);
		stream.setDatabaseId(streamId);
		eventContent.setDatabaseId(eventContentId);
		streamToId.put(stream,streamId);
		idToStream.put(streamId,stream);
		ArrayList<Parameter> parameters = idToParams.remove(streamId);
		if(parameters==null) continue;
		Iterator<Parameter> it = parameters.iterator();
		OutputModule outputModule = stream.outputModule();
		
		while (it.hasNext()) {
		    Parameter p = it.next();
		    if (p==null) continue;
		    outputModule.updateParameter(p.name(),p.type(),p.valueAsString());
		}
		outputModule.setDatabaseId(streamId);
	    }
	    
	    
              int previouslvl=0;
              boolean seqtoskip=false;
              int lvltoskip=0;
              boolean seqDone[];
              seqDone=new boolean[5000000]; 
	    while (rsSequenceEntries.next()) {
               int    sequenceId = rsSequenceEntries.getInt(1);
                int    entryLvl   = rsSequenceEntries.getInt(3);
                int    entryId    = rsSequenceEntries.getInt(4);
                int    sequenceNb = rsSequenceEntries.getInt(5);
                String entryType  = rsSequenceEntries.getString(6);
 
		
//System.out.println("found seq "+ entryId + "parent="+sequenceId+ " lvl="+entryLvl+" ord "+sequenceNb+" entryType="+entryType);

                while (entryLvl<previouslvl) {
                       if ((!seqtoskip)&&(entryLvl>=lvltoskip)) idlifo.pop();
                       previouslvl--;
                       if (previouslvl < lvltoskip) 
                       {
			seqtoskip=false;
                        lvltoskip=0;
                       }
                }
                previouslvl=entryLvl;
                //if (entryLvl<lvltoskip){
                 //       seqtoskip=false;
                  //      lvltoskip=0;
                // }

                if (entryLvl==0) {
                        if (seqDone[entryId]) {
                             seqtoskip=true;
                             lvltoskip=1;
                           }
                         else {
                          seqDone[entryId]=true;
                          idlifo.push(entryId);
                          previouslvl++;
                             lvltoskip=1;
                         }
                 }       
                if ((entryLvl>0) && (!seqtoskip))
                {
		  sequenceId=idlifo.peek();


		Sequence sequence = idToSequences.get(sequenceId);
		int      index    = sequence.entryCount();
   		sequenceNb=index;
		
		boolean fail=true;
		Operator operator = Operator.DEFAULT;
		try {
		    //operator = Operator.getOperator( rsSequenceEntries.getInt(5) );
		//	if(operatorFieldForSequencesAvailability) 
				operator = Operator.getOperator( rsSequenceEntries.getInt(7) );
		    fail=false;
		} catch (SQLException e) {
		    operator = Operator.DEFAULT;
		    fail=true;
		    
		    System.out.println("SQLException catched at confDb.java::loadConfiguration()   sequence = " + sequence.name());
		}
		
		
		if (index!=sequenceNb)
		    System.err.println("ERROR in sequence "+sequence.name()+
				       ": index="+index+" sequenceNb="
				       +sequenceNb);
		
		if (entryType.equals("Sequence")) {
                    if (seqDone[entryId]) {
                         seqtoskip=true;
                         lvltoskip=entryLvl+1;
                     }
                     else 
		       {  idlifo.push(entryId);
                          previouslvl++;
                       }


                //    if (!seqtoskip) {
                        seqDone[entryId]=true;
		    	Sequence entry = idToSequences.get(entryId);
		    	if (entry==null) {
				System.err.println("ERROR: can't find sequence for "+
					   "id=" + entryId +
					   " expected as daughter " + index +
					   " of sequence " + sequence.name());
		    	}
		    	config.insertSequenceReference(sequence,index,entry).setOperator( operator );
                 //   }
		}
		else if (entryType.equals("Module")) {
		    ModuleInstance entry = (ModuleInstance)idToModules.get(entryId);
		    config.insertModuleReference(sequence,index,entry).setOperator( operator );
		}
		else if (entryType.equals("OutputModule")) {
		    Stream entry = (Stream)idToStream.get(entryId);
		    if(entry==null) continue;
		    OutputModule referencedOutput = entry.outputModule();
		    if (referencedOutput==null) continue;
		    config.insertOutputModuleReference(sequence,index,referencedOutput).setOperator( operator );
		}
		else
		    System.err.println("Invalid entryType '"+entryType+"'");
		
		sequence.setDatabaseId(sequenceId);
		sequenceToId.put(sequence,sequenceId);
               }
	    }



	    while (rsPathEntries.next()) {
		int    pathId     = rsPathEntries.getInt(1);
		int    entryId    = rsPathEntries.getInt(2);
		int    sequenceNb = rsPathEntries.getInt(3);
		String entryType  = rsPathEntries.getString(4);
		Operator operator = Operator.getOperator( rsPathEntries.getInt(5) );
		
		Path path  = idToPaths.get(pathId);
		int  index = path.entryCount();

                ////System.out.println("found n path "+path.name()+": "+
                   ////                    "index="+index+" sequenceNb="+sequenceNb+" entryType="+entryType+" entryId="+entryId);

		if (index!=sequenceNb)
		    System.err.println("ERROR in path "+path.name()+": "+
				       "index="+index+" sequenceNb="+sequenceNb);
		
		if (entryType.equals("Path")) {
		    Path entry = idToPaths.get(entryId);
		    config.insertPathReference(path,index,entry).setOperator( operator );
		}
		else if (entryType.equals("Sequence")) {
		    Sequence entry = idToSequences.get(entryId);
		    config.insertSequenceReference(path,index,entry).setOperator( operator );
		}
		else if (entryType.equals("Module")) {
		    ModuleInstance entry = (ModuleInstance)idToModules.get(entryId);
		    config.insertModuleReference(path,index,entry).setOperator(operator);
		}	
		else if (entryType.equals("OutputModule")) {
		    Stream entry = (Stream)idToStream.get(entryId);
		    if(entry==null) continue;
		    OutputModule referencedOutput = entry.outputModule();
		    if (referencedOutput==null) continue;
		    config.insertOutputModuleReference(path,index,referencedOutput).setOperator(operator);
		}
		else
		    System.err.println("Invalid entryType '"+entryType+"'");

		path.setDatabaseId(pathId);
		pathToId.put(path,pathId);
	    }



	    while (rsDatasetEntries.next()) {
		int  datasetId = rsDatasetEntries.getInt(1);
		String datasetLabel =  rsDatasetEntries.getString(2);
		int  streamId = rsDatasetEntries.getInt(3);
		String streamLabel =  rsDatasetEntries.getString(4);
		Stream stream = idToStream.get(streamId);
		if(stream == null)
		    continue;

		PrimaryDataset primaryDataset = stream.insertDataset(datasetLabel);
		primaryDataset.setDatabaseId(datasetId);
		idToDataset.put(datasetId,primaryDataset);
		primaryDatasetToId.put(primaryDataset,datasetId);
	    }

	    //TODO the key code is here. This should build only one Stream
	    // sharing the Paths between more PrimaryDatasets for every pair (streamId,datasetId,PathId).
	    while (rsPathStreamDataset.next()) {	    
			int  pathId = rsPathStreamDataset.getInt(1);
			int  streamId = rsPathStreamDataset.getInt(2);
			int  datasetId = rsPathStreamDataset.getInt(3);
	
			Path path = idToPaths.get(pathId);
			Stream stream = idToStream.get(streamId);
			PrimaryDataset primaryDataset = idToDataset.get(datasetId); 
			
			if(path==null) continue;
			if(stream == null) continue;
			
			EventContent eventContent = stream.parentContent();
			stream.insertPath(path);
			path.addToContent(eventContent);
			
			
			if(primaryDataset==null)
			    continue;		
			primaryDataset.insertPath(path);
			
			stream.setDatabaseId(streamId);
			primaryDataset.setDatabaseId(datasetId);
	    }

	    // read content statements last since paths need to be registered!
	    while(rsEventContentStatements.next()) {
		int statementId = rsEventContentStatements.getInt(1);
		String classN = rsEventContentStatements.getString(2);
		String module = rsEventContentStatements.getString(3);
		String extra = rsEventContentStatements.getString(4);
		String process = rsEventContentStatements.getString(5);
		int statementType = rsEventContentStatements.getInt(6);
		int eventContentId = rsEventContentStatements.getInt(7);
		int statementRank = rsEventContentStatements.getInt(8);
		String name =  rsEventContentStatements.getString(9);
		int parentPathId = rsEventContentStatements.getInt(10);
	       
		EventContent eventContent = config.content(name);
		

		OutputCommand outputCommand = new OutputCommand();
		String commandToString = classN+"_"+module+"_"+extra+"_"+process; 

		if(statementType == 0){
		    commandToString = "drop "+commandToString;
		}else{
		    commandToString = "keep " + commandToString;
		}
		
		outputCommand.initializeFromString(commandToString); // Bug
		
		if( parentPathId>0){
		    
		    Path parentPath = idToPaths.get(parentPathId);
		    if(parentPath==null)
			continue;
		    Iterator<Reference> itR = parentPath.recursiveReferenceIterator();
		    boolean found = false;
		    Reference parentReference = null;
		    while (itR.hasNext()&&!found){ 
			parentReference = itR.next();
			if (parentReference.name().equals(module)) 
			    found=true;
		    }
		    
		    if (found){
			outputCommand = new OutputCommand(parentPath,parentReference);
			
			// BUG bug #88643 //TODO
			//Load the values
			
			outputCommand.setClassName(classN);
			outputCommand.setExtraName(extra);
			outputCommand.setModuleName(module);
			outputCommand.setProcessName(process);
			
		    }
		}
		
		eventContent.insertCommand(outputCommand);
	    }


	    Iterator<EventContent> contentIt = config.contentIterator();
	    while(contentIt.hasNext()){
		EventContent eventContent = contentIt.next();
		int databaseId = eventContentToId.get(eventContent);
		eventContent.setDatabaseId(databaseId);
	    }
	
	    Iterator<Stream> streamIt = config.streamIterator();
	    while(streamIt.hasNext()){
		Stream stream = streamIt.next();
		int databaseId = streamToId.get(stream);
		stream.setDatabaseId(databaseId);
	    }
	    
	    Iterator<PrimaryDataset> datasetIt = config.datasetIterator();
	    while(datasetIt.hasNext()){
		PrimaryDataset primaryDataset = datasetIt.next();
		int databaseId = primaryDatasetToId.get(primaryDataset);
		primaryDataset.setDatabaseId(databaseId);
	    }

	    
	    Iterator<Sequence> sequenceIt = config.sequenceIterator();
	    while(sequenceIt.hasNext()){
		Sequence sequence = sequenceIt.next();
		int databaseId = sequenceToId.get(sequence);
		sequence.setDatabaseId(databaseId);
	    }
	    
	    
	      Iterator<Path> pathIt = config.pathIterator();
	      while(pathIt.hasNext()){
	      Path path = pathIt.next();
	      int databaseId = pathToId.get(path);
	      path.setDatabaseId(databaseId);
	      }

	    

         ////System.out.println("###########################End of loops###################");
       

	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::loadConfiguration(Configuration config) failed "+
		"(configId="+configId+"): "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
/*	    dbConnector.release(rsInstances);
	    dbConnector.release(rsPathEntries);
	    dbConnector.release(rsSequenceEntries);
	    dbConnector.release(rsEventContentEntries);
	    dbConnector.release(rsStreamEntries);
	    dbConnector.release(rsDatasetEntries);
	    dbConnector.release(rsPathStreamDataset);
*/
	}
    }
    
    /** check if extra fields for paths are available in current db. */
    public synchronized boolean checkExtraPathFields() throws DatabaseException {
    	ResultSet rs = null;
    	int extraFields = 0;
    	try {
/*sv    		rs = psCheckPathFieldsExistence.executeQuery();
    	
    		while(rs.next()) {
    			extraFields = rs.getInt(1);	
    		}
   */ 		
        if (false) throw new SQLException();
    	}
    	catch (SQLException e) {
    	    String errMsg =
    		"ConfDB::checkExtraPathFields failed: "+
    		e.getMessage();
    	    throw new DatabaseException(errMsg,e);
    	} finally {
    		dbConnector.release(rs);
    	}
    	
		if(extraFields == 1) return true;
		else return false;
    }
    
    
    /** check if the operator fields for TMP_SEQUENCE_ENTRIES is available in current db. */
    public synchronized boolean checkOperatorFieldForSequences() throws DatabaseException {
    	ResultSet rs = null;
    	int extraFields = 0;
    	try {
/*sv    		rs = psCheckOperatorForModuleSequenceAssoc.executeQuery();
    	
    		while(rs.next()) {
    			extraFields = rs.getInt(1);	
    		}
   */ 		
         if (false) throw new SQLException();
    	}
    	catch (SQLException e) {
    	    String errMsg =
    		"ConfDB::checkOperatorFieldForSequences failed: "+
    		e.getMessage();
    	    throw new DatabaseException(errMsg,e);
    	} finally {
    		dbConnector.release(rs);
    	}
    	
		if(extraFields == 1) return true;
		else return false;
    }
    
    

    /** insert a new directory */
    public synchronized void insertDirectory(Directory dir) throws DatabaseException
    {
	ResultSet rs = null;
	try {
	    psInsertDirectory.setInt(1,dir.parentDir().dbId());
	    psInsertDirectory.setString(2,dir.name());
	    psInsertDirectory.executeUpdate();
	    rs = psInsertDirectory.getGeneratedKeys();
	    rs.next();
	    dir.setDbId(rs.getInt(1));
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertDirectory(Directory dir) failed "+
		"(parentDirId="+dir.parentDir().dbId()+",name="+dir.name()+"): "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }


    /** remove an (empty!) directory */
    public synchronized void removeDirectory(Directory dir) throws DatabaseException
    {
	try {
	    psDeleteDirectory.setInt(1,dir.dbId());
	    psDeleteDirectory.executeUpdate();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::removeDirectory(Directory dir) failed "+
		"(name="+dir.name()+"): "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
    }


    /** insert a new configuration */
    public synchronized void insertConfiguration(Configuration config,
				    String creator,String processName,
				    String comment)
	throws DatabaseException
    {
	String  releaseTag = config.releaseTag();
	int     releaseId  = getReleaseId(releaseTag);
	String  configDescriptor =
	    config.parentDir().name()+"/"+
	    config.name()+"/"+
	    "V"+config.nextVersion();

	ResultSet rs = null;
        System.out.println("Inserting config "+config.name()+" as "+configDescriptor);

		
	try {
	    dbConnector.getConnection().setAutoCommit(false);

            psFindConfiguration.setString(1,config.parentDir().name()+"/"+config.name());
	    rs = psFindConfiguration.executeQuery(); 
            Integer id_config=0;
            while (rs.next()) {
                 id_config = rs.getInt(1);
            }
            if (id_config==0) 
            {
              psInsertConfiguration.setString(1,config.parentDir().name()+"/"+config.name()); 
              psInsertConfiguration.executeUpdate();
              psFindConfiguration.setString(1,config.parentDir().name()+"/"+config.name());
	      rs = psFindConfiguration.executeQuery(); 
	      rs.next();
	      id_config= rs.getInt(1);
            }

	    psInsertConfigurationVers.setInt(1,id_config);
	    psInsertConfigurationVers.setInt(2,config.nextVersion());
	    psInsertConfigurationVers.setInt(3,releaseId);
	    psInsertConfigurationVers.setString(4,configDescriptor);
	    psInsertConfigurationVers.setInt(5,config.parentDirId());
	    psInsertConfigurationVers.setString(6,config.name());
	    psInsertConfigurationVers.setString(7,creator);
	    psInsertConfigurationVers.setString(8,processName);
	    psInsertConfigurationVers.setString(9,comment);
	    psInsertConfigurationVers.executeUpdate();
	    rs = psInsertConfigurationVers.getGeneratedKeys();
	    
	    rs.next();
	    int configId = rs.getInt(1);
	    
	    psSelectConfigurationCreated.setInt(1,configId);
	    rs = psSelectConfigurationCreated.executeQuery();
	    rs.next();
	    String created = rs.getString(1);
	    config.addNextVersion(configId,
				  created,creator,releaseTag,processName,comment);


	    config.updatePathReferences();
	    
	    // insert global psets
	    insertGlobalPSets(configId,config);
	    
	    // insert edsource
	    insertEDSources(configId,config);

	    // insert essources
	    insertESSources(configId,config);
	    
	    // insert esmodules
	    insertESModules(configId,config);
	    
	    // insert services
	    insertServices(configId,config);
	  
	    HashMap<String,Integer> primaryDatasetHashMap =
		insertPrimaryDatasets(configId,config);
	    HashMap<String,Integer> eventContentHashMap =
		insertEventContents(configId,config);
	    HashMap<String,Integer> streamHashMap =
		insertStreams(configId,config);
	  
	    insertEventContentStreamAssoc(eventContentHashMap,streamHashMap,config);
//notneeded	    insertStreamDatasetAssoc(streamHashMap,primaryDatasetHashMap,config);
 
	    // insert paths
	    HashMap<String,Integer> pathHashMap=insertPaths(configId,config);
	    
	    // insert sequences
	    HashMap<String,Integer> sequenceHashMap=insertSequences(configId,
								    config);
	    
	    // insert modules
	    HashMap<String,Integer> moduleHashMap=insertModules(config);

	 
	    insertEventContentStatements(configId,config,eventContentHashMap);	  
	    insertPathStreamPDAssoc(pathHashMap,streamHashMap,primaryDatasetHashMap,
				    config,configId);
	

	    // insert parameter bindings / values
//notyet
/*	    psInsertParameterSet.executeBatch();
	    psInsertVecParameterSet.executeBatch();
	    psInsertGlobalPSet.executeBatch();
	    psInsertSuperIdParamAssoc.executeBatch();
	    psInsertSuperIdParamSetAssoc.executeBatch();
	    psInsertSuperIdVecParamSetAssoc.executeBatch();
	    Iterator<PreparedStatement> itPS =
		insertParameterHashMap.values().iterator();
	    while (itPS.hasNext()) itPS.next().executeBatch();
	
*/
	    // insert references regarding paths and sequences
	    insertReferences(config,pathHashMap,sequenceHashMap,
			     moduleHashMap,streamHashMap);
	    
	
	    dbConnector.getConnection().commit();
	}
	catch (DatabaseException e) {
	    e.printStackTrace(); // DEBUG
	    try { dbConnector.getConnection().rollback(); }
	    catch (SQLException e2) { e2.printStackTrace(); }
	    throw e;
	}
	catch (SQLException e) {
	    e.printStackTrace(); // DEBUG
	    try { dbConnector.getConnection().rollback(); }
	    catch (SQLException e2) { e2.printStackTrace(); }
	    String errMsg =
		"ConfDB::insertConfiguration(config="+config.dbId()+
		",creator="+creator+",processName="+processName+
		",comment="+comment+") failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	catch (Exception e) {
	    e.printStackTrace(); // DEBUG
	    try { dbConnector.getConnection().rollback(); }
	    catch (SQLException e2) { e2.printStackTrace(); }
	    String errMsg =
		"ConfDB::insertConfiguration(config="+config.dbId()+
		",creator="+creator+",processName="+processName+
		",comment="+comment+") failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    try { dbConnector.getConnection().setAutoCommit(true); }
	    catch (SQLException e) {}
	    dbConnector.release(rs);
	}
    }

    /** lock a configuration and all of its versions */
    public synchronized void lockConfiguration(Configuration config,String userName)
	throws DatabaseException
    {
	reconnect();
	
	int    parentDirId   = config.parentDir().dbId();
	String parentDirName = config.parentDir().name();
	String configName    = config.name();
	
	if (config.isLocked()) {
	    String errMsg =
		"ConfDB::lockConfiguration(): Can't lock "+config.toString()+
		": already locked by user '"+config.lockedByUser()+"'.";
	    throw new DatabaseException(errMsg);
	}
	
/*	try {
	    psInsertConfigurationLock.setInt(1,parentDirId);
	    psInsertConfigurationLock.setString(2,configName);
	    psInsertConfigurationLock.setString(3,userName);
	    psInsertConfigurationLock.executeUpdate();

        if (false) throw new SQLException();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::lockConfiguration("+config.toString()+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
*/
    }

    /** unlock a configuration and all its versions */
    public synchronized void unlockConfiguration(Configuration config) throws DatabaseException
    {
	reconnect();

	int     parentDirId   = config.parentDir().dbId();
	String  parentDirName = config.parentDir().name();
	String  configName    = config.name();
	String  userName      = config.lockedByUser();

/*	try {
	    psDeleteLock.setInt(1,parentDirId);
	    psDeleteLock.setString(2,configName);
	    psDeleteLock.executeUpdate();
	if (false) throw new SQLException();
        }
	catch (SQLException e) {
	    String errMsg =
		" ConfDB::unlockConfiguration("+config.toString()+" failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
*/
    }
    
    /** insert a new super id, return its value */
    private int insertSuperId() throws DatabaseException
    {
	ResultSet rs = null;
	try {
	    psInsertSuperId.executeUpdate();
	    rs = psInsertSuperId.getGeneratedKeys();
	    rs.next();
	    return rs.getInt(1);
	}
	catch (SQLException e) {
	    String errMsg = "ConfDB::insertSuperId() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    
    /** insert configuration's global PSets */
    private void insertGlobalPSets(int configId,Configuration config)
	throws DatabaseException
    {
	for (int sequenceNb=0;sequenceNb<config.psetCount();sequenceNb++) {
	    //int           psetId = insertSuperId();
	    PSetParameter pset   = config.pset(sequenceNb);
	    try {
		// first, insert the pset (constraint!)
		psInsertGPset.setString(1,pset.name());
		psInsertGPset.setBoolean(2,pset.isTracked());
                psInsertGPset.executeUpdate();
                ResultSet rs=psInsertGPset.getGeneratedKeys();
                rs.next();
                int psetId=rs.getInt(1);

 		
		
		for (int i=0;i<pset.parameterCount();i++) {
		    Parameter p = pset.parameter(i);
		    if (p instanceof PSetParameter) {
			PSetParameter ps = (PSetParameter)p;
			insertParameterSet(psetId,i,0,ps,psInsertParameterGPset);
		    }
		    else if (p instanceof VPSetParameter) {
			VPSetParameter vps = (VPSetParameter)p;
			insertVecParameterSet(psetId,i,0,vps,psInsertParameterGPset);
		    }
		    else insertParameter(psetId,i,0,p,psInsertParameterGPset);
		}
	    
		// now, enter association to configuration
		psInsertGlobalPSet.setInt(1,configId);
		psInsertGlobalPSet.setInt(2,psetId);
		psInsertGlobalPSet.setInt(3,sequenceNb);
		psInsertGlobalPSet.executeUpdate();
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::insertGlobalPSets(configId="+configId+") failed: "+
		    e.getMessage();
		throw new DatabaseException(errMsg,e);
	    }
	}
    }
    
    /** insert configuration's edsoures */
    private void insertEDSources(int configId,Configuration config)
	throws DatabaseException
    {
	for (int sequenceNb=0;sequenceNb<config.edsourceCount();sequenceNb++) {
	    EDSourceInstance edsource   = config.edsource(sequenceNb);
	    int              edsourceId = edsource.databaseId();
	    int              templateId = edsource.template().databaseId();
	    
            ResultSet rs = null;
	    if (edsourceId<=0) {
		try {
		    psInsertEDSource.setInt(1,templateId-1000000);
		    psInsertEDSource.executeUpdate();
                    rs = psInsertEDSource.getGeneratedKeys();
                    rs.next();
		    edsourceId = rs.getInt(1);
		}
		catch (SQLException e) {
		    String errMsg =
			"ConfDB::insertEDSources(configID="+configId+
			") failed (edsourceId="+edsourceId+
			" templateId="+templateId+"): "+e.getMessage();
		    throw new DatabaseException(errMsg,e);
		}
		insertInstanceParameters(edsourceId,edsource,psInsertParameterEDS);
		edsource.setDatabaseId(edsourceId+1000000);
	    }
            else edsourceId-=1000000;
	    
	    try {
		psInsertConfigEDSourceAssoc.setInt(1,configId);
		psInsertConfigEDSourceAssoc.setInt(2,edsourceId);
		psInsertConfigEDSourceAssoc.setInt(3,sequenceNb);
		psInsertConfigEDSourceAssoc.addBatch();
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::insertEDSources(configID="+configId+") failed "+
		    "(edsourceId="+edsourceId+", sequenceNb="+sequenceNb+"): "+
		    e.getMessage();
		throw new DatabaseException(errMsg,e);   
	    }
	}
	
	try {
	    psInsertConfigEDSourceAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertEDSources(configId="+configId+") failed "+
		"(batch insert):" + e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
    }
    
    /** insert configuration's essources */
    private void insertESSources(int configId,Configuration config)
	throws DatabaseException
    {
	for (int sequenceNb=0;sequenceNb<config.essourceCount();sequenceNb++) {
	    ESSourceInstance essource    = config.essource(sequenceNb);
	    int              essourceId  = essource.databaseId();
	    int              templateId  = essource.template().databaseId();
	    boolean          isPreferred = essource.isPreferred();

           ResultSet rs = null;
 
	    if (essourceId<=0) {
		try {
		    psInsertESSource.setInt(1,templateId-2000000);
		    psInsertESSource.setString(2,essource.name());
		    psInsertESSource.executeUpdate();
                    rs = psInsertESSource.getGeneratedKeys();
                    rs.next();
                    essourceId = rs.getInt(1);
		}
		catch (SQLException e) {
		    String errMsg =
			"ConfDB::insertESSources(configID="+configId+") failed "+
			"(essourceId="+essourceId+" templateId="+templateId+"): "+
			e.getMessage();
		    throw new DatabaseException(errMsg,e);
		}
		insertInstanceParameters(essourceId,essource,psInsertParameterESS);
		essource.setDatabaseId(essourceId+2000000);
	    }
            else essourceId-=2000000;
	    
	    try {
		psInsertConfigESSourceAssoc.setInt(1,configId);
		psInsertConfigESSourceAssoc.setInt(2,essourceId);
		psInsertConfigESSourceAssoc.setInt(3,sequenceNb);
		psInsertConfigESSourceAssoc.setBoolean(4,isPreferred);
		psInsertConfigESSourceAssoc.addBatch();
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::insertESSources(configID="+configId+") failed "+
		    "(essourceId="+essourceId+", sequenceNb="+sequenceNb+"):" +
		    e.getMessage();
		throw new DatabaseException(errMsg,e);  
	    }
	}

	try {
	    psInsertConfigESSourceAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertESSources(configId="+configId+") failed "+
		"(batch insert):" + e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
    }
    
    /** insert configuration's esmodules */
    private void insertESModules(int configId,Configuration config)
	throws DatabaseException
    {
	for (int sequenceNb=0;sequenceNb<config.esmoduleCount();sequenceNb++) {
	    ESModuleInstance esmodule    = config.esmodule(sequenceNb);
	    int              esmoduleId  = esmodule.databaseId();
	    int              templateId  = esmodule.template().databaseId();
	    boolean          isPreferred = esmodule.isPreferred();
	    
            ResultSet rs = null;

	    if (esmoduleId<=0) {
		try {
		    psInsertESModule.setInt(1,templateId-3000000);
		    psInsertESModule.setString(2,esmodule.name());
		    psInsertESModule.executeUpdate();
                    rs = psInsertESModule.getGeneratedKeys();
                    rs.next();
                    esmoduleId = rs.getInt(1);

		}
		catch (SQLException e) {
		    String errMsg =
			"ConfDB::insertESModules(configID="+configId+") failed "+
			"(esmoduleId="+esmoduleId+" templateId="+templateId+"): "+
			e.getMessage();
		    throw new DatabaseException(errMsg,e);
		}
		insertInstanceParameters(esmoduleId,esmodule,psInsertParameterESM);
		esmodule.setDatabaseId(esmoduleId+3000000);
	    }
            else esmoduleId-=3000000;
	    
	    try {
		psInsertConfigESModuleAssoc.setInt(1,configId);
		psInsertConfigESModuleAssoc.setInt(2,esmoduleId);
		psInsertConfigESModuleAssoc.setInt(3,sequenceNb);
		psInsertConfigESModuleAssoc.setBoolean(4,isPreferred);
		psInsertConfigESModuleAssoc.addBatch();
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::insertESModules(configID="+configId+") failed "+
		    "(esmoduleId="+esmoduleId+", sequenceNb="+sequenceNb+"): "+
		    e.getMessage();
		throw new DatabaseException(errMsg,e);
	    }
	}

	try {
	    psInsertConfigESModuleAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertESModule(configId="+configId+") failed "+
		"(batch insert):" + e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
    }
    
    /** insert configuration's services */
    private void insertServices(int configId,Configuration config)
    	throws DatabaseException
    {
	for (int sequenceNb=0;sequenceNb<config.serviceCount();sequenceNb++) {
	    ServiceInstance service    = config.service(sequenceNb);
	    int             serviceId  = service.databaseId();
	    int             templateId = service.template().databaseId();
	    
            ResultSet rs = null;

	    if (serviceId<=0) {
		try {
		    psInsertService.setInt(1,templateId-4000000);
		    psInsertService.executeUpdate();
                    rs = psInsertService.getGeneratedKeys();
                    rs.next();
                    serviceId = rs.getInt(1);

		}
		catch (SQLException e) {
		    String errMsg =
			"ConfDB::insertServices(configID="+configId+") failed "+
			"(serviceId="+serviceId+" templateId="+templateId+"): "+
			e.getMessage();
		    throw new DatabaseException(errMsg,e);
		}
		insertInstanceParameters(serviceId,service,psInsertParameterSRV);
		service.setDatabaseId(serviceId+4000000);
	    }
                else serviceId-=4000000;
	    
	    try {
		psInsertConfigServiceAssoc.setInt(1,configId);
		psInsertConfigServiceAssoc.setInt(2,serviceId);
		psInsertConfigServiceAssoc.setInt(3,sequenceNb);
		psInsertConfigServiceAssoc.addBatch();
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::insertServices(configID="+configId+") failed "+
		    "(serviceId="+serviceId+", sequenceNb="+sequenceNb+"): "+
		    e.getMessage();
		throw new DatabaseException(errMsg,e);
	    }
	}

	try {
	    psInsertConfigServiceAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertService(configId="+configId+") failed "+
		"(batch insert):" + e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
    }

    private Integer calculatePathCRC(Path path)
    {
	return 111111;
    }
    
    private Integer calculateSequenceCRC(Sequence seq)
    {
	return 111111;
    }
    
    private Integer calculateModuleCRC(ModuleInstance module)
    {
	return 111111;
    }
    
    private Integer calculateTemplateCRC()
    {
	return 111111;
    }
    
    /** insert configuration's paths */
    private HashMap<String,Integer> insertPaths(int configId,
						Configuration config)
	throws DatabaseException
    {
	HashMap<String,Integer> result   = new HashMap<String,Integer>();
	HashMap<Integer,Path>   idToPath = new HashMap<Integer,Path>();

	ResultSet rs = null;
	try {
	    for (int sequenceNb=0;sequenceNb<config.pathCount();sequenceNb++) {
		Path   path     = config.path(sequenceNb);
		path.hasChanged();
		String  pathName       = path.name();
		int     pathId        = path.databaseId();
		boolean pathIsEndPath = path.isSetAsEndPath();
		String  description		= path.getDescription();
		String contacts			= path.getContacts();
                int vpathId = -1; 
		
			if (pathId<=0) {
		    
                        int id_path = -1;
                        int crc32 = calculatePathCRC(path);
		/*h_ tables 
                        psCheckPathName.setString(1,pathName);
                        rs=psCheckPathName.executeQuery();
                        if (rs.next()) {
                            id_path = rs.getInt(1);
                        }
                System.out.println("insertPath: searched confver "+configId+" id path "+id_path);

                 */
                        if (id_path<0) {
                                String pathnoum=pathName;
                                Integer version=0;
                                if (pathName.contains("_v")) {
                                    String[] pathandver=pathName.split("_v");
                                    pathnoum=pathandver[0];
                                    version=Integer.parseInt(pathandver[1]);
                                }
                                psCheckPathNoum.setString(1,pathnoum);
				rs=psCheckPathNoum.executeQuery();
                        	int id_pathnoum = -1;
                        	if(rs.next()) {
                            		id_pathnoum = rs.getInt(1);
                        	}
                //System.out.println("insertPath: searched pathnoum "+id_pathnoum);
                                if (id_pathnoum<0) {
					psInsertPathNoum.setString(1,pathnoum);
					psInsertPathNoum.executeUpdate();
                                        rs=psInsertPathNoum.getGeneratedKeys();
					rs.next();
					id_pathnoum=rs.getInt(1);
                //System.out.println("insertPath: created pathnoum "+id_pathnoum);
				}	
				psInsertPath.setString(1,pathName);
				psInsertPath.setInt(2,version);
				psInsertPath.setInt(3,id_pathnoum);

                                psInsertPath.executeUpdate();
				rs=psInsertPath.getGeneratedKeys();
                                rs.next();
                               	id_path=rs.getInt(1);                
                //System.out.println("insertPath: created id_path "+id_path);
                        }
				
                       
			//if(extraPathFieldsAvailability) {
			if(false) {
			     // inserting extra fields in schema:
			    psInsertPathDescription.setString(1,pathName);
                            if (pathIsEndPath){ 
			    	psInsertPathDescription.setInt(2,1);
                            } else {
			    	psInsertPathDescription.setInt(2,0);
                            }
			    psInsertPathDescription.setString(3, description);
			    psInsertPathDescription.setString(4, contacts);
			    psInsertPathDescription.executeUpdate();
			    rs = psInsertPathDescription.getGeneratedKeys();				
			} else {
				// no extra fields in schema:
			    psInsertPathIds.setInt(1,id_path);
                            if (pathIsEndPath){ 
			    	psInsertPathIds.setInt(2,1);
                            } else {
			    	psInsertPathIds.setInt(2,0);
                            }
			    psInsertPathIds.setInt(3,crc32);
			    psInsertPathIds.setInt(4,111111);
			    psInsertPathIds.executeUpdate();
			    rs = psInsertPathIds.getGeneratedKeys();
                            rs.next();
                            vpathId=rs.getInt(1);
                //System.out.println("insertPath: created vpathid "+vpathId);
			}
                        pathId=vpathId; //if not using h_ tables

                       /*only for h_ tables 
                         psCheckHPathIdCrc.setInt(1,crc32);
                        rs=psCheckHPathIdCrc.executeQuery();
                        if (rs.next()) { 
				pathId=rs.getInt(1);
                System.out.println("insertPath: found crc in pathid "+pathId);
                        } else { 
                            psInsertHPathIds.setInt(1,crc32);
                            psInsertHPathIds.setInt(2,111111);
                            psInsertHPathIds.executeUpdate();
                            rs = psInsertHPathIds.getGeneratedKeys();
                            rs.next();
                            pathId=rs.getInt(1);
                System.out.println("insertPath: created pathid "+pathId);

                            psInsertHPathId2Path.setInt(1,id_path);
                            psInsertHPathId2Path.setInt(2,pathId);
                            psInsertHPathId2Path.setBoolean(3,pathIsEndPath);
                            psInsertHPathId2Path.executeUpdate();

			    psInsertHPathId2Uq.setInt(1,vpathId);
			    psInsertHPathId2Uq.setInt(2,pathId);
			    psInsertHPathId2Uq.executeUpdate();
                        }
                     */

		    result.put(pathName,pathId);
		    idToPath.put(pathId,path);
		}
		else {
                    result.put(pathName,-pathId);
                       /*only for h_ tables 
		    psSelectPathId2Uq.setInt(2,pathId);
		    psSelectPathId2Uq.executeUpdate();
                    rs.next();
                    vpathId=rs.getInt(1);
                System.out.println("insertPath: searched (pathId existed) vpathid "+vpathId);
                       */ 
                    vpathId=pathId; //working with v_
                }
		
                //System.out.println("insertPath: Trying to insert confver "+configId+" v pathid "+vpathId);
		psInsertConfigPathAssoc.setInt(1,vpathId);
		psInsertConfigPathAssoc.setInt(2,configId);
		psInsertConfigPathAssoc.setInt(3,sequenceNb);
		psInsertConfigPathAssoc.executeUpdate();

                       /*only for h_ tables 
                System.out.println("insertPath: Trying to insert confver "+configId+" h_pathid "+pathId);
		psInsertConfigHPathAssoc.setInt(1,pathId);
		psInsertConfigHPathAssoc.setInt(2,configId);
		psInsertConfigHPathAssoc.setInt(3,sequenceNb);
		psInsertConfigHPathAssoc.executeUpdate();
                   */
	    }

	    // only *now* set the new databaseId of changed paths!
	    for (Map.Entry<Integer,Path> e : idToPath.entrySet()) {
		int  id = e.getKey();
		Path p  = e.getValue();
		p.setDatabaseId(id);
	    }
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertPaths(configId="+configId+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}

/*	try {
	    psInsertConfigPathAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertPaths(configId="+configId+
		") failed (batch insert): "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
*/	
	return result;
    }
    
    /** insert configuration's sequences */
    private HashMap<String,Integer> insertSequences(int configId,
						    Configuration config)
	throws DatabaseException
    {
	HashMap<String,Integer>   result      =new HashMap<String,Integer>();
	HashMap<Integer,Sequence> idToSequence=new HashMap<Integer,Sequence>();
	
	ResultSet rs = null;
	try {
	    for (int sequenceNb=0;sequenceNb<config.sequenceCount();sequenceNb++) {
		Sequence sequence     = config.sequence(sequenceNb);
		sequence.hasChanged();
		int      sequenceId   = sequence.databaseId();
		String   sequenceName = sequence.name();
		
		if (sequenceId<=0) {
                    int crc32 = calculateSequenceCRC(sequence);
                    psInsertPathElement.setInt(1,2); //paetype
		    psInsertPathElement.setString(2,sequenceName);
                    psInsertPathElement.setInt(3,crc32);

                    psInsertPathElement.executeUpdate();

                    rs=psInsertPathElement.getGeneratedKeys();
                    rs.next();

		    sequenceId = rs.getInt(1);
		    result.put(sequenceName,sequenceId);
		    idToSequence.put(sequenceId,sequence);
		}
		else
		    result.put(sequenceName,-sequenceId);
		
	//	psInsertConfigSequenceAssoc.setInt(1,configId);
	//	psInsertConfigSequenceAssoc.setInt(2,sequenceId);
	//	psInsertConfigSequenceAssoc.setInt(3,sequenceNb);
	//	psInsertConfigSequenceAssoc.addBatch();
	    }

	    // only *now* set the new databaseId of changed sequences!
	    for (Map.Entry<Integer,Sequence> e : idToSequence.entrySet()) {
		int      id = e.getKey();
		Sequence s  = e.getValue();
		s.setDatabaseId(id);
	    }
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertSequences(configId="+configId+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}

/*	try {
	    psInsertConfigSequenceAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertSequences(configId="+configId+") failed "+
		"(batch insert): "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
*/	
	return result;
    }
    
    /** insert configuration's modules */
    private HashMap<String,Integer> insertModules(Configuration config)
	throws DatabaseException
    {
	HashMap<String,Integer> result = new HashMap<String,Integer>();

	ArrayList<IdInstancePair> modulesToStore =
	    new ArrayList<IdInstancePair>();
	
        ResultSet rs = null;

	for (int i=0;i<config.moduleCount();i++) {
	    ModuleInstance module     = config.module(i);
	    int            moduleId   = module.databaseId();
	    int            hmoduleId   = module.databaseId();
	    int            templateId = module.template().databaseId();
	    if (moduleId>0) {
		result.put(module.name(),moduleId);
	    }
	    else {
               int crc32 = calculateModuleCRC(module);
		try {
                    psInsertPathElement.setInt(1,1); //paetype
		    psInsertPathElement.setString(2,module.name());
		    psInsertPathElement.setInt(3,crc32);
		    psInsertPathElement.executeUpdate();

                    rs=psInsertPathElement.getGeneratedKeys();
                    rs.next();
                    moduleId=rs.getInt(1);

                    psInsertMod2Templ.setInt(1,moduleId);
                    psInsertMod2Templ.setInt(2,templateId);
                    psInsertMod2Templ.executeUpdate();

                    insertInstanceParameters(moduleId,module,psInsertMoElement);

		    result.put(module.name(),moduleId);
		    modulesToStore.add(new IdInstancePair(moduleId,module));
	            module.setDatabaseId(moduleId);

 /* Only for h_ tables
                    psCheckHPaElCrc.setInt(1,crc32);
                    rs=psCheckHPaElCrc.executeQuery();
                    if (rs.next()) {
                            hmoduleId=rs.getInt(1);
                System.out.println("insertPath: found crc in pathid "+moduleId);
                    } else {
                        psInsertHPaEl.setInt(1,crc32);
                        psInsertHPaEl.setInt(2,111111);
                        psInsertHPaEl.executeUpdate();
                        rs = psInsertHPaEl.getGeneratedKeys();
                        rs.next();
                        hmoduleId=rs.getInt(1);
                System.out.println("insertPath: created HPAE "+moduleId);
	                insertInstanceParameters(hmoduleId,module,psInsertParameterHMOE);

                        }
*/
		}
		catch (SQLException e) {
		    String errMsg =
			"ConfDB::insertModules(config="+config.toString()+
			" failed (moduleId="+moduleId+" templateId="+templateId+
			"): "+e.getMessage();
		    throw new DatabaseException(errMsg,e);
		}
	    }
	}
	
/*	try {
	    psInsertModule.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertModules(configId="+config.toString()+") failed "+
		"(batch insert): "+e.getMessage();
	    throw new DatabaseException(errMsg,e); 
	}
*/	
/*
	Iterator<IdInstancePair> it=modulesToStore.iterator();
	while (it.hasNext()) {
	    IdInstancePair pair     = it.next();
	    int            moduleId = pair.id;
	    ModuleInstance module   = (ModuleInstance)pair.instance;
	    insertInstanceParameters(moduleId,module,psInsertParameterMOE);
	    module.setDatabaseId(moduleId);
	}
*/	
	return result;
    }
    
    /** insert all references, regarding paths and sequences */
    private void insertReferences(Configuration config,
				  HashMap<String,Integer> pathHashMap,
				  HashMap<String,Integer> sequenceHashMap,
				  HashMap<String,Integer> moduleHashMap,
				  HashMap<String,Integer> streamHashMap)
	throws DatabaseException
    {
	// paths
	for (int i=0;i<config.pathCount();i++) {
	    Path path   = config.path(i);
	    int  pathId = pathHashMap.get(path.name());
	    
	    if (pathId>0) {
		
		for(int sequenceNb=0;sequenceNb<path.entryCount();sequenceNb++){
		    Reference r = path.entry(sequenceNb);
/*not allowed anymore		    if (r instanceof PathReference) {
			int childPathId = Math.abs(pathHashMap.get(r.name()));
			try {
			    psInsertPathPathAssoc.setInt(1,pathId);
			    psInsertPathPathAssoc.setInt(2,childPathId);
			    psInsertPathPathAssoc.setInt(3,sequenceNb);
			    psInsertPathPathAssoc.setInt(4,r.getOperator().ordinal());
			    psInsertPathPathAssoc.addBatch();
			}
			catch (SQLException e) {
			    String errMsg = 
				"ConfDB::insertReferences(config="+
				config.toString()+
				") failed (pathId="+pathId+",childPathId="+
				childPathId+",sequenceNb="+sequenceNb+"): "+
				e.getMessage();
			    throw new DatabaseException(errMsg,e);
			}
		    } else */
		    if (r instanceof SequenceReference) {
			int sequenceId=Math.abs(sequenceHashMap.get(r.name()));
			try {
			    psInsertPathElementAssoc.setInt(1,pathId);
			    psInsertPathElementAssoc.setInt(2,sequenceId);
			    psInsertPathElementAssoc.setNull(3,Types.INTEGER);//parent
			    psInsertPathElementAssoc.setInt(4,0); //lvl
			    psInsertPathElementAssoc.setInt(5,sequenceNb);
			    psInsertPathElementAssoc.setInt(6,r.getOperator().ordinal());
			    psInsertPathElementAssoc.executeUpdate();
                            insertSeqReferences(config,pathId,sequenceId,1,sequenceHashMap,moduleHashMap,streamHashMap);
			}
			catch (SQLException e) {
			    String errMsg = 
				"ConfDB::insertReferences(config="+
				config.toString()+
				") failed (pathId="+pathId+",sequenceId="+
				sequenceId+",sequenceNb="+sequenceNb+"): "+
				e.getMessage();
			    throw new DatabaseException(errMsg,e);
			}
		    }
		    else if (r instanceof ModuleReference) {
			int moduleId = moduleHashMap.get(r.name());
			try {
			    psInsertPathElementAssoc.setInt(1,pathId);
			    psInsertPathElementAssoc.setInt(2,moduleId);
			    psInsertPathElementAssoc.setNull(3,Types.INTEGER);//parent
			    psInsertPathElementAssoc.setInt(4,0); //lvl
			    psInsertPathElementAssoc.setInt(5,sequenceNb);
			    psInsertPathElementAssoc.setInt(6,r.getOperator().ordinal());
			    psInsertPathElementAssoc.executeUpdate();
			}
			catch (SQLException e) {
			    String errMsg = 
				"ConfDB::insertReferences(config="+
				config.toString()+
				") failed (pathId="+pathId+",moduleId="+
				moduleId+",sequenceNb="+sequenceNb+"): "+
				e.getMessage();
			    throw new DatabaseException(errMsg,e);
			}
		    }
		    else if (r instanceof OutputModuleReference) {
			String streamName = r.name().replaceFirst("hltOutput","");
			int outputModuleId = streamHashMap.get(streamName);
			if(outputModuleId<0)
			    outputModuleId = -1 * outputModuleId;
			try {
			    psInsertPathOutputModuleAssoc.setInt(1,pathId);
			    psInsertPathOutputModuleAssoc.setInt(2,outputModuleId);
			    psInsertPathOutputModuleAssoc.setInt(3,sequenceNb);
			    psInsertPathOutputModuleAssoc.setInt(4,r.getOperator().ordinal());
			    psInsertPathOutputModuleAssoc.executeUpdate();
			}
			catch (SQLException e) {
			    String errMsg = 
				"ConfDB::insertReferences(config="+
				config.toString()+
				") failed (pathId="+pathId+",moduleId="+
				outputModuleId+",sequenceNb="+sequenceNb+"): "+
				e.getMessage();
			    throw new DatabaseException(errMsg,e);
			    }
		    }
		}
	    }
	}
/*        try {
            psInsertPathPathAssoc.executeBatch();
            psInsertPathSequenceAssoc.executeBatch();
            psInsertPathModuleAssoc.executeBatch();
            psInsertPathOutputModuleAssoc.executeBatch();
            psInsertSequenceSequenceAssoc.executeBatch();
            psInsertSequenceModuleAssoc.executeBatch();
            psInsertSequenceOutputModuleAssoc.executeBatch();
        }
        catch (SQLException e) {
            String errMsg =
                "ConfDB::insertReferences(config="+config.toString()+") failed "+
                "(batch insert): "+e.getMessage();
            throw new DatabaseException(errMsg,e);
        }*/
    }

    /** insert all references, regarding paths and sequences */
    private void insertSeqReferences(Configuration config,int pathId,int parentId,int lvl, 
				  HashMap<String,Integer> sequenceHashMap,
				  HashMap<String,Integer> moduleHashMap,
				  HashMap<String,Integer> streamHashMap)
	throws DatabaseException
    {
	
	
	
	// sequences
	for (int i=0;i<config.sequenceCount();i++) {
	    Sequence sequence   = config.sequence(i);
	    int      sequenceId = sequenceHashMap.get(sequence.name());
	    
	    if (sequenceId>0) {
		
		for (int sequenceNb=0;sequenceNb<sequence.entryCount();
		     sequenceNb++) {
		    Reference r = sequence.entry(sequenceNb);
		    if (r instanceof SequenceReference) {
			int childSequenceId=Math.abs(sequenceHashMap
						     .get(r.name()));
			try {
			    psInsertPathElementAssoc.setInt(1,pathId);
			    psInsertPathElementAssoc.setInt(2,sequenceId);
			    psInsertPathElementAssoc.setInt(3,parentId); //lvl
			    psInsertPathElementAssoc.setInt(4,lvl); //lvl
			    psInsertPathElementAssoc.setInt(5,sequenceNb);
			    psInsertPathElementAssoc.setInt(6,r.getOperator().ordinal());
			    psInsertPathElementAssoc.executeUpdate();
                          
                            insertSeqReferences(config,pathId,sequenceId,lvl+1,sequenceHashMap,moduleHashMap,streamHashMap);

			}
			catch (SQLException e) {
			    e.printStackTrace();
			    String errMsg = 
				"ConfDB::insertReferences(config="+
				config.toString()+
				") failed (sequenceId="+sequenceId+" ("+
				sequence.name()+"), childSequenceId="+
				childSequenceId+" ("+r.name()+")"+
				",sequenceNb="+sequenceNb+"): "+e.getMessage();
			    throw new DatabaseException(errMsg,e);
			}
		    }
		    else if (r instanceof ModuleReference) {
			int moduleId = moduleHashMap.get(r.name());
			try {
			    psInsertPathElementAssoc.setInt(1,pathId);
			    psInsertPathElementAssoc.setInt(2,moduleId);
			    psInsertPathElementAssoc.setInt(3,parentId); //lvl
			    psInsertPathElementAssoc.setInt(4,lvl); //lvl
			    psInsertPathElementAssoc.setInt(5,sequenceNb);
			    psInsertPathElementAssoc.setInt(6,r.getOperator().ordinal());
			    psInsertPathElementAssoc.executeUpdate();
			}
			catch (SQLException e) {
			    String errMsg = 
				"ConfDB::insertReferences(config="+
				config.toString()+") failed (sequenceId="+
				sequenceId+",moduleId="+moduleId+
				",sequenceNb="+sequenceNb+"): "+e.getMessage();
			    throw new DatabaseException(errMsg,e);
			}
		    }
		    else if (r instanceof OutputModuleReference) {
			String streamName = r.name().replaceFirst("hltOutput","");
			int outputModuleId = streamHashMap.get(streamName);
			if(outputModuleId<0)
			    outputModuleId = -1 * outputModuleId;
			try {
			    psInsertPathOutputModuleAssoc.setInt(1,pathId);
			    psInsertPathOutputModuleAssoc.setInt(2,outputModuleId);
			    psInsertPathOutputModuleAssoc.setInt(3,sequenceNb);
			    psInsertPathOutputModuleAssoc.setInt(4,r.getOperator().ordinal());
			    psInsertPathOutputModuleAssoc.executeUpdate();
			}
			catch (SQLException e) {
			    String errMsg = 
				"ConfDB::insertReferences(config="+
				config.toString()+
				") failed (sequenceId="+sequenceId+",outputmoduleId="+
				outputModuleId+",sequenceNb="+sequenceNb+"): "+
				e.getMessage();
			    throw new DatabaseException(errMsg,e);
			    }
		     }
		}
	    }
	}
	
    }
   

    /** insert configuration's Event Content */
    private HashMap<String,Integer> insertEventContents(int configId,Configuration config) throws DatabaseException
    {
	HashMap<String,Integer> result = new HashMap<String,Integer>();

       	Iterator<EventContent> itC = config.contentIterator();
	
	ResultSet rse;
	
	while (itC.hasNext()) {
	    EventContent eventContent = itC.next();
	    int eventContentId = eventContent.databaseId();
	    if(!eventContent.hasChanged()){
		result.put(eventContent.name(),-1*eventContentId);
		continue;
	    }
	    try {
                psCheckContents.setString(1,eventContent.name());
                rse=psCheckContents.executeQuery();
                int id_cont=-1;
                if (rse.next()) {
			id_cont=rse.getInt(1);
                } else {
                	psInsertContents.setString(1,eventContent.name());
			psInsertContents.executeUpdate();
			rse = psInsertContents.getGeneratedKeys();
			rse.next();
                        id_cont=rse.getInt(1);
                }
                psInsertContentIds.setInt(1,id_cont);
                psInsertContentIds.executeUpdate();
		rse = psInsertContentIds.getGeneratedKeys();
		rse.next();
		eventContentId = rse.getInt(1);
		result.put(eventContent.name(),eventContentId);
		eventContent.setDatabaseId(eventContentId);
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::Event Content(config="+config.toString()+") failed "+
		    "(batch insert): "+e.getMessage();
		throw new DatabaseException(errMsg,e); 
	    }
	    eventContent.setDatabaseId(eventContentId);
	}
	
	itC = config.contentIterator();
	
	while (itC.hasNext()) {
	    EventContent eventContent = itC.next();
	    int eventContentId = eventContent.databaseId();
	    try {
		psInsertContentsConfigAssoc.setInt(1,eventContentId);
		psInsertContentsConfigAssoc.setInt(2,configId);
		psInsertContentsConfigAssoc.addBatch();
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::Event Content Config Association (config="+
		    config.toString()+") failed (batch insert): "+e.getMessage();
		throw new DatabaseException(errMsg,e); 
	    }
	    
	}
	try{
	    psInsertContentsConfigAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::Event Content Config Association(config="+
		config.toString()+") failed (batch insert): "+e.getMessage();
	    throw new DatabaseException(errMsg,e); 
	}
	return result;
    }

    /** insert event content statements */
    private void insertEventContentStatements(int configId,
					      Configuration config,
					      HashMap<String,Integer>eventContentHashMap)
	throws DatabaseException
    {
	for (int i=0;i<config.contentCount();i++) {
	    EventContent eventContent = config.content(i);
	    int  contentId =  eventContentHashMap.get(eventContent.name());
	    if(contentId<0){
		continue;
	    }
	    
	    for(int j=0;j<eventContent.commandCount();j++){
	    	
		OutputCommand command = eventContent.command(j);
		String className = command.className();
		String moduleName = command.moduleName();
		String extraName = command.extraName();
		String processName = command.processName();
		int iDrop = 1;
		if(command.isDrop()) {
		    iDrop = 0;
		}
		try {
			
		    psSelectStatementId.setString(1,className);
		    psSelectStatementId.setString(2,moduleName);
		    psSelectStatementId.setString(3,extraName);
		    psSelectStatementId.setString(4,processName);
		    psSelectStatementId.setInt(5,iDrop);
		    ResultSet rsStatementId = psSelectStatementId.executeQuery();
		    int statementId = -1;
		    while(rsStatementId.next()) {
			statementId = rsStatementId.getInt(1);
		    }
		    
		    if(statementId<0){
			psInsertEventContentStatements.setString(1,className);
			psInsertEventContentStatements.setString(2,moduleName);
			psInsertEventContentStatements.setString(3,extraName);
			psInsertEventContentStatements.setString(4,processName);
			iDrop = 1;
			if(command.isDrop()){
			    iDrop = 0;
			}
			psInsertEventContentStatements.setInt(5,iDrop);
			psInsertEventContentStatements.executeUpdate();
			ResultSet rsNewStatementId =
			    psInsertEventContentStatements.getGeneratedKeys();
			rsNewStatementId.next();
			statementId = rsNewStatementId.getInt(1);
		    }
		    
		    
		    psInsertECStatementAssoc.setInt(1,j);
		    psInsertECStatementAssoc.setInt(2,statementId);
		    psInsertECStatementAssoc.setInt(3,contentId);
		    Path parentPath = command.parentPath();
		    if(parentPath!=null) {
		    	psInsertECStatementAssoc.setInt(4,parentPath.databaseId());
		    } else {
			psInsertECStatementAssoc.setInt(4,-1);
		    }
		    psInsertECStatementAssoc.addBatch();
		}
		catch (SQLException e) {
		    String errMsg =
			"ConfDB::StatementID Update(config="+config.toString()+
			") failed (batch insert): "+e.getMessage();
		    throw new DatabaseException(errMsg,e); 
		}
		
	    }
	}
	try{
	    psInsertECStatementAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::StatementID Update(config="+config.toString()+") failed "+
		"(batch insert): "+e.getMessage();
	    throw new DatabaseException(errMsg,e); 
	}
    }
    
    /** insert configuration's Streams */
    private HashMap<String,Integer> insertStreams(int configId,Configuration config) throws DatabaseException
    {
	HashMap<String,Integer> result = new HashMap<String,Integer>();
       	Iterator<Stream> itS = config.streamIterator();

	ResultSet rs = null;

	while (itS.hasNext()) {
	    Stream stream = itS.next();
	    int streamId = stream.databaseId();
	    if(streamId>0){
		result.put(stream.name(),-1*streamId);
		continue;
	    }
	    try {
		psInsertStreams.setString(1,stream.name());
		psInsertStreams.executeUpdate();
		rs = psInsertStreams.getGeneratedKeys();
		rs.next();
		streamId = rs.getInt(1);
		psInsertStreamsIds.setInt(1,streamId);
		psInsertStreamsIds.setDouble(2,stream.fractionToDisk());
		psInsertStreamsIds.executeUpdate();
		rs = psInsertStreamsIds.getGeneratedKeys();
		rs.next();
		streamId = rs.getInt(1);
		stream.setDatabaseId(streamId);
		result.put(stream.name(),streamId);
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::Streams(config="+config.toString()+") failed "+
		    "(batch insert): "+e.getMessage();
		throw new DatabaseException(errMsg,e); 
	    }
	    
	    
	    OutputModule outputModule = stream.outputModule();

	    for(int sequenceNb=0;sequenceNb<outputModule.parameterCount();sequenceNb++){
	    Parameter p = outputModule.parameter(sequenceNb);
	    
	    if (!p.isDefault()) {
		if (p instanceof VPSetParameter) {
		    VPSetParameter vpset = (VPSetParameter)p;
		    insertVecParameterSet(streamId,sequenceNb,0,vpset,psInsertParameterOUTM);
		}
		else if (p instanceof PSetParameter) {
		    PSetParameter pset = (PSetParameter)p;
		    insertParameterSet(streamId,sequenceNb,0,pset,psInsertParameterOUTM);
		}
		else {
		    insertParameter(streamId,sequenceNb,0,p,psInsertParameterOUTM);
		}
	    }
	}
	}
	
	return result;
    }

    /** insert configuration's Primary Datasets */
    private HashMap<String,Integer> insertPrimaryDatasets(int configId,Configuration config) throws DatabaseException
    {
	HashMap<String,Integer> result = new HashMap<String,Integer>();
       	Iterator<PrimaryDataset> itP = config.datasetIterator();

	ResultSet rs = null;

	while (itP.hasNext()) {
	    PrimaryDataset primaryDataset = itP.next();
	    int datasetId = primaryDataset.databaseId();
	    if(!primaryDataset.hasChanged()){
		result.put(primaryDataset.name(),-1*datasetId);
		continue;
	    }
	    try {
		psInsertPrimaryDatasets.setString(1,primaryDataset.name());
		psInsertPrimaryDatasets.executeUpdate();
		rs = psInsertPrimaryDatasets.getGeneratedKeys();
		rs.next();
		datasetId = rs.getInt(1);
		result.put(primaryDataset.name(),datasetId);
		primaryDataset.setDatabaseId(datasetId);
	    }
	    catch (SQLException e) {
		String errMsg =
		    "ConfDB::Primary Dataset (config="+config.toString()+") failed "+
		    "(batch insert): "+e.getMessage();
		throw new DatabaseException(errMsg,e); 
	    }
	}
	return result;
    }


    private void insertEventContentStreamAssoc(HashMap<String,Integer> eventContentHashMap,HashMap<String,Integer> streamHashMap,Configuration config) throws DatabaseException
    {
	for (int i=0;i<config.contentCount();i++) {
	    EventContent eventContent  = config.content(i);
	    int  contentId = eventContentHashMap.get(eventContent.name());
	    
	    for (int j=0;j<eventContent.streamCount();j++) {
	        Stream stream  = eventContent.stream(j);
		int  streamId = streamHashMap.get(stream.name());
		if(contentId<0&&streamId<0)
		    continue;
		try {
		    psInsertECStreamAssoc.setInt(1,eventContent.databaseId());
		    psInsertECStreamAssoc.setInt(2,stream.databaseId());
		    psInsertECStreamAssoc.executeUpdate();
		    
		}
		catch (SQLException e) {
		    String errMsg =
			"ConfDB::Event Content(config="+config.toString()+") failed "+
			"(batch insert): "+e.getMessage();
		    throw new DatabaseException(errMsg,e); 
		}
	    }
	}
    }


    private void insertStreamDatasetAssoc(HashMap<String,Integer> streamHashMap,HashMap<String,Integer> primaryDatasetHashMap,Configuration config) throws DatabaseException
    {
	for (int i=0;i<config.streamCount();i++) {
	    Stream stream  = config.stream(i);
	    int  streamId = streamHashMap.get(stream.name());
	    for (int j=0;j<stream.datasetCount();j++) {
		PrimaryDataset primaryDataset = stream.dataset(j);
		int  datasetId = primaryDatasetHashMap.get(primaryDataset.name());
		if(datasetId < 0 && streamId < 0)
		    continue;
		try {
		    psInsertStreamDatasetAssoc.setInt(1,stream.databaseId());
		    psInsertStreamDatasetAssoc.setInt(2,primaryDataset.databaseId());
		    psInsertStreamDatasetAssoc.addBatch();
		}
		catch (SQLException e) {
		    String errMsg =
			"ConfDB::Stream Primary dataset association(config="+config.toString()+") failed "+
			"(batch insert): "+e.getMessage();
		    throw new DatabaseException(errMsg,e); 
		}
	    }
	}
	   
	try {
	    psInsertStreamDatasetAssoc.executeBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::Stream Primary dataset association(config="+config.toString()+") failed "+
		"(batch insert): "+e.getMessage();
	    throw new DatabaseException(errMsg,e); 
	}
    }

    
    //TODO This is the other Key Code to implement the share paths.
    /*
    private void insertPathStreamPDAssoc(HashMap<String,Integer> pathHashMap,HashMap<String,Integer> streamHashMap,HashMap<String,Integer> primaryDatasetHashMap,Configuration config,int configId) throws DatabaseException
    {
		for (int i=0;i<config.streamCount();i++) {
		    Stream stream  = config.stream(i);
		    int  streamId = streamHashMap.get(stream.name());
		    
		    if(streamId<0) continue;
		    
		    for (int j=0;j<stream.pathCount();j++) {
				Path path  = stream.path(j);
				int  pathId = pathHashMap.get(path.name());
				PrimaryDataset primaryDataset = stream.dataset(path); //TODO Here we have a problem. there could be more than one.
				int datasetId = -1;
	
				if(primaryDataset!=null){
				    datasetId = primaryDataset.databaseId();
				}
			
			    try {
				    psInsertPathStreamPDAssoc.setInt(1,path.databaseId());
				    psInsertPathStreamPDAssoc.setInt(2,streamId);
				    psInsertPathStreamPDAssoc.setInt(3,datasetId);
				    psInsertPathStreamPDAssoc.executeUpdate();
				} catch (SQLException e) {
				    String errMsg =
					"ConfDB::Event Content(config="+config.toString()+") failed "+
					"(batch insert): "+e.getMessage();
				    throw new DatabaseException(errMsg,e); 
				}
		    }
		}
    }
    */
    
    //TODO This is the other Key Code to implement the share paths.
    private void insertPathStreamPDAssoc(HashMap<String,Integer> pathHashMap,HashMap<String,Integer> streamHashMap,HashMap<String,Integer> primaryDatasetHashMap,Configuration config,int configId) throws DatabaseException
    {
		for (int i=0;i<config.streamCount();i++) {
		    Stream stream  = config.stream(i);
		    int  streamId = streamHashMap.get(stream.name());
		    
		    if(streamId<0) continue;
		    
		    for (int j=0;j<stream.pathCount();j++) {
				Path path  = stream.path(j);
				int  pathId = pathHashMap.get(path.name());
				//PrimaryDataset primaryDataset = stream.dataset(path); //TODO Here we have a problem. there could be more than one.
				ArrayList<PrimaryDataset> primaryDatasets = stream.datasets(path);
				int datasetId = -1;
	
				// Make relation between Stream and Path, datasetId = -1.
				if(primaryDatasets.size() == 0) {
				    try {
					    psInsertPathStreamPDAssoc.setInt(1,path.databaseId());
					    psInsertPathStreamPDAssoc.setInt(2,streamId);
					    psInsertPathStreamPDAssoc.setInt(3,datasetId);
					    psInsertPathStreamPDAssoc.executeUpdate();
					} catch (SQLException e) {
					    String errMsg =
						"ConfDB::Event Content(config="+config.toString()+") failed "+
						"(batch insert): "+e.getMessage();
					    throw new DatabaseException(errMsg,e); 
					}
				} else {
					// Next loop makes one or more than one relations between datasets/Streams/Paths.
					for (int ds = 0; ds < primaryDatasets.size(); ds++) {
						PrimaryDataset primaryDataset = primaryDatasets.get(ds);
						datasetId = primaryDataset.databaseId();
					    try {
						    psInsertPathStreamPDAssoc.setInt(1,path.databaseId());
						    psInsertPathStreamPDAssoc.setInt(2,streamId);
						    psInsertPathStreamPDAssoc.setInt(3,datasetId);
						    psInsertPathStreamPDAssoc.executeUpdate();
						} catch (SQLException e) {
						    String errMsg =
							"ConfDB::Event Content(config="+config.toString()+") failed "+
							"(batch insert): "+e.getMessage();
						    throw new DatabaseException(errMsg,e); 
						}
					}					
				} //end
		    }
		}
    }
    

    /** insert all instance parameters */
    private void insertInstanceParameters(int superId,Instance instance,PreparedStatement dbstmnt)
	throws DatabaseException
    {
	for(int sequenceNb=0;sequenceNb<instance.parameterCount();sequenceNb++){
	    Parameter p = instance.parameter(sequenceNb);
	    
	    if (!p.isDefault()) {
		if (p instanceof VPSetParameter) {
		    VPSetParameter vpset = (VPSetParameter)p;
		    insertVecParameterSet(superId,sequenceNb,0,vpset,dbstmnt);
		}
		else if (p instanceof PSetParameter) {
		    PSetParameter pset = (PSetParameter)p;
		    insertParameterSet(superId,sequenceNb,0,pset,dbstmnt);
		}
		else {
		    insertParameter(superId,sequenceNb,0,p,dbstmnt);
		}
	    }
	}
    }


    /** get all configuration names */
    public synchronized String[] getConfigNames() throws DatabaseException
    {
	ArrayList<String> listOfNames = new ArrayList<String>();
	ResultSet rs = null;
	try {
	    rs = psSelectConfigNames.executeQuery();
	    while (rs.next()) listOfNames.add(rs.getString(1)+"/"+rs.getString(2));
	}
	catch (SQLException e) {
	    String errMsg = "ConfDB::getConfigNames() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
	return listOfNames.toArray(new String[listOfNames.size()]);
    }

    /** get all configuration names associated to a given release */
    public synchronized String[] getConfigNamesByRelease(int releaseId)
	throws DatabaseException
    {
	ArrayList<String> listOfNames = new ArrayList<String>();
	ResultSet rs = null;
	try {
	    psSelectConfigNamesByRelease.setInt(1,releaseId);
	    rs = psSelectConfigNamesByRelease.executeQuery();
	    while (rs.next())
		listOfNames.add(rs.getString(1)+"/"+rs.getString(2)+
				"/V"+rs.getInt(3));
	}
	catch (SQLException e) {
	    String errMsg="ConfDB::getConfigNamesByRelease() failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
	return listOfNames.toArray(new String[listOfNames.size()]);
    }
    
    /** get list of software release tags */
    public synchronized String[] getReleaseTags() throws DatabaseException
    {
	reconnect();
	
	ArrayList<String> listOfTags = new ArrayList<String>();
	listOfTags.add(new String());
	ResultSet rs = null;
	try {
	    rs = psSelectReleaseTags.executeQuery();
	    while (rs.next()) {
		String releaseTag = rs.getString(2);
		if (!listOfTags.contains(releaseTag)) listOfTags.add(releaseTag);
	    }
	}
	catch (SQLException e) {
	    String errMsg = "ConfDB::getReleaseTags() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	return listOfTags.toArray(new String[listOfTags.size()]);
    }

    /** get list of software release tags */
    public synchronized String[] getReleaseTagsSorted() throws DatabaseException
    {
	reconnect();
	
	ArrayList<String> listOfTags = new ArrayList<String>();
	ResultSet rs = null;
	try {
	    rs = psSelectReleaseTagsSorted.executeQuery();
	    while (rs.next()) {
		String releaseTag = rs.getString(2);
		if (!listOfTags.contains(releaseTag)) listOfTags.add(releaseTag);
	    }
	}
	catch (SQLException e) {
	    String errMsg="ConfDB::getReleaseTagsSorted() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	return listOfTags.toArray(new String[listOfTags.size()]);
    }
    
    /** get the id of a directory, -1 if it does not exist */
    public synchronized int getDirectoryId(String directoryName) throws DatabaseException
    {
	reconnect();
	ResultSet rs = null;
	try {
	    psSelectDirectoryId.setString(1,directoryName);
	    rs = psSelectDirectoryId.executeQuery();
	    rs.next();
	    return rs.getInt(1);
	}
	catch (SQLException e) {
	    String errMsg = 
		"ConfDB::getDirectoryId(directoryName="+directoryName+
		") failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }

    /** get hash map with all directories */
    public synchronized HashMap<Integer,Directory> getDirectoryHashMap()
	throws DatabaseException
    {
	reconnect();
	
	HashMap<Integer,Directory> directoryHashMap =
	    new HashMap<Integer,Directory>();
	
	Directory rootDir = null;
	ResultSet rs = null;
	try {
	    
	    rs = psSelectDirectories.executeQuery();
	    while (rs.next()) {
		int    dirId       = rs.getInt(1);
		int    parentDirId = rs.getInt(2);
		String dirName     = rs.getString(3);
		String dirCreated  = rs.getTimestamp(4).toString();
		
		if (directoryHashMap.size()==0) {
		    rootDir = new Directory(dirId,dirName,dirCreated,null);
		    directoryHashMap.put(dirId,rootDir);
		}
		else {
		    if (!directoryHashMap.containsKey(parentDirId))
			throw new DatabaseException("parentDir not found in DB"+
						    " (parentDirId="+parentDirId+
						    ")");
		    Directory parentDir = directoryHashMap.get(parentDirId);
		    Directory newDir    = new Directory(dirId,
							dirName,
							dirCreated,
							parentDir);
		    parentDir.addChildDir(newDir);
		    directoryHashMap.put(dirId,newDir);
		}
	    }
	    
	    return directoryHashMap;
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::getDirectoryHashMap() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
	

    /** get the configuration id for a configuration name */
    public synchronized int getConfigId(String fullConfigName) throws DatabaseException
    {
	reconnect();

	int version = 0;
	
	int index = fullConfigName.lastIndexOf("/V");
	if (index>=0) {
	    version = Integer.parseInt(fullConfigName.substring(index+2));
	    fullConfigName = fullConfigName.substring(0,index);
	}

	index = fullConfigName.lastIndexOf("/");
	if (index<0) {
	    String errMsg =
		"ConfDB::getConfigId(fullConfigName="+fullConfigName+
		") failed (invalid name).";
	    throw new DatabaseException(errMsg);
	}
	
	String dirName    = fullConfigName.substring(0,index);
	String configName = fullConfigName.substring(index+1);

	ResultSet rs = null;
	try {
	    
	    PreparedStatement ps = null;
	    
	    if (version>0) {
		ps = psSelectConfigurationId;
		ps.setString(1,dirName);
		ps.setString(2,configName);
		ps.setInt(3,version);
	    }
	    else {
		ps = psSelectConfigurationIdLatest;
		ps.setString(1,dirName);
		ps.setString(2,configName);
	    }
	    
	    rs = ps.executeQuery();
	    rs.next();
	    return rs.getInt(1);
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::getConfigId(fullConfigName="+fullConfigName+
		") failed (dirName="+dirName+", configName="+configName+
		",version="+version+"): "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    
    /** get ConfigInfo for a particular configId */
    public synchronized ConfigInfo getConfigInfo(int configId) throws DatabaseException
    {
	ConfigInfo result = getConfigInfo(configId,loadConfigurationTree());
	if (result==null) {
	    String errMsg =
		"ConfDB::getConfigInfo(configId="+configId+") failed.";
	    throw new DatabaseException(errMsg);
	}
	return result;
    }

    //
    // REMOVE CONFIGURATIONS / RELEASES
    //

    /** delete a configuration from the DB */
    public synchronized void removeConfiguration(int configId) throws DatabaseException
    {
	ResultSet rs = null;
	try {
	    dbConnector.getConnection().setAutoCommit(false);

	    removeGlobalPSets(configId);
	    removeEDSources(configId);
	    removeESSources(configId);
	    removeESModules(configId);
	    removeServices(configId);
	    removeSequences(configId);
	    removePaths(configId);
	    removeContent(configId);
    
	    psDeleteConfiguration.setInt(1,configId);
	    psDeleteConfiguration.executeUpdate();
	    
	    dbConnector.getConnection().commit();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    try { dbConnector.getConnection().rollback(); }
	    catch (SQLException e2) { e2.printStackTrace(); }
	    throw new DatabaseException("remove Configuration FAILED",e);
	}
	finally {
	    try { dbConnector.getConnection().setAutoCommit(true); }
	    catch (SQLException e) {}
	    dbConnector.release(rs);
	}
    }
    
    /** remove Content */
    public synchronized void removeContent(int configId) throws SQLException {
    	ResultSet rs1 = null;
    	try {
    		
    		psSelectContentForConfig.setInt(1,configId);
    		rs1 = psSelectContentForConfig.executeQuery();	// select Ids from EventContent.
    		
    		psDeleteContentFromConfig.setInt(1, configId);
    		psDeleteContentFromConfig.executeUpdate();		// delete from ConfigurationContentAssoc.
    		
    		while(rs1.next()) {
    			// for each EventContentId:
    			int pEventId 	= rs1.getInt(1);
    			ResultSet rs2 = null;

				// Delete Streams
				try {
					psSelectStreamByEventContent.setInt(1, pEventId);
					rs2 = psSelectStreamByEventContent.executeQuery();
    				// delete ECStreamAssoc
    				psDeleteECStreamFromEventCont.setInt(1, pEventId);
    				psDeleteECStreamFromEventCont.executeUpdate();

    				/*
					while(rs2.next()) {
						int pStreamId = rs2.getInt(1);
						ResultSet rs3 = null;
						try {
    						psSelectStreamAssocByStream.setInt(1, pStreamId);
    						rs3 = psSelectStreamAssocByStream.executeQuery();
    						
    						// do not delete Streams but SuperIds.
    						if(!rs3.next()) {
								removeParameters(pStreamId);
								psDeleteSuperId.setInt(1,pStreamId);
								psDeleteSuperId.executeUpdate();
    						}
							
						} finally {
							dbConnector.release(rs3);
						}
					}
					*/
				} finally {
					dbConnector.release(rs2);
				}
    				
				rs2 = null;
    			// Delete EventContentStatements
				try {
					psSelectECStatementByEventContent.setInt(1, pEventId);
					rs2 = psSelectECStatementByEventContent.executeQuery();
					// Delete ECStatementAssoc
    				psDeleteECStatementFromEventCont.setInt(1, pEventId);
    				psDeleteECStatementFromEventCont.executeUpdate();
    				
					while(rs2.next()) {
						int pECStatId = rs2.getInt(1);
						ResultSet rs3 = null;
						try {
							psSelectECStatementByECStatement.setInt(1, pECStatId);
							rs3 = psSelectECStatementByECStatement.executeQuery();
							
							if(!rs3.next()) {
								// delete eventContentStatement
								psDeleteEventContentStatement.setInt(1, pECStatId);
							}							
						} finally {
							dbConnector.release(rs3);
						}
					}
				} finally {
					dbConnector.release(rs2);
				}

    		} // end while
    	} finally {
    		dbConnector.release(rs1);
    	}
    }
    
    
    
    /** remove global psets of a configuration */
    public synchronized void removeGlobalPSets(int configId) throws SQLException
    {
	ResultSet rs1 = null;
	try {
	    psSelectPSetsForConfig.setInt(1,configId);
	    rs1 = psSelectPSetsForConfig.executeQuery();
	    psDeletePSetsFromConfig.setInt(1,configId);
	    psDeletePSetsFromConfig.executeUpdate();
	    while (rs1.next()) {
		int psetId = rs1.getInt(1);
		ResultSet rs2 = null;
		try {
		    psSelectPSetId.setInt(1,psetId);
		    rs2 = psSelectPSetId.executeQuery();
		    
		    if (!rs2.next()) {
			removeParameters(psetId);
			psDeleteSuperId.setInt(1,psetId);
			psDeleteSuperId.executeUpdate();
		    }
		}
		finally {
		    dbConnector.release(rs2);
		}
	    }
	}
	finally {
	    dbConnector.release(rs1);
	}
    }
    /** remove EDSources from a configuration */
    public synchronized void removeEDSources(int configId) throws SQLException
    {
	ResultSet rs1 = null;
	try {
	    psSelectEDSourcesForConfig.setInt(1,configId);
	    rs1 = psSelectEDSourcesForConfig.executeQuery();
	    psDeleteEDSourcesFromConfig.setInt(1,configId);
	    psDeleteEDSourcesFromConfig.executeUpdate();
	    while (rs1.next()) {
		int edsId = rs1.getInt(1);
		ResultSet rs3 = null;
		try {
		    psSelectEDSourceId.setInt(1,edsId);
		    rs3 = psSelectEDSourceId.executeQuery();

		    if (!rs3.next()) {
			removeParameters(edsId);
			psDeleteSuperId.setInt(1,edsId);
			psDeleteSuperId.executeUpdate();
		    }
		}
		finally {
		    dbConnector.release(rs3);
		}
	    }
	}
	finally {
	    dbConnector.release(rs1);
	}
    }
    /** remove ESSources */
    public synchronized void removeESSources(int configId) throws SQLException
    {
	ResultSet rs1 = null;
	try {
	    psSelectESSourcesForConfig.setInt(1,configId);
	    rs1 = psSelectESSourcesForConfig.executeQuery();
	    psDeleteESSourcesFromConfig.setInt(1,configId);
	    psDeleteESSourcesFromConfig.executeUpdate();
	    while (rs1.next()) {
		int essId = rs1.getInt(1);
		ResultSet rs3 = null;
		try {
		    psSelectESSourceId.setInt(1,essId);
		    rs3 = psSelectESSourceId.executeQuery();

		    if (!rs3.next()) {
			removeParameters(essId);
			psDeleteSuperId.setInt(1,essId);
			psDeleteSuperId.executeUpdate();
		    }
		}
		finally {
		    dbConnector.release(rs3);
		}
	    }
	}
	finally {
	    dbConnector.release(rs1);
	}
    }
    /** remove ESModules */
    public synchronized void removeESModules(int configId) throws SQLException
    {
	ResultSet rs1 = null;
	try {
	    psSelectESModulesForConfig.setInt(1,configId);
	    rs1 = psSelectESModulesForConfig.executeQuery();
	    psDeleteESModulesFromConfig.setInt(1,configId);
	    psDeleteESModulesFromConfig.executeUpdate();
	    while (rs1.next()) {
		int esmId = rs1.getInt(1);
		ResultSet rs3 = null;
		try {
		    psSelectESModuleId.setInt(1,esmId);
		    rs3 = psSelectESModuleId.executeQuery();
		    
		    if (!rs3.next()) {
			removeParameters(esmId);
			psDeleteSuperId.setInt(1,esmId);
			psDeleteSuperId.executeUpdate();
		    }
		}
		finally {
		    dbConnector.release(rs3);
		}
	    }
	}
	finally {
	    dbConnector.release(rs1);
	}
    }
    /** remove Services */
    public synchronized void removeServices(int configId) throws SQLException
    {
	ResultSet rs1 = null;
	try {
	    psSelectServicesForConfig.setInt(1,configId);
	    rs1 = psSelectServicesForConfig.executeQuery();
	    psDeleteServicesFromConfig.setInt(1,configId);
	    psDeleteServicesFromConfig.executeUpdate();
	    while (rs1.next()) {
		int svcId = rs1.getInt(1);
		ResultSet rs3 = null;
		try {
		    psSelectServiceId.setInt(1,svcId);
		    rs3 = psSelectServiceId.executeQuery();
		    
		    if (!rs3.next()) {
			removeParameters(svcId);
			psDeleteSuperId.setInt(1,svcId);
			psDeleteSuperId.executeUpdate();
		    }
		}
		finally {
		    dbConnector.release(rs3);
		}
	    }
	}
	finally {
	    dbConnector.release(rs1);
	}
    }
    /** remove Sequences */
    public synchronized void removeSequences(int configId) throws SQLException
    {
	ResultSet rs1 = null;
	try {
	    psSelectSequencesForConfig.setInt(1,configId);
	    rs1 = psSelectSequencesForConfig.executeQuery();
	    psDeleteSequencesFromConfig.setInt(1,configId);
	    psDeleteSequencesFromConfig.executeUpdate();
	    while (rs1.next()) {
		int seqId = rs1.getInt(1);
		ResultSet rs2 = null;
		try {
		    psSelectSequenceId.setInt(1,seqId);
		    rs2 = psSelectSequenceId.executeQuery();
		    
		    if (!rs2.next()) {
			psDeleteChildSeqsFromParentSeq.setInt(1,seqId);
			psDeleteChildSeqsFromParentSeq.executeUpdate();
			psDeleteChildSeqFromParentSeqs.setInt(1,seqId);
			psDeleteChildSeqFromParentSeqs.executeUpdate();
			psDeleteChildSeqFromParentPaths.setInt(1,seqId);
			psDeleteChildSeqFromParentPaths.executeUpdate();
			ResultSet rs3 = null;
			try {
			    psSelectModulesForSeq.setInt(1,seqId);
			    rs3 = psSelectModulesForSeq.executeQuery();
			    psDeleteModulesFromSeq.setInt(1,seqId);
			    psDeleteModulesFromSeq.executeUpdate();

			    while (rs3.next()) {
				int modId = rs3.getInt(1);
				removeModule(modId);
			    }
			}
			finally {
			    dbConnector.release(rs3);
			}
			psDeleteSequence.setInt(1,seqId);
			psDeleteSequence.executeUpdate();
		    }
		}
		finally {
		    dbConnector.release(rs2);
		}
	    }
	}
	finally {
	    dbConnector.release(rs1);
	}
    }
    /** remove Paths */
    public synchronized void removePaths(int configId) throws SQLException
    {
	ResultSet rs1 = null;
	try {
	    psSelectPathsForConfig.setInt(1,configId);
	    rs1 = psSelectPathsForConfig.executeQuery();
	    psDeletePathsFromConfig.setInt(1,configId);
	    psDeletePathsFromConfig.executeUpdate();
	    while (rs1.next()) {
		int pathId = rs1.getInt(1);
		ResultSet rs2 = null;
		try {
		    psSelectPathId.setInt(1,pathId);
		    rs2 = psSelectPathId.executeQuery();
		    
		    if (!rs2.next()) {
			psDeleteChildPathsFromParentPath.setInt(1,pathId);
			psDeleteChildPathsFromParentPath.executeUpdate();
			psDeleteChildPathFromParentPaths.setInt(1,pathId);
			psDeleteChildPathFromParentPaths.executeUpdate();
			psDeleteChildSeqsFromParentPath.setInt(1,pathId);
			psDeleteChildSeqsFromParentPath.executeUpdate();
			
			psDeletePathStreamDataSetAssoc.setInt(1, pathId);
			psDeletePathStreamDataSetAssoc.executeUpdate();
		    psDeletePathOutputModAssoc.setInt(1, pathId);
		    psDeletePathOutputModAssoc.executeUpdate();

			
			ResultSet rs3 = null;
			try {
			    psSelectModulesForPath.setInt(1,pathId);
			    rs3 = psSelectModulesForPath.executeQuery();
			    psDeleteModulesFromPath.setInt(1,pathId);
			    psDeleteModulesFromPath.executeUpdate();
			    
			    while (rs3.next()) {
				int modId = rs3.getInt(1);
				removeModule(modId);
			    }
			}
			finally {
			    dbConnector.release(rs3);
			}
			psDeletePath.setInt(1,pathId);
			psDeletePath.executeUpdate();
		    }
		}
		finally {
		    dbConnector.release(rs2);
		}
	    }
	}
	finally {
	    dbConnector.release(rs1);
	}
    }
    /** remove Modules */
    public synchronized void removeModule(int modId) throws SQLException
    {
	ResultSet rs1 = null;
	ResultSet rs2 = null;
	try {
	    psSelectModuleIdBySeq.setInt(1,modId);
	    rs1 = psSelectModuleIdBySeq.executeQuery();
	    psSelectModuleIdByPath.setInt(1,modId);
	    rs2 = psSelectModuleIdByPath.executeQuery();
	    if (!rs1.next()&&!rs2.next()) {
		removeParameters(modId);
		psDeleteSuperId.setInt(1,modId);
		psDeleteSuperId.executeUpdate();
	    }
	}
	finally {
	    dbConnector.release(rs1);
	    dbConnector.release(rs2);
	}
    }
    /** remove Parameters */
    public synchronized void removeParameters(int parentId) throws SQLException
    {
	ResultSet rsParams = null;
	ResultSet rsPSets  = null;
	ResultSet rsVPSets = null;

	try {
	    // parameters
	    psSelectParametersForSuperId.setInt(1,parentId);
	    rsParams = psSelectParametersForSuperId.executeQuery();
	    psDeleteParametersForSuperId.setInt(1,parentId);
	    psDeleteParametersForSuperId.executeUpdate();
	    while (rsParams.next()) {
		int paramId = rsParams.getInt(1);
		try {
		    psDeleteParameter.setInt(1,paramId);
		    psDeleteParameter.executeUpdate();
		}
		// TEST
		catch (SQLException e) {
		    System.out.println("parentId="+parentId+", "+
				       "paramId="+paramId+": "+
				       "NOT REMOVED!");
		}
		// END TEST
	    }
	}
	finally {
	    dbConnector.release(rsParams);
	}
	
	Statement stmt1 = null;
	Statement stmt2 = null;
	Statement stmt3 = null;
	
	try {
	    // psets
	    stmt1 = dbConnector.getConnection().createStatement();
	    stmt2 = dbConnector.getConnection().createStatement();
	    stmt3 = dbConnector.getConnection().createStatement();

	    rsPSets = stmt1.executeQuery("SELECT psetId "+
					 "FROM SuperIdParamSetAssoc "+
					 "WHERE superId="+parentId);
	    while (rsPSets.next()) {
		int psetId = rsPSets.getInt(1);
		removeParameters(psetId);
		stmt2.executeUpdate("DELETE FROM SuperIdParamSetAssoc "+
				    "WHERE "+
				    "superId="+parentId+
				    " AND psetId="+psetId);
		stmt3.executeUpdate("DELETE FROM SuperIds WHERE superId="+
				    psetId);
	    }
	}
	finally {
	    dbConnector.release(rsPSets);
	    stmt1.close();
	    stmt2.close();
	    stmt3.close();
	}
	
	try {
	    // vpsets
	    stmt1 = dbConnector.getConnection().createStatement();
	    stmt2 = dbConnector.getConnection().createStatement();
	    stmt3 = dbConnector.getConnection().createStatement();

	    rsVPSets = stmt1.executeQuery("SELECT vpsetId "+
					  "FROM SuperIdVecParamSetAssoc "+
					  "WHERE superId="+parentId);
	    while (rsVPSets.next()) {
		int vpsetId = rsVPSets.getInt(1);
		removeParameters(vpsetId);
		stmt2.executeUpdate("DELETE FROM SuperIdVecParamSetAssoc "+
				    "WHERE "+
				    "superId="+parentId+
				    " AND vpsetId="+vpsetId);
		stmt3.executeUpdate("DELETE FROM SuperIds WHERE superId="+
				    vpsetId);
	    }
	}
	finally {
	    dbConnector.release(rsVPSets);
	    stmt1.close();
	    stmt2.close();
	    stmt3.close();
	}
    }
    
    //
    //INSERT SOFTWARE RELEASE
    //
    public synchronized void insertRelease(String releaseTag,SoftwareRelease newRelease) throws DatabaseException
    {
	try{
	    dbConnector.getConnection().setAutoCommit(false);
	    
	    psSelectReleaseId.setString(1,releaseTag);
	    ResultSet rs = psSelectReleaseId.executeQuery();
	    if(rs.next())
		return;
	    psInsertReleaseTag.setString(1,releaseTag);
	    psInsertReleaseTag.executeUpdate();
	    ResultSet rsInsertReleaseTag = psInsertReleaseTag.getGeneratedKeys();
	    rsInsertReleaseTag.next();
	    int releaseId = rsInsertReleaseTag.getInt(1);
	    insertSoftwareSubsystem(newRelease,releaseId);

	    /*was already	    psInsertEDSourceTemplate.executeBatch();
	    psInsertESSourceTemplate.executeBatch();
	    psInsertESModuleTemplate.executeBatch();
	    psInsertServiceTemplate.executeBatch();
	    psInsertModuleTemplate.executeBatch();*/
	    
	    // insert parameter bindings / values
	    //psInsertParameterSet.executeBatch();
	    //psInsertVecParameterSet.executeBatch();
	    //psInsertGlobalPSet.executeBatch();
	    //psInsertSuperIdParamAssoc.executeBatch();
	    //psInsertSuperIdParamSetAssoc.executeBatch();
	    //psInsertSuperIdVecParamSetAssoc.executeBatch();
	    Iterator<PreparedStatement> itPS =
		insertParameterHashMap.values().iterator();
	    while (itPS.hasNext()) {
		PreparedStatement itP = itPS.next();
		if(itP!=null)
		    itP.executeBatch();
	    }
	    
	    dbConnector.getConnection().commit();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    try { dbConnector.getConnection().rollback(); }
	    catch (SQLException e2) { e2.printStackTrace(); }
	    throw new DatabaseException("removeSoftwareRelease FAILED",e); 
	}
	finally {
	    try { dbConnector.getConnection().setAutoCommit(true); }
	    catch (SQLException e) { e.printStackTrace(); }
	}
    }

    public synchronized void insertSoftwareSubsystem(SoftwareRelease newRelease,int releaseId) throws SQLException
    {
	Iterator<SoftwareSubsystem> subsysIt = newRelease.subsystemIterator();
	while (subsysIt.hasNext()) {
	    SoftwareSubsystem subsys = subsysIt.next();
	    
	    ResultSet rsSelectSoftwareSubsystemId;
	    psSelectSoftwareSubsystemId.setString(1,subsys.name());
	    rsSelectSoftwareSubsystemId=psSelectSoftwareSubsystemId.executeQuery();
	    
	    int subsysId;
	    if(rsSelectSoftwareSubsystemId.next()){
		subsysId = rsSelectSoftwareSubsystemId.getInt(1);
	    }else{
		psInsertSoftwareSubsystem.setString(1,subsys.name());
		psInsertSoftwareSubsystem.executeUpdate();
		ResultSet rsInsertSoftwareSubsystem = psInsertSoftwareSubsystem.getGeneratedKeys();
		rsInsertSoftwareSubsystem.next();
		subsysId = rsInsertSoftwareSubsystem.getInt(1);
	    }
	    insertSoftwarePackages(subsys,subsysId,releaseId);
	}
    }
    
    public synchronized void insertSoftwarePackages(SoftwareSubsystem subsys,int subsysId,int releaseId) throws SQLException
    {
       
	Iterator<SoftwarePackage> pkgIt = subsys.packageIterator();
	while (pkgIt.hasNext()) {
	    SoftwarePackage pkg = pkgIt.next();
	    
	    ResultSet rsSelectSoftwarePackageId;
	    psSelectSoftwarePackageId.setInt(1,subsysId);
	    psSelectSoftwarePackageId.setString(2,pkg.name());
	    rsSelectSoftwarePackageId=psSelectSoftwarePackageId.executeQuery();
	    
	    int pkgId;
	    if(rsSelectSoftwarePackageId.next()){
		pkgId = rsSelectSoftwarePackageId.getInt(1);
	    }else{
		psInsertSoftwarePackage.setInt(1,subsysId);
		psInsertSoftwarePackage.setString(2,pkg.name());
		psInsertSoftwarePackage.executeUpdate();
		ResultSet rsInsertSoftwarePackage = psInsertSoftwarePackage.getGeneratedKeys();
		rsInsertSoftwarePackage.next();
		pkgId = rsInsertSoftwarePackage.getInt(1);
	    }
	    insertTemplateIntoRelease(pkg,pkgId,releaseId);
	}
    }

    public synchronized void insertTemplateIntoRelease(SoftwarePackage softwarePackage,int pkgId,int releaseId) throws SQLException
    {
	Iterator<Template> templateIt = softwarePackage.templateIterator();
	while (templateIt.hasNext()) {

	 
	    Template template = templateIt.next();
	    int templateId = -1;
	    try{
		templateId = insertSuperId();
		insertSuperIdReleaseAssoc(templateId,releaseId);
		insertTemplateParameters(templateId,template,psInsertParameterMODT);
	    }catch (DatabaseException  e2) { 
		e2.printStackTrace(); 
	    } 

	    template.setDatabaseId(templateId);

	    if (template instanceof EDSourceTemplate) {
		psInsertEDSourceTemplateRelease.setInt(1,templateId);
		psInsertEDSourceTemplateRelease.setString(2,template.name());
		psInsertEDSourceTemplateRelease.setString(3,template.cvsTag());
		psInsertEDSourceTemplateRelease.setInt(4,pkgId);
		psInsertEDSourceTemplateRelease.executeUpdate();
	    }
	    else if (template instanceof ESSourceTemplate) {
	
		psInsertESSourceTemplateRelease.setInt(1,templateId);
		psInsertESSourceTemplateRelease.setString(2,template.name());
		psInsertESSourceTemplateRelease.setString(3,template.cvsTag());
		psInsertESSourceTemplateRelease.setInt(4,pkgId);
		psInsertESSourceTemplateRelease.executeUpdate();
	    }
	    else if (template instanceof ESModuleTemplate) {
		psInsertESModuleTemplateRelease.setInt(1,templateId);
		psInsertESModuleTemplateRelease.setString(2,template.name());
		psInsertESModuleTemplateRelease.setString(3,template.cvsTag());
		psInsertESModuleTemplateRelease.setInt(4,pkgId);
		psInsertESModuleTemplateRelease.executeUpdate();
	
	    }
	    else if (template instanceof ServiceTemplate) {
	
		psInsertServiceTemplateRelease.setInt(1,templateId);
		psInsertServiceTemplateRelease.setString(2,template.name());
		psInsertServiceTemplateRelease.setString(3,template.cvsTag());
		psInsertServiceTemplateRelease.setInt(4,pkgId);
		psInsertServiceTemplateRelease.executeUpdate();
	    }
	    else if (template instanceof ModuleTemplate) {
		int moduleType = moduleTypeIdHashMap.get(template.type());
		psInsertModuleTemplateRelease.setInt(1,templateId);
		psInsertModuleTemplateRelease.setInt(2,moduleType);
		psInsertModuleTemplateRelease.setString(3,template.name());
		psInsertModuleTemplateRelease.setString(4,template.cvsTag());
		psInsertModuleTemplateRelease.setInt(5,pkgId);
		psInsertModuleTemplateRelease.executeUpdate();
	    }	  
	}
    }
    
    
    /** insert all instance parameters */
    private void insertTemplateParameters(int superId,Template template,PreparedStatement dbtable)
	throws DatabaseException
    {
	for(int sequenceNb=0;sequenceNb<template.parameterCount();sequenceNb++){
	    Parameter p = template.parameter(sequenceNb);
	    if (p instanceof VPSetParameter) {
		VPSetParameter vpset = (VPSetParameter)p;
		insertVecParameterSet(superId,sequenceNb,0,vpset,dbtable);
	    }
	    else if (p instanceof PSetParameter) {
		PSetParameter pset = (PSetParameter)p;
		insertParameterSet(superId,sequenceNb,0,pset,dbtable);
	    }
	    else {
		insertParameter(superId,sequenceNb,0,p,dbtable);
	    }
	}
    }

    //
    // REMOVE SOFTWARE-RELEASE
    //

    /** remove a software release from the DB */
    public synchronized void removeSoftwareRelease(int releaseId) throws DatabaseException
    {
	if (getConfigNamesByRelease(releaseId).length>0) {
	    System.err.println("ConfDB::removeSoftwareRelease ERROR: "+
			       "Can't remove release with associated "+
			       "configurations!)");
	    return;
	}

	try {
	    dbConnector.getConnection().setAutoCommit(false);
	    
	    removeEDSourceTemplates(releaseId);
	    removeESSourceTemplates(releaseId);
	    removeESModuleTemplates(releaseId);
	    removeServiceTemplates(releaseId);
	    removeModuleTemplates(releaseId);

	    psDeleteSoftwareRelease.setInt(1,releaseId);
	    psDeleteSoftwareRelease.executeUpdate();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    try { dbConnector.getConnection().rollback(); }
	    catch (SQLException e2) { e2.printStackTrace(); }
	    throw new DatabaseException("removeSoftwareRelease FAILED",e); 
	}
	finally {
	    try { dbConnector.getConnection().setAutoCommit(true); }
	    catch (SQLException e) { e.printStackTrace(); }
	}
    }
    
    /** remove EDSourceTemplates from a release */
    private void removeEDSourceTemplates(int releaseId) throws SQLException
    {
	ResultSet rs = null;
	try {
	    psSelectEDSourceTemplatesForRelease.setInt(1,releaseId);
	    rs = psSelectEDSourceTemplatesForRelease.executeQuery();
	    removeTemplates(rs,releaseId);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    /** remove ESSourceTemplates from a release */
    private void removeESSourceTemplates(int releaseId) throws SQLException
    {
	ResultSet rs = null;
	try {
	    psSelectESSourceTemplatesForRelease.setInt(1,releaseId);
	    rs = psSelectESSourceTemplatesForRelease.executeQuery();
	    removeTemplates(rs,releaseId);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    /** remove ESModuleTemplates from a release */
    private void removeESModuleTemplates(int releaseId) throws SQLException
    {
	ResultSet rs = null;
	try {
	    psSelectESModuleTemplatesForRelease.setInt(1,releaseId);
	    rs = psSelectESModuleTemplatesForRelease.executeQuery();
	    removeTemplates(rs,releaseId);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    /** remove ServiceTemplates from a release */
    private void removeServiceTemplates(int releaseId) throws SQLException
    {
	ResultSet rs = null;
	try {
	    psSelectServiceTemplatesForRelease.setInt(1,releaseId);
	    rs = psSelectServiceTemplatesForRelease.executeQuery();
	    removeTemplates(rs,releaseId);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    /** remove ModuleTemplates from a release */
    private void removeModuleTemplates(int releaseId) throws SQLException
    {
	ResultSet rs = null;
	try {
	    psSelectModuleTemplatesForRelease.setInt(1,releaseId);
	    rs = psSelectModuleTemplatesForRelease.executeQuery();
	    removeTemplates(rs,releaseId);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    /** remove templates of any kind from release, given the superIds */
    private void removeTemplates(ResultSet rs,int releaseId)
	throws SQLException
    {
	while (rs.next()) {
	    int superId = rs.getInt(1);
	    
	    psDeleteTemplateFromRelease.setInt(1,superId);
	    psDeleteTemplateFromRelease.setInt(2,releaseId);
	    psDeleteTemplateFromRelease.executeUpdate();
	    
	    ResultSet rs3 = null;
	    try {
		psSelectTemplateId.setInt(1,superId);
		rs3 = psSelectTemplateId.executeQuery();
		
		if (!rs3.next()) {
		    removeParameters(superId);
		    psDeleteSuperId.setInt(1,superId);
		    psDeleteSuperId.executeUpdate();
		}
	    }

	    // DEBUG
	    catch (SQLException e) {
		System.out.println("releaseId="+releaseId+" "+
				   "superId="  +superId  +"\n");
		throw(e);
	    }
	    // END DEBUG
	    
	    finally {
		dbConnector.release(rs3);
	    }
	}
    }


    //
    // private member functions
    //

    /** prepare database transaction statements */
    private void prepareStatements() throws DatabaseException
    {
	int[] keyColumn = { 1 };

	try {
	    //
	    // SELECT
	    //
	    
		
		
	    psSelectModuleTypes =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
      	      	 " v_moduletypes.id," +
   		 " v_moduletypes.type " +
   		 "FROM v_moduletypes");
	    preparedStatements.add(psSelectModuleTypes);
	    

	    psSelectParameterTypes =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
		 " DISTINCT f_moelements.paramtype FROM f_moelements");
	    preparedStatements.add(psSelectParameterTypes);
	    

	    psSelectDirectories =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   		 " Directories.id," +
   		 " Directories.id_parentDir," +
   		 " Directories.name," +
   		 " Directories.created " +
   		 "FROM v_directories Directories " +
   		 "ORDER BY Directories.name ASC");
	    psSelectDirectories.setFetchSize(512);
	    preparedStatements.add(psSelectDirectories);
	    
            psSelectConfigurations =
                dbConnector.getConnection().prepareStatement
		("SELECT" +
   " Configurations.Id," +
   " Configurations.id_parentDir," +
   " Configurations.config," +
   " Configurations.version," +
   " Configurations.created," +
   " Configurations.creator," +
   " SoftwareReleases.releaseTag," +
   " Configurations.processName," +
   " Configurations.description " +
   "FROM v_confversions Configurations " +
   "JOIN v_softreleases SoftwareReleases " +
   "ON SoftwareReleases.Id = Configurations.id_release " +
   "ORDER BY Configurations.config ASC");
	    psSelectConfigurations.setFetchSize(512);
	    preparedStatements.add(psSelectConfigurations);
	    
	    psSelectLockedConfigurations =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
		 " v_directories.Name," +
		 " v_lockedconfs.id_config," +
		 " v_lockedconfs.userName " +
		 "FROM v_lockedconfs " +
		 "JOIN v_directories " +
		 "ON v_lockedconfs.id_parentdir = v_directories.id");
	    preparedStatements.add(psSelectLockedConfigurations);

	    psSelectUsersForLockedConfigs =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
		 " v_lockedconfs.userName "+
		 "FROM v_lockedconfs");
	    preparedStatements.add(psSelectUsersForLockedConfigs);

	    psSelectConfigNames =
		dbConnector.getConnection().prepareStatement
		("SELECT DISTINCT" +
   " Directories.name," +
   " Configurations.config " +
   "FROM v_confversions Configurations " +
   "JOIN v_directories Directories " +
   "ON Configurations.id_parentDir = Directories.id " +
   "ORDER BY Directories.name ASC,Configurations.config ASC");
	    psSelectConfigNames.setFetchSize(1024);
	    preparedStatements.add(psSelectConfigNames);

	    psSelectConfigNamesByRelease =
		dbConnector.getConnection().prepareStatement
		("SELECT DISTINCT" +
   " Directories.name," +
   " Configurations.config, " +
   " Configurations.version " +
   "FROM v_confversions Configurations " +
   "JOIN v_directories Directories " +
   "ON Configurations.id_parentDir = Directories.id " +
   "WHERE Configurations.id_release = ?" +
   "ORDER BY Directories.name ASC,Configurations.config ASC");
	    psSelectConfigNamesByRelease.setFetchSize(1024);
	    preparedStatements.add(psSelectConfigNamesByRelease);

	    psSelectDirectoryId =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " Directories.id " +
   "FROM v_directories Directories "+
   "WHERE Directories.name = ?");
	    preparedStatements.add(psSelectDirectoryId);
	    
	    psSelectConfigurationId =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " Configurations.id " +
   "FROM v_confversions Configurations "+
   "JOIN v_directories Directories " +
   "ON Directories.id=Configurations.id_parentDir " +
   "WHERE Directories.name = ? AND" +
   " Configurations.config = ? AND" +
   " Configurations.version = ?");
	    preparedStatements.add(psSelectConfigurationId);
	    
	    psSelectConfigurationIdLatest =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
  " Configurations.id," +
   " Configurations.version " +
   "FROM v_confversions Configurations " +
   "JOIN v_directories Directories " +
   "ON Directories.id=Configurations.id_parentDir " +
   "WHERE Directories.name = ? AND" +
   " Configurations.config = ? " +
   "ORDER BY Configurations.version DESC");
	    preparedStatements.add(psSelectConfigurationIdLatest);

	    psSelectConfigurationCreated =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
  " Configurations.created " +
   "FROM v_confversions Configurations " +
   "WHERE Configurations.id = ?");
	    preparedStatements.add(psSelectConfigurationCreated);
	    
	    psSelectReleaseTags =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " SoftwareReleases.id," +
   " SoftwareReleases.releaseTag " +
   "FROM v_softreleases SoftwareReleases " +
   "ORDER BY SoftwareReleases.id DESC");
	    psSelectReleaseTags.setFetchSize(32);
	    preparedStatements.add(psSelectReleaseTags);
	    
	    psSelectReleaseTagsSorted =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " SoftwareReleases.id," +
   " SoftwareReleases.releaseTag " +
   "FROM v_softreleases SoftwareReleases " +
   "ORDER BY SoftwareReleases.releaseTag ASC");
	    psSelectReleaseTagsSorted.setFetchSize(32);
	    preparedStatements.add(psSelectReleaseTagsSorted);
	    
	    psSelectReleaseId =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
  " SoftwareReleases.id "+
   "FROM v_softreleases SoftwareReleases " +
   "WHERE SoftwareReleases.releaseTag = ?");

	    psSelectReleaseTag =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " SoftwareReleases.releaseTag " +
   "FROM v_softreleases SoftwareReleases " +
   "WHERE SoftwareReleases.id = ?");
	    preparedStatements.add(psSelectReleaseTag);
	    
	    psSelectReleaseTagForConfig =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " SoftwareReleases.releaseTag " +
   "FROM v_softreleases SoftwareReleases " +
   "JOIN v_confversions Configurations " +
   "ON Configurations.id_release = SoftwareReleases.id " +
   "WHERE Configurations.id = ?");
	    preparedStatements.add(psSelectReleaseTagForConfig);
	    
	    psSelectSoftwareSubsystems =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " SoftwareSubsystems.subsysId," +
   " SoftwareSubsystems.name " +
   "FROM v_softsubsystems SoftwareSubsystems");
	    psSelectSoftwareSubsystems.setFetchSize(64);
	    preparedStatements.add(psSelectSoftwareSubsystems);

	    psSelectSoftwarePackages =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " SoftwarePackages.packageid," +
   " SoftwarePackages.subsysId," +
   " SoftwarePackages.name " +
   "FROM v_softpackages SoftwarePackages");
	    psSelectSoftwarePackages.setFetchSize(512);
	    preparedStatements.add(psSelectSoftwarePackages);

	    psSelectEDSourceTemplate =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
  " EDSourceTemplates.id," +
   " EDSourceTemplates.name," +
   " EDSourceTemplates.cvstag " +
   "FROM v_edstemplates EDSourceTemplates " +
   "WHERE EDSourceTemplates.name=? AND EDSourceTemplates.cvstag= ?");
	    preparedStatements.add(psSelectEDSourceTemplate);

	    psSelectESSourceTemplate =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " ESSourceTemplates.id," +
   " ESSourceTemplates.name," +
   " ESSourceTemplates.cvstag " +
   "FROM v_esstemplates ESSourceTemplates " +
   "WHERE name=? AND cvstag=?");
	    preparedStatements.add(psSelectESSourceTemplate);
	    
	    psSelectESModuleTemplate =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " ESModuleTemplates.id," +
   " ESModuleTemplates.name," +
   " ESModuleTemplates.cvstag " +
   "FROM v_esmtemplates ESModuleTemplates " +
   "WHERE name=? AND cvstag=?");
	    preparedStatements.add(psSelectESModuleTemplate);

	    psSelectServiceTemplate =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
  " ServiceTemplates.id," +
   " ServiceTemplates.name," +
   " ServiceTemplates.cvstag " +
   "FROM v_srvtemplates ServiceTemplates " +
   "WHERE name=? AND cvstag=?");
	    preparedStatements.add(psSelectServiceTemplate);

	    psSelectModuleTemplate = 
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " ModuleTemplates.id," +
   " ModuleTemplates.id_mtype," +
   " ModuleTemplates.name," +
   " ModuleTemplates.cvstag " +
   "FROM f_moduletemplates ModuleTemplates " +
   "WHERE name=? AND cvstag=?");
	    preparedStatements.add(psSelectModuleTemplate);

	        psSelectStreams =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
  " Streams.id,"+
   " Streams.name "+
   "FROM v_streams Streams " +
   "ORDER BY Streams.name ASC");


	    psSelectPrimaryDatasets =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
   " PrimaryDatasets.id,"+
   " PrimaryDatasets.name "+
   "FROM v_datasets PrimaryDatasets " +
   "ORDER BY PrimaryDatasets.name ASC");

	    
//	    psSelectPrimaryDatasetEntries =
//		dbConnector.getConnection().prepareStatement
//		("SELECT" +
//		 " PrimaryDatasetPathAssoc.datasetId," +
//		 " PrimaryDatasets.datasetLabel,"+
//		 " PrimaryDatasetPathAssoc.pathId " +
//		 "FROM PrimaryDatasetPathAssoc "+
//		 "JOIN PrimaryDatasets "+
////		 "ON PrimaryDatasets.datasetId=PrimaryDatasetPathAssoc.datasetId "+
//		 "JOIN ConfigurationPathAssoc " +
//		 "ON ConfigurationPathAssoc.pathId=PrimaryDatasetPathAssoc.pathId "+
//		 "WHERE ConfigurationPathAssoc.configId=?");
//	    psSelectPrimaryDatasetEntries.setFetchSize(64);
//	    preparedStatements.add(psSelectPrimaryDatasetEntries);
	  
//	    psSelectPSetsForConfig =
//		dbConnector.getConnection().prepareStatement
//		("SELECT"+
//		 " ParameterSets.superId "+
//		 "FROM ParameterSets " +
////		 "JOIN ConfigurationParamSetAssoc " +
//		 "ON ConfigurationParamSetAssoc.psetId="+
//		 "ParameterSets.superId " +
//		 "WHERE ConfigurationParamSetAssoc.configId=?");
//	    preparedStatements.add(psSelectPSetsForConfig);
	    
//	    psSelectContentForConfig =
//	    	dbConnector.getConnection().prepareStatement(
//	    			"SELECT "											+
//	    			"EventContents.eventContentId "						+
//	    			"FROM	EventContents "								+
//	    			"JOIN	ConfigurationContentAssoc "					+
//	    			"ON		ConfigurationContentAssoc.eventContentId "	+
//	    			"	= 	EventContents.eventContentId "				+
//	    			"WHERE	ConfigurationContentAssoc.configId = ?"		);
//	    preparedStatements.add(psSelectContentForConfig);
//	    	
	    psSelectEDSourcesForConfig =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " EDSources.id "+
    "FROM v_edsources EDSources "+
   "JOIN v_conf2eds ConfigurationEDSourceAssoc " +
   "ON ConfigurationEDSourceAssoc.id_edsource=EDSources.id " +
   "WHERE ConfigurationEDSourceAssoc.id_confver=?");
	    preparedStatements.add(psSelectEDSourcesForConfig);
	    
	    psSelectESSourcesForConfig =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
  " ESSources.id "+
   "FROM v_essources ESSources "+
   "JOIN v_conf2ess ConfigurationESSourceAssoc " +
   "ON ConfigurationESSourceAssoc.id_essource=ESSources.id " +
   "WHERE ConfigurationESSourceAssoc.id_confver=?");
	    preparedStatements.add(psSelectESSourcesForConfig);

	    psSelectESModulesForConfig =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " ESModules.id "+
   "FROM v_esmodules ESModules "+
   "JOIN v_conf2esm ConfigurationESModuleAssoc " +
   "ON ConfigurationESModuleAssoc.id_esmodule=ESModules.id " +
   "WHERE ConfigurationESModuleAssoc.id_confver=?");
	    preparedStatements.add(psSelectESModulesForConfig);
		    
	    psSelectServicesForConfig =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " Services.id "+
   "FROM v_services Services "+
   "JOIN v_conf2srv ConfigurationServiceAssoc " +
   "ON ConfigurationServiceAssoc.id_service=Services.id " +
   "WHERE ConfigurationServiceAssoc.id_confver=?");
 	    preparedStatements.add(psSelectServicesForConfig);
	    
//   	    psSelectSequencesForConfig =
//		dbConnector.getConnection().prepareStatement
//		("SELECT"+
//		 " Sequences.sequenceId "+
//		 "FROM Sequences "+
//		 "JOIN ConfigurationSequenceAssoc "+
//		 "ON ConfigurationSequenceAssoc.sequenceId=Sequences.sequenceId "+
//		 "WHERE ConfigurationSequenceAssoc.configId=?");
//	    preparedStatements.add(psSelectSequencesForConfig);
	    
	    psSelectPathsForConfig =
		dbConnector.getConnection().prepareStatement
/*h_		("SELECT"+
  " Paths.id_pathiduq "+
   "FROM h_pathid2uq Paths " +
   "JOIN h_pathid2conf ConfigurationPathAssoc " +
   "ON ConfigurationPathAssoc.id_pathid=Paths.id_pathid " +
   "WHERE ConfigurationPathAssoc.id_confver=?");
*/
                ("SELECT v_pathids.id  FROM v_pathids   JOIN v_pathid2conf ON v_pathid2conf.id_pathid=v_pathids.id WHERE v_pathid2conf.id_confver=?  order by v_pathid2conf.id_pathid");
	    preparedStatements.add(psSelectPathsForConfig);

//	    psSelectModulesForSeq =
//		dbConnector.getConnection().prepareStatement
//		("SELECT "+
//		 " SequenceModuleAssoc.moduleId "+
//		 "FROM SequenceModuleAssoc "+
//		 "WHERE sequenceId=?");
//	    preparedStatements.add(psSelectModulesForSeq);
//	    
//	    psSelectModulesForPath =
//		dbConnector.getConnection().prepareStatement
//		("SELECT "+
//		 " PathModuleAssoc.moduleId "+
//		 "FROM PathModuleAssoc "+
//		 "WHERE pathId=?");
//	    preparedStatements.add(psSelectModulesForPath);
//	    
	    psSelectEDSourceTemplatesForRelease =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " EDSourceTemplates.id "+
   "FROM v_edstemplates EDSourceTemplates "+
   "JOIN v_edst2rele SuperIdReleaseAssoc " +
   "ON SuperIdReleaseAssoc.id_edstemplate=EDSourceTemplates.id " +
   "WHERE SuperIdReleaseAssoc.id_release=?");
 	    preparedStatements.add(psSelectEDSourceTemplatesForRelease);
	    
	    psSelectESSourceTemplatesForRelease =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " ESSourceTemplates.id "+
   "FROM v_esstemplates ESSourceTemplates "+
   "JOIN v_esst2rele SuperIdReleaseAssoc " +
   "ON SuperIdReleaseAssoc.id_esstemplate=ESSourceTemplates.id " +
   "WHERE SuperIdReleaseAssoc.id_release=?");
 	    preparedStatements.add(psSelectESSourceTemplatesForRelease);
	    
	    psSelectESModuleTemplatesForRelease =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " ESModuleTemplates.id "+
   "FROM v_esmtemplates ESModuleTemplates "+
   "JOIN v_esmt2rele SuperIdReleaseAssoc " +
   "ON SuperIdReleaseAssoc.id_esmtemplate=ESModuleTemplates.id " +
   "WHERE SuperIdReleaseAssoc.id_release=?");
 	    preparedStatements.add(psSelectESModuleTemplatesForRelease);
	    
	    psSelectServiceTemplatesForRelease =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " ServiceTemplates.id "+
   "FROM v_srvtemplates ServiceTemplates "+
   "JOIN v_srvt2rele SuperIdReleaseAssoc " +
   "ON SuperIdReleaseAssoc.id_srvtemplate=ServiceTemplates.id " +
   "WHERE SuperIdReleaseAssoc.id_release=?");
 	    preparedStatements.add(psSelectServiceTemplatesForRelease);
	    
	    psSelectModuleTemplatesForRelease =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
  " ModuleTemplates.id "+
   "FROM v_moduletemplates ModuleTemplates "+
   "JOIN v_modt2rele SuperIdReleaseAssoc " +
   "ON SuperIdReleaseAssoc.id_modtemplate=ModuleTemplates.id " +
   "WHERE SuperIdReleaseAssoc.id_release=?");
 	    preparedStatements.add(psSelectModuleTemplatesForRelease);
	    
//	    psSelectParametersForSuperId =
//		dbConnector.getConnection().prepareStatement
//		("SELECT"+
//		 " SuperIdParameterAssoc.paramId "+
//		 "FROM SuperIdParameterAssoc "+
//		 "WHERE SuperIdParameterAssoc.superId=?");
//	    preparedStatements.add(psSelectParametersForSuperId);
//	    
//	    psSelectPSetsForSuperId =
//		dbConnector.getConnection().prepareStatement
//		("SELECT"+
//		 " SuperIdParamSetAssoc.psetId "+
//		 "FROM SuperIdParamSetAssoc "+
//		 "WHERE SuperIdParamSetAssoc.superId=?");
//	    preparedStatements.add(psSelectPSetsForSuperId);
//	    
//	    psSelectVPSetsForSuperId =
//		dbConnector.getConnection().prepareStatement
//		("SELECT"+
//		 " SuperIdVecParamSetAssoc.vpsetId "+
//		 "FROM SuperIdVecParamSetAssoc "+
//		 "WHERE SuperIdVecParamSetAssoc.superId=?");
//	    preparedStatements.add(psSelectVPSetsForSuperId);
//	    
//	    psSelectPSetId =
//		dbConnector.getConnection().prepareStatement
//		("SELECT ConfigurationParamSetAssoc.psetId "+
//		 "FROM ConfigurationParamSetAssoc "+
//		 "WHERE ConfigurationParamSetAssoc.psetId=?");
//	    preparedStatements.add(psSelectPSetId);

	    psSelectEDSourceId =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " ConfigurationEDSourceAssoc.id_edsource "+
   "FROM v_conf2eds ConfigurationEDSourceAssoc "+
   "WHERE ConfigurationEDSourceAssoc.id_edsource=?");
	    preparedStatements.add(psSelectEDSourceId);

	    psSelectESSourceId =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " ConfigurationESSourceAssoc.id_essource "+
   "FROM v_conf2ess ConfigurationESSourceAssoc "+
   "WHERE ConfigurationESSourceAssoc.id_essource=?");
	    preparedStatements.add(psSelectESSourceId);

	    psSelectESModuleId =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
  " ConfigurationESModuleAssoc.id_esmodule "+
   "FROM v_conf2edm ConfigurationESModuleAssoc "+
   "WHERE ConfigurationESModuleAssoc.id_esmodule=?");
     preparedStatements.add(psSelectESModuleId);

	    preparedStatements.add(psSelectESModuleId);

	    psSelectServiceId =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " ConfigurationServiceAssoc.id_service "+
   "FROM v_conf2srv ConfigurationServiceAssoc "+
   "WHERE ConfigurationServiceAssoc.id_service=?");
	    preparedStatements.add(psSelectServiceId);

	    psSelectSequenceId =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
		 " ConfigurationSequenceAssoc.sequenceId "+
		 "FROM ConfigurationSequenceAssoc "+
		 "WHERE ConfigurationSequenceAssoc.sequenceId=?");
	    preparedStatements.add(psSelectSequenceId);

	    psSelectPathId =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
   " ConfigurationPathAssoc.id_pathid "+
   "FROM v_pathid2conf ConfigurationPathAssoc "+
   "WHERE ConfigurationPathAssoc.id_pathid=?");
	    preparedStatements.add(psSelectPathId);
	    
	    
/*	    psSelectStreamByEventContent = 
	    	dbConnector.getConnection().prepareStatement(
	    			"SELECT  ECS.STREAMID "				+
	    			"FROM    ECSTREAMASSOC   ECS "		+
	    			"WHERE   ECS.EVENTCONTENTID  = ?"	);
	    preparedStatements.add(psSelectStreamByEventContent);
	    
	    psSelectStreamAssocByStream = 
	    	dbConnector.getConnection().prepareStatement(
	    			"SELECT ECS.STREAMID "				+
	    			"FROM	ECSTREAMASSOC	ECS "		+
	    			"WHERE	ECS.STREAMID = ?"			);
	    preparedStatements.add(psSelectStreamAssocByStream);

	    psSelectECStatementByEventContent =
	    	dbConnector.getConnection().prepareStatement(
	    			"SELECT ECSTAT.STATEMENTID "		+
	    			"FROM	ECSTATEMENTASSOC	ECSTAT "+
	    			"WHERE	ECSTAT.EVENTCONTENTID = ?"	);
	    preparedStatements.add(psSelectECStatementByEventContent);
	    
	    psSelectECStatementByECStatement = 
	    	dbConnector.getConnection().prepareStatement(
	    			"SELECT ECSTAT.STATEMENTID "		+
	    			"FROM	ECSTATMENTASSOC	ECSTAT "	+
	    			"WHERE	ECSTAT.STATEMENTID = ? "	);
	    preparedStatements.add(psSelectECStatementByECStatement);
	    
	    psSelectModuleIdBySeq =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
		 " SequenceModuleAssoc.moduleId "+
		 "FROM SequenceModuleAssoc "+
		 "WHERE SequenceModuleAssoc.moduleId=?");
	    preparedStatements.add(psSelectModuleIdBySeq);

	    psSelectModuleIdByPath =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
		 " PathModuleAssoc.moduleId "+
		 "FROM PathModuleAssoc "+
		 "WHERE PathModuleAssoc.moduleId=?");
	    preparedStatements.add(psSelectModuleIdByPath);

	    psSelectTemplateId =
		dbConnector.getConnection().prepareStatement
		("SELECT"+
		 " SuperIdReleaseAssoc.superId "+
		 "FROM SuperIdReleaseAssoc "+
		 "WHERE SuperIdReleaseAssoc.superId=?");
	    preparedStatements.add(psSelectTemplateId);
*/
	    
	    //Event Content, Streams and Primary Datsets

	    psSelectEventContentEntries =
		dbConnector.getConnection().prepareStatement
            ("select v_eventcontents.id,v_eventcontents.name from v_eventcontents,v_eventcontentids,v_conf2evco where v_eventcontentids.id=v_conf2evco.id_evcoid and v_eventcontents.id=v_eventcontentids.id_evco and v_conf2evco.id_confver=?");
	    psSelectEventContentEntries.setFetchSize(1024);
	    preparedStatements.add(psSelectEventContentEntries);

		
	    psSelectStreamEntries =
		dbConnector.getConnection().prepareStatement
            ("SELECT DISTINCT v_streams.id+5000000,v_streams.name,v_streamids.FRACTODISK,V_EVENTCONTENTS.ID as evcoid,V_EVENTCONTENTS.name as evconame "+
                 "FROM v_streamids,v_streams,V_EVENTCONTENTIDS,V_EVENTCONTENTS,V_EVCO2STREAM,v_pathid2outm,v_pathid2conf " +
                 "WHERE v_streams.id=v_streamids.id_stream " +
                 "AND V_EVCO2STREAM.ID_STREAMID=v_streamids.id AND V_EVCO2STREAM.id_evcoid=V_EVENTCONTENTIDS.ID " +
                 "AND V_EVENTCONTENTIDS.ID_EVCO=V_EVENTCONTENTS.ID "+
                 "AND v_pathid2conf.id_pathid=v_pathid2outm.id_pathId AND v_streamids.id=v_pathid2outm.id_streamid "+
                 "AND v_pathid2conf.id_confver = ?" );
	    psSelectStreamEntries.setFetchSize(1024);
	    preparedStatements.add(psSelectStreamEntries);
	    

	    psSelectDatasetEntries =
		dbConnector.getConnection().prepareStatement
            ("SELECT distinct v_datasets.id, v_datasets.name,v_streams.id+5000000 as streamid,v_streams.name as label from v_pathid2strdst, v_pathid2conf,v_datasetids,v_datasets,v_streams,v_streamids WHERE v_pathid2strdst.id_pathid=v_pathid2conf.id_pathid and v_datasets.id=v_datasetids.id_dataset and v_datasetids.id=v_pathid2strdst.id_datasetid and v_streams.id=v_streamids.id_stream and v_streamids.id=v_pathid2strdst.id_streamid AND v_pathid2conf.id_confver = ?");
	    //psSelectDatasetEntries.setFetchSize(64);
	    preparedStatements.add(psSelectDatasetEntries);
	    
	    psSelectPathStreamDatasetEntries =
		dbConnector.getConnection().prepareStatement
           /* h_  ("SELECT distinct h_pathid2uq.id_pathiduq,v_streams.id+5000000 as streamid,v_datasets.id as datasetid, v_datasets.name from v_pathid2strdst, v_pathid2conf,v_datasetids,v_datasets,v_streams,v_streamids,h_pathid2uq WHERE h_pathid2uq.id_pathid=v_pathid2conf.id_pathid and v_pathid2strdst.id_pathid=v_pathid2conf.id_pathid and  v_datasets.id=v_datasetids.id_dataset and v_datasetids.id=v_pathid2strdst.id_datasetid and v_streams.id=v_streamids.id_stream and v_streamids.id=v_pathid2strdst.id_streamid AND v_pathid2conf.id_confver = ?");*/
             ("SELECT distinct v_pathid2conf.id_pathid,v_streams.id+5000000 as streamid,v_datasets.id as datasetid, v_datasets.name from v_pathid2strdst, v_pathid2conf,v_datasetids,v_datasets,v_streams,v_streamids WHERE v_pathid2strdst.id_pathid=v_pathid2conf.id_pathid and  v_datasets.id=v_datasetids.id_dataset and v_datasetids.id=v_pathid2strdst.id_datasetid and v_streams.id=v_streamids.id_stream and v_streamids.id=v_pathid2strdst.id_streamid AND v_pathid2conf.id_confver = ?");
          //psSelectPathStreamDatasetEntries.setFetchSize(64);
	    preparedStatements.add(psSelectPathStreamDatasetEntries);

	    psSelectStatementId = 
		dbConnector.getConnection().prepareStatement
		("SELECT id from v_evcoStatements WHERE classN = ? " +
		 " AND moduleL = ? AND extraN = ? AND processN = ? AND statementType = ? ");
	    preparedStatements.add(psSelectStatementId);
	    
	    psSelectEventContentStatements =  
		dbConnector.getConnection().prepareStatement
           /* h_  ("select distinct v_evcostatements.id as statemId, v_evcostatements.classn,v_evcostatements.modulel,v_evcostatements.extran,v_evcostatements.processn,v_evcostatements.statementtype,v_eventcontents.id as evcoid, v_evco2stat.statementrank,v_eventcontents.name,decode(v_evco2stat.id_pathid,-1,'-1',0,'0',(select h_pathid2uq.id_pathiduq from h_pathid2uq where h_pathid2uq.id_pathid=v_evco2stat.id_pathid)) from v_eventcontents,v_eventcontentids,v_evcostatements, v_conf2evco, v_evco2stat where v_eventcontents.id=v_eventcontentids.id_evco and v_evcostatements.id=v_evco2stat.id_stat and v_evco2stat.id_evcoid=v_conf2evco.id_evcoid and v_eventcontentids.id=v_conf2evco.id_evcoid and v_conf2evco.id_confver=? order by statemid");*/
               ("select distinct v_evcostatements.id as statemId, v_evcostatements.classn,v_evcostatements.modulel,v_evcostatements.extran,v_evcostatements.processn,v_evcostatements.statementtype,v_eventcontents.id as evcoid, v_evco2stat.statementrank,v_eventcontents.name,v_evco2stat.id_pathid from v_eventcontents,v_eventcontentids,v_evcostatements, v_conf2evco, v_evco2stat where v_eventcontents.id=v_eventcontentids.id_evco and v_evcostatements.id=v_evco2stat.id_stat and v_evco2stat.id_evcoid=v_conf2evco.id_evcoid and v_eventcontentids.id=v_conf2evco.id_evcoid and v_conf2evco.id_confver=? order by statemid");
	    preparedStatements.add(psSelectEventContentStatements);


	    
	    psSelectReleaseCount =
		dbConnector.getConnection().prepareStatement
  ("SELECT COUNT(*) FROM v_softreleases SoftwareReleases");
	    preparedStatements.add(psSelectReleaseCount);

	    psSelectConfigurationCount =
		dbConnector.getConnection().prepareStatement
  ("SELECT COUNT(*) FROM v_configurations Configurations");
	    preparedStatements.add(psSelectConfigurationCount);
	    
	    psSelectDirectoryCount =
		dbConnector.getConnection().prepareStatement
  ("SELECT COUNT(*) FROM v_directories Directories");
	    preparedStatements.add(psSelectDirectoryCount);

	    psSelectSuperIdCount =
		dbConnector.getConnection().prepareStatement
  ("SELECT COUNT(*) FROM v_confversions Configurations"); //sv just a placeholder 
	    preparedStatements.add(psSelectSuperIdCount);

	    psSelectEDSourceTemplateCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_edstemplates EDSourceTemplates");
	    preparedStatements.add(psSelectEDSourceTemplateCount);
	    
	    psSelectEDSourceCount =
		dbConnector.getConnection().prepareStatement
  ("SELECT COUNT(*) FROM v_edsources EDSources");
	    preparedStatements.add(psSelectEDSourceCount);

	    psSelectESSourceTemplateCount =
		dbConnector.getConnection().prepareStatement
  ("SELECT COUNT(*) FROM v_esstemplates ESSourceTemplates");
	    preparedStatements.add(psSelectESSourceTemplateCount);

	    psSelectESSourceCount =
		dbConnector.getConnection().prepareStatement
  ("SELECT COUNT(*) FROM v_essources ESSources");
	    preparedStatements.add(psSelectESSourceCount);

	    psSelectESModuleTemplateCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_esmtemplates ESModuleTemplates");
	    preparedStatements.add(psSelectESModuleTemplateCount);

	    psSelectESModuleCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_esmodules ESModules");
	    preparedStatements.add(psSelectESModuleCount);

	    psSelectServiceTemplateCount =
		dbConnector.getConnection().prepareStatement
  ("SELECT COUNT(*) FROM v_srvtemplates ServiceTemplates");
	    preparedStatements.add(psSelectServiceTemplateCount);

	    psSelectServiceCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_services Services");
	    preparedStatements.add(psSelectServiceCount);
	    
	    psSelectModuleTemplateCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_moduletemplates ModuleTemplates"); 
	    preparedStatements.add(psSelectModuleTemplateCount);
	    
	    psSelectModuleCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_paelements Modules WHERE paetype = 1");
	    preparedStatements.add(psSelectModuleCount);

	    psSelectSequenceCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_paelements Modules WHERE paetype = 1");
	    preparedStatements.add(psSelectSequenceCount);

	    psSelectPathCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_pathids Paths");
	    preparedStatements.add(psSelectPathCount);
	    
	    psSelectParameterCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_pathids Paths"); //just a placholder
	    preparedStatements.add(psSelectParameterCount);

	    psSelectParameterSetCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_pathids Paths"); //just a placholder
	    preparedStatements.add(psSelectParameterSetCount);

	    psSelectVecParameterSetCount =
		dbConnector.getConnection().prepareStatement
("SELECT COUNT(*) FROM v_pathids Paths");//just a placeholder
	    preparedStatements.add(psSelectVecParameterSetCount);
	    
	    
	    //
	    // INSERT
	    //
	   
	    psInsertDirectory =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_directories " +
   "(id_parentdir,name,created) " +
   "VALUES (?, ?, SYSDATE)",
   keyColumn);
	    preparedStatements.add(psInsertDirectory);


	    if (dbType.equals(dbTypeMySQL))
		psInsertConfigurationVers =
		    dbConnector.getConnection().prepareStatement
		    ("INSERT INTO Configurations " +
		     "(releaseId,configDescriptor,parentDirId,config," +
		     "version,created,creator,processName,description) " +
		     "VALUES (?, ?, ?, ?, ?, NOW(), ?, ?, ?)",keyColumn);
	    else if (dbType.equals(dbTypeOracle))
		psInsertConfigurationVers =
		    dbConnector.getConnection().prepareStatement
		    ("INSERT INTO v_confversions " +
		     "(id_config,version,id_release,name,id_parentDir,config," +
		     "created,creator,processName,description) " +
		     "VALUES (?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?)",
		     keyColumn);
	    preparedStatements.add(psInsertConfigurationVers);
	    
            psFindConfiguration =
              dbConnector.getConnection().prepareStatement
                    ("SELECT id AS id_config FROM v_configurations WHERE name = ?");
            preparedStatements.add(psFindConfiguration);
	  
	    psInsertConfiguration =
               dbConnector.getConnection().prepareStatement
                    ("INSERT INTO  v_configurations(name) VALUES(?)");
            preparedStatements.add(psInsertConfiguration);

	    psInsertConfigurationLock =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_lockedconfs (parentDirId,config,userName)" +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertConfigurationLock);


	    //Insert Event Content	       
	    psCheckContents =
		dbConnector.getConnection().prepareStatement
		("SELECT id FROM v_EventContents WHERE name= ? ");
	    preparedStatements.add(psCheckContents);

	    psInsertContents =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_EventContents (name)" +
		 "VALUES(?)",keyColumn);
	    preparedStatements.add(psInsertContents);

            psInsertContentIds =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_EventContentids (id_evco)" +
                 "VALUES(?)",keyColumn);
            preparedStatements.add(psInsertContentIds);

	  
	    psInsertContentsConfigAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_conf2evco (id_evCoId,id_confver)" +
		 "VALUES(?,?)");
	    preparedStatements.add(psInsertContentsConfigAssoc);

	    psInsertEventContentStatements =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_evcostatements (classN,moduleL,extraN,processN,statementType) " +
		 "VALUES(?,?,?,?,?)",keyColumn);
	    preparedStatements.add(psInsertEventContentStatements);

	   
	     psInsertStreams =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_streams (name)" +
		 "VALUES(?)",keyColumn);
	    preparedStatements.add(psInsertStreams);

             psInsertStreamsIds =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_streamids (id_stream,streamid,fractodisk)" +
                 "VALUES(?,-1,?)",keyColumn);
            preparedStatements.add(psInsertStreamsIds);


	    
	    psInsertPrimaryDatasets =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_datasets (name)" +
		 "VALUES(?)",keyColumn);
	    preparedStatements.add(psInsertPrimaryDatasets);

	    psInsertECStreamAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_evco2stream (id_evcoid, id_streamid)" +
		 "VALUES(?,?)");
	    preparedStatements.add(psInsertECStreamAssoc);
	   
	    
	    psInsertPathStreamPDAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_pathid2strdst (id_pathid, id_streamId, id_datasetId)" +
		 "VALUES(?,?,?)");
	    preparedStatements.add(psInsertPathStreamPDAssoc);
	    
	  
	    psInsertECStatementAssoc = 
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_evco2stat (statementRank,id_stat,id_evcoid,id_pathId) " +
		 "VALUES(?,?,?,?) ");
	    preparedStatements.add(psInsertECStatementAssoc);
	    

/*	    psInsertStreamDatasetAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_pathid2strdst (id_pathid,id_streamId, id_datasetId)" +
		 "VALUES(?,?,?)");
	    preparedStatements.add(psInsertStreamDatasetAssoc);
*/
	
/*
	    if (dbType.equals(dbTypeMySQL))
		psInsertSuperId = dbConnector.getConnection().prepareStatement
		    ("INSERT INTO SuperIds VALUES()",keyColumn);
	    else if (dbType.equals(dbTypeOracle))
		psInsertSuperId = dbConnector.getConnection().prepareStatement
		    ("INSERT INTO SuperIds VALUES('')",keyColumn);
	    preparedStatements.add(psInsertSuperId);
*/
	 
	    psInsertGlobalPSet =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_conf2gpset " +
		 "(id_confver,id_gpset,ord) " +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertGlobalPSet);
	    
	 

	    psInsertEDSource =
		dbConnector.getConnection().prepareStatement
            ("INSERT INTO v_edsources (id_template) " +
            "VALUES(?)",keyColumn);
	    preparedStatements.add(psInsertEDSource);
	 
	    psInsertConfigEDSourceAssoc =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO " +
   "v_conf2eds (id_confver,id_edsource,ord) " +
   "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertConfigEDSourceAssoc);
	    

	    psInsertESSource =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO " +
   "v_essources (id_template,name) " +
   "VALUES(?, ?)", keyColumn);
	    preparedStatements.add(psInsertESSource);
	    

	    psInsertConfigESSourceAssoc =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO " +
   "v_conf2ess " +
   "(id_confver,id_essource,ord,prefer) " +
   "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertConfigESSourceAssoc);

	    psInsertESModule =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO " +
   "v_esmodules (id_template,name) " +
   "VALUES(?, ?)", keyColumn);
	    preparedStatements.add(psInsertESModule);


	    psInsertConfigESModuleAssoc =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO " +
   "v_conf2esm " +
   "(id_confver,id_esmodule,ord,prefer) " +
   "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertConfigESModuleAssoc);
	    
	
	    
	    psInsertService =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO " +
   "v_services (id_template) " +
   "VALUES(?)", keyColumn);
	    preparedStatements.add(psInsertService);

	    psInsertConfigServiceAssoc =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO " +
   "v_conf2srv (id_confver,id_service,ord) " +
   "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertConfigServiceAssoc);

	
            psCheckPathName = 
		dbConnector.getConnection().prepareStatement
                (" SELECT id from v_paths WHERE name=? ");
	    preparedStatements.add(psCheckPathName);

            psCheckPathNoum = 
		dbConnector.getConnection().prepareStatement
                (" SELECT id from v_noumpaths WHERE name=? ");
	    preparedStatements.add(psCheckPathNoum);

	    psInsertPathNoum =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_noumPaths (name) " +
		 "VALUES(?)",keyColumn);
	    preparedStatements.add(psInsertPathNoum);
	  
	    psInsertPath =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_Paths (name,version,id_noumpath) " +
		 "VALUES(?, ?, ?)",keyColumn);
	    preparedStatements.add(psInsertPath);
	  
	    psInsertPathIds =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_pathids (id_path,pathid,isEndPath,crc32,crc32logic) " +
		 "VALUES(?, -999, ?, ?, ?)",keyColumn);
	    preparedStatements.add(psInsertPathIds);
	  
	    psInsertHPathIds =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO h_pathids (crc32,crc32logic) " +
		 "VALUES(?, ? )",keyColumn);
	    preparedStatements.add(psInsertHPathIds);

	    psInsertHPathId2Path =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO h_pathid2path (id_path,id_pathid,isEndPath) " +
		 "VALUES(?, ?, ?)",keyColumn);
	    preparedStatements.add(psInsertHPathId2Path);
	  
	    psInsertHPathId2Uq =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO h_pathid2uq (id_pathid,id_pathiduq) " +
		 "VALUES(?, ? )",keyColumn);
	    preparedStatements.add(psInsertHPathId2Uq);
	  
	    psSelectPathId2Uq =
		dbConnector.getConnection().prepareStatement
		("SELECT id_pathid FROM h_pathid2uq WHERE id_pathiduq=? ");
	    preparedStatements.add(psSelectPathId2Uq);

            psCheckHPathIdCrc =
               dbConnector.getConnection().prepareStatement
	       ("SELECT id FROM h_pathids WHERE crc32=? ");
	    preparedStatements.add(psCheckHPathIdCrc);

	    psInsertConfigPathAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO " +
		 "v_pathid2conf (id_pathId,id_confver,ord) " +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertConfigPathAssoc);
	 
	    psInsertConfigHPathAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO " +
		 "h_pathid2conf (id_pathId,id_confver,ord) " +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertConfigHPathAssoc);
	 
/*	    psInsertSequence =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO Sequences (name) " +
		 "VALUES(?)",keyColumn);
	    preparedStatements.add(psInsertSequence);
	    
	
	    psInsertConfigSequenceAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO " +
		 "ConfigurationSequenceAssoc (configId,sequenceId,sequenceNb) " +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertConfigSequenceAssoc);
*/

	    psInsertPathElement =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_paelements (paetype,name,crc32,crc32logic,o_id) " +
		 "VALUES(?, ?, ?, -999,-999)",keyColumn);
	    preparedStatements.add(psInsertPathElement);
	    
	    psInsertHPathElement =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO h_paelements (moe_type,templateId,name,crc32,crc32logic) " +
		 "VALUES(?, ?, ?, ?, -999)");
	    preparedStatements.add(psInsertHPathElement);
	    
           psInsertMoElement =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_moelements (moetype,name,o_id,paramtype,tracked,crc32,value,valuelob,hex) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ? )",keyColumn);
            preparedStatements.add(psInsertMoElement);

           psInsertPae2Moe=
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_pae2moe (id_pae,id_moe,lvl,ord) " +
                 "VALUES(?, ?, ?, ?)",keyColumn);
            preparedStatements.add(psInsertPae2Moe);

           psInsertMod2Templ=
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_mod2templ (id_pae,id_templ) " +
                 "VALUES(?, ? )");
            preparedStatements.add(psInsertMod2Templ);

	    psInsertPathElementAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_pathid2pae (id_pathid,id_pae,id_parent,lvl,ord,operator) " +
		 "VALUES(?, ?, ?, ?, ?, ?)");
	    preparedStatements.add(psInsertPathModuleAssoc);

            psInsertPathOutputModuleAssoc =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_pathid2outm (id_pathid,id_streamid,ord,operator) " +
                 "VALUES(?, ?, ?, ?)");
            preparedStatements.add(psInsertPathOutputModuleAssoc);

/*	    psInsertSequenceModuleAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO SequenceModuleAssoc (sequenceId,moduleId,sequenceNb,operator) "+
		 "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertSequenceModuleAssoc);
	    
	     psInsertSequenceOutputModuleAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO SequenceOutputModAssoc (sequenceId,outputModuleId,sequenceNb,operator) "+
		 "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertSequenceOutputModuleAssoc);

	    psInsertPathPathAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO PathInPathAssoc(parentPathId,childPathId,sequenceNb,operator) "+
		 "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertPathPathAssoc);
	    
	    psInsertPathSequenceAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO PathSequenceAssoc (pathId,sequenceId,sequenceNb,operator) " +
		 "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertPathSequenceAssoc);
	    
	    psInsertSequenceSequenceAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO SequenceInSequenceAssoc"+
		 "(parentSequenceId,childSequenceId,sequenceNb,operator) "+
		 "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertSequenceSequenceAssoc);
	    
	    psInsertPathModuleAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO PathModuleAssoc (pathId,moduleId,sequenceNb,operator) " +
		 "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertPathModuleAssoc);
	    
	    psInsertPathOutputModuleAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO PathOutputModAssoc (pathId,outputModuleId,sequenceNb,operator) " +
		 "VALUES(?, ?, ?, ?)");
	    preparedStatements.add(psInsertPathOutputModuleAssoc);

	    psInsertSuperIdReleaseAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO SuperIdReleaseAssoc (superId,releaseId) " +
		 "VALUES(?, ?)");
	    preparedStatements.add(psInsertSuperIdReleaseAssoc);
*/
	    
	    psInsertServiceTemplate =
		dbConnector.getConnection().prepareStatement
 ("INSERT INTO v_srvtemplates (id_pkg,name,cvstag) " +
   "VALUES (?, ?, ?)", keyColumn);
	    preparedStatements.add(psInsertServiceTemplate);
	    
	    psInsertEDSourceTemplate =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_edstemplates (id_pkg,name,cvstag) " +
   "VALUES (?, ?, ?)", keyColumn);
	    preparedStatements.add(psInsertEDSourceTemplate);
	    
	    psInsertESSourceTemplate =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_esstemplates (pkg_id,name,cvstag) " +
   "VALUES (?, ?, ?)", keyColumn);
	    preparedStatements.add(psInsertESSourceTemplate);
	    
	    psInsertESModuleTemplate =
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_esmtemplates (id_pkg,name,cvstag) " +
   "VALUES (?, ?, ?, ?)", keyColumn);
	    preparedStatements.add(psInsertESModuleTemplate);
	    
/*	    psInsertModuleTemplate =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO ModuleTemplates (superId,typeId,name,cvstag) " +
		 "VALUES (?, ?, ?, ?)");
	    preparedStatements.add(psInsertModuleTemplate);
*/	    
	    psInsertParameterSet =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO ParameterSets(superId,name,tracked) " +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertParameterSet);

	    psInsertVecParameterSet =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO VecParameterSets(superId,name,tracked) " +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertVecParameterSet);

	    psInsertGPset =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO v_globalpsets (name,tracked) " +
		 "VALUES(?, ?)",keyColumn);
	    preparedStatements.add(psInsertGPset);

	   psInsertParameterGPset =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_gpsetelements (id_gpset,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?,-1, ?)",keyColumn);
            preparedStatements.add(psInsertParameterGPset);

           psInsertParameterEDS =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_edselements (id_edsource,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, -1, ?)",keyColumn);
            preparedStatements.add(psInsertParameterEDS);

           psInsertParameterESM =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_esmelements (id_esmodule,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, -1, ?)",keyColumn);
            preparedStatements.add(psInsertParameterESM);

           psInsertParameterESS =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_esselements (id_essource,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, -1, ?)",keyColumn);
            preparedStatements.add(psInsertParameterESS);

           psInsertParameterSRV =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_srvelements (id_service,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, -1, ?)",keyColumn);
            preparedStatements.add(psInsertParameterSRV);

           psInsertParameterOUTM =
                dbConnector.getConnection().prepareStatement
                ("INSERT INTO v_outmelements (id_streamid,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, -1, ?)",keyColumn);
            preparedStatements.add(psInsertParameterOUTM);

	    psInsertSuperIdParamSetAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO SuperIdParamSetAssoc (superId,psetId,sequenceNb) "+
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertSuperIdParamSetAssoc);
	    
	    psInsertSuperIdVecParamSetAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO " +
		 "SuperIdVecParamSetAssoc (superId,vpsetId,sequenceNb) " +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertSuperIdVecParamSetAssoc);
/*	    
	    psInsertSuperIdParamAssoc =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO SuperIdParameterAssoc (superId,paramId,sequenceNb) " +
		 "VALUES(?, ?, ?)");
	    preparedStatements.add(psInsertSuperIdParamAssoc);
	    
	    psInsertBoolParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO BoolParamValues (paramId,value) " +
		 "VALUES (?, ?)");
	    preparedStatements.add(psInsertBoolParamValue);

	    psInsertInt32ParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO Int32ParamValues (paramId,value,hex) " +
		 "VALUES (?, ?, ?)");
	    preparedStatements.add(psInsertInt32ParamValue);

	    psInsertUInt32ParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO UInt32ParamValues (paramId,value,hex) " +
		 "VALUES (?, ?, ?)");
	    preparedStatements.add(psInsertUInt32ParamValue);

	    psInsertInt64ParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO Int64ParamValues (paramId,value,hex) " +
		 "VALUES (?, ?, ?)");
	    preparedStatements.add(psInsertInt64ParamValue);

	    psInsertUInt64ParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO UInt64ParamValues (paramId,value,hex) " +
		 "VALUES (?, ?, ?)");
	    preparedStatements.add(psInsertUInt64ParamValue);

	    psInsertDoubleParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO DoubleParamValues (paramId,value) " +
		 "VALUES (?, ?)");
	    preparedStatements.add(psInsertDoubleParamValue);

	    psInsertStringParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO StringParamValues (paramId,value) " +
		 "VALUES (?, ?)");
	    preparedStatements.add(psInsertStringParamValue);

	    psInsertEventIDParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO EventIDParamValues (paramId,value) " +
		 "VALUES (?, ?)");
	    preparedStatements.add(psInsertEventIDParamValue);

	    psInsertInputTagParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO InputTagParamValues (paramId,value) " +
		 "VALUES (?, ?)");
	    preparedStatements.add(psInsertInputTagParamValue);

	    psInsertFileInPathParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO FileInPathParamValues (paramId,value) " +
		 "VALUES (?, ?)");
	    preparedStatements.add(psInsertFileInPathParamValue);

	    psInsertVInt32ParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO VInt32ParamValues "+
		 "(paramId,sequenceNb,value,hex) "+
		 "VALUES (?, ?, ?, ?)");
	    preparedStatements.add(psInsertVInt32ParamValue);

	    psInsertVUInt32ParamValue
		= dbConnector.getConnection().prepareStatement
		("INSERT INTO VUInt32ParamValues "+
		 "(paramId,sequenceNb,value,hex) " +
		 "VALUES (?, ?, ?, ?)");
	    preparedStatements.add(psInsertVUInt32ParamValue);

	    psInsertVInt64ParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO VInt64ParamValues "+
		 "(paramId,sequenceNb,value,hex) "+
		 "VALUES (?, ?, ?, ?)");
	    preparedStatements.add(psInsertVInt64ParamValue);

	    psInsertVUInt64ParamValue
		= dbConnector.getConnection().prepareStatement
		("INSERT INTO VUInt64ParamValues "+
		 "(paramId,sequenceNb,value,hex) " +
		 "VALUES (?, ?, ?, ?)");
	    preparedStatements.add(psInsertVUInt64ParamValue);

	    psInsertVDoubleParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO VDoubleParamValues (paramId,sequenceNb,value) " +
		 "VALUES (?, ?, ?)");
	    preparedStatements.add(psInsertVDoubleParamValue);

	    psInsertVStringParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO VStringParamValues (paramId,sequenceNb,value) " +
		 "VALUES (?, ?, ?)");
	    preparedStatements.add(psInsertVStringParamValue);

	    psInsertVEventIDParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO VEventIDParamValues (paramId,sequenceNb,value) " +
		 "VALUES (?, ?, ?)");
	    preparedStatements.add(psInsertVEventIDParamValue);

	    psInsertVInputTagParamValue =
		dbConnector.getConnection().prepareStatement
		("INSERT INTO VInputTagParamValues (paramId,sequenceNb,value) " +
		 "VALUES (?, ?, ?)");
	    preparedStatements.add(psInsertVInputTagParamValue);
*/
	    
	    //
	    // DELETE
	    //
	    

	    psDeleteDirectory =
		dbConnector.getConnection().prepareStatement
("DELETE FROM v_directories WHERE id=?");
	    preparedStatements.add(psDeleteDirectory);

	    psDeleteLock =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM v_lockedconfs " +
		 "WHERE id_parentdir=? AND id_config=?");
            preparedStatements.add(psDeleteLock);

	    psDeleteConfiguration =
		dbConnector.getConnection().prepareStatement
  ("DELETE FROM v_confversions " +
   "WHERE id = ?");
	    preparedStatements.add(psDeleteConfiguration);
	    
	    psDeleteSoftwareRelease =
		dbConnector.getConnection().prepareStatement
  ("DELETE FROM v_softreleases " +
   "WHERE id = ?");
	    preparedStatements.add(psDeleteSoftwareRelease);
	    
/*
	    psDeletePSetsFromConfig =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM ConfigurationParamSetAssoc "+
		 "WHERE configId=?");
	    preparedStatements.add(psDeletePSetsFromConfig);
	    
	    psDeleteContentFromConfig = 
	    	dbConnector.getConnection().prepareStatement(
	    			"DELETE FROM	ConfigurationContentAssoc	"	+
	    			"WHERE configId = ?"							);
	    preparedStatements.add(psDeleteContentFromConfig);
*/
	    
	    psDeleteEDSourcesFromConfig =
		dbConnector.getConnection().prepareStatement
  ("DELETE FROM v_conf2eds "+
   "WHERE id_confver=?");
	    preparedStatements.add(psDeleteEDSourcesFromConfig);
	    
	    psDeleteESSourcesFromConfig =
		dbConnector.getConnection().prepareStatement
  ("DELETE FROM v_conf2ess "+
   "WHERE id_confver=?");
	    preparedStatements.add(psDeleteESSourcesFromConfig);
	    
	    psDeleteESModulesFromConfig =
		dbConnector.getConnection().prepareStatement
  ("DELETE FROM v_conf2esm "+
   "WHERE id_confver=?");
	    preparedStatements.add(psDeleteESModulesFromConfig);
	    
	    psDeleteServicesFromConfig =
		dbConnector.getConnection().prepareStatement
  ("DELETE FROM v_conf2srv "+
   "WHERE id_confver=?");
	    preparedStatements.add(psDeleteServicesFromConfig);
	    
/*	    psDeleteSequencesFromConfig =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM ConfigurationSequenceAssoc "+
		 "WHERE configId=?");
	    preparedStatements.add(psDeleteSequencesFromConfig);
	    
	    psDeletePathsFromConfig =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM ConfigurationPathAssoc "+
		 "WHERE configId=?");
	    preparedStatements.add(psDeletePathsFromConfig);
*/

	    /* 28/09/2009
	       psDeleteStreamsAndDatasetsFromConfig =
	       dbConnector.getConnection().prepareStatement
	       ("DELETE FROM ConfigurationStreamAssoc "+
	       "WHERE configId=?");
	       preparedStatements.add(psDeleteStreamsAndDatasetsFromConfig);
	    */
	    
/*
	    psDeleteChildSeqsFromParentSeq =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM SequenceInSequenceAssoc "+
		 "WHERE parentSequenceId=?");
	    preparedStatements.add(psDeleteChildSeqsFromParentSeq);

	    psDeleteChildSeqFromParentSeqs =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM SequenceInSequenceAssoc "+
		 "WHERE childSequenceId=?");
	    preparedStatements.add(psDeleteChildSeqFromParentSeqs);

	    psDeleteChildSeqsFromParentPath =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM PathSequenceAssoc "+
		 "WHERE pathId=?");
	    preparedStatements.add(psDeleteChildSeqsFromParentPath);

	    psDeleteChildSeqFromParentPaths =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM PathSequenceAssoc "+
		 "WHERE sequenceId=?");
	    preparedStatements.add(psDeleteChildSeqFromParentPaths);
	    
	    psDeletePathStreamDataSetAssoc = 
	    	dbConnector.getConnection().prepareStatement(
	    			"DELETE FROM	PathStreamDataSetAssoc "+
	    			"WHERE			pathId = ?"				);
	    preparedStatements.add(psDeletePathStreamDataSetAssoc);
	    
	    psDeletePathOutputModAssoc =
	    	dbConnector.getConnection().prepareStatement(
	    			"DELETE FROM 	PathOutputModAssoc "	+
	    			"WHERE			pathId = ?"				);
	    preparedStatements.add(psDeletePathOutputModAssoc);
	    
	    psDeleteECStreamFromEventCont =
	    	dbConnector.getConnection().prepareStatement(
	    			"DELETE FROM	ECStreamAssoc "			+
	    			"WHERE			eventContentId = ?"		);
	    preparedStatements.add(psDeleteECStreamFromEventCont);
	    
	    psDeleteECStatementFromEventCont = 
	    	dbConnector.getConnection().prepareStatement(
	    			"DELETE FROM	ECStatementAssoc "		+
	    			"WHERE			eventContentId = ?"		);
	    
	    psDeleteEventContentStatement = 
	    	dbConnector.getConnection().prepareStatement(
	    			"DELETE FROM 	eventcontentstatements "+
	    			"WHERE			statementid = ?"		);

	    psDeleteChildPathsFromParentPath =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM PathInPathAssoc "+
		 "WHERE parentPathId=?");
	    preparedStatements.add(psDeleteChildPathsFromParentPath);
	    
	    psDeleteChildPathFromParentPaths =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM PathInPathAssoc "+
		 "WHERE childPathId=?");
	    preparedStatements.add(psDeleteChildPathFromParentPaths);

	    psDeleteModulesFromSeq =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM SequenceModuleAssoc "+
		 "WHERE sequenceId=?");
	    preparedStatements.add(psDeleteModulesFromSeq);

	    psDeleteModulesFromPath =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM PathModuleAssoc "+
		 "WHERE pathId=?");
	    preparedStatements.add(psDeleteModulesFromPath);

	    psDeleteTemplateFromRelease =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM SuperIdReleaseAssoc "+
		 "WHERE superId=? AND releaseId=?");
	    preparedStatements.add(psDeleteTemplateFromRelease);
	    
	    psDeleteParametersForSuperId =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM SuperIdParameterAssoc "+
		 "WHERE superId=?");
	    preparedStatements.add(psDeleteParametersForSuperId);
	    
	    psDeletePSetsForSuperId =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM SuperIdParamSetAssoc "+
		 "WHERE superId=?");
	    preparedStatements.add(psDeletePSetsForSuperId);
	    
	    psDeleteVPSetsForSuperId =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM SuperIdVecParamSetAssoc "+
		 "WHERE superId=?");
	    preparedStatements.add(psDeleteVPSetsForSuperId);
	    
	    psDeleteSuperId =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM SuperIds WHERE superId=?");
	    preparedStatements.add(psDeleteSuperId);	    
	    
	    psDeleteParameter =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM Parameters WHERE paramId = ?");
	    preparedStatements.add(psDeleteParameter);

	    psDeletePSet =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM ParameterSets WHERE superId = ?");
	    preparedStatements.add(psDeletePSet);

	    psDeleteVPSet =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM VecParameterSets WHERE superId = ?");
	    preparedStatements.add(psDeleteVPSet);

	    psDeleteSequence =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM Sequences WHERE sequenceId = ?");
	    preparedStatements.add(psDeleteSequence);

	    psDeletePath =
		dbConnector.getConnection().prepareStatement
		("DELETE FROM Paths WHERE pathId = ?");
	    preparedStatements.add(psDeletePath);
	    
*/
	    
	    //
	    // STORED PROCEDURES
	    //

	    // MySQL
	    if (dbType.equals(dbTypeMySQL)) {

		csLoadTemplate =
		    dbConnector.getConnection().prepareCall
		    ("{ CALL load_template(?,?) }");
		preparedStatements.add(csLoadTemplate);
		
		csLoadTemplates =
		    dbConnector.getConnection().prepareCall
		    ("{ CALL load_templates(?) }");
		preparedStatements.add(csLoadTemplates);
		
		csLoadTemplatesForConfig =
		    dbConnector.getConnection().prepareCall
		    ("{ CALL load_templates_for_config(?) }");
		preparedStatements.add(csLoadTemplatesForConfig);
		
		csLoadConfiguration =
		    dbConnector.getConnection().prepareCall
		    ("{ CALL load_configuration(?) }");
		preparedStatements.add(csLoadConfiguration);
		
	    }
	    // Oracle
	    else {
		csLoadTemplate =
		    dbConnector.getConnection().prepareCall
		    ("begin load_template(?,?); end;");
		preparedStatements.add(csLoadTemplate);
		
		csLoadTemplates =
		    dbConnector.getConnection().prepareCall
		    ("begin load_templates(?); end;");
		preparedStatements.add(csLoadTemplates);
		
		csLoadTemplatesForConfig =
		    dbConnector.getConnection().prepareCall
		    ("begin load_templates_for_config(?); end;");
		preparedStatements.add(csLoadTemplatesForConfig);
		
		csLoadConfiguration =
		    dbConnector.getConnection().prepareCall
		    ("begin load_configuration(?); end;");
		preparedStatements.add(csLoadConfiguration);

	    }
	    

	    //
	    // SELECT FOR TEMPORARY TABLES
	    //
	    psSelectTemplates =
		dbConnector.getConnection().prepareStatement
		("SELECT" +
		 " template_id," +
		 " template_type," +
		 " template_name," +
		 " template_cvstag," +
		 " template_pkgid " +
		 "FROM tmp_template_table "+
                 " ORDER by template_id");
	    psSelectTemplates.setFetchSize(1024);
	    preparedStatements.add(psSelectTemplates);
	    
	    psSelectInstances =
		dbConnector.getConnection().prepareStatement
		("SELECT DISTINCT" +
		 " instance_id," +
		 " template_id," +
		 " instance_type," +
		 " instance_name," +
		 " flag," +
		 " sequence_nb " +
		 "FROM tmp_instance_table " +
		 "ORDER BY instance_type,sequence_nb");
	    psSelectInstances.setFetchSize(1024);
	    preparedStatements.add(psSelectInstances);
	    
	    psSelectParametersTemplates =
		dbConnector.getConnection().prepareStatement
		("SELECT " +
		 " parameter_id," +
		 " parameter_type," +
		 " parameter_name," +
		 " parameter_trkd," +
		 " parameter_seqnb," +
		 " parent_id," +
		 " lvl," +
		 " value," +
		 " valuelob, " +
                 " hex, " +
		 " stamp " +
		 " FROM tmp_parameter_table order by stamp");  
	    psSelectParametersTemplates.setFetchSize(4096);
	    preparedStatements.add(psSelectParametersTemplates);

           psSelectParameters =
                dbConnector.getConnection().prepareStatement
                ("Select * from (Select * from (SELECT a.id+1000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_edsource+1000000, a.lvl,  a.value,  a.valuelob, a.hex  from V_EDSELEMENTS a, V_CONF2EDS c " +
		" where c.ID_CONFVER=? and c.ID_EDSOURCE=a.ID_edsource order by id ) " +
		" UNION ALL " +
		"Select * from (SELECT a.id+2000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_essource+2000000, a.lvl,  a.value,  a.valuelob , a.hex from V_ESSELEMENTS a, V_CONF2ESS c " +
		" where c.ID_CONFVER=? and c.ID_ESSOURCE=a.ID_essource order by id ) " +
		" UNION ALL " + 
//		"Select * from (SELECT a.id, a.paramtype, a.name, a.tracked, a.ord,a.id_pae, a.lvl,  a.value,  a.valuelob from V_MOELEMENTS a, V_PAELEMENTS b, V_PATHID2CONF c " +
//		" where c.ID_CONFVER=? and c.ID_PATHID=b.ID_PATHID and a.id_pae=b.id order by a.id ) " +
//		" UNION ALL " + 
                /*h_" select id,paramtype,name,tracked,ord,id_pae,lvl,value,valuelob,hex from (select sa.*, h_moelements.valuelob,h_moelements.hex from (select  distinct h_moelements.id, h_moelements.paramtype, h_moelements.name, h_moelements.tracked, h_pae2moe.ord,h_pastruct.id_pae,  h_pae2moe.lvl as lvl,  h_moelements.value,h_pae2moe.id as pae2id  from h_moelements, h_pae2moe, h_pastruct  where h_moelements.id = h_pae2moe.id_moe AND h_pastruct.id_pae = h_pae2moe.id_pae AND  h_pastruct.id  IN (SELECT h_pastruct.id FROM h_pastruct,h_pathid2conf WHERE h_pathid2conf.id_pathid=h_pastruct.id_pathid and h_pathid2conf.id_confver=?) order by h_pae2moe.id) sa, h_moelements where sa.id=h_moelements.id) " + */
                " select id,paramtype,name,tracked,ord,id_pae,lvl,value,valuelob,hex from (select sa.*, v_moelements.valuelob,v_moelements.hex from (select  distinct v_moelements.id, v_moelements.paramtype, v_moelements.name, v_moelements.tracked, v_pae2moe.ord,v_pathid2pae.id_pae,  v_pae2moe.lvl as lvl,  v_moelements.value,v_pae2moe.id as pae2id  from v_moelements, v_pae2moe, v_pathid2pae  where v_moelements.id = v_pae2moe.id_moe AND v_pathid2pae.id_pae = v_pae2moe.id_pae AND  v_pathid2pae.id  IN (SELECT v_pathid2pae.id FROM v_pathid2pae,v_pathid2conf WHERE v_pathid2conf.id_pathid=v_pathid2pae.id_pathid and v_pathid2conf.id_confver=?) order by v_pae2moe.id) sa, v_moelements where sa.id=v_moelements.id) " +
		" UNION ALL " + 
		"Select * from (SELECT a.id+4000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_service+4000000, a.lvl,  a.value,  a.valuelob, a.hex from V_SRVELEMENTS a, V_CONF2SRV c " +
		" where c.ID_CONFVER=? and c.ID_SERVICE=a.ID_Service order by id )" +
		" UNION ALL " + 
		"Select * from (SELECT a.id+3000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_esmodule+3000000, a.lvl,  a.value,  a.valuelob, a.hex from V_ESMELEMENTS a, V_CONF2ESM c " +
		" where c.ID_CONFVER=? and c.ID_esmodule=a.ID_esmodule order by id ) " +
		" UNION ALL " + 
		"Select * from (SELECT a.id+6000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_gpset+6000000, a.lvl,  a.value,  a.valuelob, a.hex from V_GPSETELEMENTS a, V_CONF2GPSET c " +
		" where c.ID_CONFVER=? and c.ID_gpset=a.ID_gpset order by id ) " +
		" UNION ALL " + 
                " select * from (SELECT a.id+5000000 as id, a.paramtype, a.name, a.tracked, a.ord,v_streamids.id_stream+5000000, a.lvl,  a.value,  a.valuelob, a.hex from v_outmelements a,v_pathid2conf,v_pathid2outm,v_streamids where a.id_streamid=v_streamids.id  AND v_streamids.id=v_pathid2outm.id_streamid and v_pathid2outm.id_pathid=v_pathid2conf.id_pathid AND v_pathid2conf.id_confver = ? order by id) )");
	    psSelectParameters.setFetchSize(8192);
	    preparedStatements.add(psSelectParameters);
	    
	    psSelectBooleanValues =
		dbConnector.getConnection().prepareStatement
		("SELECT DISTINCT"+
		 " parameter_id," +
		 " parameter_value " +
		 "FROM tmp_boolean_table");
	    psSelectBooleanValues.setFetchSize(2048);
	    preparedStatements.add(psSelectBooleanValues);
	    
	    psSelectIntValues =
		dbConnector.getConnection().prepareStatement
		("SELECT DISTINCT"+
		 " parameter_id," +
		 " parameter_value," +
		 " sequence_nb," +
		 " hex " +
		 "FROM tmp_int_table " +
		 "ORDER BY sequence_nb ASC");
	    psSelectIntValues.setFetchSize(2048);
	    preparedStatements.add(psSelectIntValues);
	    
	    psSelectRealValues =
		dbConnector.getConnection().prepareStatement
		("SELECT DISTINCT"+
		 " parameter_id," +
		 " parameter_value," +
		 " sequence_nb " +
		 "FROM tmp_real_table " +
		 "ORDER BY sequence_nb");
	    psSelectRealValues.setFetchSize(2048);
	    preparedStatements.add(psSelectRealValues);
	    
	    
	    psSelectStringValues =
		dbConnector.getConnection().prepareStatement
		("SELECT DISTINCT"+
		 " parameter_id," +
		 " parameter_value," +
		 " sequence_nb " +
		 "FROM tmp_string_table " +
		 "ORDER BY sequence_nb ASC");
	    psSelectStringValues.setFetchSize(2048);
	    preparedStatements.add(psSelectStringValues);
	    
	    /** Query new CLOB fields in temp_string_table.
	     *  NOTE: the rowid JOIN is to simulate the
	     *  DISTINCT modifier in the original query,
	     *  which might be a bad design.
	     *  I have managed to patch it up and group
	     *  the results as the DISTINCT does since
	     *  it is not possible to apply DISTINCT
	     *  and/or GROUPBY in CLOB queries.  
	    psSelectCLOBsValues =
			dbConnector.getConnection().prepareStatement
			("SELECT 						" + 
			 "   clobs.parameter_id    ,	" +
			 "   clobs.parameter_value ,	" +
			 "   clobs.sequence_nb 			" +
			 "FROM							" +
			 "   (							" +
			 "   SELECT  Max(rowid) idrow , " +
		     "   parameter_id      ,		" +
		     "   sequence_nb				" +
		     "   FROM tmp_string_table		" +
		     "   GROUP BY parameter_id ,	" +
			 "   sequence_nb				" +
			 "   ) grouped					" +
		     "JOIN							" +
			 "   tmp_string_table clobs 	" +
			 "ON (clobs.rowid = grouped.idrow)");
			 */
	    psSelectCLOBsValues =
			dbConnector.getConnection().prepareStatement
			("SELECT 						" + 
			 "   parameter_id    ,	" +
			 "   parameter_value ,	" +
			 "   sequence_nb 		" +
			 "FROM					" +
			 "   tmp_clob_table 	" );
	    psSelectCLOBsValues.setFetchSize(2048);
		preparedStatements.add(psSelectCLOBsValues);
		
		
	    
/*	    psSelectPathEntries =
		dbConnector.getConnection().prepareStatement
 ("SELECT" +
   " c.pathid AS path_id," +
   " a.id AS entry_id," +
   " a.ord AS sequence_nb," +
   " DECODE(a.paetype, 1, 'Module', 2, 'Sequence', 3, 'OutputModule', 'Undefined') AS entry_type, " +
   " a.operator " +
   "FROM v_paelements a, v_pathid2conf b, v_pathids c "+
        "WHERE a.id_pathid = b.id_pathid AND c.id=a.id_pathid AND " +
        "b.id_confver = ? AND a.lvl=0 " +
   "ORDER BY a.id_pathid ASC, a.id ASC");
	    psSelectPathEntries.setFetchSize(1024);
	    preparedStatements.add(psSelectPathEntries);
*/
	    
	    psSelectPathEntries =
		dbConnector.getConnection().prepareStatement
            /*h_    ("Select * from (SELECT h_pastruct.id_pathid, h_paelements.id, h_pastruct.ord, DECODE(h_paelements.paetype,1, 'Module', 2, 'Sequence', 3, 'OutputModule', 'Undefined') AS entry_type, h_pastruct.operator FROM h_pastruct,h_paelements, h_pathid2conf WHERE h_pathid2conf.id_pathid=h_pastruct.id_pathid and h_pastruct.id_pae=h_paelements.id and h_pastruct.lvl=0 and h_pathid2conf.id_confver = ? order by h_pastruct.id_pathid,h_pastruct.id) " +
                " UNION ALL " +
                " select * from (select h_pathid2uq.id_pathiduq, v_streams.id+5000000 as stid,V_PATHID2OUTM.ord,'OutputModule', V_PATHID2OUTM.operator from V_PATHID2OUTM,v_streams,v_streamids,v_PATHID2CONF,h_pathid2uq where v_streams.id=v_streamids.id_stream and v_streamids.id=V_PATHID2OUTM.id_streamid and v_PATHID2CONF.id_confver=? and  v_PATHID2CONF.id_pathid= V_PATHID2OUTM.id_pathid and h_pathid2uq.id_pathid=v_PATHID2CONF.id_pathid)");
	    psSelectPathEntries.setFetchSize(1024); */
                ("Select * from (SELECT v_pathid2conf.id_pathid, v_paelements.id, v_pathid2pae.ord, DECODE(v_paelements.paetype,1, 'Module', 2, 'Sequence', 3, 'OutputModule', 'Undefined') AS entry_type, v_pathid2pae.operator FROM v_pathid2pae,v_paelements, v_pathid2conf WHERE v_pathid2conf.id_pathid=v_pathid2pae.id_pathid and v_pathid2pae.id_pae=v_paelements.id and v_pathid2pae.lvl=0 and v_pathid2conf.id_confver = ? order by v_pathid2pae.id_pathid,v_pathid2pae.id) " +
               " UNION ALL " +
               " select * from (select v_PATHID2CONF.id_pathid, v_streams.id+5000000 as stid,V_PATHID2OUTM.ord,'OutputModule', V_PATHID2OUTM.operator from V_PATHID2OUTM,v_streams,v_streamids,v_PATHID2CONF where v_streams.id=v_streamids.id_stream and v_streamids.id=V_PATHID2OUTM.id_streamid and v_PATHID2CONF.id_confver=? and  v_PATHID2CONF.id_pathid= V_PATHID2OUTM.id_pathid )");
	    preparedStatements.add(psSelectPathEntries);
		/*("SELECT e.id AS sequence_id,  d.id AS entry_id, a.ord AS sequence_nb, DECODE(a.paetype, 1, 'Module', 2, 'Sequence', 3, 'OutputModule', 'Undefined') AS entry_type, a.operator, a.crc32 FROM v_paelements a, v_pathid2conf b, v_pathids c,(select min(aa.id)as id, aa.crc32 from v_paelements aa,v_pathid2conf bb,v_pathids cc where  aa.id_pathid = bb.id_pathid AND cc.id=aa.id_pathid AND bb.id_confver =2061 group by aa.crc32,aa.paetype) d , (select min(aa.id)as id, aa.crc32 from v_paelements aa,v_pathid2conf bb,v_pathids cc where  aa.id_pathid = bb.id_pathid AND cc.id=aa.id_pathid AND bb.id_confver =2061 group by aa.crc32,aa.paetype) e WHERE a.id_pathid = b.id_pathid AND c.id=a.id_pathid AND b.id_confver =? AND a.lvl>0 and a.crc32=d.crc32 AND e.crc32 in (select crc32 from v_paelements where id=a.id_parent) ORDER BY a.id_pathid ASC, a.id ASC");
*/
	    psSelectSequenceEntries =
		dbConnector.getConnection().prepareStatement
           /*h_  ("SELECT h_pastruct.id_pathid,h_pastruct.id as srid,h_pastruct.lvl, h_paelements.id, h_pastruct.ord, DECODE(h_paelements.paetype,1, 'Module', 2, 'Sequence', 3, 'OutputModule', 'Undefined') AS entry_type, h_pastruct.operator FROM h_pastruct,h_paelements, h_pathid2conf WHERE h_pathid2conf.id_pathid=h_pastruct.id_pathid and h_pastruct.id_pae=h_paelements.id and ((h_pastruct.lvl=0 and h_paelements.paetype=2) or h_pastruct.lvl>0) and h_pathid2conf.id_confver = ? order by h_pastruct.id_pathid, srid");*/
                 ("SELECT v_pathid2pae.id_pathid,v_pathid2pae.id as srid,v_pathid2pae.lvl, v_paelements.id, v_pathid2pae.ord, DECODE(v_paelements.paetype,1, 'Module', 2, 'Sequence', 3, 'OutputModule', 'Undefined') AS entry_type, v_pathid2pae.operator FROM v_pathid2pae,v_paelements, v_pathid2conf WHERE v_pathid2conf.id_pathid=v_pathid2pae.id_pathid and v_pathid2pae.id_pae=v_paelements.id and ((v_pathid2pae.lvl=0 and v_paelements.paetype=2) or v_pathid2pae.lvl>0) and v_pathid2conf.id_confver = ? order by v_pathid2pae.id_pathid, srid");

	    psSelectSequenceEntries.setFetchSize(1024);
	    preparedStatements.add(psSelectSequenceEntries);

	    psPrepareSequenceEntries =
		dbConnector.getConnection().prepareStatement
 	    ("INSERT INTO TMP_SEQUENCE_ENTRIES "+
             "VALUES (?,?,?,?,?)");
	    preparedStatements.add(psPrepareSequenceEntries);



	    // bug #91797 ConfDB operator IGNORE/NEGATE also for modules in a sequence
	    psSelectSequenceEntriesAndOperator =
			dbConnector.getConnection().prepareStatement
            ("select distinct * from tmp_sequence_entries");
	    psSelectSequenceEntriesAndOperator.setFetchSize(1024);
		    preparedStatements.add(psSelectSequenceEntriesAndOperator);
		

	    //Insert a new relesase
	    psInsertReleaseTag = 
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_softreleases " +
    " (releaseTag)  VALUES (?)",keyColumn);
	     psInsertReleaseTag .setFetchSize(1024);
	    preparedStatements.add( psInsertReleaseTag );

	    psSelectSoftwareSubsystemId = 	
		dbConnector.getConnection().prepareStatement
  ("SELECT id " +
   "FROM v_softsubsystems "+
   "WHERE name = ?");
	    psSelectSoftwareSubsystemId.setFetchSize(1024);
	    preparedStatements.add(psSelectSoftwareSubsystemId);

	    psInsertSoftwareSubsystem = 	
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_softsubsystems "+
   " (name) VALUES (?)",keyColumn);
	    preparedStatements.add(psInsertSoftwareSubsystem);
	    
	    psSelectSoftwarePackageId = 	
		dbConnector.getConnection().prepareStatement
  ("SELECT id " +
   "FROM v_softpackages "+
   "WHERE id_subs = ? AND name = ? ");
	    psSelectSoftwarePackageId.setFetchSize(1024);
	    preparedStatements.add(psSelectSoftwarePackageId);
	    
	    psInsertSoftwarePackage = 	
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_softpackages "+
   " (id_subs, name) VALUES (?,?)",keyColumn);
	    preparedStatements.add(psInsertSoftwarePackage);

	    psInsertEDSourceTemplateRelease = 
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_edstemplates "+
   " (id_pkg, name, CVSTAG) VALUES (?,?,?)",keyColumn);
	    preparedStatements.add(psInsertEDSourceTemplateRelease);

	    psInsertESSourceTemplateRelease = 
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_esstemplates "+
   " (id_pkg, name, CVSTAG,) VALUES (?,?,?)",keyColumn);
	    preparedStatements.add(psInsertESSourceTemplateRelease);

	    
	    psInsertESModuleTemplateRelease = 
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_esmtemplates "+
   " (ik_pkg, name, CVSTAG) VALUES (?,?,?)",keyColumn);
	    preparedStatements.add(psInsertESModuleTemplateRelease);

	    psInsertServiceTemplateRelease = 
		dbConnector.getConnection().prepareStatement
  ("INSERT INTO v_srvtemplates "+
   " (id_pkg, name, CVSTAG) VALUES (?,?,?)",keyColumn);
	    preparedStatements.add(psInsertServiceTemplateRelease);
/*	    
	    psInsertModuleTemplateRelease = 
		dbConnector.getConnection().prepareStatement
		("INSERT INTO ModuleTemplates "+
		 " (superId, typeId, name, CVSTAG,packageId) VALUES (?,?,?,?,?)");
	    preparedStatements.add(psInsertModuleTemplateRelease);
	    
	    // SQL statements for new path fields:
	    psCheckPathFieldsExistence = 
			dbConnector.getConnection().prepareStatement
			("Select 1 from dual where exists (			" + 
			  "select 1 from all_tab_columns			" +
			  "where table_name = 'PATHS'				" + 
			  "and column_name = 'DESCRIPTION')			");
		    preparedStatements.add(psCheckPathFieldsExistence);
		    
		psSelectPathExtraFields = 
			dbConnector.getConnection().prepareStatement(
			"SELECT p.pathid		,	" +
			"		p.description	, 	" +
			"		p.contact			" +
			"FROM	Paths	p			" +
			"WHERE	p.pathid = ?		");
		    preparedStatements.add(psSelectPathExtraFields);		  
		    
		psInsertPathDescription =
			dbConnector.getConnection().prepareStatement
			("INSERT INTO Paths (name,isEndPath, description, contact) " +
			 "VALUES(?, ?, ?, ?)",keyColumn);
		    preparedStatements.add(psInsertPathDescription);
		    
	    // bug #91797: ConfDB operator IGNORE/NEGATE also for modules in a sequence.
	    psCheckOperatorForModuleSequenceAssoc	= 
			dbConnector.getConnection().prepareStatement
			("select 1 from dual where exists ( 		" + 
			 " select 1 from all_tab_columns 			" + 
			 " where table_name = 'TMP_SEQUENCE_ENTRIES'" +
			 "   and column_name = 'OPERATOR')			");
		    preparedStatements.add(psCheckPathFieldsExistence);
*/
	}
	catch (SQLException e) {
	    String errMsg = "ConfDB::prepareStatements() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	
	// create hash maps
	moduleTypeIdHashMap      = new HashMap<String,Integer>();
	paramTypeIdHashMap       = new HashMap<String,Integer>();
	isVectorParamHashMap     = new HashMap<Integer,Boolean>();
	insertParameterHashMap   = new HashMap<String,PreparedStatement>();
        dbidParamHashMap	 = new HashMap<Integer,Integer>();
        dbidTemplateHashMap	 = new HashMap<Integer,Integer>();
        
	
	insertParameterHashMap.put("bool",      psInsertBoolParamValue);
	insertParameterHashMap.put("int32",     psInsertInt32ParamValue);
	insertParameterHashMap.put("vint32",    psInsertVInt32ParamValue);
	insertParameterHashMap.put("uint32",    psInsertUInt32ParamValue);
	insertParameterHashMap.put("vuint32",   psInsertVUInt32ParamValue);
	insertParameterHashMap.put("int64",     psInsertInt64ParamValue);
	insertParameterHashMap.put("vint64",    psInsertVInt64ParamValue);
	insertParameterHashMap.put("uint64",    psInsertUInt64ParamValue);
	insertParameterHashMap.put("vuint64",   psInsertVUInt64ParamValue);
	insertParameterHashMap.put("double",    psInsertDoubleParamValue);
	insertParameterHashMap.put("vdouble",   psInsertVDoubleParamValue);
	insertParameterHashMap.put("string",    psInsertStringParamValue);
	insertParameterHashMap.put("vstring",   psInsertVStringParamValue);
	insertParameterHashMap.put("EventID",   psInsertEventIDParamValue);
	insertParameterHashMap.put("VEventID",  psInsertVEventIDParamValue);
	insertParameterHashMap.put("InputTag",  psInsertInputTagParamValue);
	insertParameterHashMap.put("VInputTag", psInsertVInputTagParamValue);
	insertParameterHashMap.put("FileInPath",psInsertFileInPathParamValue);

	ResultSet rs = null;
	try {
	    rs = psSelectModuleTypes.executeQuery();
	    while (rs.next()) {
		int    typeId = rs.getInt(1);
		String type   = rs.getString(2);
		moduleTypeIdHashMap.put(type,typeId);
		templateTableNameHashMap.put(type,tableModuleTemplates);
	    }
	    

	    rs = psSelectParameterTypes.executeQuery();
/*	    int  typeId = 0;
	    while (rs.next()) {
		//int               typeId = rs.getInt(1);
                typeId++;
		String            type   = rs.getString(1);
		paramTypeIdHashMap.put(type,typeId);
		if (type.startsWith("v")||type.startsWith("V"))
		    isVectorParamHashMap.put(typeId,true);
		else
		    isVectorParamHashMap.put(typeId,false);
		}
*/
	    }
	catch (SQLException e) {
	    String errMsg = "ConfDB::prepareStatements() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }
    
    /** get values as strings after loading templates/configuration */
    private HashMap<Integer,ArrayList<Parameter> > getParameters(int configId)
	throws DatabaseException
    {
	HashMap<Integer,ArrayList<Parameter> > idToParameters =
	    new HashMap<Integer,ArrayList<Parameter> >();

	ResultSet rsParameters    = null;
	ResultSet rsBooleanValues = null;
	ResultSet rsIntValues     = null;
	ResultSet rsRealValues    = null;
	ResultSet rsStringValues  = null;

	try {
            if(configId==0)
            {
              //psSelectParametersTemplates.setInt(1,configId);
	      rsParameters    = psSelectParametersTemplates.executeQuery(); 
            }
	    else
            {
            	psSelectParameters.setInt(1,configId);
            	psSelectParameters.setInt(2,configId);
            	psSelectParameters.setInt(3,configId);
            	psSelectParameters.setInt(4,configId);
            	psSelectParameters.setInt(5,configId);
            	psSelectParameters.setInt(6,configId);
            	psSelectParameters.setInt(7,configId);
	    	rsParameters    = psSelectParameters.executeQuery();
            }


	    // get values as strings first
	    HashMap<Integer,String> idToValueAsString =
		new HashMap<Integer,String>();
	    
	    ArrayList<IdPSetPair>  psets  = new ArrayList<IdPSetPair>();
	    ArrayList<IdVPSetPair> vpsets = new ArrayList<IdVPSetPair>();

            //Queue<Integer> idlifo = new LinkedList<Integer>();
            Stack<Integer> idlifo= new Stack<Integer>();

            int newpamid=1000000;
            if(configId==0) newpamid=2000000;
	    int previouslvl=0;
	    while (rsParameters.next()) {
                int     parameterId       = rsParameters.getInt(1);
                String  type     = rsParameters.getString(2);
                String  name     = rsParameters.getString(3);
                boolean isTrkd   = rsParameters.getBoolean(4);
                int     seqNb    = rsParameters.getInt(5);
                int     parentId = rsParameters.getInt(6);
                int     lvl      = rsParameters.getInt(7);


             //parameterId=++newpamid;
             //parameterId=++countingParamIds;
             //dbidParamHashMap.put(parameterId,parameterId+newpamid);
             dbidParamHashMap.put(parameterId,++countingParamIds);
             parameterId=countingParamIds;

	     if (name==null) name = "";
             if (name.contains("Empty name")) name="";

            if (type.equals("bool")) { 
		String valueAsString =
		    (new Boolean(rsParameters.getBoolean(8))).toString();
		idToValueAsString.put(parameterId,valueAsString);
	    }
	    
            if (type.contains("int")) { 
		//Long    value         = new Long(rsParameters.getLong(8));
		boolean isHex         = false; //rsIntValues.getBoolean(4);
		
                if (rsParameters.getInt(10)>0) isHex=true;

		String  valueAsString = rsParameters.getString(8);

                if (type.contains("v")) { //should handle vector hex representation
                         valueAsString=rsParameters.getString(8);
                } else
                 if((isHex)&&(type.contains("64"))) valueAsString= "0x"+Long.toHexString(rsParameters.getLong(8));
                 else if (isHex) 
 		    valueAsString= "0x"+Integer.toHexString(rsParameters.getInt(8));
              
                Clob valueAsStringLOB = rsParameters.getClob(9);
                if (valueAsStringLOB!=null)
               { 
                 int lobLength=(int)valueAsStringLOB.length();
                 valueAsString=valueAsStringLOB.getSubString(1,lobLength);
                 valueAsString=valueAsString.trim();
               }	
               if (valueAsString!=null)
               { if (valueAsString.startsWith("{")) valueAsString=valueAsString.substring(1, valueAsString.length()-1);
                 valueAsString=valueAsString.trim();
               }
		
		//if (sequenceNb!=null&&
		//    idToValueAsString.containsKey(parameterId))
		//    idToValueAsString.put(parameterId,
	        //				  idToValueAsString.get(parameterId) +
	       // 				  ", "+valueAsString);
	//	else
		    idToValueAsString.put(parameterId,valueAsString);
	    }
	    
            if (type.contains("double")) { 
		String  valueAsString = rsParameters.getString(8);

                Clob valueAsStringLOB = rsParameters.getClob(9);
                if (valueAsStringLOB!=null)
               { 
                 int lobLength=(int)valueAsStringLOB.length();
                 valueAsString=valueAsStringLOB.getSubString(1,lobLength);
                 valueAsString=valueAsString.trim();
               }	
               if (valueAsString!=null)
               { if (valueAsString.startsWith("{")) valueAsString=valueAsString.substring(1, valueAsString.length()-1);
                 valueAsString=valueAsString.trim();
               }
                //valueAsString=valueAsString.trim();

		    idToValueAsString.put(parameterId,valueAsString);
	    }
	    
	    
	    /////////////////
//	    while (rsStringValues.next()) {
            if (type.contains("string")||type.contains("InputTag")||type.contains("ESInputTag")||type.contains("FileInPath")
                ||type.contains("EventID")) { 
			
	       String valueAsString = rsParameters.getString(8); // get PARAMETER_VALUE

               //if (valueAsString.startsWith("{")) valueAsString=valueAsString.substring(1, valueAsString.length()-1);
                //valueAsString=valueAsString.trim();
                Clob valueAsStringLOB = rsParameters.getClob(9);
                if (valueAsStringLOB!=null)
               { 
                 int lobLength=(int)valueAsStringLOB.length();
                 valueAsString=valueAsStringLOB.getSubString(1,lobLength);
                 valueAsString=valueAsString.trim();
               }	
               if (valueAsString!=null)
               { if (valueAsString.startsWith("{")) valueAsString=valueAsString.substring(1, valueAsString.length()-1);
                 valueAsString=valueAsString.trim();
               }
		 idToValueAsString.put(parameterId,valueAsString);
               
	    }
	    ///////////////////
	    
	    
	    
//           }	    
	    

//            if (type.equals("Pset") { 
			
/*            if(configId==0)
            {
	      	rsParameters    = psSelectParametersTemplates.executeQuery(); 
            }
	    else
            {
	    	rsParameters=psSelectParameters.executeQuery();
            }


	    int previouslvl=0;
//            Queue<Integer> idlifo = new LinkedList<Integer>();
	    while (rsParameters.next()) {
                int     parameterId       = rsParameters.getInt(1);
                String  type     = rsParameters.getString(2);
                String  name     = rsParameters.getString(3);
                boolean isTrkd   = rsParameters.getBoolean(4);
                int     seqNb    = rsParameters.getInt(5);
                int     parentId = rsParameters.getInt(6);
                int     lvl      = rsParameters.getInt(7);
 */            

                

                int orparid=parentId;
                //System.out.println("ParId "+parentId+" (origparid "+orparid+") parameterId "+parameterId+" type "+ type+" name "+name+" seqNb "+seqNb+" lvl"+lvl);

                        while (lvl<previouslvl) {
				idlifo.pop();
                                 previouslvl--;
                        }
                        if (lvl>0) parentId=idlifo.peek();
		        previouslvl=lvl;	

                //if (orparid==4774) System.out.println("ParId "+parentId+" (origparid "+orparid+") parameterId "+parameterId+" type "+
                 // type+" name "+name+" seqNb "+seqNb+" lvl"+lvl);

			String valueAsString = null;
			if (type.indexOf("PSet")<0)
			    valueAsString = idToValueAsString.remove(parameterId);
			if (valueAsString==null) valueAsString="";
			

			Parameter p = ParameterFactory.create(type,name,valueAsString,
							      isTrkd);
			
			if (type.equals("PSet")) {
                            idlifo.push(new Integer(parameterId));
                            previouslvl++;
			    psets.add(new IdPSetPair(parameterId,(PSetParameter)p));
                        }
			if (type.equals("VPSet")){
                            idlifo.push(new Integer(parameterId));
                            previouslvl++;
			    vpsets.add(new IdVPSetPair(parameterId,(VPSetParameter)p));
                        }
			
			ArrayList<Parameter> parameters = null;
			if (idToParameters.containsKey(parentId))
			    parameters = idToParameters.get(parentId);
			else {
			    parameters = new ArrayList<Parameter>();
			    idToParameters.put(parentId,parameters);
			}
			while (parameters.size()<=seqNb) parameters.add(null);
			parameters.set(seqNb,p);
	    }
	    
	    Iterator<IdPSetPair> itPSet = psets.iterator();
	    while (itPSet.hasNext()) {
		IdPSetPair    pair   = itPSet.next();
		int          psetId = pair.id;
		PSetParameter pset   = pair.pset;
		ArrayList<Parameter> parameters = idToParameters.remove(psetId);
		if (parameters!=null) {
		    int missingCount = 0;
		    Iterator<Parameter> it = parameters.iterator();
		    while (it.hasNext()) {
			Parameter p = it.next();
			if (p==null) missingCount++;
			else pset.addParameter(p);
		    }
		    // TODO Database check. This is happening.
		    if (missingCount>0)
			System.err.println("WARNING: "+missingCount+" parameter(s)"+
					   " missing from PSet '"+pset.name()+"'");
		}
	    }

	    Iterator<IdVPSetPair> itVPSet = vpsets.iterator();
	    while (itVPSet.hasNext()) {
		IdVPSetPair    pair    = itVPSet.next();
		int           vpsetId = pair.id;
		VPSetParameter vpset   = pair.vpset;
		ArrayList<Parameter> parameters=idToParameters.remove(vpsetId);
		if (parameters!=null) {
		    int missingCount = 0;
		    Iterator<Parameter> it = parameters.iterator();
		    while (it.hasNext()) {
			Parameter p = it.next();
			if (p==null||!(p instanceof PSetParameter)) missingCount++;
			else vpset.addParameterSet((PSetParameter)p);
		    }
		    // TODO database check, this is happening.
		    if (missingCount>0)
			System.err.println("WARNING: "+missingCount+" pset(s)"+
					   " missing from VPSet '"+vpset.name()+"'");
		}
	    }
	    
	}
	catch (SQLException e) {
	    String errMsg = "ConfDB::getParameters() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rsParameters);
	    dbConnector.release(rsBooleanValues);
	    dbConnector.release(rsIntValues);
	    dbConnector.release(rsRealValues);
	    dbConnector.release(rsStringValues);
	}
	
	return idToParameters;
    }

    
    private String CLOBToString(Clob cl) throws IOException, SQLException {
	    if (cl == null) return "";
	    StringBuffer strOut = new StringBuffer();
	    String aux;
	    BufferedReader br = new BufferedReader(cl.getCharacterStream());
	    while ((aux=br.readLine()) != null)
	    	strOut.append(aux);
	
	    return strOut.toString();
    }
    
    
    /** set parameters of an instance */
    private void updateInstanceParameters(Instance instance,
					  ArrayList<Parameter> parameters)
    {
	if (parameters==null) return;
	int id = instance.databaseId();
	Iterator<Parameter> it = parameters.iterator();
	while (it.hasNext()) {
	    Parameter p = it.next();
	    if (p==null) continue;
	    instance.updateParameter(p.name(),p.type(),p.valueAsString());
	}
	instance.setDatabaseId(id);
    }
    
    /** insert vpset into ParameterSets table */
    private void insertVecParameterSet(int           parentId,
				       int            sequenceNb,
                                       int           lvl,
				       VPSetParameter vpset,
                                       PreparedStatement dbstmnt)
	throws DatabaseException
    {
	//int      vpsetId = insertSuperId();
	ResultSet rs      = null;
	try {
            if (dbstmnt==psInsertMoElement) {
                dbstmnt.setInt(1,3); //moetype
                dbstmnt.setString(2,vpset.name());
                dbstmnt.setInt(3,-1);
                dbstmnt.setString(4,"VPSet");
                dbstmnt.setBoolean(5,vpset.isTracked());
                dbstmnt.setInt(6,999);
             } else {
             dbstmnt.setInt(1,parentId);
	    dbstmnt.setString(2,vpset.name());
	    dbstmnt.setInt(3,lvl);
	    dbstmnt.setBoolean(4,vpset.isTracked());
	    dbstmnt.setString(5,"VPSet");
            dbstmnt.setInt(6,sequenceNb);
            dbstmnt.setInt(10,3); //moetype
            }
            dbstmnt.setString(7,null);
            dbstmnt.setString(8,null);
            dbstmnt.setString(9,null);

	    dbstmnt.execute();
	    if (dbstmnt==psInsertMoElement) {
                rs=dbstmnt.getGeneratedKeys();
                rs.next();
                int paramId = rs.getInt(1);
                psInsertPae2Moe.setInt(1,parentId);
                psInsertPae2Moe.setInt(2,paramId);
                psInsertPae2Moe.setInt(3,lvl);
                psInsertPae2Moe.setInt(4,sequenceNb);
                psInsertPae2Moe.executeUpdate();
           }
 
	    for (int i=0;i<vpset.parameterSetCount();i++) {
		PSetParameter pset = vpset.parameterSet(i);
		insertParameterSet(parentId,i,lvl+1,pset,dbstmnt);
	    }
	}
	catch (SQLException e) { 
	    String errMsg =
		"ConfDB::insertVecParameterSet(parId="+parentId+
		",sequenceNb="+sequenceNb+",vpset="+vpset.name()+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
	//insertSuperIdVecParamSetAssoc(superId,vpsetId,sequenceNb);
    }
    
    /** insert pset into ParameterSets table */
    private void insertParameterSet(int          parentId,
				    int           sequenceNb,
                                    int         lvl,
				    PSetParameter pset,
				    PreparedStatement dbstmnt)
	throws DatabaseException
    {
	//int      psetId = insertSuperId();
	ResultSet rs = null;
	try {
              if (dbstmnt==psInsertMoElement) {
                dbstmnt.setInt(1,2); //moetype
                dbstmnt.setString(2,pset.name());
                dbstmnt.setInt(3,-1);
                dbstmnt.setString(4,"PSet");
                dbstmnt.setBoolean(5,pset.isTracked());
                dbstmnt.setInt(6,999);
             } else {
	    dbstmnt.setInt(1,parentId);
	    dbstmnt.setString(2,pset.name());
	    dbstmnt.setInt(3,lvl);
	    dbstmnt.setBoolean(4,pset.isTracked());
	    dbstmnt.setString(5,"PSet");
	    dbstmnt.setInt(6,sequenceNb);
	    dbstmnt.setInt(10,2); //moetype
	    }
            dbstmnt.setString(7,null);
	    dbstmnt.setString(8,null);
	    dbstmnt.setString(9,null);
	    dbstmnt.execute();
	    //rs=dbstmnt.getGeneratedKeys(); 
//            rs.next();
//            int psetId = rs.getInt(1);
            if (dbstmnt==psInsertMoElement) {
                rs=dbstmnt.getGeneratedKeys();
                rs.next();
                int paramId = rs.getInt(1);
                psInsertPae2Moe.setInt(1,parentId);
                psInsertPae2Moe.setInt(2,paramId);
                psInsertPae2Moe.setInt(3,lvl);
                psInsertPae2Moe.setInt(4,sequenceNb);
                psInsertPae2Moe.executeUpdate();
           }


            
	    for (int i=0;i<pset.parameterCount();i++) {
		Parameter p = pset.parameter(i);
		if (p instanceof PSetParameter) {
		    PSetParameter ps = (PSetParameter)p;
		    insertParameterSet(parentId,i,lvl+1,ps,dbstmnt);
		}
		else if (p instanceof VPSetParameter) {
		    VPSetParameter vps = (VPSetParameter)p;
		    insertVecParameterSet(parentId,i,lvl+1,vps,dbstmnt);
		}
		else {
		    insertParameter(parentId,i,lvl+1,p,dbstmnt);
		}
	    }
	}
	catch (SQLException e) { 
	    String errMsg =
		"ConfDB::insertParameterSet(parId="+parentId+
		",sequenceNb="+sequenceNb+",pset="+pset.name()+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
//	insertSuperIdParamSetAssoc(superId,psetId,sequenceNb);
    }
    
    /** insert parameter into Parameters table */
    private void insertParameter(int      parentId,
				 int       sequenceNb,
				 int       lvl,
				 Parameter parameter,
                                 PreparedStatement dbstmnt)
	throws DatabaseException
    {
	ResultSet rs      = null;
	try {
	    /* Fix for File in Path Error*/
	    /*    if(parameter instanceof FileInPathParameter){
		FileInPathParameter fileInPathParameter = (FileInPathParameter)parameter;
		//	if(fileInPathParameter.valueAsString().length()==0||fileInPathParameter.valueAsString()==null)
		fileInPathParameter.setValue("' '");
		}*/

	    if (dbstmnt==psInsertMoElement) {
	        dbstmnt.setInt(1,1); //moetype
	      	dbstmnt.setString(2,parameter.name());
	        dbstmnt.setInt(3,-1);
	    	dbstmnt.setString(4,parameter.type());
	    	dbstmnt.setBoolean(5,parameter.isTracked());
	        dbstmnt.setInt(6,999);
             } else {
	    dbstmnt.setInt(1,parentId);
	    dbstmnt.setString(2,parameter.name());
	    dbstmnt.setInt(3,lvl);
	    dbstmnt.setBoolean(4,parameter.isTracked());
	    dbstmnt.setString(5,parameter.type());
            dbstmnt.setInt(6,sequenceNb);
	    dbstmnt.setInt(10,1); //moetype
            }

            String value = "";
            int hexo=0;

            if (parameter instanceof VectorParameter) {
                value="{ ";
                VectorParameter vp = (VectorParameter)parameter;
                for (int i=0;i<vp.vectorSize();i++) {
                    if (vp instanceof VStringParameter) {
                        value+=  " \"" + (String)vp.value(i) + "\" ";
		    }
                    else if (vp instanceof VUInt64Parameter) {
                        Long vuint64=((BigInteger)vp.value(i)).longValue(); 
                        value+= " "+vuint64.toString()+" ";
                    } else if (vp instanceof VInputTagParameter) {
                        if (((String) vp.value(i)).isEmpty()) value+=" \"\" ";
                        else value+=" "+((String) vp.value(i))+" ";
		    } else 
                    {
                        value +=" "+ (vp.value(i)).toString()+" ";
                    }
                    if (i<(vp.vectorSize()-1)) { value +=",";}
                    else {value +="}";}

                    if (vp instanceof VInt32Parameter) {
                        VInt32Parameter vint32=(VInt32Parameter)vp;
                        if (vint32.isHex(i)) hexo=1;
                    } else if (vp instanceof VUInt32Parameter) {
                        VUInt32Parameter vuint32=(VUInt32Parameter)vp;
                        if (vuint32.isHex(i)) hexo=1;
                    } else if (vp instanceof VInt64Parameter) {
                        VInt64Parameter vint64=(VInt64Parameter)vp;
                        if (vint64.isHex(i)) hexo=1;
                    } else if (vp instanceof VUInt64Parameter) {
                        VUInt64Parameter vuint64=(VUInt64Parameter)vp;
                        if (vuint64.isHex(i)) hexo=1;
                    }
                        
                 }
             
             }
             else {
                ScalarParameter sp = (ScalarParameter)parameter;
                if (sp instanceof StringParameter) {
		  	value = ((StringParameter)sp).valueAsString();
                }
                else if (sp instanceof FileInPathParameter) {
                  	value =((FileInPathParameter)sp).valueAsString();
                }
                else if (sp instanceof UInt64Parameter) {
                        Long vuint64=((BigInteger)sp.value()).longValue();
                        value=vuint64.toString();
                }
                else {
               		value = (String) sp.valueAsString();
                }
		if (sp instanceof Int32Parameter) {
                    Int32Parameter int32=(Int32Parameter)sp;
		    if (int32.isHex()) hexo=1;
                } else if (sp instanceof UInt32Parameter) {
                    UInt32Parameter uint32=(UInt32Parameter)sp;
                    if (uint32.isHex()) hexo=1;
                } else if (sp instanceof Int64Parameter) {
                    Int64Parameter int64=(Int64Parameter)sp;
                    if (int64.isHex()) hexo=1;
                } else if (sp instanceof UInt64Parameter) {
                    UInt64Parameter uint64=(UInt64Parameter)sp;
                    if (uint64.isHex()) hexo=1;
                }
		
              }
  
              if (value.length()<4000) {
                   dbstmnt.setString(7,value); 
                   dbstmnt.setString(8,null); 
              } else {
              	    dbstmnt.setString(7,null);
                    dbstmnt.setString(8,value); 
              }
 
              dbstmnt.setInt(9,hexo); 
	      dbstmnt.executeUpdate();

	    if (dbstmnt==psInsertMoElement) {
            	rs=dbstmnt.getGeneratedKeys(); 
            	rs.next();
            	int paramId = rs.getInt(1);
                psInsertPae2Moe.setInt(1,parentId);
                psInsertPae2Moe.setInt(2,paramId);
                psInsertPae2Moe.setInt(3,lvl);
                psInsertPae2Moe.setInt(4,sequenceNb);
     
                psInsertPae2Moe.executeUpdate();
           }

	}
	catch (SQLException e) { 
	    String errMsg =
		"ConfDB::insertParameter(parId="+parentId+",sequenceNb="+
		sequenceNb+",parameter="+parameter.name()+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
	
    }
    
    /** associate parameter with the service/module superid */
    private void insertSuperIdParamAssoc(int superId,int paramId,
					 int sequenceNb)
	throws DatabaseException
    {
	try {
	    psInsertSuperIdParamAssoc.setInt(1,superId);
	    psInsertSuperIdParamAssoc.setInt(2,paramId);
	    psInsertSuperIdParamAssoc.setInt(3,sequenceNb);
	    psInsertSuperIdParamAssoc.addBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertSuperIdParamAssoc(superId="+superId+
		",paramId="+paramId+",sequenceNb="+sequenceNb+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
    }
    
    /** associate pset with the service/module superid */
    private void insertSuperIdParamSetAssoc(int superId,int psetId,
					    int sequenceNb)
	throws DatabaseException
    {
	try {
	    psInsertSuperIdParamSetAssoc.setInt(1,superId);
	    psInsertSuperIdParamSetAssoc.setInt(2,psetId);
	    psInsertSuperIdParamSetAssoc.setInt(3,sequenceNb);
	    psInsertSuperIdParamSetAssoc.addBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::inesrtSuperIdParamSetAssoc(superId="+superId+
		",psetId="+psetId+",sequenceNb="+sequenceNb+") failed: "+
		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
	}
    }
    
    /** associate vpset with the service/module superid */
    private void insertSuperIdVecParamSetAssoc(int superId,int vpsetId,
					       int sequenceNb)
	throws DatabaseException
    {
	try {
	    psInsertSuperIdVecParamSetAssoc.setInt(1,superId);
	    psInsertSuperIdVecParamSetAssoc.setInt(2,vpsetId);
	    psInsertSuperIdVecParamSetAssoc.setInt(3,sequenceNb);
	    psInsertSuperIdVecParamSetAssoc.addBatch();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::inesrtSuperIdVecParamSetAssoc(superId="+superId+
		",vpsetId="+vpsetId+",sequenceNb="+sequenceNb+") failed: "+
		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
	}
    }
    
    /** insert a parameter value in the table corresponding to the parameter type */
    private void insertParameterValue(int paramId,Parameter parameter)
	throws DatabaseException
    {
	if (!parameter.isValueSet()) {
	    // PS 05/06/08: allow unset tracked parameters to be saved
	    // -------------------------------------------------------
	    //if (parameter.isTracked()) {
	    //String errMsg =
	    //  "ConfDB::insertParameterValue(paramId="+paramId+
	    //  ",parameter="+parameter.name()+") failed: parameter is tracked"+
	    //  " but not set.";
	    //throw new DatabaseException(errMsg);
	    //}
	    //else return;
	    return;
	}
	
	PreparedStatement psInsertParameterValue =
	    insertParameterHashMap.get(parameter.type());

	try {
	    if (parameter instanceof VectorParameter) {
		VectorParameter vp = (VectorParameter)parameter;
		for (int i=0;i<vp.vectorSize();i++) {
		    psInsertParameterValue.setInt(1,paramId);
		    psInsertParameterValue.setInt(2,i);

		    if (vp instanceof VStringParameter) {
			String value = "\"" + (String)vp.value(i) + "\"";
			psInsertParameterValue.setString(3,value);
		    }
		    else if (vp instanceof VUInt64Parameter) {
			psInsertParameterValue.setObject(3,((BigInteger)vp.value(i)).longValue());
		    } else if (vp instanceof VInputTagParameter) {
		    	// fix to bug #90850: "Export Configuration Failed"
		    	String value = (String) vp.value(i);
		    	if(value.isEmpty()) value = "\"\"";		 
		    	psInsertParameterValue.setString(3,value);
		    } else {
			psInsertParameterValue.setObject(3,vp.value(i));
		    }

		    if (vp instanceof VInt32Parameter) {
			VInt32Parameter vint32=(VInt32Parameter)vp;
			psInsertParameterValue.setBoolean(4,vint32.isHex(i));
		    }
		    else if (vp instanceof VUInt32Parameter) {
			VUInt32Parameter vuint32=(VUInt32Parameter)vp;
			psInsertParameterValue.setBoolean(4,vuint32.isHex(i));
		    }
		    else if (vp instanceof VInt64Parameter) {
			VInt64Parameter vint64=(VInt64Parameter)vp;
			psInsertParameterValue.setBoolean(4,vint64.isHex(i));
		    }
		    else if (vp instanceof VUInt64Parameter) {
			VUInt64Parameter vuint64=(VUInt64Parameter)vp;
			psInsertParameterValue.setBoolean(4,vuint64.isHex(i));
		    }
		    psInsertParameterValue.addBatch();
		}
	    }
	    else {
		ScalarParameter sp = (ScalarParameter)parameter;
		psInsertParameterValue.setInt(1,paramId);

		if (sp instanceof StringParameter) {
		    StringParameter string = (StringParameter)sp;
		    psInsertParameterValue.setString(2,string.valueAsString());
		}
		else if (sp instanceof FileInPathParameter) {
		    FileInPathParameter fileInPathParameter = (FileInPathParameter)sp;
		    psInsertParameterValue.setString(2,fileInPathParameter.valueAsString());
		}
		else if (sp instanceof UInt64Parameter) {
		    psInsertParameterValue.setObject(2,((BigInteger)sp.value()).longValue());
		}
		else{
		    psInsertParameterValue.setObject(2,sp.value());
		}

		if (sp instanceof Int32Parameter) {
		    Int32Parameter int32=(Int32Parameter)sp;
		    psInsertParameterValue.setBoolean(3,int32.isHex());
		}
		else if (sp instanceof UInt32Parameter) {
		    UInt32Parameter uint32=(UInt32Parameter)sp;
		    psInsertParameterValue.setBoolean(3,uint32.isHex());
		}
		else if (sp instanceof Int64Parameter) {
		    Int64Parameter int64=(Int64Parameter)sp;
		    psInsertParameterValue.setBoolean(3,int64.isHex());
		}
		else if (sp instanceof UInt64Parameter) {
		    UInt64Parameter uint64=(UInt64Parameter)sp;
		    psInsertParameterValue.setBoolean(3,uint64.isHex());
		}
		psInsertParameterValue.addBatch();
	    }
	}
	catch (Exception e) {
	    String errMsg =
		"ConfDB::insertParameterValue(paramId="+paramId+
		",parameter="+parameter.name()+") failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
    }
    
    /** associate a template super id with a software release */
    private void insertSuperIdReleaseAssoc(int superId, String releaseTag)
	throws DatabaseException
    {
	int releaseId = getReleaseId(releaseTag);
	try {
	    psInsertSuperIdReleaseAssoc.setInt(1,superId);
	    psInsertSuperIdReleaseAssoc.setInt(2,releaseId);
	    psInsertSuperIdReleaseAssoc.executeUpdate();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertSuperIdReleaseAssoc(superId="+superId+
		",releaseTag="+releaseTag+") failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
	}
    }
    
    /** associate a template super id with a software release */
    private void insertSuperIdReleaseAssoc(int superId, int releaseId)
	throws DatabaseException
    {
	try {
	    psInsertSuperIdReleaseAssoc.setInt(1,superId);
	    psInsertSuperIdReleaseAssoc.setInt(2,releaseId);
	    psInsertSuperIdReleaseAssoc.executeUpdate();
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::insertSuperIdReleaseAssoc(superId="+superId+
		",releaseId="+releaseId+") failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
	}
    }
    

    /** get the release id for a release tag */
    public int getReleaseId(String releaseTag) throws DatabaseException
    {
	reconnect();
	
	ResultSet rs = null;
	try {
	    psSelectReleaseId.setString(1,releaseTag);
	    rs = psSelectReleaseId.executeQuery();
	    rs.next();
	    return rs.getInt(1);
	}
	catch (SQLException e) {
	    String errMsg =
		"ConfDB::getReleaseId(releaseTag="+releaseTag+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }

    /** get the release id for a release tag */
    private String getReleaseTag(int releaseId) throws DatabaseException
    {
	ResultSet rs = null;
	try {
	    psSelectReleaseTag.setInt(1,releaseId);
	    rs = psSelectReleaseTag.executeQuery();
	    rs.next();
	    return rs.getString(1);
	}
	catch (SQLException e) {
	    String errMsg =
		"ConbfDB::getReleaseTag(releaseId="+releaseId+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }

    /** get the release id for a release tag */
    private String getReleaseTagForConfig(int configId)
	throws DatabaseException
    {
	reconnect();

	ResultSet rs = null;
	try {
	    psSelectReleaseTagForConfig.setInt(1,configId);
	    rs = psSelectReleaseTagForConfig.executeQuery();
	    rs.next();
	    return rs.getString(1);
	}
	catch (SQLException e) {
	    String errMsg =
		"ConbfDB::getReleaseTagForConfig(configId="+configId+") failed: "+
		e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
    }

    /** look for ConfigInfo in the specified parent directory */
    private ConfigInfo getConfigInfo(int configId,Directory parentDir)
    {
	for (int i=0;i<parentDir.configInfoCount();i++) {
	    ConfigInfo configInfo = parentDir.configInfo(i);
	    for (int ii=0;ii<configInfo.versionCount();ii++) {
		ConfigVersion configVersion = configInfo.version(ii);
		if (configVersion.dbId()==configId) {
		    configInfo.setVersionIndex(ii);
		    return configInfo;
		}
	    }
	}
	
	for (int i=0;i<parentDir.childDirCount();i++) {
	    ConfigInfo configInfo = getConfigInfo(configId,parentDir.childDir(i));
	    if (configInfo!=null) return configInfo;
	}
	
	return null;
    }
    
    /** get subsystems and a hash map to all packages */
    private ArrayList<SoftwareSubsystem> getSubsystems(HashMap<Integer,
						       SoftwarePackage> idToPackage)
	throws DatabaseException
    {
	ArrayList<SoftwareSubsystem> result =
	    new ArrayList<SoftwareSubsystem>();
	
	HashMap<Integer,SoftwareSubsystem> idToSubsystem =
	    new HashMap<Integer,SoftwareSubsystem>();
	
	ResultSet rs = null;
	try {
	    rs = psSelectSoftwareSubsystems.executeQuery();
	    
	    while (rs.next()) {
		int    id   = rs.getInt(1);
		String name = rs.getString(2);
		SoftwareSubsystem subsystem = new SoftwareSubsystem(name);
		result.add(subsystem);
		idToSubsystem.put(id,subsystem);
	    }
	    
	    rs = psSelectSoftwarePackages.executeQuery();

	    while (rs.next()) {
		int    id       = rs.getInt(1);
		int    subsysId = rs.getInt(2);
		String name     = rs.getString(3);
		
		SoftwarePackage   pkg = new SoftwarePackage(name);
		pkg.setSubsystem(idToSubsystem.get(subsysId));
		idToPackage.put(id,pkg);
	    }
	}
	catch (SQLException e) {
	    String errMsg = "ConfDB::getSubsystems() failed: "+e.getMessage();
	    throw new DatabaseException(errMsg,e);
	}
	finally {
	    dbConnector.release(rs);
	}
	
	return result;
    }
    
    public String debugSQL(PreparedStatement pstmt) {
    	String value = "<SQL not found>";
    	try {
    		Class<?> stmt1 = pstmt.getClass();  
    	    java.lang.reflect.Field mem = stmt1.getField("sql");  
    	    value= (String)mem.get(pstmt);
    	} catch (NoSuchFieldException x) {
    	    x.printStackTrace();
     	} catch (IllegalAccessException x) {
     	    x.printStackTrace();
    	}
	     
	    return value;
    }
    
    
    //
    // MAIN
    //

    /** main method for testing */
    public static void main(String[] args)
    {
	String  configId    =          "";
	String  configName  =          "";

	String  releaseId   =          "";
	String  releaseName =          "";

	boolean dolistcounts=       false;
	boolean dolistconf  =       false;	
	boolean dolistrel   =       false;	
	String  list        =          "";

	boolean dopackages  =       false;
	boolean doversions  =       false;
	boolean doremove    =       false;


	String  dbType      =           "oracle";
	String  dbHost      =  "int2r1-v.cern.ch";
	String  dbPort      =            "10121";
	String  dbName      = "int2r_nolb.cern.ch";
	String  dbUser      =       "cms_hlt_gdr";
	String  dbPwrd      =                 "";

	for (int iarg=0;iarg<args.length;iarg++) {
	    String arg = args[iarg];
	    if      (arg.equals("--configId"))   { configId   = args[++iarg]; }
	    else if (arg.equals("--configName")) { configName = args[++iarg]; }
	    else if (arg.equals("--releaseId"))  { releaseId  = args[++iarg]; }
	    else if (arg.equals("--releaseName")){ releaseName= args[++iarg]; }
	    else if (arg.equals("--listCounts")){
		dolistcounts=true;
	    }
	    else if (arg.equals("--listConfigs")){
		dolistconf=true;
		list=args[++iarg];
	    }
	    else if (arg.equals("--listReleases")){
		dolistrel=true;
	    }
	    else if (arg.equals("--packages"))   { dopackages = true; }
	    else if (arg.equals("--remove"))     { doremove   = true; }
	    else if (arg.equals("--versions"))   { doversions = true; }
	    else if (arg.equals("-t")||arg.equals("--dbtype")) {
		dbType = args[++iarg];
	    }
	    else if (arg.equals("-h")||arg.equals("--dbhost")) {
		dbHost = args[++iarg];
	    }
	    else if (arg.equals("-p")||arg.equals("--dbport")) {
		dbPort = args[++iarg];
	    }
	    else if (arg.equals("-d")||arg.equals("--dbname")) {
		dbName = args[++iarg];
	    }
	    else if (arg.equals("-u")||arg.equals("--dbuser")) {
		dbUser = args[++iarg];
	    }
	    else if (arg.equals("-s")||arg.equals("--dbpwrd")) {
		dbPwrd = args[++iarg];
	    }
	    else {
		System.err.println("ERROR: invalid option '" + arg + "'!");
		System.exit(0);
	    }
	}
	
	int check = 0;
	if (configId.length()>0)    check++;
	if (configName.length()>0)  check++;
	if (releaseId.length()>0)   check++;
	if (releaseName.length()>0) check++;
	
	if (check==0&&!dolistcounts&&!dolistconf&&!dolistrel) {
	    System.err.println("ERROR: specify config, release, ");
	    System.exit(0);
	}
	if ((check>1||(dolistconf&&dolistrel))||
	    (check==0&&(dolistconf&&dolistrel))) {
	    System.err.println("ERROR: specify either of "+
			       "--configId, --configName, "+
			       "--releaseId, --releaseName,"+
			       "--listConfigs, *or* --listReleases");
	    System.exit(0);
	}
	
	if (!dolistcounts&&!dolistconf&&!dolistrel&&
	    !dopackages&&!doversions&&!doremove)
	    System.exit(0);
	
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
	
	ConfDB database = new ConfDB();

	try {
	    database.connect(dbType,dbUrl,dbUser,dbPwrd);
	    // list configurations
	    if (dolistcounts) {
		database.listCounts();
	    }
	    else if (dolistconf) {
		String[] allConfigs = database.getConfigNames();
		int count = 0;
		for (String s : allConfigs)
		    if (s.startsWith(list)) { count++; System.out.println(s); }
		System.out.println(count+" configurations");
	    }
	    // list releases
	    else if (dolistrel) {
		String[] allReleases = database.getReleaseTagsSorted();
		for (String s : allReleases) System.out.println(s);
		System.out.println(allReleases.length+" releases");
	    }
	    // configurations
	    else if (configId.length()>0||configName.length()>0) {
		int id = (configId.length()>0) ?
		    Integer.parseInt(configId) : database.getConfigId(configName);
		if (id<=0) System.out.println("Configuration not found!");
		else if (dopackages) {
		    Configuration   config  = database.loadConfiguration(id);
		    SoftwareRelease release = config.release();
		    Iterator<String> it =
			release.listOfReferencedPackages().iterator();
		    while (it.hasNext()) System.out.println(it.next());
		}
		else if (doversions) {
		    ConfigInfo info = database.getConfigInfo(id);
		    System.out.println("name=" + info.parentDir().name() + "/" +
				       info.name());
		    for (int i=0;i<info.versionCount();i++) {
			ConfigVersion version = info.version(i);
			System.out.println(version.version()+"\t"+
					   version.dbId()+"\t"+
					   version.releaseTag()+"\t"+
					   version.created()+"\t"+
					   version.creator());
			if (version.comment().length()>0)
			    System.out.println("  -> " + version.comment());
		    }
		}
		else if (doremove) {
		    ConfigInfo info = database.getConfigInfo(id);
		    System.out.println("REMOVE " + info.parentDir().name() + "/" +
				       info.name()+ "/V" + info.version());
		    try {
			database.removeConfiguration(info.dbId());
		    }
		    catch (DatabaseException e2) {
			System.out.println("REMOVE FAILED!");
			e2.printStackTrace();
		    }
		}
	    }
	    // releases
	    else if (releaseId.length()>0||releaseName.length()>0) {
		int id = (releaseId.length()>0) ?
		    Integer.parseInt(releaseId):database.getReleaseId(releaseName);
		if (id<=0) System.err.println("Release not found!");
		else if (dopackages) {
		    SoftwareRelease release = new SoftwareRelease();
		    database.loadSoftwareRelease(id,release);
		    Iterator<String> it = release.listOfPackages().iterator();
		    while (it.hasNext()) System.out.println(it.next());
		}
		else if (doremove) {
		    String[] configs = database.getConfigNamesByRelease(id);
		    if (configs.length>0) {
			System.out.println(configs.length+" configurations "+
					   "associated with release "+
					   releaseName+":");
			for (String s : configs) System.out.println(s);
			System.out.println("\nDO YOU REALLY WANT TO DELETE ALL "+
					   "LISTED RELEASES (YES/NO)?! ");
			BufferedReader br =
			    new BufferedReader(new InputStreamReader(System.in));
			String answer = null;
			try {  answer = br.readLine(); }
			catch (IOException ioe) { System.exit(1); }
			if (!answer.equals("YES")) System.exit(0);
			System.out.println("REMOVE CONFIGURATIONS!");
			for (String s : configs) {
			    System.out.print("Remove "+s+"... ");
			    int cid = database.getConfigId(s);
			    database.removeConfiguration(cid);
			    System.out.println("REMOVED");
			}
		    }
		    System.out.print("\nRemove "+releaseName+"... ");
		    database.removeSoftwareRelease(id);
		    System.out.println("REMOVED");
		}
	    }
	}
	catch (DatabaseException e) {
	    System.err.println("Failed to connet to DB: " + e.getMessage());
	}
	finally {
	    try { database.disconnect(); } catch (DatabaseException e) {}
	}
    }
    
}


//
// helper classes
//

/** define class holding a pair of id and associated instance */
class IdInstancePair
{
    public int     id;
    public Instance instance;
    IdInstancePair(int id, Instance instance)
    {
	this.id       = id;
	this.instance = instance;
    }
}

/** define class holding a pair of id and associated PSet */
class IdPSetPair
{
    public int          id;
    public PSetParameter pset;
    IdPSetPair(int id, PSetParameter pset)
    {
	this.id   = id;
	this.pset = pset;
    }
}

/** define class holding a pair of id and associated VPSet */
class IdVPSetPair
{
    public int           id;
    public VPSetParameter vpset;
    IdVPSetPair(int id, VPSetParameter vpset)
    {
	this.id    = id;
	this.vpset = vpset;
    }
}

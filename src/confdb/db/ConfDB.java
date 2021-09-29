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
 * ConfDB ------
 * 
 * @author Philipp Schieferdecker
 *
 *         Handle all database access operations.
 */
public class ConfDB {
	//
	// member data
	//

	/** define database arch types */
	public static final String dbTypeMySQL = "mysql";
	public static final String dbTypeOracle = "oracle";

	/** define database table names */
	public static final String tableEDSourceTemplates = "EDSourceTemplates";
	public static final String tableESSourceTemplates = "ESSourceTemplates";
	public static final String tableESModuleTemplates = "ESModuleTemplates";
	public static final String tableServiceTemplates = "ServiceTemplates";
	public static final String tableModuleTemplates = "ModuleTemplates";

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

	// database features (BSATARIC: is this used at all - it seems it's always
	// true?):
	private boolean extraPathFieldsAvailability = true;
	private boolean operatorFieldForSequencesAvailability = true;
	private boolean operatorFieldForTasksAvailability = true;
	private boolean operatorFieldForSwitchProducersAvailability = true;

	// sv
	private Integer countingParamIds = 20000000;

	/** template table name hash map */
	private HashMap<String, String> templateTableNameHashMap = null;

	/** module type id hash map */
	private HashMap<String, Integer> moduleTypeIdHashMap = null;

	/** parameter type id hash map */
	private HashMap<String, Integer> paramTypeIdHashMap = null;

	/** vector/scalar parameter hash map */
	private HashMap<Integer, Boolean> isVectorParamHashMap = null;

	/** 'insert parameter' sql statement hash map */
	private HashMap<String, PreparedStatement> insertParameterHashMap = null;

	/** Original parameters DB hash map */
	private HashMap<Integer, Integer> dbidParamHashMap = null;

	/** Original parameters DB hash map */
	private HashMap<Integer, Integer> dbidTemplateHashMap = null;

	/** prepared sql statements */
	private PreparedStatement psSelectModuleTypes = null;
	private PreparedStatement psSelectParameterTypes = null;

	private PreparedStatement psSelectDirectories = null;
	private PreparedStatement psSelectConfigurations = null;
	private PreparedStatement psSelectLockedConfigurations = null;
	private PreparedStatement psSelectUsersForLockedConfigs = null;

	private PreparedStatement psSelectSwArchNames = null;
	private PreparedStatement psSelectOrigDbId = null;
	private PreparedStatement psSelectNewDbId = null;

	private PreparedStatement psSelectConfigNames = null;
	private PreparedStatement psSelectConfigNamesByRelease = null;
	private PreparedStatement psSelectDirectoryId = null;
	private PreparedStatement psSelectConfigurationId = null;
	private PreparedStatement psSelectConfigurationIdLatest = null;
	private PreparedStatement psSelectConfigurationCreated = null;

	private PreparedStatement psSelectReleaseTags = null;
	private PreparedStatement psSelectReleaseTagsSorted = null;
	private PreparedStatement psSelectReleaseId = null;
	private PreparedStatement psSelectReleaseTag = null;
	private PreparedStatement psSelectReleaseTagForConfig = null;

	private PreparedStatement psSelectSoftwareSubsystems = null;
	private PreparedStatement psSelectSoftwarePackages = null;

	private PreparedStatement psSelectEDSourceTemplate = null;
	private PreparedStatement psSelectESSourceTemplate = null;
	private PreparedStatement psSelectESModuleTemplate = null;
	private PreparedStatement psSelectServiceTemplate = null;
	private PreparedStatement psSelectModuleTemplate = null;

	private PreparedStatement psSelectStreams = null;
	private PreparedStatement psSelectPrimaryDatasets = null;
	// private PreparedStatement psSelectStreamEntries = null;
	private PreparedStatement psSelectPrimaryDatasetEntries = null;

	private PreparedStatement psSelectPSetsForConfig = null;
	private PreparedStatement psSelectEDSourcesForConfig = null;
	private PreparedStatement psSelectESSourcesForConfig = null;
	private PreparedStatement psSelectESModulesForConfig = null;
	private PreparedStatement psSelectServicesForConfig = null;
	private PreparedStatement psSelectSequencesForConfig = null;
	private PreparedStatement psSelectTasksForConfig = null; // BSATARIC: not used
	private PreparedStatement psSelectSwitchProducersForConfig = null; // BSATARIC: not used
	private PreparedStatement psSelectPathsForConfig = null;
	private PreparedStatement psSelectContentForConfig = null;

	private PreparedStatement psSelectModulesForSeq = null;
	private PreparedStatement psSelectModulesForPath = null;

	private PreparedStatement psSelectEDSourceTemplatesForRelease = null;
	private PreparedStatement psSelectESSourceTemplatesForRelease = null;
	private PreparedStatement psSelectESModuleTemplatesForRelease = null;
	private PreparedStatement psSelectServiceTemplatesForRelease = null;
	private PreparedStatement psSelectModuleTemplatesForRelease = null;

	private PreparedStatement psSelectParametersForSuperId = null;
	private PreparedStatement psSelectPSetsForSuperId = null;
	private PreparedStatement psSelectVPSetsForSuperId = null;

	private PreparedStatement psSelectPSetId = null;
	private PreparedStatement psSelectEDSourceId = null;
	private PreparedStatement psSelectESSourceId = null;
	private PreparedStatement psSelectESModuleId = null;
	private PreparedStatement psSelectServiceId = null;
	private PreparedStatement psSelectSequenceId = null;
	private PreparedStatement psSelectTaskId = null; // BSATARIC: not used
	private PreparedStatement psSelectSwitchProducerId = null; // BSATARIC: not used
	private PreparedStatement psSelectPathId = null;
	private PreparedStatement psSelectModuleIdBySeq = null;
	private PreparedStatement psSelectModuleIdByPath = null;

	private PreparedStatement psSelectStreamByEventContent = null;
	private PreparedStatement psSelectStreamAssocByStream = null;
	private PreparedStatement psSelectECStatementByEventContent = null;
	private PreparedStatement psSelectECStatementByECStatement = null;

	private PreparedStatement psSelectTemplateId = null;

	private PreparedStatement psSelectReleaseCount = null;
	private PreparedStatement psSelectConfigurationCount = null;
	private PreparedStatement psSelectDirectoryCount = null;
	private PreparedStatement psSelectSuperIdCount = null;
	private PreparedStatement psSelectEDSourceTemplateCount = null;
	private PreparedStatement psSelectEDSourceCount = null;
	private PreparedStatement psSelectESSourceTemplateCount = null;
	private PreparedStatement psSelectESSourceCount = null;
	private PreparedStatement psSelectESModuleTemplateCount = null;
	private PreparedStatement psSelectESModuleCount = null;
	private PreparedStatement psSelectServiceTemplateCount = null;
	private PreparedStatement psSelectServiceCount = null;
	private PreparedStatement psSelectModuleTemplateCount = null;
	private PreparedStatement psSelectModuleCount = null;
	private PreparedStatement psSelectEDAliasCount = null;
	private PreparedStatement psSelectSequenceCount = null;
	private PreparedStatement psSelectTaskCount = null;
	private PreparedStatement psSelectSwitchProducerCount = null;
	private PreparedStatement psSelectPathCount = null;
	private PreparedStatement psSelectParameterCount = null;
	private PreparedStatement psSelectParameterSetCount = null;
	private PreparedStatement psSelectVecParameterSetCount = null;

	private PreparedStatement psSelectEventContentEntries = null;
	private PreparedStatement psSelectStreamEntries = null;
	private PreparedStatement psSelectEventContentStatements = null;

	private PreparedStatement psInsertDirectory = null;
	private PreparedStatement psInsertConfiguration = null;
	private PreparedStatement psInsertConfigurationVers = null;
	private PreparedStatement psFindConfiguration = null;
	private PreparedStatement psInsertConfigurationLock = null;
	// Insert Event Content
	private PreparedStatement psCheckContents = null;
	private PreparedStatement psInsertContents = null;
	private PreparedStatement psInsertContentIds = null;
	private PreparedStatement psInsertContentsConfigAssoc = null;
	private PreparedStatement psInsertEventContentStatements = null;
	private PreparedStatement psInsertStreams = null;
	private PreparedStatement psInsertStreamsIds = null;
	private PreparedStatement psInsertPrimaryDatasets = null;
	private PreparedStatement psInsertPrimaryDatasetIds = null;
	private PreparedStatement psInsertECStreamAssoc = null;
	private PreparedStatement psInsertPathStreamPDAssoc = null;
	private PreparedStatement psInsertStreamDatasetAssoc = null;
	private PreparedStatement psSelectStatementId = null;
	private PreparedStatement psSelectDatasetEntries = null;
	private PreparedStatement psInsertECStatementAssoc = null;
	private PreparedStatement psSelectPathStreamDatasetEntries = null;

	private PreparedStatement psInsertConfDone = null;
	private PreparedStatement psInsertConfProcessing = null;
	// Work on going

	private PreparedStatement psInsertSuperId = null;
	private PreparedStatement psInsertGlobalPSet = null;
	private PreparedStatement psInsertEDSource = null;
	private PreparedStatement psInsertConfigEDSourceAssoc = null;
	private PreparedStatement psInsertESSource = null;
	private PreparedStatement psInsertConfigESSourceAssoc = null;
	private PreparedStatement psInsertESModule = null;
	private PreparedStatement psInsertConfigESModuleAssoc = null;
	private PreparedStatement psInsertService = null;
	private PreparedStatement psInsertConfigServiceAssoc = null;
	private PreparedStatement psCheckPathName = null;
	private PreparedStatement psCheckPathNoum = null;
	private PreparedStatement psInsertPathNoum = null;
	private PreparedStatement psInsertPath = null;
	private PreparedStatement psInsertPathIds = null;
	private PreparedStatement psInsertConfigPathAssoc = null;
	private PreparedStatement psInsertHPathIds = null;
	private PreparedStatement psInsertHPathId2Path = null;
	private PreparedStatement psInsertHPathId2Uq = null;
	private PreparedStatement psSelectPathId2Uq = null;
	private PreparedStatement psCheckHPathIdCrc = null;
	private PreparedStatement psInsertConfigHPathAssoc = null;
	private PreparedStatement psInsertSequence = null;
	private PreparedStatement psInsertTask = null;
	private PreparedStatement psInsertConfigSequenceAssoc = null;
	private PreparedStatement psInsertConfigTaskAssoc = null;
	private PreparedStatement psInsertConfigSwitchProducerAssoc = null;
	private PreparedStatement psInsertModule = null;
	private PreparedStatement psInsertSequenceModuleAssoc = null;
	private PreparedStatement psInsertSequenceOutputModuleAssoc = null;
	private PreparedStatement psInsertTaskModuleAssoc = null;
	private PreparedStatement psInsertTaskOutputModuleAssoc = null;
	private PreparedStatement psInsertSwitchProducerModuleAssoc = null;
	private PreparedStatement psInsertPathPathAssoc = null;
	private PreparedStatement psInsertPathSequenceAssoc = null;
	private PreparedStatement psInsertSequenceSequenceAssoc = null;
	private PreparedStatement psInsertPathTaskAssoc = null;
	private PreparedStatement psInsertTaskTaskAssoc = null;
	private PreparedStatement psInsertPathSwitchProducerAssoc = null;
	private PreparedStatement psInsertPathModuleAssoc = null;
	private PreparedStatement psInsertPathOutputModuleAssoc = null;
	private PreparedStatement psInsertSuperIdReleaseAssoc = null;
	private PreparedStatement psInsertEDSourceT2Rele = null;
	private PreparedStatement psInsertESSourceT2Rele = null;
	private PreparedStatement psInsertESModuleT2Rele = null;
	private PreparedStatement psInsertServiceT2Rele = null;
	private PreparedStatement psInsertModuleT2Rele = null;
	private PreparedStatement psInsertServiceTemplate = null;
	private PreparedStatement psInsertEDSourceTemplate = null;
	private PreparedStatement psInsertESSourceTemplate = null;
	private PreparedStatement psInsertESModuleTemplate = null;
	private PreparedStatement psInsertModuleTemplate = null;
	private PreparedStatement psInsertGPset = null;
	private PreparedStatement psInsertParameterGPset = null;
	private PreparedStatement psInsertParameterEDS = null;
	private PreparedStatement psInsertParameterEDST = null;
	private PreparedStatement psInsertParameterESS = null;
	private PreparedStatement psInsertParameterESST = null;
	private PreparedStatement psInsertParameterESM = null;
	private PreparedStatement psInsertParameterESMT = null;
	private PreparedStatement psInsertParameterSRV = null;
	private PreparedStatement psInsertParameterSRVT = null;
	private PreparedStatement psInsertParameterMOE = null;
	private PreparedStatement psInsertParameterMODT = null;
	private PreparedStatement psInsertParameterOUTM = null;
	private PreparedStatement psInsertPathElement = null;
	private PreparedStatement psInsertPathElementAssoc = null;
	private PreparedStatement psInsertHPathElement = null;
	private PreparedStatement psInsertMoElement = null;
	private PreparedStatement psInsertMod2Templ = null;
	private PreparedStatement psInsertPae2Moe = null;
	private PreparedStatement psInsertParameterSet = null;
	private PreparedStatement psInsertVecParameterSet = null;
	private PreparedStatement psInsertSuperIdParamAssoc = null;
	private PreparedStatement psInsertSuperIdParamSetAssoc = null;
	private PreparedStatement psInsertSuperIdVecParamSetAssoc = null;
	private PreparedStatement psInsertBoolParamValue = null;
	private PreparedStatement psInsertInt32ParamValue = null;
	private PreparedStatement psInsertUInt32ParamValue = null;
	private PreparedStatement psInsertInt64ParamValue = null;
	private PreparedStatement psInsertUInt64ParamValue = null;
	private PreparedStatement psInsertDoubleParamValue = null;
	private PreparedStatement psInsertStringParamValue = null;
	private PreparedStatement psInsertEventIDParamValue = null;
	private PreparedStatement psInsertInputTagParamValue = null;
	private PreparedStatement psInsertESInputTagParamValue = null;
	private PreparedStatement psInsertFileInPathParamValue = null;
	private PreparedStatement psInsertVInt32ParamValue = null;
	private PreparedStatement psInsertVUInt32ParamValue = null;
	private PreparedStatement psInsertVInt64ParamValue = null;
	private PreparedStatement psInsertVUInt64ParamValue = null;
	private PreparedStatement psInsertVDoubleParamValue = null;
	private PreparedStatement psInsertVStringParamValue = null;
	private PreparedStatement psInsertVEventIDParamValue = null;
	private PreparedStatement psInsertVInputTagParamValue = null;
	private PreparedStatement psInsertVESInputTagParamValue = null;

	private PreparedStatement psDeleteDirectory = null;
	private PreparedStatement psDeleteLock = null;
	private PreparedStatement psDeleteConfiguration = null;
	private PreparedStatement psDeleteSoftwareRelease = null;

	private PreparedStatement psDeletePSetsFromConfig = null;
	private PreparedStatement psDeleteEDSourcesFromConfig = null;
	private PreparedStatement psDeleteESSourcesFromConfig = null;
	private PreparedStatement psDeleteESModulesFromConfig = null;
	private PreparedStatement psDeleteServicesFromConfig = null;
	private PreparedStatement psDeleteSequencesFromConfig = null;
	private PreparedStatement psDeleteTasksFromConfig = null;
	private PreparedStatement psDeleteSwitchProducersFromConfig = null;
	private PreparedStatement psDeletePathsFromConfig = null;
	private PreparedStatement psDeleteContentFromConfig = null;

	private PreparedStatement psDeleteChildSeqsFromParentSeq = null;
	private PreparedStatement psDeleteChildSeqFromParentSeqs = null;
	private PreparedStatement psDeleteChildSeqsFromParentPath = null;
	private PreparedStatement psDeleteChildSeqFromParentPaths = null;
	private PreparedStatement psDeleteChildPathsFromParentPath = null;
	private PreparedStatement psDeleteChildPathFromParentPaths = null;

	private PreparedStatement psDeleteChildTasksFromParentTask = null;
	private PreparedStatement psDeleteChildTaskFromParentTasks = null;
	private PreparedStatement psDeleteChildTasksFromParentSeq = null;
	private PreparedStatement psDeleteChildTaskFromParentSeqs = null;
	private PreparedStatement psDeleteChildTasksFromParentPath = null;
	private PreparedStatement psDeleteChildTaskFromParentPaths = null;

	private PreparedStatement psDeleteChildSwitchProducersFromParentTask = null;
	private PreparedStatement psDeleteChildSwitchProducerFromParentTasks = null;
	private PreparedStatement psDeleteChildSwitchProducersFromParentSeq = null;
	private PreparedStatement psDeleteChildSwitchProducerFromParentSeqs = null;
	private PreparedStatement psDeleteChildSwitchProducersFromParentPath = null;
	private PreparedStatement psDeleteChildSwitchProducerFromParentPaths = null;

	private PreparedStatement psDeletePathStreamDataSetAssoc = null;
	private PreparedStatement psDeletePathOutputModAssoc = null;

	private PreparedStatement psDeleteECStreamFromEventCont = null;
	private PreparedStatement psDeleteECStatementFromEventCont = null;
	private PreparedStatement psDeleteEventContentStatement = null;

	private PreparedStatement psDeleteModulesFromSeq = null;
	private PreparedStatement psDeleteModulesFromPath = null;

	private PreparedStatement psDeleteTemplateFromRelease = null;

	private PreparedStatement psDeleteParametersForSuperId = null;
	private PreparedStatement psDeletePSetsForSuperId = null;
	private PreparedStatement psDeleteVPSetsForSuperId = null;

	private PreparedStatement psDeleteSuperId = null;
	private PreparedStatement psDeleteParameter = null;
	private PreparedStatement psDeletePSet = null;
	private PreparedStatement psDeleteVPSet = null;
	private PreparedStatement psDeleteSequence = null;
	private PreparedStatement psDeleteTask = null;
	private PreparedStatement psDeleteSwitchProducer = null;
	private PreparedStatement psDeletePath = null;

	private CallableStatement csLoadTemplate = null;
	private CallableStatement csLoadTemplates = null;
	private CallableStatement csLoadTemplatesForConfig = null;
	private CallableStatement csLoadConfiguration = null;

	private PreparedStatement psSelectTemplates = null;
	private PreparedStatement psSelectInstances = null;
	private PreparedStatement psSelectParametersTemplates = null;
	private PreparedStatement psSelectParameters = null;
	private PreparedStatement psSelectBooleanValues = null;
	private PreparedStatement psSelectIntValues = null;
	private PreparedStatement psSelectRealValues = null;
	private PreparedStatement psSelectStringValues = null;
	private PreparedStatement psSelectCLOBsValues = null; // get Clobs - bug75950
	private PreparedStatement psSelectPathEntries = null;
	private PreparedStatement psSelectSeqTaskOrSPEntries = null;
	private PreparedStatement psSelectSequenceEntriesAndOperator = null; // bug #91797
	private PreparedStatement psPrepareSequenceEntries = null; // bug #91797
	private PreparedStatement psSelectTaskSProducerEntries = null;
	private PreparedStatement psSelectTaskEntriesAndOperator = null;
	private PreparedStatement psPrepareTaskEntries = null; //

	private PreparedStatement psSelectSoftwarePackageId = null;
	private PreparedStatement psInsertSoftwarePackage = null;

	private PreparedStatement psInsertReleaseTag = null;
	private PreparedStatement psSelectSoftwareSubsystemId = null;
	private PreparedStatement psInsertSoftwareSubsystem = null;
	private PreparedStatement psInsertEDSourceTemplateRelease = null;
	private PreparedStatement psInsertESSourceTemplateRelease = null;
	private PreparedStatement psInsertESModuleTemplateRelease = null;
	private PreparedStatement psInsertServiceTemplateRelease = null;
	private PreparedStatement psInsertModuleTemplateRelease = null;

	// New path fields extension:
	private PreparedStatement psCheckPathFieldsExistence = null;
	private PreparedStatement psSelectPathExtraFields = null;
	private PreparedStatement psInsertPathDescription = null;
	private PreparedStatement psUpdatePathDescription = null;

	// bug #91797: ConfDB operator IGNORE/NEGATE also for modules in a sequence.
	private PreparedStatement psCheckOperatorForModuleSequenceAssoc = null;
	private PreparedStatement psCheckOperatorForModuleTaskAssoc = null;

	private ArrayList<PreparedStatement> preparedStatements = new ArrayList<PreparedStatement>();

	//
	// construction
	//

	/** standard constructor */
	public ConfDB() {
		// template table name hash map
		templateTableNameHashMap = new HashMap<String, String>();
		templateTableNameHashMap.put("Service", tableServiceTemplates);
		templateTableNameHashMap.put("EDSource", tableEDSourceTemplates);
		templateTableNameHashMap.put("ESSource", tableESSourceTemplates);
		templateTableNameHashMap.put("ESModule", tableESModuleTemplates);
	}

	//
	// member functions
	//

	/** retrieve db url */
	public String dbUrl() {
		return this.dbUrl;
	}

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

	/**
	 * getTnsnameFormat
	 * ---------------------------------------------------------------- Use Oracle
	 * Java extensions to create TNSName Connection String bug 86323.
	 */
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

		// return getTnsnameFormat();
		return getDbURL(); // allow load balancing connection string.
	}

	// Set the URL. All other parameters will be ignored. This will be used for load
	// balancing.
	public String getDbURL() {

		String url = "jdbc:oracle:thin:@(DESCRIPTION =";
		String[] hosts = dbHost.split(",");

		if (hosts.length == 0)
			return "ERROR TNSNAME FORMAT";

		for (int i = 0; i < hosts.length; i++) {
			url += "(ADDRESS = (PROTOCOL = TCP)(HOST = " + hosts[i] + ")(PORT = " + dbPort + "))\n";
		}

		url += "(ENABLE=BROKEN) ";
		url += "(LOAD_BALANCE = yes) ";
		url += "(CONNECT_DATA = ";
		url += "  (SERVER = DEDICATED) ";
		url += " (SERVICE_NAME = " + dbName + ") ";
		url += "   (FAILOVER_MODE = (TYPE = SELECT)(METHOD = BASIC)(RETRIES = 200)(DELAY = 15)) ";
		url += ") )";

		return url;
	}

	public String getHostName() {
		return dbHost;
	}

	public String getPortNumber() {
		return dbPort;
	}

	public String getDbName() {
		return dbName;
	}

	public String getDbUser() {
		return dbUser;
	}

	// methods to retrieve db features:
	public boolean getExtraPathFieldsAvailability() {
		return extraPathFieldsAvailability;
	}

	public boolean getOperatorFieldForSequencesAvailability() {
		return operatorFieldForSequencesAvailability;
	}

	public boolean getOperatorFieldForTasksAvailability() {
		return operatorFieldForTasksAvailability;
	}

	public boolean getOperatorFieldForSwitchProducersAvailability() {
		return operatorFieldForSwitchProducersAvailability;
	}

	/** close all prepared statements */
	void closePreparedStatements() throws DatabaseException {
		for (PreparedStatement ps : preparedStatements) {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					throw new DatabaseException("ConfDB::closePreparedStatements() failed (SQL)", e);
				} catch (Exception e) {
					throw new DatabaseException("ConfDB::closePreparedStatements() failed", e);
				}
		}

		preparedStatements.clear();
	}

	/** connect to the database */
	public synchronized void connect(String dbType, String dbUrl, String dbUser, String dbPwrd)
			throws DatabaseException {
		this.dbType = dbType;
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPwrd = dbPwrd;
		if (dbType.equals(dbTypeMySQL))
			dbConnector = new MySQLDatabaseConnector(dbUrl, dbUser, dbPwrd);
		else if (dbType.equals(dbTypeOracle))
			dbConnector = new OracleDatabaseConnector(dbUrl, dbUser, dbPwrd);

		dbConnector.openConnection();
		prepareStatements();
		checkDbFeatures(); // Check Clobs and Documentation fields availability.
	}

	/** connect to the database */
	public synchronized void connect() throws DatabaseException {
		if (dbType.equals(dbTypeMySQL))
			dbConnector = new MySQLDatabaseConnector(dbUrl, dbUser, dbPwrd);
		else if (dbType.equals(dbTypeOracle))
			dbConnector = new OracleDatabaseConnector(dbUrl, dbUser, dbPwrd);

		dbConnector.openConnection();
		prepareStatements();
		checkDbFeatures(); // Check Clobs and Documentation fields availability.
	}

	/** connect to the database */
	public synchronized void connect(Connection connection) throws DatabaseException {
		this.dbType = dbTypeOracle;
		this.dbUrl = "UNKNOWN";
		dbConnector = new OracleDatabaseConnector(connection);
		prepareStatements();
		checkDbFeatures(); // Check Clobs and Documentation fields availability.
	}

	/** disconnect from database */
	public synchronized void disconnect() throws DatabaseException {
		if (dbConnector != null) {
			closePreparedStatements();
			dbConnector.closeConnection();
			dbConnector = null;
		}
	}

	/** reconnect to the database, if the connection appears to be down */
	public synchronized void reconnect() throws DatabaseException {
		if (dbConnector == null)
			return;
		ResultSet rs = null;

		int retryCount = 5;
		boolean transactionCompleted = false;
		do {
			Date dNow = new Date();
			SimpleDateFormat ft = new SimpleDateFormat("[yyyy.MM.dd@hh:mm:ss a]");

			if (retryCount != 5)
				System.err.println("[ConfDB::reconnect]" + ft.format(dNow) + " Trying to connect... attemp ("
						+ (5 - retryCount) + ")");

			try {
				rs = psSelectUsersForLockedConfigs.executeQuery();

				// If no exception is raised then reconnection is complete.
				transactionCompleted = true;
			} catch (SQLException e) {
				retryCount--;

				System.err.println("[ConfDB::reconnect]" + ft.format(dNow) + " SQLException: " + e.getMessage());
				System.err.println("[ConfDB::reconnect]" + ft.format(dNow) + " ErrorCode:    " + e.getErrorCode());

				if (!(dbConnector instanceof MySQLDatabaseConnector)
						&& !(dbConnector instanceof OracleDatabaseConnector))
					throw new DatabaseException("ConfDB::reconnect(): unknown connector type!", e);

				try {
					closePreparedStatements();
				} catch (DatabaseException dbe) {
					/* Ignore to reconnect */
					System.err.println("[Confdb::reconnect]" + ft.format(dNow) + " closePreparedStatements() FAILED!");
				}

				try {
					dbConnector.closeConnection();
				} catch (DatabaseException dbe) {
					/* Ignore to reconnect */
					System.err.println(
							"[Confdb::reconnect]" + ft.format(dNow) + " dbConnector.closeConnection() FAILED!");
				}

				try {
					dbConnector.openConnection();
				} catch (DatabaseException dbe) {
					/* Ignore to reconnect */
					System.err
							.println("[Confdb::reconnect]" + ft.format(dNow) + " dbConnector.openConnection() FAILED!");
				}

				try {
					prepareStatements();
				} catch (DatabaseException dbe) {
					/* Ignore to reconnect */
					System.err.println("[Confdb::reconnect]" + ft.format(dNow) + " prepareStatements() FAILED!");
				}

			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException sqlEx) {
						System.err.println("[ConfDB::reconnect()]" + ft.format(dNow) + " Error closing ResultSet! "
								+ sqlEx.getMessage());
					}
				}

				if (dbConnector != null)
					dbConnector.release(rs);
			}
		} while (!transactionCompleted && (retryCount > 0));

		// Raise exception when unable to connect.
		if (!transactionCompleted)
			throw new DatabaseException("ConfDB::reconnect(): Unable to connect!");
		else if (retryCount < 5)
			System.out.println("[ConfDB::reconnect] connection reestablished!");

	}

	public synchronized IDatabaseConnector getDbConnector() {
		return dbConnector;
	}

	/**
	 * Check database features. NOTE: Depending on this, database queries and app.
	 * workflow would be modified.
	 */
	public synchronized void checkDbFeatures() {
		try {
			extraPathFieldsAvailability = checkExtraPathFields();
			operatorFieldForSequencesAvailability = checkOperatorFieldForSequences();
			operatorFieldForTasksAvailability = checkOperatorFieldForTasks();
			operatorFieldForSwitchProducersAvailability = checkOperatorFieldForSwitchProducers();
		} catch (DatabaseException e) {
			e.printStackTrace(); // DEBUG
		}

	}

	/** list number of entries in (some) tables */
	public synchronized void listCounts() throws DatabaseException {
		reconnect();

		ResultSet rs = null;
		try {
			rs = psSelectReleaseCount.executeQuery();
			rs.next();
			int releaseCount = rs.getInt(1);
			rs = psSelectConfigurationCount.executeQuery();
			rs.next();
			int configurationCount = rs.getInt(1);
			rs = psSelectDirectoryCount.executeQuery();
			rs.next();
			int directoryCount = rs.getInt(1);
			rs = psSelectSuperIdCount.executeQuery();
			rs.next();
			int superIdCount = rs.getInt(1);
			rs = psSelectEDSourceTemplateCount.executeQuery();
			rs.next();
			int edsourceTemplateCount = rs.getInt(1);
			rs = psSelectEDSourceCount.executeQuery();
			rs.next();
			int edsourceCount = rs.getInt(1);
			rs = psSelectESSourceTemplateCount.executeQuery();
			rs.next();
			int essourceTemplateCount = rs.getInt(1);
			rs = psSelectESSourceCount.executeQuery();
			rs.next();
			int essourceCount = rs.getInt(1);
			rs = psSelectESModuleTemplateCount.executeQuery();
			rs.next();
			int esmoduleTemplateCount = rs.getInt(1);
			rs = psSelectESModuleCount.executeQuery();
			rs.next();
			int esmoduleCount = rs.getInt(1);
			rs = psSelectServiceTemplateCount.executeQuery();
			rs.next();
			int serviceTemplateCount = rs.getInt(1);
			rs = psSelectServiceCount.executeQuery();
			rs.next();
			int serviceCount = rs.getInt(1);
			rs = psSelectModuleTemplateCount.executeQuery();
			rs.next();
			int moduleTemplateCount = rs.getInt(1);
			rs = psSelectModuleCount.executeQuery();
			rs.next();
			int moduleCount = rs.getInt(1);
			rs = psSelectEDAliasCount.executeQuery();
			rs.next();
			int edAliasCount = rs.getInt(1);
			rs = psSelectSequenceCount.executeQuery();
			rs.next();
			int sequenceCount = rs.getInt(1);
			rs = psSelectTaskCount.executeQuery();
			rs.next();
			int taskCount = rs.getInt(1);
			rs = psSelectSwitchProducerCount.executeQuery();
			rs.next();
			int switchProducerCount = rs.getInt(1);
			rs = psSelectPathCount.executeQuery();
			rs.next();
			int pathCount = rs.getInt(1);
			rs = psSelectParameterCount.executeQuery();
			rs.next();
			int parameterCount = rs.getInt(1);
			rs = psSelectParameterSetCount.executeQuery();
			rs.next();
			int parameterSetCount = rs.getInt(1);
			rs = psSelectVecParameterSetCount.executeQuery();
			rs.next();
			int vecParameterSetCount = rs.getInt(1);

			System.out.println("\n" + "\nConfigurations: " + configurationCount + "\nReleases:       " + releaseCount
					+ "\nDirectories:    " + directoryCount + "\nSuperIds:       " + superIdCount + "\nEDSources (T):  "
					+ edsourceCount + " (" + edsourceTemplateCount + ")" + "\nESSources (T):  " + essourceCount + " ("
					+ essourceTemplateCount + ")" + "\nESModules (T):  " + esmoduleCount + " (" + esmoduleTemplateCount
					+ ")" + "\nServices (T):   " + serviceCount + " (" + serviceTemplateCount + ")"
					+ "\nModules (T):    " + moduleCount + " (" + moduleTemplateCount + ")" + "\nEDAliases:    "
					+ edAliasCount + "\nSwitch Producers:      " + switchProducerCount + "\nSequences:      "
					+ sequenceCount + "\nTasks:      " + taskCount + "\nPaths:          " + pathCount
					+ "\nParameters:     " + parameterCount + "\nPSets:          " + parameterSetCount
					+ "\nVPSets:         " + vecParameterSetCount + "\n");
		} catch (SQLException e) {
			String errMsg = "ConfDB::listCounts() failed:" + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** load information about all stored configurations */
	public synchronized Directory loadConfigurationTree() throws DatabaseException {
		reconnect();

		// System.err.println("loadConfigTree ");
		Directory rootDir = null;
		ResultSet rs = null;
		try {
			HashMap<Integer, Directory> directoryHashMap = new HashMap<Integer, Directory>();

			// DEBUG
			// long startTime = System.currentTimeMillis();

			rs = psSelectDirectories.executeQuery();
			// System.err.println("Selected Directories ");

			// DEBUG
			// long dir1Time = System.currentTimeMillis();

			while (rs.next()) {
				int dirId = rs.getInt(1);
				int parentDirId = rs.getInt(2);
				String dirName = rs.getString(3);
				String dirCreated = rs.getTimestamp(4).toString();

				// System.err.println("Retrieved Dirs: "+dirId+"- parent "+parentDirId+"
				// "+dirName+" "+dirCreated);
				if (directoryHashMap.size() == 0) {
					// System.err.println("New root DIrectrory: "+dirId+"- "+dirName+"
					// "+dirCreated);
					rootDir = new Directory(dirId, dirName, dirCreated, null);
					// System.err.println("New root DIrectrory done");
					directoryHashMap.put(dirId, rootDir);
				} else {
					// System.err.println("Now checking parenmt dir - "+parentDirId);

					if (!directoryHashMap.containsKey(parentDirId))
						throw new DatabaseException("parentDir not found in DB" + " (parentDirId=" + parentDirId + ")");
					// System.err.println("getting parentdir "+parentDirId);
					Directory parentDir = directoryHashMap.get(parentDirId);
					// System.err.println("New DIrectrory with parent: "+dirId+"- "+dirName+"
					// "+dirCreated+ parentDirId);
					Directory newDir = new Directory(dirId, dirName, dirCreated, parentDir);
					// System.err.println("New DIrectrory with parent - done");
					parentDir.addChildDir(newDir);
					// System.err.println("New DIrectrory - done adding child");
					directoryHashMap.put(dirId, newDir);
					// System.err.println("New DIrectrory - done putting hash");
				}
			}

			// DEBUG
			// long dir2Time = System.currentTimeMillis();

			// System.err.println("Done with Retrieving Dirs: ");
			// retrieve list of configurations for all directories
			HashMap<String, ConfigInfo> configHashMap = new HashMap<String, ConfigInfo>();

			// System.err.println("Try to query COnfs");

			rs = psSelectConfigurations.executeQuery();
			// System.err.println("Queried COnfs");

			// DEBUG
			// long config1Time = System.currentTimeMillis();

			while (rs.next()) {
				int configId = rs.getInt(1);
				int parentDirId = rs.getInt(2);
				String configName = rs.getString(3);
				int configVersion = rs.getInt(4);
				String configCreated = rs.getTimestamp(5).toString();
				String configCreator = rs.getString(6);
				String configReleaseTag = rs.getString(7);
				String configProcessName = rs.getString(8);
				String configComment = rs.getString(9);

				// System.err.println("Retrieved Conf: "+configName+" "+configCreated+"
				// "+configReleaseTag);
				if (configComment == null)
					configComment = "";

				Directory dir = directoryHashMap.get(parentDirId);
				if (dir == null) {
					String errMsg = "ConfDB::loadConfigurationTree(): can't find directory " + "for parentDirId="
							+ parentDirId + ".";
					throw new DatabaseException(errMsg);
				}

				String configPathAndName = dir.name() + "/" + configName;

				if (configHashMap.containsKey(configPathAndName)) {
					ConfigInfo configInfo = configHashMap.get(configPathAndName);
					configInfo.addVersion(configId, configVersion, configCreated, configCreator, configReleaseTag,
							configProcessName, configComment);
				} else {
					ConfigInfo configInfo = new ConfigInfo(configName, dir, configId, configVersion, configCreated,
							configCreator, configReleaseTag, configProcessName, configComment);
					configHashMap.put(configPathAndName, configInfo);
					dir.addConfigInfo(configInfo);
				}
				// System.err.println("ConfigHash done: "+configName+" "+configCreated+"
				// "+configReleaseTag);
			}

			rs = psSelectLockedConfigurations.executeQuery();

			while (rs.next()) {
				String dirName = rs.getString(1);
				String configName = rs.getString(2);
				String userName = rs.getString(3);
				String configPathAndName = dirName + "/" + configName;
				ConfigInfo configInfo = configHashMap.get(configPathAndName);
				if (configInfo == null) {
					String errMsg = "ConfDB::loadConfigurationTree(): can't find locked " + "configuration '"
							+ configPathAndName + "'.";
					throw new DatabaseException(errMsg);
				}
				configInfo.lock(userName);
			}

			// System.out.println("Config Lock done: ");

			// DEBUG
			// int config2Time = System.currentTimeMillis();
			// System.err.println("TIMING: "+
			// (config2Time-startTime)+": "+
			// (dir1Time-startTime)+" / "+
			// (dir2Time-dir1Time)+" / "+
			// (config1Time-dir2Time)+" / "+
			// (config2Time-config1Time));
		} catch (SQLException e) {
			String errMsg = "ConfDB::loadConfigurationTree() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}

		return rootDir;
	}

	/** load a single template from a certain release */
	public synchronized Template loadTemplate(String releaseTag, String templateName) throws DatabaseException {
		int releaseId = getReleaseId(releaseTag);
		SoftwareRelease release = new SoftwareRelease();
		release.clear(releaseTag);
		try {
			// System.err.println("loadTemplate with ReleaseTag: "+releaseId+"
			// "+templateName);
			csLoadTemplate.setInt(1, releaseId);
			csLoadTemplate.setString(2, templateName);
		} catch (SQLException e) {
			String errMsg = "ConfDB::loadTemplate(releaseTag=" + releaseTag + ",templateName=" + templateName
					+ ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}

		loadTemplates(csLoadTemplate, release);
		Iterator<Template> it = release.templateIterator();

		if (!it.hasNext()) {
			String errMsg = "ConfDB::loadTemplate(releaseTag=" + releaseTag + ",templateName=" + templateName
					+ "): template not found.";
			throw new DatabaseException(errMsg);
		}

		return it.next();
	}

	/** load a software release (all templates) */
	public synchronized void loadSoftwareRelease(int releaseId, SoftwareRelease release) throws DatabaseException {
		String releaseTag = getReleaseTag(releaseId);
		release.clear(releaseTag);
		try {
			// System.err.println("loadTemplates with ReleaseiD: "+releaseId+" "+release);
			csLoadTemplates.setInt(1, releaseId);
		} catch (SQLException e) {
			String errMsg = "ConfDB::loadSoftwareRelease(releaseId=" + releaseId + ",release) failed: "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		loadTemplates(csLoadTemplates, release);
	}

	/** load a software release (all templates) */
	public synchronized void loadSoftwareRelease(String releaseTag, SoftwareRelease release) throws DatabaseException {
		int releaseId = getReleaseId(releaseTag);
		// System.err.println("loadSoftwareRelease with releaseTag(rec ID):
		// "+releaseTag+" ("+releaseId+") "+release);
		loadSoftwareRelease(releaseId, release);
	}

	/** load a partial software release */
	public synchronized void loadPartialSoftwareRelease(int configId, SoftwareRelease release)
			throws DatabaseException {
		String releaseTag = getReleaseTagForConfig(configId);
		release.clear(releaseTag);

		try {
			// System.err.println("loadPartialSwRel configId: "+configId+" "+release);
			csLoadTemplatesForConfig.setInt(1, configId);
		} catch (SQLException e) {
			String errMsg = "ConfDB::loadPartialSoftwareRelease(configId=" + configId + ",release) failed: "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		loadTemplates(csLoadTemplatesForConfig, release);
	}

	/** load a partial software releaes */
	public synchronized void loadPartialSoftwareRelease(String configName, SoftwareRelease release)
			throws DatabaseException {
		int configId = getConfigNewId(configName);
		loadPartialSoftwareRelease(configId, release);
	}

	/** load a full software release, based on stored procedures */
	private void loadTemplates(CallableStatement cs, SoftwareRelease release) throws DatabaseException {
		reconnect();

		ResultSet rsTemplates = null;

		HashMap<Integer, SoftwarePackage> idToPackage = new HashMap<Integer, SoftwarePackage>();
		ArrayList<SoftwareSubsystem> subsystems = getSubsystems(idToPackage);

		// System.err.println("loadTemplates: release ");
		try {
			int releaseId = getReleaseId(release.releaseTag());
			System.out.println("LOADING TEMPLATE PARAMS");
			HashMap<Integer, ArrayList<Parameter>> templateParams = getParameters(-releaseId);

			// System.err.println("templateParams " + templateParams);

			psSelectTemplates.setInt(1, releaseId);
			psSelectTemplates.setInt(2, releaseId);
			psSelectTemplates.setInt(3, releaseId);
			psSelectTemplates.setInt(4, releaseId);
			psSelectTemplates.setInt(5, releaseId);

			rsTemplates = psSelectTemplates.executeQuery();

			// System.err.println("loadTemplates: gotTemplates ");

			while (rsTemplates.next()) {
				int id = rsTemplates.getInt(1);
				String type = rsTemplates.getString(2);
				String name = rsTemplates.getString(3);
				String cvstag = rsTemplates.getString(4);
				int pkgId = rsTemplates.getInt(5);

				// System.err.println("loadTemplates: id "+id+" type "+" name "+name+" cvstag
				// "+cvstag+" pkgid "+pkgId);
				// System.err.println("Template "+templateId+" "+templateName+" instance
				// "+instanceName);

				SoftwarePackage pkg = idToPackage.get(pkgId);
				if (pkg == null)
					System.err.println("pkg is NULL!");

				Template template = TemplateFactory.create(type, name, cvstag, null);
				if (template == null)
					System.err.println("template is NULL!");

				ArrayList<Parameter> params = templateParams.remove(id);

				if (params != null) {
					int missingCount = 0;
					Iterator<Parameter> it = params.iterator();
					while (it.hasNext()) {
						Parameter p = it.next();
						// System.err.println("param "+p.name());
						if (p == null)
							missingCount++;
					}
					if (missingCount > 0) {
						// TODO database check. This is happening.
						System.err.println("ERROR: " + missingCount + " parameter(s) " + "missing from "
								+ template.type() + " Template '" + template.name() + "'");
					} else {
						template.setParameters(params);
						if (pkg.templateCount() == 0)
							pkg.subsystem().addPackage(pkg);
						pkg.addTemplate(template);
					}
				} else {
					if (pkg.templateCount() == 0)
						pkg.subsystem().addPackage(pkg);
					pkg.addTemplate(template);
				}
				template.setDatabaseId(id);
			}

			for (SoftwareSubsystem s : subsystems) {
				if (s.packageCount() > 0) {
					s.sortPackages();
					release.addSubsystem(s);
				}
			}

		} catch (SQLException e) {
			String errMsg = "ConfDB::loadTemplates() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rsTemplates);
		}

		release.sortSubsystems();
		release.sortTemplates();
		// System.err.println("loadTemplates: done");
	}

	/** load a configuration & *all* release templates from the database */
	public synchronized Configuration loadConfiguration(int configId, SoftwareRelease release)
			throws DatabaseException {
		ConfigInfo configInfo = getConfigNewInfo(configId);
		// System.err.println("loadTemplates with configid: "+configId+" release
		// "+release);
		return loadConfiguration(configInfo, release);
	}

	/** load a configuration& *all* release templates from the database */
	public synchronized Configuration loadConfiguration(ConfigInfo configInfo, SoftwareRelease release)
			throws DatabaseException {
		String releaseTag = configInfo.releaseTag();

		// System.err.println("loadTemplates with configinfo: "+releaseTag+" release
		// "+release);
		if (releaseTag == null)
			System.out.println("releaseTag = " + releaseTag);
		if (release == null)
			System.out.println("release is null");
		else if (release.releaseTag() == null)
			System.out.println("WHAT?!");

		if (release == null || !releaseTag.equals(release.releaseTag()))
			loadSoftwareRelease(releaseTag, release);
		Configuration config = new Configuration(configInfo, release);
		loadConfiguration(config);
		config.setHasChanged(false);
		return config;
	}

	/** load configuration & *necessary* release templates from the database */
	public synchronized Configuration loadConfigurationWithOrigId(int configId) throws DatabaseException {
		reconnect();
		ResultSet rs = null;
		int newId = -1;
		// System.err.println("loadConfiguration with orig Id: "+configId);
		try {
			psSelectNewDbId.setInt(1, configId);
			rs = psSelectNewDbId.executeQuery();
			while (rs.next()) {
				newId = rs.getInt(1);
			}
			;
		} catch (SQLException e) {
			String errMsg = "ConfDB::loadConfigurationWithOrigId(Configuration config) failed " + "(OrigconfigId="
					+ configId + "): " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		;
		return loadConfiguration(newId);
	}

	/** load configuration & *necessary* release templates from the database */
	public synchronized Configuration loadConfiguration(int configId) throws DatabaseException {
		ConfigInfo configInfo = getConfigNewInfo(configId);
		String releaseTag = configInfo.releaseTag();
		SoftwareRelease release = new SoftwareRelease();
		release.clear(releaseTag);
		// System.err.println("loadTemplates with only configid: "+configId);
		loadPartialSoftwareRelease(configId, release);
		Configuration config = new Configuration(configInfo, release);
		loadConfiguration(config);
		config.setHasChanged(false);
		return config;
	}

	/** fill an empty configuration *after* template hash maps were filled! */
	private void loadConfiguration(Configuration config) throws DatabaseException {
		reconnect();

		int configId = config.dbId();
		Stack<Integer> idlifo = new Stack<Integer>();
		// System.err.println("loadConfiguration with config "+config);

		ResultSet rsInstances = null;

		ResultSet rsPathEntries = null;
		ResultSet rsSeqTasOrSPEntries = null;

		ResultSet rsEventContentEntries = null;
		ResultSet rsStreamEntries = null;
		ResultSet rsEventContentStatements = null;
		ResultSet rsDatasetEntries = null;
		ResultSet rsPathStreamDataset = null;

		ResultSet rsPath2Paes = null;
		ResultSet rsPaeElements = null;

		SoftwareRelease release = config.release();

		boolean pera = true;

		try {
			/*
			 * if (pera) return;
			 */
			// System.err.println("Trying rs instances"+configId);
			psSelectInstances.setInt(1, configId);
			psSelectInstances.setInt(2, configId);
			psSelectInstances.setInt(3, configId);
			psSelectInstances.setInt(4, configId);
			psSelectInstances.setInt(5, configId);
			psSelectInstances.setInt(6, configId);
			psSelectInstances.setInt(7, configId);
			psSelectInstances.setInt(8, configId);
			psSelectInstances.setInt(9, configId); // Sequence
			psSelectInstances.setInt(10, configId); // Sequence
			psSelectInstances.setInt(11, configId); // Tasks
			psSelectInstances.setInt(12, configId); // Tasks
			psSelectInstances.setInt(13, configId); // SwitchProducer
			psSelectInstances.setInt(14, configId); // SwitchProducer
			psSelectInstances.setInt(15, configId); // EDAliases
			psSelectInstances.setInt(16, configId); // EDAliases
			rsInstances = psSelectInstances.executeQuery(); // BSATARIC: get all instances for the same config
															// (sequences too)

			/*
			 * if (pera) return;
			 */

			psSelectPathEntries.setInt(1, configId);
			psSelectPathEntries.setInt(2, configId);
			// System.err.println("Trying Pathentries"+configId);
			rsPathEntries = psSelectPathEntries.executeQuery();

			psSelectSeqTaskOrSPEntries.setInt(1, configId);
			psSelectSeqTaskOrSPEntries.setInt(2, configId);
			rsSeqTasOrSPEntries = psSelectSeqTaskOrSPEntries.executeQuery();

			psSelectEventContentEntries.setInt(1, configId);
			rsEventContentEntries = psSelectEventContentEntries.executeQuery();
			psSelectStreamEntries.setInt(1, configId);
			rsStreamEntries = psSelectStreamEntries.executeQuery();
			psSelectDatasetEntries.setInt(1, configId);
			rsDatasetEntries = psSelectDatasetEntries.executeQuery();
			psSelectPathStreamDatasetEntries.setInt(1, configId);
			rsPathStreamDataset = psSelectPathStreamDatasetEntries.executeQuery();

			psSelectEventContentStatements.setInt(1, configId);
			rsEventContentStatements = psSelectEventContentStatements.executeQuery();

			HashMap<Integer, Stream> idToStream = new HashMap<Integer, Stream>();
			HashMap<Integer, PrimaryDataset> idToDataset = new HashMap<Integer, PrimaryDataset>();

			// System.err.println("getting params"+configId);
			System.out.println("loadConfiguration getParameters");
			HashMap<Integer, ArrayList<Parameter>> idToParams = getParameters(configId);
			// System.err.println("DOne params");

			HashMap<Integer, ModuleInstance> idToModules = new HashMap<Integer, ModuleInstance>();
			HashMap<Integer, EDAliasInstance> idToEDAliases = new HashMap<Integer, EDAliasInstance>();
			HashMap<Integer, Path> idToPaths = new HashMap<Integer, Path>();
			HashMap<Integer, Sequence> idToSequences = new HashMap<Integer, Sequence>();
			HashMap<Integer, Task> idToTasks = new HashMap<Integer, Task>();
			HashMap<Integer, SwitchProducer> idToSwitchProducers = new HashMap<Integer, SwitchProducer>();

			HashMap<EventContent, Integer> eventContentToId = new HashMap<EventContent, Integer>();
			HashMap<Stream, Integer> streamToId = new HashMap<Stream, Integer>();
			HashMap<PrimaryDataset, Integer> primaryDatasetToId = new HashMap<PrimaryDataset, Integer>();
			HashMap<Path, Integer> pathToId = new HashMap<Path, Integer>();
			HashMap<Sequence, Integer> sequenceToId = new HashMap<Sequence, Integer>();
			HashMap<Task, Integer> taskToId = new HashMap<Task, Integer>();
			HashMap<SwitchProducer, Integer> switchProducerToId = new HashMap<SwitchProducer, Integer>();

			while (rsInstances.next()) { // BSATARIC: populate configuration through instances loop
				int id = rsInstances.getInt(1); // BSATARIC: he literally takes return result as parameters 1,2,3 (type)
				int templateId = rsInstances.getInt(2);
				String type = rsInstances.getString(3);
				String instanceName = rsInstances.getString(4);
				boolean flag = rsInstances.getBoolean(5);

				System.out.println("****INSTANCE NAME: *****" + instanceName);
				System.out.println("****INSTANCE TYPE: *****" + type);

				String templateName = null;

				// System.err.println("found instance "+id+"name="+instanceName+"
				// templateid="+templateId+" entryType="+type);
				if (type.equals("PSet")) {
					PSetParameter pset = (PSetParameter) ParameterFactory.create("PSet", instanceName, "", flag);
					config.insertPSet(pset);
					ArrayList<Parameter> psetParams = idToParams.remove(id);
					if (psetParams != null) {
						Iterator<Parameter> it = psetParams.iterator();
						while (it.hasNext()) {
							Parameter p = it.next();
							if (p != null)
								pset.addParameter(p);
						}
					} else
						System.out.println("Found null PSet for instance " + instanceName + " " + id + "  templateid="
								+ templateId + " entryType=" + type);
					config.psets().setDatabaseId(1);
				} else if (type.equals("EDSource")) {
					templateName = release.edsourceTemplateName(templateId);
					Instance edsource = config.insertEDSource(templateName);
					edsource.setDatabaseId(id);
					updateInstanceParameters(edsource, idToParams.remove(id));

				} else if (type.equals("ESSource")) {
					int insertIndex = config.essourceCount();
					templateName = release.essourceTemplateName(templateId);
					ESSourceInstance essource = config.insertESSource(insertIndex, templateName, instanceName);
					essource.setPreferred(flag);
					essource.setDatabaseId(id);
					updateInstanceParameters(essource, idToParams.remove(id));
				} else if (type.equals("ESModule")) {
					int insertIndex = config.esmoduleCount();
					templateName = release.esmoduleTemplateName(templateId);
					ESModuleInstance esmodule = config.insertESModule(insertIndex, templateName, instanceName);
					esmodule.setPreferred(flag);
					esmodule.setDatabaseId(id);
					updateInstanceParameters(esmodule, idToParams.remove(id));
				} else if (type.equals("Service")) {
					int insertIndex = config.serviceCount();
					templateName = release.serviceTemplateName(templateId);
					Instance service = config.insertService(insertIndex, templateName);
					service.setDatabaseId(id);
					updateInstanceParameters(service, idToParams.remove(id));
				} else if (type.equals("Module")) {
					templateName = release.moduleTemplateName(templateId);
					ModuleInstance module = config.insertModule(templateName, instanceName);
					module.setDatabaseId(id);
					updateInstanceParameters(module, idToParams.remove(id));
					idToModules.put(id, module);
				} else if (type.equals("EDAlias")) { // BSATARIC: EDALIAS cannot have template
					EDAliasInstance edAlias = config.insertEDAlias(instanceName);
					edAlias.setDatabaseId(id);
					updateInstanceParameters(edAlias, idToParams.remove(id)); // TODO: check module parameters
					idToEDAliases.put(id, edAlias);
				} else if (type.equals("Path")) {

					int insertIndex = config.pathCount();
					Path path = config.insertPath(insertIndex, instanceName);

					/*
					 * if(extraPathFieldsAvailability) { String pathDesc = null; String pathCont =
					 * null; psSelectPathExtraFields.setInt(1, id); ResultSet pathDescription =
					 * psSelectPathExtraFields.executeQuery();
					 * 
					 * while (pathDescription.next()) { pathDesc = pathDescription.getString(2);
					 * pathCont = pathDescription.getString(3); } path.setDescription(pathDesc);
					 * path.setContacts(pathCont); }
					 */

					String pathDesc = null;
					String pathCont = null;

					pathDesc = rsInstances.getString(7);
					pathCont = rsInstances.getString(8);

					path.setDescription(pathDesc);
					path.setContacts(pathCont);

					path.setAsEndPath(flag);
					path.setDatabaseId(id);
					idToPaths.put(id, path);
				} else if (type.equals("Sequence")) {
					System.out.println("SEQUENCE LOADED! ID: " + id);
					int insertIndex = config.sequenceCount(); // BSATARIC: it's only previous sequence count
					Sequence sequence = config.insertSequence(insertIndex, instanceName);
					sequence.setDatabaseId(id);
					idToSequences.put(id, sequence);
				} else if (type.equals("Task")) {
					System.out.println("TASK LOADED! ID: " + id);
					int insertIndex = config.taskCount();
					Task task = config.insertTask(insertIndex, instanceName);
					task.setDatabaseId(id);
					idToTasks.put(id, task);
				} else if (type.equals("SwitchProducer")) {
					System.out.println("SWITCHPRODUCER LOADED! ID: " + id);
					int insertIndex = config.switchProducerCount();
					SwitchProducer switchProducer = config.insertSwitchProducer(insertIndex, instanceName);
					switchProducer.setDatabaseId(id);
					idToSwitchProducers.put(id, switchProducer);
				}
			}

			while (rsEventContentEntries.next()) {
				int eventContentId = rsEventContentEntries.getInt(1);
				String name = rsEventContentEntries.getString(2);
				// System.err.println("Evco id "+eventContentId+" name "+name);
				EventContent eventContent = config.insertContent(name);
				if (eventContent == null)
					continue;
				eventContent.setDatabaseId(eventContentId);
				eventContentToId.put(eventContent, eventContentId);

			}

			while (rsStreamEntries.next()) {
				int streamId = rsStreamEntries.getInt(1);
				String streamLabel = rsStreamEntries.getString(2);
				Double fracToDisk = rsStreamEntries.getDouble(3);
				int eventContentId = rsStreamEntries.getInt(4);
				String name = rsStreamEntries.getString(5);
				EventContent eventContent = config.content(name);
				// System.err.println("Stream id "+streamId+" Label "+streamLabel+" fracTo "+
				// fracToDisk+" Evco id "+eventContentId+" name "+name);
				if (eventContent == null)
					continue;

				// System.err.println("Adding Stream id "+streamId+" Label "+streamLabel+"
				// fracTo "+ fracToDisk+" Evco id "+eventContentId+" name "+name);

				Stream stream = eventContent.insertStream(streamLabel);
				stream.setFractionToDisk(fracToDisk);
				stream.setDatabaseId(streamId - 50000000);
				eventContent.setDatabaseId(eventContentId);
				streamToId.put(stream, streamId);
				idToStream.put(streamId, stream);
				ArrayList<Parameter> parameters = idToParams.remove(streamId);
				if (parameters == null)
					continue;
				Iterator<Parameter> it = parameters.iterator();
				OutputModule outputModule = stream.outputModule();

				while (it.hasNext()) {
					Parameter p = it.next();
					if (p == null)
						continue;
					/*
					 * if (p.type().equals("vstring")) { VStringParameter p2=(VStringParameter) p;
					 * System.out.println(" Param "+p2.type()+" name "+p2.name()+" val "+p2.
					 * valueAsString()+" size "+p2.vectorSize()); if (p2.vectorSize()>0) for (int
					 * x=0;x<p2.vectorSize();x++) System.out.println(" vstringd values " + x + " "
					 * +p2.value(x)); }
					 */

					outputModule.updateParameter(p.name(), p.type(), p.valueAsString());

					/*
					 * if (p.type().equals("vstring")) { VStringParameter p2 = (VStringParameter)
					 * outputModule.parameter(p.name());
					 * //System.err.println(" Reread Param "+p2.type()+" name "+p2.name()+" val "+p2
					 * .valueAsString()+" size "+p2.vectorSize()); if (p2.vectorSize()>0) for (int
					 * x=0;x<p2.vectorSize();x++) //System.err.println(" reread values " + x + " "
					 * +p2.value(x)); }
					 */
				}
				outputModule.setDatabaseId(streamId);
			}

			int previouslvl = 0;
			boolean seqTasOrSPToSkip = false;
			int lvltoskip = 0;
			boolean seqTaskOrSPDone[];
			seqTaskOrSPDone = new boolean[5000000];
			while (rsSeqTasOrSPEntries.next()) { // BSATARIC: I guess list of all sequences in configuration
				// System.out.println("SEQUENCE LOADING...");

				int seqTaskOrSPId = rsSeqTasOrSPEntries.getInt(1);
				int entryLvl = rsSeqTasOrSPEntries.getInt(3);
				int entryId = rsSeqTasOrSPEntries.getInt(4);
				int sequenceNb = rsSeqTasOrSPEntries.getInt(5);
				String entryType = rsSeqTasOrSPEntries.getString(6);

				// if (entryLvl == 0)

				System.out.println("PATH ID " + seqTaskOrSPId + " lvl = " + entryLvl + " entryId " + entryId + " ord "
						+ sequenceNb + " entryType = " + entryType);

				System.out.println("PREVIOUS LEVEL: " + previouslvl);

				while (entryLvl < previouslvl) { // next sequence/task/switch producer in the entries (zero level)
					if ((!seqTasOrSPToSkip) && (entryLvl >= lvltoskip)) {
						idlifo.pop();
						// System.out.println("POPPED seqTaskOrSPId: " + entryId + " parentId " +
						// seqTaskOrSPId);
					}
					previouslvl--;
					if (previouslvl < lvltoskip) {
						seqTasOrSPToSkip = false;
						lvltoskip = 0;
					}
				}
				previouslvl = entryLvl;
				// if (entryLvl<lvltoskip) {
				// seqTasOrSPToSkip=false;
				// lvltoskip=0;
				// }

				if (entryLvl == 0) {
					if (seqTaskOrSPDone[entryId]) {
						seqTasOrSPToSkip = true;
						lvltoskip = 1;
					} else {
						seqTaskOrSPDone[entryId] = true;
						idlifo.push(entryId);
						previouslvl++;
						lvltoskip = 1;
						if (entryType.equals("Sequence")) {
							Sequence entry = idToSequences.get(entryId);

							System.out.println("PUSHED sequenceID: " + entryId + " parentId " + seqTaskOrSPId
									+ " sequenceName " + entry.name());

							entry.setDatabaseId(seqTaskOrSPId);
							sequenceToId.put(entry, seqTaskOrSPId);
						} else if (entryType.equals("Task")) {
							Task entry = idToTasks.get(entryId);

							System.out.println("PUSHED taskID: " + entryId + " parentId " + seqTaskOrSPId + " taskName "
									+ entry.name());

							entry.setDatabaseId(seqTaskOrSPId);
							taskToId.put(entry, seqTaskOrSPId);
						} else if (entryType.equals("SwitchProducer")) {
							SwitchProducer entry = idToSwitchProducers.get(entryId);

							System.out.println("PUSHED switchProducerID: " + entryId + " parentId " + seqTaskOrSPId
									+ " switchProducerName " + entry.name());

							entry.setDatabaseId(seqTaskOrSPId);
							// System.out.println("PUTTTTTTTTTTTTTTTTTTTTTTTTT" + seqTaskOrSPId);
							switchProducerToId.put(entry, seqTaskOrSPId);
						}
					}
				}
				if ((entryLvl > 0) && (!seqTasOrSPToSkip)) { // Nested entries
					seqTaskOrSPId = idlifo.peek();

					Sequence sequence = idToSequences.get(seqTaskOrSPId);
					Task task = idToTasks.get(seqTaskOrSPId);
					SwitchProducer switchProducer = idToSwitchProducers.get(seqTaskOrSPId);

					System.out.println("********* NESTED ENTRIES START ***********");
					System.out.println("* SEQUENCE: " + sequence);
					System.out.println("* TASK: " + task);
					System.out.println("* SP: " + switchProducer);
					System.out.println("********* NESTED ENTRIES END ***********");

					if (sequence != null) { // if there is sequence with this ID - otherwise it is a task
						int index = sequence.entryCount();
						sequenceNb = index;

						boolean fail = true;
						Operator operator = Operator.DEFAULT;
						try {
							// operator = Operator.getOperator( rsSeqTasOrSPEntries.getInt(5) );
							// if(operatorFieldForSequencesAvailability)
							operator = Operator.getOperator(rsSeqTasOrSPEntries.getInt(7));
							fail = false;
						} catch (SQLException e) {
							operator = Operator.DEFAULT;
							fail = true;

							System.out.println("SQLException catched at confDb.java::loadConfiguration()   sequence = "
									+ sequence.name());
						}

						if (index != sequenceNb)
							System.err.println("ERROR in sequence " + sequence.name() + ": index=" + index
									+ " sequenceNb=" + sequenceNb);

						System.out.println("ENTRY TYPEEEEEEEEEEEEEEEEEEEEEEEE: " + entryType);
						if (entryType.equals("Sequence")) {
							if (seqTaskOrSPDone[entryId]) {
								seqTasOrSPToSkip = true;
								lvltoskip = entryLvl + 1;
							} else {
								idlifo.push(entryId);
								System.out
										.println("PUSHED NESTED sequenceId: " + entryId + " parentId " + seqTaskOrSPId);
								previouslvl++;
							}

							// if (!seqTasOrSPToSkip) {
							seqTaskOrSPDone[entryId] = true;
							Sequence entry = idToSequences.get(entryId);
							if (entry == null) {
								System.err.println("ERROR: can't find sequence for " + "id=" + entryId
										+ " expected as daughter " + index + " of sequence " + sequence.name());
							}
							config.insertSequenceReference(sequence, index, entry).setOperator(operator);
							// }
						} else if (entryType.equals("Task")) { // treat nested task inside sequence in the same way as
																// nested sequence
							if (seqTaskOrSPDone[entryId]) {
								seqTasOrSPToSkip = true;
								lvltoskip = entryLvl + 1;
							} else {
								idlifo.push(entryId);
								System.out.println("PUSHED NESTED taskID: " + entryId + " parentId " + seqTaskOrSPId);
								previouslvl++;
							}

							// if (!seqTasOrSPToSkip) {
							seqTaskOrSPDone[entryId] = true;
							Task entry = (Task) idToTasks.get(entryId);
							if (entry == null) {
								System.err.println("ERROR: can't find task for " + "id=" + entryId
										+ " expected as daughter " + index + " of sequence " + sequence.name());
							}
							config.insertTaskReference(sequence, index, entry).setOperator(operator);
						} else if (entryType.equals("SwitchProducer")) {
							if (seqTaskOrSPDone[entryId]) {
								seqTasOrSPToSkip = true;
								lvltoskip = entryLvl + 1;
							} else {
								idlifo.push(entryId);
								System.out.println(
										"PUSHED NESTED switchProducerId: " + entryId + " parentId " + seqTaskOrSPId);
								previouslvl++;
							}
							seqTaskOrSPDone[entryId] = true;
							SwitchProducer entry = (SwitchProducer) idToSwitchProducers.get(entryId);
							if (entry == null) {
								System.err.println("ERROR: can't find switch producer for " + "id=" + entryId
										+ " expected as daughter " + index + " of sequence " + sequence.name());
							}
							// System.err.println("Module "+entryId+" seq "+sequence+" index "+index);
							config.insertSwitchProducerReference(sequence, index, entry).setOperator(operator);
						} else if (entryType.equals("Module")) {
							ModuleInstance entry = (ModuleInstance) idToModules.get(entryId);
							// System.err.println("Module "+entryId+" seq "+sequence+" index "+index);
							config.insertModuleReference(sequence, index, entry).setOperator(operator);
						} else if (entryType.equals("OutputModule")) {
							Stream entry = (Stream) idToStream.get(entryId);
							if (entry == null) {
								System.err.println("ERROR: can't find stream for entryid");
								continue;
							}
							OutputModule referencedOutput = entry.outputModule();
							if (referencedOutput == null)
								continue;
							config.insertOutputModuleReference(sequence, index, referencedOutput).setOperator(operator);
						} else
							System.err.println("Invalid entryType '" + entryType + "'");

						sequence.setDatabaseId(seqTaskOrSPId);
						sequenceToId.put(sequence, seqTaskOrSPId);
					} else if (task != null) { // BSATARIC TASKS

						int index = task.entryCount();
						sequenceNb = index;

						boolean fail = true;
						Operator operator = Operator.DEFAULT;
						try {
							// operator = Operator.getOperator( rsSeqTasOrSPEntries.getInt(5) );
							// if(operatorFieldForSequencesAvailability)
							operator = Operator.getOperator(rsSeqTasOrSPEntries.getInt(7));
							fail = false;
						} catch (SQLException e) {
							operator = Operator.DEFAULT;
							fail = true;

							System.out.println(
									"SQLException catched at confDb.java::loadConfiguration()   task = " + task.name());
						}

						if (index != sequenceNb)
							System.err.println(
									"ERROR in task " + task.name() + ": index=" + index + " sequenceNb=" + sequenceNb);

						if (entryType.equals("Task")) { // treat nested task inside task in the same way as nested
														// sequence
							if (seqTaskOrSPDone[entryId]) {
								seqTasOrSPToSkip = true;
								lvltoskip = entryLvl + 1;
							} else {
								idlifo.push(entryId);
								System.out.println("PUSHED NESTED taskId: " + entryId + " parentId " + seqTaskOrSPId);
								previouslvl++;
							}

							// if (!seqTasOrSPToSkip) {
							seqTaskOrSPDone[entryId] = true;
							Task entry = idToTasks.get(entryId);
							if (entry == null) {
								System.err.println("ERROR: can't find task for " + "id=" + entryId
										+ " expected as daughter " + index + " of task " + task.name());
							}
							config.insertTaskReference(task, index, entry).setOperator(operator);
							// }
						} else if (entryType.equals("SwitchProducer")) {

							if (seqTaskOrSPDone[entryId]) {
								seqTasOrSPToSkip = true;
								lvltoskip = entryLvl + 1;
							} else {
								idlifo.push(entryId);
								System.out.println(
										"PUSHED NESTED switchProducerId: " + entryId + " parentId " + seqTaskOrSPId);
								previouslvl++;
							}
							seqTaskOrSPDone[entryId] = true;
							SwitchProducer entry = (SwitchProducer) idToSwitchProducers.get(entryId);
							if (entry == null) {
								System.err.println("ERROR: can't find switch producer for " + "id=" + entryId
										+ " expected as daughter " + index + " of task " + task.name());
							}
							config.insertSwitchProducerReference(task, index, entry).setOperator(operator);
						} else if (entryType.equals("Module")) {
							ModuleInstance entry = (ModuleInstance) idToModules.get(entryId);
							config.insertModuleReference(task, index, entry).setOperator(operator);
						} else if (entryType.equals("OutputModule")) {
							Stream entry = (Stream) idToStream.get(entryId);
							if (entry == null) {
								System.err.println("ERROR: can't find stream for entryid");
								continue;
							}
							OutputModule referencedOutput = entry.outputModule();
							if (referencedOutput == null)
								continue;
							config.insertOutputModuleReference(task, index, referencedOutput).setOperator(operator);
						} else
							System.err.println("Invalid entryType '" + entryType + "'");

						task.setDatabaseId(seqTaskOrSPId);
						taskToId.put(task, seqTaskOrSPId);
					} else if (switchProducer != null) { // BSATARIC SWITCHPRODUCERS

						int index = switchProducer.entryCount();
						sequenceNb = index;

						boolean fail = true;
						Operator operator = Operator.DEFAULT;
						try {
							// operator = Operator.getOperator( rsSeqTasOrSPEntries.getInt(5) );
							// if(operatorFieldForSequencesAvailability)
							operator = Operator.getOperator(rsSeqTasOrSPEntries.getInt(7));
							fail = false;
						} catch (SQLException e) {
							operator = Operator.DEFAULT;
							fail = true;

							System.out.println(
									"SQLException catched at confDb.java::loadConfiguration()   switchProducer = "
											+ switchProducer.name());
						}

						if (index != sequenceNb)
							System.err.println("ERROR in switchProducer " + switchProducer.name() + ": index=" + index
									+ " sequenceNb=" + sequenceNb);

						if (entryType.equals("Module")) {
							ModuleInstance entry = (ModuleInstance) idToModules.get(entryId);
							config.insertModuleReference(switchProducer, index, entry).setOperator(operator);
						} else if (entryType.equals("EDAlias")) {
							System.out.println("idToEDAliases " + idToEDAliases);
							EDAliasInstance entry = (EDAliasInstance) idToEDAliases.get(entryId);
							System.out.println("SWITCHPRODUCER: " + switchProducer);
							System.out.println("EDALIAS: " + entry);
							config.insertEDAliasReference(switchProducer, index, entry).setOperator(operator);
						} else
							System.err.println("Invalid entryType '" + entryType + "'");

						switchProducer.setDatabaseId(seqTaskOrSPId);
						System.out.println("PUTTTTTTTTTTTTTTTTTTTTTTTTT" + seqTaskOrSPId);
						switchProducerToId.put(switchProducer, seqTaskOrSPId);
					}
				}
			}

			while (rsPathEntries.next()) {
				int pathId = rsPathEntries.getInt(1);
				int entryId = rsPathEntries.getInt(2);
				int sequenceNb = rsPathEntries.getInt(3);
				String entryType = rsPathEntries.getString(4);
				Operator operator = Operator.getOperator(rsPathEntries.getInt(5));

				Path path = idToPaths.get(pathId);
				int index = path.entryCount();

				// System.err.println("found n path "+path.name()+": "+
				// "index="+index+" sequenceNb="+sequenceNb+" entryType="+entryType+"
				// entryId="+entryId);

				if (index != sequenceNb)
					System.err.println(
							"ERROR in path " + path.name() + ": " + "index=" + index + " sequenceNb=" + sequenceNb);

				if (entryType.equals("Path")) {
					Path entry = idToPaths.get(entryId);
					config.insertPathReference(path, index, entry).setOperator(operator);
				} else if (entryType.equals("Sequence")) {
					Sequence entry = idToSequences.get(entryId);
					config.insertSequenceReference(path, index, entry).setOperator(operator);
				} else if (entryType.equals("Task")) {
					Task entry = idToTasks.get(entryId);
					config.insertTaskReference(path, index, entry).setOperator(operator);
				} else if (entryType.equals("SwitchProducer")) {
					SwitchProducer entry = (SwitchProducer) idToSwitchProducers.get(entryId);
					// System.err.println("Module "+entryId+" seq "+sequence+" index "+index);
					config.insertSwitchProducerReference(path, index, entry).setOperator(operator);
				} else if (entryType.equals("Module")) {
					ModuleInstance entry = (ModuleInstance) idToModules.get(entryId);
					config.insertModuleReference(path, index, entry).setOperator(operator);
				} else if (entryType.equals("OutputModule")) {
					Stream entry = (Stream) idToStream.get(entryId);
					if (entry == null) {
						System.err.println("noStreamId for " + path.name());
						continue;
					}
					// if(entry==null) {System.err.println("noStreamId for "+path.name());}
					// else {
					OutputModule referencedOutput = entry.outputModule();
					if (referencedOutput == null) {
						System.err.println("nooutputModule for " + path.name());
						continue;
					}
					// if (referencedOutput==null) {System.err.println("nooutputModule for
					// "+path.name());}
					// else
					config.insertOutputModuleReference(path, index, referencedOutput).setOperator(operator);
					// }
				} else
					System.err.println("Invalid entryType '" + entryType + "'");

				path.setDatabaseId(pathId);
				pathToId.put(path, pathId);
			}

			while (rsDatasetEntries.next()) {
				int datasetId = rsDatasetEntries.getInt(1);
				String datasetLabel = rsDatasetEntries.getString(2);
				int streamId = rsDatasetEntries.getInt(3);
				String streamLabel = rsDatasetEntries.getString(4);
				Stream stream = idToStream.get(streamId);
				// System.err.println("Dataset entry label "+datasetLabel+" id "+datasetId+"
				// stream "+streamLabel+" streamid "+streamId);
				if ((stream == null) || (datasetLabel.equals("Unassigned path")))
					continue;

				// System.err.println("Adding dataset "+datasetLabel+" id "+datasetId+" stream
				// "+streamLabel+" streamid "+streamId);
				PrimaryDataset primaryDataset = stream.insertDataset(datasetLabel);
				primaryDataset.setDatabaseId(datasetId);
				idToDataset.put(datasetId, primaryDataset);
				primaryDatasetToId.put(primaryDataset, datasetId);
			}

			// TODO the key code is here. This should build only one Stream
			// sharing the Paths between more PrimaryDatasets for every pair
			// (streamId,datasetId,PathId).
			while (rsPathStreamDataset.next()) {
				int pathId = rsPathStreamDataset.getInt(1);
				int streamId = rsPathStreamDataset.getInt(2);
				int datasetId = rsPathStreamDataset.getInt(3);

				Path path = idToPaths.get(pathId);
				Stream stream = idToStream.get(streamId);
				PrimaryDataset primaryDataset = idToDataset.get(datasetId);

				// System.err.println("Path to dataset "+datasetId+" id "+pathId+" streamid
				// "+streamId);
				if (path == null)
					continue;
				if (stream == null)
					continue;

				EventContent eventContent = stream.parentContent();
				stream.insertPath(path);
				path.addToContent(eventContent);

				// System.err.println("Adding Path to dataset "+datasetId+" id "+pathId+"
				// streamid "+streamId);

				if (primaryDataset == null)
					continue;
				// System.err.println("Adding Path to dataset "+datasetId+" id "+pathId+"
				// streamid "+streamId);
				primaryDataset.insertPath(path);

				stream.setDatabaseId(streamId);
				primaryDataset.setDatabaseId(datasetId);
			}

			// read content statements last since paths need to be registered!
			while (rsEventContentStatements.next()) {
				int statementId = rsEventContentStatements.getInt(1);
				String classN = rsEventContentStatements.getString(2);
				String module = rsEventContentStatements.getString(3);
				String extra = rsEventContentStatements.getString(4);
				String process = rsEventContentStatements.getString(5);
				int statementType = rsEventContentStatements.getInt(6);
				int eventContentId = rsEventContentStatements.getInt(7);
				int statementRank = rsEventContentStatements.getInt(8);
				String name = rsEventContentStatements.getString(9);
				int parentPathId = rsEventContentStatements.getInt(10);

				EventContent eventContent = config.content(name);

				OutputCommand outputCommand = new OutputCommand();
				String commandToString = classN + "_" + module + "_" + extra + "_" + process;

				if (statementType == 0) {
					commandToString = "drop " + commandToString;
				} else {
					commandToString = "keep " + commandToString;
				}

				outputCommand.initializeFromString(commandToString); // Bug

				if (parentPathId > 0) {

					Path parentPath = idToPaths.get(parentPathId);
					if (parentPath == null)
						continue;
					Iterator<Reference> itR = parentPath.recursiveReferenceIterator();
					boolean found = false;
					Reference parentReference = null;
					while (itR.hasNext() && !found) {
						parentReference = itR.next();
						if (parentReference.name().equals(module))
							found = true;
					}

					if (found) {
						outputCommand = new OutputCommand(parentPath, parentReference);

						// BUG bug #88643 //TODO
						// Load the values

						outputCommand.setClassName(classN);
						outputCommand.setExtraName(extra);
						outputCommand.setModuleName(module);
						outputCommand.setProcessName(process);

					}
				}

				eventContent.insertCommand(outputCommand);
			}

			Iterator<EventContent> contentIt = config.contentIterator();
			while (contentIt.hasNext()) {
				EventContent eventContent = contentIt.next();
				int databaseId = eventContentToId.get(eventContent);
				eventContent.setDatabaseId(databaseId);
			}

			Iterator<Stream> streamIt = config.streamIterator();
			while (streamIt.hasNext()) {
				Stream stream = streamIt.next();
				int databaseId = streamToId.get(stream);
				if (databaseId > 5000000)
					databaseId -= 5000000;
				stream.setDatabaseId(databaseId);
			}

			Iterator<PrimaryDataset> datasetIt = config.datasetIterator();
			while (datasetIt.hasNext()) {
				PrimaryDataset primaryDataset = datasetIt.next();
				int databaseId = primaryDatasetToId.get(primaryDataset);
				primaryDataset.setDatabaseId(databaseId);
			}

			System.out.println("Problematic config: " + configId);

			Iterator<Sequence> sequenceIt = config.sequenceIterator();
			while (sequenceIt.hasNext()) {
				System.out.println("ITERATING SEQUENCE");
				Sequence sequence = sequenceIt.next();
				int databaseId = sequenceToId.get(sequence);
				sequence.setDatabaseId(databaseId);
			}

			Iterator<Task> taskIt = config.taskIterator(); // BSATARIC: I don't get it - hasn't this been done already 2
															// times before??
			while (taskIt.hasNext()) {
				System.out.println("ITERATING TASK");
				Task task = taskIt.next();
				int databaseId = taskToId.get(task);
				task.setDatabaseId(databaseId);
			}

			System.out.println("switchProducerToId HASHMAP " + switchProducerToId);
			Iterator<SwitchProducer> switchProducerIt = config.switchProducerIterator();
			while (switchProducerIt.hasNext()) {
				System.out.println("ITERATING SWITCHPRODUCER");
				SwitchProducer switchProducer = switchProducerIt.next();
				System.out.println("switchProducer ID " + switchProducer.databaseId());
				int databaseId = switchProducerToId.get(switchProducer);
				switchProducer.setDatabaseId(databaseId);
			}

			Iterator<Path> pathIt = config.pathIterator();
			while (pathIt.hasNext()) {
				Path path = pathIt.next();
				// System.err.println("Iterating path "+path.name()+": "+
				// "id="+path.databaseId());
				int databaseId = pathToId.get(path);
				path.setDatabaseId(databaseId);
			}

			// System.err.println("###########################End of
			// loops###################");

		} catch (SQLException e) {
			String errMsg = "ConfDB::loadConfiguration(Configuration config) failed " + "(configId=" + configId + "): "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			/*
			 * dbConnector.release(rsInstances); dbConnector.release(rsPathEntries);
			 * dbConnector.release(rsSeqTasOrSPEntries);
			 * dbConnector.release(rsEventContentEntries);
			 * dbConnector.release(rsStreamEntries); dbConnector.release(rsDatasetEntries);
			 * dbConnector.release(rsPathStreamDataset);
			 */
		}
	}

	/** check if extra fields for paths are available in current db. */
	public synchronized boolean checkExtraPathFields() throws DatabaseException {
		ResultSet rs = null;
		return true;
	}

	/**
	 * check if the operator fields for TMP_SEQUENCE_ENTRIES is available in current
	 * db.
	 */
	public synchronized boolean checkOperatorFieldForSequences() throws DatabaseException {
		ResultSet rs = null;
		return true;
	}

	/**
	 * check if the operator fields for TMP_TASK_ENTRIES is available in current db.
	 */
	public synchronized boolean checkOperatorFieldForTasks() throws DatabaseException {
		ResultSet rs = null;
		return true;
	}

	/**
	 * check if the operator fields for TMP_TASK_SWITCHPRODUCER_ENTRIES is available
	 * in current db.
	 */
	public synchronized boolean checkOperatorFieldForSwitchProducers() throws DatabaseException {
		ResultSet rs = null;
		return true;
	}

	/** insert a new directory */
	public synchronized void insertDirectory(Directory dir) throws DatabaseException {
		ResultSet rs = null;
		try {
			psInsertDirectory.setInt(1, dir.parentDir().dbId());
			psInsertDirectory.setString(2, dir.name());
			psInsertDirectory.executeUpdate();
			rs = psInsertDirectory.getGeneratedKeys();
			rs.next();
			dir.setDbId(rs.getInt(1));
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertDirectory(Directory dir) failed " + "(parentDirId=" + dir.parentDir().dbId()
					+ ",name=" + dir.name() + "): " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** remove an (empty!) directory */
	public synchronized void removeDirectory(Directory dir) throws DatabaseException {
		try {
			psDeleteDirectory.setInt(1, dir.dbId());
			psDeleteDirectory.executeUpdate();
		} catch (SQLException e) {
			String errMsg = "ConfDB::removeDirectory(Directory dir) failed " + "(name=" + dir.name() + "): "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** insert a new configuration */
	public synchronized void insertConfiguration(Configuration config, String creator, String processName,
			String comment) throws DatabaseException {
		String releaseTag = config.releaseTag();
		int releaseId = getReleaseId(releaseTag);
		String configDescriptor = config.parentDir().name() + "/" + config.name() + "/" + "V" + config.nextVersion();

		ResultSet rs = null;
		System.out.println("Inserting config " + config.name() + " as " + configDescriptor);

		try {
			dbConnector.getConnection().setAutoCommit(false);

			psFindConfiguration.setString(1, config.parentDir().name() + "/" + config.name());
			rs = psFindConfiguration.executeQuery();
			Integer id_config = 0;
			while (rs.next()) {
				id_config = rs.getInt(1);
			}
			if (id_config == 0) {
				psInsertConfiguration.setString(1, config.parentDir().name() + "/" + config.name());
				psInsertConfiguration.executeUpdate();
				psFindConfiguration.setString(1, config.parentDir().name() + "/" + config.name());
				rs = psFindConfiguration.executeQuery();
				rs.next();
				id_config = rs.getInt(1);
			}

			psInsertConfigurationVers.setInt(1, id_config);
			psInsertConfigurationVers.setInt(2, config.nextVersion());
			psInsertConfigurationVers.setInt(3, releaseId);
			psInsertConfigurationVers.setString(4, configDescriptor);
			psInsertConfigurationVers.setInt(5, config.parentDirId());
			psInsertConfigurationVers.setString(6, config.name());
			psInsertConfigurationVers.setString(7, creator);
			psInsertConfigurationVers.setString(8, processName);
			psInsertConfigurationVers.setString(9, comment);
			psInsertConfigurationVers.executeUpdate();
			rs = psInsertConfigurationVers.getGeneratedKeys();

			rs.next();
			int configId = rs.getInt(1);

			psSelectConfigurationCreated.setInt(1, configId);
			rs = psSelectConfigurationCreated.executeQuery();
			rs.next();
			String created = rs.getString(1);
			config.addNextVersion(configId, created, creator, releaseTag, processName, comment);

			config.updatePathReferences();

			// insert global psets
			insertGlobalPSets(configId, config);

			// insert edsource
			insertEDSources(configId, config);

			// insert essources
			insertESSources(configId, config);

			// insert esmodules
			insertESModules(configId, config);

			// insert services
			insertServices(configId, config);

			HashMap<String, Integer> primaryDatasetHashMap = insertPrimaryDatasets(configId, config);
			HashMap<String, Integer> eventContentHashMap = insertEventContents(configId, config);
			HashMap<String, Integer> streamHashMap = insertStreams(configId, config);

			insertEventContentStreamAssoc(eventContentHashMap, streamHashMap, config);
			insertStreamDatasetAssoc(streamHashMap, primaryDatasetHashMap, config, configId);

			// insert paths
			HashMap<String, Integer> pathHashMap = insertPaths(configId, config);

			// insert sequences
			HashMap<String, Integer> sequenceHashMap = insertSequences(configId, config);

			System.out.println("BEFORE INSERT TASKS");
			// insert tasks
			HashMap<String, Integer> taskHashMap = insertTasks(configId, config);
			System.out.println("AFTER INSERT TASKS");

			System.out.println("BEFORE INSERT SWITCHPRODUCERS");
			// insert switch producers
			HashMap<String, Integer> switchProducerHashMap = insertSwitchProducers(configId, config);
			System.out.println("AFTER INSERT SWITCHPRODUCERS");

			System.out.println("BEFORE INSERT EDALIASES");
			// insert EDAliases
			HashMap<String, Integer> EDAliasHashMap = insertEDAliases(config);
			System.out.println("AFTER INSERT EDALIASES");

			// insert modules
			HashMap<String, Integer> moduleHashMap = insertModules(config);

			System.out.println("*****MODULE HASH MAP: ****" + moduleHashMap);

			insertEventContentStatements(configId, config, eventContentHashMap);
			insertPathStreamPDAssoc(pathHashMap, streamHashMap, primaryDatasetHashMap, config, configId);

			/*
			 * sv notyet // insert parameter bindings / values
			 * psInsertParameterSet.executeBatch(); psInsertVecParameterSet.executeBatch();
			 * psInsertGlobalPSet.executeBatch(); psInsertSuperIdParamAssoc.executeBatch();
			 * psInsertSuperIdParamSetAssoc.executeBatch();
			 * psInsertSuperIdVecParamSetAssoc.executeBatch(); Iterator<PreparedStatement>
			 * itPS = insertParameterHashMap.values().iterator(); while (itPS.hasNext())
			 * itPS.next().executeBatch();
			 */
			System.out.println("BEFORE INSERT REFERENCES");

			// insert references regarding paths, sequences, tasks, switch producers etc.
			insertReferences(config, pathHashMap, sequenceHashMap, taskHashMap, switchProducerHashMap, moduleHashMap,
					EDAliasHashMap, streamHashMap, configId);

			System.out.println("AFTER INSERT REFERENCES");

			psInsertConfDone.setInt(1, configId);
			psInsertConfDone.executeUpdate();
			// psInsertConfProcessing.setInt(1,configId);
			// psInsertConfProcessing.executeUpdate();

			dbConnector.getConnection().commit();
		} catch (DatabaseException e) {
			e.printStackTrace(); // DEBUG
			try {
				dbConnector.getConnection().rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw e;
		} catch (SQLException e) {
			e.printStackTrace(); // DEBUG
			try {
				dbConnector.getConnection().rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			String errMsg = "ConfDB::insertConfiguration(config=" + config.dbId() + ",creator=" + creator
					+ ",processName=" + processName + ",comment=" + comment + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} catch (Exception e) {
			e.printStackTrace(); // DEBUG
			try {
				dbConnector.getConnection().rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			String errMsg = "ConfDB::insertConfiguration(config=" + config.dbId() + ",creator=" + creator
					+ ",processName=" + processName + ",comment=" + comment + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			try {
				dbConnector.getConnection().setAutoCommit(true);
			} catch (SQLException e) {
			}
			dbConnector.release(rs);
		}
	}

	/** lock a configuration and all of its versions */
	public synchronized void lockConfiguration(Configuration config, String userName) throws DatabaseException {
		reconnect();

		int parentDirId = config.parentDir().dbId();
		String parentDirName = config.parentDir().name();
		String configName = config.name();

		if (config.isLocked()) {
			String errMsg = "ConfDB::lockConfiguration(): Can't lock " + config.toString()
					+ ": already locked by user '" + config.lockedByUser() + "'.";
			throw new DatabaseException(errMsg);
		}

		try {
			psInsertConfigurationLock.setInt(1, parentDirId);
			psInsertConfigurationLock.setString(2, configName);
			psInsertConfigurationLock.setString(3, userName);
			psInsertConfigurationLock.executeUpdate();
		} catch (SQLException e) {
			String errMsg = "ConfDB::lockConfiguration(" + config.toString() + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** unlock a configuration and all its versions */
	public synchronized void unlockConfiguration(Configuration config) throws DatabaseException {
		reconnect();

		int parentDirId = config.parentDir().dbId();
		String parentDirName = config.parentDir().name();
		String configName = config.name();
		String userName = config.lockedByUser();

		try {
			psDeleteLock.setInt(1, parentDirId);
			psDeleteLock.setString(2, configName);
			psDeleteLock.executeUpdate();
		} catch (SQLException e) {
			String errMsg = " ConfDB::unlockConfiguration(" + config.toString() + " failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** insert a new super id, return its value */
	private int insertSuperId() throws DatabaseException {
		ResultSet rs = null;
		try {
			psInsertSuperId.executeUpdate();
			rs = psInsertSuperId.getGeneratedKeys();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertSuperId() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** insert configuration's global PSets */
	private void insertGlobalPSets(int configId, Configuration config) throws DatabaseException {
		for (int sequenceNb = 0; sequenceNb < config.psetCount(); sequenceNb++) {
			// int psetId = insertSuperId();
			PSetParameter pset = config.pset(sequenceNb);
			try {
				// first, insert the pset (constraint!)
				psInsertGPset.setString(1, pset.name());
				psInsertGPset.setBoolean(2, pset.isTracked());
				psInsertGPset.executeUpdate();
				ResultSet rs = psInsertGPset.getGeneratedKeys();
				rs.next();
				int psetId = rs.getInt(1);

				for (int i = 0; i < pset.parameterCount(); i++) {
					Parameter p = pset.parameter(i);
					if (p instanceof PSetParameter) {
						PSetParameter ps = (PSetParameter) p;
						insertParameterSet(psetId, i, 0, ps, psInsertParameterGPset);
					} else if (p instanceof VPSetParameter) {
						VPSetParameter vps = (VPSetParameter) p;
						insertVecParameterSet(psetId, i, 0, vps, psInsertParameterGPset);
					} else
						insertParameter(psetId, i, 0, p, psInsertParameterGPset);
				}

				// now, enter association to configuration
				psInsertGlobalPSet.setInt(1, configId);
				psInsertGlobalPSet.setInt(2, psetId);
				psInsertGlobalPSet.setInt(3, sequenceNb);
				psInsertGlobalPSet.executeUpdate();
			} catch (SQLException e) {
				String errMsg = "ConfDB::insertGlobalPSets(configId=" + configId + ") failed: " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}
		config.psets().setDatabaseId(1);
	}

	/** insert configuration's edsoures */
	private void insertEDSources(int configId, Configuration config) throws DatabaseException {
		for (int sequenceNb = 0; sequenceNb < config.edsourceCount(); sequenceNb++) {
			EDSourceInstance edsource = config.edsource(sequenceNb);
			int edsourceId = edsource.databaseId();
			int templateId = edsource.template().databaseId();

			ResultSet rs = null;
			if (edsourceId <= 0) {
				try {
					psInsertEDSource.setInt(1, templateId - 1000000);
					psInsertEDSource.executeUpdate();
					rs = psInsertEDSource.getGeneratedKeys();
					rs.next();
					edsourceId = rs.getInt(1);
				} catch (SQLException e) {
					String errMsg = "ConfDB::insertEDSources(configID=" + configId + ") failed (edsourceId="
							+ edsourceId + " templateId=" + templateId + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
				insertInstanceParameters(edsourceId, edsource, psInsertParameterEDS);
				edsource.setDatabaseId(edsourceId + 1000000);
			} else
				edsourceId -= 1000000;

			try {
				psInsertConfigEDSourceAssoc.setInt(1, configId);
				psInsertConfigEDSourceAssoc.setInt(2, edsourceId);
				psInsertConfigEDSourceAssoc.setInt(3, sequenceNb);
				psInsertConfigEDSourceAssoc.addBatch();
			} catch (SQLException e) {
				String errMsg = "ConfDB::insertEDSources(configID=" + configId + ") failed " + "(edsourceId="
						+ edsourceId + ", sequenceNb=" + sequenceNb + "): " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}

		try {
			psInsertConfigEDSourceAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertEDSources(configId=" + configId + ") failed " + "(batch insert):"
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** insert configuration's essources */
	private void insertESSources(int configId, Configuration config) throws DatabaseException {
		for (int sequenceNb = 0; sequenceNb < config.essourceCount(); sequenceNb++) {
			ESSourceInstance essource = config.essource(sequenceNb);
			int essourceId = essource.databaseId();
			int templateId = essource.template().databaseId();
			boolean isPreferred = essource.isPreferred();

			ResultSet rs = null;

			if (essourceId <= 0) {
				try {
					psInsertESSource.setInt(1, templateId - 2000000);
					psInsertESSource.setString(2, essource.name());
					psInsertESSource.executeUpdate();
					rs = psInsertESSource.getGeneratedKeys();
					rs.next();
					essourceId = rs.getInt(1);
				} catch (SQLException e) {
					String errMsg = "ConfDB::insertESSources(configID=" + configId + ") failed " + "(essourceId="
							+ essourceId + " templateId=" + templateId + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
				insertInstanceParameters(essourceId, essource, psInsertParameterESS);
				essource.setDatabaseId(essourceId + 2000000);
			} else
				essourceId -= 2000000;

			try {
				psInsertConfigESSourceAssoc.setInt(1, configId);
				psInsertConfigESSourceAssoc.setInt(2, essourceId);
				psInsertConfigESSourceAssoc.setInt(3, sequenceNb);
				psInsertConfigESSourceAssoc.setBoolean(4, isPreferred);
				psInsertConfigESSourceAssoc.addBatch();
			} catch (SQLException e) {
				String errMsg = "ConfDB::insertESSources(configID=" + configId + ") failed " + "(essourceId="
						+ essourceId + ", sequenceNb=" + sequenceNb + "):" + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}

		try {
			psInsertConfigESSourceAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertESSources(configId=" + configId + ") failed " + "(batch insert):"
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** insert configuration's esmodules */
	private void insertESModules(int configId, Configuration config) throws DatabaseException {
		for (int sequenceNb = 0; sequenceNb < config.esmoduleCount(); sequenceNb++) {
			ESModuleInstance esmodule = config.esmodule(sequenceNb);
			int esmoduleId = esmodule.databaseId();
			int templateId = esmodule.template().databaseId();
			boolean isPreferred = esmodule.isPreferred();

			ResultSet rs = null;

			if (esmoduleId <= 0) {
				try {
					psInsertESModule.setInt(1, templateId - 3000000);
					psInsertESModule.setString(2, esmodule.name());
					psInsertESModule.executeUpdate();
					rs = psInsertESModule.getGeneratedKeys();
					rs.next();
					esmoduleId = rs.getInt(1);

				} catch (SQLException e) {
					String errMsg = "ConfDB::insertESModules(configID=" + configId + ") failed " + "(esmoduleId="
							+ esmoduleId + " templateId=" + templateId + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
				insertInstanceParameters(esmoduleId, esmodule, psInsertParameterESM);
				esmodule.setDatabaseId(esmoduleId + 3000000);
			} else
				esmoduleId -= 3000000;

			try {
				psInsertConfigESModuleAssoc.setInt(1, configId);
				psInsertConfigESModuleAssoc.setInt(2, esmoduleId);
				psInsertConfigESModuleAssoc.setInt(3, sequenceNb);
				psInsertConfigESModuleAssoc.setBoolean(4, isPreferred);
				psInsertConfigESModuleAssoc.addBatch();
			} catch (SQLException e) {
				String errMsg = "ConfDB::insertESModules(configID=" + configId + ") failed " + "(esmoduleId="
						+ esmoduleId + ", sequenceNb=" + sequenceNb + "): " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}

		try {
			psInsertConfigESModuleAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertESModule(configId=" + configId + ") failed " + "(batch insert):"
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** insert configuration's services */
	private void insertServices(int configId, Configuration config) throws DatabaseException {
		for (int sequenceNb = 0; sequenceNb < config.serviceCount(); sequenceNb++) {
			ServiceInstance service = config.service(sequenceNb);
			int serviceId = service.databaseId();
			int templateId = service.template().databaseId();

			ResultSet rs = null;

			if (serviceId <= 0) {
				try {
					psInsertService.setInt(1, templateId - 4000000);
					psInsertService.executeUpdate();
					rs = psInsertService.getGeneratedKeys();
					rs.next();
					serviceId = rs.getInt(1);

				} catch (SQLException e) {
					String errMsg = "ConfDB::insertServices(configID=" + configId + ") failed " + "(serviceId="
							+ serviceId + " templateId=" + templateId + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
				insertInstanceParameters(serviceId, service, psInsertParameterSRV);
				service.setDatabaseId(serviceId + 4000000);
			} else
				serviceId -= 4000000;

			try {
				psInsertConfigServiceAssoc.setInt(1, configId);
				psInsertConfigServiceAssoc.setInt(2, serviceId);
				psInsertConfigServiceAssoc.setInt(3, sequenceNb);
				psInsertConfigServiceAssoc.addBatch();
			} catch (SQLException e) {
				String errMsg = "ConfDB::insertServices(configID=" + configId + ") failed " + "(serviceId=" + serviceId
						+ ", sequenceNb=" + sequenceNb + "): " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}

		try {
			psInsertConfigServiceAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertService(configId=" + configId + ") failed " + "(batch insert):"
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	private Integer calculatePathCRC(Path path) {
		return 111111;
	}

	private Integer calculateSequenceCRC(Sequence seq) {
		return 111111;
	}

	private Integer calculateTaskCRC(Task tas) {
		return 111111;
	}

	private Integer calculateModuleCRC(ModuleInstance module) {
		return 111111;
	}

	private Integer calculateTemplateCRC() {
		return 111111;
	}

	/** insert configuration's paths */
	private HashMap<String, Integer> insertPaths(int configId, Configuration config) throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		HashMap<Integer, Path> idToPath = new HashMap<Integer, Path>();

		ResultSet rs = null;
		try {
			for (int sequenceNb = 0; sequenceNb < config.pathCount(); sequenceNb++) {
				Path path = config.path(sequenceNb);
				path.hasChanged();
				String pathName = path.name();
				int pathId = path.databaseId();
				boolean pathIsEndPath = path.isSetAsEndPath();
				String description = path.getDescription();
				String contacts = path.getContacts();
				int vpathId = -1;

				if (pathId <= 0) {

					int id_path = -1;
					int crc32 = 0;
					/*
					 * h_ tables int crc32 = calculatePathCRC(path);
					 */
					psCheckPathName.setString(1, pathName);
					rs = psCheckPathName.executeQuery();
					if (rs.next()) {
						id_path = rs.getInt(1);
					}
					// System.err.println("insertPath: searched confver "+configId+" id path
					// "+id_path);

					if (id_path < 0) {
						String pathnoum = pathName;
						Integer version = 0;
						if (pathName.contains("_v")) {
							String[] pathandver = pathName.split("_v");
							pathnoum = pathandver[0];
							version = Integer.parseInt(pathandver[1]);
						}
						psCheckPathNoum.setString(1, pathnoum);
						rs = psCheckPathNoum.executeQuery();
						int id_pathnoum = -1;
						if (rs.next()) {
							id_pathnoum = rs.getInt(1);
						}
						// System.err.println("insertPath: searched pathnoum "+id_pathnoum);
						if (id_pathnoum < 0) {
							psInsertPathNoum.setString(1, pathnoum);
							psInsertPathNoum.executeUpdate();
							rs = psInsertPathNoum.getGeneratedKeys();
							rs.next();
							id_pathnoum = rs.getInt(1);
							// System.err.println("insertPath: created pathnoum "+id_pathnoum);
						}
						psInsertPath.setString(1, pathName);
						psInsertPath.setInt(2, version);
						psInsertPath.setInt(3, id_pathnoum);
						// psInsertPath.setString(4, description);
						// psInsertPath.setString(5, contacts);
						// psInsertPath.setNull(4,Types.VARCHAR);//description
						// psInsertPath.setNull(5,Types.VARCHAR);//contacts

						psInsertPath.executeUpdate();
						rs = psInsertPath.getGeneratedKeys();
						rs.next();
						id_path = rs.getInt(1);
						// System.err.println("insertPath: created id_path "+id_path);
					} /*
						 * else { psUpdatePathDescription.setString(1, description);
						 * psUpdatePathDescription.setString(2, contacts);
						 * psUpdatePathDescription.setInt(3, id_path);
						 * psUpdatePathDescription.executeUpdate(); }
						 */

					psInsertPathIds.setInt(1, id_path);
					if (pathIsEndPath) {
						psInsertPathIds.setInt(2, 1);
					} else {
						psInsertPathIds.setInt(2, 0);
					}
					// psInsertPathIds.setInt(3,crc32);
					psInsertPathIds.setNull(3, Types.INTEGER);// crc
					// psInsertPathIds.setInt(4,111111);
					psInsertPathIds.setNull(4, Types.INTEGER);
					psInsertPathIds.setString(5, description);
					psInsertPathIds.setString(6, contacts);
					psInsertPathIds.executeUpdate();
					rs = psInsertPathIds.getGeneratedKeys();
					rs.next();
					vpathId = rs.getInt(1);
					// System.err.println("insertPath: created vpathid "+vpathId);
					pathId = vpathId; // if not using h_ tables

					/*
					 * only for h_ tables psCheckHPathIdCrc.setInt(1,crc32);
					 * rs=psCheckHPathIdCrc.executeQuery(); if (rs.next()) { pathId=rs.getInt(1);
					 * System.out.println("insertPath: found crc in pathid "+pathId); } else {
					 * psInsertHPathIds.setInt(1,crc32); psInsertHPathIds.setInt(2,111111);
					 * psInsertHPathIds.executeUpdate(); rs = psInsertHPathIds.getGeneratedKeys();
					 * rs.next(); pathId=rs.getInt(1);
					 * System.out.println("insertPath: created pathid "+pathId);
					 * 
					 * psInsertHPathId2Path.setInt(1,id_path);
					 * psInsertHPathId2Path.setInt(2,pathId);
					 * psInsertHPathId2Path.setBoolean(3,pathIsEndPath);
					 * psInsertHPathId2Path.executeUpdate();
					 * 
					 * psInsertHPathId2Uq.setInt(1,vpathId); psInsertHPathId2Uq.setInt(2,pathId);
					 * psInsertHPathId2Uq.executeUpdate(); }
					 */

					result.put(pathName, pathId);
					idToPath.put(pathId, path);
				} else {
					result.put(pathName, -pathId);
					/*
					 * only for h_ tables psSelectPathId2Uq.setInt(2,pathId);
					 * psSelectPathId2Uq.executeUpdate(); rs.next(); vpathId=rs.getInt(1);
					 * System.out.println("insertPath: searched (pathId existed) vpathid "+vpathId);
					 */
					vpathId = pathId; // working with v_
				}

				// System.err.println("insertPath: Trying to insert confver "+configId+" v
				// pathid "+vpathId);
				psInsertConfigPathAssoc.setInt(1, vpathId);
				psInsertConfigPathAssoc.setInt(2, configId);
				psInsertConfigPathAssoc.setInt(3, sequenceNb);
				psInsertConfigPathAssoc.addBatch();
				// psInsertConfigPathAssoc.executeUpdate();

				/*
				 * only for h_ tables System.out.println("insertPath: Trying to insert confver "
				 * +configId+" h_pathid "+pathId); psInsertConfigHPathAssoc.setInt(1,pathId);
				 * psInsertConfigHPathAssoc.setInt(2,configId);
				 * psInsertConfigHPathAssoc.setInt(3,sequenceNb);
				 * psInsertConfigHPathAssoc.executeUpdate();
				 */
			}

			// only *now* set the new databaseId of changed paths!
			for (Map.Entry<Integer, Path> e : idToPath.entrySet()) {
				int id = e.getKey();
				Path p = e.getValue();
				p.setDatabaseId(id);
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertPaths(configId=" + configId + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}

		try {
			psInsertConfigPathAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertPaths(configId=" + configId + ") failed (batch insert): " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}

		return result;
	}

	/** insert configuration's sequences */
	private HashMap<String, Integer> insertSequences(int configId, Configuration config) throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		HashMap<Integer, Sequence> idToSequence = new HashMap<Integer, Sequence>();

		ResultSet rs = null;
		try {
			for (int sequenceNb = 0; sequenceNb < config.sequenceCount(); sequenceNb++) {
				Sequence sequence = config.sequence(sequenceNb);
				sequence.hasChanged();
				int sequenceId = sequence.databaseId();
				String sequenceName = sequence.name();

				if (sequenceId <= 0) { // BSATARIC: if sequence doesn't exist in database
					// int crc32 = calculateSequenceCRC(sequence);
					System.out.println("DIDN'T FIND SEQUENCE");
					int crc32 = 0;
					psInsertPathElement.setInt(1, 2); // paetype
					psInsertPathElement.setString(2, sequenceName);
					// psInsertPathElement.setInt(3,crc32);
					psInsertPathElement.setNull(3, Types.INTEGER);

					psInsertPathElement.executeUpdate();

					rs = psInsertPathElement.getGeneratedKeys();
					rs.next();

					sequenceId = rs.getInt(1);
					result.put(sequenceName, sequenceId);
					idToSequence.put(sequenceId, sequence);
					System.out.println("sequenceId: " + sequenceId);
				} else {
					System.out.println("FOUND SEQUENCE");
					result.put(sequenceName, -sequenceId);
					System.out.println("sequenceId: " + -sequenceId);
				}
				System.out.println("sequenceName: " + sequenceName);

				// psInsertConfigSequenceAssoc.setInt(1,configId);
				// psInsertConfigSequenceAssoc.setInt(2,sequenceId);
				// psInsertConfigSequenceAssoc.setInt(3,sequenceNb);
				// psInsertConfigSequenceAssoc.addBatch();
			}

			// only *now* set the new databaseId of changed sequences!
			for (Map.Entry<Integer, Sequence> e : idToSequence.entrySet()) {
				int id = e.getKey();
				Sequence s = e.getValue();
				s.setDatabaseId(id);
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertSequences(configId=" + configId + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}

		/*
		 * try { psInsertConfigSequenceAssoc.executeBatch(); } catch (SQLException e) {
		 * String errMsg = "ConfDB::insertSequences(configId="+configId+") failed "+
		 * "(batch insert): "+e.getMessage(); throw new DatabaseException(errMsg,e); }
		 */
		return result;
	}

	/** insert configuration's tasks */
	private HashMap<String, Integer> insertTasks(int configId, Configuration config) throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		HashMap<Integer, Task> idToTask = new HashMap<Integer, Task>();

		ResultSet rs = null;
		try {
			for (int taskNb = 0; taskNb < config.taskCount(); taskNb++) {
				System.out.println("taskNb: " + taskNb);
				Task task = config.task(taskNb);
				task.hasChanged();
				int taskId = task.databaseId();
				String taskName = task.name();

				System.out.println("taskId HASCHANGED: " + taskId);

				if (taskId <= 0) { // BSATARIC: if task doesn't exist in database
					System.out.println("DIDN'T FIND TASK");
					int crc32 = 0;
					psInsertPathElement.setInt(1, 4); // paetype = 4 for task
					psInsertPathElement.setString(2, taskName);
					// psInsertPathElement.setInt(3,crc32);
					psInsertPathElement.setNull(3, Types.INTEGER);

					psInsertPathElement.executeUpdate();

					rs = psInsertPathElement.getGeneratedKeys();
					rs.next();

					taskId = rs.getInt(1); // Task Id produced by MySQL operation (or Oracle whatever)
					result.put(taskName, taskId);
					idToTask.put(taskId, task);
					System.out.println("taskId: " + taskId);
				} else {
					System.out.println("FOUND TASK");
					result.put(taskName, -taskId);
					System.out.println("taskId: " + -taskId);
				}
				System.out.println("taskName: " + taskName);
			}

			// only *now* set the new databaseId of changed tasks!
			for (Map.Entry<Integer, Task> e : idToTask.entrySet()) {
				int id = e.getKey();
				Task t = e.getValue();
				t.setDatabaseId(id);
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertTasks(configId=" + configId + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}

		return result;
	}

	/** insert configuration's switch producers */
	private HashMap<String, Integer> insertSwitchProducers(int configId, Configuration config)
			throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		HashMap<Integer, SwitchProducer> idToSwitchProducer = new HashMap<Integer, SwitchProducer>();

		ResultSet rs = null;
		try {
			for (int switchProducerNb = 0; switchProducerNb < config.switchProducerCount(); switchProducerNb++) {
				System.out.println("switchProducerNb: " + switchProducerNb);
				SwitchProducer switchProducer = config.switchProducer(switchProducerNb);
				switchProducer.hasChanged();
				int switchProducerId = switchProducer.databaseId();
				String switchProducerName = switchProducer.name();

				System.out.println("switchProducerId HASCHANGED: " + switchProducerId);

				if (switchProducerId <= 0) { // BSATARIC: if switchProducer doesn't exist in database
					System.out.println("DIDN'T FIND SWITCHPRODUCER");
					int crc32 = 0;
					psInsertPathElement.setInt(1, 5); // paetype = 5 for switchProducer
					psInsertPathElement.setString(2, switchProducerName);
					// psInsertPathElement.setInt(3,crc32);
					psInsertPathElement.setNull(3, Types.INTEGER);

					psInsertPathElement.executeUpdate();

					rs = psInsertPathElement.getGeneratedKeys();
					rs.next();

					switchProducerId = rs.getInt(1); // switchProducer Id produced by MySQL operation (or Oracle
														// whatever)
					result.put(switchProducerName, switchProducerId);
					idToSwitchProducer.put(switchProducerId, switchProducer);
					System.out.println("switchProducerId: " + switchProducerId);
				} else {
					System.out.println("FOUND SWITCHPRODUCER");
					result.put(switchProducerName, -switchProducerId);
					System.out.println("switchProducerId: " + -switchProducerId);
				}
				System.out.println("switchProducerName: " + switchProducerName);
			}

			// only *now* set the new databaseId of changed switch producers!
			for (Map.Entry<Integer, SwitchProducer> e : idToSwitchProducer.entrySet()) {
				int id = e.getKey();
				SwitchProducer sp = e.getValue();
				sp.setDatabaseId(id);
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertSwitchProducers(configId=" + configId + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}

		return result;
	}

	/** insert configuration's EDAliases */
	private HashMap<String, Integer> insertEDAliases(Configuration config) throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();

		// ArrayList<IdInstancePair> modulesToStore = new ArrayList<IdInstancePair>();

		ResultSet rs = null;

		for (int i = 0; i < config.edAliasCount(); i++) {
			EDAliasInstance edAlias = config.edAlias(i);
			int edAliasId = edAlias.databaseId();
			if (edAliasId > 0) {
				result.put(edAlias.name(), edAliasId);
			} else {
				// int crc32 = calculateModuleCRC(edAlias);
				int crc32 = 0;
				try {
					psInsertPathElement.setInt(1, 6); // paetype
					psInsertPathElement.setString(2, edAlias.name());
					// psInsertPathElement.setInt(3,crc32);
					psInsertPathElement.setNull(3, Types.INTEGER); // crc
					psInsertPathElement.executeUpdate();

					rs = psInsertPathElement.getGeneratedKeys();
					rs.next();
					edAliasId = rs.getInt(1);

					insertInstanceParameters(edAliasId, edAlias, psInsertMoElement); // This should work automatically

					result.put(edAlias.name(), edAliasId);
					edAlias.setDatabaseId(edAliasId);

				} catch (SQLException e) {
					String errMsg = "ConfDB::insertEDAliases(config=" + config.toString() + " failed (edAliasId="
							+ edAliasId + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			}
		}

		try {
			psInsertMoElement.executeBatch(); // isn't this double update??
			// psInsertPae2Moe.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertEDAliases(configId=" + config.toString() + ") failed " + "(batch insert): "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}

		return result;
	}

	/** insert configuration's modules */
	private HashMap<String, Integer> insertModules(Configuration config) throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();

		// ArrayList<IdInstancePair> modulesToStore = new ArrayList<IdInstancePair>();

		ResultSet rs = null;

		for (int i = 0; i < config.moduleCount(); i++) {
			ModuleInstance module = config.module(i);
			int moduleId = module.databaseId();
			int hmoduleId = module.databaseId();
			int templateId = module.template().databaseId();
			if (moduleId > 0) {
				result.put(module.name(), moduleId);
			} else {
				// int crc32 = calculateModuleCRC(module);
				int crc32 = 0;
				try {
					psInsertPathElement.setInt(1, 1); // paetype
					psInsertPathElement.setString(2, module.name());
					// psInsertPathElement.setInt(3,crc32);
					psInsertPathElement.setNull(3, Types.INTEGER); // crc
					psInsertPathElement.executeUpdate();

					rs = psInsertPathElement.getGeneratedKeys();
					rs.next();
					moduleId = rs.getInt(1);

					psInsertMod2Templ.setInt(1, moduleId);
					psInsertMod2Templ.setInt(2, templateId);
					psInsertMod2Templ.addBatch();

					insertInstanceParameters(moduleId, module, psInsertMoElement);

					result.put(module.name(), moduleId);
					// modulesToStore.add(new IdInstancePair(moduleId, module));
					module.setDatabaseId(moduleId);

					/*
					 * Only for h_ tables psCheckHPaElCrc.setInt(1,crc32);
					 * rs=psCheckHPaElCrc.executeQuery(); if (rs.next()) { hmoduleId=rs.getInt(1);
					 * System.out.println("insertPath: found crc in pathid "+moduleId); } else {
					 * psInsertHPaEl.setInt(1,crc32); psInsertHPaEl.setInt(2,111111);
					 * psInsertHPaEl.executeUpdate(); rs = psInsertHPaEl.getGeneratedKeys();
					 * rs.next(); hmoduleId=rs.getInt(1);
					 * System.out.println("insertPath: created HPAE "+moduleId);
					 * insertInstanceParameters(hmoduleId,module,psInsertParameterHMOE);
					 * 
					 * }
					 */
				} catch (SQLException e) {
					String errMsg = "ConfDB::insertModules(config=" + config.toString() + " failed (moduleId="
							+ moduleId + " templateId=" + templateId + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			}
		}

		try {
			psInsertMod2Templ.executeBatch();
			psInsertMoElement.executeBatch();
			// psInsertPae2Moe.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertModules(configId=" + config.toString() + ") failed " + "(batch insert): "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}

		/*
		 * Iterator<IdInstancePair> it=modulesToStore.iterator(); while (it.hasNext()) {
		 * IdInstancePair pair = it.next(); int moduleId = pair.id; ModuleInstance
		 * module = (ModuleInstance)pair.instance;
		 * insertInstanceParameters(moduleId,module,psInsertParameterMOE);
		 * module.setDatabaseId(moduleId); }
		 */
		return result;
	}

	/** insert all references, regarding paths and sequences */
	private void insertReferences(Configuration config, HashMap<String, Integer> pathHashMap,
			HashMap<String, Integer> sequenceHashMap, HashMap<String, Integer> taskHashMap,
			HashMap<String, Integer> switchProducerHashMap, HashMap<String, Integer> moduleHashMap,
			HashMap<String, Integer> EDAliasHashMap, HashMap<String, Integer> streamHashMap, int configId)
			throws DatabaseException {
		// paths (BSATARIC: paths are basically only consisting of references)
		for (int i = 0; i < config.pathCount(); i++) {
			Path path = config.path(i);
			int pathId = pathHashMap.get(path.name());

			if (pathId > 0) {

				for (int sequenceNb = 0; sequenceNb < path.entryCount(); sequenceNb++) {
					Reference r = path.entry(sequenceNb);

					if (r instanceof SequenceReference) {
						int sequenceId = Math.abs(sequenceHashMap.get(r.name()));
						// System.err.println("insertReferences - Found seq " + sequenceId + " ( " +
						// r.name() + " )");
						try {
							psInsertPathElementAssoc.setInt(1, pathId);
							psInsertPathElementAssoc.setInt(2, sequenceId);
							psInsertPathElementAssoc.setNull(3, Types.INTEGER);// parent
							psInsertPathElementAssoc.setInt(4, 0); // lvl
							psInsertPathElementAssoc.setInt(5, sequenceNb);
							psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
							psInsertPathElementAssoc.addBatch();
							// psInsertPathElementAssoc.executeUpdate();
							System.out.println("BEFORE insertPathSeqReferences");
							System.out.println(
									"pathId " + pathId + " sequenceId " + sequenceId + " sequenceNb " + sequenceNb);

							insertPathSeqReferences((Sequence) r.parent(), pathId, sequenceId, 1, sequenceHashMap,
									taskHashMap, switchProducerHashMap, moduleHashMap, EDAliasHashMap, streamHashMap);

							System.out.println("AFTER insertPathSeqReferences");
						} catch (SQLException e) {
							String errMsg = "ConfDB::insertReferences(config=" + config.toString() + ") failed (pathId="
									+ pathId + ",sequenceId=" + sequenceId + ",sequenceNb=" + sequenceNb + "): "
									+ e.getMessage();
							throw new DatabaseException(errMsg, e);
						}
					} else if (r instanceof TaskReference) {
						int taskId = Math.abs(taskHashMap.get(r.name()));

						System.out.println("pathId " + pathId + " taskId " + taskId + " task Name " + r.name());

						try {
							psInsertPathElementAssoc.setInt(1, pathId);
							psInsertPathElementAssoc.setInt(2, taskId);
							psInsertPathElementAssoc.setNull(3, Types.INTEGER);// parent
							psInsertPathElementAssoc.setInt(4, 0); // lvl
							psInsertPathElementAssoc.setInt(5, sequenceNb);
							psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
							psInsertPathElementAssoc.addBatch();
							// psInsertPathElementAssoc.executeUpdate();

							System.out.println("BEFORE insertPathTasReferences");
							System.out.println("pathId " + pathId + " taskId " + taskId + " sequenceNb " + sequenceNb);

							insertPathTasReferences((Task) r.parent(), pathId, taskId, 1, taskHashMap,
									switchProducerHashMap, moduleHashMap, EDAliasHashMap, streamHashMap);

							System.out.println("AFTER insertPathTasReferences");
						} catch (SQLException e) {
							String errMsg = "ConfDB::insertReferences(config=" + config.toString() + ") failed (pathId="
									+ pathId + ",taskId=" + taskId + ",sequenceNb=" + sequenceNb + "): "
									+ e.getMessage();
							throw new DatabaseException(errMsg, e);
						}

					} else if (r instanceof SwitchProducerReference) {
						int switchProducerId = Math.abs(switchProducerHashMap.get(r.name()));

						System.out.println("SWITCHPRODUCER REFFFFFFFFFFFFF pathId " + pathId + " switchProducerId "
								+ switchProducerId + " switchProducer Name " + r.name());
						// System.err.println("insertReferences - Found seq " + sequenceId + " ( " +
						// r.name() + " )");
						// (id_pathid,id_pae,id_parent,lvl,ord,operator)

						try {
							psInsertPathElementAssoc.setInt(1, pathId);
							psInsertPathElementAssoc.setInt(2, switchProducerId);
							psInsertPathElementAssoc.setNull(3, Types.INTEGER);// parent
							psInsertPathElementAssoc.setInt(4, 0); // lvl
							psInsertPathElementAssoc.setInt(5, sequenceNb);
							psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
							psInsertPathElementAssoc.addBatch();
							// psInsertPathElementAssoc.executeUpdate();

							System.out.println("BEFORE insertPathTasReferences");
							System.out.println("pathId " + pathId + " switchProducerId " + switchProducerId
									+ " sequenceNb " + sequenceNb);

							insertPathSwitchProducerReferences((SwitchProducer) r.parent(), pathId, switchProducerId, 1,
									switchProducerHashMap, moduleHashMap, EDAliasHashMap);

							System.out.println("AFTER insertPathTasReferences");
						} catch (SQLException e) {
							String errMsg = "ConfDB::insertReferences(config=" + config.toString() + ") failed (pathId="
									+ pathId + ",switchProducerId=" + switchProducerId + ",sequenceNb=" + sequenceNb
									+ "): " + e.getMessage();
							throw new DatabaseException(errMsg, e);
						}

					} else if (r instanceof ModuleReference) {
						int moduleId = moduleHashMap.get(r.name());
						try {
							psInsertPathElementAssoc.setInt(1, pathId);
							psInsertPathElementAssoc.setInt(2, moduleId);
							psInsertPathElementAssoc.setNull(3, Types.INTEGER);// parent
							psInsertPathElementAssoc.setInt(4, 0); // lvl
							psInsertPathElementAssoc.setInt(5, sequenceNb);
							psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
							psInsertPathElementAssoc.addBatch();
							// psInsertPathElementAssoc.executeUpdate();
						} catch (SQLException e) {
							String errMsg = "ConfDB::insertReferences(config=" + config.toString() + ") failed (pathId="
									+ pathId + ",moduleId=" + moduleId + ",sequenceNb=" + sequenceNb + "): "
									+ e.getMessage();
							throw new DatabaseException(errMsg, e);
						}
					} else if (r instanceof OutputModuleReference) {
						String streamName = r.name().replaceFirst("hltOutput", "");
						int outputModuleId = streamHashMap.get(streamName);
						if (outputModuleId < 0)
							outputModuleId = -1 * outputModuleId;
						try {
							psInsertPathOutputModuleAssoc.setInt(1, pathId);
							psInsertPathOutputModuleAssoc.setInt(2, outputModuleId);
							psInsertPathOutputModuleAssoc.setInt(3, sequenceNb);
							psInsertPathOutputModuleAssoc.setInt(4, r.getOperator().ordinal());
							psInsertPathOutputModuleAssoc.addBatch();
							// psInsertPathOutputModuleAssoc.executeUpdate();
						} catch (SQLException e) {
							String errMsg = "ConfDB::insertReferences(config=" + config.toString() + ") failed (pathId="
									+ pathId + ",moduleId=" + outputModuleId + ",sequenceNb=" + sequenceNb + "): "
									+ e.getMessage();
							throw new DatabaseException(errMsg, e);
						}
					}
				}
			}
		}
		// sequences
		for (int sequenceNb = 0; sequenceNb < config.sequenceCount(); sequenceNb++) {
			Sequence sequence = config.sequence(sequenceNb);
			int sequenceId = sequence.databaseId();

			System.out.println("BEFORE insertSeqReferences");
			System.out.println("configId " + configId + " sequenceId " + sequenceId + " sequenceNb " + sequenceNb);

			try {
				psInsertConfigSequenceAssoc.setInt(1, configId);
				psInsertConfigSequenceAssoc.setInt(2, sequenceId);
				psInsertConfigSequenceAssoc.setNull(3, Types.INTEGER);// parent
				psInsertConfigSequenceAssoc.setInt(4, 0); // lvl
				psInsertConfigSequenceAssoc.setInt(5, sequenceNb);
				psInsertConfigSequenceAssoc.setInt(6, 0);
				psInsertConfigSequenceAssoc.addBatch();

				insertSeqReferences(sequence, configId, 0, 0, sequenceHashMap, taskHashMap, switchProducerHashMap,
						EDAliasHashMap, moduleHashMap);

				System.out.println("AFTER insertSeqReferences");

			} catch (SQLException e) {
				String errMsg = "ConfDB::insertSeqReferences(config=" + config.toString() + ") failed (configId="
						+ configId + ",sequenceId=" + sequenceId + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}
		// tasks
		for (int sequenceNb = 0; sequenceNb < config.taskCount(); sequenceNb++) {
			Task task = config.task(sequenceNb);
			int taskId = task.databaseId();
			System.out.println("BEFORE insertTasReferences");
			System.out.println("configId " + configId + " taskId " + taskId + " sequenceNb " + sequenceNb);

			try {
				psInsertConfigTaskAssoc.setInt(1, configId);
				psInsertConfigTaskAssoc.setInt(2, taskId);
				psInsertConfigTaskAssoc.setNull(3, Types.INTEGER);// parent
				psInsertConfigTaskAssoc.setInt(4, 0); // lvl
				psInsertConfigTaskAssoc.setInt(5, sequenceNb);
				psInsertConfigTaskAssoc.setInt(6, 0);
				psInsertConfigTaskAssoc.addBatch();

				insertTasReferences(task, configId, 0, 0, taskHashMap, switchProducerHashMap, EDAliasHashMap,
						moduleHashMap);

				System.out.println("AFTER insertTasReferences");

			} catch (SQLException e) {
				String errMsg = "ConfDB::insertTasReferences(config=" + config.toString() + ") failed (configId="
						+ configId + ",taskId=" + taskId + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}
		// switch producers
		for (int sequenceNb = 0; sequenceNb < config.switchProducerCount(); sequenceNb++) {
			SwitchProducer switchProducer = config.switchProducer(sequenceNb);
			int switchProducerId = switchProducer.databaseId();
			System.out.println("BEFORE insertSwitchProducerReferences");
			System.out.println(
					"configId " + configId + " switchProducerId " + switchProducerId + " sequenceNb " + sequenceNb);

			try {
				psInsertConfigSwitchProducerAssoc.setInt(1, configId);
				psInsertConfigSwitchProducerAssoc.setInt(2, switchProducerId);
				psInsertConfigSwitchProducerAssoc.setNull(3, Types.INTEGER);// parent
				psInsertConfigSwitchProducerAssoc.setInt(4, 0); // lvl
				psInsertConfigSwitchProducerAssoc.setInt(5, sequenceNb);
				psInsertConfigSwitchProducerAssoc.setInt(6, 0);
				psInsertConfigSwitchProducerAssoc.addBatch();

				insertSwitchProducerReferences(switchProducer, configId, 0, 0, switchProducerHashMap, EDAliasHashMap,
						moduleHashMap);

				System.out.println("AFTER insertSwitchProducerReferences");

			} catch (SQLException e) {
				String errMsg = "ConfDB::insertSwitchProducerReferences(config=" + config.toString()
						+ ") failed (configId=" + configId + ",switchProducerId=" + switchProducerId + ",sequenceNb="
						+ sequenceNb + "): " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}

		try {
			// BSATARIC: it seems all this is collected statements in loop but it seems
			// weird that it can work like that
			// http://tutorials.jenkov.com/jdbc/batchupdate.html
			psInsertPathElementAssoc.executeBatch();
			psInsertPathOutputModuleAssoc.executeBatch();
			psInsertConfigSequenceAssoc.executeBatch();
			psInsertConfigTaskAssoc.executeBatch();
			psInsertConfigSwitchProducerAssoc.executeBatch();
//            psInsertPathPathAssoc.executeBatch();
//            psInsertPathSequenceAssoc.executeBatch();
//            psInsertPathModuleAssoc.executeBatch();
//            psInsertPathOutputModuleAssoc.executeBatch();
//            psInsertSequenceSequenceAssoc.executeBatch();
//            psInsertSequenceModuleAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertReferences(config=" + config.toString() + ") failed " + "(batch insert): "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** insert all references, regarding paths and sequences */
	private void insertSeqReferences(Sequence sequence, int configId, int parentId, int lvl,
			HashMap<String, Integer> sequenceHashMap, HashMap<String, Integer> taskHashMap,
			HashMap<String, Integer> switchProducerHashMap, HashMap<String, Integer> EDAliasHashMap,
			HashMap<String, Integer> moduleHashMap) throws DatabaseException {
		int sequenceId = sequenceHashMap.get(sequence.name());

		for (int sequenceNb = 0; sequenceNb < sequence.entryCount(); sequenceNb++) {
			Reference r = sequence.entry(sequenceNb);
			if (r instanceof SequenceReference) {
				int childSequenceId = Math.abs(sequenceHashMap.get(r.name()));
				try {
					psInsertConfigSequenceAssoc.setInt(1, configId);
					psInsertConfigSequenceAssoc.setInt(2, childSequenceId);
					psInsertConfigSequenceAssoc.setInt(3, parentId); // lvl
					psInsertConfigSequenceAssoc.setInt(4, lvl); // lvl
					psInsertConfigSequenceAssoc.setInt(5, sequenceNb);
					psInsertConfigSequenceAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigSequenceAssoc.addBatch();

					insertSeqReferences((Sequence) r.parent(), configId, childSequenceId, lvl + 1, sequenceHashMap,
							taskHashMap, switchProducerHashMap, EDAliasHashMap, moduleHashMap);

				} catch (SQLException e) {
					e.printStackTrace();
					String errMsg = "ConfDB::insertReferences(Sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + " (" + sequence.name() + "), childSequenceId=" + childSequenceId + " ("
							+ r.name() + ")" + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof TaskReference) {
				int childTaskId = Math.abs(taskHashMap.get(r.name()));
				try {
					psInsertConfigSequenceAssoc.setInt(1, configId);
					psInsertConfigSequenceAssoc.setInt(2, childTaskId);
					psInsertConfigSequenceAssoc.setInt(3, parentId); // lvl
					psInsertConfigSequenceAssoc.setInt(4, lvl); // lvl
					psInsertConfigSequenceAssoc.setInt(5, sequenceNb);
					psInsertConfigSequenceAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigSequenceAssoc.addBatch();

					insertTasReferences((Task) r.parent(), configId, childTaskId, lvl + 1, taskHashMap,
							switchProducerHashMap, EDAliasHashMap, moduleHashMap);

				} catch (SQLException e) {
					e.printStackTrace();
					String errMsg = "ConfDB::insertReferences(Sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + " (" + sequence.name() + "), childTaskId=" + childTaskId + " (" + r.name()
							+ ")" + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof SwitchProducerReference) {
				int childSwitchProducerId = Math.abs(switchProducerHashMap.get(r.name()));
				try {
					psInsertConfigSequenceAssoc.setInt(1, configId);
					psInsertConfigSequenceAssoc.setInt(2, childSwitchProducerId);
					psInsertConfigSequenceAssoc.setInt(3, parentId); // lvl
					psInsertConfigSequenceAssoc.setInt(4, lvl); // lvl
					psInsertConfigSequenceAssoc.setInt(5, sequenceNb);
					psInsertConfigSequenceAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigSequenceAssoc.addBatch();

					insertSwitchProducerReferences((SwitchProducer) r.parent(), configId, childSwitchProducerId,
							lvl + 1, switchProducerHashMap, EDAliasHashMap, moduleHashMap);

				} catch (SQLException e) {
					e.printStackTrace();
					String errMsg = "ConfDB::insertReferences(Sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + " (" + sequence.name() + "), childSwitchProducerId=" + childSwitchProducerId
							+ " (" + r.name() + ")" + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof ModuleReference) {

				System.out.println("BEFORE INSERT CONFIG SEQUENCE MODULE REFERENCE");

				System.out.println("MODULE HASH MAP: " + moduleHashMap);
				System.out.println("r.name(): " + r.name());
				int moduleId = moduleHashMap.get(r.name());

				System.out.println("Sequence = " + sequence.name() + " Sequence ID " + sequenceId + " moduleId "
						+ moduleId + " parentId " + parentId + " lvl " + lvl + " sequenceNb " + sequenceNb);

				try {
					psInsertConfigSequenceAssoc.setInt(1, configId);
					psInsertConfigSequenceAssoc.setInt(2, moduleId);
					psInsertConfigSequenceAssoc.setInt(3, parentId); // lvl
					psInsertConfigSequenceAssoc.setInt(4, lvl); // lvl
					psInsertConfigSequenceAssoc.setInt(5, sequenceNb);
					psInsertConfigSequenceAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigSequenceAssoc.addBatch();

					System.out.println("AFTER INSERT CONFIG SEQUENCE MODULE REFERENCE");
					// System.out.println("psInsertConfigSequenceAssoc: " +
					// psInsertConfigSequenceAssoc.toString());
					// psInsertPathElementAssoc.executeUpdate();
				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(Sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + ",moduleId=" + moduleId + ",sequenceNb=" + sequenceNb + "): "
							+ e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			}
		}

	}

	/** insert all references, regarding configuration (not path) and sequences */
	private void insertPathSeqReferences(Sequence sequence, int pathId, int parentId, int lvl,
			HashMap<String, Integer> sequenceHashMap, HashMap<String, Integer> taskHashMap,
			HashMap<String, Integer> switchProducerHashMap, HashMap<String, Integer> moduleHashMap,
			HashMap<String, Integer> EDAliasHashMap, HashMap<String, Integer> streamHashMap) throws DatabaseException {

		// sequences
//      for (int i=0;i<config.sequenceCount();i++) {
//          Sequence sequence   = config.sequence(i);
//          int      sequenceId = sequenceHashMap.get(sequence.name());

		int sequenceId = sequenceHashMap.get(sequence.name());

		// System.err.println("insertSeqRef - Found " + sequence.entryCount() + " for
		// seq " + sequenceId + " ( " + sequence.name() + " )");

		for (int sequenceNb = 0; sequenceNb < sequence.entryCount(); sequenceNb++) {
			Reference r = sequence.entry(sequenceNb);
			if (r instanceof SequenceReference) {
				int childSequenceId = Math.abs(sequenceHashMap.get(r.name()));
				try {

					psInsertPathElementAssoc.setInt(1, pathId);
					psInsertPathElementAssoc.setInt(2, childSequenceId); // nesting
					psInsertPathElementAssoc.setInt(3, parentId); // lvl
					psInsertPathElementAssoc.setInt(4, lvl); // lvl
					psInsertPathElementAssoc.setInt(5, sequenceNb);
					psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
					psInsertPathElementAssoc.addBatch();
					// psInsertPathElementAssoc.executeUpdate();

					insertPathSeqReferences((Sequence) r.parent(), pathId, childSequenceId, lvl + 1, // nesting
							sequenceHashMap, taskHashMap, switchProducerHashMap, moduleHashMap, EDAliasHashMap,
							streamHashMap);

				} catch (SQLException e) {
					e.printStackTrace();
					String errMsg = "ConfDB::insertReferences(Sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + " (" + sequence.name() + "), childSequenceId=" + childSequenceId + " ("
							+ r.name() + ")" + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof TaskReference) {
				int childTaskId = Math.abs(taskHashMap.get(r.name()));
				try {
					psInsertPathElementAssoc.setInt(1, pathId);
					psInsertPathElementAssoc.setInt(2, childTaskId);
					psInsertPathElementAssoc.setInt(3, parentId); // lvl
					psInsertPathElementAssoc.setInt(4, lvl); // lvl
					psInsertPathElementAssoc.setInt(5, sequenceNb);
					psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
					psInsertPathElementAssoc.addBatch();
					// psInsertPathElementAssoc.executeUpdate();

					insertPathTasReferences((Task) r.parent(), pathId, childTaskId, lvl + 1, taskHashMap,
							switchProducerHashMap, moduleHashMap, EDAliasHashMap, streamHashMap);

				} catch (SQLException e) {
					e.printStackTrace();
					String errMsg = "ConfDB::insertReferences(Sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + " (" + sequence.name() + "), childSequenceId=" + childTaskId + " ("
							+ r.name() + ")" + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof SwitchProducerReference) {
				System.out.println("BEFORE INSERT PATH SEQUENCE SWITCH PRODUCER REFERENCE");

				int childSwitchProducerId = Math.abs(switchProducerHashMap.get(r.name()));

				System.out.println("****PROBLEM**** Sequence = " + sequence.name() + " Sequence ID " + sequenceId
						+ " pathId " + pathId + " childSwitchProducerId " + childSwitchProducerId + " parentId "
						+ parentId + " lvl " + lvl + " sequenceNb " + sequenceNb);
				try {
					psInsertPathElementAssoc.setInt(1, pathId);
					psInsertPathElementAssoc.setInt(2, childSwitchProducerId);
					psInsertPathElementAssoc.setInt(3, parentId); // lvl
					psInsertPathElementAssoc.setInt(4, lvl); // lvl
					psInsertPathElementAssoc.setInt(5, sequenceNb);
					psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
					psInsertPathElementAssoc.addBatch();
					// psInsertPathElementAssoc.executeUpdate();

					insertPathSwitchProducerReferences((SwitchProducer) r.parent(), pathId, childSwitchProducerId,
							lvl + 1, switchProducerHashMap, moduleHashMap, EDAliasHashMap);

					System.out.println("AFTER INSERT PATH SEQUENCE SWITCH PRODUCER REFERENCE");
				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(Sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + ",childSwitchProducerId=" + childSwitchProducerId + ",sequenceNb="
							+ sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof ModuleReference) {
				System.out.println("BEFORE INSERT PATH SEQUENCE MODULE REFERENCE");

				int moduleId = moduleHashMap.get(r.name());

				System.out.println("Sequence = " + sequence.name() + " Sequence ID " + sequenceId + " pathId " + pathId
						+ " moduleId " + moduleId + " parentId " + parentId + " lvl " + lvl + " sequenceNb "
						+ sequenceNb);
				try {
					psInsertPathElementAssoc.setInt(1, pathId);
					psInsertPathElementAssoc.setInt(2, moduleId);
					psInsertPathElementAssoc.setInt(3, parentId); // lvl
					psInsertPathElementAssoc.setInt(4, lvl); // lvl
					psInsertPathElementAssoc.setInt(5, sequenceNb);
					psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
					psInsertPathElementAssoc.addBatch();
					// psInsertPathElementAssoc.executeUpdate();

					System.out.println("AFTER INSERT PATH SEQUENCE MODULE REFERENCE");
				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(Sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + ",moduleId=" + moduleId + ",sequenceNb=" + sequenceNb + "): "
							+ e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof OutputModuleReference) {
				String streamName = r.name().replaceFirst("hltOutput", "");
				int outputModuleId = streamHashMap.get(streamName);
				if (outputModuleId < 0)
					outputModuleId = -1 * outputModuleId;
				try {
					psInsertPathOutputModuleAssoc.setInt(1, pathId);
					psInsertPathOutputModuleAssoc.setInt(2, outputModuleId);
					psInsertPathOutputModuleAssoc.setInt(3, sequenceNb);
					psInsertPathOutputModuleAssoc.setInt(4, r.getOperator().ordinal());
					psInsertPathOutputModuleAssoc.addBatch();
					// psInsertPathOutputModuleAssoc.executeUpdate();
				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(sequence=" + sequence.name() + ") failed (sequenceId="
							+ sequenceId + ",outputmoduleId=" + outputModuleId + ",sequenceNb=" + sequenceNb + "): "
							+ e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			}
		}

	}

	/** insert all references, regarding configuration (not path) and tasks */
	private void insertTasReferences(Task task, int configId, int parentId, int lvl,
			HashMap<String, Integer> taskHashMap, HashMap<String, Integer> switchProducerHashMap,
			HashMap<String, Integer> EDAliasHashMap, HashMap<String, Integer> moduleHashMap) throws DatabaseException {
		int taskId = taskHashMap.get(task.name());

		for (int sequenceNb = 0; sequenceNb < task.entryCount(); sequenceNb++) {
			Reference r = task.entry(sequenceNb);
			if (r instanceof TaskReference) {
				int childTaskId = Math.abs(taskHashMap.get(r.name()));
				try {
					psInsertConfigTaskAssoc.setInt(1, configId);
					psInsertConfigTaskAssoc.setInt(2, childTaskId);
					psInsertConfigTaskAssoc.setInt(3, parentId); // lvl
					psInsertConfigTaskAssoc.setInt(4, lvl); // lvl
					psInsertConfigTaskAssoc.setInt(5, sequenceNb);
					psInsertConfigTaskAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigTaskAssoc.addBatch();

					insertTasReferences((Task) r.parent(), configId, childTaskId, lvl + 1, taskHashMap,
							switchProducerHashMap, EDAliasHashMap, moduleHashMap);

				} catch (SQLException e) {
					e.printStackTrace();
					String errMsg = "ConfDB::insertReferences(Task=" + task.name() + ") failed (taskId=" + taskId + " ("
							+ task.name() + "), childTaskId=" + childTaskId + " (" + r.name() + ")" + ",sequenceNb="
							+ sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof SwitchProducerReference) {
				int childSwitchProducerId = Math.abs(switchProducerHashMap.get(r.name()));
				try {
					psInsertConfigSequenceAssoc.setInt(1, configId);
					psInsertConfigSequenceAssoc.setInt(2, childSwitchProducerId);
					psInsertConfigSequenceAssoc.setInt(3, parentId); // lvl
					psInsertConfigSequenceAssoc.setInt(4, lvl); // lvl
					psInsertConfigSequenceAssoc.setInt(5, sequenceNb);
					psInsertConfigSequenceAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigSequenceAssoc.addBatch();

					insertSwitchProducerReferences((SwitchProducer) r.parent(), configId, childSwitchProducerId,
							lvl + 1, switchProducerHashMap, EDAliasHashMap, moduleHashMap);

				} catch (SQLException e) {
					e.printStackTrace();
					String errMsg = "ConfDB::insertReferences(Task=" + task.name() + ") failed (taskId=" + taskId + " ("
							+ task.name() + "), childSwitchProducerId=" + childSwitchProducerId + " (" + r.name() + ")"
							+ ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			} else if (r instanceof ModuleReference) {

				System.out.println("BEFORE INSERT CONFIG TASK MODULE REFERENCE");

				int moduleId = moduleHashMap.get(r.name());
				System.out.println("Task = " + task.name() + " Task ID " + taskId + " moduleId " + moduleId
						+ " parentId " + parentId + " lvl " + lvl + " sequenceNb " + sequenceNb);

				try {
					psInsertConfigTaskAssoc.setInt(1, configId);
					psInsertConfigTaskAssoc.setInt(2, moduleId);
					psInsertConfigTaskAssoc.setInt(3, parentId); // lvl
					psInsertConfigTaskAssoc.setInt(4, lvl); // lvl
					psInsertConfigTaskAssoc.setInt(5, sequenceNb);
					psInsertConfigTaskAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigTaskAssoc.addBatch(); //
					// psInsertConfigTaskAssoc.executeUpdate();

					System.out.println("AFTER INSERT CONFIG TASK MODULE REFERENCE"); //
					System.out.println("psInsertConfigTaskAssoc: " + //
							psInsertConfigTaskAssoc.toString());

				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(Task=" + task.name() + ") failed (taskId=" + taskId
							+ ",moduleId=" + moduleId + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}

			}
		}
	}

	/** insert all references, regarding paths and tasks */
	private void insertPathTasReferences(Task task, int pathId, int parentId, int lvl,
			HashMap<String, Integer> taskHashMap, HashMap<String, Integer> switchProducerHashMap,
			HashMap<String, Integer> moduleHashMap, HashMap<String, Integer> EDAliasHashMap,
			HashMap<String, Integer> streamHashMap) throws DatabaseException {

		int taskId = taskHashMap.get(task.name());

		if (true) {

			for (int sequenceNb = 0; sequenceNb < task.entryCount(); sequenceNb++) {
				Reference r = task.entry(sequenceNb);
				if (r instanceof TaskReference) {
					System.out.println("Trying to insert TaskReference");
					int childTaskId = Math.abs(taskHashMap.get(r.name()));
					try {
						psInsertPathElementAssoc.setInt(1, pathId);
						psInsertPathElementAssoc.setInt(2, childTaskId);
						psInsertPathElementAssoc.setInt(3, parentId); // lvl
						psInsertPathElementAssoc.setInt(4, lvl); // lvl
						psInsertPathElementAssoc.setInt(5, sequenceNb);
						psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
						psInsertPathElementAssoc.addBatch();
						// psInsertPathElementAssoc.executeUpdate();

						insertPathTasReferences((Task) r.parent(), pathId, childTaskId, lvl + 1, taskHashMap,
								switchProducerHashMap, moduleHashMap, EDAliasHashMap, streamHashMap);
						System.out.println("Finshed inserting TaskReference");

					} catch (SQLException e) {
						e.printStackTrace();
						String errMsg = "ConfDB::insertReferences(Tasks=" + task.name() + ") failed (taskId=" + taskId
								+ " (" + task.name() + "), childTaskId=" + childTaskId + " (" + r.name() + ")"
								+ ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
						throw new DatabaseException(errMsg, e);
					}
				} else if (r instanceof SwitchProducerReference) {
					System.out.println("BEFORE INSERT PATH TASK SWITCH PRODUCER REFERENCE");

					int childSwitchProducerId = Math.abs(switchProducerHashMap.get(r.name()));

					System.out.println("Task = " + task.name() + " Task ID " + taskId + " pathId " + pathId
							+ " childSwitchProducerId " + childSwitchProducerId + " parentId " + parentId + " lvl "
							+ lvl + " sequenceNb " + sequenceNb);

					try {
						psInsertPathElementAssoc.setInt(1, pathId);
						psInsertPathElementAssoc.setInt(2, childSwitchProducerId);
						psInsertPathElementAssoc.setInt(3, parentId); // lvl
						psInsertPathElementAssoc.setInt(4, lvl); // lvl
						psInsertPathElementAssoc.setInt(5, sequenceNb);
						psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
						psInsertPathElementAssoc.addBatch(); //
						// psInsertPathElementAssoc.executeUpdate();

						insertPathSwitchProducerReferences((SwitchProducer) r.parent(), pathId, childSwitchProducerId,
								lvl + 1, switchProducerHashMap, moduleHashMap, EDAliasHashMap);

						System.out.println("AFTER INSERT PATH TASK SWITCH PRODUCER REFERENCE");

					} catch (SQLException e) {
						String errMsg = "ConfDB::insertReferences(Task=" + task.name() + ") failed (taskId=" + taskId
								+ ",childSwitchProducerId=" + childSwitchProducerId + ",sequenceNb=" + sequenceNb
								+ "): " + e.getMessage();
						throw new DatabaseException(errMsg, e);
					}

				} else if (r instanceof ModuleReference) {
					System.out.println("BEFORE INSERT PATH TASK MODULE REFERENCE");
					System.out.println("**** MODULE HASH MAP *****" + moduleHashMap);
					System.out.println("r.name() " + r.name());

					int moduleId = moduleHashMap.get(r.name());

					System.out
							.println("Task = " + task.name() + " Task ID " + taskId + " pathId " + pathId + " moduleId "
									+ moduleId + " parentId " + parentId + " lvl " + lvl + " sequenceNb " + sequenceNb);

					try {
						psInsertPathElementAssoc.setInt(1, pathId);
						psInsertPathElementAssoc.setInt(2, moduleId);
						psInsertPathElementAssoc.setInt(3, parentId); // lvl
						psInsertPathElementAssoc.setInt(4, lvl); // lvl
						psInsertPathElementAssoc.setInt(5, sequenceNb);
						psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
						psInsertPathElementAssoc.addBatch(); //
						// psInsertPathElementAssoc.executeUpdate();

						System.out.println("AFTER INSERT PATH TASK MODULE REFERENCE");

					} catch (SQLException e) {
						String errMsg = "ConfDB::insertReferences(Task=" + task.name() + ") failed (taskId=" + taskId
								+ ",moduleId=" + moduleId + ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
						throw new DatabaseException(errMsg, e);
					}

				} else if (r instanceof OutputModuleReference) {
					String streamName = r.name().replaceFirst("hltOutput", "");
					int outputModuleId = streamHashMap.get(streamName);
					if (outputModuleId < 0)
						outputModuleId = -1 * outputModuleId;
					try {
						psInsertPathOutputModuleAssoc.setInt(1, pathId);
						psInsertPathOutputModuleAssoc.setInt(2, outputModuleId);
						psInsertPathOutputModuleAssoc.setInt(3, sequenceNb);
						psInsertPathOutputModuleAssoc.setInt(4, r.getOperator().ordinal());
						psInsertPathOutputModuleAssoc.addBatch();
						// psInsertPathOutputModuleAssoc.executeUpdate();
					} catch (SQLException e) {
						String errMsg = "ConfDB::insertReferences(sequence=" + task.name() + ") failed (taskId="
								+ taskId + ",outputmoduleId=" + outputModuleId + ",sequenceNb=" + sequenceNb + "): "
								+ e.getMessage();
						throw new DatabaseException(errMsg, e);
					}
				}
			}
		}
	}

	/** insert all references, regarding paths and switch producers */
	private void insertPathSwitchProducerReferences(SwitchProducer switchProducer, int pathId, int parentId, int lvl,
			HashMap<String, Integer> switchProducerHashMap, HashMap<String, Integer> moduleHashMap,
			HashMap<String, Integer> EDAliasHashMap) throws DatabaseException {

		int switchProducerId = switchProducerHashMap.get(switchProducer.name());

		// here there should be only 2 allowed (EDProducer or EDAlias - can be done in
		// GUI)
		for (int sequenceNb = 0; sequenceNb < switchProducer.entryCount(); sequenceNb++) {
			Reference r = switchProducer.entry(sequenceNb);
			if (r instanceof ModuleReference) {
				System.out.println("BEFORE INSERT PATH SWITCHPRODUCER MODULE REFERENCE");

				int moduleId = moduleHashMap.get(r.name());

				System.out.println("SwitchProducer = " + switchProducer.name() + " SwitchProducer ID "
						+ switchProducerId + " pathId " + pathId + " moduleId " + moduleId + " parentId " + parentId
						+ " lvl " + lvl + " sequenceNb " + sequenceNb);

				try {
					psInsertPathElementAssoc.setInt(1, pathId);
					psInsertPathElementAssoc.setInt(2, moduleId);
					psInsertPathElementAssoc.setInt(3, parentId); // lvl
					psInsertPathElementAssoc.setInt(4, lvl); // lvl
					psInsertPathElementAssoc.setInt(5, sequenceNb);
					psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
					psInsertPathElementAssoc.addBatch(); //
					// psInsertPathElementAssoc.executeUpdate();

					System.out.println("AFTER INSERT PATH SWITCHPRODUCER MODULE REFERENCE");

				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(SwitchProducer=" + switchProducer.name()
							+ ") failed (switchProducerId=" + switchProducerId + ",moduleId=" + moduleId
							+ ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}

			} else if (r instanceof EDAliasReference) {
				System.out.println("BEFORE INSERT PATH SWITCHPRODUCER EDALIAS REFERENCE");

				int edAliasId = EDAliasHashMap.get(r.name());

				System.out.println("SwitchProducer = " + switchProducer.name() + " SwitchProducer ID "
						+ switchProducerId + " pathId " + pathId + " edAliasId " + edAliasId + " parentId " + parentId
						+ " lvl " + lvl + " sequenceNb " + sequenceNb);

				try {
					psInsertPathElementAssoc.setInt(1, pathId);
					psInsertPathElementAssoc.setInt(2, edAliasId);
					psInsertPathElementAssoc.setInt(3, parentId); // lvl
					psInsertPathElementAssoc.setInt(4, lvl); // lvl
					psInsertPathElementAssoc.setInt(5, sequenceNb);
					psInsertPathElementAssoc.setInt(6, r.getOperator().ordinal());
					psInsertPathElementAssoc.addBatch(); //
					// psInsertPathElementAssoc.executeUpdate();

					System.out.println("AFTER INSERT PATH SWITCHPRODUCER MODULE REFERENCE");

				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(SwitchProducer=" + switchProducer.name()
							+ ") failed (switchProducerId=" + switchProducerId + ",edAliasId=" + edAliasId
							+ ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}

			}
		}
	}

	/**
	 * insert all references, regarding configuration (not path) and switch
	 * producers
	 */
	private void insertSwitchProducerReferences(SwitchProducer switchProducer, int configId, int parentId, int lvl,
			HashMap<String, Integer> switchProducerHashMap, HashMap<String, Integer> EDAliasHashMap,
			HashMap<String, Integer> moduleHashMap) throws DatabaseException {
		int switchProducerId = switchProducerHashMap.get(switchProducer.name());

		// here there should be only 2 allowed (EDProducer or EDAlias - can be done in
		// GUI)
		for (int sequenceNb = 0; sequenceNb < switchProducer.entryCount(); sequenceNb++) {
			Reference r = switchProducer.entry(sequenceNb);
			if (r instanceof ModuleReference) {

				System.out.println("BEFORE INSERT CONFIG SWITCH PRODUCER MODULE REFERENCE");

				int moduleId = moduleHashMap.get(r.name());
				System.out.println("SwitchProducer = " + switchProducer.name() + " SwitchProducer ID "
						+ switchProducerId + " moduleId " + moduleId + " parentId " + parentId + " lvl " + lvl
						+ " sequenceNb " + sequenceNb);

				try {
					psInsertConfigSwitchProducerAssoc.setInt(1, configId);
					psInsertConfigSwitchProducerAssoc.setInt(2, moduleId);
					psInsertConfigSwitchProducerAssoc.setInt(3, parentId); // lvl
					psInsertConfigSwitchProducerAssoc.setInt(4, lvl); // lvl
					psInsertConfigSwitchProducerAssoc.setInt(5, sequenceNb);
					psInsertConfigSwitchProducerAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigSwitchProducerAssoc.addBatch(); //
					// psInsertConfigTaskAssoc.executeUpdate();

					System.out.println("AFTER INSERT CONFIG SWITCH PRODUCER MODULE REFERENCE"); //
					System.out.println("psInsertConfigSwitchProducerAssoc: " + //
							psInsertConfigSwitchProducerAssoc.toString());

				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(SwitchProducer=" + switchProducer.name()
							+ ") failed (switchProducerId=" + switchProducerId + ",moduleId=" + moduleId
							+ ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}

			} else if (r instanceof EDAliasReference) {

				System.out.println("BEFORE INSERT CONFIG SWITCH PRODUCER EDALIAS REFERENCE");

				int edAliasId = EDAliasHashMap.get(r.name());
				System.out.println("SwitchProducer = " + switchProducer.name() + " SwitchProducer ID "
						+ switchProducerId + " edAliasId " + edAliasId + " parentId " + parentId + " lvl " + lvl
						+ " sequenceNb " + sequenceNb);

				try {
					psInsertConfigSwitchProducerAssoc.setInt(1, configId);
					psInsertConfigSwitchProducerAssoc.setInt(2, edAliasId);
					psInsertConfigSwitchProducerAssoc.setInt(3, parentId); // lvl
					psInsertConfigSwitchProducerAssoc.setInt(4, lvl); // lvl
					psInsertConfigSwitchProducerAssoc.setInt(5, sequenceNb);
					psInsertConfigSwitchProducerAssoc.setInt(6, r.getOperator().ordinal());
					psInsertConfigSwitchProducerAssoc.addBatch(); //
					// psInsertConfigSwitchProducerAssoc.executeUpdate();

					System.out.println("AFTER INSERT CONFIG SWITCH PRODUCER EDALIAS REFERENCE"); //
					System.out.println("psInsertConfigSwitchProducerAssoc: " + //
							psInsertConfigSwitchProducerAssoc.toString());

				} catch (SQLException e) {
					String errMsg = "ConfDB::insertReferences(SwitchProducer=" + switchProducer.name()
							+ ") failed (switchProducerId=" + switchProducerId + ",edAliasId=" + edAliasId
							+ ",sequenceNb=" + sequenceNb + "): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			}
		}

	}

	/** insert configuration's Event Content */
	private HashMap<String, Integer> insertEventContents(int configId, Configuration config) throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();

		Iterator<EventContent> itC = config.contentIterator();

		ResultSet rse;

		while (itC.hasNext()) {
			EventContent eventContent = itC.next();
			int eventContentId = eventContent.databaseId();
			if (!eventContent.hasChanged()) {
				result.put(eventContent.name(), -1 * eventContentId);
				continue;
			}
			try {
				psCheckContents.setString(1, eventContent.name());
				rse = psCheckContents.executeQuery();
				int id_cont = -1;
				if (rse.next()) {
					id_cont = rse.getInt(1);
				} else {
					psInsertContents.setString(1, eventContent.name());
					psInsertContents.executeUpdate();
					rse = psInsertContents.getGeneratedKeys();
					rse.next();
					id_cont = rse.getInt(1);
				}
				psInsertContentIds.setInt(1, id_cont);
				psInsertContentIds.executeUpdate();
				rse = psInsertContentIds.getGeneratedKeys();
				rse.next();
				eventContentId = rse.getInt(1);
				result.put(eventContent.name(), eventContentId);
				eventContent.setDatabaseId(eventContentId);
			} catch (SQLException e) {
				String errMsg = "ConfDB::Event Content(config=" + config.toString() + ") failed " + "(batch insert): "
						+ e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
			eventContent.setDatabaseId(eventContentId);
		}

		itC = config.contentIterator();

		while (itC.hasNext()) {
			EventContent eventContent = itC.next();
			int eventContentId = eventContent.databaseId();
			try {
				psInsertContentsConfigAssoc.setInt(1, eventContentId);
				psInsertContentsConfigAssoc.setInt(2, configId);
				psInsertContentsConfigAssoc.addBatch();
			} catch (SQLException e) {
				String errMsg = "ConfDB::Event Content Config Association (config=" + config.toString()
						+ ") failed (batch insert): " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}

		}
		try {
			psInsertContentsConfigAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::Event Content Config Association(config=" + config.toString()
					+ ") failed (batch insert): " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		return result;
	}

	/** insert event content statements */
	private void insertEventContentStatements(int configId, Configuration config,
			HashMap<String, Integer> eventContentHashMap) throws DatabaseException {
		for (int i = 0; i < config.contentCount(); i++) {
			EventContent eventContent = config.content(i);
			int contentId = eventContentHashMap.get(eventContent.name());
			if (contentId < 0) {
				continue;
			}

			for (int j = 0; j < eventContent.commandCount(); j++) {

				OutputCommand command = eventContent.command(j);
				String className = command.className();
				String moduleName = command.moduleName();
				String extraName = command.extraName();
				String processName = command.processName();
				int iDrop = 1;
				if (command.isDrop()) {
					iDrop = 0;
				}
				try {

					psSelectStatementId.setString(1, className);
					psSelectStatementId.setString(2, moduleName);
					psSelectStatementId.setString(3, extraName);
					psSelectStatementId.setString(4, processName);
					psSelectStatementId.setInt(5, iDrop);
					ResultSet rsStatementId = psSelectStatementId.executeQuery();
					int statementId = -1;
					while (rsStatementId.next()) {
						statementId = rsStatementId.getInt(1);
					}

					if (statementId < 0) {
						psInsertEventContentStatements.setString(1, className);
						psInsertEventContentStatements.setString(2, moduleName);
						psInsertEventContentStatements.setString(3, extraName);
						psInsertEventContentStatements.setString(4, processName);
						iDrop = 1;
						if (command.isDrop()) {
							iDrop = 0;
						}
						psInsertEventContentStatements.setInt(5, iDrop);
						psInsertEventContentStatements.executeUpdate();
						ResultSet rsNewStatementId = psInsertEventContentStatements.getGeneratedKeys();
						rsNewStatementId.next();
						statementId = rsNewStatementId.getInt(1);
					}

					psInsertECStatementAssoc.setInt(1, j);
					psInsertECStatementAssoc.setInt(2, statementId);
					psInsertECStatementAssoc.setInt(3, contentId);
					Path parentPath = command.parentPath();
					if (parentPath != null) {
						psInsertECStatementAssoc.setInt(4, parentPath.databaseId());
					} else {
						psInsertECStatementAssoc.setInt(4, -1);
					}
					psInsertECStatementAssoc.addBatch();
				} catch (SQLException e) {
					String errMsg = "ConfDB::StatementID Update(config=" + config.toString()
							+ ") failed (batch insert): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}

			}
		}
		try {
			psInsertECStatementAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::StatementID Update(config=" + config.toString() + ") failed " + "(batch insert): "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** insert configuration's Streams */
	private HashMap<String, Integer> insertStreams(int configId, Configuration config) throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		Iterator<Stream> itS = config.streamIterator();

		ResultSet rs = null;

		while (itS.hasNext()) {
			Stream stream = itS.next();
			int streamId = stream.databaseId();
			if (streamId > 0) {
				result.put(stream.name(), -1 * streamId);
				continue;
			}
			try {
				psInsertStreams.setString(1, stream.name());
				psInsertStreams.executeUpdate();
				rs = psInsertStreams.getGeneratedKeys();
				rs.next();
				streamId = rs.getInt(1);
				psInsertStreamsIds.setInt(1, streamId);
				psInsertStreamsIds.setDouble(2, stream.fractionToDisk());
				psInsertStreamsIds.executeUpdate();
				rs = psInsertStreamsIds.getGeneratedKeys();
				rs.next();
				streamId = rs.getInt(1);
				stream.setDatabaseId(streamId);
				result.put(stream.name(), streamId);
			} catch (SQLException e) {
				String errMsg = "ConfDB::Streams(config=" + config.toString() + ") failed " + "(batch insert): "
						+ e.getMessage();
				throw new DatabaseException(errMsg, e);
			}

			OutputModule outputModule = stream.outputModule();

			for (int sequenceNb = 0; sequenceNb < outputModule.parameterCount(); sequenceNb++) {
				Parameter p = outputModule.parameter(sequenceNb);

				if (!p.isDefault()) {
					if (p instanceof VPSetParameter) {
						VPSetParameter vpset = (VPSetParameter) p;
						insertVecParameterSet(streamId, sequenceNb, 0, vpset, psInsertParameterOUTM);
					} else if (p instanceof PSetParameter) {
						PSetParameter pset = (PSetParameter) p;
						insertParameterSet(streamId, sequenceNb, 0, pset, psInsertParameterOUTM);
					} else {
						insertParameter(streamId, sequenceNb, 0, p, psInsertParameterOUTM);
					}
				}
			}
		}

		return result;
	}

	/** insert configuration's Primary Datasets */
	private HashMap<String, Integer> insertPrimaryDatasets(int configId, Configuration config)
			throws DatabaseException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		Iterator<PrimaryDataset> itP = config.datasetIterator();

		ResultSet rs = null;

		while (itP.hasNext()) {
			PrimaryDataset primaryDataset = itP.next();
			int datasetId = primaryDataset.databaseId();
			if (!primaryDataset.hasChanged()) {
				result.put(primaryDataset.name(), -1 * datasetId);
				continue;
			}
			try {
				psInsertPrimaryDatasets.setString(1, primaryDataset.name());
				psInsertPrimaryDatasets.executeUpdate();
				rs = psInsertPrimaryDatasets.getGeneratedKeys();
				rs.next();
				datasetId = rs.getInt(1);
				psInsertPrimaryDatasetIds.setInt(1, datasetId);
				psInsertPrimaryDatasetIds.executeUpdate();
				rs = psInsertPrimaryDatasetIds.getGeneratedKeys();
				rs.next();
				datasetId = rs.getInt(1);
				result.put(primaryDataset.name(), datasetId);
				primaryDataset.setDatabaseId(datasetId);
			} catch (SQLException e) {
				String errMsg = "ConfDB::Primary Dataset (config=" + config.toString() + ") failed "
						+ "(batch insert): " + e.getMessage();
				throw new DatabaseException(errMsg, e);
			}
		}
		return result;
	}

	private void insertEventContentStreamAssoc(HashMap<String, Integer> eventContentHashMap,
			HashMap<String, Integer> streamHashMap, Configuration config) throws DatabaseException {
		for (int i = 0; i < config.contentCount(); i++) {
			EventContent eventContent = config.content(i);
			int contentId = eventContentHashMap.get(eventContent.name());

			for (int j = 0; j < eventContent.streamCount(); j++) {
				Stream stream = eventContent.stream(j);
				int streamId = streamHashMap.get(stream.name());
				if (contentId < 0 && streamId < 0)
					continue;
				try {
					psInsertECStreamAssoc.setInt(1, eventContent.databaseId());
					psInsertECStreamAssoc.setInt(2, stream.databaseId());
					psInsertECStreamAssoc.executeUpdate();

				} catch (SQLException e) {
					String errMsg = "ConfDB::Event Content(config=" + config.toString() + ") failed "
							+ "(batch insert): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			}
		}
	}

	private void insertStreamDatasetAssoc(HashMap<String, Integer> streamHashMap,
			HashMap<String, Integer> primaryDatasetHashMap, Configuration config, int configId)
			throws DatabaseException {
		for (int i = 0; i < config.streamCount(); i++) {
			Stream stream = config.stream(i);
			int streamId = streamHashMap.get(stream.name());
			for (int j = 0; j < stream.datasetCount(); j++) {
				PrimaryDataset primaryDataset = stream.dataset(j);
				int datasetId = primaryDatasetHashMap.get(primaryDataset.name());
				// if(datasetId < 0 && streamId < 0)
				// continue;
				try {
					psInsertStreamDatasetAssoc.setInt(1, configId);
					psInsertStreamDatasetAssoc.setInt(2, stream.databaseId());
					psInsertStreamDatasetAssoc.setInt(3, primaryDataset.databaseId());
					psInsertStreamDatasetAssoc.addBatch();
				} catch (SQLException e) {
					String errMsg = "ConfDB::Stream Primary dataset association(config=" + config.toString()
							+ ") failed " + "(batch insert): " + e.getMessage();
					throw new DatabaseException(errMsg, e);
				}
			}
			int streamtounassigneddone = 0;
			for (int j = 0; (j < stream.pathCount()) && (streamtounassigneddone == 0); j++) {
				Path path = stream.path(j);
				ArrayList<PrimaryDataset> primaryDatasets = stream.datasets(path);
				int datasetId = -1;
				if (primaryDatasets.size() == 0) {
					try {
						psInsertStreamDatasetAssoc.setInt(1, configId);
						psInsertStreamDatasetAssoc.setInt(2, stream.databaseId());
						psInsertStreamDatasetAssoc.setInt(3, datasetId);
						psInsertStreamDatasetAssoc.addBatch();
					} catch (SQLException e) {
						String errMsg = "ConfDB::Stream Primary dataset association(config=" + config.toString()
								+ ") failed " + "(batch insert): " + e.getMessage();
						throw new DatabaseException(errMsg, e);
					}
					streamtounassigneddone = 1;
				}
			}
		}

		try {
			psInsertStreamDatasetAssoc.executeBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::Stream Primary dataset association(config=" + config.toString() + ") failed "
					+ "(batch insert): " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	// TODO This is the other Key Code to implement the share paths.
	/*
	 * private void insertPathStreamPDAssoc(HashMap<String,Integer>
	 * pathHashMap,HashMap<String,Integer> streamHashMap,HashMap<String,Integer>
	 * primaryDatasetHashMap,Configuration config,int configId) throws
	 * DatabaseException { for (int i=0;i<config.streamCount();i++) { Stream stream
	 * = config.stream(i); int streamId = streamHashMap.get(stream.name());
	 * 
	 * if(streamId<0) continue;
	 * 
	 * for (int j=0;j<stream.pathCount();j++) { Path path = stream.path(j); int
	 * pathId = pathHashMap.get(path.name()); PrimaryDataset primaryDataset =
	 * stream.dataset(path); //TODO Here we have a problem. there could be more than
	 * one. int datasetId = -1;
	 * 
	 * if(primaryDataset!=null){ datasetId = primaryDataset.databaseId(); }
	 * 
	 * try { psInsertPathStreamPDAssoc.setInt(1,path.databaseId());
	 * psInsertPathStreamPDAssoc.setInt(2,streamId);
	 * psInsertPathStreamPDAssoc.setInt(3,datasetId);
	 * psInsertPathStreamPDAssoc.executeUpdate(); } catch (SQLException e) { String
	 * errMsg = "ConfDB::Event Content(config="+config.toString()+") failed "+
	 * "(batch insert): "+e.getMessage(); throw new DatabaseException(errMsg,e); } }
	 * } }
	 */

	// TODO This is the other Key Code to implement the share paths.
	private void insertPathStreamPDAssoc(HashMap<String, Integer> pathHashMap, HashMap<String, Integer> streamHashMap,
			HashMap<String, Integer> primaryDatasetHashMap, Configuration config, int configId)
			throws DatabaseException {
		for (int i = 0; i < config.streamCount(); i++) {
			Stream stream = config.stream(i);
			int streamId = streamHashMap.get(stream.name());

			// if(streamId<0) continue;

			for (int j = 0; j < stream.pathCount(); j++) {
				Path path = stream.path(j);
				int pathId = pathHashMap.get(path.name());
				// PrimaryDataset primaryDataset = stream.dataset(path); //TODO Here we have a
				// problem. there could be more than one.
				ArrayList<PrimaryDataset> primaryDatasets = stream.datasets(path);
				int datasetId = -1;

				// Make relation between Stream and Path, datasetId = -1.
				if (primaryDatasets.size() == 0) {
					try {
						if (streamId > 0) {
							psInsertPathStreamPDAssoc.setInt(1, path.databaseId());
							psInsertPathStreamPDAssoc.setInt(2, streamId);
							psInsertPathStreamPDAssoc.setInt(3, datasetId);
							psInsertPathStreamPDAssoc.executeUpdate();
						}
					} catch (SQLException e) {
						String errMsg = "ConfDB::Event Content(config=" + config.toString() + ") failed "
								+ "(batch insert): " + e.getMessage();
						throw new DatabaseException(errMsg, e);
					}
				} else {
					// Next loop makes one or more than one relations between
					// datasets/Streams/Paths.
					for (int ds = 0; ds < primaryDatasets.size(); ds++) {
						PrimaryDataset primaryDataset = primaryDatasets.get(ds);
						datasetId = primaryDatasetHashMap.get(primaryDataset.name());
						// datasetId = primaryDataset.databaseId();
						try {
							if ((streamId > 0) || (datasetId > 0)) {
								psInsertPathStreamPDAssoc.setInt(1, path.databaseId());
								psInsertPathStreamPDAssoc.setInt(2, stream.databaseId());
								psInsertPathStreamPDAssoc.setInt(3, primaryDataset.databaseId());
								psInsertPathStreamPDAssoc.executeUpdate();
							}
						} catch (SQLException e) {
							String errMsg = "ConfDB::Event Content(config=" + config.toString() + ") failed "
									+ "(batch insert): " + e.getMessage();
							throw new DatabaseException(errMsg, e);
						}
					}
				} // end
			}
		}
	}

	/** insert all instance parameters */
	private void insertInstanceParameters(int superId, Instance instance, PreparedStatement dbstmnt)
			throws DatabaseException {
		for (int sequenceNb = 0; sequenceNb < instance.parameterCount(); sequenceNb++) {
			Parameter p = instance.parameter(sequenceNb);

			if (!p.isDefault()) {
				if (p instanceof VPSetParameter) {
					VPSetParameter vpset = (VPSetParameter) p;
					insertVecParameterSet(superId, sequenceNb, 0, vpset, dbstmnt);
				} else if (p instanceof PSetParameter) {
					PSetParameter pset = (PSetParameter) p;
					insertParameterSet(superId, sequenceNb, 0, pset, dbstmnt);
				} else {
					insertParameter(superId, sequenceNb, 0, p, dbstmnt);
				}
			}
		}
	}

	/** get all configuration names */
	public synchronized String[] getConfigNames() throws DatabaseException {
		ArrayList<String> listOfNames = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = psSelectConfigNames.executeQuery();
			while (rs.next())
				listOfNames.add(rs.getString(1) + "/" + rs.getString(2));
		} catch (SQLException e) {
			String errMsg = "ConfDB::getConfigNames() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
		return listOfNames.toArray(new String[listOfNames.size()]);
	}

	/** get all configuration names associated to a given release */
	public synchronized String[] getConfigNamesByRelease(int releaseId) throws DatabaseException {
		ArrayList<String> listOfNames = new ArrayList<String>();
		ResultSet rs = null;
		try {
			psSelectConfigNamesByRelease.setInt(1, releaseId);
			rs = psSelectConfigNamesByRelease.executeQuery();
			while (rs.next())
				listOfNames.add(rs.getString(1) + "/" + rs.getString(2) + "/V" + rs.getInt(3));
		} catch (SQLException e) {
			String errMsg = "ConfDB::getConfigNamesByRelease() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
		return listOfNames.toArray(new String[listOfNames.size()]);
	}

	/** get list of software release tags */
	public synchronized String[] getReleaseTags() throws DatabaseException {
		reconnect();

		ArrayList<String> listOfTags = new ArrayList<String>();
		listOfTags.add(new String());
		ResultSet rs = null;
		try {
			rs = psSelectReleaseTags.executeQuery();
			while (rs.next()) {
				String releaseTag = rs.getString(2);
				if (!listOfTags.contains(releaseTag))
					listOfTags.add(releaseTag);
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::getReleaseTags() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		return listOfTags.toArray(new String[listOfTags.size()]);
	}

	/** get list of software release tags */
	public synchronized String[] getReleaseTagsSorted() throws DatabaseException {
		reconnect();

		ArrayList<String> listOfTags = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = psSelectReleaseTagsSorted.executeQuery();
			while (rs.next()) {
				String releaseTag = rs.getString(2);
				if (!listOfTags.contains(releaseTag))
					listOfTags.add(releaseTag);
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::getReleaseTagsSorted() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		return listOfTags.toArray(new String[listOfTags.size()]);
	}

	/** get the id of a directory, -1 if it does not exist */
	public synchronized int getDirectoryId(String directoryName) throws DatabaseException {
		reconnect();
		ResultSet rs = null;
		try {
			psSelectDirectoryId.setString(1, directoryName);
			rs = psSelectDirectoryId.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			String errMsg = "ConfDB::getDirectoryId(directoryName=" + directoryName + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** get hash map with all directories */
	public synchronized HashMap<Integer, Directory> getDirectoryHashMap() throws DatabaseException {
		reconnect();

		HashMap<Integer, Directory> directoryHashMap = new HashMap<Integer, Directory>();

		Directory rootDir = null;
		ResultSet rs = null;
		try {

			rs = psSelectDirectories.executeQuery();
			while (rs.next()) {
				int dirId = rs.getInt(1);
				int parentDirId = rs.getInt(2);
				String dirName = rs.getString(3);
				String dirCreated = rs.getTimestamp(4).toString();

				if (directoryHashMap.size() == 0) {
					rootDir = new Directory(dirId, dirName, dirCreated, null);
					directoryHashMap.put(dirId, rootDir);
				} else {
					if (!directoryHashMap.containsKey(parentDirId))
						throw new DatabaseException("parentDir not found in DB" + " (parentDirId=" + parentDirId + ")");
					Directory parentDir = directoryHashMap.get(parentDirId);
					Directory newDir = new Directory(dirId, dirName, dirCreated, parentDir);
					parentDir.addChildDir(newDir);
					directoryHashMap.put(dirId, newDir);
				}
			}

			return directoryHashMap;
		} catch (SQLException e) {
			String errMsg = "ConfDB::getDirectoryHashMap() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	public synchronized int getConfigId(int newId) throws DatabaseException {

		reconnect();

		ResultSet rs = null;
		int oldId = -1;

		try {
			psSelectOrigDbId.setInt(1, newId);
			rs = psSelectOrigDbId.executeQuery();
			while (rs.next()) {
				oldId = rs.getInt(1);
			}
			;
		} catch (SQLException e) {
			String errMsg = "ConfDB::getConfigId(newID" + newId + ": " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		;

		return oldId;
	}

	public synchronized int getConfigId(String fullConfigName) throws DatabaseException {
		int newId = getConfigNewId(fullConfigName);
		// System.out.println("getConfigId : " + fullConfigName + " newId " + newId );

		reconnect();

		ResultSet rs = null;
		int oldId = -1;

		try {
			psSelectOrigDbId.setInt(1, newId);
			rs = psSelectOrigDbId.executeQuery();
			while (rs.next()) {
				oldId = rs.getInt(1);
			}
			;
		} catch (SQLException e) {
			String errMsg = "ConfDB::getConfigId(fullConfigName=" + fullConfigName + ": " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		;

		return oldId;
	}

	/** get the configuration id for a configuration name */
	public synchronized int getConfigNewId(String fullConfigName) throws DatabaseException {
		reconnect();

		int version = 0;

		int index = fullConfigName.lastIndexOf("/V");
		if (index >= 0) {
			version = Integer.parseInt(fullConfigName.substring(index + 2));
			fullConfigName = fullConfigName.substring(0, index);
		}

		index = fullConfigName.lastIndexOf("/");
		if (index < 0) {
			String errMsg = "ConfDB::getConfigNewId(fullConfigName=" + fullConfigName + ") failed (invalid name).";
			throw new DatabaseException(errMsg);
		}

		String dirName = fullConfigName.substring(0, index);
		String configName = fullConfigName.substring(index + 1);

		ResultSet rs = null;
		try {

			PreparedStatement ps = null;

			if (version > 0) {
				ps = psSelectConfigurationId;
				ps.setString(1, dirName);
				ps.setString(2, configName);
				ps.setInt(3, version);
			} else {
				ps = psSelectConfigurationIdLatest;
				ps.setString(1, dirName);
				ps.setString(2, configName);
			}

			rs = ps.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			String errMsg = "ConfDB::getConfigNewId(fullConfigName=" + fullConfigName + ") failed (dirName=" + dirName
					+ ", configName=" + configName + ",version=" + version + "): " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** get ConfigInfo for a particular configId */
	public synchronized ConfigInfo getConfigInfo(int configId) throws DatabaseException {
		reconnect();
		ResultSet rs = null;
		int newId = -1;
		try {
			psSelectNewDbId.setInt(1, configId);
			rs = psSelectNewDbId.executeQuery();
			while (rs.next()) {
				newId = rs.getInt(1);
			}
			;
		} catch (SQLException e) {
			String errMsg = "ConfDB::getConfigInfo(Configuration config) failed " + "(OrigconfigId=" + configId + "): "
					+ e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
		;

		return getConfigNewInfo(newId);
	};

	/** get ConfigInfo for a particular configId */
	public synchronized ConfigInfo getConfigNewInfo(int configId) throws DatabaseException {
		ConfigInfo result = getConfigNewInfo(configId, loadConfigurationTree());
		if (result == null) {
			String errMsg = "ConfDB::getConfigNewInfo(configId=" + configId + ") failed.";
			throw new DatabaseException(errMsg);
		}
		return result;
	}

	/** get all configuration names */
	public synchronized String[] getSwArchNames() throws DatabaseException {
		ArrayList<String> listOfNames = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = psSelectSwArchNames.executeQuery();
			while (rs.next())
				listOfNames.add(rs.getString(1));
		} catch (SQLException e) {
			String errMsg = "ConfDB::getSwArchNames() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
		return listOfNames.toArray(new String[listOfNames.size()]);
	}

	//
	// REMOVE CONFIGURATIONS / RELEASES
	//

	/** delete a configuration from the DB */
	public synchronized void removeConfiguration(int configId) throws DatabaseException {
		ResultSet rs = null;
		try {
			dbConnector.getConnection().setAutoCommit(false);

			// BSATARIC: empty calls (old DB)
			removeGlobalPSets(configId); // NOT WORKING
			removeEDSources(configId); // WORKS
			removeESSources(configId); // WORKS
			removeESModules(configId); // WORKS
			removeServices(configId); // WORKS
			removeSequences(configId); // NOT WORKING
			removeTasks(configId); // EMPTY
			removeSwitchProducers(configId); // EMPTY
			removePaths(configId); // HALF WORKING
			removeContent(configId); // NOT WORKING

			psDeleteConfiguration.setInt(1, configId);
			psDeleteConfiguration.executeUpdate();

			dbConnector.getConnection().commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbConnector.getConnection().rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw new DatabaseException("remove Configuration FAILED", e);
		} finally {
			try {
				dbConnector.getConnection().setAutoCommit(true);
			} catch (SQLException e) {
			}
			dbConnector.release(rs);
		}
	}

	/** remove Content */
	public synchronized void removeContent(int configId) throws SQLException {
		ResultSet rs1 = null;
		try {

			psSelectContentForConfig.setInt(1, configId);
			rs1 = psSelectContentForConfig.executeQuery(); // select Ids from EventContent.

			psDeleteContentFromConfig.setInt(1, configId);
			psDeleteContentFromConfig.executeUpdate(); // delete from ConfigurationContentAssoc.

			while (rs1.next()) {
				// for each EventContentId:
				int pEventId = rs1.getInt(1);
				ResultSet rs2 = null;

				// Delete Streams
				try {
					psSelectStreamByEventContent.setInt(1, pEventId);
					rs2 = psSelectStreamByEventContent.executeQuery();
					// delete ECStreamAssoc
					psDeleteECStreamFromEventCont.setInt(1, pEventId);
					psDeleteECStreamFromEventCont.executeUpdate();

					/*
					 * while(rs2.next()) { int pStreamId = rs2.getInt(1); ResultSet rs3 = null; try
					 * { psSelectStreamAssocByStream.setInt(1, pStreamId); rs3 =
					 * psSelectStreamAssocByStream.executeQuery();
					 * 
					 * // do not delete Streams but SuperIds. if(!rs3.next()) {
					 * removeParameters(pStreamId); psDeleteSuperId.setInt(1,pStreamId);
					 * psDeleteSuperId.executeUpdate(); }
					 * 
					 * } finally { dbConnector.release(rs3); } }
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

					while (rs2.next()) {
						int pECStatId = rs2.getInt(1);
						ResultSet rs3 = null;
						try {
							psSelectECStatementByECStatement.setInt(1, pECStatId);
							rs3 = psSelectECStatementByECStatement.executeQuery();

							if (!rs3.next()) {
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
	public synchronized void removeGlobalPSets(int configId) throws SQLException {
		ResultSet rs1 = null;
		try {
			psSelectPSetsForConfig.setInt(1, configId);
			rs1 = psSelectPSetsForConfig.executeQuery();
			psDeletePSetsFromConfig.setInt(1, configId);
			psDeletePSetsFromConfig.executeUpdate();
			while (rs1.next()) {
				int psetId = rs1.getInt(1);
				ResultSet rs2 = null;
				try {
					psSelectPSetId.setInt(1, psetId);
					rs2 = psSelectPSetId.executeQuery();

					if (!rs2.next()) {
						removeParameters(psetId);
						psDeleteSuperId.setInt(1, psetId);
						psDeleteSuperId.executeUpdate();
					}
				} finally {
					dbConnector.release(rs2);
				}
			}
		} finally {
			dbConnector.release(rs1);
		}
	}

	/** remove EDSources from a configuration */
	public synchronized void removeEDSources(int configId) throws SQLException {
		ResultSet rs1 = null;
		try {
			psSelectEDSourcesForConfig.setInt(1, configId);
			rs1 = psSelectEDSourcesForConfig.executeQuery();
			psDeleteEDSourcesFromConfig.setInt(1, configId);
			psDeleteEDSourcesFromConfig.executeUpdate();
			while (rs1.next()) {
				int edsId = rs1.getInt(1);
				ResultSet rs3 = null;
				try {
					psSelectEDSourceId.setInt(1, edsId);
					rs3 = psSelectEDSourceId.executeQuery();

					if (!rs3.next()) {
						removeParameters(edsId);
						psDeleteSuperId.setInt(1, edsId);
						psDeleteSuperId.executeUpdate();
					}
				} finally {
					dbConnector.release(rs3);
				}
			}
		} finally {
			dbConnector.release(rs1);
		}
	}

	/** remove ESSources */
	public synchronized void removeESSources(int configId) throws SQLException {
		ResultSet rs1 = null;
		try {
			psSelectESSourcesForConfig.setInt(1, configId);
			rs1 = psSelectESSourcesForConfig.executeQuery();
			psDeleteESSourcesFromConfig.setInt(1, configId);
			psDeleteESSourcesFromConfig.executeUpdate();
			while (rs1.next()) {
				int essId = rs1.getInt(1);
				ResultSet rs3 = null;
				try {
					psSelectESSourceId.setInt(1, essId);
					rs3 = psSelectESSourceId.executeQuery();

					if (!rs3.next()) {
						removeParameters(essId);
						psDeleteSuperId.setInt(1, essId);
						psDeleteSuperId.executeUpdate();
					}
				} finally {
					dbConnector.release(rs3);
				}
			}
		} finally {
			dbConnector.release(rs1);
		}
	}

	/** remove ESModules */
	public synchronized void removeESModules(int configId) throws SQLException {
		ResultSet rs1 = null;
		try {
			psSelectESModulesForConfig.setInt(1, configId);
			rs1 = psSelectESModulesForConfig.executeQuery();
			psDeleteESModulesFromConfig.setInt(1, configId);
			psDeleteESModulesFromConfig.executeUpdate();
			while (rs1.next()) {
				int esmId = rs1.getInt(1);
				ResultSet rs3 = null;
				try {
					psSelectESModuleId.setInt(1, esmId);
					rs3 = psSelectESModuleId.executeQuery();

					if (!rs3.next()) {
						removeParameters(esmId);
						psDeleteSuperId.setInt(1, esmId);
						psDeleteSuperId.executeUpdate();
					}
				} finally {
					dbConnector.release(rs3);
				}
			}
		} finally {
			dbConnector.release(rs1);
		}
	}

	/** remove Services */
	public synchronized void removeServices(int configId) throws SQLException {
		ResultSet rs1 = null;
		try {
			psSelectServicesForConfig.setInt(1, configId);
			rs1 = psSelectServicesForConfig.executeQuery();
			psDeleteServicesFromConfig.setInt(1, configId);
			psDeleteServicesFromConfig.executeUpdate();
			while (rs1.next()) {
				int svcId = rs1.getInt(1);
				ResultSet rs3 = null;
				try {
					psSelectServiceId.setInt(1, svcId);
					rs3 = psSelectServiceId.executeQuery();

					if (!rs3.next()) {
						removeParameters(svcId);
						psDeleteSuperId.setInt(1, svcId);
						psDeleteSuperId.executeUpdate();
					}
				} finally {
					dbConnector.release(rs3);
				}
			}
		} finally {
			dbConnector.release(rs1);
		}
	}

	/** remove Sequences */
	public synchronized void removeSequences(int configId) throws SQLException {
		ResultSet rs1 = null;
		try {
			psSelectSequencesForConfig.setInt(1, configId);
			rs1 = psSelectSequencesForConfig.executeQuery();
			psDeleteSequencesFromConfig.setInt(1, configId);
			psDeleteSequencesFromConfig.executeUpdate();
			while (rs1.next()) {
				int seqId = rs1.getInt(1);
				ResultSet rs2 = null;
				try {
					psSelectSequenceId.setInt(1, seqId);
					rs2 = psSelectSequenceId.executeQuery();

					if (!rs2.next()) {
						psDeleteChildSeqsFromParentSeq.setInt(1, seqId);
						psDeleteChildSeqsFromParentSeq.executeUpdate();
						psDeleteChildSeqFromParentSeqs.setInt(1, seqId);
						psDeleteChildSeqFromParentSeqs.executeUpdate();
						psDeleteChildSeqFromParentPaths.setInt(1, seqId);
						psDeleteChildSeqFromParentPaths.executeUpdate();
						ResultSet rs3 = null;
						try {
							psSelectModulesForSeq.setInt(1, seqId);
							rs3 = psSelectModulesForSeq.executeQuery();
							psDeleteModulesFromSeq.setInt(1, seqId);
							psDeleteModulesFromSeq.executeUpdate();

							while (rs3.next()) {
								int modId = rs3.getInt(1);
								removeModule(modId);
							}
						} finally {
							dbConnector.release(rs3);
						}
						psDeleteSequence.setInt(1, seqId);
						psDeleteSequence.executeUpdate();
					}
				} finally {
					dbConnector.release(rs2);
				}
			}
		} finally {
			dbConnector.release(rs1);
		}
	}

	/**
	 * TODO: remove Tasks (it seems this is not used for sequences as well - only
	 * testing purposes)
	 */
	public synchronized void removeTasks(int configId) throws SQLException {
	}

	/**
	 * TODO: remove SwitchProducers (it seems this is not used for sequences as well
	 * - only testing purposes)
	 */
	public synchronized void removeSwitchProducers(int configId) throws SQLException {
	}

	/** remove Paths */
	public synchronized void removePaths(int configId) throws SQLException {
		ResultSet rs1 = null;
		try {
			psSelectPathsForConfig.setInt(1, configId);
			rs1 = psSelectPathsForConfig.executeQuery();
			psDeletePathsFromConfig.setInt(1, configId);
			psDeletePathsFromConfig.executeUpdate();
			while (rs1.next()) {
				int pathId = rs1.getInt(1);
				ResultSet rs2 = null;
				try {
					psSelectPathId.setInt(1, pathId);
					rs2 = psSelectPathId.executeQuery();

					if (!rs2.next()) {
						psDeleteChildPathsFromParentPath.setInt(1, pathId);
						psDeleteChildPathsFromParentPath.executeUpdate();
						psDeleteChildPathFromParentPaths.setInt(1, pathId);
						psDeleteChildPathFromParentPaths.executeUpdate();
						psDeleteChildSeqsFromParentPath.setInt(1, pathId);
						psDeleteChildSeqsFromParentPath.executeUpdate();

						psDeletePathStreamDataSetAssoc.setInt(1, pathId);
						psDeletePathStreamDataSetAssoc.executeUpdate();
						psDeletePathOutputModAssoc.setInt(1, pathId);
						psDeletePathOutputModAssoc.executeUpdate();

						ResultSet rs3 = null;
						try {
							psSelectModulesForPath.setInt(1, pathId);
							rs3 = psSelectModulesForPath.executeQuery();
							psDeleteModulesFromPath.setInt(1, pathId);
							psDeleteModulesFromPath.executeUpdate();

							while (rs3.next()) {
								int modId = rs3.getInt(1);
								removeModule(modId);
							}
						} finally {
							dbConnector.release(rs3);
						}
						psDeletePath.setInt(1, pathId);
						psDeletePath.executeUpdate();
					}
				} finally {
					dbConnector.release(rs2);
				}
			}
		} finally {
			dbConnector.release(rs1);
		}
	}

	/** remove Modules */
	public synchronized void removeModule(int modId) throws SQLException {
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		try {
			psSelectModuleIdBySeq.setInt(1, modId);
			rs1 = psSelectModuleIdBySeq.executeQuery();
			psSelectModuleIdByPath.setInt(1, modId);
			rs2 = psSelectModuleIdByPath.executeQuery();
			if (!rs1.next() && !rs2.next()) {
				removeParameters(modId);
				psDeleteSuperId.setInt(1, modId);
				psDeleteSuperId.executeUpdate();
			}
		} finally {
			dbConnector.release(rs1);
			dbConnector.release(rs2);
		}
	}

	/** remove Parameters */
	public synchronized void removeParameters(int parentId) throws SQLException {
		ResultSet rsParams = null;
		ResultSet rsPSets = null;
		ResultSet rsVPSets = null;

		try {
			// parameters
			psSelectParametersForSuperId.setInt(1, parentId);
			rsParams = psSelectParametersForSuperId.executeQuery();
			psDeleteParametersForSuperId.setInt(1, parentId);
			psDeleteParametersForSuperId.executeUpdate();
			while (rsParams.next()) {
				int paramId = rsParams.getInt(1);
				try {
					psDeleteParameter.setInt(1, paramId);
					psDeleteParameter.executeUpdate();
				}
				// TEST
				catch (SQLException e) {
					System.out.println("parentId=" + parentId + ", " + "paramId=" + paramId + ": " + "NOT REMOVED!");
				}
				// END TEST
			}
		} finally {
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

			rsPSets = stmt1.executeQuery("SELECT psetId " + "FROM SuperIdParamSetAssoc " + "WHERE superId=" + parentId);
			while (rsPSets.next()) {
				int psetId = rsPSets.getInt(1);
				removeParameters(psetId);
				stmt2.executeUpdate("DELETE FROM SuperIdParamSetAssoc " + "WHERE " + "superId=" + parentId
						+ " AND psetId=" + psetId);
				stmt3.executeUpdate("DELETE FROM SuperIds WHERE superId=" + psetId);
			}
		} finally {
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

			rsVPSets = stmt1
					.executeQuery("SELECT vpsetId " + "FROM SuperIdVecParamSetAssoc " + "WHERE superId=" + parentId);
			while (rsVPSets.next()) {
				int vpsetId = rsVPSets.getInt(1);
				removeParameters(vpsetId);
				stmt2.executeUpdate("DELETE FROM SuperIdVecParamSetAssoc " + "WHERE " + "superId=" + parentId
						+ " AND vpsetId=" + vpsetId);
				stmt3.executeUpdate("DELETE FROM SuperIds WHERE superId=" + vpsetId);
			}
		} finally {
			dbConnector.release(rsVPSets);
			stmt1.close();
			stmt2.close();
			stmt3.close();
		}
	}

	//
	// INSERT SOFTWARE RELEASE
	//
	public synchronized void insertRelease(String releaseTag, SoftwareRelease newRelease) throws DatabaseException {
		try {
			dbConnector.getConnection().setAutoCommit(false);

			psSelectReleaseId.setString(1, releaseTag);
			ResultSet rs = psSelectReleaseId.executeQuery();
			if (rs.next())
				return;
			psInsertReleaseTag.setString(1, releaseTag);
			psInsertReleaseTag.executeUpdate();
			ResultSet rsInsertReleaseTag = psInsertReleaseTag.getGeneratedKeys();
			rsInsertReleaseTag.next();
			int releaseId = rsInsertReleaseTag.getInt(1);
			insertSoftwareSubsystem(newRelease, releaseId);

			/*
			 * was already psInsertEDSourceTemplate.executeBatch();
			 * psInsertESSourceTemplate.executeBatch();
			 * psInsertESModuleTemplate.executeBatch();
			 * psInsertServiceTemplate.executeBatch();
			 * psInsertModuleTemplate.executeBatch();
			 */

			// insert parameter bindings / values
			// psInsertParameterSet.executeBatch();
			// psInsertVecParameterSet.executeBatch();
			// psInsertGlobalPSet.executeBatch();
			// psInsertSuperIdParamAssoc.executeBatch();
			// psInsertSuperIdParamSetAssoc.executeBatch();
			// psInsertSuperIdVecParamSetAssoc.executeBatch();
			Iterator<PreparedStatement> itPS = insertParameterHashMap.values().iterator();
			while (itPS.hasNext()) {
				PreparedStatement itP = itPS.next();
				if (itP != null)
					itP.executeBatch();
			}

			dbConnector.getConnection().commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbConnector.getConnection().rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw new DatabaseException("removeSoftwareRelease FAILED", e);
		} finally {
			try {
				dbConnector.getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void insertSoftwareSubsystem(SoftwareRelease newRelease, int releaseId) throws SQLException {
		Iterator<SoftwareSubsystem> subsysIt = newRelease.subsystemIterator();
		while (subsysIt.hasNext()) {
			SoftwareSubsystem subsys = subsysIt.next();

			ResultSet rsSelectSoftwareSubsystemId;
			psSelectSoftwareSubsystemId.setString(1, subsys.name());
			rsSelectSoftwareSubsystemId = psSelectSoftwareSubsystemId.executeQuery();

			int subsysId;
			if (rsSelectSoftwareSubsystemId.next()) {
				subsysId = rsSelectSoftwareSubsystemId.getInt(1);
			} else {
				psInsertSoftwareSubsystem.setString(1, subsys.name());
				psInsertSoftwareSubsystem.executeUpdate();
				ResultSet rsInsertSoftwareSubsystem = psInsertSoftwareSubsystem.getGeneratedKeys();
				rsInsertSoftwareSubsystem.next();
				subsysId = rsInsertSoftwareSubsystem.getInt(1);
			}
			insertSoftwarePackages(subsys, subsysId, releaseId);
		}
	}

	public synchronized void insertSoftwarePackages(SoftwareSubsystem subsys, int subsysId, int releaseId)
			throws SQLException {

		Iterator<SoftwarePackage> pkgIt = subsys.packageIterator();
		while (pkgIt.hasNext()) {
			SoftwarePackage pkg = pkgIt.next();

			ResultSet rsSelectSoftwarePackageId;
			psSelectSoftwarePackageId.setInt(1, subsysId);
			psSelectSoftwarePackageId.setString(2, pkg.name());
			rsSelectSoftwarePackageId = psSelectSoftwarePackageId.executeQuery();

			int pkgId;
			if (rsSelectSoftwarePackageId.next()) {
				pkgId = rsSelectSoftwarePackageId.getInt(1);
			} else {
				psInsertSoftwarePackage.setInt(1, subsysId);
				psInsertSoftwarePackage.setString(2, pkg.name());
				psInsertSoftwarePackage.executeUpdate();
				ResultSet rsInsertSoftwarePackage = psInsertSoftwarePackage.getGeneratedKeys();
				rsInsertSoftwarePackage.next();
				pkgId = rsInsertSoftwarePackage.getInt(1);
			}
			insertTemplateIntoRelease(pkg, pkgId, releaseId);
		}
	}

	public synchronized void insertTemplateIntoRelease(SoftwarePackage softwarePackage, int pkgId, int releaseId)
			throws SQLException {
		Iterator<Template> templateIt = softwarePackage.templateIterator();
		while (templateIt.hasNext()) {

			Template template = templateIt.next();
			int templateId = -1;
			try {

				ResultSet rs;

				if (template instanceof EDSourceTemplate) {
					psInsertEDSourceTemplateRelease.setString(1, template.name());
					psInsertEDSourceTemplateRelease.setString(2, template.cvsTag());
					psInsertEDSourceTemplateRelease.setInt(3, pkgId);
					psInsertEDSourceTemplateRelease.executeUpdate();
					rs = psInsertEDSourceTemplateRelease.getGeneratedKeys();
					if (rs.next()) {
						templateId = rs.getInt(1);
						psInsertEDSourceT2Rele.setInt(1, templateId);
						psInsertEDSourceT2Rele.setInt(2, releaseId);
						psInsertEDSourceT2Rele.executeUpdate();
						insertTemplateParameters(templateId, template, psInsertParameterEDST);
					}
				} else if (template instanceof ESSourceTemplate) {

					psInsertESSourceTemplateRelease.setString(1, template.name());
					psInsertESSourceTemplateRelease.setString(2, template.cvsTag());
					psInsertESSourceTemplateRelease.setInt(3, pkgId);
					psInsertESSourceTemplateRelease.executeUpdate();
					rs = psInsertESSourceTemplateRelease.getGeneratedKeys();
					if (rs.next()) {
						templateId = rs.getInt(1);
						psInsertESSourceT2Rele.setInt(1, templateId);
						psInsertESSourceT2Rele.setInt(2, releaseId);
						psInsertESSourceT2Rele.executeUpdate();
						insertTemplateParameters(templateId, template, psInsertParameterESST);
					}
				} else if (template instanceof ESModuleTemplate) {
					psInsertESModuleTemplateRelease.setString(1, template.name());
					psInsertESModuleTemplateRelease.setString(2, template.cvsTag());
					psInsertESModuleTemplateRelease.setInt(3, pkgId);
					System.out.println(
							"ESM name=" + template.name() + " cvsTag=" + template.cvsTag() + " pkgId=" + pkgId);
					psInsertESModuleTemplateRelease.executeUpdate();
					rs = psInsertESModuleTemplateRelease.getGeneratedKeys();
					if (rs.next()) {
						templateId = rs.getInt(1);
						psInsertESModuleT2Rele.setInt(1, templateId);
						psInsertESModuleT2Rele.setInt(2, releaseId);
						psInsertESModuleT2Rele.executeUpdate();
						insertTemplateParameters(templateId, template, psInsertParameterESMT);
					}

				} else if (template instanceof ServiceTemplate) {

					psInsertServiceTemplateRelease.setString(1, template.name());
					psInsertServiceTemplateRelease.setString(2, template.cvsTag());
					psInsertServiceTemplateRelease.setInt(3, pkgId);
					psInsertServiceTemplateRelease.executeUpdate();
					rs = psInsertServiceTemplateRelease.getGeneratedKeys();
					if (rs.next()) {
						templateId = rs.getInt(1);
						psInsertServiceT2Rele.setInt(1, templateId);
						psInsertServiceT2Rele.setInt(2, releaseId);
						psInsertServiceT2Rele.executeUpdate();
						insertTemplateParameters(templateId, template, psInsertParameterSRVT);
					}
				} else if (template instanceof ModuleTemplate) {
					int moduleType = moduleTypeIdHashMap.get(template.type());
					psInsertModuleTemplateRelease.setInt(1, moduleType);
					psInsertModuleTemplateRelease.setString(2, template.name());
					psInsertModuleTemplateRelease.setString(3, template.cvsTag());
					psInsertModuleTemplateRelease.setInt(4, pkgId);
					psInsertModuleTemplateRelease.executeUpdate();
					rs = psInsertModuleTemplateRelease.getGeneratedKeys();
					if (rs.next()) {
						templateId = rs.getInt(1);
						psInsertModuleT2Rele.setInt(1, templateId);
						psInsertModuleT2Rele.setInt(2, releaseId);
						psInsertModuleT2Rele.executeUpdate();
						insertTemplateParameters(templateId, template, psInsertParameterMODT);
					}
				}
				template.setDatabaseId(templateId);
			} catch (DatabaseException e2) {
				e2.printStackTrace();
			}
		}
	}

	/** insert all instance parameters */
	private void insertTemplateParameters(int superId, Template template, PreparedStatement dbtable)
			throws DatabaseException {
		for (int sequenceNb = 0; sequenceNb < template.parameterCount(); sequenceNb++) {
			Parameter p = template.parameter(sequenceNb);
			if (p instanceof VPSetParameter) {
				VPSetParameter vpset = (VPSetParameter) p;
				insertVecParameterSet(superId, sequenceNb, 0, vpset, dbtable);
			} else if (p instanceof PSetParameter) {
				PSetParameter pset = (PSetParameter) p;
				insertParameterSet(superId, sequenceNb, 0, pset, dbtable);
			} else {
				insertParameter(superId, sequenceNb, 0, p, dbtable);
			}
		}
	}

	//
	// REMOVE SOFTWARE-RELEASE
	//

	/** remove a software release from the DB */
	public synchronized void removeSoftwareRelease(int releaseId) throws DatabaseException {
		if (getConfigNamesByRelease(releaseId).length > 0) {
			System.err.println("ConfDB::removeSoftwareRelease ERROR: " + "Can't remove release with associated "
					+ "configurations!)");
			return;
		}

		try {
			dbConnector.getConnection().setAutoCommit(false);

			removeEDSourceTemplates(releaseId);
			removeESSourceTemplates(releaseId);
			removeESModuleTemplates(releaseId);
			removeServiceTemplates(releaseId);
			removeModuleTemplates(releaseId);

			psDeleteSoftwareRelease.setInt(1, releaseId);
			psDeleteSoftwareRelease.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbConnector.getConnection().rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw new DatabaseException("removeSoftwareRelease FAILED", e);
		} finally {
			try {
				dbConnector.getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/** remove EDSourceTemplates from a release */
	private void removeEDSourceTemplates(int releaseId) throws SQLException {
		ResultSet rs = null;
		try {
			psSelectEDSourceTemplatesForRelease.setInt(1, releaseId);
			rs = psSelectEDSourceTemplatesForRelease.executeQuery();
			removeTemplates(rs, releaseId);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** remove ESSourceTemplates from a release */
	private void removeESSourceTemplates(int releaseId) throws SQLException {
		ResultSet rs = null;
		try {
			psSelectESSourceTemplatesForRelease.setInt(1, releaseId);
			rs = psSelectESSourceTemplatesForRelease.executeQuery();
			removeTemplates(rs, releaseId);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** remove ESModuleTemplates from a release */
	private void removeESModuleTemplates(int releaseId) throws SQLException {
		ResultSet rs = null;
		try {
			psSelectESModuleTemplatesForRelease.setInt(1, releaseId);
			rs = psSelectESModuleTemplatesForRelease.executeQuery();
			removeTemplates(rs, releaseId);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** remove ServiceTemplates from a release */
	private void removeServiceTemplates(int releaseId) throws SQLException {
		ResultSet rs = null;
		try {
			psSelectServiceTemplatesForRelease.setInt(1, releaseId);
			rs = psSelectServiceTemplatesForRelease.executeQuery();
			removeTemplates(rs, releaseId);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** remove ModuleTemplates from a release */
	private void removeModuleTemplates(int releaseId) throws SQLException {
		ResultSet rs = null;
		try {
			psSelectModuleTemplatesForRelease.setInt(1, releaseId);
			rs = psSelectModuleTemplatesForRelease.executeQuery();
			removeTemplates(rs, releaseId);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** remove templates of any kind from release, given the superIds */
	private void removeTemplates(ResultSet rs, int releaseId) throws SQLException {
		while (rs.next()) {
			int superId = rs.getInt(1);

			psDeleteTemplateFromRelease.setInt(1, superId);
			psDeleteTemplateFromRelease.setInt(2, releaseId);
			psDeleteTemplateFromRelease.executeUpdate();

			ResultSet rs3 = null;
			try {
				psSelectTemplateId.setInt(1, superId);
				rs3 = psSelectTemplateId.executeQuery();

				if (!rs3.next()) {
					removeParameters(superId);
					psDeleteSuperId.setInt(1, superId);
					psDeleteSuperId.executeUpdate();
				}
			}

			// DEBUG
			catch (SQLException e) {
				System.out.println("releaseId=" + releaseId + " " + "superId=" + superId + "\n");
				throw (e);
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
	private void prepareStatements() throws DatabaseException {
		int[] keyColumn = { 1 };

		try {
			//
			// SELECT
			//

			psSelectModuleTypes = dbConnector.getConnection()
					.prepareStatement("SELECT" + " u_moduletypes.id," + " u_moduletypes.type " + "FROM u_moduletypes");
			preparedStatements.add(psSelectModuleTypes);

			psSelectParameterTypes = dbConnector.getConnection()
					.prepareStatement("SELECT" + " DISTINCT u_moelements.paramtype FROM u_moelements");
			preparedStatements.add(psSelectParameterTypes);

			psSelectDirectories = dbConnector.getConnection()
					.prepareStatement("SELECT" + " Directories.id," + " Directories.id_parentDir,"
							+ " Directories.name," + " Directories.created " + "FROM u_directories Directories " +
							// "ORDER BY Directories.id,Directories.name ASC");
							"ORDER BY Directories.name ASC");
			psSelectDirectories.setFetchSize(512);
			preparedStatements.add(psSelectDirectories);

			psSelectConfigurations = dbConnector.getConnection().prepareStatement("SELECT" + " Configurations.Id,"
					+ " Configurations.id_parentDir," + " Configurations.config," + " Configurations.version,"
					+ " Configurations.created," + " Configurations.creator," + " SoftwareReleases.releaseTag,"
					+ " Configurations.processName," + " Configurations.description "
					+ "FROM u_confversions Configurations " + "JOIN u_softreleases SoftwareReleases "
					+ "ON SoftwareReleases.Id = Configurations.id_release " + "ORDER BY Configurations.config ASC");
			psSelectConfigurations.setFetchSize(512);
			preparedStatements.add(psSelectConfigurations);

			psSelectLockedConfigurations = dbConnector.getConnection()
					.prepareStatement("SELECT" + " u_directories.Name," + " u_lockedconfs.id_config,"
							+ " u_lockedconfs.userName " + "FROM u_lockedconfs " + "JOIN u_directories "
							+ "ON u_lockedconfs.id_parentdir = u_directories.id");
			preparedStatements.add(psSelectLockedConfigurations);

			psSelectUsersForLockedConfigs = dbConnector.getConnection()
					.prepareStatement("SELECT" + " u_lockedconfs.userName " + "FROM u_lockedconfs");
			preparedStatements.add(psSelectUsersForLockedConfigs);

			psSelectSwArchNames = dbConnector.getConnection().prepareStatement(
					"select unique(SW_ARCH) HLT_SW_ARCH from ( select CMSSW_REL || ' ' || ARCH as SW_ARCH from HLT_CMSSW_ARCH where CMSSW_REL is not null) order by HLT_SW_ARCH desc");
			psSelectSwArchNames.setFetchSize(1024);
			preparedStatements.add(psSelectSwArchNames);

			psSelectOrigDbId = dbConnector.getConnection()
					.prepareStatement("select configid from u_confversions where id=? ");
			preparedStatements.add(psSelectOrigDbId);

			psSelectNewDbId = dbConnector.getConnection()
					.prepareStatement("select id from u_confversions where configid=? ");
			preparedStatements.add(psSelectNewDbId);

			psSelectConfigNames = dbConnector.getConnection()
					.prepareStatement("SELECT DISTINCT" + " Directories.name," + " Configurations.config "
							+ "FROM u_confversions Configurations " + "JOIN u_directories Directories "
							+ "ON Configurations.id_parentDir = Directories.id "
							+ "ORDER BY Directories.name ASC,Configurations.config ASC");
			psSelectConfigNames.setFetchSize(1024);
			preparedStatements.add(psSelectConfigNames);

			psSelectConfigNamesByRelease = dbConnector.getConnection()
					.prepareStatement("SELECT DISTINCT" + " Directories.name," + " Configurations.config, "
							+ " Configurations.version " + "FROM u_confversions Configurations "
							+ "JOIN u_directories Directories " + "ON Configurations.id_parentDir = Directories.id "
							+ "WHERE Configurations.id_release = ?"
							+ "ORDER BY Directories.name ASC,Configurations.config ASC");
			psSelectConfigNamesByRelease.setFetchSize(1024);
			preparedStatements.add(psSelectConfigNamesByRelease);

			psSelectDirectoryId = dbConnector.getConnection().prepareStatement(
					"SELECT" + " Directories.id " + "FROM u_directories Directories " + "WHERE Directories.name = ?");
			preparedStatements.add(psSelectDirectoryId);

			psSelectConfigurationId = dbConnector.getConnection()
					.prepareStatement("SELECT" + " Configurations.id " + "FROM u_confversions Configurations "
							+ "JOIN u_directories Directories " + "ON Directories.id=Configurations.id_parentDir "
							+ "WHERE Directories.name = ? AND" + " Configurations.config = ? AND"
							+ " Configurations.version = ?");
			preparedStatements.add(psSelectConfigurationId);

			psSelectConfigurationIdLatest = dbConnector.getConnection()
					.prepareStatement("SELECT" + " Configurations.id," + " Configurations.version "
							+ "FROM u_confversions Configurations " + "JOIN u_directories Directories "
							+ "ON Directories.id=Configurations.id_parentDir " + "WHERE Directories.name = ? AND"
							+ " Configurations.config = ? " + "ORDER BY Configurations.version DESC");
			preparedStatements.add(psSelectConfigurationIdLatest);

			psSelectConfigurationCreated = dbConnector.getConnection()
					.prepareStatement("SELECT" + " Configurations.created " + "FROM u_confversions Configurations "
							+ "WHERE Configurations.id = ?");
			preparedStatements.add(psSelectConfigurationCreated);

			psSelectReleaseTags = dbConnector.getConnection()
					.prepareStatement("SELECT" + " SoftwareReleases.id," + " SoftwareReleases.releaseTag "
							+ "FROM u_softreleases SoftwareReleases " + "ORDER BY SoftwareReleases.id DESC");
			psSelectReleaseTags.setFetchSize(32);
			preparedStatements.add(psSelectReleaseTags);

			psSelectReleaseTagsSorted = dbConnector.getConnection()
					.prepareStatement("SELECT" + " SoftwareReleases.id," + " SoftwareReleases.releaseTag "
							+ "FROM u_softreleases SoftwareReleases " + "ORDER BY SoftwareReleases.releaseTag ASC");
			psSelectReleaseTagsSorted.setFetchSize(32);
			preparedStatements.add(psSelectReleaseTagsSorted);

			psSelectReleaseId = dbConnector.getConnection().prepareStatement("SELECT" + " SoftwareReleases.id "
					+ "FROM u_softreleases SoftwareReleases " + "WHERE SoftwareReleases.releaseTag = ?");

			psSelectReleaseTag = dbConnector.getConnection().prepareStatement("SELECT" + " SoftwareReleases.releaseTag "
					+ "FROM u_softreleases SoftwareReleases " + "WHERE SoftwareReleases.id = ?");
			preparedStatements.add(psSelectReleaseTag);

			psSelectReleaseTagForConfig = dbConnector.getConnection()
					.prepareStatement("SELECT" + " SoftwareReleases.releaseTag "
							+ "FROM u_softreleases SoftwareReleases " + "JOIN u_confversions Configurations "
							+ "ON Configurations.id_release = SoftwareReleases.id " + "WHERE Configurations.id = ?");
			preparedStatements.add(psSelectReleaseTagForConfig);

			psSelectSoftwareSubsystems = dbConnector.getConnection()
					.prepareStatement("SELECT id, name FROM u_softsubsystems");
			psSelectSoftwareSubsystems.setFetchSize(64);
			preparedStatements.add(psSelectSoftwareSubsystems);

			psSelectSoftwarePackages = dbConnector.getConnection()
					.prepareStatement("SELECT id, id_subs, name FROM u_softpackages");
			psSelectSoftwarePackages.setFetchSize(512);
			preparedStatements.add(psSelectSoftwarePackages);

			psSelectEDSourceTemplate = dbConnector.getConnection()
					.prepareStatement("SELECT" + " EDSourceTemplates.id," + " EDSourceTemplates.name,"
							+ " EDSourceTemplates.cvstag " + "FROM u_edstemplates EDSourceTemplates "
							+ "WHERE EDSourceTemplates.name=? AND EDSourceTemplates.cvstag= ?");
			preparedStatements.add(psSelectEDSourceTemplate);

			psSelectESSourceTemplate = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ESSourceTemplates.id," + " ESSourceTemplates.name,"
							+ " ESSourceTemplates.cvstag " + "FROM u_esstemplates ESSourceTemplates "
							+ "WHERE name=? AND cvstag=?");
			preparedStatements.add(psSelectESSourceTemplate);

			psSelectESModuleTemplate = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ESModuleTemplates.id," + " ESModuleTemplates.name,"
							+ " ESModuleTemplates.cvstag " + "FROM u_esmtemplates ESModuleTemplates "
							+ "WHERE name=? AND cvstag=?");
			preparedStatements.add(psSelectESModuleTemplate);

			psSelectServiceTemplate = dbConnector.getConnection().prepareStatement(
					"SELECT" + " ServiceTemplates.id," + " ServiceTemplates.name," + " ServiceTemplates.cvstag "
							+ "FROM u_srvtemplates ServiceTemplates " + "WHERE name=? AND cvstag=?");
			preparedStatements.add(psSelectServiceTemplate);

			psSelectModuleTemplate = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ModuleTemplates.id," + " ModuleTemplates.id_mtype,"
							+ " ModuleTemplates.name," + " ModuleTemplates.cvstag "
							+ "FROM u_moduletemplates ModuleTemplates " + "WHERE name=? AND cvstag=?");
			preparedStatements.add(psSelectModuleTemplate);

			psSelectStreams = dbConnector.getConnection().prepareStatement("SELECT" + " Streams.id," + " Streams.name "
					+ "FROM u_streams Streams " + "ORDER BY Streams.name ASC");

			psSelectPrimaryDatasets = dbConnector.getConnection()
					.prepareStatement("SELECT" + " PrimaryDatasets.id," + " PrimaryDatasets.name "
							+ "FROM u_datasets PrimaryDatasets " + "ORDER BY PrimaryDatasets.name ASC");

			psSelectEDSourcesForConfig = dbConnector.getConnection()
					.prepareStatement("SELECT" + " EDSources.id " + "FROM u_edsources EDSources "
							+ "JOIN u_conf2eds ConfigurationEDSourceAssoc "
							+ "ON ConfigurationEDSourceAssoc.id_edsource=EDSources.id "
							+ "WHERE ConfigurationEDSourceAssoc.id_confver=?");
			preparedStatements.add(psSelectEDSourcesForConfig);

			psSelectESSourcesForConfig = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ESSources.id " + "FROM u_essources ESSources "
							+ "JOIN u_conf2ess ConfigurationESSourceAssoc "
							+ "ON ConfigurationESSourceAssoc.id_essource=ESSources.id "
							+ "WHERE ConfigurationESSourceAssoc.id_confver=?");
			preparedStatements.add(psSelectESSourcesForConfig);

			psSelectESModulesForConfig = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ESModules.id " + "FROM u_esmodules ESModules "
							+ "JOIN u_conf2esm ConfigurationESModuleAssoc "
							+ "ON ConfigurationESModuleAssoc.id_esmodule=ESModules.id "
							+ "WHERE ConfigurationESModuleAssoc.id_confver=?");
			preparedStatements.add(psSelectESModulesForConfig);

			psSelectServicesForConfig = dbConnector.getConnection()
					.prepareStatement("SELECT" + " Services.id " + "FROM u_services Services "
							+ "JOIN u_conf2srv ConfigurationServiceAssoc "
							+ "ON ConfigurationServiceAssoc.id_service=Services.id "
							+ "WHERE ConfigurationServiceAssoc.id_confver=?");
			preparedStatements.add(psSelectServicesForConfig);

			psSelectPathsForConfig = dbConnector.getConnection().prepareStatement(
					"SELECT u_pathids.id  FROM u_pathids   JOIN u_pathid2conf ON u_pathid2conf.id_pathid=u_pathids.id "
							+ "WHERE u_pathid2conf.id_confver=?  order by u_pathid2conf.id_pathid");
			preparedStatements.add(psSelectPathsForConfig);

			psSelectEDSourceTemplatesForRelease = dbConnector.getConnection()
					.prepareStatement("SELECT" + " EDSourceTemplates.id " + "FROM u_edstemplates EDSourceTemplates "
							+ "JOIN u_edst2rele SuperIdReleaseAssoc "
							+ "ON SuperIdReleaseAssoc.id_edstemplate=EDSourceTemplates.id "
							+ "WHERE SuperIdReleaseAssoc.id_release=?");
			preparedStatements.add(psSelectEDSourceTemplatesForRelease);

			psSelectESSourceTemplatesForRelease = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ESSourceTemplates.id " + "FROM u_esstemplates ESSourceTemplates "
							+ "JOIN u_esst2rele SuperIdReleaseAssoc "
							+ "ON SuperIdReleaseAssoc.id_esstemplate=ESSourceTemplates.id "
							+ "WHERE SuperIdReleaseAssoc.id_release=?");
			preparedStatements.add(psSelectESSourceTemplatesForRelease);

			psSelectESModuleTemplatesForRelease = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ESModuleTemplates.id " + "FROM u_esmtemplates ESModuleTemplates "
							+ "JOIN u_esmt2rele SuperIdReleaseAssoc "
							+ "ON SuperIdReleaseAssoc.id_esmtemplate=ESModuleTemplates.id "
							+ "WHERE SuperIdReleaseAssoc.id_release=?");
			preparedStatements.add(psSelectESModuleTemplatesForRelease);

			psSelectServiceTemplatesForRelease = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ServiceTemplates.id " + "FROM u_srvtemplates ServiceTemplates "
							+ "JOIN u_srvt2rele SuperIdReleaseAssoc "
							+ "ON SuperIdReleaseAssoc.id_srvtemplate=ServiceTemplates.id "
							+ "WHERE SuperIdReleaseAssoc.id_release=?");
			preparedStatements.add(psSelectServiceTemplatesForRelease);

			psSelectModuleTemplatesForRelease = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ModuleTemplates.id " + "FROM u_moduletemplates ModuleTemplates "
							+ "JOIN u_modt2rele SuperIdReleaseAssoc "
							+ "ON SuperIdReleaseAssoc.id_modtemplate=ModuleTemplates.id "
							+ "WHERE SuperIdReleaseAssoc.id_release=?");
			preparedStatements.add(psSelectModuleTemplatesForRelease);

			psSelectEDSourceId = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ConfigurationEDSourceAssoc.id_edsource "
							+ "FROM u_conf2eds ConfigurationEDSourceAssoc "
							+ "WHERE ConfigurationEDSourceAssoc.id_edsource=?");
			preparedStatements.add(psSelectEDSourceId);

			psSelectESSourceId = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ConfigurationESSourceAssoc.id_essource "
							+ "FROM u_conf2ess ConfigurationESSourceAssoc "
							+ "WHERE ConfigurationESSourceAssoc.id_essource=?");
			preparedStatements.add(psSelectESSourceId);

			psSelectESModuleId = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ConfigurationESModuleAssoc.id_esmodule "
							+ "FROM u_conf2edm ConfigurationESModuleAssoc "
							+ "WHERE ConfigurationESModuleAssoc.id_esmodule=?");
			preparedStatements.add(psSelectESModuleId);

			preparedStatements.add(psSelectESModuleId);

			psSelectServiceId = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ConfigurationServiceAssoc.id_service "
							+ "FROM u_conf2srv ConfigurationServiceAssoc "
							+ "WHERE ConfigurationServiceAssoc.id_service=?");
			preparedStatements.add(psSelectServiceId);

			psSelectSequenceId = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ConfigurationSequenceAssoc.sequenceId "
							+ "FROM ConfigurationSequenceAssoc " + "WHERE ConfigurationSequenceAssoc.sequenceId=?");
			preparedStatements.add(psSelectSequenceId);

			psSelectTaskId = dbConnector.getConnection().prepareStatement("SELECT" + " ConfigurationTaskAssoc.taskId "
					+ "FROM ConfigurationTaskAssoc " + "WHERE ConfigurationTaskAssoc.taskId=?");
			preparedStatements.add(psSelectTaskId);

			psSelectSwitchProducerId = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ConfigurationSwitchProducerAssoc.switchProducerId "
							+ "FROM ConfigurationSwitchProducerAssoc "
							+ "WHERE ConfigurationSwitchProducerAssoc.switchProducerId=?");
			preparedStatements.add(psSelectSwitchProducerId);

			psSelectPathId = dbConnector.getConnection()
					.prepareStatement("SELECT" + " ConfigurationPathAssoc.id_pathid "
							+ "FROM u_pathid2conf ConfigurationPathAssoc "
							+ "WHERE ConfigurationPathAssoc.id_pathid=?");
			preparedStatements.add(psSelectPathId);

			// Event Content, Streams and Primary Datsets

			psSelectEventContentEntries = dbConnector.getConnection().prepareStatement(
					"select u_eventcontentids.id,u_eventcontents.name from u_eventcontents,u_eventcontentids,u_conf2evco where u_eventcontentids.id=u_conf2evco.id_evcoid and u_eventcontents.id=u_eventcontentids.id_evco and u_conf2evco.id_confver=?");
			psSelectEventContentEntries.setFetchSize(1024);
			preparedStatements.add(psSelectEventContentEntries);

			psSelectStreamEntries = dbConnector.getConnection().prepareStatement(
					"SELECT DISTINCT u_streamids.id+5000000,u_streams.name,u_streamids.FRACTODISK,u_EVENTCONTENTIDS.ID "
							+ "as evcoid,u_EVENTCONTENTS.name as evconame "
							+ "FROM u_streamids,u_streams,u_EVENTCONTENTIDS,u_EVENTCONTENTS,u_EVCO2STREAM,u_conf2evco "
							+ "WHERE u_streams.id=u_streamids.id_stream AND u_EVENTCONTENTIDS.ID_EVCO=u_EVENTCONTENTS.ID "
							+ "AND u_EVCO2STREAM.id_evcoid=u_EVENTCONTENTIDS.ID AND u_EVCO2STREAM.ID_STREAMID=u_streamids.id "
							+ "AND u_conf2evco.id_evcoid=u_EVENTCONTENTIDS.ID AND u_conf2evco.id_confver = ? order by u_streams.name");
			psSelectStreamEntries.setFetchSize(1024);
			preparedStatements.add(psSelectStreamEntries);

			psSelectDatasetEntries = dbConnector.getConnection().prepareStatement(
					"select distinct u_datasetids.id, u_datasets.name,u_streamids.id+5000000 as streamid,u_streams.name as label from u_conf2strdst,u_datasetids,u_datasets,u_streams,u_streamids WHERE   u_datasets.id=u_datasetids.id_dataset and u_datasetids.id=u_conf2strdst.id_datasetid and u_streams.id=u_streamids.id_stream and u_streamids.id=u_conf2strdst.id_streamid and u_conf2strdst.id_confver = ? order by id");

			preparedStatements.add(psSelectDatasetEntries);

			psSelectPathStreamDatasetEntries = dbConnector.getConnection().prepareStatement(
					"SELECT distinct u_pathid2conf.id_pathid,u_evco2stream.id_streamid+5000000 as streamid,"
							+ "u_pathid2strdst.id_datasetid  datasetid FROM u_pathid2strdst, u_pathid2conf, "
							+ "u_evco2stream, u_conf2evco, u_conf2strdst WHERE u_pathid2strdst.id_pathid=u_pathid2conf.id_pathid "
							+ "and u_evco2stream.id_streamid = u_pathid2strdst.id_streamid AND "
							+ "u_evco2stream.id_evcoid=u_conf2evco.id_evcoid and u_conf2evco.id_confver=u_pathid2conf.id_confver "
							+ "and u_conf2strdst.id_datasetid=u_pathid2strdst.id_datasetid and "
							+ "u_conf2strdst.id_confver= u_pathid2conf.id_confver and u_pathid2conf.id_confver=?");

			preparedStatements.add(psSelectPathStreamDatasetEntries);

			psSelectStatementId = dbConnector.getConnection()
					.prepareStatement("SELECT id from u_evcoStatements WHERE classN = ? "
							+ " AND moduleL = ? AND extraN = ? AND processN = ? AND statementType = ? ");
			preparedStatements.add(psSelectStatementId);

			psSelectEventContentStatements = dbConnector.getConnection().prepareStatement(
					"select distinct u_evcostatements.id as statemId, u_evcostatements.classn,u_evcostatements.modulel,u_evcostatements.extran,u_evcostatements.processn,u_evcostatements.statementtype,u_eventcontentids.id as evcoid, u_evco2stat.statementrank,u_eventcontents.name,u_evco2stat.id_pathid from u_eventcontents,u_eventcontentids,u_evcostatements, u_conf2evco, u_evco2stat where u_eventcontents.id=u_eventcontentids.id_evco and u_evcostatements.id=u_evco2stat.id_stat and u_evco2stat.id_evcoid=u_conf2evco.id_evcoid and u_eventcontentids.id=u_conf2evco.id_evcoid and u_conf2evco.id_confver=? order by statemid");
			preparedStatements.add(psSelectEventContentStatements);

			psSelectReleaseCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_softreleases SoftwareReleases");
			preparedStatements.add(psSelectReleaseCount);

			psSelectConfigurationCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_configurations Configurations");
			preparedStatements.add(psSelectConfigurationCount);

			psSelectDirectoryCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_directories Directories");
			preparedStatements.add(psSelectDirectoryCount);

			psSelectSuperIdCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_confversions Configurations"); // sv just a placeholder
			preparedStatements.add(psSelectSuperIdCount);

			psSelectEDSourceTemplateCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_edstemplates EDSourceTemplates");
			preparedStatements.add(psSelectEDSourceTemplateCount);

			psSelectEDSourceCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_edsources EDSources");
			preparedStatements.add(psSelectEDSourceCount);

			psSelectESSourceTemplateCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_esstemplates ESSourceTemplates");
			preparedStatements.add(psSelectESSourceTemplateCount);

			psSelectESSourceCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_essources ESSources");
			preparedStatements.add(psSelectESSourceCount);

			psSelectESModuleTemplateCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_esmtemplates ESModuleTemplates");
			preparedStatements.add(psSelectESModuleTemplateCount);

			psSelectESModuleCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_esmodules ESModules");
			preparedStatements.add(psSelectESModuleCount);

			psSelectServiceTemplateCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_srvtemplates ServiceTemplates");
			preparedStatements.add(psSelectServiceTemplateCount);

			psSelectServiceCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_services Services");
			preparedStatements.add(psSelectServiceCount);

			psSelectModuleTemplateCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_moduletemplates ModuleTemplates");
			preparedStatements.add(psSelectModuleTemplateCount);

			psSelectModuleCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_paelements Modules WHERE paetype = 1");
			preparedStatements.add(psSelectModuleCount);

			psSelectEDAliasCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_paelements Modules WHERE paetype = 6");
			preparedStatements.add(psSelectEDAliasCount);

			psSelectSequenceCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_paelements Modules WHERE paetype = 1"); // BSATARIC not
																										// 2??
			preparedStatements.add(psSelectSequenceCount);

			psSelectTaskCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_paelements Modules WHERE paetype = 4"); // BSATARIC

			preparedStatements.add(psSelectTaskCount);

			psSelectSwitchProducerCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_paelements Modules WHERE paetype = 5"); // BSATARIC

			preparedStatements.add(psSelectSwitchProducerCount);

			psSelectPathCount = dbConnector.getConnection().prepareStatement("SELECT COUNT(*) FROM u_pathids Paths");
			preparedStatements.add(psSelectPathCount);

			psSelectParameterCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_pathids Paths"); // just a placholder
			preparedStatements.add(psSelectParameterCount);

			psSelectParameterSetCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_pathids Paths"); // just a placholder
			preparedStatements.add(psSelectParameterSetCount);

			psSelectVecParameterSetCount = dbConnector.getConnection()
					.prepareStatement("SELECT COUNT(*) FROM u_pathids Paths");// just a placeholder
			preparedStatements.add(psSelectVecParameterSetCount);

			//
			// INSERT
			//

			psInsertDirectory = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_directories " + "(id_parentdir,name,created) " + "VALUES (?, ?, SYSDATE)",
					keyColumn);
			preparedStatements.add(psInsertDirectory);

			if (dbType.equals(dbTypeMySQL))
				psInsertConfigurationVers = dbConnector.getConnection().prepareStatement(
						"INSERT INTO Configurations " + "(releaseId,configDescriptor,parentDirId,config,"
								+ "version,created,creator,processName,description) "
								+ "VALUES (?, ?, ?, ?, ?, NOW(), ?, ?, ?)",
						keyColumn);
			else if (dbType.equals(dbTypeOracle))
				psInsertConfigurationVers = dbConnector.getConnection()
						.prepareStatement("INSERT INTO u_confversions "
								+ "(id_config,version,id_release,name,id_parentDir,config,"
								+ "created,creator,processName,description,fromdb) "
								+ "VALUES (?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?,'gui')", keyColumn);
			preparedStatements.add(psInsertConfigurationVers);

			psFindConfiguration = dbConnector.getConnection()
					.prepareStatement("SELECT id AS id_config FROM u_configurations WHERE name = ?");
			preparedStatements.add(psFindConfiguration);

			psInsertConfiguration = dbConnector.getConnection()
					.prepareStatement("INSERT INTO  u_configurations(name) VALUES(?)");
			preparedStatements.add(psInsertConfiguration);

			psInsertConfigurationLock = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_lockedconfs (id_parentDir,id_config,userName)" + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertConfigurationLock);

			// Insert Event Content
			psCheckContents = dbConnector.getConnection()
					.prepareStatement("SELECT id FROM u_EventContents WHERE name= ? ");
			preparedStatements.add(psCheckContents);

			psInsertContents = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_EventContents (name)" + "VALUES(?)", keyColumn);
			preparedStatements.add(psInsertContents);

			psInsertContentIds = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_EventContentids (id_evco)" + "VALUES(?)", keyColumn);
			preparedStatements.add(psInsertContentIds);

			psInsertContentsConfigAssoc = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_conf2evco (id_evCoId,id_confver)" + "VALUES(?,?)");
			preparedStatements.add(psInsertContentsConfigAssoc);

			psInsertEventContentStatements = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_evcostatements (classN,moduleL,extraN,processN,statementType) "
							+ "VALUES(?,?,?,?,?)", keyColumn);
			preparedStatements.add(psInsertEventContentStatements);

			psInsertStreams = dbConnector.getConnection().prepareStatement("INSERT INTO u_streams (name)" + "VALUES(?)",
					keyColumn);
			preparedStatements.add(psInsertStreams);

			psInsertStreamsIds = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_streamids (id_stream,streamid,fractodisk,fromdb)" + "VALUES(?,-1,?,'gui')",
					keyColumn);
			preparedStatements.add(psInsertStreamsIds);

			psInsertPrimaryDatasets = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_datasets (name)" + "VALUES(?)", keyColumn);
			preparedStatements.add(psInsertPrimaryDatasets);

			psInsertPrimaryDatasetIds = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_datasetids (id_dataset,datasetid,fromdb)" + "VALUES(?,-1,'gui')", keyColumn);
			preparedStatements.add(psInsertPrimaryDatasetIds);

			psInsertECStreamAssoc = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_evco2stream (id_evcoid, id_streamid)" + "VALUES(?,?)");
			preparedStatements.add(psInsertECStreamAssoc);

			psInsertPathStreamPDAssoc = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_pathid2strdst (id_pathid, id_streamId, id_datasetId)" + "VALUES(?,?,?)");
			preparedStatements.add(psInsertPathStreamPDAssoc);

			psInsertECStatementAssoc = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_evco2stat (statementRank,id_stat,id_evcoid,id_pathId) " + "VALUES(?,?,?,?) ");
			preparedStatements.add(psInsertECStatementAssoc);

			psInsertStreamDatasetAssoc = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_conf2strdst (id_confver,id_streamId, id_datasetId)" + "VALUES(?,?,?)");
			preparedStatements.add(psInsertStreamDatasetAssoc);

			psInsertConfDone = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_confdone (id_confver) VALUES(?)");
			preparedStatements.add(psInsertConfDone);

			psInsertConfProcessing = dbConnector.getConnection()
					.prepareStatement("INSERT INTO f_queue (id_confver) VALUES(?)");
			preparedStatements.add(psInsertConfProcessing);

			psInsertGlobalPSet = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_conf2gpset " + "(id_confver,id_gpset,ord) " + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertGlobalPSet);

			psInsertEDSource = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_edsources (id_template) " + "VALUES(?)", keyColumn);
			preparedStatements.add(psInsertEDSource);

			psInsertConfigEDSourceAssoc = dbConnector.getConnection()
					.prepareStatement("INSERT INTO " + "u_conf2eds (id_confver,id_edsource,ord) " + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertConfigEDSourceAssoc);

			psInsertESSource = dbConnector.getConnection()
					.prepareStatement("INSERT INTO " + "u_essources (id_template,name) " + "VALUES(?, ?)", keyColumn);
			preparedStatements.add(psInsertESSource);

			psInsertConfigESSourceAssoc = dbConnector.getConnection().prepareStatement(
					"INSERT INTO " + "u_conf2ess " + "(id_confver,id_essource,ord,prefer) " + "VALUES(?, ?, ?, ?)");
			preparedStatements.add(psInsertConfigESSourceAssoc);

			psInsertESModule = dbConnector.getConnection()
					.prepareStatement("INSERT INTO " + "u_esmodules (id_template,name) " + "VALUES(?, ?)", keyColumn);
			preparedStatements.add(psInsertESModule);

			psInsertConfigESModuleAssoc = dbConnector.getConnection().prepareStatement(
					"INSERT INTO " + "u_conf2esm " + "(id_confver,id_esmodule,ord,prefer) " + "VALUES(?, ?, ?, ?)");
			preparedStatements.add(psInsertConfigESModuleAssoc);

			psInsertService = dbConnector.getConnection()
					.prepareStatement("INSERT INTO " + "u_services (id_template) " + "VALUES(?)", keyColumn);
			preparedStatements.add(psInsertService);

			psInsertConfigServiceAssoc = dbConnector.getConnection()
					.prepareStatement("INSERT INTO " + "u_conf2srv (id_confver,id_service,ord) " + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertConfigServiceAssoc);

			psCheckPathName = dbConnector.getConnection().prepareStatement(" SELECT id from u_paths WHERE name=? ");
			preparedStatements.add(psCheckPathName);

			psCheckPathNoum = dbConnector.getConnection().prepareStatement(" SELECT id from u_noumpaths WHERE name=? ");
			preparedStatements.add(psCheckPathNoum);

			psInsertPathNoum = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_noumPaths (name) " + "VALUES(?)", keyColumn);
			preparedStatements.add(psInsertPathNoum);

			psInsertPath = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_Paths (name,version,id_noumpath) " + "VALUES(?, ?, ?)", keyColumn);
			preparedStatements.add(psInsertPath);

			psInsertPathIds = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_pathids (id_path,pathid,isEndPath,crc32,crc32logic,description, contact, fromdb) "
							+ "VALUES(?, NULL, ?, ?, ?, ?, ?,'gui')",
					keyColumn);
			preparedStatements.add(psInsertPathIds);

			psInsertConfigPathAssoc = dbConnector.getConnection()
					.prepareStatement("INSERT INTO " + "u_pathid2conf (id_pathId,id_confver,ord) " + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertConfigPathAssoc);

			psInsertConfigSequenceAssoc = dbConnector.getConnection().prepareStatement("INSERT INTO "
					+ "u_conf2pae (id_confver,id_pae,id_parent,lvl,ord,operator) " + "VALUES(?, ?, ?, ?, ?, ?)");
			preparedStatements.add(psInsertConfigSequenceAssoc);

			psInsertConfigTaskAssoc = dbConnector.getConnection().prepareStatement("INSERT INTO "
					+ "u_conf2pae (id_confver,id_pae,id_parent,lvl,ord,operator) " + "VALUES(?, ?, ?, ?, ?, ?)");
			preparedStatements.add(psInsertConfigTaskAssoc);

			psInsertConfigSwitchProducerAssoc = dbConnector.getConnection().prepareStatement("INSERT INTO "
					+ "u_conf2pae (id_confver,id_pae,id_parent,lvl,ord,operator) " + "VALUES(?, ?, ?, ?, ?, ?)");
			preparedStatements.add(psInsertConfigSwitchProducerAssoc);

			psInsertPathElement = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_paelements (paetype,name,crc32,crc32logic,o_id) " + "VALUES(?, ?, ?, NULL,NULL)",
					keyColumn);
			preparedStatements.add(psInsertPathElement);

			psInsertHPathElement = dbConnector.getConnection()
					.prepareStatement("INSERT INTO h_paelements (moe_type,templateId,name,crc32,crc32logic) "
							+ "VALUES(?, ?, ?, ?, NULL)");
			preparedStatements.add(psInsertHPathElement);

			psInsertMoElement = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_moelements (moetype,name,o_id,paramtype,tracked,crc32,value,valuelob,hex) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ? )",
					keyColumn);
			preparedStatements.add(psInsertMoElement);

			psInsertPae2Moe = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_pae2moe (id_pae,id_moe,lvl,ord) " + "VALUES(?, ?, ?, ?)", keyColumn);
			preparedStatements.add(psInsertPae2Moe);

			psInsertMod2Templ = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_mod2templ (id_pae,id_templ) " + "VALUES(?, ? )");
			preparedStatements.add(psInsertMod2Templ);

			psInsertPathElementAssoc = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_pathid2pae (id_pathid,id_pae,id_parent,lvl,ord,operator) "
							+ "VALUES(?, ?, ?, ?, ?, ?)");
			preparedStatements.add(psInsertPathModuleAssoc);

			psInsertPathOutputModuleAssoc = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_pathid2outm (id_pathid,id_streamid,ord,operator) " + "VALUES(?, ?, ?, ?)");
			preparedStatements.add(psInsertPathOutputModuleAssoc);

			psInsertEDSourceT2Rele = dbConnector.getConnection()
					.prepareStatement("insert into u_edst2rele (id_edstemplate,id_release) values (?,?)");
			preparedStatements.add(psInsertEDSourceT2Rele);

			psInsertESSourceT2Rele = dbConnector.getConnection()
					.prepareStatement("insert into u_esst2rele (id_esstemplate,id_release) values (?,?)");
			preparedStatements.add(psInsertESSourceT2Rele);

			psInsertESModuleT2Rele = dbConnector.getConnection()
					.prepareStatement("insert into u_esmt2rele (id_esmtemplate,id_release) values (?,?)");
			preparedStatements.add(psInsertESModuleT2Rele);

			psInsertServiceT2Rele = dbConnector.getConnection()
					.prepareStatement("insert into u_srvt2rele (id_srvtemplate,id_release) values (?,?)");
			preparedStatements.add(psInsertServiceT2Rele);

			psInsertModuleT2Rele = dbConnector.getConnection()
					.prepareStatement("insert into u_modt2rele (id_modtemplate,id_release) values (?,?)");
			preparedStatements.add(psInsertModuleT2Rele);

			psInsertServiceTemplate = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_srvtemplates (id_pkg,name,cvstag) " + "VALUES (?, ?, ?)", keyColumn);
			preparedStatements.add(psInsertServiceTemplate);

			psInsertEDSourceTemplate = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_edstemplates (id_pkg,name,cvstag) " + "VALUES (?, ?, ?)", keyColumn);
			preparedStatements.add(psInsertEDSourceTemplate);

			psInsertESSourceTemplate = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_esstemplates (pkg_id,name,cvstag) " + "VALUES (?, ?, ?)", keyColumn);
			preparedStatements.add(psInsertESSourceTemplate);

			psInsertESModuleTemplate = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_esmtemplates (id_pkg,name,cvstag) " + "VALUES (?, ?, ?)", keyColumn);
			preparedStatements.add(psInsertESModuleTemplate);

			psInsertModuleTemplate = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_ModuleTemplates (typeId,name,cvstag) " + "VALUES (?, ?, ?)");
			preparedStatements.add(psInsertModuleTemplate);

			psInsertParameterSet = dbConnector.getConnection()
					.prepareStatement("INSERT INTO ParameterSets(superId,name,tracked) " + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertParameterSet);

			psInsertVecParameterSet = dbConnector.getConnection()
					.prepareStatement("INSERT INTO VecParameterSets(superId,name,tracked) " + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertVecParameterSet);

			psInsertGPset = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_globalpsets (name,tracked) " + "VALUES(?, ?)", keyColumn);
			preparedStatements.add(psInsertGPset);

			psInsertParameterGPset = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_gpsetelements (id_gpset,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?,NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterGPset);

			psInsertParameterEDS = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_edselements (id_edsource,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterEDS);

			psInsertParameterEDST = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_edstelements (id_edstemplate,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterEDST);

			psInsertParameterESM = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_esmelements (id_esmodule,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterESM);

			psInsertParameterESMT = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_esmtelements (id_esmtemplate,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterESMT);

			psInsertParameterESS = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_esselements (id_essource,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterESS);

			psInsertParameterESST = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_esstelements (id_esstemplate,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterESST);

			psInsertParameterSRV = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_srvelements (id_service,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterSRV);

			psInsertParameterSRVT = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_srvtelements (id_srvtemplate,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterSRVT);

			psInsertParameterMODT = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_modtelements (id_modtemplate,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterMODT);

			psInsertParameterOUTM = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_outmelements (id_streamid,name,lvl,tracked,paramtype,ord,value,valuelob,hex,o_id,moetype) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)",
					keyColumn);
			preparedStatements.add(psInsertParameterOUTM);

			psInsertSuperIdParamSetAssoc = dbConnector.getConnection().prepareStatement(
					"INSERT INTO SuperIdParamSetAssoc (superId,psetId,sequenceNb) " + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertSuperIdParamSetAssoc);

			psInsertSuperIdVecParamSetAssoc = dbConnector.getConnection().prepareStatement(
					"INSERT INTO " + "SuperIdVecParamSetAssoc (superId,vpsetId,sequenceNb) " + "VALUES(?, ?, ?)");
			preparedStatements.add(psInsertSuperIdVecParamSetAssoc);

			//
			// DELETE
			//

			psDeleteDirectory = dbConnector.getConnection().prepareStatement("DELETE FROM u_directories WHERE id=?");
			preparedStatements.add(psDeleteDirectory);

			psDeleteLock = dbConnector.getConnection()
					.prepareStatement("DELETE FROM u_lockedconfs " + "WHERE id_parentdir=? AND id_config=?");
			preparedStatements.add(psDeleteLock);

			psDeleteConfiguration = dbConnector.getConnection()
					.prepareStatement("DELETE FROM u_confversions " + "WHERE id = ?");
			preparedStatements.add(psDeleteConfiguration);

			psDeleteSoftwareRelease = dbConnector.getConnection()
					.prepareStatement("DELETE FROM u_softreleases " + "WHERE id = ?");
			preparedStatements.add(psDeleteSoftwareRelease);

			psDeleteEDSourcesFromConfig = dbConnector.getConnection()
					.prepareStatement("DELETE FROM u_conf2eds " + "WHERE id_confver=?");
			preparedStatements.add(psDeleteEDSourcesFromConfig);

			psDeleteESSourcesFromConfig = dbConnector.getConnection()
					.prepareStatement("DELETE FROM u_conf2ess " + "WHERE id_confver=?");
			preparedStatements.add(psDeleteESSourcesFromConfig);

			psDeleteESModulesFromConfig = dbConnector.getConnection()
					.prepareStatement("DELETE FROM u_conf2esm " + "WHERE id_confver=?");
			preparedStatements.add(psDeleteESModulesFromConfig);

			psDeleteServicesFromConfig = dbConnector.getConnection()
					.prepareStatement("DELETE FROM u_conf2srv " + "WHERE id_confver=?");
			preparedStatements.add(psDeleteServicesFromConfig);

			//
			// STORED PROCEDURES
			//

			// MySQL
			if (dbType.equals(dbTypeMySQL)) {

				csLoadTemplate = dbConnector.getConnection().prepareCall("{ CALL load_template(?,?) }");
				preparedStatements.add(csLoadTemplate);

				csLoadTemplates = dbConnector.getConnection().prepareCall("{ CALL load_templates(?) }");
				preparedStatements.add(csLoadTemplates);

				csLoadTemplatesForConfig = dbConnector.getConnection()
						.prepareCall("{ CALL load_templates_for_config(?) }");
				preparedStatements.add(csLoadTemplatesForConfig);

				csLoadConfiguration = dbConnector.getConnection().prepareCall("{ CALL load_configuration(?) }");
				preparedStatements.add(csLoadConfiguration);

			}
			// Oracle
			else {
				csLoadTemplate = dbConnector.getConnection().prepareCall("begin load_template(?,?); end;");
				preparedStatements.add(csLoadTemplate);

				csLoadTemplates = dbConnector.getConnection().prepareCall("begin load_templates(?); end;");
				preparedStatements.add(csLoadTemplates);

				csLoadTemplatesForConfig = dbConnector.getConnection()
						.prepareCall("begin load_templates_for_config(?); end;");
				preparedStatements.add(csLoadTemplatesForConfig);

				csLoadConfiguration = dbConnector.getConnection().prepareCall("begin load_configuration(?); end;");
				preparedStatements.add(csLoadConfiguration);

			}

			psSelectTemplates = dbConnector.getConnection().prepareStatement(
					"select u_moduletemplates.id,u_moduletypes.type,u_moduletemplates.name,u_moduletemplates.cvstag,u_moduletemplates.id_pkg from u_moduletemplates, u_modt2rele,u_moduletypes where u_modt2rele.id_release=? and u_modt2rele.id_modtemplate=u_moduletemplates.id and u_moduletypes.id=u_moduletemplates.id_mtype "
							+ " UNION ALL "
							+ " SELECT id+4000000,'Service',name,cvstag,id_pkg FROM u_srvtemplates  WHERE id in (select id_srvtemplate as id from u_srvt2rele where id_release=?) "
							+ " UNION ALL "
							+ " SELECT id+3000000,'ESModule',name,cvstag,id_pkg FROM u_esmtemplates  WHERE id in (select id_esmtemplate from u_esmt2rele where id_release=?) "
							+ " UNION ALL "
							+ " SELECT id+2000000,'ESSource',name,cvstag,id_pkg  FROM u_esstemplates  WHERE id in (select id_esstemplate as id from u_esst2rele where id_release=?) "
							+ " UNION ALL "
							+ " SELECT id+1000000,'EDSource',name,cvstag,id_pkg FROM u_edstemplates  WHERE id in (select id_edstemplate as id from u_edst2rele where id_release=?) order by 5,1");
			psSelectTemplates.setFetchSize(1024);
			preparedStatements.add(psSelectTemplates);

			psSelectParametersTemplates = dbConnector.getConnection().prepareStatement

			("select id,paramtype,name,tracked,ord,id_modtemplate,lvl,value,valuelob,hex from u_modtelements where id_modtemplate in (select distinct id_modtemplate as id from u_modt2rele where id_release=?) "
					+ " UNION ALL "
					+ " select id+4000000,paramtype,name,tracked,ord,id_srvtemplate+4000000,lvl,value,valuelob,hex from u_srvtelements where id_srvtemplate in (select distinct id_srvtemplate as id from u_srvt2rele where id_release=?) "
					+ " UNION ALL "
					+ " select id+3000000,paramtype,name,tracked,ord,id_esmtemplate+3000000,lvl,value,valuelob,hex  from u_esmtelements where id_esmtemplate in (select distinct id_esmtemplate as id from u_esmt2rele where id_release=?) "
					+ " UNION ALL "
					+ " select id+2000000,paramtype,name,tracked,ord,id_esstemplate+2000000,lvl,value,valuelob,hex  from u_esstelements where id_esstemplate in (select distinct id_esstemplate as id from u_esst2rele where id_release=?) "
					+ " UNION ALL "
					+ " select id+1000000,paramtype,name,tracked,ord,id_edstemplate+1000000,lvl,value,valuelob,hex from u_edstelements where id_edstemplate in (select distinct id_edstemplate as id from u_edst2rele where id_release=?) order by 6,1");
			psSelectParametersTemplates.setFetchSize(8192);
			preparedStatements.add(psSelectParametersTemplates);

			psSelectInstances = dbConnector.getConnection().prepareStatement(
					"SELECT u_globalpsets.id+6000000, NULL, 'PSet', u_globalpsets.name, u_globalpsets.tracked, u_conf2gpset.ord, NULL as description, NULL as contact FROM u_globalpsets,u_conf2gpset WHERE u_conf2gpset.id_confver=? AND u_globalpsets.id=u_conf2gpset.id_gpset "
							+ " UNION ALL "
							+ " SELECT u_edsources.id+1000000, u_edsources.id_template+1000000, 'EDSource',NULL,NULL, u_conf2eds.ord,NULL as description, NULL as contact  FROM u_edsources,u_conf2eds WHERE u_conf2eds.id_edsource=u_edsources.id and u_conf2eds.id_confver = ? "
							+ " UNION ALL "
							+ " SELECT u_essources.id+2000000, u_essources.id_template+2000000, 'ESSource', u_essources.name, u_conf2ess.prefer, u_conf2ess.ord,NULL as description, NULL as contact  FROM u_essources,u_conf2ess WHERE u_conf2ess.id_essource=u_essources.id and u_conf2ess.id_confver = ? "
							+ " UNION ALL "
							+ " SELECT u_esmodules.id+3000000, u_esmodules.id_template+3000000, 'ESModule', u_esmodules.name, u_conf2esm.prefer, u_conf2esm.ord,NULL as description, NULL as contact  FROM u_esmodules,u_conf2esm WHERE u_conf2esm.id_esmodule=u_esmodules.id and u_conf2esm.id_confver = ? "
							+ " UNION ALL "
							+ " SELECT u_services.id+4000000, u_services.id_template+4000000, 'Service',NULL,NULL, u_conf2srv.ord,NULL as description, NULL as contact  FROM u_services,u_conf2srv WHERE u_conf2srv.id_service=u_services.id and u_conf2srv.id_confver = ? "
							+ " UNION ALL "
							+ " SELECT ta.*,NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, u_mod2templ.id_templ, 'Module', u_paelements.name,NULL as endpath, NULL as ord FROM u_pathid2pae,u_paelements, u_pathid2conf, u_mod2templ WHERE u_pathid2conf.id_pathid=u_pathid2pae.id_pathid and u_pathid2pae.id_pae=u_paelements.id and u_paelements.paetype=1 and u_mod2templ.id_pae=u_paelements.id and u_pathid2conf.id_confver = ? ) ta"
							+ " UNION ALL "
							+ " SELECT u_pathid2conf.id_pathid, NULL, 'Path', u_paths.name, u_pathids.isEndPath, u_pathid2conf.ord,u_pathids.description,u_pathids.contact FROM u_paths,u_pathid2conf,u_pathids WHERE u_pathids.id=u_pathid2conf.id_pathid and u_paths.id=u_pathids.id_path and u_pathid2conf.id_confver = ? "
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, u_mod2templ.id_templ, 'Module', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_conf2pae,u_mod2templ WHERE  u_conf2pae.id_pae=u_paelements.id and u_paelements.paetype=1 and u_conf2pae.id_confver = ? and u_mod2templ.id_pae=u_paelements.id and u_conf2pae.id_pae not in (SELECT a.id_pae FROM u_pathid2pae a, u_pathid2conf b WHERE a.id_pathid = b.id_pathid AND b.id_confver =u_conf2pae.id_confver )) ta  "
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, NULL, 'Sequence', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_conf2pae WHERE  u_conf2pae.id_pae=u_paelements.id and u_paelements.paetype=2 and u_conf2pae.id_confver = ? and u_conf2pae.id_pae not in (SELECT a.id_pae FROM u_pathid2pae a, u_pathid2conf b WHERE a.id_pathid = b.id_pathid AND b.id_confver =u_conf2pae.id_confver )) ta "
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, NULL, 'Sequence', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_pathid2conf,u_pathid2pae WHERE u_pathid2conf.id_pathid=u_pathid2pae.id_pathid and u_pathid2pae.id_pae=u_paelements.id and u_paelements.paetype=2 and u_pathid2conf.id_confver = ?) ta"
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, NULL, 'Task', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_conf2pae WHERE  u_conf2pae.id_pae=u_paelements.id and u_paelements.paetype=4 and u_conf2pae.id_confver = ? and u_conf2pae.id_pae not in (SELECT a.id_pae FROM u_pathid2pae a, u_pathid2conf b WHERE a.id_pathid = b.id_pathid AND b.id_confver =u_conf2pae.id_confver )) ta "
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, NULL, 'Task', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_pathid2conf,u_pathid2pae WHERE u_pathid2conf.id_pathid=u_pathid2pae.id_pathid and u_pathid2pae.id_pae=u_paelements.id and u_paelements.paetype=4 and u_pathid2conf.id_confver = ?) ta "
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, NULL, 'SwitchProducer', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_conf2pae WHERE  u_conf2pae.id_pae=u_paelements.id and u_paelements.paetype=5 and u_conf2pae.id_confver = ? and u_conf2pae.id_pae not in (SELECT a.id_pae FROM u_pathid2pae a, u_pathid2conf b WHERE a.id_pathid = b.id_pathid AND b.id_confver =u_conf2pae.id_confver )) ta "
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, NULL, 'SwitchProducer', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_pathid2conf,u_pathid2pae WHERE u_pathid2conf.id_pathid=u_pathid2pae.id_pathid and u_pathid2pae.id_pae=u_paelements.id and u_paelements.paetype=5 and u_pathid2conf.id_confver = ?) ta "
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, NULL, 'EDAlias', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_conf2pae WHERE  u_conf2pae.id_pae=u_paelements.id and u_paelements.paetype=6 and u_conf2pae.id_confver = ? and u_conf2pae.id_pae not in (SELECT a.id_pae FROM u_pathid2pae a, u_pathid2conf b WHERE a.id_pathid = b.id_pathid AND b.id_confver =u_conf2pae.id_confver )) ta "
							+ " UNION ALL "
							+ " select ta.*, NULL as description,NULL as contact from (SELECT UNIQUE u_paelements.id, NULL, 'EDAlias', u_paelements.name, NULL as endpath,NULL as ord FROM u_paelements, u_pathid2conf,u_pathid2pae WHERE u_pathid2conf.id_pathid=u_pathid2pae.id_pathid and u_pathid2pae.id_pae=u_paelements.id and u_paelements.paetype=6 and u_pathid2conf.id_confver = ?) ta order by 3,6,4");
			psSelectInstances.setFetchSize(2048);
			preparedStatements.add(psSelectInstances);
			//
			// SELECT FOR TEMPORARY TABLES (BSATARIC: TODO EDALIAS)
			//
			psSelectParameters = dbConnector.getConnection().prepareStatement(
					"Select * from (Select * from (SELECT a.id+1000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_edsource+1000000, a.lvl,  a.value,  a.valuelob, a.hex  from u_EDSELEMENTS a, u_CONF2EDS c "
							+ " where c.ID_CONFVER=? and c.ID_EDSOURCE=a.ID_edsource order by a.id_edsource+1000000,id ) "
							+ " UNION ALL "
							+ "Select * from (SELECT a.id+2000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_essource+2000000, a.lvl,  a.value,  a.valuelob , a.hex from u_ESSELEMENTS a, u_CONF2ESS c "
							+ " where c.ID_CONFVER=? and c.ID_ESSOURCE=a.ID_essource order by a.id_essource+2000000,id ) "
							+ " UNION ALL "
							+ " select * from (select id as moeid,paramtype,name,tracked,ord,id_pae,lvl,value,valuelob,hex from (select sa.*, u_moelements.valuelob,u_moelements.hex from (select  distinct u_moelements.id, u_moelements.paramtype, u_moelements.name, u_moelements.tracked, u_pae2moe.ord,u_pathid2pae.id_pae,  u_pae2moe.lvl as lvl,  u_moelements.value,u_pae2moe.id as pae2id  from u_moelements, u_pae2moe, u_pathid2pae  where u_moelements.id = u_pae2moe.id_moe AND u_pathid2pae.id_pae = u_pae2moe.id_pae AND  u_pathid2pae.id  IN (SELECT u_pathid2pae.id FROM u_pathid2pae,u_pathid2conf WHERE u_pathid2conf.id_pathid=u_pathid2pae.id_pathid and u_pathid2conf.id_confver=?) order by u_pae2moe.id_moe,u_pae2moe.id) sa, u_moelements where sa.id=u_moelements.id) order by moeid )"
							+ " UNION ALL "
							+ " select * from (select id as moeid,paramtype,name,tracked,ord,id_pae,lvl,value,valuelob,hex from (SELECT u_moelements.*,u_pae2moe.lvl,u_conf2pae.id_pae,u_pae2moe.ord FROM u_conf2pae,u_paelements,u_moelements,u_pae2moe  WHERE  u_conf2pae.id_pae=u_paelements.id and u_pae2moe.id_pae=u_paelements.id and u_pae2moe.id_moe=u_moelements.id and ((u_conf2pae.lvl=0 and u_paelements.paetype=1) or u_conf2pae.lvl>0) and u_conf2pae.id_confver = ? and u_conf2pae.id_pae not in (SELECT a.id_pae FROM u_pathid2pae a, u_pathid2conf b WHERE a.id_pathid = b.id_pathid AND b.id_confver =u_conf2pae.id_confver )) order by moeid) "
							+ " UNION ALL "
							+ "Select * from (SELECT a.id+4000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_service+4000000, a.lvl,  a.value,  a.valuelob, a.hex from u_SRVELEMENTS a, u_CONF2SRV c "
							+ " where c.ID_CONFVER=? and c.ID_SERVICE=a.ID_Service order by a.id_service+4000000,id )"
							+ " UNION ALL "
							+ "Select * from (SELECT a.id+3000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_esmodule+3000000, a.lvl,  a.value,  a.valuelob, a.hex from u_ESMELEMENTS a, u_CONF2ESM c "
							+ " where c.ID_CONFVER=? and c.ID_esmodule=a.ID_esmodule order by a.id_esmodule+3000000,id ) "
							+ " UNION ALL "
							+ "Select * from (SELECT a.id+6000000 as id, a.paramtype, a.name, a.tracked, a.ord,a.id_gpset+6000000, a.lvl,  a.value,  a.valuelob, a.hex from u_GPSETELEMENTS a, u_CONF2GPSET c "
							+ " where c.ID_CONFVER=? and c.ID_gpset=a.ID_gpset order by a.id_gpset+6000000,id ) "
							+ " UNION ALL "
							+ " select * from (SELECT a.id+5000000 as id, a.paramtype, a.name, a.tracked, a.ord,u_streamids.id+5000000, a.lvl,  a.value,  a.valuelob, a.hex from u_outmelements a,u_pathid2conf,u_pathid2outm,u_streamids where a.id_streamid=u_streamids.id  AND u_streamids.id=u_pathid2outm.id_streamid and u_pathid2outm.id_pathid=u_pathid2conf.id_pathid AND u_pathid2conf.id_confver = ? order by u_streamids.id+5000000,id) )");
			psSelectParameters.setFetchSize(8192);
			preparedStatements.add(psSelectParameters);

			psSelectBooleanValues = dbConnector.getConnection().prepareStatement(
					"SELECT DISTINCT" + " parameter_id," + " parameter_value " + "FROM tmp_boolean_table");
			psSelectBooleanValues.setFetchSize(2048);
			preparedStatements.add(psSelectBooleanValues);

			psSelectIntValues = dbConnector.getConnection()
					.prepareStatement("SELECT DISTINCT" + " parameter_id," + " parameter_value," + " sequence_nb,"
							+ " hex " + "FROM tmp_int_table " + "ORDER BY sequence_nb ASC");
			psSelectIntValues.setFetchSize(2048);
			preparedStatements.add(psSelectIntValues);

			psSelectRealValues = dbConnector.getConnection().prepareStatement("SELECT DISTINCT" + " parameter_id,"
					+ " parameter_value," + " sequence_nb " + "FROM tmp_real_table " + "ORDER BY sequence_nb");
			psSelectRealValues.setFetchSize(2048);
			preparedStatements.add(psSelectRealValues);

			psSelectStringValues = dbConnector.getConnection().prepareStatement("SELECT DISTINCT" + " parameter_id,"
					+ " parameter_value," + " sequence_nb " + "FROM tmp_string_table " + "ORDER BY sequence_nb ASC");
			psSelectStringValues.setFetchSize(2048);
			preparedStatements.add(psSelectStringValues);

			psSelectCLOBsValues = dbConnector.getConnection()
					.prepareStatement("SELECT                                                "
							+ "   parameter_id    ,  " + "   parameter_value ,  " + "   sequence_nb                "
							+ "FROM                                  " + "   tmp_clob_table     ");
			psSelectCLOBsValues.setFetchSize(2048);
			preparedStatements.add(psSelectCLOBsValues);

			// EDAlias probably unnecessary
			psSelectPathEntries = dbConnector.getConnection().prepareStatement(
					"Select * from (Select * from (SELECT u_pathid2conf.id_pathid, u_paelements.id, u_pathid2pae.ord, "
							+ "DECODE(u_paelements.paetype,1, 'Module', 2, 'Sequence', 3, 'OutputModule', 4, 'Task', 5, 'SwitchProducer', 6, 'EDAlias', 'Undefined') "
							+ "AS entry_type, u_pathid2pae.operator FROM u_pathid2pae,u_paelements, u_pathid2conf WHERE "
							+ "u_pathid2conf.id_pathid=u_pathid2pae.id_pathid and u_pathid2pae.id_pae=u_paelements.id and u_pathid2pae.lvl=0 "
							+ "and u_pathid2conf.id_confver = ? order by u_pathid2pae.id_pathid,u_pathid2pae.id) "
							+ " UNION ALL "
							+ "select * from (select u_PATHID2CONF.id_pathid, u_streamids.id+5000000 as "
							+ "stid,u_PATHID2OUTM.ord,'OutputModule', u_PATHID2OUTM.operator from "
							+ "u_PATHID2OUTM,u_streams,u_streamids,u_PATHID2CONF where u_streams.id=u_streamids.id_stream "
							+ "and u_streamids.id=u_PATHID2OUTM.id_streamid and u_PATHID2CONF.id_confver=? "
							+ "and  u_PATHID2CONF.id_pathid= u_PATHID2OUTM.id_pathid )) order by id_pathid,ord");
			psSelectPathEntries.setFetchSize(1024);
			preparedStatements.add(psSelectPathEntries);

			// EDAlias probably unnecessary
			psSelectSeqTaskOrSPEntries = dbConnector.getConnection().prepareStatement(
					"select * from (SELECT u_pathid2pae.id_pathid,u_pathid2pae.id as srid,u_pathid2pae.lvl, "
							+ "u_paelements.id, u_pathid2pae.ord, DECODE(u_paelements.paetype,1, "
							+ "'Module', 2, 'Sequence', 3, 'OutputModule', 4, 'Task', 5, 'SwitchProducer', 6, 'EDAlias', 'Undefined') "
							+ "AS entry_type, u_pathid2pae.operator FROM u_pathid2pae,u_paelements, "
							+ "u_pathid2conf WHERE u_pathid2conf.id_pathid=u_pathid2pae.id_pathid and "
							+ "u_pathid2pae.id_pae=u_paelements.id and ((u_pathid2pae.lvl=0 and (u_paelements.paetype=2 or u_paelements.paetype=4 or u_paelements.paetype=5)) "
							+ "or u_pathid2pae.lvl>0) and u_pathid2conf.id_confver = ? order by u_pathid2pae.id_pathid, srid) "
							+ "UNION ALL select * from(SELECT 0,u_conf2pae.id as srid,u_conf2pae.lvl, u_paelements.id, u_conf2pae.ord, "
							+ "DECODE(u_paelements.paetype,1, 'Module', 2, 'Sequence', 3, 'OutputModule', 4, 'Task', 5, 'SwitchProducer', 6, 'EDAlias', 'Undefined') AS entry_type, "
							+ "u_conf2pae.operator FROM u_conf2pae,u_paelements  WHERE  u_conf2pae.id_pae=u_paelements.id and "
							+ "((u_conf2pae.lvl=0 and (u_paelements.paetype=2 or u_paelements.paetype=4 or u_paelements.paetype=5)) or u_conf2pae.lvl>0) and u_conf2pae.id_confver = ?  "
							+ "order by u_conf2pae.id_confver, srid)");
			psSelectSeqTaskOrSPEntries.setFetchSize(1024);
			preparedStatements.add(psSelectSeqTaskOrSPEntries);

			psPrepareSequenceEntries = dbConnector.getConnection()
					.prepareStatement("INSERT INTO TMP_SEQUENCE_ENTRIES " + "VALUES (?,?,?,?,?)");
			preparedStatements.add(psPrepareSequenceEntries);

			// bug #91797 ConfDB operator IGNORE/NEGATE also for modules in a sequence
			psSelectSequenceEntriesAndOperator = dbConnector.getConnection()
					.prepareStatement("select distinct * from tmp_sequence_entries");
			psSelectSequenceEntriesAndOperator.setFetchSize(1024);
			preparedStatements.add(psSelectSequenceEntriesAndOperator);

			// Insert a new relesase
			psInsertReleaseTag = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_softreleases " + " (releaseTag)  VALUES (?)", keyColumn);
			psInsertReleaseTag.setFetchSize(1024);
			preparedStatements.add(psInsertReleaseTag);

			psSelectSoftwareSubsystemId = dbConnector.getConnection()
					.prepareStatement("SELECT id " + "FROM u_softsubsystems " + "WHERE name = ?");
			psSelectSoftwareSubsystemId.setFetchSize(1024);
			preparedStatements.add(psSelectSoftwareSubsystemId);

			psInsertSoftwareSubsystem = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_softsubsystems " + " (name) VALUES (?)", keyColumn);
			preparedStatements.add(psInsertSoftwareSubsystem);

			psSelectSoftwarePackageId = dbConnector.getConnection()
					.prepareStatement("SELECT id " + "FROM u_softpackages " + "WHERE id_subs = ? AND name = ? ");
			psSelectSoftwarePackageId.setFetchSize(1024);
			preparedStatements.add(psSelectSoftwarePackageId);

			psInsertSoftwarePackage = dbConnector.getConnection()
					.prepareStatement("INSERT INTO u_softpackages " + " (id_subs, name) VALUES (?,?)", keyColumn);
			preparedStatements.add(psInsertSoftwarePackage);

			psInsertEDSourceTemplateRelease = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_edstemplates " + " (name, CVSTAG, id_pkg) VALUES (?,?,?)", keyColumn);
			preparedStatements.add(psInsertEDSourceTemplateRelease);

			psInsertESSourceTemplateRelease = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_esstemplates " + " (name, CVSTAG, id_pkg) VALUES (?,?,?)", keyColumn);
			preparedStatements.add(psInsertESSourceTemplateRelease);

			psInsertESModuleTemplateRelease = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_esmtemplates " + " (name, CVSTAG, id_pkg) VALUES (?,?,?)", keyColumn);
			preparedStatements.add(psInsertESModuleTemplateRelease);

			psInsertServiceTemplateRelease = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_srvtemplates " + " (name, CVSTAG, id_pkg) VALUES (?,?,?)", keyColumn);
			preparedStatements.add(psInsertServiceTemplateRelease);

			psInsertModuleTemplateRelease = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_moduletemplates " + " (id_mtype, name, CVSTAG,id_pkg) VALUES (?,?,?,?)", keyColumn);
			preparedStatements.add(psInsertModuleTemplateRelease);

			// SQL statements for new path fields:
			psCheckPathFieldsExistence = dbConnector.getConnection()
					.prepareStatement("Select 1 from dual where exists (                     "
							+ "select 1 from all_tab_columns                        "
							+ "where table_name = 'U_PATHS'                         "
							+ "and column_name = 'DESCRIPTION')                     ");
			preparedStatements.add(psCheckPathFieldsExistence);

			psSelectPathExtraFields = dbConnector.getConnection()
					.prepareStatement("SELECT p.id            ,       " + "               p.description   ,       "
							+ "               p.contact                       "
							+ "FROM   u_pathids       p                       " + "WHERE  p.id = ?                ");
			preparedStatements.add(psSelectPathExtraFields);

			psInsertPathDescription = dbConnector.getConnection().prepareStatement(
					"INSERT INTO u_paths (name,isEndPath, description, contact) " + "VALUES(?, ?, ?, ?)", keyColumn);
			preparedStatements.add(psInsertPathDescription);

			psUpdatePathDescription = dbConnector.getConnection()
					.prepareStatement("UPDATE u_paths SET description=?, contact=? WHERE id= ? ");
			preparedStatements.add(psUpdatePathDescription);

		} catch (SQLException e) {
			String errMsg = "ConfDB::prepareStatements() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}

		// create hash maps
		moduleTypeIdHashMap = new HashMap<String, Integer>();
		paramTypeIdHashMap = new HashMap<String, Integer>();
		isVectorParamHashMap = new HashMap<Integer, Boolean>();
		insertParameterHashMap = new HashMap<String, PreparedStatement>();
		dbidParamHashMap = new HashMap<Integer, Integer>();
		dbidTemplateHashMap = new HashMap<Integer, Integer>();

		insertParameterHashMap.put("bool", psInsertBoolParamValue);
		insertParameterHashMap.put("int32", psInsertInt32ParamValue);
		insertParameterHashMap.put("vint32", psInsertVInt32ParamValue);
		insertParameterHashMap.put("uint32", psInsertUInt32ParamValue);
		insertParameterHashMap.put("vuint32", psInsertVUInt32ParamValue);
		insertParameterHashMap.put("int64", psInsertInt64ParamValue);
		insertParameterHashMap.put("vint64", psInsertVInt64ParamValue);
		insertParameterHashMap.put("uint64", psInsertUInt64ParamValue);
		insertParameterHashMap.put("vuint64", psInsertVUInt64ParamValue);
		insertParameterHashMap.put("double", psInsertDoubleParamValue);
		insertParameterHashMap.put("vdouble", psInsertVDoubleParamValue);
		insertParameterHashMap.put("string", psInsertStringParamValue);
		insertParameterHashMap.put("vstring", psInsertVStringParamValue);
		insertParameterHashMap.put("EventID", psInsertEventIDParamValue);
		insertParameterHashMap.put("VEventID", psInsertVEventIDParamValue);
		insertParameterHashMap.put("InputTag", psInsertInputTagParamValue);
		insertParameterHashMap.put("ESInputTag", psInsertESInputTagParamValue);
		insertParameterHashMap.put("VInputTag", psInsertVInputTagParamValue);
		insertParameterHashMap.put("VESInputTag", psInsertVESInputTagParamValue);
		insertParameterHashMap.put("FileInPath", psInsertFileInPathParamValue);

		ResultSet rs = null;
		try {
			rs = psSelectModuleTypes.executeQuery();
			while (rs.next()) {
				int typeId = rs.getInt(1);
				String type = rs.getString(2);
				moduleTypeIdHashMap.put(type, typeId);
				templateTableNameHashMap.put(type, tableModuleTemplates);
			}

			rs = psSelectParameterTypes.executeQuery();
			/*
			 * int typeId = 0; while (rs.next()) { //int typeId = rs.getInt(1); typeId++;
			 * String type = rs.getString(1); paramTypeIdHashMap.put(type,typeId); if
			 * (type.startsWith("v")||type.startsWith("V"))
			 * isVectorParamHashMap.put(typeId,true); else
			 * isVectorParamHashMap.put(typeId,false); }
			 */
		} catch (SQLException e) {
			String errMsg = "ConfDB::prepareStatements() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/**
	 * get values as strings after loading templates/configuration TODO: EDAliases
	 */
	private HashMap<Integer, ArrayList<Parameter>> getParameters(int configId) throws DatabaseException {
		HashMap<Integer, ArrayList<Parameter>> idToParameters = new HashMap<Integer, ArrayList<Parameter>>();

		ResultSet rsParameters = null;
		ResultSet rsBooleanValues = null;
		ResultSet rsIntValues = null;
		ResultSet rsRealValues = null;
		ResultSet rsStringValues = null;

// System.err.println("getParameters ");

		try {
			if (configId < 0) {
				int releaseId = -configId;
				psSelectParametersTemplates.setInt(1, releaseId);
				psSelectParametersTemplates.setInt(2, releaseId);
				psSelectParametersTemplates.setInt(3, releaseId);
				psSelectParametersTemplates.setInt(4, releaseId);
				psSelectParametersTemplates.setInt(5, releaseId);
				rsParameters = psSelectParametersTemplates.executeQuery();
			} else {
				psSelectParameters.setInt(1, configId);
				psSelectParameters.setInt(2, configId);
				psSelectParameters.setInt(3, configId);
				psSelectParameters.setInt(4, configId);
				psSelectParameters.setInt(5, configId);
				psSelectParameters.setInt(6, configId);
				psSelectParameters.setInt(7, configId);
				psSelectParameters.setInt(8, configId);
				rsParameters = psSelectParameters.executeQuery();
			}

			System.out.println("psSelectParameters configId " + configId);

// System.err.println("getParameter query dones ");

			// get values as strings first
			HashMap<Integer, String> idToValueAsString = new HashMap<Integer, String>();

			ArrayList<IdPSetPair> psets = new ArrayList<IdPSetPair>();
			ArrayList<IdVPSetPair> vpsets = new ArrayList<IdVPSetPair>();

			// Queue<Integer> idlifo = new LinkedList<Integer>();
			Stack<Integer> idlifo = new Stack<Integer>();

			// int newpamid=8000000;
			// if(configId==0) newpamid=10000000;
			int previouslvl = 0;
			int counter = 0;
//long thebefore=System.currentTimeMillis();
			while (rsParameters.next()) {
				int parameterId = rsParameters.getInt(1); // moeid
				String type = rsParameters.getString(2); // paramtype
				String name = rsParameters.getString(3); // name
				boolean isTrkd = rsParameters.getBoolean(4); // tracked
				int seqNb = rsParameters.getInt(5); // ord
				int parentId = rsParameters.getInt(6); // id_pae
				int lvl = rsParameters.getInt(7); // value

				if (name != null)
					if (configId < 0 && name.equals("RegionPSet") && parameterId == 7076738) {

						System.out.println("PARAMETER ID " + parameterId);
						System.out.println("PARAMETER TYPE " + type);
						System.out.println("PARAMETER NAME " + name);
						System.out.println("PARAMETER PARENT " + parentId);  //parent is from moduletemplates table for modules!!!!
						System.out.println("PARAMETER LVL " + lvl);

					}

				// parameterId=++newpamid;
				// parameterId=++countingParamIds;
				// dbidParamHashMap.put(parameterId,parameterId+newpamid);
				/*
				 * if (name != null) if (configId < 0 && name.equals("RegionPSet")) {
				 * System.out.println("PARAMETER ID before " + parameterId);
				 * System.out.println("countingParamIds ID before " + countingParamIds);
				 * 
				 * }
				 */
				//TODO: uncomment (although it does nothing IMO)
				//dbidParamHashMap.put(parameterId, ++countingParamIds); //bsataric: this hashmap is actually not used anywhere...
				//parameterId = countingParamIds; //this is also done almost for no reason at all

				/*
				 * if (name != null) if (configId < 0 && name.equals("RegionPSet")) {
				 * System.out.println("PARAMETER ID after " + parameterId);
				 * System.out.println("countingParamIds ID after " + countingParamIds);
				 * 
				 * }
				 */

				if (name == null)
					name = "";
				if (name.contains("Empty name"))
					name = "";

				if (type.equals("bool")) {
// System.err.println("getParameters: name "+name+" id="+parameterId+" parentId "+parentId);
					String valueAsString = (new Boolean(rsParameters.getBoolean(8))).toString();
					idToValueAsString.put(parameterId, valueAsString);
				}

				if (type.contains("int")) {
					// Long value = new Long(rsParameters.getLong(8));
					boolean isHex = false; // rsIntValues.getBoolean(4);

					if (rsParameters.getInt(10) > 0)
						isHex = true;

					String valueAsString = rsParameters.getString(8);
// System.err.println("getParameters: name "+name+" id="+parameterId+" parentId "+parentId);

					if (type.contains("v")) { // should handle vector hex representation
						valueAsString = rsParameters.getString(8);
					} else if ((isHex) && (type.contains("64")))
						valueAsString = "0x" + Long.toHexString(rsParameters.getLong(8));
					else if (isHex)
						valueAsString = "0x" + Integer.toHexString(rsParameters.getInt(8));

					Clob valueAsStringLOB = rsParameters.getClob(9);
					if (valueAsStringLOB != null) {
						int lobLength = (int) valueAsStringLOB.length();
						valueAsString = valueAsStringLOB.getSubString(1, lobLength);
						valueAsString = valueAsString.trim();
					}
					if (valueAsString != null) {
						if (valueAsString.startsWith("{"))
							valueAsString = valueAsString.substring(1, valueAsString.length() - 1);
						valueAsString = valueAsString.trim();
					}

					idToValueAsString.put(parameterId, valueAsString);
				}

				if (type.contains("double")) {
					String valueAsString = rsParameters.getString(8);

					Clob valueAsStringLOB = rsParameters.getClob(9);
					if (valueAsStringLOB != null) {
						int lobLength = (int) valueAsStringLOB.length();
						valueAsString = valueAsStringLOB.getSubString(1, lobLength);
						valueAsString = valueAsString.trim();
					}
					if (valueAsString != null) {
						if (valueAsString.startsWith("{"))
							valueAsString = valueAsString.substring(1, valueAsString.length() - 1);
						valueAsString = valueAsString.trim();
					}
					// valueAsString=valueAsString.trim();

					idToValueAsString.put(parameterId, valueAsString);
				}

				/////////////////
//          while (rsStringValues.next()) {
				if (type.contains("string") || type.contains("InputTag") || type.contains("ESInputTag")
						|| type.contains("FileInPath") || type.contains("EventID")) {

					String valueAsString = rsParameters.getString(8); // get PARAMETER_VALUE

					// if (valueAsString.startsWith("{")) valueAsString=valueAsString.substring(1,
					// valueAsString.length()-1);
					// valueAsString=valueAsString.trim();
					Clob valueAsStringLOB = rsParameters.getClob(9);
					if (valueAsStringLOB != null) {
						int lobLength = (int) valueAsStringLOB.length();
						valueAsString = valueAsStringLOB.getSubString(1, lobLength);
						valueAsString = valueAsString.trim();
					}
					if (valueAsString != null) {
						if (valueAsString.startsWith("{"))
							valueAsString = valueAsString.substring(1, valueAsString.length() - 1);
						valueAsString = valueAsString.trim();
					}
					idToValueAsString.put(parameterId, valueAsString);

				}
				///////////////////

//           }

//            if (type.equals("Pset") {

				/*
				 * if(configId==0) { rsParameters = psSelectParametersTemplates.executeQuery();
				 * } else { rsParameters=psSelectParameters.executeQuery(); }
				 * 
				 * int previouslvl=0; // Queue<Integer> idlifo = new LinkedList<Integer>();
				 * while (rsParameters.next()) { int parameterId = rsParameters.getInt(1);
				 * String type = rsParameters.getString(2); String name =
				 * rsParameters.getString(3); boolean isTrkd = rsParameters.getBoolean(4); int
				 * seqNb = rsParameters.getInt(5); int parentId = rsParameters.getInt(6); int
				 * lvl = rsParameters.getInt(7);
				 */

				int orparid = parentId;
				// System.err.println("ParId "+parentId+" (origparid "+orparid+") parameterId
				// "+parameterId+" type "+ type+" name "+name+" seqNb "+seqNb+" lvl"+lvl);

				while (lvl < previouslvl) {
					int tmp = idlifo.pop();
					previouslvl--;

					//if (name.equals("RegionPSet"))
						//System.out.println("IDLIFO after POP: " + idlifo);

				}
				if (lvl > 0) {
					parentId = idlifo.peek(); //7076738 is important
					if (parentId == 7076738) {
						counter++;
						System.out.println("counter " + counter);
					}
				}
				previouslvl = lvl;

				// if (configId<0) System.out.println("ParId "+parentId+" (origparid
				// "+orparid+") parameterId "+parameterId+" type "+
				// type+" name "+name+" seqNb "+seqNb+" lvl"+lvl);

				String valueAsString = null;
				if (type.indexOf("PSet") < 0)
					valueAsString = idToValueAsString.remove(parameterId);
				if (valueAsString == null)
					valueAsString = "";

				Parameter p = ParameterFactory.create(type, name, valueAsString, isTrkd);
				
				if (parentId == 7076738) {
					System.out.println("Paramter p: " + p);
					System.out.println("PARAMETER ID " + parameterId);
					System.out.println("PARAMETER TYPE " + type);
					System.out.println("PARAMETER NAME " + name);
					System.out.println("PARAMETER PARENT " + parentId);  //parent is from moduletemplates table for modules!!!!
					System.out.println("PARAMETER LVL " + lvl);
				}

				/*
				 * if (name.equals("RegionPSet")) System.out.println("parameter: " +
				 * p.toString() + " valueAsString " + valueAsString);
				 */

				if (type.equals("PSet")) {
					// idlifo.push(new Integer(parameterId));
					idlifo.push(parameterId);

					if (name.equals("RegionPSet"))
						System.out.println("IDLIFO after PUSH: " + idlifo);

					previouslvl++;
					psets.add(new IdPSetPair(parameterId, (PSetParameter) p));
				}
				if (type.equals("VPSet")) {
					// idlifo.push(new Integer(parameterId));
					idlifo.push(parameterId);
					previouslvl++;
					vpsets.add(new IdVPSetPair(parameterId, (VPSetParameter) p));
				}

				ArrayList<Parameter> parameters = null;
				if (idToParameters.containsKey(parentId))
					parameters = idToParameters.get(parentId);
				else {
					parameters = new ArrayList<Parameter>();
					idToParameters.put(parentId, parameters);
				}
				while (parameters.size() <= seqNb)
					parameters.add(null);
				parameters.set(seqNb, p);

				if (name.equals("RegionPSet")) {
					// System.out.println("RegionPSet parameters: " + parameters);
				}

			}

			Iterator<IdPSetPair> itPSet = psets.iterator();
			while (itPSet.hasNext()) {
				IdPSetPair pair = itPSet.next();
				int psetId = pair.id;
				PSetParameter pset = pair.pset;
				ArrayList<Parameter> parameters = idToParameters.remove(psetId);
				if (parameters != null) {
					int missingCount = 0;
					Iterator<Parameter> it = parameters.iterator();
					while (it.hasNext()) {
						Parameter p = it.next();
						// System.out.println("PARAMETER P: " + p);
						if (p == null)
							missingCount++;
						else
							pset.addParameter(p);
					}
					// TODO Database check. This is happening.
					if (missingCount > 0)
						System.err.println("WARNING: " + missingCount + " parameter(s)" + " missing from PSet '"
								+ pset.name() + "'");
				}
			}

			Iterator<IdVPSetPair> itVPSet = vpsets.iterator();
			while (itVPSet.hasNext()) {
				IdVPSetPair pair = itVPSet.next();
				int vpsetId = pair.id;
				VPSetParameter vpset = pair.vpset;
				ArrayList<Parameter> parameters = idToParameters.remove(vpsetId);
				if (parameters != null) {
					int missingCount = 0;
					Iterator<Parameter> it = parameters.iterator();
					while (it.hasNext()) {
						Parameter p = it.next();
						if (p == null || !(p instanceof PSetParameter))
							missingCount++;
						else
							vpset.addParameterSet((PSetParameter) p);
					}
					// TODO database check, this is happening.
					if (missingCount > 0)
						System.err.println(
								"WARNING: " + missingCount + " pset(s)" + " missing from VPSet '" + vpset.name() + "'");
				}
			}

		} catch (SQLException e) {
			String errMsg = "ConfDB::getParameters() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rsParameters);
			dbConnector.release(rsBooleanValues);
			dbConnector.release(rsIntValues);
			dbConnector.release(rsRealValues);
			dbConnector.release(rsStringValues);
		}

		return idToParameters;
	}

	private String CLOBToString(Clob cl) throws IOException, SQLException {
		if (cl == null)
			return "";
		StringBuffer strOut = new StringBuffer();
		String aux;
		BufferedReader br = new BufferedReader(cl.getCharacterStream());
		while ((aux = br.readLine()) != null)
			strOut.append(aux);

		return strOut.toString();
	}

	/** set parameters of an instance */
	private void updateInstanceParameters(Instance instance, ArrayList<Parameter> parameters) {
		if (parameters == null)
			return;
		int id = instance.databaseId();
		Iterator<Parameter> it = parameters.iterator();
		while (it.hasNext()) {
			Parameter p = it.next();
			if (p == null)
				continue;
			instance.updateParameter(p.name(), p.type(), p.valueAsString());
		}
		instance.setDatabaseId(id);
	}

	/** insert vpset into ParameterSets table */
	private void insertVecParameterSet(int parentId, int sequenceNb, int lvl, VPSetParameter vpset,
			PreparedStatement dbstmnt) throws DatabaseException {
		// int vpsetId = insertSuperId();
		ResultSet rs = null;
		try {
			if (dbstmnt == psInsertMoElement) {
				dbstmnt.setInt(1, 3); // moetype
				dbstmnt.setString(2, vpset.name());
				dbstmnt.setNull(3, Types.INTEGER);
				dbstmnt.setString(4, "VPSet");
				dbstmnt.setBoolean(5, vpset.isTracked());
				// dbstmnt.setInt(6,999);
				dbstmnt.setNull(6, Types.INTEGER);// crc
			} else {
				dbstmnt.setInt(1, parentId);
				dbstmnt.setString(2, vpset.name());
				dbstmnt.setInt(3, lvl);
				dbstmnt.setBoolean(4, vpset.isTracked());
				dbstmnt.setString(5, "VPSet");
				dbstmnt.setInt(6, sequenceNb);
				dbstmnt.setInt(10, 3); // moetype
			}
			dbstmnt.setString(7, null);
			dbstmnt.setString(8, null);
			dbstmnt.setString(9, null);

			dbstmnt.execute();
			if (dbstmnt == psInsertMoElement) {
				rs = dbstmnt.getGeneratedKeys();
				rs.next();
				int paramId = rs.getInt(1);
				psInsertPae2Moe.setInt(1, parentId);
				psInsertPae2Moe.setInt(2, paramId);
				psInsertPae2Moe.setInt(3, lvl);
				psInsertPae2Moe.setInt(4, sequenceNb);
				// psInsertPae2Moe.addBatch();
				psInsertPae2Moe.executeUpdate();
			}

			for (int i = 0; i < vpset.parameterSetCount(); i++) {
				PSetParameter pset = vpset.parameterSet(i);
				insertParameterSet(parentId, i, lvl + 1, pset, dbstmnt);
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertVecParameterSet(parId=" + parentId + ",sequenceNb=" + sequenceNb + ",vpset="
					+ vpset.name() + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
		/*
		 * try { if (dbstmnt==psInsertMoElement) { psInsertPae2Moe.executeBatch(); } }
		 * catch (SQLException e) { String errMsg =
		 * "ConfDB::insertVecParameterSet(parId="+parentId+") failed "+
		 * "(batch insert):" + e.getMessage(); throw new DatabaseException(errMsg,e); }
		 */

		// insertSuperIdVecParamSetAssoc(superId,vpsetId,sequenceNb);
	}

	/** insert pset into ParameterSets table */
	private void insertParameterSet(int parentId, int sequenceNb, int lvl, PSetParameter pset,
			PreparedStatement dbstmnt) throws DatabaseException {
		// int psetId = insertSuperId();
		ResultSet rs = null;
		try {
			if (dbstmnt == psInsertMoElement) {
				dbstmnt.setInt(1, 2); // moetype
				dbstmnt.setString(2, pset.name());
				dbstmnt.setInt(3, -1);
				dbstmnt.setString(4, "PSet");
				dbstmnt.setBoolean(5, pset.isTracked());
				// dbstmnt.setInt(6,999);
				dbstmnt.setNull(6, Types.INTEGER);// crc
			} else {
				dbstmnt.setInt(1, parentId);
				dbstmnt.setString(2, pset.name());
				dbstmnt.setInt(3, lvl);
				dbstmnt.setBoolean(4, pset.isTracked());
				dbstmnt.setString(5, "PSet");
				dbstmnt.setInt(6, sequenceNb);
				dbstmnt.setInt(10, 2); // moetype
			}
			dbstmnt.setString(7, null);
			dbstmnt.setString(8, null);
			dbstmnt.setString(9, null);
			dbstmnt.execute();
			// rs=dbstmnt.getGeneratedKeys();
//            rs.next();
//            int psetId = rs.getInt(1);
			if (dbstmnt == psInsertMoElement) {
				rs = dbstmnt.getGeneratedKeys();
				rs.next();
				int paramId = rs.getInt(1);
				psInsertPae2Moe.setInt(1, parentId);
				psInsertPae2Moe.setInt(2, paramId);
				psInsertPae2Moe.setInt(3, lvl);
				psInsertPae2Moe.setInt(4, sequenceNb);
				// psInsertPae2Moe.addBatch();
				psInsertPae2Moe.executeUpdate();
			}

			for (int i = 0; i < pset.parameterCount(); i++) {
				Parameter p = pset.parameter(i);
				if (p instanceof PSetParameter) {
					PSetParameter ps = (PSetParameter) p;
					insertParameterSet(parentId, i, lvl + 1, ps, dbstmnt);
				} else if (p instanceof VPSetParameter) {
					VPSetParameter vps = (VPSetParameter) p;
					insertVecParameterSet(parentId, i, lvl + 1, vps, dbstmnt);
				} else {
					insertParameter(parentId, i, lvl + 1, p, dbstmnt);
				}
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertParameterSet(parId=" + parentId + ",sequenceNb=" + sequenceNb + ",pset="
					+ pset.name() + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}

		/*
		 * try { if (dbstmnt==psInsertMoElement) { psInsertPae2Moe.executeBatch(); } }
		 * catch (SQLException e) { String errMsg =
		 * "ConfDB::insertParameterSet(parId="+parentId+") failed "+ "(batch insert):" +
		 * e.getMessage(); throw new DatabaseException(errMsg,e); }
		 */

//      insertSuperIdParamSetAssoc(superId,psetId,sequenceNb);
	}

	/** insert parameter into Parameters table */
	private void insertParameter(int parentId, int sequenceNb, int lvl, Parameter parameter, PreparedStatement dbstmnt)
			throws DatabaseException {
		ResultSet rs = null;
		try {
			/* Fix for File in Path Error */
			/*
			 * if(parameter instanceof FileInPathParameter){ FileInPathParameter
			 * fileInPathParameter = (FileInPathParameter)parameter; //
			 * if(fileInPathParameter.valueAsString().length()==0||fileInPathParameter.
			 * valueAsString()==null) fileInPathParameter.setValue("' '"); }
			 */

			if (dbstmnt == psInsertMoElement) {
				dbstmnt.setInt(1, 1); // moetype
				dbstmnt.setString(2, parameter.name());
				dbstmnt.setNull(3, Types.INTEGER);
				dbstmnt.setString(4, parameter.type());
				dbstmnt.setBoolean(5, parameter.isTracked());
				// dbstmnt.setInt(6,999);
				dbstmnt.setNull(6, Types.INTEGER);// crc
			} else {
				dbstmnt.setInt(1, parentId);
				dbstmnt.setString(2, parameter.name());
				dbstmnt.setInt(3, lvl);
				dbstmnt.setBoolean(4, parameter.isTracked());
				dbstmnt.setString(5, parameter.type());
				dbstmnt.setInt(6, sequenceNb);
				dbstmnt.setInt(10, 1); // moetype
			}

			String value = "";
			int hexo = 0;

			if (parameter instanceof VectorParameter) {
				value = "{ ";
				VectorParameter vp = (VectorParameter) parameter;
				for (int i = 0; i < vp.vectorSize(); i++) {
					if (vp instanceof VStringParameter) {
						value += " \"" + (String) vp.value(i) + "\" ";
					} else if (vp instanceof VUInt64Parameter) {
						Long vuint64 = ((BigInteger) vp.value(i)).longValue();
						value += " " + vuint64.toString() + " ";
					} else if (vp instanceof VInputTagParameter) {
						if (((String) vp.value(i)).isEmpty())
							value += " \"\" ";
						else
							value += " " + ((String) vp.value(i)) + " ";
					} else {
						value += " " + (vp.value(i)).toString() + " ";
					}
					if (i < (vp.vectorSize() - 1)) {
						value += ",";
					}

					if (vp instanceof VInt32Parameter) {
						VInt32Parameter vint32 = (VInt32Parameter) vp;
						if (vint32.isHex(i))
							hexo = 1;
					} else if (vp instanceof VUInt32Parameter) {
						VUInt32Parameter vuint32 = (VUInt32Parameter) vp;
						if (vuint32.isHex(i))
							hexo = 1;
					} else if (vp instanceof VInt64Parameter) {
						VInt64Parameter vint64 = (VInt64Parameter) vp;
						if (vint64.isHex(i))
							hexo = 1;
					} else if (vp instanceof VUInt64Parameter) {
						VUInt64Parameter vuint64 = (VUInt64Parameter) vp;
						if (vuint64.isHex(i))
							hexo = 1;
					}

				}
				value += " }";

			} else {
				ScalarParameter sp = (ScalarParameter) parameter;
				if (sp instanceof StringParameter) {
					value = ((StringParameter) sp).valueAsString();
				} else if (sp instanceof FileInPathParameter) {
					value = ((FileInPathParameter) sp).valueAsString();
				} else if (sp instanceof UInt64Parameter) {
					// Long vuint64=((BigInteger)sp.value()).longValue();
					value = Long.toString(((BigInteger) sp.value()).longValue());
				} else if (sp instanceof BoolParameter) {
					if ((Boolean) sp.value())
						value = "1";
					else
						value = "0";
				} else {
					value = (String) sp.valueAsString();
				}
				if (sp instanceof Int32Parameter) {
					Int32Parameter int32 = (Int32Parameter) sp;
					if (int32.isHex()) {
						hexo = 1;
						value = int32.value().toString();
					}
				} else if (sp instanceof UInt32Parameter) {
					UInt32Parameter uint32 = (UInt32Parameter) sp;
					if (uint32.isHex()) {
						hexo = 1;
						value = uint32.value().toString();
					}
				} else if (sp instanceof Int64Parameter) {
					Int64Parameter int64 = (Int64Parameter) sp;
					if (int64.isHex()) {
						hexo = 1;
						// value=int64.value().toString();
					}
				} else if (sp instanceof UInt64Parameter) {
					UInt64Parameter uint64 = (UInt64Parameter) sp;
					if (uint64.isHex()) {
						hexo = 1;
						// value=uint64.value().toString();
					}
				}

			}

			if (value.length() < 4000) {
				dbstmnt.setString(7, value);
				dbstmnt.setString(8, null);
			} else {
				dbstmnt.setString(7, null);
				dbstmnt.setString(8, value);
			}

			dbstmnt.setInt(9, hexo);
			dbstmnt.executeUpdate();

			if (dbstmnt == psInsertMoElement) {
				rs = dbstmnt.getGeneratedKeys();
				rs.next();
				int paramId = rs.getInt(1);
				psInsertPae2Moe.setInt(1, parentId);
				psInsertPae2Moe.setInt(2, paramId);
				psInsertPae2Moe.setInt(3, lvl);
				psInsertPae2Moe.setInt(4, sequenceNb);

				// psInsertPae2Moe.addBatch();
				psInsertPae2Moe.executeUpdate();
			}

		} catch (SQLException e) {
			String errMsg = "ConfDB::insertParameter(parId=" + parentId + ",sequenceNb=" + sequenceNb + ",parameter="
					+ parameter.name() + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}

		/*
		 * try { if (dbstmnt==psInsertMoElement) { psInsertPae2Moe.executeBatch(); } }
		 * catch (SQLException e) { String errMsg =
		 * "ConfDB::insertParameter(parId="+parentId+") failed "+ "(batch insert):" +
		 * e.getMessage(); throw new DatabaseException(errMsg,e); }
		 */

	}

	/** associate parameter with the service/module superid */
	private void insertSuperIdParamAssoc(int superId, int paramId, int sequenceNb) throws DatabaseException {
		try {
			psInsertSuperIdParamAssoc.setInt(1, superId);
			psInsertSuperIdParamAssoc.setInt(2, paramId);
			psInsertSuperIdParamAssoc.setInt(3, sequenceNb);
			psInsertSuperIdParamAssoc.addBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertSuperIdParamAssoc(superId=" + superId + ",paramId=" + paramId
					+ ",sequenceNb=" + sequenceNb + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** associate pset with the service/module superid */
	private void insertSuperIdParamSetAssoc(int superId, int psetId, int sequenceNb) throws DatabaseException {
		try {
			psInsertSuperIdParamSetAssoc.setInt(1, superId);
			psInsertSuperIdParamSetAssoc.setInt(2, psetId);
			psInsertSuperIdParamSetAssoc.setInt(3, sequenceNb);
			psInsertSuperIdParamSetAssoc.addBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::inesrtSuperIdParamSetAssoc(superId=" + superId + ",psetId=" + psetId
					+ ",sequenceNb=" + sequenceNb + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** associate vpset with the service/module superid */
	private void insertSuperIdVecParamSetAssoc(int superId, int vpsetId, int sequenceNb) throws DatabaseException {
		try {
			psInsertSuperIdVecParamSetAssoc.setInt(1, superId);
			psInsertSuperIdVecParamSetAssoc.setInt(2, vpsetId);
			psInsertSuperIdVecParamSetAssoc.setInt(3, sequenceNb);
			psInsertSuperIdVecParamSetAssoc.addBatch();
		} catch (SQLException e) {
			String errMsg = "ConfDB::inesrtSuperIdVecParamSetAssoc(superId=" + superId + ",vpsetId=" + vpsetId
					+ ",sequenceNb=" + sequenceNb + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** insert a parameter value in the table corresponding to the parameter type */
	private void insertParameterValue(int paramId, Parameter parameter) throws DatabaseException {
		if (!parameter.isValueSet()) {
			// PS 05/06/08: allow unset tracked parameters to be saved
			// -------------------------------------------------------
			// if (parameter.isTracked()) {
			// String errMsg =
			// "ConfDB::insertParameterValue(paramId="+paramId+
			// ",parameter="+parameter.name()+") failed: parameter is tracked"+
			// " but not set.";
			// throw new DatabaseException(errMsg);
			// }
			// else return;
			return;
		}

		PreparedStatement psInsertParameterValue = insertParameterHashMap.get(parameter.type());

		try {
			if (parameter instanceof VectorParameter) {
				VectorParameter vp = (VectorParameter) parameter;
				for (int i = 0; i < vp.vectorSize(); i++) {
					psInsertParameterValue.setInt(1, paramId);
					psInsertParameterValue.setInt(2, i);

					if (vp instanceof VStringParameter) {
						String value = "\"" + (String) vp.value(i) + "\"";
						psInsertParameterValue.setString(3, value);
					} else if (vp instanceof VUInt64Parameter) {
						psInsertParameterValue.setObject(3, ((BigInteger) vp.value(i)).longValue());
					} else if ((vp instanceof VInputTagParameter) || (vp instanceof VESInputTagParameter)) {
						// fix to bug #90850: "Export Configuration Failed"
						String value = (String) vp.value(i);
						if (value.isEmpty())
							value = "\"\"";
						psInsertParameterValue.setString(3, value);
					} else {
						psInsertParameterValue.setObject(3, vp.value(i));
					}

					if (vp instanceof VInt32Parameter) {
						VInt32Parameter vint32 = (VInt32Parameter) vp;
						psInsertParameterValue.setBoolean(4, vint32.isHex(i));
					} else if (vp instanceof VUInt32Parameter) {
						VUInt32Parameter vuint32 = (VUInt32Parameter) vp;
						psInsertParameterValue.setBoolean(4, vuint32.isHex(i));
					} else if (vp instanceof VInt64Parameter) {
						VInt64Parameter vint64 = (VInt64Parameter) vp;
						psInsertParameterValue.setBoolean(4, vint64.isHex(i));
					} else if (vp instanceof VUInt64Parameter) {
						VUInt64Parameter vuint64 = (VUInt64Parameter) vp;
						psInsertParameterValue.setBoolean(4, vuint64.isHex(i));
					}
					psInsertParameterValue.addBatch();
				}
			} else {
				ScalarParameter sp = (ScalarParameter) parameter;
				psInsertParameterValue.setInt(1, paramId);

				if (sp instanceof StringParameter) {
					StringParameter string = (StringParameter) sp;
					psInsertParameterValue.setString(2, string.valueAsString());
				} else if (sp instanceof FileInPathParameter) {
					FileInPathParameter fileInPathParameter = (FileInPathParameter) sp;
					psInsertParameterValue.setString(2, fileInPathParameter.valueAsString());
				} else if (sp instanceof UInt64Parameter) {
					psInsertParameterValue.setObject(2, ((BigInteger) sp.value()).longValue());
				} else {
					psInsertParameterValue.setObject(2, sp.value());
				}

				if (sp instanceof Int32Parameter) {
					Int32Parameter int32 = (Int32Parameter) sp;
					psInsertParameterValue.setBoolean(3, int32.isHex());
				} else if (sp instanceof UInt32Parameter) {
					UInt32Parameter uint32 = (UInt32Parameter) sp;
					psInsertParameterValue.setBoolean(3, uint32.isHex());
				} else if (sp instanceof Int64Parameter) {
					Int64Parameter int64 = (Int64Parameter) sp;
					psInsertParameterValue.setBoolean(3, int64.isHex());
				} else if (sp instanceof UInt64Parameter) {
					UInt64Parameter uint64 = (UInt64Parameter) sp;
					psInsertParameterValue.setBoolean(3, uint64.isHex());
				}
				psInsertParameterValue.addBatch();
			}
		} catch (Exception e) {
			String errMsg = "ConfDB::insertParameterValue(paramId=" + paramId + ",parameter=" + parameter.name()
					+ ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** associate a template super id with a software release */
	private void insertSuperIdReleaseAssoc(int superId, String releaseTag) throws DatabaseException {
		int releaseId = getReleaseId(releaseTag);
		try {
			psInsertSuperIdReleaseAssoc.setInt(1, superId);
			psInsertSuperIdReleaseAssoc.setInt(2, releaseId);
			psInsertSuperIdReleaseAssoc.executeUpdate();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertSuperIdReleaseAssoc(superId=" + superId + ",releaseTag=" + releaseTag
					+ ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** associate a template super id with a software release */
	private void insertSuperIdReleaseAssoc(int superId, int releaseId) throws DatabaseException {
		try {
			psInsertSuperIdReleaseAssoc.setInt(1, superId);
			psInsertSuperIdReleaseAssoc.setInt(2, releaseId);
			psInsertSuperIdReleaseAssoc.executeUpdate();
		} catch (SQLException e) {
			String errMsg = "ConfDB::insertSuperIdReleaseAssoc(superId=" + superId + ",releaseId=" + releaseId
					+ ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		}
	}

	/** get the release id for a release tag */
	public int getReleaseId(String releaseTag) throws DatabaseException {
		reconnect();

		ResultSet rs = null;
		try {
			psSelectReleaseId.setString(1, releaseTag);
			rs = psSelectReleaseId.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			String errMsg = "ConfDB::getReleaseId(releaseTag=" + releaseTag + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** get the release id for a release tag */
	private String getReleaseTag(int releaseId) throws DatabaseException {
		ResultSet rs = null;
		try {
			psSelectReleaseTag.setInt(1, releaseId);
			rs = psSelectReleaseTag.executeQuery();
			rs.next();
			return rs.getString(1);
		} catch (SQLException e) {
			String errMsg = "ConbfDB::getReleaseTag(releaseId=" + releaseId + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** get the release id for a release tag */
	private String getReleaseTagForConfig(int configId) throws DatabaseException {
		reconnect();

		ResultSet rs = null;
		try {
			psSelectReleaseTagForConfig.setInt(1, configId);
			rs = psSelectReleaseTagForConfig.executeQuery();
			rs.next();
			return rs.getString(1);
		} catch (SQLException e) {
			String errMsg = "ConbfDB::getReleaseTagForConfig(configId=" + configId + ") failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}
	}

	/** look for ConfigInfo in the specified parent directory */
	private ConfigInfo getConfigNewInfo(int configId, Directory parentDir) {
		for (int i = 0; i < parentDir.configInfoCount(); i++) {
			ConfigInfo configInfo = parentDir.configInfo(i);
			for (int ii = 0; ii < configInfo.versionCount(); ii++) {
				ConfigVersion configVersion = configInfo.version(ii);
				if (configVersion.dbId() == configId) {
					configInfo.setVersionIndex(ii);
					return configInfo;
				}
			}
		}

		for (int i = 0; i < parentDir.childDirCount(); i++) {
			ConfigInfo configInfo = getConfigNewInfo(configId, parentDir.childDir(i));
			if (configInfo != null)
				return configInfo;
		}

		return null;
	}

	/** get subsystems and a hash map to all packages */
	private ArrayList<SoftwareSubsystem> getSubsystems(HashMap<Integer, SoftwarePackage> idToPackage)
			throws DatabaseException {
		ArrayList<SoftwareSubsystem> result = new ArrayList<SoftwareSubsystem>();

		HashMap<Integer, SoftwareSubsystem> idToSubsystem = new HashMap<Integer, SoftwareSubsystem>();

		ResultSet rs = null;
		try {
			rs = psSelectSoftwareSubsystems.executeQuery();

			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				SoftwareSubsystem subsystem = new SoftwareSubsystem(name);
				result.add(subsystem);
				idToSubsystem.put(id, subsystem);
			}

			rs = psSelectSoftwarePackages.executeQuery();

			while (rs.next()) {
				int id = rs.getInt(1);
				int subsysId = rs.getInt(2);
				String name = rs.getString(3);

				SoftwarePackage pkg = new SoftwarePackage(name);
				pkg.setSubsystem(idToSubsystem.get(subsysId));
				idToPackage.put(id, pkg);
			}
		} catch (SQLException e) {
			String errMsg = "ConfDB::getSubsystems() failed: " + e.getMessage();
			throw new DatabaseException(errMsg, e);
		} finally {
			dbConnector.release(rs);
		}

		return result;
	}

	public String debugSQL(PreparedStatement pstmt) {
		String value = "<SQL not found>";
		try {
			Class<?> stmt1 = pstmt.getClass();
			java.lang.reflect.Field mem = stmt1.getField("sql");
			value = (String) mem.get(pstmt);
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
	public static void main(String[] args) {
		String configId = "";
		String configName = "";

		String releaseId = "";
		String releaseName = "";

		boolean dolistcounts = false;
		boolean dolistconf = false;
		boolean dolistrel = false;
		String list = "";

		boolean dopackages = false;
		boolean doversions = false;
		boolean doremove = false;

		String dbType = "oracle";
		String dbHost = "cmsr1-v.cern.ch";
		String dbPort = "10121";
		String dbName = "cmsr.cern.ch";
		String dbUser = "cms_hlt_gdr";
		String dbPwrd = "";

		for (int iarg = 0; iarg < args.length; iarg++) {
			String arg = args[iarg];
			if (arg.equals("--configId")) {
				configId = args[++iarg];
			} else if (arg.equals("--configName")) {
				configName = args[++iarg];
			} else if (arg.equals("--releaseId")) {
				releaseId = args[++iarg];
			} else if (arg.equals("--releaseName")) {
				releaseName = args[++iarg];
			} else if (arg.equals("--listCounts")) {
				dolistcounts = true;
			} else if (arg.equals("--listConfigs")) {
				dolistconf = true;
				list = args[++iarg];
			} else if (arg.equals("--listReleases")) {
				dolistrel = true;
			} else if (arg.equals("--packages")) {
				dopackages = true;
			} else if (arg.equals("--remove")) {
				doremove = true;
			} else if (arg.equals("--versions")) {
				doversions = true;
			} else if (arg.equals("-t") || arg.equals("--dbtype")) {
				dbType = args[++iarg];
			} else if (arg.equals("-h") || arg.equals("--dbhost")) {
				dbHost = args[++iarg];
			} else if (arg.equals("-p") || arg.equals("--dbport")) {
				dbPort = args[++iarg];
			} else if (arg.equals("-d") || arg.equals("--dbname")) {
				dbName = args[++iarg];
			} else if (arg.equals("-u") || arg.equals("--dbuser")) {
				dbUser = args[++iarg];
			} else if (arg.equals("-s") || arg.equals("--dbpwrd")) {
				dbPwrd = args[++iarg];
			} else {
				System.err.println("ERROR: invalid option '" + arg + "'!");
				System.exit(0);
			}
		}

		int check = 0;
		if (configId.length() > 0)
			check++;
		if (configName.length() > 0)
			check++;
		if (releaseId.length() > 0)
			check++;
		if (releaseName.length() > 0)
			check++;

		if (check == 0 && !dolistcounts && !dolistconf && !dolistrel) {
			System.err.println("ERROR: specify config, release, ");
			System.exit(0);
		}
		if ((check > 1 || (dolistconf && dolistrel)) || (check == 0 && (dolistconf && dolistrel))) {
			System.err.println("ERROR: specify either of " + "--configId, --configName, "
					+ "--releaseId, --releaseName," + "--listConfigs, *or* --listReleases");
			System.exit(0);
		}

		if (!dolistcounts && !dolistconf && !dolistrel && !dopackages && !doversions && !doremove)
			System.exit(0);

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

		ConfDB database = new ConfDB();

		try {
			database.connect(dbType, dbUrl, dbUser, dbPwrd);
			// list configurations
			if (dolistcounts) {
				database.listCounts();
			} else if (dolistconf) {
				String[] allConfigs = database.getConfigNames();
				int count = 0;
				for (String s : allConfigs)
					if (s.startsWith(list)) {
						count++;
						System.out.println(s);
					}
				System.out.println(count + " configurations");
			}
			// list releases
			else if (dolistrel) {
				String[] allReleases = database.getReleaseTagsSorted();
				for (String s : allReleases)
					System.out.println(s);
				System.out.println(allReleases.length + " releases");
			}
			// configurations
			else if (configId.length() > 0 || configName.length() > 0) {
				int id = (configId.length() > 0) ? Integer.parseInt(configId) : database.getConfigNewId(configName);
				if (id <= 0)
					System.out.println("Configuration not found!");
				else if (dopackages) {
					Configuration config = database.loadConfiguration(id);
					SoftwareRelease release = config.release();
					Iterator<String> it = release.listOfReferencedPackages().iterator();
					while (it.hasNext())
						System.out.println(it.next());
				} else if (doversions) {
					ConfigInfo info = database.getConfigNewInfo(id);
					System.out.println("name=" + info.parentDir().name() + "/" + info.name());
					for (int i = 0; i < info.versionCount(); i++) {
						ConfigVersion version = info.version(i);
						System.out.println(version.version() + "\t" + version.dbId() + "\t" + version.releaseTag()
								+ "\t" + version.created() + "\t" + version.creator());
						if (version.comment().length() > 0)
							System.out.println("  -> " + version.comment());
					}
				} else if (doremove) {
					ConfigInfo info = database.getConfigNewInfo(id);
					System.out.println("REMOVE " + info.parentDir().name() + "/" + info.name() + "/V" + info.version());
					try {
						database.removeConfiguration(info.dbId());
					} catch (DatabaseException e2) {
						System.out.println("REMOVE FAILED!");
						e2.printStackTrace();
					}
				}
			}
			// releases
			else if (releaseId.length() > 0 || releaseName.length() > 0) {
				int id = (releaseId.length() > 0) ? Integer.parseInt(releaseId) : database.getReleaseId(releaseName);
				if (id <= 0)
					System.err.println("Release not found!");
				else if (dopackages) {
					SoftwareRelease release = new SoftwareRelease();
					database.loadSoftwareRelease(id, release);
					Iterator<String> it = release.listOfPackages().iterator();
					while (it.hasNext())
						System.out.println(it.next());
				} else if (doremove) {
					String[] configs = database.getConfigNamesByRelease(id);
					if (configs.length > 0) {
						System.out.println(
								configs.length + " configurations " + "associated with release " + releaseName + ":");
						for (String s : configs)
							System.out.println(s);
						System.out.println("\nDO YOU REALLY WANT TO DELETE ALL " + "LISTED RELEASES (YES/NO)?! ");
						BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						String answer = null;
						try {
							answer = br.readLine();
						} catch (IOException ioe) {
							System.exit(1);
						}
						if (!answer.equals("YES"))
							System.exit(0);
						System.out.println("REMOVE CONFIGURATIONS!");
						for (String s : configs) {
							System.out.print("Remove " + s + "... ");
							int cid = database.getConfigNewId(s);
							database.removeConfiguration(cid);
							System.out.println("REMOVED");
						}
					}
					System.out.print("\nRemove " + releaseName + "... ");
					database.removeSoftwareRelease(id);
					System.out.println("REMOVED");
				}
			}
		} catch (DatabaseException e) {
			System.err.println("Failed to connet to DB: " + e.getMessage());
		} finally {
			try {
				database.disconnect();
			} catch (DatabaseException e) {
			}
		}
	}

}

//
// helper classes
//

/** define class holding a pair of id and associated instance */
class IdInstancePair {
	public int id;
	public Instance instance;

	IdInstancePair(int id, Instance instance) {
		this.id = id;
		this.instance = instance;
	}
}

/** define class holding a pair of id and associated PSet */
class IdPSetPair {
	public int id;
	public PSetParameter pset;

	IdPSetPair(int id, PSetParameter pset) {
		this.id = id;
		this.pset = pset;
	}
}

/** define class holding a pair of id and associated VPSet */
class IdVPSetPair {
	public int id;
	public VPSetParameter vpset;

	IdVPSetPair(int id, VPSetParameter vpset) {
		this.id = id;
		this.vpset = vpset;
	}
}

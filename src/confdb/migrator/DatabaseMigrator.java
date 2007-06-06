package confdb.migrator;

import confdb.data.*;
import confdb.db.CfgDatabase;


/**
 * DatabaseMigrator
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Migrate a configuration from its current database to another database.
 */
public class DatabaseMigrator
{
    //
    // data members
    //
    
    /** configuration to be migrated */
    private Configuration sourceConfig = null;

    /** configuration to be migrated */
    private Configuration targetConfig = null;

    /** source database */
    private CfgDatabase sourceDB = null;

    /** target database */
    private CfgDatabase targetDB = null;
    
    /** release migrator to actually migrate the configuration */
    private ReleaseMigrator releaseMigrator = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public DatabaseMigrator(Configuration sourceConfig,
			    CfgDatabase   sourceDB,
			    CfgDatabase   targetDB)
    {
	this.sourceConfig = sourceConfig;
	this.sourceDB = sourceDB;
	this.targetDB = targetDB;
    }
    
    
    //
    // member functions
    //
    
    /** migrate the configuration from sourceDB to targetDB */
    public boolean migrate(String targetName,Directory targetDir)
    {
	SoftwareRelease sourceRelease = sourceConfig.release();
	SoftwareRelease targetRelease = new SoftwareRelease();
	String          releaseTag    = sourceRelease.releaseTag();
	
	if (!targetDB.hasSoftwareRelease(releaseTag)) return false;
	targetDB.loadSoftwareRelease(releaseTag,targetRelease);
	
	ConfigInfo targetConfigInfo = new ConfigInfo(targetName,targetDir,releaseTag);
	targetConfig = new Configuration(targetConfigInfo,targetRelease);
	
	releaseMigrator = new ReleaseMigrator(sourceConfig,targetConfig);
	releaseMigrator.migrate();
	
	if (!targetDB.insertConfiguration(targetConfig)) return false;
	
	return true;
    }

    /** retrieve the release-migrator */
    public ReleaseMigrator releaseMigrator() { return releaseMigrator; }
    
}

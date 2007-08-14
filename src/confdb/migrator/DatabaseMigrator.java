package confdb.migrator;

import confdb.data.*;
import confdb.db.ConfDB;


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
    private ConfDB sourceDB = null;

    /** target database */
    private ConfDB targetDB = null;
    
    /** release migrator to actually migrate the configuration */
    private ReleaseMigrator releaseMigrator = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public DatabaseMigrator(Configuration sourceConfig,
			    ConfDB        sourceDB,
			    ConfDB        targetDB)
    {
	this.sourceConfig = sourceConfig;
	this.sourceDB     = sourceDB;
	this.targetDB     = targetDB;
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
	String          creator       = System.getProperty("user.name");
	
	if (!targetDB.hasSoftwareRelease(releaseTag)) return false;
	targetDB.loadSoftwareRelease(releaseTag,targetRelease);
	
	ConfigInfo targetConfigInfo = new ConfigInfo(targetName,targetDir,releaseTag);
	targetConfig = new Configuration(targetConfigInfo,sourceConfig.processName(),
					 targetRelease);
	
	releaseMigrator = new ReleaseMigrator(sourceConfig,targetConfig);
	releaseMigrator.migrate();
	
	if (!targetDB.insertConfiguration(targetConfig,creator)) return false;
	
	return true;
    }

    /** retrieve the release-migrator */
    public ReleaseMigrator releaseMigrator() { return releaseMigrator; }
    
}

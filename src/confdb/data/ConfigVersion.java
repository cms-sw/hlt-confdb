package confdb.data;


/**
 * ConfigVersion
 * -------------
 * @author Philipp Schieferdecker
 *
 * Information about one version of a configuration.
 */
public class ConfigVersion implements Comparable<ConfigVersion>
{
    //
    // member data
    //
    
    /** database id */
    private int dbId = -1;

    /** version number */
    private int version = -1;
    
    /** creation date */
    private String created = null;
    
    /** release tag associated with this version */
    private String releaseTag = null;


    //
    // construction
    //

    /** standard constructor */
    public ConfigVersion(int dbId,int version,String created,String releaseTag)
    {
	this.dbId = dbId;
	this.version = version;
	this.created = created;
	this.releaseTag = releaseTag;
    }
    

    //
    // member functions
    //
   
    /** get database id */
    public int dbId() { return dbId; }
    
    /** get version number */
    public int version() { return version; }
    
    /** get creation date */
    public String created() { return created; }
    
    /** get releaese tag */
    public String releaseTag() { return releaseTag; }
    
    /** Comparable: compareTo() */
    public int compareTo(ConfigVersion otherVersion)
    {
	if      (otherVersion.version()>version())  return +1;
	else if (otherVersion.version()<version())  return -1;
	return 0;
    }
    
}

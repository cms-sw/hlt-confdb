package confdb.data;

import java.io.Serializable;


/**
 * ConfigVersion
 * -------------
 * @author Philipp Schieferdecker
 *
 * Information about one version of a configuration.
 */
public class ConfigVersion implements Comparable<ConfigVersion>, Serializable
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
    
    /** user who created this version */
    private String creator = null;
    
    /** release tag associated with this version */
    private String releaseTag = null;

    /** process name */
    private String processName = null;
    
    /** comment */
    private String comment = "";

    //
    // construction
    //

    /** standard constructor */
    public ConfigVersion(int dbId,int version,
			 String created,String creator,
			 String releaseTag,String processName,
			 String comment)
    {
	this.dbId        = dbId;
	this.version     = version;
	this.created     = created;
	this.creator     = creator;
	this.releaseTag  = releaseTag;
	this.processName = processName;
	this.comment     = comment;
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
    
    /** get creator */
    public String creator() { return creator; }
    
    /** get releaese tag */
    public String releaseTag() { return releaseTag; }
    
    /** get process name */
    public String processName() { return processName; }

    /** get comment */
    public String comment() { return comment; }

    /** Comparable: compareTo() */
    public int compareTo(ConfigVersion otherVersion)
    {
	if      (otherVersion.version()>version())  return +1;
	else if (otherVersion.version()<version())  return -1;
	return 0;
    }
    
}

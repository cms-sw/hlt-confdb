package confdb.data;

import java.util.ArrayList;
import java.util.Collections;


/**
 * ConfigInfo
 * ----------
 * @author Philipp Schieferdecker
 *
 * Information about a configuration.
 */
public class ConfigInfo
{
    //
    // member data
    //
    
    /** name of the configuration */
    private String name = null;

    /** parent directory */
    private Directory parentDir = null;
    
    /** current releaseTag */
    private String releaseTag = null;
    
    /** versions of the configuration */
    private ArrayList<ConfigVersion> versions = new ArrayList<ConfigVersion>();
    
    /** selected version index */
    private int versionIndex = -1;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ConfigInfo(String name,Directory parentDir,
		      int dbId,int version,String created,String releaseTag)
    {
	this.name         = name;
	this.parentDir    = parentDir;
	this.releaseTag   = releaseTag;
	this.versionIndex = 0;
	versions.add(new ConfigVersion(dbId,version,created,releaseTag));
    }
    
    /** constructor without a version */
    public ConfigInfo(String name,Directory parentDir,String releaseTag)
    {
	this.name         = name;
	this.parentDir    = parentDir;
	this.releaseTag   = releaseTag;
	this.versionIndex = -1;
    }


    //
    // member functions
    //
    
    /** toString overload */
    public String toString() { return name; }
    
    /** get configuration name */
    public String name() { return name; }

    /** get the parent directory */
    public Directory parentDir() { return parentDir; }
    
    /** get database id */
    public int dbId()
    {
	return (versionIndex<0) ? -1 : versions.get(versionIndex).dbId();
    }
    
    /** get latest configuration version */
    public int version()
    {
	return (versionIndex<0) ? 0 : versions.get(versionIndex).version();
    }
    
    /** get latest configuration creation date */
    public String created()
    {
	return (versionIndex<0) ? "" : versions.get(versionIndex).created();
    }
    
    /** get the latest releaseTag */
    public String releaseTag() { return releaseTag; }
    
   /** number of versions */
    public int versionCount() { return versions.size(); }
    
    /** get the i-th version */
    public ConfigVersion version(int i) { return versions.get(i); }
    
    /** add an new version of this configuration */
    public void addVersion(int dbId,int version,String created,String releaseTag)
    {
	for (ConfigVersion v : versions) {
	    if (v.version()==version) {
		System.out.println("addVersion ERROR: version exists already.");
		return;
	    }
	}
	
	ConfigVersion configVersion = new ConfigVersion(dbId,
							version,
							created,
							releaseTag);
	versions.add(configVersion);
	Collections.<ConfigVersion>sort(versions);
	this.releaseTag = releaseTag;
    }
    
    /** the the index of the selected version */
    public void setVersionIndex(int index)
    {
	versionIndex = index;
	releaseTag = versions.get(versionIndex).releaseTag();
    }
    
    /** move to the next available version number */
    public int nextVersion()
    {
	return (versionCount()>0) ? versions.get(0).version()+1 : 1;
    }
    
}

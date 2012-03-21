package confdb.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Directory
 * ---------
 * @author Philipp Schieferdecker
 *
 * Directory to hold configurations or other directories.
 */
public class Directory implements Serializable
{
    //
    // member data
    //
    
    /** database id */
    private int dbId = -1;

    /** name of the directory */
    private String name = null;

    /** creation date */
    private String created = new String();
    
    /** parent directory */
    private Directory parentDir = null;

    /** child diretories */
    private ArrayList<Directory> directories = new ArrayList<Directory>();

    /** directory entries */
    private ArrayList<ConfigInfo> configInfos = new ArrayList<ConfigInfo>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public Directory(int dbId,String name,String created,Directory parentDir)
    {
	this.dbId      = dbId;
	this.name      = name;
	this.created   = created;
	this.parentDir = parentDir;
    }
    
    
    //
    // member functions
    //

    /** overloaded toString() */
    public String toString()
    {
	if (parentDir==null) return name;
	String[] vec = name.split("/");
	return vec[vec.length-1];
    }
    
    /** database id of the directory */
    public int dbId() { return dbId; }
    
    /** name of the directory */
    public String name() { return name; }

    /** creation date of the directory */
    public String created() { return created; }
    
    /** get the parent directory */
    public Directory parentDir() { return parentDir; }

    /** retrieve configurations in this directory */
    public int configInfoCount() { return configInfos.size(); }

    /** get i=th config info */
    public ConfigInfo configInfo(int i) { return configInfos.get(i); }
    
    /** set the database id */
    public void setDbId(int dbId) { this.dbId = dbId; }

    /** set the name of the directory */
    public void setName(String name) { this.name = name; }

    /** set the parent directory */
    public void setParentDir(Directory parentDir) { this.parentDir = parentDir; }
    
    /** add information about a configuration in this dir */
    public void addConfigInfo(ConfigInfo configInfo)
    {
	configInfo.setParentDir(this);
	configInfos.add(configInfo);
    }
    
    /** number of child directories */
    public int childDirCount() { return directories.size(); }
    
    /** retrieve the i=th child dir*/
    public Directory childDir(int i) { return directories.get(i); }

    /** index of child directory */
    public int indexOfChildDir(Directory childDir)
    {
	return directories.indexOf(childDir);
    }
    
    /** add a child directory */
    public void addChildDir(Directory childDir)
    {
	childDir.setParentDir(this);
	directories.add(childDir);
    }

    /** remove a child direcory */
    public boolean removeChildDir(Directory childDir)
    {
	int i = indexOfChildDir(childDir);
	if (i>=0) {
	    directories.remove(i);
	    return true;
	}
	return false;
    }

    /** list all (direct!) daughters which are of type Directory */
    public Directory[] listOfDirectories()
    {
	Directory[] list = new Directory[childDirCount()];
	for (int i=0;i<childDirCount();i++) list[i] = childDir(i);
	return list;
    }

    /** list all (direct!) daughters of type ConfigInfo */
    public ConfigInfo[] listOfConfigurations()
    {
	ConfigInfo[] list = new ConfigInfo[configInfoCount()];
	for (int i=0;i<configInfoCount();i++) list[i]=configInfo(i);
	return list;
    }

    /** list all configurations recursively */
    public ArrayList<ConfigInfo> listAllConfigurations()
    {
	ArrayList<ConfigInfo> result = new ArrayList<ConfigInfo>();
	for (int i=0;i<configInfoCount();i++) result.add(configInfo(i));
	for (int i=0;i<childDirCount();i++) {
	    Iterator<ConfigInfo> it=childDir(i).listAllConfigurations().iterator();
	    while (it.hasNext()) result.add(it.next());
	}
	return result;
    }
    
    /** get directory with name/release filter(s) applied */
    public Directory filter(String filterString,String releaseTag)
    {
	Directory result = new Directory(dbId,name,created,parentDir);
	for (int i=0;i<childDirCount();i++) {
	    Directory filteredChildDir=childDir(i).filter(filterString,releaseTag);
	    if (filteredChildDir.childDirCount()>0||
		filteredChildDir.configInfoCount()>0)
		result.addChildDir(filteredChildDir);
	}
	boolean startsWith = filterString.startsWith("/");
	for (int i=0;i<configInfoCount();i++) {
	    ConfigInfo info = configInfo(i);
	    if (releaseTag.length()>0) {
		boolean hasReleaseTag = false;
		for (int j=0;j<info.versionCount();j++) {
		    if (info.version(j).releaseTag().equals(releaseTag)) {
			hasReleaseTag = true;
			break;
		    }
		}
		if (!hasReleaseTag) continue;
	    }
	    if (filterString.length()==0) {
		result.addConfigInfo(info);
	    }
	    else {
		String fullName = info.parentDir().name()+"/"+info.name();
		int    index    = fullName.indexOf(filterString);
		if ((startsWith&&index==0)||(!startsWith&&index>=0))
		    result.addConfigInfo(info);
	    }
	}
	return result;
    }

}

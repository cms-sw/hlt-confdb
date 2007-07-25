package confdb.data;

import java.util.ArrayList;

/**
 * Directory
 * ---------
 * @author Philipp Schieferdecker
 *
 * Directory to hold configurations or other directories.
 */
public class Directory
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

    /** add information about a configuration in this dir */
    public void addConfigInfo(ConfigInfo configInfo) { configInfos.add(configInfo); }
    
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
    public void addChildDir(Directory childDir) { directories.add(childDir); }

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
    
}

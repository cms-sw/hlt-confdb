package confdb.gui;
import confdb.db.*;
import confdb.data.*;
import java.util.ArrayList;

/*

This class indicates whether we have permissions to edit a given
config or not
It is not intended to be secure, just stop users accidently
doing things they shouldnt

This is database dependent, right now we do it off DB username
which works as we are only interested in restricting to the Offline-Run3 db
which has a different username to all other databases


*/

public class UserPermissionsManager {

    private ArrayList<String> admins = new ArrayList<String>();
	private ConfDB database = null;
    private ArrayList<String> restrictedDBUserNames = new ArrayList<String>();
    private ArrayList<String> nonRestrictedPaths = new ArrayList<String>();

    public UserPermissionsManager(ConfDB database) {
        this.database = database;
        this.admins.add("admin");
		// Andrea Bocci
		this.admins.add("fwyzard");
		// Martin Gruenewald
		this.admins.add("gruen");
		this.admins.add("martin");
		// Silvio Donato
		this.admins.add("sdonato");
		// Sam Harper
		//this.admins.add("sharper");
		// Marino Missiroli
		this.admins.add("missirol");
        // Mateusz Zarucki
        this.admins.add("mzarucki");
        this.restrictedDBUserNames.add("cms_hlt_v3_w");
        this.restrictedDBUserNames.add("cms_hlt_gdrdev_w");
        this.nonRestrictedPaths.add("/users");
    }

    private boolean isAdmin() {
        String userName = System.getProperty("user.name");
        return admins.contains(userName);
    }

    private boolean isRestrictedDB(){
        return restrictedDBUserNames.contains(database.getDbUser());
    }

    private boolean isRestrictedPath(String name){
        for(int i=0;i<nonRestrictedPaths.size();i++){
            if(name.startsWith(nonRestrictedPaths.get(i))){
                return false;
            }
        }
        return true;
    }

    private boolean isRestricted(String name){
        return isRestrictedDB() && isRestrictedPath(name);
    }

    public boolean canUnlock(){
        return isAdmin();
    }

    public boolean hasWritePermission(Configuration config){
        return hasWritePermission(config.parentDir().name());
    }
    
    public boolean hasWritePermission(String name){
        if(isRestricted(name)) {
            return isAdmin();
        }else{
            return true;
        }
    }
}
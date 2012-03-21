package confdb.data;

import java.io.Serializable;

/**
 * DatabaseEntry
 * --------------
 * @author Philipp Schieferdecker
 *
 * Class to keep track of the status of a component in the
 * database: cache the primary key for objects which are already in
 * the DB and have not changed, indicate those object which are either
 * not yet in the DB or have changed (and therefore need to have a new
 * copy saved.
 */
public class DatabaseEntry implements Serializable
{
    //
    // member data
    //
    
    /** database id (primary key) */
    private int databaseId = 0;

    //
    // member functions
    //
    
    /** retrieve the database id*/
    public int databaseId() { return databaseId; }

    /** indicate wether this object is not in the DB or has changed */
    public boolean hasChanged() { return databaseId==0; }

    /** set the database id */
    public void setDatabaseId(int databaseId)
    {
	this.databaseId = databaseId;
    }

    /** indicate that this object is not in DB or has changed */
    public void setHasChanged() { databaseId = 0; }
    
}
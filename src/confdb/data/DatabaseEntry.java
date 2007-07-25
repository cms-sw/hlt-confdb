package confdb.data;

/**
 * DatabaseEntry
 * -------------
 * @author Philipp Schieferdecker
 *
 */

public class DatabaseEntry
{
    //
    // member data
    //
    
    /** primary database key (0: not in database / has changed) */
    private int databaseId = 0;

    
    //
    // constructor
    //
    
    /** standard constructor */
    public DatabaseEntry() { this.databaseId = 0; }

    /** constructor with databaseId as argument*/
    public DatabaseEntry(int databaseId) { this.databaseId = databaseId; }

    
    //
    // member functions
    //
    
    /** retrieve database id */
    public int databaseId() { return this.databaseId; }

    /** is this object in this database / has it changed? */
    public boolean hasChanged() { return (this.databaseId==0); }
    
    /** set the database id */
    public void setDatabaseId(int databaseId) { this.databaseId = databaseId; }
    
    /** indicate that this object has changed */
    public void setHasChanged() { this.databaseId=0; }

}

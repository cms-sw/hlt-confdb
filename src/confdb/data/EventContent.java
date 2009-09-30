package confdb.data;

/**
 * EventContent
 * ------------
 * @author Philipp Schieferdecker
 *
 * Manage different CMSSW file formats, which Streams are based on.
 *
 */
public class EventContent extends    DatabaseEntry
                          implements Comparable<EventContent>
{
    //
    // member data
    //

    /** label of this event content */
    private String label;

    /** */

    /** collection of assigned streams */
    private ArrayList<Stream> streams = new ArrayList<Stream>();

    
    //
    // construction
    //



    //
    // member functions
    //
    

}

package confdb.data;


/**
 * EDSourceInstance
 * ----------------
 * @author Philipp Schieferdecker
 *
 * CMSSW event data source instance.
 */
public class EDSourceInstance extends Instance
{
    //
    // construction
    //
    
    /** standard constructor */
    public EDSourceInstance(String name,EDSourceTemplate template)
	throws DataException
    {
	super(name,template);
    }
    
}

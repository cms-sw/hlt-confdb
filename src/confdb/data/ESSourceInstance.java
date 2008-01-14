package confdb.data;


/**
 * ESSourceInstance
 * ----------------
 * @author Philipp Schieferdecker
 *
 * CMSSW event setup source instance.
 */
public class ESSourceInstance extends Instance implements ESPreferable
{
    //
    // member data
    //

    /** is data from this ESSource preferred? */
    private boolean isPreferred = false;


    //
    // construction
    //
    
    /** standard constructor */
    public ESSourceInstance(String name,ESSourceTemplate template)
	throws DataException
    {
	super(name,template);
    }
    
    /** construcor setting isPreferred */
    public ESSourceInstance(String name,ESSourceTemplate template,
			    boolean isPreferred)
	throws DataException
    {
	super(name,template);
	setPreferred(isPreferred);
    }


    //
    // member functions
    //

    /** check if this source is preferred */
    public boolean isPreferred() { return isPreferred; }
    
    /** set if this source is preferred */
    public void setPreferred(boolean isPreferred)
    {
	this.isPreferred=isPreferred;
	setHasChanged();
    }
    
}

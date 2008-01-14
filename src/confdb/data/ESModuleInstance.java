package confdb.data;


/**
 * ESModuleInstance
 * ----------------
 * @author Philipp Schieferdecker
 *
 * CMSSW event setup module instance.
 */
public class ESModuleInstance extends Instance implements ESPreferable
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
    public ESModuleInstance(String name,ESModuleTemplate template)
	throws DataException
    {
	super(name,template);
    }
        
    /** constructor setting isPreferred */
    public ESModuleInstance(String name,ESModuleTemplate template,
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

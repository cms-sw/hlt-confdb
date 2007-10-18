package confdb.data;


/**
 * ESModuleInstance
 * ----------------
 * @author Philipp Schieferdecker
 *
 * CMSSW event setup module instance.
 */
public class ESModuleInstance extends Instance implements Preferable
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
    {
	super(name,template);
    }
        
    /** constructor setting isPreferred */
    public ESModuleInstance(String name,ESModuleTemplate template,
			    boolean isPreferred)
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
    public void setPreferred(boolean isPreferred) { this.isPreferred=isPreferred; }

}

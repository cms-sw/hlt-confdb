package confdb.data;

import java.io.Serializable;


/**
 * Reference
 * ---------
 * @author Philipp Schieferdecker
 *
 * Reference base class.
 */
public class Reference implements Serializable
{
    //
    // member data
    //
    
    /** container: no reference outside a container! */
    private ReferenceContainer container = null;
    
    /** parent object */
    private Referencable parent = null;

    
    //
    // construction
    //
    
    /** standard constructor */
    public Reference(ReferenceContainer container, Referencable parent)
    {
	this.container = container;
	this.parent    = parent;
    }
    
    //
    // member functions
    //
    
    /** overload toString() */
    public String toString() { return name(); }
    
    /** remove the reference, both from the container and the parent */
    public void remove()
    {
	container.removeEntry(this);
	parent.removeReference(this);
    }
    
    /** the name of the reference */
    public String name() { return parent.name(); }
    
    /** get the container */
    public ReferenceContainer container() { return container; }
    
    /** get the parent */
    public Referencable parent() { return parent; }
    
}

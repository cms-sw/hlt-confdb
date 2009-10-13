package confdb.gui;

/**
 * ConfigurationTreeNode
 * ---------------------
 * @author Philipp Schieferdecker
 *
 * Simple structure containing the object and its parent within a
 * (Configuration) tree.
 */
public class ConfigurationTreeNode
{
    //
    // member data
    //
    
    /** the parent of the object */
    private Object parent = null;

    /** the object behind the node */
    private Object object = null;

    //
    // construction
    //

    /** standard constructor */
    public ConfigurationTreeNode(Object parent,Object object)
    {
	this.parent = parent;
	this.object = object;
    }

    
    //
    // member functions
    //

    /** Object: toString() */
    public String toString() { return object.toString(); }

    /** retrieve the node object */
    public Object object() { return object; }

    /** retrieve the parent */
    public Object parent() { return parent; }
    
}
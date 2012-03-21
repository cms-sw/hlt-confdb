package confdb.data;

import java.io.Serializable;
import java.util.ArrayList;


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

    /** flag to ignore / negate outcome in path/sequence containing this item */
    private Operator operator = Operator.DEFAULT;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    protected Reference(ReferenceContainer container, Referencable parent)
    {
	this.container = container;
	this.parent    = parent;
    }
    
    //
    // member functions
    //
    
    /** overload toString() */
    public String toString() { return name(); }

    
    /** get name + operator flags */
    public String getOperatorAndName()
    {
    	switch ( operator )
    	{
    		case NEGATE:
    			return "~" + name();
    		case IGNORE:
    			return "ignore( " + name() + " )";
    	}
    	return name();
    }
    
    /** get name + operator flags */
    public String getPythonCode( String object )
    {
    	switch ( operator )
    	{
    		case NEGATE:
    			return "~" + object + name();
    		case IGNORE:
    			return "cms.ignore(" + object + name() + ")";
    	}
    	return object + name();
    }
    
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

    /** get parent path */
    public Path[] parentPaths()
    {
	ArrayList<Path> list = new ArrayList<Path>();
	addParentPaths(container(),list);
	return list.toArray(new Path[list.size()]);
    }
    
    /** add parent paths of a reference container to list of paths */
    private void addParentPaths(ReferenceContainer rc,ArrayList<Path> list)
    {
	if      (rc==null)           return;
	else if (rc instanceof Path) list.add((Path)rc);
	else {
	    for (int i=0;i<rc.referenceCount();i++) {
		Path[] paths = rc.reference(i).parentPaths();
		for (Path p : paths) list.add(p);
	    }
	}
    }

	public Operator getOperator() {
		return operator;
	}

	public void setOperator( Operator newValue ) 
	{
		if ( newValue != operator )
			container.setHasChanged();
		operator = newValue;
	}
        
    
}

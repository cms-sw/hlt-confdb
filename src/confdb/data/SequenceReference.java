package confdb.data;

import java.util.ArrayList;


/**
 * SequenceReference
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Reference of a Sequence within another path.
*/
public class SequenceReference extends Reference
{
    //
    // construction
    //

    /** standard constructor */
    public SequenceReference(ReferenceContainer container,Sequence sequence)
    {
	super(container,sequence);
    }

}


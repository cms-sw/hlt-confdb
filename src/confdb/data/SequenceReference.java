package confdb.data;



/**
 * SequenceReference
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Reference of a Sequence within another sequence or path.
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


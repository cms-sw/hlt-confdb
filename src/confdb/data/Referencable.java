package confdb.data;


/**
 * Referencable
 * ------------
 * @author Philipp Schieferdecker
 *
 * A Common interace for anything that can be referenced: module
 * instance, path, sequence.
 */
public interface Referencable
{
    /** name of this */
    public String name();

    /** create a reference of this in a reference container (path/sequence) */
    public Reference createReference(ReferenceContainer container,int i);

    /** number of references */
    public int referenceCount();
    
    /** retrieve the i-th reference */
    public Reference reference(int i);

    /** test if a specifc reference refers to this entity */
    public boolean isReferencedBy(Reference reference);

    /** remove a reference of this */
    public void removeReference(Reference reference);

}

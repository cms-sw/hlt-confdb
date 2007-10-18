package confdb.data;


/**
 * Preferable
 * ----------
 * @author Philipp Schieferdecker
 *
 */
public interface Preferable
{
    /** query if this module is preferred */
    public boolean isPreferred();

    /** set if this module is preferred */
    public void setPreferred(boolean isPreferred);

}


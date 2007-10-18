package confdb.data;


/**
 * ESPreferable
 * ------------
 * @author Philipp Schieferdecker
 *
 */
public interface ESPreferable
{
    /** query if this module is preferred */
    public boolean isPreferred();

    /** set if this module is preferred */
    public void setPreferred(boolean isPreferred);

}


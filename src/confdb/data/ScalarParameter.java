package confdb.data;

/**
 * ScalarParameter
 * ---------------
 * @author Philipp Schieferdecker
 *
 * parameter base class for scalar-type parameters.
 */
abstract public class ScalarParameter extends Parameter
{
    //
    // construction
    //
    
    /** standard constructor */
    public ScalarParameter(String name,boolean isTracked,boolean isDefault)
    {
	super(name,isTracked,isDefault);
    }

    //
    // abstract member functions
    //
    
    /** retrieve the value of the parameter */
    abstract public Object value();
    
}

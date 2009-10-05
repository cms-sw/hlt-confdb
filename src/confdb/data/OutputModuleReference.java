package confdb.data;


/**
 * OutputModuleReference
 * ---------------------
 * @author Philipp Schieferdecker
 *
 * Reference to an OutputModule, as it appears in reference containers
 * like sequences and paths.
 */
public class OutputModuleReference extends Reference
{
    //
    // construction
    //
    
    /** standard constructor */
    public OutputModuleReference(ReferenceContainer container,
				 OutputModule outputModule)
    {
	super(container,outputModule);
    }
    
    
    //
    // member functions
    //

    /** Object: toString() */
    public String toString() {	return parent().toString(); }
    
    /** number of parameters */
    public int parameterCount()
    {
	OutputModule output = (OutputModule)parent();
	return output.parameterCount();
    }
    
    /** get i-th parameter */
    public Parameter parameter(int i)
    {
	OutputModule output = (OutputModule)parent();
	return output.parameter(i);
    }
    
    /** index of a certain parameter */
    public int indexOfParameter(Parameter p)
    {
	OutputModule output = (OutputModule)parent();
	return output.indexOfParameter(p);
    }


}
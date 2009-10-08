package confdb.data;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * GlobalPSetContainer
 * -------------------
 * @author Philipp Schieferdecker
 *
 * Special ParameterContainer for the global parameters sets (PSets)
 * of a configuration.
 */
public class GlobalPSetContainer extends ParameterContainer
{

    //
    // member functions
    //

    /** ParameterContainer: indicate wether a parameter is at its default */
    public boolean isParameterAtItsDefault(Parameter p) { return false; }

    /** ParameterContainer: indicate wether parameter is removable */
    public boolean isParameterRemovable(Parameter p) { return true; }
    
}

package confdb.data;

/**
 * ModuleReference
 * ---------------
 * @author Philipp Schieferdecker
 *
 * CMSSW framework module reference.
 */
public class ModuleReference extends Reference {
	//
	// construction
	//

	/** standard constructor */
	public ModuleReference(ReferenceContainer container, ModuleInstance module) {
		super(container, module);
	}

	//
	// member functions
	//

	/** Object: toString() */
	public String toString() {
		return parent().toString();
	}

	/** number of parameters */
	public int parameterCount() {
		ModuleInstance module = (ModuleInstance) parent();
		return module.parameterCount();
	}

	/** get i-th parameter */
	public Parameter parameter(int i) {
		ModuleInstance module = (ModuleInstance) parent();
		return module.parameter(i);
	}

	/** index of a certain parameter */
	public int indexOfParameter(Parameter p) {
		ModuleInstance module = (ModuleInstance) parent();
		return module.indexOfParameter(p);
	}

	/** number of unset tracked parameters */
	public int unsetTrackedParameterCount() {
		ModuleInstance module = (ModuleInstance) parent();
		return module.unsetTrackedParameterCount();
	}

}

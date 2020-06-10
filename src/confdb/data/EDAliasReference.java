package confdb.data;

/**
 * ModuleReference ---------------
 * 
 * @author Bogdan Sataric
 *
 *         CMSSW framework EDAlias reference.
 */
public class EDAliasReference extends Reference {
	//
	// construction
	//

	/** standard constructor */
	public EDAliasReference(ReferenceContainer container, EDAliasInstance edalias) {
		super(container, edalias);
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
		EDAliasInstance edalias = (EDAliasInstance) parent();
		return edalias.parameterCount();
	}

	/** get i-th parameter */
	public Parameter parameter(int i) {
		EDAliasInstance edalias = (EDAliasInstance) parent();
		return edalias.parameter(i);
	}

	/** index of a certain parameter */
	public int indexOfParameter(Parameter p) {
		EDAliasInstance edalias = (EDAliasInstance) parent();
		return edalias.indexOfParameter(p);
	}

	/** number of unset tracked parameters */
	public int unsetTrackedParameterCount() {
		EDAliasInstance edalias = (EDAliasInstance) parent();
		return edalias.unsetTrackedParameterCount();
	}

}

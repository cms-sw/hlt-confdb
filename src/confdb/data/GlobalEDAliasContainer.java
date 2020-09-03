package confdb.data;

/**
 * GlobalEDAliasContainer
 * -------------------
 * @author Bogdan Sataric
 *
 * Special ParameterContainer for the global EDAliases
 * of a configuration. (probably redundant)
 */
public class GlobalEDAliasContainer extends ParameterContainer {

	public boolean isParameterAtItsDefault(Parameter p) {
		return false;
	}

	public boolean isParameterRemovable(Parameter p) {
		return false;
	}

}

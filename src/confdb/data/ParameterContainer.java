package confdb.data;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * ParameterContainer
 * ------------------
 * @author Philipp Schieferdecker
 *
 * Base-class for all components which contain a list of parameters,
 * like instances, output modules, etc.
 */
abstract public class ParameterContainer extends DatabaseEntry {
	//
	// member data
	//

	/** collection of parameters */
	private ArrayList<Parameter> parameters = new ArrayList<Parameter>();

	//
	// abstract member functions
	//

	/** indicate weather parameter is at its default */
	abstract public boolean isParameterAtItsDefault(Parameter p);

	/** indicate weather a parameter can be removed or not */
	abstract public boolean isParameterRemovable(Parameter p);

	//
	// non-abstract member functions
	//

	/** add a parameter */
	public void addParameter(Parameter parameter) {
		parameters.add(parameter);
		parameter.setParent(this);
		setHasChanged();
	}

	/** remove a given parameter */
	public void removeParameter(Parameter parameter) {
		parameters.remove(parameter);
		parameter.setParent(null);
		setHasChanged();
	}

	/** remove all parameters */
	public void clear() {
		parameters.clear();
		setHasChanged();
	}

	/** number of parameters */
	public int parameterCount() {
		return parameters.size();
	}

	/** retrieve i-th parameter */
	public Parameter parameter(int i) {
		return parameters.get(i);
	}

	/** retrieve index of a given parameter */
	public int indexOfParameter(Parameter p) {
		return parameters.indexOf(p);
	}

	/** retrieve parameter iterator */
	public Iterator<Parameter> parameterIterator() {
		return parameters.iterator();
	}

	/** check weather this container contains a given parameter */
	public boolean containsParameter(Parameter p) {
		return parameters.contains(p);
	}

	/** remove untracked parameter */
	public boolean removeUntrackedParameter(Parameter p) {
		if (isParameterRemovable(p)) {
			parameters.remove(p);
			setHasChanged();
			return true;
		}
		return false;
	}

	/** get parameter by name */
	public Parameter parameter(String name) {
		Iterator<Parameter> itP = parameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			if (p.name().equals(name))
				return p;
		}
		System.err.println("ERROR: ParameterContainer nas no parameter '" + name + "'!");
		return null;
	}

	/** get parameter by name AND type */
	public Parameter parameter(String name, String type) {
		Iterator<Parameter> itP = parameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			if (p.name().equals(name) && p.type().equals(type))
				return p;
		}
		return null;
	}

	/** recursively retrieve parameters to all levels */
	public Iterator<Parameter> recursiveParameterIterator() {
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		Parameter.getParameters(parameterIterator(), params);
		return params.iterator();
	}

	/** find parameter (recursively) with specified name */
	public Parameter findParameter(String name) {
		Iterator<Parameter> itP = recursiveParameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			String fullParamName = p.fullName();
			if (fullParamName.equals(name))
				return p;
		}
		return null;
	}

	/** get all parameters (recursively) with specified name */
	public Parameter[] findParameters(String name) {
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		Iterator<Parameter> itP = recursiveParameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			String fullParamName = p.fullName();
			if (fullParamName.equals(name) || (!fullParamName.equals(name) && fullParamName.endsWith("::" + name)))
				params.add(p);
		}
		return params.toArray(new Parameter[params.size()]);
	}

	/** get all parameter (recursively) with specified strict name *and* type */
	public Parameter findParameter(String name, String type) {
		if (type == null)
			return findParameter(name);

		Iterator<Parameter> itP = recursiveParameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			String fullParamName = p.fullName();
			if ((fullParamName.equals(name)) && p.type().equals(type))
				return p;
		}
		return null;
	}

	/** get all parameters (recursively) with specified name *and* type */
	public Parameter[] findParameters(String name, String type) {
		if (type == null)
			return findParameters(name);
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		Iterator<Parameter> itP = recursiveParameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			String fullParamName = p.fullName();
			if ((fullParamName.equals(name) || (!fullParamName.equals(name) && fullParamName.endsWith("::" + name)))
					&& p.type().equals(type))
				params.add(p);
		}
		return params.toArray(new Parameter[params.size()]);
	}

	/** get all parameters (recursively) with specified name *and* type */
	public Parameter[] findParameters(String name, String type, String value) {
		if (value == null)
			return findParameters(name, type);
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		Iterator<Parameter> itP = recursiveParameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			String paramType = p.type();
			String fullParamName = p.fullName();
			String paramValue = p.valueAsString();

			boolean typeMatch = (type == null) ? true : paramType.equals(type);
			boolean nameMatch = (name == null) ? true
					: ((fullParamName.equals(name))
							|| (!fullParamName.equals(name) && fullParamName.endsWith("::" + name)));
			boolean valueMatch = (value == null) ? true : paramValue.equals(value);
			if (typeMatch && nameMatch && valueMatch)
				params.add(p);
		}
		return params.toArray(new Parameter[params.size()]);
	}

	/** get all parameters (recursively) with specified name *and* type */
	public boolean isMatch(String source, String search, int alg) {
		switch (alg) {
		case 1:
			return source.contains(search);
		case 2:
			return source.matches(search);
		case 3:
			return source.startsWith(search);
		default:
			return false;
		}
	}

	public Parameter[] findParameters(String name, String type, String value, int alg) {
		String One = new String("'");
		char[] two = { '"' };
		String Two = new String(two);

		ArrayList<Parameter> params = new ArrayList<Parameter>();
		Iterator<Parameter> itP = recursiveParameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			String paramType = p.type();
			String paramName = p.name();
			String paramValue = p.valueAsString();
			if ((p instanceof StringParameter) || (p instanceof VStringParameter))
				paramValue = paramValue.replace(One, "").replace(Two, "");

			boolean typeMatch = (type == null) ? true : isMatch(paramType, type, alg);

			boolean nameMatch = (name == null) ? true : isMatch(paramName, name, alg);

			boolean valueMatch = (value == null) ? true : isMatch(paramValue, value, alg);

			if (typeMatch && nameMatch && valueMatch)
				params.add(p);
		}
		return params.toArray(new Parameter[params.size()]);
	}

	/** update a parameter when the value is changed */
	public boolean updateParameter(int index, String valueAsString) {
		Parameter p = parameter(index);
		String oldValueAsString = p.valueAsString();
		if (valueAsString.equals(oldValueAsString))
			return true;
		boolean result = p.setValue(valueAsString);
		if (result)
			setHasChanged();
		return result;
	}

	/** update a parameter when the value is changed */
	public boolean updateParameter(String name, String type, String valueAsString) {
		Parameter p = findParameter(name, type);
		// handle existing parameter, top-level or not
		if (p != null) {
			String oldValueAsString = p.valueAsString();
			if (valueAsString.equals(oldValueAsString))
				return true;
			boolean result = p.setValue(valueAsString);
			if (result)
				setHasChanged();
			return result;
		}
		// add an untracked parameter to the top-level
		else {
			Parameter parameterNew = ParameterFactory.create(type, name, valueAsString, false);
			System.err
					.println("ParameterContainer INFO: " + "Adding untracked parameter to top-level: " + parameterNew);
			addParameter(parameterNew);
			setHasChanged();
			return true;
		}
	}
	
	/** update a tracked parameter when the value is changed */
	public boolean updateTrackedParameter(String name, String type, String valueAsString) {
		Parameter p = findParameter(name, type);
		// handle existing parameter, top-level or not
		if (p != null) {
			String oldValueAsString = p.valueAsString();
			if (valueAsString.equals(oldValueAsString))
				return true;
			boolean result = p.setValue(valueAsString);
			if (result)
				setHasChanged();
			return result;
		}
		// add an tracked parameter to the top-level
		else {
			Parameter parameterNew = ParameterFactory.create(type, name, valueAsString, true);
			System.err
					.println("ParameterContainer INFO: " + "Adding tracked parameter to top-level: " + parameterNew);
			addParameter(parameterNew);
			setHasChanged();
			return true;
		}
	}

	/** number of unset tracked parameters */
	public int unsetTrackedParameterCount() {
		int result = 0;
		Iterator<Parameter> itP = parameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			if (p instanceof VPSetParameter) {
				VPSetParameter vpset = (VPSetParameter) p;
				result += vpset.unsetTrackedParameterCount();
			} else if (p instanceof PSetParameter) {
				PSetParameter pset = (PSetParameter) p;
				result += pset.unsetTrackedParameterCount();
			} else {
				if (p.isTracked() && !p.isValueSet())
					result++;
			}
		}
		return result;
	}

	/** nuymber of unresolved ESInputTags */
	public int unresolvedESInputTagCount(IConfiguration config) {
		int result = 0;
		Iterator<Parameter> itP = parameterIterator();
		while (itP.hasNext()) {
			Parameter p = itP.next();
			if (p instanceof VPSetParameter) {
				VPSetParameter vpset = (VPSetParameter) p;
				result += vpset.unresolvedESInputTagCount(config);
			} else if (p instanceof PSetParameter) {
				PSetParameter pset = (PSetParameter) p;
				result += pset.unresolvedESInputTagCount(config);
			} else if (p instanceof ESInputTagParameter) {
				ESInputTagParameter par = (ESInputTagParameter) p;
				result += par.unresolvedESInputTagCount(config);
			} else if (p instanceof VESInputTagParameter) {
				VESInputTagParameter par = (VESInputTagParameter) p;
				result += par.unresolvedESInputTagCount(config);
			}
		}
		return result;
	}
}

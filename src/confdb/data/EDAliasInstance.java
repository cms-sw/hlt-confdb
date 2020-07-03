package confdb.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * EDAliasInstance --------------
 * 
 * @author Bogdan Sataric
 *
 *         CMSSW framework EDAlias instance.
 */
public class EDAliasInstance extends Instance implements Referencable {
	//
	// data members
	//

	/** database ID */
	private int databaseId = 0;

	/** list of references */
	private ArrayList<EDAliasReference> references = new ArrayList<EDAliasReference>();

	//
	// construction
	//

	/** standard constructor - EDAlias has no template */
	public EDAliasInstance(String name) throws DataException {
		super(name, null);
	}

	//
	// member functions
	//

	/** create a reference of this instance */
	public Reference createReference(ReferenceContainer container, int i) {
		EDAliasReference reference = new EDAliasReference(container, this);
		references.add(reference);
		System.out.println("EDALIAS REFERENCE CREATED " + reference.toString());
		container.insertEntry(i, reference);
		System.out.println("CONTAINER EDALIAS REFERENCE INSERTED " + container.toString());
		return reference;
	}

	/** number of references */
	public int referenceCount() {
		return references.size();
	}

	/** retrieve the i-th reference */
	public Reference reference(int i) {
		return references.get(i);
	}

	/** test if a specific reference refers to this entity */
	public boolean isReferencedBy(Reference reference) {
		return references.contains(reference);
	}

	/** remove a reference of this instance */
	public void removeReference(Reference reference) {
		int index = references.indexOf(reference);
		references.remove(index);
	}

	/** get list of parent paths */
	public Path[] parentPaths() {
		ArrayList<Path> list = new ArrayList<Path>();
		for (int i = 0; i < referenceCount(); i++) {
			Path[] paths = reference(i).parentPaths();
			for (Path p : paths)
				list.add(p);
		}
		return list.toArray(new Path[list.size()]);
	}

	/** set the name and propagate it to all relevant downstreams InputTags */
	public void setNameAndPropagate(String name) throws DataException {
		String oldName = name();
		if (oldName.equals(name))
			return;
		super.setName(name);

		// need to check all paths containing this EDAlias
		Path[] paths = parentPaths();
		HashSet<Path> pathSet = new HashSet<Path>();
		for (Path path : paths)
			pathSet.add(path);

		// as well as endpaths for outputmodules/eventcontents
		if (config() != null) {
			Iterator<Path> itP = config().pathIterator();
			while (itP.hasNext()) {
				Path path = itP.next();
				if (path.isEndPath()) {
					pathSet.add(path);
				}
			}
		}

		Iterator<Path> itPath = pathSet.iterator();
		while (itPath.hasNext()) {
			Path path = itPath.next();

			// outputmodules/eventcontent
			Iterator<OutputModule> itO = path.outputIterator();
			while (itO.hasNext()) {
				OutputModule output = itO.next();
				Stream stream = output.parentStream();
				EventContent content = stream.parentContent();
				Iterator<OutputCommand> itC = content.commandIterator();
				while (itC.hasNext()) {
					OutputCommand command = itC.next();
					if (command.moduleName().equals(oldName)) {
						command.setModuleName(name());
					}
				}
			}

			boolean isDownstream = path.isEndPath();
			Iterator<EDAliasInstance> itEDA = path.edAliasIterator();
			while (itEDA.hasNext()) {
				EDAliasInstance edAlias = itEDA.next();
				if (!isDownstream) {
					if (edAlias == this)
						isDownstream = true;
					continue;
				}
				Iterator<Parameter> itP = edAlias.recursiveParameterIterator();
				while (itP.hasNext()) {
					Parameter p = itP.next();
					if (!p.isValueSet())
						continue;
					if (p instanceof InputTagParameter) {
						InputTagParameter inputTag = (InputTagParameter) p;
						if (inputTag.label().equals(oldName)) {
							InputTagParameter tmp = (InputTagParameter) inputTag.clone(null);
							tmp.setLabel(name());
							edAlias.updateParameter(inputTag.fullName(), inputTag.type(), tmp.valueAsString());
						}
					} else if (p instanceof ESInputTagParameter) {
						ESInputTagParameter esinputTag = (ESInputTagParameter) p;
						//TODO: BSATARIC what is this?
						/*
						 * if (esinputTag.edAlias().equals(oldName)) { ESInputTagParameter tmp =
						 * (ESInputTagParameter) esinputTag.clone(null); tmp.setModule(name());
						 * edAlias.updateParameter(esinputTag.fullName(), esinputTag.type(),
						 * tmp.valueAsString()); }
						 */
					} else if (p instanceof VInputTagParameter) {
						VInputTagParameter vInputTag = (VInputTagParameter) p;
						VInputTagParameter tmp = (VInputTagParameter) vInputTag.clone(null);
						for (int i = 0; i < tmp.vectorSize(); i++) {
							InputTagParameter inputTag = new InputTagParameter("", tmp.value(i).toString(), false);
							if (inputTag.label().equals(oldName)) {
								inputTag.setLabel(name());
								tmp.setValue(i, inputTag.valueAsString());
							}
						}
						edAlias.updateParameter(vInputTag.fullName(), vInputTag.type(), tmp.valueAsString());
					} else if (p instanceof VESInputTagParameter) {
						VESInputTagParameter vESInputTag = (VESInputTagParameter) p;
						VESInputTagParameter tmp = (VESInputTagParameter) vESInputTag.clone(null);
						//TODO: EDAlias
						/*
						 * for (int i = 0; i < tmp.vectorSize(); i++) { ESInputTagParameter ESinputTag =
						 * new ESInputTagParameter("", tmp.value(i).toString(), false); if
						 * (ESinputTag.edAlias().equals(oldName)) { ESinputTag.setModule(name());
						 * tmp.setValue(i, ESinputTag.valueAsString()); } }
						 */
						edAlias.updateParameter(vESInputTag.fullName(), vESInputTag.type(), tmp.valueAsString());
					}

				}
			}
		}
	}

	// TODO!
	public boolean squeeze() {

		boolean result = false;

		/*
		 * if (template().toString().equals("TriggerResultsFilter")) { VStringParameter
		 * parameterTriggerConditions = (VStringParameter)
		 * parameter("triggerConditions", "vstring"); boolean check = true; while
		 * (check) { check = false; for (int i = 0; i <
		 * parameterTriggerConditions.vectorSize(); i++) { if
		 * (parameterTriggerConditions.value(i).equals("")) {
		 * parameterTriggerConditions.removeValue(i); result = true; check = true;
		 * break; } } } }
		 */
		return result;

	}

}

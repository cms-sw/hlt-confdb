package confdb.data;

import java.util.Iterator;
import java.util.HashSet;
/**
 * SwitchProducer
 * --------
 * @author Bogdan Sataric
 * 
 * A 'SwitchProducer' can host only 1 or 2 elements of type EDProducer
 * or EDAlias
 */
public class SwitchProducer extends ReferenceContainer {
	//
	// construction
	//

	/** standard constructor */
	public SwitchProducer(String name) {
		super(name);
	}

	//
	// member functions
	//

	/** insert a EDProducer or EDAlias into the SwitchProducer 
	 ** it is assumed that the caller ensures that the module
	 ** is not already in another SP/Task/Sequence/Task */
	public void insertEntry(int i, Reference reference) {
		if (reference instanceof ModuleReference || reference instanceof EDAliasReference) {
			if (!entries.contains(reference)) {
				if (reference instanceof ModuleReference) {
					ModuleReference module = (ModuleReference) reference;
					module.setModuleType(1);										
				}

				if (!reference.name().startsWith(modulePrefix())) {				
					try {
						reference.parent().setName(getDefaultModuleName());
					} catch (DataException e) {
						System.err.println(e.getMessage());
					}
				}				
				
				entries.add(i, reference);
				
				setHasChanged();
				return;

			}
		}
		System.err.println("SwitchProducer.insertEntry FAILED.");
	}

	/** check if SwitchProducer contains a specific EDProducer or EDAlias */
	public boolean containsEntry(Reference reference) {
		Referencable parent = reference.parent();
		Iterator<Reference> it = entries.iterator();
		while (it.hasNext()) {
			Reference r = it.next();
			if (parent.isReferencedBy(r))
				return true;
		}
		return false;
	}

	/** create a reference of this in a reference container (path/sequence/task) */
	public Reference createReference(ReferenceContainer container, int i) {
		SwitchProducerReference reference = new SwitchProducerReference(container, this);
		references.add(reference);
		container.insertEntry(i, reference);
		container.setHasChanged();
		return reference;
	}

	/** set the name and propagate it to all relevant modules */
	public void setNameAndPropagate(String name) throws DataException {
		String oldName = name();
		String oldModulePrefix = modulePrefix();
		if (oldName.equals(name)) {
			return;
		}
		super.setName(name);
		for (Reference ref : entries) {
			Referencable entry = ref.parent();
			String newName = entry.name().replace(oldModulePrefix,"");
			newName = modulePrefix() + newName ;
			entry.setName(newName);
		}	
		
		// need to check all paths containing this module
		Path[] paths = parentPaths();
		HashSet<Path> pathSet = new HashSet<Path>();
		for (Path path : paths)
			pathSet.add(path);

		// as well as endpaths/finalpaths for outputmodules/eventcontents		
		if (config() != null) {
			Iterator<Path> itP = config().pathIterator();
			while (itP.hasNext()) {
				Path path = itP.next();
				if (path.isEndPath() || path.isFinalPath()) {
					pathSet.add(path);
				}
			}
		}
		updateInputTagsAndEvtContent(pathSet,oldName);

		
	}

	//the name seperater charactor string
	public static String nameSeperator() {
		return ".";
	}

	public String modulePrefix() {
		return name()+nameSeperator();
	}

	private String getDefaultModuleName() {
		if (entries.isEmpty()){
			return modulePrefix()+"cpu";
		}else{
			if ( entries.get(0).name().endsWith("cuda")) {
				return modulePrefix()+"cpu";
			}else{
				return modulePrefix()+"cuda";
			}
		}
	}

	/** copy paste of ModuleInstance.setNameAndPropagate with minor changes (sigh) */
	public void updateInputTagsAndEvtContent(HashSet<Path> pathSet,String oldName){
	
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

			Iterator<ModuleInstance> itM = path.moduleIterator();
			while (itM.hasNext()) {
				ModuleInstance module = itM.next();
				Iterator<Parameter> itP = module.recursiveParameterIterator();
				while (itP.hasNext()) {
					Parameter p = itP.next();
					if (!p.isValueSet())
						continue;
					if (p instanceof InputTagParameter) {
						InputTagParameter inputTag = (InputTagParameter) p;
						if (inputTag.label().equals(oldName)) {
							InputTagParameter tmp = (InputTagParameter) inputTag.clone(null);
							tmp.setLabel(name()
);
							module.updateParameter(inputTag.fullName(), inputTag.type(), tmp.valueAsString());
						}
					} else if (p instanceof ESInputTagParameter) {
						ESInputTagParameter esinputTag = (ESInputTagParameter) p;
						if (esinputTag.module().equals(oldName)) {
							ESInputTagParameter tmp = (ESInputTagParameter) esinputTag.clone(null);
							tmp.setModule(name());
							module.updateParameter(esinputTag.fullName(), esinputTag.type(), tmp.valueAsString());
						}
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
						module.updateParameter(vInputTag.fullName(), vInputTag.type(), tmp.valueAsString());
					} else if (p instanceof VESInputTagParameter) {
						VESInputTagParameter vESInputTag = (VESInputTagParameter) p;
						VESInputTagParameter tmp = (VESInputTagParameter) vESInputTag.clone(null);
						for (int i = 0; i < tmp.vectorSize(); i++) {
							ESInputTagParameter ESinputTag = new ESInputTagParameter("", tmp.value(i).toString(),
									false);
							if (ESinputTag.module().equals(oldName)) {
								ESinputTag.setModule(name());
								tmp.setValue(i, ESinputTag.valueAsString());
							}
						}
						module.updateParameter(vESInputTag.fullName(), vESInputTag.type(), tmp.valueAsString());
					}
				}
			}
		}
	}
}

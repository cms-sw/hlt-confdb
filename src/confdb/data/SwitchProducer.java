package confdb.data;

import java.util.Iterator;

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
	}

	//the name seperater charactor string
	public static String nameSeperator() {
		return "_";
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
}

package confdb.data;

import java.util.Iterator;

/**
 * SwitchProducer
 * --------
 * @author Bogdan Sataric
 * 
 * A 'SwitchProducer' can host only 1 or 2 elements of type EDProducer
 * or EDAlias
 * 
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

	/** insert a EDProducer or EDAlias into the SwitchProducer */
	public void insertEntry(int i, Reference reference) {
		//System.out.println("INSERTING SWITCHPRODUCER ENTRY");
		if (reference instanceof ModuleReference || reference instanceof EDAliasReference) {
			if (!entries.contains(reference)) {
				//System.out.println("INSERTING ENTRY");
				//System.out.println("REFERENCE CLASS " + reference.getClass().toString());
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

}

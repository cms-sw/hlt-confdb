package confdb.data;

import java.util.Iterator;

/**
 * Task --------
 * 
 * @author bsataric
 * 
 *         A 'Task' can host any number of ModuleReferences, TaskReferences and SwitchProducers,
 *         but no references to other Paths.
 */
public class Task extends ReferenceContainer {
	//
	// construction
	//

	/** standard constructor */
	public Task(String name) {
		super(name);
	}

	//
	// member functions
	//

	/** insert a module or task into the Task */
	public void insertEntry(int i, Reference reference) {
		if (reference instanceof ModuleReference || reference instanceof TaskReference 
				|| reference instanceof SwitchProducerReference 
				|| reference instanceof OutputModuleReference) {
			if (!entries.contains(reference)) {
				entries.add(i, reference);
				setHasChanged();
				return;
			}
		}
		System.err.println("Task.insertEntry FAILED.");
	}

	/** check if Task contains a specific module */
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

	/** create a reference of this in a reference container (path/Sequence/Task) */
	public Reference createReference(ReferenceContainer container, int i) {
		TaskReference reference = new TaskReference(container, this);
		references.add(reference);
		container.insertEntry(i, reference);
		container.setHasChanged();
		return reference;
	}

}

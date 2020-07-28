package confdb.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * EDAliasInstance
 * --------------
 * @author Bogdan Sataric
 *
 * CMSSW framework EDAlias instance.
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
		container.insertEntry(i, reference);
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
}

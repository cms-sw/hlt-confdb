package confdb.data;

/**
 * TaskReference -----------------
 * 
 * @author Bogdan Sataric
 *
 *         Reference of a Task within another Task or path.
 */
public class TaskReference extends Reference {
	//
	// construction
	//

	/** standard constructor */
	public TaskReference(ReferenceContainer container, Task Task) {
		super(container, Task);
	}

}

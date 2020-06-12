package confdb.data;

/**
 * switchProducerReference
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Reference of a SwitchProducer within path/sequence or the task.
 */
public class SwitchProducerReference extends Reference {
	//
	// construction
	//

	/** standard constructor */
	public SwitchProducerReference(ReferenceContainer container, SwitchProducer switchProducer) {
		super(container, switchProducer);
	}

}

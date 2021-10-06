package confdb.data;

/**
 * SwitchProducerReference
 * -----------------
 * @author Bogdan Sataric
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

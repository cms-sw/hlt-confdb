package confdb.converter;

import confdb.data.SwitchProducer;

public interface ISwitchProducerWriter {
	
	public String toString(SwitchProducer switchProducer, ConverterEngine converterEngine, String indent);

}

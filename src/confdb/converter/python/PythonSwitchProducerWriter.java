package confdb.converter.python;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ISwitchProducerWriter;
import confdb.data.Reference;
import confdb.data.SwitchProducer;

public class PythonSwitchProducerWriter implements ISwitchProducerWriter {
	public String toString(SwitchProducer switchProducer, ConverterEngine converterEngine, String object) {
		String str = object + switchProducer.name() + " = cms.SwitchProducer( ";
		if (switchProducer.entryCount() > 0) {
			String sep = " + ";
			Iterator<Reference> list = switchProducer.entryIterator();
			str += list.next().getPythonCode(object);
			while (list.hasNext())
				str += sep + list.next().getPythonCode(object);
		}
		str += " )\n";
		return str;
	}
}

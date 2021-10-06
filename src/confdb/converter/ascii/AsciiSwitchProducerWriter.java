package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ISwitchProducerWriter;
import confdb.data.SwitchProducer;

public class AsciiSwitchProducerWriter implements ISwitchProducerWriter {

	public String toString(SwitchProducer switchProducer, ConverterEngine converterEngine, String indent) {
		String str = indent + "switch producer " + decorateName(switchProducer.name()) + " = { ";
		for (int i = 0; i < switchProducer.entryCount(); i++) {
			str += decorate(switchProducer.entry(i).name());
			if (i + 1 < switchProducer.entryCount())
				str += " & ";
		}
		str += " }" + converterEngine.getNewline();
		return str;
	}
	
	protected String decorateName(String name) {
		return name;
	}

	protected String decorate(String name) {
		return name;
	}

}

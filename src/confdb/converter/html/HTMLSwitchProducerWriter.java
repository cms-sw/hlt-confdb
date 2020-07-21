package confdb.converter.html;

import confdb.converter.ConverterEngine;
import confdb.converter.ISwitchProducerWriter;
import confdb.data.SwitchProducer;

public class HTMLSwitchProducerWriter implements ISwitchProducerWriter {
	public String toString(SwitchProducer switchProducer, ConverterEngine converterEngine, String indent) {
		String str = indent + "switchProducer " + decorateName(switchProducer.name()) + " = ";
		for (int i = 0; i < switchProducer.entryCount(); i++) {
			str += decorate(switchProducer.entry(i).name());
			if (i + 1 < switchProducer.entryCount())
				str += " + ";
		}
		str += converterEngine.getNewline();
		return str;
	}

	protected String decorate(String entryName) {
		return "<a href=\"#" + entryName + "\">" + entryName + "</a>";
	}

	protected String decorateName(String switchProducerName) {
		return "<a name=\"" + switchProducerName + "\"><b>" + switchProducerName + "</b></a>";
	}
}

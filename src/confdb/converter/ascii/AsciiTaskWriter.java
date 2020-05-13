package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ITaskWriter;
import confdb.data.Task;

public class AsciiTaskWriter implements ITaskWriter {

	public String toString(Task task, ConverterEngine converterEngine, String indent) {
		String str = indent + "task " + decorateName(task.name()) + " = { ";
		for (int i = 0; i < task.entryCount(); i++) {
			str += decorate(task.entry(i).name());
			if (i + 1 < task.entryCount())
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

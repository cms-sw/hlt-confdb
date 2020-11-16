package confdb.converter.python;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ITaskWriter;
import confdb.data.Reference;
import confdb.data.Task;

public class PythonTaskWriter implements ITaskWriter {
	public String toString(Task task, ConverterEngine converterEngine, String object) {
		String str = object + task.name() + " = cms.Task( ";
		if (task.entryCount() > 0) {
			String sep = " , ";
			Iterator<Reference> list = task.entryIterator();
			str += list.next().getPythonCode(object);
			while (list.hasNext())
				str += sep + list.next().getPythonCode(object);
		}
		str += " )\n";
		return str;
	}

}

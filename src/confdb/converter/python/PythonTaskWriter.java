package confdb.converter.python;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ITaskWriter;
import confdb.data.Reference;
import confdb.data.Task;
import confdb.data.ReleaseVersionInfo;

public class PythonTaskWriter implements ITaskWriter {
	public String toString(Task task, ConverterEngine converterEngine, String object) {
		
		ReleaseVersionInfo relInfo = new ReleaseVersionInfo(task.config().releaseTag());
		String taskType = null;
		if(relInfo.geq(12,4,0,4)){
			taskType = new String("cms.ConditionalTask");
		}else{
			taskType = new String("cms.Task");
		}

		String str = object + task.name() + " = "+taskType+"( ";
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

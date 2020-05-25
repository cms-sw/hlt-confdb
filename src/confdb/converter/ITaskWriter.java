package confdb.converter;

import confdb.data.Task;

public interface ITaskWriter {

	public String toString(Task task, ConverterEngine converterEngine, String indent);

}

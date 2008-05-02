package confdb.converter.summary.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.IPathWriter;
import confdb.data.Path;

public class AsciiPathWriter implements IPathWriter 
{
    public String toString(Path path, ConverterEngine converterEngine, String indent) 
    {
	StringBuffer result = new StringBuffer();
	result.append(path.name()).append("\n");
	return result.toString();
    }
}

package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.IESModuleWriter;
import confdb.data.ESModuleInstance;

public class PythonESModuleWriter extends PythonInstanceWriter implements IESModuleWriter 
{
    public String toString(ESModuleInstance esmodule,ConverterEngine converterEngine, String indent) 
    {
	return toString("es_module",esmodule,converterEngine, indent);
    }
    
}

package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IESModuleWriter;
import confdb.data.ESModuleInstance;

public class AsciiESModuleWriter extends AsciiInstanceWriter implements IESModuleWriter 
{
    public String toString(ESModuleInstance esmodule,ConverterEngine converterEngine, String indent) throws ConverterException 
    {
	return toString("es_module",esmodule,converterEngine, indent);
    }
    
}

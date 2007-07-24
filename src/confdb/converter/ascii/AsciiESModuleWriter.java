package confdb.converter.ascii;

import confdb.converter.Converter;
import confdb.converter.IESModuleWriter;
import confdb.data.ESModuleInstance;

public class AsciiESModuleWriter extends AsciiInstanceWriter implements IESModuleWriter 
{
    public String toString(ESModuleInstance esmodule,Converter converter) 
    {
	return toString("es_module",esmodule,converter);
    }
    
}

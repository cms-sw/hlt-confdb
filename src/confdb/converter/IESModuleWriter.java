package confdb.converter;

import confdb.data.ESModuleInstance;

public interface IESModuleWriter
{
    public String toString(ESModuleInstance esmodule,Converter converter);
}

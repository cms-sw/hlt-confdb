package confdb.converter;

import confdb.data.ModuleInstance;

public interface IModuleWriter extends ConverterEngineSetter {
	
	public String toString( ModuleInstance module ) throws ConverterException;
        default public String toString( ModuleInstance module, String indent ) throws ConverterException{
	    return toString(module);
	}
}

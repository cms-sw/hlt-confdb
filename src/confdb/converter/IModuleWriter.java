package confdb.converter;

import confdb.data.ModuleInstance;

public interface IModuleWriter extends ConverterSetter {
	
	public String toString( ModuleInstance module );

}

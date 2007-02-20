package confdb.converter;

import confdb.data.ModuleInstance;

public interface IModuleWriter {
	
	public String toString( ModuleInstance module, Converter converter );

}

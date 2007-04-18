package confdb.converter;

import confdb.data.ModuleInstance;

public class ModuleWriterV1  extends InstanceWriter implements IModuleWriter {
	
	protected Converter converter = null;

	public String toString( ModuleInstance module ) 
	{
		return toString( "module", module, converter );
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

}

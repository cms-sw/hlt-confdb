package confdb.converter;

import confdb.data.EDAliasInstance;

public interface IEDAliasWriter extends ConverterEngineSetter {

	public String toString(EDAliasInstance edAlias) throws ConverterException;

}

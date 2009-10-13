package confdb.converter;

import confdb.data.OutputModule;

public interface IOutputWriter extends ConverterEngineSetter
{
    public String toString( OutputModule output ) throws ConverterException;
}

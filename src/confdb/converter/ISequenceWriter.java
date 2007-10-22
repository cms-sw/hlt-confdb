package confdb.converter;

import confdb.data.Sequence;

public interface ISequenceWriter {
	
	public String toString( Sequence sequence, ConverterEngine converterEngine );

}

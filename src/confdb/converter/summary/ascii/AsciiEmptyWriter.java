package confdb.converter.summary.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.IServiceWriter;
import confdb.data.EDSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;
import confdb.data.Sequence;
import confdb.data.ServiceInstance;

public class AsciiEmptyWriter implements ISequenceWriter, IServiceWriter, IEDSourceWriter, IESSourceWriter, IESModuleWriter, IModuleWriter, IParameterWriter
{
	public String toString(Sequence sequence, ConverterEngine converterEngine,
			String indent) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(ServiceInstance service,
			ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(EDSourceInstance edsource,
			ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(ESSourceInstance essource,
			ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(ESModuleInstance esmodule,
			ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(ModuleInstance module) throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		// TODO Auto-generated method stub
		
	}

	public String toString(Parameter parameter,
			ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

}

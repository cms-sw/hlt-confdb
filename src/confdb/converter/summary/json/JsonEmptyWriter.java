package confdb.converter.summary.json;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IEDAliasWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.ITaskWriter;
import confdb.converter.ISwitchProducerWriter;
import confdb.converter.IServiceWriter;
import confdb.data.EDSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ModuleInstance;
import confdb.data.EDAliasInstance;
import confdb.data.Parameter;
import confdb.data.Sequence;
import confdb.data.ServiceInstance;
import confdb.data.Task;
import confdb.data.SwitchProducer;

public class JsonEmptyWriter implements ISequenceWriter, ITaskWriter, ISwitchProducerWriter, IServiceWriter, IEDSourceWriter, IESSourceWriter,
		IESModuleWriter, IModuleWriter, IEDAliasWriter, IParameterWriter {
	public String toString(Sequence sequence, ConverterEngine converterEngine, String indent) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(Task task, ConverterEngine converterEngine, String indent) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public String toString(SwitchProducer switchProducer, ConverterEngine converterEngine, String indent) {
		// TODO Auto-generated method stub
		return null;
	}


	public String toString(ServiceInstance service, ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(EDSourceInstance edsource, ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(ESSourceInstance essource, ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(ESModuleInstance esmodule, ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(ModuleInstance module) throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}
	

	public String toString(EDAliasInstance edAlias) throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		// TODO Auto-generated method stub

	}

	public String toString(Parameter parameter, ConverterEngine converterEngine, String indent)
			throws ConverterException {
		// TODO Auto-generated method stub
		return null;
	}
}

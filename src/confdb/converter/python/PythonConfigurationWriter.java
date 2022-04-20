package confdb.converter.python;

import java.util.Iterator;

import org.omg.CORBA.DynAnyPackage.InvalidValue;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IEDAliasWriter;
import confdb.converter.IOutputWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.ITaskWriter;
import confdb.converter.ISwitchProducerWriter;
import confdb.converter.IServiceWriter;
import confdb.data.Block;
import confdb.data.ESPreferable;
import confdb.data.IConfiguration;
import confdb.data.EDSourceInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ModuleInstance;
import confdb.data.EDAliasInstance;
import confdb.data.OutputModule;
import confdb.data.Parameter;
import confdb.data.Path;
import confdb.data.ReleaseVersionInfo;
import confdb.data.Sequence;
import confdb.data.Task;
import confdb.data.SwitchProducer;
import confdb.data.ServiceInstance;

public class PythonConfigurationWriter implements IConfigurationWriter {
	protected ConverterEngine converterEngine = null;

	/**
	 * converts the configuration to python
	 * note there are some thing not stored by the config which are added by this function
	 * eg process.HLTConfigVersion, process.schedule, process.ProcessAcceleratorCUDA
	 * these should all be listed in Configuration.reservedNames to ensure we dont accidently include
	 * them in the config
	 */
	public String toString(IConfiguration conf, WriteProcess writeProcess) throws ConverterException {
		String indent = "  ";
		StringBuffer str = new StringBuffer(100000);
		// String fullName = conf.parentDir().name() + "/" + conf.name() + "/V" +
		// conf.version() ;
		String fullName = conf.toString();
		str.append("# " + fullName + " (" + conf.releaseTag() + ")" + converterEngine.getNewline()
				+ converterEngine.getNewline());

		str.append("import FWCore.ParameterSet.Config as cms\n\n");
		if(conf.switchProducerCount() > 0) {
			ReleaseVersionInfo relVarInfo = new ReleaseVersionInfo(conf.releaseTag());
		    str.append("from HeterogeneousCore.CUDACore.SwitchProducerCUDA import SwitchProducerCUDA\n\n");
			if(relVarInfo.cycle()>=12 && relVarInfo.major()>=3){
				str.append("from HeterogeneousCore.CUDACore.ProcessAcceleratorCUDA import ProcessAcceleratorCUDA\nprocess.ProcessAcceleratorCUDA = ProcessAcceleratorCUDA()\n\n");
			}
		}


		String object = "";
		if (writeProcess == WriteProcess.YES) {
			object = "process.";
			str.append("process = cms.Process( \"" + conf.processName() + "\" )\n");
		} else
			indent = "";

		str.append("\n" + object + "HLTConfigVersion = cms.PSet(\n  tableName = cms.string('" + fullName + "')\n)\n\n");

		if (conf.psetCount() > 0) {
			IParameterWriter parameterWriter = converterEngine.getParameterWriter();
			for (int i = 0; i < conf.psetCount(); i++) {
				Parameter pset = conf.pset(i);
				String psetStr = parameterWriter.toString(pset, converterEngine, "");
				if (psetStr != null && psetStr.length() > 0)
					str.append(object + psetStr);
			}
			str.append("\n");
		}
		
		if (conf.globalEDAliasCount() > 0) {
			IEDAliasWriter globalEDAliasWriter = converterEngine.getGlobalEDAliasWriter();
			for (int i = 0; i < conf.globalEDAliasCount(); i++) {
				EDAliasInstance globalEDAlias = conf.globalEDAlias(i);
				str.append(object);
				str.append(globalEDAliasWriter.toString(globalEDAlias));
			}
			str.append("\n");
		}

		if (conf.edsourceCount() > 0) {
			IEDSourceWriter edsourceWriter = converterEngine.getEDSourceWriter();
			for (int i = 0; i < conf.edsourceCount(); i++) {
				EDSourceInstance edsource = conf.edsource(i);
				str.append(object);
				str.append(edsourceWriter.toString(edsource, converterEngine, indent));
			}
			str.append("\n");
		}

		if (conf.essourceCount() > 0) {
			IESSourceWriter essourceWriter = converterEngine.getESSourceWriter();
			for (int i = 0; i < conf.essourceCount(); i++) {
				ESSourceInstance essource = conf.essource(i);
				str.append(object);
				str.append(essourceWriter.toString(essource, converterEngine, indent));
			}

			for (int i = 0; i < conf.essourceCount(); i++) {
				ESSourceInstance instance = conf.essource(i);
				if (instance instanceof ESPreferable) {
					ESPreferable esp = (ESPreferable) instance;
					if (esp.isPreferred()) {
						str.append(object);
						str.append("es_prefer_" + instance.name() + " = cms.ESPrefer( \"" + instance.template().name()
								+ "\", \"" + instance.name() + "\" )\n");
					}
				}
			}
			str.append("\n");
		}

		if (conf.esmoduleCount() > 0) {
			IESModuleWriter esmoduleWriter = converterEngine.getESModuleWriter();
			for (int i = 0; i < conf.esmoduleCount(); i++) {
				ESModuleInstance esmodule = conf.esmodule(i);
				str.append(object);
				str.append(esmoduleWriter.toString(esmodule, converterEngine, ""));
				if (esmodule.isPreferred()) {
					str.append(object);
					str.append("es_prefer_" + esmodule.name() + " = cms.ESPrefer( \"" + esmodule.template().name()
							+ "\", \"" + esmodule.name() + "\" )\n");
				}
			}

			str.append("\n");
		}

		if (conf.serviceCount() > 0) {
			IServiceWriter serviceWriter = converterEngine.getServiceWriter();
			for (int i = 0; i < conf.serviceCount(); i++) {
				ServiceInstance service = conf.service(i);
				str.append(object);
				str.append(serviceWriter.toString(service, converterEngine, indent));
			}
			str.append("\n");
		}

		if (conf.moduleCount() > 0) {
			IModuleWriter moduleWriter = converterEngine.getModuleWriter();
			for (int i = 0; i < conf.moduleCount(); i++) {
				ModuleInstance module = conf.module(i);
				//do not write out switch producer modules
				if (module.moduleType()!=1) {
					str.append(object);
					str.append(moduleWriter.toString(module));
				}
			}
			str.append("\n");
		}
		
		if (conf.switchProducerCount() > 0) {
			ISwitchProducerWriter switchProducerWriter = converterEngine.getSwitchProducerWriter();
			Iterator<SwitchProducer> switchProducerIterator = conf.orderedSwitchProducerIterator();
			while (switchProducerIterator.hasNext()) {
				SwitchProducer switchProducer = switchProducerIterator.next();
				str.append(switchProducerWriter.toString(switchProducer, converterEngine, object));
			}
			str.append("\n");
		}

		if (conf.outputCount() > 0) {
			IOutputWriter outputWriter = converterEngine.getOutputWriter();
			for (int i = 0; i < conf.outputCount(); i++) {
				OutputModule output = conf.output(i);
				str.append(object);
				str.append(outputWriter.toString(output));
			}
			str.append("\n");
		}

		if (conf.taskCount() > 0) {
			ITaskWriter taskWriter = converterEngine.getTaskWriter();
			Iterator<Task> taskIterator = conf.orderedTaskIterator();
			while (taskIterator.hasNext()) {
				Task task = taskIterator.next();
				str.append(taskWriter.toString(task, converterEngine, object));
			}
			str.append("\n");
		}

		if (conf.sequenceCount() > 0) {
			ISequenceWriter sequenceWriter = converterEngine.getSequenceWriter();
			Iterator<Sequence> sequenceIterator = conf.orderedSequenceIterator();
			while (sequenceIterator.hasNext()) {
				Sequence sequence = sequenceIterator.next();
				str.append(sequenceWriter.toString(sequence, converterEngine, object));
			}
			str.append("\n");
		}		
				
		if (conf.pathCount() > 0) {
			IPathWriter pathWriter = converterEngine.getPathWriter();
			for (int i = 0; i < conf.pathCount(); i++) {
				Path path = conf.path(i);
				str.append(pathWriter.toString(path, converterEngine, object));
			}
			str.append("\n");
		}

		IParameterWriter parameterWriter = converterEngine.getParameterWriter();
		Iterator<Block> blockIterator = conf.blockIterator();
		while (blockIterator.hasNext()) {
			Block block = blockIterator.next();
			str.append(block.name() + " = cms.PSet(\n");
			Iterator<Parameter> parameterIterator = block.parameterIterator();
			while (parameterIterator.hasNext()) {
				str.append(parameterWriter.toString(parameterIterator.next(), converterEngine, indent));
			}
			str.append(")\n");
		}

		if (conf.pathCount() > 0) {
			str.append("\n" + object + "schedule = cms.Schedule( *(");
			for (int i = 0; i < conf.pathCount(); i++) {
				Path path = conf.path(i);
				//we need the "," when there is just one path
				//and for multi paths it does no harm to leave it
				str.append(object + path.name() + ", ");
			}
			str.append("))\n");
		}

		return str.toString();
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}

}

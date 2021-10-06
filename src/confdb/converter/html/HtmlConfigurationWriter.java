package confdb.converter.html;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IConfigurationWriter;
import confdb.converter.IEDSourceWriter;
import confdb.converter.IESModuleWriter;
import confdb.converter.IESSourceWriter;
import confdb.converter.IModuleWriter;
import confdb.converter.IEDAliasWriter;
import confdb.converter.IOutputWriter;
import confdb.converter.IParameterWriter;
import confdb.converter.IPathWriter;
import confdb.converter.ISequenceWriter;
import confdb.converter.ITaskWriter;
import confdb.converter.ISwitchProducerWriter;
import confdb.converter.IServiceWriter;
import confdb.data.IConfiguration;
import confdb.data.EDSourceInstance;
import confdb.data.ESModuleInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ModuleInstance;
import confdb.data.EDAliasInstance;
import confdb.data.OutputModule;
import confdb.data.Parameter;
import confdb.data.Path;
import confdb.data.Sequence;
import confdb.data.Task;
import confdb.data.SwitchProducer;
import confdb.data.ServiceInstance;

public class HtmlConfigurationWriter implements IConfigurationWriter {
	protected String indent = "  ";
	protected ConverterEngine converterEngine = null;
	private static final String spaces = "                                                                       ";
	private static final int maxSpaces = spaces.length();

	public String toString(IConfiguration conf, WriteProcess writeProcess) throws ConverterException {
		StringBuffer str = new StringBuffer(100000);
		if (writeProcess == WriteProcess.YES)
			str.append("process <b>" + conf.processName() + "</b> = {" + converterEngine.getNewline());

		str.append(converterEngine.getNewline());

		if (conf.psetCount() > 0) {
			str.append("<a name=\"psets\"></a>");
			IParameterWriter parameterWriter = converterEngine.getParameterWriter();
			for (int i = 0; i < conf.psetCount(); i++) {
				Parameter pset = conf.pset(i);
				str.append(parameterWriter.toString(pset, converterEngine, "  "));
			}
			str.append(converterEngine.getNewline());
		}
		
		if (conf.globalEDAliasCount() > 0) {
			str.append("<a name=\"EDAliases\"><hr noshade></a>");
			IEDAliasWriter globalEDAliasWriter = converterEngine.getGlobalEDAliasWriter();
			for (int i = 0; i < conf.globalEDAliasCount(); i++) {
				EDAliasInstance globalEDAlias = conf.globalEDAlias(i);
				str.append(globalEDAliasWriter.toString(globalEDAlias));
			}
			str.append(converterEngine.getNewline());
		}

		if (conf.pathCount() > 0) {
			str.append("<a name=\"paths\"><hr noshade></a>");
			IPathWriter pathWriter = converterEngine.getPathWriter();
			for (int i = 0; i < conf.pathCount(); i++) {
				Path path = conf.path(i);
				str.append(wrapLine(pathWriter.toString(path, converterEngine, "  "), '&', 12 + path.name().length()));
			}
			if (conf.sequenceCount() == 0)
				str.append(converterEngine.getNewline());
		}

		if (conf.sequenceCount() > 0) {
			str.append("<a name=\"sequences\"><hr noshade></a>" + converterEngine.getNewline());
			ISequenceWriter sequenceWriter = converterEngine.getSequenceWriter();
			for (int i = 0; i < conf.sequenceCount(); i++) {
				Sequence sequence = conf.sequence(i);
				str.append(wrapLine(sequenceWriter.toString(sequence, converterEngine, "  "), '&',
						16 + sequence.name().length()));
			}
			str.append(converterEngine.getNewline());
		}

		if (conf.taskCount() > 0) {
			str.append("<a name=\"tasks\"><hr noshade></a>" + converterEngine.getNewline());
			ITaskWriter taskWriter = converterEngine.getTaskWriter();
			for (int i = 0; i < conf.taskCount(); i++) {
				Task task = conf.task(i);
				str.append(wrapLine(taskWriter.toString(task, converterEngine, "  "), '&', 16 + task.name().length()));
			}
			str.append(converterEngine.getNewline());
		}
		
		if (conf.switchProducerCount() > 0) {
			str.append("<a name=\"switch producers\"><hr noshade></a>" + converterEngine.getNewline());
			ISwitchProducerWriter switchProducerWriter = converterEngine.getSwitchProducerWriter();
			for (int i = 0; i < conf.switchProducerCount(); i++) {
				SwitchProducer switchProducer = conf.switchProducer(i);
				str.append(wrapLine(switchProducerWriter.toString(switchProducer, converterEngine, "  "), 
						'&', 16 + switchProducer.name().length()));
			}
			str.append(converterEngine.getNewline());
		}

		if (conf.moduleCount() > 0) {
			str.append("<a name=\"modules\"><hr noshade></a>");
			IModuleWriter moduleWriter = converterEngine.getModuleWriter();
			for (int i = 0; i < conf.moduleCount(); i++) {
				ModuleInstance module = conf.module(i);
				str.append(moduleWriter.toString(module));
			}
			str.append(converterEngine.getNewline());
		}
		
		if (conf.edAliasCount() > 0) {
			str.append("<a name=\"edaliases\"><hr noshade></a>");
			IEDAliasWriter edAliasWriter = converterEngine.getEDAliasWriter();
			for (int i = 0; i < conf.edAliasCount(); i++) {
				EDAliasInstance edAlias = conf.edAlias(i);
				str.append(edAliasWriter.toString(edAlias));
			}
			str.append(converterEngine.getNewline());
		}

		if (conf.outputCount() > 0) {
			str.append("<a name=\"outputs\"><hr noshade></a>");
			IOutputWriter outputWriter = converterEngine.getOutputWriter();
			for (int i = 0; i < conf.outputCount(); i++) {
				OutputModule output = conf.output(i);
				str.append(outputWriter.toString(output));
			}
			str.append(converterEngine.getNewline());
		}

		if (conf.edsourceCount() > 0)
			str.append("<a name=\"ed_sources\"><hr noshade></a>");
		IEDSourceWriter edsourceWriter = converterEngine.getEDSourceWriter();
		for (int i = 0; i < conf.edsourceCount(); i++) {
			EDSourceInstance edsource = conf.edsource(i);
			str.append(edsourceWriter.toString(edsource, converterEngine, indent));
		}
		if (conf.edsourceCount() == 0) // edsource may be overridden
			str.append(edsourceWriter.toString(null, converterEngine, indent));
		if (conf.edsourceCount() > 0)
			str.append(converterEngine.getNewline());

		if (conf.essourceCount() > 0) {
			str.append("<a name=\"es_sources\"><hr noshade></a>");
			IESSourceWriter essourceWriter = converterEngine.getESSourceWriter();
			for (int i = 0; i < conf.essourceCount(); i++) {
				ESSourceInstance essource = conf.essource(i);
				str.append(essourceWriter.toString(essource, converterEngine, indent));
			}
			str.append(converterEngine.getNewline());
		}

		if (conf.esmoduleCount() > 0) {
			str.append("<a name=\"es_modules\"><hr noshade></a>");
			IESModuleWriter esmoduleWriter = converterEngine.getESModuleWriter();
			for (int i = 0; i < conf.esmoduleCount(); i++) {
				ESModuleInstance esmodule = conf.esmodule(i);
				str.append(esmoduleWriter.toString(esmodule, converterEngine, indent));
			}
			str.append(converterEngine.getNewline());
		}

		if (conf.serviceCount() > 0) {
			str.append("<a name=\"services\"><hr noshade></a>");
			IServiceWriter serviceWriter = converterEngine.getServiceWriter();
			for (int i = 0; i < conf.serviceCount(); i++) {
				ServiceInstance service = conf.service(i);
				str.append(serviceWriter.toString(service, converterEngine, indent));
			}
		}

		if (writeProcess == WriteProcess.YES)
			str.append(converterEngine.getConfigurationTrailer());
		return str.toString();
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}

	protected String wrapLine(String line, char separator, int nspaces) {
		if (line.length() < converterEngine.getMaxLineLength())
			return line;
		int split = line.lastIndexOf(separator, converterEngine.getMaxLineLength());
		if (split <= 0)
			return line;
		String spacer = null;
		if (nspaces >= maxSpaces)
			spacer = spaces;
		else
			spacer = spaces.substring(0, nspaces);

		String firstLine = line.substring(0, split + 2);
		String secondLine = wrapLine(spacer + line.substring(split + 2), separator, nspaces);
		line = firstLine + converterEngine.getNewline() + secondLine;
		return line;
	}
}

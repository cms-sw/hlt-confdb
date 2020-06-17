package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IEDAliasWriter;
import confdb.converter.IParameterWriter;
import confdb.data.EDAliasInstance;
import confdb.data.Parameter;

public class PythonEDAliasWriter implements IEDAliasWriter {

	private IParameterWriter parameterWriter = null;
	private ConverterEngine converterEngine = null;
	private static final String indent = "  ";

	public String toString(EDAliasInstance edAlias) throws ConverterException {
		if (parameterWriter == null)
			parameterWriter = converterEngine.getParameterWriter();

		String name = edAlias.name();

		StringBuffer str = new StringBuffer(name); //this might need fixing since there is no template
		if (edAlias.parameterCount() == 0) {
			str.append(" )\n");
			return str.toString();
		}

		str.append(",\n");
		for (int i = 0; i < edAlias.parameterCount(); i++) {
			Parameter parameter = edAlias.parameter(i);
			String param = parameterWriter.toString(parameter, converterEngine, indent + "  ");
			if (param.length() > 0)
				PythonFormatter.addComma(str, param);
		}
		PythonFormatter.removeComma(str);
		str.append(")\n");
		return str.toString();
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}

	protected String decorate(String name) {
		return name;
	}

}

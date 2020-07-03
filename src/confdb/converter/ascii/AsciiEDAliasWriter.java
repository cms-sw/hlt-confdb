package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IEDAliasWriter;
import confdb.converter.IParameterWriter;
import confdb.data.EDAliasInstance;
import confdb.data.Parameter;

public class AsciiEDAliasWriter implements IEDAliasWriter {

	private IParameterWriter parameterWriter = null;
	private ConverterEngine converterEngine = null;
	private static final String indent = "  ";

	public String toString(EDAliasInstance edAlias) throws ConverterException {
		if (parameterWriter == null)
			parameterWriter = converterEngine.getParameterWriter();

		String name = edAlias.name();

		StringBuffer str = new StringBuffer(indent + "edAlias " + decorate(name) + " = ");
		appendType(str, edAlias);
		str.append(" {");
		if (edAlias.parameterCount() == 0)
			return str.toString() + "}" + converterEngine.getNewline();

		str.append(converterEngine.getNewline());
		for (int i = 0; i < edAlias.parameterCount(); i++) {
			Parameter parameter = edAlias.parameter(i);
			str.append(parameterWriter.toString(parameter, converterEngine, indent + "  "));
		}
		str.append(indent + "}" + converterEngine.getNewline());
		return str.toString();
	}

	public void setConverterEngine(ConverterEngine converterEngine) {
		this.converterEngine = converterEngine;
	}

	protected String decorate(String name) {
		return name;
	}

	protected void appendType(StringBuffer str, EDAliasInstance edAlias) {
		str.append("cms.EDAlias"); // no template
	}

}

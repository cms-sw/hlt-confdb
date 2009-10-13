package confdb.converter;

import confdb.converter.IConfigurationWriter.WriteProcess;
import confdb.data.IConfiguration;

public class ConverterEngine
{
	private String format = "?";
	
	private IConfigurationWriter configurationWriter = null;
	private IParameterWriter parameterWriter = null;
	private IEDSourceWriter edsourceWriter  = null;
	private IESSourceWriter essourceWriter = null;
	private IESModuleWriter esmoduleWriter = null;
	private IServiceWriter serviceWriter = null;
	private IModuleWriter moduleWriter = null;
        private IOutputWriter outputWriter = null;
	private IPathWriter pathWriter = null;
	private ISequenceWriter sequenceWriter = null;

	private int maxLineLength = 250;
	
	static final private String newline = "\n";
	
        //final private String configurationHeader = "process FU = {" + newline;
	final private String configurationTrailer = "}" + newline;

	protected ConverterEngine( String format )
	{
		this.format = format;
	}
	
	public String convert( IConfiguration configuration ) throws ConverterException
	{
		return configurationWriter.toString( configuration, WriteProcess.YES );
	}
	
	public String getNewline()
	{
		return newline;
	}

	public IConfigurationWriter getConfigurationWriter() {
		return configurationWriter;
	}

	protected void setConfigurationWriter(IConfigurationWriter configurationWriter) {
		this.configurationWriter = configurationWriter;
		this.configurationWriter.setConverterEngine(this);
	}


	public IPathWriter getPathWriter() {
		return pathWriter;
	}


	protected void setPathWriter(IPathWriter pathWriter) {
		this.pathWriter = pathWriter;
	}


	public IServiceWriter getServiceWriter() {
		return serviceWriter;
	}


	protected void setServiceWriter(IServiceWriter serviceWriter) {
		this.serviceWriter = serviceWriter;
	}


	public IParameterWriter getParameterWriter() {
		return parameterWriter;
	}


	protected void setParameterWriter(IParameterWriter parameterWriter) {
		this.parameterWriter = parameterWriter;
	}


	public IEDSourceWriter getEDSourceWriter() {
		return edsourceWriter;
	}


	protected void setEDSourceWriter(IEDSourceWriter edsourceWriter) {
		this.edsourceWriter = edsourceWriter;
	}


	public IESSourceWriter getESSourceWriter() {
		return essourceWriter;
	}


	public IESModuleWriter getESModuleWriter() {
		return esmoduleWriter;
	}


	protected void setESSourceWriter(IESSourceWriter essourceWriter) {
		this.essourceWriter = essourceWriter;
	}


	protected void setESModuleWriter(IESModuleWriter esmoduleWriter) {
		this.esmoduleWriter = esmoduleWriter;
	}


	public IModuleWriter getModuleWriter() {
		return moduleWriter;
	}


	protected void setModuleWriter(IModuleWriter moduleWriter) {
		this.moduleWriter = moduleWriter;
		this.moduleWriter.setConverterEngine(this);
	}


	public IOutputWriter getOutputWriter() {
		return outputWriter;
	}


	protected void setOutputWriter(IOutputWriter outputWriter) {
		this.outputWriter = outputWriter;
		this.outputWriter.setConverterEngine(this);
	}


	public ISequenceWriter getSequenceWriter() {
		return sequenceWriter;
	}


	protected void setSequenceWriter(ISequenceWriter sequenceWriter) {
		this.sequenceWriter = sequenceWriter;
	}


	public String getConfigurationTrailer() {
		return configurationTrailer;
	}
	
	static protected String getAsciiNewline()
	{
		return newline;
	}

	public int getMaxLineLength() {
		return maxLineLength;
	}

	public void setMaxLineLength(int maxLineLength) {
		this.maxLineLength = maxLineLength;
	}

	public String getFormat() {
		return format;
	}
	

}

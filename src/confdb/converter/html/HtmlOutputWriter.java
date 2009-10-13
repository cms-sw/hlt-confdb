package confdb.converter.html;

import confdb.converter.IOutputWriter;
import confdb.converter.ascii.AsciiOutputWriter;
import confdb.data.IConfiguration;
import confdb.data.OutputModule;


public class HtmlOutputWriter extends AsciiOutputWriter implements IOutputWriter 
{
	private String release = "";
	
	protected String decorate( String name )
	{
		return "<a name=\"" + name + "\"><b>" + name + "</b></a>";
	}
	
	protected void appendType( StringBuffer str, OutputModule output )
	{
		String type = output.className();
		String cmsPackage = "Modules";
		String subsystem = "EventFilter";

		IConfiguration iconfig = output.config();
		if ( !release.equals( iconfig.releaseTag() ) )
		{
			release = iconfig.releaseTag();
			//System.out.println( "release = " + release );
		}

		str.append( "<a href=\"javascript:showSource('" + release + "','" + type + "','" + cmsPackage + "','" + subsystem + "')\">" + type + "</a>" );
	}
}

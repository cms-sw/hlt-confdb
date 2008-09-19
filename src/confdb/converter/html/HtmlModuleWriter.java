package confdb.converter.html;

import confdb.converter.IModuleWriter;
import confdb.converter.ascii.AsciiModuleWriter;
import confdb.data.IConfiguration;
import confdb.data.ModuleInstance;


public class HtmlModuleWriter extends AsciiModuleWriter implements IModuleWriter 
{
	private String release = "";
	
	protected String decorate( String name )
	{
		return "<a name=\"" + name + "\"><b>" + name + "</b></a>";
	}
	
	protected void appendType( StringBuffer str, ModuleInstance module )
	{
		String type = module.template().name();
		String cmsPackage = module.template().parentPackage().name();

		IConfiguration iconfig = module.config();
		if ( !release.equals( iconfig.releaseTag() ) )
		{
			release = iconfig.releaseTag();
			//System.out.println( "release = " + release );
		}

		str.append( "<a href=\"javascript:showSource('" + release + "','" + type + "','" + cmsPackage + "')\">" + type + "</a>" );
	}
}

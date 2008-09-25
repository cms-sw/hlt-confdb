package confdb.converter.html;

import confdb.converter.ascii.AsciiInstanceWriter;
import confdb.data.Instance;

public class HtmlInstanceWriter extends AsciiInstanceWriter 
{	
	@Override
	protected String getName(Instance instance) 
	{
		return "<b>" + instance.name() + "</b>";
	}

	@Override
	protected String getTemplateName( Instance instance ) 
	{
		String type = instance.template().name();
		String release = instance.config().releaseTag();
		String cmsPackage = instance.template().parentPackage().name();
		String subsystem = instance.template().parentPackage().subsystem().name();


		return "<a href=\"javascript:showSource('" + release + "','" + type + "','" + cmsPackage + "','" + subsystem + "')\">" + type + "</a>";
	}
	
	
}

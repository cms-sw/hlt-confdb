package confdb.converter.html;

import confdb.converter.IEDAliasWriter;
import confdb.converter.ascii.AsciiEDAliasWriter;
import confdb.data.IConfiguration;
import confdb.data.EDAliasInstance;

public class HtmlEDAliasWriter extends AsciiEDAliasWriter implements IEDAliasWriter {

	private String release = "";

	protected String decorate(String name) {
		return "<a name=\"" + name + "\"><b>" + name + "</b></a>";
	}

	protected void appendType(StringBuffer str, EDAliasInstance edAlias) {
		//EDAlias has no template
		IConfiguration iconfig = edAlias.config();
		if (!release.equals(iconfig.releaseTag())) {
			release = iconfig.releaseTag();
			// System.out.println( "release = " + release );
		}

		str.append("<a href=\"javascript:showSource('" + release + "')\">"  + "</a>"); 
	}

}

package confdb.converter.summary.data;

import java.util.Iterator;
import java.util.ArrayList;

import confdb.data.*;

/**
 * SummaryTableRow
 * ---------------
 * @author Philipp Schieferdecker
 *
 * Organize the summary data associated with one trigger path.
 */
public class SummaryTableRow implements ITableRow
{
    //
    // data members
    //
    private static final String l1TemplateName    = "HLTLevel1GTSeed";
    private static final String l1CondParamName   = "L1SeedsLogicalExpression";
    private static final String l3TemplateType    = "HLTFilter";
    private static final String l3TypeBlackList[] = { "HLTBool","HLTPrescaler" };
    
    /** flag indicating if this path is really a trigger */
    private boolean isNoTrigger = false;

    /** trigger path name */
    private String triggerName;

    /** level1 trigger condition, unformatted */
    private String l1Condition;

    /** the names of the filters to appear in the summary */
    private ArrayList<String> filterNames  = new ArrayList<String>();

    /** the parameters of each filter, encoded in a string (p1=v2::p2=v2::...) */
    private ArrayList<String> filterParams = new ArrayList<String>();
    
    //
    // construction
    //
    
    /** standard constructor */
    public SummaryTableRow(Path path)
    {
	ModuleInstance l1Module = null;
	ArrayList<ModuleInstance> filters = new ArrayList<ModuleInstance>();
	Iterator<ModuleInstance> itM = path.moduleIterator();
	while (itM.hasNext()) {
	    ModuleInstance module = itM.next();
	    String templateName = module.template().name();
	    String templateType = module.template().type();
	    if (templateName.equals(l1TemplateName)) l1Module = module;
	    else if (templateType.equals(l3TemplateType)) {
		if (isRelevantFilter(templateName)) filters.add(module);
	    }
	    else filters.clear();
	}
	
	isNoTrigger = (l1Module==null);

	triggerName = path.name();	
	
	l1Condition = (isNoTrigger()) ?
	    "-" : l1Module.parameter(l1CondParamName,"string").valueAsString();
	if (l1Condition.startsWith("\""))
	    l1Condition = l1Condition.substring(1);
	if (l1Condition.endsWith("\""))
	    l1Condition = l1Condition.substring(0,l1Condition.length()-1);
	
	itM = filters.iterator();
	while (itM.hasNext()) {
	    ModuleInstance filter = itM.next();
	    filterNames.add(filter.name());
	    StringBuffer sbParams = new StringBuffer();
	    Iterator<Parameter> itP = filter.parameterIterator();
	    while (itP.hasNext()) {
		Parameter p = itP.next();
		if (p.type().equals("double")||p.type().equals("int32")) {
		    if (sbParams.length()>0) sbParams.append("::");
		    sbParams.append(p.name()).append("=").append(p.valueAsString());
		}
	    }
	    filterParams.add(sbParams.toString());
	}
    }
    
    
    //
    // member functions
    //

    /** flag indicating if this path is really a trigger */
    public boolean isNoTrigger() { return isNoTrigger; }

    /** number of columns for _this_ trigger */
    public int columnCount() { return filterNames.size()+2; }

    /** number of lines needed to display this trigger */
    public int lineCount()
    {
	int result = (l1Condition.split(" ").length+1)/2;
	Iterator<String> it = filterParams.iterator();
	while (it.hasNext()) {
	    int tmp = it.next().split("::").length+1;
	    if (tmp>result) result = tmp;
	}
	return result;
    }

    /** width needed to display a particular column for this trigger */
    public int columnWidth(int iColumn)
    {
	if (iColumn>=columnCount()) return 0;
	if (iColumn==0) return triggerName.length()+1;
	if (iColumn==1) {
	    int width = 0;
	    String a[] = l1Condition.split(" ");
	    for (String s : a) if (s.length()>width) width = s.length();
	    return width+5;
	}
	int width = filterNames.get(iColumn-2).length();
	String a[] = filterParams.get(iColumn-2).split("::");
	for (String s : a) if (s.length()>width) width = s.length();
	return width+1;
    }

    /** get the data entry to be displayed in particluar column/line */
    public String dataEntry(int iColumn,int iLine)
    {
	if      (iColumn==0) return (iLine==0) ? triggerName : "";
	else if (iColumn==1) return l1ConditionLineEntry(iLine);
	else return filterLineEntry(iColumn-2,iLine);
    }

    //
    // private member functions
    //

    /** get the data to be displayed regarding the lvl1 cond. for particular line */
    private String l1ConditionLineEntry(int iLine)
    {
	String a[] = l1Condition.split(" ");
	int    i   = iLine*2;
	if (i>=a.length) return new String();
	String result = a[i];
	if (i<a.length-1) result+=" "+a[i+1];
	return result;
    }

    /** get the data to be displayed regarding filter i, for particular line */
    private String filterLineEntry(int iFilter,int iLine)
    {
	if (iFilter>=filterNames.size()) return new String();
	if (iLine==0) return filterNames.get(iFilter);
	int i = iLine-1;
	String a[] = filterParams.get(iFilter).split("::");
	return (i<a.length) ? a[i] : new String();
    }

    /** indicates if filter of this type is relevant for the summary */
    private boolean isRelevantFilter(String templateName)
    {
	for (String s : l3TypeBlackList) if (s.equals(templateName)) return false;
	return true;
    }
    
}

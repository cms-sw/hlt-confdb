package confdb.data;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.StringTokenizer;

/**
 * SmartPrescaleTable
 * -------------
 * @author Vasundhara Chetluru
 *
 */
public class SmartPrescaleTable
{
    //
    // member data
    //
    

    /** prescale table rows */
    private ArrayList<SmartPrescaleTableRow> rows=new ArrayList<SmartPrescaleTableRow>();
    private ArrayList<Stream> streams=new ArrayList<Stream>();
    public ModuleInstance module;
    private IConfiguration config;
    
    //
    // construction
    //
    
    /** standard constructor */
    public SmartPrescaleTable(IConfiguration config, ModuleInstance module)
    {
	this.config = config;
	this.module = module;
	initialize();
    }

    


    //
    // member functions
    //
    
     
    /** number of prescaled paths */
    public int prescaleConditionCount() { return rows.size(); }

    /** get i-th path name */
    public String  prescaleCondition(int i)
    {
	return rows.get(i).triggerCondition;
    }

    
    /** check if the i-th path is prescaled at all */
    public boolean isSmartPrescaled(int i)
    {
	//need to implement the code
	return true;
    }

    /** check if the i-th path is prescaled at all */
    public boolean addRow(int i,String strCondition)
    {
	//need to implement the code
	if(prescaleConditionCount()==1){
	    if(prescaleCondition(i).equals("")){
		rows.remove(i); 
		module.setHasChanged();
	    }
	}
	rows.add(i,new SmartPrescaleTableRow(strCondition)); 
	module.setHasChanged();
	return true;
    }

    public boolean modRow(int i,String strCondition)
    {
	rows.set(i,new SmartPrescaleTableRow(strCondition));
	module.setHasChanged();
	return true;
    }

    /**check path in the streams **/
    public Path checkPathExists(String strPath){
	if(streams.size()>0){
	    for(int j=0;j<streams.size();j++){
		Path path =streams.get(j).path(strPath);
		if(path!=null)
		    return path;
	    }
	}else{
	    return config.path(strPath);
	}
	return null;
    }
    
    /** check if the i-th path is prescaled at all */
    public boolean removeRow(int i)
    {
	rows.remove(i); 
	if(prescaleConditionCount()==0){
	    rows.add(new SmartPrescaleTableRow("")); 
	}
	module.setHasChanged();
	return true;
    }


    //
    // private member functions
    //
    
    /** initialize the prescale table from a given configuration */
    private void initialize()
    {
	
	Path[] paths = module.parentPaths();
	
	for (Path p : paths){
	    if(p.isEndPath()&&p.hasOutputModule()){		
		Iterator<OutputModule> outputIterator = p.outputIterator();
		while(outputIterator.hasNext()){
		    OutputModule outputModule = outputIterator.next(); 
		    Stream stream = outputModule.parentStream();
		    streams.add(stream);
		}
	    }
	}
	update();
    }

    public void update(){

	rows.clear();
      
	VStringParameter parameterTriggerConditions =  (VStringParameter)module.parameter("triggerConditions");
	
	if (parameterTriggerConditions==null) {
	    rows.add(new SmartPrescaleTableRow("")); 
	    return;
	}

	for(int i=0;i<parameterTriggerConditions.vectorSize();i++){
	    String trgCondition = (String)parameterTriggerConditions.value(i);
	    String strCondition = SmartPrescaleTable.regularise(trgCondition);

	    // replace unknown paths by FALSE
	    StringTokenizer pathTokens = new StringTokenizer(strCondition,"/ ");
	    while ( pathTokens.hasMoreTokens()) {
		String strPath = pathTokens.nextToken().trim();
		if (strPath.length()<5) continue;
		int g = -10000;
		try { 
		    g = Integer.parseInt(strPath); 
		}catch (NumberFormatException e) { 
		    g = -10000;
		}
		if ( (g<0)
		     && (!strPath.equals("FALSE"))
		     && (!strPath.substring(0,2).equals("L1"))
		     && (checkPathExists(strPath)==null) ) {
		    strCondition = strCondition.replaceAll(strPath,"FALSE");
		}
	    }

	    // replace conditions containing only FALSE by empty conditions
	    strCondition = SmartPrescaleTable.simplify(strCondition);

	    if (!strCondition.equals(trgCondition)) {
		module.setHasChanged();
		parameterTriggerConditions.setValue(i,strCondition);
	    }
	    if (!strCondition.equals("")) {
		rows.add(new SmartPrescaleTableRow(strCondition));   
	    }
	}
	// remove empty conditions
	if (module.squeeze()) module.setHasChanged();	
    }

    public static String regularise(String input) {
	String work = new String (input);
	work = work.replaceAll("/"," / ");
	work = work.replaceAll("\\("," \\( "); // regex escape
        work = work.replaceAll("\\)"," \\) "); // regex escape
	while (work.indexOf("  ")>=0) { work=work.replaceAll("  "," "); }
	work = work.trim();
	return work;
    }

    public static String simplify(String input) {
	String work = new String(input);
	work=regularise(work);

	/* replace conditions containing only FALSE by empty conditions */
	StringTokenizer moreTokens = new StringTokenizer(work,"/ ");
	int n=0;
	while ( moreTokens.hasMoreTokens()) {
	    String strPath = moreTokens.nextToken().trim();
	    if (strPath.length()<5) continue;
	    int g = -10000;
	    try { 
		g = Integer.parseInt(strPath); 
	    }catch (NumberFormatException e) { 
		g = -10000;
	    }
	    if ( (g<0) && (!strPath.equals("FALSE"))) n++;
	}
	if (n==0) work="";
	return work;
    }

}


//
// class to hold the data for one smart prescale table row
//
class SmartPrescaleTableRow
{
    public String triggerCondition;
    public SmartPrescaleTableRow(String triggerCondition)
    {
	this.triggerCondition = triggerCondition;
    }
}

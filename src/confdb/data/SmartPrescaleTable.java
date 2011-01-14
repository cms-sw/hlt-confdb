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
	return rows.get(i).pathName;
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
	
	
	if(streams.size()>0){
	    for(int i=0;i<parameterTriggerConditions.vectorSize();i++){
		String strValue = (String)parameterTriggerConditions.value(i);
		
		StringTokenizer pathTokens = new StringTokenizer(strValue,"+-/&* ");
		
		while ( pathTokens.hasMoreTokens()) {
		    String strPath = pathTokens.nextToken();
		    int g = -10000;
		    try { 
			g = Integer.parseInt(strPath); 
		    }catch (NumberFormatException e) { 
			g = -10000;
		    }
		    if(g>0||strPath.equals("FALSE"))
			continue;
		    Path pathT = null;
		    for(int j=0;j<streams.size();j++){
			pathT = streams.get(j).path(strPath);
		    }
		    if(pathT==null){
			strValue = strValue.replaceAll(strPath,"FALSE");
			module.setHasChanged();
		    };
		}
		rows.add(new SmartPrescaleTableRow(strValue));   
	    } 
	}else{
		for(int i=0;i<parameterTriggerConditions.vectorSize();i++){
		    String strValue = (String)parameterTriggerConditions.value(i);
		    rows.add(new SmartPrescaleTableRow(strValue)); 
		}
	}
  
    }
    
}


//
// class to hold the data for one prescale table row
//
class SmartPrescaleTableRow
{
    public String pathName;
    public SmartPrescaleTableRow(String pathName)
    {
	this.pathName = pathName;
    }
    public SmartPrescaleTableRow(String pathName, int prescaleCount)
    {
	this.pathName = pathName;
    }
}

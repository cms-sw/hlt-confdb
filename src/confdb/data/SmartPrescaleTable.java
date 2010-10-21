package confdb.data;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;


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
    

    //
    // construction
    //
    
    /** standard constructor */
    public SmartPrescaleTable(IConfiguration config, ModuleInstance module)
    {
	initialize(config,module);
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
	    if(prescaleCondition(i).equals(""))
		rows.remove(i); 
	}
	rows.add(i,new SmartPrescaleTableRow(strCondition)); 
	return true;
    }

    
    /** check if the i-th path is prescaled at all */
    public boolean removeRow(int i)
    {
	rows.remove(i); 
	if(prescaleConditionCount()==0){
	    rows.add(new SmartPrescaleTableRow("")); 
	}
	return true;
    }


    //
    // private member functions
    //
    
    /** initialize the prescale table from a given configuration */
    private void initialize(IConfiguration config,ModuleInstance module)
    {
	update(config,module);
    }

    public void update(IConfiguration config,ModuleInstance module){
	rows.clear();
      
	VStringParameter parameterTriggerConditions =  (VStringParameter)module.parameter("triggerConditions");
	
	if (parameterTriggerConditions==null) {
	    rows.add(new SmartPrescaleTableRow("")); 
	    return;
	}
	
	for(int i=0;i<parameterTriggerConditions.vectorSize();i++){
	    String strValue = (String)parameterTriggerConditions.value(i);
	    rows.add(new SmartPrescaleTableRow(strValue)); 
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

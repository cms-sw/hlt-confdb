package confdb.converter.python;

import java.util.Iterator;
import java.util.ArrayList;
import confdb.converter.ConverterEngine;
import confdb.converter.IPathWriter;
import confdb.data.Path;
import confdb.data.Reference;
import confdb.data.Task;

public class PythonPathWriter implements IPathWriter 
{
	public String toString( Path path, ConverterEngine converterEngine, String object ) 
	{
		String str = "";
		if ( path.isEndPath() )
			str = object + path.name() +  " = cms.EndPath( "; 
		else if( path.isFinalPath() )
			str = object + path.name() +  " = cms.FinalPath( "; 
		else
			str = object + path.name() +  " = cms.Path( "; 
		
		if ( path.entryCount() > 0 )
		{                 
		        ArrayList<String> nonTasks = new ArrayList<String>();
			ArrayList<String> tasks = new ArrayList<String>();
               		String nonTaskSep =  " + ";
			String taskSep =  " , ";
			Iterator<Reference> list = path.entryIterator();
			while ( list.hasNext() ){
			    Reference ref = list.next();
			    if(ref.parent() instanceof Task){
				tasks.add(ref.getPythonCode(object));
			    }else{
			        nonTasks.add(ref.getPythonCode(object));
			    }
			}
			String nonTaskStr = String.join(nonTaskSep,nonTasks);
			String taskStr = String.join(taskSep,tasks);
			if(nonTaskStr.isEmpty() || taskStr.isEmpty()){
			    str += nonTaskStr + taskStr;
			}else{
			    str += nonTaskStr + " , " + taskStr;
			}
		}
		
		str += " )\n";
		return str;
	}

}

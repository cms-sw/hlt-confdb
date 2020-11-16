package confdb.converter.python;

import java.util.Iterator;
import java.util.ArrayList;
import confdb.converter.ConverterEngine;
import confdb.converter.ISequenceWriter;
import confdb.data.Reference;
import confdb.data.Sequence;
import confdb.data.Task;

public class PythonSequenceWriter implements ISequenceWriter 
{
	public String toString( Sequence sequence, ConverterEngine converterEngine, String object ) 
	{
		String str = object + sequence.name() +  " = cms.Sequence( ";
		if ( sequence.entryCount() > 0 )
		{
		        ArrayList<String> nonTasks = new ArrayList<String>();
			ArrayList<String> tasks = new ArrayList<String>();
               		String nonTaskSep =  " + ";
			String taskSep =  " , ";
			Iterator<Reference> list = sequence.entryIterator();
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
			    str += nonTaskStr + ","+taskStr;
			}
		}
		str += " )\n";
		return str;
	}

}

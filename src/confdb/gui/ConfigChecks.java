package confdb.gui;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.Map;

import confdb.data.*;
/** 
 * this class performs all the checks to ensure the config is valid before saving/convert
 * all functions return false if their check fails. A warn function always returns true
 */
class ConfigChecks {

    /** checks if the config is valid, running all checks and warnings
     * returns true if valid, false if not
    */
    public static boolean pass(Configuration config,JFrame frame) {
		if (config.isEmpty())
			return false;

		return checkUnassignedPaths(config, frame) &&
        checkEmptyContainer(config, frame) &&
        checkModsAndTaskAndSequence(config, frame) &&
        checkUnassignedPaths(config, frame) &&
        checkDatasetPaths(config, frame) && 
        checkDatasetPathContent(config, frame) &&
        checkDatasetPathPrescales(config, frame) &&
        checkStreamOutputPaths(config, frame) &&
        checkFinalPathContent(config, frame) &&
        checkForExtraFinalPaths(config, frame) && 
        checkForReservedNames(config, frame) &&
        checkForInvalidDefaultPSCol(config, frame) && 
        checkForDupPaths(config, frame);
	}

    public static boolean checkUnassignedPaths(Configuration config, JFrame frame){
        boolean unassignedPaths = false;
		String streamsWithUnassigned = new String("");
		Iterator<Stream> streamIt = config.streamIterator();
		while (streamIt.hasNext()) {
			Stream stream = streamIt.next();
			if(stream.unassignedPathCount()!=0){
				unassignedPaths = true;				
				if(!streamsWithUnassigned.isEmpty()){
					streamsWithUnassigned+=" ";
				}
				streamsWithUnassigned += stream.name();
			}
		}
		if(unassignedPaths){
			String msg = "current configuration has following streams \"" + streamsWithUnassigned+ "\"with unassigned paths, those paths must be removed from the streams before saving/converting!";			
			JOptionPane.showMessageDialog(frame, msg, "", JOptionPane.ERROR_MESSAGE);
			return false;
		}
        return true;
    }
   
    public static boolean checkEmptyContainer(Configuration config, JFrame frame){
		int emptyContainerCount = config.emptyContainerCount();
		if (emptyContainerCount > 0) {
			String msg = "current configuration contains " + emptyContainerCount
					+ " empty containers (paths/sequences/tasks/switchProducers). "
					+ "\nThey must be filled before saving/converting!"
                    + "\nA typical action to take is \"remove unreferenced containers\" on sequences/tasks";
			JOptionPane.showMessageDialog(frame, msg, "", JOptionPane.ERROR_MESSAGE);
			return false;
		}
        return true;
    }

    public static boolean warnUnsetTrackedParameters(Configuration config, JFrame frame){
        int unsetParamCount = config.unsetTrackedParameterCount();
		if (unsetParamCount > 0) {
			String msg = "current configuration contains " + unsetParamCount
					+ " unset tracked parameters. They *should* be set before " + "saving/converting!";
			JOptionPane.showMessageDialog(frame, msg, "", JOptionPane.WARNING_MESSAGE);
		}
        return true;
    }

    public static boolean checkModsAndTaskAndSequence(Configuration config, JFrame frame){
        Iterator<ModuleInstance> modIt = config.moduleIterator();
		String taskModsOnSeqsPaths = new String();
		int nrRows = 3;
		while(modIt.hasNext()){
			ModuleInstance mod = modIt.next();
			ArrayList<Path> paths = new ArrayList<Path>();
			ArrayList<Sequence> seqs = new ArrayList<Sequence>();
			ArrayList<Task> tasks = new ArrayList<Task>();
			for(int refNr=0;refNr<mod.referenceCount();refNr++){
				Reference ref = mod.reference(refNr);
				if(ref.container() instanceof Path){
					paths.add((Path) ref.container());
				}else if(ref.container() instanceof Sequence){
					seqs.add((Sequence) ref.container());
				}else if (ref.container() instanceof Task){
					tasks.add((Task) ref.container());
				}
			}
			if(tasks.size()!=0 && seqs.size()+paths.size()!=0){
				nrRows+=4;
				taskModsOnSeqsPaths+="\nmodule "+mod.name()+"\n   tasks:";
				for(Task task: tasks) taskModsOnSeqsPaths+=" "+task.name();
				taskModsOnSeqsPaths+="\n   sequences:";
				for(Sequence seq: seqs) taskModsOnSeqsPaths+=" "+seq.name();
				taskModsOnSeqsPaths+="\n   paths:";
				for(Path path: paths) taskModsOnSeqsPaths+=" "+path.name();
				taskModsOnSeqsPaths+="\n";
			}
		}
		if(!taskModsOnSeqsPaths.isEmpty()){
			String msg = new String("current configuration has modules on tasks which are directly on Sequences/Paths which is forbidden\nplease remove the offending modules below from the tasks or seq/paths\n");
			msg+=taskModsOnSeqsPaths;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(nrRows,50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);
			return false;
		}
        return true;

    }

    public static boolean checkDatasetPaths(Configuration config, JFrame frame){

        //we will write our all our errors here to then display to the user
        //errors are as they come and are not otherwise ordered
        ArrayList<String> errors = new ArrayList<String>();

        ArrayList<String> validDatasetPathNames = new ArrayList<String>();        
        ArrayList<String> datasetsChecked = new ArrayList<String>();
        Iterator<PrimaryDataset> datasetIt = config.datasetIterator();
        while(datasetIt.hasNext()){
            PrimaryDataset dataset = datasetIt.next();
            if(dataset.datasetPath()!=null){                
                validDatasetPathNames.add(dataset.datasetPath().name());
            }
            ArrayList<PrimaryDataset> splitSiblings = dataset.getSplitSiblings();
            if(datasetsChecked.indexOf(dataset.name())==-1){             
                for(int instNr=0;instNr<splitSiblings.size();instNr++){                    
                    PrimaryDataset splitSibling = splitSiblings.get(instNr);
                    datasetsChecked.add(splitSibling.name());                     
                    if(instNr!=splitSibling.splitInstanceNumber()){                    
                        errors.add("dataset path of "+splitSibling.name()+" has prescale offset of "+splitSibling.splitInstanceNumber()+" rather than required "+instNr);                        
                    }
                }
            }

        }
                
        Iterator<Path> pathIt = config.pathIterator();
        String datasetPathPrefix = PrimaryDataset.datasetPathNamePrefix();
        while(pathIt.hasNext()){
            Path path = pathIt.next();
            if(path.isDatasetPath()){
                if(validDatasetPathNames.indexOf(path.name())==-1){
                    errors.add("dataset path "+path.name()+" does not have a corresponding dataset or that dataset is not properly linked to it");
                }
                if(!path.name().startsWith(datasetPathPrefix)){
                    errors.add("dataset path "+path.name()+" does not start with "+datasetPathPrefix);
                }
            }else {
                if(validDatasetPathNames.indexOf(path.name())!=-1){
                    errors.add("path "+path.name()+" is thought by a dataset to be its dataset path but is not set as a dataset path");
                }            
                if(path.name().startsWith(datasetPathPrefix)){
                    errors.add("path "+path.name()+" starts with "+datasetPathPrefix+" but is not a dataset path");
                }
            }
        }  
        if(!errors.isEmpty()){
            String errStr = new String();
            for(String error : errors ){
                errStr+="\n"+error;
            }
            String msg = new String("The current config has problems with the dataset paths.\nThis shouldnt really be possible and it would be good to report this to the ConfdbDev channel of the TSG mattermost.\nIt may not be possible to fix this without expert help.\n");
			msg+=errStr;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(errors.size()+5,50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * a dataset path should have exactly three items
     * HLTDatasetPathBeginSequence
     * a TriggerResultsFilter
     * a prescale module
     * in that order
     */
    public static boolean checkDatasetPathContent(Configuration config, JFrame frame){
        ArrayList<String> errors = new ArrayList<String>();
        Iterator<Path> pathIt = config.pathIterator();
        while(pathIt.hasNext()){
            Path path = pathIt.next();
            if(path.isDatasetPath()){
                if(path.entryCount()!=3){
                    errors.add("path "+path.name()+" has "+path.entryCount()+" when exactly three are required");
                }else{
                    if(!path.entry(0).name().equals(PrimaryDataset.datasetPathBeginSequenceName())){
                        errors.add("path "+path.name()+" first entry is not "+PrimaryDataset.datasetPathBeginSequenceName()+" as required");
                    }   
                    if(!(path.entry(1).parent() instanceof ModuleInstance) || !((ModuleInstance) path.entry(1).parent()).isDatasetPathFilter()){
                        errors.add("path "+path.name()+" second entry is not a datasetPathFilter as required");
                    }
                    if(!(path.entry(2).parent() instanceof ModuleInstance) || !((ModuleInstance) path.entry(2).parent()).isDatasetPathPrescaler()){
                        errors.add("path "+path.name()+" third entry is not a prescale module as required");
                    }   
                }
            }
        }
                
        if(!errors.isEmpty()){
            String errStr = new String();
            for(String error : errors ){
                errStr+="\n"+error;
            }
            String msg = new String("The current config the following dataset paths with invalid content.\nA dataset path should solely contain the following in this exact order:\n   "+PrimaryDataset.datasetPathBeginSequenceName()+", a "+PrimaryDataset.pathFilterType()+" and a prescaler module\n");

			msg+=errStr;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(errors.size()+5,50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public static boolean checkDatasetPathPrescales(Configuration config, JFrame frame){

        ArrayList<String> errors = new ArrayList<String>();

        Iterator<PrimaryDataset> datasetIt = config.datasetIterator();
        ArrayList<String> datasetsChecked = new ArrayList<String>();
        PrescaleTable psTbl = new PrescaleTable(config);

        while(datasetIt.hasNext()){
            PrimaryDataset dataset = datasetIt.next();
            if(datasetsChecked.contains(dataset.nameWithoutInstanceNr())){                
                continue;
            }            
            datasetsChecked.add(dataset.nameWithoutInstanceNr());
            
            ArrayList<PrimaryDataset> splitSiblings = dataset.getSplitSiblings();            
            //there will always be at least one sibling, itself
            ArrayList<Long> prescales0 = psTbl.prescales(splitSiblings.get(0).datasetPathName());
        
            for(Long prescale : prescales0){
                if(prescale<splitSiblings.size() && prescale!=0){
                    errors.add("Error Dataset "+dataset.nameWithoutInstanceNr()+" has dataset path with a non-zero precale value lower than its number of split instances  ("+splitSiblings.size()+")");
                    break;
                }
            }

            for(int siblingNr=1;siblingNr<splitSiblings.size();siblingNr++){
                PrimaryDataset splitSibling = splitSiblings.get(siblingNr);
                ArrayList<Long> prescalesSib = psTbl.prescales(splitSibling.datasetPathName());
                if(!prescales0.equals(prescalesSib)){
                    errors.add("Error datasetpath of "+splitSibling.name()+" has different prescales to its 0th instance");
                }
            }
        }

        if(!errors.isEmpty()){
            String errStr = new String();
            for(String error : errors ){
                errStr+="\n"+error;
            }
            String msg = new String("The following split datasets have problems with the dataset path prescales.\nNote that a dataset path can not have a prescale lower than the number of split instances\notherwise the split instances will select the same events\n");
			msg+=errStr;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(errors.size()+5,50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;

    }
    /**
     * checks each stream with a datasetpath has an output module
     */
    static boolean checkStreamOutputPaths(Configuration config, JFrame frame){
        ArrayList<String> errors = new ArrayList<String>();
        Iterator<Stream> streamIt = config.streamIterator();
        while(streamIt.hasNext()){
            Stream stream = streamIt.next();
            if(stream.hasDatasetPath()){
                Path outPath = config.path(stream.outputPathName());
                if(outPath==null) {
                    errors.add(stream.outputPathName()+" for stream "+stream.name()+" output does not exist ");
                } else if(!outPath.isOutputPathOfStream(stream)){
                    errors.add(stream.outputPathName()+" for stream "+stream.name()+" output exists but is not valid");
                }
            }
        }
        if(!errors.isEmpty()){
            String errStr = new String();
            for(String error : errors ){
                errStr+="\n"+error;
            }         
            String msg = new String("The current config has streams with a DatasetPath which do not have a StreamOutputPath.\nA stream output path is named <StreamName>Output, is a FinalPath and contains only the streams output module.\nThese are automatically generated but this generation can fail if a path exists of the same name.\nPlease delete/rename the offending paths if they exist and then right click on streams and select \"Generate Output Paths\"\n");
			msg+=errStr;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(errors.size()+6,50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);            
            return false;
        }

        return true;

    }
    //checks if there are any final paths which contain something other than an output module
    static boolean checkFinalPathContent(Configuration config, JFrame frame){
        ArrayList<String> errors = new ArrayList<String>();
        Iterator<Path> pathIt = config.pathIterator();
        while(pathIt.hasNext()){
            Path path = pathIt.next();
            if(path.isFinalPath()){
                Iterator<Reference> entryIt = path.entryIterator();
                while(entryIt.hasNext()){
                    Reference entry = entryIt.next();
                    if(!(entry.parent() instanceof OutputModule)){
                        errors.add("final path "+path.name()+" has entries which are not an output module");
                        break;
                    }
                }

            }
        }
        if(!errors.isEmpty()){
            String errStr = new String();
            for(String error : errors ){
                errStr+="\n"+error;
            }         
            String msg = new String("The current config has FinalPaths which contain something other than OutputModules which is forbidden.\nThis includes containers such as sequences/tasks.\nPlease delete/fix the offending paths\n\n");
			msg+=errStr;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(errors.size()+6,50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);            
            return false;
        }
        return true;
    }
    
    static boolean checkForExtraFinalPaths(Configuration config, JFrame frame){
        ArrayList<String> errors = new ArrayList<String>();
        Iterator<Stream> streamIt = config.streamIterator();
        ArrayList<String> validFinalPathNames = new ArrayList<String>();
        while(streamIt.hasNext()){
            Stream stream = streamIt.next();
            if(stream.hasDatasetPath()){
                validFinalPathNames.add(stream.outputPathName());
            }
        }
        Iterator<Path> pathIt = config.pathIterator();
        while(pathIt.hasNext()){
            Path path = pathIt.next();
            if(path.isFinalPath() && validFinalPathNames.indexOf(path.name())==-1){
                errors.add("path "+path.name()+" is a final path but is not a StreamOutputPath, please remove it");
            }

        }


        if(!errors.isEmpty()){
            String errStr = new String();
            for(String error : errors ){
                errStr+="\n"+error;
            }         
            String msg = new String("The current config has FinalPaths which are not named such they correspont to a  StreamOutputPath.\nPlease remove them. If you think they should exist please alert the ConfdbDev channel of the TSG mattermost\n");
			msg+=errStr;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(errors.size()+5,50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);            
            return false;
        }

        return true;

    }

    static boolean checkForReservedNames(Configuration config, JFrame frame){        
        ArrayList<String> errors = new ArrayList<String>();
        for(String name : Configuration.reservedNames){
            if(!config.nameNotInProcessObject(name)){
                errors.add("reserved name \""+name+"\" is present in the config");
            }
        }
        if(!errors.isEmpty()){
            String errStr = new String();
            for(String error : errors ){
                errStr+="\n"+error;
            }         
            String msg = new String("The following reserved names are present in the configuration\nThese will be overriden by the python converter so please rename them.\n");
			msg+=errStr;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(errors.size()+5,50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);            
            return false;
        }
        return true;
    }

    static boolean checkForInvalidDefaultPSCol(Configuration config, JFrame frame){

        ServiceInstance psService = config.service("PrescaleService");
		if (psService != null) {
            VStringParameter colNames = (VStringParameter)psService.parameter("lvl1Labels","vstring");
            StringParameter defaultLabel = (StringParameter)psService.parameter("lvl1DefaultLabel","string");
            boolean defaultFound = false;
            for(int colNr=0;colNr<colNames.vectorSize();colNr++){
                if(defaultLabel.value().equals(colNames.value(colNr))){
                    defaultFound=true;                    
                    break;
                }
            }
            if(!defaultFound){                      
                String msg = new String("The default PS column \""+defaultLabel.value()+"\" does not exist.\nPlease set it to an available column.\n");
                msg+="The available columns are:";
                for(int colNr=0;colNr<colNames.vectorSize();colNr++){
                    msg+="\n   "+colNames.value(colNr);
                }
                JTextArea textArea = new JTextArea(msg);
                JScrollPane scrollPane = new JScrollPane(textArea);  
                //textArea.setLineWrap(true);  
                //textArea.setWrapStyleWord(true); 
                textArea.setColumns(80);
                textArea.setRows(Math.min(msg.split("\n").length+1,50));
                JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);            
                return false; 
            }
        }
        return true;

    }
    
    static boolean checkForDupPaths(Configuration config, JFrame frame){
      ArrayList<String> pathNames = new ArrayList<String>();
      for(int pathNr=0;pathNr<config.pathCount();pathNr++){
        pathNames.add(config.path(pathNr).name());
      }
      Map<String, Long> counts = pathNames.stream().collect(Collectors.groupingBy(p -> Path.rmVersion(p),Collectors.counting()));
       
      ArrayList<String> errors = new ArrayList<String>();
      Iterator<Map.Entry<String,Long>> pathIt = counts.entrySet().iterator();
      while(pathIt.hasNext()){
        Map.Entry<String,Long> pathEntry = pathIt.next();
        if(pathEntry.getValue()>1){
          errors.add(pathEntry.getKey()+" : "+pathEntry.getValue()+" versions");
        }

      }
      if(!errors.isEmpty()){
        String errStr = new String();
        for(String error : errors ){
            errStr+="\n"+error;
        }         
        String msg = new String("There are multiple versions of path(s) present in the menu\n\neg HLT_Ele32_WPTight_v3 and HLT_Ele32_WPTight_v4 would be multiple versions of the same path\n\nPlease remove the duplicate versions of the following paths\n");
        msg+=errStr;			
        JTextArea textArea = new JTextArea(msg);
        JScrollPane scrollPane = new JScrollPane(textArea);  
        //textArea.setLineWrap(true);  
        //textArea.setWrapStyleWord(true); 
        textArea.setColumns(80);
        textArea.setRows(Math.min(errors.size()+7,50));
        JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);            
        return false;
    }
    return true;

  }
}
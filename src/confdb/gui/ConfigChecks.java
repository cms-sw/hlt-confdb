package confdb.gui;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Iterator;

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
        checkDatasetPaths(config, frame);
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
        Iterator<PrimaryDataset> datasetIt = config.datasetIterator();
        while(datasetIt.hasNext()){
            PrimaryDataset dataset = datasetIt.next();
            if(dataset.datasetPath()!=null){                
                validDatasetPathNames.add(dataset.datasetPath().name());
            }

        }
                
        Iterator<Path> pathIt = config.pathIterator();
        while(pathIt.hasNext()){
            Path path = pathIt.next();
            if(path.isDatasetPath()){
                if(validDatasetPathNames.indexOf(path.name())==-1){
                    errors.add("dataset path "+path.name()+" does not have a corresponding dataset or that dataset is not properly linked to it");
                }
                if(!path.name().startsWith("Dataset_")){
                    errors.add("dataset path "+path.name()+" does not start with Dataset_");
                }
            }else {
                if(validDatasetPathNames.indexOf(path.name())==-1){
                    errors.add("path "+path.name()+" is thought by a dataset to be its dataset path but is not set as a dataset path");
                }            
                if(path.name().startsWith("Dataset_")){
                    errors.add("path "+path.name()+" starts with Dataset_ but is not a dataset path");
                }
            }
        }  
        if(!errors.isEmpty()){
            String errStr = new String();
            for(String error : errors ){
                errStr+="\n"+error;
            }
            String msg = new String("The current config has problems with the dataset paths.\nThis shouldnt really be possible and it would be good to report this to the ConfdbDev channel of the TSG mattermost.\nIt may not be possible to fix this without expert help.");
			msg+=errStr;			
			JTextArea textArea = new JTextArea(msg);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			//textArea.setLineWrap(true);  
			//textArea.setWrapStyleWord(true); 
			textArea.setColumns(80);
			textArea.setRows(Math.min(errors.size(),50));
			JOptionPane.showMessageDialog(frame, scrollPane, "Invalid Config", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
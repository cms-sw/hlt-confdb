package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.data.*;


/**
 * PrimaryDatasetTreeActions
 * -------------------------
 * @author Philipp Schieferdecker
 *
 * Repository of actions which change the primary dataset
 * configuration.
 */
public class PrimaryDatasetTreeActions
{
    public static boolean addDataset(JTree tree,String datasetLabel)
    {
	PrimaryDatasetTreeModel model    = (PrimaryDatasetTreeModel)tree.getModel();
	Configuration           config   = model.getConfiguration();
	TreePath                treePath = tree.getSelectionPath();
	
	PrimaryDataset dataset = config.insertDataset(datasetLabel);
	model.nodeInserted(model.getRoot(),config.datasetCount()-1);

	return true;
    }

    public static boolean removeDataset(JTree tree)
    {
	PrimaryDatasetTreeModel model    = (PrimaryDatasetTreeModel)tree.getModel();
	Configuration           config   = model.getConfiguration();
	TreePath                treePath = tree.getSelectionPath();
	PrimaryDataset  dataset  = (PrimaryDataset)treePath.getLastPathComponent();
	
	int index = config.indexOfDataset(dataset);
	config.removeDataset(dataset);
	model.nodeRemoved(model.getRoot(),index,dataset);

	return true;
    }


    public static boolean removePath(JTree tree)
    {
	PrimaryDatasetTreeModel model    = (PrimaryDatasetTreeModel)tree.getModel();
	Configuration           config   = model.getConfiguration();
	TreePath                treePath = tree.getSelectionPath();
	
	Path           path    = (Path)treePath.getLastPathComponent();
	PrimaryDataset dataset = (PrimaryDataset)treePath.getParentPath()
	    .getLastPathComponent();
	int index = dataset.indexOfPath(path);
	
	dataset.removePath(path);
	model.nodeRemoved(dataset,index,path);
	
	return true;
    }
    
    public static boolean addPath(JTree tree,String cmd)
    {
	PrimaryDatasetTreeModel model    = (PrimaryDatasetTreeModel)tree.getModel();
	Configuration           config   = model.getConfiguration();
	TreePath                treePath = tree.getSelectionPath();
	
	PrimaryDataset dataset = (PrimaryDataset)treePath.getLastPathComponent();
	Path           path    = config.path(cmd);
	dataset.insertPath(path);
	model.nodeInserted(dataset,dataset.pathCount()-1);
	return true;
    }
    
    public static boolean addAllPaths(JTree tree)
    {
	PrimaryDatasetTreeModel model    = (PrimaryDatasetTreeModel)tree.getModel();
	Configuration           config   = model.getConfiguration();
	TreePath                treePath = tree.getSelectionPath();
	
	PrimaryDataset dataset = (PrimaryDataset)treePath.getLastPathComponent();
	Iterator<Path> it = config.pathIterator();
	while (it.hasNext()) {
	    Path path = it.next();
	    if (path.isEndPath()) continue;
	    if (dataset.indexOfPath(path)<0) {
		dataset.insertPath(path);
		model.nodeInserted(dataset,dataset.pathCount()-1);
	    }
	}
	model.nodeChanged(dataset);
	return true;
    }
    
}

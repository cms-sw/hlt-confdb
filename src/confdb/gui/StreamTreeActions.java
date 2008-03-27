package confdb.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.data.*;


/**
 * StreamTreeActions
 * -----------------
 * @author Philipp Schieferdecker
 *
 * Repository of actions which change the stream configuration and at
 * the same time need to be visually represented in the (stream-)
 * JTree.
 */
public class StreamTreeActions
{
    public static boolean addStream(JTree tree, String streamLabel)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	
	Stream newStream = config.insertStream(streamLabel);
	model.nodeInserted(model.getRoot(),config.streamCount()-1);

	return true;
    }

    public static boolean removeStream(JTree tree)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	Stream          stream   = (Stream)treePath.getLastPathComponent();
	
	int index = config.indexOfStream(stream);
	config.removeStream(stream);
	model.nodeRemoved(model.getRoot(),index,stream);

	return true;
    }

    public static boolean removeDataset(JTree tree)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	
	PrimaryDataset dataset = (PrimaryDataset)treePath.getLastPathComponent();
	Stream stream = (Stream)treePath.getParentPath().getLastPathComponent();
	int index = stream.indexOfDataset(dataset);
	
	stream.removeDataset(dataset);
	model.nodeRemoved(stream,index,dataset);
	
	return true;
    }
    
    public static boolean addDataset(JTree tree,String cmd)
    {
	StreamTreeModel model    = (StreamTreeModel)tree.getModel();
	Configuration   config   = model.getConfiguration();
	TreePath        treePath = tree.getSelectionPath();
	
	Stream         stream  = (Stream)treePath.getLastPathComponent();
	PrimaryDataset dataset = config.dataset(cmd);
	stream.insertDataset(dataset);
	model.nodeInserted(stream,stream.datasetCount()-1);
	
	return true;
    }
    
}

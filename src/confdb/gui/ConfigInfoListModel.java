package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import confdb.data.Directory;
import confdb.data.ConfigInfo;


/**
 * ConfigInfoListModel
 * -------------------
 * @author Philipp Schieferdecker
 *
 * Display list of configurations, seeded by a Directory.
 */
public class ConfigInfoListModel extends AbstractListModel
{
    //
    // member data
    //
    
    /** list of ConfigInfos */
    private ArrayList<ConfigInfo> configs = new ArrayList<ConfigInfo>();
    
    //
    // construction
    //

    /** standard constructor */
    public ConfigInfoListModel(Directory dir)
    {
	setDirectory(dir);
    }
    
    //
    // member functions
    //
    
    /** set directory */
    public void setDirectory(Directory dir)
    {
	fireIntervalRemoved(this,0,getSize());
	configs = dir.listAllConfigurations();
	Collections.sort(configs);
	fireIntervalAdded(this,0,getSize());
    }

    /** get the index of a configuration info */
    public int indexOf(ConfigInfo ci) { return configs.indexOf(ci); }
    
    /** ListModel::getSize() */
    public int getSize() { return configs.size(); }

    /** ListModel::getElementAt() */
    public Object getElementAt(int i) {	return configs.get(i); }
    
}

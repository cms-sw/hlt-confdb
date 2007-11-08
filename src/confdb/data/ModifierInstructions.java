package confdb.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;


/**
 * ModifierInstructions
 * --------------------
 * @author Philipp Schieferdecker
 *
 * Instructions for the ConfigurationModifier how to filter/manipulate its
 * master configuration. 
 */
public class ModifierInstructions
{
    //
    // member data
    //

    /** global PSets */
    private boolean filterAllPSets = false;
    private ArrayList<String> psetBlackList = new ArrayList<String>();
    private ArrayList<String> psetWhiteList = new ArrayList<String>();
    
    /** EDSources */
    private boolean filterAllEDSources = false;
    private ArrayList<String> edsourceBlackList = new ArrayList<String>();
    private ArrayList<String> edsourceWhiteList = new ArrayList<String>();

    /** ESSources */
    private boolean filterAllESSources = false;
    private ArrayList<String> essourceBlackList = new ArrayList<String>();
    private ArrayList<String> essourceWhiteList = new ArrayList<String>();

    /** ESModules */
    private boolean filterAllESModules = false;
    private ArrayList<String> esmoduleBlackList = new ArrayList<String>();
    private ArrayList<String> esmoduleWhiteList = new ArrayList<String>();
    
    /** Services */
    private boolean filterAllServices = false;
    private ArrayList<String> serviceBlackList = new ArrayList<String>();
    private ArrayList<String> serviceWhiteList = new ArrayList<String>();
    
    /** Paths */
    private boolean filterAllPaths = false;
    private boolean filterAllOutputModules = false;
    private ArrayList<String> pathBlackList = new ArrayList<String>();
    private ArrayList<String> pathWhiteList = new ArrayList<String>();

    /** sequences requested regardless of being referenced in requested paths */
    private ArrayList<String> requestedSequences = new ArrayList<String>();

    /** modules reqested regardless of being referenced in requested path */
    private ArrayList<String> requestedModules = new ArrayList<String>();
    
    /** template for the EDSource to be substituted (if any) */
    private EDSourceTemplate edsourceT = null;
    
    /** template for the OutputModule to be substituted (if any) */
    private ModuleTemplate   outputT = null;

    
    //
    // construction
    //

    /** standard constructor */
    public ModifierInstructions() {}
    
    
    //
    // member functions
    //

    /** interpret arguments stored in a HashMap */
    public void interpretArgs(HashMap<String,String> args) throws DataException
    {
	String value;
	
	value = args.remove("nopsets");
	if (value!=null) filterAllPSets();
	value = args.remove("noedsources");
	if (value!=null) {
	    filterAllEDSources();
	    args.remove("input");
	}
	value = args.remove("noes");
	if (value!=null) {
	    filterAllESSources();
	    filterAllESModules();
	}
	else {
	    value = args.remove("noessources");
	    if (value!=null) filterAllESSources();
	    value = args.remove("noesmodules");
	    if (value!=null) filterAllESModules();
	}
	value = args.remove("noservices");
	if (value!=null) filterAllServices();
	value = args.remove("nopaths");
	if (value!=null) filterAllPaths();
	value = args.remove("nooutput");
	if (value!=null) {
	    filterAllOutputModules();
	    args.remove("output");
	}

	value = args.remove("psets");
	if (value!=null) {
	    String[] psetNames = value.split(",");
	    for (String s : psetNames) {
		if (s.startsWith("-")) insertPSetIntoBlackList(s.substring(1));
		else insertPSetIntoWhiteList(s);
	    }
	}
	
	value = args.remove("edsources");
	if (value!=null) {
	    String[] edsourceNames = value.split(",");
	    for (String s : edsourceNames) {
		if (s.startsWith("-")) insertEDSourceIntoBlackList(s.substring(1));
		else insertEDSourceIntoWhiteList(s);
	    }
	}
	
	value = args.remove("essources");
	if (value!=null) {
	    String[] essourceNames = value.split(",");
	    for (String s : essourceNames) {
		if (s.startsWith("-")) insertESSourceIntoBlackList(s.substring(1));
		else insertESSourceIntoWhiteList(s);
	    }
	}
	
	value = args.remove("esmodules");
	if (value!=null) {
	    String[] esmoduleNames = value.split(",");
	    for (String s : esmoduleNames) {
		if (s.startsWith("-")) insertESModuleIntoBlackList(s.substring(1));
		else insertESModuleIntoWhiteList(s);
	    }
	}
	
	value = args.remove("services");
	if (value!=null) {
	    String[] serviceNames = value.split(",");
	    for (String s : serviceNames) {
		if (s.startsWith("-")) insertServiceIntoBlackList(s.substring(1));
		else insertServiceIntoWhiteList(s);
	    }
	}
	
	value = args.remove("paths");
	if (value!=null) {
	    String[] pathNames = value.split(",");
	    for (String s : pathNames) {
		if (s.startsWith("-")) insertPathIntoBlackList(s.substring(1));
		else insertPathIntoWhiteList(s);
	    }
	}
	
	value = args.remove("sequences");
	if (value!=null) {
	    String[] sequenceNames = value.split(",");
	    for (String s : sequenceNames) {
		if (s.startsWith("-"))
		    throw new DataException("ModifierInstructions.interpretArgs"+
					    " ERROR: sequences can *not* be "+
					    "blacklisted!");
		else requestSequence(s);
	    }
	}
	
	value = args.remove("modules");
	if (value!=null) {
	    String[] moduleNames = value.split(",");
	    for (String s : moduleNames) {
		if (s.startsWith("-"))
		    throw new DataException("ModifierInstructions.interpretArgs"+
					    " ERROR: modules can *not* be "+
					    "blacklisted!");
		else requestModule(s);
	    }
	}

	value = args.remove("input");
	if (value!=null) {
	    if (value.indexOf(",")<0&&value.endsWith(".list")) {
		BufferedReader inputStream = null;
		String listFileName = value;
		try {
		    inputStream=new BufferedReader(new FileReader(listFileName));
		    value="";
		    String fileName;
		    while ((fileName = inputStream.readLine()) != null) {
			if (value.length()>0) value+=",";
			value+=fileName;
		    }
		}
		catch (IOException e) {
		    System.out.println("Error parsing '"+listFileName+"':"
				       +e.getMessage());
		}
		finally {
		    if (inputStream != null) 
			try { inputStream.close(); } catch (IOException e) {}
		}
	    }
	    insertPoolSource(value);
	}
	
	value = args.remove("output");
	if (value!=null) {
	    insertPoolOutputModule(value);
	}
	
	if (args.size()>0)
	    throw new DataException("ModifierInstructions.interpretArgs ERROR: "+
				    "invalid arguments detected.");
    }

    /** resolve white-lists based on a given configuration */
    public boolean resolve(IConfiguration config)
    {
	if (!filterAllPSets&&psetWhiteList.size()>0) {
	    if (psetBlackList.size()>0) {
		System.err.println("ModifierInstructions.resolve ERROR: " +
				   "white&black lists provided for global psets.");
		return false;
	    }
	    else {
		Iterator it = config.psetIterator();
		while (it.hasNext()) {
		    PSetParameter pset = (PSetParameter)it.next();
		    if (!psetWhiteList.contains(pset.name()))
			psetBlackList.add(pset.name());
		}
	    }
	}
	
	if (!filterAllEDSources&&edsourceWhiteList.size()>0) {
	    if (edsourceBlackList.size()>0) {
		System.err.println("ModifierInstructions.resolve ERROR: " +
				   "white&black lists provided for edsources.");
		return false;
	    }
	    else {
		Iterator it = config.edsourceIterator();
		while (it.hasNext()) {
		    EDSourceInstance edsource = (EDSourceInstance)it.next();
		    if (!edsourceWhiteList.contains(edsource.name()))
			edsourceBlackList.add(edsource.name());
		}
	    }
	}
	
	if (!filterAllESSources&&essourceWhiteList.size()>0) {
	    if (essourceBlackList.size()>0) {
		System.err.println("ModifierInstructions.resolve ERROR: " +
				   "white&black lists provided for essources.");
		return false;
	    }
	    else {
		Iterator it = config.essourceIterator();
		while (it.hasNext()) {
		    ESSourceInstance essource = (ESSourceInstance)it.next();
		    if (!essourceWhiteList.contains(essource.name()))
			essourceBlackList.add(essource.name());
		}
	    }
	}
	
	if (!filterAllESModules&&esmoduleWhiteList.size()>0) {
	    if (esmoduleBlackList.size()>0) {
		System.err.println("ModifierInstructions.resolve ERROR: " +
				   "white&black lists provided for esmodules.");
		return false;
	    }
	    else {
		Iterator it = config.esmoduleIterator();
		while (it.hasNext()) {
		    ESModuleInstance esmodule = (ESModuleInstance)it.next();
		    if (!esmoduleWhiteList.contains(esmodule.name()))
			esmoduleBlackList.add(esmodule.name());
		}
	    }
	}
	
	if (!filterAllServices&&serviceWhiteList.size()>0) {
	    if (serviceBlackList.size()>0) {
		System.err.println("ModifierInstructions.resolve ERROR: " +
				   "white&black lists provided for services.");
		return false;
	    }
	    else {
		Iterator it = config.serviceIterator();
		while (it.hasNext()) {
		    ServiceInstance service = (ServiceInstance)it.next();
		    if (!serviceWhiteList.contains(service.name()))
			serviceBlackList.add(service.name());
		}
	    }
	}
	
	if (filterAllOutputModules&&pathWhiteList.size()==0) {
	    Iterator it = config.pathIterator();
	    while (it.hasNext()) {
		Path path = (Path)it.next();
		if (path.hasOutputModule()) insertPathIntoBlackList(path.name());
	    }
	}
	
	if (!filterAllPaths&&pathWhiteList.size()>0) {
	    if (pathBlackList.size()>0) {
		System.err.println("ModifierInstructions.resolve ERROR: " +
				   "white&black lists provided for paths.");
		return false;
	    }
	    else {
		Iterator it = config.pathIterator();
		while (it.hasNext()) {
		    Path path = (Path)it.next();
		    if (!pathWhiteList.contains(path.name()))
			pathBlackList.add(path.name());
		}
	    }
	}
	
	return true;
    }

    /** check filter flags */
    public boolean doFilterAllPSets()         { return filterAllPSets; }
    public boolean doFilterAllEDSources()     { return filterAllEDSources; }
    public boolean doFilterAllESSources()     { return filterAllESSources; }
    public boolean doFilterAllESModules()     { return filterAllESModules; }
    public boolean doFilterAllServices()      { return filterAllServices; }
    public boolean doFilterAllPaths()         { return filterAllPaths; }
    public boolean doFilterAllOutputModules() { return filterAllOutputModules; }
    
    /** check if passed object is in a blacklist */
    public boolean isInBlackList(Object obj)
    {
	if (obj instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)obj;
	    return psetBlackList.contains(pset.name());
	}
	else if (obj instanceof EDSourceInstance) {
	    EDSourceInstance edsource = (EDSourceInstance)obj;
	    return edsourceBlackList.contains(edsource.name());
	}
	else if (obj instanceof ESSourceInstance) {
	    ESSourceInstance essource = (ESSourceInstance)obj;
	    return essourceBlackList.contains(essource.name());
	}
	else if (obj instanceof ESModuleInstance) {
	    ESModuleInstance esmodule = (ESModuleInstance)obj;
	    return esmoduleBlackList.contains(esmodule.name());
	}
	else if (obj instanceof ServiceInstance) {
	    ServiceInstance edsource = (ServiceInstance)obj;
	    return edsourceBlackList.contains(edsource.name());
	}
	else if (obj instanceof Path) {
	    Path path = (Path)obj;
	    return pathBlackList.contains(path.name());
	}
	return false;
    }

    /** check if an EDSource is to be added */
    public boolean doInsertEDSource() { return (edsourceT!=null); }
    
    /** retrieve the EDSource to be added */
    public EDSourceInstance edsourceToBeAdded()
    {
	if (edsourceT==null) return null;
	edsourceT.removeAllInstances();
	EDSourceInstance result = null;
	try {
	    result = (EDSourceInstance)edsourceT.instance();
	}
	catch (DataException e) {
	    System.err.println(e.getMessage());
	}
	return result;
    }

    /** check if an OutputModule is to be added */
    public boolean doInsertOutputModule() { return (outputT!=null); }

    /** *retrieve the OutputModule template to be added */
    public ModuleInstance outputModuleToBeAdded(String name)
    {
	if (outputT==null) return null;
	ModuleInstance result = null;
	try {
	    result = (ModuleInstance)outputT.instance(name);
	}
	catch (DataException e) {
	    System.err.println(e.getMessage());
	}
	return result;
    }

    /** get iterator for requested sequences */
    public Iterator requestedSequenceIterator()
    {
	return requestedSequences.iterator();
    }

    /** get iterator for requested modules */
    public Iterator requestedModuleIterator()
    {
	return requestedModules.iterator();
    }
    
    /** filter all plugins of a certain type */
    public void filterAllPSets()         { filterAllPSets         = true; }
    public void filterAllEDSources()     { filterAllEDSources     = true; }
    public void filterAllESSources()     { filterAllESSources     = true; }
    public void filterAllESModules()     { filterAllESModules     = true; }
    public void filterAllServices()      { filterAllServices      = true; }
    public void filterAllPaths()         { filterAllPaths         = true; }
    public void filterAllOutputModules() { filterAllOutputModules = true; }
    
    /** insert components into the corresponding whitelist/blacklist */
    public void insertPSetIntoBlackList(String psetName)
    {
	psetBlackList.add(psetName);
    }
    public void insertPSetIntoWhiteList(String psetName)
    {
	psetWhiteList.add(psetName);
    }
    public void insertEDSourceIntoBlackList(String edsourceName)
    {
	edsourceBlackList.add(edsourceName);
    }
    public void insertEDSourceIntoWhiteList(String edsourceName)
    {
	edsourceWhiteList.add(edsourceName);
    }
    public void insertESSourceIntoBlackList(String essourceName)
    {
	essourceBlackList.add(essourceName);
    }
    public void insertESSourceIntoWhiteList(String essourceName)
    {
	essourceWhiteList.add(essourceName);
    }
    public void insertESModuleIntoBlackList(String esmoduleName)
    {
	esmoduleBlackList.add(esmoduleName);
    }
    public void insertESModuleIntoWhiteList(String esmoduleName)
    {
	esmoduleWhiteList.add(esmoduleName);
    }
    public void insertServiceIntoBlackList(String serviceName)
    {
	serviceBlackList.add(serviceName);
    }
    public void insertServiceIntoWhiteList(String serviceName)
    {
	serviceWhiteList.add(serviceName);
    }
    public void insertPathIntoBlackList(String pathName)
    {
	pathBlackList.add(pathName);
    }
    public void insertPathIntoWhiteList(String pathName)
    {
	pathWhiteList.add(pathName);
    }
    
    /** request a sequence regardless of it being referenced in path */
    public void requestSequence(String sequenceName)
    {
	requestedSequences.add(sequenceName);
    }

    /** request a module regardless of it being referenced in path */
    public void requestModule(String moduleName)
    {
	requestedModules.add(moduleName);
    }
    
    /** insert a DaqSource */
    public void insertDaqSource()
    {
	filterAllEDSources = true;
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	params.add(new StringParameter("readerPluginName","FUShmReader",false,false));
	edsourceT = new EDSourceTemplate("DaqSource","UNKNOWN",-1,params);
    }
    
    /** insert PoolSource [fileNames = comma-separated list!] */
    public void insertPoolSource(String fileNames)
    {
	filterAllEDSources = true;
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	params.add(new VStringParameter("fileNames",fileNames,false,false));
	edsourceT = new EDSourceTemplate("PoolSource","UNKNOWN",-1,params);
    }
    
    /** insert/replace FUShmStreamConsumer */
    public void insertShmStreamConsumer()
    {
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	params.add(new PSetParameter("SelectEvents","",false,true));
	params.add(new VStringParameter("outputCommands","",false,true));
	outputT = new ModuleTemplate("ShmStreamConsumer",
				     "UNKNOWN",-1,params,"OutputModule");
    }

    /** insert/replace PoolOutputModule */
    public void insertPoolOutputModule(String fileName)
    {
	ArrayList<Parameter> params = new ArrayList<Parameter>();
	params.add(new StringParameter("fileName",fileName,false,true));
	params.add(new PSetParameter("SelectEvents","",false,true));
	params.add(new VStringParameter("outputCommands","",false,true));
	outputT = new ModuleTemplate("PoolOutputModule",
				     "UNKNOWN",-1,params,"OutputModule");
    }
    
}

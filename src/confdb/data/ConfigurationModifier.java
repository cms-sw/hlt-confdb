package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * ConfigurationModifier
 * ---------------------
 * @author Philipp Schieferdecker
 *
 * retrieve only selected parts of a configuraiton,
 * after applying configurable filter decisions.
 */
public class ConfigurationModifier implements IConfiguration
{
    //
    // member data
    //

    /** the master configuration */
    private IConfiguration master = null;

    /** flag indicating if the filter was applied */
    private boolean isModified = false;

    /** filtered components */
    private ArrayList<PSetParameter>    psets    = new ArrayList<PSetParameter>();
    private ArrayList<EDSourceInstance> edsources= new ArrayList<EDSourceInstance>();
    private ArrayList<ESSourceInstance> essources= new ArrayList<ESSourceInstance>();
    private ArrayList<ESModuleInstance> esmodules= new ArrayList<ESModuleInstance>();
    private ArrayList<ServiceInstance>  services = new ArrayList<ServiceInstance>();
    private ArrayList<ModuleInstance>   modules  = new ArrayList<ModuleInstance>();
    private ArrayList<Path>             paths    = new ArrayList<Path>();
    private ArrayList<Sequence>         sequences= new ArrayList<Sequence>();
    private ArrayList<Stream>           streams  = new ArrayList<Stream>();
    
    /** global pset filter settings */
    private boolean           filterAllPSets     = false;
    private ArrayList<String> filteredPSets      = new ArrayList<String>();
    
    /** EDSource filter settings */
    private boolean           filterAllEDSources = false;
    private ArrayList<String> filteredEDSources  = new ArrayList<String>();
    private EDSourceInstance  addedEDSource      = null;

    /** ESSource filter settings */
    private boolean           filterAllESSources = false;
    private ArrayList<String> filteredESSources  = new ArrayList<String>();
    
    /** ESModule filter settings */
    private boolean           filterAllESModules = false;
    private ArrayList<String> filteredESModules  = new ArrayList<String>();
    
    /** Service filter settings */
    private boolean           filterAllServices  = false;
    private ArrayList<String> filteredServices   = new ArrayList<String>();

    /** Path filter settings */
    private boolean           filterAllPaths     = false;
    private ArrayList<String> filteredPaths      = new ArrayList<String>();
    private ArrayList<Path>   addedPaths         = new ArrayList<Path>();

    /** Sequences requested, regardless of being referenced */
    private ArrayList<String> requestedSequences = new ArrayList<String>();
    
    /** Modules requested, regardless of being referenced */
    private ArrayList<String> requestedModules   = new ArrayList<String>();
    
    
    //
    // construction
    //

    /** standard constructor */
    public ConfigurationModifier(IConfiguration config)
    {
	this.master = config;
    }

    //
    // member functions
    //
    
    /** choose to filter all global psets */
    public void filterAllPSets() { filterAllPSets = true; }
    
    /** choose to filter all edsources */
    public void filterAllEDSources() { filterAllEDSources = true; }
    
    /** choose to filter all essource */
    public void filterAllESSources() { filterAllESSources = true; }
    
    /** choose to filter all esmodule */
    public void filterAllESModules() { filterAllESModules = true; }

    /** choose to filter all esmodule */
    public void filterAllServices() { filterAllServices = true; }
    
    /** choose to filter all paths */
    public void filterAllPaths() { filterAllPaths = true; }

    /** request a sequence, regardless of it being referenced in a path */
    public boolean requestSequence(String sequenceName)
    {
	Sequence sequence = master.sequence(sequenceName);
	if (sequence==null) return false;
	if (requestedSequences.indexOf(sequenceName)<0)
	    requestedSequences.add(sequenceName);
	return true;
    }

    /** request a module, regardless of it being referenced in a path */
    public boolean requestModule(String moduleName)
    {
	ModuleInstance module = master.module(moduleName);
	if (module==null) return false;
	if (requestedModules.indexOf(moduleName)<0)
	    requestedModules.add(moduleName);
	return true;
    }

    /** replace the current EDSource */
    public void replaceEDSource(EDSourceInstance edsource)
    {
	filterAllEDSources = true;
	addedEDSource = edsource;
    }

    /** replace the current OutputModule */
    public boolean replaceOutputModule(ModuleInstance module)
    {
	int            count          = 0;
	Path           replacedPath   = null;
	
	Iterator it = master.pathIterator();
	while (it.hasNext()) {
	    Path path = (Path)it.next();
	    if (path.hasOutputModule()) {
		count++;
		if (replacedPath==null) replacedPath = path;
	    }
	}
	
	if (count==0) {
	    System.err.println("replaceOutputModule ERROR: "+
			       "no OutputModule found to replace!");
	    return false;
	}
	if (count>1) {
	    System.err.println("replaceOutputModule ERROR: "+
			       "found more than 1 OutputModule!");
	    return false;
	}
	
	Path path = new Path(replacedPath.name());
	Iterator itM = replacedPath.moduleIterator();
	while (itM.hasNext()) {
	    ModuleInstance m = (ModuleInstance)itM.next();
	    if (m.template().type().equals("OutputModule"))
		module.createReference(path,path.entryCount());
	    else
		m.createReference(path,path.entryCount());
	}
	
	filteredPaths.add(replacedPath.name());
	addedPaths.add(path);
	
	return true;
    }
    
    /** apply modifications */
    public void modify()
    {
	psets.clear();
	edsources.clear();
	essources.clear();
	esmodules.clear();
	services.clear();
	modules.clear();
	paths.clear();
	sequences.clear();
	streams.clear();
	
	if (!filterAllPSets) {
	    Iterator it=master.psetIterator();
	    while (it.hasNext()) {
		PSetParameter pset = (PSetParameter)it.next();
		if (filteredPSets.indexOf(pset.name())<0)
		    psets.add(pset);
	    }
	}

	if (!filterAllEDSources) {
	    Iterator it=master.edsourceIterator();
	    while (it.hasNext()) {
		EDSourceInstance edsource = (EDSourceInstance)it.next();
		if (filteredEDSources.indexOf(edsource.name())<0)
		    edsources.add(edsource);
	    }
	}
	if (addedEDSource!=null) edsources.add(addedEDSource);
	
	if (!filterAllESSources) {
	    Iterator it=master.essourceIterator();
	    while (it.hasNext()) {
		ESSourceInstance essource = (ESSourceInstance)it.next();
		if (filteredESSources.indexOf(essource.name())<0)
		    essources.add(essource);
	    }
	}

	if (!filterAllESModules) {
	    Iterator it=master.esmoduleIterator();
	    while (it.hasNext()) {
		ESModuleInstance esmodule = (ESModuleInstance)it.next();
		if (filteredESModules.indexOf(esmodule.name())<0)
		    esmodules.add(esmodule);
	    }
	}

	if (!filterAllServices) {
	    Iterator it=master.serviceIterator();
	    while (it.hasNext()) {
		ServiceInstance service = (ServiceInstance)it.next();
		if (filteredServices.indexOf(service.name())<0)
		    services.add(service);
	    }
	}

	if (!filterAllPaths) {
	    Iterator it=master.pathIterator();
	    while (it.hasNext()) {
		Path path = (Path)it.next();
		if (filteredPaths.indexOf(path.name())<0) {
		    paths.add(path);
		    Iterator itS = path.sequenceIterator();
		    while (itS.hasNext()) {
			Sequence sequence = (Sequence)itS.next();
			if (sequences.indexOf(sequence)<0)
			    sequences.add(sequence);
		    }
		    Iterator itM = path.moduleIterator();
		    while (itM.hasNext()) {
			ModuleInstance module = (ModuleInstance)itM.next();
			if (modules.indexOf(module)<0)
			    modules.add(module);
		    }
		}
	    }
	}
	Iterator itP = addedPaths.iterator();
	while (itP.hasNext()) {
	    Path path = (Path)itP.next();
	    paths.add(path);
	    Iterator itS = path.sequenceIterator();
	    while (itS.hasNext()) {
		Sequence sequence = (Sequence)itS.next();
		if (sequences.indexOf(sequence)<0)
		    sequences.add(sequence);
	    }
	    Iterator itM = path.moduleIterator();
	    while (itM.hasNext()) {
		ModuleInstance module = (ModuleInstance)itM.next();
		if (modules.indexOf(module)<0)
		    modules.add(module);
	    }
	}
	
	Iterator itS = requestedSequences.iterator();
	while (itS.hasNext()) {
	    String   sequenceName = (String)itS.next();
	    Sequence sequence     = master.sequence(sequenceName);
	    if (sequence!=null&&sequences.indexOf(sequence)<0)
		sequences.add(sequence);
	}
	
	Iterator itM = requestedModules.iterator();
	while (itM.hasNext()) {
	    String         moduleLabel = (String)itM.next();
	    ModuleInstance module      = master.module(moduleLabel);
	    if (module!=null&&modules.indexOf(module)<0)
		modules.add(module);
	}
	
	isModified = true;
    }

    /** reset all modifications */
    public void reset()
    {
	filterAllPSets     = false;
	filterAllEDSources = false;
	filterAllESSources = false;
	filterAllServices  = false;
	filterAllPaths     = false;
	
	filteredPSets.clear();
	filteredEDSources.clear();
	filteredESSources.clear();
	filteredESModules.clear();
	filteredServices.clear();
	filteredPaths.clear();
	
	requestedSequences.clear();
	requestedModules.clear();
	
	psets.clear();
	edsources.clear();
	essources.clear();
	esmodules.clear();
	services.clear();
	modules.clear();
	paths.clear();
	sequences.clear();
	streams.clear();

	isModified = false;
    }

    
    //
    // IConfiguration interface
    //
    
    /** name of the configuration */
    public String name() { return master.name(); }

    /** parent directory of the configuration */
    public Directory parentDir() { return master.parentDir(); }

    /** version of the configuration */
    public int version() { return master.version(); }
    
    /** get configuration date of creation as a string */
    public String created() { return master.created(); }
	
    /** get configuration creator */
    public String creator() { return master.creator(); }
    
    /** release tag */
    public String releaseTag() { return master.releaseTag(); }

    /** process name */
    public String processName() { return master.processName(); }

    
    /** check if the configuration is empty */
    public boolean isEmpty() { return false; }

    /** check if the provided string is already in use as a label */
    public boolean isUniqueQualifier(String qualifier)
    {
	return master.isUniqueQualifier(qualifier);
    }

    
    /** total number of unset tracked parameters */
    public int unsetTrackedParameterCount() {
	int result = 0;
	result += unsetTrackedPSetParameterCount();
	result += unsetTrackedEDSourceParameterCount();
	result += unsetTrackedESSourceParameterCount();
	result += unsetTrackedESModuleParameterCount();
	result += unsetTrackedServiceParameterCount();
	result += unsetTrackedModuleParameterCount();
	return result;
    }
    
    /** number of unsert tracked global pset parameters */
    public int unsetTrackedPSetParameterCount()
    {
	if (!isModified) return master.unsetTrackedPSetParameterCount();
	int result = 0;
	for (PSetParameter pset : psets)
	    result += pset.unsetTrackedParameterCount();
	return result;
    }
    
    /** number of unsert tracked edsource parameters */
    public int unsetTrackedEDSourceParameterCount()
    {
	if (!isModified) return master.unsetTrackedEDSourceParameterCount();
	int result = 0;
	for (EDSourceInstance eds : edsources)
	    result+=eds.unsetTrackedParameterCount();
	return result;
    }

    /** number of unsert tracked essource parameters */
    public int unsetTrackedESSourceParameterCount()
    {
	if (!isModified) return master.unsetTrackedESSourceParameterCount();
	int result = 0;
	for (ESSourceInstance ess : essources)
	    result+=ess.unsetTrackedParameterCount();
	return result;
    }

    /** number of unsert tracked esmodule parameters */
    public int unsetTrackedESModuleParameterCount()
    {
	if (!isModified) return master.unsetTrackedESModuleParameterCount();
	int result = 0;
	for (ESModuleInstance esm : esmodules)
	    result+=esm.unsetTrackedParameterCount();
	return result;
    }

    /** number of unsert tracked service parameters */
    public int unsetTrackedServiceParameterCount()
    {
	if (!isModified) return master.unsetTrackedServiceParameterCount();
	int result = 0;
	for (ServiceInstance svc : services)
	    result+=svc.unsetTrackedParameterCount();
	return result;
    }

    /** number of unsert tracked module parameters */
    public int unsetTrackedModuleParameterCount()
    {
	if (!isModified) return master.unsetTrackedModuleParameterCount();
	int result = 0;
	for (ModuleInstance mod : modules)
	    result+=mod.unsetTrackedParameterCount();
	return result;
    }

    /** number of paths unassigned to any stream */
    public int pathNotAssignedToStreamCount()
    {
	if (!isModified) return master.pathNotAssignedToStreamCount();
	int result = 0;
	if (streams.size()==0) return result;
	for (Path p : paths) if (p.streamCount()==0) result++;
	return result;
    }
    
    /**  number of global PSets */
    public int psetCount()
    {
	return (isModified) ? psets.size() : master.psetCount();
    }

    /** get i-th global PSet */
    public PSetParameter pset(int i)
    {
	return (isModified) ? psets.get(i) : master.pset(i);
    }
    
    /** index of a certain global PSet */
    public int indexOfPSet(PSetParameter pset)
    {
	return (isModified) ? psets.indexOf(pset) : master.indexOfPSet(pset);
    }

    /** retrieve pset iterator */
    public Iterator psetIterator()
    {
	return (isModified) ? psets.iterator() : master.psetIterator();
    }
    
    
    /**  number of EDSources */
    public int edsourceCount()
    {
	return (isModified) ? edsources.size() : master.edsourceCount();
    }

    /** get i-th EDSource */
    public EDSourceInstance edsource(int i)
    {
	return (isModified) ? edsources.get(i) : master.edsource(i);
    }

    /** index of a certain EDSource */
    public int indexOfEDSource(EDSourceInstance edsource)
    {
	return (isModified) ?
	    edsources.indexOf(edsource) : master.indexOfEDSource(edsource);
    }
	
    /** retrieve edsource iterator */
    public Iterator edsourceIterator()
    {
	return (isModified) ? edsources.iterator() : master.edsourceIterator();
    }

    
    /**  number of ESSources */
    public int essourceCount()
    {
	return (isModified) ? essources.size() : master.essourceCount();
    }
    
    /** get i-th ESSource */
    public ESSourceInstance essource(int i)
    {
	return (isModified) ? essources.get(i) : master.essource(i);
    }

    /** index of a certain ESSource */
    public int indexOfESSource(ESSourceInstance essource)
    {
	return (isModified) ?
	    essources.indexOf(essource) : master.indexOfESSource(essource);
    }

    /** retrieve essource iterator */
    public Iterator essourceIterator()
    {
	return (isModified) ? essources.iterator() : master.essourceIterator();
    }
    
    
    /**  number of ESModules */
    public int esmoduleCount()
    {
	return (isModified) ? esmodules.size() : master.esmoduleCount();
    }
    
    /** get i-th ESModule */
    public ESModuleInstance esmodule(int i)
    {
	return (isModified) ? esmodules.get(i) : master.esmodule(i);
    }

    /** index of a certain ESSource */
    public int indexOfESModule(ESModuleInstance esmodule)
    {
	return (isModified) ?
	    esmodules.indexOf(esmodule) : master.indexOfESModule(esmodule);
    }
    
    /** retrieve esmodule iterator */
    public Iterator esmoduleIterator()
    {
	return (isModified) ? esmodules.iterator() : master.esmoduleIterator();
    }

    
    /**  number of Services */
    public int serviceCount()
    {
	return (isModified) ? services.size() : master.serviceCount();
    }

    /** get i-th Service */
    public ServiceInstance service(int i)
    {
	return (isModified) ? services.get(i) : master.service(i);
    }

    /** index of a certain Service */
    public int indexOfService(ServiceInstance service)
    {
	return (isModified) ?
	    services.indexOf(service) : master.indexOfService(service);
    }
    
    /** retrieve service iterator */
    public Iterator serviceIterator()
    {
	return (isModified) ? services.iterator() : master.serviceIterator();
    }
    
    
    /**  number of Modules */
    public int moduleCount()
    {
	return (isModified) ? modules.size() : master.moduleCount();
    }

    /** get i-th Module */
    public ModuleInstance module(int i)
    {
	return (isModified) ? modules.get(i) : master.module(i);
    }
    
    /** get Module by name */
    public ModuleInstance module(String moduleName)
    {
	return master.module(moduleName);
    }

    /** index of a certain Module */
    public int indexOfModule(ModuleInstance module)
    {
	return (isModified) ?
	    modules.indexOf(module) : master.indexOfModule(module);
    }
    
    /** retrieve module iterator */
    public Iterator moduleIterator()
    {
	return (isModified) ? modules.iterator() : master.moduleIterator();
    }
    

    /** number of Paths */
    public int pathCount()
    {
	return (isModified) ? paths.size() : master.pathCount();
    }
    
    /** get i-th Path */
    public Path path(int i)
    {
	return (isModified) ? paths.get(i) : master.path(i);
    }

    /** geth Path by name */
    public Path path(String pathName)
    {
	return master.path(pathName);
    }

    /** index of a certain Path */
    public int indexOfPath(Path path)
    {
	return (isModified) ? paths.indexOf(path) : master.indexOfPath(path);
    }
    
    /** retrieve path iterator */
    public Iterator pathIterator()
    {
	return (isModified) ? paths.iterator() : master.pathIterator();
    }

    
    /** number of Sequences */
    public int sequenceCount()
    {
	return (isModified) ? sequences.size() : master.sequenceCount();
    }
    
    /** get i-th Sequence */
    public Sequence sequence(int i)
    {
	return (isModified) ? sequences.get(i) : master.sequence(i);
    }

    /** get Sequence by name */
    public Sequence sequence(String sequenceName)
    {
	return master.sequence(sequenceName);
    }

    /** index of a certain Sequence */
    public int indexOfSequence(Sequence sequence)
    {
	return (isModified) ?
	    sequences.indexOf(sequence) : master.indexOfSequence(sequence);
    }

    /** retrieve sequence iterator */
    public Iterator sequenceIterator()
    {
	return (isModified) ? sequences.iterator() : master.sequenceIterator();
    }
    
    
    /** number of streams */
    public int streamCount()
    {
	return (isModified) ? streams.size() : master.streamCount();
    }
    
    /** retrieve i-th stream */
    public Stream stream(int i)
    {
	return (isModified) ? streams.get(i) : master.stream(i);
    }
    
    /** index of a certain stream */
    public int indexOfStream(Stream stream)
    {
	return (isModified) ?
	    streams.indexOf(stream) : master.indexOfStream(stream);
    }
    
    /** retrieve stream iterator */
    public Iterator streamIterator()
    {
	return (isModified) ? streams.iterator() : master.streamIterator();
    }
    
}

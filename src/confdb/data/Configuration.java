package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import java.util.StringTokenizer;

/**
 * Configuration -------------
 * 
 * @author Philipp Schieferdecker
 *
 *         Description of a CMSSW job configuration.
 */
public class Configuration implements IConfiguration {
	//
	// member data
	//
	/** configuration information */
	private ConfigInfo configInfo = null;

	/** current software release */
	private SoftwareRelease release = null;

	/** has the configuration changed since the last 'save' operation? */
	private boolean hasChanged = false;

	/** list of global parameter sets */
	private GlobalPSetContainer psets = null;

	/** list of EDSources */
	private ArrayList<EDSourceInstance> edsources = null;

	/** list of ESSources */
	private ArrayList<ESSourceInstance> essources = null;

	/** list of ESModules */
	private ArrayList<ESModuleInstance> esmodules = null;

	/** list of Services */
	private ArrayList<ServiceInstance> services = null;

	/** list of Modules */
	private ArrayList<ModuleInstance> modules = null;
	
	/** list of EDAliases */
	private ArrayList<EDAliasInstance> edaliases = null;
	
	/** list of SwitchProducers */
	private ArrayList<SwitchProducer> switchproducers = null;

	/** list of Paths */
	private ArrayList<Path> paths = null;

	/** list of Sequences */
	private ArrayList<Sequence> sequences = null;

	/** list of Tasks */
	private ArrayList<Task> tasks = null;

	/** list of EventContents */
	private ArrayList<EventContent> contents = null;

	/** list of blocks (always empty for Configuration!) */
	private ArrayList<Block> blocks = new ArrayList<Block>();

	//
	// construction
	//

	/** empty constructor */
	public Configuration() {
		psets = new GlobalPSetContainer();
		edsources = new ArrayList<EDSourceInstance>();
		essources = new ArrayList<ESSourceInstance>();
		esmodules = new ArrayList<ESModuleInstance>();
		services = new ArrayList<ServiceInstance>();
		modules = new ArrayList<ModuleInstance>();
		edaliases = new ArrayList<EDAliasInstance>();
		switchproducers = new ArrayList<SwitchProducer>();
		paths = new ArrayList<Path>();
		sequences = new ArrayList<Sequence>();
		tasks = new ArrayList<Task>();
		contents = new ArrayList<EventContent>();
	}

	/** standard constructor */
	public Configuration(ConfigInfo configInfo, SoftwareRelease release) {
		psets = new GlobalPSetContainer();
		edsources = new ArrayList<EDSourceInstance>();
		essources = new ArrayList<ESSourceInstance>();
		esmodules = new ArrayList<ESModuleInstance>();
		services = new ArrayList<ServiceInstance>();
		modules = new ArrayList<ModuleInstance>();
		edaliases = new ArrayList<EDAliasInstance>();
		switchproducers = new ArrayList<SwitchProducer>();
		paths = new ArrayList<Path>();
		sequences = new ArrayList<Sequence>();
		tasks = new ArrayList<Task>();
		contents = new ArrayList<EventContent>();

		initialize(configInfo, release);
	}

	//
	// public member functions
	//

	/** new configuration */
	public void initialize(ConfigInfo configInfo, SoftwareRelease release) {
		this.configInfo = configInfo;
		this.release = release;

		setHasChanged(false);

		psets.clear();
		edsources.clear();
		essources.clear();
		services.clear();
		modules.clear();
		paths.clear();
		sequences.clear();
		tasks.clear();
		contents.clear();
	}

	/** reset configuration */
	public void reset() {
		configInfo = null;
		release = null;
		setHasChanged(false);

		psets.clear();
		edsources.clear();
		essources.clear();
		services.clear();
		modules.clear();
		paths.clear();
		sequences.clear();
		tasks.clear();
		contents.clear();
	}

	/** set the configuration info */
	public void setConfigInfo(ConfigInfo configInfo) {
		if (!configInfo.releaseTag().equals(releaseTag()))
			configInfo.setReleaseTag(releaseTag());
		this.configInfo = configInfo;
	}

	/** Overload toString() */
	public String toString() {
		String result = new String();
		if (configInfo == null)
			return result;
		if (parentDir() != null)
			result += parentDir().name();
		if (result.length() != 1)
			result += "/";
		result += name() + "/V" + version();
		return result;
	}

	/** number of components of a certain type */
	public int componentCount(Class<?> c) {
		if (c == PSetParameter.class)
			return psetCount();
		else if (c == EDSourceInstance.class)
			return edsourceCount();
		else if (c == ESSourceInstance.class)
			return essourceCount();
		else if (c == ESModuleInstance.class)
			return esmoduleCount();
		else if (c == ServiceInstance.class)
			return serviceCount();
		else if (c == Path.class)
			return pathCount();
		else if (c == Sequence.class)
			return sequenceCount();
		else if (c == Task.class)
			return taskCount();
		else if (c == ModuleInstance.class)
			return moduleCount();
		else if (c == EDAliasInstance.class)
			return moduleCount();
		else if (c == SwitchProducer.class)
			return switchProducerCount();
		else if (c == OutputModule.class)
			return outputCount();
		else if (c == EventContent.class)
			return contentCount();
		else if (c == Stream.class)
			return streamCount();
		else if (c == PrimaryDataset.class)
			return datasetCount();
		System.err.println("ERROR: unknwon class " + c.getName());
		return 0;
	}

	/** isEmpty() */
	public boolean isEmpty() {
		return (name().length() == 0 && // psets.isEmpty()&&
				psets.parameterCount() == 0 && edsources.isEmpty() && essources.isEmpty() && services.isEmpty()
				&& modules.isEmpty() && paths.isEmpty() && sequences.isEmpty() && tasks.isEmpty()
				&& contents.isEmpty());
	}

	/** retrieve ConfigInfo object */
	public ConfigInfo configInfo() {
		return configInfo;
	}

	/** check if configuration and all its versions are locked */
	public boolean isLocked() {
		return (configInfo != null) ? configInfo.isLocked() : false;
	}

	/** check by which user the configuration and all its versions are locked */
	public String lockedByUser() {
		return (configInfo != null) ? configInfo.lockedByUser() : new String();
	}

	/** database identifier */
	public int dbId() {
		return (configInfo != null) ? configInfo.dbId() : -1;
	}

	/** get configuration name */
	public String name() {
		return (configInfo != null) ? configInfo.name() : "";
	}

	/** get parent directory */
	public Directory parentDir() {
		return (configInfo != null) ? configInfo.parentDir() : null;
	}

	/** get parent directory database id */
	public int parentDirId() {
		return (parentDir() != null) ? parentDir().dbId() : 0;
	}

	/** get configuration version */
	public int version() {
		return (configInfo != null) ? configInfo.version() : 0;
	}

	/** next version */
	public int nextVersion() {
		return (configInfo != null) ? configInfo.nextVersion() : 0;
	}

	/** add the next version */
	public void addNextVersion(int versionId, String created, String creator, String releaseTag, String processName,
			String comment) {
		configInfo.addVersion(versionId, nextVersion(), created, creator, releaseTag, processName, comment);
		configInfo.setVersionIndex(0);
	}

	/** get configuration date of creation as a string */
	public String created() {
		return (configInfo != null) ? configInfo.created() : "";
	}

	/** get configuration creator */
	public String creator() {
		return (configInfo != null) ? configInfo.creator() : "";
	}

	/** get release tag this configuration is associated with */
	public String releaseTag() {
		return (configInfo != null) ? configInfo.releaseTag() : "";
	}

	public void setReleaseTag(String releaseTag) {
		if (configInfo != null)
			configInfo.setReleaseTag(releaseTag);
	}

	/** get the process name */
	public String processName() {
		return (configInfo != null) ? configInfo.processName() : "";
	}

	/** get the comment */
	public String comment() {
		return (configInfo != null) ? configInfo.comment() : "";
	}

	/** get the software release */
	public SoftwareRelease release() {
		return release;
	}

	/** indicate if configuration must be saved */
	public boolean hasChanged() {
		if (hasChanged)
			return true;
		if (psets.hasChanged())
			return true;
		for (EDSourceInstance eds : edsources)
			if (eds.hasChanged())
				return true;
		for (ESSourceInstance ess : essources)
			if (ess.hasChanged())
				return true;
		for (ESModuleInstance esm : esmodules)
			if (esm.hasChanged())
				return true;
		for (ServiceInstance svc : services)
			if (svc.hasChanged())
				return true;
		for (Path pth : paths)
			if (pth.hasChanged())
				return true;
		for (Sequence seq : sequences)
			if (seq.hasChanged())
				return true;
		for (Task tas : tasks)
			if (tas.hasChanged())
				return true;
		for (EDAliasInstance eda : edaliases)  //BSATARIC: not sure if this is necessary
			if (eda.hasChanged())
				return true;
		for (SwitchProducer swp : switchproducers)
			if (swp.hasChanged())
				return true;	
		for (EventContent evc : contents)
			if (evc.hasChanged())
				return true;
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext())
			if (itS.next().hasChanged())
				return true;
		Iterator<PrimaryDataset> itD = datasetIterator();
		while (itD.hasNext())
			if (itD.next().hasChanged())
				return true;
		return false;
	}

	/** set the 'hasChanged' flag */
	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

	/** check if a qualifier is unique */
	public boolean isUniqueQualifier(String qualifier) {
		if (qualifier.length() == 0)
			return false;
		for (ESSourceInstance ess : essources)
			if (ess.name().equals(qualifier))
				return false;
		for (ESModuleInstance esm : esmodules)
			if (esm.name().equals(qualifier))
				return false;
		for (ModuleInstance m : modules)
			if (m.name().equals(qualifier))
				return false;
		for (EDAliasInstance eda : edaliases)
			if (eda.name().equals(qualifier))
				return false;
		for (SwitchProducer swp : switchproducers)
			if (swp.name().equals(qualifier))
				return false;
		for (Path p : paths)
			if (p.name().equals(qualifier))
				return false;
		for (Sequence s : sequences)
			if (s.name().equals(qualifier))
				return false;
		for (Task t : tasks)
			if (t.name().equals(qualifier))
				return false;

		Iterator<OutputModule> itOM = outputIterator();
		while (itOM.hasNext())
			if (itOM.next().name().equals(qualifier))
				return false;

		return true;
	}

	/** check if the reference container has a unique qualifier */
	public boolean hasUniqueQualifier(Referencable referencable) {
		if (referencable.name().length() == 0)
			return false;
		for (ESSourceInstance ess : essources)
			if (ess.name().equals(referencable.name()))
				return false;
		for (ESModuleInstance esm : esmodules)
			if (esm.name().equals(referencable.name()))
				return false;
		for (ModuleInstance m : modules) {
			if (m == referencable)
				continue;
			if (m.name().equals(referencable.name()))
				return false;
		}
		for (EDAliasInstance eda : edaliases) {
			if (eda == referencable)
				continue;
			if (eda.name().equals(referencable.name()))
				return false;
		}
		for (SwitchProducer swp : switchproducers) {
			if (swp == referencable)
				continue;
			if (swp.name().equals(referencable.name()))
				return false;
		}
		Iterator<OutputModule> itOM = outputIterator();
		while (itOM.hasNext()) {
			OutputModule om = itOM.next();
			if (om == referencable)
				continue;
			if (om.name().equals(referencable.name()))
				return false;
		}
		for (Path p : paths) {
			if (p == referencable)
				continue;
			if (p.name().equals(referencable.name()))
				return false;
		}
		for (Sequence s : sequences) {
			if (s == referencable)
				continue;
			if (s.name().equals(referencable.name()))
				return false;
		}
		for (Task t : tasks) {
			if (t == referencable)
				continue;
			if (t.name().equals(referencable.name()))
				return false;
		}
		return true;
	}

	/** check if all entries of a reference container are unique */
	public boolean hasUniqueEntries(ReferenceContainer container) {
		for (int i = 0; i < container.entryCount(); i++) {
			Reference entry = container.entry(i);
			if (entry.parent() instanceof ReferenceContainer) {
				ReferenceContainer c = (ReferenceContainer) entry.parent();
				if (!hasUniqueQualifier(c))
					return false;
				if (!hasUniqueEntries(c))
					return false;
			} else if (!isUniqueQualifier(entry.name()))
				return false;
		}
		return true;
	}

	/** number of empty containers (paths / sequences / tasks) */
	public int emptyContainerCount() {
		int result = 0;
		Iterator<Path> itP = paths.iterator();
		while (itP.hasNext()) {
			Path p = itP.next();
			if (p.entryCount() == 0)
				result++;
		}
		Iterator<Sequence> itS = sequences.iterator();
		while (itS.hasNext()) {
			Sequence s = itS.next();
			if (s.entryCount() == 0)
				result++;
		}
		Iterator<Task> itT = tasks.iterator();
		int taskNumber = 0;
		while (itT.hasNext()) {
			Task t = itT.next();
			System.out.println("TASK NUMBER: " + taskNumber++);
			System.out.println("TASK ENTRYCOUNT: " + t.entryCount());
			if (t.entryCount() == 0)
				result++;
		}
		Iterator<SwitchProducer> itSP = switchproducers.iterator();
		while (itSP.hasNext()) {
			SwitchProducer sp = itSP.next();
			if (sp.entryCount() == 0)
				result++;
		}
		return result;
	}

	//
	// unset tracked parameter counts
	//

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
	public int unsetTrackedPSetParameterCount() {
		int result = 0;
		Iterator<Parameter> itP = psets.parameterIterator();
		while (itP.hasNext())
			result += ((PSetParameter) itP.next()).unsetTrackedParameterCount();
		return result;
	}

	/** number of unsert tracked edsource parameters */
	public int unsetTrackedEDSourceParameterCount() {
		int result = 0;
		for (EDSourceInstance eds : edsources)
			result += eds.unsetTrackedParameterCount();
		return result;
	}

	/** number of unsert tracked essource parameters */
	public int unsetTrackedESSourceParameterCount() {
		int result = 0;
		for (ESSourceInstance ess : essources)
			result += ess.unsetTrackedParameterCount();
		return result;
	}

	/** number of unsert tracked esmodule parameters */
	public int unsetTrackedESModuleParameterCount() {
		int result = 0;
		for (ESModuleInstance esm : esmodules)
			result += esm.unsetTrackedParameterCount();
		return result;
	}

	/** number of unset tracked service parameters */
	public int unsetTrackedServiceParameterCount() {
		int result = 0;
		for (ServiceInstance svc : services)
			result += svc.unsetTrackedParameterCount();
		return result;
	}

	/** number of unset tracked module parameters */
	public int unsetTrackedModuleParameterCount() {
		int result = 0;
		for (ModuleInstance mod : modules)
			result += mod.unsetTrackedParameterCount();
		return result;
	}
	

	/** number of unset tracked EDAlias parameters */
	public int unsetTrackedEDAliasParameterCount() {
		int result = 0;
		for (EDAliasInstance eda : edaliases)
			result += eda.unsetTrackedParameterCount();
		return result;
	}

	/** number of output modules not assigned to any path */
	public int unassignedOutputModuleCount() {
		int result = 0;
		Iterator<OutputModule> itOM = outputIterator();
		while (itOM.hasNext())
			if (itOM.next().referenceCount() == 0)
				result++;
		return result;
	}

	/** number of paths unassigned to any stream */
	public int pathNotAssignedToStreamCount() {
		int result = 0;
		for (Path p : paths) {
			if (p.isEndPath())
				continue;
			if (p.streamCount() == 0)
				result++;
		}
		return result;
	}

	/** number of paths unassigned to any primary dataset */
	public int pathNotAssignedToDatasetCount() {
		int result = 0;
		for (Path p : paths) {
			if (p.isEndPath())
				continue;
			if (p.datasetCount() == 0)
				result++;
		}
		return result;
	}

	/** retrieve instance by name regardless of type */
	public Instance instance(String name) {
		Instance result = null;
		result = edsource(name);
		if (result != null)
			return result;
		result = essource(name);
		if (result != null)
			return result;
		result = esmodule(name);
		if (result != null)
			return result;
		result = service(name);
		if (result != null)
			return result;
		result = module(name);
		if (result != null)
			return result;
		System.err.println("Configuration::instance(): can't find '" + name + "'");
		return null;
	}

	//
	// PSets
	//

	/** retrieve the ParameterContainer 'psets' */
	public ParameterContainer psets() {
		return psets;
	}

	/** number of global PSets */
	public int psetCount() {
		return psets.parameterCount();
	}

	/** get i-th global PSet */
	public PSetParameter pset(int i) {
		return (PSetParameter) psets.parameter(i);
	}

	/** get global pset by name */
	public PSetParameter pset(String name) {
		Iterator<Parameter> itP = psets.parameterIterator();
		while (itP.hasNext()) {
			PSetParameter pset = (PSetParameter) itP.next();
			if (pset.name().equals(name))
				return pset;
		}
		return null;
	}

	/** index of a certain global PSet */
	public int indexOfPSet(PSetParameter pset) {
		return psets.indexOfParameter(pset);
	}

	/** retrieve pset iterator */
	public Iterator<PSetParameter> psetIterator() {
		ArrayList<PSetParameter> list = new ArrayList<PSetParameter>();
		Iterator<Parameter> itP = psets.parameterIterator();
		while (itP.hasNext())
			list.add((PSetParameter) itP.next());
		return list.iterator();
	}

	/** insert global pset at i-th position */
	public void insertPSet(PSetParameter pset) {
		psets.addParameter(pset);
		hasChanged = true;
	}

	/** remove a global PSet */
	public void removePSet(PSetParameter pset) {
		psets.removeParameter(pset);
		hasChanged = true;
	}

	// public void sortPSets() { Collections.sort(psets); hasChanged=true; }

	//
	// EDSources
	//

	/** number of EDSources */
	public int edsourceCount() {
		return edsources.size();
	}

	/** get i-th EDSource */
	public EDSourceInstance edsource(int i) {
		return edsources.get(i);
	}

	/** get EDSource by name */
	public EDSourceInstance edsource(String name) {
		for (EDSourceInstance eds : edsources)
			if (eds.name().equals(name))
				return eds;
		return null;
	}

	/** index of a certain EDSource */
	public int indexOfEDSource(EDSourceInstance edsource) {
		return edsources.indexOf(edsource);
	}

	/** retrieve edsource iterator */
	public Iterator<EDSourceInstance> edsourceIterator() {
		return edsources.iterator();
	}

	/** insert EDSource at i-th position */
	public EDSourceInstance insertEDSource(String templateName) {
		if (edsourceCount() > 0)
			return null;

		EDSourceTemplate template = (EDSourceTemplate) release.edsourceTemplate(templateName);
		if (template == null) {
			System.err.println("insertEDSource ERROR: unknown template '" + templateName + "'!");
			return null;
		}

		EDSourceInstance instance = null;
		try {
			instance = (EDSourceInstance) template.instance();
			edsources.add(instance);
			instance.setConfig(this);
			hasChanged = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return instance;
	}

	/** remove a EDSource */
	public void removeEDSource(EDSourceInstance edsource) {
		edsource.remove();
		int index = edsources.indexOf(edsource);
		edsources.remove(index);
		hasChanged = true;
	}

	/** sort EDSources */
	public void sortEDSources() {
		Collections.sort(edsources);
		hasChanged = true;
	}

	//
	// ESSources
	//

	/** number of ESSources */
	public int essourceCount() {
		return essources.size();
	}

	/** get i-th ESSource */
	public ESSourceInstance essource(int i) {
		return essources.get(i);
	}

	/** get ESSource by name */
	public ESSourceInstance essource(String name) {
		for (ESSourceInstance ess : essources)
			if (ess.name().equals(name))
				return ess;
		return null;
	}

	/** index of a certain ESSource */
	public int indexOfESSource(ESSourceInstance essource) {
		return essources.indexOf(essource);
	}

	/** retrieve essource iterator */
	public Iterator<ESSourceInstance> essourceIterator() {
		return essources.iterator();
	}

	/** insert ESSource at i=th position */
	public ESSourceInstance insertESSource(int i, String templateName, String instanceName) {
		ESSourceTemplate template = (ESSourceTemplate) release.essourceTemplate(templateName);
		if (template == null) {
			System.err.println("insertESSource ERROR: unknown template '" + templateName + "'!");
			return null;
		}

		ESSourceInstance instance = null;
		try {
			instance = (ESSourceInstance) template.instance(instanceName);
			essources.add(i, instance);
			instance.setConfig(this);
			hasChanged = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return instance;
	}

	/** remove a ESSource */
	public void removeESSource(ESSourceInstance essource) {
		essource.remove();
		int index = essources.indexOf(essource);
		essources.remove(index);
		hasChanged = true;
	}

	/** sort ESSources */
	public void sortESSources() {
		Collections.sort(essources);
		hasChanged = true;
	}

	//
	// ESModules
	//

	/** number of ESModules */
	public int esmoduleCount() {
		return esmodules.size();
	}

	/** get i-th ESModule */
	public ESModuleInstance esmodule(int i) {
		return esmodules.get(i);
	}

	/** get ESModule by name */
	public ESModuleInstance esmodule(String name) {
		for (ESModuleInstance esm : esmodules)
			if (esm.name().equals(name))
				return esm;
		return null;
	}

	/** index of a certain ESSource */
	public int indexOfESModule(ESModuleInstance esmodule) {
		return esmodules.indexOf(esmodule);
	}

	/** retrieve esmodule iterator */
	public Iterator<ESModuleInstance> esmoduleIterator() {
		return esmodules.iterator();
	}

	/** insert ESModule at i-th position */
	public ESModuleInstance insertESModule(int i, String templateName, String instanceName) {
		ESModuleTemplate template = (ESModuleTemplate) release.esmoduleTemplate(templateName);
		if (template == null) {
			System.err.println("insertESModule ERROR: unknown template '" + templateName + "'!");
			return null;
		}

		ESModuleInstance instance = null;
		try {
			instance = (ESModuleInstance) template.instance(instanceName);
			esmodules.add(i, instance);
			instance.setConfig(this);
			hasChanged = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return instance;
	}

	/** remove a ESModule */
	public void removeESModule(ESModuleInstance esmodule) {
		esmodule.remove();
		int index = esmodules.indexOf(esmodule);
		esmodules.remove(index);
		hasChanged = true;
	}

	/** sort ESModules */
	public void sortESModules() {
		Collections.sort(esmodules);
		hasChanged = true;
	}

	//
	// Services
	//

	/** number of Services */
	public int serviceCount() {
		return services.size();
	}

	/** get i-th Service */
	public ServiceInstance service(int i) {
		return services.get(i);
	}

	/** get Service by name */
	public ServiceInstance service(String name) {
		for (ServiceInstance svc : services)
			if (svc.name().equals(name))
				return svc;
		return null;
	}

	/** index of a certain Service */
	public int indexOfService(ServiceInstance service) {
		return services.indexOf(service);
	}

	/** retrieve service iterator */
	public Iterator<ServiceInstance> serviceIterator() {
		return services.iterator();
	}

	/** insert Service at i=th position */
	public ServiceInstance insertService(int i, String templateName) {
		ServiceTemplate template = (ServiceTemplate) release.serviceTemplate(templateName);
		if (template == null) {
			System.err.println("insertService ERROR: unknown template '" + templateName + "'!");
			return null;
		}

		ServiceInstance instance = null;
		try {
			instance = (ServiceInstance) template.instance();
			services.add(i, instance);
			instance.setConfig(this);
			hasChanged = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return instance;
	}

	/** remove a Service */
	public void removeService(ServiceInstance service) {
		service.remove();
		int index = services.indexOf(service);
		services.remove(index);
		hasChanged = true;
	}

	/** sort services */
	public void sortServices() {
		Collections.sort(services);
		hasChanged = true;
	}

	//
	// Modules
	//

	/** number of Modules */
	public int moduleCount() {
		return modules.size();
	}

	/** get i-th Module */
	public ModuleInstance module(int i) {
		return modules.get(i);
	}

	/** get Module by name */
	public ModuleInstance module(String moduleName) {
		for (ModuleInstance m : modules)
			if (m.name().equals(moduleName))
				return m;
		return null;
	}

	/** index of a certain Module */
	public int indexOfModule(ModuleInstance module) {
		return modules.indexOf(module);
	}

	/** retrieve module iterator */
	public Iterator<ModuleInstance> moduleIterator() {
		return modules.iterator();
	}

	/** insert a module */
	public ModuleInstance insertModule(String templateName, String instanceName) {
		ModuleTemplate template = (ModuleTemplate) release.moduleTemplate(templateName);
		if (template == null) {
			System.err.println(
					"insertModule ERROR: unknown template '" + templateName + "' (instanceName=" + instanceName + ")!");
			return null;
		}

		ModuleInstance instance = null;
		try {
			instance = (ModuleInstance) template.instance(instanceName);
			if (instance.referenceCount() == 0) {
				modules.add(instance);
				instance.setConfig(this);
				hasChanged = true;
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return instance;
	}

	/** insert a pre-existing module */
	public boolean insertModule(int i, ModuleInstance module) {
		if (modules.indexOf(module) < 0 && module.referenceCount() == 0) {
			modules.add(i, module);
			module.setConfig(this);
			hasChanged = true;
			return true;
		}
		return false;
	}

	/** remove a module reference */
	public void removeModuleReference(ModuleReference module) {
		ModuleInstance instance = (ModuleInstance) module.parent();
		module.remove();
		if (instance.referenceCount() == 0) {
			int index = modules.indexOf(instance);
			modules.remove(index);
		}
		hasChanged = true;
	}

	/** insert ModuleReference at i-th position into a path/sequence/task */
	public ModuleReference insertModuleReference(ReferenceContainer container, int i, ModuleInstance instance) {
		ModuleReference reference = (ModuleReference) instance.createReference(container, i);
		hasChanged = true;
		return reference;
	}

	/** insert ModuleReference at i-th position into a path/sequence/task */
	public ModuleReference insertModuleReference(ReferenceContainer container, int i, String templateName,
			String instanceName) {
		ModuleInstance instance = insertModule(templateName, instanceName);
		return (instance != null) ? insertModuleReference(container, i, instance) : null;
	}

	/** sort Modules */
	public void sortModules() {
		Collections.sort(modules);
	}
	
	
	//
	// EDAliases
	//

	/** number of EDAliases */
	public int edAliasCount() {
		return edaliases.size();
	}

	/** get i-th EDAlias */
	public EDAliasInstance edAlias(int i) {
		return edaliases.get(i);
	}

	/** get EDAlias by name */
	public EDAliasInstance edAlias(String edAliasName) {
		for (EDAliasInstance e : edaliases)
			if (e.name().equals(edAliasName))
				return e;
		return null;
	}

	/** index of a certain EDAlias */
	public int indexOfEDAlias(EDAliasInstance edAlias) {
		return edaliases.indexOf(edAlias);
	}

	/** retrieve EDAlias iterator */
	public Iterator<EDAliasInstance> edAliasIterator() {
		return edaliases.iterator();
	}

	/** insert an EDAlias */
	public EDAliasInstance insertEDAlias(String instanceName) {

		EDAliasInstance instance = null;

		try {
			instance = new EDAliasInstance(instanceName);
			if (instance.referenceCount() == 0) {
				edaliases.add(instance);
				instance.setConfig(this);
				hasChanged = true;
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return instance;
	}

	/** insert a pre-existing EDAlias */
	public boolean insertEDAlias(int i, EDAliasInstance edAlias) {
		if (edaliases.indexOf(edAlias) < 0 && edAlias.referenceCount() == 0) {
			edaliases.add(i, edAlias);
			edAlias.setConfig(this);
			hasChanged = true;
			return true;
		}
		return false;
	}

	/** remove a EDAlias reference */
	public void removeEDAliasReference(EDAliasReference edAlias) {
		EDAliasInstance instance = (EDAliasInstance) edAlias.parent();
		edAlias.remove();
		if (instance.referenceCount() == 0) {
			int index = edaliases.indexOf(instance);
			edaliases.remove(index);
		}
		hasChanged = true;
	}

	/** insert EDAliasReference at i-th position into a path/sequence/task */
	public EDAliasReference insertEDAliasReference(ReferenceContainer container, int i, EDAliasInstance instance) {
		EDAliasReference reference = (EDAliasReference) instance.createReference(container, i);
		hasChanged = true;
		return reference;
	}

	/** insert EDAliasReference at i-th position into a path/sequence/task */
	public EDAliasReference insertEDAliasReference(ReferenceContainer container, int i,
			String instanceName) {
		EDAliasInstance instance = insertEDAlias(instanceName);
		return (instance != null) ? insertEDAliasReference(container, i, instance) : null;
	}

	/** sort EDAliases */
	public void sortEDAliases() {
		Collections.sort(edaliases);
	}
	
	//
	// SwitchProducers
	//

	/** number of SwitchProducers */
	public int switchProducerCount() {
		return switchproducers.size();
	}

	/** get i-th SwitchProducer */
	public SwitchProducer switchProducer(int i) {
		return switchproducers.get(i);
	}

	/** get SwitchProducer by name */
	public SwitchProducer switchProducer(String switchProducerName) {
		for (SwitchProducer s : switchproducers)
			if (s.name().equals(switchProducerName))
				return s;
		return null;
	}

	/** index of a certain SwitchProducer */
	public int indexOfSwitchProducer(SwitchProducer switchProducer) {
		return switchproducers.indexOf(switchProducer);
	}

	/** retrieve switch producers iterator */
	public Iterator<SwitchProducer> switchProducerIterator() {
		return switchproducers.iterator();
	}

	/** retrieve switch producers iterator */
	public Iterator<SwitchProducer> orderedSwitchProducerIterator() {
		ArrayList<SwitchProducer> result = new ArrayList<SwitchProducer>();
		Iterator<SwitchProducer> itS = switchProducerIterator();
		while (itS.hasNext())
			result.add(itS.next());
		boolean isOrdered = false;
		while (!isOrdered) {
			isOrdered = true;
			int indexS = 0;
			while (indexS < result.size()) {
				SwitchProducer switchProducer = result.get(indexS);
				int indexMax = -1;
				itS = switchProducer.switchProducerIterator();
				while (itS.hasNext()) {
					int index = result.indexOf(itS.next());
					if (index > indexMax)
						indexMax = index;
				}
				if (indexMax > indexS) {
					isOrdered = false;
					result.remove(indexS);
					result.add(indexMax, switchProducer);
				} else {
					indexS++;
				}
			}
		}
		return result.iterator();
	}

	/** insert switch producer */
	public SwitchProducer insertSwitchProducer(int i, String switchProducerName) {
		SwitchProducer switchProducer = new SwitchProducer(switchProducerName);
		switchproducers.add(i, switchProducer);
		switchProducer.setConfig(this);
		hasChanged = true;
		return switchProducer;
	}

	/** move a switch producer to another position within switch producers */
	public boolean moveSwitchProducer(SwitchProducer switchProducer, int targetIndex) {
		int currentIndex = switchproducers.indexOf(switchProducer);
		if (currentIndex < 0)
			return false;
		if (currentIndex == targetIndex)
			return true;
		if (targetIndex > switchproducers.size())
			return false;
		if (currentIndex < targetIndex)
			targetIndex--;
		switchproducers.remove(currentIndex);
		switchproducers.add(targetIndex, switchProducer);
		hasChanged = true;
		return true;
	}

	/** remove a switch producer */
	public void removeSwitchProducer(SwitchProducer switchProducer) {
		while (switchProducer.referenceCount() > 0) {
			SwitchProducerReference reference = (SwitchProducerReference) switchProducer.reference(0);
			reference.remove();
		}

		// remove all modules and EDAliases from this switchProducer
		while (switchProducer.entryCount() > 0) {
			Reference reference = switchProducer.entry(0);
			reference.remove();
			if (reference instanceof ModuleReference) {
				ModuleReference module = (ModuleReference) reference;
				ModuleInstance instance = (ModuleInstance) module.parent();
				if (instance.referenceCount() == 0) {
					int index = modules.indexOf(instance);
					modules.remove(index);
				}
			} else if (reference instanceof EDAliasReference) {
				EDAliasReference edAlias = (EDAliasReference) reference;
				EDAliasInstance instance = (EDAliasInstance) edAlias.parent();
				if (instance.referenceCount() == 0) {
					int index = edaliases.indexOf(instance);
					edaliases.remove(index);
				}
			}
		}

		int index = switchproducers.indexOf(switchProducer);
		switchproducers.remove(index);
		hasChanged = true;
	}

	/** insert a switch producer into another path */
	public SwitchProducerReference insertSwitchProducerReference(ReferenceContainer parent, int i, SwitchProducer switchProducer) {
		SwitchProducerReference reference = (SwitchProducerReference) switchProducer.createReference(parent, i);
		hasChanged = true;
		return reference;
	}

	/** sort Switch producers */
	public void sortSwitchProducers() {
		Collections.sort(switchproducers);
		hasChanged = true;
	}

	//
	// OutputModules
	//

	/** number of OutputModules */
	public int outputCount() {
		return streamCount();
	}

	/** get i-th OutputModule */
	public OutputModule output(int i) {
		return stream(i).outputModule();
	}

	/** get OutputModule by name */
	public OutputModule output(String outputName) {
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext()) {
			OutputModule om = itS.next().outputModule();
			if (om.name().equals(outputName))
				return om;
		}
		return null;
	}

	/** index of a certain OutputModule */
	public int indexOfOutput(OutputModule output) {
		int index = 0;
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext()) {
			if (itS.next().outputModule() == output)
				return index;
			index++;
		}
		return -1;
	}

	/** retrieve OutputModule iterator */
	public Iterator<OutputModule> outputIterator() {
		ArrayList<OutputModule> outputs = new ArrayList<OutputModule>();
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext())
			outputs.add(itS.next().outputModule());
		return outputs.iterator();
	}

	/** insert output module reference at i-th position into a path/sequence */
	public OutputModuleReference insertOutputModuleReference(ReferenceContainer container, int i, OutputModule output) {
		OutputModuleReference reference = (OutputModuleReference) output.createReference(container, i);
		hasChanged = true;

		return reference;
	}

	/** remove an output module reference */
	public void removeOutputModuleReference(OutputModuleReference reference) {
		OutputModule output = (OutputModule) reference.parent();
		reference.remove();
		hasChanged = true;
	}

	//
	// Paths
	//

	/** number of Paths */
	public int pathCount() {
		return paths.size();
	}

	/** get i-th Path */
	public Path path(int i) {
		return paths.get(i);
	}

	/** get Path by name */
	public Path path(String pathName) {
		for (Path p : paths)
			if (p.name().equals(pathName))
				return p;
		return null;
	}

	/** index of a certain Path */
	public int indexOfPath(Path path) {
		return paths.indexOf(path);
	}

	/** retrieve path iterator */
	public Iterator<Path> pathIterator() {
		return paths.iterator();
	}

	/** insert path at i-th position */
	public Path insertPath(int i, String pathName) {
		Path path = new Path(pathName);
		paths.add(i, path);
		path.setConfig(this);
		hasChanged = true;
		return path;
	}

	/** move a path to another position within paths */
	public boolean movePath(Path path, int targetIndex) {
		int currentIndex = paths.indexOf(path);
		if (currentIndex < 0)
			return false;
		if (currentIndex == targetIndex)
			return true;
		if (targetIndex >= paths.size())
			return false;
		if (currentIndex < targetIndex)
			targetIndex--;
		paths.remove(currentIndex);
		paths.add(targetIndex, path);
		hasChanged = true;
		return true;
	}

	/** get the sequence number of a certain path */
	public int pathSequenceNb(Path path) {
		return paths.indexOf(path);
	}

	/** remove a path */
	public void removePath(Path path) {
		while (path.referenceCount() > 0) {
			PathReference reference = (PathReference) path.reference(0);
			reference.remove();
		}

		// remove all entries of this path
		while (path.entryCount() > 0) {
			Reference reference = path.entry(0);
			reference.remove();
			if (reference instanceof ModuleReference) {
				ModuleReference module = (ModuleReference) reference;
				ModuleInstance instance = (ModuleInstance) module.parent();
				if (instance.referenceCount() == 0) {
					int index = modules.indexOf(instance);
					modules.remove(index);
				}
			} 
		}
			
		// remove this paths from all streams (includes datasets & contents)
		Iterator<Stream> itS = path.streamIterator();
		while (itS.hasNext())
			itS.next().removePath(path);

		int index = paths.indexOf(path);
		paths.remove(index);

		for (int i = 0; i < pathCount(); i++) {
			Path p = path(i);
			for (int sequenceNb = 0; sequenceNb < p.entryCount(); sequenceNb++) {
				Reference r = p.entry(sequenceNb);
				if (r instanceof OutputModuleReference) {
					OutputModule om = (OutputModule) r.parent();
					if (om.hasChanged()) {
						p.setHasChanged();
					}
				}
			}
		}

		/* remove path from PrescaleService */
		ServiceInstance pss = service("PrescaleService");
		if (pss != null) {
			VPSetParameter psTable = (VPSetParameter) pss.parameter("prescaleTable");
			if (psTable != null) {
				ArrayList<PSetParameter> psetsToRemove = new ArrayList<PSetParameter>();
				Iterator<PSetParameter> itPSet = psTable.psetIterator();
				while (itPSet.hasNext()) {
					PSetParameter pset = itPSet.next();
					StringParameter pPathName = (StringParameter) pset.parameter("pathName");
					String pathName = (String) pPathName.value();
					if (pathName.equals(path.name()))
						psetsToRemove.add(pset);
				}
				Iterator<PSetParameter> itRmv = psetsToRemove.iterator();
				while (itRmv.hasNext())
					psTable.removeParameterSet(itRmv.next());
				if (psetsToRemove.size() > 0)
					pss.setHasChanged();
			}
		}

		/* remove path from TriggerResultsFilters */
		Iterator<ModuleInstance> itM = moduleIterator();
		while (itM.hasNext()) {
			ModuleInstance module = itM.next();
			if (module.template().toString().equals("TriggerResultsFilter")) {
				VStringParameter parameterTriggerConditions = (VStringParameter) module.parameter("triggerConditions",
						"vstring");
				int n = 0;
				for (int i = 0; i < parameterTriggerConditions.vectorSize(); i++) {
					String trgCondition = (String) parameterTriggerConditions.value(i);
					// replace removed path by FALSE
					String strCondition = SmartPrescaleTable.rename(trgCondition, path.name(), "FALSE");
					// replace conditions containing only FALSE by empty conditions
					strCondition = SmartPrescaleTable.simplify(strCondition);
					// update needed?
					if (!strCondition.equals(trgCondition)) {
						n++;
						parameterTriggerConditions.setValue(i, strCondition);
					}
				}
				// remove empty conditions
				if (module.squeeze())
					n++;
				if (n > 0)
					module.setHasChanged();
			}
		}

		hasChanged = true;
	}

	/** insert a path reference into another path/sequence/task */
	public PathReference insertPathReference(ReferenceContainer parentPath, int i, Path path) {
		PathReference reference = (PathReference) path.createReference(parentPath, i);
		hasChanged = true;
		return reference;
	}

	/** update a path reference before saving **/
	public void updatePathReferences() {
		for (int i = 0; i < pathCount(); i++) {
			Path p = path(i);
			for (int sequenceNb = 0; sequenceNb < p.entryCount(); sequenceNb++) {
				Reference r = p.entry(sequenceNb);
				if (r instanceof OutputModuleReference) {
					OutputModule om = (OutputModule) r.parent();
					if (om.hasChanged()) {
						p.setHasChanged();
					}
				}
			}
		}
		hasChanged = true;
	}

	/** sort Paths */
	public void sortPaths() {
		Collections.sort(paths);
		hasChanged = true;
	}

	//
	// Sequences
	//

	/** number of Sequences */
	public int sequenceCount() {
		return sequences.size();
	}

	/** get i-th Sequence */
	public Sequence sequence(int i) {
		return sequences.get(i);
	}

	/** get Sequence by name */
	public Sequence sequence(String sequenceName) {
		for (Sequence s : sequences)
			if (s.name().equals(sequenceName))
				return s;
		return null;
	}

	/** index of a certain Sequence */
	public int indexOfSequence(Sequence sequence) {
		return sequences.indexOf(sequence);
	}

	/** retrieve sequence iterator */
	public Iterator<Sequence> sequenceIterator() {
		return sequences.iterator();
	}

	/** retrieve sequence iterator */
	public Iterator<Sequence> orderedSequenceIterator() {
		ArrayList<Sequence> result = new ArrayList<Sequence>();
		Iterator<Sequence> itS = sequenceIterator();
		while (itS.hasNext())
			result.add(itS.next());
		boolean isOrdered = false;
		while (!isOrdered) {
			isOrdered = true;
			int indexS = 0;
			while (indexS < result.size()) {
				Sequence sequence = result.get(indexS);
				int indexMax = -1;
				itS = sequence.sequenceIterator();
				while (itS.hasNext()) {
					int index = result.indexOf(itS.next());
					if (index > indexMax)
						indexMax = index;
				}
				if (indexMax > indexS) {
					isOrdered = false;
					result.remove(indexS);
					result.add(indexMax, sequence);
				} else {
					indexS++;
				}
			}
		}
		return result.iterator();
	}

	/** insert sequence */
	public Sequence insertSequence(int i, String sequenceName) {
		Sequence sequence = new Sequence(sequenceName);
		sequences.add(i, sequence);
		sequence.setConfig(this);
		hasChanged = true;
		return sequence;
	}

	/** move a sequence to another position within sequences */
	public boolean moveSequence(Sequence sequence, int targetIndex) {
		int currentIndex = sequences.indexOf(sequence);
		if (currentIndex < 0)
			return false;
		if (currentIndex == targetIndex)
			return true;
		if (targetIndex > sequences.size())
			return false;
		if (currentIndex < targetIndex)
			targetIndex--;
		sequences.remove(currentIndex);
		sequences.add(targetIndex, sequence);
		hasChanged = true;
		return true;
	}

	/** remove a sequence */
	public void removeSequence(Sequence sequence) {
		while (sequence.referenceCount() > 0) {
			SequenceReference reference = (SequenceReference) sequence.reference(0);
			reference.remove();
		}

		// remove all modules and EDAliases from this sequence
		while (sequence.entryCount() > 0) {
			Reference reference = sequence.entry(0);
			reference.remove();
			if (reference instanceof ModuleReference) {
				ModuleReference module = (ModuleReference) reference;
				ModuleInstance instance = (ModuleInstance) module.parent();
				if (instance.referenceCount() == 0) {
					int index = modules.indexOf(instance);
					modules.remove(index);
				}
			}
		}

		int index = sequences.indexOf(sequence);
		sequences.remove(index);
		hasChanged = true;
	}

	/** insert a sequence reference into another path */
	public SequenceReference insertSequenceReference(ReferenceContainer parent, int i, Sequence sequence) {
		SequenceReference reference = (SequenceReference) sequence.createReference(parent, i);
		hasChanged = true;
		return reference;
	}

	/** sort Sequences */
	public void sortSequences() {
		Collections.sort(sequences);
		hasChanged = true;
	}

	//
	// Tasks
	//

	/** number of Tasks */
	public int taskCount() {
		return tasks.size();
	}

	/** get i-th Task */
	public Task task(int i) {
		return tasks.get(i);
	}

	/** get Task by name */
	public Task task(String taskName) {
		for (Task t : tasks)
			if (t.name().equals(taskName))
				return t;
		return null;
	}

	/** index of a certain Task */
	public int indexOfTask(Task task) {
		return tasks.indexOf(task);
	}

	/** retrieve task iterator */
	public Iterator<Task> taskIterator() {
		return tasks.iterator();
	}

	/** retrieve task iterator */
	public Iterator<Task> orderedTaskIterator() {
		ArrayList<Task> result = new ArrayList<Task>();
		Iterator<Task> itT = taskIterator();
		while (itT.hasNext())
			result.add(itT.next());
		boolean isOrdered = false;
		while (!isOrdered) {
			isOrdered = true;
			int indexT = 0;
			while (indexT < result.size()) {
				Task task = result.get(indexT); // take iterator on subtasks of the main task
				int indexMax = -1;
				itT = task.taskIterator();
				while (itT.hasNext()) {
					int index = result.indexOf(itT.next());
					if (index > indexMax)
						indexMax = index;
				}
				if (indexMax > indexT) {
					isOrdered = false;
					result.remove(indexT);
					result.add(indexMax, task);
				} else {
					indexT++;
				}
			}
		}
		return result.iterator();
	}

	/** insert task */
	public Task insertTask(int i, String taskName) {
		Task task = new Task(taskName);
		tasks.add(i, task);
		task.setConfig(this);
		hasChanged = true;
		return task;
	}

	/** move a task to another position within tasks */
	public boolean moveTask(Task task, int targetIndex) {
		int currentIndex = tasks.indexOf(task);
		if (currentIndex < 0)
			return false;
		if (currentIndex == targetIndex)
			return true;
		if (targetIndex > tasks.size())
			return false;
		if (currentIndex < targetIndex)
			targetIndex--;
		tasks.remove(currentIndex);
		tasks.add(targetIndex, task);
		hasChanged = true;
		return true;
	}

	/** remove a task */
	public void removeTask(Task task) {
		while (task.referenceCount() > 0) {
			TaskReference reference = (TaskReference) task.reference(0);
			reference.remove();
		}

		// remove all modules from this task
		while (task.entryCount() > 0) {
			Reference reference = task.entry(0);
			reference.remove();
			if (reference instanceof ModuleReference) {
				ModuleReference module = (ModuleReference) reference;
				ModuleInstance instance = (ModuleInstance) module.parent();
				if (instance.referenceCount() == 0) {
					int index = modules.indexOf(instance);
					modules.remove(index);
				}
			}
		}

		int index = tasks.indexOf(task);
		tasks.remove(index);
		hasChanged = true;
	}

	/** insert a task reference into another path */
	public TaskReference insertTaskReference(ReferenceContainer parent, int i, Task task) {
		TaskReference reference = (TaskReference) task.createReference(parent, i);
		hasChanged = true;
		return reference;
	}

	/** sort Tasks */
	public void sortTasks() {
		Collections.sort(tasks);
		hasChanged = true;
	}

	//
	// EventContents
	//

	/** number of event contents */
	public int contentCount() {
		return contents.size();
	}

	/** retrieve i-th event content */
	public EventContent content(int i) {
		Collections.sort(contents);
		return contents.get(i);
	}

	/** retrieve content by name */
	public EventContent content(String contentName) {
		for (EventContent ec : contents)
			if (ec.name().equals(contentName))
				return ec;
		return null;
	}

	/** index of a certain event content */
	public int indexOfContent(EventContent ec) {
		Collections.sort(contents);
		return contents.indexOf(ec);
	}

	/** retrieve event content iterator */
	public Iterator<EventContent> contentIterator() {
		Collections.sort(contents);
		return contents.iterator();
	}

	/** insert new event content */
	public EventContent insertContent(String contentName) {
		for (EventContent ec : contents)
			if (ec.name().equals(contentName))
				return ec;
		EventContent content = new EventContent(contentName);
		contents.add(content);
		content.setConfig(this);
		Collections.sort(contents);
		hasChanged = true;
		return content;
	}

	/** remove event content */
	public void removeContent(EventContent ec) {
		int index = contents.indexOf(ec);
		if (index < 0)
			return;
		ec.removeStreams();
		contents.remove(index);
		hasChanged = true;
	}

	/** move a content to another position within contents */
	/*
	 * public boolean moveContent(EventContent content,int targetIndex) { int
	 * currentIndex = contents.indexOf(content); if (currentIndex<0) return false;
	 * if (currentIndex==targetIndex) return true; if (targetIndex>=contents.size())
	 * return false; if (currentIndex<targetIndex) targetIndex--;
	 * contents.remove(currentIndex); contents.add(targetIndex,content); hasChanged
	 * = true; return true; }
	 */

	//
	// Streams
	//

	/** number of streams */
	public int streamCount() {
		int result = 0;
		Iterator<EventContent> itC = contentIterator();
		while (itC.hasNext())
			result += itC.next().streamCount();
		return result;
	}

	/** retrieve i-th stream */
	public Stream stream(int i) {
		ArrayList<Stream> streams = new ArrayList<Stream>();
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext())
			streams.add(itS.next());
		return streams.get(i);
	}

	/** retrieve stream by name */
	public Stream stream(String streamName) {
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext()) {
			Stream stream = itS.next();
			if (stream.name().equals(streamName))
				return stream;
		}
		return null;
	}

	/** index of a certain stream */
	public int indexOfStream(Stream stream) {
		ArrayList<Stream> streams = new ArrayList<Stream>();
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext())
			streams.add(itS.next());
		return streams.indexOf(stream);
	}

	/** retrieve stream iterator */
	public Iterator<Stream> streamIterator() {
		ArrayList<Stream> streams = new ArrayList<Stream>();
		Iterator<EventContent> itC = contentIterator();
		while (itC.hasNext()) {
			Iterator<Stream> itS = itC.next().streamIterator();
			while (itS.hasNext())
				streams.add(itS.next());
		}
		Collections.sort(streams);
		return streams.iterator();
	}

	//
	// Primary Datasets
	//

	/** number of primary datasets */
	public int datasetCount() {
		int result = 0;
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext())
			result += itS.next().datasetCount();
		return result;
	}

	/** retrieve i-th primary dataset */
	public PrimaryDataset dataset(int i) {
		ArrayList<PrimaryDataset> datasets = new ArrayList<PrimaryDataset>();
		Iterator<PrimaryDataset> itD = datasetIterator();
		while (itD.hasNext())
			datasets.add(itD.next());
		return datasets.get(i);
	}

	/** retrieve primary dataset by name */
	public PrimaryDataset dataset(String datasetName) {
		Iterator<PrimaryDataset> itD = datasetIterator();
		while (itD.hasNext()) {
			PrimaryDataset dataset = itD.next();
			if (dataset.name().equals(datasetName))
				return dataset;
		}
		return null;
	}

	/** index of a certain primary dataset */
	public int indexOfDataset(PrimaryDataset dataset) {
		ArrayList<PrimaryDataset> datasets = new ArrayList<PrimaryDataset>();
		Iterator<PrimaryDataset> itD = datasetIterator();
		while (itD.hasNext())
			datasets.add(itD.next());
		return datasets.indexOf(dataset);
	}

	/** retrieve primary dataset iterator */
	public Iterator<PrimaryDataset> datasetIterator() {
		ArrayList<PrimaryDataset> datasets = new ArrayList<PrimaryDataset>();
		Iterator<Stream> itS = streamIterator();
		while (itS.hasNext()) {
			Iterator<PrimaryDataset> itD = itS.next().datasetIterator();
			while (itD.hasNext())
				datasets.add(itD.next());
		}
		Collections.sort(datasets);
		return datasets.iterator();
	}

	//
	// Blocks
	//

	/** retrieve block iterator */
	public Iterator<Block> blockIterator() {
		return blocks.iterator();
	}

	//
	//
	//

	/** cleanup of empty datsets/streams/contents/outputs */

	public int cleanup() {

		ArrayList<EventContent> cList = new ArrayList<EventContent>();
		ArrayList<Stream> sList = new ArrayList<Stream>();
		ArrayList<PrimaryDataset> dList = new ArrayList<PrimaryDataset>();
		ArrayList<Path> pList = new ArrayList<Path>();

		cList.clear();
		int contentCount = 0;
		Iterator<EventContent> itC = contentIterator();
		while (itC.hasNext()) {
			EventContent c = itC.next();
			sList.clear();
			int streamCount = 0;
			Iterator<Stream> itS = c.streamIterator();
			while (itS.hasNext()) {
				Stream s = itS.next();
				int omrefs = 0;
				OutputModule om = s.outputModule();
				if (om != null)
					omrefs = om.referenceCount();
				dList.clear();
				int pathCount = 0;
				Iterator<PrimaryDataset> itD = s.datasetIterator();
				while (itD.hasNext()) {
					PrimaryDataset d = itD.next();
					if (d.pathCount() == 0 || omrefs == 0) {
						dList.add(d);
					} else {
						pathCount += d.pathCount();
					}
				}
				for (PrimaryDataset id : dList) {
					pList.clear();
					Iterator<Path> itP = id.pathIterator();
					while (itP.hasNext()) {
						Path p = itP.next();
						pList.add(p);
					}
					for (Path ip : pList) {
						s.removePath(ip);
					}
					s.removeDataset(id);
				}
				pathCount += s.pathCount();
				if (pathCount == 0) {
					sList.add(s);
				} else {
					streamCount += pathCount;
				}
			}
			for (Stream is : sList)
				c.removeStream(is);
			if (streamCount == 0 || c.commandCount() == 0) {
				cList.add(c);
			} else {
				contentCount += streamCount;
			}
		}
		for (EventContent ic : cList)
			removeContent(ic);

		return contentCount;
	}

}

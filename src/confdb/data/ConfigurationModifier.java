package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * ConfigurationModifier ---------------------
 * 
 * @author Philipp Schieferdecker
 *
 *         retrieve only selected parts of a configuration, after applying
 *         configurable filter decisions.
 */
public class ConfigurationModifier implements IConfiguration {
	//
	// member data
	//

	/** the master configuration */
	private IConfiguration master = null;

	/** flag indicating if the filter was applied */
	private boolean isModified = false;

	/** filtered components */
	private ArrayList<PSetParameter> psets = new ArrayList<PSetParameter>();
	private ArrayList<EDSourceInstance> edsources = new ArrayList<EDSourceInstance>();
	private ArrayList<ESSourceInstance> essources = new ArrayList<ESSourceInstance>();
	private ArrayList<ESModuleInstance> esmodules = new ArrayList<ESModuleInstance>();
	private ArrayList<ServiceInstance> services = new ArrayList<ServiceInstance>();
	private ArrayList<ModuleInstance> modules = new ArrayList<ModuleInstance>();
	private ArrayList<EDAliasInstance> edaliases = new ArrayList<EDAliasInstance>();
	private ArrayList<SwitchProducer> switchproducers = new ArrayList<SwitchProducer>();
	private ArrayList<OutputModule> outputs = new ArrayList<OutputModule>();
	private ArrayList<Path> paths = new ArrayList<Path>();
	private ArrayList<Sequence> sequences = new ArrayList<Sequence>();
	private ArrayList<Task> tasks = new ArrayList<Task>();
	private ArrayList<EventContent> contents = new ArrayList<EventContent>();
	private ArrayList<Stream> streams = new ArrayList<Stream>();
	private ArrayList<PrimaryDataset> datasets = new ArrayList<PrimaryDataset>();
	private ArrayList<Block> blocks = new ArrayList<Block>();

	/** internal instructions */
	private ModifierInstructions modifications = new ModifierInstructions();

	//
	// construction
	//

	/** standard constructor */
	public ConfigurationModifier(IConfiguration config) {
		this.master = config;
	}

	//
	// member functions
	//

	/** toString() */
	public String toString() {
		String result = new String();
		if (parentDir() != null)
			result += parentDir().name();
		if (result.length() != 1)
			result += "/";
		result += name() + "/V" + version();
		return result;
	}

	/** replace the current EDSource with a PoolSource */
	public void insertPoolSource(String fileNames) {
		modifications.insertPoolSource(fileNames);
	}

	/** replace current OutputModules with PoolOutputModules */
	public void insertPoolOutputModule(String fileName) {
		modifications.insertPoolOutputModule(fileName);
	}

	/** replace the current EDSource with a DaqSource */
	public void insertDaqSource() {
		if (edsourceCount() == 0 || !edsource(0).template().name().equals("DaqSource"))
			modifications.insertDaqSource();
	}

	/** replace current OutputModules with ShmStreamConsumer */
	public void insertShmStreamConsumer() {
		Iterator<ModuleInstance> itM = moduleIterator();
		while (itM.hasNext()) {
			ModuleInstance module = itM.next();
			if (module.template().name().equals("ShmStreamConsumer"))
				return;
		}
		modifications.insertShmStreamConsumer();
	}

	/** insert global PSet after (!) modification */
	public void insertPSet(PSetParameter pset) {
		if (isModified) {
			Iterator<PSetParameter> it = psets.iterator();
			while (it.hasNext())
				if (it.next().name().equals(pset.name()))
					it.remove();
			psets.add(pset);
		}
	}

	/** insert additional service after (!) modification */
	public void insertService(ServiceInstance service) {
		if (isModified) {
			Iterator<ServiceInstance> it = services.iterator();
			while (it.hasNext())
				if (it.next().name().equals(service.name()))
					it.remove();
			services.add(service);
		}
	}

	/** remove global PSet 'maxEvents' */
	public void removeMaxEvents() {
		modifications.insertPSetIntoBlackList("maxEvents");
	}

	/** apply modifications based on internal modifications */
	public void modify() {
		modify(modifications);
	}

	/** apply modifications */
	public void modify(ModifierInstructions modifications) {
		if (modifications == null)
			return;

		psets.clear();
		edsources.clear();
		essources.clear();
		esmodules.clear();
		services.clear();
		modules.clear();
		outputs.clear();
		paths.clear();
		sequences.clear();
		tasks.clear();
		edaliases.clear();
		switchproducers.clear();
		contents.clear();
		streams.clear();
		datasets.clear();
		blocks.clear();

		if (!modifications.resolve(master))
			return;

		if (!modifications.doFilterAllPSets()) {
			Iterator<PSetParameter> it = master.psetIterator();
			while (it.hasNext()) {
				PSetParameter pset = it.next();
				if (!modifications.isInBlackList(pset))
					psets.add(pset);
			}
		}

		if (!modifications.doFilterAllEDSources()) {
			Iterator<EDSourceInstance> it = master.edsourceIterator();
			while (it.hasNext()) {
				EDSourceInstance edsource = it.next();
				if (!modifications.isInBlackList(edsource))
					edsources.add(edsource);
			}
		}
		if (modifications.doInsertEDSource()) {
			EDSourceInstance eds = modifications.edsourceToBeAdded();
			eds.updateParameter("fileNames", "vstring", modifications.edsourceFileNames());
			edsources.add(eds);
		}

		if (!modifications.doFilterAllESSources()) {
			Iterator<ESSourceInstance> it = master.essourceIterator();
			while (it.hasNext()) {
				ESSourceInstance essource = it.next();
				if (!modifications.isInBlackList(essource))
					essources.add(essource);
			}
		}

		if (!modifications.doFilterAllESModules()) {
			Iterator<ESModuleInstance> it = master.esmoduleIterator();
			while (it.hasNext()) {
				ESModuleInstance esmodule = it.next();
				if (!modifications.isInBlackList(esmodule))
					esmodules.add(esmodule);
			}
		}

		if (!modifications.doFilterAllServices()) {
			Iterator<ServiceInstance> it = master.serviceIterator();
			while (it.hasNext()) {
				ServiceInstance service = it.next();
				if (!modifications.isInBlackList(service))
					services.add(service);
			}
		}

		boolean hasOutputModule = false;

		if (!modifications.doFilterAllPaths()) {
			Iterator<Path> itP = master.pathIterator();
			while (itP.hasNext()) {
				Path path = itP.next();
				if (!modifications.isInBlackList(path)) {

					if (path.hasOutputModule())
						hasOutputModule = true;

					if (path.hasOutputModule() && modifications.doInsertOutputModule()) {

						Path copy = new Path(path.name());
						Iterator<Reference> it = path.entryIterator();
						while (it.hasNext()) {
							Reference entry = it.next();
							ModuleInstance outputModule = null;
							if (entry instanceof ModuleReference) {
								ModuleReference ref = (ModuleReference) entry;
								ModuleInstance inst = (ModuleInstance) ref.parent();
								if (inst.template().type().equals("OutputModule"))
									outputModule = inst;
							}
							if (outputModule != null) {
								ModuleInstance outputI = modifications.outputModuleToBeAdded(entry.name());
								outputI.updateParameter("SelectEvents", "PSet",
										outputModule.parameter("SelectEvents", "PSet").valueAsString());
								outputI.updateParameter("outputCommands", "vstring",
										outputModule.parameter("outputCommands", "vstring").valueAsString());

								outputI.createReference(copy, copy.entryCount());
							} else
								copy.insertEntry(copy.entryCount(), entry);
						}
						path = copy;
					}

					paths.add(path);
					Iterator<Sequence> itS = path.sequenceIterator();
					while (itS.hasNext()) {
						Sequence sequence = itS.next();
						if (!modifications.isUndefined(sequence) && !sequences.contains(sequence))
							sequences.add(sequence);
					}
					Iterator<Task> itT = path.taskIterator();
					while (itT.hasNext()) {
						Task task = itT.next();
						if (!modifications.isUndefined(task) && !tasks.contains(task))
							tasks.add(task);
					}
					Iterator<ModuleInstance> itM = path.moduleIterator();
					while (itM.hasNext()) {
						ModuleInstance module = itM.next();
						if (!modifications.isUndefined(module) && !modules.contains(module))
							modules.add(module);
					}
					Iterator<EDAliasInstance> itEDA = path.edAliasIterator();
					while (itEDA.hasNext()) {
						EDAliasInstance edAlias = itEDA.next();
						if (!modifications.isUndefined(edAlias) && !edaliases.contains(edAlias))
							edaliases.add(edAlias);
					}
					Iterator<SwitchProducer> itSP = path.switchProducerIterator();
					while (itSP.hasNext()) {
						SwitchProducer switchProducer = itSP.next();
						if (!modifications.isUndefined(switchProducer) && !switchproducers.contains(switchProducer))
							switchproducers.add(switchProducer);
					}
					Iterator<OutputModule> itOM = path.outputIterator();
					while (itOM.hasNext()) {
						OutputModule output = itOM.next();
						if (!modifications.isUndefined(output) && !outputs.contains(output))
							outputs.add(output);
					}
				}
			}
		}

		if (!hasOutputModule && modifications.doInsertOutputModule()) {
			Path out = new Path("output");
			ModuleInstance outputI = modifications.outputModuleToBeAdded("out");
			outputI.createReference(out, 0);
			out.setAsEndPath(true);
			paths.add(out);
		}

		Iterator<String> itS = modifications.requestedSequenceIterator();
		while (itS.hasNext()) {
			Sequence sequence = master.sequence(itS.next());
			if (sequence != null && sequences.indexOf(sequence) < 0)
				sequences.add(sequence);
		}

		Iterator<String> itT = modifications.requestedTaskIterator();
		while (itT.hasNext()) {
			Task task = master.task(itT.next());
			if (task != null && tasks.indexOf(task) < 0)
				tasks.add(task);
		}

		Iterator<String> itM = modifications.requestedModuleIterator();
		while (itM.hasNext()) {
			ModuleInstance module = master.module(itM.next());
			if (module != null && modules.indexOf(module) < 0)
				modules.add(module);
		}
		
		Iterator<String> itEDA = modifications.requestedEDAliasIterator();
		while (itEDA.hasNext()) {
			EDAliasInstance edalias = master.edAlias(itEDA.next());
			if (edalias != null && edaliases.indexOf(edalias) < 0)
				edaliases.add(edalias);
		}
		
		Iterator<String> itSP = modifications.requestedSwitchProducerIterator();
		while (itSP.hasNext()) {
			SwitchProducer switchProducer = master.switchProducer(itSP.next());
			if (switchProducer != null && switchproducers.indexOf(switchProducer) < 0)
				switchproducers.add(switchProducer);
		}

		Iterator<String> itOM = modifications.requestedOutputIterator();
		while (itOM.hasNext()) {
			OutputModule output = master.output(itOM.next());
			if (output != null && outputs.indexOf(output) < 0)
				outputs.add(output);
		}

		Iterator<String> itEC = modifications.requestedContentIterator();
		while (itEC.hasNext()) {
			EventContent content = master.content(itEC.next());
			if (content != null && contents.indexOf(content) < 0)
				contents.add(content);
		}

		Iterator<String> itST = modifications.requestedStreamIterator();
		while (itST.hasNext()) {
			Stream stream = master.stream(itST.next());
			if (stream != null && streams.indexOf(stream) < 0)
				streams.add(stream);
		}

		Iterator<String> itPD = modifications.requestedDatasetIterator();
		while (itPD.hasNext()) {
			PrimaryDataset dataset = master.dataset(itPD.next());
			if (dataset != null && datasets.indexOf(dataset) < 0)
				datasets.add(dataset);
		}

		Iterator<String> itB = modifications.blockIterator();
		while (itB.hasNext()) {
			String[] a = itB.next().split("::");
			String outputName = a[0];
			String[] paramNames = a[1].split(":");
			OutputModule output = master.output(outputName);
			if (output != null)
				blocks.add(new Block(output, paramNames));
		}

		isModified = true;
	}

	/**
	 * order sequences such that each sequence is defined before being referenced
	 */
	public Iterator<Sequence> orderedSequenceIterator() {
		ArrayList<Sequence> result = new ArrayList<Sequence>();
		Iterator<Sequence> itS = sequenceIterator();
		while (itS.hasNext()) {
			Sequence sequence = itS.next();
			int indexS = result.indexOf(sequence);
			if (indexS < 0) {
				indexS = result.size();
				result.add(sequence);
			}
			Iterator<Reference> itR = sequence.entryIterator();
			while (itR.hasNext()) {
				Reference reference = itR.next();
				Referencable parent = reference.parent();
				if (parent instanceof Sequence) {
					Sequence s = (Sequence) parent;
					if (isModified && !sequences.contains(s))
						continue;
					int indexR = result.indexOf(s);
					if (indexR < 0) {
						indexR = indexS;
						indexS++;
						result.add(indexR, s);
					} else if (indexR > indexS) {
						result.remove(indexR);
						indexR = indexS;
						indexS++;
						result.add(indexR, s);
					}
				}
			}
		}
		return result.iterator();
	}

	/**
	 * order tasks such that each task is defined before being referenced
	 */
	public Iterator<Task> orderedTaskIterator() {
		ArrayList<Task> result = new ArrayList<Task>();
		Iterator<Task> itT = taskIterator();
		while (itT.hasNext()) {
			Task task = itT.next();
			int indexT = result.indexOf(task);
			if (indexT < 0) {
				indexT = result.size();
				result.add(task);
			}
			Iterator<Reference> itR = task.entryIterator();
			while (itR.hasNext()) {
				Reference reference = itR.next();
				Referencable parent = reference.parent();
				if (parent instanceof Task) {
					Task t = (Task) parent;
					if (isModified && !tasks.contains(t))
						continue;
					int indexR = result.indexOf(t);
					if (indexR < 0) {
						indexR = indexT;
						indexT++;
						result.add(indexR, t);
					} else if (indexR > indexT) {
						result.remove(indexR);
						indexR = indexT;
						indexT++;
						result.add(indexR, t);
					}
				}
			}
		}
		return result.iterator();
	}
	
	
	/**
	 * order switch producers such that each switch producer is defined before being referenced
	 * BSATARIC: this method is probably obsolete since SP cannot contain SP in itself
	 */
	public Iterator<SwitchProducer> orderedSwitchProducerIterator() {
		ArrayList<SwitchProducer> result = new ArrayList<SwitchProducer>();
		Iterator<SwitchProducer> itSP = switchProducerIterator();
		while (itSP.hasNext()) {
			SwitchProducer switchProducer = itSP.next();
			int indexSP = result.indexOf(switchProducer);
			if (indexSP < 0) {
				indexSP = result.size();
				result.add(switchProducer);
			}
			Iterator<Reference> itR = switchProducer.entryIterator();
			while (itR.hasNext()) {
				Reference reference = itR.next();
				Referencable parent = reference.parent();
				if (parent instanceof SwitchProducer) {
					SwitchProducer sp = (SwitchProducer) parent;
					if (isModified && !switchproducers.contains(sp))
						continue;
					int indexR = result.indexOf(sp);
					if (indexR < 0) {
						indexR = indexSP;
						indexSP++;
						result.add(indexR, sp);
					} else if (indexR > indexSP) {
						result.remove(indexR);
						indexR = indexSP;
						indexSP++;
						result.add(indexR, sp);
					}
				}
			}
		}
		return result.iterator();
	}

	/** reset all modifications */
	public void reset() {
		modifications = new ModifierInstructions();

		psets.clear();
		edsources.clear();
		essources.clear();
		esmodules.clear();
		services.clear();
		modules.clear();
		outputs.clear();
		paths.clear();
		sequences.clear();
		tasks.clear();
		edaliases.clear();
		switchproducers.clear();
		contents.clear();
		streams.clear();
		datasets.clear();
		blocks.clear();

		isModified = false;
	}

	//
	// IConfiguration interface
	//

	/** name of the configuration */
	public String name() {
		return master.name();
	}

	/** parent directory of the configuration */
	public Directory parentDir() {
		return master.parentDir();
	}

	/** version of the configuration */
	public int version() {
		return master.version();
	}

	/** get configuration date of creation as a string */
	public String created() {
		return master.created();
	}

	/** get configuration creator */
	public String creator() {
		return master.creator();
	}

	/** release tag */
	public String releaseTag() {
		return master.releaseTag();
	}

	/** process name */
	public String processName() {
		return master.processName();
	}

	/** comment */
	public String comment() {
		return master.comment();
	}

	/** has the configuration changed w.r.t. the last version in the DB? */
	public boolean hasChanged() {
		return master.hasChanged();
	}

	/** indicate that the configuration has changed in memory */
	public void setHasChanged(boolean hasChanged) {
		master.setHasChanged(hasChanged);
	}

	/** total number of components of a certain type */
	public int componentCount(Class<?> c) {
		return master.componentCount(c);
	}

	/** check if the configuration is empty */
	public boolean isEmpty() {
		return master.isEmpty();
	}

	/** check if the provided string is already in use as a name */
	public boolean isUniqueQualifier(String qualifier) {
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
	public int unsetTrackedPSetParameterCount() {
		if (!isModified)
			return master.unsetTrackedPSetParameterCount();
		int result = 0;
		for (PSetParameter pset : psets)
			result += pset.unsetTrackedParameterCount();
		return result;
	}

	/** number of unsert tracked edsource parameters */
	public int unsetTrackedEDSourceParameterCount() {
		if (!isModified)
			return master.unsetTrackedEDSourceParameterCount();
		int result = 0;
		for (EDSourceInstance eds : edsources)
			result += eds.unsetTrackedParameterCount();
		return result;
	}

	/** number of unsert tracked essource parameters */
	public int unsetTrackedESSourceParameterCount() {
		if (!isModified)
			return master.unsetTrackedESSourceParameterCount();
		int result = 0;
		for (ESSourceInstance ess : essources)
			result += ess.unsetTrackedParameterCount();
		return result;
	}

	/** number of unsert tracked esmodule parameters */
	public int unsetTrackedESModuleParameterCount() {
		if (!isModified)
			return master.unsetTrackedESModuleParameterCount();
		int result = 0;
		for (ESModuleInstance esm : esmodules)
			result += esm.unsetTrackedParameterCount();
		return result;
	}

	/** number of unsert tracked service parameters */
	public int unsetTrackedServiceParameterCount() {
		if (!isModified)
			return master.unsetTrackedServiceParameterCount();
		int result = 0;
		for (ServiceInstance svc : services)
			result += svc.unsetTrackedParameterCount();
		return result;
	}

	/** number of unset tracked module parameters */
	public int unsetTrackedModuleParameterCount() {
		if (!isModified)
			return master.unsetTrackedModuleParameterCount();
		int result = 0;
		for (ModuleInstance mod : modules)
			result += mod.unsetTrackedParameterCount();
		return result;
	}
	
	/** number of unset tracked EDAlias parameters */
	public int unsetTrackedEDAliasParameterCount() {
		if (!isModified)
			return master.unsetTrackedEDAliasParameterCount();
		int result = 0;
		for (EDAliasInstance eda : edaliases)
			result += eda.unsetTrackedParameterCount();
		return result;
	}

	/** number of output modules not assigned to any path */
	public int unassignedOutputModuleCount() {
		if (!isModified)
			return master.unassignedOutputModuleCount();
		int result = 0;
		Iterator<OutputModule> itOM = outputIterator();
		while (itOM.hasNext())
			if (itOM.next().referenceCount() == 0)
				result++;
		return result;
	}

	/** number of paths unassigned to any stream */
	public int pathNotAssignedToStreamCount() {
		if (!isModified)
			return master.pathNotAssignedToStreamCount();
		int result = 0;
		if (streamCount() == 0)
			return result;
		for (Path p : paths)
			if (p.streamCount() == 0)
				result++;
		return result;
	}

	/** number of paths unassigned to any dataset */
	public int pathNotAssignedToDatasetCount() {
		if (!isModified)
			return master.pathNotAssignedToDatasetCount();
		int result = 0;
		Iterator<Path> itP = pathIterator();
		while (itP.hasNext()) {
			Path path = itP.next();
			if (path.isEndPath())
				continue;
			if (path.datasetCount() == 0)
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
		System.err.println("ConfigurationModifier::instance(): can't find '" + name + "'");
		return null;
	}

	/** number of global PSets */
	public int psetCount() {
		return (isModified) ? psets.size() : master.psetCount();
	}

	/** get i-th global PSet */
	public PSetParameter pset(int i) {
		return (isModified) ? psets.get(i) : master.pset(i);
	}

	/** get global PSet by name */
	public PSetParameter pset(String name) {
		if (!isModified)
			return master.pset(name);
		for (PSetParameter pset : psets)
			if (pset.name().equals(name))
				return pset;
		return null;
	}

	/** index of a certain global PSet */
	public int indexOfPSet(PSetParameter pset) {
		return (isModified) ? psets.indexOf(pset) : master.indexOfPSet(pset);
	}

	/** retrieve pset iterator */
	public Iterator<PSetParameter> psetIterator() {
		return (isModified) ? psets.iterator() : master.psetIterator();
	}

	/** number of EDSources */
	public int edsourceCount() {
		return (isModified) ? edsources.size() : master.edsourceCount();
	}

	/** get i-th EDSource */
	public EDSourceInstance edsource(int i) {
		return (isModified) ? edsources.get(i) : master.edsource(i);
	}

	/** get EDSource by name */
	public EDSourceInstance edsource(String name) {
		if (!isModified)
			return master.edsource(name);
		for (EDSourceInstance eds : edsources)
			if (eds.name().equals(name))
				return eds;
		return null;
	}

	/** index of a certain EDSource */
	public int indexOfEDSource(EDSourceInstance edsource) {
		return (isModified) ? edsources.indexOf(edsource) : master.indexOfEDSource(edsource);
	}

	/** retrieve edsource iterator */
	public Iterator<EDSourceInstance> edsourceIterator() {
		return (isModified) ? edsources.iterator() : master.edsourceIterator();
	}

	/** number of ESSources */
	public int essourceCount() {
		return (isModified) ? essources.size() : master.essourceCount();
	}

	/** get i-th ESSource */
	public ESSourceInstance essource(int i) {
		return (isModified) ? essources.get(i) : master.essource(i);
	}

	/** get ESSource by name */
	public ESSourceInstance essource(String name) {
		if (!isModified)
			return master.essource(name);
		for (ESSourceInstance ess : essources)
			if (ess.name().equals(name))
				return ess;
		return null;
	}

	/** index of a certain ESSource */
	public int indexOfESSource(ESSourceInstance essource) {
		return (isModified) ? essources.indexOf(essource) : master.indexOfESSource(essource);
	}

	/** retrieve essource iterator */
	public Iterator<ESSourceInstance> essourceIterator() {
		return (isModified) ? essources.iterator() : master.essourceIterator();
	}

	/** number of ESModules */
	public int esmoduleCount() {
		return (isModified) ? esmodules.size() : master.esmoduleCount();
	}

	/** get i-th ESModule */
	public ESModuleInstance esmodule(int i) {
		return (isModified) ? esmodules.get(i) : master.esmodule(i);
	}

	/** get ESModule by name */
	public ESModuleInstance esmodule(String name) {
		if (!isModified)
			return master.esmodule(name);
		for (ESModuleInstance esm : esmodules)
			if (esm.name().equals(name))
				return esm;
		return null;
	}

	/** index of a certain ESSource */
	public int indexOfESModule(ESModuleInstance esmodule) {
		return (isModified) ? esmodules.indexOf(esmodule) : master.indexOfESModule(esmodule);
	}

	/** retrieve esmodule iterator */
	public Iterator<ESModuleInstance> esmoduleIterator() {
		return (isModified) ? esmodules.iterator() : master.esmoduleIterator();
	}

	/** number of Services */
	public int serviceCount() {
		return (isModified) ? services.size() : master.serviceCount();
	}

	/** get i-th Service */
	public ServiceInstance service(int i) {
		return (isModified) ? services.get(i) : master.service(i);
	}

	/** get Service by name */
	public ServiceInstance service(String name) {
		if (!isModified)
			return master.service(name);
		for (ServiceInstance svc : services)
			if (svc.name().equals(name))
				return svc;
		return null;
	}

	/** index of a certain Service */
	public int indexOfService(ServiceInstance service) {
		return (isModified) ? services.indexOf(service) : master.indexOfService(service);
	}

	/** retrieve service iterator */
	public Iterator<ServiceInstance> serviceIterator() {
		return (isModified) ? services.iterator() : master.serviceIterator();
	}

	/** number of Modules */
	public int moduleCount() {
		return (isModified) ? modules.size() : master.moduleCount();
	}

	/** get i-th Module */
	public ModuleInstance module(int i) {
		return (isModified) ? modules.get(i) : master.module(i);
	}

	/** get Module by name */
	public ModuleInstance module(String moduleName) {
		return master.module(moduleName);
	}

	/** index of a certain Module */
	public int indexOfModule(ModuleInstance module) {
		return (isModified) ? modules.indexOf(module) : master.indexOfModule(module);
	}

	/** retrieve module iterator */
	public Iterator<ModuleInstance> moduleIterator() {
		return (isModified) ? modules.iterator() : master.moduleIterator();
	}
	
	/** number of EDAliases */
	public int edAliasCount() {
		return (isModified) ? edaliases.size() : master.edAliasCount();
	}

	/** get i-th EDAlias */
	public EDAliasInstance edAlias(int i) {
		return (isModified) ? edaliases.get(i) : master.edAlias(i);
	}

	/** get EDAlias by name */
	public EDAliasInstance edAlias(String edAliasName) {
		return master.edAlias(edAliasName);
	}
	
	/** index of a certain EDAlias */
	public int indexOfEDAlias(EDAliasInstance edAlias) {
		return (isModified) ? edaliases.indexOf(edAlias) : master.indexOfEDAlias(edAlias);
	}
	
	public Iterator<EDAliasInstance> edAliasIterator() {
		return (isModified) ? edaliases.iterator() : master.edAliasIterator();
	}
	
	/** number of switch producers */
	public int switchProducerCount() {
		return (isModified) ? switchproducers.size() : master.switchProducerCount();
	}

	/** get i-th switch producer */
	public SwitchProducer switchProducer(int i) {
		return (isModified) ? switchproducers.get(i) : master.switchProducer(i);
	}

	/** get switch producer by name */
	public SwitchProducer switchProducer(String switchProducerName) {
		return master.switchProducer(switchProducerName);
	}

	/** index of a certain switch producer */
	public int indexOfSwitchProducer(SwitchProducer switchProducer) {
		return (isModified) ? switchproducers.indexOf(switchProducer) : master.indexOfSwitchProducer(switchProducer);
	}

	/** retrieve switch producer iterator */
	public Iterator<SwitchProducer> switchProducerIterator() {
		return (isModified) ? switchproducers.iterator() : master.switchProducerIterator();
	}

	/** number of OutputModules */
	public int outputCount() {
		return (isModified) ? outputs.size() : master.outputCount();
	}

	/** get i-th OutputModule */
	public OutputModule output(int i) {
		return (isModified) ? outputs.get(i) : master.output(i);
	}

	/** get OutputModule by name */
	public OutputModule output(String outputName) {
		return master.output(outputName);
	}

	/** index of a certain OutputModule */
	public int indexOfOutput(OutputModule output) {
		return (isModified) ? outputs.indexOf(output) : master.indexOfOutput(output);
	}

	/** retrieve output iterator */
	public Iterator<OutputModule> outputIterator() {
		return (isModified) ? outputs.iterator() : master.outputIterator();
	}

	/** number of Paths */
	public int pathCount() {
		return (isModified) ? paths.size() : master.pathCount();
	}

	/** get i-th Path */
	public Path path(int i) {
		return (isModified) ? paths.get(i) : master.path(i);
	}

	/** geth Path by name */
	public Path path(String pathName) {
		return master.path(pathName);
	}

	/** index of a certain Path */
	public int indexOfPath(Path path) {
		return (isModified) ? paths.indexOf(path) : master.indexOfPath(path);
	}

	/** retrieve path iterator */
	public Iterator<Path> pathIterator() {
		return (isModified) ? paths.iterator() : master.pathIterator();
	}

	/** number of Sequences */
	public int sequenceCount() {
		return (isModified) ? sequences.size() : master.sequenceCount();
	}

	/** get i-th Sequence */
	public Sequence sequence(int i) {
		return (isModified) ? sequences.get(i) : master.sequence(i);
	}

	/** get Sequence by name */
	public Sequence sequence(String sequenceName) {
		return master.sequence(sequenceName);
	}

	/** index of a certain Sequence */
	public int indexOfSequence(Sequence sequence) {
		return (isModified) ? sequences.indexOf(sequence) : master.indexOfSequence(sequence);
	}

	/** retrieve sequence iterator */
	public Iterator<Sequence> sequenceIterator() {
		return (isModified) ? sequences.iterator() : master.sequenceIterator();
	}

	/** number of event contents */
	public int contentCount() {
		return (isModified) ? contents.size() : master.contentCount();
	}

	/** retrieve i-th event content */
	public EventContent content(int i) {
		return (isModified) ? contents.get(i) : master.content(i);
	}

	/** retrieve event content by name */
	public EventContent content(String contentName) {
		return master.content(contentName);
	}

	/** index of a certain event content */
	public int indexOfContent(EventContent content) {
		return (isModified) ? contents.indexOf(content) : master.indexOfContent(content);
	}

	/** retrieve event content iterator */
	public Iterator<EventContent> contentIterator() {
		return (isModified) ? contents.iterator() : master.contentIterator();
	}

	/** number of streams */
	public int streamCount() {
		return (isModified) ? streams.size() : master.streamCount();
	}

	/** retrieve i-th stream */
	public Stream stream(int i) {
		return (isModified) ? streams.get(i) : master.stream(i);
	}

	/** retrieve stream by name */
	public Stream stream(String streamName) {
		return master.stream(streamName);
	}

	/** index of a certain stream */
	public int indexOfStream(Stream stream) {
		return (isModified) ? streams.indexOf(stream) : master.indexOfStream(stream);
	}

	/** retrieve stream iterator */
	public Iterator<Stream> streamIterator() {
		return (isModified) ? streams.iterator() : master.streamIterator();
	}

	/** number of primary datasets */
	public int datasetCount() {
		return (isModified) ? datasets.size() : master.datasetCount();
	}

	/** retrieve i-th primary dataset */
	public PrimaryDataset dataset(int i) {
		return (isModified) ? datasets.get(i) : master.dataset(i);
	}

	/** retrieve primary dataset by name */
	public PrimaryDataset dataset(String datasetName) {
		return master.dataset(datasetName);
	}

	/** index of a certain primary dataset */
	public int indexOfDataset(PrimaryDataset dataset) {
		return (isModified) ? datasets.indexOf(dataset) : master.indexOfDataset(dataset);
	}

	/** retrieve primary dataset iterator */
	public Iterator<PrimaryDataset> datasetIterator() {
		return (isModified) ? datasets.iterator() : master.datasetIterator();
	}

	/** retrieve block iterator */
	public Iterator<Block> blockIterator() {
		return blocks.iterator();
	}

	/** add a block */
	public void insertBlock(Block block) {
		blocks.add(block);
	}

	/** number of Tasks */
	public int taskCount() {
		return (isModified) ? tasks.size() : master.taskCount();
	}

	/** get i-th Task */
	public Task task(int i) {
		return (isModified) ? tasks.get(i) : master.task(i);
	}

	/** get Task by name */
	public Task task(String taskName) {
		return master.task(taskName);
	}

	/** index of a certain Task */
	public int indexOfTask(Task task) {
		return (isModified) ? tasks.indexOf(task) : master.indexOfTask(task);
	}

	/** retrieve task iterator */
	public Iterator<Task> taskIterator() {
		return (isModified) ? tasks.iterator() : master.taskIterator();
	}

}

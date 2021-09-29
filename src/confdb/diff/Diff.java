package confdb.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import confdb.converter.ConfCache;
import confdb.converter.ConverterException;
import confdb.data.*;
import confdb.db.*;

/**
 * Diff
 * ----
 * @author Philipp Schieferdecker
 *
 * Determine differences between different components, typically but not
 * necessarily in different configurations (or versions).
 */
public class Diff {
	//
	// static member data
	//

	/** database instance */
	private static ConfDB database = null;

	//
	// member data
	//

	/** configurations to be compared */
	private IConfiguration config1;
	private IConfiguration config2;

	/** comparisons of the various components */
	private ArrayList<Comparison> psets = new ArrayList<Comparison>();
	private ArrayList<Comparison> edsources = new ArrayList<Comparison>();
	private ArrayList<Comparison> essources = new ArrayList<Comparison>();
	private ArrayList<Comparison> esmodules = new ArrayList<Comparison>();
	private ArrayList<Comparison> services = new ArrayList<Comparison>();
	private ArrayList<Comparison> paths = new ArrayList<Comparison>();
	private ArrayList<Comparison> sequences = new ArrayList<Comparison>();
	private ArrayList<Comparison> tasks = new ArrayList<Comparison>();
	private ArrayList<Comparison> switchproducers = new ArrayList<Comparison>();
	private ArrayList<Comparison> modules = new ArrayList<Comparison>();
	private ArrayList<Comparison> edaliases = new ArrayList<Comparison>();
	private ArrayList<Comparison> outputs = new ArrayList<Comparison>();
	private ArrayList<Comparison> contents = new ArrayList<Comparison>();
	private ArrayList<Comparison> streams = new ArrayList<Comparison>();
	private ArrayList<Comparison> datasets = new ArrayList<Comparison>();

	private HashMap<String, Comparison> containerMap = new HashMap<String, Comparison>();
	private HashMap<String, Comparison> instanceMap = new HashMap<String, Comparison>();

	//
	// construction
	//

	/** standard constructor */
	public Diff(IConfiguration config1, IConfiguration config2) {
		this.config1 = config1;
		this.config2 = config2;
	}

	//
	// member functions
	//

	/** get old configuration identifier */
	public String configName1() {
		return config1.toString();
	}

	/** get new configuration identifier */
	public String configName2() {
		return config2.toString();
	}

	/** compare the two configurations and store all non-identical comparisons */
	public void compare() {
		// global parameter sets
		Iterator<PSetParameter> itPSet2 = config2.psetIterator();
		while (itPSet2.hasNext()) {
			PSetParameter pset2 = itPSet2.next();
			PSetParameter pset1 = config1.pset(pset2.name());
			Comparison c = comparePSets(pset1, pset2);
			if (!c.isIdentical())
				psets.add(c);
		}
		Iterator<PSetParameter> itPSet1 = config1.psetIterator();
		while (itPSet1.hasNext()) {
			PSetParameter pset1 = itPSet1.next();
			if (config2.pset(pset1.name()) == null)
				psets.add(comparePSets(pset1, null));
		}

		// EDSources
		Iterator<EDSourceInstance> itEDS2 = config2.edsourceIterator();
		while (itEDS2.hasNext()) {
			EDSourceInstance eds2 = itEDS2.next();
			EDSourceInstance eds1 = config1.edsource(eds2.name());
			Comparison c = compareInstances(eds1, eds2);
			if (!c.isIdentical())
				edsources.add(c);
		}
		Iterator<EDSourceInstance> itEDS1 = config1.edsourceIterator();
		while (itEDS1.hasNext()) {
			EDSourceInstance eds1 = itEDS1.next();
			if (config2.edsource(eds1.name()) == null)
				edsources.add(compareInstances(eds1, null));
		}

		// ESSources
		Iterator<ESSourceInstance> itESS2 = config2.essourceIterator();
		while (itESS2.hasNext()) {
			ESSourceInstance ess2 = itESS2.next();
			ESSourceInstance ess1 = config1.essource(ess2.name());
			Comparison c = compareInstances(ess1, ess2);
			if (!c.isIdentical())
				essources.add(c);
		}
		Iterator<ESSourceInstance> itESS1 = config1.essourceIterator();
		while (itESS1.hasNext()) {
			ESSourceInstance ess1 = itESS1.next();
			if (config2.essource(ess1.name()) == null)
				essources.add(compareInstances(ess1, null));
		}

		// ESModules
		Iterator<ESModuleInstance> itESM2 = config2.esmoduleIterator();
		while (itESM2.hasNext()) {
			ESModuleInstance esm2 = itESM2.next();
			ESModuleInstance esm1 = config1.esmodule(esm2.name());
			Comparison c = compareInstances(esm1, esm2);
			if (!c.isIdentical())
				esmodules.add(c);
		}
		Iterator<ESModuleInstance> itESM1 = config1.esmoduleIterator();
		while (itESM1.hasNext()) {
			ESModuleInstance esm1 = itESM1.next();
			if (config2.esmodule(esm1.name()) == null)
				esmodules.add(compareInstances(esm1, null));
		}

		// Services
		Iterator<ServiceInstance> itSvc2 = config2.serviceIterator();
		while (itSvc2.hasNext()) {
			ServiceInstance svc2 = itSvc2.next();
			ServiceInstance svc1 = config1.service(svc2.name());
			Comparison c = compareInstances(svc1, svc2);
			if (!c.isIdentical())
				services.add(c);
		}
		Iterator<ServiceInstance> itSvc1 = config1.serviceIterator();
		while (itSvc1.hasNext()) {
			ServiceInstance svc1 = itSvc1.next();
			if (config2.service(svc1.name()) == null)
				services.add(compareInstances(svc1, null));
		}

		// Modules
		Iterator<ModuleInstance> itMod2 = config2.moduleIterator();
		while (itMod2.hasNext()) {
			ModuleInstance mod2 = itMod2.next();
			ModuleInstance mod1 = config1.module(mod2.name());
			Comparison c = compareInstances(mod1, mod2);
			if (!c.isIdentical())
				modules.add(c);
		}
		Iterator<ModuleInstance> itMod1 = config1.moduleIterator();
		while (itMod1.hasNext()) {
			ModuleInstance mod1 = itMod1.next();
			if (config2.module(mod1.name()) == null)
				modules.add(compareInstances(mod1, null));
		}
		
		// EDAliases
		Iterator<EDAliasInstance> itEDA2 = config2.edAliasIterator();
		while (itEDA2.hasNext()) {
			EDAliasInstance eda2 = itEDA2.next();
			EDAliasInstance eda1 = config1.edAlias(eda2.name());
			Comparison c = compareInstances(eda1, eda2);  //pretty convoluted how this works
			if (!c.isIdentical())
				edaliases.add(c);
		}
		Iterator<EDAliasInstance> itEDA1 = config1.edAliasIterator();
		while (itEDA1.hasNext()) {
			EDAliasInstance eda1 = itEDA1.next();
			if (config2.module(eda1.name()) == null)
				edaliases.add(compareInstances(eda1, null));
		}


		// Outputs
		Iterator<OutputModule> itOut2 = config2.outputIterator();
		while (itOut2.hasNext()) {
			OutputModule out2 = itOut2.next();
			OutputModule out1 = config1.output(out2.name());
			Comparison c = compareOutputModules(out1, out2);
			if (!c.isIdentical())
				outputs.add(c);
		}
		Iterator<OutputModule> itOut1 = config1.outputIterator();
		while (itOut1.hasNext()) {
			OutputModule out1 = itOut1.next();
			if (config2.output(out1.name()) == null)
				outputs.add(compareOutputModules(out1, null));
		}

		// Sequences
		Iterator<Sequence> itSeq2 = config2.sequenceIterator();
		while (itSeq2.hasNext()) {
			Sequence seq2 = itSeq2.next();
			Sequence seq1 = config1.sequence(seq2.name());
			Comparison c = compareContainers(seq1, seq2);
			if (!c.isIdentical())
				sequences.add(c);
		}
		Iterator<Sequence> itSeq1 = config1.sequenceIterator();
		while (itSeq1.hasNext()) {
			Sequence seq1 = itSeq1.next();
			if (config2.sequence(seq1.name()) == null)
				sequences.add(compareContainers(seq1, null));
		}

		// Tasks
		Iterator<Task> itTas2 = config2.taskIterator();
		while (itTas2.hasNext()) {
			Task tas2 = itTas2.next();
			Task tas1 = config1.task(tas2.name());
			Comparison c = compareContainers(tas1, tas2);
			if (!c.isIdentical())
				tasks.add(c);
		}
		Iterator<Task> itTas1 = config1.taskIterator();
		while (itTas1.hasNext()) {
			Task tas1 = itTas1.next();
			if (config2.task(tas1.name()) == null)
				tasks.add(compareContainers(tas1, null));
		}
		
		// Switch Producers
		Iterator<SwitchProducer> itSP2 = config2.switchProducerIterator();
		while (itSP2.hasNext()) {
			SwitchProducer sp2 = itSP2.next();
			SwitchProducer sp1 = config1.switchProducer(sp2.name());
			Comparison c = compareContainers(sp1, sp2);
			if (!c.isIdentical())
				switchproducers.add(c);
		}
		Iterator<SwitchProducer> itSP1 = config1.switchProducerIterator();
		while (itSP1.hasNext()) {
			SwitchProducer sp1 = itSP1.next();
			if (config2.switchProducer(sp1.name()) == null)
				switchproducers.add(compareContainers(sp1, null));
		}

		// Paths
		Iterator<Path> itPath2 = config2.pathIterator();
		while (itPath2.hasNext()) {
			Path path2 = itPath2.next();
			Path path1 = config1.path(path2.name());
			Comparison c = compareContainers(path1, path2);
			if (!c.isIdentical())
				paths.add(c);
		}
		Iterator<Path> itPath1 = config1.pathIterator();
		while (itPath1.hasNext()) {
			Path path1 = itPath1.next();
			if (config2.path(path1.name()) == null)
				paths.add(compareContainers(path1, null));
		}

		// EventContents
		Iterator<EventContent> itEventContent2 = config2.contentIterator();
		while (itEventContent2.hasNext()) {
			EventContent ec2 = itEventContent2.next();
			EventContent ec1 = config1.content(ec2.name());
			Comparison c = compareEventContents(ec1, ec2);
			if (!c.isIdentical())
				contents.add(c);
		}
		Iterator<EventContent> itEventContent1 = config1.contentIterator();
		while (itEventContent1.hasNext()) {
			EventContent ec1 = itEventContent1.next();
			if (config2.content(ec1.name()) == null)
				contents.add(compareEventContents(ec1, null));
		}

		// Streams
		Iterator<Stream> itStream2 = config2.streamIterator();
		while (itStream2.hasNext()) {
			Stream s2 = itStream2.next();
			Stream s1 = config1.stream(s2.name());
			Comparison c = compareStreams(s1, s2);
			if (!c.isIdentical())
				streams.add(c);
		}
		Iterator<Stream> itStream1 = config1.streamIterator();
		while (itStream1.hasNext()) {
			Stream s1 = itStream1.next();
			if (config2.stream(s1.name()) == null)
				streams.add(compareStreams(s1, null));
		}

		// Datasets
		Iterator<PrimaryDataset> itDataset2 = config2.datasetIterator();
		while (itDataset2.hasNext()) {
			PrimaryDataset dataset2 = itDataset2.next();
			PrimaryDataset dataset1 = config1.dataset(dataset2.name());
			Comparison c = compareDatasets(dataset1, dataset2);
			if (!c.isIdentical())
				datasets.add(c);
		}
		Iterator<PrimaryDataset> itDataset1 = config1.datasetIterator();
		while (itDataset1.hasNext()) {
			PrimaryDataset dataset1 = itDataset1.next();
			if (config2.dataset(dataset1.name()) == null)
				datasets.add(compareDatasets(dataset1, null));
		}
	}

	/** Compare a subset of components of the same type. */
	/** Usage: after import all components. */
	public void compare(String type, ArrayList<String> items) {
		if (type.equalsIgnoreCase("Path")) {
			for (int i = 0; i < items.size(); i++) {
				Path path2 = config1.path(items.get(i));
				Path path1 = config2.path(items.get(i));
				Comparison c = compareContainers(path1, path2);
				if (!c.isIdentical())
					paths.add(c);
			}
		} else if (type.equalsIgnoreCase("Sequence")) {
			for (int i = 0; i < items.size(); i++) {
				Sequence seq1 = config1.sequence(items.get(i));
				Sequence seq2 = config2.sequence(items.get(i));
				Comparison c = compareContainers(seq1, seq2);
				if (!c.isIdentical())
					sequences.add(c);
			}
		} else if (type.equalsIgnoreCase("Task")) {
			for (int i = 0; i < items.size(); i++) {
				Task tas1 = config1.task(items.get(i));
				Task tas2 = config2.task(items.get(i));
				Comparison c = compareContainers(tas1, tas2);
				if (!c.isIdentical())
					tasks.add(c);
			}
		} else if (type.equalsIgnoreCase("SwitchProducer")) {
			for (int i = 0; i < items.size(); i++) {
				SwitchProducer sp1 = config1.switchProducer(items.get(i));
				SwitchProducer sp2 = config2.switchProducer(items.get(i));
				Comparison c = compareContainers(sp1, sp2);
				if (!c.isIdentical())
					switchproducers.add(c);
			}
		}
		else if (type.equalsIgnoreCase("EDSource")) {
			for (int i = 0; i < items.size(); i++) {
				EDSourceInstance edsold = config1.edsource(items.get(i));
				EDSourceInstance edsnew = config2.edsource(items.get(i));
				Comparison c = compareInstances(edsold, edsnew);
				if (!c.isIdentical())
					edsources.add(c);
			}
		} else if (type.equalsIgnoreCase("ESSource")) {
			for (int i = 0; i < items.size(); i++) {
				ESSourceInstance essold = config1.essource(items.get(i));
				ESSourceInstance essnew = config2.essource(items.get(i));
				Comparison c = compareInstances(essold, essnew);
				if (!c.isIdentical())
					essources.add(c);
			}
		} else if (type.equalsIgnoreCase("ESModule")) {
			for (int i = 0; i < items.size(); i++) {
				ESModuleInstance esmodold = config1.esmodule(items.get(i));
				ESModuleInstance esmodnew = config2.esmodule(items.get(i));
				Comparison c = compareInstances(esmodold, esmodnew);
				if (!c.isIdentical())
					esmodules.add(c);
			}
		} else if (type.equalsIgnoreCase("Service")) {
			for (int i = 0; i < items.size(); i++) {
				ServiceInstance servold = config1.service(items.get(i));
				ServiceInstance servnew = config2.service(items.get(i));
				Comparison c = compareInstances(servold, servnew);
				if (!c.isIdentical())
					services.add(c);
			}
		} else if (type.equalsIgnoreCase("PSet")) {
			for (int i = 0; i < items.size(); i++) {
				PSetParameter pset1 = config1.pset(items.get(i));
				PSetParameter pset2 = config2.pset(items.get(i));
				Comparison c = comparePSets(pset1, pset2);
				if (!c.isIdentical())
					psets.add(c);
			}
		} else {
			// by default:
			for (int i = 0; i < items.size(); i++) {
				String search = type + ":" + items.get(i);
				this.compare(search);
			}
		}
	}

	/** compare specific components */
	public void compare(String search) {
		String type = search.split(":")[0];

		if (search.split(":").length > 1) {
			// many names to compare
			String[] names = search.split(":")[1].split(",");
			String oldName = names[0];
			String newName = (names.length > 1) ? names[1] : oldName;

			if (type.equalsIgnoreCase("PSet")) {
				PSetParameter psetold = config1.pset(oldName);
				PSetParameter psetnew = config2.pset(newName);
				Comparison c = comparePSets(psetold, psetnew);
				if (!c.isIdentical())
					psets.add(c);
			} else if (type.equalsIgnoreCase("EDSource") || type.equalsIgnoreCase("eds")) {
				EDSourceInstance edsold = config1.edsource(oldName);
				EDSourceInstance edsnew = config2.edsource(newName);
				Comparison c = compareInstances(edsold, edsnew);
				if (!c.isIdentical())
					edsources.add(c);
			} else if (type.equalsIgnoreCase("ESSource") || type.equalsIgnoreCase("ess")) {
				ESSourceInstance essold = config1.essource(oldName);
				ESSourceInstance essnew = config2.essource(newName);
				Comparison c = compareInstances(essold, essnew);
				if (!c.isIdentical())
					essources.add(c);
			} else if (type.equalsIgnoreCase("ESModule") || type.equalsIgnoreCase("esm")) {
				ESModuleInstance esmold = config1.esmodule(oldName);
				ESModuleInstance esmnew = config2.esmodule(newName);
				Comparison c = compareInstances(esmold, esmnew);
				if (!c.isIdentical())
					esmodules.add(c);
			} else if (type.equalsIgnoreCase("Service") || type.equalsIgnoreCase("svc")) {
				ServiceInstance svcold = config1.service(oldName);
				ServiceInstance svcnew = config2.service(newName);
				Comparison c = compareInstances(svcold, svcnew);
				if (!c.isIdentical())
					services.add(c);
			} else if (type.equalsIgnoreCase("Module") || type.equalsIgnoreCase("m")) {
				ModuleInstance mold = config1.module(oldName);
				ModuleInstance mnew = config2.module(newName);
				Comparison c = compareInstances(mold, mnew);
				if (!c.isIdentical())
					modules.add(c);
			} else if (type.equalsIgnoreCase("EDAlias") || type.equalsIgnoreCase("eda")) {
				EDAliasInstance edaold = config1.edAlias(oldName);
				EDAliasInstance edanew = config2.edAlias(newName);
				Comparison c = compareInstances(edaold, edanew);
				if (!c.isIdentical())
					edaliases.add(c);
			} else if (type.equalsIgnoreCase("OutputModule") || type.equalsIgnoreCase("om")) {
				OutputModule omold = config1.output(oldName);
				OutputModule omnew = config2.output(newName);
				Comparison c = compareOutputModules(omold, omnew);
				if (!c.isIdentical())
					modules.add(c);
			} else if (type.equalsIgnoreCase("EventContent") || type.equalsIgnoreCase("ec")) {
				EventContent ecold = config1.content(oldName);
				EventContent ecnew = config2.content(newName);
				Comparison c = compareEventContents(ecold, ecnew);
				if (!c.isIdentical())
					contents.add(c);
			} else if (type.equalsIgnoreCase("Path") || type.equalsIgnoreCase("p")) {
				Path pold = config1.path(oldName);
				Path pnew = config2.path(newName);
				Comparison c = compareContainers(pold, pnew);
				if (!c.isIdentical()) {
					paths.add(c);
					Iterator<Comparison> it = c.recursiveComparisonIterator();
					while (it.hasNext()) {
						Comparison cc = it.next();
						if (cc instanceof ContainerComparison)
							sequences.add(cc);
						else if (cc instanceof InstanceComparison)
							modules.add(cc);
					}
				}
			} else if (type.equalsIgnoreCase("Sequence") || type.equalsIgnoreCase("s")) {
				Sequence sold = config1.sequence(oldName);
				Sequence snew = config2.sequence(newName);
				Comparison c = compareContainers(sold, snew);
				if (!c.isIdentical()) {
					sequences.add(c);
					Iterator<Comparison> it = c.recursiveComparisonIterator();
					while (it.hasNext()) {
						Comparison cc = it.next();
						if (cc instanceof ContainerComparison)
							sequences.add(cc);
						else if (cc instanceof InstanceComparison)
							modules.add(cc);
					}
				}
			} else if (type.equalsIgnoreCase("Task") || type.equalsIgnoreCase("t")) {
				Task told = config1.task(oldName);
				Task tnew = config2.task(newName);
				Comparison c = compareContainers(told, tnew); // BSATARIC: black box
				if (!c.isIdentical()) {
					tasks.add(c);
					Iterator<Comparison> it = c.recursiveComparisonIterator();
					while (it.hasNext()) {
						Comparison cc = it.next();
						if (cc instanceof ContainerComparison)
							tasks.add(cc);
						else if (cc instanceof InstanceComparison)
							modules.add(cc);
					}
				}
			} else if (type.equalsIgnoreCase("SwitchProducer") || type.equalsIgnoreCase("sp")) {
				SwitchProducer spold = config1.switchProducer(oldName);
				SwitchProducer spnew = config2.switchProducer(newName);
				Comparison c = compareContainers(spold, spnew);
				if (!c.isIdentical()) {
					switchproducers.add(c);
					Iterator<Comparison> it = c.recursiveComparisonIterator();
					while (it.hasNext()) {
						InstanceComparison icc = (InstanceComparison) it.next(); //switchproducers can only have EDProducers or aliases
						if (icc.isInstanceOf(ModuleInstance.class))
							modules.add(icc);
						else if (icc.isInstanceOf(EDAliasInstance.class))
							edaliases.add(icc);
					}
				}
			} else if (type.equalsIgnoreCase("Stream")) {
				Stream sold = config1.stream(oldName);
				Stream snew = config2.stream(newName);
				Comparison c = compareStreams(sold, snew);
				if (!c.isIdentical())
					streams.add(c);
			} else if (type.equalsIgnoreCase("Dataset") || type.equalsIgnoreCase("d")) {
				PrimaryDataset dold = config1.dataset(oldName);
				PrimaryDataset dnew = config2.dataset(newName);
				Comparison c = compareDatasets(dold, dnew);
				if (!c.isIdentical())
					datasets.add(c);
			}

		}
	}

	/** compare all paths and store all non-identical comparisons */
	public void comparePaths() {
		Iterator<Path> itPath2 = config2.pathIterator();
		while (itPath2.hasNext()) {
			Path path2 = itPath2.next();
			Path path1 = config1.path(path2.name());
			Comparison c = compareContainers(path1, path2);
			if (!c.isIdentical())
				paths.add(c);
		}
		Iterator<Path> itPath1 = config1.pathIterator();
		while (itPath1.hasNext()) {
			Path path1 = itPath1.next();
			if (config2.path(path1.name()) == null)
				paths.add(compareContainers(path1, null));
		}
	}

	/**
	 * comparePathsIgnoreStreams
	 * ---------------------------------------------------------------- Compare all
	 * paths and store all non-identical comparisons NOTE: This method ignores the
	 * Streams by making use of the constructor "compareContainersIgnoreStreams".
	 * This was originally designed to ignore extra differences in the previous
	 * analysis of DeepImport feature.
	 */
	public void comparePathsIgnoreStreams() {
		Iterator<Path> itPath2 = config2.pathIterator();
		while (itPath2.hasNext()) {
			Path path2 = itPath2.next();
			Path path1 = config1.path(path2.name());
			Comparison c = compareContainersIgnoreStreams(path1, path2);
			if (!c.isIdentical())
				paths.add(c);
		}
		Iterator<Path> itPath1 = config1.pathIterator();
		while (itPath1.hasNext()) {
			Path path1 = itPath1.next();
			if (config2.path(path1.name()) == null)
				paths.add(compareContainersIgnoreStreams(path1, null));
		}
	}

	/** compare all parameter sets and store all non-identical comparisons */
	public void comparePSets() {
		// global parameter sets
		Iterator<PSetParameter> itPSet2 = config2.psetIterator();
		while (itPSet2.hasNext()) {
			PSetParameter pset2 = itPSet2.next();
			PSetParameter pset1 = config1.pset(pset2.name());
			Comparison c = comparePSets(pset1, pset2);
			if (!c.isIdentical())
				psets.add(c);
		}
		Iterator<PSetParameter> itPSet1 = config1.psetIterator();
		while (itPSet1.hasNext()) {
			PSetParameter pset1 = itPSet1.next();
			if (config2.pset(pset1.name()) == null)
				psets.add(comparePSets(pset1, null));
		}
	}

	/** compare all EDSources and store all non-identical comparisons */
	public void compareEDSources() {
		// EDSources
		Iterator<EDSourceInstance> itEDS2 = config2.edsourceIterator();
		while (itEDS2.hasNext()) {
			EDSourceInstance eds2 = itEDS2.next();
			EDSourceInstance eds1 = config1.edsource(eds2.name());
			Comparison c = compareInstances(eds1, eds2);
			if (!c.isIdentical())
				edsources.add(c);
		}
		Iterator<EDSourceInstance> itEDS1 = config1.edsourceIterator();
		while (itEDS1.hasNext()) {
			EDSourceInstance eds1 = itEDS1.next();
			if (config2.edsource(eds1.name()) == null)
				edsources.add(compareInstances(eds1, null));
		}
	}

	/** compare all ESSources and store all non-identical comparisons */
	public void compareESSources() {
		// ESSources
		Iterator<ESSourceInstance> itESS2 = config2.essourceIterator();
		while (itESS2.hasNext()) {
			ESSourceInstance ess2 = itESS2.next();
			ESSourceInstance ess1 = config1.essource(ess2.name());
			Comparison c = compareInstances(ess1, ess2);
			if (!c.isIdentical())
				essources.add(c);
		}
		Iterator<ESSourceInstance> itESS1 = config1.essourceIterator();
		while (itESS1.hasNext()) {
			ESSourceInstance ess1 = itESS1.next();
			if (config2.essource(ess1.name()) == null)
				essources.add(compareInstances(ess1, null));
		}
	}

	/** compare all ESModules and store all non-identical comparisons */
	public void compareESModules() {
		// ESModules
		Iterator<ESModuleInstance> itESM2 = config2.esmoduleIterator();
		while (itESM2.hasNext()) {
			ESModuleInstance esm2 = itESM2.next();
			ESModuleInstance esm1 = config1.esmodule(esm2.name());
			Comparison c = compareInstances(esm1, esm2);
			if (!c.isIdentical())
				esmodules.add(c);
		}
		Iterator<ESModuleInstance> itESM1 = config1.esmoduleIterator();
		while (itESM1.hasNext()) {
			ESModuleInstance esm1 = itESM1.next();
			if (config2.esmodule(esm1.name()) == null)
				esmodules.add(compareInstances(esm1, null));
		}
	}

	/** compare all Services and store all non-identical comparisons */
	public void compareServices() {
		// Services
		Iterator<ServiceInstance> itSvc2 = config2.serviceIterator();
		while (itSvc2.hasNext()) {
			ServiceInstance svc2 = itSvc2.next();
			ServiceInstance svc1 = config1.service(svc2.name());
			Comparison c = compareInstances(svc1, svc2);
			if (!c.isIdentical())
				services.add(c);
		}
		Iterator<ServiceInstance> itSvc1 = config1.serviceIterator();
		while (itSvc1.hasNext()) {
			ServiceInstance svc1 = itSvc1.next();
			if (config2.service(svc1.name()) == null)
				services.add(compareInstances(svc1, null));
		}
	}

	/** compare all Modules and store all non-identical comparisons */
	public void compareModules() {
		// Modules
		Iterator<ModuleInstance> itMod2 = config2.moduleIterator();
		while (itMod2.hasNext()) {
			ModuleInstance mod2 = itMod2.next();
			ModuleInstance mod1 = config1.module(mod2.name());
			Comparison c = compareInstances(mod1, mod2);
			if (!c.isIdentical())
				modules.add(c);
		}
		Iterator<ModuleInstance> itMod1 = config1.moduleIterator();
		while (itMod1.hasNext()) {
			ModuleInstance mod1 = itMod1.next();
			if (config2.module(mod1.name()) == null)
				modules.add(compareInstances(mod1, null));
		}
	}
	
	/** compare all EDAliases and store all non-identical comparisons */
	public void compareEDAliases() {
		// EDAliases
		Iterator<EDAliasInstance> itEDA2 = config2.edAliasIterator();
		while (itEDA2.hasNext()) {
			EDAliasInstance eda2 = itEDA2.next();
			EDAliasInstance eda1 = config1.edAlias(eda2.name());
			Comparison c = compareInstances(eda1, eda2);
			if (!c.isIdentical())
				edaliases.add(c);
		}
		Iterator<EDAliasInstance> itEDA1 = config1.edAliasIterator();
		while (itEDA1.hasNext()) {
			EDAliasInstance eda1 = itEDA1.next();
			if (config2.edAlias(eda1.name()) == null)
				edaliases.add(compareInstances(eda1, null));
		}
	}

	/** compare all Output Modules and store all non-identical comparisons */
	public void compareOutputModules() {
		// Outputs
		Iterator<OutputModule> itOut2 = config2.outputIterator();
		while (itOut2.hasNext()) {
			OutputModule out2 = itOut2.next();
			OutputModule out1 = config1.output(out2.name());
			Comparison c = compareOutputModules(out1, out2);
			if (!c.isIdentical())
				outputs.add(c);
		}
		Iterator<OutputModule> itOut1 = config1.outputIterator();
		while (itOut1.hasNext()) {
			OutputModule out1 = itOut1.next();
			if (config2.output(out1.name()) == null)
				outputs.add(compareOutputModules(out1, null));
		}
	}

	/** compare all Sequences and store all non-identical comparisons */
	public void compareSequences() {
		// Sequences
		Iterator<Sequence> itSeq2 = config2.sequenceIterator();
		while (itSeq2.hasNext()) {
			Sequence seq2 = itSeq2.next();
			Sequence seq1 = config1.sequence(seq2.name());
			Comparison c = compareContainers(seq1, seq2);
			if (!c.isIdentical())
				sequences.add(c);
		}
		Iterator<Sequence> itSeq1 = config1.sequenceIterator();
		while (itSeq1.hasNext()) {
			Sequence seq1 = itSeq1.next();
			if (config2.sequence(seq1.name()) == null)
				sequences.add(compareContainers(seq1, null));
		}
	}

	/**
	 * compareSequencesIgnoreStreams
	 * ---------------------------------------------------------------- Compare all
	 * Sequences and store all non-identical comparisons NOTE: This method ignores
	 * the Streams by making use of the constructor
	 * "compareContainersIgnoreStreams". This was originally designed to ignore
	 * extra differences in the previous analysis of DeepImport feature.
	 */
	public void compareSequencesIgnoreStreams() {
		// Sequences
		Iterator<Sequence> itSeq2 = config2.sequenceIterator();
		while (itSeq2.hasNext()) {
			Sequence seq2 = itSeq2.next();
			Sequence seq1 = config1.sequence(seq2.name());
			Comparison c = compareContainersIgnoreStreams(seq1, seq2);
			if (!c.isIdentical())
				sequences.add(c);
		}
		Iterator<Sequence> itSeq1 = config1.sequenceIterator();
		while (itSeq1.hasNext()) {
			Sequence seq1 = itSeq1.next();
			if (config2.sequence(seq1.name()) == null)
				sequences.add(compareContainersIgnoreStreams(seq1, null));
		}
	}
	
	/** compare all Tasks and store all non-identical comparisons */
	public void compareTasks() {
		// Tasks
		Iterator<Task> itTas2 = config2.taskIterator();
		while (itTas2.hasNext()) {
			Task tas2 = itTas2.next();
			Task tas1 = config1.task(tas2.name());
			Comparison c = compareContainers(tas1, tas2);
			if (!c.isIdentical())
				tasks.add(c);
		}
		Iterator<Task> itTas1 = config1.taskIterator();
		while (itTas1.hasNext()) {
			Task tas1 = itTas1.next();
			if (config2.task(tas1.name()) == null)
				tasks.add(compareContainers(tas1, null));
		}
	}

	/**
	 * compareTasksIgnoreStreams
	 * ---------------------------------------------------------------- Compare all
	 * Tasks and store all non-identical comparisons NOTE: This method ignores the
	 * Streams by making use of the constructor "compareContainersIgnoreStreams".
	 * This was originally designed to ignore extra differences in the previous
	 * analysis of DeepImport feature.
	 */
	public void compareTasksIgnoreStreams() {
		// Tasks
		Iterator<Task> itTas2 = config2.taskIterator();
		while (itTas2.hasNext()) {
			Task tas2 = itTas2.next();
			Task tas1 = config1.task(tas2.name());
			Comparison c = compareContainersIgnoreStreams(tas1, tas2);
			if (!c.isIdentical())
				tasks.add(c);
		}
		Iterator<Task> itTas1 = config1.taskIterator();
		while (itTas1.hasNext()) {
			Task tas1 = itTas1.next();
			if (config2.task(tas1.name()) == null)
				tasks.add(compareContainersIgnoreStreams(tas1, null));
		}
	}
	
	/** compare all SwitchProducers and store all non-identical comparisons */
	public void compareSwitchProducers() {
		// Switch producers
		Iterator<SwitchProducer> itSP2 = config2.switchProducerIterator();
		while (itSP2.hasNext()) {
			SwitchProducer sp2 = itSP2.next();
			SwitchProducer sp1 = config1.switchProducer(sp2.name());
			Comparison c = compareContainers(sp1, sp2);
			if (!c.isIdentical())
				switchproducers.add(c);
		}
		Iterator<SwitchProducer> itSP1 = config1.switchProducerIterator();
		while (itSP1.hasNext()) {
			SwitchProducer sp1 = itSP1.next();
			if (config2.switchProducer(sp1.name()) == null)
				switchproducers.add(compareContainers(sp1, null));
		}
	}

	/**
	 * compareSwitchProducersIgnoreStreams
	 * ---------------------------------------------------------------- Compare all
	 * SwitchProducers and store all non-identical comparisons NOTE: This method ignores the
	 * Streams by making use of the constructor "compareContainersIgnoreStreams".
	 * This was originally designed to ignore extra differences in the previous
	 * analysis of DeepImport feature.
	 */
	public void compareSwitchProducersIgnoreStreams() {
		// SwitchProducers
		Iterator<SwitchProducer> itSP2 = config2.switchProducerIterator();
		while (itSP2.hasNext()) {
			SwitchProducer sp2 = itSP2.next();
			SwitchProducer sp1 = config1.switchProducer(sp2.name());
			Comparison c = compareContainersIgnoreStreams(sp1, sp2);
			if (!c.isIdentical())
				switchproducers.add(c);
		}
		Iterator<SwitchProducer> itSP1 = config1.switchProducerIterator();
		while (itSP1.hasNext()) {
			SwitchProducer sp1 = itSP1.next();
			if (config2.switchProducer(sp1.name()) == null)
				switchproducers.add(compareContainersIgnoreStreams(sp1, null));
		}
	}
	

	/** compare all EventContents sets and store all non-identical comparisons */
	public void compareEventContents() {
		// EventContents
		Iterator<EventContent> itEventContent2 = config2.contentIterator();
		while (itEventContent2.hasNext()) {
			EventContent ec2 = itEventContent2.next();
			EventContent ec1 = config1.content(ec2.name());
			Comparison c = compareEventContents(ec1, ec2);
			if (!c.isIdentical())
				contents.add(c);
		}
		Iterator<EventContent> itEventContent1 = config1.contentIterator();
		while (itEventContent1.hasNext()) {
			EventContent ec1 = itEventContent1.next();
			if (config2.content(ec1.name()) == null)
				contents.add(compareEventContents(ec1, null));
		}
	}

	/** compare all Streams sets and store all non-identical comparisons */
	public void compareStreams() {
		// Streams
		Iterator<Stream> itStream2 = config2.streamIterator();
		while (itStream2.hasNext()) {
			Stream s2 = itStream2.next();
			Stream s1 = config1.stream(s2.name());
			Comparison c = compareStreams(s1, s2);
			if (!c.isIdentical())
				streams.add(c);
		}
		Iterator<Stream> itStream1 = config1.streamIterator();
		while (itStream1.hasNext()) {
			Stream s1 = itStream1.next();
			if (config2.stream(s1.name()) == null)
				streams.add(compareStreams(s1, null));
		}
	}

	/** compare all Datasets sets and store all non-identical comparisons */
	public void compareDatasets() {
		// Datasets
		Iterator<PrimaryDataset> itDataset2 = config2.datasetIterator();
		while (itDataset2.hasNext()) {
			PrimaryDataset dataset2 = itDataset2.next();
			PrimaryDataset dataset1 = config1.dataset(dataset2.name());
			Comparison c = compareDatasets(dataset1, dataset2);
			if (!c.isIdentical())
				datasets.add(c);
		}
		Iterator<PrimaryDataset> itDataset1 = config1.datasetIterator();
		while (itDataset1.hasNext()) {
			PrimaryDataset dataset1 = itDataset1.next();
			if (config2.dataset(dataset1.name()) == null)
				datasets.add(compareDatasets(dataset1, null));
		}

	}

	/** check if there are any differences at all */
	public boolean isIdentical() {
		return (psetCount() == 0 && edsourceCount() == 0 && essourceCount() == 0 && esmoduleCount() == 0
				&& serviceCount() == 0 && pathCount() == 0 && sequenceCount() == 0 && taskCount() == 0 
				&& edAliasCount() == 0 && switchProducerCount() == 0 && moduleCount() == 0 
				&& outputCount() == 0 && contentCount() == 0 && streamCount() == 0
				&& datasetCount() == 0);
	}

	/** number of psets */
	public int psetCount() {
		return psets.size();
	}

	/** retrieve i-th pset comparison */
	public Comparison pset(int i) {
		return psets.get(i);
	}

	/** get index of pset comparison */
	public int indexOfPSet(Comparison pset) {
		return psets.indexOf(pset);
	}

	/** iterator over all global psets */
	public Iterator<Comparison> psetIterator() {
		return psets.iterator();
	}

	/** number of edsources */
	public int edsourceCount() {
		return edsources.size();
	}

	/** retrieve i-th edsource comparison */
	public Comparison edsource(int i) {
		return edsources.get(i);
	}

	/** get index of edsource comparison */
	public int indexOfEDSource(Comparison eds) {
		return edsources.indexOf(eds);
	}

	/** iterator over all edsources */
	public Iterator<Comparison> edsourceIterator() {
		return edsources.iterator();
	}

	/** number of essources */
	public int essourceCount() {
		return essources.size();
	}

	/** retrieve i-th essource comparison */
	public Comparison essource(int i) {
		return essources.get(i);
	}

	/** get index of essource comparison */
	public int indexOfESSource(Comparison ess) {
		return essources.indexOf(ess);
	}

	/** iterator over all essources */
	public Iterator<Comparison> essourceIterator() {
		return essources.iterator();
	}

	/** number of esmodules */
	public int esmoduleCount() {
		return esmodules.size();
	}

	/** retrieve i-th esmodule comparison */
	public Comparison esmodule(int i) {
		return esmodules.get(i);
	}

	/** get index of esmodule comparison */
	public int indexOfESModule(Comparison esm) {
		return esmodules.indexOf(esm);
	}

	/** iterator over all esmodules */
	public Iterator<Comparison> esmoduleIterator() {
		return esmodules.iterator();
	}

	/** number of services */
	public int serviceCount() {
		return services.size();
	}

	/** retrieve i-th service comparison */
	public Comparison service(int i) {
		return services.get(i);
	}

	/** get index of service comparison */
	public int indexOfService(Comparison svc) {
		return services.indexOf(svc);
	}

	/** iterator over all services */
	public Iterator<Comparison> serviceIterator() {
		return services.iterator();
	}

	/** number of paths */
	public int pathCount() {
		return paths.size();
	}

	/** retrieve i-th path comparison */
	public Comparison path(int i) {
		return paths.get(i);
	}

	/** get index of path comparison */
	public int indexOfPath(Comparison path) {
		return paths.indexOf(path);
	}

	/** iterator over all paths */
	public Iterator<Comparison> pathIterator() {
		return paths.iterator();
	}

	/** number of sequences */
	public int sequenceCount() {
		return sequences.size();
	}

	/** retrieve i-th sequence comparison */
	public Comparison sequence(int i) {
		return sequences.get(i);
	}

	/** get index of sequence comparison */
	public int indexOfSequence(Comparison seq) {
		return sequences.indexOf(seq);
	}

	/** iterator over all sequences */
	public Iterator<Comparison> sequenceIterator() {
		return sequences.iterator();
	}

	/** number of tasks */
	public int taskCount() {
		return tasks.size();
	}

	/** retrieve i-th task comparison */
	public Comparison task(int i) {
		return tasks.get(i);
	}

	/** get index of task comparison */
	public int indexOfTask(Comparison tas) {
		return tasks.indexOf(tas);
	}

	/** iterator over all tasks */
	public Iterator<Comparison> taskIterator() {
		return tasks.iterator();
	}
	
	/** number of switch producers */
	public int switchProducerCount() {
		return switchproducers.size();
	}

	/** retrieve i-th switch producer comparison */
	public Comparison switchProducer(int i) {
		return switchproducers.get(i);
	}

	/** get index of switch producer comparison */
	public int indexOfSwitchProducer(Comparison sp) {
		return switchproducers.indexOf(sp);
	}

	/** iterator over all switch producers */
	public Iterator<Comparison> switchProducerIterator() {
		return switchproducers.iterator();
	}

	/** number of modules */
	public int moduleCount() {
		return modules.size();
	}

	/** retrieve i-th module comparison */
	public Comparison module(int i) {
		return modules.get(i);
	}

	/** get index of module comparison */
	public int indexOfModule(Comparison module) {
		return modules.indexOf(module);
	}

	/** iterator over all modules */
	public Iterator<Comparison> moduleIterator() {
		return modules.iterator();
	}
	
	/** number of edaliases */
	public int edAliasCount() {
		return edaliases.size();
	}

	/** retrieve i-th edalias comparison */
	public Comparison edAlias(int i) {
		return edaliases.get(i);
	}

	/** get index of edalias comparison */
	public int indexOfEDAlias(Comparison edalias) {
		return edaliases.indexOf(edalias);
	}

	/** iterator over all edaliases */
	public Iterator<Comparison> edAliasIterator() {
		return edaliases.iterator();
	}

	/** number of outputs */
	public int outputCount() {
		return outputs.size();
	}

	/** retrieve i-th output comparison */
	public Comparison output(int i) {
		return outputs.get(i);
	}

	/** get index of output comparison */
	public int indexOfOutput(Comparison output) {
		return outputs.indexOf(output);
	}

	/** iterator over all outputs */
	public Iterator<Comparison> outputIterator() {
		return outputs.iterator();
	}

	/** number of contents */
	public int contentCount() {
		return contents.size();
	}

	/** retrieve i-th content comparison */
	public Comparison content(int i) {
		return contents.get(i);
	}

	/** get index of content comparison */
	public int indexOfContent(Comparison content) {
		return contents.indexOf(content);
	}

	/** iterator over all contents */
	public Iterator<Comparison> contentIterator() {
		return contents.iterator();
	}

	/** number of streams */
	public int streamCount() {
		return streams.size();
	}

	/** retrieve i-th stream comparison */
	public Comparison stream(int i) {
		return streams.get(i);
	}

	/** get index of stream comparison */
	public int indexOfStream(Comparison stream) {
		return streams.indexOf(stream);
	}

	/** iterator over all streams */
	public Iterator<Comparison> streamIterator() {
		return streams.iterator();
	}

	/** number of datasets */
	public int datasetCount() {
		return datasets.size();
	}

	/** retrieve i-th dataset comparison */
	public Comparison dataset(int i) {
		return datasets.get(i);
	}

	/** get index of dataset comparison */
	public int indexOfDataset(Comparison dataset) {
		return datasets.indexOf(dataset);
	}

	/** iterator over all datasets */
	public Iterator<Comparison> datasetIterator() {
		return datasets.iterator();
	}

	/** compare two (global) parameter sets */
	public Comparison comparePSets(PSetParameter pset1, PSetParameter pset2) {
		Comparison result = new ParameterComparison(pset1, pset2);
		if (!result.isAdded() && !result.isRemoved()) {
			Comparison paramComparisons[] = compareParameterLists(pset1.parameterIterator(), pset2.parameterIterator());
			for (Comparison c : paramComparisons)
				if (!c.isIdentical())
					result.addComparison(c);
		}
		return result;
	}

	/** compare two instances */
	public Comparison compareInstances(Instance i1, Instance i2) {
		if (i1 != null && i2 != null && instanceMap.containsKey(i1.name() + "::" + i2.name()))
			return instanceMap.get(i1.name() + "::" + i2.name());

		Comparison result = new InstanceComparison(i1, i2);

		if (!result.isAdded() && !result.isRemoved()) {
			Comparison paramComparisons[] = compareParameterLists(i1.parameterIterator(), i2.parameterIterator());
			for (Comparison c : paramComparisons)
				if (!c.isIdentical())
					result.addComparison(c);

			instanceMap.put(i1.name() + "::" + i2.name(), result);
		}

		return result;
	}

	/** compare two references */
	public Comparison compareReferences(Reference r1, Reference r2) {

		Comparison result = new ReferenceComparison(r1, r2);

		return result;
	}

	/** compare two output modules */
	public Comparison compareOutputModules(OutputModule om1, OutputModule om2) {
		Comparison result = new OutputModuleComparison(om1, om2);

		if (!result.isAdded() && !result.isRemoved()) {
			Comparison paramComparisons[] = compareParameterLists(om1.parameterIterator(), om2.parameterIterator());
			for (Comparison c : paramComparisons)
				if (!c.isIdentical())
					result.addComparison(c);
		}

		return result;
	}

	/** compare two output modules ignoring the Streams */
	public Comparison compareOutputModules(OutputModule om1, OutputModule om2, boolean ignoreStreams) {
		// avoid checking the Streams using the flag TRUE.
		Comparison result = new OutputModuleComparison(om1, om2, ignoreStreams);

		if (!result.isAdded() && !result.isRemoved()) {
			Comparison paramComparisons[] = compareParameterLists(om1.parameterIterator(), om2.parameterIterator());
			for (Comparison c : paramComparisons)
				if (!c.isIdentical())
					result.addComparison(c);
		}

		return result;
	}

	/** compare two event contents */
	public Comparison compareEventContents(EventContent ec1, EventContent ec2) {
		Comparison result = new EventContentComparison(ec1, ec2);

		VStringParameter oc1 = new VStringParameter("outputCommands", "", false);
		VStringParameter oc2 = new VStringParameter("outputCommands", "", false);

		if (!result.isAdded() && !result.isRemoved()) {
			Iterator<OutputCommand> itOC1 = ec1.commandIterator();
			while (itOC1.hasNext()) {
				OutputCommand OC1 = itOC1.next();
				oc1.addValue(OC1.toString());
			}
			Iterator<OutputCommand> itOC2 = ec2.commandIterator();
			while (itOC2.hasNext()) {
				OutputCommand OC2 = itOC2.next();
				oc2.addValue(OC2.toString());
			}
			Comparison c = compareParameters(oc1, oc2);
			if (!c.isIdentical())
				result.addComparison(c);
		}

		return result;
	}

	/** compare two reference containers (path/sequence/task/switchproducer) */
	public Comparison compareContainers(ReferenceContainer rc1, ReferenceContainer rc2) {
		if (rc1 != null && rc2 != null && containerMap.containsKey(rc1.name() + "::" + rc2.name()))
			return containerMap.get(rc1.name() + "::" + rc2.name()); // BSATARIC: if comparison was already done before

		Comparison result = new ContainerComparison(rc1, rc2);

		// BSATARIC: going from parent components (rc1 and rc2) dig into child
		// components (entries) and compare them
		if (!result.isAdded() && !result.isRemoved()) {
			Iterator<Reference> itRef2 = rc2.entryIterator();
			while (itRef2.hasNext()) {
				Reference reference2 = itRef2.next();
				Reference reference1 = rc1.entry(reference2.name());
				Referencable parent2 = reference2.parent();
				Referencable parent1 = (reference1 == null) ? null : reference1.parent();

				Comparison r = compareReferences(reference1, reference2);
				if (r.isChanged())
					result.addComparison(r);
				if (parent2 instanceof ReferenceContainer) {
					Comparison c = compareContainers((ReferenceContainer) parent1, (ReferenceContainer) parent2);
					if (!c.isIdentical())
						result.addComparison(c);
				} else if (parent2 instanceof ModuleInstance || parent2 instanceof EDAliasInstance) {
					Comparison c = compareInstances((Instance) parent1, (Instance) parent2);
					if (!c.isIdentical())
						result.addComparison(c);
				} else if (parent2 instanceof OutputModule) {
					if (parent1 == null || (parent1 instanceof OutputModule)) {
						Comparison c = compareOutputModules((OutputModule) parent1, (OutputModule) parent2);
						if (!c.isIdentical())
							result.addComparison(c);
					}
				}
			}

			Iterator<Reference> itRef1 = rc1.entryIterator();
			while (itRef1.hasNext()) {
				Reference reference1 = itRef1.next();
				Reference reference2 = rc2.entry(reference1.name());
				if (reference2 != null)
					continue;

				Referencable parent1 = reference1.parent();

				Comparison r = compareReferences(reference1, reference2);
				if (r.isChanged())
					result.addComparison(r);
				if (parent1 instanceof ReferenceContainer) {
					ReferenceContainer rc = (ReferenceContainer) parent1;
					result.addComparison(new ContainerComparison(rc, null));
				} else if (parent1 instanceof ModuleInstance || parent1 instanceof EDAliasInstance) {
					Instance i = (Instance) parent1;
					result.addComparison(new InstanceComparison(i, null));
				} else if (parent1 instanceof OutputModule) {
					OutputModule om = (OutputModule) parent1;
					result.addComparison(new OutputModuleComparison(om, null));
				}

				containerMap.put(rc1.name() + "::" + rc2.name(), result);
			}
		}

		return result;
	}

	/**
	 * compareContainersIgnoreStreams
	 * ---------------------------------------------------------------- compare two
	 * reference containers (path/sequence/task/switchproducer) NOTE: This method ignores the
	 * Streams by making use of the constructor "OutputModuleComparison" with the
	 * flag "TRUE". This was originally designed to ignore extra differences in the
	 * previous analysis of DeepImport feature.
	 */
	public Comparison compareContainersIgnoreStreams(ReferenceContainer rc1, ReferenceContainer rc2) {
		if (rc1 != null && rc2 != null && containerMap.containsKey(rc1.name() + "::" + rc2.name()))
			return containerMap.get(rc1.name() + "::" + rc2.name());

		Comparison result = new ContainerComparison(rc1, rc2);

		if (!result.isAdded() && !result.isRemoved()) {
			Iterator<Reference> itRef2 = rc2.entryIterator();
			while (itRef2.hasNext()) {
				Reference reference2 = itRef2.next();
				Reference reference1 = rc1.entry(reference2.name());
				Referencable parent2 = reference2.parent();
				Referencable parent1 = (reference1 == null) ? null : reference1.parent();

				Comparison r = compareReferences(reference1, reference2);
				if (r.isChanged())
					result.addComparison(r);
				if (parent2 instanceof ReferenceContainer) {
					Comparison c = compareContainersIgnoreStreams((ReferenceContainer) parent1,
							(ReferenceContainer) parent2);
					if (!c.isIdentical())
						result.addComparison(c);
				} else if (parent2 instanceof ModuleInstance || parent2 instanceof EDAliasInstance) {
					Comparison c = compareInstances((Instance) parent1, (Instance) parent2);
					if (!c.isIdentical())
						result.addComparison(c);
				} else if (parent2 instanceof OutputModule) {
					if (parent1 == null || (parent1 instanceof OutputModule)) {
						// avoid checking the Streams using the flag TRUE.
						Comparison c = compareOutputModules((OutputModule) parent1, (OutputModule) parent2, true);
						if (!c.isIdentical())
							result.addComparison(c);
					}
				}
			}

			Iterator<Reference> itRef1 = rc1.entryIterator();
			while (itRef1.hasNext()) {
				Reference reference1 = itRef1.next();
				Reference reference2 = rc2.entry(reference1.name());
				if (reference2 != null)
					continue;

				Referencable parent1 = reference1.parent();

				Comparison r = compareReferences(reference1, reference2);
				if (r.isChanged())
					result.addComparison(r);
				if (parent1 instanceof ReferenceContainer) {
					ReferenceContainer rc = (ReferenceContainer) parent1;
					result.addComparison(new ContainerComparison(rc, null));
				} else if (parent1 instanceof ModuleInstance || parent1 instanceof EDAliasInstance) {
					Instance i = (Instance) parent1;
					result.addComparison(new InstanceComparison(i, null));
				} else if (parent1 instanceof OutputModule) {
					OutputModule om = (OutputModule) parent1;
					result.addComparison(new OutputModuleComparison(om, null, true)); // avoid checking the Streams
																						// using the flag TRUE.
				}

				containerMap.put(rc1.name() + "::" + rc2.name(), result);
			}
		}

		return result;
	}

	/** compare two streams */
	public Comparison compareStreams(Stream s1, Stream s2) {
		Comparison result = new StreamComparison(s1, s2);

		if (!result.isAdded() && !result.isRemoved()) {
			Iterator<PrimaryDataset> itDataset2 = s2.datasetIterator();
			while (itDataset2.hasNext()) {
				PrimaryDataset d2 = itDataset2.next();
				if (s1.dataset(d2.name()) == null)
					result.addComparison(new DatasetComparison(null, d2));
			}
			Iterator<PrimaryDataset> itDataset1 = s1.datasetIterator();
			while (itDataset1.hasNext()) {
				PrimaryDataset d1 = itDataset1.next();
				if (s2.dataset(d1.name()) == null)
					result.addComparison(new DatasetComparison(d1, null));
			}
		}
		return result;
	}

	/** compare two datasets */
	public Comparison compareDatasets(PrimaryDataset d1, PrimaryDataset d2) {
		Comparison result = new DatasetComparison(d1, d2);

		if (!result.isAdded() && !result.isRemoved()) {
			Iterator<Path> itPath2 = d2.pathIterator();
			while (itPath2.hasNext()) {
				Path p2 = itPath2.next();
				if (d1.path(p2.name()) == null)
					result.addComparison(new ContainerComparison(null, p2));
			}
			Iterator<Path> itPath1 = d1.pathIterator();
			while (itPath1.hasNext()) {
				Path p1 = itPath1.next();
				if (d2.path(p1.name()) == null)
					result.addComparison(new ContainerComparison(p1, null));
			}
		}
		return result;
	}

	/** print all comparisons */
	public String printAll() {
		StringBuffer result = new StringBuffer();
		// global psets
		if (psetCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("Global PSets (" + psetCount() + "):\n");
			result.append(printInstanceComparisons(psetIterator()));
		}

		// edsources
		if (edsourceCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("EDSources (" + edsourceCount() + "):\n");
			result.append(printInstanceComparisons(edsourceIterator()));
		}

		// essources
		if (essourceCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("ESSources (" + essourceCount() + "):\n");
			result.append(printInstanceComparisons(essourceIterator()));
		}

		// esmodules
		if (esmoduleCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("ESModules (" + esmoduleCount() + "):\n");
			result.append(printInstanceComparisons(esmoduleIterator()));
		}

		// services
		if (serviceCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("Services (" + serviceCount() + "):\n");
			result.append(printInstanceComparisons(serviceIterator()));
		}

		// paths
		if (pathCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("Paths (" + pathCount() + "):\n");
			result.append(printContainerComparisons(pathIterator()));
		}

		// sequences
		if (sequenceCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("Sequences (" + sequenceCount() + "):\n");
			result.append(printContainerComparisons(sequenceIterator()));
		}

		// tasks
		if (taskCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("Tasks (" + taskCount() + "):\n");
			result.append(printContainerComparisons(taskIterator()));
		}
		
		// switchproducers
		if (switchProducerCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("SwitchProducers (" + switchProducerCount() + "):\n");
			result.append(printContainerComparisons(switchProducerIterator()));
		}

		// modules
		if (moduleCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("Modules (" + moduleCount() + "):\n");
			result.append(printInstanceComparisons(moduleIterator()));
		}
		
		// edaliases
		if (edAliasCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("EDAliases (" + edAliasCount() + "):\n");
			result.append(printInstanceComparisons(edAliasIterator()));
		}

		// outputs
		if (outputCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("OutputModules (" + outputCount() + "):\n");
			result.append(printInstanceComparisons(outputIterator()));
		}

		// contents
		if (contentCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("EventContents (" + contentCount() + "):\n");
			result.append(printInstanceComparisons(contentIterator()));
		}

		// streams
		if (streamCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("Streams (" + streamCount() + "):\n");
			result.append(printStreamComparisons(streamIterator()));
		}

		// datasets
		if (datasetCount() > 0) {
			result.append("\n---------------------------------------" + "----------------------------------------\n");
			result.append("Datasets (" + datasetCount() + "):\n");
			result.append(printDatasetComparisons(datasetIterator()));
		}

		return result.toString();
	}

	/** print instance comparisons */
	public String printInstanceComparisons(Iterator<Comparison> itC) {
		StringBuffer result = new StringBuffer();
		while (itC.hasNext()) {
			Comparison c = itC.next();
			result.append("  -> ").append(c.toString()).append("\n");
			Iterator<Comparison> it = c.recursiveComparisonIterator();
			while (it.hasNext()) {
				ParameterComparison cParam = (ParameterComparison) it.next();
				if (cParam.isChanged() && cParam.isPSet())
					continue;
				result.append("       ").append(cParam.toString()).append("\n");
			}
		}
		return result.toString();
	}

	/** print container comparisons */
	public String printContainerComparisons(Iterator<Comparison> itC) {
		StringBuffer result = new StringBuffer();
		while (itC.hasNext()) {
			Comparison c = itC.next();
			result.append("  -> ").append(c.toString()).append("\n");
			Iterator<Comparison> it = c.comparisonIterator();
			while (it.hasNext())
				result.append("      -> ").append(it.next().toString()).append("\n");
		}
		return result.toString();
	}

	/** print stream comparisons */
	public String printStreamComparisons(Iterator<Comparison> itC) {
		StringBuffer result = new StringBuffer();
		while (itC.hasNext()) {
			Comparison c = itC.next();
			result.append("  -> ").append(c.toString()).append("\n");
			Iterator<Comparison> it = c.recursiveComparisonIterator();
			while (it.hasNext()) {
				DatasetComparison cDataset = (DatasetComparison) it.next();
				// if (cDataset.isChanged()) continue;
				result.append("       ").append(cDataset.toString()).append("\n");
			}
		}
		return result.toString();
	}

	/** print dataset comparisons */
	public String printDatasetComparisons(Iterator<Comparison> itC) {
		StringBuffer result = new StringBuffer();
		while (itC.hasNext()) {
			Comparison c = itC.next();
			result.append("  -> ").append(c.toString()).append("\n");
			Iterator<Comparison> it = c.recursiveComparisonIterator();
			while (it.hasNext()) {
				ContainerComparison cPath = (ContainerComparison) it.next();
				// if (cPath.isChanged()) continue;
				result.append("       ").append(cPath.toString()).append("\n");
			}
		}
		return result.toString();
	}

	//
	// private member functions
	//

	/** compare two lists of parameters */
	private Comparison[] compareParameterLists(Iterator<Parameter> it1, Iterator<Parameter> it2) {
		ArrayList<Comparison> result = new ArrayList<Comparison>();

		HashMap<String, Parameter> map = new HashMap<String, Parameter>();
		while (it1.hasNext()) {
			Parameter p = it1.next();
			map.put(p.type() + "::" + p.fullName(), p);
		}

		while (it2.hasNext()) {
			Parameter p2 = it2.next();
			Parameter p1 = map.remove(p2.type() + "::" + p2.fullName());
			Comparison c = compareParameters(p1, p2);
			if (!c.isIdentical())
				result.add(c);
		}
		Iterator<Parameter> itRemoved = map.values().iterator();
		while (itRemoved.hasNext())
			result.add(compareParameters(itRemoved.next(), null));

		return result.toArray(new Comparison[result.size()]);
	}

	/** compare two parameters */
	private Comparison compareParameters(Parameter p1, Parameter p2) {
		Comparison result = new ParameterComparison(p1, p2);

		if (!result.isAdded() && !result.isRemoved()) {
			if (p2 instanceof PSetParameter) {
				PSetParameter pset1 = (PSetParameter) p1;
				PSetParameter pset2 = (PSetParameter) p2;
				Comparison[] paramComparisons = compareParameterLists(pset1.parameterIterator(),
						pset2.parameterIterator());
				for (Comparison c : paramComparisons)
					if (!c.isIdentical())
						result.addComparison(c);
			} else if (p2 instanceof VPSetParameter) {
				VPSetParameter vpset1 = (VPSetParameter) p1;
				VPSetParameter vpset2 = (VPSetParameter) p2;
				Comparison[] paramComparisons = compareParameterLists(vpset1.parameterIterator(),
						vpset2.parameterIterator());
				for (Comparison c : paramComparisons)
					if (!c.isIdentical())
						result.addComparison(c);
			}
		}
		return result;
	}

	//
	// static member functions
	//

	/** get the database instance */
	public static ConfDB getDatabase() {
		return database;
	}

	/** initialize database */
	public static void initDatabase() throws DiffException {
		if (database != null)
			return;
		database = new ConfDB();
		String url = "jdbc:oracle:thin:@//" + "cmsr1-v.cern.ch:10121/cms_cond.cern.ch";
		try {
			database.connect("oracle", url, "cms_hltdev_reader", "convertme!");
		} catch (DatabaseException e) {
			String errMsg = "Diff::initDatabase() failed: " + e.getMessage();
			throw new DiffException(errMsg, e);
		}
	}

	/** get a configuration, given the id or name */
	public static IConfiguration getConfiguration(String configIdAsString) throws DiffException {
		int configId = -1;
		try {
			configId = Integer.parseInt(configIdAsString);
		} catch (NumberFormatException e1) {
			try {
				configId = getDatabase().getConfigNewId(configIdAsString);
			} catch (DatabaseException e2) {
				String errMsg = "Diff.getConfiguration(configIdAsString=" + configIdAsString + ") failed.";
				throw new DiffException(errMsg, e2);
			}
		}

		try {
			return ConfCache.getCache().getConfiguration(configId, getDatabase());
		} catch (DatabaseException e) {
			String errMsg = "Diff::getConfiguration(configIdAsString=" + configIdAsString + ") failed.";
			throw new DiffException(errMsg, e);
		}
	}

	//
	// MAIN
	//

	/** main method */
	public static void main(String[] args) {
		String configs = "";
		String search = "";
		String dbType = "oracle";
		String dbHost = "cmsr1-v.cern.ch";
		String dbPort = "10121";
		String dbName = "cms_cond.cern.ch";
		String dbUser = "cms_hltdev_reader";
		String dbPwrd = "convertme!";

		for (int iarg = 0; iarg < args.length; iarg++) {
			String arg = args[iarg];
			if (arg.equals("--configs")) {
				iarg++;
				configs = args[iarg];
			} else if (arg.equals("--search")) {
				iarg++;
				search = args[iarg];
			} else if (arg.equals("-t")) {
				iarg++;
				dbType = args[iarg];
			} else if (arg.equals("-h")) {
				iarg++;
				dbHost = args[iarg];
			} else if (arg.equals("-p")) {
				iarg++;
				dbPort = args[iarg];
			} else if (arg.equals("-d")) {
				iarg++;
				dbName = args[iarg];
			} else if (arg.equals("-u")) {
				iarg++;
				dbUser = args[iarg];
			} else if (arg.equals("-s")) {
				iarg++;
				dbPwrd = args[iarg];
			} else {
				System.err.println("Invalid option '" + arg + "'.");
				System.exit(0);
			}
		}

		// configuration ids
		String a[] = configs.split(",");
		int configId1 = -1;
		int configId2 = -1;
		String configName1 = "";
		String configName2 = "";
		try {
			configId1 = Integer.parseInt(a[0]);
			configId2 = Integer.parseInt(a[1]);
		} catch (Exception e) {
			configName1 = a[0];
			configName2 = a[1];
		}

		// construct database url
		String dbUrl = null;
		if (dbType.equals("mysql"))
			dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
		else if (dbType.equals("oracle"))
			dbUrl = "jdbc:oracle:thin:@//" + dbHost + ":" + dbPort + "/" + dbName;

		// database connection
		ConfDB database = new ConfDB();
		try {
			database.connect(dbType, dbUrl, dbUser, dbPwrd);
		} catch (DatabaseException e) {
			System.err.println("Failed to connect to database: " + e.getMessage());
			System.exit(0);
		}

		Configuration config1 = null;
		Configuration config2 = null;
		try {
			int id1 = (configId1 > 0) ? configId1 : database.getConfigNewId(configName1);
			int id2 = (configId2 > 0) ? configId2 : database.getConfigNewId(configName2);

			config1 = database.loadConfiguration(id1);
			config2 = database.loadConfiguration(id2);
			System.out.println("old: " + config1.toString());
			System.out.println("new: " + config2.toString());
		} catch (DatabaseException e) {
			System.err.println("Failed to load configurations: " + e.getMessage());
			System.exit(0);
		}

		Diff diff = new Diff(config1, config2);
		if (search.length() > 0)
			diff.compare(search);
		else
			diff.compare();
		System.out.println(diff.printAll());
		try {
			database.disconnect();
		} catch (DatabaseException e) {
			System.err.println("Failed to disconnect from DB.");
			e.printStackTrace();
		}
	}

}

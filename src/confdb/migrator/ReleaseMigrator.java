package confdb.migrator;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.data.*;

/**
 * ReleaseMigrator
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Migrate a configuration from its current database to another
 * database.
 */
public class ReleaseMigrator {
	//
	// data members
	//

	/** configuration to be migrated */
	private Configuration sourceConfig = null;

	/** configuration to be migrated */
	private Configuration targetConfig = null;

	/** source software release */
	private SoftwareRelease sourceRelease = null;

	/** target software release */
	private SoftwareRelease targetRelease = null;

	/** problem report messages */
	private ArrayList<String> messages = new ArrayList<String>();

	/** number of missing templates */
	private int missingTemplateCount = 0;

	/** number of missing parameters */
	private int missingParameterCount = 0;

	/** number of parameters with mismatched type */
	private int mismatchParameterTypeCount = 0;

	//
	// construction
	//

	/** standard constructor */
	public ReleaseMigrator(Configuration sourceConfig, Configuration targetConfig) {
		this.sourceConfig = sourceConfig;
		this.targetConfig = targetConfig;
		this.sourceRelease = sourceConfig.release();
		this.targetRelease = targetConfig.release();
	}

	//
	// member functions
	//

	/** migrate the configuration to the new release */
	public void migrate() {
		// migrate PSets
		for (int i = 0; i < sourceConfig.psetCount(); i++) {
			PSetParameter pset = sourceConfig.pset(i);
			targetConfig.insertPSet((PSetParameter) pset.clone(null));
		}
		
		// migrate global EDAliases
		for (int i = 0; i < sourceConfig.globalEDAliasCount(); i++) {
			EDAliasInstance source = sourceConfig.globalEDAlias(i);
			EDAliasInstance target = targetConfig.insertGlobalEDAlias(source.name());
			if (target != null) {
				migrateParameters(source, target);
			}
		}

		// migrate EDSources
		for (int i = 0; i < sourceConfig.edsourceCount(); i++) {
			EDSourceInstance source = sourceConfig.edsource(i);
			EDSourceInstance target = targetConfig.insertEDSource(source.name());
			if (target != null) {
				migrateParameters(source, target);
			} else {
				String msg = "TEMPLATE NOT FOUND: EDSource '" + source.name() + "'.";
				messages.add(msg);
				missingTemplateCount++;
			}
		}

		// migrate ESSources
		int essourceCount = 0;
		for (int i = 0; i < sourceConfig.essourceCount(); i++) {
			ESSourceInstance source = sourceConfig.essource(i);
			ESSourceInstance target = targetConfig.insertESSource(essourceCount, source.template().name(),
					source.name());
			if (target != null) {
				essourceCount++;
				target.setPreferred(source.isPreferred());
				migrateParameters(source, target);
			} else {
				String msg = "TEMPLATE NOT FOUND: ESSource '" + source.template().name() + "'.";
				messages.add(msg);
				missingTemplateCount++;
			}
		}

		// migrate ESModules
		int esmoduleCount = 0;
		for (int i = 0; i < sourceConfig.esmoduleCount(); i++) {
			ESModuleInstance source = sourceConfig.esmodule(i);
			ESModuleInstance target = targetConfig.insertESModule(esmoduleCount, source.template().name(),
					source.name());

			if (target != null) {
				esmoduleCount++;
				target.setPreferred(source.isPreferred());
				migrateParameters(source, target);
			} else {
				String msg = "TEMPLATE NOT FOUND: ESModule '" + source.template().name() + "'.";
				messages.add(msg);
				missingTemplateCount++;
			}
		}

		// migrate Services
		int serviceCount = 0;
		for (int i = 0; i < sourceConfig.serviceCount(); i++) {
			ServiceInstance source = sourceConfig.service(i);
			ServiceInstance target = targetConfig.insertService(serviceCount, source.template().name());
			if (target != null) {
				serviceCount++;
				migrateParameters(source, target);
			} else {
				String msg = "TEMPLATE NOT FOUND: Service '" + source.name() + "'.";
				messages.add(msg);
				missingTemplateCount++;
			}
		}

		// migrate Modules
		for (int i = 0; i < sourceConfig.moduleCount(); i++) {
			ModuleInstance source = sourceConfig.module(i);
			ModuleInstance target = targetConfig.insertModule(source.template().name(), source.name());
			if (target != null) {
				migrateParameters(source, target);
			} else {
				String msg = "TEMPLATE NOT FOUND: " + source.template().type() + " '" + source.template().name() + "'.";
				messages.add(msg);
				missingTemplateCount++;
			}
		}
		
		// migrate EDAliases
		for (int i = 0; i < sourceConfig.edAliasCount(); i++) {
			EDAliasInstance source = sourceConfig.edAlias(i);
			EDAliasInstance target = targetConfig.insertEDAlias(source.name());
			if (target != null) {
				migrateParameters(source, target);
			}
		}

		// migrate Paths
		for (int i = 0; i < sourceConfig.pathCount(); i++) {
			Path source = sourceConfig.path(i);
			Path target = targetConfig.insertPath(i, source.name());
			target.setFields(source);
		}

		// migrate Sequences
		for (int i = 0; i < sourceConfig.sequenceCount(); i++) {
			Sequence source = sourceConfig.sequence(i);
			Sequence target = targetConfig.insertSequence(i, source.name());
		}

		// migrate Tasks
		for (int i = 0; i < sourceConfig.taskCount(); i++) {
			Task source = sourceConfig.task(i);
			Task target = targetConfig.insertTask(i, source.name());
		}
		
		// migrate SwitchProducers
		for (int i = 0; i < sourceConfig.switchProducerCount(); i++) {
			SwitchProducer source = sourceConfig.switchProducer(i);
			SwitchProducer target = targetConfig.insertSwitchProducer(i, source.name());
		}


		// migrate eventcontent
		for (int i = 0; i < sourceConfig.contentCount(); i++) {
			EventContent source = sourceConfig.content(i);
			EventContent target = targetConfig.insertContent(source.name());

			Iterator<Stream> itS = source.streamIterator();
			while (itS.hasNext()) {
				Stream sourceStream = itS.next();
				Stream targetStream = target.insertStream(sourceStream.name());

				targetStream.setFractionToDisk(sourceStream.fractionToDisk());

				OutputModule sourceOutputModule = sourceStream.outputModule();
				OutputModule targetOutputModule = targetStream.outputModule();

				Iterator<Parameter> itOPar = sourceOutputModule.parameterIterator();
				while (itOPar.hasNext()) {
					Parameter p = itOPar.next();
					if (p == null)
						continue;
					targetOutputModule.updateParameter(p.name(), p.type(), p.valueAsString());
				}

				Iterator<Path> itPas = sourceStream.pathIterator();
				while (itPas.hasNext()) {
					Path sourcePath = itPas.next();
					if (!targetStream.insertPath(targetConfig.path(sourcePath.name())))
						System.out.println("There is a problem inserting a path in stream while migration");

				}

				Iterator<PrimaryDataset> itP = sourceStream.datasetIterator();
				while (itP.hasNext()) {
					PrimaryDataset sourceDataset = itP.next();
					PrimaryDataset targetDataset = targetStream.insertDataset(sourceDataset.name());

					Iterator<Path> itPad = sourceDataset.pathIterator();
					while (itPad.hasNext()) {
						Path sourcePath = itPad.next();
						if (!targetDataset.insertPath(targetConfig.path(sourcePath.name())))
							System.out.println("There is a problem inserting a path in dataset while migration");

					}
				}
			}

		}

		// migrate References within Paths
		for (int i = 0; i < sourceConfig.pathCount(); i++) {
			Path source = sourceConfig.path(i);
			Path target = targetConfig.path(i);
			migrateReferences(source, target);
		}

		// migrate References within Sequences
		for (int i = 0; i < sourceConfig.sequenceCount(); i++) {
			Sequence source = sourceConfig.sequence(i);
			Sequence target = targetConfig.sequence(i);
			migrateReferences(source, target);
		}

		// migrate References within Tasks
		for (int i = 0; i < sourceConfig.taskCount(); i++) {
			Task source = sourceConfig.task(i);
			Task target = targetConfig.task(i);
			migrateReferences(source, target);
		}
		
		// migrate References within SwitchProducers
		for (int i = 0; i < sourceConfig.switchProducerCount(); i++) {
			SwitchProducer source = sourceConfig.switchProducer(i);
			SwitchProducer target = targetConfig.switchProducer(i);
			migrateReferences(source, target);
		}

		// migrate eventcontent
		for (int i = 0; i < sourceConfig.contentCount(); i++) {
			EventContent source = sourceConfig.content(i);
			EventContent target = targetConfig.insertContent(source.name());

			Iterator<OutputCommand> outComIter = source.commandIterator();
			while (outComIter.hasNext()) {
				OutputCommand sourcetOutputCommand = outComIter.next();
				Path sourcePath = sourcetOutputCommand.parentPath();
				if (sourcePath == null) {
					target.insertCommand(sourcetOutputCommand);
					continue;
				}
				Path targetPath = targetConfig.path(sourcePath.name());

				Reference sourceReference = sourcetOutputCommand.parentReference();
				if (sourceReference == null)
					continue;
				Reference targetReference = targetPath.entry(sourceReference.name());
				if (targetReference == null)
					continue;

				target.insertCommand(new OutputCommand(targetPath, targetReference));
			}
		}

	}

	/** retrieve message iterator */
	public Iterator<String> messageIterator() {
		return messages.iterator();
	}

	/** retrieve number of missing templates */
	public int missingTemplateCount() {
		return missingTemplateCount;
	}

	/** retrieve number of missing parameters */
	public int missingParameterCount() {
		return missingParameterCount;
	}

	/** retrieve number of missing templates */
	public int mismatchParameterTypeCount() {
		return mismatchParameterTypeCount;
	}

	//
	// private memeber functions
	//

	/** set the target parameters according to the source parameters */
	private void migrateParameters(Instance source, Instance target) {
		if (source.parameterCount() != target.parameterCount()) {
			String msg = "";
			if (source.template() != null) {
				msg = "PARAMETER COUNT MISMATCH: '" + source.template().name() + "' source="
						+ source.parameterCount() + " target=" + target.parameterCount();
			} else {
				msg = "PARAMETER COUNT MISMATCH: '" + source.name() + "' source="
						+ source.parameterCount() + " target=" + target.parameterCount();
			}
			messages.add(msg);
		}

		for (int i = 0; i < target.parameterCount(); i++) {
			Parameter targetParameter = target.parameter(i);
			String parameterName = targetParameter.name();
			String parameterType = targetParameter.type();
			Parameter sourceParameter = source.parameter(parameterName);
			// parameterType);

			if (sourceParameter != null) {
				if (sourceParameter.type().equals(parameterType)) {
					if (sourceParameter.isDefault()) {// THIS IS NEW! 8/10/2009
						String msg = "PARAMETER REMAINS AT DEFAULT: " + "source=" + sourceParameter + " -> " + "target="
								+ targetParameter;
						// messages.add(msg);
						String valueAsString = sourceParameter.valueAsString();
						target.updateParameter(parameterName, parameterType, valueAsString);
					} else {
						String valueAsString = sourceParameter.valueAsString();
						target.updateParameter(parameterName, parameterType, valueAsString);
					}
				} else if (parameterType.equals("v" + sourceParameter.type())
						|| parameterType.equals("V" + sourceParameter.type())) {
					String valueAsString = sourceParameter.valueAsString();
					target.updateParameter(parameterName, parameterType, valueAsString);
				} else {
					String msg = "";
					if (source.template() != null) {
						msg = "PARAMETER TYPE MISMATCH: " + source.template().type() + " '"
							+ source.template().name() + "' : " + "source=" + sourceParameter.type() + " " + "target="
							+ parameterType;
					} else {
						msg = "PARAMETER TYPE MISMATCH: " + source.name() + "' : " + 
								"source=" + sourceParameter.type() + " " + "target="
								+ parameterType;
					}
					messages.add(msg);
					mismatchParameterTypeCount++;
				}
			} else {
				String msg = "";
				if (source.template() != null) {
					msg = "MISSING SOURCE PARAMETER: " + source.template().type() + " '" + source.template().name()
						+ "' : " + parameterName;
				} else {
					msg = "MISSING SOURCE PARAMETER:" + source.name() + "' : " + parameterName;
				}
				messages.add(msg);
				missingParameterCount++;
			}
		}

		// consider added untracked top-level parameters!
		if (source.template() != null)
			for (int i = source.template().parameterCount(); i < source.parameterCount(); i++) {
				Parameter sourceParameter = source.parameter(i);
				if (source.isParameterRemovable(sourceParameter)) {
					target.updateParameter(sourceParameter.name(), sourceParameter.type(), sourceParameter.valueAsString());
				} else {
					System.err
							.println("ERROR: this is not a removable parameter:" + sourceParameter + ", WHAT THE F!@# !?");
				}
			}

		// special E/g migration
		// // if (target.template().name().equals("HLTEgammaEtFilter") ||
		// target.template().name().equals("HLTPMMassFilter")) {
		// target.updateParameter("l1EGCand","InputTag",source.parameter("L1IsoCand","InputTag").valueAsString());
		// } else if (target.template().name().equals("HLTEgammaGenericQuadraticFilter")
		// ||
		// target.template().name().equals("HLTEgammaGenericFilter") ||
		// target.template().name().equals("HLTMuonGenericFilter")) {
		// target.updateParameter("varTag","InputTag",source.parameter("isoTag","InputTag").valueAsString());
		// target.updateParameter("l1EGCand","InputTag",source.parameter("L1IsoCand","InputTag").valueAsString());
		// } else if (target.template().name().equals("HLTElectronPixelMatchFilter")) {
		// target.updateParameter("l1EGCand","InputTag",source.parameter("L1IsoCand","InputTag").valueAsString());
		// target.updateParameter("l1PixelSeedsTag","InputTag",source.parameter("L1IsoPixelSeedsTag","InputTag").valueAsString());
		// }
		// special E/g migration

	}

	/**
	 * migrate references from source Path/Sequence/Task/SwitchProducer to target
	 * Path/Sequence/Task/SwitchProducer
	 */
	private void migrateReferences(ReferenceContainer source, ReferenceContainer target) {
		int iTarget = 0;
		for (int i = 0; i < source.entryCount(); i++) {
			Reference reference = source.entry(i);
			if (reference instanceof PathReference) {
				Path sourcePath = (Path) reference.parent();
				Path targetPath = targetConfig.path(sourceConfig.indexOfPath(sourcePath));
				Path parentPath = (Path) target;
				PathReference targetReference = targetConfig.insertPathReference(parentPath, iTarget++, targetPath);
				targetReference.setOperator(reference.getOperator());

			} else if (reference instanceof SequenceReference) {
				Sequence sourceSequence = (Sequence) reference.parent();
				Sequence targetSequence = targetConfig.sequence(sourceConfig.indexOfSequence(sourceSequence));
				SequenceReference targetReference = targetConfig.insertSequenceReference(target, iTarget++,
						targetSequence);
				targetReference.setOperator(reference.getOperator());
			} else if (reference instanceof TaskReference) {
				Task sourceTask = (Task) reference.parent();
				Task targetTask = targetConfig.task(sourceConfig.indexOfTask(sourceTask));
				TaskReference targetReference = targetConfig.insertTaskReference(target, iTarget++, targetTask);
				targetReference.setOperator(reference.getOperator());
			} else if (reference instanceof SwitchProducerReference) {
				SwitchProducer sourceSwitchProducer = (SwitchProducer) reference.parent();
				SwitchProducer targetSwitchProducer = 
						targetConfig.switchProducer(sourceConfig.indexOfSwitchProducer(sourceSwitchProducer));
				SwitchProducerReference targetReference = 
						targetConfig.insertSwitchProducerReference(target, iTarget++, targetSwitchProducer);
				targetReference.setOperator(reference.getOperator());
			} else if (reference instanceof ModuleReference) {
				ModuleInstance sourceModule = (ModuleInstance) reference.parent();
				ModuleInstance targetModule = targetConfig.module(sourceModule.name());
				if (targetModule != null) {
					ModuleReference targetReference = targetConfig.insertModuleReference(target, iTarget++,
							targetModule);
					targetReference.setOperator(reference.getOperator());
				} else {
					String msg = "MODULE MISSING FROM PATH/SEQUENCE/TASK/SWITCHPRODUCER: " + sourceModule.template().type() + " '"
							+ sourceModule.name() + "' / " + sourceModule.template().name() + " missing from "
							+ source.name();
					messages.add(msg);
				}
			} else if (reference instanceof EDAliasReference) {
				EDAliasInstance sourceEDAlias = (EDAliasInstance) reference.parent();
				EDAliasInstance targetEDAlias = targetConfig.edAlias(sourceEDAlias.name());
				if (targetEDAlias != null) {
					EDAliasReference targetReference = targetConfig.insertEDAliasReference(target, iTarget++,
							targetEDAlias);
					targetReference.setOperator(reference.getOperator());
				} else {
					String msg = "EDALIAS MISSING FROM SEQUENCEPRODUCER: "
							+ sourceEDAlias.name() + " missing from " + source.name();
					messages.add(msg);
				}
			} else if (reference instanceof OutputModuleReference) {

				OutputModule sourceOutputModule = (OutputModule) reference.parent();
				OutputModule targetOutputModule = targetConfig.output(sourceOutputModule.name());

				if (targetOutputModule != null) {
					OutputModuleReference targetReference = targetConfig.insertOutputModuleReference(target, iTarget++,
							targetOutputModule);
					targetReference.setOperator(reference.getOperator());

				} else {
					String msg = "MODULE MISSING FROM PATH/SEQUENCE/TASK: " + " '" + sourceOutputModule.name() + "' / "
							+ " missing from " + source.name();
					messages.add(msg);
				}
			}
		}
	}

}

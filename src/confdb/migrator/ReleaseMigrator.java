package confdb.migrator;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.data.*;


/**
 * ReleaseMigrator
 * ----------------
 * @author Philipp Schieferdecker
 *
 * Migrate a configuration from its current database to another database.
 */
public class ReleaseMigrator
{
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
    public ReleaseMigrator(Configuration sourceConfig,Configuration targetConfig)
    {
	this.sourceConfig  = sourceConfig;
	this.targetConfig  = targetConfig;
	this.sourceRelease = sourceConfig.release();
	this.targetRelease = targetConfig.release();
    }
    
    
    //
    // member functions
    //
    
    /** migrate the configuration to the new release */
    public boolean migrate()
    {
	// migrate PSets
	for (int i=0;i<sourceConfig.psetCount();i++) {
	    PSetParameter pset = sourceConfig.pset(i);
	    targetConfig.insertPSet((PSetParameter)pset.clone(null));
	}

	// migrate EDSources
	for (int i=0;i<sourceConfig.edsourceCount();i++) {
	    EDSourceInstance source = sourceConfig.edsource(i);
	    EDSourceInstance target = targetConfig.insertEDSource(source.name());
	    if (target!=null) {
		migrateParameters(source,target);
	    }
	    else {
		String msg = "TEMPLATE NOT FOUND: EDSource '"+source.name()+"'.";
		messages.add(msg);
		missingTemplateCount++;
	    }
	}

	// migrate ESSources
	for (int i=0;i<sourceConfig.essourceCount();i++) {
	    ESSourceInstance source = sourceConfig.essource(i);
	    ESSourceInstance target =
		targetConfig.insertESSource(i,source.template().name(),source.name());
	    if (target!=null) {
		migrateParameters(source,target);
	    }
	    else {
		String msg = "TEMPLATE NOT FOUND: ESSource '"+
		    source.template().name()+"'.";
		messages.add(msg);
		missingTemplateCount++;
	    }
	}

	// migrate ESModules
	for (int i=0;i<sourceConfig.esmoduleCount();i++) {
	    ESModuleInstance source = sourceConfig.esmodule(i);
	    ESModuleInstance target =
		targetConfig.insertESModule(i,source.template().name(),source.name());
	    if (target!=null) {
		migrateParameters(source,target);
	    }
	    else {
		String msg = "TEMPLATE NOT FOUND: ESModule '"+
		    source.template().name()+"'.";
		messages.add(msg);
		missingTemplateCount++;
	    }
	}

	// migrate Services
	for (int i=0;i<sourceConfig.serviceCount();i++) {
	    ServiceInstance source = sourceConfig.service(i);
	    ServiceInstance target =
		targetConfig.insertService(i,source.template().name());
	    if (target!=null) {
		migrateParameters(source,target);
	    }
	    else {
		String msg = "TEMPLATE NOT FOUND: Service '"+source.name()+"'.";
		messages.add(msg);
		missingTemplateCount++;
	    }
	}
	
	// migrate Modules
	for (int i=0;i<sourceConfig.moduleCount();i++) {
	    ModuleInstance source = sourceConfig.module(i);
	    ModuleInstance target =
		targetConfig.insertModule(source.template().name(),source.name());
	    if (target!=null) {
		migrateParameters(source,target);
	    }
	    else {
		String msg = "TEMPLATE NOT FOUND: "+source.template().type()+
		    " '"+source.template().name()+"'.";
		messages.add(msg);
		missingTemplateCount++;
	    }
	}

	// migrate Paths
	for (int i=0;i<sourceConfig.pathCount();i++) {
	    Path source = sourceConfig.path(i);
	    Path target = targetConfig.insertPath(i,source.name());
	}

	// migrate Sequences
	for (int i=0;i<sourceConfig.sequenceCount();i++) {
	    Sequence source = sourceConfig.sequence(i);
	    Sequence target = targetConfig.insertSequence(i,source.name());
	}
	
	// migrate References within Paths
	for (int i=0;i<sourceConfig.pathCount();i++) {
	    Path source = sourceConfig.path(i);
	    Path target = targetConfig.path(i);
	    migrateReferences(source,target);
	}

	// migrate References within Sequnences
	for (int i=0;i<sourceConfig.sequenceCount();i++) {
	    Sequence source = sourceConfig.sequence(i);
	    Sequence target = targetConfig.sequence(i);
	    migrateReferences(source,target);
	}
	
	// migrate streams
	for (int i=0;i<sourceConfig.streamCount();i++) {
	    Stream source = sourceConfig.stream(i);
	    Stream target = targetConfig.insertStream(i,source.label());
	    Iterator it = source.pathIterator();
	    while (it.hasNext()) {
		Path sourcePath = (Path)it.next();
		Path targetPath = targetConfig.path(sourcePath.name());
		if (targetPath!=null)
		    target.insertPath(targetPath);
		else
		    System.out.println("ERROR: path '"+sourcePath.name()+
				       "' not found in target configuration!");
	    }
	}

	return true;
    }
    
    /** retrieve message iterator */
    public Iterator messageIterator() { return messages.iterator(); }
    
    /** retrieve number of missing templates */
    public int missingTemplateCount() { return missingTemplateCount; }

    /** retrieve number of missing parameters */
    public int missingParameterCount() { return missingParameterCount; }
    
    /** retrieve number of missing templates */
    public int mismatchParameterTypeCount() { return mismatchParameterTypeCount; }

    
    //
    // private memeber functions
    //

    /** set the target parameters according to the source parameters */
    private void migrateParameters(Instance source,Instance target)
    {
	if (source.parameterCount()!=target.parameterCount()) {
	    String msg =
		"PARAMETER COUNT MISMATCH: '"+source.template().name()+
		"' source="+source.parameterCount()+
		" target="+target.parameterCount();
	    messages.add(msg);
	}
	
	for (int i=0;i<target.parameterCount();i++) {
	    Parameter targetParameter = target.parameter(i);
	    String    parameterName   = targetParameter.name();
	    String    parameterType   = targetParameter.type();
	    Parameter sourceParameter = source.parameter(parameterName,parameterType);
	    if (sourceParameter!=null) {
		if (sourceParameter.type().equals(parameterType)) {
		    String valueAsString=sourceParameter.valueAsString();
		    target.updateParameter(parameterName,parameterType,valueAsString);
		}
		else {
		    String msg =
			"PARAMETER TYPE MISMATCH: " +
			source.template().type()+" '"+source.template().name()+"' : "+
			"source="+sourceParameter.type() + " " +
			"target="+parameterType;
		    messages.add(msg);
		    mismatchParameterTypeCount++;
		}
	    }
	    else {
		String msg =
		    "MISSING SOURCE PARAMETER: " +
		    source.template().type()+" '"+source.template().name()+"' : "+
		    parameterName;
		messages.add(msg);
		missingParameterCount++;
	    }
	}	
    }
    
    /** migrate references from source Path/Sequence to target Path/Sequence */
    private void migrateReferences(ReferenceContainer source,
				   ReferenceContainer target)
    {
	int iTarget=0;
	for (int i=0;i<source.entryCount();i++) {
	    Reference reference = source.entry(i);
	    if (reference instanceof PathReference) {
		Path sourcePath = (Path)reference.parent();
		Path targetPath = targetConfig
		    .path(sourceConfig.indexOfPath(sourcePath));
		Path parentPath = (Path)target;
		targetConfig.insertPathReference(parentPath,iTarget++,targetPath);
	    }
	    else if (reference instanceof SequenceReference) {
		Sequence sourceSequence = (Sequence)reference.parent();
		Sequence targetSequence = targetConfig
		    .sequence(sourceConfig.indexOfSequence(sourceSequence));
		targetConfig.insertSequenceReference(target,iTarget++,targetSequence);
	    }
	    else if (reference instanceof ModuleReference) {
		ModuleInstance sourceModule=(ModuleInstance)reference.parent();
		ModuleInstance targetModule=targetConfig.module(sourceModule.name());
		if (targetModule!=null) {
		    targetConfig.insertModuleReference(target,iTarget++,targetModule);
		}
		else {
		    String msg =
			"MODULE MISSING FROM PATH/SEQUENCE: " +
			sourceModule.template().type() + " '" + sourceModule.name() +
			"' / " + sourceModule.template().name() +
			" missing from "+source.name();
		    messages.add(msg);
		}
	    }
	}
    }
    
}

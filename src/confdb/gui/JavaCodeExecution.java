package confdb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import confdb.data.*;

/**
 * JavaCodeExecution
 * -----------------
 *
 */

public class JavaCodeExecution
{
    private Configuration config = null;

    public JavaCodeExecution(Configuration config)
    {
	this.config = config;
    }

    public Configuration config()
    {
	return this.config;
    }

    public void execute()
    {
	System.out.println(" ");
	System.out.println("[JavaCodeExecution] start:");
	runCodeFillPSet();
	//      runCode3211();
	//	runCode2466();
	//      runCode2286();
	System.out.println(" ");
	System.out.println("[JavaCodeExecution] ended!");
    }

    private void runCodeFillPSet()
    {
	PSetParameter pset = config.pset("HLTPSetPvClusterComparer");
	if (pset==null) {
	    pset = new PSetParameter("HLTPSetPvClusterComparer","",true);
	    config.insertPSet(pset);
	}
	DoubleParameter track_pt_min = new DoubleParameter("track_pt_min", 1.0,true);
	DoubleParameter track_pt_max = new DoubleParameter("track_pt_max",10.0,true);
	DoubleParameter track_chi2_max = new DoubleParameter("track_chi2_max",9999999.0,true);
	DoubleParameter track_prob_min = new DoubleParameter("track_prob_min",-1.0,true);
	pset.addParameter(track_pt_min);
	pset.addParameter(track_pt_max);
	pset.addParameter(track_chi2_max);
	pset.addParameter(track_prob_min);
	config.psets().setHasChanged();
    }

    private void runCode3211()
    {

	// Example code to migrate the HLT config following integration of PR #3211
	// used to make PR #3322

	// Turn TrajectoryFilterESProducer instances into top-level PSets
	// and remove ESP instances
	esModule2PSetTypeA3211("TrajectoryFilterESProducer");
	// Turn [Muon]CkfTrajectoryBuilderESProducer instances into top-level PSets
	// and remove ESP instances
	esModule2PSetTypeB3211("CkfTrajectoryBuilderESProducer");
	esModule2PSetTypeB3211("MuonCkfTrajectoryBuilderESProducer");
	// esModule2PSetTypeB3211("GroupedCkfTrajectoryBuilderESProducer"): not used in HLT config

	// Update CkfTrackCandidateMaker and CkfTrajectoryMaker instances
	edModuleUpdate3211("CkfTrackCandidateMaker");
	edModuleUpdate3211("CkfTrajectoryMaker");


	// Adding the cache producer and InputTag
	ModuleInstance cacheModule     = null;
	String         cacheModuleName = null;
	PSetParameter     pset = null;
	InputTagParameter para = null;

	// Find all SiPixelClusterProducer instances
	ModuleInstance SiPixelClusterProducerModule     = null;
	String         SiPixelClusterProducerModuleName = null;
	for (int i=0; i<config.moduleCount(); i++) {
          SiPixelClusterProducerModule = config.module(i);
	  if (SiPixelClusterProducerModule.template().name().equals("SiPixelClusterProducer")) {
	    // Found an instance of SiPixelClusterProducer
	    SiPixelClusterProducerModuleName = SiPixelClusterProducerModule.name();
	    System.out.println("Found SiPixelClusterProducer instance "+SiPixelClusterProducerModuleName);

	    // Name of corresponding (new) caching module
	    cacheModuleName = SiPixelClusterProducerModuleName+"Cache";

	    // Construct the caching module and insert it in list of modules
	    System.out.println("  Inserting new SiPixelClusterShapeCacheProducer instance: "+cacheModuleName);
	    cacheModule = config.insertModule("SiPixelClusterShapeCacheProducer",cacheModuleName);
	    cacheModule.updateParameter("src","InputTag",SiPixelClusterProducerModuleName);

	    // Insert new caching module in sequences/paths directly after the SiPixelClusterProducer instance
	    for (int l=0; l<SiPixelClusterProducerModule.referenceCount(); l++) {
	      Reference reference = SiPixelClusterProducerModule.reference(l);
	      ReferenceContainer container = reference.container();
	      int index = container.indexOfEntry(reference);
	      Reference existing = container.entry(cacheModuleName);
	      if (existing == null) {
		System.out.println("  Insert Reference to "+cacheModuleName+" into "+container.name());
		cacheModule.createReference(container,index+1);
	      } else if (index>container.indexOfEntry(existing)) {
		System.out.println("  Moving Reference to "+cacheModuleName+" into "+container.name());
		container.moveEntry(existing,index+1);
	      } else {
		System.out.println("  NOT Inserting "+cacheModuleName+" into "+container.name());
	      }  
	    }

	    // Find SiPixelRecHitConverter instances using product of SiPixelClusterProducer instance
	    ModuleInstance  SiPixelRecHitConverterModule     = null;
	    String          SiPixelRecHitConverterModuleName = null;
	    for (int j=0; j<config.moduleCount(); j++) {
	      SiPixelRecHitConverterModule = config.module(j);
	      if (SiPixelRecHitConverterModule.template().name().equals("SiPixelRecHitConverter")) {
		// Found instance of SiPixelRecHitConverter
	        SiPixelRecHitConverterModuleName = SiPixelRecHitConverterModule.name();
		System.out.println("  Found SiPixelRecHitConverter instance "+SiPixelRecHitConverterModuleName);
		// Check if this instance uses the SiPixelClusterProducer instance
		if (SiPixelRecHitConverterModule.parameter("src","InputTag").valueAsString().equals(SiPixelClusterProducerModuleName)) {
		  System.out.println("  Found using SiPixelRecHitConverter instance "+SiPixelRecHitConverterModuleName);

		  // Find SeedingLayersEDProducer instances using product of SiPixelRecHitConverter instance
		  ModuleInstance SeedingLayersEDProducerModule     = null;
		  String         SeedingLayersEDProducerModuleName = null;
		  for (int k=0; k<config.moduleCount(); k++) {
		    SeedingLayersEDProducerModule = config.module(k);
		    if (SeedingLayersEDProducerModule.template().name().equals("SeedingLayersEDProducer")) {
		      // Found instance of SeedingLayersEDProducer
		      SeedingLayersEDProducerModuleName = SeedingLayersEDProducerModule.name();
		      System.out.println("    Found SeedingLayersEDProducer instance "+SeedingLayersEDProducerModuleName);
		      PSetParameter psetfpix = (PSetParameter) SeedingLayersEDProducerModule.parameter("FPix","PSet");
		      PSetParameter psetbpix = (PSetParameter) SeedingLayersEDProducerModule.parameter("BPix","PSet");
		      // Check if this instance uses the SiPixelRecHitConverter instance
		      if ( (psetfpix.parameter("HitProducer")!=null) && (psetbpix.parameter("HitProducer")!=null) ) {
			String FPixHitProducer = psetfpix.parameter("HitProducer").valueAsString();
			FPixHitProducer = FPixHitProducer.substring(1,FPixHitProducer.length()-1);
			String BPixHitProducer = psetbpix.parameter("HitProducer").valueAsString();
			BPixHitProducer = BPixHitProducer.substring(1,BPixHitProducer.length()-1);
			System.out.println("    Found SeedingLayersEDProducer instance with HitProducers "+SeedingLayersEDProducerModuleName);
			// Check if this instance uses the SiPixelRecHitConverter instance
			if ( FPixHitProducer.equals(SiPixelRecHitConverterModuleName) &&
			     BPixHitProducer.equals(SiPixelRecHitConverterModuleName) ) {
			  System.out.println("    Found using SeedingLayersEDProducer instance "+SeedingLayersEDProducerModuleName);

			  // Find PixelTrackProducer instances using product of SeedingLayersEDProducer instance
			  ModuleInstance PixelTrackProducerModule     = null;
			  String         PixelTrackProducerModuleName = null;
			  for (int l=0; l<config.moduleCount(); l++) {
			    PixelTrackProducerModule = config.module(l);
			    if (PixelTrackProducerModule.template().name().equals("PixelTrackProducer")) {
			      // Found PixelTrackProducer instance
			      PixelTrackProducerModuleName = PixelTrackProducerModule.name();
			      System.out.println("      Found PixelTrackProducer instance "+PixelTrackProducerModuleName);
			      String ComponentName = null;

			      pset = (PSetParameter) PixelTrackProducerModule.parameter("OrderedHitsFactoryPSet");
			      // Check if this instance uses the SeedingLayersEDProducer instance
			      if (pset.parameter("SeedingLayers").valueAsString().equals(SeedingLayersEDProducerModuleName)) {
				pset = (PSetParameter) pset.parameter("GeneratorPSet");
				pset = (PSetParameter) pset.parameter("SeedComparitorPSet");
				System.out.println("      Found using PixelTrackProducer instance "+PixelTrackProducerModuleName);
				// Check further is this instance needs to be updated, and do so.

				ComponentName = pset.parameter("ComponentName").valueAsString();
				ComponentName = ComponentName.substring(1,ComponentName.length()-1);
				if (ComponentName.equals("LowPtClusterShapeSeedComparitor")) {
				  System.out.println("      Found using PixelTrackProducer instance to update A "+PixelTrackProducerModuleName);
				  para = new InputTagParameter("clusterShapeCacheSrc",cacheModuleName,true);
				  pset.addParameter(para);
				  PixelTrackProducerModule.setHasChanged();
				}
				// special HIon case
				pset = (PSetParameter) PixelTrackProducerModule.parameter("FilterPSet");
				ComponentName = pset.parameter("ComponentName").valueAsString();
				ComponentName = ComponentName.substring(1,ComponentName.length()-1);
				if (ComponentName.equals("HIPixelTrackFilter")) {
				    System.out.println("      Found using PixelTrackProducer instance to update H "+PixelTrackProducerModuleName);
				    para = new InputTagParameter("clusterShapeCacheSrc",cacheModuleName,true);
				    pset.addParameter(para);
				    PixelTrackProducerModule.setHasChanged();
				}

			      }
			    }
			  }
			}
		      }
		    }
		  }
		}
	      }
	    }
	  }
	}
    }

    private void esModule2PSetTypeA3211(String templateName)
    {
	// esModule2PSetTypeA3211("TrajectoryFilterESProducer");
	String name = null;
	PSetParameter pset = null;
	ESModuleInstance esmodule = null;
	for (int i=config.esmoduleCount()-1; i>=0; i--) {
	    esmodule = config.esmodule(i);
	    if (esmodule.template().name().equals(templateName)) {
		name = esmodule.parameter("ComponentName","string").valueAsString();
		name = name.substring(1,name.length()-1);
		name = name.replace("hlt","HLT").replace("ESP","PSet");
		System.out.println("Converting "+templateName+" to top-level PSet: "+esmodule.name()+" / "+name);
		pset = new PSetParameter(name,"",true);

		// Copy over all parameters from filterPset to PSet
		PSetParameter filterPset = (PSetParameter) esmodule.parameter("filterPset","PSet");
		Iterator<Parameter> itP = filterPset.parameterIterator();
		while (itP.hasNext()) {
		    Parameter p = itP.next();
		    pset.addParameter(p.clone(pset));
		}
		config.insertPSet(pset);
		// Remove obsolete ESModule
		System.out.println("  Removing "+templateName+" instance "+esmodule.name());
		config.removeESModule(esmodule);
	    }
	}
    }

    private void esModule2PSetTypeB3211(String templateName)
    {
	// esModule2PSetTypeB3211("CkfTrajectoryBuilderESProducer");
	// esModule2PSetTypeB3211("MuonCkfTrajectoryBuilderESProducer");
	// esModule2PSetTypeB3211("GroupedCkfTrajectoryBuilderESProducer"): not used in HLT config
	String name = null;
	PSetParameter pset = null;
	StringParameter para = null;
	ESModuleInstance esmodule = null;
	for (int i=config.esmoduleCount()-1; i>=0; i--) {
	    esmodule = config.esmodule(i);
	    if (esmodule.template().name().equals(templateName)) {
		name = esmodule.parameter("ComponentName","string").valueAsString();
		name = name.substring(1,name.length()-1);
		name = name.replace("hlt","HLT").replace("ESP","PSet");
		System.out.println("Converting "+templateName+" to top-level PSet: "+esmodule.name()+" / "+name);
		pset = new PSetParameter(name,"",true);
		
		// Copy over all parameters from esmodule to PSet as far as required
		Iterator<Parameter> itP = esmodule.parameterIterator();
		while (itP.hasNext()) {
		    Parameter p = itP.next();
		    if (p.name().equals("ComponentName")) {
			para = new StringParameter("ComponentType",templateName.replace("ESProducer",""),true);
			pset.addParameter(para);
		    } else if (p.name().equals("trajectoryFilterName")) {
			PSetParameter pset1 = new PSetParameter("trajectoryFilter","",true);
			para = new StringParameter("refToPSet_",p.valueAsString().replace("hlt","HLT").replace("ESP","PSet"),true);
			pset1.addParameter(para);
			pset.addParameter(pset1);
		    } else {
			pset.addParameter(p.clone(pset));
		    }
		}
		config.insertPSet(pset);
		// Remove obsolete ESModule
		System.out.println("  Removing "+templateName+" "+esmodule.name());
		config.removeESModule(esmodule);
	    }
	}
    }

    private void edModuleUpdate3211(String templateName)
    {
	String value = null;
	StringParameter para = null;
	PSetParameter pset = null;
	ModuleInstance module = null;
	for (int i=0; i<config.moduleCount(); i++) {
	    module = config.module(i);
	    if (module.template().name().equals(templateName)) {
		value = config.module(module.name()).parameter("TrajectoryBuilder","string").valueAsString();
		value = value.substring(1,value.length()-1);
		value = value.replace("hlt","HLT").replace("ESP","PSet");
		System.out.println("Converting "+templateName+" to access top-level PSet: "+module.name()+" / "+value);
		pset = new PSetParameter("TrajectoryBuilderPSet","",true);
		para = new StringParameter("refToPSet_",value,true);
		pset.addParameter(para);
		module.updateParameter("TrajectoryBuilderPSet","PSet",pset.valueAsString());
		module.setHasChanged();
	    }
	}
    }

    private void runCode2466()
    {
	// Example code to migrate the HLT config following integration of PR #2466,
	// used to make PR #2640

	// Update parameters of MeasurementTrackerESProducer instances
	ESModuleInstance esmodule = null;
	for (int i=0; i<config.esmoduleCount(); i++) {
	    esmodule = config.esmodule(i);
	    if (esmodule.template().name().equals("MeasurementTrackerESProducer")) {
		System.out.println("Fixing MeasurementTrackerESProducer parameters (2): "+esmodule.name());
		esmodule.updateParameter("Regional","bool","false");
		esmodule.updateParameter("OnDemand","bool","false");
	    }
	}

	// Update parameters MaskedMeasurementTrackerEventProducer instances
	ModuleInstance module = null;
	for (int i=0; i<config.moduleCount(); i++) {
	    module = config.module(i);
	    if (module.template().name().equals("MaskedMeasurementTrackerEventProducer")) {
		System.out.println("Fixing MaskedMeasurementTrackerEventProducer parameters (1): "+module.name());
		module.updateParameter("OnDemand","bool","false");
	    }
	}

	// Replacements keeping module label names
	replaceAllInstances(    0,"HLTTrackClusterRemover","HLTTrackClusterRemoverNew");
	replaceAllInstances(24660,"MeasurementTrackerSiStripRefGetterProducer","MeasurementTrackerEventProducer");
	replaceAllInstances(24661,"SiStripRawToClusters","SiStripClusterizerFromRaw");
    }

    private void replaceAllInstances(int special, String oldTemplateName, String newTemplateName)
    {
	ModuleInstance oldModule=null;
	ModuleInstance newModule=null;

	String oldModuleName=null;
	String newModuleName=null;

	String label24660 = null;

	for (int i=config.moduleCount()-1; i>=0; i--) {
	    oldModule = config.module(i);
	    if (oldModule.template().name().equals(oldTemplateName)) {
		oldModuleName = oldModule.name();
		System.out.println("Replacing "+oldTemplateName+"/"+newTemplateName+": "+oldModuleName);
		newModuleName = oldModuleName+"NEW";
		newModule = config.insertModule(newTemplateName,newModuleName);
	
		// Copy over all parameters from old to new as far as possible
		Iterator<Parameter> itP = oldModule.parameterIterator();
		while (itP.hasNext()) {
		    Parameter p = itP.next();
		    Parameter q = newModule.parameter(p.name(),p.type());
		    if (q==null) {
			System.out.println("  Parameter does not exist: "+p.name());
			if (special==24660) {
			    label24660=p.valueAsString();
			}
		    } else {
			System.out.println("  Transferring parameter  : "+p.name());
			newModule.updateParameter(p.name(),p.type(),p.valueAsString());
		    }
		}
		if (special==24660) {
		    Iterator<Parameter> itQ = newModule.parameterIterator();
		    while (itQ.hasNext()) {
			Parameter q = itQ.next();
			if (q.name().equals("stripLazyGetterProducer")) newModule.updateParameter(q.name(),q.type(),"''");
			if (q.name().equals("stripClusterProducer")) newModule.updateParameter(q.name(),q.type(),label24660);
		    }
		}
		if (special==24661) {
		    Iterator<Parameter> itQ = newModule.parameterIterator();
		    while (itQ.hasNext()) {
			Parameter q = itQ.next();
			if (q.name().equals("onDemand")) newModule.updateParameter(q.name(),q.type(),"true");
		    }
		}		    

		// Get hold of oldModule's Refs etc.
		int index = config.indexOfModule(oldModule);
		int refCount = oldModule.referenceCount();
		ReferenceContainer[] parents = new ReferenceContainer[refCount];
		int[]                indices = new int[refCount];
		Operator[]           operators = new Operator[refCount];
		int iRefCount=0;
		while (oldModule.referenceCount()>0) {
		    Reference reference = oldModule.reference(0);
		    parents[iRefCount] = reference.container();
		    indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
		    operators[iRefCount] = reference.getOperator();
		    config.removeModuleReference((ModuleReference)reference);
		    iRefCount++;
		}

		// oldModule's refCount is now 0 and hence oldModule is removed
		// from the config; thus we can rename newModule to oldModule's
		// name
		try {
		    newModule.setNameAndPropagate(oldModuleName);
		}
		catch (DataException e) {
		    System.err.println(e.getMessage());
		}
	
		// update refs pointing to oldModule to point to newModule
		for (int j=0;j<refCount;j++) {
		    config.insertModuleReference(parents[j],indices[j],newModule).setOperator(operators[j]);
		}
	
	    }	    

	}
    }

    private void runCode2286()
    {
	// Example code to migrate the HLT config for PR #2286

	// Find all SeedingLayersESProducer instances
	for (int i=config.esmoduleCount()-1; i>=0; i--) {
	    ESModuleInstance esmodule = config.esmodule(i);
	    if (esmodule.template().name().equals("SeedingLayersESProducer")) {
		// Get their ComponentName - this is what clients use to access
		String componentName  = esmodule.parameter("ComponentName","string").valueAsString();
		componentName = componentName.substring(1,componentName.length()-1);
		System.out.println(" ");
		System.out.println("Found SeedingLayersESProducer: "+esmodule.name()+" / "+componentName);

		// Prepare SeedingLayersEDProducer (replacing SeedingLayersESProducer)
		String edTemplateName = "SeedingLayersEDProducer";
		String edModuleName = componentName.replace("ESP","");

		Boolean first = true;
		ModuleInstance edModule = null;

		// Look for modules accessing the deprecated ESProducer
		for (int j=0; j<config.moduleCount(); j++) {
		    ModuleInstance module = config.module(j);
		    Parameter params[] = module.findParameters(null,"string",componentName,2);
		    int n=params.length;
		    if (n>0) {
			// Found one - need to insert replacement EDProducer in the Config
			if (first) {
			    first = false;
			    System.out.println("  Inserting new EDProducer: "+edModuleName);
			    edModule = config.insertModule(edTemplateName,edModuleName);
			    // Copy over all parameters from ESP to EDP
			    Iterator<Parameter> itP = esmodule.parameterIterator();
			    while (itP.hasNext()) {
				Parameter p = itP.next();
				Parameter q = edModule.parameter(p.name(),p.type());
				if (q==null) {
				    System.out.println("  Parameter does not exist: "+p.name());
				} else {
				    System.out.println("  Transferring parameter  : "+p.name());
				    edModule.updateParameter(p.name(),p.type(),p.valueAsString());
				}
			    }
			}
			// Insert EDProducer instance before client module
			for (int l=0;l<module.referenceCount();l++) {
			    Reference reference = module.reference(l);
			    ReferenceContainer container = reference.container();
			    int index = container.indexOfEntry(reference);
			    Reference existing  = container.entry(edModuleName);
			    if (existing == null) {
				System.out.println("  Insert Reference to "+edModuleName+" into "+container.name());
				edModule.createReference(container,index);
			    } else if (index<container.indexOfEntry(existing)) {
				System.out.println("  Moving Reference to "+edModuleName+" into "+container.name());
				container.moveEntry(existing,index);
			    } else {
				System.out.println("  NOT Inserting "+edModuleName+" into "+container.name());
			    }
			}
			// Replace string by InputTag with updated label in client module config
			System.out.println("  Updating client "+module.name()+": "+n+" parameters to fix");
			for (int k=0; k<n; k++) {
			    String fullName = params[k].fullName();
			    System.out.println("    Fixing parameter "+k+": "+fullName);
                            Parameter parameter = new InputTagParameter(params[k].name(),edModuleName,"","",params[k].isTracked());
			    PSetParameter pset = (PSetParameter)params[k].parent();
			    pset.removeParameter(params[k]);
			    pset.addParameter(parameter);
			}
			module.setHasChanged();
		    }
		}
		// Remove all deprecated ESProducers
		System.out.println("  Removing SeedingLayersESProducer "+esmodule.name());
		config.removeESModule(esmodule);
	    }
	}
    }
}

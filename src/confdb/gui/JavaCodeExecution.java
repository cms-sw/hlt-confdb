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
    private Configuration currentConfig = null;

    public JavaCodeExecution(Configuration currentConfig)
    {
	this.currentConfig = currentConfig;
    }

    public Configuration currentConfig()
    {
	return this.currentConfig;
    }

    public void execute()
    {
	System.out.println(" ");
	System.out.println("[JavaCodeExecution] start:");
	//        runCode2286();
	runCode2466();
	System.out.println(" ");
	System.out.println("[JavaCodeExecution] ended!");
    }

    private void runCode2466()
    {
	// Example code to migrate the HLT config following integration of PR #2466,
	// used to make PR #2640

	// Update parameters of MeasurementTrackerESProducer instances
	ESModuleInstance esmodule = null;
	for (int i=0; i<currentConfig.esmoduleCount(); i++) {
	    esmodule = currentConfig.esmodule(i);
	    if (esmodule.template().name().equals("MeasurementTrackerESProducer")) {
		System.out.println("Fixing MeasurementTrackerESProducer parameters (2): "+esmodule.name());
		esmodule.updateParameter("Regional","bool","false");
		esmodule.updateParameter("OnDemand","bool","false");
	    }
	}

	// Update parameters MaskedMeasurementTrackerEventProducer instances
	ModuleInstance module = null;
	for (int i=0; i<currentConfig.moduleCount(); i++) {
	    module = currentConfig.module(i);
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

	for (int i=currentConfig.moduleCount()-1; i>=0; i--) {
	    oldModule = currentConfig.module(i);
	    if (oldModule.template().name().equals(oldTemplateName)) {
		oldModuleName = oldModule.name();
		System.out.println("Replacing "+oldTemplateName+"/"+newTemplateName+": "+oldModuleName);
		newModuleName = oldModuleName+"NEW";
		newModule = currentConfig.insertModule(newTemplateName,newModuleName);
	
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
		int index = currentConfig.indexOfModule(oldModule);
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
		    currentConfig.removeModuleReference((ModuleReference)reference);
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
		    currentConfig.insertModuleReference(parents[j],indices[j],newModule).setOperator(operators[j]);
		}
	
	    }	    

	}
    }

    private void runCode2286()
    {
	// Example code to migrate the HLT config for PR #2286

	// Find all SeedingLayersESProducer instances
	for (int i=currentConfig.esmoduleCount()-1; i>=0; i--) {
	    ESModuleInstance esmodule = currentConfig.esmodule(i);
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
		for (int j=0; j<currentConfig.moduleCount(); j++) {
		    ModuleInstance module = currentConfig.module(j);
		    Parameter params[] = module.findParameters(null,"string",componentName,2);
		    int n=params.length;
		    if (n>0) {
			// Found one - need to insert replacement EDProducer in the Config
			if (first) {
			    first = false;
			    System.out.println("  Inserting new EDProducer: "+edModuleName);
			    edModule = currentConfig.insertModule(edTemplateName,edModuleName);
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
		currentConfig.removeESModule(esmodule);
	    }
	}
    }
}

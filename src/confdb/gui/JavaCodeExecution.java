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
        runCode();
	System.out.println(" ");
	System.out.println("[JavaCodeExecution] ended!");
    }

    private void runCode()
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
		    }
		}
		// Remove all deprecated ESProducers
		System.out.println("  Removing SeedingLayersESProducer "+esmodule.name());
		currentConfig.removeESModule(esmodule);
	    }
	}
    }
}

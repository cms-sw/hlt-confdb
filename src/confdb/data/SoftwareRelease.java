package confdb.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * SoftwareRelease
 * ---------------
 * @author Philipp Schieferdecker
 *
 * Manage software component templates for a release. Templates are
 * organized in software packages which are organized in software
 * subsystems. 
 */
public class SoftwareRelease implements Serializable
{
    //
    // member data
    //
    
    /** name of the release */
    private String releaseTag="";
    
    /** list of software subsystems */
    private ArrayList<SoftwareSubsystem> subsystems = null;


    /** list of all available templates */
    private ArrayList<Template> templates = null;
    
    /** list of available EDSource templates */
    private ArrayList<EDSourceTemplate> edsourceTemplates = null;
    
    /** list of available ESSource templates */
    private ArrayList<ESSourceTemplate> essourceTemplates = null;
    
    /** list of available ESModule templates */
    private ArrayList<ESModuleTemplate> esmoduleTemplates = null;
    
    /** list of available Service templates */
    private ArrayList<ServiceTemplate> serviceTemplates = null;
    
    /** list of available Module templates */
    private ArrayList<ModuleTemplate> moduleTemplates = null;
    

    /** hash-map of available templates BY NAME */
    private HashMap<String,Template> templatesByName = null;

    /** hash-map of available EDSource templates BY NAME */
    private HashMap<String,EDSourceTemplate> edsourceTemplatesByName = null;

    /** hash-map of available ESSource templates BY NAME */
    private HashMap<String,ESSourceTemplate> essourceTemplatesByName = null;

    /** hash-map of available ESModule templates BY NAME */
    private HashMap<String,ESModuleTemplate> esmoduleTemplatesByName = null;

    /** hash-map of available Service templates BY NAME */
    private HashMap<String,ServiceTemplate> serviceTemplatesByName = null;

    /** hash-map of available Module templates BY NAME */
    private HashMap<String,ModuleTemplate> moduleTemplatesByName  = null;
    
    
    /** hash-map of available EDSource templates names BY DBID */
    private HashMap<Integer,String> edsourceTemplateNamesByDbId = null;

    /** hash-map of available ESSource templates names BY DBID */
    private HashMap<Integer,String> essourceTemplateNamesByDbId = null;

    /** hash-map of available ESModule templates names BY DBID */
    private HashMap<Integer,String> esmoduleTemplateNamesByDbId = null;

    /** hash-map of available Service templates names BY DBID */
    private HashMap<Integer,String> serviceTemplateNamesByDbId = null;

    /** hash-map of available Module templates names BY DBID */
    private HashMap<Integer,String> moduleTemplateNamesByDbId  = null;
    

    //
    // construction
    //
    
    /** standard constructor */
    public SoftwareRelease()
    {
	releaseTag = new String();
	
	subsystems = new ArrayList<SoftwareSubsystem>();
	
	templates            = new ArrayList<Template>();
	edsourceTemplates    = new ArrayList<EDSourceTemplate>();
	essourceTemplates    = new ArrayList<ESSourceTemplate>();
	esmoduleTemplates    = new ArrayList<ESModuleTemplate>();
	serviceTemplates     = new ArrayList<ServiceTemplate>();
	moduleTemplates      = new ArrayList<ModuleTemplate>();
	
	templatesByName         = new HashMap<String,Template>();
	edsourceTemplatesByName = new HashMap<String,EDSourceTemplate>();
	essourceTemplatesByName = new HashMap<String,ESSourceTemplate>();
	esmoduleTemplatesByName = new HashMap<String,ESModuleTemplate>();
	serviceTemplatesByName  = new HashMap<String,ServiceTemplate>();
	moduleTemplatesByName   = new HashMap<String,ModuleTemplate>();

	edsourceTemplateNamesByDbId = new HashMap<Integer,String>();
	essourceTemplateNamesByDbId = new HashMap<Integer,String>();
	esmoduleTemplateNamesByDbId = new HashMap<Integer,String>();
	serviceTemplateNamesByDbId  = new HashMap<Integer,String>();
	moduleTemplateNamesByDbId   = new HashMap<Integer,String>();
    }
    
    /** construct release from another release */
    public SoftwareRelease(SoftwareRelease otherRelease)
    {
	this.releaseTag = otherRelease.releaseTag();
	
	subsystems = new ArrayList<SoftwareSubsystem>();
	
	templates            = new ArrayList<Template>();
	edsourceTemplates    = new ArrayList<EDSourceTemplate>();
	essourceTemplates    = new ArrayList<ESSourceTemplate>();
	esmoduleTemplates    = new ArrayList<ESModuleTemplate>();
	serviceTemplates     = new ArrayList<ServiceTemplate>();
	moduleTemplates      = new ArrayList<ModuleTemplate>();
	
	templatesByName         = new HashMap<String,Template>();
	edsourceTemplatesByName = new HashMap<String,EDSourceTemplate>();
	essourceTemplatesByName = new HashMap<String,ESSourceTemplate>();
	esmoduleTemplatesByName = new HashMap<String,ESModuleTemplate>();
	serviceTemplatesByName  = new HashMap<String,ServiceTemplate>();
	moduleTemplatesByName   = new HashMap<String,ModuleTemplate>();

	edsourceTemplateNamesByDbId = new HashMap<Integer,String>();
	essourceTemplateNamesByDbId = new HashMap<Integer,String>();
	esmoduleTemplateNamesByDbId = new HashMap<Integer,String>();
	serviceTemplateNamesByDbId  = new HashMap<Integer,String>();
	moduleTemplateNamesByDbId   = new HashMap<Integer,String>();
	
	Iterator<SoftwareSubsystem> subsysIt = otherRelease.subsystemIterator();
	while (subsysIt.hasNext()) {
	    SoftwareSubsystem otherSubsys = subsysIt.next();
	    SoftwareSubsystem subsys = new SoftwareSubsystem(otherSubsys.name());
	    Iterator<SoftwarePackage> pkgIt = otherSubsys.packageIterator();
	    while (pkgIt.hasNext()) {
		SoftwarePackage otherPkg = pkgIt.next();
		SoftwarePackage pkg = new SoftwarePackage(otherPkg.name());
		subsys.addPackage(pkg);
		Iterator<Template> templateIt = otherPkg.templateIterator();
		while (templateIt.hasNext()) {
		    Template template = templateIt.next();
		    ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		    Iterator<Parameter> parameterIt = template.parameterIterator();
		    while (parameterIt.hasNext()) {
			Parameter p = parameterIt.next();
			parameters.add(p.clone(null));
		    }
		    if (template instanceof EDSourceTemplate) {
			EDSourceTemplate newTemplate = 
			    new EDSourceTemplate(template.name(),
						 template.cvsTag(),
						 parameters);
			newTemplate.setDatabaseId(template.databaseId());
			pkg.addTemplate(newTemplate);
		    }
		    else if (template instanceof ESSourceTemplate) {
			ESSourceTemplate newTemplate =
			    new ESSourceTemplate(template.name(),
						 template.cvsTag(),
						 parameters);
			newTemplate.setDatabaseId(template.databaseId());
			pkg.addTemplate(newTemplate);
		    }
		    else if (template instanceof ESModuleTemplate) {
			ESModuleTemplate newTemplate =
			    new ESModuleTemplate(template.name(),
						 template.cvsTag(),
						 parameters);
			newTemplate.setDatabaseId(template.databaseId());
			pkg.addTemplate(newTemplate);
		    }
		    else if (template instanceof ServiceTemplate) {
			ServiceTemplate newTemplate =
			    new ServiceTemplate(template.name(),
						template.cvsTag(),
						parameters);
			newTemplate.setDatabaseId(template.databaseId());
			pkg.addTemplate(newTemplate);
		    }
		    else if (template instanceof ModuleTemplate) {
			ModuleTemplate newTemplate =
			    new ModuleTemplate(template.name(),
					       template.cvsTag(),
					       parameters,
					       template.type());
			newTemplate.setDatabaseId(template.databaseId());
			pkg.addTemplate(newTemplate);
		    }
		}
	    }
	    addSubsystem(subsys);
	}
    }

    
    //
    // member functions
    //
    
    /** get the release tag */
    public String releaseTag() { return releaseTag; }

    /** get number of subsystems */
    public int subsystemCount() { return subsystems.size(); }

    /** get the i-th subsystem */
    public SoftwareSubsystem subsystem(int i) { return subsystems.get(i); }

    /** get subsystem iterator */
    public Iterator<SoftwareSubsystem> subsystemIterator() { return subsystems.iterator(); }

    /** index of a certain subsystem */
    public int indexOfSubsystem(SoftwareSubsystem s) { return subsystems.indexOf(s); }
    
    /** get list of all referenced packages */
    public ArrayList<String> listOfReferencedPackages()
    {
	ArrayList<String> result = new ArrayList<String>();
	Iterator<SoftwareSubsystem> itS = subsystemIterator();
	while (itS.hasNext()) {
	    SoftwareSubsystem subsys = itS.next();
	    if (subsys.referencedPackageCount()==0) continue;
	    Iterator<SoftwarePackage> itP = subsys.packageIterator();
	    while (itP.hasNext()) {
		SoftwarePackage pkg = itP.next();
		if (pkg.instantiatedTemplateCount()>0)
		    result.add(subsys.name()+"/"+pkg.name());
	    }
	}
	return result;
    }

    /** get list of all all packages, including cvstag */
    public ArrayList<String> listOfPackages()
    {
	ArrayList<String> result = new ArrayList<String>();
	Iterator<SoftwareSubsystem> itS = subsystemIterator();
	while (itS.hasNext()) {
	    SoftwareSubsystem subsys = itS.next();
	    Iterator<SoftwarePackage> itP = subsys.packageIterator();
	    while (itP.hasNext()) {
		SoftwarePackage pkg = itP.next();
		if (pkg.templateCount()>0)
		    result.add(pkg.template(0).cvsTag()+" "+
			       subsys.name()+"/"+pkg.name());
	    }
	}
	return result;
    }


    /** sort software subsystems */
    public void sortSubsystems() { Collections.sort(subsystems); }

    /** sort templates alphabetically */
    public void sortTemplates()
    {
	Collections.sort(templates);
	Collections.sort(edsourceTemplates);
	Collections.sort(essourceTemplates);
	Collections.sort(esmoduleTemplates);
	Collections.sort(serviceTemplates);
	Collections.sort(moduleTemplates);
    }
    
    /** clear the whole release */
    public void clear(String releaseTag)
    {
	this.releaseTag = releaseTag;

	subsystems.clear();

	templates.clear();
	edsourceTemplates.clear();
	essourceTemplates.clear();
	esmoduleTemplates.clear();
	serviceTemplates.clear();
	moduleTemplates.clear();
	
	templatesByName.clear();
	edsourceTemplatesByName.clear();
	essourceTemplatesByName.clear();
	esmoduleTemplatesByName.clear();
	serviceTemplatesByName.clear();
	moduleTemplatesByName.clear();

	edsourceTemplateNamesByDbId.clear();
	essourceTemplateNamesByDbId.clear();
	esmoduleTemplateNamesByDbId.clear();
	serviceTemplateNamesByDbId.clear();
	moduleTemplateNamesByDbId.clear();
    }
    
    /** clear the instances which might be instantiated */
    public int clearInstances()
    {
	int result = 0;
	for (Template t : templates) result += t.removeAllInstances();
	return result;
    }

    /** count all instances */
    public int instanceCount()
    {
	int result = 0;
	for (Template t : templates) result += t.instanceCount();
	return result;
    }
    
    /** template iterator */
    public Iterator<Template> templateIterator()
    {
	return templates.iterator();
    }

    /** EDSource template iterator */
    public Iterator<EDSourceTemplate> edsourceTemplateIterator()
    {
	return edsourceTemplates.iterator();
    }

    /** ESSource template iterator */
    public Iterator<ESSourceTemplate> essourceTemplateIterator()
    {
	return essourceTemplates.iterator();
    }

    /** ESModule template iterator */
    public Iterator<ESModuleTemplate> esmoduleTemplateIterator()
    {
	return esmoduleTemplates.iterator();
    }

    /** Service template iterator */
    public Iterator<ServiceTemplate> serviceTemplateIterator()
    {
	return serviceTemplates.iterator();
    }

    /** Module template iterator */
    public Iterator<ModuleTemplate> moduleTemplateIterator()
    {
	return moduleTemplates.iterator();
    }

    /** number of templates */
    public int templateCount() { return templates.size(); }

    /** number of EDSource templates */
    public int edsourceTemplateCount() { return edsourceTemplates.size(); }

    /** number of ESSource templates */
    public int essourceTemplateCount() { return essourceTemplates.size(); }

    /** number of ESModule templates */
    public int esmoduleTemplateCount() { return esmoduleTemplates.size(); }

    /** number of Service templates */
    public int serviceTemplateCount() { return serviceTemplates.size(); }

    /** number of Module templates */
    public int moduleTemplateCount() { return moduleTemplates.size(); }

    /** get template by name */
    public Template template(String name)
    {
	return templatesByName.get(name);
    }

    /** get EDSource template by name */
    public EDSourceTemplate edsourceTemplate(String name)
    {
	return edsourceTemplatesByName.get(name);
    }
    
    /** get ESSource template by name */
    public ESSourceTemplate essourceTemplate(String name)
    {
	return essourceTemplatesByName.get(name);
    }
    
    /** get ESModule template by name */
    public ESModuleTemplate esmoduleTemplate(String name)
    {
	return esmoduleTemplatesByName.get(name);
    }
    
    /** get Service template by name */
    public ServiceTemplate serviceTemplate(String name)
    {
	return serviceTemplatesByName.get(name);
    }
    
    /** get Module template by name */
    public ModuleTemplate moduleTemplate(String name)
    {
	return moduleTemplatesByName.get(name);
    }
    
    /** get EDSource template name by dbId */
    public String edsourceTemplateName(int dbId)
    {
	return edsourceTemplateNamesByDbId.get(dbId);
    }
    
    /** get ESSource template name by dbId */
    public String essourceTemplateName(int dbId)
    {
	return essourceTemplateNamesByDbId.get(dbId);
    }
    
    /** get ESModule template name by dbId */
    public String esmoduleTemplateName(int dbId)
    {
	return esmoduleTemplateNamesByDbId.get(dbId);
    }
    
    /** get Service template name by dbId */
    public String serviceTemplateName(int dbId)
    {
	return serviceTemplateNamesByDbId.get(dbId);
    }
    
    /** get Module template name by dbId */
    public String moduleTemplateName(int dbId)
    {
	return moduleTemplateNamesByDbId.get(dbId);
    }
    

    /** add a software subsystem */
    public boolean addSubsystem(SoftwareSubsystem subsystem)
    {
	subsystems.add(subsystem);
	Iterator<SoftwarePackage> itP = subsystem.packageIterator();
	while (itP.hasNext()) {
	    SoftwarePackage pkg = itP.next();
	    Iterator<Template> templateIt = pkg.templateIterator();
	    while (templateIt.hasNext()) {
		Template t = templateIt.next();
		addTemplate(t);
	    }
	}
	return true;
    }

    //
    // private member functions
    //
    
    /** add a template */
    private boolean addTemplate(Template template)
    {
	if (template instanceof EDSourceTemplate) {
	    EDSourceTemplate edsource = (EDSourceTemplate)template;
	    if (!edsourceTemplatesByName.containsKey(edsource.name())) {
		edsourceTemplates.add(edsource);
		edsourceTemplatesByName.put(edsource.name(),edsource);
		edsourceTemplateNamesByDbId.put(edsource.databaseId(),
						edsource.name());
		templates.add(edsource);
		templatesByName.put(edsource.name,edsource);
		return true;
	    }
	}
	else if (template instanceof ESSourceTemplate) {
	    ESSourceTemplate essource = (ESSourceTemplate)template;
	    if (!essourceTemplatesByName.containsKey(essource.name())) {
		essourceTemplates.add(essource);
		essourceTemplatesByName.put(essource.name(),essource);
		essourceTemplateNamesByDbId.put(essource.databaseId(),
						essource.name());
		templates.add(essource);
		templatesByName.put(essource.name(),essource);
		return true;
	    }
	}
	else if (template instanceof ESModuleTemplate) {
	    ESModuleTemplate esmodule = (ESModuleTemplate)template;
	    if (!esmoduleTemplatesByName.containsKey(esmodule.name())) {
		esmoduleTemplates.add(esmodule);
		esmoduleTemplatesByName.put(esmodule.name(),esmodule);
		esmoduleTemplateNamesByDbId.put(esmodule.databaseId(),
						esmodule.name());
		templates.add(esmodule);
		templatesByName.put(esmodule.name(),esmodule);
		return true;
	    }
	}
	else if (template instanceof ServiceTemplate) {
	    ServiceTemplate service = (ServiceTemplate)template;
	    if (!serviceTemplatesByName.containsKey(service.name())) {
		serviceTemplates.add(service);
		serviceTemplatesByName.put(service.name(),service);
		serviceTemplateNamesByDbId.put(service.databaseId(),
					       service.name());
		templates.add(service);
		templatesByName.put(service.name(),service);
		return true;
	    }
	}
	else if (template instanceof ModuleTemplate) {
	    ModuleTemplate module = (ModuleTemplate)template;
	    if (!moduleTemplatesByName.containsKey(module.name())) {
		moduleTemplates.add(module);
		moduleTemplatesByName.put(module.name(),module);
		moduleTemplateNamesByDbId.put(module.databaseId(),
					      module.name());
		templates.add(module);
		templatesByName.put(module.name(),module);
		return true;
	    }
	}
	
	//System.err.println("addTemplate("+template.name()+
	//	   ") FAILED ("+template.getClass().getName()+")");
	
	return false;
    }
    
}

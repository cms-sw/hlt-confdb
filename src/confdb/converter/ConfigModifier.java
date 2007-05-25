package confdb.converter;

import java.util.ArrayList;

import confdb.data.ConfigInfo;
import confdb.data.Configuration;
import confdb.data.Directory;
import confdb.data.EDSourceInstance;
import confdb.data.ESSourceInstance;
import confdb.data.ModuleInstance;
import confdb.data.ModuleReference;
import confdb.data.ModuleTemplate;
import confdb.data.PSetParameter;
import confdb.data.Path;
import confdb.data.PathReference;
import confdb.data.ReferenceContainer;
import confdb.data.Sequence;
import confdb.data.SequenceReference;
import confdb.data.ServiceInstance;
import confdb.data.Template;

public class ConfigModifier extends Configuration 
{
	private Configuration orig = null;
	
	public ConfigModifier( Configuration orig, Path newEndpath )
	{
		this.orig = orig;
	}
	
	

	public void addNextVersion(int versionId, String created, String releaseTag) {
		orig.addNextVersion(versionId, created, releaseTag);
	}

	public String created() {
		return orig.created();
	}

	public int dbId() {
		return orig.dbId();
	}

	public EDSourceInstance edsource(int i) {
		return orig.edsource(i);
	}

	public int edsourceCount() {
		return orig.edsourceCount();
	}

	public boolean equals(Object obj) {
		return orig.equals(obj);
	}

	public ESSourceInstance essource(int i) {
		return orig.essource(i);
	}

	public int essourceCount() {
		return orig.essourceCount();
	}

	public boolean hasChanged() {
		return orig.hasChanged();
	}

	public int hashCode() {
		return orig.hashCode();
	}

	public int indexOfEDSource(EDSourceInstance edsource) {
		return orig.indexOfEDSource(edsource);
	}

	public int indexOfESSource(ESSourceInstance essource) {
		return orig.indexOfESSource(essource);
	}

	public int indexOfModule(ModuleInstance module) {
		return orig.indexOfModule(module);
	}

	public int indexOfPath(Path path) {
		return orig.indexOfPath(path);
	}

	public int indexOfPSet(PSetParameter pset) {
		return orig.indexOfPSet(pset);
	}

	public int indexOfSequence(Sequence sequence) {
		return orig.indexOfSequence(sequence);
	}

	public int indexOfService(ServiceInstance service) {
		return orig.indexOfService(service);
	}

	public void initialize(ConfigInfo configInfo, ArrayList<Template> edsourceTemplateList, ArrayList<Template> essourceTemplateList, ArrayList<Template> serviceTemplateList, ArrayList<Template> moduleTemplateList) {
		orig.initialize(configInfo, edsourceTemplateList, essourceTemplateList, serviceTemplateList, moduleTemplateList);
	}

	public EDSourceInstance insertEDSource(String templateName) {
		return orig.insertEDSource(templateName);
	}

	public ESSourceInstance insertESSource(int i, String templateName, String instanceName) {
		return orig.insertESSource(i, templateName, instanceName);
	}

	public ModuleInstance insertModule(String templateName, String instanceName) {
		return orig.insertModule(templateName, instanceName);
	}

	public ModuleReference insertModuleReference(ReferenceContainer container, int i, ModuleInstance instance) {
		return orig.insertModuleReference(container, i, instance);
	}

	public ModuleReference insertModuleReference(ReferenceContainer container, int i, String templateName, String instanceName) {
		return orig.insertModuleReference(container, i, templateName, instanceName);
	}

	public Path insertPath(int i, String pathName) {
		return orig.insertPath(i, pathName);
	}

	public PathReference insertPathReference(Path parentPath, int i, Path path) {
		return orig.insertPathReference(parentPath, i, path);
	}

	public void insertPSet(PSetParameter pset) {
		orig.insertPSet(pset);
	}

	public Sequence insertSequence(int i, String sequenceName) {
		return orig.insertSequence(i, sequenceName);
	}

	public SequenceReference insertSequenceReference(Path parentPath, int i, Sequence sequence) {
		return orig.insertSequenceReference(parentPath, i, sequence);
	}

	public ServiceInstance insertService(int i, String templateName) {
		return orig.insertService(i, templateName);
	}

	public boolean isEmpty() {
		return orig.isEmpty();
	}

	public ModuleInstance module(int i) {
		return orig.module(i);
	}

	public int moduleCount() {
		return orig.moduleCount();
	}

	public ModuleTemplate moduleTemplate(String templateName) {
		return orig.moduleTemplate(templateName);
	}

	public String name() {
		return orig.name();
	}

	public int nextVersion() {
		return orig.nextVersion();
	}

	public Directory parentDir() {
		return orig.parentDir();
	}

	public int parentDirId() {
		return orig.parentDirId();
	}

	public Path path(int i) {
		return orig.path(i);
	}

	public int pathCount() {
		return orig.pathCount();
	}

	public int pathSequenceNb(Path path) {
		return orig.pathSequenceNb(path);
	}

	public PSetParameter pset(int i) {
		return orig.pset(i);
	}

	public int psetCount() {
		return orig.psetCount();
	}

	public String releaseTag() {
		return orig.releaseTag();
	}

	public void removeEDSource(EDSourceInstance edsource) {
		orig.removeEDSource(edsource);
	}

	public void removeESSource(ESSourceInstance essource) {
		orig.removeESSource(essource);
	}

	public void removeModuleReference(ModuleReference module) {
		orig.removeModuleReference(module);
	}

	public void removePath(Path path) {
		orig.removePath(path);
	}

	public void removePSet(PSetParameter pset) {
		orig.removePSet(pset);
	}

	public void removeSequence(Sequence sequence) {
		orig.removeSequence(sequence);
	}

	public void removeService(ServiceInstance service) {
		orig.removeService(service);
	}

	public void reset() {
		orig.reset();
	}

	public Sequence sequence(int i) {
		return orig.sequence(i);
	}

	public int sequenceCount() {
		return orig.sequenceCount();
	}

	public ServiceInstance service(int i) {
		return orig.service(i);
	}

	public int serviceCount() {
		return orig.serviceCount();
	}

	public void setConfigInfo(ConfigInfo configInfo) {
		orig.setConfigInfo(configInfo);
	}

	public void setHasChanged(boolean hasChanged) {
		orig.setHasChanged(hasChanged);
	}

	public String toString() {
		return orig.toString();
	}

	public int unsetTrackedEDSourceParameterCount() {
		return orig.unsetTrackedEDSourceParameterCount();
	}

	public int unsetTrackedESSourceParameterCount() {
		return orig.unsetTrackedESSourceParameterCount();
	}

	public int unsetTrackedModuleParameterCount() {
		return orig.unsetTrackedModuleParameterCount();
	}

	public int unsetTrackedParameterCount() {
		return orig.unsetTrackedParameterCount();
	}

	public int unsetTrackedPSetParameterCount() {
		return orig.unsetTrackedPSetParameterCount();
	}

	public int unsetTrackedServiceParameterCount() {
		return orig.unsetTrackedServiceParameterCount();
	}

	public void updateHashMaps(ArrayList<Template> edsourceTemplateList, ArrayList<Template> essourceTemplateList, ArrayList<Template> serviceTemplateList, ArrayList<Template> moduleTemplateList) {
		orig.updateHashMaps(edsourceTemplateList, essourceTemplateList, serviceTemplateList, moduleTemplateList);
	}

	public int version() {
		return orig.version();
	}
	
	
	
	
}

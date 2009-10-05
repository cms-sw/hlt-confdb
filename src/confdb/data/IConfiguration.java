package confdb.data;

import java.util.Iterator;


/**
 * IConfiguration
 * --------------
 * @author Philipp Schieferdecker
 *
 * Configuration Interface.
 */
public interface IConfiguration
{
    /** name of the configuration */
    public String name();

    /** parent directory of the configuration */
    public Directory parentDir();

    /** version of the configuration */
    public int version();
    
    /** get configuration date of creation as a string */
    public String created();
	
    /** get configuration creator */
    public String creator();
    
    /** release tag */
    public String releaseTag();

    /** process name */
    public String processName();

    /** comment */
    public String comment();
    

    /** has the configuration changed w.r.t. the last version in the DB? */
    public boolean hasChanged();
    
    /** indicate that the configuration has changed in memory */
    public void setHasChanged(boolean hasChanged);
    

    /** total number of components of a certain type */
    public int componentCount(Class<?> c);
    
    /** check if the configuration is empty */
    public boolean isEmpty();

    /** check if the provided string is already in use as a label */
    public boolean isUniqueQualifier(String qualifier);

    
    /** total number of unset tracked parameters */
    public int unsetTrackedParameterCount();

    /** number of unsert tracked global pset parameters */
    public int unsetTrackedPSetParameterCount();
    
    /** number of unsert tracked edsource parameters */
    public int unsetTrackedEDSourceParameterCount();

    /** number of unsert tracked essource parameters */
    public int unsetTrackedESSourceParameterCount();

    /** number of unsert tracked esmodule parameters */
    public int unsetTrackedESModuleParameterCount();

    /** number of unsert tracked service parameters */
    public int unsetTrackedServiceParameterCount();

    /** number of unsert tracked module parameters */
    public int unsetTrackedModuleParameterCount();

    /** number of paths unassigned to any stream */
    public int pathNotAssignedToStreamCount();
    
    
    /** get instance by label, regardless of type */
    public Instance instance(String label);


    /**  number of global PSets */
    public int psetCount();

    /** get i-th global PSet */
    public PSetParameter pset(int i);
    
    /** get pset by label */
    public PSetParameter pset(String name);
    
    /** index of a certain global PSet */
    public int indexOfPSet(PSetParameter pset);

    /** retrieve pset iterator */
    public Iterator<PSetParameter> psetIterator();
    
    /** insert a global pset */
    public void insertPSet(PSetParameter pset);
    
    /**  number of EDSources */
    public int edsourceCount();

    /** get i-th EDSource */
    public EDSourceInstance edsource(int i);

    /** get EDSource by label */
    public EDSourceInstance edsource(String name);
    
    /** index of a certain EDSource */
    public int indexOfEDSource(EDSourceInstance edsource);
	
    /** retrieve edsource iterator */
    public Iterator<EDSourceInstance> edsourceIterator();

    
    /**  number of ESSources */
    public int essourceCount();
    
    /** get i-th ESSource */
    public ESSourceInstance essource(int i);

    /** get ESSource by label */
    public ESSourceInstance essource(String name);

    /** index of a certain ESSource */
    public int indexOfESSource(ESSourceInstance essource);

    /** retrieve essource iterator */
    public Iterator<ESSourceInstance> essourceIterator();
    
    
    /**  number of ESModules */
    public int esmoduleCount();
    
    /** get i-th ESModule */
    public ESModuleInstance esmodule(int i);

    /** get ESModule by label */
    public ESModuleInstance esmodule(String name);
    
    /** index of a certain ESSource */
    public int indexOfESModule(ESModuleInstance esmodule);
    
    /** retrieve esmodule iterator */
    public Iterator<ESModuleInstance> esmoduleIterator();

    
    /**  number of Services */
    public int serviceCount();

    /** get i-th Service */
    public ServiceInstance service(int i);

    /** get Service by label */
    public ServiceInstance service(String name);
    
    /** index of a certain Service */
    public int indexOfService(ServiceInstance service);
    
    /** retrieve service iterator */
    public Iterator<ServiceInstance> serviceIterator();
    
    
    /**  number of Modules */
    public int moduleCount();

    /** get i-th Module */
    public ModuleInstance module(int i);
    
    /** get module by name (label) */
    public ModuleInstance module(String moduleName);
    
    /** index of a certain Module */
    public int indexOfModule(ModuleInstance module);
    
    /** retrieve module iterator */
    public Iterator<ModuleInstance> moduleIterator();
    

    /** number of Paths */
    public int pathCount();
    
    /** get i-th Path */
    public Path path(int i);

    /** get path by name */
    public Path path(String pathName);

    /** index of a certain Path */
    public int indexOfPath(Path path);
    
    /** retrieve path iterator */
    public Iterator<Path> pathIterator();

    
    /** number of Sequences */
    public int sequenceCount();
    
    /** get i-th Sequence */
    public Sequence sequence(int i);

    /** get sequence by name */
    public Sequence sequence(String sequenceName);
    
    /** index of a certain Sequence */
    public int indexOfSequence(Sequence sequence);

    /** retrieve sequence iterator */
    public Iterator<Sequence> sequenceIterator();
    
    /** retrieve sequence iterator */
    public Iterator<Sequence> orderedSequenceIterator();
    
    
    /** number of event contents */
    public int contentCount();
    
    /** retrieve i-th event contents */
    public EventContent content(int i);
    
    /** retrieve event content by label */
    public EventContent content(String eventContentLabel);
    
    /** index of a certain event content */
    public int indexOfContent(EventContent eventContent);
    
    /** retrieve event content iterator */
    public Iterator<EventContent> contentIterator();
    
    
    /** number of streams */
    public int streamCount();
    
    /** retrieve i-th stream */
    public Stream stream(int i);
    
    /** retrieve stream by label */
    public Stream stream(String streamLabel);
    
    /** index of a certain stream */
    public int indexOfStream(Stream stream);
    
    /** retrieve stream iterator */
    public Iterator<Stream> streamIterator();
    

    /** number of primary datasets */
    public int datasetCount();
    
    /** retrieve i-th primary dataset */
    public PrimaryDataset dataset(int i);
    
    /** retrieve primary dataset by label */
    public PrimaryDataset dataset(String datasetLabel);
    
    /** index of a certain primary dataset */
    public int indexOfDataset(PrimaryDataset dataset);
    
    /** retrieve primary dataset iterator */
    public Iterator<PrimaryDataset> datasetIterator();


    /** retrieve block iterator */
    public Iterator<Block> blockIterator();
    


}

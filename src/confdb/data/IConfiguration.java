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
    

    /**  number of global PSets */
    public int psetCount();

    /** get i-th global PSet */
    public PSetParameter pset(int i);
    
    /** index of a certain global PSet */
    public int indexOfPSet(PSetParameter pset);

    /** retrieve pset iterator */
    public Iterator psetIterator();
    
    
    /**  number of EDSources */
    public int edsourceCount();

    /** get i-th EDSource */
    public EDSourceInstance edsource(int i);

    /** index of a certain EDSource */
    public int indexOfEDSource(EDSourceInstance edsource);
	
    /** retrieve edsource iterator */
    public Iterator edsourceIterator();

    
    /**  number of ESSources */
    public int essourceCount();
    
    /** get i-th ESSource */
    public ESSourceInstance essource(int i);

    /** index of a certain ESSource */
    public int indexOfESSource(ESSourceInstance essource);

    /** retrieve essource iterator */
    public Iterator essourceIterator();
    
    
    /**  number of ESModules */
    public int esmoduleCount();
    
    /** get i-th ESModule */
    public ESModuleInstance esmodule(int i);

    /** index of a certain ESSource */
    public int indexOfESModule(ESModuleInstance esmodule);
    
    /** retrieve esmodule iterator */
    public Iterator esmoduleIterator();

    
    /**  number of Services */
    public int serviceCount();

    /** get i-th Service */
    public ServiceInstance service(int i);

    /** index of a certain Service */
    public int indexOfService(ServiceInstance service);
    
    /** retrieve service iterator */
    public Iterator serviceIterator();
    
    
    /**  number of Modules */
    public int moduleCount();

    /** get i-th Module */
    public ModuleInstance module(int i);
    
    /** get module by name (label) */
    public ModuleInstance module(String moduleName);
    
    /** index of a certain Module */
    public int indexOfModule(ModuleInstance module);
    
    /** retrieve module iterator */
    public Iterator moduleIterator();
    

    /** number of Paths */
    public int pathCount();
    
    /** get i-th Path */
    public Path path(int i);

    /** get path by name */
    public Path path(String pathName);

    /** index of a certain Path */
    public int indexOfPath(Path path);
    
    /** retrieve path iterator */
    public Iterator pathIterator();

    
    /** number of Sequences */
    public int sequenceCount();
    
    /** get i-th Sequence */
    public Sequence sequence(int i);

    /** get sequence by name */
    public Sequence sequence(String sequenceName);
    
    /** index of a certain Sequence */
    public int indexOfSequence(Sequence sequence);

    /** retrieve sequence iterator */
    public Iterator sequenceIterator();
    
    
    /** number of streams */
    public int streamCount();
    
    /** retrieve i-th stream */
    public Stream stream(int i);
    
    /** index of a certain stream */
    public int indexOfStream(Stream stream);
    
    /** retrieve stream iterator */
    public Iterator streamIterator();
    
    
}

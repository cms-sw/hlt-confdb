package confdb.data;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * PrimaryDataset
 * --------------
 * @author Philipp Schieferdecker
 * updated by Sam Harper
 *
 * PrimaryDatasets are a set (!) of paths assigned with the parent
 * stream. Each path can only be assigned to one of the PDs assigned
 * with a single stream.
 * 
 * A PrimaryDataset has exactly one parent stream.
 * 
 * Primary dataset definations have now been reworked such that they can be defined by a 
 * path containing a TriggerResults filter which applies the path selection for the dataset
 * 
 * This is known as a DatasetPath and contains a begin sequence shared amounst all the dataset paths 
 * which allows us to add things like L1 digis which we dont know apriori, a prescale module and a
 * TriggerResults filter which selects the actual paths of the dataset
 * 
 * A DatasetPath is unique to a dataset but its trigger results filter aka PathFilter can be shared
 * between multiple datasets. This is useful for splitting datasets or cloning them for different 
 * event contents. A dataset is considered split if other datasets share a the PathFilter but are 
 * in the same event content. The only situation this makes sense is each dataset selects a subset 
 * of the total events selected by their paths. 
 *  
 * As Streams are now simply the combination of their datasets paths, the PrimaryDataset
 * is exclusively responsible for adding/removing Paths to their event content
 * 
 * Old style datasets are still supported, if the dataset path doesnt exist, then its an old style
 * dataset and behaves as it did
 * 
 * In the update of this, its not clear to me about the rules for hasChanged()
 * In theory a DatasetPath Dataset is no longer "changing" when a path is added/rmed 
 * from a db perspective, only the dataset Path is changing. 
 * However for some reason its triggered as "changed" whenever any 
 * path that belongs it changes. Need to understand why that is the case and whether
 * we can update what defines a "change"
 *   
 * note: we found a bug where if a path is not a member of a stream but is checked in the hasChanged
 * of a dataset (ie the DatasetPath), it'll cause the dataset to reset its datasetId itself if
 * hasChanged is called. This is bad as the this  can happen when the dataset is already inserted
 * it is still mystifying to me why a dataset changes if its path changes, surely that should only
 * happen if its name is changed as nothing else is stored as part of the dataset 
 * long story short: the DatasetPath is excluded from hasChanged as it doesnt change the dataset
 * 
 * so I know understand the hasChanged requirements
 * the path /stream/ dataset association is entered only if the stream & dataset hasChanged
 * this is a way of preventing the same info being written multiple times
 * however this requires that if a path changes, we need to change the dataset to allow this to be
 * written, hence the hasChange if a path changes
 * what I dont understand is why we just dont check if the path has changed and if so add that
 * I guess this way only paths in the config associate to that datasets id and one
 * doesnt need to check the path is actually in the config
 * 
 */
public class PrimaryDataset extends DatabaseEntry
                            implements Comparable<PrimaryDataset>
{
    //
    // member data
    //
    
    /** name of the stream */ 
    private String name;
    
    /** collection of assigned paths */
    private ArrayList<Path> paths = new ArrayList<Path>();
    
    /** parent stream */
    private Stream parentStream = null;
    
    /** path representing the dataset **/
    private Path datasetPath = null;

    /** the wrapper class managing the filter module selecting dataset's paths **/
    private PathFilter pathFilter = null;

    /** tells us if paths have been added/rm since the list time it was set
     * cant used hasChanged here as 1) its triggered if the path itself hasChanged
     * and 2) it can reset the dataset database id
     * this is mainly for streams to poll their child datasets to see if they have added any 
     * paths or not 
     */
    private boolean hasPathListChanged = false;


    //
    // construction
    //
    
    /** standard constructor */
    public PrimaryDataset(String name,Stream parentStream)
    {
	this.name         = name.replaceAll("\\W", "");
	this.parentStream = parentStream;        
    }
    
    
    //
    // member functions
    //
    
    /** name of this stream */
    public String name() {
   
	return name; 
    }

   public boolean hasChanged(){    
	for (Path p : paths){
	    if(p.hasChanged()){
		setHasChanged();        
		break;
	    }
	}
    if(datasetPath!=null){
        if(datasetPath.hasChanged()){
            setHasChanged();
        }
    }
    
	return super.hasChanged();
    }

    public void setHasChanged(){
        super.setHasChanged();
    }
    
    public boolean hasPathListChanged(){
        return hasPathListChanged;
    }

    public void setPathListChanged(boolean value){
        hasPathListChanged = value;
    }

    /** get the parent stream */
    public Stream parentStream() { return parentStream; }

    public Path datasetPath() { return datasetPath; }

    public PathFilter pathFilter() { return pathFilter; }

    /** set name of this stream */
    public void setName(String name) {
        String oldName = new String(this.name);
	    this.name = name.replaceAll("\\W", "");
	    setHasChanged();
	    for (Path p : paths) {
	        p.setHasChanged();
	    }
        if(this.datasetPath!=null){
            try{
                this.datasetPath.setNameAndPropagate(datasetPathName());                
                this.pathFilter.setNameAndPropagate(this.pathFilter.name().replace(oldName,name));
            } catch (DataException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    
    /** overload 'toString()' */
    public String toString() { return name(); }
    
    /** Comparable: compareTo() */
    public int compareTo(PrimaryDataset s)
    {
	return toString().compareTo(s.toString());
    }

    /** number of paths */
    public int pathCount() { return paths.size(); }

    /** retrieve i-th path */
    public Path path(int i)
    {
	Collections.sort(paths);
	return paths.get(i);
    }
    
    /** retrieve path by name */
    public Path path(String pathName)
    {
	for (Path p : paths) if (p.name().equals(pathName)) return p;
	return null;
    }
    
    /** retrieve iterator over paths */
    public Iterator<Path> pathIterator()
    {
	Collections.sort(paths);
	return paths.iterator();
    }
    
    /** retrieve path iterator (alphabetical order) */
    public Iterator<Path> orderedPathIterator()
    {
	ArrayList<Path> orderedPaths = new ArrayList<Path>(paths);
	Collections.sort(orderedPaths);
	return orderedPaths.iterator();
    }    
    
    /** index of a certain path */
    public int indexOfPath(Path path)
    {
	Collections.sort(paths);
	return paths.indexOf(path);
    }

    /** insert and associate a path with this stream */
    public boolean insertPath(Path path)
    {
	if (paths.indexOf(path)>=0) {
	    System.err.println("PrimaryDataset.insertPath() ERROR: "+
			       "path already associated!");
	    return false;
	}
	if(this.pathFilter!=null){
        this.pathFilter.addPath(path);
    }else{
        addPathToPathList(path);
    }
    
	return true;
    }
    
    /** remove a path from this dataset */
    public boolean removePath(Path path)
    {
	    int index = paths.indexOf(path);
	    if (index<0) return false;

        if(this.pathFilter!=null) {
            this.pathFilter.removePath(path);
        }else {
            removePathFromPathList(path);
        }
	    return true;
    }
    
    /** remove all paths */
    public void clear()
    {
        if(this.pathFilter!=null) {
            this.pathFilter.clearPaths();
        }else {
            clearPathList();
        }
    }
	
    public String datasetPathName()
    {
        return "Dataset_"+name();
    }
    public String pathFilterDefaultName()
    {
        return "hltDataset"+name();
    }

    public static String datasetPathBeginSequenceName()
    {
        return "HLTDatasetPathBeginSequence";
    }
    /** create the path representing the path including its modules **/ 
    public void createDatasetPath() {
        createDatasetPath(null);
    }

    /** create the path representing the path including its modules **/ 
    public void createDatasetPath(PathFilter existingPathFilter)
    {        
        Configuration cfg = (Configuration) parentStream.parentContent().config();
        this.datasetPath = cfg.path(datasetPathName());
        if(this.datasetPath==null) {
            this.datasetPath = cfg.insertPath(cfg.pathCount(),datasetPathName());
            this.datasetPath.setAsDatasetPath();
            Sequence beginSeq = cfg.sequence(datasetPathBeginSequenceName());
            if( beginSeq == null){
                beginSeq = cfg.insertSequence(cfg.sequenceCount(),datasetPathBeginSequenceName());
                //this is only to save time, if this module exists we add it
                //usually they want it, if not the user can adjust it later
                ModuleInstance l1Digis = cfg.module("hltGtStage2Digis");
                if(l1Digis!=null){
                    cfg.insertModuleReference(beginSeq,0,l1Digis);
                }
            }
            cfg.insertSequenceReference(this.datasetPath, 0, beginSeq);            
            cfg.insertModuleReference(this.datasetPath,1,"HLTPrescaler",Path.hltPrescalerLabel(this.datasetPath.name()));
            addPathFilter(cfg,existingPathFilter);
        }else{
            setPathFilter();
        }
    }

    public void setDatasetPath(Path path)
    {
        if(this.datasetPath==null){            
            this.datasetPath = path;
            setPathFilter();
        }else{
            System.err.println("Error dataset "+name()+" already has a dataset path "+this.datasetPath.name()+" therefore can not set it to "+path.name());
        }

    }

    /** gets all datasets sharing the same trigger results filter including this dataset */
    public ArrayList<PrimaryDataset> getSiblings() {
        ArrayList<PrimaryDataset> siblings = new ArrayList<PrimaryDataset>();
        if(this.pathFilter!=null){
            Configuration cfg = (Configuration) parentStream.parentContent().config();
            Iterator<PrimaryDataset> pdIt = cfg.datasetIterator();
            while(pdIt.hasNext()){
                PrimaryDataset dataset = pdIt.next();
                if(dataset.pathFilter()==this.pathFilter()){
                    siblings.add(dataset);
                }
            } 
        }else{
            siblings.add(this);
        }
        return siblings;
    }

    public ArrayList<StreamIndexPair > getSiblingsStreamsIndexOfPath(Path path){
        ArrayList<StreamIndexPair > streamsAndIndex = new ArrayList<StreamIndexPair>();
        ArrayList<PrimaryDataset> siblings = getSiblings();
        HashSet<Stream> uniqStreams = new HashSet<Stream>();
        for(PrimaryDataset dataset : siblings){
            Stream stream = dataset.parentStream();
            if(uniqStreams.add(stream)){
                streamsAndIndex.add(new StreamIndexPair(stream,stream.indexOfPath(path)));
            }            
        }
        return streamsAndIndex;
    }

    public ArrayList<ContentIndexPair > getSiblingsContentsIndexOfPath(Path path){
        ArrayList<ContentIndexPair > contentsAndIndex = new ArrayList<ContentIndexPair>();
        ArrayList<PrimaryDataset> siblings = getSiblings();
        HashSet<EventContent> uniqContent = new HashSet<EventContent>();
        for(PrimaryDataset dataset : siblings){
            EventContent content = dataset.parentStream().parentContent();
            if(uniqContent.add(content)){
                contentsAndIndex.add(new ContentIndexPair(content,content.indexOfPath(path)));
            }            
        }
        return contentsAndIndex;
    }


    
    private void addPathFilter(Configuration cfg,PathFilter existingPathFilter) {
        
        ModuleReference pathFilterRef =  cfg.insertModuleReference(this.datasetPath,this.datasetPath.entryCount(),"TriggerResultsFilter",existingPathFilter!=null ? existingPathFilter.name() : pathFilterDefaultName());

    
        ModuleInstance pathFilterMod = (ModuleInstance) pathFilterRef.parent(); 
        if(pathFilterMod.referenceCount()!=1){
            this.pathFilter = cfg.getDatasetPathFilter(pathFilterMod);
            this.paths = this.pathFilter.getPathList(cfg);            
        }else{
            this.pathFilter = new PathFilter(pathFilterMod,this.paths);                        
        }
        //this syncs this dataset path list to the path filter
        this.pathFilter.addDataset(this);        
    }

    /** sets the path filter using the dataset paths path filter, creating it if necessary
     * looks in the config to see if any datasets have this path filter and if so takes 
     * their PathFilter object otherwise makes one
    */
    private void setPathFilter()
    {
        Configuration cfg = (Configuration) parentStream.parentContent().config();
        ArrayList<ModuleInstance> trigFiltArray = this.datasetPath.moduleArray("TriggerResultsFilter");
        if(trigFiltArray.size()==0){
            System.err.println("Error, datasetPath "+this.datasetPath+" has no TriggerResultFilters when it should have exactly one, creating it");
            addPathFilter(cfg,null);
        }else{

            if(trigFiltArray.size()>1){
                System.err.println("Error, datasetPath "+this.datasetPath+" has "+trigFiltArray.size()+" TriggerResultFilters when it should have exactly one, taking first one");
            }
            this.pathFilter = cfg.getDatasetPathFilter(trigFiltArray.get(0));
            if(this.pathFilter==null){
                this.pathFilter = new PathFilter(trigFiltArray.get(0),null);
            }
            this.pathFilter.addDataset(this);
            
        }

    }


    /** function which adds directly to the path list
     * called by the PathFilter and it is assumed that all checks on whether to add
     * this path have been done
     * also the dataset is now responseible for making sure the path knows its event content
     */
    private void addPathToPathList(Path path) {
        paths.add(path);
        //maybe we should move this logic to the PathFilter, slightly inefficient here
        //as it gets called for each sibbling dataset
        path.addToContent(parentStream().parentContent());
        Collections.sort(paths);
        setHasChanged();
        setPathListChanged(true);        
    }
    
    /** a slightly more efficient version of addPathToPathList for adding all paths in one go
     */
    private void addPathsToPathList(ArrayList<Path> paths) {
        for(Path path: paths){
            this.paths.add(path);
            //maybe we should move this logic to the PathFilter, slightly inefficient here
            //as it gets called for each sibbling dataset
            path.addToContent(parentStream().parentContent());
        }
        Collections.sort(this.paths);
        setHasChanged();
        setPathListChanged(true);        
    }
    /** function which removes directly form the path list
     * called by the PathFilter and it is assumed that all checks on whether to add
     * this path have been done
     * also the dataset is now responseible for making sure the path knows its event content
     */
    private void removePathFromPathList(Path path) {
        int index = paths.indexOf(path);
        paths.remove(index);
        setHasChanged();
        setPathListChanged(true);
        parentStream.setHasChanged();
        removeFromContent(path);
    }

    /** function which removes directly form the path list
    * called by the PathFilter and it is assumed that all checks on whether to add
    * this path have been done
    */
    private void clearPathList()
    {
        ArrayList<Path> oldPaths = new ArrayList<Path>(paths);
	    paths.clear();        
	    setHasChanged();
        setPathListChanged(true);
        parentStream.setHasChanged();
        for(Path path : oldPaths){
            removeFromContent(path);
        }
    }

     /** if the path nolonger exists in the event content, remove it
     * needs to be called after path has been removed from the dataset
     * and the dataset has registered its changed
     * slow function, could be much faster, also probably should be in PathFilter...
     */ 
    private void removeFromContent(Path path){
        if(this.parentStream().parentContent().path(path.name())==null){
            path.removeFromContent(this.parentStream().parentContent());
        }
    }


    interface DatasetAction {
        void action(PrimaryDataset d);
    }
    /** a small class to manage the path filter
     * which is shared between datasets
     */
    class PathFilter {
        /** filter module selecting dataset's paths **/
        private ModuleInstance pathFilter = null;

        /** the parameter of the filter module selecting paths **/
        /** note because we are maniputating this directly, we need to call **/
        /** the pathFilters "hasChanged" method whenever we do so **/
        private VStringParameter pathFilterParam = null;

        /** datasets which use this filter */
        private HashSet<PrimaryDataset> datasets = new HashSet<PrimaryDataset>(); 


        public String name(){
            if(this.pathFilter!=null){
                return this.pathFilter.name();
            }else{
                return "";
            }
        }

        PathFilter(ModuleInstance pathFilter,ArrayList<Path> paths){
            this.pathFilter = pathFilter;            
            InputTagParameter trigResultTag = new InputTagParameter("hltResults","","","", true);
            this.pathFilter.updateParameter(trigResultTag.name(),trigResultTag.type(),trigResultTag.valueAsString());
            if(this.pathFilter.template().findParameter("usePathStatus")!=null){
                BoolParameter usePathStatus= new BoolParameter("usePathStatus",true,true);
                this.pathFilter.updateParameter(usePathStatus.name(),usePathStatus.type(),usePathStatus.valueAsString());
            }

            InputTagParameter l1ResultTag = new InputTagParameter("l1tResults","","","", true);
            this.pathFilter.updateParameter(l1ResultTag.name(),l1ResultTag.type(),l1ResultTag.valueAsString());
            
            this.pathFilterParam = (VStringParameter) this.pathFilter.findParameter("triggerConditions");
            if(this.pathFilterParam==null){
                System.err.println("error dataset's path filter "+this.pathFilter.name()+" has no trigger conditions parameter, this should not be possible");
            }else if(paths!=null){
                clearPaths();
                for(Path path : paths){
                    addPath(path);
                }
            }            
        }


        public void setNameAndPropagate(String name) throws DataException {
            if(this.pathFilter!=null){
                this.pathFilter.setNameAndPropagate(name);
            }
        }

        public boolean sameFilter(ModuleInstance rhs){
            return this.pathFilter == rhs;
        }

        /**
         * 
         */
        public ArrayList<Path> getPathList(Configuration cfg){
            ArrayList<Path> paths = new ArrayList<Path>();            
            for(String pathname : this.pathFilterParam.values()){
                paths.add(cfg.path(pathname.split(" / ")[0]));
            } 
            return paths;
        }

        public boolean addPath(Path path){            
            if(this.pathFilterParam!=null){
                this.pathFilterParam.addValue(path.name());
                this.pathFilter.setHasChanged();
                informDatasetsOfChange((PrimaryDataset d) -> d.addPathToPathList(path));
                return true;
            }else{
                return false;
            }
        }
    
        public boolean removePath(Path path){            
            if(this.pathFilterParam==null){
                return false;
            }else{
                if(this.pathFilterParam.values().removeIf(x -> (x.split(" / ")[0].equals(path.name())))){
                    this.pathFilter.setHasChanged();
                    informDatasetsOfChange((PrimaryDataset d) -> d.removePathFromPathList(path));
                    return true;
                }else{
                    return false;
                }
            }
        }
    
        public boolean clearPaths(){
            if(this.pathFilterParam==null){
                return false;
            }else{
                this.pathFilterParam.values().clear();
                this.pathFilter.setHasChanged();
                informDatasetsOfChange((PrimaryDataset d) -> d.clearPathList());
                return true;
            }
        }


        public boolean addDataset(PrimaryDataset dataset){
            if(this.datasets.add(dataset)){
                dataset.clearPathList();
                ArrayList<Path> paths = getPathList((Configuration) dataset.parentStream().parentContent().config());
                dataset.addPathsToPathList(paths);
                return true;                
            }else{
                return false;
            }
        }

        public boolean removeDataset(PrimaryDataset dataset){
            return this.datasets.remove(dataset);
        }

        private void informDatasetsOfChange(DatasetAction datasetAction){
            for(PrimaryDataset dataset : datasets){                
                datasetAction.action(dataset);
            }
        }
    

    }

    public class StreamIndexPair {
        public Stream stream = null;
        public int index = -1;
        public StreamIndexPair(Stream stream,int index){
            this.stream = stream;
            this.index = index;
        }
    }

    public class ContentIndexPair {
        public EventContent content = null;
        public int index = -1;

        public ContentIndexPair(EventContent content, int index){
            this.content = content;
            this.index = index;
        }


    }
}



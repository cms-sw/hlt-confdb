package confdb.data;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Path
 * ----
 * @author Philipp Schieferdecker
 *
 * CMSSW framework path.
 */
public class Path extends ReferenceContainer {
	//
	// member data
	//
	public enum Type {
		STD(0),
		END(1),
		FINAL(2),
		DATASET(3);

		public final int value;

		private Type(int value){
			this.value = value;
		} 

	} 

	/** collection of event contents this path is associated with */
	private ArrayList<EventContent> contents = new ArrayList<EventContent>();

	private Type pathType = Type.STD; 
	
	/** field indicating a short description of a particular path */
	private String description = "";
	/**
	 * field indicating emails addresses of the people responsible of a particular
	 * path
	 */
	private String contacts = "";

	//
	// construction
	//

	/** standard constructor */
	public Path(String name) {
		super(name);
	}

	//
	// member functions
	//

	/** set the name of the path; check 'SelectEvents' of OutputModules */
	/*
	 * public void setName(String name) throws DataException { String oldName =
	 * name(); super.setName(name); if (config!=null) { Iterator<ModuleInstance> itM
	 * = config.moduleIterator(); while (itM.hasNext()) { ModuleInstance module =
	 * itM.next(); if (!module.template().type().equals("OutputModule")) continue;
	 * Parameter[] tmp=module.findParameters("SelectEvents::SelectEvents"); if
	 * (tmp.length!=1) continue; VStringParameter selEvts =
	 * (VStringParameter)tmp[0]; for (int i=0;i<selEvts.vectorSize();i++) { String
	 * iAsString = (String)selEvts.value(i); if (iAsString.equals(oldName)) {
	 * selEvts.setValue(i,name); module.setHasChanged(); } } } } }
	 */

	public void setFields(Path src) {
		setDescription(src.getDescription());
		setContacts(src.getContacts());
		this.pathType = src.pathType;
	}

	public String getDescription() {
		return this.description;
	}

	public String getContacts() {
		return this.contacts;
	}

	public void setDescription(String Desc) {
		// System.err.println("XX1: "+(this.description==null)+(Desc==null));
		if (Desc == null)
			return;
		if (this.description.equals(Desc))
			return;
		this.description = Desc;
		setHasChanged();
	}

	public void setContacts(String Cont) {
		if (Cont == null)
			return;
		if (this.contacts.equals(Cont))
			return;
		this.contacts = Cont;
		setHasChanged();
	}

	public Type pathType() {
		return this.pathType;
	}

	public boolean isEndPath() {
		return this.pathType  == Type.END;
	}

	public boolean isFinalPath() {
		return this.pathType == Type.FINAL;
	}

	public boolean isDatasetPath(){
		return this.pathType == Type.DATASET;
	}

	public boolean isStdPath(){
		return this.pathType == Type.STD;
	}

	/** set this path to be an endpath */
	public boolean setAsEndPath(){
		return setType(Type.END);
	}
	
	public boolean setAsFinalPath(){
		return setType(Type.FINAL);
	}

	public boolean setAsStdPath() {
		return setType(Type.STD);
	}

	public boolean setAsDatasetPath() {
		return setType(Type.DATASET);
	}
	

	public boolean setType(Type type) {
		if(this.pathType == type){
			return true;
		}else{
			this.pathType = type;
			setHasChanged();
			return true;
		}

	}

	/**
	 * a path is a valid output path of a stream if
	 * 1) it is a final path
	 * 2) it contains exactly one entry
	 * 3) that entry is that streams output module
	 * if no stream is specified then it just looks for one output module to be present
	 */
	public boolean isOutputPathOfStream(Stream stream){
		if(isFinalPath() && entryCount()==1){			
			Iterator<OutputModule> outputIt = outputIterator();
			if(outputIt.hasNext() && (stream==null || outputIt.next()==stream.outputModule())){
				return true;
			}
		}
		return false;
	}

	/** insert a path entry */
	public void insertEntry(int i, Reference reference) {
		if (!entries.contains(reference)) {
			entries.add(i, reference);
			setHasChanged();
		} else
			System.err.println("Path.insertEntry FAILED.");
	}

	/** check if path contains a specific entry */
	public boolean containsEntry(Reference reference) {
		Referencable parent = reference.parent();
		Iterator<Reference> it = entries.iterator();
		while (it.hasNext()) {
			Reference r = it.next();
			if (parent.isReferencedBy(r))
				return true;
			if (r.parent() instanceof ReferenceContainer) {
				ReferenceContainer container = (ReferenceContainer) r.parent();
				if (container.containsEntry(reference))
					return true;
			}
		}
		return false;
	}

	/** create a reference of this in a reference container (path/sequence) */
	public Reference createReference(ReferenceContainer container, int i) {
		PathReference reference = new PathReference(container, this);
		references.add(reference);
		container.insertEntry(i, reference);
		container.setHasChanged();
		return reference;
	}

	/** number of event contents this path is associated with */
	public int contentCount() {
		return contents.size();
	}

	/** retrieve i-th event content */
	public EventContent content(int i) {
		return contents.get(i);
	}

	/** retrieve event content iterator */
	public Iterator<EventContent> contentIterator() {
		return contents.iterator();
	}

	/** add this path to an event content */
	public boolean addToContent(EventContent content) {
		if (contents.indexOf(content) >= 0)
			return false;
		contents.add(content);
		// DON'T CALL setHasChanged()!?!
		return true;
	}

	/** remove this path from an event content */
	public boolean removeFromContent(EventContent content) {
		int index = contents.indexOf(content);
		if (index < 0)
			return false;
		contents.remove(index);
		// DON'T CALL setHasChanged()!?!
		return true;
	}

	/** number of streams this path is assiged to */
	public int streamCount() {
		return streams().size();
	}

	/** retrieve stream iterator */
	public Iterator<Stream> streamIterator() {
		return streams().iterator();
	}

	/** number of datasets this path is assigned to */
	public int datasetCount() {
		return datasets().size();
	}

	/** retrieve dataset iterator */
	public Iterator<PrimaryDataset> datasetIterator() {
		return datasets().iterator();
	}

	//
	// private member functions
	//

	/** retrieve a list of streams this path is associated with */
	private ArrayList<Stream> streams() {
		ArrayList<Stream> result = new ArrayList<Stream>();
		Iterator<EventContent> itC = contentIterator();
		while (itC.hasNext()) {
			Iterator<Stream> itS = itC.next().streamIterator();
			while (itS.hasNext()) {
				Stream stream = itS.next();
				if (stream.indexOfPath(this) >= 0)
					result.add(stream);
			}
		}
		return result;
	}

	/** retrieve list of primary datasets this path is associated with */
	public ArrayList<PrimaryDataset> datasets() {
		ArrayList<PrimaryDataset> result = new ArrayList<PrimaryDataset>();
		Iterator<Stream> itS = streams().iterator();
		while (itS.hasNext()) {
			Iterator<PrimaryDataset> itPD = itS.next().datasetIterator();
			while (itPD.hasNext()) {
				PrimaryDataset dataset = itPD.next();
				if (dataset.indexOfPath(this) >= 0)
					result.add(dataset);
			}
		}
		return result;
	}

	static public String hltPrescalerLabel(String name) {
		int first = 0;
		if (name.startsWith("HLT_"))
			first = 4;
		int last = name.lastIndexOf("_v");
		if (last == -1) {
			last = name.length();
		}
		return "hltPre" + name.substring(first, last).replace("_", "");
	}

	static public String rmVersion(String name){
		return name.replaceAll("_v[0-9]+$","");
	}

	/** set the name and propagate it to all relevant modules */
	public void setNameAndPropagate(String name) throws DataException {
		String oldName = name();
		if (oldName.equals(name))
			return;
		super.setName(name);

		/* propagate path name change to HLTPrescaler instance in path */
		String newLabel = hltPrescalerLabel(name);
		for (Reference r : entries) {
			Referencable parent = r.parent();
			if (parent instanceof ModuleInstance) {
				ModuleInstance module = (ModuleInstance) parent;
				if (module.template().toString().equals("HLTPrescaler")) {
					if (!(newLabel.equals(module.name()))) {
						module.setNameAndPropagate(newLabel);
						module.setHasChanged();
					}
				}
			}
		}

		/* propagate path name change to all TriggerResultsFilter instances */
		Iterator<ModuleInstance> itM = config().moduleIterator();
		while (itM.hasNext()) {
			ModuleInstance module = itM.next();
			if (module.template().toString().equals("TriggerResultsFilter")) {
				VStringParameter vStr = (VStringParameter) module.parameter("triggerConditions", "vstring");
				int n = 0;
				for (int i = 0; i < vStr.vectorSize(); i++) {
					String str = (String) vStr.value(i);
					String upd = SmartPrescaleTable.rename(str, oldName, name);
					if (!str.equals(upd)) {
						n++;
						vStr.setValue(i, upd);
					}
				}
				if (n > 0)
					module.setHasChanged();
			}

		}

		/* propagate path name change to PrescaleService */
		ServiceInstance prescaleSvc = config().service("PrescaleService");
		if (prescaleSvc == null)
			return;
		VPSetParameter vpsetPrescaleTable = (VPSetParameter) prescaleSvc.parameter("prescaleTable", "VPSet");
		if (vpsetPrescaleTable == null)
			return;
		int n = 0;
		for (int i = 0; i < vpsetPrescaleTable.parameterSetCount(); i++) {
			PSetParameter pset = vpsetPrescaleTable.parameterSet(i);
			StringParameter Str = (StringParameter) pset.parameter("pathName");
			String str = (String) Str.value();
			if (str.equals(oldName)) {
				n++;
				Str.setValue(name);
			}
		}
		if (n > 0)
			prescaleSvc.setHasChanged();

	}

}

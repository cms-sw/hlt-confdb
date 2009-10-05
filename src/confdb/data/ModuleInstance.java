package confdb.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


/**
 * ModuleInstance
 * --------------
 * @author Philipp Schieferdecker
 *
 * CMSSW framework module instance.
 */
public class ModuleInstance extends Instance implements Referencable
{
    //
    // data members
    //
    
    /** database ID */
    private int databaseId = 0;
    
    /** list of references */
    private ArrayList<ModuleReference> references =
	new ArrayList<ModuleReference>();
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ModuleInstance(String name,ModuleTemplate template)
	throws DataException
    {
	super(name,template);
    }
    
    
    //
    // member functions
    //

    /** create a reference of this instance */
    public Reference createReference(ReferenceContainer container,int i)
    {
	ModuleReference reference = new ModuleReference(container,this);
	references.add(reference);
	container.insertEntry(i,reference);
	return reference;
    }
    
    /** number of references */
    public int referenceCount() { return references.size(); }
    
    /** retrieve the i-th reference */
    public Reference reference(int i) { return references.get(i); }

    /** test if a specifc reference refers to this entity */
    public boolean isReferencedBy(Reference reference) 
    {
	return references.contains(reference);
    }
    
    /** remove a reference of this instance */
    public void removeReference(Reference reference)
    {
	int index = references.indexOf(reference);
	references.remove(index);
	if (referenceCount()==0) remove();
    }
    
    /** get list of parent paths */
    public Path[] parentPaths()
    {
	ArrayList<Path> list = new ArrayList<Path>();
	for (int i=0;i<referenceCount();i++) {
	    Path[] paths = reference(i).parentPaths();
	    for (Path p : paths) list.add(p);
	}
	return list.toArray(new Path[list.size()]);
    }
    
    /** set the name and propagate it to all relevant downstreams InputTags */
    public void setNameAndPropagate(String name) throws DataException
    {
	String oldName = name();
	super.setName(name);

	Path[] paths = parentPaths();
	HashSet<Path> pathSet = new HashSet<Path>();
	for (Path path : paths) pathSet.add(path);
	if (config()!=null) {
	    Iterator<Path> itP = config().pathIterator();
	    while (itP.hasNext()) {
		Path path = itP.next();
		if (path.isEndPath()) {
		    pathSet.add(path);
		    Iterator<ModuleInstance> itM = path.moduleIterator();
		    while (itM.hasNext()) {
			ModuleInstance module = itM.next();
			if (!module.template().type().equals("OutputModule"))
			    continue;
			VStringParameter outCom = (VStringParameter)
			    module.parameter("outputCommands","vstring");
			if (outCom==null) continue;
			for (int i=0;i<outCom.vectorSize();i++) {
			    String a[]=((String)outCom.value(i)).split(" ");
			    if (!a[0].equals("keep")) continue;
			    String b[] = a[1].split("_");
			    if (b.length!=4) continue;
			    if (b[1].equals(oldName)) {
				outCom.setValue(i,"keep "+b[0]+"_"+name+
						"_"+b[2]+"_"+b[3]);
				module.setHasChanged();
			    }
			}
		    }
		}
	    }
	}

	Iterator<Path> itPath = pathSet.iterator();
	while (itPath.hasNext()) {
	    Path path = itPath.next();
	    boolean isDownstream = path.isEndPath();
	    Iterator<ModuleInstance> itM = path.moduleIterator();
	    while (itM.hasNext()) {
		ModuleInstance module = itM.next();
		if (!isDownstream) {
		    if (module==this) isDownstream = true;
		    continue;
		}
		Iterator<Parameter> itP = module.recursiveParameterIterator();
		while (itP.hasNext()) {
		    Parameter p = itP.next();
		    if (!p.isValueSet()) continue;
		    if (p instanceof InputTagParameter) {
			InputTagParameter inputTag = (InputTagParameter)p;
			if (inputTag.label().equals(oldName)) {
			    InputTagParameter tmp =
				(InputTagParameter)inputTag.clone(null);
			    tmp.setLabel(name());
			    module.updateParameter(inputTag.fullName(),
						   inputTag.type(),
						   tmp.valueAsString());
			}
		    }
		    else if (p instanceof VInputTagParameter) {
			VInputTagParameter vInputTag = (VInputTagParameter)p;
			VInputTagParameter tmp =
			    (VInputTagParameter)vInputTag.clone(null);
			for (int i=0;i<tmp.vectorSize();i++) {
			    InputTagParameter inputTag = 
				new InputTagParameter("",
						      tmp.value(i)
						      .toString(),
						      false,false);
			    if (inputTag.label().equals(oldName)) {
				inputTag.setLabel(name());
				tmp.setValue(i,inputTag.valueAsString());
			    }
			}
			module.updateParameter(vInputTag.fullName(),
					       vInputTag.type(),
					       tmp.valueAsString());
		    }
		}
	    }
	}
    }
    
}

package confdb.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;


/**
 * SoftwareSubsystem
 * -----------------
 * @author Philipp Schieferdecker
 *
 * A software subsystem contains one layer of software packages.
 */
public class SoftwareSubsystem implements Comparable<SoftwareSubsystem>, Serializable
{
    //
    // member data
    //
    
    /** name of the subsystem */
    private String name = null;

    /** list of packages */
    private ArrayList<SoftwarePackage> packages = new ArrayList<SoftwarePackage>();


    //
    // construction
    //

    /** standard constructor */
    public SoftwareSubsystem(String name)
    {
	this.name = name;
    }

    //
    // member functions
    //

    /** toString() */
    public String toString() { return name; }

    /** Comparable: compareTo() */
    public int compareTo(SoftwareSubsystem s) { return name().compareTo(s.name()); }
    
    /** get subsystem name */
    public String name() { return name; }

    /** get number of packages */
    public int packageCount() { return packages.size(); }

    /** get number of packages referenced by the current configuration */
    public int referencedPackageCount()
    {
	int result = 0;
	for (SoftwarePackage p : packages)
	    if (p.instantiatedTemplateCount()>0) result++;
	return result;
    }

    /** get i=th package */
    public SoftwarePackage getPackage(int i) { return packages.get(i); }

    /** get packkage iterator */
    public Iterator<SoftwarePackage> packageIterator() { return packages.iterator(); }

    /** get index of a certain package */
    public int indexOfPackage(SoftwarePackage p) { return packages.indexOf(p); }

    /** add a package */
    public void addPackage(SoftwarePackage p)
    {
	if (packages.indexOf(p)>=0) {
	    System.err.println("SoftwareSubsystem.addPackage ERROR: " +
			       name() + " contains " + p.name() +
			       " already!");
	    return;
	}
	p.setSubsystem(this);
	packages.add(p);
    }

    /** sort packages alphabetically */
    public void sortPackages() { Collections.sort(packages); }
}

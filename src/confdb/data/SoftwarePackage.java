package confdb.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
 * SoftwarePackage
 * ---------------
 * @author Philipp Schieferdecker
 *
 * A software package belongs to a software subsystem and contains a
 * list of software templates.
 */
public class SoftwarePackage implements Comparable<SoftwarePackage>, Serializable
{
    //
    // member data
    //
    
    /** name of the package */
    private String name = null;

    /** cvs tag of the package */
    private String cvsTag = null;
    
    /** the parent software subsystem */
    private SoftwareSubsystem subsystem = null;

    /** list of templates */
    private ArrayList<Template> templates = new ArrayList<Template>();
    

    //
    // construction
    //
    
    /** standard constructor */
    public SoftwarePackage(String name)
    {
	this.name = name;
    }

    
    //
    // member functions
    //
    
    /** toString() */
    public String toString() { return name; }
    
    /** Comparable: compareTo() */
    public int compareTo(SoftwarePackage p) { return name().compareTo(p.name()); }

    /** get the package name */
    public String name() { return name; }

    /** get the package cvs-tag*/
    public String cvsTag() { return cvsTag; }

    /** get the parent subsystem */
    public SoftwareSubsystem subsystem() { return subsystem; }
    
    /** get the number of templates */
    public int templateCount() { return templates.size(); }

    /** get the i-th template */
    public Template template(int i) { return templates.get(i); }

    /** get template iterator */
    public Iterator<Template> templateIterator() { return templates.iterator(); }

    /** get index of a certain template within the package */
    public int indexOfTemplate(Template t) { return templates.indexOf(t); }

    /** get number of referenced templates */
    public int instantiatedTemplateCount()
    {
	int result = 0;
	for (Template t : templates) if (t.instanceCount()>0) result++;
	return result;
    }
    
    
    /** set the parent software subsystem */
    public void setSubsystem(SoftwareSubsystem s) { this.subsystem = s; }
    
    /** add a template */
    public void addTemplate(Template t)
    {
	t.setParentPackage(this);
	if (templates.size()==0)
	    cvsTag = t.cvsTag();
	else if (!t.cvsTag().equals("V00-00-00")&&!t.cvsTag().equals(cvsTag()))
	    System.err.println("SoftwarePackage.addTemplate ERROR: " +
			       "cvsTag mismatch!\n package: "+name()+" / "+cvsTag() +
			       "\ntemplate: "+t.name()+" / "+t.cvsTag());
	// else
	templates.add(t);
    }

    /** sort templates */
    public void sortTemplates() { Collections.sort(templates); }
}

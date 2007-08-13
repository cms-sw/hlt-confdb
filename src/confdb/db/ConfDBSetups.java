package confdb.db;

import java.util.ArrayList;
import java.util.Properties;

import java.io.InputStream;
import java.io.IOException;


/**
 * ConfDBSetups
 * ------------
 * @author Philipp Schieferdecker
 *
 * Repository of standard database connections, initialized by a text
 * file (default: /conf/confdb.properties).
 */
public class ConfDBSetups
{
    //
    // member data
    //

    /** database setup labels */
    private ArrayList<String> labels = new ArrayList<String>();
    
    /** database setup db types */
    private ArrayList<String> types = new ArrayList<String>();
    
    /** database setup db hosts */
    private ArrayList<String> hosts = new ArrayList<String>();
    
    /** database setup db ports */
    private ArrayList<String> ports = new ArrayList<String>();
    
    /** database setup db names */
    private ArrayList<String> names = new ArrayList<String>();

    /** database setup db users */
    private ArrayList<String> users = new ArrayList<String>();
    

    //
    // construction
    //

    /** standard constructor */
    public ConfDBSetups()
    {
	initialize("/conf/confdb.properties");
    }

    /** contructor with another properties file */
    public ConfDBSetups(String fileName)
    {
	initialize(fileName);
	
    }


    //
    // member functions
    //
    
    /** number of setups */
    public int setupCount() { return labels.size(); }

    /** setup labels as array */
    public String[] labelsAsArray()
    {
	return labels.toArray(new String[labels.size()]);
    }
    
    /** retrieve i-th type */
    public String type(int i) { return types.get(i); }

    /** retrieve i-th host */
    public String host(int i) { return hosts.get(i); }

    /** retrieve i-th port */
    public String port(int i) { return ports.get(i); }

    /** retrieve i-th name */
    public String name(int i) { return names.get(i); }

    /** retrieve i-th user */
    public String user(int i) { return users.get(i); }

    /** retrieve type by label */
    public String type(String label)
    {
	int index = labels.indexOf(label);
	return (index>=0) ? types.get(index) : null;
    }

    /** retrieve host by label */
    public String host(String label)
    {
	int index = labels.indexOf(label);
	return (index>=0) ? hosts.get(index) : null;
    }

    /** retrieve port by label */
    public String port(String label)
    {
	int index = labels.indexOf(label);
	return (index>=0) ? ports.get(index) : null;
    }

    /** retrieve name by label */
    public String name(String label)
    {
	int index = labels.indexOf(label);
	return (index>=0) ? names.get(index) : null;
    }
    
    /** retrieve user by label */
    public String user(String label)
    {
	int index = labels.indexOf(label);
	return (index>=0) ? users.get(index) : null;
    }
    
    /** initialize from properties file */
    private void initialize(String fileName)
    {
	labels.add("");
	types.add("");
	ports.add("");
	names.add("");
	users.add("");
	hosts.add("");
	
	try {
	    InputStream inStream   =  getClass().getResourceAsStream(fileName);
	    Properties  properties = new Properties();
	    properties.load(inStream);
	    
	    String  property   = properties.getProperty("confdb.setupCount");
	    Integer setupCount = new Integer(property);
	    
	    for (int i=0;i<setupCount.intValue();i++) {
		property = properties.getProperty("confdb"+i+".dbSetup");
		labels.add(new String(property));
		property = properties.getProperty("confdb"+i+".dbType");
		types.add(new String(property));
		property = properties.getProperty("confdb"+i+".dbHost");
		hosts.add(new String(property));
		property = properties.getProperty("confdb"+i+".dbPort");
		ports.add(new String(property));
		property = properties.getProperty("confdb"+i+".dbName");
		names.add(new String(property));
		property = properties.getProperty("confdb"+i+".dbUser");
		users.add(new String(property));
	    }
	}
	catch (IOException e) {
	    System.out.println("Failed to initialize ConfDBSetups from '" +
			       fileName + "': " + e.getMessage());
	}
    }

}

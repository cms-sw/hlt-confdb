package confdb.gui;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * ConfdbSoftwareVersion
 * -----------
 * @author Raul Jimenez Estupinan.
 * 
 * Retrieve information about the application.
 */
public class ConfdbSoftwareVersion {
	java.io.BufferedInputStream inputStream;
	java.io.BufferedOutputStream outputStream;
	java.io.FileOutputStream fileOutputStream;
	
	String URL = "";
	String confdbWebVersion = "";
	String fileName = "confdb.version"; // already deployed in the container.
	
	// local confdb.version properties:
	String confdbUrl = "";
	String confdbContact = "";
	String confdbVersion = "";
	
	
	ConfdbSoftwareVersion() {
		loadLocalProperties();
		
		URL = getUrl() + fileName;
	}
	
	ConfdbSoftwareVersion(String url) {
		loadLocalProperties();
		
		URL = url + fileName;
	}

	public String getVersionFromWebContainer() {
		try {

			URL oracle = new URL(URL);
	        BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

	        confdbWebVersion = in.readLine(); // the version must be in the first line
	        in.close();
	        
	        if(confdbWebVersion.indexOf("=") != -1) // This is to fix the Properties class format.
	        	confdbWebVersion = confdbWebVersion.substring(confdbWebVersion.indexOf("=") + 1); 
			
			
		} catch (IOException e) {
			System.err.println("[ERROR][ConfdbSoftwareVersion] Unable to get confdb.version from " + URL);
			//e.printStackTrace();
		}
		return confdbWebVersion;
	}
	
	public String CheckSoftwareVersion() {

		// Get client version:
		
		
		// Get web container version.
		String containerVersion = getVersionFromWebContainer();
		int Container_version = versionToInt(containerVersion);
		int Current_version = versionToInt(confdbVersion);
		
		if(Container_version > Current_version) {
			System.out.println("[WARNING] You are using a lower version of Confdb GUI!");
			System.out.println("[WARNING] client = " + confdbVersion);
			System.out.println("[WARNING] containerVersion = " + containerVersion);
			
			
			String message = 	"A newer version of Confdb-GUI has been released!\n"+
								"It's highly recommended to get the latest version ("+containerVersion+").\n";
								
			JPanel panel = new JPanel();
	        panel.setLayout(new GridLayout(2, 2));
			JOptionPane.showMessageDialog(panel, message,"WARNING!", JOptionPane.WARNING_MESSAGE);
		} else if(Container_version < Current_version) {
			System.out.println("[WARNING] You are using an experimental version of Confdb GUI!");
			System.out.println("[WARNING] client = " + confdbVersion);
			System.out.println("[WARNING] containerVersion = " + containerVersion);
			
			String message = 	"You're running an experimental version of Confdb-GUI at the moment!\n"+
								"It's highly recommended to get the latest public version ("+containerVersion+").\n";
								
			JPanel panel = new JPanel();
	        panel.setLayout(new GridLayout(2, 2));
			JOptionPane.showMessageDialog(panel, message,"WARNING!", JOptionPane.WARNING_MESSAGE);
		}
		
		
		
		loadLocalProperties();
		
		return containerVersion;
	}
	
	private int versionToInt(String version) {
		int serial = -1;
		String foo = "";
		
		foo = version.replaceAll("V", "");
		foo = foo.replaceAll("v", "");
		//System.out.println("Replacing V -> " + foo);
		foo = foo.replaceAll("-", "");
		//System.out.println("Replacing -> " + foo);
		try {
		serial = Integer.parseInt(foo);
		} catch (NumberFormatException e) {
            System.out.println ("[ConfdbSoftwareVersion] ##Wrong Number##");
            serial = -1;
        }
		
		return serial;
	}
	
	public String getUrl() {
		return confdbUrl;
	}
	
	public String getContact() {
		return confdbContact;
	}
	
	public String getClientVersion() {
		return confdbVersion;
	}
	
	public void loadLocalProperties() {
		// Get client version:
		InputStream inStream = getClass().getResourceAsStream("/conf/confdb.version");
	    Properties  properties = new Properties();
	    try {
			properties.load(inStream);
		} catch (IOException e) {
			System.err.println("[ERROR][CheckSoftwareVersion] unable to load confdb.version file!");
			e.printStackTrace();
		}
		
		confdbVersion 	= properties.getProperty("confdb.version")	;
		confdbContact 	= properties.getProperty("confdb.contact")	;
		confdbUrl 		= properties.getProperty("confdb.url")		;

	}
	
	
}

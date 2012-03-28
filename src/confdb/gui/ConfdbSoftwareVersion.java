package confdb.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class ConfdbSoftwareVersion {
	java.io.BufferedInputStream inputStream;
	java.io.BufferedOutputStream outputStream;
	java.io.FileOutputStream fileOutputStream;
	
	String URL = "";
	String confdbVersion = "";
	String fileName = "confdb.version"; // already deployed in the container.
	
	ConfdbSoftwareVersion() {
		URL = getUrl() + fileName;
	}
	
	ConfdbSoftwareVersion(String url) {
		URL = url + fileName;
	}

	public String getVersionFromWebContainer() {
		try {

			URL oracle = new URL(URL);
	        BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

	        confdbVersion = in.readLine(); // the version must be in the first line
	        in.close();
	        
	        if(confdbVersion.indexOf("=") != -1) // This is to fix the Properties class format.
	        	confdbVersion = confdbVersion.substring(confdbVersion.indexOf("=") + 1); 
			
			
		} catch (IOException e) {
			System.err.println("[ERROR][ConfdbSoftwareVersion] Unable to get confdb.version from " + URL);
			//e.printStackTrace();
		}
		return confdbVersion;
	}
	
	public String CheckSoftwareVersion() {

		// Get client version:
		InputStream inStream = getClass().getResourceAsStream("/conf/confdb.version");
	    Properties  properties = new Properties();
	    try {
			properties.load(inStream);
		} catch (IOException e) {
			System.err.println("[ERROR][CheckSoftwareVersion] unable to load confdb.version file!");
			e.printStackTrace();
		}
		String clientVersion = properties.getProperty("confdb.version");
		
		// Get web container version.
		String containerVersion = getVersionFromWebContainer();
		
		if(!clientVersion.equals(containerVersion)) {
			System.out.println("[WARNING] You are using a lower version of Confdb GUI!");
			System.out.println("[WARNING] client = " + clientVersion);
			System.out.println("[WARNING] containerVersion = " + containerVersion);
		}
		
		return containerVersion;
	}
	
	public String getUrl() {
		String url_ = "";
		// Get client version:
		InputStream inStream = getClass().getResourceAsStream("/conf/confdb.version");
	    Properties  properties = new Properties();
	    try {
			properties.load(inStream);
		} catch (IOException e) {
			System.err.println("[ERROR][CheckSoftwareVersion] unable to load confdb.version file!");
			e.printStackTrace();
		}
		
		url_ = properties.getProperty("confdb.url");
		
		return url_;
	}
	
}

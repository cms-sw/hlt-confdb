package confdb.parser;

import java.util.Scanner;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.*;

import confdb.data.*;
import confdb.db.*;


/**
 * JPythonParser
 * ------------
 * @author Philipp Schieferdecker
 *
 * Parse a flat *.py configuration file using Jython
 *
 */

@SuppressWarnings("unchecked")
public class JPythonParser
{
    //
    // member data
    //
    
    /** the software release w.r.t. which the configuration is created */
    private SoftwareRelease release = null;
    private String cmsswTag = null;
    
    /** Create an instance of the PythonInterpreter */
    private PythonInterpreter pythonInterpreter = null;
    
    /** Python Configuration object */
    private PyObject process = null;

    /** ConfDB Configuration object */
    private Configuration configuration = null;
    
    /** set with all problem components */
    private HashSet<String> problemModules = new HashSet<String>();
    
    /** set with all problem parameter names */
    private HashSet<String> problemParameters = new HashSet<String>();
    
    /** output stream for problems.txt */
    private PrintWriter  problemStream = null;
    private StringBuffer problemBuffer = new StringBuffer();

    
    //
    // construction
    //

    /** standard constructor */
    public JPythonParser(SoftwareRelease release)
    {
	this.release = release;
	cmsswTag = release.releaseTag();
	int index=cmsswTag.indexOf("_HLT");
	if (index>=0) cmsswTag = cmsswTag.substring(0,index);
    }
    
    static private <T> T convert(PyObject object, Class<T> c) {
        T value = (T) object.__tojava__(c);
        return value;
    }

    //
    // member functions
    //
    
    /** get problems as string */
    public String problemsAsString() { return problemBuffer.toString(); }

    /** exec py cmd */
    public void pyCmd(String pycmd) {
	System.out.println("pycmd="+pycmd);
	pythonInterpreter.exec(pycmd);
    }

    /** parse a file */
    public void parseFile(String fileName) throws JParserException
    {

	// path to file without trailing /
	String path = new String(fileName);
	path=path.substring(0,path.lastIndexOf("/")+1);
	if (path.lastIndexOf("/")>=0) path=path.substring(0,path.lastIndexOf("/"));

	// name of file including extension
	String name = new String(fileName);
	name=name.substring(name.lastIndexOf("/")+1);

	// name of file excluding extension
	String leaf = new String(name);
	if (leaf.indexOf(".py")>=0) leaf=leaf.substring(0,leaf.indexOf(".py"));

	System.out.println("JPythonParser: ");
	System.out.println("  ReleaseTag: "+release.releaseTag());
	System.out.println("  CMSSW  Tag: "+cmsswTag);
	System.out.println("  Full File : "+fileName);
	System.out.println("  File Path : "+path);
	System.out.println("  File Name : "+name);
	System.out.println("  Leaf Name : "+leaf);

	String pyCmd = null;

	try {

	    pythonInterpreter = new PythonInterpreter();

	    // for large py files, would need pyc compiled files but this does not work
	    //      pyCmd("import commands");
	    //      pyCmd("print commands.getstatusoutput('python -m py_compile "+fileName+"')");
	    //      pyCmd("print commands.getstatusoutput('python -m compileall -f "+path+"/FWCore')");

	    // need to set up search path
	    pyCmd("import sys");
	    pyCmd("sys.path.append('"+path+"')");
	    //	    pyCmd("sys.path.append('"+path+"/FWCore')");
	    //	    pyCmd("sys.path.append('"+path+"/FWCore/ParameterSet')");
	    pyCmd("print sys.path");

	    // now import pyc file and its process
	    //      pyCmd("import pycimport");
	    //	    pyCmd("from FWCore.ParameterSet.Options import Options");
	    //	    pyCmd("import Config as cms");
	    //	    pyCmd("import FWCore.ParameterSet.Config as cms");
	    //	    pyCmd("theProcess = cms.Process('HIGGS')");
	    //	    pyCmd("theProcess = __import__('"+leaf+"')");
	    //	    pyCmd("import FWCore.ParameterSet.Config as cms");

	    // now import process object from (uncompiled) py file
	    pyCmd("from "+leaf+" import process");
	    pyCmd("print process");

	    // get process object
	    process = pythonInterpreter.get("process");
	    System.out.println("Process object found: "+(process!=null));

	    // get its process name
	    String processName = convert(process.invoke("name_"),String.class);
	    System.out.println("Process  name  found: "+processName);

	    // configinfo of new configuration
	    ConfigInfo configInfo =
		new ConfigInfo(name,null,-1,0,"","",
			       release.releaseTag(),processName,
			       "parsed from "+fileName);

	    // new configuration
	    configuration = new Configuration();
	    configuration.initialize(configInfo,release);
	    
	}
	/*
	catch (IOException e) {
	    System.err.println("Error opening file " + fileName + ": " +
			       e.getMessage());
	}
	*/
	catch (Exception e) {
	    e.printStackTrace();
	    //printParsedTree();
	}

    }
    
    /** turn process object into configuration */
    public Configuration createConfiguration() throws JParserException
    {

	System.out.println("JPythonParser::createConfiguration() 0 called!");

	if (release==null || process==null ) return null;
	
	System.out.println("JPythonParser::createConfiguration() 1 called!");

	// add global psets
	
	// add primary data source (edsource)

	// add essources

	// add esmodules

	// set preferred essources / esmodules

	// add services

	// add sequences

	// add paths

	// add endpaths

	return configuration;
    }

    /** close the problem stream, if it is open */
    public boolean closeProblemStream()
    {
	if (problemStream==null) return false;
	problemStream.flush();
	problemStream.close();
	return true;
    }

    //
    // private member functions
    //
    
    /** record a missing/mismatched parameter */
    private void addProblem(String type,String name,String subsys,String pkg,
			    Parameter p)
    {
	if (problemParameters.contains(name + "." + p.name())) return;
	   
	if (problemStream==null) {
	    try {
		problemStream = new PrintWriter(new FileWriter("problems.txt"));
		problemBuffer = new StringBuffer();
	    }
	    catch (IOException e) {
		System.err.println("Can't open problems.txt: "+e.getMessage());
		return;
	    }
	}
	
	if (!problemModules.contains(name)) {
	    problemModules.add(name);
	    String s = (type.startsWith("Module:")) ?
		"Module " + type.substring(7) : type;
	    problemStream.println("\n"+s+" "+name+" "+subsys+" "+pkg);
	    problemBuffer.append("\n"+s+" "+name+" "+subsys+" "+pkg+"\n");
	}
	
	String trkd = (p.isTracked()) ? "tracked" : "untracked";
	String pline = name + ".Parameter " + p.name() + " " + p.type() + " ";
	if (p instanceof PSetParameter|| p instanceof VPSetParameter) {
	    pline += trkd;
	}
	else if (p instanceof VectorParameter) {
	    pline += "{ " + p.valueAsString() + " } " + trkd;
	}
	else {
	    pline += p.valueAsString() + " " + trkd;
	}
	problemStream.println(pline);
	problemBuffer.append(pline+"\n");
	problemParameters.add(name + "." + p.name());
    }

    //
    // main (testing)
    //

    /** main method */

    public static void main(String[] args)
    {


    }

}

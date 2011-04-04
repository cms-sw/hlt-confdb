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

    /** parse a file */
    public void parseFile(String fileName) throws JParserException
    {
	System.out.println("JPythonParser: "+fileName);
	String name = fileName;
	while (name.indexOf("/")>=0) name=name.substring(name.indexOf("/")+1);
	if    (name.indexOf(".py")>0)name=name.substring(0,name.indexOf(".py"));
	System.out.println("JPythonParser: "+name);

	String processName = null;
	try {

	    System.out.println("JPythonParser: parse 1");

	    pythonInterpreter = new org.python.util.PythonInterpreter();
	    System.out.println("JPythonParser: parse 2");
	    pythonInterpreter.exec("import sys");
	    System.out.println("A2:");
	    pythonInterpreter.exec("import FWCore.ParameterSet.Config as cms");
	    System.out.println("A2:");
	    pythonInterpreter.exec("from FWCore.ParameterSet import DictTypes");
	    System.out.println("A3:");
	    pythonInterpreter.exec("import sys, os, os.path");
	    System.out.println("A4:");

	    System.out.println("loading HLT configuration from "+fileName);
	    pythonInterpreter.exec("from full import process");
	    System.out.println("...done");

	    process = pythonInterpreter.get("process");
	    processName = convert(process.invoke("name_"),String.class);

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

	System.out.println("XXX: "+processName);

	ConfigInfo configInfo =
	    new ConfigInfo(name,null,-1,0,"","",
			   release.releaseTag(),processName,
			   "parsed from "+fileName);

	configuration = new Configuration();
	configuration.initialize(configInfo,release);

    }
    
    /** turn parsed tree into configuration */
    public Configuration createConfiguration() throws JParserException
    {

	System.out.println("JPythonParser::createConfiguration() called!");
	
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

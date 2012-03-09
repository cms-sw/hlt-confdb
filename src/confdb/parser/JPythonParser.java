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
	    
	    /////////////////////////////////////////////////////////
	    // 27-feb-2012
	    pyCmd("import sys");
	    pyCmd("sys.path.append('python')");   // add the CMSSW Python path
	    pyCmd("import pycimport");            // load precompiled .pyc files
	    /////////////////////////////////////////////////////////

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

	//////////////////////////////////////////////////////////
    //Parser parser = new Parser();
    //Process Pyprocess = parser.parseProcess(process);
    //Pyprocess.dump();
	//////////////////////////////////////////////////////////
	
	// add global psets
	parsePSets(process);
	// add primary data source (edsource)
	parseEDSources(process);
	// add essources
	parseESSources(process);
	// add esmodules
	parseESModules(process);
	// set preferred essources / esmodules
	//?
	// add services
	parseServices(process);
	
	// MODULES:
	// add producers:
	parseProducerModules(process);
	// add filters:
	parseFilterModules(process);
	// add analyzers:
	parseAnalyzerModules(process);
	// add outputmodules:
	parseOutputModules(process);
	
	
	// add sequences
	parseSequencesFromPython(process);
	// add paths
    parsePathsFromPython(process);
	// add endpaths
    parseEndPathsFromPython(process);
    
    
	return configuration;
    }
    
    
    // parse Services:
    private void parseServices(PyObject process) {
        PyDictionary producers = (PyDictionary) process.__getattr__("_Process__services");
        if(validPyObject(producers)) 
        	parseServiceMap(producers);
    }
    
    // parse EDSources:
    private void parseEDSources(PyObject process) {
    	PyObject source = (PyObject) process.__getattr__("_Process__source");
        if(validPyObject(source)) 
        	parseEDSource(source);
    }
    
    // parse Psets:
    private void parsePSets(PyObject process) {
        PyDictionary producers = (PyDictionary) process.__getattr__("_Process__psets");
        if(validPyObject(producers)) 
        	parsePSetMap(producers);
    }
    
    
    // parse Modules of type ESModules
    private void parseESModules(PyObject process) {
        PyDictionary producers = (PyDictionary) process.__getattr__("_Process__esproducers");
        if(validPyObject(producers)) 
        	parseESModuleMap(producers);
        else System.out.println("[parseESModules] ESModules no valid objects to parse."); //TODO: this message is not needed
    }
    
    // parse ESSources:
    private void parseESSources(PyObject process) {
        PyDictionary producers = (PyDictionary) process.__getattr__("_Process__essources");
        if(validPyObject(producers)) 
        	parseESSourceMap(producers);
    }
    
    // parse Modules of type Producer:
    private void parseProducerModules(PyObject process) {
        PyDictionary producers = (PyDictionary) process.__getattr__("_Process__producers");
        if(validPyObject(producers)) 
        	parseModuleMap(producers);
    }
    
    // parse Modules of type filters:
    private void parseFilterModules(PyObject process) {
        PyDictionary producers = (PyDictionary) process.__getattr__("_Process__filters");
        if(validPyObject(producers)) 
        	parseModuleMap(producers);
    }
    
    // parse Modules of type analyzers:
    private void parseAnalyzerModules(PyObject process) {
        PyDictionary producers = (PyDictionary) process.__getattr__("_Process__analyzers");
        if(validPyObject(producers)) 
        	parseModuleMap(producers);
    }
    
    // parse Modules of type outputmodules:
    private void parseOutputModules(PyObject process) {
        PyDictionary producers = (PyDictionary) process.__getattr__("_Process__outputmodules");
        if(validPyObject(producers)) 
        	parseModuleMap(producers);
    }
    
        
    // parse Services dictionary
    private void parseServiceMap(PyDictionary pydict) {
    	System.out.println("Services (" + pydict.size() + ")");
        for (Object o : pydict.entrySet()) {
            PyDictionary.Entry<String, PyObject> moduleObject = (PyDictionary.Entry<String, PyObject>) o;
            parseService(moduleObject.getValue());
        }
    }
    
    // parse EDSource dictionary
    private void parseEDSource(PyObject pydict) {

        String type  = getType(pydict);
        String label = getLabel(pydict);
        
        String moduleClass = convert(pydict.invoke("type_"), String.class);
        
        //ModuleInstance module = configuration.insertModule(moduleClass,label);
        EDSourceInstance module = configuration.insertEDSource(moduleClass);
        //TODO: Update module with file values?
        PyDictionary parameterContainerObject = (PyDictionary) pydict.invoke("parameters_");
        updateModuleParameters(parameterContainerObject, module);
        
    }
    
    // parse modules dictionary
    private void parseESModuleMap(PyDictionary pydict) {
    	System.out.println("ESModules (" + pydict.size() + ")");
        for (Object o : pydict.entrySet()) {
            PyDictionary.Entry<String, PyObject> moduleObject = (PyDictionary.Entry<String, PyObject>) o;
            parseESModule(moduleObject.getValue());
        }
    }
    
    // parse modules dictionary
    private void parseESSourceMap(PyDictionary pydict) {
    	System.out.println("ESSources (" + pydict.size() + ")");
        for (Object o : pydict.entrySet()) {
            PyDictionary.Entry<String, PyObject> moduleObject = (PyDictionary.Entry<String, PyObject>) o;
            parseESSource(moduleObject.getValue());
        }
    }
    
    // parse modules dictionary
    private void parseModuleMap(PyDictionary pydict) {
    	System.out.println("Modules (" + pydict.size() + ")");
        for (Object o : pydict.entrySet()) {
            PyDictionary.Entry<String, PyObject> moduleObject = (PyDictionary.Entry<String, PyObject>) o;
            parseModule(moduleObject.getValue());
        }
    }
    
    // parse PSets dictionary
    private void parsePSetMap(PyDictionary pydict) {
    	System.out.println("PSets (" + pydict.size() + ")");
        for (Object o : pydict.entrySet()) {
            PyDictionary.Entry<String, PyObject> moduleObject = (PyDictionary.Entry<String, PyObject>) o;
            
            parsePSet(moduleObject.getValue());
        }
    }
    
    // Parse one single module
    private boolean parseModule(PyObject moduleObject) {
    	
        String type  = getType(moduleObject);
        String label = getLabel(moduleObject);
        
        String moduleClass = convert(moduleObject.invoke("type_"), String.class);
        
        ModuleInstance module = configuration.insertModule(moduleClass,label);
        //TODO: Update module with file values?
        PyDictionary parameterContainerObject = (PyDictionary) moduleObject.invoke("parameters_");
        updateModuleParameters(parameterContainerObject, module);
        
        return true;
    }
    
    // Parse one single Dataset
    private boolean parsePSet(PyObject psetObject) {
        String type  = getType(psetObject);
        String label = getLabel(psetObject);
        
        System.out.println("parsePset: type="+type+", label="+label);
        Boolean     tracked = convert(psetObject.invoke("isTracked"), Boolean.class);
	    PSetParameter pset = (PSetParameter)ParameterFactory.create("PSet",label,"",tracked);
	    //
        // Add parameters to PSET.
	    ArrayList<confdb.data.Parameter> params = parsePSetParameters(psetObject);
	    for(int It = 0; It < params.size(); It++)
	    	pset.addParameter(params.get(It));
	    
	    // Insert PSet to configuration.
	    configuration.insertPSet(pset);
        
        return true;
    }
    
    // Parse one single ESModule
    private boolean parseESModule(PyObject moduleObject) {
    	
        String type  = getType(moduleObject);
        String label = getLabel(moduleObject);
        
        String moduleClass = convert(moduleObject.invoke("type_"), String.class);
        
        //ModuleInstance module = configuration.insertModule(moduleClass,label);
        ESModuleInstance module = configuration.insertESModule(configuration.esmoduleCount(), moduleClass, label);
        //TODO: Update module with file values?
        PyDictionary parameterContainerObject = (PyDictionary) moduleObject.invoke("parameters_");
        updateModuleParameters(parameterContainerObject, module);
        
        return true;
    }
    
    // Parse one single Service
    private boolean parseService(PyObject moduleObject) {
    	
        String type  = getType(moduleObject);
        String label = getLabel(moduleObject);
        
        String moduleClass = convert(moduleObject.invoke("type_"), String.class);
        
        //ModuleInstance module = configuration.insertModule(moduleClass,label);
        ServiceInstance module = configuration.insertService(configuration.serviceCount(), moduleClass);
        //TODO: Update module with file values?
        PyDictionary parameterContainerObject = (PyDictionary) moduleObject.invoke("parameters_");
        updateModuleParameters(parameterContainerObject, module);
        
        return true;
    }
    
    // Parse one single ESSource
    private boolean parseESSource(PyObject moduleObject) {
    	
        String type  = getType(moduleObject);
        String label = getLabel(moduleObject);
        
        String moduleClass = convert(moduleObject.invoke("type_"), String.class);
        
        //ModuleInstance module = configuration.insertModule(moduleClass,label);
        
        ESSourceInstance module = configuration.insertESSource(configuration.essourceCount(), moduleClass, label);
        //TODO: Update module with file values?
        PyDictionary parameterContainerObject = (PyDictionary) moduleObject.invoke("parameters_");
        updateModuleParameters(parameterContainerObject, module);
        
        return true;
    }
    
    // Parse Sequences:
    private void parseSequencesFromPython(PyObject pyprocess){
        PyDictionary sequences = (PyDictionary) pyprocess.__getattr__("_Process__sequences");
        if(validPyObject(sequences)) 
        	parseSequenceMap(sequences);
    }
    
    // Parse Paths:
    private void parsePathsFromPython(PyObject pyprocess){
        PyDictionary paths = (PyDictionary) pyprocess.__getattr__("_Process__paths");
        
        if(validPyObject(paths))
        	parsePathMap(paths);
        
    }
    
    // Parse Paths:
    private void parseEndPathsFromPython(PyObject pyprocess){
        PyDictionary paths = (PyDictionary) pyprocess.__getattr__("_Process__endpaths");
        
        if(validPyObject(paths))
        	parseEndPathMap(paths);
        
    }
    
    private boolean parsePathMap(PyDictionary pydict) {
    	System.out.println("Paths (" + pydict.size() + ")");
    	PyList keys = (PyList) pydict.invoke("keys");
        for (Object key : keys) {
        	String pathName = (String) key;
        	//System.out.println("Path: " + key);
        	Path path = configuration.insertPath(configuration.pathCount(), pathName);
        	PyObject value = pydict.__getitem__(new PyString((String) key));
        	parsePath(value);
        }
        
        return true;
    }
    
    private boolean parseEndPathMap(PyDictionary pydict) {
    	System.out.println("EndPaths (" + pydict.size() + ")");
    	PyList keys = (PyList) pydict.invoke("keys");
        for (Object key : keys) {
        	String pathName = (String) key;
        	//System.out.println("Path: " + key);
        	Path path = configuration.insertPath(configuration.pathCount(), pathName);
        	path.isEndPath();
        	PyObject value = pydict.__getitem__(new PyString((String) key));
        	parsePath(value);
        }
        
        return true;
    }
    
    // parse one single path, creating references to its sequences.
    private void parsePath(PyObject object) {
    	String type  = getType(object);
        String label = getLabel(object);
        //System.out.println("[parseSequence] " + type + " " + label);
        confdb.data.Path insertedPath = null;
        
        if(confdbTypes.path.is(type)) {
        	insertedPath = configuration.path(label);
        	
        	if(insertedPath == null)
        		System.err.println("[parsePath] path does not exist!" + label);

        	
    		// Content:
        	PyObject pathContent = object.__getattr__(new PyString("_seq"));
        	parseReferenceContainerContent(pathContent, insertedPath);
        	
        } else System.out.println("[parsePath] CHUNGO: type " + type);
    }
    
    

    private void updateModuleParameters(PyDictionary parameterContainer, 
    																confdb.data.Instance module) {
    	
        for (Object parameterObject : parameterContainer.entrySet()) {
            PyDictionary.Entry<String, PyObject> entry = (PyDictionary.Entry<String, PyObject>) parameterObject;
            //map.put(entry.getKey(), parseParameter(entry.getValue()));
            parseParameter(entry.getKey(), entry.getValue(), module);
        }
        
    }
    
    
    
    private ArrayList<confdb.data.Parameter> parsePSetParameters(PyObject parameterContainerObject) {
    	ArrayList<confdb.data.Parameter> params = new ArrayList<confdb.data.Parameter>();
        String type  = getType(parameterContainerObject);
        String label = getLabel(parameterContainerObject);
        System.out.println("PSET parameter type="+type+", label="+label);
    	PyDictionary parameterContainer = (PyDictionary) parameterContainerObject.invoke("parameters_");
			for (Object parameterObject : parameterContainer.entrySet()) {
			PyDictionary.Entry<String, PyObject> entry = (PyDictionary.Entry<String, PyObject>) parameterObject;
			//map.put(entry.getKey(), parseParameter(entry.getValue()));
			
			System.out.println("      entry getKey = "+entry.getKey()); //TODO Be careful with this line, could be empty?
			confdb.data.Parameter param = __parseParameter(entry.getValue(), entry.getKey());
			
			params.add(param);
			System.out.println("      parameter full name = "+param.fullName());
			System.out.println("      parameter value as string = "+param.valueAsString());
			}
			return params;
    }
    
    
    
    // Parse parameter types
    private confdb.data.Parameter __parseParameter(PyObject parameterObject, String name) {
        String      type    = parameterObject.getType().getName();
        Boolean     tracked = convert(parameterObject.invoke("isTracked"), Boolean.class);
        PyObject    value   = parameterObject.invoke("value");
        
        
        
        return ParameterFactory.create(type, name, value.toString(), tracked);
    }
   
    
    // Parse parameter types
    private void parseParameter(String parameterName, PyObject parameterObject, Instance module) {
        String      type    = parameterObject.getType().getName();
        Boolean     tracked = convert(parameterObject.invoke("isTracked"), Boolean.class);
        PyObject    value   = parameterObject.invoke("value");

        /* Note:
         * all integer types are extracted using the generic Number class;
         * this will automatically use BigInteger for uint64_t numbers, etc.
         */

        /* Note:
         * Care should be taken to keep hex numbers in hex notation
         */

        /* Note:
         * an InputTag could either be split in
         *   'label', 'instance', 'pyprocess'
         * or left as a single string
         *   'label:instance:pyprocess'
         */

        /* Note:
         * these CMS types are not yet supported:
         *   FileInPath
         *   EventID
         *   VEventID
         *   LuminosityBlockID
         *   VLuminosityBlockID
         *   EventRange
         *   VEventRange
         *   LuminosityBlockRange
         *   VLuminosityBlockRange
         */
        
        if ("bool" == type) {
            //Boolean v = convert(value, Boolean.class);
            
            module.updateParameter(parameterName,type,value.toString());
            module.findParameter(parameterName).setTracked(tracked);
        } else if ("vbool" == type) {
            //List<Boolean> v = convert(value, List.class);
            
            module.updateParameter(parameterName,type,value.toString());
            module.findParameter(parameterName).setTracked(tracked);
        } else if ("uint32" == type || "int32" == type || "uint64" == type || "int64" == type) {
            //Number v = convert(value, Number.class);
            
            module.updateParameter(parameterName,type,value.toString());
            module.findParameter(parameterName).setTracked(tracked);
        } else if ("vuint32" == type || "vint32" == type || "vuint64" == type || "vint64" == type) {
            //List<Number> v = convert(value, List.class);
            // NOTE: No need to convert or to cast value by value.
        	// It can be done at once, but erasing the vector brackets.
            confdb.data.Parameter param = module.findParameter(parameterName);
            if(param != null) {
            	if(param instanceof VectorParameter) {
            		VectorParameter param_sp = (VectorParameter) param;
            		String clean_value = cleanBrackets(value.toString());
            		param_sp.setValue(clean_value);
            		
                    module.updateParameter(parameterName,type,clean_value);
                    module.findParameter(parameterName).setTracked(tracked);
            	} else System.err.println("[parseParameter] parameter not VectorParameter! " + parameterName);
            } else System.err.println("[parseParameter] parameter not found! " + parameterName);
            
        } else if ("double" == type) {
            Double v = convert(value, Double.class);
            
            module.updateParameter(parameterName,type,value.toString());
            module.findParameter(parameterName).setTracked(tracked);
        } else if ("vdouble" == type) {
            //List<Double> v = convert(value, List.class);
            
            confdb.data.Parameter param = module.findParameter(parameterName);
            if(param != null) {
            	if(param instanceof VectorParameter) {
            		VectorParameter param_sp = (VectorParameter) param;
            		String clean_value = cleanBrackets(value.toString());
            		param_sp.setValue(clean_value);
            		
                    module.updateParameter(parameterName,type,clean_value);
                    module.findParameter(parameterName).setTracked(tracked);
            	} else System.err.println("[parseParameter] parameter not VectorParameter! " + parameterName);
            } else System.err.println("[parseParameter] parameter not found! " + parameterName);
            
        } else if ("string" == type) {
            String v = convert(value, String.class);
            if(v.isEmpty()) System.out.println("*****************+ Setting an empty string!");
            module.updateParameter(parameterName,type,value.toString());
            module.findParameter(parameterName).setTracked(tracked);
        } else if ("vstring" == type) {
            //List<String> v = convert(value, List.class);
            
            confdb.data.Parameter param = module.findParameter(parameterName);
            if(param != null) {
            	if(param instanceof VectorParameter) {
            		VectorParameter param_sp = (VectorParameter) param;
            		String clean_value = cleanBrackets(value.toString());
            		param_sp.setValue(clean_value);
            		
                    module.updateParameter(parameterName,type,clean_value);
                    module.findParameter(parameterName).setTracked(tracked);
            	} else System.err.println("[parseParameter] parameter not VectorParameter! " + parameterName);
            } else System.err.println("[parseParameter] parameter not found! " + parameterName);
            
        } else if ("PSet" == type) {
            PyDictionary v = (PyDictionary) parameterObject.invoke("parameters_");
            module.updateParameter(parameterName,type,value.toString());
            module.findParameter(parameterName).setTracked(tracked);
            
        } else if ("VPSet" == type) {
            //TODO: need parseList method. ?
            confdb.data.Parameter param = module.findParameter(parameterName);
            if(param != null) {
            	if(param instanceof VectorParameter) {
            		VectorParameter param_sp = (VectorParameter) param;
            		String clean_value = cleanBrackets(value.toString());
            		param_sp.setValue(clean_value);
            		
                    module.updateParameter(parameterName,type,clean_value);
                    module.findParameter(parameterName).setTracked(tracked);
            	} else System.err.println("[parseParameter] parameter not VectorParameter! " + parameterName);
            } else System.err.println("[parseParameter] parameter not found! " + parameterName);
        } else if ("InputTag" == type) {
            //String v = convert(value, String.class);
            
            module.updateParameter(parameterName,type,value.toString());
            module.findParameter(parameterName).setTracked(tracked);
        } else if ("VInputTag" == type) {
            //List<String> v = convert(value, List.class);
            
            confdb.data.Parameter param = module.findParameter(parameterName);
            if(param != null) {
            	if(param instanceof VectorParameter) {
            		VectorParameter param_sp = (VectorParameter) param;
            		String clean_value = cleanBrackets(value.toString());
            		param_sp.setValue(clean_value);
            		
                    module.updateParameter(parameterName,type,clean_value);
                    module.findParameter(parameterName).setTracked(tracked);
            	} else System.err.println("[parseParameter] parameter not VectorParameter! " + parameterName);
            } else System.err.println("[parseParameter] parameter not found! " + parameterName);
        } else {
            System.out.println("[parseParameter] TYPE: [unsupported] " + type);
        }
        
    }
    
    private String cleanBrackets(String vectorValues) {
    	return  vectorValues.replace("[", "").replace("]", "");
    }
    
    
    
    private void parseSequenceMap(PyDictionary pydict) {
    	System.out.println("Sequences (" + pydict.size() + ")");
    	PyList keys = (PyList) pydict.invoke("keys");
    	
        for (Object key : keys) {
        	String sequenceName = (String) key;
        	
        	confdb.data.Sequence seq = configuration.sequence(sequenceName);
        	if(seq == null) {
        		
        		PyObject value = pydict.__getitem__(new PyString((String) key)); // Get the Sequence Python Object.
        		parseSequence(value);	// Parse it and insert the sequence. Recursively
        		
        	} //else System.out.println("Seq: " + sequenceName + " already exist! "); 
        }
    	
    }
    
    
    private confdb.data.Sequence parseSequence(PyObject object) {
    	String type  = getType(object);
        String label = getLabel(object);
        //System.out.println("[parseSequence] " + type + " " + label);
        confdb.data.Sequence insertedSeq = null;
        
        
        if(confdbTypes.sequence.is(type)) {
        	insertedSeq = configuration.sequence(label);
        	
        	if(insertedSeq == null) {
	        	// Now we can insert the resulting sequence (with subitems already on it).
	    		int seqIndex = configuration.sequenceCount();
	    		insertedSeq = configuration.insertSequence(seqIndex, label);
        	}
        	
    		// Content:
        	PyObject sequenceContent = object.__getattr__(new PyString("_seq"));
        	parseReferenceContainerContent(sequenceContent, insertedSeq);
        	
        } else System.out.println("[parseSequence] CHUNGO: type " + type);
        
        return insertedSeq;
    }
    
    // Parse sequence/path content:
    private void parseReferenceContainerContent(PyObject sequenceContent, confdb.data.ReferenceContainer parentContainer) {
    	String type  = getType(sequenceContent);
        String label = getLabel(sequenceContent);	// subseq.
        
        if(pythonObjects.sequence.is(type)) {
        	// Parse sub-items of label sequence on demand:
        	confdb.data.Sequence subSequence = parseSequenceOnDemand(label);
        	
        	Reference seqRef = parentContainer.entry(label);
        	
        	// If sequence reference does not exist in this container, add it!
        	if(seqRef == null) configuration.insertSequenceReference(parentContainer,parentContainer.entryCount(),subSequence); // Missing operator.

        	
        } else if(	pythonObjects.seqOpFollows.is(type)	||
        			pythonObjects.seqOpAids.is(type)) 	{
        	// Contain list of sequences?
        	PyObject leftContent  = sequenceContent.__getattr__(new PyString("_left"));
        	PyObject rightContent = sequenceContent.__getattr__(new PyString("_right"));
        	
        	parseReferenceContainerContent(leftContent, parentContainer);
        	parseReferenceContainerContent(rightContent, parentContainer);
        	
        	
        } else if(pythonObjects.seqIgnore.is(type)) {
        	PyObject operandContent = sequenceContent.__getattr__(new PyString("_operand"));
        	parseReferenceContainerContent(operandContent, parentContainer);
        	// TODO: Must set ignore to the generated sequence. How?
        } else if(pythonObjects.seqNegation.is(type)) {
        	PyObject operandContent = sequenceContent.__getattr__(new PyString("_operand"));
        	parseReferenceContainerContent(operandContent, parentContainer);
        	// TODO: Must negate the generated sequence. How?
        } else if(	pythonObjects.EDProducer.is(type)	||
        			pythonObjects.EDFilter.is(type)		||
        			pythonObjects.EDAnalyzer.is(type)	||
        			pythonObjects.OutputModule.is(type)){
        	
        	//TODO: Insert references to previous inserted modules of these types.
        	ModuleInstance module = configuration.module(label);
        	if(module == null) {
        		System.err.println("[JPythonParser::parseSequenceImpl] module not found! ");
        		System.err.println("[JPythonParser::parseSequenceImpl] label = " + label);
        		System.err.println("[JPythonParser::parseSequenceImpl] type  = " + type);
        	}
        	
        	Reference moduleRef = parentContainer.entry(module.name());
        	
        	if(moduleRef == null)
        		configuration.insertModuleReference(parentContainer, parentContainer.entryCount(), module);
        	// TODO: How to set the operator of the module reference?
        	//config.insertModuleReference(sequence,index,entry).setOperator( operator );
        }
        //else System.out.println("type = " + type);
        
    }
    
    // Return the recently inserted sequence to be linked to the parent seq/container.
    private confdb.data.Sequence parseSequenceOnDemand(String label) {
    	PyDictionary sequences = (PyDictionary) process.__getattr__("_Process__sequences");
    	PyObject sequence = sequences.__getitem__(new PyString(label));
    	
    	return parseSequence(sequence);
    }

    private static boolean validPyObject(PyObject object) {
        String type  = getType(object);
        String label = getLabel(object);
        boolean validObject = true;
    	if((type == "NoneType")||(label== "null")) {
    		validObject = false;
    		
    		System.out.println("[validPyObject] Object of type = " + type + ", label = " + label + " does not exist in this Config.");
    	}
    	
    	return validObject;
    }
    
    private static String getType(PyObject object) {
    	return object.getType().getName();
    }

    private static String getLabel(PyObject object) {
    	
    	PyStringMap dict = (PyStringMap) object.getDict();
    	
    	if(dict == null) return null;	// patched to don't brake
    	
    	if (dict.has_key("_Labelable__label"))
    		return convert(dict.__getitem__("_Labelable__label"), String.class);
    	else if (dict.has_key("label") && dict.__getitem__("label").isCallable())
            return convert(object.invoke("label"), String.class);
        else
        	return null;
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
			    confdb.data.Parameter p)
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

    
    // ENUM types for support.
    
    public enum confdbTypes {
  	  sequence("Sequence"),
  	  path("Path"),
  	  module("Module");

  	  private String text;

  	  confdbTypes(String text) {
  	    this.text = text;
  	  }

  	  public String getText() {
  	    return this.text;
  	  }

  	  public static confdbTypes fromString(String text) {
  	    if (text != null) {
  	      for (confdbTypes b : confdbTypes.values()) {
  	        if (text.equalsIgnoreCase(b.text)) {
  	          return b;
  	        }
  	      }
  	    }
  	    return null;
  	  }
  	  
      public boolean is(String OutText) {
      	if(this.text.compareTo(OutText) == 0) return true;
      	return false;
      }
      
  	}
    
    public enum confdbDataTypes {
    	bool("bool"),
    	vbool("vbool"),
    	uint32("uint32"),
    	int32("int32"),
    	uint64("uint64"),
    	int64("int64"),
    	vuint32("vuint32"),
    	vint32("vint32"),
    	vuint64("vuint64"),
    	vint64("vint64"),
    	Double("double"),
    	vdouble("vdouble"),
    	string("string"),
    	vstring("vstring"),
    	PSet("PSet"),
    	InputTag("InputTag"),
    	VInputTag("VInputTag");

    	  private String text;

    	  confdbDataTypes(String text) {
    	    this.text = text;
    	  }

    	  public String getText() {
    	    return this.text;
    	  }

    	  public static confdbDataTypes fromString(String text) {
    	    if (text != null) {
    	      for (confdbDataTypes b : confdbDataTypes.values()) {
    	        if (text.equalsIgnoreCase(b.text)) {
    	          return b;
    	        }
    	      }
    	    }
    	    return null;
    	  }
    	  
        public boolean is(String OutText) {
        	if(this.text.compareTo(OutText) == 0) return true;
        	return false;
        }
        
    	}
    
    public enum pythonObjects {
    	  sequence("Sequence"),
    	  seqOpFollows("_SequenceOpFollows"),
    	  seqNegation("_SequenceNegation"),
    	  seqIgnore("_SequenceNegation"),
    	  seqOpAids("_SequenceOpAids"), 
    	  EDProducer("EDProducer"),
    	  EDFilter("EDFilter"),
    	  EDAnalyzer("EDAnalyzer"),
    	  OutputModule("OutputModule");

    	  private String text;

    	  pythonObjects(String text) {
    	    this.text = text;
    	  }

    	  public String getText() {
    	    return this.text;
    	  }

    	  public static pythonObjects fromString(String text) {
    	    if (text != null) {
    	      for (pythonObjects b : pythonObjects.values()) {
    	        if (text.equalsIgnoreCase(b.text)) {
    	          return b;
    	        }
    	      }
    	    }
    	    return null;
    	  }
    	  
        public boolean is(String OutText) {
        	if(this.text.compareTo(OutText) == 0) return true;
        	return false;
        }
    }

}





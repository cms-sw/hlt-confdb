package confdb.parser;

import java.util.Scanner;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import java.io.*;

import confdb.data.*;
import confdb.db.*;
import confdb.converter.*;


/**
 * PythonParser
 * ------------
 * @author Philipp Schieferdecker
 *
 * Parse a flat *.py configuration file, created with EdmConfigToPython
 * tool.
 */
public class PythonParser
{
    //
    // member data
    //
    
    /** valid opening brackets */
    private static final String obrackets[] = { "{", "[", "(", ":" };
    
    /** corresponding valid closing brackets */
    private static final String cbrackets[] = { "}", "]", ")" };

    /** valid opening quotes */
    private static final String oquotes[] = { "r\"'","r'\"","'" };

    /** corresponding valid closing quotes */
    private static final String cquotes[] = { "'\"", "\"'", "'" };

    /** the software release w.r.t. which the configuration is created */
    private SoftwareRelease release = null;
    
    /** the root node of the parsed tree */
    private ParseNode rootNode = null;
    
    /** set with all problem components */
    private HashSet<String> problemModules = new HashSet<String>();
    
    /** set with all problem parameter names */
    private HashSet<String> problemParameters = new HashSet<String>();
    
    /** output stream for problems.txt */
    private PrintWriter problemStream = null;
    
    
    //
    // construction
    //

    /** standard constructor */
    public PythonParser(SoftwareRelease release)
    {
	this.release = release;
    }
    

    //
    // member functions
    //
    
    /** parse a file */
    public void parseFile(String fileName) throws ParserException
    {
	String name = fileName;
	while (name.indexOf("/")>=0) name=name.substring(name.indexOf("/")+1);
	if    (name.indexOf(".py")>0)name=name.substring(0,name.indexOf(".py"));
	
	rootNode = new ParseNode(null);
	rootNode.setContent("'" + name + "'");
	
	ParseNode currentNode = rootNode;
	Scanner   fileScan    = null;

	Stack<String> openBrackets = new Stack<String>();
	int           makeNewLevel = 0;
	int           lineCount = 0;
	try {
	    fileScan=new Scanner(new BufferedReader(new FileReader(fileName)));
	    
	    while (fileScan.hasNextLine()) {

		lineCount++;

		String line  = fileScan.nextLine();
		int    index = line.indexOf("#");
		if (index>=0) line = line.substring(0,index);
		
		Scanner lineScan = new Scanner(line);
		lineScan.useDelimiter("[\\s*]+");
		
		while (lineScan.hasNext()) {
		    
		    String tok = lineScan.next();
		    
		    while (tok.length()>0) {
			
			String subtok = getSubToken(tok);
			tok = tok.substring(subtok.length());

			if (subtok.equals(",")) continue;
			
			if (isOpeningBracket(subtok)>=0) {
			    
			    if (!openBrackets.empty()&&
				openBrackets.peek().equals(":")) {
				openBrackets.pop();
				makeNewLevel--;
			    }
			    makeNewLevel++;
			    openBrackets.push(subtok);
			}
			else if (isClosingBracket(subtok)>=0) {
			    
			    if (openBrackets.empty())
				throw new ParserException(lineCount,
							  "Bracket Mismatch: "+
							  "no open bracket for "
							  +subtok);
			    
			    String openBracket = openBrackets.peek();
			    if(isOpeningBracket(openBracket)!=
			       isClosingBracket(subtok))
				throw new ParserException(lineCount,
							  "Bracket Mismatch"+
							  openBrackets.peek()+
							  subtok);
			    
			    openBrackets.pop();
			    
			    if (makeNewLevel==0)
				currentNode = currentNode.parent();
			    else
				makeNewLevel--;
			    
			    if (currentNode.unquoted().equals("PSet")&&
				currentNode.parent().parent().child(0)
				.unquoted().equals("VPSet")) {
				currentNode = currentNode.parent();
				makeNewLevel++;
			    }

			}
			else {
			    if (makeNewLevel==2) {
				
				if (!currentNode.parent().child(0)
				    .unquoted().equals("VPSet"))
				    throw new ParserException(lineCount,
							      "VPSet expected");
				
				makeNewLevel--;
				currentNode = currentNode.addNode();
				currentNode.setContent("'PSet'");
			    }

			    if (makeNewLevel==1) {
				makeNewLevel--;
				currentNode = currentNode.addNode();
			    }
			    else if (currentNode.isComplete()) {
				currentNode = currentNode.parent().addNode();
			    }
			    currentNode.addContent(subtok);
			    
			    if (currentNode.isComplete()&&
				openBrackets.peek().equals(":")) {
				openBrackets.pop();
				currentNode = currentNode.parent();
			    }
			}
		    } // token
		} // lineScan
	    } // fileScan
	    
	    if (currentNode!=rootNode)
		throw new ParserException(fileName + " parsed, " +
					  "but currentNode!=rootNode");
	    if (!openBrackets.empty())
		throw new ParserException(fileName + " parsed, " +
					  "but open brackets left!");
	}
	catch (IOException e) {
	    System.err.println("Error opening file " + fileName + ": " +
			       e.getMessage());
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	finally {
	    if (fileScan!=null) fileScan.close();
	}
    }
    
    /** turn parsed tree into configuration */
    public Configuration createConfiguration() throws ParserException
    {
	if (release==null||rootNode==null) return null;
	
	// create configuration
	String name = rootNode.unquoted();
	String releaseTag = release.releaseTag();
	String process = null;
	for (int i=0;i<rootNode.childCount();i++) {
	    if (rootNode.child(i).unquoted().equals("procname")&&
		rootNode.child(i).childCount()>0) {
		process = rootNode.child(i).child(0).unquoted();
		break;
	    }
	}
	if (process==null) throw new ParserException("createConfiguration: "+
						     "can't find process name");
	
	Configuration config = new Configuration();
	config.initialize(new ConfigInfo(name,null,releaseTag),process,release);
	
	// add global psets
	ParseNode psetNode = getChildNode(rootNode,"psets");
	for (int i=0;i<psetNode.childCount();i++) {
	    PSetParameter pset = (PSetParameter)getParameter(psetNode.child(i));
	    config.insertPSet(pset);
	}
	
	// add primary data source (edsource)
	ParseNode edsourceNode = getChildNode(rootNode,"main_input");
	String    edsourceName = getNodeValue(edsourceNode,"@classname");
	EDSourceInstance edsource = config.insertEDSource(edsourceName);
	ArrayList<Parameter> edsourceParams = getParameters(edsourceNode);
	for (Parameter p : edsourceParams) {
	    if (edsource==null||
		!edsource.updateParameter(p.name(),p.type(),p.valueAsString()))
		addProblem("EDSource",edsourceName,p);
	}
	
	// add essources
	ParseNode essourcesNode = getChildNode(rootNode,"es_sources");
	for (int i=0;i<essourcesNode.childCount();i++) {
	    ParseNode essourceNode = essourcesNode.child(i);
	    String    essourceName = getNodeValue(essourceNode,"@classname");
	    String    essourceLabel = getNodeValue(essourceNode,"@label");
	    if (essourceLabel.equals(new String())) essourceLabel=essourceName;
	    ESSourceInstance essource =
		config.insertESSource(config.essourceCount(),
				      essourceName,essourceLabel);
	    ArrayList<Parameter> essourceParams = getParameters(essourceNode);
	    for (Parameter p : essourceParams) {
		if (essource==null||
		    !essource.updateParameter(p.name(),p.type(),
					      p.valueAsString()))
		    addProblem("ESSource",essourceName,p);
	    }
	}
	
	// add esmodules
	ParseNode esmodulesNode = getChildNode(rootNode,"es_modules");
	for (int i=0;i<esmodulesNode.childCount();i++) {
	    ParseNode esmoduleNode  = esmodulesNode.child(i);
	    String    esmoduleName  = getNodeValue(esmoduleNode,"@classname");
	    String    esmoduleLabel = getNodeValue(esmoduleNode,"@label");
	    if (esmoduleLabel.equals(new String())) esmoduleLabel=esmoduleName;
	    ESModuleInstance esmodule =
		config.insertESModule(config.esmoduleCount(),
				      esmoduleName,esmoduleLabel);
	    ArrayList<Parameter> esmoduleParams = getParameters(esmoduleNode);
	    for (Parameter p : esmoduleParams) {
		if (esmodule==null||
		    !esmodule.updateParameter(p.name(),p.type(),
					      p.valueAsString()))
		    addProblem("ESModule",esmoduleName,p);
	    }
	}
	
	// add services
	ParseNode servicesNode = getChildNode(rootNode,"services");
	for (int i=0;i<servicesNode.childCount();i++) {
	    ParseNode serviceNode  = servicesNode.child(i);
	    String    serviceName  = getNodeValue(serviceNode,"@classname");
	    ServiceInstance service =
		config.insertService(config.serviceCount(),serviceName);
	    ArrayList<Parameter> serviceParams = getParameters(serviceNode);
	    for (Parameter p : serviceParams) {
		if (service==null||
		    !service.updateParameter(p.name(),p.type(),
					     p.valueAsString()))
		    addProblem("Service",serviceName,p);
	    }
	}
	
	// add sequences
	HashMap<String,Sequence> nameToSequenceMap =
	    new HashMap<String,Sequence>();
	HashMap<Sequence,ParseNode> sequenceToNodeMap =
	    new HashMap<Sequence,ParseNode>();
	ParseNode sequencesNode = getChildNode(rootNode,"sequences");
	for (int i=0;i<sequencesNode.childCount();i++) {
	    String   sequenceName = sequencesNode.child(i).unquoted();
	    Sequence sequence = config.insertSequence(config.sequenceCount(),
						      sequenceName);
	    nameToSequenceMap.put(sequence.name(),sequence);
	    sequenceToNodeMap.put(sequence,sequencesNode.child(i));
	}

	// add paths
	HashMap<String,Path> nameToPathMap =
	    new HashMap<String,Path>();
	HashMap<Path,ParseNode> pathToNodeMap =
	    new HashMap<Path,ParseNode>();
	ParseNode pathsNode = getChildNode(rootNode,"paths");
	for (int i=0;i<pathsNode.childCount();i++) {
	    String pathName = pathsNode.child(i).unquoted();
	    Path   path = config.insertPath(config.pathCount(),pathName);
	    nameToPathMap.put(path.name(),path);
	    pathToNodeMap.put(path,pathsNode.child(i));
	}
	
	// get modules ParseNode
	ParseNode modulesNode = getChildNode(rootNode,"modules");
	
	// fill sequences
	Iterator sequenceIt = sequenceToNodeMap.keySet().iterator();
	while (sequenceIt.hasNext()) {
	    Sequence  sequence     = (Sequence)sequenceIt.next();
	    ParseNode sequenceNode = (ParseNode)sequenceToNodeMap.get(sequence);
	    ArrayList<String> entries = parseContainerNode(sequenceNode);
	    
	    for (String entry : entries) {
		if (nameToSequenceMap.containsKey(entry)) {
		    Sequence s = (Sequence)nameToSequenceMap.get(entry);
		    config.insertSequenceReference(sequence,
						   sequence.entryCount(),s);
		}
		else {
		    ParseNode moduleNode=getChildNode(modulesNode,entry);
		    String    moduleName=getNodeValue(moduleNode,"@classname");
		    String    moduleLabel=entry;
		    if (moduleLabel.equals(new String()))moduleLabel=moduleName;

		    ModuleInstance  module = null;
		    ModuleReference reference =
			config.insertModuleReference(sequence,
						     sequence.entryCount(),
						     moduleName,
						     moduleLabel);
		    ArrayList<Parameter> moduleParams=getParameters(moduleNode);
		    int referenceCount = 0;
		    String moduleType = "UNKNOWN";
		    
		    if (reference!=null) {
			module         = (ModuleInstance)reference.parent();
			referenceCount = module.referenceCount();
			moduleType     = module.template().type();
		    }
		    
		    for (Parameter p : moduleParams) {
			if (module==null||
			    (referenceCount==1&&
			     !module.updateParameter(p.name(),p.type(),
						     p.valueAsString())))
			    addProblem("Module:"+moduleType,moduleName,p);
		    }
		}
	    }
	}
	

	// fill paths
	Iterator pathIt = pathToNodeMap.keySet().iterator();
	while (pathIt.hasNext()) {
	    Path      path     = (Path)pathIt.next();
	    ParseNode pathNode = (ParseNode)pathToNodeMap.get(path);
	    ArrayList<String> entries = parseContainerNode(pathNode);
	    
	    for (String entry : entries) {
		if (nameToPathMap.containsKey(entry)) {
		    Path p = (Path)nameToPathMap.get(entry);
		    config.insertPathReference(path,path.entryCount(),p);
		}
		else if (nameToSequenceMap.containsKey(entry)) {
		    Sequence s = (Sequence)nameToSequenceMap.get(entry);
		    config.insertSequenceReference(path,path.entryCount(),s);
		}
		else {
		    ParseNode moduleNode=getChildNode(modulesNode,entry);
		    String    moduleName=getNodeValue(moduleNode,"@classname");
		    String    moduleLabel=entry;
		    if (moduleLabel.equals(new String()))moduleLabel=moduleName;
		    
		    ModuleInstance  module = null;
		    ModuleReference reference =
			config.insertModuleReference(path,
						     path.entryCount(),
						     moduleName,
						     moduleLabel);
		    ArrayList<Parameter> moduleParams=getParameters(moduleNode);
		    int referenceCount = 0;
		    String moduleType = "UNKNOWN";
		    
		    if (reference!=null) {
			module         = (ModuleInstance)reference.parent();
			referenceCount = module.referenceCount();
			moduleType     = module.template().type();
		    }
		    
		    for (Parameter p : moduleParams) {
			if (module==null||
			    (referenceCount==1&&
			     !module.updateParameter(p.name(),p.type(),
						     p.valueAsString())))
			    addProblem("Module:"+moduleType,moduleName,p);
		    }
		}
	    }

	    if (path.entryCount()<entries.size())
		System.out.println("WARNING: only "+path.entryCount()+"/"+
				   entries.size()+" entries found for path "+
				   path.name());

	}
	
	
	return config;
    }

    /** close the problem stream, if it is open */
    public boolean closeProblemStream()
    {
	if (problemStream==null) return false;
	problemStream.flush();
	problemStream.close();
	return true;
    }

    /** print the parsed tree */
    public void printParsedTree()
    {
	if (rootNode!=null) printParseNode(rootNode,0);
    }
    
    //
    // private member functions
    //
    
    /** get the next sub-token from the current token */
    private String getSubToken(String token)
    {
	int    index      = -1;
	String brackets[] = { ":", "{", "}", "[", "]", "(", ")" };
	
	// token starts with ','
	if (token.startsWith(",")) return new String(",");
	
	// token starts with an opening quote
	for (int i=0;i<oquotes.length;i++) {
	    if (token.startsWith(oquotes[i])) {
		token  = token.substring(oquotes[i].length());
		index  = token.indexOf(cquotes[i]);
		return (index>=0) ?
		    oquotes[i]+token.substring(0,index+cquotes[i].length()) : 
		    oquotes[i]+token;
	    }
	}
	
	// token starts with a bracket
	for (int i=0;i<brackets.length;i++) {
	    if (token.startsWith(brackets[i])) return brackets[i];
	}
	
	// a closing quote is in the token
	for (int i=0;i<cquotes.length;i++) {
	    if ((index=token.indexOf(cquotes[i]))>=0)
		return token.substring(0,index+cquotes[i].length());
	}
	
	return token;
    }

    /** is token an opening bracket? including ':'! */
    private int isOpeningBracket(String token)
    {
	if (token.length()>1) return -1;
	for (int i=0;i<obrackets.length;i++)
	    if (token.equals(obrackets[i])) return i;
	return -1;
    }
    
    /** is token a closing bracket? */
    private int isClosingBracket(String token)
    {
	if (token.length()>1) return -1;
	for (int i=0;i<cbrackets.length;i++)
	    if (token.equals(cbrackets[i])) return i;
	return -1;
    }

    /** get the ParseNode, child of rootNode with unquoted */
    private ParseNode getChildNode(ParseNode node,
				   String unquoted) throws ParserException
    {
	for (int i=0;i<node.childCount();i++)
	    if (node.child(i).unquoted().equals(unquoted))
		return node.child(i);
	throw new ParserException("ParseNode "+unquoted+" not found.");
    }
    
    /** get the node value: third child of unquoted child */
    private String getNodeType(ParseNode node,String unquoted)
	throws ParserException
    {
	ParseNode childNode = getChildNode(node,unquoted);
	if (childNode.childCount()<2)
	    throw new ParserException("getNodeType: unexpected childCount");
	return childNode.child(0).unquoted();
    }
    
    /** get the node value: third child of unquoted child */
    private String getNodeQualifier(ParseNode node,String unquoted)
	throws ParserException
    {
	ParseNode childNode = getChildNode(node,unquoted);
	if (childNode.childCount()<2)
	    throw new ParserException("getNodeQualifier: "+
				      "unexpected childCount");
	String qualifier = childNode.child(1).unquoted();
	if (!qualifier.equals("tracked")&&
	    !qualifier.equals("untracked"))
	    throw new ParserException("getNodeQualifier: must be [un]tracked");
	return qualifier;
    }
    
    /** get the node value: third child of unquoted child */
    private String getNodeValue(ParseNode node,String unquoted)
	throws ParserException
    {
	ParseNode childNode = getChildNode(node,unquoted);
	if (childNode.childCount()<3)
	    throw new ParserException("getClassName: unexpected childCount");
	return childNode.child(2).unquoted();
    }
    
    /** get the list of parameters, given the component node */
    private ArrayList<Parameter> getParameters(ParseNode node)
	throws ParserException
    {
	ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	for (int i=0;i<node.childCount();i++) {
	    if (node.child(i).unquoted().startsWith("@")) continue;
	    parameters.add(getParameter(node.child(i)));
	}
	return parameters;
    }
    
    /** get the parameter, given the parameter node */
    private Parameter getParameter(ParseNode paramNode)
	throws ParserException
    {
	String  name,type;
	boolean istracked,isdefault;

	if (paramNode.unquoted().equals("PSet")) {
	    name = "";
	    type = "PSet";
	    istracked = false;
	    isdefault = true;
	}
	else {

	    name = paramNode.unquoted();
	    type = paramNode.child(0).unquoted();
	    String qual = paramNode.child(1).unquoted();
	    istracked = qual.equals("tracked");
	    isdefault = true;
	}
	
	// TEMPORARY!
	if (type.equals("FileInPath")) type = "string";
	
	Parameter parameter =
	    ParameterFactory.create(type,name,"",istracked,isdefault);
	
	if (parameter instanceof PSetParameter) {
	    PSetParameter pset = (PSetParameter)parameter;

	    if (pset.name().length()>0) {
		for (int i=0;i<paramNode.child(1).childCount();i++)
		    pset.addParameter(getParameter(paramNode.child(1)
						   .child(i)));
	    }
	    else {
		for (int i=0;i<paramNode.childCount();i++)
		    pset.addParameter(getParameter(paramNode.child(i)));
	    }
	}
	else if (parameter instanceof VPSetParameter) {
	    VPSetParameter vpset = (VPSetParameter)parameter;
	    for (int i=0;i<paramNode.child(1).childCount();i++) {
		PSetParameter pset = (PSetParameter)getParameter(paramNode
								 .child(1)
								 .child(i));
		vpset.addParameterSet(pset);
	    }
	}
	else if (parameter instanceof VectorParameter) {
	    VectorParameter vparameter = (VectorParameter)parameter;
	    String  valueAsString = "";
	    for (int i=0;i<paramNode.child(1).childCount();i++) {
		String vas = paramNode.child(1).child(i).unquoted();
		valueAsString += vas +",";
	    }
	    if (valueAsString.length()>0)
		valueAsString=valueAsString.substring(0,
						      valueAsString.length()-1);
		vparameter.setValue(valueAsString);
	}
	else {
	    if (paramNode.childCount()<3)
		throw new ParserException("Missing parameter value");
	    
	    String valueAsString = paramNode.child(2).unquoted();
	    if (valueAsString.length()>0) parameter.setValue(valueAsString);
	}
	
	return parameter;
    }

    /** parse the content of a container (path or sequence) node */
    private ArrayList<String> parseContainerNode(ParseNode node)
	throws ParserException
    {
	ArrayList<String> entries = new ArrayList<String>();

	if (node.childCount()==1) {
	    StringBuffer entry = new StringBuffer();
	    int          count = 0;
	    Scanner s = new Scanner(node.child(0).unquoted());
	    s.useDelimiter("\\s*");
	    while (s.hasNext()) {
		String token = (String)s.next();
		if (token.equals("(")) {
		    count++;
		}
		else if (token.equals(")")) {
		    count--;
		    if (entry.length()>0) {
			entries.add(entry.toString());
			entry = new StringBuffer();
		    }
		}
		else if (token.equals(",")||token.equals("&")) {
		    if (entry.length()>0) {
			entries.add(entry.toString());
			entry = new StringBuffer();
		    }
		}
		else {
		    entry.append(token);
		}
	    }
	    if (count!=0) throw new ParserException("parseContainerNode: "+
						    "'()' mismatch.");
	    if (entry.length()>0) entries.add(entry.toString());
	}
	else if (node.childCount()>0)
	    throw new ParserException("parseContainerNode: "+
				      "unexpected childCount");
	
	return entries;
    }
    
    /** record a missing/mismatched parameter */
    private void addProblem(String type,String name,Parameter p)
    {
	if (problemParameters.contains(name + "." + p.name())) return;
	   
	if (problemStream==null) {
	    try {
		problemStream = new PrintWriter(new FileWriter("problems.txt"));
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
	    problemStream.println("\n" + s + " " + name);
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
	problemParameters.add(name + "." + p.name());
    }

    /** print the ParseNode and all child nodes */
    private void printParseNode(ParseNode node,int ntab)
    {
	String tab = "";
	for (int i=0;i<ntab;i++) tab+=" ";
	System.out.println(tab + node.unquoted());
	for (int i=0;i<node.childCount();i++)
	    printParseNode(node.child(i),ntab+2);
    }
    
    
    //
    // main (testing)
    //

    /** main method */
    public static void main(String[] args)
    {
	if (args.length!=1) {
	    System.out.println("Usage: java " +
			       PythonParser.class.getName() +
			       " <file.py>");
	    System.exit(1);
	}
	
	try {
	    // connect ot db and load release
	    SoftwareRelease release = new SoftwareRelease();
	    String releaseTag = "CMSSW_1_6_0";
	    System.out.println("connect to DB and load release " +
			       releaseTag + "...");
	    ConfDB database = new ConfDB();
	    database.connect("mysql","jdbc:mysql://localhost:3306/hltdb",
			     "schiefer","monopoles");
	    database.loadSoftwareRelease(releaseTag,release);
	    System.out.println("release " + releaseTag + " loaded!\n");

	    // parse the input Python file
	    String fileName = args[0];
	    PythonParser parser = new PythonParser(release);
	    parser.parseFile(fileName);
	    //parser.printParsedTree();

	    // create the configuration
	    Configuration config = parser.createConfiguration();
	    
	    // convert the configuration to ascii
	    ConverterFactory factory=ConverterFactory.getFactory("default");
	    Converter        cnv    = factory.getConverter("ascii");
	    System.out.println(cnv.convert(config));

	    if (parser.closeProblemStream())
		System.out.println("problems encountered, see problems.txt.");
	}
	catch (DatabaseException e) {
	    System.err.println("Failed to connect to DB: " + e.getMessage());
	}
	catch (ParserException e) {
	    System.err.println("Error parsing "+args[0]+": "+e.getMessage());
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
}

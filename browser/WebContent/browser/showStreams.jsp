<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="confdb.converter.BrowserConverter"%>
<%@page import="confdb.converter.streams.L1Seed"%>
<%@page import="confdb.data.ModifierInstructions"%>
<%@page import="confdb.converter.OnlineConverter"%>
<%@page import="confdb.data.IConfiguration"%>
<%@page import="confdb.converter.OfflineConverter"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.Iterator"%>
<%@page import="confdb.data.Stream"%>
<%@page import="confdb.data.PrimaryDataset"%>
<%@page import="confdb.data.Path"%>
<%@page import="confdb.data.ModuleInstance"%>
<%@page import="java.util.ArrayList"%>
<%@page import="confdb.data.ServiceInstance"%>
<%@page import="java.util.HashMap"%>
<%@page import="confdb.data.Parameter"%>
<%@page import="confdb.data.VPSetParameter"%>
<%@page import="confdb.data.PSetParameter"%>
<%@page import="confdb.data.VStringParameter"%>
<%@page import="confdb.data.StringParameter"%>
<%@page import="confdb.data.Reference"%>

<%@page import="confdb.data.VInt32Parameter"%>
<%@page import="confdb.data.Int32Parameter"%>
<%@page import="confdb.data.ModuleReference"%>
<%@page import="confdb.data.OutputModuleReference"%>
<%@page import="confdb.data.OutputModule"%>
<%@page import="confdb.data.VUInt32Parameter"%>

<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.SortedSet"%>
<%@page import="java.util.TreeSet"%>
<%@page import="confdb.converter.ConverterBase"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Streams </title>

<%
  String db = request.getParameter( "dbName" );
  String yui = "../js/yui";
  String css = "../css";
  String js = "../js";
  String img = "../img";
  boolean online = false;
  if ( request.getParameter( "online" ) != null )
  {
	online = true;
  	yui = "../../../gui/yui";
    css = "../../css";
    js = "../../js";
    img = "../../img";
  }
%>


<!-- css -->
<link rel="stylesheet" type="text/css" href="../css/smoothness/jquery-ui-1.8.6.custom.css" rel="stylesheet" />	
<link rel="stylesheet" type="text/css" href="../css/jquery.treeTable.css" />
<link rel="stylesheet" type="text/css" href="../css/confdb-jq.css" />

<!-- js -->
<script type="text/javascript" src="../js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="../js/jquery.cookie.js"></script>
<script type="text/javascript" src="../js/jquery.treeTable.js"></script>
<script type="text/javascript" src="../js/json2.js"></script>


<style>


.even {
	background: #ffffff;
}

.odd {
	background: #edf4f9;
}

.prescaleTH {
	font-weight: normal;
	padding-left: 1em;
}

.prescaleTD {
	padding-left: 1em;
}


.pathCell {
	cursor: pointer;
}

.pathCell:hover {
	text-decoration: underline;
}

/*
.seedCell {
	cursor: pointer;
}

.seedCell:hover {
	text-decoration: underline;
}
*/

.seedCell a:hover {
	text-decoration: underline;
}

.seedCell a {
	color: black;
	text-decoration: none;
}

body {
	padding-left:3px; 
	padding-right:3px; 
	padding-top:3px;
}

tbody {
border: 1px solid #aaaaaa;

}


</style>



<script type="text/javascript">

var COOKIE_NAME = 'createConfigParas';
var params = {};
var rows = 0;

function setCookie( cookie )
{
	var cookieStr = JSON.stringify( cookie );
	$.cookie( COOKIE_NAME, cookieStr );
//	alert( cookieStr );
}

function signalReady()
{
	  if ( parent &&  parent.iframeReady )
		    parent.iframeReady();
}

function showPath( path )
{
	if ( parent &&  parent.scrollTo )
		parent.scrollTo( path, "details" );
}

function showSeed( path )
{
	if ( parent &&  parent.scrollTo )
		parent.scrollTo( path, "details" );
}



$(function(){

	var cookieStr = $.cookie( COOKIE_NAME );
	if ( cookieStr != null )
	{
		params = eval('(' + cookieStr + ')');
		//$("#doc").hide();
	}
	else
		params = {  dbhost: "cmsr1-v.cern.ch", 
		    		dbname: "cms_cond.cern.ch",  
		    		dbuser: "cms_hltdev_writer", 
		    		master: "/dev/CMSSW_3_4_0/pre6/HLT", 
		    		target: "/dev/CMSSW_3_4_0/pre6", 
		    		tables: [ { name: "1E31" }, { name: "8E29" } ] };

	/*
	$( "#dbhost" ).val( params.dbhost );
	$( "#dbname" ).val( params.dbname );
	$( "#dbuser" ).val( params.dbuser );
	$( "#user" ).val( params.user );
	$( "#master" ).val( params.master );
	$( "#target" ).val( params.target );
	
    $("#submitButton").click( doIt ); 

    for ( var i in params.tables )
    {
        $("#tables").append( '<tr><td><input type="text" size="20" value="' 
                + params.tables[i].name + '" name="table' + i + '"></td>'
                + '<td><input type="file" name="file' + i + '" size="50" maxlength="100000" accept="text/*"></td>'
                + '<td><input type="checkbox" name="checked' + i + '" checked value="on"></input></td></tr>' );
    }
	rows = params.tables;
	*/

    $("#main").treeTable( { clickableNodeNames: true } );
    $(".pathCell").click( function () {
    	showPath( $(this).html() );
    } );

    /*
    $(".seedCell").click( function () {
    	showSeed( $(this).html() );
    } );
    */
    signalReady();
});

</script>


</head>
<body>
<%!
int verbose = 0;
int columns = 0;
String[] columnName = {};
String prescalerType = "";
HashMap<String,int[]> prescale = null;


class VisitedPath
{
	Path path;
	
	VisitedPath( Path path )
	{
		this.path = path;	
	}
	
	
	void visit( SearchModule visitor )
	{
		Iterator<Reference> it = path.entryIterator();
		while ( it.hasNext() )
			visitor.enter( it.next() );
	}
	
}



// search a path for a single module with a certain name

/*
class SearchModuleByName(object):
  def __init__(self, target, barrier = None):
    self.target  = target
    self.barrier = barrier
    self.found   = [ ]
    self.stop    = False

  def enter(self, node):
    if self.stop:
      return

    if isinstance(node, cms._Module):
      if node.label_() == self.barrier:
        self.stop = True
        return
      if node.label_() == self.target:
        self.found.append(node)
    
  def leave(self, node):
    pass
*/

abstract class SearchModule
{
	  String target;
	  ArrayList<Reference> found = null;

	  SearchModule( String target )
	  {
		  this.target = target;
	  }
	 	  
	  abstract void enter( Reference node );
}

class SearchModuleByName extends SearchModule
{
	  SearchModuleByName( String target )
	  {
		  super( target );
	  }
	 	  
	  void enter( Reference node )
	  {
		  if (    node instanceof OutputModuleReference 
				   || node instanceof ModuleReference )
		  {
			  if ( node.name().equals( target ) )
			  {
				  if ( found == null )
					  found = new ArrayList<Reference>();
			  	found.add( node );
		  	  }
		  }
		  else if (verbose > 0 )
			  System.out.println( node.getClass() + " " + node  + " != module" );
	  }
	  
}

/*
  # search a path for a single module of a certain type
  class SearchModuleByType(object):
    def __init__(self, target, barrier = None):
      self.target  = target
      self.barrier = barrier
      self.found   = [ ]
      self.stop    = False

    def enter(self, node):
      if self.stop:
        return

      if isinstance(node, cms._Module):
        if node.label_() == self.barrier:
          self.stop = True
          return
        if node.type_() == self.target:
          self.found.append(node)
      
    def leave(self, node):
      pass
*/

class SearchModuleByType extends SearchModule
{
	  SearchModuleByType( String target )
	  {
		  super( target );
	  }
	 	  
	  void enter( Reference node )
	  {
		  if (  node instanceof ModuleReference )
		  {
			  ModuleInstance module = (ModuleInstance)node.parent();
			  if ( module.template().name().equals( target ) )
			  {
				  if ( found == null )
					  found = new ArrayList<Reference>();
			  	found.add( node );
			  	//System.out.println( module.name() + " instanceof " + target );
		  	  }
		  }
		  else if (  !(node instanceof OutputModuleReference)  &&  verbose > 0 )
			  System.out.println( node.getClass() + " " + node  + " != module" );
	  }
	  
}

/*
    # search a path for a "dumb" prescaler
    class SearchDumbPrescale(SearchModuleByType):
      def __init__(self, barrier = None):
        super(SearchDumbPrescale, self).__init__('HLTPrescaler', barrier)
*/

class SearchDumbPrescale extends SearchModuleByType
{
	SearchDumbPrescale()
	{
		super( "HLTPrescaler" );
	}
}


/*
      # search a path for a "smart" prescaler
      class SearchSmartPrescale(SearchModuleByType):
        def __init__(self, barrier = None):
          super(SearchSmartPrescale, self).__init__('HLTHighLevelDev', barrier)
*/
class SearchSmartPrescale extends SearchModuleByType
{
	SearchSmartPrescale()
	{
		super( "HLTHighLevelDev" );
	}
}





/*
      # search a path for a "smart" prescaler
      class SearchNewSmartPrescale(SearchModuleByType):
        def __init__(self, barrier = None):
          super(SearchNewSmartPrescale, self).__init__('TriggerResultsFilter', barrier)
*/
class SearchNewSmartPrescale extends SearchModuleByType
{
	SearchNewSmartPrescale()
	{
		super( "TriggerResultsFilter" );
	}
}

          

      


/*
def getEndPath(output):
  # look for the EndPath with the corresponding output module
  out = ''
  for o in process.endpaths.itervalues():
    searchOut = SearchModuleByName(output)
    o.visit(searchOut)
    if searchOut.found:
      out = o.label_()
      break
  else:
    print "    *** corresponding EndPath not found ***"
  return out
*/

Path getEndPath( String output, IConfiguration conf )
{
	Iterator<Path> it = conf.pathIterator();
	while ( it.hasNext() )
	{
		Path path = it.next();
		if ( path.isEndPath() )
		{
			SearchModuleByName searchOut = new SearchModuleByName( output );
			( new VisitedPath( path ) ).visit( searchOut );	
			if ( searchOut.found != null )
			{
				return path;
			}
		}
	}
	if ( verbose > 0 )
		System.out.println( "endpath for " + output + " not found" );
	return null;
}

// get a tuple with the prescale factors for a path in a given endpath

//  def getPrescales(name, out, end):
int[] getPrescales( String name, String out, Path endp ) throws NumberFormatException
{
	/*
    # look for a gobal prescale for the given path
    if name in prescale:
      pre = prescale[name]
    else:
      pre = [1] * columns
    */
    
    int[] pre = null;
    if ( prescale.get( name ) != null )
    {
		pre = prescale.get( name ).clone();
		prescalerType = "g";
    }
    else
    {
    	pre = new int[ columns ];
		for ( int i = 0; i < pre.length; i++ )
			pre[i] = 1;
    }
    
    
	/*
    # check for a valid EndPath
    if out and end:
        endp = process.endpaths[end]
    */    
    if ( out != null  && endp != null )
    {
	  /*
      # look for a local dumb prescaler in the output path
      dumb = SearchDumbPrescale(out)
      endp.visit(dumb)
      if dumb.found and end in prescale:
        pre = map(operator.mul, pre, prescale[end])
      */
      SearchDumbPrescale dumb = new SearchDumbPrescale();
	  ( new VisitedPath( endp ) ).visit( dumb );
	  if ( dumb.found != null  &&  prescale.get( endp.name() ) != null )
	  {
		  int[] factor = prescale.get( endp.name() );
		  for ( int i = 0; i < pre.length; i++ )
			  pre[i] = pre[i] * factor[i];
		  prescalerType += "d";
	  }
      
      
	  /*
      # look for an old-style local smart prescaler in the output path
      smart = SearchSmartPrescale(out)
      endp.visit(smart)
      # FIXME wildcards are not supported yet
      for found in smart.found:
        if name in found.HLTPaths.value():
          index = found.HLTPaths.value().index(name)
          scale = found.HLTPathsPrescales.value()[index] * found.HLTOverallPrescale.value()
          pre = [ scale * p for p in pre ]
        else:
          pre = [ 0 ] * columns
      */
      SearchModule smart = new SearchSmartPrescale();
      ( new VisitedPath( endp ) ).visit( smart );
      if ( smart.found != null )
      {
    	  for ( Reference found : smart.found )
    	  {
      		//System.out.println( "smart prescaler " + found + " found for " + endp.name() );
			ModuleInstance module = (ModuleInstance)found.parent();
			Parameter p = module.parameter( "HLTPaths" );
			if ( p instanceof VStringParameter )
			{
				int index = -1;
				VStringParameter vstring = (VStringParameter)p;
				for ( int i = 0; i < vstring.vectorSize(); i++ )
				{
					if ( vstring.value(i).equals( name ) )
					{
						index = i;
						break;
					}
				}
				if ( index >= 0 )
				{
					int scale1 = 0;
					int scale2 = 0;
					p = module.parameter( "HLTPathsPrescales" );
					if ( p instanceof VUInt32Parameter )
					{
						VUInt32Parameter vint = (VUInt32Parameter)p;
						scale1 = Integer.parseInt( vint.value(index).toString() );
					}
					else if ( verbose > 0 )
						System.out.println( "HLTPathsPrescales not found " + p.getClass() );
					scale2 = Integer.parseInt( module.parameter( "HLTOverallPrescale" ).valueAsString() );
					for ( int i = 0; i < pre.length; i++ )
						pre[i] *= scale1 * scale2;
					prescalerType += "s";
//					if ( scale1 != 0 )
//						System.out.println( "smart prescaler " + name + ": " + scale1 + "(" + scale2 + ")" );
				}
				else
				{
					for ( int i = 0; i < pre.length; i++ )
						pre[i] = 0;					
				}				
			}
    	  }
      }
      

      /*
      # look for a new-style local smart prescaler in the output path
      smart = SearchNewSmartPrescale(out)
      endp.visit(smart)
      # FIXME wildcards are not supported yet
      # FIXME arbitrary expressions are not supported yet, only "HLT_Xxx" and "HLT_Xxx / N"
      */
      //match_pre = re.compile(r'%s\s*/\s*(\d+)' % name)
      /*
      for found in smart.found:
        scale = 0
        for condition in found.triggerConditions.value():
          if name == condition:
            scale = 1
          elif match_pre.match(condition):
            scale = int(match_pre.match(condition).groups()[0])
        # apply the smart prescale to all columns 
        pre = [ scale * p for p in pre ]
	  */
      smart = new SearchNewSmartPrescale();
      ( new VisitedPath( endp ) ).visit( smart );
      Pattern match_pre = Pattern.compile( name + "\\s*/\\s*(\\d+)" ); 
      if ( smart.found != null )
      {
    	  for ( Reference found : smart.found )
    	  {
			int scale = 0;
			ModuleInstance module = (ModuleInstance)found.parent();
			Parameter p = module.parameter( "triggerConditions" );
			if ( p instanceof VStringParameter )
			{
				int index = -1;
				VStringParameter vstring = (VStringParameter)p;
				for ( int i = 0; i < vstring.vectorSize(); i++ )
				{
					if ( vstring.value(i).equals( name ) )
						scale = 1;
					else
					{
						Matcher m = match_pre.matcher( vstring.value(i).toString() );
						if ( m.matches() )
						{
				        	//System.out.println( "new smart prescaler " + vstring.value(i).toString() );
							scale = Integer.parseInt( m.group(1) );
						}
					}
				}
				for ( int i = 0; i < pre.length; i++ )
					pre[i] *= scale;
				prescalerType += "n";
//				if ( scale != 0  && scale != 1 )
//						System.out.println( "new smart prescaler " + name + ": " + scale );
			}
    	  }
      }
      

    }
	return pre;
}
	
/*
# get the prescale factors for a path in a given endpath
def getPrescalesDescription(name, out, end):
  pre = getPrescales(name, out, end)
  return ''.join(['  %6d' % p for p in pre])
*/
String getPrescalesDescription( String name, String out, Path endp ) throws NumberFormatException
{
	prescalerType = "";
	int[] pre = getPrescales( name, out, endp );
	
	StringBuffer str = new StringBuffer();
	for ( int p : pre )
		str.append( "<td align='right' class='prescaleTD'>" + p + "</td>" );
	return str.toString();	
}



private void initPrescalerStuff( IConfiguration conf )
{
	prescale = new HashMap<String,int[]>();
	if ( verbose > 0 )
	{
		System.out.println();
		System.out.println( "----------------------------------------------------------" );
		System.out.println();
	}
	columns = 1;
	prescalerType = "";
	ServiceInstance service = conf.service( "PrescaleService" );
	if ( service == null )
	{
		if ( verbose > 0 )
			System.out.println( "no PrescaleService" );
		return;
	}

	Parameter p = service.parameter( "lvl1Labels" );
	if ( p instanceof VStringParameter )
	{
		VStringParameter vstring = (VStringParameter)p;
		columns = vstring.vectorSize();
		columnName = new String[ columns ];
		for ( int i = 0; i < columns; i++ )
			columnName[i] = vstring.value(i).toString();
	}
	else if ( verbose > 0 )
		System.out.println( "lvl1Labels not found " + p.getClass() );
	if ( verbose > 0 )
		System.out.println( columns + " columns" );
	  
	Parameter table = service.parameter( "prescaleTable" );
	if ( !(table instanceof VPSetParameter) )
	{
		if ( verbose > 0 )
			System.out.println( "no prescaleTable" );
		return;
	}
	VPSetParameter set = (VPSetParameter)table;
	for ( int i = 0 ; i < set.parameterSetCount(); i++ )
	{
		PSetParameter pset = set.parameterSet(i);
		Parameter name = pset.parameter( "pathName" );
		if ( name instanceof StringParameter  &&  pset.parameter( "prescales" ) != null )
		{
			Object o = pset.parameter( "prescales" );
			if ( o instanceof VUInt32Parameter )
			{
				VUInt32Parameter vint = (VUInt32Parameter)o;
				int[] v = new int[ vint.vectorSize() ];
				for ( int ii = 0; ii < v.length; ii++ )
					v[ii] = ((Long)vint.value(ii)).intValue();
				prescale.put( ((StringParameter)name).value().toString(), v );
//				System.out.println( name.value().toString() + ": " +
//						  p.parameter( "prescales" ).valueAsString() );
			}
		}
	}
}

public String getL1Seed( Path path )
{
	ModuleInstance l1Module = null;
	ArrayList<ModuleInstance> filters = new ArrayList<ModuleInstance>();
	Iterator<ModuleInstance> moduleList = path.moduleIterator();
	int seedCounter = 0;
	String allSeeds = new String();
	while ( moduleList.hasNext() ) 
	{
    	ModuleInstance module = moduleList.next();
    	String seed = L1Seed.getL1Seed( module );
    	if ( seed != null )
    	{
    		if ( ++seedCounter > 1 )
    			allSeeds += ") <br> AND ("; 
			allSeeds += "<a href=javascript:showSeed(\"" + module.name() + "\")>" + (L1Seed.isL1TechnicalTriggerSeed( module ) ? "technical bits: " : "" ) + seed + "</a>";
    	}
	}
	if ( seedCounter == 0 )
		return "-";
	if ( seedCounter == 1 )
		return allSeeds;
	
	return "(" + allSeeds + ")";
}

SortedSet<String> getSelectEvents( String hltOut, IConfiguration conf )
{
	TreeSet<String> set = new TreeSet<String>();
	OutputModule module = conf.output( hltOut );
	if ( module != null )
	{
		Parameter p = module.findParameter( "SelectEvents" );
		if ( p instanceof PSetParameter )
		{
			PSetParameter pset = (PSetParameter)p;
			p = pset.parameter( "SelectEvents" );
			if ( p instanceof VStringParameter )
			{
				VStringParameter v = (VStringParameter)p;
				for ( int i = 0; i < v.vectorSize(); i++ )
					set.add( v.value(i).toString() );
			}
		}
	}
	return set;
}


void verbose1( String message )
{
	if ( verbose > 0 )
		System.out.println(  message );
}

%>

<%
	BrowserConverter.UrlParameter paras = 
		BrowserConverter.getUrlParameter( request.getParameterMap() );

	paras.format = "summary.html";
	String result = "";
	ConverterBase converter = null;
	IConfiguration conf = null;	
	try {

		ModifierInstructions modifierInstructions = new ModifierInstructions();
	    //modifierInstructions.interpretArgs( paras.toModifier );
        if ( paras.runNumber != -1 )
        {
        	paras.dbName = "ORCOFF";
        	paras.configName = null;
	        BrowserConverter c = BrowserConverter.getConverter( paras.dbName );
        	paras.configId = c.getKeyFromRunSummary( paras.runNumber );
        	if ( paras.configId <= 0 )
        	{
        		out.println( "ERROR: no config found for runNumber " + paras.runNumber + "!<br>" );
        		return;
        	}
        	converter = c;
        }

	    if ( paras.dbName.equals( "online" ) )
	    	converter = OnlineConverter.getConverter();
	    else
	        converter = BrowserConverter.getConverter( paras.dbName );
        if ( paras.configName != null )
			paras.configId = converter.getDatabase().getConfigId( paras.configName );
        conf = converter.getConfiguration( paras.configId );
        //OfflineConverter helper = new OfflineConverter( "HTML" );
	    //result = helper.getConfigString( conf, paras.format,
		//				      modifierInstructions,
		//				      paras.asFragment);
	} catch (Exception e) {
    	Throwable cause = e.getCause(); 
	    out.print(e.getMessage()+"\n"); 
	    if ( cause != null )
		    out.print( "cause: " + cause.getMessage() + "\n\n");
	    else
	    	out.print( "\n" );
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    PrintWriter writer = new PrintWriter(buffer);
	    e.printStackTrace(writer);
	    writer.close();
	    out.println(buffer.toString());
	    if ( converter != null )
	        BrowserConverter.deleteConverter( converter );
	    return;
	}
%>
<center>

<table class='ui-widget' id="main" rules="groups" border="1" style="padding-left:10px" width="100%">
<thead class='ui-widget-header'>
<%
	try {
		if ( request.getParameter( "verbose" ) != null )
			verbose = Integer.parseInt( request.getParameter( "verbose" ) );
		initPrescalerStuff( conf );
	
		// header
		String rowspan = columns > 1 ? "rowspan='2'" : "";
		out.println( "<tr><th align='left' " + rowspan + ">Stream</th><th align='left' " + rowspan + ">Primary Dataset</th><th align='left' " + rowspan + ">HLT path</th>"
			+ "<th " + ( columns > 1 ? ("colspan=" + columns) : "align='right'" ) + ">Prescaler</th>"
		    + "<th " + rowspan + " style='min-width:3em'></th><th align='left' " + rowspan + ">L1 seed</th></tr>" );
		if ( columns > 1 )
		{
			out.println( "<tr>" );
			for ( String name : columnName )
				out.println( "<th class='prescaleTH'>" + name + "</th>" );
			out.println( "</tr>" );
		}
		out.println( "</thead><tbody class='ui-widget'>" );

		// body
		String emptyTDs = "<td></td><td></td><td></td><td></td>";
		for ( int i = 1; i < columns; i++ )
			emptyTDs += "<td></td>";

		Iterator<Stream> it = conf.streamIterator();
		while ( it.hasNext() )
		{
			Stream stream = it.next();
			String hltOut = "hltOutput" + stream.name();
	    	Path endp = getEndPath( hltOut, conf );
	    	
	    	SortedSet<String> unassigned = getSelectEvents( hltOut, conf );
	    	
			out.println( "<tr id='s-" + stream.name() + "'><td class='treeColumn'>" + stream.name() + "</td><td></td>" + emptyTDs + "</tr>" );
			Iterator<PrimaryDataset> datasets = stream.datasetIterator();
			while ( datasets.hasNext() )
			{
				PrimaryDataset dataset = datasets.next();
				out.println( "<tr id='pd-" + dataset.name() + "' class='child-of-s-" + stream.name() + "'><td></td><td  class='treeColumn' >" + dataset.name() + "</td>" + emptyTDs + "</tr>" );
				Iterator<Path> paths = dataset.pathIterator();
				while ( paths.hasNext() )
				{
					Path path = paths.next();
					out.println( "<tr id='p-" + path.name() + "' class='child-of-pd-" + dataset.name() + "'>" 
							+ "<td></td><td></td>" 
							+ "<td class='pathCell'>" + path.name() + "</td>" 
							+ getPrescalesDescription( path.name(), hltOut, endp )
							+ "<td align='center'>" + ( verbose > 0 ? prescalerType : "" ) + "</td>" 
							+ "<td class='seedCell'>" + getL1Seed(path) + "</td>" 
							+ "</tr>" );
					unassigned.remove( path.name() );
				}
			}
			if ( !unassigned.isEmpty() )
			{
				out.println( "<tr id='pd-unassigned-" + stream.name() + "' class='child-of-s-" + stream.name() + "'><td></td><td  class='treeColumn' >unassigned</td>" + emptyTDs + "</tr>" );
				for ( String pathName : unassigned )
				{
					Path path = conf.path( pathName );
					out.println( "<tr id='p-" + path.name() + "' class='child-of-pd-unassigned-" + stream.name() + "'>" 
							+ "<td></td><td></td>" 
							+ "<td>" + path.name() + "</td>" 
							+ getPrescalesDescription( path.name(), hltOut, endp ) 
							+ "<td align='center'>" + ( verbose > 0  &&  !prescalerType.equals("g") ? prescalerType : "" ) + "</td>" 
							+ "<td>" + getL1Seed(path) + "</td>" 
							+ "</tr>" );
				}
			}
		}
	} catch (Exception e) {
    	Throwable cause = e.getCause(); 
	    out.print(e.getMessage()+"\n"); 
	    if ( cause != null )
		    out.print( "cause: " + cause.getMessage() + "\n\n");
	    else
	    	out.print( "\n" );
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    PrintWriter writer = new PrintWriter(buffer);
	    e.printStackTrace(writer);
	    writer.close();
	    out.println(buffer.toString());
	}
%>

</tbody>
</table>
</center>

</body>
</html>
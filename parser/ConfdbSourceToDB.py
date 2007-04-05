#!/usr/local/bin/python2.4

# ConfdbSourceToDB.py
# Main file for parsing source code in a CMSSW release and
# loading module templates to the Conf DB.
# 
# Jonathan Hollar LLNL Feb. 22, 2007

import os, string, sys, posix, tokenize, array, getopt
import ConfdbSourceParser
import MySQLdb, ConfdbSQLModuleLoader, ConfdbOracleModuleLoader

def main(argv):
    # Get information from the environment
    input_base_path = os.environ.get("CMSSW_RELEASE_BASE")
    input_cmsswrel = os.environ.get("CMSSW_VERSION")

    # User can provide a list of packages to ignore...
    input_usingblacklist = False
    input_blacklist = []

    # or a list of packages (and only these packages) to use
    input_usingwhitelist = False
    input_whitelist = []

    input_verbose = 0
    input_dbname = "hltdb1"
    input_dbuser = "jjhollar"
    input_dbpwd = "password"
    input_dbtype = "MySQL"
    input_host = "localhost"

    # Parse command line options
    opts, args = getopt.getopt(sys.argv[1:], "r:p:b:w:c:v:d:u:s:t:o:h", ["release=","sourcepath=","blacklist=","whitelist=","releasename=","verbose=","dbname=","user=","password=","dbtype=","hostname=","help="])
    for o, a in opts:
	if o in ("-r","release="):
	    if(input_base_path and input_cmsswrel):
		input_base_path = input_base_path.replace(input_cmsswrel,str(a))
		input_cmsswrel = str(a)
		print "Using release " + input_cmsswrel + " at path " + input_base_path
	    else:
		print "Could not resolve path to the release " + str(a)
		print "Check that the CMSSW_RELEASE_BASE and CMSSW_VERSION envvars are set"
		return
        if o in ("-p","releasepath="):
            input_base_path = str(a)
	if o in ("-c","releasename="):
	    input_cmsswrel = str(a)
	    print "Using release " + input_cmsswrel
        if o in ("-b","blacklist="):
	    input_blacklist.append(a.split(","))
            print 'Skip directories:'
	    input_usingblacklist = True
        if o in ("-w","whitelist="):
	    input_whitelist.append(a.split(","))
            print 'Use directories:'
	    print input_whitelist
	    input_usingwhitelist = True	    
	if o in ("-v","verbose="):
	    input_verbose = int(a)
	    print "Verbosity = " + str(input_verbose)
	if o in ("-d","dbname="):
	    input_dbname = str(a)
	    print "Using DB named " + input_dbname
	if o in ("-u","user="):
	    input_dbuser = str(a)
	    print "Connecting as user " + input_dbuser
	if o in ("-s","password="):
	    input_dbpwd = str(a)
	    print "Use DB password " + input_dbpwd
	if o in ("-o","hostname="):
	    input_host = str(a)
	    print "Use hostname " + input_host
	if o in ("-t","dbtype="):
	    input_dbtype = str(a)
	    if(input_dbtype == "MySQL"):
		print "Using MySQL DB"
	    elif(input_dbtype == "Oracle"):
		print "Using Oracle DB"
		print "\t***Oracle is not tested yet. Exiting now***"
		return
	    else:
		print "Unknown DB type " + input_dbtype + ", exiting now"
		return
	if o in ("-h","help="):
	    print "Help menu for ConfdbSourceToDB"
	    print "\t-r <CMSSW release (default is the CMSSW_VERSION envvar)>"
	    print "\t-p <Absolute path to the release>"
	    print "\t-c <Manually set the name of the release>"
	    print "\t-w <Comma-delimited list of packages to parse>"
	    print "\t-b <Comma-delimited list of packages to ignore>"
	    print "\t-v <Verbosity level (0-3)>"
	    print "\t-d <Name of the database to connect to>"
	    print "\t-u <User name to connect as>" 
	    print "\t-s <Database password>"
	    print "\t-o <Hostname>"
	    print "\t-t <Type of database. Options are MySQL (default) or Oracle (not yet implemented)>"
	    print "\t-h Print this help menu"
	    return

    if((not input_base_path) or (not input_cmsswrel)):
	print "Configuration error: Could not resolve path to the release"
	print "\tEither the CMSSW_RELEASE_BASE and CMSSW_VERSION envvars must be set, or use the -p and -c options to explicitly set the path"
	print "\tExiting now"
	return

    print "Using release base: " + input_base_path

    confdbjob = ConfdbSourceToDB(input_cmsswrel,input_base_path,input_whitelist,input_blacklist,input_usingwhitelist,input_usingblacklist,input_verbose,input_dbname,input_dbuser,input_dbtype,input_dbpwd,input_host)
    confdbjob.BeginJob()

class ConfdbSourceToDB:
    def __init__(self,clirel,clibasepath,cliwhitelist,cliblacklist,cliusingwhitelist,cliusingblacklist,cliverbose,clidbname,clidbuser,clidbtype,clidbpwd,clihost):
	self.data = []
	self.dbname = clidbname
	self.dbuser = clidbuser
	self.dbtype = clidbtype
	self.verbose = int(cliverbose)
	self.dbpwd = clidbpwd
	self.dbhost = clihost

	# Get a Conf DB connection. Only need to do this once at the 
	# beginning of a job.
	if(self.dbtype == "MySQL"):
	    self.dbloader = ConfdbSQLModuleLoader.ConfdbMySQLModuleLoader(self.verbose)
	    self.dbcursor = self.dbloader.ConfdbMySQLConnect(self.dbname,self.dbuser,self.dbpwd,self.dbhost)
	elif(self.dbtype == "Oracle"):
	    self.dbloader = ConfdbOracleModuleLoader.ConfdbOracleModuleLoader(self.verbose)
	    self.dbcursor = self.dbloader.ConfdbOracleConnect(self.dbname,self.dbuser,self.dbpwd,self.dbhost)

	# Deal with package tags for this release.
	self.tagtuple = []
	self.cmsswrel = clirel
	self.base_path = clibasepath
	self.whitelist = cliwhitelist
	self.blacklist = cliblacklist
	self.usingwhitelist = cliusingwhitelist
	self.usingblacklist = cliusingblacklist

    def BeginJob(self):
	# Get the list of valid package tags for this release, using the 
	# CmsTCPackageList.pl script.
	os.system("CmsTCPackageList.pl --rel " + self.cmsswrel + " >& temptags.txt")
	tagfile = open("temptags.txt")
	taglines = tagfile.readlines()
	for tagline in taglines:
	    self.tagtuple.append(((tagline.split())[0], (tagline.split())[1]))
	os.system("rm temptags.txt")

	# List of all available modules
	sealcomponenttuple = []

	# Track all modules which will be modified
	modifiedmodules = []

	source_tree = self.base_path + "//src/"

	print "The  source tree is: " + source_tree

	foundcomponent = 0

	# Add this release to the DB and check for success
	print "Release is = " + self.cmsswrel
	addrelease = -1
	addrelease = self.dbloader.ConfdbAddNewRelease(self.dbcursor,self.cmsswrel)
	if(addrelease > 0):
	    print "Succesfully added release " + self.cmsswrel + " to the DB"
	else:
	    print "Error: Failed to add release " + self.cmsswrel + " to the DB"
	    print "Exiting now"
	    return

	# Get package list for this release
	packagelist = os.listdir(source_tree)

	# Start decending into the source tree
	for package in packagelist:
	    foundcomponent = 0

	    # Check if this is really a directory
	    if(os.path.isdir(source_tree + package)):

		subdirlist = os.listdir(source_tree + package)

		for subdir in subdirlist:
		    # Check if the user really wants to use this package
		    if (self.usingblacklist == True and (package+"/"+subdir) in self.blacklist[0]):
			if(self.verbose > 0):
			    print "Skipping package " + (package+"/"+subdir)
			continue

		    elif (self.usingwhitelist == True and not ((package+"/"+subdir) in self.whitelist[0])):
			if(self.verbose > 0):
			    print "Skipping package " + (package+"/"+subdir)
			continue

		    packagedir = source_tree + package + "/" + subdir
		
		    print "Scanning package: " + package + "/" + subdir
                
		    srcdir = packagedir + "/src/"
		    testdir = packagedir + "/test/"
		    pluginsdir = packagedir + "/plugins/"

		    if(os.path.isdir(srcdir) or os.path.isdir(pluginsdir)):
			if(os.path.isdir(srcdir)):
			    srcfiles = os.listdir(srcdir)
			    if(os.path.isdir(pluginsdir)):
				srcfiles = srcfiles + os.listdir(pluginsdir)
			elif(os.path.isdir(pluginsdir)):
			    srcfiles = os.listdir(pluginsdir)

			for srcfile in srcfiles:
			    # Get all cc files
			    if(srcfile.endswith(".cc")):				
				if(os.path.isfile(srcdir + srcfile)):
				    sealcomponentfilename = srcdir + srcfile
				elif(os.path.isfile(pluginsdir + srcfile)):
				    sealcomponentfilename = pluginsdir + srcfile

				if(os.path.isfile(sealcomponentfilename)):
				    sealcomponentfile = open(sealcomponentfilename)
                  
				    sealcomponentlines = sealcomponentfile.readlines()

				    # Look through each file for framework component definitions
				    for sealcomponentline in sealcomponentlines:
					if(sealcomponentline.lstrip().startswith("//")):
					    continue

					# First modules
					if(sealcomponentline.find("DEFINE_FWK_MODULE") != -1 or
					   sealcomponentline.find("DEFINE_ANOTHER_FWK_MODULE") != -1):
					    sealmodulestring = sealcomponentline.split('(')

					    sealclass = (sealmodulestring[1]).split(')')[0].lstrip().rstrip()
					    sealmodule = package + "/" + sealclass

					    if(self.verbose > 0):
						print "\n\tSEAL Module name = " + sealmodule
                                        
					    sealcomponenttuple.append(sealmodule)

					    # If a new module definition was found, start parsing the source
					    # code for the details
					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,1)

					    foundcomponent = 1

					# Next Services
					if((sealcomponentline.find("DEFINE_FWK_SERVICE") != -1 or
					    sealcomponentline.find("DEFINE_ANOTHER_FWK_SERVICE") != -1) and
					   sealcomponentline.find("DEFINE_FWK_SERVICE_MAKER") == -1 and
					   sealcomponentline.find("DEFINE_ANOTHER_FWK_SERVICE_MAKER") == -1):
					    sealservicestring = sealcomponentline.split('(')

					    sealclass = (sealservicestring[1]).split(')')[0].lstrip().rstrip()
					    sealservice = package + "/" + sealclass
					
					    if(self.verbose > 0):
						print "\n\tSEAL Service name = " + sealservice

					    sealcomponenttuple.append(sealservice)

					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,2)

					    foundcomponent = 1

					# And next services defined through service makers
					if(sealcomponentline.find("DEFINE_FWK_SERVICE_MAKER") != -1 or
					   sealcomponentline.find("DEFINE_ANOTHER_FWK_SERVICE_MAKER") != -1):
					    sealservicestring = sealcomponentline.split('(')

					    sealservice = ((sealservicestring[1]).split(',')[0]).lstrip().rstrip()

					    if(self.verbose > 0):
						print "\n\tSEAL ServiceMaker name = " + sealservice

					    sealcomponenttuple.append(sealservice)

					    self.ScanComponent(sealservice, packagedir,package+"/"+subdir,source_tree,2)

					    foundcomponent = 1	   

					# Then ES_Sources
					if((sealcomponentline.find("DEFINE_FWK_EVENTSETUP_SOURCE") != -1 or
					    sealcomponentline.find("DEFINE_ANOTHER_FWK_EVENTSETUP_SOURCE") != -1)): 
					    sealessourcestring = sealcomponentline.split('(')

					    sealclass = (sealessourcestring[1]).split(')')[0].lstrip().rstrip()
					    sealessource = package + "/" + sealclass

					    if(self.verbose > 0):
						print "\n\tSEAL ES_Source name = " + sealessource

					    sealcomponenttuple.append(sealessource)

					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,3)

					    foundcomponent = 1					

					# And finally ED_Sources
					if((sealcomponentline.find("DEFINE_FWK_INPUT_SOURCE") != -1 or
					    sealcomponentline.find("DEFINE_ANOTHER_FWK_INPUT_SOURCE") != -1)): 
					    sealessourcestring = sealcomponentline.split('(')

					    sealclass = (sealessourcestring[1]).split(')')[0].lstrip().rstrip()
					    sealessource = package + "/" + sealclass

					    if(self.verbose > 0):
						print "\n\tSEAL ED_Source name = " + sealessource

					    sealcomponenttuple.append(sealessource)

					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,4)

					    foundcomponent = 1			

	# Just print a list of all components found in the release
	if(self.verbose > 0):					
	    print "Scanned the following framework components:"
	    for mycomponent in sealcomponenttuple:
		print "\t" + mycomponent
	   
	    print "End of job: Scanned " + str(len(sealcomponenttuple)) + " fwk components for release" + self.cmsswrel 
	    self.dbloader.PrintStats()

	# Commit and disconnect to be compatible with either INNODB or MyISAM
	self.dbloader.ConfdbExitGracefully()

    def ScanComponent(self,modulename, packagedir, packagename, sourcetree, componenttype):

	# Get a parser object
	myParser = ConfdbSourceParser.SourceParser(self.verbose,sourcetree)

	srcdir = packagedir + "/src/"
	interfacedir = packagedir + "/interface/"
	datadir = packagedir + "/data/"
	cvsdir = packagedir + "/CVS/"
	testdir = packagedir + "/test/"
	pluginsdir = packagedir + "/plugins/"

	tagline = ""

#	tagfile = open("tags130_pre2.txt")
#	taglines = tagfile.readlines()
#
#	for modtag in taglines:
#	    if((modtag.split()[0]).lstrip().rstrip() == packagename.lstrip().rstrip()):
#		tagline = (modtag.split()[1]).lstrip().rstrip()

        for modtag, cvstag in self.tagtuple:
	    if(modtag.lstrip().rstrip() == packagename.lstrip().rstrip()):
		tagline = cvstag

	if(os.path.isdir(srcdir) or os.path.isdir(pluginsdir)):        
	    if(os.path.isdir(srcdir)):
		srcfiles = os.listdir(srcdir)
		if(os.path.isdir(pluginsdir)):
		    srcfiles = srcfiles + os.listdir(pluginsdir)
	    elif(os.path.isdir(pluginsdir)):
		srcfiles = os.listdir(pluginsdir)

	    for srcfile in srcfiles:
		# Get all cc files
		if(srcfile.endswith(".cc")):
		    interfacefile = srcfile.rstrip('.cc') + '.h'

		    try:
			# Find the base class from the .h files in the interface/ directory
			if(os.path.isdir(interfacedir)):
			    myParser.ParseInterfaceFile(interfacedir + interfacefile, modulename)

			if(os.path.isdir(srcdir) and os.path.isfile(srcdir + srcfile)):
			    # Because some people like to put .h files in the src/ directory...
			    myParser.ParseInterfaceFile(srcdir + interfacefile, modulename)

			    # And other people like to put class definitions in .cc files
			    myParser.ParseInterfaceFile(srcdir + srcfile, modulename)

			    # Now find the relevant constructor and parameter declarations
			    # in the .cc files in the src/ directory
			    myParser.ParseSrcFile(srcdir + srcfile, modulename, datadir, "")

			    # Lastly the special case of modules declared via typedef
			    myParser.HandleTypedefs(srcdir + srcfile, modulename, srcdir, interfacedir, datadir, sourcetree)

			if(os.path.isdir(pluginsdir) and os.path.isfile(pluginsdir + srcfile)):
			    # Even if the typedefs are in a special "plugins" directory
			    myParser.HandleTypedefs(pluginsdir + srcfile, modulename, pluginsdir, interfacedir, datadir, sourcetree)

		    except IndexError:
			print "Error: exception caught during parsing. The component " + modulename + " will not be loaded to the DB"
			return

	# Retrieve the relevant information to be loaded to the DB
	hltparamlist = myParser.GetParams(modulename)
	hltvecparamlist = myParser.GetVectorParams(modulename)
	hltparamsetlist = myParser.GetParamSets(modulename)
	hltvecparamsetlist = myParser.GetVecParamSets(modulename)
	modulebaseclass = myParser.GetBaseClass()
	    

	# OK, now we know the module, it's base class, it's parameters, and their
	# default values. Start updating the database if necessary
	if(componenttype == 1):

	    # Make sure we recognize the base class of this module
	    if(modulebaseclass == "EDProducer" or
	       modulebaseclass == "EDFilter" or 
	       modulebaseclass == "ESProducer" or
	       modulebaseclass == "OutputModule" or
	       modulebaseclass == "EDAnalyzer" or 
	       modulebaseclass == "HLTProducer" or 
	       modulebaseclass == "HLTFilter"):
		# First check if this module template already exists
		if(modulebaseclass):
		    modid = self.dbloader.ConfdbCheckModuleExistence(self.dbcursor,modulebaseclass,modulename,tagline)
		    if(modid):
			# If so, see if parameters need to be updated
			print "***UPDATING MODULE " + modulename + "***"
			self.dbloader.ConfdbUpdateModuleTemplate(self.dbcursor,modulename,modulebaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
		    else:
			# If not, make a new template
			self.dbloader.ConfdbLoadNewModuleTemplate(self.dbcursor,modulename,modulebaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)


	    # This is an unknown base class. See if it really inherits from something
	    # else we know.
	    else:
		if(modulebaseclass):
		    therealbaseclass = myParser.FindOriginalBaseClass(modulebaseclass, sourcetree)

		    if(therealbaseclass == "EDProducer" or
		       therealbaseclass == "EDFilter" or 
		       therealbaseclass == "ESProducer" or
		       therealbaseclass == "OutputModule" or
		       therealbaseclass == "EDAnalyzer" or 
		       therealbaseclass == "HLTProducer" or 
		       therealbaseclass == "HLTFilter"):
			# First check if this module template already exists
			if(therealbaseclass):
			    modid = self.dbloader.ConfdbCheckModuleExistence(self.dbcursor,therealbaseclass,modulename,tagline)
			    if(modid):
				# If so, see if parameters need to be updated
				print "***UPDATING MODULE " + modulename + "***"
				self.dbloader.ConfdbUpdateModuleTemplate(self.dbcursor,modulename,therealbaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
			    else:
				# If not, make a new template
				self.dbloader.ConfdbLoadNewModuleTemplate(self.dbcursor,modulename,therealbaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
		    else:
			print  "Message: Unknown module base class " + modulebaseclass + ":" + therealbaseclass + ". Module will not be loaded."
		else:
		    print "Error: No module base class at all for " + modulename + ". Module will not be loaded"

	# This is a Service. Use the ServiceTemplate
	elif(componenttype == 2):
	    # First check if this service template already exists
	    servid = self.dbloader.ConfdbCheckServiceExistence(self.dbcursor,modulename,tagline)
	    if(servid):
		# If so, see if parameters need to be updated
		print "***UPDATING SERVICE " + modulename + "***"
		self.dbloader.ConfdbUpdateServiceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
	    else:
		# If not, make a new template
		self.dbloader.ConfdbLoadNewServiceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)	

	# This is an ES_Source. Use the ESSourceTemplate
	elif(componenttype == 3):
	    # First check if this service template already exists
	    sourceid = self.dbloader.ConfdbCheckESSourceExistence(self.dbcursor,modulename,tagline)
	    if(sourceid):
		# If so, see if parameters need to be updated
		print "***UPDATING " + modulename + "***"
		self.dbloader.ConfdbUpdateESSourceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
	    else:
		# If not, make a new template
		self.dbloader.ConfdbLoadNewESSourceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)	

	# This is an ED_Source. Use the EDSourceTemplate
	elif(componenttype == 4):
	    # First check if this service template already exists
	    sourceid = self.dbloader.ConfdbCheckEDSourceExistence(self.dbcursor,modulename,tagline)
	    if(sourceid):
		# If so, see if parameters need to be updated
		print "***UPDATING EDSOURCE " + modulename + "***"
		self.dbloader.ConfdbUpdateEDSourceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
	    else:
		# If not, make a new template
		self.dbloader.ConfdbLoadNewEDSourceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)	


	# Display any cases where we ran into trouble
	myParser.ShowParamFailures()
	myParser.ResetParams()
                
if __name__ == "__main__":
    main(sys.argv[1:])

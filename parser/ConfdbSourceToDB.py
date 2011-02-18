#!/usr/bin/env python

# ConfdbSourceToDB.py
# Main file for parsing source code in a CMSSW release and
# loading module templates to the Conf DB.
# 
# Jonathan Hollar LLNL Jan. 12, 2008

import os, string, sys, posix, tokenize, array, getopt
import ConfdbSourceParser
#import ConfdbSQLModuleLoader
import ConfdbOracleModuleLoader
import ConfdbConfigurationComponentParser

def main(argv):
    # Get information from the environment
    input_base_path = os.environ.get("CMSSW_RELEASE_BASE")
    input_cmsswrel = os.environ.get("CMSSW_VERSION")
    input_addfromreldir = os.environ.get("CMSSW_BASE")
    input_baserelease_path = input_base_path

    # User can provide a list of packages to ignore...
    input_usingblacklist = False
    input_blacklist = []

    # or a list of packages (and only these packages) to use
    input_usingwhitelist = False
    input_whitelist = []

    input_verbose = 0
    input_dbname = "hltdb1"
#    input_dbuser = "jjhollar"
#    input_dbpwd = "password"
#    input_dbtype = "MySQL"
    input_dbuser = "CMS_HLT_TEST"
    input_dbpwd = "hltdevtest1"
    input_host = "CMS_ORCOFF_INT2R"
    input_dbtype = "Oracle"
#    input_host = "localhost"
    input_configfile = ""
    input_dotest = False
    input_noload = False
    input_addtorelease = "none"
    input_comparetorelease = ""
    input_configflavor = "python"

    # Parse command line options
    opts, args = getopt.getopt(sys.argv[1:], "r:p:b:w:c:v:d:u:s:t:o:l:e:a:m:f:nh", ["release=","sourcepath=","blacklist=","whitelist=","releasename=","verbose=","dbname=","user=","password=","dbtype=","hostname=","configfile=","parsetestdir=","addtorelease=","comparetorelease=","configflavor=","noload=","help="])
    for o, a in opts:
	if o in ("-r","release="):
            foundinscramlist = False
            scramlisthandles = os.popen("scramv1 list CMSSW | grep " + str(a)).readlines()
            for scramlisthandle in scramlisthandles:
                if(scramlisthandle.lstrip().startswith('-->') and scramlisthandle.rstrip().endswith(str(a))):
                    scramlistpath = scramlisthandle.lstrip().rstrip().split('-->')[1]
                    if(input_addtorelease == "none"):
                        input_base_path = scramlistpath.lstrip().rstrip() + "/"
                    foundinscramlist = True
                    input_cmsswrel = str(a)
                                                                                                                                
            if(input_base_path and input_cmsswrel and foundinscramlist == True):                
                input_cmsswrel = str(a)
                print "Using release " + input_cmsswrel + " at path " + input_base_path
	    else:
		print "Could not resolve path to the release " + str(a)
		print "Check that the CMSSW_RELEASE_BASE and CMSSW_VERSION envvars are set, and that the release is appears in scramv1 list"
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
	    else:
		print "Unknown DB type " + input_dbtype + ", exiting now"
		return
	if o in ("-l","config="):
	    input_configfile = str(a)
	    print "Parsing components for config: " + input_configfile
	if o in ("-e","parsetestdir="):
	    if(int(a) == 1):
		print "Will parse test/ directories"
		input_dotest = True
	    else:
		print "Will not parse test/ directories"
		input_dotest = False
	if o in ("-a","addtorelease="):
	    input_addtorelease = str(a)    
	    input_baserelease_path = input_base_path
	    input_base_path = input_addfromreldir
	    print "Will create new release " + input_addtorelease + " using packages in " + input_addfromreldir
	if o in ("-m","comparetorelease="):
	    print "Will update releative to release " + str(a)
	    input_comparetorelease = str(a)
        if o in ("-f","configflavor="):
            print "Will use " + str(a) + "-language cfi files to find parameter defaults"
            input_configflavor = str(a)
	if o in ("-n","noload="):
	    print "Will parse release without loading to the DB"
	    input_noload = True
	if o in ("-h","help="):
	    print "Help menu for ConfdbSourceToDB"
	    print "\t-r <CMSSW release (default is the CMSSW_VERSION envvar)>"
	    print "\t-p <Absolute path to the release>"
	    print "\t-c <Manually set the name of the release>"
	    print "\t-m <Release to compare to when updating>"
	    print "\t-w <Comma-delimited list of packages to parse>"
	    print "\t-b <Comma-delimited list of packages to ignore>"
	    print "\t-v <Verbosity level (0-3)>"
	    print "\t-d <Name of the database to connect to>"
	    print "\t-u <User name to connect as>" 
	    print "\t-s <Database password>"
	    print "\t-o <Hostname>"
	    print "\t-t <Type of database. Options are MySQL (deprecated!) or Oracle)>"
	    print "\t-l <Name of config file>"
            print "\t-f <Flavor of configuration file. Options are python (default) or cfg>"
	    print "\t-e <Parse test/ directories. 1 = yes, 0/default = no>"
	    print "\t-h Print this help menu"
	    return

    if((not input_base_path) or (not input_cmsswrel)):
	print "Configuration error: Could not resolve path to the release"
	print "\tEither the CMSSW_RELEASE_BASE and CMSSW_VERSION envvars must be set, or use the -p and -c options to explicitly set the path"
	print "\tExiting now"
	return

    print "Using release base: " + input_base_path

    confdbjob = ConfdbSourceToDB(input_cmsswrel,input_base_path,input_whitelist,input_blacklist,input_usingwhitelist,input_usingblacklist,input_verbose,input_dbname,input_dbuser,input_dbtype,input_dbpwd,input_host,input_configfile,input_dotest,input_noload,input_addtorelease,input_comparetorelease,input_baserelease_path,input_configflavor)
    confdbjob.BeginJob()

class ConfdbSourceToDB:
    def __init__(self,clirel,clibasepath,cliwhitelist,cliblacklist,cliusingwhitelist,cliusingblacklist,cliverbose,clidbname,clidbuser,clidbtype,clidbpwd,clihost,cliconfig,clidotest,clinoload,cliaddtorelease,clicomparetorelease,clibasereleasepath,cliconfigflavor):
	self.data = []
	self.dbname = clidbname
	self.dbuser = clidbuser
	self.dbtype = clidbtype
	self.verbose = int(cliverbose)
	self.dbpwd = clidbpwd
	self.dbhost = clihost
	self.dotestdir = clidotest
	self.doconfig = cliconfig
	self.noload = clinoload
	self.addtorelease = cliaddtorelease
	self.comparetorelease = clicomparetorelease
        self.configflavor = cliconfigflavor
	self.moduledefinedinfile = ""
	self.needconfigcomponents = []
	self.needconfigpackages = []
	self.sqlerrors = []
	self.parseerrors = []
	self.baseclasserrors = []
	self.unknownbaseclasses = []
	self.addedtemplatenames = []
	self.addedtemplatetags = []
        self.nocficomponents = []

	# Get a Conf DB connection. Only need to do this once at the 
	# beginning of a job.
	if(self.dbtype == "MySQL" and self.noload == False):
	    self.dbloader = ConfdbSQLModuleLoader.ConfdbMySQLModuleLoader(self.verbose,self.addtorelease,self.comparetorelease)
	    self.dbcursor = self.dbloader.ConfdbMySQLConnect(self.dbname,self.dbuser,self.dbpwd,self.dbhost)
	elif(self.dbtype == "Oracle" and self.noload == False):
	    self.dbloader = ConfdbOracleModuleLoader.ConfdbOracleModuleLoader(self.verbose,self.addtorelease,self.comparetorelease)
	    self.dbcursor = self.dbloader.ConfdbOracleConnect(self.dbname,self.dbuser,self.dbpwd,self.dbhost)

	# Deal with package tags for this release.
	self.tagtuple = []
	self.basereltagtuple = []
	self.cmsswrel = clirel
	self.base_path = clibasepath
	self.baserelease_path = clibasereleasepath
	self.whitelist = cliwhitelist
	self.blacklist = cliblacklist
	self.usingwhitelist = cliusingwhitelist
	self.usingblacklist = cliusingblacklist

    def BeginJob(self):
	# Get the list of valid package tags for this release, using the 
	# CmsTCPackageList.pl script.
        #	os.system("CmsTCPackageList.pl --rel " + self.cmsswrel + " >& temptags.txt")
        #	tagfile = open("temptags.txt")

	if(self.addtorelease != "none"):
	    # If we're creating an intermediate release, get the list of tags from the *base* release
	    if(os.path.isfile(self.baserelease_path + "//src/PackageList.cmssw")):
		tagfile = open(self.baserelease_path + "//src/PackageList.cmssw")
		taglines = tagfile.readlines()
		for tagline in taglines:
		    self.tagtuple.append(((tagline.split())[0], (tagline.split())[1]))
	else:
	    # Otherwise, get the list of tags from *this* release
	    if(os.path.isfile(self.base_path + "//src/PackageList.cmssw")):
		tagfile = open(self.base_path + "//src/PackageList.cmssw")
		taglines = tagfile.readlines()
		for tagline in taglines:
		    self.tagtuple.append(((tagline.split())[0], (tagline.split())[1]))

	# List of all available modules
	sealcomponenttuple = []

	# Track all modules which will be modified
	modifiedmodules = []

	source_tree = self.base_path + "//src/"

	print "The  source tree is: " + source_tree

        if(not (os.path.isdir(self.base_path))):
            print 'Fatal error: release source tree not found. Exiting now'
            return

	# Generate list of components needed for a particular configuration
	# if requested
	if(self.doconfig != ""):
	    self.configcomp = ConfdbConfigurationComponentParser.ConfigurationComponentParser(1)
	    self.needconfigcomponents = self.configcomp.FindConfigurationComponents(self.doconfig,source_tree,0)

	foundcomponent = 0

	# Add this release to the DB and check for success
	print "Release is = " + self.cmsswrel
	if(self.noload == False):
	    addrelease = -1
	    print "Add to release is " + self.addtorelease
	    if(self.addtorelease == "none"):
	        addrelease = self.dbloader.ConfdbAddNewRelease(self.dbcursor,self.cmsswrel)
	    else:
		addrelease = self.dbloader.ConfdbAddNewRelease(self.dbcursor,self.addtorelease)

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

            if(not (os.path.isdir(self.base_path))):
                print 'Fatal error: release source tree not found. Exiting now'
                return
                                
	    foundcomponent = 0

	    # Check if this is really a directory
	    if(os.path.isdir(source_tree + package)):

		subdirlist = os.listdir(source_tree + package)

		for subdir in subdirlist:
		    # Check if the user really wants to use this package
		    if (self.usingblacklist == True and (package) in self.blacklist[0]):
			if(self.verbose > 0):
			    print "Skipping subsystem/package " + (package) + "/" + subdir
			continue

		    elif (self.usingwhitelist == True and not ((package) in self.whitelist[0])):
			if(self.verbose > 0):
			    print "Skipping subsystem/package " + (package) + "/" + subdir
			continue

		    packagedir = source_tree + package + "/" + subdir
		
		    print "Scanning package: " + package + "/" + subdir
                
		    srcdir = packagedir + "/src/"
		    testdir = packagedir + "/test/"
		    pluginsdir = packagedir + "/plugins/"

		    # Stupid way of dealing with duplicate file names when parsing both src/ and test/ 
		    donefiles = []

		    if(os.path.isdir(srcdir) or os.path.isdir(pluginsdir) or 
		       (self.dotestdir == True and os.path.isdir(testdir))):        
			if(os.path.isdir(srcdir)):
			    srcfiles = os.listdir(srcdir)
			    if(os.path.isdir(pluginsdir)):
				srcfiles = srcfiles + os.listdir(pluginsdir)
			    if(os.path.isdir(testdir) and self.dotestdir == True):
				srcfiles = srcfiles + os.listdir(testdir)
			elif(os.path.isdir(pluginsdir)):
			    srcfiles = os.listdir(pluginsdir)
			    if(os.path.isdir(testdir) and self.dotestdir == True):
				srcfiles = srcfiles + os.listdir(testdir)
			elif(os.path.isdir(testdir) and self.dotestdir == True):
			    srcfiles = os.listdir(testdir)

			for srcfile in srcfiles:
			    # Get all cc files
			    if(srcfile.endswith(".cc")):				
				if(os.path.isfile(srcdir + srcfile) and not ((srcdir + srcfile) in donefiles)):
				    sealcomponentfilename = srcdir + srcfile
				    donefiles.append(sealcomponentfilename)
				elif(os.path.isfile(pluginsdir + srcfile) and not ((pluginsdir + srcfile) in donefiles)):
				    sealcomponentfilename = pluginsdir + srcfile
				    donefiles.append(sealcomponentfilename)
				elif(self.dotestdir == True and 
				     (os.path.isfile(testdir + srcfile)) and not ((testdir + srcfile) in donefiles)):
				    sealcomponentfilename = testdir + srcfile
				    donefiles.append(sealcomponentfilename)

				if(self.verbose > 1):
				    print "Searching for Fwk. components in " + sealcomponentfilename

				if(os.path.isfile(sealcomponentfilename)):
				    sealcomponentfile = open(sealcomponentfilename)
                  
				    sealcomponentlines = sealcomponentfile.readlines()

				    startedccomment = False

				    # Look through each file for framework component definitions
				    for sealcomponentline in sealcomponentlines:
					if(sealcomponentline.lstrip().startswith("//")):
					    continue

					if(sealcomponentline.lstrip().startswith("/*")):
					    startedccomment = True

					if(sealcomponentline.rstrip().endswith("*/")):
					    startedccomment = False

                                        if(sealcomponentline.find("*/") != -1):
                                            if((sealcomponentline.split("*/"))[1].find("//") != -1):
                                                startedccomment = False

					if(startedccomment == True):
					    continue

					# First modules
					if(sealcomponentline.find("DEFINE_FWK_MODULE") != -1 or
					   sealcomponentline.find("DEFINE_ANOTHER_FWK_MODULE") != -1):
					    sealmodulestring = sealcomponentline.split('(')

					    sealclass = (sealmodulestring[1]).split(')')[0].lstrip().rstrip()
					    sealmodule = package + "/" + sealclass

					    if(self.verbose > 0):
						print "\n\tSEAL Module name = " + sealmodule

					    if(self.doconfig != "" and (not sealclass in self.needconfigcomponents)):
						if(self.verbose > 0):
						    print "\t\tModule " + sealclass + " not needed for this config"
						continue
                                        
					    if(not (package+"/"+subdir) in self.needconfigpackages):
						self.needconfigpackages.append(package+"/"+subdir)

                                            if(sealmodule in sealcomponenttuple):
                                                print "Warning: Component already found in this release. Ignoring second occurance."
                                                continue
                                            
					    sealcomponenttuple.append(sealmodule)

					    # If a new module definition was found, start parsing the source
					    # code for the details
					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,1,None)

					    self.moduledefinedinfile = sealcomponentfilename

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

					    if(self.doconfig != "" and (not sealclass in self.needconfigcomponents)):
						if(self.verbose > 0):
						    print "\t\tService " + sealclass + " not needed for this config"
						continue
                                        
					    if(not (package+"/"+subdir) in self.needconfigpackages):
						self.needconfigpackages.append(package+"/"+subdir)


                                            if(sealservice in sealcomponenttuple):
                                                print "Warning: Component already found in this release. Ignoring second occurance."
                                                continue

					    sealcomponenttuple.append(sealservice)

					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,2,None)

					    self.moduledefinedinfile = sealcomponentfilename

					    foundcomponent = 1

					# And next services defined through service makers
					if(sealcomponentline.find("DEFINE_FWK_SERVICE_MAKER") != -1 or
					   sealcomponentline.find("DEFINE_ANOTHER_FWK_SERVICE_MAKER") != -1):
					    sealservicestring = sealcomponentline.split('(')

					    sealservice = ((sealservicestring[1]).split(',')[0]).lstrip().rstrip()

					    if(self.verbose > 0):
						print "\n\tSEAL ServiceMaker name = " + sealservice

					    if(self.doconfig != "" and (not sealclass in self.needconfigcomponents)):
						if(self.verbose > 0):
						    print "\t\tServiceMaker " + sealclass + " not needed for this config"
						continue
                                        
					    if(not (package+"/"+subdir) in self.needconfigpackages):
						self.needconfigpackages.append(package+"/"+subdir)


                                            if(sealservice in sealcomponenttuple):
                                                print "Warning: Component already found in this release. Ignoring second occurance."
                                                continue

					    sealcomponenttuple.append(sealservice)

					    self.ScanComponent(sealservice, packagedir,package+"/"+subdir,source_tree,2,None)

					    self.moduledefinedinfile = sealcomponentfilename

					    foundcomponent = 1	   

					# And a special case for jet corrections services
					if(sealcomponentline.find("DEFINE_JET_CORRECTION_SERVICE") != -1):
					    sealservicestring = sealcomponentline.split('(')

					    sealclass = (sealservicestring[1]).split(')')[0].split(',')[0].lstrip().rstrip()
					    sealname =  (sealservicestring[1]).split(')')[0].split(',')[1].lstrip().rstrip()
					    sealservice = package + "/" + sealclass
					
					    if(self.verbose > 0):
						print "\n\tSEAL Jet Correction Service name = " + sealservice

					    if(self.doconfig != "" and (not sealclass in self.needconfigcomponents)):
						if(self.verbose > 0):
						    print "\t\tService " + sealclass + " not needed for this config"
						continue
                                        
					    if(not (package+"/"+subdir) in self.needconfigpackages):
						self.needconfigpackages.append(package+"/"+subdir)

                                            if(sealservice in sealcomponenttuple):
                                                print "Warning: Component already found in this release. Ignoring second occurance."
                                                continue

					    sealcomponenttuple.append(sealservice)

					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,3,sealname)

					    self.moduledefinedinfile = sealcomponentfilename

					    foundcomponent = 1					    

					# Then ES_Sources
					if((sealcomponentline.find("DEFINE_FWK_EVENTSETUP_SOURCE") != -1 or
					    sealcomponentline.find("DEFINE_ANOTHER_FWK_EVENTSETUP_SOURCE") != -1)): 
					    sealessourcestring = sealcomponentline.split('(')

					    sealclass = (sealessourcestring[1]).split(')')[0].lstrip().rstrip()
					    sealessource = package + "/" + sealclass

					    if(self.verbose > 0):
						print "\n\tSEAL ES_Source name = " + sealessource

					    if(self.doconfig != "" and (not sealclass in self.needconfigcomponents)):
						if(self.verbose > 0):
						    print "\t\tESSource " + sealclass + " not needed for this config"
						continue
                                        
					    if(not (package+"/"+subdir) in self.needconfigpackages):
						self.needconfigpackages.append(package+"/"+subdir)

                                            if(sealessource in sealcomponenttuple):
                                                print "Warning: Component already found in this release. Ignoring second occurance."
                                                continue

					    sealcomponenttuple.append(sealessource)

					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,3,None)

					    self.moduledefinedinfile = sealcomponentfilename

					    foundcomponent = 1					

					# And finally ED_Sources
					if((sealcomponentline.find("DEFINE_FWK_INPUT_SOURCE") != -1 or
					    sealcomponentline.find("DEFINE_ANOTHER_FWK_INPUT_SOURCE") != -1)): 
					    sealessourcestring = sealcomponentline.split('(')

					    sealclass = (sealessourcestring[1]).split(')')[0].lstrip().rstrip()
					    sealessource = package + "/" + sealclass

					    if(self.verbose > 0):
						print "\n\tSEAL ED_Source name = " + sealessource

					    if(self.doconfig != "" and (not sealclass in self.needconfigcomponents)):
						if(self.verbose > 0):
						    print "\t\tEDSource " + sealclass + " not needed for this config"
						continue
                                        
					    if(not (package+"/"+subdir) in self.needconfigpackages):
						self.needconfigpackages.append(package+"/"+subdir)

                                            if(sealessource in sealcomponenttuple):
                                                print "Warning: Component already found in this release. Ignoring second occurance."
                                                continue
                                                                                                                                            
					    sealcomponenttuple.append(sealessource)

					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,4,None)

					    self.moduledefinedinfile = sealcomponentfilename

					    foundcomponent = 1			

					# And really finally ESModules
					if ((sealcomponentline.find("DEFINE_FWK_EVENTSETUP_MODULE") != -1 or
					    sealcomponentline.find("DEFINE_ANOTHER_FWK_EVENTSETUP_MODULE") != -1)): 
					    sealessourcestring = sealcomponentline.split('(')

					    sealclass = (sealessourcestring[1]).split(')')[0].lstrip().rstrip()
					    sealessource = package + "/" + sealclass

					    if(self.verbose > 0):
						print "\n\tSEAL ES_Module name = " + sealessource

					    if(self.doconfig != "" and (not sealclass in self.needconfigcomponents)):
						if(self.verbose > 0):
						    print "\t\tES_Module " + sealclass + " not needed for this config"
						continue
                                        
					    if(not (package+"/"+subdir) in self.needconfigpackages):
						self.needconfigpackages.append(package+"/"+subdir)

                                            if(sealessource in sealcomponenttuple):
                                                print "Warning: Component already found in this release. Ignoring second occurance."
                                                continue

					    sealcomponenttuple.append(sealessource)

					    self.ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,5,None)

					    self.moduledefinedinfile = sealcomponentfilename

					    foundcomponent = 1			


	# Reassociate components that weren't changed
        if(self.addtorelease != "none"):
            if(self.noload == False):
                self.dbloader.ConfdbReassociateTemplates(self.dbcursor,self.cmsswrel,self.addtorelease,self.addedtemplatenames)

	# Just print a list of all components found in the release
	if(self.verbose > 0):					
	    print "Scanned the following framework components:"
	    for mycomponent in sealcomponenttuple:
		print "\t" + mycomponent
	   
	print "\n\n*************************************************************************"
	print "End of job report"
	if(self.addtorelease != "none"):
	    print "The add to release option (-a) was used." 
	    print "\tA new intermediate release named " + self.addtorelease + " was created from the base release " + self.cmsswrel + "."
	    print "\tThe following " + str(len(self.addedtemplatenames)) + " components were updated/added from the test release area " + self.base_path + ":"
	    i = 0
            print "\n\t\tTemplate name" + "\t\tBase release" + "\t\tIntermediate release" 
	    for myaddedtemplatename in self.addedtemplatenames:
		print "\t\t" + myaddedtemplatename + "\t" + (self.addedtemplatetags[i])[0] + "\t-->\t" + (self.addedtemplatetags[i])[1]
		i = i + 1
	else:
	    print "Scanned " + str(len(sealcomponenttuple)) + " fwk components for release " + self.cmsswrel 
	    if(self.noload == False):
		self.dbloader.PrintStats()
        if(len(self.nocficomponents) > 0):
            print "The following " + str(len(self.nocficomponents)) + " components were located in the source code, but a cfi.py with the defaults was not found"
            for nocficomponent in self.nocficomponents:
                print "\t" + nocficomponent
	if(len(self.unknownbaseclasses) > 0):
	    print "The following " + str(len(self.unknownbaseclasses)) + " components were not loaded because their base class" 
	    print "does not appear to be one of"
	    print "EDProducer/EDFilter/ESProducer/OutputModule/EDAnalyzer/HLTProducer/HLTFilter:"
	    for myunknownclass in self.unknownbaseclasses:
		print "\t" + myunknownclass
	if(len(self.baseclasserrors) > 0):
	    print "The parser was unable to determine the base class for the following " + str(len(self.baseclasserrors)) + " components:"
	    for mybaseclasserror in self.baseclasserrors:
		print "\t" + mybaseclasserror
	if(len(self.parseerrors) > 0):
	    print "The following " + str(len(self.parseerrors)) + " components had unknown parse errors:"
	    for myparseerror in self.parseerrors:
		print "\t" + myparseerror
	if(len(self.sqlerrors) > 0):
	    print "The following " + str(len(self.sqlerrors)) + " components had parameter type mismatch/SQL errors:"
	    for mysqlerror in self.sqlerrors:
		print "\t" + mysqlerror

	if(self.doconfig != ""):
	    confpkgfile = "configurationpackagesused.txt"
	    confpkgfilehandle = open(confpkgfile,"wt")
	    print "Printing list of packages needed for the specified configuration to the file " + confpkgfile
	    for confpkg in self.needconfigpackages:
		confpkgfilehandle.write(confpkg + "\n")
	if(self.noload == True):
            print "\n*************************************************************************" 
	    print "This job was run with the -n option. No DB operations were performed"
	    return

	# Commit and disconnect to be compatible with either INNODB or MyISAM
	self.dbloader.ConfdbExitGracefully()

    def ScanComponent(self,modulename, packagedir, packagename, sourcetree, componenttype, componentrename):

	# Get a parser object
	myParser = ConfdbSourceParser.SourceParser(self.verbose,sourcetree,self.configflavor)
	myParser.SetModuleDefFile(self.moduledefinedinfile)

	srcdir = packagedir + "/src/"
	interfacedir = packagedir + "/interface/"
	datadir = packagedir + "/data/"
	cvsdir = packagedir + "/CVS/"
	testdir = packagedir + "/test/"
	pluginsdir = packagedir + "/plugins/"

	tagline = ""
	basetagline = ""

#	tagfile = open("tags200_pre2.txt")
#	taglines = tagfile.readlines()
#
#	for modtag in taglines:
#	    if((modtag.split()[0]).lstrip().rstrip() == packagename.lstrip().rstrip()):
#		tagline = (modtag.split()[1]).lstrip().rstrip()

        for modtag, cvstag in self.tagtuple:
	    if(modtag.lstrip().rstrip() == packagename.lstrip().rstrip()):
		tagline = cvstag.lstrip().rstrip()
		basetagline = cvstag.lstrip().rstrip()

	# If making an intermediate psuedo-release, get the CVS tags from the 
	# checked-out packages
        if(self.addtorelease != "none"):
	    if(os.path.isfile(cvsdir + "/Tag")):
		cvscotagfile = open(cvsdir + "/Tag")
		cvscotaglines = cvscotagfile.readlines()
		for cvscotagline in cvscotaglines:
		    tagline = cvscotagline.lstrip().rstrip()
		    if(tagline.startswith('N')): 
			tagline = tagline.split('N')[1] 
		    if(self.verbose > 0):
			print "Will enter component with CVS tag " + tagline

		#Now see if this tag is different from the one in the release
                #		if((tagline.lstrip().rstrip() == basetagline.lstrip().rstrip()) and (packagedir.find("Alignment/") == -1)):
                if(tagline.lstrip().rstrip() == basetagline.lstrip().rstrip()):
		    if(self.verbose > 0):
			print "Base release and test release tags are the same -  will reassociate."
		    return
		else:
		    if(self.verbose > 0):
			print "Base release and test release tags are not the same - will modify this template"
			print "\t" + tagline.lstrip().rstrip() + " vs. " + basetagline.lstrip().rstrip()
		    self.addedtemplatenames.append(modulename)
		    self.addedtemplatetags.append((basetagline.lstrip().rstrip(),tagline.lstrip().rstrip()))
                    

	if(os.path.isdir(srcdir) or os.path.isdir(pluginsdir) or 
	   (self.dotestdir == True and os.path.isdir(testdir))):        
	    if(os.path.isdir(srcdir)):
		srcfiles = os.listdir(srcdir)
		if(os.path.isdir(pluginsdir)):
		    srcfiles = srcfiles + os.listdir(pluginsdir)
		if(os.path.isdir(testdir) and self.dotestdir == True):
		    srcfiles = srcfiles + os.listdir(testdir)
	    elif(os.path.isdir(pluginsdir)):
		srcfiles = os.listdir(pluginsdir)
		if(os.path.isdir(testdir) and self.dotestdir == True):
		    srcfiles = srcfiles + os.listdir(testdir)
	    elif(os.path.isdir(testdir) and self.dotestdir == True):
		srcfiles = os.listdir(testdir)

	    for srcfile in srcfiles:
		# Get all cc files
		if(srcfile.endswith(".cc")):
		    interfacefile = srcfile.replace('.cc','.h')

		    if(modulename == "L1GlobalTriggerRawToDigi"):
			interfacefile = interfacefile.replace('L1GlobaTriggerRawToDigi','L1GlobalTriggerRawToDigi')

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
			    # Even if things are in a special "plugins" directory

			    myParser.ParseInterfaceFile(pluginsdir + interfacefile, modulename)

			    myParser.ParseInterfaceFile(pluginsdir + srcfile, modulename)

			    myParser.ParseSrcFile(pluginsdir + srcfile, modulename, datadir, "")

			    myParser.HandleTypedefs(pluginsdir + srcfile, modulename, pluginsdir, interfacedir, datadir, sourcetree)

                        # JJH But wait - there's more! What if the class is implemented in a .h file?
                        if(os.path.isdir(interfacedir) and os.path.isfile(interfacedir + interfacefile)):
                            myParser.ParseSrcFile(interfacedir + interfacefile, modulename, datadir, "")

			if(self.dotestdir == True): 
			    if(os.path.isfile(testdir + interfacefile)):
				myParser.ParseInterfaceFile(testdir + interfacefile, modulename)
			    if(os.path.isfile(testdir + srcfile)):
				myParser.ParseInterfaceFile(testdir + srcfile, modulename)
			    if (os.path.isfile(testdir+srcfile)):
				myParser.ParseSrcFile(testdir + srcfile, modulename, testdir, "")

                    except:
			print "Error: exception caught during parsing. The component " + modulename + " will not be loaded to the DB"
			self.parseerrors.append(modulename + "\t(in " + packagename +")")
			return

            # But wait - there's more! If the class implementation is in the .h file, we get
            # to do it all over again
            if(os.path.isdir(interfacedir)):
                interfacefiles = os.listdir(interfacedir)
                for interfacefile in interfacefiles:
                    if(os.path.isfile(interfacedir+interfacefile)):
                        myParser.ParseInterfaceFile(interfacedir+interfacefile, modulename)
                        myParser.ParseSrcFile(interfacedir + interfacefile, modulename, testdir, "")
            
	# Retrieve the relevant information to be loaded to the DB
	hltparamlist = myParser.GetParams(modulename)
	hltvecparamlist = myParser.GetVectorParams(modulename)
	hltparamsetlist = myParser.GetParamSets(modulename)
	hltvecparamsetlist = myParser.GetVecParamSets(modulename)
	modulebaseclass = myParser.GetBaseClass()
        componentswithoutcfis = myParser.GetNoCfis()

        for componentwithoutcfi in componentswithoutcfis:
            if(not ((packagename + "/" + componentwithoutcfi) in self.nocficomponents)): 
                self.nocficomponents.append(packagename + "/" + componentwithoutcfi)

	try:
	    # OK, now we know the module, it's base class, it's parameters, and their
	    # default values. Start updating the database if necessary

	    # For components defined via macro-associations, the name of the class/file may not 
	    # agree with the name of the framework component
	    if(componentrename != None):
		modulename = componentrename
	    
	    # First enter the package & subsystem information
            if(self.noload == False):
                packageid = self.dbloader.ConfdbInsertPackageSubsystem(self.dbcursor,packagename.split('/')[0].lstrip().rstrip(),packagename.split('/')[1].lstrip().rstrip())

	    if(componenttype == 1):
                if(modulebaseclass == "PFTauDiscriminationProducerBase" or
                   modulebaseclass == "CaloTauDiscriminationProducerBase"):
                    modulebaseclass = "EDProducer"
		
		# Make sure we recognize the base class of this module
		if(modulebaseclass == "EDProducer" or
		   modulebaseclass == "EDFilter" or 
		   modulebaseclass == "ESProducer" or
		   modulebaseclass == "OutputModule" or
		   modulebaseclass == "EDAnalyzer" or 
		   modulebaseclass == "HLTProducer" or 
		   modulebaseclass == "HLTFilter"):
		    if(self.noload == True):
			return

		    # First check if this module template already exists
		    if(modulebaseclass):
			modid = self.dbloader.ConfdbCheckModuleExistence(self.dbcursor,modulebaseclass,modulename,tagline)
			if(modid):
			    # If so, see if parameters need to be updated
			    print "***UPDATING MODULE " + modulename + "***"
			    self.dbloader.ConfdbUpdateModuleTemplate(self.dbcursor,modulename,modulebaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)
			else:
			    # If not, make a new template
			    self.dbloader.ConfdbLoadNewModuleTemplate(self.dbcursor,modulename,modulebaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)


		# This is an unknown base class. See if it really inherits from something
		# else we know.
		else:
		    if(modulebaseclass):
			therealbaseclass = myParser.FindOriginalBaseClass(modulebaseclass, sourcetree, datadir)

			if(therealbaseclass == "EDProducer" or
			   therealbaseclass == "EDFilter" or 
			   therealbaseclass == "ESProducer" or
			   therealbaseclass == "OutputModule" or
			   therealbaseclass == "EDAnalyzer" or 
			   therealbaseclass == "HLTProducer" or 
			   therealbaseclass == "HLTFilter"):
			    if(self.noload == True):
				return

			    # First check if this module template already exists
			    if(therealbaseclass):
				modid = self.dbloader.ConfdbCheckModuleExistence(self.dbcursor,therealbaseclass,modulename,tagline)
				if(modid):
				    # If so, see if parameters need to be updated
				    print "***UPDATING MODULE " + modulename + "***"
				    self.dbloader.ConfdbUpdateModuleTemplate(self.dbcursor,modulename,therealbaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)
				else:
				    # If not, make a new template
				    self.dbloader.ConfdbLoadNewModuleTemplate(self.dbcursor,modulename,therealbaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)
			else:
			    print  "Message: Unknown module base class " + modulebaseclass + ":" + therealbaseclass + ". Module will not be loaded."
			    self.unknownbaseclasses.append(modulename + "\t(in " + packagename + ") has base class: " +  modulebaseclass)
		    else:
			print "Error: No module base class at all for " + modulename + ". Module will not be loaded"
			self.baseclasserrors.append(modulename + "\t(in " + packagename +")")

	    # This is a Service. Use the ServiceTemplate
	    elif(componenttype == 2):
		if(self.noload == True):
		    return

		# First check if this service template already exists
		servid = self.dbloader.ConfdbCheckServiceExistence(self.dbcursor,modulename,tagline)
		if(servid):
		    # If so, see if parameters need to be updated
		    print "***UPDATING SERVICE " + modulename + "***"
		    self.dbloader.ConfdbUpdateServiceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)
		else:
		    # If not, make a new template
		    self.dbloader.ConfdbLoadNewServiceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)	

	    # This is an ES_Source. Use the ESSourceTemplate
	    elif(componenttype == 3):
		if(self.noload == True):
		    return

		# First check if this service template already exists
		sourceid = self.dbloader.ConfdbCheckESSourceExistence(self.dbcursor,modulename,tagline)
		if(sourceid):
		    # If so, see if parameters need to be updated
		    print "***UPDATING " + modulename + "***"
		    self.dbloader.ConfdbUpdateESSourceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)
		else:
		    # If not, make a new template
		    self.dbloader.ConfdbLoadNewESSourceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)	

	    # This is an ED_Source. Use the EDSourceTemplate
	    elif(componenttype == 4):
		if(self.noload == True):
		    return

		# First check if this service template already exists
		sourceid = self.dbloader.ConfdbCheckEDSourceExistence(self.dbcursor,modulename,tagline)
		if(sourceid):
		    # If so, see if parameters need to be updated
		    print "***UPDATING EDSOURCE " + modulename + "***"
		    self.dbloader.ConfdbUpdateEDSourceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)
		else:
		    # If not, make a new template
		    self.dbloader.ConfdbLoadNewEDSourceTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)	

	    # This is an ES_Module. Use the ESModuleTemplate
	    elif(componenttype == 5):
		if(self.noload == True):
		    return

		# First check if this es_module template already exists
		sourceid = self.dbloader.ConfdbCheckESModuleExistence(self.dbcursor,modulename,tagline)
		if(sourceid):
		    # If so, see if parameters need to be updated
		    print "***UPDATING ESMODULE " + modulename + "***"
		    self.dbloader.ConfdbUpdateESModuleTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)
		else:
		    # If not, make a new template
		    self.dbloader.ConfdbLoadNewESModuleTemplate(self.dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist,packageid)	

	    # Display any cases where we ran into trouble
	    myParser.ShowParamFailures()
	    myParser.ResetParams()

        except FloatingPointError:
            print "Error: SQL exception caught while loading the component " + modulename + " to DB. The template may be incomplete" 
            self.sqlerrors.append(modulename + "\t(in " + packagename +")")
            return
    
if __name__ == "__main__":
    main(sys.argv[1:])

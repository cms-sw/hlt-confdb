#!/usr/local/bin/python2.4

# ConfdbSourceToDB.py
# Main file for parsing source code in a CMSSW release and
# loading module templates to the Conf DB.
# 
# Jonathan Hollar LLNL Feb. 12, 2006

import os, string, sys, posix, tokenize, array, getopt
import ConfdbSourceParser
import MySQLdb, ConfdbSQLModuleLoader

# Get a Conf DB connection. Only need to do this once at the 
# beginning of a job.
dbloader = ConfdbSQLModuleLoader.ConfdbMySQLModuleLoader()
dbcursor = dbloader.ConfdbMySQLConnect()
verbose = 0

# List of all package tags for this release.
tagtuple = []

def main(argv):
    # Get information from the environment
    base_path = os.environ.get("CMSSW_RELEASE_BASE")
    cmsswrel = os.environ.get("CMSSW_VERSION")

    # User can provide a list of packages to ignore...
    usingblacklist = False
    blacklist = []

    # or a list of packages (and only these packages) to use
    usingwhitelist = False
    whitelist = []

    # Parse command line options
    opts, args = getopt.getopt(sys.argv[1:], "p:b:w:c:", ["sourcepath=","blacklist=","whitelist=","release="])
    for o, a in opts:
        if o in ("-p","releasepath="):
            base_path = str(a)
        if o in ("-b","blacklist="):
	    blacklist.append(a.split(","))
            print 'Skip directories:'
	    usingblacklist = True
        if o in ("-w","whitelist="):
	    whitelist.append(a.split(","))
            print 'Use directories:'
	    print whitelist
	    usingwhitelist = True	    
	if o in ("-c","release="):
	    cmsswrel = str(a)
	    print "Using release " + cmsswrel

    print "Using release base: " + base_path

    # Get the list of valid package tags for this release, using the 
    # CmsTCPackageList.pl script.
    os.system("CmsTCPackageList.pl --rel " + cmsswrel + " >& temptags.txt")
    tagfile = open("temptags.txt")
    taglines = tagfile.readlines()
    for tagline in taglines:
	tagtuple.append(((tagline.split())[0], (tagline.split())[1]))
    os.system("rm temptags.txt")

    # List of all available modules
    sealcomponenttuple = []

    # Track all modules which will be modified
    modifiedmodules = []

    source_tree = base_path + "/src/"

    print "The  source tree is: " + source_tree

    foundcomponent = 0

    # Add this release to the DB and check for success
    print "Release is = " + cmsswrel
    addrelease = dbloader.ConfdbAddNewRelease(dbcursor,cmsswrel)

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
		if (usingblacklist == True and (package+"/"+subdir) in blacklist[0]):
		    if(verbose > 0):
			print "Skipping package " + (package+"/"+subdir)
		    continue

		elif (usingwhitelist == True and not ((package+"/"+subdir) in whitelist[0])):
		    if(verbose > 0):
			print "Skipping package " + (package+"/"+subdir)
		    continue


                packagedir = source_tree + package + "/" + subdir
		
		if(verbose > 0):
		    print packagedir
                
                srcdir = packagedir + "/src/"
                testdir = packagedir + "/test/"
    
                if(os.path.isdir(srcdir)):
                    srcfiles = os.listdir(srcdir)

                    for srcfile in srcfiles:
                        # Get all cc files
                        if(srcfile.endswith(".cc")):
                            sealcomponentfilename = srcdir + srcfile
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
                                        sealmodule = package + sealclass

					if(verbose > 0):
					    print "\tSEAL Module name = " + sealmodule
                                        
                                        sealcomponenttuple.append(sealmodule)

                                        # If a new module definition was found, start parsing the source
                                        # code for the details
                                        ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,1)

                                        foundcomponent = 1

				    # Next Services
				    if((sealcomponentline.find("DEFINE_FWK_SERVICE") != -1 or
					sealcomponentline.find("DEFINE_ANOTHER_FWK_SERVICE") != -1) and
				       sealcomponentline.find("DEFINE_FWK_SERVICE_MAKER") == -1 and
				       sealcomponentline.find("DEFINE_ANOTHER_FWK_SERVICE_MAKER") == -1):
                                        sealservicestring = sealcomponentline.split('(')

                                        sealclass = (sealservicestring[1]).split(')')[0].lstrip().rstrip()
                                        sealservice = package + sealclass
					
					if(verbose > 0):
					    print "\tSEAL Service name = " + sealservice

					sealcomponenttuple.append(sealservice)

                                        ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,2)

                                        foundcomponent = 1

				    # And next services defined through service makers
				    if(sealcomponentline.find("DEFINE_FWK_SERVICE_MAKER") != -1 or
					sealcomponentline.find("DEFINE_ANOTHER_FWK_SERVICE_MAKER") != -1):
				       sealservicestring = sealcomponentline.split('(')

				       sealservice = ((sealservicestring[1]).split(',')[0]).lstrip().rstrip()

				       if(verbose > 0):
					   print "\tSEAL ServiceMaker name = " + sealservice

				       sealcomponenttuple.append(sealservice)

				       ScanComponent(sealservice, packagedir,package+"/"+subdir,source_tree,2)

				       foundcomponent = 1	   

				    # Then ES_Sources
                                    if((sealcomponentline.find("DEFINE_FWK_EVENTSETUP_SOURCE") != -1 or
                                        sealcomponentline.find("DEFINE_ANOTHER_FWK_EVENTSETUP_SOURCE") != -1)): 
                                        sealessourcestring = sealcomponentline.split('(')

                                        sealclass = (sealessourcestring[1]).split(')')[0].lstrip().rstrip()
                                        sealessource = package + sealclass

					if(verbose > 0):
					    print "\tSEAL ES_Source name = " + sealessource

					sealcomponenttuple.append(sealessource)

                                        ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,3)

                                        foundcomponent = 1					

				    # And finally ED_Sources
                                    if((sealcomponentline.find("DEFINE_FWK_INPUT_SOURCE") != -1 or
                                        sealcomponentline.find("DEFINE_ANOTHER_FWK_INPUT_SOURCE") != -1)): 
                                        sealessourcestring = sealcomponentline.split('(')

                                        sealclass = (sealessourcestring[1]).split(')')[0].lstrip().rstrip()
                                        sealessource = package + sealclass

					if(verbose > 0):
					    print "\tSEAL ED_Source name = " + sealessource

					sealcomponenttuple.append(sealessource)

                                        ScanComponent(sealclass, packagedir,package+"/"+subdir,source_tree,4)

                                        foundcomponent = 1			

    # Just print a list of all components found in the release
    if(verbose > 1):					
	for mycomponent in sealcomponenttuple:
	    print mycomponent

    if(verbose > 0):
	print "Scanned " + str(len(sealcomponenttuple)) + " fwk components"

def ScanComponent(modulename, packagedir, packagename, sourcetree, componenttype):

    # Get a parser object
    myParser = ConfdbSourceParser.SourceParser()

    srcdir = packagedir + "/src/"
    interfacedir = packagedir + "/interface/"
    datadir = packagedir + "/data/"
    cvsdir = packagedir + "/CVS/"
    testdir = packagedir + "/test/"

    tagline = ""

#    tagfile = open("tags130.txt")
#    taglines = tagfile.readlines()
#    for modtag in taglines:
#	if((modtag.split()[0]).lstrip().rstrip() == packagename.lstrip().rstrip()):
#	    tagline = (modtag.split()[1]).lstrip().rstrip()

    for modtag, cvstag in tagtuple:
	if(modtag.lstrip().rstrip() == packagename.lstrip().rstrip()):
	    tagline = cvstag

    if(os.path.isdir(srcdir)):        
        srcfiles = os.listdir(srcdir)

        for srcfile in srcfiles:
            # Get all cc files
            if(srcfile.endswith(".cc")):
                interfacefile = srcfile.rstrip('.cc') + '.h'

                # Find the base class from the .h files in the interface/ directory
		if(os.path.isdir(interfacedir)):
		    myParser.ParseInterfaceFile(interfacedir + interfacefile, modulename)

                # Because some people like to put .h files in the src/ directory...
                myParser.ParseInterfaceFile(srcdir + interfacefile, modulename)

                # Now find the relevant constructor and parameter declarations
                # in the .cc files in the src/ directory
                myParser.ParseSrcFile(srcdir + srcfile, modulename, datadir, "")

		# Lastly the special case of modules declared via typedef
		myParser.HandleTypedefs(srcdir + srcfile, modulename, srcdir, interfacedir, datadir, sourcetree)

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
		modid = dbloader.ConfdbCheckModuleExistence(dbcursor,modulebaseclass,modulename,tagline)
		if(modid):
		    # If so, see if parameters need to be updated
		    print "***UPDATING MODULE " + modulename + "***"
		    dbloader.ConfdbUpdateModuleTemplate(dbcursor,modulename,modulebaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
		else:
		    # If not, make a new template
		    dbloader.ConfdbLoadNewModuleTemplate(dbcursor,modulename,modulebaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)


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
			modid = dbloader.ConfdbCheckModuleExistence(dbcursor,therealbaseclass,modulename,tagline)
			if(modid):
			    # If so, see if parameters need to be updated
			    print "***UPDATING MODULE " + modulename + "***"
			    dbloader.ConfdbUpdateModuleTemplate(dbcursor,modulename,therealbaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
			else:
			    # If not, make a new template
			    dbloader.ConfdbLoadNewModuleTemplate(dbcursor,modulename,therealbaseclass,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
		else:
		    print  "Unknown module base class " + modulebaseclass + ":" + therealbaseclass

    # This is a Service. Use the ServiceTemplate
    elif(componenttype == 2):
	# First check if this service template already exists
	servid = dbloader.ConfdbCheckServiceExistence(dbcursor,modulename,tagline)
	if(servid):
	    # If so, see if parameters need to be updated
	    print "***UPDATING SERVICE " + modulename + "***"
	    dbloader.ConfdbUpdateServiceTemplate(dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
	else:
	    # If not, make a new template
	    dbloader.ConfdbLoadNewServiceTemplate(dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)	

    # This is an ES_Source. Use the ESSourceTemplate
    elif(componenttype == 3):
	# First check if this service template already exists
	sourceid = dbloader.ConfdbCheckESSourceExistence(dbcursor,modulename,tagline)
	if(sourceid):
	    # If so, see if parameters need to be updated
	    print "***UPDATING " + modulename + "***"
	    dbloader.ConfdbUpdateESSourceTemplate(dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
	else:
	    # If not, make a new template
	    dbloader.ConfdbLoadNewESSourceTemplate(dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)	

    # This is an ED_Source. Use the EDSourceTemplate
    elif(componenttype == 4):
	# First check if this service template already exists
	sourceid = dbloader.ConfdbCheckEDSourceExistence(dbcursor,modulename,tagline)
	if(sourceid):
	    # If so, see if parameters need to be updated
	    print "***UPDATING EDSOURCE " + modulename + "***"
	    dbloader.ConfdbUpdateEDSourceTemplate(dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)
	else:
	    # If not, make a new template
	    dbloader.ConfdbLoadNewEDSourceTemplate(dbcursor,modulename,tagline,hltparamlist,hltvecparamlist,hltparamsetlist,hltvecparamsetlist)	


    # Display any cases where we ran into trouble
    myParser.ShowParamFailures()
    myParser.ResetParams()
                
if __name__ == "__main__":
    main(sys.argv[1:])

#!/usr/bin/env python

# ConfdbLoadParamsfromConfigs.py
# Main file for parsing python configs in a CMSSW release and
# loading templates to the Conf DB.
# 
# Jonathan Hollar LLNL Nov. 3, 2008

import os, string, sys, posix, tokenize, array, getopt
import ConfdbOracleModuleLoader
import FWCore.ParameterSet.Config as cms

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
    #    input_usingwhitelist = False
    input_usingwhitelist = True
    input_whitelist = [
        "CalibCalorimetry",
        "CalibMuon",
        "CalibTracker",
        "Calibration",
        "CondCore",
        "DQM",
        "DQMServices",
        "EventFilter",
        "FWCore",
        "Geometry",
        "GeometryReaders",
        "HLTrigger",
        "IOPool",
        "IORawData",
        "JetMETCorrections",
        "L1Trigger",
        "L1TriggerConfig",
        "MagneticField",
        "PhysicsTools",
        "RecoBTag",
        "RecoBTau",
        "RecoCaloTools",
        "RecoEcal",
        "RecoEgamma",
        "RecoJets",
        "RecoLocalCalo",
        "RecoLocalMuon",
        "RecoLocalTracker",
        "RecoLuminosity",
        "RecoMET",
        "RecoMuon",
        "RecoPixelVertexing",
        "RecoTauTag",
        "RecoTracker",
        "RecoVertex",
        "SimCalorimetry",
        "SimGeneral",
        "TrackPropagation",
        "TrackingTools"
        ]

    input_verbose = 0
    input_dbuser = "CMS_HLT_TEST"
    input_dbpwd = "hltdevtest1"
    input_host = "CMS_ORCOFF_INT2R"
    input_addtorelease = "none"
    input_comparetorelease = ""
    input_noload = False
    input_preferfile = ""
    
    print "Using release base: " + input_base_path

    opts, args = getopt.getopt(sys.argv[1:], "r:b:w:c:v:d:u:s:t:o:a:m:i:nh", ["release=","blacklist=","whitelist=","releasename=","verbose=","dbname=","user=","password=","dbtype=","hostname=","addtorelease=","comparetorelease=","preferredcfis=","noload=","help="])

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
        if o in ("-c","releasename="):
            input_cmsswrel = str(a)
            print "Using release " + input_cmsswrel
        if o in ("-b","blacklist="):
            input_blacklist.append(a.split(","))
            print 'Skip directories:'
            input_usingblacklist = True
        if o in ("-w","whitelist="):
            input_whitelist = []
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
        if o in ("-a","addtorelease="):
            input_addtorelease = str(a)
            input_baserelease_path = input_base_path
            input_base_path = input_addfromreldir
            print "Will create new release " + input_addtorelease + " using packages in " + input_addfromreldir
        if o in ("-m","comparetorelease="):
            print "Will update releative to release " + str(a)
            input_comparetorelease = str(a)
        if o in ("-i","preferredcfis="):
            input_preferfile = str(a)
        if o in ("-n","noload="):
            print "Will parse release without loading to the DB"
            input_noload = True
        if o in ("-h","help="):
            print "Help menu for ConfdbSourceToDB"
            print "\t-r <CMSSW release (default is the CMSSW_VERSION envvar)>"
            print "\t-c <Manually set the name of the release>"
            print "\t-m <Release to compare to when updating>"
            print "\t-w <Comma-delimited list of packages to parse>"
            print "\t-b <Comma-delimited list of packages to ignore>"
            print "\t-i <Text file containing list of preferred cfi.py files>"
            print "\t-v <Verbosity level (0-3)>"
            print "\t-d <Name of the database to connect to>"
            print "\t-u <User name to connect as>"
            print "\t-s <Database password>"
            print "\t-o <Hostname>"
            print "\t-h Print this help menu"
            return

    confdbjob = ConfdbLoadParamsfromConfigs(input_cmsswrel,input_baserelease_path,input_whitelist,input_blacklist,input_usingwhitelist,input_usingblacklist,input_verbose,input_dbuser,input_dbpwd,input_host,input_noload,input_addtorelease,input_comparetorelease,input_preferfile)
    confdbjob.BeginJob()

class ConfdbLoadParamsfromConfigs:
    def __init__(self,clirel,clibasereleasepath,cliwhitelist,cliblacklist,cliusingwhitelist,cliusingblacklist,cliverbose,clidbuser,clidbpwd,clihost,clinoload,cliaddtorelease,clicomparetorelease,clipreferfile):

        self.dbname = ''
        self.dbuser = clidbuser
        self.verbose = int(cliverbose)
        self.dbpwd = clidbpwd
        self.dbhost = clihost
        self.verbose = cliverbose
        self.noload = clinoload
        self.addtorelease = cliaddtorelease
        self.comparetorelease = clicomparetorelease
        self.prefercfifile = clipreferfile
        self.componenttable = ''
        self.componentname = ''
        self.softpackageid = ''

        # Track CVS tags
        self.cvstag = ''
        self.packageid = ''
        self.tagtuple = []
        self.basereltagtuple = []

	# Get a Conf DB connection here. Only need to do this once at the 
	# beginning of a job.
        self.dbloader = ConfdbOracleModuleLoader.ConfdbOracleModuleLoader(self.verbose,self.addtorelease,self.comparetorelease)
        self.dbcursor = self.dbloader.ConfdbOracleConnect(self.dbname,self.dbuser,self.dbpwd,self.dbhost)

        self.modtypedict = {}
        self.paramtypedict = {}
        self.paramtabledict = {}
        self.preferredcfilist = []
        self.usedcfilist = []
        self.thepyfile = ''
        self.usedcfifile = "listofusedcfis." + str(clirel) + ".log"
        
	# Deal with package tags for this release.
	self.cmsswrel = clirel
        self.cmsswrelid = 0
	self.baserelease_path = clibasereleasepath
        self.base_path = clibasereleasepath
        self.whitelist = cliwhitelist
        self.blacklist = cliblacklist
        self.usingwhitelist = cliusingwhitelist
        self.usingblacklist = cliusingblacklist

        self.oldcmsswrelid = 0

        # Global bookkeeping
        self.localseq = 0
        self.nesting = []
        self.finishedtemplates = []

        # Logfile for parsing output
        self.outputlogfile = "parse." + str(clirel) + "." + str(clihost) + ".log"
        self.outputlogfilehandle = None

    def BeginJob(self):
	# List of all available modules
	sealcomponenttuple = []

	# Track all modules which will be modified
	modifiedmodules = []

	source_tree = self.base_path + "//src/"

        self.outputlogfilehandle = open(self.outputlogfile, 'w')
        
	print "The  source tree is: " + source_tree

        if(not (os.path.isdir(self.base_path))):
            print 'Fatal error: release source tree not found. Exiting now'
            return

        # Find this release in the DB
        self.dbcursor.execute("SELECT SoftwareReleases.releaseId FROM SoftwareReleases WHERE (releaseTag = '" + self.cmsswrel + "')")
        #        self.dbcursor.execute("SELECT SoftwareReleases.releaseId FROM SoftwareReleases WHERE (releaseTag = 'CMSSW_3_1_X_2009-04-07-0000_TEST')")
        tmprelid = self.dbcursor.fetchone()

        if(tmprelid):
            self.cmsswrelid = tmprelid[0]
        else:
            self.dbcursor.execute("INSERT INTO SoftwareReleases (releaseTag) VALUES ('" + str(self.cmsswrel) + "')")
            self.dbcursor.execute("SELECT ReleaseId_Sequence.currval from dual")
            self.cmsswrelid = self.dbcursor.fetchone()[0]
            print "Inserted new release " + str(self.cmsswrel) + " with key = " + str(self.cmsswrelid)

        if(self.comparetorelease != ""):
            self.dbcursor.execute("SELECT SoftwareReleases.releaseId FROM SoftwareReleases WHERE (releaseTag = '" + self.comparetorelease + "')") 
            tmprelid = self.dbcursor.fetchone()

            if(tmprelid):
                self.oldcmsswrelid = tmprelid[0]

        # Find CVS tags
        self.GetCVSTags()

        # Do some one-time operations - get dictionaries of parameter, module,
        # and service type mappings so we don't have to do this every time
        self.dbcursor.execute("SELECT ParameterTypes.paramType, ParameterTypes.paramTypeId FROM ParameterTypes")
        temptuple = self.dbcursor.fetchall()
        for temptype, tempname in temptuple:
            self.paramtypedict[temptype] = tempname

        self.dbcursor.execute("SELECT ModuleTypes.type, ModuleTypes.typeId FROM ModuleTypes")
        temptuple = self.dbcursor.fetchall()
        for temptype, tempname in temptuple:
            self.modtypedict[temptype] = tempname

        self.paramtabledict = {"int32":"Int32ParamValues",
                               "vint32":"VInt32ParamValues",
                               "uint32":"UInt32ParamValues",
                               "vuint32":"VUInt32ParamValues",
                               "int64":"Int64ParamValues",
                               "vint64":"VInt64ParamValues",
                               "uint64":"UInt64ParamValues",
                               "vuint64":"VUInt64ParamValues",
                               "bool":"BoolParamValues",
                               "double":"DoubleParamValues",
                               "vdouble":"VDoubleParamValues",
                               "string":"StringParamValues",
                               "vstring":"VStringParamValues",
                               "InputTag":"InputTagParamValues",
                               "VInputTag":"VInputTagParamValues",
                               "EventID":"EventIDParamValues",
                               "VEventID":"VEventIDParamValues",
                               "FileInPath":"FileInPathParamValues"}

        if(self.prefercfifile != ""):
            self.CreatePreferredCfiList()

        self.VerbosePrint("\n",0)
        self.VerbosePrint("*********************",0)
        self.VerbosePrint("* Beginning parsing *",0)
        self.VerbosePrint("*********************",0) 
        self.VerbosePrint("\n",0)
                               
	# Get package list for this release
	packagelist = os.listdir(source_tree)

        self.VerbosePrint("Will parse packages in the whitelist",1)
        for okpackage in self.whitelist:
            self.VerbosePrint("\t" + str(okpackage),1)

	# Start decending into the source tree
	for package in packagelist:
            
            if(not (os.path.isdir(self.base_path))):
                print 'Fatal error: release source tree not found. Exiting now'
                return
            
            if(self.usingwhitelist == True):
                skip = True
                for whitelists in self.whitelist:
                    if str(whitelists) == str(package):
                        skip = False
                if(skip == True):
                    continue
            if(self.usingblacklist == True):
                skip = False
                for blacklists in self.blacklist:
                    if str(blacklists) == str(package):
                        skip = True
                if(skip == True):
                    continue

	    # Check if this is really a directory
	    if(os.path.isdir(source_tree + package)):

		subdirlist = os.listdir(source_tree + package)

		for subdir in subdirlist:
                    if(subdir.startswith(".")):
                       continue
		    # Check if the user really wants to use this package

		    packagedir = source_tree + package + "/" + subdir
		
                    self.VerbosePrint("Scanning package: " + package + "/" + subdir, 0)
                
		    srcdir = packagedir + "/src/"
		    testdir = packagedir + "/test/"
		    pluginsdir = packagedir + "/plugins/"
                    pydir = packagedir + "/python/"

                    if(not os.path.isdir(pydir)):
                        continue

                    # Retrieve the CVS tag
                    packagename = package+"/"+subdir
                    for modtag, cvstag in self.tagtuple:
                        if(modtag.lstrip().rstrip() == packagename.lstrip().rstrip()):
                            self.cvstag = cvstag
                            self.VerbosePrint("\tCVS tag: " + cvstag,0)

                    self.GetPackageID(package,subdir)
                            
                    pyfiles = os.listdir(pydir)

                    # Try to recursively add cfi's contained in subdirectories of python/.
                    for pyfile in pyfiles:
                        if(os.path.isdir(pydir + "/" + pyfile)):
                            subpyfiles = os.listdir(pydir + "/" + pyfile)
                            for subpyfile in subpyfiles:
                                pyfiles.append(pyfile + "." + subpyfile)
                                
                    thesubsystempackage = pydir.split('src/')[1].split('/data/')[0].lstrip().rstrip()
                    thesubsystempackagenopy = thesubsystempackage.split("/python")[0]
                    thesubsystem = thesubsystempackage.split('/')[0]
                    thepackage = thesubsystempackage.split('/')[1]

                    # First check if there are any preferred cfi files to use for this Package/Subsystem.
                    # If so, get the parameters from there.
                    for preferredsubpackage, preferredcfi in self.preferredcfilist:
                        if thesubsystempackagenopy == preferredsubpackage: 
                            self.VerbosePrint("Using the preferred cfi.py: " + preferredcfi + " for " + thesubsystempackagenopy,0)
                            pyfile = preferredcfi
                            thecomponent = pyfile.split('.py')[0]

                            try:
                                self.ExtendTheCfi(pyfile, pydir)

                            # cfi files are not guaranteed to be valid :( If we find an invalid one, catch the
                            # exception from the python config API and move on to the next
                            except FloatingPointError:
                                print 'Dummy exception - this should never happen!!!'
                            except NameError:
                                print "Name Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue
                            except TypeError:
                                print "Type Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue
                            except ImportError:
                                print "Import Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue
                            except SyntaxError:
                                print "Syntax Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue
                             
                    # Now look through all other cfi files.
                    for pyfile in pyfiles:
                        if(pyfile.endswith("_cfi.py")):
                            thecomponent = pyfile.split('.py')[0]

                            try:
                                self.ExtendTheCfi(pyfile, pydir)

                            # cfi files are not guaranteed to be valid :( If we find an invalid one, catch the
                            # exception from the python config API and move on to the next
                            except FloatingPointError:
                                print 'Dummy exception - this should never happen!!!'
                            except NameError:
                                print "Name Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue
                            except TypeError:
                                print "Type Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue
                            except ImportError:
                                print "Import Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue
                            except SyntaxError:
                                print "Syntax Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue
                            
        # Commit and disconnect to be compatible with either INNODB or MyISAM
        self.dbloader.ConfdbExitGracefully()
        self.GenerateUsedCfiTable()
        self.outputlogfilehandle.close()

    def ExtendTheCfi(self, pyfile, pydir):

        self.VerbosePrint("Extending the python cfi file " + str(pyfile),0)
        thecomponent = pyfile.split('.py')[0]
        thebasecomponent = thecomponent.split('_cfi')[0]
        
        # Construct the py-cfi to import
        thesubsystempackage = pydir.split('src/')[1].split('/data/')[0].lstrip().rstrip()
        thesubsystem = thesubsystempackage.split('/')[0]
        thepackage = thesubsystempackage.split('/')[1]
        importcommand = "import " + thesubsystem + "." + thepackage + "." + thecomponent
        self.thepyfile = thesubsystem + "/" + thepackage + "/" + "python/" + pyfile


        process = cms.Process("MyProcess")

        exec importcommand
        # Now create a process and construct the command to extend it with the py-cfi
        theextend = "process.extend(" + thesubsystem + "." + thepackage + "." + thecomponent + ")"
        eval(theextend)
        
        myproducers = process.producers_()
        self.componenttable = "ModuleTemplates"
        self.FindParamsFromPython(thesubsystem, thepackage, myproducers,"EDProducer")
        
        myfilters = process.filters_()
        self.componenttable = "ModuleTemplates"
        self.FindParamsFromPython(thesubsystem, thepackage, myfilters,"EDFilter") 
        
        myservices = process.services_()
        self.componenttable = "ServiceTemplates"                                
        self.FindParamsFromPython(thesubsystem, thepackage, myservices,"Service") 
        
        myanalyzers = process.analyzers_()
        self.componenttable = "ModuleTemplates"
        self.FindParamsFromPython(thesubsystem, thepackage, myanalyzers,"EDAnalyzer") 
        
        myoutputmodules = process.outputModules_()
        self.componenttable = "ModuleTemplates"
        self.FindParamsFromPython(thesubsystem, thepackage, myoutputmodules,"OutputModule")
        
        myessources = process.es_sources_()
        self.componenttable = "ESSourceTemplates"                                
        self.FindParamsFromPython(thesubsystem, thepackage, myessources,"ESSource") 
        
        myesproducers = process.es_producers_()
        self.componenttable = "ESModuleTemplates"                                
        self.FindParamsFromPython(thesubsystem, thepackage, myesproducers,"ESModule") 
            
    def DoPsetRecursion(self,psetval,psetname,psetsid):

        params = psetval.parameters_()
        subobjectsuperid = -1

        nextseqid = -1
        self.dbcursor.execute("SELECT sequenceNb FROM SuperIdParameterAssoc WHERE superId = " + str(psetsid) + " ORDER BY sequenceNb DESC")
        nextpseqid = self.dbcursor.fetchone()
        if(nextpseqid):
            nextseqid = nextpseqid[0]
        self.dbcursor.execute("SELECT sequenceNb FROM SuperIdParamSetAssoc WHERE superId = " + str(psetsid) + " ORDER BY sequenceNb DESC")
        nextpsetseqid = self.dbcursor.fetchone()
        if(nextpsetseqid):
            if(nextpsetseqid[0] > nextseqid):
                nextseqid = nextpsetseqid[0]
        self.dbcursor.execute("SELECT sequenceNb FROM SuperIdVecParamSetAssoc WHERE superId = " + str(psetsid) + " ORDER BY sequenceNb DESC")
        nextvpsetseqid = self.dbcursor.fetchone()
        if(nextvpsetseqid):
            if(nextvpsetseqid[0] > nextseqid):
                nextseqid = nextvpsetseqid[0]
                
        self.VerbosePrint("The last inserted sequenceNb for PSet " + str(psetname) + " component was " + str(nextseqid),2)
        self.localseq = 0
        self.localseq = nextseqid + 1
        self.VerbosePrint("The first parameter for PSet " + str(psetname) + " will be inserted with sequenceNb " + str(self.localseq),2)

        for paramname, paramval in params.iteritems():
            if(paramval.configTypeName() == "PSet" or paramval.configTypeName() == "untracked PSet"):
                subobjectsuperid = self.LoadUpdatePSet(paramname,psetname,paramval,psetsid)
                self.nesting.append(('PSet',paramname,subobjectsuperid))
                prerecursionseq = self.localseq
                self.DoPsetRecursion(paramval,psetname+"."+paramname,subobjectsuperid)
                self.localseq = prerecursionseq
                del self.nesting[-1]

            elif(paramval.configTypeName().find("VPSet") == -1):
                self.VerbosePrint("\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval),1)
                self.LoadUpdateParam(paramname,psetname,paramval,psetsid)
                
            else:
                self.VerbosePrint("\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval.configTypeName()) + "[" + str(len(paramval)) + "]",1)
                i = 1
                for vpsetentry in paramval:
                    subobjectsuperid = self.LoadUpdatePSet(paramname,psetname,paramval,psetsid)
                    self.nesting.append(('PSet',paramname,subobjectsuperid))
                    prerecursionseq = self.localseq
                    self.DoPsetRecursion(vpsetentry,psetname+'['+str(i)+']',subobjectsuperid)
                    self.localseq = prerecursionseq
                    del self.nesting[-1]
                    i = i + 1
    
    def FindParamsFromPython(self, thesubsystem, thepackage, mycomponents, componenttype):

        self.nesting = []
        for name, value in mycomponents.iteritems():
            psetname = "TopLevel"
            self.VerbosePrint("(" + str(componenttype) + " " + value.type_() + ") " + thesubsystem + "." + thepackage + "." + name, 1)

            template = thesubsystem+thepackage+(value.type_())
            if(not template in self.finishedtemplates):
                self.finishedtemplates.append(template)
            else:
                self.VerbosePrint("Skipping the cfi for " + name + " because a template was already loaded for " + value.type_(),1)
                return

            self.componentname = value.type_()
            self.usedcfilist.append((self.thepyfile,self.componentname))
            componentsuperid = self.LoadUpdateComponent(value.type_(),componenttype)
            objectsuperid = -1
            vobjectsuperid = -1


            nextseqid = -1
            self.dbcursor.execute("SELECT sequenceNb FROM SuperIdParameterAssoc WHERE superId = " + str(componentsuperid) + " ORDER BY sequenceNb DESC")
            nextpseqid = self.dbcursor.fetchone()
            if(nextpseqid):
                nextseqid = nextpseqid[0]
            self.dbcursor.execute("SELECT sequenceNb FROM SuperIdParamSetAssoc WHERE superId = " + str(componentsuperid) + " ORDER BY sequenceNb DESC")
            nextpsetseqid = self.dbcursor.fetchone()
            if(nextpsetseqid):
                if(nextpsetseqid[0] > nextseqid):
                    nextseqid = nextpsetseqid[0]
            self.dbcursor.execute("SELECT sequenceNb FROM SuperIdVecParamSetAssoc WHERE superId = " + str(componentsuperid) + " ORDER BY sequenceNb DESC")
            nextvpsetseqid = self.dbcursor.fetchone() 
            if(nextvpsetseqid):
                if(nextvpsetseqid[0] > nextseqid):
                    nextseqid = nextvpsetseqid[0]

            self.VerbosePrint("The last inserted sequenceNb for this component was " + str(nextseqid),2)
            self.localseq = 0
            self.localseq = nextseqid + 1
            self.VerbosePrint("The first parameter will be inserted with sequenceNb " + str(self.localseq),2)
            
            if(componentsuperid != -1):
                params = value.parameters_()

                for paramname, paramval in params.iteritems():
                    if(paramval.configTypeName() == "PSet" or paramval.configTypeName() == "untracked PSet"):
                        psetname = paramname
                        objectsuperid = self.LoadUpdatePSet(paramname,psetname,paramval,componentsuperid)
                        self.nesting.append(('PSet',paramname,objectsuperid))
                        prerecursionseq = self.localseq
                        self.DoPsetRecursion(paramval,paramname,objectsuperid)
                        self.localseq = prerecursionseq
                        del self.nesting[-1]
                    
                    elif(paramval.configTypeName().find("VPSet") == -1):
                        self.VerbosePrint("\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval), 1)
                        self.LoadUpdateParam(paramname,psetname,paramval,componentsuperid)

                    else:
                        vobjectsuperid = self.LoadUpdateVPSet(paramname,psetname,paramval,componentsuperid)
                        vpsetname = paramname
                        self.nesting.append(('VPSet',paramname,objectsuperid))
                        self.VerbosePrint("\t\t" + str(paramname) + "\t" + str(paramval.configTypeName()) + "[" + str(len(paramval)) + "]", 1)
                        psetname = str(paramval.configTypeName()) + "[" + str(len(paramval)) + "]"
                        sizeofvpset = self.VPSetSize(vobjectsuperid)
                        if(sizeofvpset == 0):
                            i = 1
                            prevpsetiterseq = self.localseq
                            for vpsetentry in paramval:
                                self.localseq = i-1
                                vobjectmembersuperid = self.LoadUpdatePSet(paramname,psetname,paramval,vobjectsuperid) 
                                self.nesting.append(('PSet','VPSet['+str(i)+']',vobjectmembersuperid))
                                prerecursionseq = self.localseq
                                self.DoPsetRecursion(vpsetentry,paramname+'['+str(i)+']',vobjectmembersuperid)
                                self.localseq = prerecursionseq
                                del self.nesting[-1]
                                i = i + 1
                            self.localseq = prevpsetiterseq
                            del self.nesting[-1]
                        else:
                            self.VerbosePrint("\t\t"  + str(paramname) + " already appears with " + str(sizeofvpset) + " entries. Will not add more entries",1)

    def FindObjectSuperId(self):
        print "Not yet"
        
    def LoadUpdateComponent(self,componentname,componenttype):

        componenttable = self.componenttable
        modtypestr = ''
        returnid = -1
            
        selectstring = "SELECT " + componenttable + ".superId, " + componenttable + ".name, " + componenttable + ".cvstag, " + componenttable + ".packageId FROM " + componenttable + " JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = " + componenttable + ".superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.cmsswrelid) + ") AND (" + componenttable + ".name = '" + componentname + "')"
        self.VerbosePrint(selectstring, 3)

        self.dbcursor.execute(selectstring)

        componentsuperid = self.dbcursor.fetchone()

        if(componentsuperid):
            # See if this template already exists for this release - if so, just return its superId
            returnid = componentsuperid[0]
        else:
            doloadupdate = False
            
            if(self.comparetorelease != ""):
                # If not, first check if it existed in the previous release
                oldcvstag = self.GetOldReleaseCVSTag(componentname)
                
                if(self.cvstag == oldcvstag):
                    self.VerbosePrint("The CVS tag is unchanged: " + str(oldcvstag) + " to " + str(self.cvstag),2)
                    self.ReassociateSuperId(componentname)
                    
                    # If so, just reassociate the old template to the new release
                    doloadupdate = False
                else:
                    self.VerbosePrint("The CVS tag changed: " + str(oldcvstag) + " to " + str(self.cvstag),2)
                    doloadupdate = True
            else:
                doloadupdate = True

            # See if we need to load/update this component. Do this if the component doesn't already exist in the release and either:
            # 1) The component wasn't found in the previous release
            # 2) The component exists in the previous release but the CVS tag has changed
            # 3) The user isn't updating from a previous release
            if(doloadupdate == True):
                # Let's add it.
                newsuperid = -1
                self.dbcursor.execute("INSERT INTO SuperIds VALUES('')")

                self.dbcursor.execute("SELECT SuperId_Sequence.currval from dual")
                newsuperid = (self.dbcursor.fetchall()[0])[0]

                # Attach this template to the currect release
                self.dbcursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.cmsswrelid) + ")")
            
                # Now create a new module
                insertstring = ''
                if(self.componenttable == "ModuleTemplates"):
                    modbaseclassid = self.modtypedict[componenttype]
                    insertstring = "INSERT INTO " + self.componenttable + "(superId, typeId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + componentname + "', '" + self.cvstag  + "', '" + str(self.softpackageid) + "')"
                else:
                    insertstring = "INSERT INTO " + self.componenttable + "(superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + componentname + "', '" + self.cvstag  + "', '" + str(self.softpackageid) + "')"

                self.VerbosePrint(insertstring, 3)
                self.dbcursor.execute(insertstring)

                returnid = newsuperid

        return returnid

    def LoadUpdateParam(self,parametername,psetname,pval,sid):

        returnid = 0
        paramistracked = 0
        unmodifiedparamid = 0

        parametervalue = pval.value()
        parametertype = pval.configTypeName()
        parametertracked = pval.isTracked()

        #For now, ignore the cms.SecSource type (used for mixing pileup in MC)
        if(parametertype.find("secsource") != -1):
            return

        if(parametertype.find("untracked") != -1):
            parametertype = parametertype.split("untracked")[1].lstrip().rstrip()

        # Reformat representations of Booleans for python -> Oracle

        # Tracked true/false -> 1/0
        if(parametertracked == True):
            paramistracked =  1
        else:
            paramistracked = 0

        # Boolean parameter true/false -> 1/0
        if(parametertype == "bool"):
            if(parametervalue == True):
                parametervalue = str(1)
            if(parametervalue == False):
                parametervalue = str(0)

        selectstr = "SELECT SuperIdParameterAssoc.paramId FROM SuperIdParameterAssoc JOIN Parameters ON (Parameters.name = '" + parametername + "') WHERE (SuperIdParameterAssoc.superId = " + str(sid) + ") AND (SuperIdParameterAssoc.paramId = Parameters.paramId)"

        self.VerbosePrint(selectstr, 3)
        self.dbcursor.execute(selectstr)

        paramid = self.dbcursor.fetchone()

        if(paramid):
            returnid = paramid[0]
        else:
            parametertypeint = self.paramtypedict[parametertype]
            paramtable = self.paramtabledict[parametertype]

            # Check parameter value from old release here
            if(self.comparetorelease != ""):                
                unmodifiedparamid = self.CompareParamToOldRelease(parametername,parametervalue,parametertype)

            # Add this parameter to the new release
            if((unmodifiedparamid == 0) or (self.comparetorelease == "")):
                insertstr1 = "INSERT INTO Parameters (paramTypeId, name, tracked) VALUES (" + str(parametertypeint) + ", '" + parametername + "', " + str(paramistracked) + ")"
                self.VerbosePrint(insertstr1, 3)
                self.dbcursor.execute(insertstr1)
                
                self.dbcursor.execute("SELECT ParamId_Sequence.currval from dual")
                newparamid = self.dbcursor.fetchone()[0] 
                
                insertstr2 = "INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(self.localseq) + ")"
                self.VerbosePrint(insertstr2, 3)
                self.dbcursor.execute(insertstr2)                                                

                returnid = newparamid
                
                self.VerbosePrint('Added ' + str(parametername) + ' with paramId = ' + str(newparamid),2)                            
                if(self.nesting != []):
                    self.VerbosePrint('The nesting is:',2)
                    self.VerbosePrint(self.nesting,2)

                self.localseq = self.localseq + 1
        
                if((not parametertype.startswith('v')) and (not parametertype.startswith('V'))):
                    insertstr3 = "INSERT INTO " + str(paramtable) + " (paramId, value) VALUES (" + str(newparamid) + ", '" + str(parametervalue) + "')"
                    self.VerbosePrint(insertstr3, 3)
                    if(str(parametervalue).find("'") == -1):                        
                        self.dbcursor.execute(insertstr3)
                    
                else:
                    paramindex = 1
                    for parametervectorvalue in parametervalue:
                        insertstr3 = "INSERT INTO " + str(paramtable) + " (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(paramindex) + ", '" + str(parametervectorvalue) + "')"
                        self.VerbosePrint(insertstr3, 3)
                        if(str(parametervectorvalue).find("'") == -1):
                            # Protect against loading empty VInputTags
                            if(str(parametervectorvalue) != ''):
                                self.dbcursor.execute(insertstr3)
                                paramindex = paramindex + 1

            # Reassociate this parameter from the previous release
            if((unmodifiedparamid > 0) and (self.comparetorelease != "")):
                insertstr4 = "INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(sid) + ", " + str(unmodifiedparamid) + ", " + str(self.localseq) + ")"
                nextseqid = self.dbcursor.fetchone()
                if(nextseqid):
                    self.localseq = nextseqid[0] + 1
                                        
        return returnid

    def LoadUpdatePSet(self,parametername,psetname,pval,sid):

        returnid = 0
        paramistracked = 0

        parametervalue = pval.value()
        parametertype = pval.configTypeName()
        parametertracked = pval.isTracked()

        if(psetname.find("[") != -1 and psetname.find("]") != -1):
            parametername = ''

        # Reformat representations of Booleans for python -> Oracle

        # Tracked true/false -> 1/0
        if(parametertracked == True):
            paramistracked =  1
        else:
            paramistracked = 0
                                                 
        selectstr = "SELECT SuperIdParamSetAssoc.psetId FROM SuperIdParamSetAssoc JOIN ParameterSets ON (ParameterSets.name = '" + parametername + "') WHERE (SuperIdParamSetAssoc.superId = " + str(sid) + ") AND (SuperIdParamSetAssoc.psetId = ParameterSets.superId)"

        self.VerbosePrint(selectstr, 3)
        self.dbcursor.execute(selectstr)

        paramid = self.dbcursor.fetchone()
        
        if(paramid):
            returnid = paramid[0]
        else:
            self.dbcursor.execute("INSERT INTO SuperIds VALUES('')")

            self.dbcursor.execute("SELECT SuperId_Sequence.currval from dual")
            newparamid = self.dbcursor.fetchone()[0]

            insertstr1 = "INSERT INTO ParameterSets (superId, name, tracked) VALUES (" + str(newparamid) + ", '" + parametername + "', " + str(paramistracked) + ")"
            self.VerbosePrint(insertstr1, 3)
            self.dbcursor.execute(insertstr1)
                                                                                                 
            insertstr2 = "INSERT INTO SuperIdParamSetAssoc (superId, psetId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(self.localseq) + ")"
            self.VerbosePrint(insertstr2, 3)
            self.dbcursor.execute(insertstr2)

            self.localseq = self.localseq + 1
            
            self.VerbosePrint('Added PSet ' + str(parametername) + ' with superId = ' + str(newparamid),2)
            
            returnid = newparamid

        return returnid

    def LoadUpdateVPSet(self,parametername,psetname,pval,sid):

        returnid = 0
        paramistracked = 0

        parametervalue = pval.value()
        parametertype = pval.configTypeName()
        parametertracked = pval.isTracked()

        # Reformat representations of Booleans for python -> Oracle

        # Tracked true/false -> 1/0
        if(parametertracked == True):
            paramistracked =  1
        else:
            paramistracked = 0
                                                 
        selectstr = "SELECT SuperIdVecParamSetAssoc.vpsetId FROM SuperIdVecParamSetAssoc JOIN VecParameterSets ON (VecParameterSets.name = '" + parametername + "') WHERE (SuperIdVecParamSetAssoc.superId = " + str(sid) + ") AND (SuperIdVecParamSetAssoc.vpsetId = VecParameterSets.superId)"

        self.VerbosePrint(selectstr, 3)
        self.dbcursor.execute(selectstr)

        paramid = self.dbcursor.fetchone()
        
        if(paramid):
            returnid = paramid[0]
        else:
            self.dbcursor.execute("INSERT INTO SuperIds VALUES('')")

            self.dbcursor.execute("SELECT SuperId_Sequence.currval from dual")
            newparamid = self.dbcursor.fetchone()[0]

            insertstr1 = "INSERT INTO VecParameterSets (superId, name, tracked) VALUES (" + str(newparamid) + ", '" + parametername + "', " + str(paramistracked) + ")"
            self.VerbosePrint(insertstr1, 3)
            self.dbcursor.execute(insertstr1)
                                                                                                 
            insertstr2 = "INSERT INTO SuperIdVecParamSetAssoc (superId, vpsetId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(self.localseq) + ")"
            self.VerbosePrint(insertstr2, 3)
            self.dbcursor.execute(insertstr2)

            self.localseq = self.localseq + 1
            
            self.VerbosePrint('Added VPSet ' + str(parametername) + ' with superId = ' + str(newparamid),2)
            
            returnid = newparamid

        return returnid

    def VPSetSize(self,vpsetsid): 

        vpsetlen = 0

        selectstr = "SELECT * FROM SuperIdParamSetAssoc WHERE superId = " + str(vpsetsid) + " ORDER BY psetId DESC"
        self.VerbosePrint(selectstr, 3)
        self.dbcursor.execute(selectstr)

        tmppsetentries = (self.dbcursor.fetchone())
        if(tmppsetentries):
            vpsetlen = tmppsetentries[0]

        return vpsetlen
        
    def GetCVSTags(self):
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


    def CompareParamToOldRelease(self,parametername,parametervalue,parametertype):

        componenttable = self.componenttable
        modtypestr = ''
        matchingparamid = 0

        # First find the old module 
        selectstr = "SELECT " + componenttable + ".superId, " + componenttable + ".name, " + componenttable + ".cvstag, " + componenttable + ".packageId FROM " + componenttable + " JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = " + componenttable + ".superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.oldcmsswrelid) + ") AND (" + componenttable + ".name = '" + self.componentname + "')"
        self.VerbosePrint(selectstr, 3)
        
        self.dbcursor.execute(selectstr)

        oldcomponentsuperid = ''
        tmpoldcomponentsuperid = (self.dbcursor.fetchone())
        if(tmpoldcomponentsuperid):
            oldcomponentsuperid = tmpoldcomponentsuperid[0]

        # Then work downwards through the PSet nesting to find the old parameter value to compare to

        if(len(self.nesting) == 0):
            oldcomponentsuperid = tmpoldcomponentsuperid[0]
        else:
            for fwkcomponenttype, fwkcomponentname, newsuperid in self.nesting:
                if(fwkcomponenttype == 'PSet' and fwkcomponentname.find("VPSet") == -1):
                    selectstr = "SELECT ParameterSets.superId FROM ParameterSets JOIN SuperIdParamSetAssoc ON (SuperIdParamSetAssoc.superId = " + str(oldcomponentsuperid) + ") WHERE (ParameterSets.name = '" + str(fwkcomponentname) + "') AND (ParameterSets.superId = SuperIdParamSetAssoc.psetId)"
                
                    self.VerbosePrint(selectstr,3)
                    self.dbcursor.execute(selectstr)
                
                    tmpcomponentsuperid = self.dbcursor.fetchone()
                    if(tmpcomponentsuperid):
                        oldcomponentsuperid = tmpcomponentsuperid[0]

        if(oldcomponentsuperid):
            # The module and sub-PSet exists in the old release, now check if the parameter does too
#            selectstr = "SELECT SuperIdParameterAssoc.paramId FROM SuperIdParameterAssoc JOIN Parameters ON (Parameters.name = '" + parametername + "') WHERE (SuperIdParameterAssoc.superId = " + str(oldcomponentsuperid) + ") AND (SuperIdParameterAssoc.paramId = Parameters.paramId)"

            selectstr2 = "SELECT Parameters.paramId, Parameters.paramTypeId FROM Parameters JOIN SuperIdParameterAssoc ON (SuperIdParameterAssoc.superId = " + str(oldcomponentsuperid) + ") WHERE (Parameters.name = '" + str(parametername) + "') AND (Parameters.paramId = SuperIdParameterAssoc.paramId)"

            self.VerbosePrint(selectstr2, 3)
            self.dbcursor.execute(selectstr2)

            oldparamid = ''
            tmpoldparamid = self.dbcursor.fetchone()
            if(tmpoldparamid):
                oldparamid = tmpoldparamid[0]

            parametertypeint = self.paramtypedict[parametertype]
            paramtable = self.paramtabledict[parametertype]

            if((not parametertype.startswith('v')) and (not parametertype.startswith('V')) and (oldparamid != '')):
                selectstr3 = "SELECT  " + paramtable + ".value FROM " + paramtable + " WHERE " + paramtable + ".paramId = " + str(oldparamid)
                
                self.VerbosePrint(selectstr3, 3)
                self.dbcursor.execute(selectstr3)
                                                                
                tmpoldparamval = self.dbcursor.fetchone()
                if(tmpoldparamval):
                    oldparamval = tmpoldparamval[0]

                    if((str(oldparamval) != str(parametervalue)) or (str(parametervalue) == '' and str(oldparamval) == 'None')):
                        self.VerbosePrint("\tParameter " + str(parametername) + " changed from " + str(oldparamval) + " to " + str(parametervalue),2)
                        matchingparamid = oldparamid
                    else:
                        self.VerbosePrint("\tParameter " + str(parametername) + " is unchanged with value " + str(parametervalue),2)
                else:
                    self.VerbosePrint("\tParameter " + str(parametername) + " is new",2)                    
                    matchingparamid = 0

        return matchingparamid

    def GetOldReleaseCVSTag(self,componentname):
        oldcvstag = "none"
        
        selectstring = "SELECT " + self.componenttable + ".superId, " + self.componenttable + ".name, " + self.componenttable + ".cvstag, " + self.componenttable + ".packageId FROM " + self.componenttable + " JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = " + self.componenttable + ".superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.oldcmsswrelid) + ") AND (" + self.componenttable + ".name = '" + componentname + "')"

        self.VerbosePrint(selectstring,3)
        self.dbcursor.execute(selectstring)
        
        oldmodule = self.dbcursor.fetchone()

        if(oldmodule):
            oldcvstag = oldmodule[2]

        return oldcvstag    

    def ReassociateSuperId(self,componentname): 
        oldsuperid = -1

        selectstring = "SELECT " + self.componenttable + ".superId, " + self.componenttable + ".name, " + self.componenttable + ".cvstag, " + self.componenttable + ".packageId FROM " + self.componenttable + " JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = " + self.componenttable + ".superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.oldcmsswrelid) + ") AND (" + self.componenttable + ".name = '" + componentname + "')"
        
        self.VerbosePrint(selectstring,3)
        self.dbcursor.execute(selectstring)
        
        oldmodule = self.dbcursor.fetchone()
        
        if(oldmodule):
            oldsuperid = oldmodule[0]
            insertstr = "INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.cmsswrelid) + ")"
            self.VerbosePrint(insertstr,3)
            self.dbcursor.execute(insertstr)

    def GetPackageID(self,subsystem,package):

        packageid = self.dbloader.ConfdbInsertPackageSubsystem(self.dbcursor,subsystem,package)

        self.softpackageid = packageid

        self.VerbosePrint("Package ID = " + str(self.softpackageid), 2)

    def VerbosePrint(self,message,severity):
        if(self.verbose >= severity):
            print str(message)
            self.outputlogfilehandle.write(str(message) + "\n")

    def CreatePreferredCfiList(self):

        # If there is a preferred cfi.py for a given package, define it here.
        # Need 1-to-many mapping, so it's a list instead of a dictionary...

        if(not os.path.isfile(self.prefercfifile)):
            self.VerbosePrint("Preferred cfi.py file " + str(self.prefercfifile) + " not found. Will load parameters from the first valid cfi.py file encountered",0)
            return
            
        preffile = open(self.prefercfifile)
        preflines = preffile.readlines()
        self.VerbosePrint("Reading list of preferred cfi.py files from " + str(self.prefercfifile),1)
        for prefline in preflines:
            prefpackage = str((prefline.split())[0])
            prefcfi = str((prefline.split())[1])
            self.preferredcfilist.append((prefpackage,prefcfi)) 
            self.VerbosePrint("\t" + str(prefpackage) + " " + str(prefcfi),1)


    def GenerateUsedCfiTable(self):
        usedcfihandle = open(self.usedcfifile, 'w')
        
        for thecfipy, thefwkcomponent in self.usedcfilist:
            usedcfihandle.write(str(thefwkcomponent).ljust(50) + str(thecfipy).ljust(200) + "\n")

        usedcfihandle.close()
        
if __name__ == "__main__":
    main(sys.argv[1:])

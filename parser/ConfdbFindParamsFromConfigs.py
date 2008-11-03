#!/usr/bin/env python

# ConfdbFindParamsFromConfigs.py
# Main file for parsing python configs in a CMSSW release and
# loading templates to the Conf DB.
# 
# Jonathan Hollar LLNL Nov. 3, 2008

import os, string, sys, posix, tokenize, array, getopt
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
    input_usingwhitelist = False
    #    input_whitelist = []
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
    input_dbtype = "Oracle"
    input_addtorelease = "none"
    input_comparetorelease = ""
    input_configflavor = "python"
    

    print "Using release base: " + input_base_path

    confdbjob = ConfdbFindParamsFromConfigs(input_cmsswrel,input_baserelease_path,input_whitelist)
    confdbjob.BeginJob()

class ConfdbFindParamsFromConfigs:
    def __init__(self,clirel,clibasereleasepath,cliwhitelist):

	# Get a Conf DB connection here. Only need to do this once at the 
	# beginning of a job.

	# Deal with package tags for this release.
	self.cmsswrel = clirel
	self.baserelease_path = clibasereleasepath
        self.base_path = clibasereleasepath
        self.whitelist = cliwhitelist

    def BeginJob(self):
	# List of all available modules
	sealcomponenttuple = []

	# Track all modules which will be modified
	modifiedmodules = []

	source_tree = self.base_path + "//src/"

	print "The  source tree is: " + source_tree

        if(not (os.path.isdir(self.base_path))):
            print 'Fatal error: release source tree not found. Exiting now'
            return

	# Get package list for this release
	packagelist = os.listdir(source_tree)

	# Start decending into the source tree
	for package in packagelist:

            if(not (os.path.isdir(self.base_path))):
                print 'Fatal error: release source tree not found. Exiting now'
                return

            if(not package in self.whitelist):
                continue

	    # Check if this is really a directory
	    if(os.path.isdir(source_tree + package)):

		subdirlist = os.listdir(source_tree + package)

		for subdir in subdirlist:
                    if(subdir.startswith(".")):
                       continue
		    # Check if the user really wants to use this package

		    packagedir = source_tree + package + "/" + subdir
		
                    #                    print "Scanning package: " + package + "/" + subdir
                
		    srcdir = packagedir + "/src/"
		    testdir = packagedir + "/test/"
		    pluginsdir = packagedir + "/plugins/"
                    pydir = packagedir + "/python/"

                    if(not os.path.isdir(pydir)):
                        continue
                    
                    pyfiles = os.listdir(pydir)

                    for pyfile in pyfiles:
                        if(pyfile.endswith("_cfi.py")):
                            #                            print "the pyfile = " + str(pyfile)
                            thecomponent = pyfile.split('.py')[0]
                            thebasecomponent = thecomponent.split('_cfi')[0]

                            # Construct the py-cfi to import
                            thesubsystempackage = pydir.split('src/')[1].split('/data/')[0].lstrip().rstrip()
                            thesubsystem = thesubsystempackage.split('/')[0]
                            thepackage = thesubsystempackage.split('/')[1]
                            importcommand = "import " + thesubsystem + "." + thepackage + "." + thecomponent
                            #                            print importcommand
                            process = cms.Process("MyProcess")

                            try:
                                exec importcommand
                                # Now create a process and construct the command to extend it with the py-cfi
                                theextend = "process.extend(" + thesubsystem + "." + thepackage + "." + thecomponent + ")"
                                #                                print theextend
                                eval(theextend)

                                myproducers = process.producers_()
                                self.FindParamsFromPython(thesubsystem, thepackage, myproducers,"EDProducer")

                                myfilters = process.filters_()
                                self.FindParamsFromPython(thesubsystem, thepackage, myfilters,"EDFilter") 

                                myservices = process.services_()
                                self.FindParamsFromPython(thesubsystem, thepackage, myservices,"Service") 

                                myanalyzers = process.analyzers_()
                                self.FindParamsFromPython(thesubsystem, thepackage, myanalyzers,"EDAnalyzer") 
                                                                                                                                                                           
                                myessources = process.es_sources_()
                                self.FindParamsFromPython(thesubsystem, thepackage, myessources,"ESSource") 
                                            
                                myesproducers = process.es_producers_()
                                self.FindParamsFromPython(thesubsystem, thepackage, myesproducers,"ESModule") 

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

    def DoPsetRecursion(self,psetval,psetname):
        params = psetval.parameters_()
        for paramname, paramval in params.iteritems():
            if(paramval.configTypeName() == "PSet" or paramval.configTypeName() == "untracked PSet"):
                self.DoPsetRecursion(paramval,psetname+"."+paramname)
            elif(paramval.configTypeName().find("VPSet") == -1):
                print "\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval)
            else:
                print "\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval.configTypeName()) + "[" + str(len(paramval)) + "]"
                i = 1
                for vpsetentry in paramval:
                    self.DoPsetRecursion(vpsetentry,psetname+'['+str(i)+']')
                    i = i + 1
    
    def FindParamsFromPython(self, thesubsystem, thepackage, mycomponents, componenttype):

        for name, value in mycomponents.iteritems():
            psetname = "TopLevel"
            print "(" + str(componenttype) + ") " + thesubsystem + "." + thepackage + "." + name
            params = value.parameters_()
            for paramname, paramval in params.iteritems():
                if(paramval.configTypeName() == "PSet" or paramval.configTypeName() == "untracked PSet"):
                    self.DoPsetRecursion(paramval,paramname)
                elif(paramval.configTypeName().find("VPSet") == -1):
                    print "\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval)
                else:
                    print "\t\t" + str(paramname) + "\t" + str(paramval.configTypeName()) + "[" + str(len(paramval)) + "]" 
                    i = 1
                    for vpsetentry in paramval:
                        self.DoPsetRecursion(vpsetentry,paramname+'['+str(i)+']')
                        i = i + 1
                                                            
if __name__ == "__main__":
    main(sys.argv[1:])
